package com.example.flamepro;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigationrail.NavigationRailView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private NavigationRailView navigationRail;
    private View cartNotificationBar;
    private TextView tvTotalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int bottomPadding = (bottomNavigation != null) ? 0 : systemBars.bottom;
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding); 
            return insets;
        });

        initializeViews();
        setupNavigation();
        setupCartNotificationBar();

        // Load default fragment
        if (savedInstanceState == null) {
            String userName = getIntent().getStringExtra("USER_NAME");
            loadFragment(DashboardFragment.newInstance(userName));
        }

        UserManager.getInstance().loadFromPreferences(this);
        syncUserOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncUserOrders();
    }

    private void syncUserOrders() {
        int userId = UserManager.getInstance().getUserId();
        if (userId <= 0) {
            UserManager.getInstance().loadFromPreferences(this);
            userId = UserManager.getInstance().getUserId();
        }
        if (userId > 0) {
            com.example.flamepro.network.ApiClient.getApiService().getUserOrders(userId).enqueue(new retrofit2.Callback<com.example.flamepro.network.models.OrderListResponse>() {
                @Override
                public void onResponse(@androidx.annotation.NonNull retrofit2.Call<com.example.flamepro.network.models.OrderListResponse> call,
                                       @androidx.annotation.NonNull retrofit2.Response<com.example.flamepro.network.models.OrderListResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getOrders() != null) {
                        OrderManager.getInstance().syncFromBackend(response.body().getOrders());
                    }
                }

                @Override
                public void onFailure(@androidx.annotation.NonNull retrofit2.Call<com.example.flamepro.network.models.OrderListResponse> call,
                                      @androidx.annotation.NonNull Throwable t) {}
            });
        }
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        navigationRail = findViewById(R.id.navigationRail);
        cartNotificationBar = findViewById(R.id.cartNotificationBar);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
    }

    private void setupCartNotificationBar() {
        if (cartNotificationBar == null) return;

        CartManager.getInstance().addListener(totalItems -> {
            updateCartNotificationVisibility();
            if (tvTotalPrice != null) {
                tvTotalPrice.setText(String.format(Locale.getDefault(), "₱ %.2f", CartManager.getInstance().getTotalPrice()));
            }
        });

        findViewById(R.id.btnCheckout).setOnClickListener(v -> loadFragment(new CartFragment()));
    }

    private void updateCartNotificationVisibility() {
        if (cartNotificationBar == null) return;
        cartNotificationBar.setVisibility(View.GONE);
    }

    private void setupNavigation() {
        if (bottomNavigation != null) {
            setupBottomNavigation();
        } else if (navigationRail != null) {
            setupNavigationRail();
        }
    }

    private void setupBottomNavigation() {
        BadgeDrawable badge = bottomNavigation.getOrCreateBadge(R.id.nav_cart);
        badge.setBackgroundColor(getColor(R.color.brand_red));
        badge.setBadgeTextColor(getColor(R.color.white));

        CartManager.getInstance().addListener(totalItems -> {
            badge.setNumber(totalItems);
            badge.setVisible(totalItems > 0);
        });

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            final Fragment selectedFragment;
            if (itemId == R.id.nav_home) {
                String userName = getIntent().getStringExtra("USER_NAME");
                selectedFragment = DashboardFragment.newInstance(userName);
            } else if (itemId == R.id.nav_shop) {
                selectedFragment = new ShopFragment();
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = new CartFragment();
            } else if (itemId == R.id.nav_order) {
                selectedFragment = new MyOrdersFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else {
                selectedFragment = null;
            }
            
            if (selectedFragment != null) {
                loadTopLevelFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void setupNavigationRail() {
        BadgeDrawable badge = navigationRail.getOrCreateBadge(R.id.nav_cart);
        badge.setBackgroundColor(getColor(R.color.brand_red));
        badge.setBadgeTextColor(getColor(R.color.white));

        CartManager.getInstance().addListener(totalItems -> {
            badge.setNumber(totalItems);
            badge.setVisible(totalItems > 0);
        });

        navigationRail.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            final Fragment selectedFragment;
            if (itemId == R.id.nav_home) {
                String userName = getIntent().getStringExtra("USER_NAME");
                selectedFragment = DashboardFragment.newInstance(userName);
            } else if (itemId == R.id.nav_shop) {
                selectedFragment = new ShopFragment();
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = new CartFragment();
            } else if (itemId == R.id.nav_order) {
                selectedFragment = new MyOrdersFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else {
                selectedFragment = null;
            }
            
            if (selectedFragment != null) {
                loadTopLevelFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    public void loadTopLevelFragment(Fragment fragment) {
        // Clear backstack when switching top level tabs
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
        
        getWindow().getDecorView().post(this::updateCartNotificationVisibility);
    }

    public void loadFragment(Fragment fragment) {
        loadFragment(fragment, false);
    }

    public void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.nav_host_fragment, fragment);
        
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        
        transaction.commit();
        
        // Use post to ensure the fragment transaction is completed before checking visibility
        getWindow().getDecorView().post(this::updateCartNotificationVisibility);
    }

    // Removed the problematic onBackPressed override, will use default activity behavior 
    // which works well with FragmentManager's backstack when addToBackStack is used correctly.


    public void setBottomNavigationVisibility(int visibility) {
        if (bottomNavigation != null) {
            bottomNavigation.setVisibility(visibility);
        }
        View navDivider = findViewById(R.id.navDivider);
        if (navDivider != null) {
            navDivider.setVisibility(visibility);
        }
    }
}
