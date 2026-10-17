package com.example.flamepro;

import java.util.ArrayList;
import java.util.List;

public class OrderManager {
    private static OrderManager instance;
    private final List<Order> orders;
    private final List<RecentOrderProduct> recentOrders;

    private OrderManager() {
        orders = new ArrayList<>();
        recentOrders = new ArrayList<>();
        initializeRecentOrders();
    }

    private void initializeRecentOrders() {
        // Real user orders will populate recent orders
    }

    public static synchronized OrderManager getInstance() {
        if (instance == null) {
            instance = new OrderManager();
        }
        return instance;
    }

    public List<RecentOrderProduct> getRecentOrders() {
        return recentOrders;
    }

    public void clearRecentOrders() {
        recentOrders.clear();
    }

    public void removeRecentOrder(int position) {
        if (position >= 0 && position < recentOrders.size()) {
            recentOrders.remove(position);
        }
    }

    public void clearOrders() {
        orders.clear();
    }

    public void setOrders(List<Order> newOrders) {
        orders.clear();
        if (newOrders != null) {
            orders.addAll(newOrders);
        }
    }

    public void addOrder(Order order) {
        // Add to the beginning so newest order is at the top
        orders.add(0, order);
        if (order != null && order.getItems() != null) {
            for (CartItem ci : order.getItems()) {
                if (ci != null && ci.getProduct() != null) {
                    recentOrders.add(0, new RecentOrderProduct("FlamePro Official Store", "Order Placed", ci.getProduct()));
                }
            }
        }
    }

    public void syncFromBackend(List<com.example.flamepro.network.models.OrderDto> dtos) {
        if (dtos == null) return;
        orders.clear();
        recentOrders.clear();
        for (com.example.flamepro.network.models.OrderDto dto : dtos) {
            List<CartItem> items = new ArrayList<>();
            if (dto.getItems() != null) {
                for (com.example.flamepro.network.models.OrderDto.OrderItemDto item : dto.getItems()) {
                    Product p = new Product(
                            item.getProductName(),
                            item.getUnitPrice() != null ? item.getUnitPrice() : "₱0",
                            "", "", 5.0f, 100,
                            R.drawable.logo, null, "", "Fire Safety", "", null, "", true
                    );
                    items.add(new CartItem(p, item.getQuantity()));
                    recentOrders.add(new RecentOrderProduct("FlamePro Official Store", dto.getCreatedAt() != null ? dto.getCreatedAt() : dto.getStatus(), p));
                }
            }
            Order.OrderStatus status = Order.OrderStatus.PENDING;
            if (dto.getStatus() != null) {
                String s = dto.getStatus().toLowerCase();
                if (s.contains("deliver")) status = Order.OrderStatus.DELIVERED;
                else if (s.contains("cancel")) status = Order.OrderStatus.CANCELLED;
                else if (s.contains("transit") || s.contains("ship") || s.contains("out")) status = Order.OrderStatus.SHIPPED;
            }
            String dateDisplay = dto.getCreatedAt() != null && dto.getCreatedAt().length() >= 10 ? dto.getCreatedAt().substring(0, 10) : "—";
            orders.add(new Order(
                    dto.getOrderNumber() != null ? dto.getOrderNumber() : "#" + dto.getId(),
                    items,
                    dateDisplay,
                    dto.getStatus(),
                    dto.getTotalPrice() != null ? dto.getTotalPrice() : "₱0.00",
                    status
            ));
        }
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        List<Order> filtered = new ArrayList<>();
        for (Order o : orders) {
            if (o.getStatus() == status) {
                filtered.add(o);
            }
        }
        return filtered;
    }

    public void updateOrderStatus(String orderId, Order.OrderStatus status) {
        if (orderId == null) return;
        for (Order o : orders) {
            if (o.getOrderId() != null && o.getOrderId().equalsIgnoreCase(orderId)) {
                o.setStatus(status);
            }
        }
    }

    public void updateOrderStatus(Order order, Order.OrderStatus status) {
        if (order == null) return;
        order.setStatus(status);
        for (Order o : orders) {
            if ((o.getOrderId() != null && o.getOrderId().equalsIgnoreCase(order.getOrderId()))
                    || (o.getBackendId() > 0 && o.getBackendId() == order.getBackendId())) {
                o.setStatus(status);
            }
        }
    }
}
