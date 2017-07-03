package com.simthesocialrobot.app.android.bluetooth;

import com.simthesocialrobot.app.Message;

/**
 * Created by jluetke on 7/3/17.
 */
public interface BluetoothOutgoingMessageSender {

    /**
     * Facilitates sending a message via Bluetooth.
     *
     * @param message the message to send
     */
    public void sendMessage(Message message);
}
