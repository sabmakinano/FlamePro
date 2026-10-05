package com.example.flamepro.network.models;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("username_or_email")
    private String usernameOrEmail;

    @SerializedName("password")
    private String password;

    public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    public String getUsernameOrEmail() { return usernameOrEmail; }
    public String getPassword() { return password; }
}
