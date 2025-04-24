package com.example.zyntra;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Chat {
    private String chatId;
    private String user1Id;
    private String user2Id;
    private String lastMessage;
    private @ServerTimestamp Date lastMessageTimestamp;
    private String lastMessageSenderId;
    private boolean seen;
    
    // User info fields (to avoid extra queries)
    private String otherUserName;
    private String otherUserProfileImage;

    // Empty constructor for Firestore
    public Chat() {
    }

    public Chat(String chatId, String user1Id, String user2Id) {
        this.chatId = chatId;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.lastMessage = "";
        this.seen = true;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(String user1Id) {
        this.user1Id = user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(String user2Id) {
        this.user2Id = user2Id;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Date getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Date lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserProfileImage() {
        return otherUserProfileImage;
    }

    public void setOtherUserProfileImage(String otherUserProfileImage) {
        this.otherUserProfileImage = otherUserProfileImage;
    }
} 