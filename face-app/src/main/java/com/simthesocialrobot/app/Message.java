package com.simthesocialrobot.app;

/**
 * Created by jluetke on 7/3/17.
 */

public class Message {

    public static final Message HAPPY = new Message("happy");
    public static final Message IDLE = new Message("idle");
    public static final Message LITTLE_HAPPY = new Message("little_happy");
    public static final Message LITTLE_SAD = new Message("little_sad");
    public static final Message SAD = new Message("sad");
    public static final Message SLEEP = new Message("sleep");
    public static final Message SWITCH = new Message("switch");

    public static Message parse(String message) {
        return new Message(message);
    }

    public static Message createLookMessage(int x, int y, int width, int height) {
        return Message.parse(String.format("{\"x\": %d, \"y\": %d, \"w\": %d, \"h\": %d}", x, y, width, height));
    }

    private String message;

    private Message(String message) {
        this.message = message;
    }

    public String getMessageString() {
        return message;
    }

    public boolean equals(Message other) {
        return this.getMessageString().equals(other.getMessageString());
    }
}
