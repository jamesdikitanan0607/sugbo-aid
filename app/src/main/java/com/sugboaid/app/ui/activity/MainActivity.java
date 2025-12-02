package com.sugboaid.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.sugboaid.app.R;
import com.sugboaid.app.manager.AuthManager;
import com.sugboaid.app.ui.fragment.DashboardFragment;
import com.sugboaid.app.ui.fragment.DonationFragment;
import com.sugboaid.app.ui.fragment.InventoryFragment;
import com.sugboaid.app.ui.fragment.TransparencyFragment;
import com.sugboaid.app.util.Constants;
import com.sugboaid.app.util.ThemeUtils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigation;
    private Toolbar toolbar;
    private AuthManager authManager;
    private boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply theme
        ThemeUtils.applyTheme(this);
        
        // Check authentication
        authManager = AuthManager.getInstance(this);
        if (!authManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }
        
        // Determine if device is tablet
        isTablet = ThemeUtils.isTablet(this);
        
        setContentView(R.layout.activity_main);
        
        initViews();
        setupNavigation();
        setupToolbar();
        
        // Load initial fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupNavigation() {
        if (isTablet) {
            // Use navigation drawer for tablets
            bottomNavigation.setVisibility(android.view.View.GONE);
            navigationView.setNavigationItemSelectedListener(this);
            setupNavigationDrawer();
        } else {
            // Use bottom navigation for phones
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            navigationView.setVisibility(android.view.View.GONE);
            setupBottomNavigation();
        }
    }

    private void setupNavigationDrawer() {
        // Configure navigation drawer based on user role
        Menu menu = navigationView.getMenu();
        configureMenuBasedOnRole(menu);
    }

    private void setupBottomNavigation() {
        // Configure bottom navigation based on user role
        Menu menu = bottomNavigation.getMenu();
        configureMenuBasedOnRole(menu);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            return handleNavigationItemSelected(item);
        });
    }

    private void configureMenuBasedOnRole(Menu menu) {
        String userRole = authManager.getCurrentUser().getRole();
        
        // Show/hide menu items based on user role
        MenuItem donationItem = menu.findItem(R.id.nav_donation);
        MenuItem inventoryItem = menu.findItem(R.id.nav_inventory);
        MenuItem dashboardItem = menu.findItem(R.id.nav_dashboard);
        
        if (donationItem != null) {
            donationItem.setVisible(authManager.canAccessFeature(Constants.FEATURE_DONATION_POS));
        }
        
        if (inventoryItem != null) {
            inventoryItem.setVisible(authManager.canAccessFeature(Constants.FEATURE_INVENTORY_MANAGEMENT));
        }
        
        if (dashboardItem != null) {
            dashboardItem.setVisible(authManager.canAccessFeature(Constants.FEATURE_ANALYTICS_DASHBOARD));
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return handleNavigationItemSelected(item);
    }

    private boolean handleNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        
        int itemId = item.getItemId();
        if (itemId == R.id.nav_dashboard) {
            fragment = new DashboardFragment();
        } else if (itemId == R.id.nav_donation) {
            fragment = new DonationFragment();
        } else if (itemId == R.id.nav_inventory) {
            fragment = new InventoryFragment();
        } else if (itemId == R.id.nav_transparency) {
            fragment = new TransparencyFragment();
        } else if (itemId == R.id.nav_settings) {
            // Navigate to settings
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (itemId == R.id.nav_logout) {
            performLogout();
            return true;
        }
        
        if (fragment != null) {
            loadFragment(fragment);
            
            // Close drawer if tablet
            if (isTablet && drawerLayout != null) {
                drawerLayout.closeDrawers();
            }
            
            return true;
        }
        
        return false;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == android.R.id.home) {
            if (isTablet && drawerLayout != null) {
                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.closeDrawer(navigationView);
                } else {
                    drawerLayout.openDrawer(navigationView);
                }
            }
            return true;
        } else if (itemId == R.id.action_notifications) {
            // Handle notifications
            return true;
        } else if (itemId == R.id.action_search) {
            // Handle search
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {
        authManager.logout();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isTablet && drawerLayout != null && drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            super.onBackPressed();
        }
    }
}