package com.app.randomchat.Pojo;

public class OnlineUser {

    String userId;
    String gender;

    public OnlineUser() {
    }

    public OnlineUser(String userId, String gender) {
        this.userId = userId;
        this.gender = gender;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
