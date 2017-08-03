package com.simthesocialrobot.app.android;

/**
 * Created by jluetke on 7/31/17.
 */

public abstract class Runnable implements java.lang.Runnable {

    protected volatile boolean stop = false;

    public void stop() {
        stop = true;
    }
}
