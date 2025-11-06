package com.sugboaid.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.sugboaid.donation.R;
import com.sugboaid.donation.views.GlassmorphicCardView;
import com.sugboaid.donation.views.AnimatedGradientButton;
import com.sugboaid.donation.views.StatisticsCard;

/**
 * Utility class for applying glassmorphism effects consistently across the app
 * Provides methods to apply glassmorphic styling to various UI components
 */
public class GlassmorphismUtils {

    /**
     * Apply glassmorphic effect to a CardView
     * @param cardView The CardView to apply effects to
     * @param context The context
     */
    public static void applyGlassmorphicCard(CardView cardView, Context context) {
        if (cardView == null) return;
        
        // Set glassmorphic background
        cardView.setBackground(ContextCompat.getDrawable(context, R.drawable.glassmorphic_card_background));
        
        // Configure CardView properties
        cardView.setCardElevation(8f);
        cardView.setRadius(16f);
        cardView.setCardBackgroundColor(android.graphics.Color.TRANSPARENT);
        
        // Add subtle animation
        cardView.setAlpha(0.9f);
        cardView.animate()
            .alpha(1f)
            .setDuration(300)
            .start();
    }

    /**
     * Apply glassmorphic effect to a MaterialButton
     * @param button The MaterialButton to apply effects to
     * @param context The context
     * @param buttonType The type of button (primary, success, warning, etc.)
     */
    public static void applyGlassmorphicButton(MaterialButton button, Context context, ButtonType buttonType) {
        if (button == null) return;
        
        Drawable background;
        switch (buttonType) {
            case SUCCESS:
                background = ContextCompat.getDrawable(context, R.drawable.glassmorphic_button_success);
                break;
            case PRIMARY:
            default:
                background = ContextCompat.getDrawable(context, R.drawable.glassmorphic_button_primary);
                break;
        }
        
        button.setBackground(background);
        button.setElevation(4f);
        
        // Add press animation
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }

    /**
     * Apply glassmorphic effect to a View with background
     * @param view The View to apply effects to
     * @param context The context
     */
    public static void applyGlassmorphicBackground(View view, Context context) {
        if (view == null) return;
        
        view.setBackground(ContextCompat.getDrawable(context, R.drawable.glassmorphic_card_background));
        view.setElevation(4f);
        
        // Add entrance animation
        view.setAlpha(0f);
        view.setScaleX(0.9f);
        view.setScaleY(0.9f);
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .start();
    }

    /**
     * Apply glassmorphic effects to all child views in a ViewGroup
     * @param viewGroup The ViewGroup containing child views
     * @param context The context
     */
    public static void applyGlassmorphicToChildren(ViewGroup viewGroup, Context context) {
        if (viewGroup == null) return;
        
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            
            if (child instanceof GlassmorphicCardView) {
                ((GlassmorphicCardView) child).applyCurrentTheme();
            } else if (child instanceof AnimatedGradientButton) {
                boolean isDarkMode = ThemeUtils.isDarkModeActive(context);
                ((AnimatedGradientButton) child).updateTheme(isDarkMode);
                ((AnimatedGradientButton) child).applyGlassmorphicEffect();
            } else if (child instanceof CardView) {
                applyGlassmorphicCard((CardView) child, context);
            } else if (child instanceof MaterialButton) {
                applyGlassmorphicButton((MaterialButton) child, context, ButtonType.PRIMARY);
            } else if (child instanceof ViewGroup) {
                // Recursively apply to nested ViewGroups
                applyGlassmorphicToChildren((ViewGroup) child, context);
            }
        }
    }

    /**
     * Update glassmorphic effects based on theme change
     * @param view The root view to update
     * @param context The context
     * @param isDarkMode Whether dark mode is active
     */
    public static void updateGlassmorphicTheme(View view, Context context, boolean isDarkMode) {
        if (view == null) return;
        
        if (view instanceof GlassmorphicCardView) {
            ((GlassmorphicCardView) view).setDarkMode(isDarkMode);
        } else if (view instanceof AnimatedGradientButton) {
            ((AnimatedGradientButton) view).updateTheme(isDarkMode);
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                updateGlassmorphicTheme(viewGroup.getChildAt(i), context, isDarkMode);
            }
        }
    }

    /**
     * Create glassmorphic entrance animation
     * @param view The view to animate
     * @param delay Delay before animation starts
     */
    public static void animateGlassmorphicEntrance(View view, long delay) {
        if (view == null) return;
        
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.setScaleX(0.9f);
        view.setScaleY(0.9f);
        
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setStartDelay(delay)
            .setInterpolator(new android.view.animation.DecelerateInterpolator())
            .start();
    }

    /**
     * Create glassmorphic hover effect
     * @param view The view to add hover effect to
     */
    public static void addGlassmorphicHoverEffect(View view) {
        if (view == null) return;
        
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate()
                        .scaleX(1.05f)
                        .scaleY(1.05f)
                        .alpha(0.9f)
                        .setDuration(150)
                        .start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(150)
                        .start();
                    break;
            }
            return false;
        });
    }

    /**
     * Apply glassmorphic blur effect simulation
     * @param view The view to apply blur effect to
     */
    public static void applyBlurEffect(View view) {
        if (view == null) return;
        
        // Simulate blur effect with multiple layers and transparency
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        view.setAlpha(0.95f);
        
        // Add subtle shadow for depth
        view.setElevation(8f);
    }

    /**
     * Button types for glassmorphic styling
     */
    public enum ButtonType {
        PRIMARY,
        SUCCESS,
        WARNING,
        DANGER,
        INFO
    }
}