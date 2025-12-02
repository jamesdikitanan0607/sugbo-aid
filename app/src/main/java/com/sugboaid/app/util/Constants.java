package com.sugboaid.app.util;

public class Constants {
    // User Roles
    public static final String ROLE_DONOR = "DONOR";
    public static final String ROLE_ORGANIZATION = "ORGANIZATION";
    public static final String ROLE_VOLUNTEER = "VOLUNTEER";
    public static final String ROLE_RECIPIENT = "RECIPIENT";
    public static final String ROLE_GUEST = "GUEST";

    // Donation Types
    public static final String DONATION_TYPE_CASH = "CASH";
    public static final String DONATION_TYPE_GOODS = "GOODS";
    public static final String DONATION_TYPE_SERVICES = "SERVICES";

    // Donation Categories
    public static final String CATEGORY_FOOD = "FOOD";
    public static final String CATEGORY_CLOTHING = "CLOTHING";
    public static final String CATEGORY_MEDICAL = "MEDICAL";
    public static final String CATEGORY_EDUCATION = "EDUCATION";
    public static final String CATEGORY_SHELTER = "SHELTER";
    public static final String CATEGORY_EMERGENCY = "EMERGENCY";
    public static final String CATEGORY_OTHER = "OTHER";

    // Donation Status
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_DISTRIBUTED = "DISTRIBUTED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Item Conditions
    public static final String CONDITION_NEW = "NEW";
    public static final String CONDITION_GOOD = "GOOD";
    public static final String CONDITION_FAIR = "FAIR";
    public static final String CONDITION_POOR = "POOR";

    // Features
    public static final String FEATURE_DONATION_POS = "donation_pos";
    public static final String FEATURE_INVENTORY_MANAGEMENT = "inventory_management";
    public static final String FEATURE_ANALYTICS_DASHBOARD = "analytics_dashboard";
    public static final String FEATURE_TRANSPARENCY_VIEW = "transparency_view";
    public static final String FEATURE_DONATION_HISTORY = "donation_history";

    // SharedPreferences Keys
    public static final String PREF_THEME_MODE = "theme_mode";
    public static final String PREF_LANGUAGE = "language";
    public static final String PREF_NOTIFICATIONS = "notifications_enabled";
    public static final String PREF_FIRST_LAUNCH = "first_launch";

    // Theme Modes
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";

    // Request Codes
    public static final int REQUEST_CODE_QR_SCAN = 1001;
    public static final int REQUEST_CODE_CAMERA_PERMISSION = 1002;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 1003;

    // Animation Durations
    public static final int ANIMATION_DURATION_SHORT = 200;
    public static final int ANIMATION_DURATION_MEDIUM = 300;
    public static final int ANIMATION_DURATION_LONG = 500;

    // UI Constants
    public static final int MIN_TOUCH_TARGET_SIZE = 44; // dp
    public static final int TABLET_MIN_WIDTH = 600; // dp
    public static final int CARD_ELEVATION = 8; // dp
    public static final int CORNER_RADIUS = 12; // dp

    // Validation Constants
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_DONATION_AMOUNT = 1000000;
    public static final int MAX_INVENTORY_STOCK = 99999;

    // Date Formats
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";
    public static final String DATE_FORMAT_FULL = "MMMM dd, yyyy 'at' hh:mm a";
    public static final String DATE_FORMAT_SHORT = "MM/dd/yy";

    // Currency
    public static final String DEFAULT_CURRENCY = "PHP";
    public static final String CURRENCY_SYMBOL = "₱";

    // QR Code
    public static final String QR_PREFIX_DONATION = "SUGBOAID_DONATION";
    public static final String QR_PREFIX_RECEIPT = "SUGBOAID_RECEIPT";
    public static final String QR_PREFIX_INVENTORY = "SUGBOAID_INVENTORY";

    // Network (for future use)
    public static final String BASE_URL = "https://api.sugboaid.org/";
    public static final int NETWORK_TIMEOUT = 30000; // 30 seconds

    // File Paths
    public static final String EXPORT_FOLDER = "SugboAid/Exports/";
    public static final String RECEIPT_FOLDER = "SugboAid/Receipts/";
    public static final String BACKUP_FOLDER = "SugboAid/Backups/";
}