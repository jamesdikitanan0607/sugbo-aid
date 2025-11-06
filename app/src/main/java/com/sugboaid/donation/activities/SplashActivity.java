package com.sugboaid.donation.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.sugboaid.donation.R;
import com.sugboaid.utils.DiagnosticLogger;
import com.sugboaid.utils.SharedPreferencesHelper;
import com.sugboaid.utils.StartupDiagnosticManager;
import com.sugboaid.viewmodels.AuthViewModel;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private static final int ANIMATION_DURATION = 1500; // 1.5 seconds
    
    private ImageView logoImageView;
    private TextView taglineMain;
    private TextView taglineSub;
    private LinearLayout roleSelectionContainer;
    private View backgroundOverlay;
    
    private SharedPreferencesHelper prefsHelper;
    private AuthViewModel authViewModel;
    private StartupDiagnosticManager diagnosticManager;
    private boolean animationsCompleted = false;
    private boolean authCheckCompleted = false;
    private long splashStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        splashStartTime = System.currentTimeMillis();
        
        try {
            // Initialize diagnostic manager first
            diagnosticManager = StartupDiagnosticManager.getInstance(this);
            diagnosticManager.initialize();
            diagnosticManager.logActivityStartup(TAG, getIntent());
            
            DiagnosticLogger.logStartup("SplashActivity onCreate started");
            
            super.onCreate(savedInstanceState);
            
            // Set content view with error handling
            try {
                setContentView(R.layout.activity_splash);
                DiagnosticLogger.logStartup("SplashActivity layout set successfully");
            } catch (Exception e) {
                DiagnosticLogger.logCrash(e, "SplashActivity setContentView failed");
                throw e;
            }
            
            // Initialize SharedPreferences helper
            try {
                prefsHelper = SharedPreferencesHelper.getInstance(this);
                DiagnosticLogger.logDebug(TAG, "SharedPreferencesHelper initialized");
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to initialize SharedPreferencesHelper", e);
                throw e;
            }
            
            // Initialize AuthViewModel
            try {
                authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
                DiagnosticLogger.logDebug(TAG, "AuthViewModel initialized");
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to initialize AuthViewModel", e);
                throw e;
            }
            
            // Initialize views
            initViews();
            
            // Start animations
            startLogoAnimation();
            
            // Check authentication status and navigate accordingly
            checkAuthenticationAndNavigate();
            
            DiagnosticLogger.logStartup("SplashActivity onCreate completed successfully");
            
        } catch (Exception e) {
            DiagnosticLogger.logCrash(e, "SplashActivity onCreate failed");
            // Re-throw to maintain existing error handling behavior
            throw e;
        }
    }

    private void initViews() {
        DiagnosticLogger.logStartup("SplashActivity initViews started");
        
        // Track view IDs for diagnostic logging
        int[] criticalViewIds = {
            R.id.iv_logo,
            R.id.tv_tagline_main,
            R.id.tv_tagline_sub,
            R.id.ll_role_selection,
            R.id.view_background_overlay
        };
        
        try {
            logoImageView = findViewById(R.id.iv_logo);
            taglineMain = findViewById(R.id.tv_tagline_main);
            taglineSub = findViewById(R.id.tv_tagline_sub);
            roleSelectionContainer = findViewById(R.id.ll_role_selection);
            backgroundOverlay = findViewById(R.id.view_background_overlay);
            
            // Validate critical views
            boolean allViewsValid = true;
            String[] viewNames = {"logo", "tagline_main", "tagline_sub", "role_selection", "background_overlay"};
            View[] views = {logoImageView, taglineMain, taglineSub, roleSelectionContainer, backgroundOverlay};
            
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
                return;
            }
            
            // Initially hide role selection and set initial states
            roleSelectionContainer.setVisibility(View.GONE);
            logoImageView.setScaleX(0f);
            logoImageView.setScaleY(0f);
            logoImageView.setAlpha(0f);
            taglineMain.setAlpha(0f);
            taglineSub.setAlpha(0f);
            taglineMain.setTranslationY(50f);
            taglineSub.setTranslationY(50f);
            
            DiagnosticLogger.logDebug(TAG, "Initial view states set successfully");
            DiagnosticLogger.logStartup("SplashActivity initViews completed successfully");
            
        } catch (Exception e) {
            DiagnosticLogger.logCrash(e, "SplashActivity initViews failed");
            diagnosticManager.logViewBindingEvent(TAG, criticalViewIds, false, e.getMessage());
            throw e;
        }
    }

    private void startLogoAnimation() {
        // Logo scale and fade in animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoImageView, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoImageView, "scaleY", 0f, 1.2f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(logoImageView, "alpha", 0f, 1f);
        
        AnimatorSet logoAnimSet = new AnimatorSet();
        logoAnimSet.playTogether(scaleX, scaleY, alpha);
        logoAnimSet.setDuration(ANIMATION_DURATION);
        logoAnimSet.setInterpolator(new DecelerateInterpolator());
        
        // Tagline animations
        ObjectAnimator taglineMainAlpha = ObjectAnimator.ofFloat(taglineMain, "alpha", 0f, 1f);
        ObjectAnimator taglineMainTransY = ObjectAnimator.ofFloat(taglineMain, "translationY", 50f, 0f);
        
        ObjectAnimator taglineSubAlpha = ObjectAnimator.ofFloat(taglineSub, "alpha", 0f, 1f);
        ObjectAnimator taglineSubTransY = ObjectAnimator.ofFloat(taglineSub, "translationY", 50f, 0f);
        
        AnimatorSet taglineMainSet = new AnimatorSet();
        taglineMainSet.playTogether(taglineMainAlpha, taglineMainTransY);
        taglineMainSet.setDuration(800);
        taglineMainSet.setStartDelay(500);
        taglineMainSet.setInterpolator(new DecelerateInterpolator());
        
        AnimatorSet taglineSubSet = new AnimatorSet();
        taglineSubSet.playTogether(taglineSubAlpha, taglineSubTransY);
        taglineSubSet.setDuration(800);
        taglineSubSet.setStartDelay(700);
        taglineSubSet.setInterpolator(new DecelerateInterpolator());
        
        // Start background parallax animation
        startBackgroundAnimation();
        
        // Start all animations
        logoAnimSet.start();
        taglineMainSet.start();
        taglineSubSet.start();
        
        // Mark animations as completed after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> animationsCompleted = true, ANIMATION_DURATION + 500);
    }

    private void startBackgroundAnimation() {
        // Subtle parallax effect on background overlay
        ValueAnimator backgroundAnimator = ValueAnimator.ofFloat(0f, 1f);
        backgroundAnimator.setDuration(3000);
        backgroundAnimator.setRepeatCount(ValueAnimator.INFINITE);
        backgroundAnimator.setRepeatMode(ValueAnimator.REVERSE);
        backgroundAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        backgroundAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            if (backgroundOverlay != null) {
                backgroundOverlay.setAlpha(0.1f + (value * 0.05f)); // Subtle alpha change
            }
        });
        
        backgroundAnimator.start();
    }

    /**
     * Check authentication status and navigate accordingly
     */
    private void checkAuthenticationAndNavigate() {
        DiagnosticLogger.logStartup("SplashActivity authentication check started");
        
        // Perform authentication check in background
        new Thread(() -> {
            try {
                DiagnosticLogger.logDebug(TAG, "Starting authentication status check");
                
                // Check if user is authenticated
                boolean isAuthenticated = authViewModel.checkAuthenticationStatus();
                
                DiagnosticLogger.logDebug(TAG, "Authentication check completed - authenticated: " + isAuthenticated);
                
                // Mark auth check as completed
                authCheckCompleted = true;
                
                // Wait for minimum splash delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    proceedWithNavigation(isAuthenticated);
                }, SPLASH_DELAY);
                
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Authentication check failed", e);
                
                // If authentication check fails, treat as unauthenticated
                authCheckCompleted = true;
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    DiagnosticLogger.logWarning(TAG, "Proceeding as unauthenticated due to auth check failure");
                    proceedWithNavigation(false);
                }, SPLASH_DELAY);
            }
        }).start();
    }

    /**
     * Proceed with navigation based on authentication status
     * @param isAuthenticated Whether user is authenticated
     */
    private void proceedWithNavigation(boolean isAuthenticated) {
        DiagnosticLogger.logDebug(TAG, "Proceeding with navigation - authenticated: " + isAuthenticated + 
            ", animations completed: " + animationsCompleted);
        
        if (!animationsCompleted) {
            // Wait a bit more if animations aren't complete
            DiagnosticLogger.logDebug(TAG, "Waiting for animations to complete");
            new Handler(Looper.getMainLooper()).postDelayed(() -> 
                proceedWithNavigation(isAuthenticated), 500);
            return;
        }
        
        long splashDuration = System.currentTimeMillis() - splashStartTime;
        DiagnosticLogger.logStartup("SplashActivity navigation decision made after " + splashDuration + "ms");
        
        if (isAuthenticated) {
            // User is authenticated, navigate to MainActivity with Dashboard
            DiagnosticLogger.logDebug(TAG, "User authenticated - navigating to main activity");
            diagnosticManager.logNavigationEvent("Authentication flow", "SplashActivity", "MainActivity", null);
            navigateToMainActivity();
        } else {
            // User is not authenticated, show role selection or navigate to login
            DiagnosticLogger.logDebug(TAG, "User not authenticated - showing role selection");
            diagnosticManager.logNavigationEvent("Authentication flow", "SplashActivity", "RoleSelection", null);
            showRoleSelection();
        }
    }

    private void showRoleSelection() {
        // Fade in role selection container
        roleSelectionContainer.setVisibility(View.VISIBLE);
        roleSelectionContainer.setAlpha(0f);
        roleSelectionContainer.setTranslationY(100f);
        
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(roleSelectionContainer, "alpha", 0f, 1f);
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(roleSelectionContainer, "translationY", 100f, 0f);
        
        AnimatorSet roleAnimSet = new AnimatorSet();
        roleAnimSet.playTogether(fadeIn, slideUp);
        roleAnimSet.setDuration(800);
        roleAnimSet.setInterpolator(new DecelerateInterpolator());
        roleAnimSet.start();
        
        // Setup role selection button click listeners
        setupRoleSelectionListeners();
    }

    private void setupRoleSelectionListeners() {
        try {
            // Add null checks for each button before setting listeners
            View btnDonor = findViewById(R.id.btn_donor);
            View btnOrganization = findViewById(R.id.btn_organization);
            View btnVolunteer = findViewById(R.id.btn_volunteer);
            View btnRecipient = findViewById(R.id.btn_recipient);
            View btnGuest = findViewById(R.id.btn_guest);
            
            if (btnDonor != null) {
                btnDonor.setOnClickListener(v -> selectRole("Donor", v));
                DiagnosticLogger.logDebug(TAG, "Donor button listener set successfully");
            } else {
                DiagnosticLogger.logResourceError("view", "btn_donor", "Donor button not found");
            }
            
            if (btnOrganization != null) {
                btnOrganization.setOnClickListener(v -> selectRole("Organization", v));
                DiagnosticLogger.logDebug(TAG, "Organization button listener set successfully");
            } else {
                DiagnosticLogger.logResourceError("view", "btn_organization", "Organization button not found");
            }
            
            if (btnVolunteer != null) {
                btnVolunteer.setOnClickListener(v -> selectRole("Volunteer", v));
                DiagnosticLogger.logDebug(TAG, "Volunteer button listener set successfully");
            } else {
                DiagnosticLogger.logResourceError("view", "btn_volunteer", "Volunteer button not found");
            }
            
            if (btnRecipient != null) {
                btnRecipient.setOnClickListener(v -> selectRole("Recipient", v));
                DiagnosticLogger.logDebug(TAG, "Recipient button listener set successfully");
            } else {
                DiagnosticLogger.logResourceError("view", "btn_recipient", "Recipient button not found");
            }
            
            if (btnGuest != null) {
                btnGuest.setOnClickListener(v -> selectRole("Guest", v));
                DiagnosticLogger.logDebug(TAG, "Guest button listener set successfully");
            } else {
                DiagnosticLogger.logResourceError("view", "btn_guest", "Guest button not found");
            }
            
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Error setting up role selection listeners", e);
        }
    }

    private void selectRole(String role, View clickedButton) {
        try {
            DiagnosticLogger.logDebug(TAG, "Role selected: " + role);
            
            // Save selected role to SharedPreferences
            if (prefsHelper != null) {
                prefsHelper.saveUserRole(role);
                DiagnosticLogger.logDebug(TAG, "Role saved to preferences: " + role);
            } else {
                DiagnosticLogger.logWarning(TAG, "SharedPreferencesHelper is null - role not saved");
            }
            
            // Log navigation event
            diagnosticManager.logNavigationEvent("Role selection", "SplashActivity", 
                "AuthenticationFlow (" + role + ")", null);
            
            // Animate button press and navigate to authentication flow
            if (clickedButton != null) {
                animateButtonPress(clickedButton, () -> navigateToAuthenticationFlow());
            } else {
                // If button is null, navigate directly
                DiagnosticLogger.logWarning(TAG, "Clicked button is null - navigating directly");
                navigateToAuthenticationFlow();
            }
            
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Error in selectRole for role: " + role, e);
            // Fallback: navigate directly to authentication flow
            navigateToAuthenticationFlow();
        }
    }

    private void animateButtonPress(View button, Runnable onComplete) {
        if (button == null) {
            DiagnosticLogger.logWarning(TAG, "Button is null - skipping animation and executing callback");
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        
        try {
            // Scale down animation
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.92f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.92f);
            ObjectAnimator alphaDown = ObjectAnimator.ofFloat(button, "alpha", 1f, 0.8f);
            
            // Scale up animation
            ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(button, "scaleX", 0.92f, 1.05f, 1f);
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.92f, 1.05f, 1f);
            ObjectAnimator alphaUp = ObjectAnimator.ofFloat(button, "alpha", 0.8f, 1f);
            
            AnimatorSet scaleDown = new AnimatorSet();
            scaleDown.playTogether(scaleDownX, scaleDownY, alphaDown);
            scaleDown.setDuration(120);
            scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());
            
            AnimatorSet scaleUp = new AnimatorSet();
            scaleUp.playTogether(scaleUpX, scaleUpY, alphaUp);
            scaleUp.setDuration(200);
            scaleUp.setInterpolator(new DecelerateInterpolator());
            
            scaleDown.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    try {
                        scaleUp.start();
                    } catch (Exception e) {
                        DiagnosticLogger.logError(TAG, "Error starting scale up animation", e);
                        // Execute callback anyway
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                }
                
                @Override
                public void onAnimationCancel(android.animation.Animator animation) {
                    // Execute callback if animation is cancelled
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
            
            scaleUp.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    // Add a slight delay before navigation for better UX
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (onComplete != null) {
                            try {
                                onComplete.run();
                            } catch (Exception e) {
                                DiagnosticLogger.logError(TAG, "Error executing animation callback", e);
                            }
                        }
                    }, 150);
                }
                
                @Override
                public void onAnimationCancel(android.animation.Animator animation) {
                    // Execute callback if animation is cancelled
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
            
            scaleDown.start();
            
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Error in animateButtonPress", e);
            // Fallback: execute callback directly
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    /**
     * Navigate to authentication flow (LoginFragment)
     */
    private void navigateToAuthenticationFlow() {
        try {
            DiagnosticLogger.logDebug(TAG, "Starting navigation to authentication flow");
            
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("start_destination", "login");
            
            // Add user role if available
            if (prefsHelper != null) {
                try {
                    String userRole = prefsHelper.getUserRole();
                    if (userRole != null && !userRole.isEmpty()) {
                        intent.putExtra("user_role", userRole);
                        DiagnosticLogger.logDebug(TAG, "Added user role to intent: " + userRole);
                    }
                } catch (Exception roleException) {
                    DiagnosticLogger.logError(TAG, "Error getting user role", roleException);
                }
            }
            
            long totalSplashTime = System.currentTimeMillis() - splashStartTime;
            DiagnosticLogger.logStartup("SplashActivity completed - navigating to login after " + totalSplashTime + "ms");
            
            startActivity(intent);
            finish();
            
            // Add transition animation with error handling
            try {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } catch (Exception animException) {
                DiagnosticLogger.logError(TAG, "Error applying transition animation", animException);
            }
            
            DiagnosticLogger.logDebug(TAG, "Navigation to authentication flow completed");
            
        } catch (android.content.ActivityNotFoundException e) {
            DiagnosticLogger.logError(TAG, "MainActivity not found", e);
            // Try to finish the current activity anyway
            try {
                finish();
            } catch (Exception finishException) {
                DiagnosticLogger.logError(TAG, "Failed to finish SplashActivity", finishException);
            }
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Failed to navigate to authentication flow", e);
            // Try to finish the current activity anyway
            try {
                finish();
            } catch (Exception finishException) {
                DiagnosticLogger.logError(TAG, "Failed to finish SplashActivity", finishException);
            }
        }
    }

    /**
     * Navigate to main activity with dashboard (for authenticated users)
     */
    private void navigateToMainActivity() {
        try {
            DiagnosticLogger.logDebug(TAG, "Starting navigation to main activity");
            
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("start_destination", "dashboard");
            
            // Add user role if available
            if (prefsHelper != null) {
                try {
                    String userRole = prefsHelper.getUserRole();
                    if (userRole != null && !userRole.isEmpty()) {
                        intent.putExtra("user_role", userRole);
                        DiagnosticLogger.logDebug(TAG, "Added user role to intent: " + userRole);
                    }
                } catch (Exception roleException) {
                    DiagnosticLogger.logError(TAG, "Error getting user role", roleException);
                }
            }
            
            long totalSplashTime = System.currentTimeMillis() - splashStartTime;
            DiagnosticLogger.logStartup("SplashActivity completed - navigating to dashboard after " + totalSplashTime + "ms");
            
            startActivity(intent);
            finish();
            
            // Add transition animation with error handling
            try {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } catch (Exception animException) {
                DiagnosticLogger.logError(TAG, "Error applying transition animation", animException);
            }
            
            DiagnosticLogger.logDebug(TAG, "Navigation to main activity completed");
            
        } catch (android.content.ActivityNotFoundException e) {
            DiagnosticLogger.logError(TAG, "MainActivity not found", e);
            // Try to finish the current activity anyway
            try {
                finish();
            } catch (Exception finishException) {
                DiagnosticLogger.logError(TAG, "Failed to finish SplashActivity", finishException);
            }
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Failed to navigate to main activity", e);
            // Try to finish the current activity anyway
            try {
                finish();
            } catch (Exception finishException) {
                DiagnosticLogger.logError(TAG, "Failed to finish SplashActivity", finishException);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button on splash screen
        // Do nothing
    }
}