package com.simthesocialrobot.app.android.bluetooth;

import android.bluetooth.BluetoothSocket;

/**
 * Created by jluetke on 7/3/17.
 */
public interface BluetoothConnectionHandler {

    /**
     * Called when a BluetoothSocket is either available for communication or is unable to be opened.
     * If {@code socket} is {@code null}, a socket could not be opened, and this handler should
     * exit the corresponding activity and inform the user.
     *
     * @param socket the available socket, or null
     */
    public void onBluetoothSocketAvailable(BluetoothSocket socket);

}
