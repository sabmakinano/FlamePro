package com.example.flamepro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flamepro.network.ApiClient;
import com.example.flamepro.network.models.OrderDto;
import com.example.flamepro.network.models.OrderListResponse;
import com.example.flamepro.network.models.ProductDto;
import com.example.flamepro.network.models.ProductListResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private RecyclerView rvShopRow1, rvShopRow2, rvServices;
    private AutoCompleteTextView actvBranch;
    private View notificationLayout;
    private TextView tvUserName;
    private String userName;

    public static DashboardFragment newInstance(String name) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString("user_name", name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userName = getArguments().getString("user_name");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupWelcome(userName);
        setupDropdowns();
        setupRecyclerViews();
        setupServicesRecyclerView();
        setupClickListeners();
        setupTabletStats(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            setupTabletStats(getView());
        }
        syncOrdersWithBackend();
    }

    private void syncOrdersWithBackend() {
        int userId = UserManager.getInstance().getUserId();
        if (userId <= 0) return;

        ApiClient.getApiService().getUserOrders(userId).enqueue(new Callback<OrderListResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderListResponse> call, @NonNull Response<OrderListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getOrders() != null) {
                    List<Order> synced = new ArrayList<>();
                    for (OrderDto dto : response.body().getOrders()) {
                        synced.add(Order.fromDto(dto));
                    }
                    OrderManager.getInstance().setOrders(synced);
                    if (getView() != null) {
                        setupTabletStats(getView());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderListResponse> call, @NonNull Throwable t) {
                // Keep local orders
            }
        });
    }

    private void initializeViews(View view) {
        rvShopRow1 = view.findViewById(R.id.rvShopRow1);
        rvShopRow2 = view.findViewById(R.id.rvShopRow2);
        rvServices = view.findViewById(R.id.rvServices);
        actvBranch = view.findViewById(R.id.actvBranch);
        notificationLayout = view.findViewById(R.id.notificationLayout);
        tvUserName = view.findViewById(R.id.tvUserName);
    }

    private void setupWelcome(String name) {
        if (tvUserName == null) return;
        
        String currentName = UserManager.getInstance().getFirstName();
        if (currentName != null && !currentName.isEmpty()) {
            tvUserName.setText(currentName);
        } else if (name != null && !name.isEmpty()) {
            String displayName = name;
            if (name.contains("@")) {
                displayName = name.split("@")[0];
            }
            tvUserName.setText(displayName);
        } else {
            tvUserName.setText("User");
        }
    }

    private void setupServicesRecyclerView() {
        List<Service> services = new ArrayList<>();
        services.add(new Service("Refilling", R.drawable.ic_cat_powder));
        services.add(new Service("Maintenance", R.drawable.ic_coverage));
        services.add(new Service("Installation", R.drawable.ic_cat_foam));
        services.add(new Service("Inspection", R.drawable.ic_check_circle));
        services.add(new Service("Delivery", R.drawable.ic_delivery));

        ServiceAdapter adapter = new ServiceAdapter(services, service -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                BottomNavigationView nav = mainActivity.findViewById(R.id.bottomNavigation);

                String name = service.getName();
                if (name.equals("Delivery")) {
                    if (nav != null) nav.setSelectedItemId(R.id.nav_order);
                    mainActivity.loadTopLevelFragment(new MyOrdersFragment());
                } else {
                    // Open service request form for Refilling, Installation, Maintenance, Inspection
                    ServiceRequestSheet.newInstance(name)
                            .show(getParentFragmentManager(), "service_request");
                }
            }
        });

        if (rvServices != null) {
            rvServices.setAdapter(adapter);
        }
    }

    private void setupClickListeners() {
        if (notificationLayout != null) {
            notificationLayout.setOnClickListener(v -> 
                Toast.makeText(getContext(), "You have new notifications!", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupDropdowns() {
        String[] branches = {getString(R.string.cebu), getString(R.string.bohol), getString(R.string.dumaguete)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, branches);
        actvBranch.setAdapter(adapter);

        View container = getView() != null ? getView().findViewById(R.id.dropdownContainer) : null;
        if (container != null) {
            container.setOnClickListener(v -> actvBranch.showDropDown());
        }
        actvBranch.setOnClickListener(v -> actvBranch.showDropDown());
    }

    public static boolean isExtinguisher(Product p) {
        if (p == null) return false;
        String cat = p.getCategoryTag() != null ? p.getCategoryTag().toLowerCase(Locale.ROOT) : "";
        String name = p.getName() != null ? p.getName().toLowerCase(Locale.ROOT) : "";
        return cat.contains("extinguish") || name.contains("extinguish")
                || cat.contains("powder") || name.contains("powder")
                || name.contains("co2") || name.contains("foam")
                || name.contains("hfc");
    }

    private void setupRecyclerViews() {
        ExtinguisherAdapter.OnProductClickListener listener = product -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(ProductDetailsFragment.newInstance(product), true);
            }
        };

        ExtinguisherAdapter adapter1 = new ExtinguisherAdapter(new ArrayList<>(), listener);
        ExtinguisherAdapter adapter2 = new ExtinguisherAdapter(new ArrayList<>(), listener);

        if (rvShopRow1 != null) {
            rvShopRow1.setAdapter(adapter1);
        }
        if (rvShopRow2 != null) {
            rvShopRow2.setAdapter(adapter2);
        }

        // Prioritize fetching live products from API; only fallback if offline
        ApiClient.getApiService().getProducts(null, null)
                .enqueue(new Callback<ProductListResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ProductListResponse> call,
                                           @NonNull Response<ProductListResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getProducts() != null) {
                            List<ProductDto> dtos = response.body().getProducts();
                            List<Product> row1 = new ArrayList<>();
                            List<Product> row2 = new ArrayList<>();
                            for (ProductDto dto : dtos) {
                                Product p = dto.toProduct();
                                if (isExtinguisher(p)) {
                                    row1.add(p);
                                } else {
                                    row2.add(p);
                                }
                            }
                            adapter1.updateList(row1);
                            adapter2.updateList(row2);
                        } else {
                            applyOfflineFallback(adapter1, adapter2);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ProductListResponse> call, @NonNull Throwable t) {
                        applyOfflineFallback(adapter1, adapter2);
                    }
                });
    }

    private void applyOfflineFallback(ExtinguisherAdapter adapter1, ExtinguisherAdapter adapter2) {
        List<Product> dummy = getDummyProducts();
        List<Product> row1 = new ArrayList<>();
        List<Product> row2 = new ArrayList<>();
        for (Product p : dummy) {
            if (isExtinguisher(p)) {
                row1.add(p);
            } else {
                row2.add(p);
            }
        }
        if (adapter1 != null) adapter1.updateList(row1);
        if (adapter2 != null) adapter2.updateList(row2);
    }

    private List<Product> getDummyProducts() {
        List<Product> products = new ArrayList<>();
        List<Integer> carousel = new ArrayList<>();
        carousel.add(R.drawable.logo);
        
        List<String> features = new ArrayList<>();
        features.add("Certified Safety");

        // Row 1 (Extinguishers)
        products.add(new Product("ABC Dry Powder 5 lb", "₱ 45.00", "₱ 55.00", "18% OFF", 4.8f, 24, R.drawable.logo, carousel, "5 lb", "Dry Powder", "10-15 ft", features, "Fire Extinguishers", true));
        products.add(new Product("CO2 Extinguisher 10 lb", "₱ 89.99", "₱ 110.00", "18% OFF", 4.7f, 15, R.drawable.logo, carousel, "10 lb", "CO2", "8-12 ft", features, "Fire Extinguishers", true));
        products.add(new Product("Foam Extinguisher 9 L", "₱ 59.50", "₱ 75.00", "20% OFF", 4.5f, 10, R.drawable.logo, carousel, "9L", "Foam", "12-18 ft", features, "Fire Extinguishers", true));
        
        // Row 2 (Sprinklers / Fireman Equipment / Accessories)
        products.add(new Product("Automatic Sprinkler Head", "₱ 24.99", "₱ 30.00", "16% OFF", 4.9f, 42, R.drawable.logo, carousel, "0.5 lb", "Automatic", "200 sq ft", features, "Fire Sprinklers", true));
        products.add(new Product("Fireman Helmet (Pro)", "₱ 120.00", "₱ 150.00", "20% OFF", 5.0f, 8, R.drawable.logo, carousel, "3 lb", "Protective", "N/A", features, "Fireman Equipments", true));
        products.add(new Product("Fire Hose Reel 30 m", "₱ 199.99", "₱ 250.00", "20% OFF", 4.6f, 5, R.drawable.logo, carousel, "15 lb", "Manual", "30m", features, "Fire Hose", true));
        products.add(new Product("Fire Blanket 1.2 x 1.2 m", "₱ 15.99", "₱ 20.00", "20% OFF", 4.8f, 56, R.drawable.logo, carousel, "1 lb", "Fiberglass", "1.2x1.2m", features, "Safety", true));
        products.add(new Product("Smoke Detector", "₱ 12.00", "₱ 18.00", "33% OFF", 4.4f, 120, R.drawable.logo, carousel, "0.3 lb", "Electronics", "Room", features, "Safety", true));
        products.add(new Product("First Aid Kit", "₱ 65.00", "₱ 85.00", "23% OFF", 4.9f, 45, R.drawable.logo, carousel, "5 lb", "Medical", "Body", features, "Safety", true));
        
        return products;
    }

    private void setupTabletStats(View view) {
        if (view == null) return;

        List<Order> orders = OrderManager.getInstance().getOrders();
        int total = orders.size();
        int pending = 0;
        int inTransit = 0;
        int delivered = 0;

        for (Order o : orders) {
            if (o.getStatus() == Order.OrderStatus.PENDING) {
                pending++;
            } else if (o.getStatus() == Order.OrderStatus.SHIPPED) {
                inTransit++;
            } else if (o.getStatus() == Order.OrderStatus.DELIVERED) {
                delivered++;
            }
        }

        // Tablet stats
        View statTotal = view.findViewById(R.id.statTotal);
        if (statTotal != null) {
            updateStatRow(statTotal, getString(R.string.total_orders), String.valueOf(total));
            updateStatRow(view.findViewById(R.id.statPending), getString(R.string.pending), String.valueOf(pending));
            updateStatRow(view.findViewById(R.id.statTransit), getString(R.string.in_transit), String.valueOf(inTransit));
            updateStatRow(view.findViewById(R.id.statDelivered), getString(R.string.delivered), String.valueOf(delivered));
        }

        // Phone stats
        TextView tvStatTotal = view.findViewById(R.id.tvStatTotalValue);
        if (tvStatTotal != null) tvStatTotal.setText(String.valueOf(total));
        TextView tvStatPending = view.findViewById(R.id.tvStatPendingValue);
        if (tvStatPending != null) tvStatPending.setText(String.valueOf(pending));
        TextView tvStatTransit = view.findViewById(R.id.tvStatTransitValue);
        if (tvStatTransit != null) tvStatTransit.setText(String.valueOf(inTransit));
        TextView tvStatDelivered = view.findViewById(R.id.tvStatDeliveredValue);
        if (tvStatDelivered != null) tvStatDelivered.setText(String.valueOf(delivered));
    }

    private void updateStatRow(View view, String label, String value) {
        if (view != null) {
            TextView tvLabel = view.findViewById(R.id.tvStatLabel);
            TextView tvVal = view.findViewById(R.id.tvStatValue);
            if (tvLabel != null) tvLabel.setText(label);
            if (tvVal != null) tvVal.setText(value);
        }
    }
}
