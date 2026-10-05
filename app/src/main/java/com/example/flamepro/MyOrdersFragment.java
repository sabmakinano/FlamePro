package com.example.flamepro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flamepro.network.ApiClient;
import com.example.flamepro.network.models.OrderDto;
import com.example.flamepro.network.models.OrderListResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyOrdersFragment extends Fragment {

    private RecyclerView rvOrders;
    private OrderHistoryAdapter adapter;
    private TextView tvOrderCountSub;
    private TextView btnTabAll, btnTabPending, btnTabShipped, btnTabDelivered, btnTabCancelled;
    private View progressBar;

    // Local list populated from backend
    private final List<Order> allOrders = new ArrayList<>();

    private enum TabFilter { ALL, PENDING, SHIPPED, DELIVERED, CANCELLED }
    private TabFilter currentFilter = TabFilter.ALL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvOrderCountSub = view.findViewById(R.id.tvOrderCountSub);
        rvOrders = view.findViewById(R.id.rvOrders);
        progressBar = view.findViewById(R.id.progressOrders);

        btnTabAll = view.findViewById(R.id.btnTabAll);
        btnTabPending = view.findViewById(R.id.btnTabPending);
        btnTabShipped = view.findViewById(R.id.btnTabShipped);
        btnTabDelivered = view.findViewById(R.id.btnTabDelivered);
        btnTabCancelled = view.findViewById(R.id.btnTabCancelled);

        btnTabAll.setOnClickListener(v -> selectTab(TabFilter.ALL));
        btnTabPending.setOnClickListener(v -> selectTab(TabFilter.PENDING));
        btnTabShipped.setOnClickListener(v -> selectTab(TabFilter.SHIPPED));
        btnTabDelivered.setOnClickListener(v -> selectTab(TabFilter.DELIVERED));
        btnTabCancelled.setOnClickListener(v -> selectTab(TabFilter.CANCELLED));

        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh orders from server every time the screen is opened
        fetchOrdersFromBackend();
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter(new ArrayList<>(), this::showCancellationDialog);
        rvOrders.setAdapter(adapter);
    }

    private void fetchOrdersFromBackend() {
        int userId = UserManager.getInstance().getUserId();
        if (userId <= 0) {
            // Not logged in yet, fall back to local orders
            allOrders.clear();
            allOrders.addAll(OrderManager.getInstance().getOrders());
            filterAndPopulateList();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getUserOrders(userId).enqueue(new Callback<OrderListResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderListResponse> call, @NonNull Response<OrderListResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getOrders() != null) {
                    allOrders.clear();
                    for (OrderDto dto : response.body().getOrders()) {
                        allOrders.add(convertDtoToOrder(dto));
                    }
                } else {
                    // Fallback to local
                    allOrders.clear();
                    allOrders.addAll(OrderManager.getInstance().getOrders());
                }
                filterAndPopulateList();
            }

            @Override
            public void onFailure(@NonNull Call<OrderListResponse> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                // Offline: show locally placed orders
                allOrders.clear();
                allOrders.addAll(OrderManager.getInstance().getOrders());
                filterAndPopulateList();
            }
        });
    }

    /**
     * Maps a backend OrderDto to the local Order model used by OrderHistoryAdapter.
     * Maps backend status strings → Order.OrderStatus enum.
     */
    private Order convertDtoToOrder(OrderDto dto) {
        // Build a display title from items
        List<CartItem> fakeItems = new ArrayList<>();
        if (dto.getItems() != null) {
            for (OrderDto.OrderItemDto item : dto.getItems()) {
                // Minimal Product for display purposes only
                Product p = new Product(
                        item.getProductName(),
                        item.getUnitPrice() != null ? item.getUnitPrice() : "₱0",
                        "", "", 0f, 0,
                        R.drawable.logo, null, "", "", "", null, "", true
                );
                fakeItems.add(new CartItem(p, item.getQuantity()));
            }
        }

        Order.OrderStatus status = mapStatus(dto.getStatus());

        // Format date nicely if possible
        String dateDisplay = dto.getCreatedAt() != null ? dto.getCreatedAt().substring(0, 10) : "—";
        String estDelivery = status == Order.OrderStatus.DELIVERED ? "Delivered" :
                             status == Order.OrderStatus.SHIPPED  ? "Out for Delivery" : "Pending";

        return new Order(
                dto.getOrderNumber() != null ? dto.getOrderNumber() : "#" + dto.getId(),
                fakeItems,
                dateDisplay,
                estDelivery,
                dto.getTotalPrice() != null ? dto.getTotalPrice() : "₱0.00",
                status
        );
    }

    private Order.OrderStatus mapStatus(String backendStatus) {
        if (backendStatus == null) return Order.OrderStatus.PENDING;
        switch (backendStatus.toLowerCase(Locale.ROOT)) {
            case "out for delivery":
            case "in transit":
            case "shipping":
            case "shipped":
            case "assigned":
            case "confirmed":
                return Order.OrderStatus.SHIPPED;
            case "delivered":
                return Order.OrderStatus.DELIVERED;
            case "cancelled":
            case "canceled":
                return Order.OrderStatus.CANCELLED;
            default:
                return Order.OrderStatus.PENDING;
        }
    }

    private void showCancellationDialog(Order order) {
        String[] reasons = {
            "Changed my mind",
            "Ordered by mistake",
            "Found a cheaper product",
            "Delivery takes too long",
            "Wrong item/quantity",
            "Seller requested cancellation"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Are you sure you want to cancel order?");
        builder.setSingleChoiceItems(reasons, -1, (dialog, which) -> {});
        builder.setPositiveButton("Yes, Cancel", (dialog, which) -> {
            order.setStatus(Order.OrderStatus.CANCELLED);
            filterAndPopulateList();
            Toast.makeText(getContext(), "Order cancelled successfully", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("No, Keep Order", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void selectTab(TabFilter filter) {
        currentFilter = filter;
        updateTabUI();
        filterAndPopulateList();
    }

    private void updateTabUI() {
        resetTabStyle(btnTabAll);
        resetTabStyle(btnTabPending);
        resetTabStyle(btnTabShipped);
        resetTabStyle(btnTabDelivered);
        resetTabStyle(btnTabCancelled);

        switch (currentFilter) {
            case ALL: setTabSelectedStyle(btnTabAll); break;
            case PENDING: setTabSelectedStyle(btnTabPending); break;
            case SHIPPED: setTabSelectedStyle(btnTabShipped); break;
            case DELIVERED: setTabSelectedStyle(btnTabDelivered); break;
            case CANCELLED: setTabSelectedStyle(btnTabCancelled); break;
        }
    }

    private void resetTabStyle(TextView tab) {
        if (tab == null) return;
        tab.setBackgroundResource(R.drawable.chip_unselected_bg);
        tab.setTextColor(getResources().getColor(R.color.black, null));
        tab.setPadding(convertDpToPx(16), convertDpToPx(8), convertDpToPx(16), convertDpToPx(8));
    }

    private void setTabSelectedStyle(TextView tab) {
        if (tab == null) return;
        tab.setBackgroundResource(R.drawable.chip_selected_bg);
        tab.setTextColor(getResources().getColor(R.color.white, null));
        tab.setPadding(convertDpToPx(24), convertDpToPx(8), convertDpToPx(24), convertDpToPx(8));
    }

    private int convertDpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void filterAndPopulateList() {
        List<Order> displayList;
        if (currentFilter == TabFilter.ALL) {
            displayList = new ArrayList<>(allOrders);
        } else {
            Order.OrderStatus statusMatch;
            switch (currentFilter) {
                case PENDING: statusMatch = Order.OrderStatus.PENDING; break;
                case SHIPPED: statusMatch = Order.OrderStatus.SHIPPED; break;
                case DELIVERED: statusMatch = Order.OrderStatus.DELIVERED; break;
                case CANCELLED: statusMatch = Order.OrderStatus.CANCELLED; break;
                default: statusMatch = Order.OrderStatus.PENDING;
            }
            displayList = new ArrayList<>();
            for (Order o : allOrders) {
                if (o.getStatus() == statusMatch) displayList.add(o);
            }
        }

        adapter.updateOrders(displayList);
        String countText = String.format(Locale.getDefault(), "%d orders placed", displayList.size());
        if (tvOrderCountSub != null) tvOrderCountSub.setText(countText);
    }
}
