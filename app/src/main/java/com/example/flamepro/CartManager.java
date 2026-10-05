package com.example.flamepro;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;
    private List<OnCartChangedListener> listeners;

    public interface OnCartChangedListener {
        void onCartChanged(int totalItems);
    }

    private CartManager() {
        cartItems = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addProduct(Product product, int quantity) {
        boolean exists = false;
        for (CartItem item : cartItems) {
            if (item.getProduct().getName().equals(product.getName())) {
                item.setQuantity(item.getQuantity() + quantity);
                exists = true;
                break;
            }
        }
        if (!exists) {
            cartItems.add(new CartItem(product, quantity));
        }
        notifyListeners();
    }

    public void removeProduct(Product product) {
        cartItems.removeIf(item -> item.getProduct().getName().equals(product.getName()));
        notifyListeners();
    }

    public void clearCart() {
        cartItems.clear();
        notifyListeners();
    }

    public void updateQuantity(Product product, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getName().equals(product.getName())) {
                item.setQuantity(quantity);
                if (quantity <= 0) {
                    cartItems.remove(item);
                }
                break;
            }
        }
        notifyListeners();
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public int getTotalItems() {
        int total = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            if (!item.isSelected()) continue;
            
            String priceStr = item.getProduct().getPrice();
            if (priceStr == null) continue;
            
            // Safe removal of currency symbols and whitespace
            priceStr = priceStr.replaceAll("[^0-9.]", "").trim();
            
            try {
                if (!priceStr.isEmpty()) {
                    total += Double.parseDouble(priceStr) * item.getQuantity();
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return total;
    }

    public void addListener(OnCartChangedListener listener) {
        listeners.add(listener);
        listener.onCartChanged(getTotalItems());
    }

    public void removeListener(OnCartChangedListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        int total = getTotalItems();
        for (OnCartChangedListener listener : listeners) {
            listener.onCartChanged(total);
        }
    }
}
