package com.stephenfong.pepperchat.model.user;

public class User {
    private String userID;
    private String userName;
    private String userPhone;
    private String profileImage;
    private String imageCover;
    private String email;
    private String dateOfBirth;
    private String gender;
    private String status;
    private String bio;

    public User() {
    }

    public User(String userID, String userName, String userPhone, String profileImage, String imageCover,
                String email, String dateOfBirth, String gender, String status, String bio) {
        this.userID = userID;
        this.userName = userName;
        this.userPhone = userPhone;
        this.profileImage = profileImage;
        this.imageCover = imageCover;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.status = status;
        this.bio = bio;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getImageCover() {
        return imageCover;
    }

    public void setImageCover(String imageCover) {
        this.imageCover = imageCover;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
