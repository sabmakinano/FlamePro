package com.example.flamepro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> products;
    private final OnAddToCartClickListener addToCartListener;
    private final OnBuyNowClickListener buyNowListener;
    private final OnProductClickListener productClickListener;

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Product product);
    }

    public interface OnBuyNowClickListener {
        void onBuyNowClick(Product product);
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnAddToCartClickListener addToCartListener, 
                          OnBuyNowClickListener buyNowListener, OnProductClickListener productClickListener) {
        this.products = products;
        this.addToCartListener = addToCartListener;
        this.buyNowListener = buyNowListener;
        this.productClickListener = productClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        
        // Handle horizontal orientation by setting a fixed width
        if (parent instanceof RecyclerView) {
            RecyclerView.LayoutManager lm = ((RecyclerView) parent).getLayoutManager();
            if (lm instanceof LinearLayoutManager &&
                ((LinearLayoutManager) lm).getOrientation() == RecyclerView.HORIZONTAL) {
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                lp.width = (int) (180 * parent.getContext().getResources().getDisplayMetrics().density);
                view.setLayoutParams(lp);
            } else {
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                view.setLayoutParams(lp);
            }
        }
        
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(product.getPrice());
        holder.tvReviews.setText("(" + product.getReviews() + ")");
        ImageLoader.load(holder.ivImage, product.getImageUrl(), product.getImageResource());

        boolean isAvailable = product.isInStock() && product.getStockQuantity() > 0;

        if (!isAvailable) {
            holder.itemView.setAlpha(0.45f);
            holder.btnAddToCart.setEnabled(false);
            holder.btnAddToCart.setAlpha(0.3f);
            holder.btnBuyNow.setEnabled(false);
            holder.btnBuyNow.setAlpha(0.3f);
            holder.btnBuyNow.setText("OUT");
            holder.btnAddToCart.setText("OUT");

            View.OnClickListener outOfStockListener = v -> {
                android.widget.Toast.makeText(v.getContext(), 
                        "'" + product.getName() + "' is currently out of stock", 
                        android.widget.Toast.LENGTH_SHORT).show();
            };

            holder.itemView.setOnClickListener(outOfStockListener);
            holder.ivImage.setOnClickListener(outOfStockListener);
            holder.btnAddToCart.setOnClickListener(outOfStockListener);
            holder.btnBuyNow.setOnClickListener(outOfStockListener);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.btnAddToCart.setEnabled(true);
            holder.btnAddToCart.setAlpha(1.0f);
            holder.btnBuyNow.setEnabled(true);
            holder.btnBuyNow.setAlpha(1.0f);
            holder.btnBuyNow.setText(R.string.buy_caps);
            holder.btnAddToCart.setText(R.string.cart_caps);

            holder.itemView.setOnClickListener(v -> {
                if (productClickListener != null) {
                    productClickListener.onProductClick(product);
                }
            });

            holder.ivImage.setOnClickListener(v -> {
                if (productClickListener != null) {
                    productClickListener.onProductClick(product);
                }
            });

            holder.btnAddToCart.setOnClickListener(v -> {
                if (addToCartListener != null) {
                    addToCartListener.onAddToCartClick(product);
                }
            });

            holder.btnBuyNow.setOnClickListener(v -> {
                if (buyNowListener != null) {
                    buyNowListener.onBuyNowClick(product);
                }
            });
        }
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
        TextView tvName, tvPrice, tvReviews;
        ImageView ivImage;
        MaterialButton btnAddToCart, btnBuyNow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvReviews = itemView.findViewById(R.id.tvReviewCount);
            ivImage = itemView.findViewById(R.id.ivProductImage);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnBuyNow = itemView.findViewById(R.id.btnBuyNow);
        }
    }
}
