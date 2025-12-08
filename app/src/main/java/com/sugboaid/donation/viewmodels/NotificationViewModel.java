package com.sugboaid.donation.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.sugboaid.donation.repositories.NotificationRepository;
import com.sugboaid.models.AppNotification;
import com.sugboaid.models.NotificationType;
import com.sugboaid.utils.SharedPreferencesHelper;
import java.util.List;

/**
 * ViewModel for managing notification data and UI state
 */
public class NotificationViewModel extends AndroidViewModel {
    
    private final NotificationRepository notificationRepository;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    private final MutableLiveData<Boolean> showEmptyState;
    private final LiveData<List<AppNotification>> notifications;
    private final LiveData<Integer> unreadCount;
    private final Observer<List<AppNotification>> notificationsObserver;
    
    public NotificationViewModel(@NonNull Application application) {
        super(application);
        
        SharedPreferencesHelper prefsHelper = SharedPreferencesHelper.getInstance(application);
        notificationRepository = NotificationRepository.getInstance(prefsHelper, application);
        
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        showEmptyState = new MutableLiveData<>(false);
        
        // Get LiveData from repository
        notifications = notificationRepository.getNotifications();
        unreadCount = notificationRepository.getUnreadCount();
        
        // Create a single observer for notifications
        notificationsObserver = notifications -> {
            if (notifications != null) {
                showEmptyState.setValue(notifications.isEmpty());
            }
        };
        
        // Observe notifications with the observer
        notifications.observeForever(notificationsObserver);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Remove the observer when the ViewModel is no longer used
        if (notifications != null && notificationsObserver != null) {
            notifications.removeObserver(notificationsObserver);
        }
    }
    
    // LiveData getters
    public LiveData<List<AppNotification>> getNotifications() {
        return notifications;
    }
    
    public LiveData<Integer> getUnreadCount() {
        return unreadCount;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> getShowEmptyState() {
        return showEmptyState;
    }
    
    // Notification management methods
    public void markAsRead(int notificationId) {
        notificationRepository.markAsRead(notificationId);
    }
    
    public void markAllAsRead() {
        notificationRepository.markAllAsRead();
    }
    
    public void removeNotification(int notificationId) {
        notificationRepository.removeNotification(notificationId);
    }
    
    public void clearAllNotifications() {
        notificationRepository.clearAllNotifications();
    }
    
    public void addNotification(AppNotification notification) {
        notificationRepository.addNotification(notification);
    }
    
    // Convenience methods for creating notifications
    public void addDonationNotification(String donorName, String amount) {
        notificationRepository.addDonationNotification(donorName, amount);
    }
    
    public void addLowInventoryNotification(String itemName, int stock) {
        notificationRepository.addLowInventoryNotification(itemName, stock);
    }
    
    public void addDistributionNotification(String location, int families) {
        notificationRepository.addDistributionNotification(location, families);
    }
    
    public void addSystemUpdateNotification(String title, String message) {
        notificationRepository.addSystemUpdateNotification(title, message);
    }
    
    public void addAlertNotification(String title, String message) {
        notificationRepository.addAlertNotification(title, message);
    }
    
    public void addInfoNotification(String title, String message) {
        notificationRepository.addInfoNotification(title, message);
    }
    
    // Filter methods
    public List<AppNotification> getNotificationsByType(NotificationType type) {
        return notificationRepository.getNotificationsByType(type);
    }
    
    public int getUnreadCountByType(NotificationType type) {
        return notificationRepository.getUnreadCountByType(type);
    }
    
    // UI state management
    public void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }
    
    public void setErrorMessage(String message) {
        errorMessage.setValue(message);
    }
    
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }
    
    // Utility methods
    public void refreshNotifications() {
        setLoading(true);
        // Simulate refresh delay
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            setLoading(false);
        }, 1000);
    }
    
    public boolean hasUnreadNotifications() {
        Integer count = getUnreadCount().getValue();
        return count != null && count > 0;
    }
    
    public boolean hasNotifications() {
        List<AppNotification> notifications = getNotifications().getValue();
        return notifications != null && !notifications.isEmpty();
    }
    
    // Method for testing - generates sample notifications
    public void generateSampleNotifications() {
        notificationRepository.generateSampleNotifications();
    }
    
    // Handle notification actions
    public void handleNotificationClick(AppNotification notification) {
        // Mark as read if not already read
        if (!notification.isRead()) {
            markAsRead(notification.getId());
        }
        
        // Handle action URL if present
        if (notification.getActionUrl() != null && !notification.getActionUrl().isEmpty()) {
            // This could trigger navigation or other actions
            // For now, we'll just clear any error messages
            clearErrorMessage();
        }
    }
    
    public void handleNotificationLongClick(AppNotification notification) {
        // Long click could show options menu or mark as unread
        // For now, we'll toggle read status
        if (notification.isRead()) {
            notification.markAsUnread();
        } else {
            notification.markAsRead();
        }
    }
    
    public void handleNotificationSwipe(AppNotification notification) {
        // Remove notification on swipe
        removeNotification(notification.getId());
    }
}