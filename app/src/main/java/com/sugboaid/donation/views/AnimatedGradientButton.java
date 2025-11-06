package com.sugboaid.donation.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.sugboaid.donation.R;

/**
 * Custom Button with animated gradient backgrounds and press effects
 */
public class AnimatedGradientButton extends AppCompatButton {

    private Paint backgroundPaint;
    private RectF rectF;
    private ValueAnimator gradientAnimator;
    private ValueAnimator pressAnimator;
    
    private int gradientStartColor;
    private int gradientEndColor;
    private int gradientOrientation;
    private int animationDuration;
    private float cornerRadius;
    private float currentAnimationValue = 0f;
    private float pressScale = 1f;
    
    private static final int ORIENTATION_HORIZONTAL = 0;
    private static final int ORIENTATION_VERTICAL = 1;
    private static final int ORIENTATION_DIAGONAL = 2;

    public AnimatedGradientButton(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public AnimatedGradientButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AnimatedGradientButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Initialize paint and rect
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();
        
        // Set default values
        gradientStartColor = ContextCompat.getColor(context, R.color.primary_blue);
        gradientEndColor = ContextCompat.getColor(context, R.color.light_blue);
        gradientOrientation = ORIENTATION_HORIZONTAL;
        animationDuration = 2000; // 2 seconds
        cornerRadius = 12f;
        
        // Read custom attributes if provided
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimatedGradientButton);
            
            gradientStartColor = a.getColor(R.styleable.AnimatedGradientButton_gradientStartColor, gradientStartColor);
            gradientEndColor = a.getColor(R.styleable.AnimatedGradientButton_gradientEndColor, gradientEndColor);
            gradientOrientation = a.getInt(R.styleable.AnimatedGradientButton_gradientOrientation, gradientOrientation);
            animationDuration = a.getInt(R.styleable.AnimatedGradientButton_animationDuration, animationDuration);
            cornerRadius = a.getDimension(R.styleable.AnimatedGradientButton_cornerRadius, cornerRadius);
            
            a.recycle();
        }
        
        // Configure button properties
        setBackground(null); // Remove default background
        setWillNotDraw(false);
        
        // Setup gradient animation
        setupGradientAnimation();
    }

    private void setupGradientAnimation() {
        gradientAnimator = ValueAnimator.ofFloat(0f, 1f);
        gradientAnimator.setDuration(animationDuration);
        gradientAnimator.setRepeatCount(ValueAnimator.INFINITE);
        gradientAnimator.setRepeatMode(ValueAnimator.REVERSE);
        gradientAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        gradientAnimator.addUpdateListener(animation -> {
            currentAnimationValue = (float) animation.getAnimatedValue();
            invalidate();
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rectF.set(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Save canvas state
        int saveCount = canvas.save();
        
        // Apply press scale
        if (pressScale != 1f) {
            float pivotX = getWidth() / 2f;
            float pivotY = getHeight() / 2f;
            canvas.scale(pressScale, pressScale, pivotX, pivotY);
        }
        
        // Create animated gradient
        LinearGradient gradient = createAnimatedGradient();
        backgroundPaint.setShader(gradient);
        
        // Draw rounded rectangle background
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, backgroundPaint);
        
        // Restore canvas state
        canvas.restoreToCount(saveCount);
        
        // Draw text
        super.onDraw(canvas);
    }

    private LinearGradient createAnimatedGradient() {
        float width = getWidth();
        float height = getHeight();
        
        // Calculate animated color positions
        float animatedOffset = currentAnimationValue * 0.3f; // 30% color shift
        
        int startColor = interpolateColor(gradientStartColor, gradientEndColor, animatedOffset);
        int endColor = interpolateColor(gradientEndColor, gradientStartColor, animatedOffset);
        
        // Create gradient based on orientation
        switch (gradientOrientation) {
            case ORIENTATION_VERTICAL:
                return new LinearGradient(0, 0, 0, height, startColor, endColor, Shader.TileMode.CLAMP);
            case ORIENTATION_DIAGONAL:
                return new LinearGradient(0, 0, width, height, startColor, endColor, Shader.TileMode.CLAMP);
            case ORIENTATION_HORIZONTAL:
            default:
                return new LinearGradient(0, 0, width, 0, startColor, endColor, Shader.TileMode.CLAMP);
        }
    }

    private int interpolateColor(int colorA, int colorB, float factor) {
        if (factor <= 0f) return colorA;
        if (factor >= 1f) return colorB;
        
        int aA = (colorA >> 24) & 0xFF;
        int aR = (colorA >> 16) & 0xFF;
        int aG = (colorA >> 8) & 0xFF;
        int aB = colorA & 0xFF;
        
        int bA = (colorB >> 24) & 0xFF;
        int bR = (colorB >> 16) & 0xFF;
        int bG = (colorB >> 8) & 0xFF;
        int bBB = colorB & 0xFF;
        
        int resultA = (int) (aA + factor * (bA - aA));
        int resultR = (int) (aR + factor * (bR - aR));
        int resultG = (int) (aG + factor * (bG - aG));
        int resultB = (int) (aB + factor * (bBB - aB));
        
        return (resultA << 24) | (resultR << 16) | (resultG << 8) | resultB;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                animatePress(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                animatePress(false);
                break;
        }
        return super.onTouchEvent(event);
    }

    private void animatePress(boolean pressed) {
        if (pressAnimator != null) {
            pressAnimator.cancel();
        }
        
        float targetScale = pressed ? 0.95f : 1f;
        pressAnimator = ValueAnimator.ofFloat(pressScale, targetScale);
        pressAnimator.setDuration(150);
        pressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        pressAnimator.addUpdateListener(animation -> {
            pressScale = (float) animation.getAnimatedValue();
            invalidate();
        });
        
        pressAnimator.start();
    }

    /**
     * Start gradient animation
     */
    public void startGradientAnimation() {
        if (gradientAnimator != null && !gradientAnimator.isRunning()) {
            gradientAnimator.start();
        }
    }

    /**
     * Stop gradient animation
     */
    public void stopGradientAnimation() {
        if (gradientAnimator != null && gradientAnimator.isRunning()) {
            gradientAnimator.cancel();
        }
    }

    /**
     * Set gradient colors
     * @param startColor Start color of gradient
     * @param endColor End color of gradient
     */
    public void setGradientColors(int startColor, int endColor) {
        this.gradientStartColor = startColor;
        this.gradientEndColor = endColor;
        invalidate();
    }

    /**
     * Set gradient orientation
     * @param orientation Gradient orientation (0=horizontal, 1=vertical, 2=diagonal)
     */
    public void setGradientOrientation(int orientation) {
        this.gradientOrientation = orientation;
        invalidate();
    }

    /**
     * Set animation duration
     * @param duration Animation duration in milliseconds
     */
    public void setAnimationDuration(int duration) {
        this.animationDuration = duration;
        if (gradientAnimator != null) {
            gradientAnimator.setDuration(duration);
        }
    }

    /**
     * Set corner radius
     * @param radius Corner radius in pixels
     */
    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        invalidate();
    }

    /**
     * Animate click effect (for programmatic clicks)
     */
    public void animateClick() {
        animatePress(true);
        postDelayed(() -> animatePress(false), 150);
    }

    /**
     * Update theme colors for the button
     * @param isDarkMode Whether dark mode is active
     */
    public void updateTheme(boolean isDarkMode) {
        Context context = getContext();
        
        // Adjust gradient colors based on theme
        if (isDarkMode) {
            // Slightly brighter colors for dark theme
            int alpha = 200; // Slightly transparent for glassmorphic effect
            gradientStartColor = adjustColorAlpha(gradientStartColor, alpha);
            gradientEndColor = adjustColorAlpha(gradientEndColor, alpha);
        } else {
            // Standard colors for light theme
            gradientStartColor = ContextCompat.getColor(context, R.color.primary_blue);
            gradientEndColor = ContextCompat.getColor(context, R.color.light_blue);
        }
        
        invalidate();
    }

    /**
     * Adjust color alpha for transparency effects
     */
    private int adjustColorAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    /**
     * Apply glassmorphic effect to the button
     */
    public void applyGlassmorphicEffect() {
        // Add subtle transparency and blur-like effect
        setAlpha(0.95f);
        
        // Animate entrance effect
        setScaleX(0.9f);
        setScaleY(0.9f);
        animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startGradientAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopGradientAnimation();
        if (pressAnimator != null) {
            pressAnimator.cancel();
        }
    }
}