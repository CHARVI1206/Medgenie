package com.example.medgenie;

public class NotificationItem {
    private String title;
    private String message;
    private String timestamp;

    // Required for Firestore
    public NotificationItem() {
    }

    public NotificationItem(String title, String message, String timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
