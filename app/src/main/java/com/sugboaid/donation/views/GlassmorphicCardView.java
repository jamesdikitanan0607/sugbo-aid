package com.sugboaid.donation.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.sugboaid.donation.R;

/**
 * Custom CardView with glassmorphism effects
 * Provides backdrop blur, transparency, and gradient borders
 */
public class GlassmorphicCardView extends CardView {

    private Paint backgroundPaint;
    private Paint borderPaint;
    private Path clipPath;
    private RectF rectF;
    private float cornerRadius;
    private int backgroundColor;
    private int borderColor;
    private float borderWidth;
    private float backgroundAlpha;
    private boolean isDarkMode;

    public GlassmorphicCardView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public GlassmorphicCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GlassmorphicCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Initialize paints
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clipPath = new Path();
        rectF = new RectF();

        // Set default values
        cornerRadius = 16f;
        borderWidth = 1f;
        backgroundAlpha = 0.6f;
        isDarkMode = false;

        // Read custom attributes if provided
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GlassmorphicCardView);
            
            cornerRadius = a.getDimension(R.styleable.GlassmorphicCardView_glassCornerRadius, cornerRadius);
            borderWidth = a.getDimension(R.styleable.GlassmorphicCardView_glassBorderWidth, borderWidth);
            backgroundAlpha = a.getFloat(R.styleable.GlassmorphicCardView_glassBackgroundAlpha, backgroundAlpha);
            backgroundColor = a.getColor(R.styleable.GlassmorphicCardView_glassBackgroundColor, 
                ContextCompat.getColor(context, R.color.glass_white_60));
            borderColor = a.getColor(R.styleable.GlassmorphicCardView_glassBorderColor, 
                ContextCompat.getColor(context, R.color.glass_border));
            
            a.recycle();
        } else {
            // Set default colors
            backgroundColor = ContextCompat.getColor(context, R.color.glass_white_60);
            borderColor = ContextCompat.getColor(context, R.color.glass_border);
        }

        // Configure CardView properties
        setCardElevation(8f);
        setRadius(cornerRadius);
        setCardBackgroundColor(android.graphics.Color.TRANSPARENT);
        
        // Enable drawing
        setWillNotDraw(false);
        
        // Setup paints
        setupPaints();
    }

    private void setupPaints() {
        // Background paint with transparency
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setAlpha((int) (255 * backgroundAlpha));

        // Border paint
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(borderColor);
        borderPaint.setAlpha((int) (255 * 0.3f)); // Semi-transparent border
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Update rect for drawing
        rectF.set(borderWidth / 2, borderWidth / 2, 
                 w - borderWidth / 2, h - borderWidth / 2);
        
        // Update clip path
        clipPath.reset();
        clipPath.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Save canvas state
        int saveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        
        // Draw glassmorphic background with enhanced effects
        drawGlassmorphicBackground(canvas);
        
        // Draw subtle inner shadow for depth
        drawInnerShadow(canvas);
        
        // Draw border
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint);
        
        // Restore canvas state
        canvas.restoreToCount(saveCount);
        
        super.onDraw(canvas);
    }

    /**
     * Draw glassmorphic background with enhanced transparency effects
     */
    private void drawGlassmorphicBackground(Canvas canvas) {
        // Create multiple layers for better glassmorphic effect
        Paint layerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // Base layer with lower opacity
        layerPaint.setColor(backgroundColor);
        layerPaint.setAlpha((int) (255 * backgroundAlpha * 0.6f));
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, layerPaint);
        
        // Secondary layer with slight offset for depth
        RectF offsetRect = new RectF(rectF);
        offsetRect.inset(1f, 1f);
        layerPaint.setAlpha((int) (255 * backgroundAlpha * 0.3f));
        canvas.drawRoundRect(offsetRect, cornerRadius - 1f, cornerRadius - 1f, layerPaint);
        
        // Main background layer
        layerPaint.setAlpha((int) (255 * backgroundAlpha));
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, layerPaint);
    }

    /**
     * Draw subtle inner shadow for depth effect
     */
    private void drawInnerShadow(Canvas canvas) {
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.STROKE);
        shadowPaint.setStrokeWidth(1f);
        
        // Inner shadow color based on theme
        int shadowColor = isDarkMode ? 
            ContextCompat.getColor(getContext(), R.color.glass_white_10) :
            ContextCompat.getColor(getContext(), R.color.glass_dark_20);
        
        shadowPaint.setColor(shadowColor);
        shadowPaint.setAlpha(80);
        
        // Draw inner shadow
        RectF shadowRect = new RectF(rectF);
        shadowRect.inset(0.5f, 0.5f);
        canvas.drawRoundRect(shadowRect, cornerRadius - 0.5f, cornerRadius - 0.5f, shadowPaint);
    }

    /**
     * Set glassmorphic background color
     * @param color The background color
     */
    public void setGlassBackgroundColor(int color) {
        this.backgroundColor = color;
        backgroundPaint.setColor(color);
        backgroundPaint.setAlpha((int) (255 * backgroundAlpha));
        invalidate();
    }

    /**
     * Set glassmorphic border color
     * @param color The border color
     */
    public void setGlassBorderColor(int color) {
        this.borderColor = color;
        borderPaint.setColor(color);
        invalidate();
    }

    /**
     * Set background alpha (transparency)
     * @param alpha Alpha value between 0.0 and 1.0
     */
    public void setGlassBackgroundAlpha(float alpha) {
        this.backgroundAlpha = Math.max(0f, Math.min(1f, alpha));
        backgroundPaint.setAlpha((int) (255 * backgroundAlpha));
        invalidate();
    }

    /**
     * Set corner radius
     * @param radius Corner radius in pixels
     */
    public void setGlassCornerRadius(float radius) {
        this.cornerRadius = radius;
        setRadius(radius);
        invalidate();
    }

    /**
     * Set border width
     * @param width Border width in pixels
     */
    public void setGlassBorderWidth(float width) {
        this.borderWidth = width;
        borderPaint.setStrokeWidth(width);
        invalidate();
    }

    /**
     * Update theme mode for glassmorphic effects
     * @param isDarkMode Whether dark mode is active
     */
    public void setDarkMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        updateThemeColors();
    }

    /**
     * Update colors based on current theme
     */
    private void updateThemeColors() {
        Context context = getContext();
        
        // Get theme attributes
        int[] attrs = {R.attr.colorGlassBackground, R.attr.colorGlassBorder};
        android.content.res.TypedArray ta = context.obtainStyledAttributes(attrs);
        
        try {
            backgroundColor = ta.getColor(0, isDarkMode ? 
                ContextCompat.getColor(context, R.color.glass_dark_20) : 
                ContextCompat.getColor(context, R.color.glass_white_20));
            borderColor = ta.getColor(1, ContextCompat.getColor(context, R.color.glass_border));
        } finally {
            ta.recycle();
        }
        
        setupPaints();
        invalidate();
    }

    /**
     * Apply theme automatically based on current configuration
     */
    public void applyCurrentTheme() {
        int nightModeFlags = getContext().getResources().getConfiguration().uiMode & 
                           android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean isDark = nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        setDarkMode(isDark);
    }

    /**
     * Apply glassmorphic effect with animation
     */
    public void applyGlassmorphicEffect() {
        // Animate the alpha for a smooth glassmorphic effect
        animate()
            .alpha(0.9f)
            .setDuration(300)
            .withEndAction(() -> animate().alpha(1f).setDuration(200));
    }
}