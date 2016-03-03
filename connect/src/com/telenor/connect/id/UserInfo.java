package com.telenor.connect.id;

import com.google.gson.annotations.SerializedName;

public class UserInfo {

    private String sub;
    private String name;
    private String email;

    @SerializedName("email_verified")
    private boolean emailVerified;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("phone_number_verified")
    private boolean phoneNumberVerified;

    public UserInfo(
            String sub,
            String name,
            String email,
            boolean emailVerified,
            String phoneNumber,
            boolean phoneNumberVerified) {
        this.sub = sub;
        this.name = name;
        this.email = email;
        this.emailVerified = emailVerified;
        this.phoneNumber = phoneNumber;
        this.phoneNumberVerified = phoneNumberVerified;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isPhoneNumberVerified() {
        return phoneNumberVerified;
    }

    public void setPhoneNumberVerified(boolean phoneNumberVerified) {
        this.phoneNumberVerified = phoneNumberVerified;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "sub='" + sub + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", emailVerified=" + emailVerified +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", phoneNumberVerified=" + phoneNumberVerified +
                '}';
    }
}
