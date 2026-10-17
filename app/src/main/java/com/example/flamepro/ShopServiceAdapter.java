package com.example.flamepro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ShopServiceAdapter extends RecyclerView.Adapter<ShopServiceAdapter.ServiceViewHolder> {

    public interface OnServiceItemClickListener {
        void onServiceClick(ShopServiceItem service);
    }

    private final List<ShopServiceItem> services;
    private final OnServiceItemClickListener listener;

    public ShopServiceAdapter(List<ShopServiceItem> services, OnServiceItemClickListener listener) {
        this.services = new ArrayList<>(services);
        this.listener = listener;
    }

    public void updateList(List<ShopServiceItem> newList) {
        services.clear();
        if (newList != null) {
            services.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ShopServiceItem service = services.get(position);
        holder.tvName.setText(service.getTitle());
        holder.tvSubtitle.setText(service.getSubtitle());
        holder.ivIcon.setImageResource(service.getIconRes());
        holder.tvBadge.setText(service.getBadgeText());

        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onServiceClick(service);
            }
        };

        holder.itemView.setOnClickListener(clickListener);
        holder.btnBook.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvBadge;
        TextView tvName;
        TextView tvSubtitle;
        MaterialButton btnBook;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivServiceIcon);
            tvBadge = itemView.findViewById(R.id.tvServiceBadge);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvSubtitle = itemView.findViewById(R.id.tvServiceSubtitle);
            btnBook = itemView.findViewById(R.id.btnBookService);
        }
    }
}
