package com.example.flamepro;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private int backendId;
    private String orderId;
    private List<CartItem> items;
    private String orderDate;
    private String estDelivery;
    private String totalPrice;
    private OrderStatus status;
    private boolean isExpanded;

    public enum OrderStatus {
        PENDING("Pending"),
        SHIPPED("Shipped"),
        DELIVERED("Delivered"),
        CANCELLED("Cancelled");

        private String label;
        OrderStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public Order(String orderId, List<CartItem> items, String orderDate, String estDelivery, String totalPrice, OrderStatus status) {
        this(0, orderId, items, orderDate, estDelivery, totalPrice, status);
    }

    public Order(int backendId, String orderId, List<CartItem> items, String orderDate, String estDelivery, String totalPrice, OrderStatus status) {
        this.backendId = backendId;
        this.orderId = orderId;
        this.items = items;
        this.orderDate = orderDate;
        this.estDelivery = estDelivery;
        this.totalPrice = totalPrice;
        this.status = status;
        this.isExpanded = false;
    }

    public int getBackendId() { return backendId; }
    public void setBackendId(int backendId) { this.backendId = backendId; }
    public String getOrderId() { return orderId; }
    public List<CartItem> getItems() { return items; }
    public String getOrderDate() { return orderDate; }
    public String getEstDelivery() { return estDelivery; }
    public String getTotalPrice() { return totalPrice; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { this.isExpanded = expanded; }

    public static OrderStatus mapStatus(String backendStatus) {
        if (backendStatus == null) return OrderStatus.PENDING;
        switch (backendStatus.toLowerCase(java.util.Locale.ROOT)) {
            case "out for delivery":
            case "in transit":
            case "shipping":
            case "shipped":
            case "assigned":
            case "confirmed":
                return OrderStatus.SHIPPED;
            case "delivered":
                return OrderStatus.DELIVERED;
            case "cancelled":
            case "canceled":
                return OrderStatus.CANCELLED;
            default:
                return OrderStatus.PENDING;
        }
    }

    public static Order fromDto(com.example.flamepro.network.models.OrderDto dto) {
        if (dto == null) return null;
        java.util.List<CartItem> fakeItems = new java.util.ArrayList<>();
        if (dto.getItems() != null) {
            for (com.example.flamepro.network.models.OrderDto.OrderItemDto item : dto.getItems()) {
                Product p = new Product(
                        item.getProductName(),
                        item.getUnitPrice() != null ? item.getUnitPrice() : "₱0",
                        "", "", 0f, 0,
                        R.drawable.logo, null, "", "", "", null, "", true
                );
                fakeItems.add(new CartItem(p, item.getQuantity()));
            }
        }

        OrderStatus status = mapStatus(dto.getStatus());
        String dateDisplay = dto.getCreatedAt() != null && dto.getCreatedAt().length() >= 10 
                ? dto.getCreatedAt().substring(0, 10) : (dto.getCreatedAt() != null ? dto.getCreatedAt() : "—");
        String estDelivery = status == OrderStatus.DELIVERED ? "Delivered" :
                             status == OrderStatus.SHIPPED  ? "Out for Delivery" : "Pending";

        return new Order(
                dto.getId(),
                dto.getOrderNumber() != null ? dto.getOrderNumber() : "#" + dto.getId(),
                fakeItems,
                dateDisplay,
                estDelivery,
                dto.getTotalPrice() != null ? dto.getTotalPrice() : "₱0.00",
                status
        );
    }
}
