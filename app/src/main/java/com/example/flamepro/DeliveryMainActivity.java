package com.example.flamepro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flamepro.network.ApiClient;
import com.example.flamepro.network.models.DeliveryOrderDto;
import com.example.flamepro.network.models.DeliveryOrdersResponse;
import com.example.flamepro.network.models.SimpleStatusResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryMainActivity extends AppCompatActivity {

    private TextView tvRiderName;
    private TextView tvActiveCount;
    private TextView tvDeliveredCount;
    private RecyclerView rvDeliveryOrders;
    private View layoutEmptyState;
    private DeliveryOrderAdapter adapter;
    private final List<DeliveryOrderDto> orderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delivery_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.deliveryMainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        UserManager.getInstance().loadFromPreferences(this);

        initializeViews();
        setupRecyclerView();
        loadAssignedOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAssignedOrders();
    }

    private void initializeViews() {
        tvRiderName = findViewById(R.id.tvRiderName);
        tvActiveCount = findViewById(R.id.tvActiveCount);
        tvDeliveredCount = findViewById(R.id.tvDeliveredCount);
        rvDeliveryOrders = findViewById(R.id.rvDeliveryOrders);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        String riderName = UserManager.getInstance().getFullName().trim();
        if (riderName.isEmpty()) {
            riderName = UserManager.getInstance().getEmailOrUsername();
        }
        tvRiderName.setText("Rider: " + riderName);

        View btnRefresh = findViewById(R.id.btnRefresh);
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                Toast.makeText(DeliveryMainActivity.this, "Refreshing deliveries...", Toast.LENGTH_SHORT).show();
                loadAssignedOrders();
            });
        }

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to sign out of the Delivery Portal?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        Intent intent = new Intent(DeliveryMainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private int deliveredCountToday = 0;

    private void setupRecyclerView() {
        rvDeliveryOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeliveryOrderAdapter(orderList, new DeliveryOrderAdapter.OnDeliveryActionListener() {
            @Override
            public void onMarkDelivered(DeliveryOrderDto order) {
                showConfirmDeliveredDialog(order);
            }

            @Override
            public void onReschedule(DeliveryOrderDto order) {
                showConfirmRescheduleDialog(order);
            }
        });
        rvDeliveryOrders.setAdapter(adapter);
    }

    private void loadAssignedOrders() {
        int riderId = UserManager.getInstance().getUserId();
        ApiClient.getApiService().getDeliveryOrders(riderId).enqueue(new Callback<DeliveryOrdersResponse>() {
            @Override
            public void onResponse(Call<DeliveryOrdersResponse> call, Response<DeliveryOrdersResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getOrders() != null) {
                    orderList.clear();
                    deliveredCountToday = 0;
                    for (DeliveryOrderDto o : response.body().getOrders()) {
                        if ("Delivered".equalsIgnoreCase(o.getStatus())) {
                            deliveredCountToday++;
                        } else {
                            orderList.add(o);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateCounters();
                } else {
                    layoutEmptyState.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<DeliveryOrdersResponse> call, Throwable t) {
                layoutEmptyState.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
                Toast.makeText(DeliveryMainActivity.this, "Offline or server error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCounters() {
        tvActiveCount.setText(String.valueOf(orderList.size()));
        tvDeliveredCount.setText(String.valueOf(deliveredCountToday));
        layoutEmptyState.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showConfirmDeliveredDialog(DeliveryOrderDto order) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delivery & Payment")
                .setMessage("Have you delivered Order #" + order.getOrderNumber() + " and collected " + order.getTotalPrice() + " (COD)?")
                .setPositiveButton("Yes, Delivered", (dialog, which) -> markOrderDelivered(order))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showConfirmRescheduleDialog(DeliveryOrderDto order) {
        new AlertDialog.Builder(this)
                .setTitle("Reschedule Delivery")
                .setMessage("No one received Order #" + order.getOrderNumber() + "? It will be automatically rescheduled for tomorrow.")
                .setPositiveButton("Reschedule for Tomorrow", (dialog, which) -> markOrderRescheduled(order))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markOrderDelivered(DeliveryOrderDto order) {
        Map<String, Object> body = new HashMap<>();
        body.put("order_id", order.getId());
        body.put("status", "Delivered");
        body.put("rider_id", UserManager.getInstance().getUserId());

        ApiClient.getApiService().updateDeliveryStatus(body).enqueue(new Callback<SimpleStatusResponse>() {
            @Override
            public void onResponse(Call<SimpleStatusResponse> call, Response<SimpleStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(DeliveryMainActivity.this, "Order marked as Delivered & Cash Collected!", Toast.LENGTH_SHORT).show();
                    orderList.remove(order);
                    deliveredCountToday++;
                    adapter.notifyDataSetChanged();
                    updateCounters();
                } else {
                    Toast.makeText(DeliveryMainActivity.this, "Failed to update order status.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SimpleStatusResponse> call, Throwable t) {
                Toast.makeText(DeliveryMainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markOrderRescheduled(DeliveryOrderDto order) {
        Map<String, Object> body = new HashMap<>();
        body.put("order_id", order.getId());
        body.put("status", "Attempted - Rescheduled");

        ApiClient.getApiService().updateDeliveryStatus(body).enqueue(new Callback<SimpleStatusResponse>() {
            @Override
            public void onResponse(Call<SimpleStatusResponse> call, Response<SimpleStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(DeliveryMainActivity.this, "Order rescheduled for tomorrow.", Toast.LENGTH_SHORT).show();
                    order.setStatus("Attempted - Rescheduled");
                    adapter.notifyDataSetChanged();
                    updateCounters();
                } else {
                    Toast.makeText(DeliveryMainActivity.this, "Failed to update order status.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SimpleStatusResponse> call, Throwable t) {
                Toast.makeText(DeliveryMainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
