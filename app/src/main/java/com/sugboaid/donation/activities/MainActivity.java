package com.sugboaid.donation.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;

import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.lifecycle.ViewModelProvider;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sugboaid.donation.R;
import com.sugboaid.donation.viewmodels.DashboardViewModel;
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
    private NavController.OnDestinationChangedListener destinationChangedListener;
    private BottomNavigationView bottomNavigation;
    private OfflineBannerView offlineBanner;
    private FloatingActionButton fabDarkModeToggle;
    private AndroidNotificationManager androidNotificationManager;
    private OfflineQueueManager offlineQueueManager;
    private StartupDiagnosticManager diagnosticManager;
    private DashboardViewModel dashboardViewModel;

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

    private void setupAccessibilityFeatures() {
        try {
            View root = findViewById(android.R.id.content);
            if (root != null) {
                AccessibilityUtils.enableImportantForAccessibility(root);
            }
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Error in setupAccessibilityFeatures", e);
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

            // Update Admin visibility based on role
            try {
                updateAdminMenuVisibility();
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to update admin menu visibility", e);
            }

            // Avoid reloading current destination when the same tab is reselected
            bottomNavigation.setOnItemReselectedListener(item -> {
                // no-op to preserve current fragment state
            });
            
            // Handle fragment lifecycle properly with comprehensive logging
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination != null) {
                    DiagnosticLogger.logDebug(TAG, "Navigation: Destination changed to " + destination.getLabel());
                    announceNavigationChange(destination.getId());
                    
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

                    // BottomNavigation selection is auto-synced by NavigationUI; avoid manual updates to prevent loops
                    
                    // Announce navigation change for accessibility
                    try {
                        announceNavigationChange(destination.getId());
                    } catch (Exception e) {
                        DiagnosticLogger.logError(TAG, "Failed to announce navigation change", e);
                    }
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

    /**
     * Preload Dashboard data so it's ready when shown.
     */
    private void preloadDashboard() {
        try {
            if (dashboardViewModel == null) {
                dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
            }
            dashboardViewModel.refreshData();
            DiagnosticLogger.logDebug(TAG, "Dashboard preloading triggered");
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Failed to preload dashboard", e);
        }
    }

    private void handleFragmentChange(int destinationId) {
        // Handle specific fragment requirements like hiding/showing bottom nav
        if (bottomNavigation != null) {
            // Hide bottom navigation for authentication and loading fragments
            if (destinationId == R.id.loginFragment || destinationId == R.id.signupFragment || destinationId == R.id.postAuthLoadingFragment) {
                bottomNavigation.setVisibility(View.GONE);
            } else {
                // Show bottom navigation for main app fragments
                bottomNavigation.setVisibility(View.VISIBLE);
            }
        }
        if (destinationId == R.id.dashboardFragment) {
            try {
                if (dashboardViewModel == null) {
                    dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
                }
                boolean needEmit = true;
                try {
                    needEmit = dashboardViewModel.getDashboardStatistics().getValue() == null
                        || !dashboardViewModel.hasRecentActivities();
                } catch (Exception ignored) { }
                if (needEmit) {
                    dashboardViewModel.forceRefresh();
                }
            } catch (Exception ignored) { }
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
                    NotificationPermissionHelper.showPermissionDeniedDialog(MainActivity.this);
                }
            });
    }
    
    private void handleNotificationIntent(android.content.Intent intent) {
        if (intent != null && navController != null) {
            boolean openNotifications = intent.getBooleanExtra("open_notifications", false);
            boolean openReports = intent.getBooleanExtra("open_reports", false);
            boolean openInventory = intent.getBooleanExtra("open_inventory", false);
            boolean openTransparency = intent.getBooleanExtra("open_transparency", false);
            try {
                if (openNotifications) {
                    navController.navigate(R.id.notificationsFragment);
                } else if (openReports) {
                    navController.navigate(R.id.reportsFragment);
                } else if (openInventory) {
                    navController.navigate(R.id.inventoryFragment);
                } else if (openTransparency) {
                    navController.navigate(R.id.transparencyFragment);
                }
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Notification intent navigation failed", e);
            }
        }
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
        handleAuthenticationNavigation();
        // Role may change across intents; refresh admin visibility
        try { updateAdminMenuVisibility(); } catch (Exception ignored) {}
    }

    private void updateAdminMenuVisibility() {
        if (bottomNavigation == null) return;
        try {
            String role = prefsHelper != null ? prefsHelper.getUserRole() : null;
            boolean isAdmin = "Admin".equals(role);
            android.view.Menu menu = bottomNavigation.getMenu();
            android.view.MenuItem adminItem = menu.findItem(R.id.adminDashboardFragment);
            if (adminItem != null) {
                adminItem.setVisible(isAdmin);
            }
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Error updating admin menu visibility", e);
        }
    }
    /**
     * Handle authentication navigation based on intent extras
     */
    private void handleAuthenticationNavigation() {
        try {
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
            
            // If no specific destination, default to login
            if (startDestination == null || startDestination.isEmpty()) {
                startDestination = "login";
            }
            
            // Ensure navigation is done after the NavController is properly initialized
            // Use a more robust approach with retry mechanism
            performNavigationWithRetry(startDestination, 0);
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Error in handleAuthenticationNavigation", e);
            // Fallback to login screen if there's an error
            performNavigationWithRetry("login", 0);
        }
    }
    
    /**
     * Perform navigation with retry mechanism to handle timing issues
     */
    private void performNavigationWithRetry(String startDestination, int retryCount) {
        final int MAX_RETRIES = 5;
        final int RETRY_DELAY = 300; // Increased delay
        long delay = (retryCount == 0) ? RETRY_DELAY : RETRY_DELAY * (long)(retryCount + 1);
        
        // Ensure we're on the main thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(() -> performNavigationWithRetry(startDestination, retryCount));
            return;
        }
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (navController == null) {
                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
                if (navHostFragment != null) {
                    try {
                        navController = navHostFragment.getNavController();
                    } catch (IllegalStateException e) {
                        DiagnosticLogger.logError(TAG, "Error getting NavController", e);
                    }
                }
            }
            
            if (navController == null) {
                if (retryCount < MAX_RETRIES) {
                    DiagnosticLogger.logDebug(TAG, "NavController is null, retrying... (" + (retryCount + 1) + "/" + MAX_RETRIES + ")");
                    performNavigationWithRetry(startDestination, retryCount + 1);
                } else {
                    DiagnosticLogger.logError(TAG, "Failed to initialize NavController after " + MAX_RETRIES + " attempts", null);
                }
                return;
            }
        
        // Proceed even if currentDestination is null; we'll set the graph explicitly
        if (navController.getCurrentDestination() == null) {
            DiagnosticLogger.logDebug(TAG, "NavController has no current destination yet; proceeding to set graph explicitly");
        }
        
        try {
            if ("login".equals(startDestination)) {
                // Switch to the dedicated auth graph (login is its startDestination)
                try {
                    NavGraph authGraph = navController.getNavInflater().inflate(R.navigation.auth_nav_graph);
                    navController.setGraph(authGraph);
                    DiagnosticLogger.logDebug(TAG, "Set navigation graph to auth_nav_graph");
                    if (diagnosticManager != null) {
                        diagnosticManager.logNavigationEvent(
                            "Authentication navigation", "MainActivity", "LoginFragment", navController
                        );
                    }
                } catch (Exception e) {
                    DiagnosticLogger.logError(TAG, "Error setting up auth navigation graph", e);
                    throw e;
                }
            } else if ("signup".equals(startDestination)) {
                // Switch to auth graph, then navigate to signup explicitly
                try {
                    NavGraph authGraph = navController.getNavInflater().inflate(R.navigation.auth_nav_graph);
                    navController.setGraph(authGraph);
                    DiagnosticLogger.logDebug(TAG, "Set navigation graph to auth_nav_graph (signup)");
                    try {
                        navController.navigate(R.id.signupFragment);
                    } catch (Exception navEx) {
                        DiagnosticLogger.logError(TAG, "Explicit navigate to signupFragment failed", navEx);
                    }
                    if (diagnosticManager != null) {
                        diagnosticManager.logNavigationEvent(
                            "Authentication navigation", "MainActivity", "SignupFragment", navController
                        );
                    }
                } catch (Exception e) {
                    DiagnosticLogger.logError(TAG, "Error setting up auth navigation graph for signup", e);
                    throw e;
                }
            } else if ("dashboard".equals(startDestination)) {
                // Switch to main graph; show loading screen which will preload and then navigate to dashboard
                try {
                    NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);
                    navController.setGraph(navGraph);
                    DiagnosticLogger.logDebug(TAG, "Set navigation graph to nav_graph");
                    try {
                        NavOptions opts = new NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .setLaunchSingleTop(true)
                            .build();
                        navController.navigate(R.id.postAuthLoadingFragment, null, opts);
                    } catch (Exception navEx) {
                        DiagnosticLogger.logError(TAG, "Navigate to PostAuthLoadingFragment failed", navEx);
                    }
                    if (diagnosticManager != null) {
                        diagnosticManager.logNavigationEvent(
                            "Authentication navigation", "MainActivity", "PostAuthLoadingFragment", navController
                        );
                    }
                } catch (Exception graphEx) {
                    DiagnosticLogger.logError(TAG, "Failed to set main graph for loading", graphEx);
                    if (retryCount < MAX_RETRIES) {
                        performNavigationWithRetry(startDestination, retryCount + 1);
                        return;
                    }
                }
            } else {
                DiagnosticLogger.logDebug(TAG, "No specific destination - using navigation graph default");
            }
            // If no specific destination, let the navigation graph handle the default
        } catch (IllegalArgumentException e) {
                    DiagnosticLogger.logError(TAG, "Navigation destination not found", e);
                    // Fallback: try to navigate to login as default
                    try {
                        try {
                            NavGraph authGraph = navController.getNavInflater().inflate(R.navigation.auth_nav_graph);
                            navController.setGraph(authGraph);
                            DiagnosticLogger.logDebug(TAG, "Fallback: set navigation graph to auth_nav_graph (login is startDestination)");
                        } catch (Exception graphEx) {
                            DiagnosticLogger.logError(TAG, "Failed to set auth graph in fallback", graphEx);
                        }
                    } catch (Exception fallbackException) {
                        DiagnosticLogger.logError(TAG, "Fallback navigation failed", fallbackException);
                        if (retryCount < MAX_RETRIES) {
                            DiagnosticLogger.logDebug(TAG, "Retrying navigation due to error... (" + (retryCount + 1) + "/" + MAX_RETRIES + ")");
                            performNavigationWithRetry(startDestination, retryCount + 1);
                        }
                    }
            }
        }, delay);
    }

    // Public helper to switch to main graph and show dashboard (used by auth fragments on success)
    public void switchToMainGraphAndShowDashboard() {
        switchToMainGraphAndShowDashboard(false);
    }

    public void switchToMainGraphAndShowDashboard(boolean justSignedUp) {
        try {
            if (navController != null) {
                if (dashboardViewModel == null) {
                    dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
                }

                // Ensure data load begins

                final boolean[] proceeded = new boolean[]{false};

                Runnable proceed = () -> {
                    if (proceeded[0]) return;
                    proceeded[0] = true;
                    try {
                        navController.setGraph(R.navigation.nav_graph);
                    } catch (Exception graphEx) {
                        DiagnosticLogger.logError(TAG, "Failed to set main graph", graphEx);
                    }
                    // Update admin tab visibility after successful auth
                    try { updateAdminMenuVisibility(); } catch (Exception ignored) {}
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        try {
                            NavOptions opts = new NavOptions.Builder()
                                .setPopUpTo(R.id.nav_graph, true)
                                .setLaunchSingleTop(true)
                                .build();
                            navController.navigate(R.id.postAuthLoadingFragment, null, opts);
                        } catch (Exception navEx) {
                            DiagnosticLogger.logError(TAG, "Navigate to PostAuthLoadingFragment after graph switch failed", navEx);
                        }
                    }, 150);
                    DiagnosticLogger.logDebug(TAG, "Switched to main graph");
                };

                // Proceed immediately so DashboardFragment is created and observers attach before any refresh
                proceed.run();
                // Observe for first non-null combined stats emission, then proceed (guarded to avoid duplicate attach)
                if (!proceeded[0]) {
                    final androidx.lifecycle.Observer<com.sugboaid.donation.viewmodels.DashboardViewModel.DashboardStatistics> statsObserver =
                        new androidx.lifecycle.Observer<com.sugboaid.donation.viewmodels.DashboardViewModel.DashboardStatistics>() {
                            @Override
                            public void onChanged(com.sugboaid.donation.viewmodels.DashboardViewModel.DashboardStatistics stats) {
                                if (stats != null && !proceeded[0]) {
                                    try {
                                        dashboardViewModel.getDashboardStatistics().removeObserver(this);
                                    } catch (Exception ignored) {}
                                    proceed.run();
                                }
                            }
                        };

                    try {
                        dashboardViewModel.getDashboardStatistics().observe(this, statsObserver);
                    } catch (Exception e) {
                        // Fallback if observe fails
                        new Handler(Looper.getMainLooper()).postDelayed(proceed, 300);
                    }

                    // Timeout fallback to avoid stalling in case repositories are slow
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        try {
                            if (!proceeded[0]) {
                                dashboardViewModel.getDashboardStatistics().removeObserver(statsObserver);
                                proceed.run();
                            }
                        } catch (Exception ignored) {
                            proceed.run();
                        }
                    }, 700);
                }
            }
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "switchToMainGraphAndShowDashboard failed", e);
        }
    }

    // Public method used by BaseFragment to access the NavController
    public androidx.navigation.NavController getNavController() {
        return navController;
    }

    private String getDestinationName(int destinationId) {
        try {
            if (navController != null) {
                androidx.navigation.NavDestination dest = navController.getGraph().findNode(destinationId);
                if (dest != null) {
                    CharSequence label = dest.getLabel();
                    if (label != null) return label.toString();
                    return dest.toString();
                }
            }
            return getResources().getResourceEntryName(destinationId);
        } catch (Exception e) {
            return "Unknown";
        }
    }

    

    private void announceNavigationChange(int destinationId) {
        try {
            View root = findViewById(android.R.id.content);
            String name = getDestinationName(destinationId);
            if (root != null && name != null) {
                AccessibilityUtils.announceForAccessibility(root, name);
            }
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Error announcing navigation change", e);
        }
    }
}