package com.example.flamepro.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderListResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("count")
    private int count;

    @SerializedName("orders")
    private List<OrderDto> orders;

    public boolean isSuccess() { return success; }
    public int getCount() { return count; }
    public List<OrderDto> getOrders() { return orders; }
}
