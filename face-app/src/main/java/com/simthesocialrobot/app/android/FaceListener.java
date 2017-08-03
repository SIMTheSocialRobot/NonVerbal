package com.simthesocialrobot.app.android;

import android.util.Log;

import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.affectiva.android.affdex.sdk.detector.Detector;

/**
 * Created by jluetke on 7/2/17.
 */

public class FaceListener implements Detector.FaceListener {

    private final String TAG = FaceListener.class.getSimpleName();

    @Override
    public void onFaceDetectionStarted() {
        Log.d(TAG, "onFaceDetectionStarted");
    }

    @Override
    public void onFaceDetectionStopped() {
        Log.d(TAG, "onFaceDetectionStopped");
    }
}
