package com.simthesocialrobot.app.android;

import android.graphics.PointF;
import android.util.Log;

import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.simthesocialrobot.app.Affdex;
import com.simthesocialrobot.app.Message;
import com.simthesocialrobot.app.android.bluetooth.BluetoothOutgoingMessageSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uw.hcde.capstone.nonverbal.RobotFaceActivity;

/**
 * Created by jluetke on 7/2/17.
 *
 * Adapted from "testaffectiva.js"
 */

public class EmotionListener implements Detector.ImageListener {

    private final String TAG = EmotionListener.class.getSimpleName();

    private final int TOLERANCE = 96;

    BluetoothOutgoingMessageSender btSender;
    RobotFaceActivity activity;
    Map<Integer, List<Float>> valence = new HashMap<>();

    public EmotionListener(RobotFaceActivity activity, BluetoothOutgoingMessageSender btSender) {
        this.btSender = btSender;
        this.activity = activity;
    }

    @Override
    public void onImageResults(List<Face> faces, Frame image, float timestamp) {
        if (faces.size() <= 0) {
            return;
        }

        //Log.d(TAG, String.format("[%f] Recieved emotional results", timestamp));
        //Log.d(TAG, String.format("[%f] Found %d faces", timestamp, faces.size()));

        // TODO: Track different faces?
        // Force the first face for now
        Face face = faces.get(0);

        // Face tracking
        PointF[] facePoints = face.getFacePoints();
        PointF trackingPoint = facePoints[Affdex.FacePoints.NOSE_ROOT];
        int goalX = image.getWidth() / 2;
        int goalY = image.getHeight() / 2;

        activity.setFacePoints(facePoints, image.getWidth(), image.getHeight());

        int dx = -1 * (int) (trackingPoint.x - goalX);
        int dy = (int) (trackingPoint.y - goalY);

        Log.i(TAG, String.format("trackingPoint.y: %.2f", trackingPoint.y));
        Log.i(TAG, String.format("goalY: %d", goalY));

        // halved because of -/+
        if (Math.abs(dy) <= TOLERANCE / 2) {
            dy = 0;
            Log.i(TAG, "dy is within tolerance range");
        }

        Log.i(TAG, String.format("dy: %d", dy));

//        int tx = (int) (image.getWidth() - trackingPoint.x);
//        int ty = (int) (trackingPoint.y - image.getHeight());
//        int tx = (int) Math.abs(image.getWidth() - trackingPoint.x);
//        int ty = (int) Math.abs(trackingPoint.y - image.getHeight());
        int tx = (int) Math.abs(image.getWidth() - trackingPoint.x);
        int ty = (int) Math.abs(image.getHeight() - trackingPoint.y);




        btSender.sendMessage(Message.createLookMessage(dx, dy, image.getWidth(), image.getHeight()));


        //for (Face face : faces) {
            //Log.i(TAG, String.format("[%f] Face %d appearance: %s ", timestamp, face.getId(), face.appearance));
            //Log.i(TAG, String.format("[%f] Face %d emotion: %s ", timestamp, face.getId(), face.emotions));
            //Log.i(TAG, String.format("[%f] Face %d dominate emote: %s ", timestamp, face.getId(), face.emojis.getDominant()));
            Log.i(TAG, face.emotions.getValence() + "");

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
