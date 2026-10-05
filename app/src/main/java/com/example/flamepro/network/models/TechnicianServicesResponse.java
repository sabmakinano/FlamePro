package com.example.flamepro.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TechnicianServicesResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("count")
    private int count;

    @SerializedName("services")
    private List<TechnicianServiceDto> services;

    public boolean isSuccess() { return success; }
    public int getCount() { return count; }
    public List<TechnicianServiceDto> getServices() { return services; }
}
