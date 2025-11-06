package com.sugboaid.donation.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sugboaid.donation.R;

/**
 * Custom view that displays a shimmer loading effect
 * Used for loading states throughout the app
 */
public class ShimmerView extends View {

    private Paint shimmerPaint;
    private LinearGradient shimmerGradient;
    private Matrix gradientMatrix;
    private ValueAnimator shimmerAnimator;
    private RectF rectF;
    
    private float shimmerTranslateX = 0f;
    private float cornerRadius = 8f;
    private int shimmerColor1;
    private int shimmerColor2;
    private int shimmerColor3;
    private boolean isShimmering = false;

    public ShimmerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ShimmerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ShimmerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        shimmerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gradientMatrix = new Matrix();
        rectF = new RectF();
        
        // Set shimmer colors based on theme
        updateShimmerColors(context);
        
        setWillNotDraw(false);
    }

    private void updateShimmerColors(Context context) {
        // Check if dark mode is active (simplified check)
        boolean isDarkMode = (getResources().getConfiguration().uiMode & 
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
            android.content.res.Configuration.UI_MODE_NIGHT_YES;
        
        if (isDarkMode) {
            shimmerColor1 = 0xFF2A2A2A;
            shimmerColor2 = 0xFF3A3A3A;
            shimmerColor3 = 0xFF2A2A2A;
        } else {
            shimmerColor1 = 0xFFE0E0E0;
            shimmerColor2 = 0xFFF5F5F5;
            shimmerColor3 = 0xFFE0E0E0;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rectF.set(0, 0, w, h);
        
        // Create shimmer gradient
        shimmerGradient = new LinearGradient(
            -w, 0, 0, 0,
            new int[]{shimmerColor1, shimmerColor2, shimmerColor3},
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        
        shimmerPaint.setShader(shimmerGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (shimmerGradient != null) {
            // Update gradient matrix for animation
            gradientMatrix.setTranslate(shimmerTranslateX, 0);
            shimmerGradient.setLocalMatrix(gradientMatrix);
            
            // Draw shimmer rectangle
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, shimmerPaint);
        }
    }

    /**
     * Start shimmer animation
     */
    public void startShimmer() {
        if (isShimmering) return;
        
        isShimmering = true;
        
        shimmerAnimator = ValueAnimator.ofFloat(0f, getWidth() + getWidth());
        shimmerAnimator.setDuration(1500);
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setRepeatMode(ValueAnimator.RESTART);
        shimmerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        shimmerAnimator.addUpdateListener(animation -> {
            shimmerTranslateX = (float) animation.getAnimatedValue();
            invalidate();
        });
        
        shimmerAnimator.start();
    }

    /**
     * Stop shimmer animation
     */
    public void stopShimmer() {
        if (!isShimmering) return;
        
        isShimmering = false;
        
        if (shimmerAnimator != null) {
            shimmerAnimator.cancel();
            shimmerAnimator = null;
        }
        
        shimmerTranslateX = 0f;
        invalidate();
    }

    /**
     * Set corner radius for the shimmer shape
     * @param radius Corner radius in pixels
     */
    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        invalidate();
    }

    /**
     * Update shimmer colors for theme changes
     * @param isDarkMode Whether dark mode is active
     */
    public void updateTheme(boolean isDarkMode) {
        if (isDarkMode) {
            shimmerColor1 = 0xFF2A2A2A;
            shimmerColor2 = 0xFF3A3A3A;
            shimmerColor3 = 0xFF2A2A2A;
        } else {
            shimmerColor1 = 0xFFE0E0E0;
            shimmerColor2 = 0xFFF5F5F5;
            shimmerColor3 = 0xFFE0E0E0;
        }
        
        // Recreate gradient with new colors
        if (getWidth() > 0 && getHeight() > 0) {
            shimmerGradient = new LinearGradient(
                -getWidth(), 0, 0, 0,
                new int[]{shimmerColor1, shimmerColor2, shimmerColor3},
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
            );
            shimmerPaint.setShader(shimmerGradient);
            invalidate();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startShimmer();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopShimmer();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        
        if (visibility == VISIBLE) {
            startShimmer();
        } else {
            stopShimmer();
        }
    }
}