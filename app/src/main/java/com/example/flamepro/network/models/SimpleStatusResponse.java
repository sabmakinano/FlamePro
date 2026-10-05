package com.example.flamepro.network.models;

import com.google.gson.annotations.SerializedName;

public class SimpleStatusResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
