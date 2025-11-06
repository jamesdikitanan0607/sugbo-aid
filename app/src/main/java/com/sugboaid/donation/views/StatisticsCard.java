package com.sugboaid.donation.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sugboaid.donation.R;

import java.text.DecimalFormat;

/**
 * Custom view for displaying statistics with animated number counting
 * Includes gradient background, icon, title, value, and percentage change
 */
public class StatisticsCard extends View {

    private Paint backgroundPaint;
    private TextPaint titlePaint;
    private TextPaint valuePaint;
    private TextPaint percentagePaint;
    private RectF rectF;
    
    private String title;
    private String targetValue;
    private String percentage;
    private Drawable icon;
    private int gradientStartColor;
    private int gradientEndColor;
    private boolean animateNumbers;
    
    private float cornerRadius;
    private float currentValue = 0f;
    private float targetNumericValue = 0f;
    private ValueAnimator numberAnimator;
    private DecimalFormat decimalFormat;
    
    private float titleTextSize;
    private float valueTextSize;
    private float percentageTextSize;
    private int titleColor;
    private int valueColor;
    private int percentageColor;
    
    private static final float PADDING = 24f;
    private static final float ICON_SIZE = 32f;

    public StatisticsCard(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public StatisticsCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StatisticsCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Initialize paints
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        valuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        percentagePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();
        decimalFormat = new DecimalFormat("#,###");
        
        // Set default values
        title = "Statistics";
        targetValue = "0";
        percentage = "+0%";
        cornerRadius = 16f;
        animateNumbers = true;
        
        gradientStartColor = ContextCompat.getColor(context, R.color.primary_blue);
        gradientEndColor = ContextCompat.getColor(context, R.color.light_blue);
        
        titleTextSize = 14f * getResources().getDisplayMetrics().scaledDensity;
        valueTextSize = 24f * getResources().getDisplayMetrics().scaledDensity;
        percentageTextSize = 12f * getResources().getDisplayMetrics().scaledDensity;
        
        titleColor = ContextCompat.getColor(context, android.R.color.white);
        valueColor = ContextCompat.getColor(context, android.R.color.white);
        percentageColor = ContextCompat.getColor(context, R.color.success_green);
        
        // Read custom attributes if provided
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StatisticsCard);
            
            title = a.getString(R.styleable.StatisticsCard_statTitle);
            targetValue = a.getString(R.styleable.StatisticsCard_statValue);
            percentage = a.getString(R.styleable.StatisticsCard_statPercentage);
            
            int iconRes = a.getResourceId(R.styleable.StatisticsCard_statIcon, 0);
            if (iconRes != 0) {
                icon = ContextCompat.getDrawable(context, iconRes);
            }
            
            gradientStartColor = a.getColor(R.styleable.StatisticsCard_statGradientStart, gradientStartColor);
            gradientEndColor = a.getColor(R.styleable.StatisticsCard_statGradientEnd, gradientEndColor);
            animateNumbers = a.getBoolean(R.styleable.StatisticsCard_animateNumbers, animateNumbers);
            
            a.recycle();
        }
        
        // Setup paints
        setupPaints();
        
        // Parse target numeric value for animation
        parseTargetValue();
    }

    private void setupPaints() {
        // Title paint
        titlePaint.setTextSize(titleTextSize);
        titlePaint.setColor(titleColor);
        titlePaint.setTypeface(Typeface.DEFAULT);
        titlePaint.setTextAlign(Paint.Align.LEFT);
        
        // Value paint
        valuePaint.setTextSize(valueTextSize);
        valuePaint.setColor(valueColor);
        valuePaint.setTypeface(Typeface.DEFAULT_BOLD);
        valuePaint.setTextAlign(Paint.Align.LEFT);
        
        // Percentage paint
        percentagePaint.setTextSize(percentageTextSize);
        percentagePaint.setColor(percentageColor);
        percentagePaint.setTypeface(Typeface.DEFAULT);
        percentagePaint.setTextAlign(Paint.Align.RIGHT);
    }

    private void parseTargetValue() {
        if (targetValue != null) {
            try {
                // Remove non-numeric characters except decimal point
                String numericString = targetValue.replaceAll("[^\\d.]", "");
                if (!numericString.isEmpty()) {
                    targetNumericValue = Float.parseFloat(numericString);
                }
            } catch (NumberFormatException e) {
                targetNumericValue = 0f;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rectF.set(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw gradient background
        drawGradientBackground(canvas);
        
        // Draw content
        drawContent(canvas);
    }

    private void drawGradientBackground(Canvas canvas) {
        LinearGradient gradient = new LinearGradient(
            0, 0, getWidth(), getHeight(),
            gradientStartColor, gradientEndColor,
            Shader.TileMode.CLAMP
        );
        
        backgroundPaint.setShader(gradient);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, backgroundPaint);
    }

    private void drawContent(Canvas canvas) {
        float padding = PADDING * getResources().getDisplayMetrics().density;
        float iconSize = ICON_SIZE * getResources().getDisplayMetrics().density;
        
        // Draw icon if available
        float iconTop = padding;
        if (icon != null) {
            icon.setBounds(
                (int) padding,
                (int) iconTop,
                (int) (padding + iconSize),
                (int) (iconTop + iconSize)
            );
            icon.draw(canvas);
        }
        
        // Draw title
        float titleY = iconTop + iconSize + padding / 2;
        canvas.drawText(title != null ? title : "", padding, titleY, titlePaint);
        
        // Draw animated value
        String displayValue = animateNumbers ? 
            formatAnimatedValue(currentValue) : 
            (targetValue != null ? targetValue : "0");
        
        float valueY = titleY + titleTextSize + padding / 3;
        canvas.drawText(displayValue, padding, valueY, valuePaint);
        
        // Draw percentage with trend arrow
        if (percentage != null) {
            float percentageY = valueY + percentageTextSize + padding / 4;
            
            // Draw trend arrow
            String arrow = "";
            if (percentage.startsWith("+")) {
                arrow = "↗ ";
            } else if (percentage.startsWith("-")) {
                arrow = "↘ ";
            }
            
            String percentageText = arrow + percentage;
            canvas.drawText(percentageText, getWidth() - padding, percentageY, percentagePaint);
        }
        
        // Set content description for accessibility
        updateContentDescription();
    }

    private void updateContentDescription() {
        String description = String.format("%s: %s, %s change", 
            title != null ? title : "Statistic",
            targetValue != null ? targetValue : "0",
            percentage != null ? percentage : "no change");
        setContentDescription(description);
    }

    private String formatAnimatedValue(float value) {
        if (targetValue != null && targetValue.contains("₱")) {
            return "₱" + decimalFormat.format((long) value);
        } else if (targetValue != null && targetValue.contains("%")) {
            return decimalFormat.format((long) value) + "%";
        } else {
            return decimalFormat.format((long) value);
        }
    }

    /**
     * Start number animation
     */
    public void startAnimation() {
        if (!animateNumbers || targetNumericValue == 0f) return;
        
        if (numberAnimator != null) {
            numberAnimator.cancel();
        }
        
        numberAnimator = ValueAnimator.ofFloat(0f, targetNumericValue);
        numberAnimator.setDuration(2000); // 2 seconds
        numberAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        numberAnimator.addUpdateListener(animation -> {
            currentValue = (float) animation.getAnimatedValue();
            invalidate();
        });
        
        numberAnimator.start();
    }

    /**
     * Set statistics data
     * @param title The title text
     * @param value The value text
     * @param percentage The percentage change text
     */
    public void setStatisticsData(String title, String value, String percentage) {
        this.title = title;
        this.targetValue = value;
        this.percentage = percentage;
        
        parseTargetValue();
        
        // Update percentage color based on positive/negative
        if (percentage != null) {
            if (percentage.startsWith("+")) {
                percentageColor = ContextCompat.getColor(getContext(), R.color.success_green);
            } else if (percentage.startsWith("-")) {
                percentageColor = ContextCompat.getColor(getContext(), R.color.error_red);
            } else {
                percentageColor = ContextCompat.getColor(getContext(), R.color.medium_gray);
            }
            percentagePaint.setColor(percentageColor);
        }
        
        invalidate();
        
        if (animateNumbers) {
            startAnimation();
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
     * Set icon drawable
     * @param icon The icon drawable
     */
    public void setIcon(Drawable icon) {
        this.icon = icon;
        invalidate();
    }

    /**
     * Set whether to animate numbers
     * @param animate Whether to animate number counting
     */
    public void setAnimateNumbers(boolean animate) {
        this.animateNumbers = animate;
    }

    /**
     * Set only the value (for dashboard updates)
     * @param value The new value
     */
    public void setValue(String value) {
        this.targetValue = value;
        parseTargetValue();
        invalidate();
        
        if (animateNumbers) {
            startAnimation();
        }
    }

    /**
     * Set only the percentage (for dashboard updates)
     * @param percentage The new percentage
     */
    public void setPercentage(String percentage) {
        this.percentage = percentage;
        
        // Update percentage color based on positive/negative
        if (percentage != null) {
            if (percentage.startsWith("+")) {
                percentageColor = ContextCompat.getColor(getContext(), R.color.success_green);
            } else if (percentage.startsWith("-")) {
                percentageColor = ContextCompat.getColor(getContext(), R.color.error_red);
            } else {
                percentageColor = ContextCompat.getColor(getContext(), R.color.medium_gray);
            }
            percentagePaint.setColor(percentageColor);
        }
        
        invalidate();
    }

    /**
     * Animate click effect
     */
    public void animateClick() {
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(1.0f, 0.95f, 1.0f);
        scaleAnimator.setDuration(150);
        scaleAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            setScaleX(scale);
            setScaleY(scale);
        });
        scaleAnimator.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (animateNumbers) {
            startAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (numberAnimator != null) {
            numberAnimator.cancel();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Set minimum size
        int minWidth = (int) (200 * getResources().getDisplayMetrics().density);
        int minHeight = (int) (120 * getResources().getDisplayMetrics().density);
        
        int width = Math.max(minWidth, MeasureSpec.getSize(widthMeasureSpec));
        int height = Math.max(minHeight, MeasureSpec.getSize(heightMeasureSpec));
        
        setMeasuredDimension(width, height);
    }
}