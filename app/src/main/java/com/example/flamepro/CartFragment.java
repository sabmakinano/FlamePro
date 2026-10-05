package com.example.flamepro;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    private RecyclerView rvCart;
    private TextView tvTotalPrice, tvItemCount;
    private View llEmptyCart, clBottomBar;
    private ImageView cbAll;
    private CartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header and Status
        tvItemCount = view.findViewById(R.id.tvTitle);
        
        // Lists and States
        rvCart = view.findViewById(R.id.rvCart);
        llEmptyCart = view.findViewById(R.id.llEmptyCart);
        clBottomBar = view.findViewById(R.id.clBottomBar);
        cbAll = view.findViewById(R.id.cbAll);
        
        // Summary
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);

        View ivBack = view.findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        View btnBrowse = view.findViewById(R.id.btnBrowseProducts);
        if (btnBrowse != null) {
            btnBrowse.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    BottomNavigationView nav = mainActivity.findViewById(R.id.bottomNavigation);
                    if (nav != null) {
                        nav.setSelectedItemId(R.id.nav_shop);
                    }
                }
            });
        }

        View btnCheckout = view.findViewById(R.id.btnCheckout);
        if (btnCheckout != null) {
            btnCheckout.setOnClickListener(v -> {
                double total = CartManager.getInstance().getTotalPrice();
                if (total <= 0) {
                    android.widget.Toast.makeText(getContext(),
                            "Please select at least one item", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                // Show order breakdown sheet before confirming
                CartOrderSummarySheet.newInstance()
                        .show(getParentFragmentManager(), "cart_summary");
            });
        }

        if (cbAll != null) {
            cbAll.setOnClickListener(v -> {
                boolean allSelected = true;
                List<CartItem> cartItems = CartManager.getInstance().getCartItems();
                for (CartItem item : cartItems) {
                    if (!item.isSelected()) {
                        allSelected = false;
                        break;
                    }
                }
                boolean targetSelected = !allSelected;
                for (CartItem item : cartItems) {
                    item.setSelected(targetSelected);
                }
                adapter.notifyDataSetChanged();
                updateUI();
            });
        }

        setupRecyclerView();
        updateUI();
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(CartManager.getInstance().getCartItems(), new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                CartManager.getInstance().updateQuantity(item.getProduct(), newQuantity);
                updateUI();
            }

            @Override
            public void onRemoveItem(CartItem item) {
                List<CartItem> cartItems = CartManager.getInstance().getCartItems();
                int position = cartItems.indexOf(item);
                CartManager.getInstance().removeProduct(item.getProduct());
                if (position != -1) {
                    adapter.removeItem(position);
                }
                updateUI();
            }
        });
        rvCart.setAdapter(adapter);

        // Swipe-left-to-delete
        ItemTouchHelper swipeHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;
                List<CartItem> items = CartManager.getInstance().getCartItems();
                if (position < items.size()) {
                    CartItem item = items.get(position);
                    CartManager.getInstance().removeProduct(item.getProduct());
                    adapter.removeItem(position);
                    updateUI();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    View itemView = viewHolder.itemView;
                    Paint paint = new Paint();
                    paint.setColor(Color.parseColor("#E53935")); // red

                    RectF background = new RectF(
                            itemView.getRight() + dX,
                            itemView.getTop() + 8f,
                            itemView.getRight(),
                            itemView.getBottom() - 8f
                    );
                    float radius = 16f;
                    c.drawRoundRect(background, radius, radius, paint);

                    // Draw trash icon
                    Drawable trashIcon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete);
                    if (trashIcon != null) {
                        trashIcon.setTint(Color.WHITE);
                        int iconSize = 64;
                        int iconLeft = itemView.getRight() - iconSize - 32;
                        int iconTop = itemView.getTop() + (itemView.getHeight() - iconSize) / 2;
                        trashIcon.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize);
                        trashIcon.draw(c);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        swipeHelper.attachToRecyclerView(rvCart);
    }

    private void updateUI() {
        if (getView() == null) return;

        List<CartItem> items = CartManager.getInstance().getCartItems();
        int totalItemsCount = CartManager.getInstance().getTotalItems();
        boolean isEmpty = items.isEmpty();

        // Safe visibility updates
        if (llEmptyCart != null) llEmptyCart.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (rvCart != null) rvCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (clBottomBar != null) clBottomBar.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        View clTopBar = getView().findViewById(R.id.clTopBar);
        if (clTopBar != null) {
            clTopBar.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }

        if (!isEmpty && adapter != null) {
            adapter.updateItems(items);
            
            if (tvItemCount != null) {
                String title = String.format(Locale.getDefault(), "Shopping cart (%d)", totalItemsCount);
                tvItemCount.setText(title);
            }
            
            double total = CartManager.getInstance().getTotalPrice();
            if (tvTotalPrice != null) tvTotalPrice.setText(String.format(Locale.getDefault(), "%.2f", total));

            if (cbAll != null) {
                boolean allSelected = true;
                for (CartItem item : items) {
                    if (!item.isSelected()) {
                        allSelected = false;
                        break;
                    }
                }
                boolean isChecked = allSelected && !items.isEmpty();
                cbAll.setImageResource(isChecked ? R.drawable.custom_checked_circle : R.drawable.custom_unchecked_circle);
            }
        }
    }
}
