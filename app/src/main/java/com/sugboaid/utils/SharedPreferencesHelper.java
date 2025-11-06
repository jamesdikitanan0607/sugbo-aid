package com.sugboaid.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sugboaid.models.Donation;
import com.sugboaid.models.InventoryItem;
import com.sugboaid.models.Transaction;
import com.sugboaid.models.AppNotification;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing SharedPreferences data storage
 * Provides comprehensive wrapper for all app data persistence
 */
public class SharedPreferencesHelper {
    private static final String PREF_NAME = "SugboAidPrefs";
    
    // Keys for different data types
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_FOLLOW_SYSTEM_THEME = "follow_system_theme";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_DONATIONS = "donations_json";
    private static final String KEY_INVENTORY_ITEMS = "inventory_items_json";
    private static final String KEY_TRANSACTIONS = "transactions_json";
    private static final String KEY_NOTIFICATIONS = "notifications_json";
    private static final String KEY_TOTAL_DONATIONS = "total_donations";
    private static final String KEY_TOTAL_FAMILIES_HELPED = "total_families_helped";
    private static final String KEY_TOTAL_DISTRIBUTED_ITEMS = "total_distributed_items";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_LAST_SYNC_TIME = "last_sync_time";
    private static final String KEY_OFFLINE_MODE = "offline_mode";
    private static final String KEY_SELECTED_CAMPAIGN = "selected_campaign";
    private static final String KEY_PREVIOUS_TOTAL_DONATIONS = "previous_total_donations";
    private static final String KEY_PREVIOUS_DISTRIBUTED_ITEMS = "previous_distributed_items";
    private static final String KEY_PREVIOUS_FAMILIES_HELPED = "previous_families_helped";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    
    private static SharedPreferencesHelper instance;

    // Private constructor for singleton pattern
    private SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }

    // Singleton instance getter
    public static synchronized SharedPreferencesHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Dark Mode Preferences
    public void saveDarkModePreference(boolean isDarkMode) {
        editor.putBoolean(KEY_DARK_MODE, isDarkMode);
        editor.apply();
    }

    public boolean getDarkModePreference() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }

    // Theme Mode Preferences (Enhanced)
    public void saveThemeMode(int themeMode) {
        editor.putInt(KEY_THEME_MODE, themeMode);
        editor.apply();
    }

    public int getThemeMode() {
        return sharedPreferences.getInt(KEY_THEME_MODE, ThemeUtils.THEME_MODE_SYSTEM);
    }

    public void saveFollowSystemTheme(boolean followSystem) {
        editor.putBoolean(KEY_FOLLOW_SYSTEM_THEME, followSystem);
        editor.apply();
    }

    public boolean getFollowSystemTheme() {
        return sharedPreferences.getBoolean(KEY_FOLLOW_SYSTEM_THEME, true);
    }

    // User Role Preferences
    public void saveUserRole(String role) {
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "Guest");
    }

    // Donation Management
    public void saveDonations(List<Donation> donations) {
        String donationsJson = gson.toJson(donations);
        editor.putString(KEY_DONATIONS, donationsJson);
        editor.apply();
    }

    public List<Donation> getDonations() {
        String donationsJson = sharedPreferences.getString(KEY_DONATIONS, "[]");
        Type listType = new TypeToken<List<Donation>>(){}.getType();
        List<Donation> donations = gson.fromJson(donationsJson, listType);
        return donations != null ? donations : new ArrayList<>();
    }

    public void addDonation(Donation donation) {
        if (donation == null || !donation.isValid()) {
            throw new IllegalArgumentException("Invalid donation data");
        }
        
        List<Donation> donations = getDonations();
        donations.add(donation);
        saveDonations(donations);
        
        // Update statistics
        updateTotalDonations();
    }

    public void removeDonation(String donationId) {
        List<Donation> donations = getDonations();
        donations.removeIf(donation -> donation.getId().equals(donationId));
        saveDonations(donations);
        updateTotalDonations();
    }

    // Inventory Management
    public void saveInventoryItems(List<InventoryItem> items) {
        String itemsJson = gson.toJson(items);
        editor.putString(KEY_INVENTORY_ITEMS, itemsJson);
        editor.apply();
    }

    public List<InventoryItem> getInventoryItems() {
        String itemsJson = sharedPreferences.getString(KEY_INVENTORY_ITEMS, "[]");
        Type listType = new TypeToken<List<InventoryItem>>(){}.getType();
        List<InventoryItem> items = gson.fromJson(itemsJson, listType);
        return items != null ? items : new ArrayList<>();
    }

    public void addInventoryItem(InventoryItem item) {
        if (item == null || !item.isValid()) {
            throw new IllegalArgumentException("Invalid inventory item data");
        }
        
        List<InventoryItem> items = getInventoryItems();
        
        // Check if item already exists and update it
        boolean updated = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getName().equals(item.getName())) {
                items.set(i, item);
                updated = true;
                break;
            }
        }
        
        if (!updated) {
            items.add(item);
        }
        
        saveInventoryItems(items);
    }

    public void removeInventoryItem(String itemName) {
        List<InventoryItem> items = getInventoryItems();
        items.removeIf(item -> item.getName().equals(itemName));
        saveInventoryItems(items);
    }

    // Transaction Management
    public void saveTransactions(List<Transaction> transactions) {
        String transactionsJson = gson.toJson(transactions);
        editor.putString(KEY_TRANSACTIONS, transactionsJson);
        editor.apply();
    }

    public List<Transaction> getTransactions() {
        String transactionsJson = sharedPreferences.getString(KEY_TRANSACTIONS, "[]");
        Type listType = new TypeToken<List<Transaction>>(){}.getType();
        List<Transaction> transactions = gson.fromJson(transactionsJson, listType);
        return transactions != null ? transactions : new ArrayList<>();
    }

    public void addTransaction(Transaction transaction) {
        if (transaction == null || !transaction.isValid()) {
            throw new IllegalArgumentException("Invalid transaction data");
        }
        
        List<Transaction> transactions = getTransactions();
        transactions.add(transaction);
        saveTransactions(transactions);
    }

    public void removeTransaction(String transactionId) {
        List<Transaction> transactions = getTransactions();
        transactions.removeIf(transaction -> transaction.getId().equals(transactionId));
        saveTransactions(transactions);
    }

    // Notification Management
    public void saveNotifications(List<AppNotification> notifications) {
        String notificationsJson = gson.toJson(notifications);
        editor.putString(KEY_NOTIFICATIONS, notificationsJson);
        editor.apply();
    }

    public List<AppNotification> getNotifications() {
        String notificationsJson = sharedPreferences.getString(KEY_NOTIFICATIONS, "[]");
        Type listType = new TypeToken<List<AppNotification>>(){}.getType();
        List<AppNotification> notifications = gson.fromJson(notificationsJson, listType);
        return notifications != null ? notifications : new ArrayList<>();
    }

    public void addNotification(AppNotification notification) {
        if (notification == null || !notification.isValid()) {
            throw new IllegalArgumentException("Invalid notification data");
        }
        
        List<AppNotification> notifications = getNotifications();
        notifications.add(0, notification); // Add to beginning for newest first
        saveNotifications(notifications);
    }

    public void markNotificationAsRead(int notificationId) {
        List<AppNotification> notifications = getNotifications();
        for (AppNotification notification : notifications) {
            if (notification.getId() == notificationId) {
                notification.markAsRead();
                break;
            }
        }
        saveNotifications(notifications);
    }

    public void markAllNotificationsAsRead() {
        List<AppNotification> notifications = getNotifications();
        for (AppNotification notification : notifications) {
            notification.markAsRead();
        }
        saveNotifications(notifications);
    }

    public int getUnreadNotificationCount() {
        List<AppNotification> notifications = getNotifications();
        int count = 0;
        for (AppNotification notification : notifications) {
            if (!notification.isRead()) {
                count++;
            }
        }
        return count;
    }

    // Statistics Management
    public void saveTotalDonations(double total) {
        editor.putFloat(KEY_TOTAL_DONATIONS, (float) total);
        editor.apply();
    }

    public double getTotalDonations() {
        return sharedPreferences.getFloat(KEY_TOTAL_DONATIONS, 0.0f);
    }

    public void saveTotalFamiliesHelped(int total) {
        editor.putInt(KEY_TOTAL_FAMILIES_HELPED, total);
        editor.apply();
    }

    public int getTotalFamiliesHelped() {
        return sharedPreferences.getInt(KEY_TOTAL_FAMILIES_HELPED, 0);
    }

    public void saveTotalDistributedItems(int total) {
        editor.putInt(KEY_TOTAL_DISTRIBUTED_ITEMS, total);
        editor.apply();
    }

    public int getTotalDistributedItems() {
        return sharedPreferences.getInt(KEY_TOTAL_DISTRIBUTED_ITEMS, 0);
    }

    // App State Management
    public void saveFirstLaunch(boolean isFirstLaunch) {
        editor.putBoolean(KEY_FIRST_LAUNCH, isFirstLaunch);
        editor.apply();
    }

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void saveLastSyncTime(long timestamp) {
        editor.putLong(KEY_LAST_SYNC_TIME, timestamp);
        editor.apply();
    }

    public long getLastSyncTime() {
        return sharedPreferences.getLong(KEY_LAST_SYNC_TIME, 0);
    }

    public void saveOfflineMode(boolean isOffline) {
        editor.putBoolean(KEY_OFFLINE_MODE, isOffline);
        editor.apply();
    }

    public boolean isOfflineMode() {
        return sharedPreferences.getBoolean(KEY_OFFLINE_MODE, false);
    }

    public void saveSelectedCampaign(String campaign) {
        editor.putString(KEY_SELECTED_CAMPAIGN, campaign);
        editor.apply();
    }

    public String getSelectedCampaign() {
        return sharedPreferences.getString(KEY_SELECTED_CAMPAIGN, "General Relief");
    }

    // Previous statistics for percentage calculations
    public void savePreviousTotalDonations(double total) {
        editor.putFloat(KEY_PREVIOUS_TOTAL_DONATIONS, (float) total);
        editor.apply();
    }

    public double getPreviousTotalDonations() {
        return sharedPreferences.getFloat(KEY_PREVIOUS_TOTAL_DONATIONS, 0.0f);
    }

    public void savePreviousDistributedItems(int total) {
        editor.putInt(KEY_PREVIOUS_DISTRIBUTED_ITEMS, total);
        editor.apply();
    }

    public int getPreviousDistributedItems() {
        return sharedPreferences.getInt(KEY_PREVIOUS_DISTRIBUTED_ITEMS, 0);
    }

    public void savePreviousFamiliesHelped(int total) {
        editor.putInt(KEY_PREVIOUS_FAMILIES_HELPED, total);
        editor.apply();
    }

    public int getPreviousFamiliesHelped() {
        return sharedPreferences.getInt(KEY_PREVIOUS_FAMILIES_HELPED, 0);
    }

    // Utility Methods
    private void updateTotalDonations() {
        List<Donation> donations = getDonations();
        double total = 0.0;
        for (Donation donation : donations) {
            if (donation.getType().getValue().equals("cash")) {
                total += donation.getAmount();
            }
        }
        saveTotalDonations(total);
    }

    public void clearAllData() {
        editor.clear();
        editor.apply();
    }

    public void clearDonations() {
        editor.remove(KEY_DONATIONS);
        editor.remove(KEY_TOTAL_DONATIONS);
        editor.apply();
    }

    public void clearInventory() {
        editor.remove(KEY_INVENTORY_ITEMS);
        editor.apply();
    }

    public void clearTransactions() {
        editor.remove(KEY_TRANSACTIONS);
        editor.apply();
    }

    public void clearNotifications() {
        editor.remove(KEY_NOTIFICATIONS);
        editor.apply();
    }

    // Export/Import functionality for backup
    public String exportAllData() {
        return gson.toJson(sharedPreferences.getAll());
    }

    public boolean importData(String jsonData) {
        try {
            // This would need proper implementation based on requirements
            // For now, just return true to indicate the method exists
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Get SharedPreferences instance for direct access
    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    // Data validation and integrity checks
    public boolean validateDataIntegrity() {
        try {
            // Validate donations
            List<Donation> donations = getDonations();
            for (Donation donation : donations) {
                if (!donation.isValid()) {
                    return false;
                }
            }
            
            // Validate inventory items
            List<InventoryItem> items = getInventoryItems();
            for (InventoryItem item : items) {
                if (!item.isValid()) {
                    return false;
                }
            }
            
            // Validate transactions
            List<Transaction> transactions = getTransactions();
            for (Transaction transaction : transactions) {
                if (!transaction.isValid()) {
                    return false;
                }
            }
            
            // Validate notifications
            List<AppNotification> notifications = getNotifications();
            for (AppNotification notification : notifications) {
                if (!notification.isValid()) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}