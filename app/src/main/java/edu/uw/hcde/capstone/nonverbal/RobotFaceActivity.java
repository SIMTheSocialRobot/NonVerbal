package edu.uw.hcde.capstone.nonverbal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.VideoView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RobotFaceActivity extends Activity {

    VideoView videoView;
    Uri nextVideoUri;
    TypedArray videos;
    RobotMode mode;

    final Random random = new Random();
    int numIdleExpressions;

    BluetoothAdapter btAdapter;
    BTConnectThread btConnectThread;
    BTMessageThread btMessageThread;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent = getIntent();
        mode = RobotMode.valueOf(intent.getStringExtra(MainActivity.ROBOT_MODE));

        btConnectThread = new BTConnectThread();
        btConnectThread.start();

        setContentView(R.layout.activity_robot_face);
        videoView = (VideoView) findViewById(R.id.video_view);

        // Hide System UI by default
        videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        try {
            if (mode == RobotMode.DUMBOT) {
                videos = getResources().obtainTypedArray(R.array.vidoes_dumbot);
                numIdleExpressions = 1;
            }
            else {
                videos = getResources().obtainTypedArray(R.array.videos_sim);
                numIdleExpressions = 4;
            }

            videoView.requestFocus();
            playVideo(chooseRandomIdleExpression());
        }
        catch (Exception e) {
            Log.e("Video loading", e.getMessage());
            e.printStackTrace();
        }

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer player) {
                videoView.seekTo(0);
                videoView.start();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer player) {
                if (nextVideoUri == null) {
                    playVideo(chooseRandomIdleExpression());
                }
                else {
                    playNextVideo();
                }

            }
        });

        videoView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (videoView.equals(v) && (videoView.getSystemUiVisibility() & VideoView.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
                    videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                }
                return true;
            }
        });
    }

    private Uri getResourceUri(int id) {
        return Uri.parse("android.resource://" + getPackageName() + "/" + id);
    }

    private Uri chooseRandomIdleExpression() {
        int id = random.nextInt(numIdleExpressions);
        Uri video = getResourceUri(videos.getResourceId(id, 0));
        return video;
    }

    private void playNextVideo() {
        playVideo(nextVideoUri);
        nextVideoUri = null;
    }

    private void playVideo(Uri videoToPlay) {
        if (videoToPlay == null) {
            videoToPlay = chooseRandomIdleExpression();
        }
        videoView.setVideoURI(videoToPlay);
        videoView.start();
    }

    private void onBluetoothSocketAvailable(BluetoothSocket socket) {
        if (socket == null) {
            setResult(MainActivity.BT_CONNECTION_TIMEOUT);
            finish();
        }
        else {
            btMessageThread = new BTMessageThread(socket);
            btMessageThread.start();
        }
    }

    private class BTConnectThread extends Thread {
        private static final String TAG = MainActivity.BLUETOOTH_SERVICE_NAME;
        private static final int BT_TIMEOUT = 15000;

        private final BluetoothServerSocket serverSocket;

        public BTConnectThread() {
            BluetoothServerSocket tmpServerSocket = null;
            try {
                tmpServerSocket = btAdapter.listenUsingInsecureRfcommWithServiceRecord(MainActivity.BLUETOOTH_SERVICE_NAME, UUID.fromString(MainActivity.BLUETOOTH_SERVICE_UUID));
            }
            catch (IOException e) {
                Log.e(TAG, "Failed to listen for connection", e);
            }

            serverSocket = tmpServerSocket;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = serverSocket.accept(BT_TIMEOUT);
                    onBluetoothSocketAvailable(socket);
                }
                catch (IOException e) {
                    Log.e(TAG, "Server Socket's accept() method failed", e);
                    break;
                }
                if (socket != null) {
                    try {
                        serverSocket.close();
                        break;
                    } catch (IOException e) {
                        Log.e(TAG, "Server Socket's close() method failed", e);
                        break;
                    }
                }
            }

            if (socket == null) {
                onBluetoothSocketAvailable(null);
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            }
            catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    public Uri setNextVideoFromMessage(String message) {
        for (int i = 0; i < videos.length(); i++) {
            String item = videos.getString(i);
            if (item.toLowerCase().contains(message.toLowerCase())) {
                nextVideoUri = getResourceUri(videos.getResourceId(i, 0));
                break;
            }
        }

        return nextVideoUri;
    }

    private class BTMessageThread extends Thread {
        private static final String TAG = MainActivity.BLUETOOTH_SERVICE_NAME;

        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private byte[] buffer;

        public BTMessageThread(BluetoothSocket socket) {
            this.socket = socket;

            InputStream tmpInputStream = null;

            try {
                tmpInputStream = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }

            inputStream = tmpInputStream;
        }

        public void run() {
            buffer = new byte[1024];
            int numBytes;

            while (true) {
                try {
                    numBytes = inputStream.read(buffer);
                    Log.d(TAG, String.format("Got message: %s", new String(buffer).substring(0, numBytes)));
                    setNextVideoFromMessage(new String(buffer, 0, numBytes));
                }
                catch (IOException e) {
                    Log.e(TAG, "Input stream was disconnected", e);
                    finish();
                    break;
                }
                catch (NullPointerException e) {
                    setNextVideoFromMessage("idle");
                }
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

}
