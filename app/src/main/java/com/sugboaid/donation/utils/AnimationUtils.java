package com.sugboaid.donation.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
// Removed conflicting import - using android.view.animation.AnimationUtils directly
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.sugboaid.donation.R;

/**
 * Utility class for managing animations throughout the app
 * Provides consistent animation timing, easing, and effects
 */
public class AnimationUtils {

    // Animation durations
    public static final int DURATION_SHORT = 150;
    public static final int DURATION_MEDIUM = 300;
    public static final int DURATION_LONG = 500;
    public static final int DURATION_EXTRA_LONG = 800;

    // Animation delays
    public static final int DELAY_SHORT = 50;
    public static final int DELAY_MEDIUM = 100;
    public static final int DELAY_LONG = 200;

    /**
     * Apply entrance animation to a view
     * @param view The view to animate
     * @param animationType Type of animation (slide, fade, scale)
     * @param delay Delay before starting animation
     */
    public static void animateViewEntrance(View view, AnimationType animationType, int delay) {
        if (view == null) return;

        view.setVisibility(View.VISIBLE);
        
        switch (animationType) {
            case SLIDE_UP:
                animateSlideUp(view, delay);
                break;
            case SLIDE_DOWN:
                animateSlideDown(view, delay);
                break;
            case SLIDE_LEFT:
                animateSlideLeft(view, delay);
                break;
            case SLIDE_RIGHT:
                animateSlideRight(view, delay);
                break;
            case FADE_IN:
                animateFadeIn(view, delay);
                break;
            case SCALE_IN:
                animateScaleIn(view, delay);
                break;
            case BOUNCE_IN:
                animateBounceIn(view, delay);
                break;
        }
    }

    /**
     * Apply exit animation to a view
     * @param view The view to animate
     * @param animationType Type of animation
     * @param onComplete Callback when animation completes
     */
    public static void animateViewExit(View view, AnimationType animationType, Runnable onComplete) {
        if (view == null) return;

        switch (animationType) {
            case SLIDE_UP:
                animateSlideUpExit(view, onComplete);
                break;
            case SLIDE_DOWN:
                animateSlideDownExit(view, onComplete);
                break;
            case SLIDE_LEFT:
                animateSlideLeftExit(view, onComplete);
                break;
            case SLIDE_RIGHT:
                animateSlideRightExit(view, onComplete);
                break;
            case FADE_OUT:
                animateFadeOut(view, onComplete);
                break;
            case SCALE_OUT:
                animateScaleOut(view, onComplete);
                break;
        }
    }

    /**
     * Animate fragment transitions
     * @param transaction FragmentTransaction to apply animations to
     * @param transitionType Type of transition
     */
    public static void applyFragmentTransition(FragmentTransaction transaction, TransitionType transitionType) {
        switch (transitionType) {
            case SLIDE_HORIZONTAL:
                transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                );
                break;
            case SLIDE_VERTICAL:
                transaction.setCustomAnimations(
                    R.anim.slide_up_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_down_out
                );
                break;
            case FADE:
                transaction.setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.fade_out
                );
                break;
            case SCALE:
                transaction.setCustomAnimations(
                    R.anim.scale_in,
                    R.anim.scale_out,
                    R.anim.scale_in,
                    R.anim.scale_out
                );
                break;
        }
    }

    /**
     * Animate button press effect
     * @param view The button view
     */
    public static void animateButtonPress(View view) {
        if (view == null) return;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(DURATION_SHORT);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    /**
     * Animate floating action button entrance
     * @param fab The floating action button
     */
    public static void animateFabEntrance(View fab) {
        if (fab == null) return;

        fab.setScaleX(0f);
        fab.setScaleY(0f);
        fab.setVisibility(View.VISIBLE);
        
        fab.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(DURATION_MEDIUM)
            .setInterpolator(new OvershootInterpolator())
            .start();
    }

    /**
     * Animate card entrance with stagger effect
     * @param container ViewGroup containing cards
     * @param staggerDelay Delay between each card animation
     */
    public static void animateCardEntranceStagger(ViewGroup container, int staggerDelay) {
        if (container == null) return;

        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            animateViewEntrance(child, AnimationType.SLIDE_UP, i * staggerDelay);
        }
    }

    /**
     * Create shimmer loading animation
     * @param view The view to apply shimmer to
     * @return ValueAnimator for the shimmer effect
     */
    public static ValueAnimator createShimmerAnimation(View view) {
        if (view == null) return null;

        ValueAnimator shimmerAnimator = ValueAnimator.ofFloat(0f, 1f);
        shimmerAnimator.setDuration(1500);
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setRepeatMode(ValueAnimator.RESTART);
        shimmerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        shimmerAnimator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            view.setAlpha(0.3f + (animatedValue * 0.7f));
        });
        
        return shimmerAnimator;
    }

    /**
     * Animate progress bar with smooth transition
     * @param progressBar The progress bar view (can be any view with progress)
     * @param targetProgress Target progress value (0-100)
     */
    public static void animateProgress(View progressBar, int targetProgress) {
        // This would be implemented based on the specific progress bar implementation
        // For now, we'll create a generic scale animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(progressBar, "scaleX", 0f, targetProgress / 100f);
        scaleX.setDuration(DURATION_LONG);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleX.start();
    }

    /**
     * Animate notification badge appearance
     * @param badge The badge view
     */
    public static void animateBadgeAppearance(View badge) {
        if (badge == null) return;

        badge.setScaleX(0f);
        badge.setScaleY(0f);
        badge.setVisibility(View.VISIBLE);
        
        badge.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(DURATION_SHORT)
            .setInterpolator(new OvershootInterpolator(2f))
            .start();
    }

    /**
     * Animate view shake (for error states)
     * @param view The view to shake
     */
    public static void animateShake(View view) {
        if (view == null) return;

        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shake.setDuration(DURATION_LONG);
        shake.start();
    }

    /**
     * Animate success checkmark
     * @param view The checkmark view
     */
    public static void animateSuccessCheckmark(View view) {
        if (view == null) return;

        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setVisibility(View.VISIBLE);
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1.2f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, rotation);
        animatorSet.setDuration(DURATION_LONG);
        animatorSet.setInterpolator(new OvershootInterpolator());
        animatorSet.start();
    }

    // Private animation methods
    private static void animateSlideUp(View view, int delay) {
        view.setTranslationY(view.getHeight());
        view.setAlpha(0f);
        
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(DURATION_MEDIUM)
            .setStartDelay(delay)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private static void animateSlideDown(View view, int delay) {
        view.setTranslationY(-view.getHeight());
        view.setAlpha(0f);
        
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(DURATION_MEDIUM)
            .setStartDelay(delay)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private static void animateSlideLeft(View view, int delay) {
        view.setTranslationX(view.getWidth());
        view.setAlpha(0f);
        
        view.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(DURATION_MEDIUM)
            .setStartDelay(delay)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private static void animateSlideRight(View view, int delay) {
        view.setTranslationX(-view.getWidth());
        view.setAlpha(0f);
        
        view.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(DURATION_MEDIUM)
            .setStartDelay(delay)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private static void animateFadeIn(View view, int delay) {
        view.setAlpha(0f);
        
        view.animate()
            .alpha(1f)
            .setDuration(DURATION_MEDIUM)
            .setStartDelay(delay)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private static void animateScaleIn(View view, int delay) {
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setAlpha(0f);
        
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(DURATION_MEDIUM)
            .setStartDelay(delay)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private static void animateBounceIn(View view, int delay) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(0f);
        
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(DURATION_MEDIUM)
            .setStartDelay(delay)
            .setInterpolator(new OvershootInterpolator())
            .start();
    }

    // Exit animations
    private static void animateSlideUpExit(View view, Runnable onComplete) {
        view.animate()
            .translationY(-view.getHeight())
            .alpha(0f)
            .setDuration(DURATION_MEDIUM)
            .setInterpolator(new AccelerateInterpolator())
            .withEndAction(() -> {
                view.setVisibility(View.GONE);
                if (onComplete != null) onComplete.run();
            })
            .start();
    }

    private static void animateSlideDownExit(View view, Runnable onComplete) {
        view.animate()
            .translationY(view.getHeight())
            .alpha(0f)
            .setDuration(DURATION_MEDIUM)
            .setInterpolator(new AccelerateInterpolator())
            .withEndAction(() -> {
                view.setVisibility(View.GONE);
                if (onComplete != null) onComplete.run();
            })
            .start();
    }

    private static void animateSlideLeftExit(View view, Runnable onComplete) {
        view.animate()
            .translationX(-view.getWidth())
            .alpha(0f)
            .setDuration(DURATION_MEDIUM)
            .setInterpolator(new AccelerateInterpolator())
            .withEndAction(() -> {
                view.setVisibility(View.GONE);
                if (onComplete != null) onComplete.run();
            })
            .start();
    }

    private static void animateSlideRightExit(View view, Runnable onComplete) {
        view.animate()
            .translationX(view.getWidth())
            .alpha(0f)
            .setDuration(DURATION_MEDIUM)
            .setInterpolator(new AccelerateInterpolator())
            .withEndAction(() -> {
                view.setVisibility(View.GONE);
                if (onComplete != null) onComplete.run();
            })
            .start();
    }

    private static void animateFadeOut(View view, Runnable onComplete) {
        view.animate()
            .alpha(0f)
            .setDuration(DURATION_MEDIUM)
            .setInterpolator(new AccelerateInterpolator())
            .withEndAction(() -> {
                view.setVisibility(View.GONE);
                if (onComplete != null) onComplete.run();
            })
            .start();
    }

    private static void animateScaleOut(View view, Runnable onComplete) {
        view.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .alpha(0f)
            .setDuration(DURATION_MEDIUM)
            .setInterpolator(new AccelerateInterpolator())
            .withEndAction(() -> {
                view.setVisibility(View.GONE);
                if (onComplete != null) onComplete.run();
            })
            .start();
    }

    /**
     * Animation types for view entrance/exit
     */
    public enum AnimationType {
        SLIDE_UP,
        SLIDE_DOWN,
        SLIDE_LEFT,
        SLIDE_RIGHT,
        FADE_IN,
        FADE_OUT,
        SCALE_IN,
        SCALE_OUT,
        BOUNCE_IN
    }

    /**
     * Transition types for fragment navigation
     */
    public enum TransitionType {
        SLIDE_HORIZONTAL,
        SLIDE_VERTICAL,
        FADE,
        SCALE
    }
}