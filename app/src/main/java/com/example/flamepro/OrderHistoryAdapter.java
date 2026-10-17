package com.example.flamepro;

import android.graphics.Color;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    public interface OnOrderCancelListener {
        void onOrderCancelled(Order order);
    }

    private List<Order> orders;
    private OnOrderCancelListener cancelListener;

    public OrderHistoryAdapter(List<Order> orders, OnOrderCancelListener cancelListener) {
        this.orders = orders != null ? new ArrayList<>(orders) : new ArrayList<>();
        this.cancelListener = cancelListener;
    }

    public void updateOrders(List<Order> newOrders) {
        List<Order> safeNewOrders = newOrders != null ? new ArrayList<>(newOrders) : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new OrderDiffCallback(this.orders, safeNewOrders));
        this.orders = safeNewOrders;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order, cancelListener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderDiffCallback extends DiffUtil.Callback {
        private final List<Order> oldList;
        private final List<Order> newList;

        public OrderDiffCallback(List<Order> oldList, List<Order> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList != null ? oldList.size() : 0;
        }

        @Override
        public int getNewListSize() {
            return newList != null ? newList.size() : 0;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Order oldOrder = oldList.get(oldItemPosition);
            Order newOrder = newList.get(newItemPosition);
            if (oldOrder == null || newOrder == null) return false;
            if (oldOrder.getBackendId() > 0 && newOrder.getBackendId() > 0) {
                return oldOrder.getBackendId() == newOrder.getBackendId();
            }
            return oldOrder.getOrderId() != null && oldOrder.getOrderId().equalsIgnoreCase(newOrder.getOrderId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Order oldOrder = oldList.get(oldItemPosition);
            Order newOrder = newList.get(newItemPosition);
            if (oldOrder == null || newOrder == null) return false;

            boolean statusSame = oldOrder.getStatus() == newOrder.getStatus();
            boolean expandedSame = oldOrder.isExpanded() == newOrder.isExpanded();
            boolean priceSame = (oldOrder.getTotalPrice() == null && newOrder.getTotalPrice() == null)
                    || (oldOrder.getTotalPrice() != null && oldOrder.getTotalPrice().equals(newOrder.getTotalPrice()));
            boolean itemsCountSame = (oldOrder.getItems() != null ? oldOrder.getItems().size() : 0)
                    == (newOrder.getItems() != null ? newOrder.getItems().size() : 0);

            return statusSame && expandedSame && priceSame && itemsCountSame;
        }
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvTitle, tvDate, tvEstDelivery, tvPrice, btnCancelOrder;
        ImageView ivProduct, ivExpandArrow;
        LinearLayout llStatusContainer, layoutExpandedItems, llItemsContainer;
        TextView tvItemsBreakdownHeader;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderNumber);
            tvStatus = itemView.findViewById(R.id.tvStatusBadge);
            tvTitle = itemView.findViewById(R.id.tvOrderTitle);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvEstDelivery = itemView.findViewById(R.id.tvItemEstDelivery);
            tvPrice = itemView.findViewById(R.id.tvOrderPrice);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
            ivProduct = itemView.findViewById(R.id.ivProductImage);
            ivExpandArrow = itemView.findViewById(R.id.ivExpandArrow);
            llStatusContainer = itemView.findViewById(R.id.llStatusContainer);
            layoutExpandedItems = itemView.findViewById(R.id.layoutExpandedItems);
            llItemsContainer = itemView.findViewById(R.id.llItemsContainer);
            tvItemsBreakdownHeader = itemView.findViewById(R.id.tvItemsBreakdownHeader);
        }

        public void bind(Order order, OnOrderCancelListener cancelListener) {
            tvOrderId.setText(order.getOrderId());
            tvDate.setText(order.getOrderDate());
            tvEstDelivery.setText(order.getEstDelivery());
            tvPrice.setText(order.getTotalPrice());

            List<CartItem> items = order.getItems();
            if (items != null && !items.isEmpty()) {
                CartItem firstItem = items.get(0);
                if (firstItem != null && firstItem.getProduct() != null) {
                    int otherCount = items.size() - 1;
                    if (otherCount > 0) {
                        String summary = firstItem.getProduct().getName() + " x " + firstItem.getQuantity() 
                                + " (+" + otherCount + (otherCount == 1 ? " other item)" : " other items)");
                        tvTitle.setText(summary);
                    } else {
                        tvTitle.setText(firstItem.getProduct().getName() + " x " + firstItem.getQuantity());
                    }
                    ImageLoader.load(ivProduct, firstItem.getProduct().getImageUrl(), firstItem.getProduct().getImageResource());
                } else {
                    tvTitle.setText("Order Items");
                }
            } else {
                tvTitle.setText("Order Items");
            }

            setStatusBadge(order.getStatus());

            if (order.getStatus() == Order.OrderStatus.PENDING) {
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnCancelOrder.setOnClickListener(v -> {
                    if (cancelListener != null) {
                        cancelListener.onOrderCancelled(order);
                    }
                });
            } else {
                btnCancelOrder.setVisibility(View.GONE);
                btnCancelOrder.setOnClickListener(null);
            }

            // Sync expanded UI state
            boolean isExpanded = order.isExpanded();
            if (layoutExpandedItems != null) {
                layoutExpandedItems.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                if (isExpanded) {
                    populateExpandedItems(order);
                }
            }
            if (ivExpandArrow != null) {
                ivExpandArrow.setRotation(isExpanded ? 90f : 0f);
            }

            // Toggle expansion listener on arrow / status badge and card
            View.OnClickListener toggleExpandListener = v -> {
                boolean newExpanded = !order.isExpanded();
                order.setExpanded(newExpanded);
                if (itemView.getParent() instanceof ViewGroup) {
                    TransitionManager.beginDelayedTransition((ViewGroup) itemView.getParent());
                }
                if (layoutExpandedItems != null) {
                    layoutExpandedItems.setVisibility(newExpanded ? View.VISIBLE : View.GONE);
                }
                if (ivExpandArrow != null) {
                    ivExpandArrow.animate().rotation(newExpanded ? 90f : 0f).setDuration(200).start();
                }
                if (newExpanded) {
                    populateExpandedItems(order);
                }
            };

            if (llStatusContainer != null) {
                llStatusContainer.setOnClickListener(toggleExpandListener);
            }
            itemView.setOnClickListener(toggleExpandListener);
        }

        private void populateExpandedItems(Order order) {
            if (llItemsContainer == null) return;
            List<CartItem> items = order.getItems();
            int neededCount = items != null ? items.size() : 0;
            if (neededCount == 0) {
                llItemsContainer.removeAllViews();
                return;
            }

            if (tvItemsBreakdownHeader != null) {
                tvItemsBreakdownHeader.setText(String.format(java.util.Locale.getDefault(), "Ordered Items (%d)", neededCount));
            }

            int existingCount = llItemsContainer.getChildCount();
            LayoutInflater inflater = LayoutInflater.from(itemView.getContext());

            // Reuse views, inflate only missing, remove excess to prevent lag
            for (int i = existingCount; i < neededCount; i++) {
                View row = inflater.inflate(R.layout.item_order_summary_row, llItemsContainer, false);
                llItemsContainer.addView(row);
            }
            if (existingCount > neededCount) {
                llItemsContainer.removeViews(neededCount, existingCount - neededCount);
            }

            for (int i = 0; i < neededCount; i++) {
                View row = llItemsContainer.getChildAt(i);
                CartItem item = items.get(i);
                TextView tvName = row.findViewById(R.id.tvSummaryRowName);
                TextView tvQty = row.findViewById(R.id.tvSummaryRowQty);
                TextView tvPrice = row.findViewById(R.id.tvSummaryRowPrice);

                if (item != null && item.getProduct() != null) {
                    tvName.setText(item.getProduct().getName());
                    tvQty.setText("x" + item.getQuantity());
                    String price = item.getProduct().getPrice();
                    tvPrice.setText(price != null && !price.isEmpty() ? price : "—");
                }
            }
        }

        private void setStatusBadge(Order.OrderStatus status) {
            tvStatus.setText(status.getLabel());
            switch (status) {
                case SHIPPED:
                    tvStatus.setBackgroundResource(R.drawable.badge_shipped);
                    tvStatus.setTextColor(Color.parseColor("#E65100"));
                    break;
                case DELIVERED:
                    tvStatus.setBackgroundResource(R.drawable.badge_delivered);
                    tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                    break;
                case CANCELLED:
                    tvStatus.setBackgroundResource(R.drawable.badge_cancelled);
                    tvStatus.setTextColor(Color.parseColor("#C62828"));
                    break;
                case PENDING:
                default:
                    tvStatus.setBackgroundResource(R.drawable.badge_pending);
                    tvStatus.setTextColor(Color.parseColor("#757575"));
                    break;
            }
        }
    }
}
