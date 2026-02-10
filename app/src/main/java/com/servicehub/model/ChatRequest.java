package com.servicehub.model;

public class ChatRequest {
    private final String message;
    private final Integer userId;

    public ChatRequest(String message) {
        this.message = message;
        this.userId = null;
    }

    public ChatRequest(String message, Integer userId) {
        this.message = message;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public Integer getUserId() {
        return userId;
    }
}
