package com.example.flamepro.network.models;

import com.google.gson.annotations.SerializedName;

public class SimpleStatusResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("otp")
    private String otp;

    @SerializedName("email_sent")
    private Boolean emailSent;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getOtp() { return otp; }
    public Boolean isEmailSent() { return emailSent != null && emailSent; }
}
