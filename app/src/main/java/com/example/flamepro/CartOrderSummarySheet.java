package com.example.flamepro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import com.example.flamepro.network.ApiClient;
import com.example.flamepro.network.models.OrderRequest;
import com.example.flamepro.network.models.OrderResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shows a full order breakdown (each item × price, subtotal, delivery fee, total)
 * before the user confirms checkout from the cart.
 */
public class CartOrderSummarySheet extends BottomSheetDialogFragment {

    private static final double DELIVERY_FEE = 9.99;

    public static CartOrderSummarySheet newInstance() {
        return new CartOrderSummarySheet();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null) {
            view.post(() -> {
                View parent = (View) view.getParent();
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
                behavior.setFitToContents(false);
                behavior.setExpandedOffset(0);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                ViewGroup.LayoutParams lp = parent.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                parent.setLayoutParams(lp);
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_cart_summary_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get selected items from cart
        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : CartManager.getInstance().getCartItems()) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }

        // References
        LinearLayout llItemsContainer = view.findViewById(R.id.llItemsContainer);
        TextView tvSummaryItemCount  = view.findViewById(R.id.tvSummaryItemCount);
        TextView tvSummarySubtotal   = view.findViewById(R.id.tvSummarySubtotal);
        TextView tvSummaryDelivery   = view.findViewById(R.id.tvSummaryDelivery);
        TextView tvSummaryTotal      = view.findViewById(R.id.tvSummaryTotal);
        MaterialButton btnConfirm    = view.findViewById(R.id.btnConfirmOrder);
        MaterialButton btnCancel     = view.findViewById(R.id.btnCancelSummary);

        view.findViewById(R.id.btnBackSummary).setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());

        // Build item rows dynamically
        double subtotal = 0.0;
        int totalQty = 0;

        for (CartItem ci : selectedItems) {
            String priceStr = ci.getProduct().getPrice().replaceAll("[^0-9.]", "").trim();
            double unitPrice = 0;
            try { unitPrice = Double.parseDouble(priceStr); } catch (NumberFormatException ignored) {}
            double lineTotal = unitPrice * ci.getQuantity();
            subtotal += lineTotal;
            totalQty += ci.getQuantity();

            // Inflate item row
            View itemRow = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_order_summary_row, llItemsContainer, false);

            TextView tvItemName  = itemRow.findViewById(R.id.tvSummaryRowName);
            TextView tvItemQty   = itemRow.findViewById(R.id.tvSummaryRowQty);
            TextView tvItemPrice = itemRow.findViewById(R.id.tvSummaryRowPrice);

            tvItemName.setText(ci.getProduct().getName());
            tvItemQty.setText("x" + ci.getQuantity());
            tvItemPrice.setText(String.format(Locale.getDefault(), "₱ %.2f", lineTotal));

            llItemsContainer.addView(itemRow);
        }

        double total = subtotal + DELIVERY_FEE;

        tvSummaryItemCount.setText(totalQty + (totalQty == 1 ? " item" : " items"));
        tvSummarySubtotal.setText(String.format(Locale.getDefault(), "₱ %.2f", subtotal));
        tvSummaryDelivery.setText(String.format(Locale.getDefault(), "₱ %.2f", DELIVERY_FEE));
        tvSummaryTotal.setText(String.format(Locale.getDefault(), "₱ %.2f", total));

        String finalTotalStr = String.format(Locale.getDefault(), "₱ %.2f", total);
        btnConfirm.setText("Confirm Order • " + finalTotalStr);

        btnConfirm.setOnClickListener(v -> {
            // Save order
            String estDelivery = OrderSuccessFragment.calculateDeliveryDateString();
            TimeZone tz = TimeZone.getTimeZone("Asia/Manila");
            Calendar cal = Calendar.getInstance(tz);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.ENGLISH);
            sdf.setTimeZone(tz);
            String orderDateStr = sdf.format(cal.getTime());
            String orderId = "ORD-" + (4000 + new Random().nextInt(5000));

            if (!selectedItems.isEmpty()) {
                Order newOrder = new Order(orderId, selectedItems, orderDateStr, estDelivery,
                        finalTotalStr, Order.OrderStatus.PENDING);
                OrderManager.getInstance().addOrder(newOrder);

                // Call Backend API to persist order
                int userId = UserManager.getInstance().getUserId();
                String address = UserManager.getInstance().getAddress();
                if (address == null || address.trim().isEmpty()) {
                    address = "Customer Address";
                }
                List<OrderRequest.OrderItemRequest> apiItems = new ArrayList<>();
                for (CartItem ci : selectedItems) {
                    apiItems.add(new OrderRequest.OrderItemRequest(null, ci.getProduct().getName(), ci.getQuantity(), ci.getProduct().getPrice()));
                }

                OrderRequest orderReq = new OrderRequest(userId > 0 ? userId : null, finalTotalStr, "Cash on Delivery", address, UserManager.getInstance().getFullName(), apiItems);
                ApiClient.getApiService().createOrder(orderReq).enqueue(new Callback<OrderResponse>() {
                    @Override
                    public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {}
                    @Override
                    public void onFailure(Call<OrderResponse> call, Throwable t) {}
                });

                // Remove purchased items from cart
                for (CartItem ci : selectedItems) {
                    CartManager.getInstance().removeProduct(ci.getProduct());
                }
            }

            dismiss();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(
                        OrderSuccessFragment.newInstance(finalTotalStr, estDelivery));
            }
        });
    }
}
