package com.simthesocialrobot.app;

/**
 * Created by jluetke on 7/3/17.
 */

public class Message {

    public static final Message HAPPY = new Message("happy");
    public static final Message IDLE = new Message("idle");
    public static final Message LITTLE_HAPPY = new Message("little happy");
    public static final Message LITTLE_SAD = new Message("little sad");
    public static final Message SAD = new Message("sad");
    public static final Message SLEEP = new Message("sleep");
    public static final Message SWITCH = new Message("switch");

    public static Message parse(String message) {
        return new Message(message);
    }

    private String message;

    private Message(String message) {
        this.message = message;
    }

    public String getMessageString() {
        return message;
    }
}
