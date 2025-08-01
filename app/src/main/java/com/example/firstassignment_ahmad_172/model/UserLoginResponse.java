package com.example.firstassignment_ahmad_172.model;

public class UserLoginResponse {
    private boolean success;
    private String message;
    private String token;
    private User user; // ✅ Add this field

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public User getUser() { // ✅ Getter for user object
        return user;
    }
}
