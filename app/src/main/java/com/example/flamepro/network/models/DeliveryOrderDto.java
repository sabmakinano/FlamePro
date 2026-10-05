package com.example.flamepro.network.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class DeliveryOrderDto implements Serializable {
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

    @SerializedName("delivery_status")
    private String deliveryStatus;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("customer_phone")
    private String customerPhone;

    @SerializedName("items")
    private List<DeliveryItemDto> items;

    public int getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public String getTotalPrice() { return totalPrice; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getShippingAddress() { return shippingAddress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public String getCreatedAt() { return createdAt; }
    public String getCustomerName() { 
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""); 
    }
    public String getCustomerPhone() { return customerPhone; }
    public List<DeliveryItemDto> getItems() { return items; }

    public static class DeliveryItemDto implements Serializable {
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
