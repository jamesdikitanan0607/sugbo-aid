package com.sugboaid.donation.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sugboaid.donation.R;
import com.sugboaid.donation.utils.AndroidNotificationManager;
import com.sugboaid.donation.utils.AnimationUtils;
import com.sugboaid.donation.utils.NotificationPermissionHelper;
import com.sugboaid.utils.AccessibilityUtils;
import com.sugboaid.utils.DiagnosticLogger;
import com.sugboaid.utils.NetworkUtils;
import com.sugboaid.utils.OfflineQueueManager;
import com.sugboaid.utils.StartupDiagnosticManager;
import com.sugboaid.views.OfflineBannerView;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private NavController navController;
    private BottomNavigationView bottomNavigation;
    private OfflineBannerView offlineBanner;
    private FloatingActionButton fabDarkModeToggle;
    private AndroidNotificationManager androidNotificationManager;
    private OfflineQueueManager offlineQueueManager;
    private StartupDiagnosticManager diagnosticManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            // Initialize diagnostic manager first
            diagnosticManager = StartupDiagnosticManager.getInstance(this);
            diagnosticManager.initialize();
            diagnosticManager.logActivityStartup(TAG, getIntent());
            
            DiagnosticLogger.logStartup("MainActivity onCreate started");
            
            super.onCreate(savedInstanceState);
            
            // Set content view with error handling
            try {
                setContentView(R.layout.activity_main);
                DiagnosticLogger.logStartup("MainActivity layout set successfully");
            } catch (Exception e) {
                DiagnosticLogger.logCrash(e, "MainActivity setContentView failed");
                throw e;
            }
            
            // Initialize views and setup with comprehensive error handling
            initViews();
            setupListeners();
            setupNavigation();
            
            // Initialize notification manager
            try {
                androidNotificationManager = new AndroidNotificationManager(this);
                DiagnosticLogger.logStartup("AndroidNotificationManager initialized");
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to initialize AndroidNotificationManager", e);
            }
            
            // Initialize offline queue manager
            try {
                offlineQueueManager = OfflineQueueManager.getInstance(this);
                DiagnosticLogger.logStartup("OfflineQueueManager initialized");
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to initialize OfflineQueueManager", e);
            }
            
            // Request notification permission
            requestNotificationPermission();
            
            // Handle notification intents
            handleNotificationIntent(getIntent());
            
            // Handle authentication navigation
            handleAuthenticationNavigation();
            
            // Log successful startup completion
            diagnosticManager.logStartupCompletion(TAG);
            DiagnosticLogger.logStartup("MainActivity onCreate completed successfully");
            
        } catch (Exception e) {
            DiagnosticLogger.logCrash(e, "MainActivity onCreate failed");
            // Re-throw to maintain existing error handling behavior
            throw e;
        }
    }

    @Override
    protected void initViews() {
        DiagnosticLogger.logStartup("MainActivity initViews started");
        
        // Track view IDs for diagnostic logging
        int[] criticalViewIds = {
            R.id.bottom_navigation,
            R.id.offline_banner,
            R.id.fab_dark_mode_toggle
        };
        
        try {
            // Initialize views with comprehensive error handling
            bottomNavigation = findViewById(R.id.bottom_navigation);
            offlineBanner = findViewById(R.id.offline_banner);
            fabDarkModeToggle = findViewById(R.id.fab_dark_mode_toggle);
            
            // Validate critical views with detailed logging
            boolean allViewsValid = true;
            
            if (bottomNavigation == null) {
                DiagnosticLogger.logResourceError("view", "bottom_navigation", 
                    "Bottom navigation view not found in layout");
                allViewsValid = false;
            } else {
                DiagnosticLogger.logDebug(TAG, "Bottom navigation view initialized successfully");
            }
            
            if (offlineBanner == null) {
                DiagnosticLogger.logResourceError("view", "offline_banner", 
                    "Offline banner view not found in layout");
                allViewsValid = false;
            } else {
                DiagnosticLogger.logDebug(TAG, "Offline banner view initialized successfully");
            }
            
            if (fabDarkModeToggle == null) {
                DiagnosticLogger.logResourceError("view", "fab_dark_mode_toggle", 
                    "FAB dark mode toggle not found in layout");
                allViewsValid = false;
            } else {
                DiagnosticLogger.logDebug(TAG, "FAB dark mode toggle initialized successfully");
            }
            
            // Log view binding results
            diagnosticManager.logViewBindingEvent(TAG, criticalViewIds, allViewsValid, 
                allViewsValid ? null : "One or more critical views not found");
            
            if (!allViewsValid) {
                DiagnosticLogger.logWarning(TAG, "Some critical views are missing - app may not function properly");
                return;
            }
            
            // Setup accessibility features
            try {
                setupAccessibilityFeatures();
                DiagnosticLogger.logDebug(TAG, "Accessibility features setup completed");
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to setup accessibility features", e);
            }
            
            // Setup offline banner
            try {
                setupOfflineBanner();
                DiagnosticLogger.logDebug(TAG, "Offline banner setup completed");
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to setup offline banner", e);
            }
            
            // Update FAB icon based on current theme
            try {
                updateDarkModeToggleIcon();
                DiagnosticLogger.logDebug(TAG, "Dark mode toggle icon updated");
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to update dark mode toggle icon", e);
            }
            
            DiagnosticLogger.logStartup("MainActivity initViews completed successfully");
            
        } catch (Exception e) {
            DiagnosticLogger.logCrash(e, "MainActivity initViews failed");
            diagnosticManager.logViewBindingEvent(TAG, criticalViewIds, false, e.getMessage());
            throw e;
        }
    }

    @Override
    protected void setupListeners() {
        DiagnosticLogger.logStartup("MainActivity setupListeners started");
        
        try {
            setupDarkModeToggle();
            DiagnosticLogger.logDebug(TAG, "Dark mode toggle listener setup completed");
            DiagnosticLogger.logStartup("MainActivity setupListeners completed successfully");
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Error setting up listeners", e);
            throw e;
        }
    }

    private void setupNavigation() {
        DiagnosticLogger.logStartup("MainActivity setupNavigation started");
        
        try {
            // Get the NavHostFragment using the FragmentManager
            FragmentManager fragmentManager = getSupportFragmentManager();
            NavHostFragment navHostFragment = (NavHostFragment) fragmentManager.findFragmentById(R.id.nav_host_fragment);
            
            if (navHostFragment == null) {
                DiagnosticLogger.logError(TAG, "NavHostFragment not found in layout", null);
                throw new IllegalStateException("NavHostFragment not found in layout");
            }
            
            // Get the NavController from the NavHostFragment
            navController = navHostFragment.getNavController();
            
            if (navController == null) {
                DiagnosticLogger.logError(TAG, "NavController is null after findNavController", null);
                throw new IllegalStateException("NavController could not be initialized");
            }
            
            DiagnosticLogger.logDebug(TAG, "NavController obtained successfully");
            
            // Verify bottom navigation exists before connecting
            if (bottomNavigation == null) {
                DiagnosticLogger.logError(TAG, "Bottom navigation is null during setup", null);
                throw new IllegalStateException("Bottom navigation not initialized");
            }
            
            // Setup bottom navigation with NavController
            NavigationUI.setupWithNavController(bottomNavigation, navController);
            DiagnosticLogger.logDebug(TAG, "Bottom navigation connected to NavController");
            
            // Ensure initial destination is shown to avoid white screen on cold start
            try {
                if (navController.getCurrentDestination() == null) {
                    DiagnosticLogger.logDebug(TAG, "Current destination is null, forcing navigation to dashboard");
                    bottomNavigation.setSelectedItemId(R.id.dashboardFragment);
                    navController.navigate(R.id.dashboardFragment);
                }
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to force initial navigation to dashboard", e);
            }
            
            // Handle fragment lifecycle properly with comprehensive logging
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                String destinationName = getDestinationName(destination.getId());
                String previousDestination = "Unknown";
                
                // Log navigation event
                diagnosticManager.logNavigationEvent(
                    "Destination changed", 
                    previousDestination, 
                    destinationName, 
                    controller
                );
                
                // Handle specific fragment requirements
                try {
                    handleFragmentChange(destination.getId());
                    DiagnosticLogger.logDebug(TAG, "Fragment change handled for: " + destinationName);
                } catch (Exception e) {
                    DiagnosticLogger.logError(TAG, "Failed to handle fragment change for: " + destinationName, e);
                }
                
                // Announce navigation change for accessibility
                try {
                    announceNavigationChange(destination.getId());
                } catch (Exception e) {
                    DiagnosticLogger.logError(TAG, "Failed to announce navigation change", e);
                }
            });
            
            DiagnosticLogger.logStartup("MainActivity setupNavigation completed successfully");
            
        } catch (IllegalArgumentException e) {
            // Handle case where NavController is not found
            DiagnosticLogger.logError(TAG, "NavController not found", e);
            DiagnosticLogger.logResourceError("navigation", "nav_host_fragment", 
                "NavHostFragment not found in layout");
            
            // Fallback: try to setup navigation after a delay
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                DiagnosticLogger.logDebug(TAG, "Attempting delayed navigation setup");
                try {
                    navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                    NavigationUI.setupWithNavController(bottomNavigation, navController);
                    DiagnosticLogger.logDebug(TAG, "Delayed navigation setup successful");
                } catch (Exception ex) {
                    DiagnosticLogger.logError(TAG, "Delayed navigation setup failed", ex);
                }
            }, 100);
        } catch (Exception e) {
            DiagnosticLogger.logCrash(e, "MainActivity setupNavigation failed");
            throw e;
        }
    }

    private void handleFragmentChange(int destinationId) {
        // Handle specific fragment requirements like hiding/showing bottom nav
        if (bottomNavigation != null) {
            // Hide bottom navigation for authentication fragments
            if (destinationId == R.id.loginFragment || destinationId == R.id.signupFragment) {
                bottomNavigation.setVisibility(View.GONE);
            } else {
                // Show bottom navigation for main app fragments
                bottomNavigation.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onNetworkAvailable() {
        handleNetworkRestored();
    }

    @Override
    protected void onNetworkLost() {
        handleNetworkLost();
    }

    private void setupOfflineBanner() {
        if (offlineBanner != null) {
            offlineBanner.setOnSyncRequestListener(() -> {
                if (NetworkUtils.isNetworkAvailable(this)) {
                    startDataSync();
                } else {
                    showToast(getString(R.string.error_network_unavailable));
                }
            });
        }
    }

    private void handleNetworkLost() {
        if (offlineQueueManager != null && offlineBanner != null && prefsHelper != null) {
            int pendingActions = offlineQueueManager.getPendingActionCount();
            offlineBanner.showOfflineState(pendingActions);
            
            // Update offline mode in preferences
            prefsHelper.saveOfflineMode(true);
        }
    }

    private void handleNetworkRestored() {
        if (prefsHelper != null && offlineQueueManager != null && offlineBanner != null) {
            // Update offline mode in preferences
            prefsHelper.saveOfflineMode(false);
            
            // Check if there are pending actions to sync
            if (offlineQueueManager.hasPendingActions()) {
                // Auto-start sync if there are pending actions
                startDataSync();
            } else {
                // Just hide the banner if no pending actions
                offlineBanner.hide();
            }
        }
    }

    private void startDataSync() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            offlineBanner.showSyncErrorState(getString(R.string.error_network_unavailable));
            return;
        }

        offlineQueueManager.startSync(this, new OfflineQueueManager.SyncCallback() {
            @Override
            public void onSyncStarted() {
                runOnUiThread(() -> {
                    int totalActions = offlineQueueManager.getPendingActionCount();
                    offlineBanner.showSyncingState(0, totalActions);
                });
            }

            @Override
            public void onSyncProgress(int completed, int total) {
                runOnUiThread(() -> {
                    offlineBanner.updateSyncProgress(completed, total);
                });
            }

            @Override
            public void onSyncCompleted(boolean success, java.util.List<OfflineQueueManager.SyncConflict> conflicts) {
                runOnUiThread(() -> {
                    if (success && conflicts.isEmpty()) {
                        int syncedActions = offlineQueueManager.getPendingActionCount();
                        offlineBanner.showSyncCompletedState(true, syncedActions);
                        
                        // Clear processed actions
                        offlineQueueManager.clearProcessedActions();
                        
                        showToast(getString(R.string.success_data_synced));
                    } else if (!conflicts.isEmpty()) {
                        offlineBanner.showSyncErrorState(getString(R.string.sync_conflicts_detected));
                        // Handle conflicts - could show a dialog or navigate to conflict resolution screen
                    } else {
                        offlineBanner.showSyncCompletedState(false, 0);
                    }
                });
            }

            @Override
            public void onSyncError(String error) {
                runOnUiThread(() -> {
                    offlineBanner.showSyncErrorState(error);
                    showToast(getString(R.string.error_data_sync));
                });
            }
        });
    }

    private void setupDarkModeToggle() {
        if (fabDarkModeToggle != null) {
            fabDarkModeToggle.setOnClickListener(v -> {
                // Animate button press
                AnimationUtils.animateButtonPress(v);
                
                // Toggle dark mode after animation
                v.postDelayed(this::toggleDarkMode, AnimationUtils.DURATION_SHORT);
            });
            
            // Animate FAB entrance
            AnimationUtils.animateFabEntrance(fabDarkModeToggle);
        }
    }

    private void updateDarkModeToggleIcon() {
        if (fabDarkModeToggle != null && prefsHelper != null) {
            boolean isDarkMode = prefsHelper.getDarkModePreference();
            int iconRes = isDarkMode ? R.drawable.ic_light_mode : R.drawable.ic_dark_mode;
            fabDarkModeToggle.setImageResource(iconRes);
        }
    }
    
    private void requestNotificationPermission() {
        NotificationPermissionHelper.requestNotificationPermission(this, 
            new NotificationPermissionHelper.PermissionCallback() {
                @Override
                public void onPermissionGranted() {
                    // Permission granted, notifications will work
                }
                
                @Override
                public void onPermissionDenied() {
                    // Show dialog explaining the impact
                    NotificationPermissionHelper.showPermissionDeniedDialog(MainActivity.this);
                }
            });
    }
    
    private void handleNotificationIntent(android.content.Intent intent) {
        if (intent != null) {
            boolean openNotifications = intent.getBooleanExtra("open_notifications", false);
            boolean openReports = intent.getBooleanExtra("open_reports", false);
            boolean openInventory = intent.getBooleanExtra("open_inventory", false);
            boolean openTransparency = intent.getBooleanExtra("open_transparency", false);
            
            if (openNotifications) {
                // Navigate to notifications fragment
                navController.navigate(R.id.notificationsFragment);
            } else if (openReports) {
                navController.navigate(R.id.reportsFragment);
            } else if (openInventory) {
                navController.navigate(R.id.inventoryFragment);
            } else if (openTransparency) {
                navController.navigate(R.id.transparencyFragment);
            }
        }
    }
    

    
    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
        handleAuthenticationNavigation();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        NotificationPermissionHelper.handlePermissionResult(requestCode, permissions, grantResults,
            new NotificationPermissionHelper.PermissionCallback() {
                @Override
                public void onPermissionGranted() {
                    // Permission granted
                }
                
                @Override
                public void onPermissionDenied() {
                    NotificationPermissionHelper.showPermissionDeniedDialog(MainActivity.this);
                }
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    // Public method to get NavController for fragments
    public NavController getNavController() {
        return navController;
    }
    
    /**
     * Setup comprehensive accessibility features for the main activity
     */
    private void setupAccessibilityFeatures() {
        // Setup bottom navigation accessibility
        setupBottomNavigationAccessibility();
        
        // Setup dark mode toggle accessibility
        setupDarkModeToggleAccessibility();
        
        // Setup offline banner accessibility
        setupOfflineBannerAccessibility();
    }
    
    /**
     * Setup accessibility for bottom navigation
     */
    private void setupBottomNavigationAccessibility() {
        if (bottomNavigation != null) {
            // Set content description for the navigation container
            bottomNavigation.setContentDescription(getString(R.string.accessibility_navigation_container));
            
            // Setup individual navigation items
            for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
                View navItem = bottomNavigation.findViewById(bottomNavigation.getMenu().getItem(i).getItemId());
                if (navItem != null) {
                    String destination = bottomNavigation.getMenu().getItem(i).getTitle().toString();
                    AccessibilityUtils.setupNavigationAccessibility(navItem, destination, false);
                }
            }
        }
    }
    
    /**
     * Setup accessibility for dark mode toggle
     */
    private void setupDarkModeToggleAccessibility() {
        if (fabDarkModeToggle != null) {
            AccessibilityUtils.setupClickableAccessibility(
                fabDarkModeToggle,
                getString(R.string.toggle_dark_mode),
                getString(R.string.accessibility_action_toggle_theme)
            );
        }
    }
    
    /**
     * Setup accessibility for offline banner
     */
    private void setupOfflineBannerAccessibility() {
        if (offlineBanner != null) {
            AccessibilityUtils.setupLiveRegion(offlineBanner, 
                androidx.core.view.ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
        }
    }
    
    /**
     * Announce navigation changes for accessibility
     */
    private void announceNavigationChange(int destinationId) {
        String destination = getDestinationName(destinationId);
        if (destination != null) {
            AccessibilityUtils.announceNavigation(this, bottomNavigation, destination);
        }
    }
    
    /**
     * Get destination name from navigation ID
     */
    private String getDestinationName(int destinationId) {
        if (destinationId == R.id.dashboardFragment) {
            return getString(R.string.nav_dashboard);
        } else if (destinationId == R.id.inventoryFragment) {
            return getString(R.string.nav_inventory);
        } else if (destinationId == R.id.transparencyFragment) {
            return getString(R.string.nav_transparency);
        } else if (destinationId == R.id.reportsFragment) {
            return getString(R.string.nav_reports);
        } else if (destinationId == R.id.notificationsFragment) {
            return getString(R.string.nav_notifications);
        }
        return null;
    }

    /**
     * Handle authentication navigation based on intent extras
     */
    private void handleAuthenticationNavigation() {
        String startDestination = getIntent().getStringExtra("start_destination");
        String userRole = getIntent().getStringExtra("user_role");
        DiagnosticLogger.logDebug(TAG, "Handling authentication navigation to: " + startDestination + 
            ", user role: " + userRole);
        
        // Save user role if provided
        if (userRole != null && prefsHelper != null) {
            try {
                prefsHelper.saveUserRole(userRole);
                DiagnosticLogger.logDebug(TAG, "User role saved: " + userRole);
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to save user role", e);
            }
        }
        
        // Ensure navigation is done after the NavController is properly initialized
        // Use a more robust approach with retry mechanism
        performNavigationWithRetry(startDestination, 0);
    }
    
    /**
     * Perform navigation with retry mechanism to handle timing issues
     */
    private void performNavigationWithRetry(String startDestination, int retryCount) {
        final int MAX_RETRIES = 5;
        final int RETRY_DELAY = 300; // Increased delay
        
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (navController != null) {
                try {
                    // Verify NavController is ready by checking if it has a current destination
                    if (navController.getCurrentDestination() == null && retryCount < MAX_RETRIES) {
                        DiagnosticLogger.logDebug(TAG, "NavController not ready, retrying... (" + (retryCount + 1) + "/" + MAX_RETRIES + ")");
                        performNavigationWithRetry(startDestination, retryCount + 1);
                        return;
                    }
                    
                    if ("login".equals(startDestination)) {
                        // Navigate to login fragment
                        diagnosticManager.logNavigationEvent(
                            "Authentication navigation", "MainActivity", "LoginFragment", navController
                        );
                        navController.navigate(R.id.loginFragment);
                        DiagnosticLogger.logDebug(TAG, "Navigated to login fragment");
                    } else if ("dashboard".equals(startDestination)) {
                        // Navigate to dashboard fragment (default behavior)
                        diagnosticManager.logNavigationEvent(
                            "Authentication navigation", "MainActivity", "DashboardFragment", navController
                        );
                        navController.navigate(R.id.dashboardFragment);
                        DiagnosticLogger.logDebug(TAG, "Navigated to dashboard fragment");
                    } else {
                        DiagnosticLogger.logDebug(TAG, "No specific destination - using navigation graph default");
                    }
                    // If no specific destination, let the navigation graph handle the default
                } catch (IllegalArgumentException e) {
                    DiagnosticLogger.logError(TAG, "Navigation destination not found", e);
                    // Fallback: try to navigate to login as default
                    try {
                        navController.navigate(R.id.loginFragment);
                        DiagnosticLogger.logDebug(TAG, "Fallback navigation to login successful");
                    } catch (Exception fallbackException) {
                        DiagnosticLogger.logError(TAG, "Fallback navigation failed", fallbackException);
                    }
                } catch (Exception e) {
                    DiagnosticLogger.logError(TAG, "Authentication navigation error", e);
                    if (retryCount < MAX_RETRIES) {
                        DiagnosticLogger.logDebug(TAG, "Retrying navigation due to error... (" + (retryCount + 1) + "/" + MAX_RETRIES + ")");
                        performNavigationWithRetry(startDestination, retryCount + 1);
                    }
                }
            } else {
                DiagnosticLogger.logWarning(TAG, "NavController is null during authentication navigation");
                if (retryCount < MAX_RETRIES) {
                    DiagnosticLogger.logDebug(TAG, "Retrying navigation setup... (" + (retryCount + 1) + "/" + MAX_RETRIES + ")");
                    // Retry navigation setup
                    try {
                        setupNavigation();
                        performNavigationWithRetry(startDestination, retryCount + 1);
                    } catch (Exception retryException) {
                        DiagnosticLogger.logError(TAG, "Navigation retry failed", retryException);
                        performNavigationWithRetry(startDestination, retryCount + 1);
                    }
                } else {
                    DiagnosticLogger.logError(TAG, "Max retries reached, navigation failed", null);
                }
            }
        }, retryCount == 0 ? RETRY_DELAY : RETRY_DELAY * (retryCount + 1));
    }
}