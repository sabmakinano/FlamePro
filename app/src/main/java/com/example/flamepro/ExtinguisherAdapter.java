package com.example.flamepro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ExtinguisherAdapter extends RecyclerView.Adapter<ExtinguisherAdapter.ViewHolder> {

    private List<Product> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ExtinguisherAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_extinguisher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        ImageLoader.load(holder.ivExtinguisher, product.getImageUrl(), product.getImageResource());
        holder.tvName.setText(product.getName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateList(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivExtinguisher;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivExtinguisher = itemView.findViewById(R.id.ivExtinguisher);
            tvName = itemView.findViewById(R.id.tvExtinguisherName);
        }
    }
}
