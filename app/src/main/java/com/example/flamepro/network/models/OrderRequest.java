package com.example.flamepro.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderRequest {
    @SerializedName("user_id")
    private Integer userId;

    @SerializedName("total_price")
    private String totalPrice;

    @SerializedName("payment_method")
    private String paymentMethod;

    @SerializedName("shipping_address")
    private String shippingAddress;

    @SerializedName("customer_name")
    private String customerName;

    @SerializedName("items")
    private List<OrderItemRequest> items;

    public OrderRequest(Integer userId, String totalPrice, String paymentMethod, String shippingAddress, String customerName, List<OrderItemRequest> items) {
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.shippingAddress = shippingAddress;
        this.customerName = customerName;
        this.items = items;
    }

    public static class OrderItemRequest {
        @SerializedName("product_id")
        private Integer productId;

        @SerializedName("product_name")
        private String productName;

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("unit_price")
        private String unitPrice;

        public OrderItemRequest(Integer productId, String productName, int quantity, String unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }
}
