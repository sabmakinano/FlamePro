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
        Product p1 = new Product("HALO Smart Smoke Detector", "₱ 2,499.00", "", "", 4.8f, 100, R.drawable.ic_check_circle, null, "White", "Smart Sensor", "", null, "", true);
        Product p2 = new Product("UGREEN PD 20W Fast Charger", "₱ 703.48", "", "", 4.7f, 50, R.drawable.ic_lightning, null, "White + 1M Cable", "Charger", "", null, "", true);
        Product p3 = new Product("Industrial Safety Helmet", "₱ 980.00", "", "", 4.9f, 30, R.drawable.ic_fire_type, null, "Yellow", "PPE", "", null, "", true);
        Product p4 = new Product("Heavy-Duty Rescue Rope", "₱ 2,150.00", "", "", 4.6f, 20, R.drawable.ic_coverage, null, "30m", "Safety Gear", "", null, "", true);

        recentOrders.add(new RecentOrderProduct("HaloSafetyShop", "1:17 PM Delivered", p1));
        recentOrders.add(new RecentOrderProduct("UgreenOfficialShop", "1:17 PM Delivered", p2));
        recentOrders.add(new RecentOrderProduct("ArmorGuard Off", "Seller preparing package", p3));
        recentOrders.add(new RecentOrderProduct("ClimbSafe Gear", "In Transit", p4));
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

    public void addOrder(Order order) {
        // Add to the beginning so newest order is at the top
        orders.add(0, order);
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
}
