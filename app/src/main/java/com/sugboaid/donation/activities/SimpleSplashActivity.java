package com.sugboaid.donation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sugboaid.donation.R;
import com.sugboaid.utils.DiagnosticLogger;
import com.sugboaid.utils.GlobalExceptionHandler;
import com.sugboaid.utils.SharedPreferencesHelper;
import com.sugboaid.utils.StartupDiagnosticManager;

public class SimpleSplashActivity extends AppCompatActivity {

    private static final String TAG = "SimpleSplashActivity";
    private static final int SPLASH_DELAY = 3000; // 3 seconds
    
    private StartupDiagnosticManager diagnosticManager;
    private SharedPreferencesHelper prefsHelper;
    private long splashStartTime;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        splashStartTime = System.currentTimeMillis();
        
        try {
            // Install global exception handler first
            GlobalExceptionHandler.install(this);
            
            // Initialize diagnostic manager
            diagnosticManager = StartupDiagnosticManager.getInstance(this);
            diagnosticManager.initialize();
            diagnosticManager.logActivityStartup(TAG, getIntent());
            
            DiagnosticLogger.logStartup("SimpleSplashActivity onCreate started - App launch initiated");
            
            super.onCreate(savedInstanceState);
            
            // Set content view with error handling
            try {
                setContentView(R.layout.activity_simple_splash);
                DiagnosticLogger.logStartup("SimpleSplashActivity layout set successfully");
            } catch (Exception e) {
                DiagnosticLogger.logCrash(e, "SimpleSplashActivity setContentView failed");
                // Fallback to default layout if custom one fails
                try {
                    setContentView(R.layout.activity_splash);
                    DiagnosticLogger.logWarning(TAG, "Fell back to default splash layout");
                } catch (Exception fallbackException) {
                    DiagnosticLogger.logCrash(fallbackException, "Failed to load fallback splash layout");
                    throw e; // Re-throw original exception if fallback fails
                }
            }
            
            // Initialize SharedPreferences helper
            try {
                prefsHelper = SharedPreferencesHelper.getInstance(this);
                DiagnosticLogger.logDebug(TAG, "SharedPreferencesHelper initialized");
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to initialize SharedPreferencesHelper", e);
            }
            
            // Initialize views with comprehensive error handling and validation
            initializeViews();
            
            // Set up authentication buttons
            try {
                View btnLogin = findViewById(R.id.btn_login);
                View btnSignup = findViewById(R.id.btn_signup);
                if (btnLogin != null) {
                    btnLogin.setOnClickListener(v -> navigateToLogin());
                }
                if (btnSignup != null) {
                    btnSignup.setOnClickListener(v -> navigateToSignup());
                }
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Error setting up auth button listeners", e);
            }
            
            DiagnosticLogger.logStartup("SimpleSplashActivity onCreate completed successfully");
            
        } catch (Exception e) {
            DiagnosticLogger.logCrash(e, "SimpleSplashActivity onCreate failed");
            // Fallback: navigate directly to login
            navigateToLogin();
        }
    }
    
    /**
     * Initialize and validate views with comprehensive error handling
     */
    private void initializeViews() {
        DiagnosticLogger.logStartup("SimpleSplashActivity initializeViews started");
        
        // Track view IDs for diagnostic logging
        int[] criticalViewIds = {
            R.id.iv_logo,
            R.id.tv_tagline_main,
            R.id.tv_tagline_sub,
            R.id.btn_login,
            R.id.btn_signup
        };
        
        try {
            // Initialize views with null checks
            ImageView logo = findViewById(R.id.iv_logo);
            TextView taglineMain = findViewById(R.id.tv_tagline_main);
            TextView taglineSub = findViewById(R.id.tv_tagline_sub);
            View btnLogin = findViewById(R.id.btn_login);
            View btnSignup = findViewById(R.id.btn_signup);
            
            // Validate critical views
            boolean allViewsValid = true;
            String[] viewNames = {"logo", "tagline_main", "tagline_sub", "btn_login", "btn_signup"};
            View[] views = {logo, taglineMain, taglineSub, btnLogin, btnSignup};
            
            for (int i = 0; i < views.length; i++) {
                if (views[i] == null) {
                    DiagnosticLogger.logResourceError("view", viewNames[i], 
                        "View not found in splash layout");
                    allViewsValid = false;
                } else {
                    DiagnosticLogger.logDebug(TAG, viewNames[i] + " view initialized successfully");
                }
            }
            
            // Log view binding results
            diagnosticManager.logViewBindingEvent(TAG, criticalViewIds, allViewsValid, 
                allViewsValid ? null : "One or more critical views not found");
            
            if (!allViewsValid) {
                DiagnosticLogger.logWarning(TAG, "Some critical views are missing - splash may not display properly");
            }
            
            DiagnosticLogger.logStartup("SimpleSplashActivity initializeViews completed");
            
        } catch (Exception e) {
            DiagnosticLogger.logCrash(e, "SimpleSplashActivity initializeViews failed");
            diagnosticManager.logViewBindingEvent(TAG, criticalViewIds, false, e.getMessage());
            throw e;
        }
    }
    
    private void navigateToLogin() {
        runOnUiThread(() -> {
            try {
                long totalSplashTime = System.currentTimeMillis() - splashStartTime;
                DiagnosticLogger.logStartup("SimpleSplashActivity navigating to Login after " + totalSplashTime + "ms");
                Intent intent = new Intent(SimpleSplashActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("start_destination", "login");
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Unexpected error in navigateToLogin", e);
                finish();
            }
        });
    }
    
    private void navigateToSignup() {
        runOnUiThread(() -> {
            try {
                long totalSplashTime = System.currentTimeMillis() - splashStartTime;
                DiagnosticLogger.logStartup("SimpleSplashActivity navigating to Signup after " + totalSplashTime + "ms");
                Intent intent = new Intent(SimpleSplashActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("start_destination", "signup");
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Unexpected error in navigateToSignup", e);
                finish();
            }
        });
    }
    
    @SuppressWarnings("deprecation")
    private void showErrorAndExit() {
        try {
            // Show error to user
            runOnUiThread(() -> {
                try {
                    // Use MaterialAlertDialogBuilder if available
                    try {
                        // Try to use MaterialAlertDialogBuilder from Material Components
                        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                            .setTitle("Error")
                            .setMessage("Unable to start the application. Please try again.")
                            .setPositiveButton("OK", (dialog, which) -> finish())
                            .setCancelable(false)
                            .show();
                    } catch (Exception e) {
                        // Fallback to standard AlertDialog
                        new android.app.AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Unable to start the application. Please try again.")
                            .setPositiveButton("OK", (dialog, which) -> finish())
                            .setCancelable(false)
                            .show();
                    }
                } catch (Exception e) {
                    // If UI thread is not available, just finish
                    finish();
                }
            });
        } catch (Exception e) {
            // If something goes wrong, just finish the activity
            finish();
        }
    }
    
    @Override
    public void onBackPressed() {
        // Disable back button on splash screen to prevent users from exiting the app
        // during the initial loading phase
        DiagnosticLogger.logDebug(TAG, "Back button pressed - ignored on splash screen");
        // No call to super.onBackPressed() to prevent default behavior
        
        // For Android 13+ with predictive back gesture, we'll still consume the back press
        if (android.os.Build.VERSION.SDK_INT >= 33) { // TIRAMISU
            // Just consume the back press without doing anything
            return;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Back press handling is now done in onBackPressed()
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // No need to clean up back press handling for legacy implementation
    }
    
    @SuppressWarnings("deprecation")
    private void onBackPressedDeprecated() {
        // This method is only called on older Android versions
        // No call to super.onBackPressed() to prevent default behavior
        DiagnosticLogger.logDebug(TAG, "Back button pressed (legacy) - ignored on splash screen");
    }
}