package com.example.flamepro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.flamepro.network.ApiClient;
import com.example.flamepro.network.models.OrderDto;
import com.example.flamepro.network.models.OrderListResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EquipmentTrackerBottomSheet extends BottomSheetDialogFragment {

    private LinearLayout llEquipmentList;
    private ProgressBar pbEquipment;
    private TextView tvEmptyEquipment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_equipment_tracker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnBackEquipment).setOnClickListener(v -> dismiss());
        llEquipmentList = view.findViewById(R.id.llEquipmentList);
        pbEquipment = view.findViewById(R.id.pbEquipment);
        tvEmptyEquipment = view.findViewById(R.id.tvEmptyEquipment);

        loadPurchasedExtinguishers();
    }

    private void loadPurchasedExtinguishers() {
        int userId = UserManager.getInstance().getUserId();
        if (userId <= 0) {
            tvEmptyEquipment.setVisibility(View.VISIBLE);
            return;
        }

        pbEquipment.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getUserOrders(userId).enqueue(new Callback<OrderListResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderListResponse> call, @NonNull Response<OrderListResponse> response) {
                pbEquipment.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getOrders() != null) {
                    populateItems(response.body().getOrders());
                } else {
                    tvEmptyEquipment.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderListResponse> call, @NonNull Throwable t) {
                pbEquipment.setVisibility(View.GONE);
                tvEmptyEquipment.setVisibility(View.VISIBLE);
            }
        });
    }

    private void populateItems(List<OrderDto> orders) {
        llEquipmentList.removeAllViews();
        int count = 0;

        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

        for (OrderDto order : orders) {
            if (order.getItems() == null) continue;

            for (OrderDto.OrderItemDto item : order.getItems()) {
                count++;
                View card = LayoutInflater.from(requireContext()).inflate(R.layout.item_delivery_order, llEquipmentList, false);

                TextView tvName = card.findViewById(R.id.tvOrderNumber);
                TextView tvStatus = card.findViewById(R.id.tvOrderStatus);
                TextView tvCustomer = card.findViewById(R.id.tvCustomerName);
                TextView tvAddress = card.findViewById(R.id.tvCustomerAddress);
                TextView tvPhone = card.findViewById(R.id.tvCustomerPhone);
                TextView tvSummary = card.findViewById(R.id.tvItemsSummary);
                TextView tvTotal = card.findViewById(R.id.tvTotalAmount);
                MaterialButton btnDeliver = card.findViewById(R.id.btnMarkDelivered);
                MaterialButton btnReschedule = card.findViewById(R.id.btnReschedule);

                if (btnReschedule != null) btnReschedule.setVisibility(View.GONE);

                String prodName = item.getProductName() != null ? item.getProductName() : "Fire Extinguisher";
                tvName.setText("🔥 " + prodName);
                tvCustomer.setText("Unit Qty: " + item.getQuantity());

                // Calculate Purchase Date and 1-Year Expiration
                String purchaseDateStr = order.getCreatedAt() != null && order.getCreatedAt().length() >= 10
                        ? order.getCreatedAt().substring(0, 10) : "";
                String expireDateStr = "1 Year from Purchase";

                try {
                    Date pDate = inFormat.parse(purchaseDateStr);
                    if (pDate != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(pDate);
                        cal.add(Calendar.YEAR, 1);
                        expireDateStr = outFormat.format(cal.getTime());
                        purchaseDateStr = outFormat.format(pDate);
                    }
                } catch (Exception ignored) {}

                tvAddress.setText("Purchased: " + purchaseDateStr);
                tvPhone.setText("Refill Due: " + expireDateStr);
                tvSummary.setText("Safety Standard: Requires refilling / pressure check every 12 months.");
                tvTotal.setText(item.getUnitPrice());

                tvStatus.setText("Active (Certified)");
                tvStatus.setBackgroundResource(R.drawable.badge_delivered);

                btnDeliver.setText("Book Refill");
                btnDeliver.setBackgroundTintList(requireContext().getColorStateList(R.color.brand_red));
                btnDeliver.setOnClickListener(v -> {
                    dismiss();
                    ServiceRequestSheet.newInstance("Refilling").show(getParentFragmentManager(), "service_request");
                });

                llEquipmentList.addView(card);
            }
        }

        if (count == 0) {
            tvEmptyEquipment.setVisibility(View.VISIBLE);
        } else {
            tvEmptyEquipment.setVisibility(View.GONE);
        }
    }
}
