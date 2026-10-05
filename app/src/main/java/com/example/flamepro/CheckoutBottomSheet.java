package com.example.flamepro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
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

public class CheckoutBottomSheet extends BottomSheetDialogFragment {

    private Product product;
    private int quantity = 1;
    private double deliveryFee = 100.0;

    public static CheckoutBottomSheet newInstance(Product product) {
        CheckoutBottomSheet fragment = new CheckoutBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable("product", product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable("product");
        }
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
                
                // Keep it steady at the top
                behavior.setFitToContents(false);
                behavior.setExpandedOffset(0);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                
                // Force parent to take full screen height
                ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                parent.setLayoutParams(layoutParams);
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_checkout_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivProduct = view.findViewById(R.id.ivProduct);
        TextView tvProductName = view.findViewById(R.id.tvProductName);
        TextView tvVariant = view.findViewById(R.id.tvVariant);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvQty = view.findViewById(R.id.tvQty);
        TextView tvSubtotal = view.findViewById(R.id.tvSubtotalValue);
        TextView tvTotal = view.findViewById(R.id.tvTotalValue);
        TextView tvItemCount = view.findViewById(R.id.tvItemCount);
        MaterialButton btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);
        android.widget.Spinner spnVariants = view.findViewById(R.id.spnVariants);

        android.widget.Spinner spnDeliveryArea = view.findViewById(R.id.spnDeliveryArea);
        String[] deliveryZones = {
                "Zone 1: Metro Cebu (₱100)",
                "Zone 2: Greater Cebu (₱150)",
                "Zone 3: Provincial Cebu (₱200)"
        };
        android.widget.ArrayAdapter<String> deliveryAdapter = new android.widget.ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, deliveryZones);
        deliveryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDeliveryArea.setAdapter(deliveryAdapter);
        
        spnDeliveryArea.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int position, long id) {
                if (position == 0) deliveryFee = 100.0;
                else if (position == 1) deliveryFee = 150.0;
                else if (position == 2) deliveryFee = 200.0;
                
                updatePriceUI(tvQty, tvPrice, tvSubtotal, tvTotal, btnPlaceOrder, tvItemCount);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        if (product != null) {
            ImageLoader.load(ivProduct, product.getImageUrl(), product.getImageResource());
            tvProductName.setText(product.getName());
            tvVariant.setText(product.getWeight() + " • " + product.getType());
            
            if (product.getVariantWeights() != null && !product.getVariantWeights().isEmpty()) {
                spnVariants.setVisibility(View.VISIBLE);
                tvVariant.setVisibility(View.GONE); // Hide static text if spinner is used
                android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_spinner_item, product.getVariantWeights());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnVariants.setAdapter(adapter);
                
                // Set initial selection based on product weight
                int selectedPosition = product.getVariantWeights().indexOf(product.getWeight());
                if(selectedPosition >= 0) spnVariants.setSelection(selectedPosition);

                spnVariants.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View v, int position, long id) {
                        product.setWeight(product.getVariantWeights().get(position));
                        product.setPrice(product.getVariantPrices().get(position));
                        updatePriceUI(tvQty, tvPrice, tvSubtotal, tvTotal, btnPlaceOrder, tvItemCount);
                    }
                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
            } else {
                spnVariants.setVisibility(View.GONE);
                tvVariant.setVisibility(View.VISIBLE);
            }
            
            updatePriceUI(tvQty, tvPrice, tvSubtotal, tvTotal, btnPlaceOrder, tvItemCount);
        }

        view.findViewById(R.id.btnPlus).setOnClickListener(v -> {
            quantity++;
            updatePriceUI(tvQty, tvPrice, tvSubtotal, tvTotal, btnPlaceOrder, tvItemCount);
        });

        view.findViewById(R.id.btnMinus).setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updatePriceUI(tvQty, tvPrice, tvSubtotal, tvTotal, btnPlaceOrder, tvItemCount);
            }
        });

        view.findViewById(R.id.btnPlaceOrder).setOnClickListener(v -> {
            String totalStr = tvTotal.getText().toString();
            String estDelivery = OrderSuccessFragment.calculateDeliveryDateString();
            
            if (product != null) {
                List<CartItem> orderedItems = new ArrayList<>();
                orderedItems.add(new CartItem(product, quantity));
                
                TimeZone tz = TimeZone.getTimeZone("Asia/Manila");
                Calendar cal = Calendar.getInstance(tz);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.ENGLISH);
                sdf.setTimeZone(tz);
                String orderDateStr = sdf.format(cal.getTime());
                
                String orderId = "ORD - " + (4000 + new Random().nextInt(5000));
                Order newOrder = new Order(orderId, orderedItems, orderDateStr, estDelivery, totalStr, Order.OrderStatus.PENDING);
                OrderManager.getInstance().addOrder(newOrder);

                // Call Backend API to persist order
                int userId = UserManager.getInstance().getUserId();
                String address = UserManager.getInstance().getAddress();
                if (address == null || address.trim().isEmpty()) {
                    address = "Customer Address";
                }
                String customerName = UserManager.getInstance().getFullName();
                if (customerName == null || customerName.trim().isEmpty()) {
                    customerName = "Guest";
                }
                List<OrderRequest.OrderItemRequest> apiItems = new ArrayList<>();
                apiItems.add(new OrderRequest.OrderItemRequest(null, product.getName(), quantity, product.getPrice()));

                OrderRequest orderReq = new OrderRequest(userId > 0 ? userId : null, totalStr, "Cash on Delivery", address, customerName, apiItems);
                ApiClient.getApiService().createOrder(orderReq).enqueue(new Callback<OrderResponse>() {
                    @Override
                    public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {}
                    @Override
                    public void onFailure(Call<OrderResponse> call, Throwable t) {}
                });
            }

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(OrderSuccessFragment.newInstance(totalStr, estDelivery));
                dismiss();
            }
        });

        // Toggle radio button on row click
        View codRow = view.findViewById(R.id.codRow);
        RadioButton rbCod = view.findViewById(R.id.rbCod);
        if (codRow != null && rbCod != null) {
            View.OnClickListener toggleListener = v -> rbCod.setChecked(!rbCod.isChecked());
            codRow.setOnClickListener(toggleListener);
            rbCod.setOnClickListener(toggleListener);
        }

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnBack).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnClearCart).setOnClickListener(v -> dismiss());
    }

    private void updatePriceUI(TextView tvQty, TextView tvPrice, TextView tvSubtotal, 
                              TextView tvTotal, MaterialButton btnPlaceOrder, TextView tvItemCount) {
        
        String cleanPrice = product.getPrice().replaceAll("[^0-9.]", "");
        double unitPrice = Double.parseDouble(cleanPrice);
        double subtotal = unitPrice * quantity;
        double total = subtotal + deliveryFee;

        tvQty.setText(String.valueOf(quantity));
        tvPrice.setText(String.format(Locale.getDefault(), "₱ %.2f", subtotal));
        tvSubtotal.setText(String.format(Locale.getDefault(), "₱ %.2f", subtotal));
        tvTotal.setText(String.format(Locale.getDefault(), "₱ %.2f", total));
        
        String itemText = quantity == 1 ? "1 item" : quantity + " items";
        tvItemCount.setText(itemText);
        
        btnPlaceOrder.setText(String.format(Locale.getDefault(), "Place Order • ₱ %.2f", total));
    }
}
