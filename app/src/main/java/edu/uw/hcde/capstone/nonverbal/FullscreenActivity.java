package edu.uw.hcde.capstone.nonverbal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends Activity {
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

    private final int DEFAULT_VIDEO_ID = R.raw.emotions01;
    private final Random random = new Random();
    private TypedArray videos;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        videoView = (VideoView) findViewById(R.id.video_view);

        // Hide System UI by default
        videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        try {
            videos = getResources().obtainTypedArray(R.array.videos);
            DEFAULT_VIDEO_URI = getResourceUri(DEFAULT_VIDEO_ID);
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
                if (counter > threshold) {
                    // pick a random one for demo purposes
                    int id = random.nextInt(videos.length());
                    nextVideoUri = getResourceUri(videos.getResourceId(id, 0));
                }

                if (nextVideoUri == null) {
                    playVideo(DEFAULT_VIDEO_URI);
                    counter++;
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

    private void playVideo(Uri videoToPlay) {
        videoView.setVideoURI(videoToPlay);
        videoView.start();
    }
}
