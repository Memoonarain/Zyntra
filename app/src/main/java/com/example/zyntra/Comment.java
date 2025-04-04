package com.example.zyntra;
public class Comment {
    private String commentId, userId, username, userImg, commentText;
    private long commentTime;

    public Comment() { }

    public Comment(String commentId, String userId, String username, String userImg, String commentText, long commentTime) {
        this.commentId = commentId;
        this.userId = userId;
        this.username = username;
        this.userImg = userImg;
        this.commentText = commentText;
        this.commentTime = commentTime;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public void setCommentTime(long commentTime) {
        this.commentTime = commentTime;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCommentId() { return commentId; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getUserImg() { return userImg; }
    public String getCommentText() { return commentText; }
    public long getCommentTime() { return commentTime; }
}
