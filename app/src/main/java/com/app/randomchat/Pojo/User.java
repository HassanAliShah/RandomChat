package com.app.randomchat.Pojo;

public class User extends Super {

    String id;
    String firstName;
    String lastName;
    String email;
    String password;
    String userImageUrl;
    String gender;
    String priority;
    String age;

    String ageUpper;
    String ageLower;

    public User() {

    }

    public User(String id, String firstName, String lastName, String email,
                String password, String userImageUrl, String gender, String priority,
                String age, String ageUpper, String ageLower) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.userImageUrl = userImageUrl;
        this.gender = gender;
        this.priority = priority;
        this.age = age;
        this.ageUpper = ageUpper;
        this.ageLower = ageLower;
    }

    public String getAgeUpper() {
        return ageUpper;
    }

    public void setAgeUpper(String ageUpper) {
        this.ageUpper = ageUpper;
    }

    public String getAgeLower() {
        return ageLower;
    }

    public void setAgeLower(String ageLower) {
        this.ageLower = ageLower;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
