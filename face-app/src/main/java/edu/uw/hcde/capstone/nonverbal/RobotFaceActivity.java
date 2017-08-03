package edu.uw.hcde.capstone.nonverbal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.VideoView;

import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.simthesocialrobot.app.Message;
import com.simthesocialrobot.app.android.CameraRunnable;
import com.simthesocialrobot.app.android.FacePointsView;
import com.simthesocialrobot.app.android.bluetooth.BluetoothConnectThread;
import com.simthesocialrobot.app.android.bluetooth.BluetoothConnectionHandler;

import com.simthesocialrobot.app.android.EmotionListener;
import com.simthesocialrobot.app.android.FaceListener;
import com.simthesocialrobot.app.android.bluetooth.BluetoothOutgoingMessageHandler;
import com.simthesocialrobot.app.android.bluetooth.BluetoothOutgoingMessageSender;
import com.simthesocialrobot.app.android.bluetooth.BluetoothOutgoingMessageThread;

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
    BluetoothOutgoingMessageThread btOutgoingMessageThread;
    // If a bluetooth connection is established, this will be the outgoing thread
    BluetoothOutgoingMessageSender btMessageSender;

    CameraDetector detector;
    SurfaceView cameraView;
    FacePointsView previewDrawer;

    final int CAMERA_FPS = 5;
    RobotFaceActivity activity = null;

    private CameraRunnable cameraRunnable;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_robot_face);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        cameraView = (SurfaceView) findViewById(R.id.camera_preview_view);
        previewDrawer = (FacePointsView) findViewById(R.id.preview_overlay);

        cameraRunnable = new CameraRunnable(this.getApplicationContext(), cameraView);
        cameraRunnable.setFaceListener(new FaceListener());

        btConnectThread = new BluetoothConnectThread(btAdapter, new BluetoothConnectionHandler() {
            @Override
            public void onBluetoothSocketAvailable(BluetoothSocket socket) {
                if (socket == null) {
                    setResult(MainActivity.BT_CONNECTION_TIMEOUT);
                    finish();
                }
                else {
                    nextVideoUri = getResourceUri(R.raw.happy_s01);

                    btOutgoingMessageThread = new BluetoothOutgoingMessageThread(socket, new BluetoothOutgoingMessageHandler() {
                        @Override
                        public void onMessageSent(Message message) {
                            playVideo(message);
                        }

                        @Override
                        public void onOutputStreamDisconnected() {
                            finish();
                        }
                    });

                    btMessageSender = btOutgoingMessageThread;

                    btOutgoingMessageThread.start();

                    cameraRunnable.setImageListener(new EmotionListener(activity, btOutgoingMessageThread));
                }
            }
        });
        btConnectThread.start();

        btMessageSender = new BluetoothOutgoingMessageSender() {
            @Override
            public void sendMessage(Message message) {
                Log.i("DefaultBTMessageSender", message.getMessageString());
                playVideo(message);
            }
        };

        Intent intent = getIntent();
        robotType = RobotType.valueOf(intent.getStringExtra(MainActivity.ROBOT_TYPE));
        robotMode = RobotMode.IDLE;
        //debugView = intent.getBooleanExtra(MainActivity.DEBUG_VIEW, false);
        numIdleExpressions = getNumIdleExpressions();

        videoView = (VideoView) findViewById(R.id.video_view);

        if (robotType == RobotType.SIM) {
            videoView.setVisibility(View.VISIBLE);
            cameraView.setVisibility(View.VISIBLE);
        }
        else if (robotType == RobotType.MIRROR) {
            videoView.setVisibility(View.GONE);
            cameraView.setVisibility(View.VISIBLE);
        }

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

        Thread cameraThread = new Thread(cameraRunnable, "CameraDetector");
        cameraThread.start();
    }

    public void setFacePoints(PointF[] points, int w, int h) {
        previewDrawer.pH = h;
        previewDrawer.pW = w;
        previewDrawer.points = points;
        previewDrawer.invalidate();
    }

    @Override
    public void finish() {
        if (cameraRunnable != null)
            cameraRunnable.stop();

        if (btOutgoingMessageThread != null)
            btOutgoingMessageThread.interrupt();

        if (btConnectThread != null)
            btConnectThread.interrupt();

        super.finish();
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

    private void playVideo(Message message) {
        if (message.equals(Message.HAPPY)) {
            nextVideoUri = (getResourceUri(R.raw.happy_s02));
        }
        else if (message.equals(Message.IDLE)) {
            nextVideoUri = (chooseRandomIdleExpression());
        }
        else if (message.equals(Message.LITTLE_HAPPY)) {
            nextVideoUri = (getResourceUri(R.raw.happy_s01));
        }
        else if (message.equals(Message.LITTLE_SAD)) {
            nextVideoUri = (getResourceUri(R.raw.sad_s01));
        }
        else if (message.equals(Message.LITTLE_HAPPY)) {
            nextVideoUri = (getResourceUri(R.raw.happy_s01));
        }
        else if (message.equals(Message.SAD)) {
            nextVideoUri = (getResourceUri(R.raw.sad_s02));
        }
        else if (message.equals(Message.SLEEP)) {
            nextVideoUri = (getResourceUri(R.raw.sleep_s01));
        }
        else if (message.equals(Message.SWITCH)) {
            nextVideoUri = (getResourceUri(R.raw.switch_software));
        }
    }

    private int getNumIdleExpressions() {
        return //robotType == RobotType.MIRROR ?
            //getResources().getIntArray(R.array.numIdleExpressions)[0] :
            getResources().getIntArray(R.array.numIdleExpressions)[1];
    }

    private TypedArray loadVideos() {
        return //robotType == RobotType.MIRROR ?
            //getResources().obtainTypedArray(R.array.dumbotVideos) :
            getResources().obtainTypedArray(R.array.simVideos);
    }
}
