package com.servicehub.model;

public class ChatResponse {
    private final String reply;
    private final String method;
    private final String timestamp;

    public ChatResponse(String reply, String method) {
        this.reply = reply;
        this.method = method;
        this.timestamp = null;
    }

    public ChatResponse(String reply, String method, String timestamp) {
        this.reply = reply;
        this.method = method;
        this.timestamp = timestamp;
    }

    public String getReply() {
        return reply;
    }

    public String getMethod() {
        return method;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
