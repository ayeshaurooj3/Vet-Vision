package com.example.firstassignment_ahmad_172.model;
import com.google.gson.annotations.SerializedName;

public class User {
    private String name;
    private String email;

    @SerializedName("id")  // Changed from "_id" to "id"
    private String id;

    // Keep other fields and methods as they are
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    // Add toString() for better debugging
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}