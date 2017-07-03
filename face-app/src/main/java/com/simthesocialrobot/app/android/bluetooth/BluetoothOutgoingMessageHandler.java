package com.simthesocialrobot.app.android.bluetooth;

/**
 * Created by jluetke on 7/3/17.
 */
public interface BluetoothOutgoingMessageHandler {

    /**
     * Called when an IO Exception is thrown when sending a Bluetooth message, usually signifies that
     * the output Stream was closed.
     */
    public void onOutputStreamDisconnected();

}
