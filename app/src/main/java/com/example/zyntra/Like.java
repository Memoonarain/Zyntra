package com.example.zyntra;
public class Like {
    private String userId;
    private long timestamp;

    public Like() {}

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Like(String userId, long timestamp) {
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getters & setters if needed
}
