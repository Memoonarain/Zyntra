package com.example.zyntra;
import com.google.firebase.Timestamp;


public class Post {
    private String postId;
    private String userId;
    private String userName;
    private String userImageUrl;
    private String postText;
    private String postImageUrl;
    private String postVideoUrl;
    private Timestamp timestamp;

    // Empty constructor for Firebase
    public Post() {}

    public Post(String postId, String userId, String userName, String userImageUrl,
                String postText, String postImageUrl, String postVideoUrl, Timestamp  timestamp) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.userImageUrl = userImageUrl;
        this.postText = postText;
        this.postImageUrl = postImageUrl;
        this.postVideoUrl = postVideoUrl;
        this.timestamp = timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public String getPostVideoUrl() {
        return postVideoUrl;
    }

    public void setPostVideoUrl(String postVideoUrl) {
        this.postVideoUrl = postVideoUrl;
    }

    public Timestamp  getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
// Getters and setters...
}
