package com.sugboaid.donation.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;

import com.sugboaid.donation.R;
import com.sugboaid.donation.views.ShimmerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager class for handling loading states and animations
 * Provides consistent loading UI patterns throughout the app
 */
public class LoadingStateManager {

    private Context context;
    private List<Animator> activeAnimators;
    private List<ShimmerView> activeShimmers;

    public LoadingStateManager(Context context) {
        this.context = context;
        this.activeAnimators = new ArrayList<>();
        this.activeShimmers = new ArrayList<>();
    }

    /**
     * Show loading state with shimmer effect
     * @param container The container to show loading in
     * @param itemCount Number of shimmer items to show
     */
    public void showShimmerLoading(ViewGroup container, int itemCount) {
        container.removeAllViews();
        
        for (int i = 0; i < itemCount; i++) {
            ShimmerView shimmerView = new ShimmerView(context);
            
            // Set layout parameters
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (60 * context.getResources().getDisplayMetrics().density)
            );
            shimmerView.setLayoutParams(params);
            
            // Add margin
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
                int margin = (int) (8 * context.getResources().getDisplayMetrics().density);
                marginParams.setMargins(margin, margin, margin, margin);
            }
            
            container.addView(shimmerView);
            activeShimmers.add(shimmerView);
        }
    }

    /**
     * Show loading state with progress bar
     * @param container The container to show loading in
     * @param message Loading message to display
     */
    public void showProgressLoading(ViewGroup container, String message) {
        container.removeAllViews();
        
        // Create progress bar
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        
        // Set progress bar color
        progressBar.getIndeterminateDrawable().setColorFilter(
            ContextCompat.getColor(context, R.color.primary_blue),
            android.graphics.PorterDuff.Mode.SRC_IN
        );
        
        // Add to container
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        progressBar.setLayoutParams(params);
        
        container.addView(progressBar);
        
        // Animate entrance
        AnimationUtils.animateViewEntrance(progressBar, AnimationUtils.AnimationType.SCALE_IN, 0);
    }

    /**
     * Show pulsing loading animation on a view
     * @param view The view to animate
     */
    public void showPulseLoading(View view) {
        AnimatorSet pulseAnimator = MicroInteractionHelper.createPulseAnimation(view);
        pulseAnimator.start();
        activeAnimators.add(pulseAnimator);
    }

    /**
     * Show skeleton loading for cards
     * @param container The container to show skeleton in
     * @param cardCount Number of skeleton cards to show
     */
    public void showSkeletonLoading(ViewGroup container, int cardCount) {
        container.removeAllViews();
        
        for (int i = 0; i < cardCount; i++) {
            View skeletonCard = createSkeletonCard();
            container.addView(skeletonCard);
            
            // Animate entrance with stagger
            AnimationUtils.animateViewEntrance(
                skeletonCard, 
                AnimationUtils.AnimationType.SLIDE_UP, 
                i * AnimationUtils.DELAY_SHORT
            );
        }
    }

    /**
     * Hide all loading states
     * @param container The container to clear
     */
    public void hideLoading(ViewGroup container) {
        // Stop all active animations
        for (Animator animator : activeAnimators) {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
        }
        activeAnimators.clear();
        
        // Stop all shimmer animations
        for (ShimmerView shimmer : activeShimmers) {
            if (shimmer != null) {
                shimmer.stopShimmer();
            }
        }
        activeShimmers.clear();
        
        // Clear container with animation
        if (container.getChildCount() > 0) {
            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);
                AnimationUtils.animateViewExit(child, AnimationUtils.AnimationType.FADE_OUT, null);
            }
            
            // Clear after animation
            container.postDelayed(container::removeAllViews, AnimationUtils.DURATION_MEDIUM);
        }
    }

    /**
     * Show loading overlay on a view
     * @param view The view to overlay
     * @return The overlay view (for manual removal)
     */
    public View showLoadingOverlay(View view) {
        if (!(view.getParent() instanceof ViewGroup)) {
            return null;
        }
        
        ViewGroup parent = (ViewGroup) view.getParent();
        
        // Create overlay
        View overlay = new View(context);
        overlay.setBackgroundColor(ContextCompat.getColor(context, R.color.loading_overlay));
        overlay.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        
        // Add progress bar to overlay
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        
        // Add overlay to parent
        parent.addView(overlay);
        
        // Animate entrance
        AnimationUtils.animateViewEntrance(overlay, AnimationUtils.AnimationType.FADE_IN, 0);
        
        return overlay;
    }

    /**
     * Hide loading overlay
     * @param overlay The overlay view to hide
     */
    public void hideLoadingOverlay(View overlay) {
        if (overlay != null && overlay.getParent() instanceof ViewGroup) {
            AnimationUtils.animateViewExit(overlay, AnimationUtils.AnimationType.FADE_OUT, () -> {
                ViewGroup parent = (ViewGroup) overlay.getParent();
                parent.removeView(overlay);
            });
        }
    }

    /**
     * Create a skeleton card view for loading
     * @return The skeleton card view
     */
    private View createSkeletonCard() {
        ShimmerView skeletonCard = new ShimmerView(context);
        
        // Set card-like appearance
        skeletonCard.setCornerRadius(12f);
        
        // Set layout parameters
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (int) (100 * context.getResources().getDisplayMetrics().density)
        );
        skeletonCard.setLayoutParams(params);
        
        // Add margin
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
            int margin = (int) (12 * context.getResources().getDisplayMetrics().density);
            marginParams.setMargins(margin, margin, margin, margin);
        }
        
        activeShimmers.add(skeletonCard);
        return skeletonCard;
    }

    /**
     * Cleanup all resources
     */
    public void cleanup() {
        // Stop all animations
        for (Animator animator : activeAnimators) {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
        }
        activeAnimators.clear();
        
        // Stop all shimmers
        for (ShimmerView shimmer : activeShimmers) {
            if (shimmer != null) {
                shimmer.stopShimmer();
            }
        }
        activeShimmers.clear();
    }
}