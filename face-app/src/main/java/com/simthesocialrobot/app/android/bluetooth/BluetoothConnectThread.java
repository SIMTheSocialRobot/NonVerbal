package com.simthesocialrobot.app.android.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import edu.uw.hcde.capstone.nonverbal.MainActivity;

/**
 * Created by jluetke on 7/3/17.
 *
 * Refactored from RobotFaceActivity$BTConnectThread in commit 33436453859b9bda115fb1e309009acffea7c7b4
 */
public class BluetoothConnectThread extends Thread {

    private static final String TAG = MainActivity.BLUETOOTH_SERVICE_NAME;
    private static final int BT_TIMEOUT = 15000;

    private final BluetoothServerSocket serverSocket;
    private final BluetoothConnectionHandler connectionHandler;

    public BluetoothConnectThread(final BluetoothAdapter btAdapter, final BluetoothConnectionHandler handler) {
        BluetoothServerSocket tmpServerSocket = null;
        try {
            tmpServerSocket = btAdapter.listenUsingInsecureRfcommWithServiceRecord(MainActivity.BLUETOOTH_SERVICE_NAME, UUID.fromString(MainActivity.BLUETOOTH_SERVICE_UUID));
        }
        catch (IOException e) {
            Log.e(TAG, "Failed to listen for connection", e);
        }

        serverSocket = tmpServerSocket;
        connectionHandler = handler;
    }

    public void run() {
        BluetoothSocket socket = null;
        while (true) {
            try {
                socket = serverSocket.accept(BT_TIMEOUT);
                if (connectionHandler != null) {
                    connectionHandler.onBluetoothSocketAvailable(socket);
                }
                else {
                    Log.w(TAG, "No BluetoothConnectionHandler available.");
                }
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
            if (connectionHandler != null) {
                connectionHandler.onBluetoothSocketAvailable(null);
            }
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
