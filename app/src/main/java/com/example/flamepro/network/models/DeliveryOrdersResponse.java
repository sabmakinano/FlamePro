package com.example.flamepro.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DeliveryOrdersResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("count")
    private int count;

    @SerializedName("orders")
    private List<DeliveryOrderDto> orders;

    public boolean isSuccess() { return success; }
    public int getCount() { return count; }
    public List<DeliveryOrderDto> getOrders() { return orders; }
}
