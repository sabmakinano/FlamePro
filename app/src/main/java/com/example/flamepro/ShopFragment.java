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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flamepro.network.ApiClient;
import com.example.flamepro.network.models.ProductDto;
import com.example.flamepro.network.models.ProductListResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopFragment extends Fragment {

    private enum ShopMode { PRODUCTS, SERVICES }
    private ShopMode currentMode = ShopMode.PRODUCTS;

    private RecyclerView rvProducts;
    private RecyclerView rvServicesShop;
    private View notificationLayout;
    private ImageView ivFilter;
    private EditText etSearch;
    private TextView tvShopSubtitle;
    private View hsvCategoryChips;
    private TextView btnToggleProducts;
    private TextView btnToggleServices;
    private View rootView;

    private ProductAdapter adapter;
    private ShopServiceAdapter servicesAdapter;
    private final List<Product> allProductsList = new ArrayList<>();
    private final List<ShopServiceItem> allServicesList = new ArrayList<>();

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
        rvServicesShop = view.findViewById(R.id.rvServicesShop);
        notificationLayout = view.findViewById(R.id.notificationLayout);
        ivFilter = view.findViewById(R.id.ivFilter);
        etSearch = view.findViewById(R.id.etSearch);
        tvShopSubtitle = view.findViewById(R.id.tvShopSubtitle);
        hsvCategoryChips = view.findViewById(R.id.hsvCategoryChips);
        btnToggleProducts = view.findViewById(R.id.btnToggleProducts);
        btnToggleServices = view.findViewById(R.id.btnToggleServices);

        setupToggle();
        setupServices();
        setupProducts();
        setupNotifications();
        setupFilter();
        setupSearch();
        setupCategoryChips(view);
    }

    private void setupToggle() {
        if (btnToggleProducts != null) {
            btnToggleProducts.setOnClickListener(v -> setShopMode(ShopMode.PRODUCTS));
        }
        if (btnToggleServices != null) {
            btnToggleServices.setOnClickListener(v -> setShopMode(ShopMode.SERVICES));
        }
    }

    private void setShopMode(ShopMode mode) {
        currentMode = mode;
        if (mode == ShopMode.PRODUCTS) {
            btnToggleProducts.setBackgroundResource(R.drawable.shop_toggle_selected_bg);
            btnToggleProducts.setTextColor(getResources().getColor(R.color.brand_red, null));
            btnToggleServices.setBackgroundResource(android.R.color.transparent);
            btnToggleServices.setTextColor(getResources().getColor(R.color.white, null));

            if (tvShopSubtitle != null) tvShopSubtitle.setText(R.string.fire_safety_products);
            if (hsvCategoryChips != null) hsvCategoryChips.setVisibility(View.VISIBLE);
            if (rvProducts != null) rvProducts.setVisibility(View.VISIBLE);
            if (rvServicesShop != null) rvServicesShop.setVisibility(View.GONE);
            if (ivFilter != null) ivFilter.setVisibility(View.VISIBLE);
            if (etSearch != null) {
                etSearch.setHint(R.string.search_hint);
                filterProducts(etSearch.getText().toString());
            }
        } else {
            btnToggleServices.setBackgroundResource(R.drawable.shop_toggle_selected_bg);
            btnToggleServices.setTextColor(getResources().getColor(R.color.brand_red, null));
            btnToggleProducts.setBackgroundResource(android.R.color.transparent);
            btnToggleProducts.setTextColor(getResources().getColor(R.color.white, null));

            if (tvShopSubtitle != null) tvShopSubtitle.setText("Fire Safety & Maintenance Services");
            // Exclude extinguisher and sprinkler product category chips & items
            if (hsvCategoryChips != null) hsvCategoryChips.setVisibility(View.GONE);
            if (rvProducts != null) rvProducts.setVisibility(View.GONE);
            if (rvServicesShop != null) rvServicesShop.setVisibility(View.VISIBLE);
            if (ivFilter != null) ivFilter.setVisibility(View.GONE);
            if (etSearch != null) {
                etSearch.setHint("Search fire safety services...");
                filterServices(etSearch.getText().toString());
            }
        }
    }

    private void setupServices() {
        allServicesList.clear();
        // 4 confirmed services
        allServicesList.add(new ShopServiceItem(
                "1",
                "Extinguisher Refilling",
                "Refilling",
                "Fire Extinguisher Refilling & Maintenance",
                "Certified refilling and pressure maintenance for all extinguisher types.",
                R.drawable.ic_cat_powder,
                "Refilling"
        ));
        allServicesList.add(new ShopServiceItem(
                "2",
                "System Installation",
                "Installation",
                "Fire Protection & Sprinkler Installation",
                "Professional installation of fire sprinkler, alarm, and detection systems.",
                R.drawable.ic_coverage,
                "Installation"
        ));
        allServicesList.add(new ShopServiceItem(
                "3",
                "Safety Inspection",
                "Inspection",
                "Fire Safety Inspection & Assessment",
                "Comprehensive fire safety assessment and compliance certification.",
                R.drawable.ic_check_circle,
                "Inspection"
        ));
        allServicesList.add(new ShopServiceItem(
                "4",
                "On-site Safety Training",
                "Safety Training",
                "On-site Safety Training / Demo",
                "Live fire drills, extinguisher handling, and emergency response training.",
                R.drawable.ic_fire_type,
                "Training / Demo"
        ));

        servicesAdapter = new ShopServiceAdapter(allServicesList, service -> {
            ServiceRequestSheet.newInstance(service.getRequestServiceType())
                    .show(getParentFragmentManager(), "service_request");
        });

        if (rvServicesShop != null) {
            rvServicesShop.setAdapter(servicesAdapter);
        }
    }

    private void setupSearch() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (currentMode == ShopMode.PRODUCTS) {
                        filterProducts(s.toString());
                    } else {
                        filterServices(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        String q = query.toLowerCase(Locale.ROOT).trim();
        for (Product product : allProductsList) {
            if (q.isEmpty() || product.getName().toLowerCase(Locale.ROOT).contains(q)) {
                filteredList.add(product);
            }
        }
        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }

    private void filterServices(String query) {
        List<ShopServiceItem> filteredList = new ArrayList<>();
        String q = query.toLowerCase(Locale.ROOT).trim();
        for (ShopServiceItem item : allServicesList) {
            if (q.isEmpty() || item.getTitle().toLowerCase(Locale.ROOT).contains(q)
                    || item.getSubtitle().toLowerCase(Locale.ROOT).contains(q)
                    || item.getDescription().toLowerCase(Locale.ROOT).contains(q)) {
                filteredList.add(item);
            }
        }
        if (servicesAdapter != null) {
            servicesAdapter.updateList(filteredList);
        }
    }

    private void setupCategoryChips(View view) {
        TextView chipAll = view.findViewById(R.id.chipAll);
        TextView chipExtinguishers = view.findViewById(R.id.chipExtinguishers);
        TextView chipSprinklers = view.findViewById(R.id.chipSprinklers);
        TextView chipServices = view.findViewById(R.id.chipServices);

        if (chipAll == null) return;

        Runnable resetChips = () -> {
            chipAll.setBackgroundResource(R.drawable.chip_unselected_bg);
            chipAll.setTextColor(getResources().getColor(R.color.black, null));
            chipExtinguishers.setBackgroundResource(R.drawable.chip_unselected_bg);
            chipExtinguishers.setTextColor(getResources().getColor(R.color.black, null));
            chipSprinklers.setBackgroundResource(R.drawable.chip_unselected_bg);
            chipSprinklers.setTextColor(getResources().getColor(R.color.black, null));
            chipServices.setBackgroundResource(R.drawable.chip_unselected_bg);
            chipServices.setTextColor(getResources().getColor(R.color.brand_red, null));
        };

        chipAll.setOnClickListener(v -> {
            resetChips.run();
            chipAll.setBackgroundResource(R.drawable.chip_selected_bg);
            chipAll.setTextColor(getResources().getColor(R.color.white, null));
            if (adapter != null) adapter.updateList(new ArrayList<>(allProductsList));
        });

        chipExtinguishers.setOnClickListener(v -> {
            resetChips.run();
            chipExtinguishers.setBackgroundResource(R.drawable.chip_selected_bg);
            chipExtinguishers.setTextColor(getResources().getColor(R.color.white, null));
            filterByCategory("Extinguish");
        });

        chipSprinklers.setOnClickListener(v -> {
            resetChips.run();
            chipSprinklers.setBackgroundResource(R.drawable.chip_selected_bg);
            chipSprinklers.setTextColor(getResources().getColor(R.color.white, null));
            filterByCategory("Sprinkler");
        });

        chipServices.setOnClickListener(v -> {
            // Smoothly switch toggle to Services mode
            setShopMode(ShopMode.SERVICES);
        });
    }

    private void filterByCategory(String keyword) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProductsList) {
            String cat = p.getCategoryTag() != null ? p.getCategoryTag() : "";
            String name = p.getName() != null ? p.getName() : "";
            if (cat.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT)) ||
                name.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT))) {
                filtered.add(p);
            }
        }
        if (adapter != null) adapter.updateList(filtered);
    }

    private void setupFilter() {
        if (ivFilter != null) {
            ivFilter.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(requireContext(), v);
                popup.getMenu().add("All Products");
                popup.getMenu().add(getString(R.string.cat_fire_extinguishers));
                popup.getMenu().add(getString(R.string.cat_fire_sprinklers));
                popup.getMenu().add(getString(R.string.cat_fireman_equipments));
                popup.getMenu().add(getString(R.string.cat_fire_hose));
                popup.getMenu().add("🛠️ Services");

                popup.setOnMenuItemClickListener(item -> {
                    String title = item.getTitle().toString();
                    if (title.contains("Services")) {
                        setShopMode(ShopMode.SERVICES);
                    } else if (title.equals("All Products")) {
                        if (adapter != null) adapter.updateList(new ArrayList<>(allProductsList));
                    } else {
                        filterByCategory(title);
                    }
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

        adapter = new ProductAdapter(new ArrayList<>(), product -> {
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

        // Fetch live products from PostgreSQL backend (prioritizing API over fallback)
        fetchProductsFromBackend();
    }

    private void fetchProductsFromBackend() {
        ApiClient.getApiService().getProducts(null, null)
                .enqueue(new Callback<ProductListResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ProductListResponse> call,
                                           @NonNull Response<ProductListResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getProducts() != null) {
                            List<ProductDto> dtos = response.body().getProducts();
                            allProductsList.clear();
                            for (ProductDto dto : dtos) {
                                allProductsList.add(dto.toProduct());
                            }
                            if (adapter != null) {
                                adapter.updateList(new ArrayList<>(allProductsList));
                            }
                        } else {
                            loadOfflineFallbackProducts();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ProductListResponse> call, @NonNull Throwable t) {
                        // Offline or server not reachable; only then use filtered fallback
                        loadOfflineFallbackProducts();
                    }
                });
    }

    private void loadOfflineFallbackProducts() {
        allProductsList.clear();
        List<Integer> carousel = new ArrayList<>();
        carousel.add(R.drawable.logo);

        List<String> features = new ArrayList<>();
        features.add("UL Listed & Certified");
        features.add("Standard Safety Compliance");
        features.add("1-Year Warranty");

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

        if (adapter != null) {
            adapter.updateList(new ArrayList<>(allProductsList));
        }
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
