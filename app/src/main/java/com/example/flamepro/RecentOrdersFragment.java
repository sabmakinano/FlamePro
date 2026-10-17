package com.example.flamepro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecentOrdersFragment extends Fragment implements RecentOrdersAdapter.OnRecentOrderActionListener {

    private View flCartAnim;
    private final List<RecentOrderProduct> recentOrdersList = new ArrayList<>();
    private RecentOrdersAdapter adapter;
    private TextView tvEmptyRecent;
    private RecyclerView rvRecentOrders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recent_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.ivRecentBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        flCartAnim = view.findViewById(R.id.flCartAnim);
        tvEmptyRecent = view.findViewById(R.id.tvEmptyRecent);
        
        // Setup Delete All button
        view.findViewById(R.id.btnDeleteAll).setOnClickListener(v -> showClearHistoryDialog());

        rvRecentOrders = view.findViewById(R.id.rvRecentOrders);
        adapter = new RecentOrdersAdapter(recentOrdersList, this);
        rvRecentOrders.setAdapter(adapter);

        loadRecentOrders();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecentOrders();
    }

    private void loadRecentOrders() {
        int userId = UserManager.getInstance().getUserId();
        if (userId <= 0 && getContext() != null) {
            UserManager.getInstance().loadFromPreferences(getContext());
            userId = UserManager.getInstance().getUserId();
        }

        if (userId > 0) {
            com.example.flamepro.network.ApiClient.getApiService().getUserOrders(userId)
                    .enqueue(new retrofit2.Callback<com.example.flamepro.network.models.OrderListResponse>() {
                        @Override
                        public void onResponse(@NonNull retrofit2.Call<com.example.flamepro.network.models.OrderListResponse> call,
                                               @NonNull retrofit2.Response<com.example.flamepro.network.models.OrderListResponse> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful() && response.body() != null && response.body().getOrders() != null) {
                                OrderManager.getInstance().syncFromBackend(response.body().getOrders());
                            }
                            recentOrdersList.clear();
                            recentOrdersList.addAll(OrderManager.getInstance().getRecentOrders());
                            if (adapter != null) adapter.notifyDataSetChanged();
                            updateEmptyState();
                        }

                        @Override
                        public void onFailure(@NonNull retrofit2.Call<com.example.flamepro.network.models.OrderListResponse> call,
                                              @NonNull Throwable t) {
                            if (!isAdded()) return;
                            recentOrdersList.clear();
                            recentOrdersList.addAll(OrderManager.getInstance().getRecentOrders());
                            if (adapter != null) adapter.notifyDataSetChanged();
                            updateEmptyState();
                        }
                    });
        } else {
            recentOrdersList.clear();
            recentOrdersList.addAll(OrderManager.getInstance().getRecentOrders());
            if (adapter != null) adapter.notifyDataSetChanged();
            updateEmptyState();
        }
    }

    private void showClearHistoryDialog() {
        if (getContext() == null || recentOrdersList.isEmpty()) {
            if (recentOrdersList.isEmpty()) {
                Toast.makeText(getContext(), "History is already empty", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Clear History")
                .setMessage("Are you sure you want to delete all recent purchase history?")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    OrderManager.getInstance().clearRecentOrders();
                    recentOrdersList.clear();
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    Toast.makeText(getContext(), "History cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateEmptyState() {
        if (recentOrdersList.isEmpty()) {
            tvEmptyRecent.setVisibility(View.VISIBLE);
            rvRecentOrders.setVisibility(View.GONE);
        } else {
            tvEmptyRecent.setVisibility(View.GONE);
            rvRecentOrders.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBuyAgain(Product product) {
        // Open the checkout summary dialog directly instead of just adding to cart
        if (getParentFragmentManager() != null) {
            CheckoutBottomSheet.newInstance(product).show(getParentFragmentManager(), "checkout_history");
        }
    }

    @Override
    public void onAddToCart(Product product) {
        CartManager.getInstance().addProduct(product, 1);
        performCartAnimation(flCartAnim);
    }

    @Override
    public void onDeleteOrder(int position) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Remove Order")
                .setMessage("Remove this item from your recent history?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    if (position >= 0 && position < recentOrdersList.size()) {
                        recentOrdersList.remove(position);
                        OrderManager.getInstance().removeRecentOrder(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, adapter.getItemCount());
                        updateEmptyState();
                        Toast.makeText(getContext(), "Item removed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performCartAnimation(View animView) {
        if (animView == null) return;

        animView.setVisibility(View.VISIBLE);
        animView.setScaleX(0f);
        animView.setScaleY(0f);
        animView.setAlpha(1f);

        animView.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> animView.animate()
                            .scaleX(1.8f)
                            .scaleY(1.8f)
                            .alpha(0f)
                            .setDuration(500)
                            .setInterpolator(new AccelerateInterpolator())
                            .withEndAction(() -> animView.setVisibility(View.GONE))
                            .start())
                .start();
    }
}
