package com.simthesocialrobot.app.android.bluetooth;

import com.simthesocialrobot.app.Message;

/**
 * Created by jluetke on 7/3/17.
 */
public interface BluetoothIncomingMessageHandler {

    /**
     * Called when an IO Exception is thrown when reading a Bluetooth message, usually significes that
     * the Input Stream was closed.
     */
    public void onInputStreamDisconnected();

    /**
     * Called when a new Message is recieved via Bluetooth.
     *
     * @param message the Message
     */
    public void onMessageRecieved(Message message);
}
