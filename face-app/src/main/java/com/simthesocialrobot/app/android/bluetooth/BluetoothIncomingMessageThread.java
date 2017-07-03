package com.simthesocialrobot.app.android.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.simthesocialrobot.app.Message;

import java.io.IOException;
import java.io.InputStream;

import edu.uw.hcde.capstone.nonverbal.MainActivity;

/**
 * Created by jluetke on 7/3/17.
 *
 * Refactored from RobotFaceActivity$BTMessageThread in commit 33436453859b9bda115fb1e309009acffea7c7b4
 */
public class BluetoothIncomingMessageThread extends Thread {
    private static final String TAG = MainActivity.BLUETOOTH_SERVICE_NAME;

    private final BluetoothSocket socket;
    private final BluetoothIncomingMessageHandler messageHandler;
    private final InputStream inputStream;
    private byte[] inputBuffer;

    public BluetoothIncomingMessageThread(BluetoothSocket socket, BluetoothIncomingMessageHandler handler) {
        this.socket = socket;
        this.messageHandler = handler;

        InputStream tmpInputStream = null;

        try {
            tmpInputStream = socket.getInputStream();
        }
        catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }

        inputStream = tmpInputStream;
    }

    public void run() {
        inputBuffer = new byte[1024];
        int numBytes;

        while (true) {
            try {
                numBytes = inputStream.read(inputBuffer);
                Log.d(TAG, String.format("Got message: %s", new String(inputBuffer).substring(0, numBytes)));
                messageHandler.onMessageRecieved(Message.parse(new String(inputBuffer, 0, numBytes)));
            }
            catch (IOException e) {
                Log.e(TAG, "Input stream was disconnected", e);
                messageHandler.onInputStreamDisconnected();
                break;
            }
            catch (NullPointerException e) {
                messageHandler.onMessageRecieved(Message.IDLE);
            }
        }
    }

    public void cancel() {
        try {
            socket.close();
        }
        catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}
