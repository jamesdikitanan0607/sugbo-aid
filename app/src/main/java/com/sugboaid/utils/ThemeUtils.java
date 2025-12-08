package com.sugboaid.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Comprehensive utility class for managing app themes and dark mode functionality
 * Supports manual theme switching, system theme detection, and theme persistence
 */
public class ThemeUtils {

    // Theme mode constants
    public static final int THEME_MODE_SYSTEM = 0;
    public static final int THEME_MODE_LIGHT = 1;
    public static final int THEME_MODE_DARK = 2;

    /**
     * Apply theme based on dark mode preference
     * @param context The context (usually Activity)
     * @param isDarkMode Whether dark mode should be enabled
     */
    public static void applyTheme(Context context, boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Apply theme based on theme mode (system, light, or dark)
     * @param context The context
     * @param themeMode The theme mode to apply
     */
    public static void applyThemeMode(Context context, int themeMode) {
        switch (themeMode) {
            case THEME_MODE_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case THEME_MODE_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_MODE_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    /**
     * Get current theme mode
     * @return Current night mode setting
     */
    public static int getCurrentNightMode() {
        return AppCompatDelegate.getDefaultNightMode();
    }

    /**
     * Check if dark mode is currently active
     * @param context The context to check
     * @return true if dark mode is active
     */
    public static boolean isDarkModeActive(Context context) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & 
                           Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Check if system is in dark mode (Android 10+)
     * @param context The context to check
     * @return true if system is in dark mode
     */
    public static boolean isSystemInDarkMode(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int nightModeFlags = context.getResources().getConfiguration().uiMode & 
                               Configuration.UI_MODE_NIGHT_MASK;
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        }
        return false;
    }

    /**
     * Toggle between light and dark themes
     * @param context The context
     * @param prefsHelper SharedPreferences helper to save preference
     */
    public static void toggleTheme(Context context, SharedPreferencesHelper prefsHelper) {
        boolean currentDarkMode = prefsHelper.getDarkModePreference();
        boolean newDarkMode = !currentDarkMode;
        
        prefsHelper.saveDarkModePreference(newDarkMode);
        applyTheme(context, newDarkMode);
        
        // Recreate activity if it's an Activity
        if (context instanceof Activity) {
            ((Activity) context).recreate();
        }
    }

    /**
     * Initialize theme on app startup
     * @param context The application context
     * @param prefsHelper SharedPreferences helper
     */
    public static void initializeTheme(Context context, SharedPreferencesHelper prefsHelper) {
        // Apply the saved theme preference only if it differs from current mode
        boolean isDark = prefsHelper.getDarkModePreference();
        int targetMode = isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode);
        }
    }

    /**
     * Get theme mode based on current settings
     * @param prefsHelper SharedPreferences helper
     * @return Current theme mode
     */
    public static int getCurrentThemeMode(SharedPreferencesHelper prefsHelper) {
        boolean isDarkMode = prefsHelper.getDarkModePreference();
        return isDarkMode ? THEME_MODE_DARK : THEME_MODE_LIGHT;
    }

    /**
     * Set theme mode and save preference
     * @param context The context
     * @param prefsHelper SharedPreferences helper
     * @param themeMode The theme mode to set
     */
    public static void setThemeMode(Context context, SharedPreferencesHelper prefsHelper, int themeMode) {
        switch (themeMode) {
            case THEME_MODE_SYSTEM:
                // For system mode, detect current system theme and save it
                boolean systemDarkMode = isSystemInDarkMode(context);
                prefsHelper.saveDarkModePreference(systemDarkMode);
                applyThemeMode(context, THEME_MODE_SYSTEM);
                break;
            case THEME_MODE_LIGHT:
                prefsHelper.saveDarkModePreference(false);
                applyThemeMode(context, THEME_MODE_LIGHT);
                break;
            case THEME_MODE_DARK:
                prefsHelper.saveDarkModePreference(true);
                applyThemeMode(context, THEME_MODE_DARK);
                break;
        }
        
        // Recreate activity if it's an Activity
        if (context instanceof Activity) {
            ((Activity) context).recreate();
        }
    }

    /**
     * Get theme mode name for display
     * @param themeMode The theme mode
     * @return Human readable theme mode name
     */
    public static String getThemeModeName(int themeMode) {
        switch (themeMode) {
            case THEME_MODE_SYSTEM:
                return "System";
            case THEME_MODE_LIGHT:
                return "Light";
            case THEME_MODE_DARK:
                return "Dark";
            default:
                return "System";
        }
    }

    /**
     * Check if device supports system theme detection
     * @return true if system theme detection is supported
     */
    public static boolean supportsSystemTheme() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    /**
     * Get appropriate icon resource for current theme
     * @param context The context
     * @return Resource ID for theme toggle icon
     */
    public static int getThemeToggleIcon(Context context) {
        if (isDarkModeActive(context)) {
            return android.R.drawable.ic_dialog_info; // Replace with actual light mode icon
        } else {
            return android.R.drawable.ic_dialog_info; // Replace with actual dark mode icon
        }
    }

    /**
     * Apply theme without recreating activity (for fragments)
     * @param context The context
     * @param isDarkMode Whether dark mode should be enabled
     */
    public static void applyThemeWithoutRecreate(Context context, boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}