package com.simthesocialrobot.app.android;

import android.content.Context;
import android.view.SurfaceView;

import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.affectiva.android.affdex.sdk.detector.Detector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jluetke on 7/31/17.
 */

public class CameraRunnable extends Runnable {

    private static Logger logger = LoggerFactory.getLogger(CameraRunnable.class);

    final int CAMERA_FPS = 15;

    private CameraDetector detector;

    public CameraRunnable(Context context, SurfaceView cameraPreviewView) {
        detector = new CameraDetector(context, CameraDetector.CameraType.CAMERA_FRONT, cameraPreviewView, 1, Detector.FaceDetectorMode.SMALL_FACES);
        detector.setMaxProcessRate(CAMERA_FPS);
        detector.setDetectAllAppearances(true);
        detector.setDetectAllEmojis(false);
        detector.setDetectAllEmotions(true);
        detector.setDetectAllExpressions(false);
    }

    public void setFaceListener(Detector.FaceListener listener) {
        detector.setFaceListener(listener);
    }

    public void setImageListener(Detector.ImageListener listener) {
        detector.setImageListener(listener);
    }

    public void run() {
        logger.info(String.format("CameraRunnable[%s] starting...", Thread.currentThread().getId()));
        detector.start();
        while (!stop) {}
        detector.stop();
        logger.info(String.format("CameraRunnable[%s] stopped.", Thread.currentThread().getId()));
    }
}
