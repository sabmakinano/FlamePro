package com.example.flamepro.network.models;

import com.google.gson.annotations.SerializedName;

public class OrderResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("order")
    private OrderData order;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public OrderData getOrder() { return order; }

    public static class OrderData {
        @SerializedName("id")
        private int id;

        @SerializedName("order_number")
        private String orderNumber;

        @SerializedName("total_price")
        private String totalPrice;

        @SerializedName("status")
        private String status;

        public int getId() { return id; }
        public String getOrderNumber() { return orderNumber; }
        public String getTotalPrice() { return totalPrice; }
        public String getStatus() { return status; }
    }
}
