package com.example.flamepro;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShopFragment extends Fragment {

    private RecyclerView rvProducts;
    private View notificationLayout;
    private ImageView ivFilter;
    private EditText etSearch;
    private View rootView;
    private ProductAdapter adapter;
    private final List<Product> allProductsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_shop, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvProducts = view.findViewById(R.id.rvProducts);
        notificationLayout = view.findViewById(R.id.notificationLayout);
        ivFilter = view.findViewById(R.id.ivFilter);
        etSearch = view.findViewById(R.id.etSearch);

        setupProducts();
        setupNotifications();
        setupFilter();
        setupSearch();
    }

    private void setupSearch() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void filter(String query) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : allProductsList) {
            if (product.getName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
                filteredList.add(product);
            }
        }
        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }

    private void setupFilter() {
        if (ivFilter != null) {
            ivFilter.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(requireContext(), v);
                popup.getMenu().add(getString(R.string.cat_fire_extinguishers));
                popup.getMenu().add(getString(R.string.cat_fire_sprinklers));
                popup.getMenu().add(getString(R.string.cat_fireman_equipments));
                popup.getMenu().add(getString(R.string.cat_fire_hose));

                popup.setOnMenuItemClickListener(item -> {
                    Toast.makeText(getContext(), "Category: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                });
                popup.show();
            });
        }
    }

    private void setupNotifications() {
        if (notificationLayout != null) {
            notificationLayout.setOnClickListener(v -> 
                Toast.makeText(getContext(), "Fire safety alerts & updates will appear here!", Toast.LENGTH_SHORT).show());
        }
    }



    private void setupProducts() {
        allProductsList.clear();
        List<Integer> carousel = new ArrayList<>();
        carousel.add(R.drawable.logo);
        carousel.add(R.drawable.logo);
        carousel.add(R.drawable.logo);

        List<String> features = new ArrayList<>();
        features.add("UL Listed & Certified");
        features.add("Standard Safety Compliance");
        features.add("1-Year Warranty");

        // Adding 20 different products
        Product hfc = new Product("HFC236FA GREEN FIRE EXTINGUISHER", "₱ 1500.00", "₱ 1800.00", "15% OFF", 4.9f, 42, R.drawable.logo, carousel, "1kg", "HFC236FA", "10-15 ft", features, "Fire Extinguishers", true);
        hfc.setVariantWeights(java.util.Arrays.asList("1kg", "2.5kg", "5kg", "9kg", "25kg"));
        hfc.setVariantPrices(java.util.Arrays.asList("₱ 1500.00", "₱ 2800.00", "₱ 4500.00", "₱ 7500.00", "₱ 18000.00"));
        allProductsList.add(hfc);
        
        allProductsList.add(new Product("ABC Dry Powder 5 lb", "₱ 45.00", "₱ 55.00", "18% OFF", 4.8f, 24, R.drawable.logo, carousel, "5 lb", "Dry Powder", "10-15 ft", features, "Fire Extinguishers", true));
        allProductsList.add(new Product("CO2 Extinguisher 10 lb", "₱ 89.99", "₱ 110.00", "18% OFF", 4.7f, 15, R.drawable.logo, carousel, "10 lb", "CO2", "8-12 ft", features, "Fire Extinguishers", true));
        allProductsList.add(new Product("Foam Extinguisher 9 L", "₱ 59.50", "₱ 75.00", "20% OFF", 4.5f, 10, R.drawable.logo, carousel, "9L", "Foam", "12-18 ft", features, "Fire Extinguishers", true));
        allProductsList.add(new Product("Automatic Sprinkler Head", "₱ 24.99", "₱ 30.00", "16% OFF", 4.9f, 42, R.drawable.logo, carousel, "0.5 lb", "Automatic", "200 sq ft", features, "Fire Sprinklers", true));
        allProductsList.add(new Product("Fireman Helmet (Pro)", "₱ 120.00", "₱ 150.00", "20% OFF", 5.0f, 8, R.drawable.logo, carousel, "3 lb", "Protective", "N/A", features, "Fireman Equipments", true));
        allProductsList.add(new Product("Fire Hose Reel 30 m", "₱ 199.99", "₱ 250.00", "20% OFF", 4.6f, 5, R.drawable.logo, carousel, "15 lb", "Manual", "30m", features, "Fire Hose", true));
        allProductsList.add(new Product("Fire Blanket 1.2 x 1.2 m", "₱ 15.99", "₱ 20.00", "20% OFF", 4.8f, 56, R.drawable.logo, carousel, "1 lb", "Fiberglass", "1.2x1.2m", features, "Fire Extinguishers", true));
        allProductsList.add(new Product("Smoke Detector (Battery)", "₱ 12.50", "₱ 18.00", "30% OFF", 4.4f, 120, R.drawable.logo, carousel, "0.3 lb", "Ionization", "Room", features, "Others", true));
        allProductsList.add(new Product("Fire Exit Sign (LED)", "₱ 35.00", "₱ 45.00", "22% OFF", 4.7f, 30, R.drawable.logo, carousel, "2 lb", "LED Emergency", "Visual", features, "Others", true));
        allProductsList.add(new Product("Fireman Suit (Standard)", "₱ 450.00", "₱ 550.00", "18% OFF", 4.9f, 3, R.drawable.logo, carousel, "12 lb", "Heat Resistant", "Body", features, "Fireman Equipments", true));
        allProductsList.add(new Product("Fire Hose Nozzle (Brass)", "₱ 42.00", "₱ 55.00", "23% OFF", 4.5f, 18, R.drawable.logo, carousel, "2 lb", "Adjustable", "Variable", features, "Fire Hose", true));
        allProductsList.add(new Product("Water Extinguisher 6 L", "₱ 39.99", "₱ 50.00", "20% OFF", 4.3f, 12, R.drawable.logo, carousel, "6L", "Water", "15-20 ft", features, "Fire Extinguishers", true));
        allProductsList.add(new Product("Wet Chemical Extinguisher", "₱ 75.00", "₱ 95.00", "21% OFF", 4.8f, 9, R.drawable.logo, carousel, "6L", "Wet Chemical", "10-12 ft", features, "Fire Extinguishers", true));
        allProductsList.add(new Product("Sprinkler Pipe 2 m", "₱ 18.50", "₱ 25.00", "26% OFF", 4.2f, 20, R.drawable.logo, carousel, "5 lb", "Steel", "2m", features, "Fire Sprinklers", true));
        allProductsList.add(new Product("Fireman Boots", "₱ 85.00", "₱ 110.00", "22% OFF", 4.7f, 14, R.drawable.logo, carousel, "4 lb", "Waterproof", "Feet", features, "Fireman Equipments", true));
        allProductsList.add(new Product("Fire Axe (Heavy Duty)", "₱ 55.00", "₱ 70.00", "21% OFF", 4.6f, 22, R.drawable.logo, carousel, "6 lb", "Steel", "N/A", features, "Fireman Equipments", true));
        allProductsList.add(new Product("Fire Hose Cabinet", "₱ 145.00", "₱ 180.00", "19% OFF", 4.5f, 7, R.drawable.logo, carousel, "20 lb", "Metal", "Standard", features, "Fire Hose", true));
        allProductsList.add(new Product("First Aid Kit (Large)", "₱ 65.00", "₱ 85.00", "23% OFF", 4.9f, 45, R.drawable.logo, carousel, "5 lb", "Emergency", "Medical", features, "Others", true));
        allProductsList.add(new Product("Fire Whistle", "₱ 5.99", "₱ 10.00", "40% OFF", 4.0f, 80, R.drawable.logo, carousel, "0.1 lb", "Alert", "Audible", features, "Fireman Equipments", true));
        allProductsList.add(new Product("Gas Mask (Single Filter)", "₱ 95.00", "₱ 120.00", "20% OFF", 4.8f, 11, R.drawable.logo, carousel, "2 lb", "Air Purifying", "Head", features, "Fireman Equipments", true));

        adapter = new ProductAdapter(new ArrayList<>(allProductsList), product -> {
            CartManager.getInstance().addProduct(product.copy(), 1);
            performCartAnimation(rootView.findViewById(R.id.flCartAnim));
        }, product -> {
            CheckoutBottomSheet.newInstance(product.copy()).show(getParentFragmentManager(), "checkout");
        }, product -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, ProductDetailsFragment.newInstance(product.copy()))
                    .addToBackStack(null)
                    .commit();
        });
        rvProducts.setAdapter(adapter);

        // Fetch dynamic products from PostgreSQL backend
        fetchProductsFromBackend();
    }

    private void fetchProductsFromBackend() {
        com.example.flamepro.network.ApiClient.getApiService().getProducts(null, null)
                .enqueue(new retrofit2.Callback<com.example.flamepro.network.models.ProductListResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.flamepro.network.models.ProductListResponse> call,
                                           retrofit2.Response<com.example.flamepro.network.models.ProductListResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getProducts() != null) {
                            List<com.example.flamepro.network.models.ProductDto> dtos = response.body().getProducts();
                            if (!dtos.isEmpty()) {
                                allProductsList.clear();
                                for (com.example.flamepro.network.models.ProductDto dto : dtos) {
                                    allProductsList.add(dto.toProduct());
                                }
                                if (adapter != null) {
                                    adapter.updateList(new ArrayList<>(allProductsList));
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.flamepro.network.models.ProductListResponse> call, Throwable t) {
                        // Offline or server not reachable yet; fallback local products are kept
                    }
                });
    }

    private void performCartAnimation(View animView) {
        if (animView == null) return;

        animView.setVisibility(View.VISIBLE);
        animView.setScaleX(0f);
        animView.setScaleY(0f);
        animView.setAlpha(1f);

        animView.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> animView.animate()
                            .scaleX(1.8f)
                            .scaleY(1.8f)
                            .alpha(0f)
                            .setDuration(500)
                            .setInterpolator(new AccelerateInterpolator())
                            .withEndAction(() -> animView.setVisibility(View.GONE))
                            .start())
                .start();
    }
}
