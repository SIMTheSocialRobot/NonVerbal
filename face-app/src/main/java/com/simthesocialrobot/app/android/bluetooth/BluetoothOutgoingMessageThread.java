package com.simthesocialrobot.app.android.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.simthesocialrobot.app.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import edu.uw.hcde.capstone.nonverbal.MainActivity;

/**
 * Created by jluetke on 7/3/17.
 */
public class BluetoothOutgoingMessageThread extends Thread implements BluetoothOutgoingMessageSender {
    private static final String TAG = MainActivity.BLUETOOTH_SERVICE_NAME;

    private final BluetoothSocket socket;
    private final BluetoothOutgoingMessageHandler messageHandler;
    private final OutputStream outputStream;
    private byte[] outputBuffer;
    private String outgoingMessage;

    public BluetoothOutgoingMessageThread(BluetoothSocket socket, BluetoothOutgoingMessageHandler handler) {
        this.socket = socket;
        this.messageHandler = handler;
        outputBuffer = new byte[1024];

        OutputStream tmpOutputStream = null;

        try {
            tmpOutputStream = socket.getOutputStream();
        }
        catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        outputStream = tmpOutputStream;
    }

    public void sendMessage(Message message) {
        Log.i(TAG, String.format("Sending message: %s", message.getMessageString()));
        outgoingMessage = message.getMessageString();
    }

    public void run() {
        int numBytes;

        while (true) {
            try {
                if (outgoingMessage != null && !outgoingMessage.isEmpty()) {
                    numBytes = outgoingMessage.getBytes(Charset.forName("ascii")).length;
                    outputBuffer = outgoingMessage.getBytes(Charset.forName("ascii"));
                    outgoingMessage = null;

                    outputStream.write(outputBuffer);
                    Log.d(TAG, String.format("Sent message: %s", new String(outputBuffer).substring(0, numBytes)));
                }
            }
            catch (IOException e) {
                Log.e(TAG, "Output stream was disconnected", e);
                messageHandler.onOutputStreamDisconnected();
                break;
            }
            catch (NullPointerException e) {
                Log.e(TAG, "NPE when sending message", e);
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
