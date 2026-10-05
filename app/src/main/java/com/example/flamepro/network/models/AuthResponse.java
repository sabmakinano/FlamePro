package com.example.flamepro.network.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private UserModel user;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public UserModel getUser() { return user; }
}
