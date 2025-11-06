package com.sugboaid.utils;

import android.graphics.Color;
import androidx.annotation.ColorInt;

/**
 * Utility class for ensuring proper color contrast for accessibility
 * Implements WCAG 2.1 guidelines for color contrast ratios
 */
public class ColorContrastUtils {
    
    // WCAG 2.1 contrast ratio thresholds
    private static final double MIN_CONTRAST_NORMAL = 4.5;
    private static final double MIN_CONTRAST_LARGE = 3.0;
    private static final double MIN_CONTRAST_AA_NORMAL = 4.5;
    private static final double MIN_CONTRAST_AA_LARGE = 3.0;
    private static final double MIN_CONTRAST_AAA_NORMAL = 7.0;
    private static final double MIN_CONTRAST_AAA_LARGE = 4.5;
    
    /**
     * Calculate the contrast ratio between two colors
     * @param foreground The foreground color
     * @param background The background color
     * @return The contrast ratio (1.0 to 21.0)
     */
    public static double calculateContrastRatio(@ColorInt int foreground, @ColorInt int background) {
        double foregroundLuminance = calculateLuminance(foreground);
        double backgroundLuminance = calculateLuminance(background);
        
        double lighter = Math.max(foregroundLuminance, backgroundLuminance);
        double darker = Math.min(foregroundLuminance, backgroundLuminance);
        
        return (lighter + 0.05) / (darker + 0.05);
    }
    
    /**
     * Calculate the relative luminance of a color
     * @param color The color to calculate luminance for
     * @return The relative luminance (0.0 to 1.0)
     */
    private static double calculateLuminance(@ColorInt int color) {
        double red = Color.red(color) / 255.0;
        double green = Color.green(color) / 255.0;
        double blue = Color.blue(color) / 255.0;
        
        // Apply gamma correction
        red = (red <= 0.03928) ? red / 12.92 : Math.pow((red + 0.055) / 1.055, 2.4);
        green = (green <= 0.03928) ? green / 12.92 : Math.pow((green + 0.055) / 1.055, 2.4);
        blue = (blue <= 0.03928) ? blue / 12.92 : Math.pow((blue + 0.055) / 1.055, 2.4);
        
        // Calculate luminance using ITU-R BT.709 coefficients
        return 0.2126 * red + 0.7152 * green + 0.0722 * blue;
    }
    
    /**
     * Check if color combination meets WCAG AA standards for normal text
     */
    public static boolean meetsWCAG_AA_Normal(@ColorInt int foreground, @ColorInt int background) {
        return calculateContrastRatio(foreground, background) >= MIN_CONTRAST_AA_NORMAL;
    }
    
    /**
     * Check if color combination meets WCAG AA standards for large text
     */
    public static boolean meetsWCAG_AA_Large(@ColorInt int foreground, @ColorInt int background) {
        return calculateContrastRatio(foreground, background) >= MIN_CONTRAST_AA_LARGE;
    }
    
    /**
     * Check if color combination meets WCAG AAA standards for normal text
     */
    public static boolean meetsWCAG_AAA_Normal(@ColorInt int foreground, @ColorInt int background) {
        return calculateContrastRatio(foreground, background) >= MIN_CONTRAST_AAA_NORMAL;
    }
    
    /**
     * Check if color combination meets WCAG AAA standards for large text
     */
    public static boolean meetsWCAG_AAA_Large(@ColorInt int foreground, @ColorInt int background) {
        return calculateContrastRatio(foreground, background) >= MIN_CONTRAST_AAA_LARGE;
    }
    
    /**
     * Get a contrasting color that meets accessibility standards
     * @param backgroundColor The background color
     * @param preferDark Whether to prefer dark text over light text
     * @return A color that provides sufficient contrast
     */
    @ColorInt
    public static int getAccessibleTextColor(@ColorInt int backgroundColor, boolean preferDark) {
        int darkText = Color.BLACK;
        int lightText = Color.WHITE;
        
        double darkContrast = calculateContrastRatio(darkText, backgroundColor);
        double lightContrast = calculateContrastRatio(lightText, backgroundColor);
        
        // Check if preferred option meets standards
        if (preferDark && darkContrast >= MIN_CONTRAST_AA_NORMAL) {
            return darkText;
        } else if (!preferDark && lightContrast >= MIN_CONTRAST_AA_NORMAL) {
            return lightText;
        }
        
        // Return the option with better contrast
        return darkContrast > lightContrast ? darkText : lightText;
    }
    
    /**
     * Adjust color brightness to meet contrast requirements
     * @param color The color to adjust
     * @param backgroundColor The background color to contrast against
     * @param targetRatio The target contrast ratio
     * @return The adjusted color
     */
    @ColorInt
    public static int adjustColorForContrast(@ColorInt int color, @ColorInt int backgroundColor, double targetRatio) {
        double currentRatio = calculateContrastRatio(color, backgroundColor);
        
        if (currentRatio >= targetRatio) {
            return color; // Already meets requirements
        }
        
        // Try making the color darker or lighter
        int adjustedColor = color;
        double bestRatio = currentRatio;
        
        // Try darkening
        for (float factor = 0.9f; factor > 0.1f; factor -= 0.1f) {
            int darkerColor = darkenColor(color, factor);
            double ratio = calculateContrastRatio(darkerColor, backgroundColor);
            if (ratio >= targetRatio) {
                return darkerColor;
            }
            if (ratio > bestRatio) {
                bestRatio = ratio;
                adjustedColor = darkerColor;
            }
        }
        
        // Try lightening
        for (float factor = 1.1f; factor < 2.0f; factor += 0.1f) {
            int lighterColor = lightenColor(color, factor);
            double ratio = calculateContrastRatio(lighterColor, backgroundColor);
            if (ratio >= targetRatio) {
                return lighterColor;
            }
            if (ratio > bestRatio) {
                bestRatio = ratio;
                adjustedColor = lighterColor;
            }
        }
        
        return adjustedColor;
    }
    
    /**
     * Darken a color by a given factor
     */
    @ColorInt
    private static int darkenColor(@ColorInt int color, float factor) {
        int red = (int) (Color.red(color) * factor);
        int green = (int) (Color.green(color) * factor);
        int blue = (int) (Color.blue(color) * factor);
        
        return Color.rgb(
            Math.max(0, Math.min(255, red)),
            Math.max(0, Math.min(255, green)),
            Math.max(0, Math.min(255, blue))
        );
    }
    
    /**
     * Lighten a color by a given factor
     */
    @ColorInt
    private static int lightenColor(@ColorInt int color, float factor) {
        int red = (int) (Color.red(color) + (255 - Color.red(color)) * (factor - 1));
        int green = (int) (Color.green(color) + (255 - Color.green(color)) * (factor - 1));
        int blue = (int) (Color.blue(color) + (255 - Color.blue(color)) * (factor - 1));
        
        return Color.rgb(
            Math.max(0, Math.min(255, red)),
            Math.max(0, Math.min(255, green)),
            Math.max(0, Math.min(255, blue))
        );
    }
    
    /**
     * Get contrast ratio description for debugging
     */
    public static String getContrastDescription(double ratio) {
        if (ratio >= MIN_CONTRAST_AAA_NORMAL) {
            return "AAA (Excellent)";
        } else if (ratio >= MIN_CONTRAST_AA_NORMAL) {
            return "AA (Good)";
        } else if (ratio >= MIN_CONTRAST_AA_LARGE) {
            return "AA Large Text Only";
        } else {
            return "Insufficient Contrast";
        }
    }
    
    /**
     * Validate all color combinations in the app
     * This method can be used during development to check accessibility
     */
    public static void validateAppColors(android.content.Context context) {
        // This would typically be called in debug builds to validate color combinations
        // Implementation would check all color resources against each other
        android.util.Log.d("ColorContrast", "Validating app color combinations...");
        
        // Example validation (would be expanded for all app colors)
        int primaryBlue = androidx.core.content.ContextCompat.getColor(context, com.sugboaid.donation.R.color.primary_blue);
        int white = Color.WHITE;
        int black = Color.BLACK;
        
        double blueWhiteRatio = calculateContrastRatio(white, primaryBlue);
        double blueBlackRatio = calculateContrastRatio(black, primaryBlue);
        
        android.util.Log.d("ColorContrast", "Primary Blue vs White: " + blueWhiteRatio + " (" + getContrastDescription(blueWhiteRatio) + ")");
        android.util.Log.d("ColorContrast", "Primary Blue vs Black: " + blueBlackRatio + " (" + getContrastDescription(blueBlackRatio) + ")");
    }
}