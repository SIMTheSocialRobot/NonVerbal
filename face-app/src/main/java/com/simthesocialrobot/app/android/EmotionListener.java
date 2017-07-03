package com.simthesocialrobot.app.android;

import android.util.Log;

import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.simthesocialrobot.app.Message;
import com.simthesocialrobot.app.android.bluetooth.BluetoothOutgoingMessageSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jluetke on 7/2/17.
 *
 * Adapted from "testaffectiva.js"
 */

public class EmotionListener implements Detector.ImageListener {

    private final String TAG = EmotionListener.class.getSimpleName();

    BluetoothOutgoingMessageSender btSender;
    CameraDetector detector;
    Map<Integer, List<Float>> valence = new HashMap<>();

    public EmotionListener(CameraDetector detector, BluetoothOutgoingMessageSender btSender) {
        this.detector = detector;
        this.btSender = btSender;
    }

    @Override
    public void onImageResults(List<Face> faces, Frame image, float timestamp) {
        if (faces.size() <= 0) {
            return;
        }

        Log.d(TAG, String.format("[%f] Recieved emotional results", timestamp));
        Log.d(TAG, String.format("[%f] Found %d faces", timestamp, faces.size()));

        // TODO: Track different faces?
        // Force the first face for now
        Face face = faces.get(0);

        //for (Face face : faces) {
            Log.i(TAG, String.format("[%f] Face %d appearance: %s ", timestamp, face.getId(), face.appearance));
            Log.i(TAG, String.format("[%f] Face %d emotion: %s ", timestamp, face.getId(), face.emotions));
            Log.i(TAG, String.format("[%f] Face %d dominate emote: %s ", timestamp, face.getId(), face.emojis.getDominant()));

            if (!valence.containsKey(face.getId())) {
                valence.put(face.getId(), new ArrayList<Float>());
            }

            valence.get(face.getId()).add(face.emotions.getValence());

            if(valence.get(face.getId()).size() > 20) {
                double sum = 0.0;
                for(short i = 0; i < valence.get(face.getId()).size(); i++) {
                    sum = sum + valence.get(face.getId()).get(i);
                }

                double avg = sum/20.0;
                // console.log(avg)

                if(avg >= 15 && avg <= 30) {
                    btSender.sendMessage(Message.LITTLE_HAPPY);
                }

                if(avg > 30) {
                    btSender.sendMessage(Message.HAPPY);
                }

                if(avg < -30) {
                    btSender.sendMessage(Message.SAD);
                }

                if(avg <= -15 && avg >= -30){
                    btSender.sendMessage(Message.LITTLE_SAD);
                }

                valence.get(face.getId()).clear();
                sum = 0;
                avg = 0;
            }
        //}
    }
}
