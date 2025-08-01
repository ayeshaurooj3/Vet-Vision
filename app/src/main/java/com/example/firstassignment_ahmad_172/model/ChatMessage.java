package com.example.firstassignment_ahmad_172.model;

public class ChatMessage {
    private String message;
    private String imageUrl;
    private boolean isUserMessage;

    // Constructor for text messages
    public ChatMessage(String message, boolean isUserMessage) {
        this.message = message;
        this.isUserMessage = isUserMessage;
        this.imageUrl = null;
    }

    // Constructor for image messages
    public ChatMessage(String imageUrl, boolean isUserMessage, boolean isImage) {
        this.imageUrl = imageUrl;
        this.isUserMessage = isUserMessage;
        this.message = null;  // Set text to null for image messages
    }

    public String getMessage() {
        return message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }

    public boolean isImage() {
        return imageUrl != null;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
