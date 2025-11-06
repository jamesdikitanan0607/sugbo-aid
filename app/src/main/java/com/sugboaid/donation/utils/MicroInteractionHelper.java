package com.sugboaid.donation.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.sugboaid.donation.R;

/**
 * Helper class for creating micro-interactions and button feedback
 * Provides consistent interaction patterns throughout the app
 */
public class MicroInteractionHelper {

    private static final float PRESS_SCALE = 0.95f;
    private static final float HOVER_SCALE = 1.05f;
    private static final int PRESS_DURATION = 100;
    private static final int RELEASE_DURATION = 150;
    private static final int RIPPLE_DURATION = 300;

    /**
     * Apply press and release animations to a view
     * @param view The view to apply animations to
     * @param enableHaptic Whether to enable haptic feedback
     */
    public static void applyPressAnimation(View view, boolean enableHaptic) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private AnimatorSet pressAnimator;
            private AnimatorSet releaseAnimator;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Cancel any existing animations
                        if (releaseAnimator != null) releaseAnimator.cancel();
                        
                        // Create press animation
                        pressAnimator = createPressAnimation(v);
                        pressAnimator.start();
                        
                        // Haptic feedback
                        if (enableHaptic) {
                            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        }
                        break;
                        
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Cancel press animation
                        if (pressAnimator != null) pressAnimator.cancel();
                        
                        // Create release animation
                        releaseAnimator = createReleaseAnimation(v);
                        releaseAnimator.start();
                        break;
                }
                return false; // Allow other touch events to be processed
            }
        });
    }

    /**
     * Apply hover effect to a view (for accessibility and mouse interaction)
     * @param view The view to apply hover effect to
     */
    public static void applyHoverEffect(View view) {
        view.setOnHoverListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    animateHoverEnter(v);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    animateHoverExit(v);
                    break;
            }
            return false;
        });
    }

    /**
     * Create a ripple effect animation
     * @param view The view to apply ripple to
     * @param centerX X coordinate of ripple center
     * @param centerY Y coordinate of ripple center
     */
    public static void createRippleEffect(View view, float centerX, float centerY) {
        // Create a circular reveal animation
        float maxRadius = Math.max(view.getWidth(), view.getHeight());
        
        ValueAnimator rippleAnimator = ValueAnimator.ofFloat(0f, maxRadius);
        rippleAnimator.setDuration(RIPPLE_DURATION);
        rippleAnimator.setInterpolator(new DecelerateInterpolator());
        
        rippleAnimator.addUpdateListener(animation -> {
            float radius = (float) animation.getAnimatedValue();
            // This would typically be implemented with a custom drawable
            // For now, we'll use a simple alpha animation
            float alpha = 1f - (radius / maxRadius);
            view.setAlpha(Math.max(0.1f, alpha));
        });
        
        rippleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setAlpha(1f);
            }
        });
        
        rippleAnimator.start();
    }

    /**
     * Animate button with bounce effect
     * @param button The button to animate
     */
    public static void animateButtonBounce(View button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.2f, 1f);
        
        AnimatorSet bounceSet = new AnimatorSet();
        bounceSet.playTogether(scaleX, scaleY);
        bounceSet.setDuration(400);
        bounceSet.setInterpolator(new BounceInterpolator());
        bounceSet.start();
    }

    /**
     * Animate floating action button with rotation
     * @param fab The floating action button
     */
    public static void animateFabRotation(View fab) {
        ObjectAnimator rotation = ObjectAnimator.ofFloat(fab, "rotation", 0f, 360f);
        rotation.setDuration(500);
        rotation.setInterpolator(new AccelerateDecelerateInterpolator());
        rotation.start();
    }

    /**
     * Create loading pulse animation
     * @param view The view to pulse
     * @return ValueAnimator for the pulse effect
     */
    public static AnimatorSet createPulseAnimation(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f, 1f);
        
        // Set repeat properties on individual animators
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.RESTART);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.RESTART);
        alpha.setRepeatCount(ValueAnimator.INFINITE);
        alpha.setRepeatMode(ValueAnimator.RESTART);
        
        AnimatorSet pulseSet = new AnimatorSet();
        pulseSet.playTogether(scaleX, scaleY, alpha);
        pulseSet.setDuration(1000);
        pulseSet.setInterpolator(new AccelerateDecelerateInterpolator());
        
        return pulseSet;
    }

    /**
     * Animate icon change with rotation
     * @param imageView The ImageView containing the icon
     * @param newIcon The new icon drawable
     */
    public static void animateIconChange(ImageView imageView, Drawable newIcon) {
        ObjectAnimator rotateOut = ObjectAnimator.ofFloat(imageView, "rotationY", 0f, 90f);
        rotateOut.setDuration(150);
        
        ObjectAnimator rotateIn = ObjectAnimator.ofFloat(imageView, "rotationY", -90f, 0f);
        rotateIn.setDuration(150);
        
        rotateOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                imageView.setImageDrawable(newIcon);
                rotateIn.start();
            }
        });
        
        rotateOut.start();
    }

    /**
     * Create staggered entrance animation for child views
     * @param container The parent container
     * @param staggerDelay Delay between each child animation
     */
    public static void animateChildrenStagger(ViewGroup container, int staggerDelay) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            
            // Initial state
            child.setAlpha(0f);
            child.setTranslationY(50f);
            
            // Animate entrance
            child.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(i * staggerDelay)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        }
    }

    /**
     * Animate progress bar with smooth transition
     * @param progressView The progress view
     * @param targetProgress Target progress (0-100)
     */
    public static void animateProgressBar(View progressView, int targetProgress) {
        ValueAnimator progressAnimator = ValueAnimator.ofFloat(0f, targetProgress / 100f);
        progressAnimator.setDuration(800);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        
        progressAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            progressView.setScaleX(progress);
        });
        
        progressAnimator.start();
    }

    /**
     * Create notification badge animation
     * @param badge The badge view
     * @param count The notification count
     */
    public static void animateNotificationBadge(View badge, int count) {
        if (count > 0) {
            // Show badge with bounce
            badge.setVisibility(View.VISIBLE);
            badge.setScaleX(0f);
            badge.setScaleY(0f);
            
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(badge, "scaleX", 0f, 1.3f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(badge, "scaleY", 0f, 1.3f, 1f);
            
            AnimatorSet badgeSet = new AnimatorSet();
            badgeSet.playTogether(scaleX, scaleY);
            badgeSet.setDuration(300);
            badgeSet.setInterpolator(new OvershootInterpolator());
            badgeSet.start();
        } else {
            // Hide badge with scale out
            badge.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(200)
                .withEndAction(() -> badge.setVisibility(View.GONE))
                .start();
        }
    }

    /**
     * Create success animation with checkmark
     * @param view The view to animate
     */
    public static void animateSuccess(View view) {
        // Scale animation with overshoot
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1.2f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        
        AnimatorSet successSet = new AnimatorSet();
        successSet.playTogether(scaleX, scaleY, rotation);
        successSet.setDuration(600);
        successSet.setInterpolator(new OvershootInterpolator());
        successSet.start();
    }

    /**
     * Create error shake animation
     * @param view The view to shake
     */
    public static void animateError(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 
            0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shake.setDuration(500);
        shake.start();
        
        // Add haptic feedback for error
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    /**
     * Create card flip animation
     * @param frontView The front view of the card
     * @param backView The back view of the card
     */
    public static void animateCardFlip(View frontView, View backView) {
        ObjectAnimator frontRotation = ObjectAnimator.ofFloat(frontView, "rotationY", 0f, 90f);
        ObjectAnimator backRotation = ObjectAnimator.ofFloat(backView, "rotationY", -90f, 0f);
        
        frontRotation.setDuration(300);
        backRotation.setDuration(300);
        
        frontRotation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                frontView.setVisibility(View.GONE);
                backView.setVisibility(View.VISIBLE);
                backRotation.start();
            }
        });
        
        frontRotation.start();
    }

    // Private helper methods
    private static AnimatorSet createPressAnimation(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, PRESS_SCALE);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, PRESS_SCALE);
        
        AnimatorSet pressSet = new AnimatorSet();
        pressSet.playTogether(scaleX, scaleY);
        pressSet.setDuration(PRESS_DURATION);
        pressSet.setInterpolator(new AccelerateDecelerateInterpolator());
        
        return pressSet;
    }

    private static AnimatorSet createReleaseAnimation(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", view.getScaleX(), 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", view.getScaleY(), 1f);
        
        AnimatorSet releaseSet = new AnimatorSet();
        releaseSet.playTogether(scaleX, scaleY);
        releaseSet.setDuration(RELEASE_DURATION);
        releaseSet.setInterpolator(new OvershootInterpolator(1.2f));
        
        return releaseSet;
    }

    private static void animateHoverEnter(View view) {
        view.animate()
            .scaleX(HOVER_SCALE)
            .scaleY(HOVER_SCALE)
            .setDuration(200)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private static void animateHoverExit(View view) {
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }
}