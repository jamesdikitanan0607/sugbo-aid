package com.sugboaid.donation.fragments;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.sugboaid.donation.activities.MainActivity;
import com.sugboaid.donation.utils.AnimationUtils;
import com.sugboaid.utils.SharedPreferencesHelper;

/**
 * Base Fragment class providing common functionality for all fragments
 * Includes navigation helpers, theme management, and utility methods
 */
public abstract class BaseFragment extends Fragment {

    protected SharedPreferencesHelper prefsHelper;
    protected NavController navController;
    protected MainActivity mainActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize SharedPreferences helper
        prefsHelper = SharedPreferencesHelper.getInstance(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize NavController
        try {
            navController = Navigation.findNavController(view);
        } catch (IllegalStateException e) {
            // NavController not available, might be in testing or special case
            navController = null;
        }
        
        // Initialize views and setup listeners
        initViews(view);
        setupListeners();
        observeData();
        
        // Apply entrance animations
        applyEntranceAnimations(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        refreshData();
    }

    /**
     * Navigate to a destination using NavController
     * @param actionId The navigation action ID
     */
    protected void navigateTo(int actionId) {
        if (navController != null) {
            try {
                navController.navigate(actionId);
            } catch (Exception e) {
                // Handle navigation error
                showToast("Navigation error occurred");
            }
        }
    }

    /**
     * Navigate to a destination with arguments
     * @param actionId The navigation action ID
     * @param args The arguments bundle
     */
    protected void navigateTo(int actionId, Bundle args) {
        if (navController != null) {
            try {
                navController.navigate(actionId, args);
            } catch (Exception e) {
                // Handle navigation error
                showToast("Navigation error occurred");
            }
        }
    }

    /**
     * Navigate back to previous destination
     */
    protected void navigateBack() {
        if (navController != null) {
            navController.popBackStack();
        } else if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    /**
     * Get NavController from MainActivity if available
     * @return NavController instance or null
     */
    protected NavController getMainNavController() {
        if (mainActivity != null) {
            return mainActivity.getNavController();
        }
        return navController;
    }

    /**
     * Check if network is available
     * @return true if network is available
     */
    protected boolean isNetworkAvailable() {
        return mainActivity != null && mainActivity.isNetworkAvailable();
    }

    /**
     * Get current dark mode preference
     * @return true if dark mode is enabled
     */
    protected boolean isDarkModeEnabled() {
        return prefsHelper.getDarkModePreference();
    }

    /**
     * Get user role from preferences
     * @return The selected user role
     */
    protected String getUserRole() {
        return prefsHelper.getUserRole();
    }

    /**
     * Show a short toast message
     * @param message The message to display
     */
    protected void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show a long toast message
     * @param message The message to display
     */
    protected void showLongToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
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
     * Show loading state
     * Override in subclasses to implement specific loading UI
     */
    protected void showLoading() {
        // Default implementation - can be overridden by subclasses
    }

    /**
     * Hide loading state
     * Override in subclasses to implement specific loading UI
     */
    protected void hideLoading() {
        // Default implementation - can be overridden by subclasses
    }

    /**
     * Handle error state
     * Override in subclasses to implement specific error handling
     * @param error The error message
     */
    protected void handleError(String error) {
        showToast(error);
    }

    /**
     * Handle offline state
     * Override in subclasses to implement specific offline handling
     */
    protected void handleOfflineState() {
        showToast("You are currently offline");
    }

    /**
     * Abstract method to be implemented by subclasses for view initialization
     * @param view The root view of the fragment
     */
    protected abstract void initViews(View view);

    /**
     * Abstract method to be implemented by subclasses for setting up listeners
     */
    protected abstract void setupListeners();

    /**
     * Abstract method to be implemented by subclasses for observing data
     */
    protected abstract void observeData();

    /**
     * Abstract method to be implemented by subclasses for refreshing data
     */
    protected abstract void refreshData();

    /**
     * Apply entrance animations to fragment views
     * Override in subclasses to customize animations
     * @param rootView The root view of the fragment
     */
    protected void applyEntranceAnimations(View rootView) {
        // Default fade-in animation for the entire fragment
        AnimationUtils.animateViewEntrance(rootView, AnimationUtils.AnimationType.FADE_IN, 0);
    }

    /**
     * Animate button press with feedback
     * @param button The button to animate
     */
    protected void animateButtonPress(View button) {
        AnimationUtils.animateButtonPress(button);
    }

    /**
     * Animate card entrance with stagger effect
     * @param container ViewGroup containing cards
     */
    protected void animateCardEntrance(ViewGroup container) {
        AnimationUtils.animateCardEntranceStagger(container, AnimationUtils.DELAY_SHORT);
    }

    /**
     * Create shimmer loading animation
     * @param view The view to apply shimmer to
     * @return ValueAnimator for the shimmer effect
     */
    protected ValueAnimator createShimmerAnimation(View view) {
        return AnimationUtils.createShimmerAnimation(view);
    }

    /**
     * Animate success state
     * @param view The view to animate (usually a checkmark or success icon)
     */
    protected void animateSuccess(View view) {
        AnimationUtils.animateSuccessCheckmark(view);
    }

    /**
     * Animate error state
     * @param view The view to animate (usually an input field or error container)
     */
    protected void animateError(View view) {
        AnimationUtils.animateShake(view);
    }

    /**
     * Animate floating action button entrance
     * @param fab The floating action button
     */
    protected void animateFabEntrance(View fab) {
        AnimationUtils.animateFabEntrance(fab);
    }
}