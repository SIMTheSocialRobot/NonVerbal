package com.simthesocialrobot.app.android.bluetooth;

import com.simthesocialrobot.app.Message;

/**
 * Created by jluetke on 7/3/17.
 */
public interface BluetoothOutgoingMessageHandler {

    /**
     * Called when a message has been sent.
     *
     * @param message what was sent
     */
    public void onMessageSent(Message message);

    /**
     * Called when an IO Exception is thrown when sending a Bluetooth message, usually signifies that
     * the output Stream was closed.
     */
    public void onOutputStreamDisconnected();

}
