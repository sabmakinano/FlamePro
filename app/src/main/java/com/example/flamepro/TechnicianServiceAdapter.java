package com.example.flamepro;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flamepro.network.models.TechnicianServiceDto;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TechnicianServiceAdapter extends RecyclerView.Adapter<TechnicianServiceAdapter.TechnicianViewHolder> {

    public interface OnServiceActionListener {
        void onMarkCompleted(TechnicianServiceDto service);
    }

    private final List<TechnicianServiceDto> services;
    private final OnServiceActionListener listener;

    public TechnicianServiceAdapter(List<TechnicianServiceDto> services, OnServiceActionListener listener) {
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TechnicianViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_technician_service, parent, false);
        return new TechnicianViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TechnicianViewHolder holder, int position) {
        TechnicianServiceDto srv = services.get(position);
        Context context = holder.itemView.getContext();

        holder.tvServiceRequestId.setText("#SR - " + srv.getId());
        holder.tvServiceTypeBadge.setText(srv.getServiceType() != null ? srv.getServiceType() : "Service");
        holder.tvTechCustomerName.setText(srv.getCustomerName().trim().isEmpty() ? "Customer" : srv.getCustomerName());
        holder.tvTechCustomerAddress.setText(srv.getAddress() != null && !srv.getAddress().isEmpty() ? srv.getAddress() : "Address on File");

        String phone = srv.getCustomerPhone() != null && !srv.getCustomerPhone().isEmpty() ? srv.getCustomerPhone() : "N/A";
        holder.tvTechCustomerPhone.setText("📞 " + phone);
        holder.tvTechCustomerPhone.setOnClickListener(v -> {
            if (!phone.equals("N/A")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                context.startActivity(intent);
            }
        });

        holder.tvServiceDetails.setText(srv.getDetails() != null && !srv.getDetails().isEmpty() 
                ? srv.getDetails() : "Standard Safety Inspection & Service");

        String dateStr = (srv.getServiceDate() != null && !srv.getServiceDate().isEmpty()) 
                ? srv.getServiceDate() : "Scheduled";
        String timeStr = (srv.getServiceTime() != null && !srv.getServiceTime().isEmpty()) 
                ? " • " + srv.getServiceTime() : "";
        holder.tvServiceDateTime.setText(dateStr + timeStr);

        boolean isCompleted = "Completed".equalsIgnoreCase(srv.getStatus());
        if (isCompleted) {
            holder.btnMarkCompleted.setEnabled(false);
            holder.btnMarkCompleted.setText("Completed ✓");
            holder.btnMarkCompleted.setBackgroundTintList(context.getColorStateList(R.color.success_green));
        } else {
            holder.btnMarkCompleted.setEnabled(true);
            holder.btnMarkCompleted.setText("Complete Job");
            holder.btnMarkCompleted.setBackgroundTintList(context.getColorStateList(R.color.brand_blue_primary));
            holder.btnMarkCompleted.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMarkCompleted(srv);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public void updateList(List<TechnicianServiceDto> newServices) {
        services.clear();
        services.addAll(newServices);
        notifyDataSetChanged();
    }

    static class TechnicianViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceRequestId, tvServiceTypeBadge, tvTechCustomerName, tvTechCustomerAddress, tvTechCustomerPhone, tvServiceDetails, tvServiceDateTime;
        MaterialButton btnMarkCompleted;

        public TechnicianViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceRequestId = itemView.findViewById(R.id.tvServiceRequestId);
            tvServiceTypeBadge = itemView.findViewById(R.id.tvServiceTypeBadge);
            tvTechCustomerName = itemView.findViewById(R.id.tvTechCustomerName);
            tvTechCustomerAddress = itemView.findViewById(R.id.tvTechCustomerAddress);
            tvTechCustomerPhone = itemView.findViewById(R.id.tvTechCustomerPhone);
            tvServiceDetails = itemView.findViewById(R.id.tvServiceDetails);
            tvServiceDateTime = itemView.findViewById(R.id.tvServiceDateTime);
            btnMarkCompleted = itemView.findViewById(R.id.btnMarkCompleted);
        }
    }
}
