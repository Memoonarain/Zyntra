package com.example.zyntra;

public class NotificationModel {
    String userToken,  title,  body,  senderId,  receiverId,  postId,  type,message;
    Boolean seen;
    long timeStamp;
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSeen() {
        return seen;
    }

    public NotificationModel(String body, String message, String postId, String receiverId, Boolean seen, String senderId, long timeStamp, String title, String type, String userToken) {
        this.body = body;
        this.message = message;
        this.postId = postId;
        this.receiverId = receiverId;
        this.seen = seen;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
        this.title = title;
        this.type = type;
        this.userToken = userToken;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }


    public NotificationModel() {
    }

    public NotificationModel(String body, String postId, String receiverId, String senderId, String title, String type, String userToken) {
        this.body = body;
        this.postId = postId;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.title = title;
        this.type = type;
        this.userToken = userToken;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
}