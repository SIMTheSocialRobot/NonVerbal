package edu.uw.hcde.capstone.nonverbal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 0;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private VideoView videoView;
    private Uri DEFAULT_VIDEO_URI;
    private Uri nextVideoUri;
    private int counter = 0, threshold = 1;

    private final Random random = new Random();
    private TypedArray videos;

    BluetoothAdapter btAdapter;
    BluetoothDevice btControlDevice;
    BluetoothSocket btSocket;
    BTConnectThread btConnectThread;
    BTMessageThread btMessageThread;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent = getIntent();
        btControlDevice = btAdapter.getRemoteDevice(intent.getStringExtra(MainActivity.BLUETOOTH_CONTROL_DEVICE_ADDRESS));

        btConnectThread = new BTConnectThread();
        btConnectThread.start();

        setContentView(R.layout.activity_robot_face);
        videoView = (VideoView) findViewById(R.id.video_view);

        // Hide System UI by default
        videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        try {
            videos = getResources().obtainTypedArray(R.array.videos);
            DEFAULT_VIDEO_URI = chooseRandomIdleExpression();
            videoView.requestFocus();
            playVideo(DEFAULT_VIDEO_URI);
        } catch (Exception e) {
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
                    playVideo(nextVideoUri);
                    nextVideoUri = null;
                    counter = 0;
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
        // Assumes the first X are the idle expressions
        int id = random.nextInt(4);
        return getResourceUri(videos.getResourceId(id, 0));
    }

    private void playVideo(Uri videoToPlay) {
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

    public void setNextVideoFromMessage(String message) {
        for (int i = 0; i < videos.length(); i++) {
            String item = videos.getString(i);
            if (item.toLowerCase().contains(message.toLowerCase())) {
                nextVideoUri = getResourceUri(videos.getResourceId(i, 0));
            }
        }
    }

    private class BTMessageThread extends Thread {
        private static final String TAG = MainActivity.BLUETOOTH_SERVICE_NAME;

        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private byte[] buffer;
        private Handler msgHandler;

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
                    processMessage(new String(buffer, 0, numBytes));
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        protected void processMessage(String message) {
            Log.i(TAG, message);
            setNextVideoFromMessage(message);
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
