package com.servicehub.model;

public class Message {
    private final String text;
    private final boolean isSentByUser;
    private final boolean isTypingIndicator;

    public Message(String text, boolean isSentByUser) {
        this.text = text;
        this.isSentByUser = isSentByUser;
        this.isTypingIndicator = false;
    }

    public Message(String text, boolean isSentByUser, boolean isTypingIndicator) {
        this.text = text;
        this.isSentByUser = isSentByUser;
        this.isTypingIndicator = isTypingIndicator;
    }

    public String getText() {
        return text;
    }

    public boolean isSentByUser() {
        return isSentByUser;
    }

    public boolean isTypingIndicator() {
        return isTypingIndicator;
    }
}
