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
                setContentView(R.layout.activity_splash);
                DiagnosticLogger.logStartup("SimpleSplashActivity layout set successfully");
            } catch (Exception e) {
                DiagnosticLogger.logCrash(e, "SimpleSplashActivity setContentView failed");
                throw e;
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
            
            // Set up role selection buttons
            setupRoleButtons();
            
            // Show role selection after delay
            new Handler(Looper.getMainLooper()).postDelayed(this::showRoleSelection, SPLASH_DELAY);
            
            DiagnosticLogger.logStartup("SimpleSplashActivity onCreate completed successfully");
            
        } catch (Exception e) {
            DiagnosticLogger.logCrash(e, "SimpleSplashActivity onCreate failed");
            // Fallback: navigate directly to main activity
            navigateToMain("Guest");
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
            R.id.ll_role_selection
        };
        
        try {
            // Initialize views with null checks
            ImageView logo = findViewById(R.id.iv_logo);
            TextView taglineMain = findViewById(R.id.tv_tagline_main);
            TextView taglineSub = findViewById(R.id.tv_tagline_sub);
            View roleSelection = findViewById(R.id.ll_role_selection);
            
            // Validate critical views
            boolean allViewsValid = true;
            String[] viewNames = {"logo", "tagline_main", "tagline_sub", "role_selection"};
            View[] views = {logo, taglineMain, taglineSub, roleSelection};
            
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
    
    private void setupRoleButtons() {
        DiagnosticLogger.logDebug(TAG, "Setting up role selection buttons");
        
        try {
            View btnDonor = findViewById(R.id.btn_donor);
            View btnOrganization = findViewById(R.id.btn_organization);
            View btnVolunteer = findViewById(R.id.btn_volunteer);
            View btnRecipient = findViewById(R.id.btn_recipient);
            View btnGuest = findViewById(R.id.btn_guest);
            
            // Track which buttons are found
            String[] buttonNames = {"donor", "organization", "volunteer", "recipient", "guest"};
            View[] buttons = {btnDonor, btnOrganization, btnVolunteer, btnRecipient, btnGuest};
            String[] roles = {"Donor", "Organization", "Volunteer", "Recipient", "Guest"};
            
            int validButtons = 0;
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i] != null) {
                    final String role = roles[i];
                    // Add try-catch around click listener to prevent crashes
                    try {
                        buttons[i].setOnClickListener(v -> {
                            try {
                                DiagnosticLogger.logDebug(TAG, "Role selected: " + role);
                                diagnosticManager.logNavigationEvent("Role selection", "SimpleSplashActivity", 
                                    "MainActivity (" + role + ")", null);
                                navigateToMain(role);
                            } catch (Exception clickException) {
                                DiagnosticLogger.logError(TAG, "Error handling role selection click for " + role, clickException);
                                // Fallback: navigate with Guest role
                                navigateToMain("Guest");
                            }
                        });
                        validButtons++;
                        DiagnosticLogger.logDebug(TAG, buttonNames[i] + " button setup successfully");
                    } catch (Exception listenerException) {
                        DiagnosticLogger.logError(TAG, "Error setting listener for " + buttonNames[i] + " button", listenerException);
                    }
                } else {
                    DiagnosticLogger.logResourceError("view", "btn_" + buttonNames[i], 
                        "Role button not found in layout");
                }
            }
            
            DiagnosticLogger.logDebug(TAG, "Role buttons setup completed - " + validButtons + " of " + 
                buttons.length + " buttons found");
            
            // If no buttons were found, provide fallback navigation
            if (validButtons == 0) {
                DiagnosticLogger.logWarning(TAG, "No role buttons found - providing fallback navigation");
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    DiagnosticLogger.logDebug(TAG, "Fallback navigation triggered");
                    navigateToMain("Guest");
                }, 2000);
            }
            
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Error setting up role buttons", e);
            // Fallback: navigate to main with Guest role
            navigateToMain("Guest");
        }
    }
    
    private void showRoleSelection() {
        long splashDuration = System.currentTimeMillis() - splashStartTime;
        DiagnosticLogger.logStartup("SimpleSplashActivity showing role selection after " + splashDuration + "ms");
        
        try {
            View roleSelection = findViewById(R.id.ll_role_selection);
            if (roleSelection != null) {
                roleSelection.setVisibility(View.VISIBLE);
                DiagnosticLogger.logDebug(TAG, "Role selection view made visible");
                diagnosticManager.logNavigationEvent("UI transition", "SplashScreen", "RoleSelection", null);
            } else {
                DiagnosticLogger.logResourceError("view", "ll_role_selection", 
                    "Role selection view not found");
                DiagnosticLogger.logWarning(TAG, "Role selection view not found - navigating to main with Guest role");
                // Fallback: navigate directly to main activity
                navigateToMain("Guest");
            }
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Error showing role selection", e);
            navigateToMain("Guest");
        }
    }
    
    private void navigateToMain(String role) {
        // Create a final copy of the role parameter to use in the lambda
        final String finalRole = (role == null || role.trim().isEmpty()) ? "Guest" : role.trim();
        
        runOnUiThread(() -> {
            try {
                long totalSplashTime = System.currentTimeMillis() - splashStartTime;
                DiagnosticLogger.logStartup("SimpleSplashActivity navigating to MainActivity with role: " + 
                    finalRole + " after " + totalSplashTime + "ms");
                
                // Log if we had to default the role
                if (role == null || role.trim().isEmpty()) {
                    DiagnosticLogger.logWarning(TAG, "Role was null or empty, defaulting to Guest");
                }
                
                // Save the selected role to preferences
                if (prefsHelper != null) {
                    try {
                        prefsHelper.saveUserRole(finalRole);
                        DiagnosticLogger.logDebug(TAG, "User role saved to preferences: " + finalRole);
                    } catch (Exception e) {
                        DiagnosticLogger.logError(TAG, "Failed to save user role to preferences", e);
                        finish();
                        DiagnosticLogger.logDebug(TAG, "Splash activity finished");
                    }
                }
                
                Intent intent = new Intent(SimpleSplashActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("user_role", finalRole);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Unexpected error in navigateToMain", e);
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