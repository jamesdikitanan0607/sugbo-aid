package com.sugboaid.app.util;

import android.content.Context;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;
import com.sugboaid.app.data.SharedPrefHelper;

public class ThemeUtils {
    private static SharedPrefHelper prefHelper;

    public static void initialize(Context context) {
        prefHelper = new SharedPrefHelper(context);
    }

    public static void applyTheme(Context context) {
        if (prefHelper == null) {
            initialize(context);
        }
        
        String themeMode = prefHelper.getString(Constants.PREF_THEME_MODE, Constants.THEME_SYSTEM);
        
        switch (themeMode) {
            case Constants.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case Constants.THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case Constants.THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static void setThemeMode(Context context, String themeMode) {
        if (prefHelper == null) {
            initialize(context);
        }
        
        prefHelper.saveString(Constants.PREF_THEME_MODE, themeMode);
        applyTheme(context);
    }

    public static String getCurrentThemeMode(Context context) {
        if (prefHelper == null) {
            initialize(context);
        }
        
        return prefHelper.getString(Constants.PREF_THEME_MODE, Constants.THEME_SYSTEM);
    }

    public static boolean isDarkMode(Context context) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static int pxToDp(Context context, int px) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(px / density);
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
}