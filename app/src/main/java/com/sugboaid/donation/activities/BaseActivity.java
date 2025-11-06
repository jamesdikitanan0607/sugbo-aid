package com.sugboaid.donation.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sugboaid.utils.DiagnosticLogger;
import com.sugboaid.utils.SharedPreferencesHelper;
import com.sugboaid.utils.ThemeUtils;
import com.sugboaid.utils.GlassmorphismUtils;

/**
 * Base Activity class providing common functionality for all activities
 * Includes theme management, network monitoring, and utility methods
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    protected SharedPreferencesHelper prefsHelper;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isNetworkAvailable = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            String activityName = this.getClass().getSimpleName();
            DiagnosticLogger.logActivityLifecycle(activityName, "onCreate");
            
            // Initialize SharedPreferences helper
            try {
                prefsHelper = SharedPreferencesHelper.getInstance(this);
                DiagnosticLogger.logDebug(TAG, "SharedPreferencesHelper initialized in " + activityName);
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to initialize SharedPreferencesHelper in " + activityName, e);
                throw e;
            }
            
            // Initialize and apply theme before calling super.onCreate()
            try {
                ThemeUtils.initializeTheme(this, prefsHelper);
                DiagnosticLogger.logDebug(TAG, "Theme initialized in " + activityName);
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to initialize theme in " + activityName, e);
            }
            
            super.onCreate(savedInstanceState);
            
            // Setup network monitoring
            try {
                setupNetworkMonitoring();
                DiagnosticLogger.logDebug(TAG, "Network monitoring setup in " + activityName);
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to setup network monitoring in " + activityName, e);
            }
            
        } catch (Exception e) {
            DiagnosticLogger.logCrash(e, "BaseActivity onCreate failed");
            throw e;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Apply glassmorphic effects to the current view
        applyGlassmorphicEffects();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup network callback
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    /**
     * Setup network connectivity monitoring
     */
    private void setupNetworkMonitoring() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                runOnUiThread(() -> {
                    isNetworkAvailable = true;
                    onNetworkAvailable();
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(() -> {
                    isNetworkAvailable = false;
                    onNetworkLost();
                });
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                
                runOnUiThread(() -> {
                    if (hasInternet && !isNetworkAvailable) {
                        isNetworkAvailable = true;
                        onNetworkAvailable();
                    } else if (!hasInternet && isNetworkAvailable) {
                        isNetworkAvailable = false;
                        onNetworkLost();
                    }
                });
            }
        };

        // Register network callback
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
        
        // Check initial network state
        checkInitialNetworkState();
    }

    /**
     * Check initial network connectivity state
     */
    private void checkInitialNetworkState() {
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            if (capabilities != null && 
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                isNetworkAvailable = true;
                onNetworkAvailable();
            } else {
                isNetworkAvailable = false;
                onNetworkLost();
            }
        } else {
            isNetworkAvailable = false;
            onNetworkLost();
        }
    }

    /**
     * Called when network becomes available
     * Override in subclasses to handle network availability
     */
    protected void onNetworkAvailable() {
        DiagnosticLogger.logInfo(TAG, "Network became available in " + this.getClass().getSimpleName());
    }

    /**
     * Called when network is lost
     * Override in subclasses to handle network loss
     */
    protected void onNetworkLost() {
        DiagnosticLogger.logWarning(TAG, "Network lost in " + this.getClass().getSimpleName());
    }

    /**
     * Check if network is currently available
     * @return true if network is available
     */
    public boolean isNetworkAvailable() {
        return isNetworkAvailable;
    }

    /**
     * Toggle dark mode theme
     */
    protected void toggleDarkMode() {
        ThemeUtils.toggleTheme(this, prefsHelper);
        // Note: Activity will be recreated, so glassmorphic effects will be reapplied in onCreate
    }

    /**
     * Set specific theme mode
     * @param themeMode The theme mode to set (THEME_MODE_SYSTEM, THEME_MODE_LIGHT, THEME_MODE_DARK)
     */
    protected void setThemeMode(int themeMode) {
        ThemeUtils.setThemeMode(this, prefsHelper, themeMode);
    }

    /**
     * Get current dark mode preference
     * @return true if dark mode is enabled
     */
    protected boolean isDarkModeEnabled() {
        return prefsHelper.getDarkModePreference();
    }

    /**
     * Get current theme mode
     * @return Current theme mode
     */
    protected int getCurrentThemeMode() {
        return ThemeUtils.getCurrentThemeMode(prefsHelper);
    }

    /**
     * Check if dark mode is currently active
     * @return true if dark mode is active
     */
    protected boolean isDarkModeActive() {
        return ThemeUtils.isDarkModeActive(this);
    }

    /**
     * Show a short toast message
     * @param message The message to display
     */
    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show a long toast message
     * @param message The message to display
     */
    protected void showLongToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Show or hide a view with animation
     * @param view The view to animate
     * @param show Whether to show or hide the view
     */
    protected void animateViewVisibility(View view, boolean show) {
        if (view == null) return;
        
        if (show) {
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);
            view.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null);
        } else {
            view.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> view.setVisibility(View.GONE));
        }
    }

    /**
     * Get user role from preferences
     * @return The selected user role
     */
    protected String getUserRole() {
        return prefsHelper.getUserRole();
    }

    /**
     * Save user role to preferences
     * @param role The user role to save
     */
    protected void saveUserRole(String role) {
        prefsHelper.saveUserRole(role);
    }

    /**
     * Abstract method to be implemented by subclasses for initialization
     */
    protected abstract void initViews();

    /**
     * Abstract method to be implemented by subclasses for setting up listeners
     */
    protected abstract void setupListeners();

    /**
     * Apply glassmorphic effects to the current activity's views
     */
    protected void applyGlassmorphicEffects() {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            GlassmorphismUtils.applyGlassmorphicToChildren((ViewGroup) rootView, this);
        }
    }

    /**
     * Update glassmorphic theme for all views
     */
    protected void updateGlassmorphicTheme() {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            boolean isDarkMode = isDarkModeActive();
            GlassmorphismUtils.updateGlassmorphicTheme(rootView, this, isDarkMode);
        }
    }
}