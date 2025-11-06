package com.sugboaid.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.sugboaid.donation.R;
import com.sugboaid.utils.ValidationUtils;

/**
 * Custom view for displaying password strength indicator
 * Shows visual feedback for password strength with color-coded bars
 */
public class PasswordStrengthIndicator extends View {

    private static final int BAR_COUNT = 4;
    private static final float BAR_HEIGHT_DP = 4f;
    private static final float BAR_SPACING_DP = 4f;
    private static final float BAR_CORNER_RADIUS_DP = 2f;

    private Paint paint;
    private RectF rectF;
    private ValidationUtils.PasswordStrength.Level currentLevel;
    
    // Colors for different strength levels
    private int colorWeak;
    private int colorMedium;
    private int colorStrong;
    private int colorVeryStrong;
    private int colorInactive;

    // Dimensions
    private float barHeight;
    private float barSpacing;
    private float barCornerRadius;

    public PasswordStrengthIndicator(Context context) {
        super(context);
        init(context, null);
    }

    public PasswordStrengthIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PasswordStrengthIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Initialize paint
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();
        
        // Convert dp to pixels
        float density = context.getResources().getDisplayMetrics().density;
        barHeight = BAR_HEIGHT_DP * density;
        barSpacing = BAR_SPACING_DP * density;
        barCornerRadius = BAR_CORNER_RADIUS_DP * density;
        
        // Initialize colors
        initColors(context);
        
        // Set initial state
        currentLevel = ValidationUtils.PasswordStrength.Level.NONE;
        
        // Handle custom attributes if provided
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PasswordStrengthIndicator);
            try {
                // You can add custom attributes here if needed
            } finally {
                a.recycle();
            }
        }
        
        // Set content description for accessibility
        setContentDescription("Password strength indicator");
    }

    private void initColors(Context context) {
        colorWeak = ContextCompat.getColor(context, R.color.password_strength_weak);
        colorMedium = ContextCompat.getColor(context, R.color.password_strength_medium);
        colorStrong = ContextCompat.getColor(context, R.color.password_strength_strong);
        colorVeryStrong = ContextCompat.getColor(context, R.color.password_strength_very_strong);
        colorInactive = ContextCompat.getColor(context, R.color.password_strength_inactive);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) barHeight;
        
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (getWidth() <= 0) return;
        
        float barWidth = (getWidth() - (barSpacing * (BAR_COUNT - 1))) / BAR_COUNT;
        
        for (int i = 0; i < BAR_COUNT; i++) {
            float left = i * (barWidth + barSpacing);
            float top = 0;
            float right = left + barWidth;
            float bottom = barHeight;
            
            rectF.set(left, top, right, bottom);
            
            // Determine bar color based on strength level and position
            paint.setColor(getBarColor(i));
            
            canvas.drawRoundRect(rectF, barCornerRadius, barCornerRadius, paint);
        }
    }

    private int getBarColor(int barIndex) {
        int activeBarCount = getActiveBarCount();
        
        if (barIndex >= activeBarCount) {
            return colorInactive;
        }
        
        switch (currentLevel) {
            case WEAK:
                return colorWeak;
            case MEDIUM:
                return colorMedium;
            case STRONG:
                return colorStrong;
            case VERY_STRONG:
                return colorVeryStrong;
            default:
                return colorInactive;
        }
    }

    private int getActiveBarCount() {
        switch (currentLevel) {
            case WEAK:
                return 1;
            case MEDIUM:
                return 2;
            case STRONG:
                return 3;
            case VERY_STRONG:
                return 4;
            default:
                return 0;
        }
    }

    /**
     * Update the password strength level
     * @param strength PasswordStrength object containing level and feedback
     */
    public void setPasswordStrength(ValidationUtils.PasswordStrength strength) {
        if (strength == null) {
            currentLevel = ValidationUtils.PasswordStrength.Level.NONE;
        } else {
            currentLevel = strength.getLevel();
        }
        
        // Update content description for accessibility
        updateContentDescription();
        
        // Trigger redraw
        invalidate();
    }

    /**
     * Update content description for accessibility
     */
    private void updateContentDescription() {
        String description;
        switch (currentLevel) {
            case WEAK:
                description = "Password strength: Weak";
                break;
            case MEDIUM:
                description = "Password strength: Medium";
                break;
            case STRONG:
                description = "Password strength: Strong";
                break;
            case VERY_STRONG:
                description = "Password strength: Very Strong";
                break;
            default:
                description = "Password strength: None";
                break;
        }
        setContentDescription(description);
    }

    /**
     * Get current password strength level
     * @return Current PasswordStrength.Level
     */
    public ValidationUtils.PasswordStrength.Level getCurrentLevel() {
        return currentLevel;
    }
}