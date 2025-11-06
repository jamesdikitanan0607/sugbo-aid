package com.sugboaid.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.sugboaid.donation.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Specialized loading state manager for authentication operations
 * Provides consistent loading UI patterns for login, signup, and logout operations
 */
public class AuthLoadingStateManager {

    private final Context context;
    private final List<View> disabledViews;
    private final List<Animator> activeAnimators;
    private boolean isLoading;

    public AuthLoadingStateManager(@NonNull Context context) {
        this.context = context;
        this.disabledViews = new ArrayList<>();
        this.activeAnimators = new ArrayList<>();
        this.isLoading = false;
    }

    /**
     * Show loading state for authentication button
     * @param button The MaterialButton to show loading on
     * @param loadingText Text to display during loading
     * @param originalText Original button text to restore later
     */
    public void showButtonLoading(@NonNull MaterialButton button, @NonNull String loadingText, @NonNull String originalText) {
        if (isLoading) return;
        
        isLoading = true;
        button.setEnabled(false);
        button.setText(loadingText);
        
        // Store original text as tag for restoration
        button.setTag(originalText);
        
        // Add subtle animation to indicate loading
        ObjectAnimator pulseAnimator = ObjectAnimator.ofFloat(button, "alpha", 1.0f, 0.7f, 1.0f);
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        pulseAnimator.start();
        
        activeAnimators.add(pulseAnimator);
        disabledViews.add(button);
    }

    /**
     * Hide loading state for authentication button
     * @param button The MaterialButton to restore
     */
    public void hideButtonLoading(@NonNull MaterialButton button) {
        if (!isLoading) return;
        
        button.setEnabled(true);
        
        // Restore original text from tag
        Object originalText = button.getTag();
        if (originalText instanceof String) {
            button.setText((String) originalText);
        }
        
        // Stop animations
        stopAnimationsForView(button);
        disabledViews.remove(button);
        
        // Reset alpha
        button.setAlpha(1.0f);
        
        if (disabledViews.isEmpty()) {
            isLoading = false;
        }
    }

    /**
     * Show loading state for form fields
     * @param fields Array of TextInputLayout fields to disable during loading
     */
    public void showFormLoading(@NonNull TextInputLayout... fields) {
        for (TextInputLayout field : fields) {
            field.setEnabled(false);
            field.setAlpha(0.6f);
            disabledViews.add(field);
        }
    }

    /**
     * Hide loading state for form fields
     * @param fields Array of TextInputLayout fields to re-enable
     */
    public void hideFormLoading(@NonNull TextInputLayout... fields) {
        for (TextInputLayout field : fields) {
            field.setEnabled(true);
            field.setAlpha(1.0f);
            disabledViews.remove(field);
        }
    }

    /**
     * Show comprehensive loading state for entire authentication form
     * @param button The submit button
     * @param loadingText Loading text for button
     * @param originalText Original button text
     * @param fields Form fields to disable
     */
    public void showAuthLoading(@NonNull MaterialButton button, @NonNull String loadingText, 
                               @NonNull String originalText, @NonNull TextInputLayout... fields) {
        showButtonLoading(button, loadingText, originalText);
        showFormLoading(fields);
    }

    /**
     * Hide comprehensive loading state for entire authentication form
     * @param button The submit button
     * @param fields Form fields to re-enable
     */
    public void hideAuthLoading(@NonNull MaterialButton button, @NonNull TextInputLayout... fields) {
        hideButtonLoading(button);
        hideFormLoading(fields);
    }

    /**
     * Show loading overlay on a specific view
     * @param targetView View to overlay
     * @return The overlay view for manual removal
     */
    public View showLoadingOverlay(@NonNull View targetView) {
        if (!(targetView.getParent() instanceof android.view.ViewGroup)) {
            return null;
        }
        
        android.view.ViewGroup parent = (android.view.ViewGroup) targetView.getParent();
        
        // Create overlay container
        android.widget.FrameLayout overlay = new android.widget.FrameLayout(context);
        overlay.setBackgroundColor(context.getResources().getColor(R.color.loading_overlay, null));
        overlay.setLayoutParams(new android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        ));
        
        // Create progress bar
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        progressBar.getIndeterminateDrawable().setColorFilter(
            context.getResources().getColor(R.color.primary_blue, null),
            android.graphics.PorterDuff.Mode.SRC_IN
        );
        
        // Center the progress bar
        android.widget.FrameLayout.LayoutParams progressParams = new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        );
        progressParams.gravity = android.view.Gravity.CENTER;
        progressBar.setLayoutParams(progressParams);
        
        overlay.addView(progressBar);
        parent.addView(overlay);
        
        // Animate entrance
        overlay.setAlpha(0f);
        overlay.animate()
                .alpha(1f)
                .setDuration(200)
                .start();
        
        return overlay;
    }

    /**
     * Hide loading overlay
     * @param overlay The overlay view to hide
     */
    public void hideLoadingOverlay(@Nullable View overlay) {
        if (overlay != null && overlay.getParent() instanceof android.view.ViewGroup) {
            overlay.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            android.view.ViewGroup parent = (android.view.ViewGroup) overlay.getParent();
                            if (parent != null) {
                                parent.removeView(overlay);
                            }
                        }
                    })
                    .start();
        }
    }

    /**
     * Show loading state with progress indicator
     * @param container Container to show progress in
     * @param message Loading message
     */
    public void showProgressLoading(@NonNull android.view.ViewGroup container, @NonNull String message) {
        container.removeAllViews();
        
        // Create vertical layout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(32, 32, 32, 32);
        
        // Create progress bar
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        progressBar.getIndeterminateDrawable().setColorFilter(
            context.getResources().getColor(R.color.primary_blue, null),
            android.graphics.PorterDuff.Mode.SRC_IN
        );
        
        // Create message text
        android.widget.TextView messageText = new android.widget.TextView(context);
        messageText.setText(message);
        messageText.setTextColor(context.getResources().getColor(R.color.text_secondary, null));
        messageText.setTextSize(14);
        messageText.setGravity(android.view.Gravity.CENTER);
        
        // Set layout parameters
        android.widget.LinearLayout.LayoutParams progressParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        progressParams.bottomMargin = 16;
        
        android.widget.LinearLayout.LayoutParams textParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        
        layout.addView(progressBar, progressParams);
        layout.addView(messageText, textParams);
        
        container.addView(layout, new android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        ));
        
        // Animate entrance
        layout.setAlpha(0f);
        layout.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    /**
     * Show shimmer loading effect for authentication forms
     * @param container Container to show shimmer in
     */
    public void showShimmerLoading(@NonNull android.view.ViewGroup container) {
        container.removeAllViews();
        
        // Create shimmer placeholders for form fields
        for (int i = 0; i < 3; i++) {
            View shimmerView = createShimmerField();
            container.addView(shimmerView);
        }
        
        // Create shimmer placeholder for button
        View shimmerButton = createShimmerButton();
        container.addView(shimmerButton);
    }

    /**
     * Stop all active animations
     */
    public void stopAllAnimations() {
        for (Animator animator : activeAnimators) {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
        }
        activeAnimators.clear();
    }

    /**
     * Reset all loading states
     */
    public void resetAllStates() {
        stopAllAnimations();
        
        // Re-enable all disabled views
        for (View view : disabledViews) {
            view.setEnabled(true);
            view.setAlpha(1.0f);
            
            // Restore button text if it's a MaterialButton
            if (view instanceof MaterialButton) {
                MaterialButton button = (MaterialButton) view;
                Object originalText = button.getTag();
                if (originalText instanceof String) {
                    button.setText((String) originalText);
                }
            }
        }
        
        disabledViews.clear();
        isLoading = false;
    }

    /**
     * Check if currently in loading state
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return isLoading;
    }

    /**
     * Get count of disabled views
     * @return Number of views currently disabled due to loading
     */
    public int getDisabledViewCount() {
        return disabledViews.size();
    }

    /**
     * Stop animations for a specific view
     */
    private void stopAnimationsForView(@NonNull View view) {
        List<Animator> toRemove = new ArrayList<>();
        for (Animator animator : activeAnimators) {
            if (animator != null) {
                // Note: getTarget() method may not be available on all Animator types
                // Using a safer approach by canceling all animations
                animator.cancel();
                toRemove.add(animator);
            }
        }
        activeAnimators.removeAll(toRemove);
    }

    /**
     * Create shimmer placeholder for form field
     */
    private View createShimmerField() {
        View shimmer = new View(context);
        shimmer.setBackgroundColor(context.getResources().getColor(R.color.light_gray, null));
        
        android.view.ViewGroup.LayoutParams params = new android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            (int) (56 * context.getResources().getDisplayMetrics().density)
        );
        shimmer.setLayoutParams(params);
        
        // Add margin
        if (params instanceof android.view.ViewGroup.MarginLayoutParams) {
            android.view.ViewGroup.MarginLayoutParams marginParams = (android.view.ViewGroup.MarginLayoutParams) params;
            int margin = (int) (8 * context.getResources().getDisplayMetrics().density);
            marginParams.setMargins(0, margin, 0, margin);
        }
        
        return shimmer;
    }

    /**
     * Create shimmer placeholder for button
     */
    private View createShimmerButton() {
        View shimmer = new View(context);
        shimmer.setBackgroundColor(context.getResources().getColor(R.color.medium_gray, null));
        
        android.view.ViewGroup.LayoutParams params = new android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            (int) (56 * context.getResources().getDisplayMetrics().density)
        );
        shimmer.setLayoutParams(params);
        
        // Add margin
        if (params instanceof android.view.ViewGroup.MarginLayoutParams) {
            android.view.ViewGroup.MarginLayoutParams marginParams = (android.view.ViewGroup.MarginLayoutParams) params;
            int margin = (int) (16 * context.getResources().getDisplayMetrics().density);
            marginParams.setMargins(0, margin, 0, 0);
        }
        
        return shimmer;
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        resetAllStates();
    }
}