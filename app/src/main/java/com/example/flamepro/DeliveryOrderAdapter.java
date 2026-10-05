package com.example.flamepro;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flamepro.network.models.DeliveryOrderDto;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class DeliveryOrderAdapter extends RecyclerView.Adapter<DeliveryOrderAdapter.DeliveryViewHolder> {

    public interface OnDeliveryActionListener {
        void onMarkDelivered(DeliveryOrderDto order);
    }

    private final List<DeliveryOrderDto> orders;
    private final OnDeliveryActionListener listener;

    public DeliveryOrderAdapter(List<DeliveryOrderDto> orders, OnDeliveryActionListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delivery_order, parent, false);
        return new DeliveryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        DeliveryOrderDto order = orders.get(position);
        Context context = holder.itemView.getContext();

        holder.tvOrderNumber.setText(order.getOrderNumber());
        holder.tvCustomerName.setText(order.getCustomerName().trim().isEmpty() ? "Customer" : order.getCustomerName());
        holder.tvCustomerAddress.setText(order.getShippingAddress() != null && !order.getShippingAddress().isEmpty() 
                ? order.getShippingAddress() : "Address on File");
        
        String phone = order.getCustomerPhone() != null && !order.getCustomerPhone().isEmpty() 
                ? order.getCustomerPhone() : "N/A";
        holder.tvCustomerPhone.setText("📞 " + phone);
        holder.tvCustomerPhone.setOnClickListener(v -> {
            if (!phone.equals("N/A")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                context.startActivity(intent);
            }
        });

        // Items Summary
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (DeliveryOrderDto.DeliveryItemDto item : order.getItems()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("• ").append(item.getProductName()).append(" (x").append(item.getQuantity()).append(")");
            }
            holder.tvItemsSummary.setText(sb.toString());
            holder.tvItemsSummary.setVisibility(View.VISIBLE);
        } else {
            holder.tvItemsSummary.setVisibility(View.GONE);
        }

        holder.tvTotalAmount.setText(order.getTotalPrice());

        boolean isDelivered = "Delivered".equalsIgnoreCase(order.getStatus());
        if (isDelivered) {
            holder.tvOrderStatus.setText("Delivered");
            holder.tvOrderStatus.setBackgroundResource(R.drawable.badge_delivered);
            holder.btnMarkDelivered.setEnabled(false);
            holder.btnMarkDelivered.setText("Delivered ✓");
            holder.btnMarkDelivered.setBackgroundTintList(context.getColorStateList(R.color.success_green));
        } else {
            holder.tvOrderStatus.setText(order.getStatus() != null ? order.getStatus() : "Assigned");
            holder.tvOrderStatus.setBackgroundResource(R.drawable.badge_pending);
            holder.btnMarkDelivered.setEnabled(true);
            holder.btnMarkDelivered.setText("Mark Delivered");
            holder.btnMarkDelivered.setBackgroundTintList(context.getColorStateList(R.color.brand_red));
            holder.btnMarkDelivered.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMarkDelivered(order);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateList(List<DeliveryOrderDto> newOrders) {
        orders.clear();
        orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    static class DeliveryViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber, tvOrderStatus, tvCustomerName, tvCustomerAddress, tvCustomerPhone, tvItemsSummary, tvTotalAmount;
        MaterialButton btnMarkDelivered;

        public DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerAddress = itemView.findViewById(R.id.tvCustomerAddress);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvItemsSummary = itemView.findViewById(R.id.tvItemsSummary);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            btnMarkDelivered = itemView.findViewById(R.id.btnMarkDelivered);
        }
    }
}
