package com.app.randomchat.Pojo;

public class OnlineUser {

    String userId;
    String age;

    public OnlineUser() {
    }

    public OnlineUser(String userId, String age) {
        this.userId = userId;
        this.age = age;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAge() {
        return age;
    }

    public void setGender(String age) {
        this.age = age;
    }
}
