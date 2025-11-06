package com.sugboaid.donation.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.sugboaid.donation.repositories.NotificationRepository;
import com.sugboaid.utils.SharedPreferencesHelper;

/**
 * BroadcastReceiver for handling notification actions like "Mark as Read"
 */
public class NotificationActionReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if ("MARK_AS_READ".equals(action)) {
            int notificationId = intent.getIntExtra("notification_id", -1);
            if (notificationId != -1) {
                markNotificationAsRead(context, notificationId);
            }
        }
    }
    
    private void markNotificationAsRead(Context context, int notificationId) {
        try {
            SharedPreferencesHelper prefsHelper = SharedPreferencesHelper.getInstance(context);
            NotificationRepository repository = NotificationRepository.getInstance(prefsHelper, context);
            repository.markAsRead(notificationId);
            
            // Cancel the system notification
            AndroidNotificationManager androidNotificationManager = new AndroidNotificationManager(context);
            androidNotificationManager.cancelNotification(notificationId);
            
        } catch (Exception e) {
            // Handle error silently
        }
    }
}