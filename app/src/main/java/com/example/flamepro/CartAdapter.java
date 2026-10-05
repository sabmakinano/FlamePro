package com.example.flamepro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItem> items;
    private OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
    }

    public CartAdapter(List<CartItem> items, OnCartItemChangeListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        Product product = item.getProduct();
        
        holder.tvName.setText(product.getName());
        holder.tvVariant.setText(product.getWeight() + ", " + product.getType());
        
        String rawPrice = product.getPrice().replaceAll("[^0-9.]", "").trim();
        double priceVal = 0;
        try {
            if (!rawPrice.isEmpty()) {
                priceVal = Double.parseDouble(rawPrice);
            }
        } catch (NumberFormatException ignored) {}
        
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%.2f", priceVal));
        
        if (product.getOriginalPrice() != null) {
            holder.tvOriginalPrice.setText("₱" + product.getOriginalPrice().replaceAll("[^0-9.]", "") + " " + product.getDiscount());
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            holder.tvOriginalPrice.setVisibility(View.GONE);
        }

        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        ImageLoader.load(holder.ivProduct, product.getImageUrl(), product.getImageResource());

        // Checkbox logic
        holder.cbItem.setImageResource(item.isSelected() ? R.drawable.custom_checked_circle : R.drawable.custom_unchecked_circle);
        holder.cbItem.setOnClickListener(v -> {
            item.setSelected(!item.isSelected());
            if (listener != null) {
                listener.onQuantityChanged(item, item.getQuantity()); // Using this to trigger UI update in Fragment
            }
        });

        // Shop Header Checkbox logic
        boolean allSelected = true;
        for (CartItem ci : items) {
            if (!ci.isSelected()) {
                allSelected = false;
                break;
            }
        }
        holder.cbShop.setImageResource(allSelected ? R.drawable.custom_checked_circle : R.drawable.custom_unchecked_circle);
        final boolean nextAllSelected = !allSelected;
        holder.cbShop.setOnClickListener(v -> {
            for (CartItem ci : items) {
                ci.setSelected(nextAllSelected);
            }
            notifyDataSetChanged();
            if (listener != null) {
                listener.onQuantityChanged(item, item.getQuantity());
            }
        });

        // Standard logic for buttons
        holder.ivMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                listener.onQuantityChanged(item, item.getQuantity() - 1);
            }
        });

        holder.ivPlus.setOnClickListener(v -> {
            listener.onQuantityChanged(item, item.getQuantity() + 1);
        });

        holder.ivDelete.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (listener != null && currentPos != RecyclerView.NO_POSITION) {
                listener.onRemoveItem(items.get(currentPos));
            }
        });
        
        // Hide shop header for every item except the first one in this simple implementation
        // Ideally we'd group them, but for now we just match the visual style
        holder.llShopHeader.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<CartItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, items.size());
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity, tvOriginalPrice, tvVariant, tvShopName;
        ImageView ivProduct, ivMinus, ivPlus, ivDelete, cbItem, cbShop;
        View llShopHeader;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvVariant = itemView.findViewById(R.id.tvVariant);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            ivMinus = itemView.findViewById(R.id.ivMinus);
            ivPlus = itemView.findViewById(R.id.ivPlus);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            
            cbItem = itemView.findViewById(R.id.cbItem);
            cbShop = itemView.findViewById(R.id.cbShop);
            tvShopName = itemView.findViewById(R.id.tvShopName);
            llShopHeader = itemView.findViewById(R.id.llShopHeader);
        }
    }
}
