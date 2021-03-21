package com.app.randomchat.Pojo;

public class UserConHistory extends Super {
    String targetUserId;
    String conId;
    String userImageUrl;
    String userName;
    String lastMessage;

    public UserConHistory() {
    }

    public UserConHistory(String targetUserId, String conId,
                          String userImageUrl, String userName, String lastMessage) {
        this.targetUserId = targetUserId;
        this.conId = conId;
        this.userImageUrl = userImageUrl;
        this.lastMessage = lastMessage;
        this.userName = userName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getConId() {
        return conId;
    }

    public void setConId(String conId) {
        this.conId = conId;
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
}
