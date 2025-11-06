package com.sugboaid.donation.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sugboaid.donation.utils.AndroidNotificationManager;
import com.sugboaid.models.AppNotification;
import com.sugboaid.models.NotificationType;
import com.sugboaid.utils.SharedPreferencesHelper;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Repository for managing notification data using SharedPreferences
 */
public class NotificationRepository {
    private static final String NOTIFICATIONS_KEY = "notifications_list";
    private static NotificationRepository instance;
    
    private SharedPreferencesHelper prefsHelper;
    private MutableLiveData<List<AppNotification>> notificationsLiveData;
    private MutableLiveData<Integer> unreadCountLiveData;
    private Gson gson;
    private AndroidNotificationManager androidNotificationManager;
    
    private NotificationRepository(SharedPreferencesHelper prefsHelper, android.content.Context context) {
        this.prefsHelper = prefsHelper;
        this.gson = new Gson();
        this.notificationsLiveData = new MutableLiveData<>();
        this.unreadCountLiveData = new MutableLiveData<>();
        
        // Initialize Android notification manager
        this.androidNotificationManager = new AndroidNotificationManager(context);
        
        // Load initial data
        loadNotifications();
    }
    
    public static synchronized NotificationRepository getInstance(SharedPreferencesHelper prefsHelper, 
                                                                   android.content.Context context) {
        if (instance == null) {
            instance = new NotificationRepository(prefsHelper, context);
        }
        return instance;
    }
    
    public LiveData<List<AppNotification>> getNotifications() {
        return notificationsLiveData;
    }
    
    public LiveData<Integer> getUnreadCount() {
        return unreadCountLiveData;
    }
    
    public void addNotification(AppNotification notification) {
        if (notification == null || !notification.isValid()) {
            return;
        }
        
        List<AppNotification> currentNotifications = getCurrentNotifications();
        currentNotifications.add(0, notification); // Add to top
        
        saveNotifications(currentNotifications);
        updateLiveData(currentNotifications);
        
        // Show system notification
        if (androidNotificationManager != null) {
            androidNotificationManager.showNotification(notification);
        }
    }
    
    public void markAsRead(int notificationId) {
        List<AppNotification> notifications = getCurrentNotifications();
        boolean updated = false;
        
        for (AppNotification notification : notifications) {
            if (notification.getId() == notificationId) {
                notification.markAsRead();
                updated = true;
                break;
            }
        }
        
        if (updated) {
            saveNotifications(notifications);
            updateLiveData(notifications);
        }
    }
    
    public void markAllAsRead() {
        List<AppNotification> notifications = getCurrentNotifications();
        boolean hasUnread = false;
        
        for (AppNotification notification : notifications) {
            if (!notification.isRead()) {
                notification.markAsRead();
                hasUnread = true;
            }
        }
        
        if (hasUnread) {
            saveNotifications(notifications);
            updateLiveData(notifications);
        }
    }
    
    public void removeNotification(int notificationId) {
        List<AppNotification> notifications = getCurrentNotifications();
        boolean removed = notifications.removeIf(n -> n.getId() == notificationId);
        
        if (removed) {
            saveNotifications(notifications);
            updateLiveData(notifications);
        }
    }
    
    public void clearAllNotifications() {
        saveNotifications(new ArrayList<>());
        updateLiveData(new ArrayList<>());
    }
    
    public List<AppNotification> getNotificationsByType(NotificationType type) {
        List<AppNotification> allNotifications = getCurrentNotifications();
        List<AppNotification> filteredNotifications = new ArrayList<>();
        
        for (AppNotification notification : allNotifications) {
            if (notification.getType() == type) {
                filteredNotifications.add(notification);
            }
        }
        
        return filteredNotifications;
    }
    
    public int getUnreadCountByType(NotificationType type) {
        List<AppNotification> notifications = getNotificationsByType(type);
        int count = 0;
        
        for (AppNotification notification : notifications) {
            if (!notification.isRead()) {
                count++;
            }
        }
        
        return count;
    }
    
    // Convenience methods for creating common notifications
    public void addDonationNotification(String donorName, String amount) {
        AppNotification notification = AppNotification.createDonationNotification(donorName, amount);
        addNotification(notification);
    }
    
    public void addLowInventoryNotification(String itemName, int stock) {
        AppNotification notification = AppNotification.createLowInventoryNotification(itemName, stock);
        addNotification(notification);
    }
    
    public void addDistributionNotification(String location, int families) {
        AppNotification notification = AppNotification.createDistributionNotification(location, families);
        addNotification(notification);
    }
    
    public void addSystemUpdateNotification(String title, String message) {
        AppNotification notification = new AppNotification(NotificationType.SYSTEM_UPDATE, title, message);
        addNotification(notification);
    }
    
    public void addAlertNotification(String title, String message) {
        AppNotification notification = new AppNotification(NotificationType.ALERT, title, message);
        addNotification(notification);
    }
    
    public void addInfoNotification(String title, String message) {
        AppNotification notification = new AppNotification(NotificationType.INFO, title, message);
        addNotification(notification);
    }
    
    // Private helper methods
    private List<AppNotification> getCurrentNotifications() {
        List<AppNotification> notifications = notificationsLiveData.getValue();
        return notifications != null ? new ArrayList<>(notifications) : new ArrayList<>();
    }
    
    private void loadNotifications() {
        try {
            List<AppNotification> notifications = prefsHelper.getNotifications();
            
            // Sort by timestamp (newest first)
            Collections.sort(notifications, new Comparator<AppNotification>() {
                @Override
                public int compare(AppNotification n1, AppNotification n2) {
                    return Long.compare(n2.getTimestamp(), n1.getTimestamp());
                }
            });
            
            updateLiveData(notifications);
            
        } catch (Exception e) {
            // If there's an error loading notifications, start with empty list
            updateLiveData(new ArrayList<>());
        }
    }
    
    private void saveNotifications(List<AppNotification> notifications) {
        try {
            prefsHelper.saveNotifications(notifications);
        } catch (Exception e) {
            // Handle save error silently
        }
    }
    
    private void updateLiveData(List<AppNotification> notifications) {
        notificationsLiveData.setValue(notifications);
        
        // Update unread count
        int unreadCount = 0;
        for (AppNotification notification : notifications) {
            if (!notification.isRead()) {
                unreadCount++;
            }
        }
        unreadCountLiveData.setValue(unreadCount);
    }
    
    // Method to generate sample notifications for testing
    public void generateSampleNotifications() {
        List<AppNotification> sampleNotifications = new ArrayList<>();
        
        // Sample donation notification
        sampleNotifications.add(AppNotification.createDonationNotification("John Doe", "₱1,000"));
        
        // Sample low inventory notification
        sampleNotifications.add(AppNotification.createLowInventoryNotification("Rice", 15));
        
        // Sample distribution notification
        sampleNotifications.add(AppNotification.createDistributionNotification("Barangay Lahug", 25));
        
        // Sample system update
        AppNotification systemUpdate = new AppNotification(NotificationType.SYSTEM_UPDATE, 
            "System Update", "New features have been added to the transparency dashboard");
        sampleNotifications.add(systemUpdate);
        
        // Sample alert
        AppNotification alert = new AppNotification(NotificationType.ALERT, 
            "Critical Stock Level", "Multiple items are critically low and need immediate restocking");
        sampleNotifications.add(alert);
        
        // Sample info
        AppNotification info = new AppNotification(NotificationType.INFO, 
            "Weekly Report", "Your weekly donation summary is now available");
        sampleNotifications.add(info);
        
        // Add timestamps with some variation
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < sampleNotifications.size(); i++) {
            sampleNotifications.get(i).setTimestamp(baseTime - (i * 3600000)); // 1 hour apart
        }
        
        saveNotifications(sampleNotifications);
        updateLiveData(sampleNotifications);
    }
}