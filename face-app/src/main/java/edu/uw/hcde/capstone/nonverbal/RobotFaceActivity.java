package edu.uw.hcde.capstone.nonverbal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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

import com.simthesocialrobot.app.Message;
import com.simthesocialrobot.app.android.bluetooth.BluetoothConnectThread;
import com.simthesocialrobot.app.android.bluetooth.BluetoothConnectionHandler;
import com.simthesocialrobot.app.android.bluetooth.BluetoothIncomingMessageThread;
import com.simthesocialrobot.app.android.bluetooth.BluetoothIncomingMessageHandler;

import java.util.Random;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RobotFaceActivity extends Activity {

    VideoView videoView;
    Uri nextVideoUri;
    TypedArray videos;
    RobotType robotType;
    RobotMode robotMode;

    final Random random = new Random();
    int numIdleExpressions;

    BluetoothAdapter btAdapter;
    BluetoothConnectThread btConnectThread;
    BluetoothIncomingMessageThread btIncomingMessageThread;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        btConnectThread = new BluetoothConnectThread(btAdapter, new BluetoothConnectionHandler() {
            @Override
            public void onBluetoothSocketAvailable(BluetoothSocket socket) {
                if (socket == null) {
                    setResult(MainActivity.BT_CONNECTION_TIMEOUT);
                    finish();
                }
                else {
                    nextVideoUri = getResourceUri(R.raw.happy_s01);

                    btIncomingMessageThread = new BluetoothIncomingMessageThread(socket, new BluetoothIncomingMessageHandler() {
                        @Override
                        public void onInputStreamDisconnected() {
                            finish();
                        }

                        @Override
                        public void onMessageRecieved(Message message) {
                            if (message == null) {
                                return;
                            }

                            if (message.equals(Message.SLEEP)) {
                                robotMode = RobotMode.SLEEP;
                            }
                            else {
                                robotMode = RobotMode.IDLE;
                            }

                            if (message.equals(Message.SWITCH)) {
                                robotType = robotType == RobotType.DUMBOT ? RobotType.SIM : RobotType.DUMBOT;
                                videos = loadVideos();
                                numIdleExpressions = getNumIdleExpressions();
                            }

                            for (int i = 0; i < videos.length(); i++) {
                                String item = videos.getString(i);
                                if (item.toLowerCase().contains(message.getMessageString().toLowerCase())) {
                                    nextVideoUri = getResourceUri(videos.getResourceId(i, 0));
                                    break;
                                }
                            }
                        }
                    });

                    btIncomingMessageThread.start();
                }
            }
        });
        btConnectThread.start();

        Intent intent = getIntent();
        robotType = RobotType.valueOf(intent.getStringExtra(MainActivity.ROBOT_TYPE));
        robotMode = RobotMode.IDLE;
        numIdleExpressions = getNumIdleExpressions();

        setContentView(R.layout.activity_robot_face);
        videoView = (VideoView) findViewById(R.id.video_view);

        // Hide System UI by default
        videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        try {
            videos = loadVideos();
            numIdleExpressions = getNumIdleExpressions();

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
                    if (robotMode == RobotMode.SLEEP) {
                        if (robotType == RobotType.SIM) {
                            playVideo(getResourceUri(R.raw.sleep_s01));
                        }
                        else {
                            playVideo(getResourceUri(R.raw.sleep_d01));
                        }
                    }
                    else {
                        playVideo(chooseRandomIdleExpression());
                    }
                }
                else {
                    playVideo(nextVideoUri);
                    nextVideoUri = null;
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

    private void playVideo(Uri videoToPlay) {
        if (videoToPlay == null) {
            videoToPlay = chooseRandomIdleExpression();
        }
        videoView.setVideoURI(videoToPlay);
        videoView.start();
    }

    private int getNumIdleExpressions() {
        return robotType == RobotType.DUMBOT ?
            getResources().getIntArray(R.array.numIdleExpressions)[0] :
            getResources().getIntArray(R.array.numIdleExpressions)[1];
    }

    private TypedArray loadVideos() {
        return robotType == RobotType.DUMBOT ?
            getResources().obtainTypedArray(R.array.dumbotVideos) :
            getResources().obtainTypedArray(R.array.simVideos);
    }
}
