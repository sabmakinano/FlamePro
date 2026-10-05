package com.example.flamepro.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderDto {
    @SerializedName("id")
    private int id;

    @SerializedName("order_number")
    private String orderNumber;

    @SerializedName("total_price")
    private String totalPrice;

    @SerializedName("payment_method")
    private String paymentMethod;

    @SerializedName("shipping_address")
    private String shippingAddress;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("items")
    private List<OrderItemDto> items;

    public int getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public String getTotalPrice() { return totalPrice; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getShippingAddress() { return shippingAddress; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public List<OrderItemDto> getItems() { return items; }

    public static class OrderItemDto {
        @SerializedName("product_name")
        private String productName;

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("unit_price")
        private String unitPrice;

        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public String getUnitPrice() { return unitPrice; }
    }
}
