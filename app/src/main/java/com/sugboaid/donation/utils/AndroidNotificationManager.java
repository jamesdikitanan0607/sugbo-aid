package com.sugboaid.donation.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.sugboaid.donation.R;
import com.sugboaid.donation.activities.MainActivity;
import com.sugboaid.models.AppNotification;
import com.sugboaid.models.NotificationType;

/**
 * Manager for Android system notifications with channel support and permission handling
 */
public class AndroidNotificationManager {
    
    private static final String CHANNEL_DONATIONS = "donations_channel";
    private static final String CHANNEL_INVENTORY = "inventory_channel";
    private static final String CHANNEL_DISTRIBUTION = "distribution_channel";
    private static final String CHANNEL_SYSTEM = "system_channel";
    private static final String CHANNEL_ALERTS = "alerts_channel";
    private static final String CHANNEL_INFO = "info_channel";
    
    private static final int NOTIFICATION_ID_BASE = 1000;
    
    private Context context;
    private NotificationManagerCompat notificationManager;
    
    public AndroidNotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(this.context);
        createNotificationChannels();
    }
    
    /**
     * Creates notification channels for different types of notifications (Android 8.0+)
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager systemNotificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            // Donations Channel
            NotificationChannel donationsChannel = new NotificationChannel(
                CHANNEL_DONATIONS,
                "Donations",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            donationsChannel.setDescription("Notifications about new donations received");
            donationsChannel.enableLights(true);
            donationsChannel.setLightColor(context.getColor(R.color.success_green));
            donationsChannel.enableVibration(true);
            donationsChannel.setVibrationPattern(new long[]{100, 200, 300, 400});
            
            // Inventory Channel
            NotificationChannel inventoryChannel = new NotificationChannel(
                CHANNEL_INVENTORY,
                "Inventory Alerts",
                NotificationManager.IMPORTANCE_HIGH
            );
            inventoryChannel.setDescription("Notifications about low inventory levels");
            inventoryChannel.enableLights(true);
            inventoryChannel.setLightColor(context.getColor(R.color.warning_orange));
            inventoryChannel.enableVibration(true);
            inventoryChannel.setVibrationPattern(new long[]{0, 250, 250, 250});
            
            // Distribution Channel
            NotificationChannel distributionChannel = new NotificationChannel(
                CHANNEL_DISTRIBUTION,
                "Distribution Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            distributionChannel.setDescription("Notifications about distribution activities");
            distributionChannel.enableLights(true);
            distributionChannel.setLightColor(context.getColor(R.color.primary_blue));
            
            // System Channel
            NotificationChannel systemChannel = new NotificationChannel(
                CHANNEL_SYSTEM,
                "System Updates",
                NotificationManager.IMPORTANCE_LOW
            );
            systemChannel.setDescription("System updates and maintenance notifications");
            
            // Alerts Channel
            NotificationChannel alertsChannel = new NotificationChannel(
                CHANNEL_ALERTS,
                "Critical Alerts",
                NotificationManager.IMPORTANCE_HIGH
            );
            alertsChannel.setDescription("Critical alerts requiring immediate attention");
            alertsChannel.enableLights(true);
            alertsChannel.setLightColor(context.getColor(R.color.error_red));
            alertsChannel.enableVibration(true);
            alertsChannel.setVibrationPattern(new long[]{0, 100, 100, 100, 100, 100});
            
            // Info Channel
            NotificationChannel infoChannel = new NotificationChannel(
                CHANNEL_INFO,
                "General Information",
                NotificationManager.IMPORTANCE_LOW
            );
            infoChannel.setDescription("General information and updates");
            
            // Register channels
            systemNotificationManager.createNotificationChannel(donationsChannel);
            systemNotificationManager.createNotificationChannel(inventoryChannel);
            systemNotificationManager.createNotificationChannel(distributionChannel);
            systemNotificationManager.createNotificationChannel(systemChannel);
            systemNotificationManager.createNotificationChannel(alertsChannel);
            systemNotificationManager.createNotificationChannel(infoChannel);
        }
    }
    
    /**
     * Shows a system notification for an app notification
     */
    public void showNotification(AppNotification appNotification) {
        if (!hasNotificationPermission()) {
            return;
        }
        
        String channelId = getChannelIdForType(appNotification.getType());
        int notificationId = NOTIFICATION_ID_BASE + appNotification.getId();
        
        // Create intent for when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("open_notifications", true);
        intent.putExtra("notification_id", appNotification.getId());
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            notificationId, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
            .setSmallIcon(getNotificationIcon(appNotification.getType()))
            .setContentTitle(appNotification.getTitle())
            .setContentText(appNotification.getMessage())
            .setPriority(getNotificationPriority(appNotification.getType()))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setWhen(appNotification.getTimestamp())
            .setShowWhen(true);
        
        // Add large icon for certain types
        if (appNotification.getType() == NotificationType.DONATION_RECEIVED) {
            builder.setLargeIcon(android.graphics.BitmapFactory.decodeResource(
                context.getResources(), R.mipmap.ic_launcher));
        }
        
        // Add action buttons for certain notification types
        addNotificationActions(builder, appNotification, notificationId);
        
        // Set notification style for longer messages
        if (appNotification.getMessage().length() > 50) {
            builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(appNotification.getMessage()));
        }
        
        // Add color for Android 5.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(getNotificationColor(appNotification.getType()));
        }
        
        // Show notification
        notificationManager.notify(notificationId, builder.build());
    }
    
    /**
     * Adds action buttons to notifications based on type
     */
    private void addNotificationActions(NotificationCompat.Builder builder, 
                                      AppNotification appNotification, int notificationId) {
        switch (appNotification.getType()) {
            case DONATION_RECEIVED:
                // Add "View Details" action
                Intent viewIntent = new Intent(context, MainActivity.class);
                viewIntent.putExtra("open_reports", true);
                PendingIntent viewPendingIntent = PendingIntent.getActivity(
                    context, notificationId + 1, viewIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                builder.addAction(R.drawable.ic_visibility, "View Details", viewPendingIntent);
                break;
                
            case INVENTORY_LOW:
                // Add "Check Inventory" action
                Intent inventoryIntent = new Intent(context, MainActivity.class);
                inventoryIntent.putExtra("open_inventory", true);
                PendingIntent inventoryPendingIntent = PendingIntent.getActivity(
                    context, notificationId + 2, inventoryIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                builder.addAction(R.drawable.ic_inventory, "Check Inventory", inventoryPendingIntent);
                break;
                
            case DISTRIBUTION_COMPLETE:
                // Add "View Impact" action
                Intent impactIntent = new Intent(context, MainActivity.class);
                impactIntent.putExtra("open_transparency", true);
                PendingIntent impactPendingIntent = PendingIntent.getActivity(
                    context, notificationId + 3, impactIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                builder.addAction(R.drawable.ic_impact, "View Impact", impactPendingIntent);
                break;
        }
        
        // Add "Mark as Read" action for all notifications
        Intent markReadIntent = new Intent(context, NotificationActionReceiver.class);
        markReadIntent.setAction("MARK_AS_READ");
        markReadIntent.putExtra("notification_id", appNotification.getId());
        PendingIntent markReadPendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 10, markReadIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_mark_read, "Mark as Read", markReadPendingIntent);
    }
    
    /**
     * Gets the appropriate channel ID for a notification type
     */
    private String getChannelIdForType(NotificationType type) {
        switch (type) {
            case DONATION_RECEIVED:
                return CHANNEL_DONATIONS;
            case INVENTORY_LOW:
                return CHANNEL_INVENTORY;
            case DISTRIBUTION_COMPLETE:
                return CHANNEL_DISTRIBUTION;
            case SYSTEM_UPDATE:
                return CHANNEL_SYSTEM;
            case ALERT:
                return CHANNEL_ALERTS;
            case INFO:
            default:
                return CHANNEL_INFO;
        }
    }
    
    /**
     * Gets the appropriate notification icon for a type
     */
    private int getNotificationIcon(NotificationType type) {
        switch (type) {
            case DONATION_RECEIVED:
                return R.drawable.ic_donation;
            case INVENTORY_LOW:
                return R.drawable.ic_warning;
            case DISTRIBUTION_COMPLETE:
                return R.drawable.ic_check_circle;
            case SYSTEM_UPDATE:
                return R.drawable.ic_system_update;
            case ALERT:
                return R.drawable.ic_alert;
            case INFO:
            default:
                return R.drawable.ic_info;
        }
    }
    
    /**
     * Gets the notification priority for a type
     */
    private int getNotificationPriority(NotificationType type) {
        switch (type) {
            case ALERT:
            case INVENTORY_LOW:
                return NotificationCompat.PRIORITY_HIGH;
            case DONATION_RECEIVED:
            case DISTRIBUTION_COMPLETE:
                return NotificationCompat.PRIORITY_DEFAULT;
            case SYSTEM_UPDATE:
            case INFO:
            default:
                return NotificationCompat.PRIORITY_LOW;
        }
    }
    
    /**
     * Gets the notification color for a type
     */
    private int getNotificationColor(NotificationType type) {
        switch (type) {
            case DONATION_RECEIVED:
                return context.getColor(R.color.success_green);
            case INVENTORY_LOW:
                return context.getColor(R.color.warning_orange);
            case DISTRIBUTION_COMPLETE:
                return context.getColor(R.color.primary_blue);
            case SYSTEM_UPDATE:
                return context.getColor(R.color.light_blue);
            case ALERT:
                return context.getColor(R.color.error_red);
            case INFO:
            default:
                return context.getColor(R.color.text_secondary);
        }
    }
    
    /**
     * Checks if the app has notification permission (Android 13+)
     */
    public boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(context, 
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return notificationManager.areNotificationsEnabled();
    }
    
    /**
     * Cancels a specific notification
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(NOTIFICATION_ID_BASE + notificationId);
    }
    
    /**
     * Cancels all notifications
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
    
    /**
     * Shows a summary notification for multiple unread notifications
     */
    public void showSummaryNotification(int unreadCount) {
        if (!hasNotificationPermission() || unreadCount <= 1) {
            return;
        }
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_notifications", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_INFO)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("SugboAid Notifications")
            .setContentText(String.format("You have %d unread notifications", unreadCount))
            .setNumber(unreadCount)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup("sugboaid_notifications")
            .setGroupSummary(true);
        
        notificationManager.notify(999, builder.build());
    }
    
    /**
     * Convenience methods for creating specific notification types
     */
    public void showDonationNotification(String donorName, String amount) {
        AppNotification notification = AppNotification.createDonationNotification(donorName, amount);
        showNotification(notification);
    }
    
    public void showLowInventoryNotification(String itemName, int stock) {
        AppNotification notification = AppNotification.createLowInventoryNotification(itemName, stock);
        showNotification(notification);
    }
    
    public void showDistributionNotification(String location, int families) {
        AppNotification notification = AppNotification.createDistributionNotification(location, families);
        showNotification(notification);
    }
    
    public void showSystemUpdateNotification(String title, String message) {
        AppNotification notification = new AppNotification(NotificationType.SYSTEM_UPDATE, title, message);
        showNotification(notification);
    }
    
    public void showAlertNotification(String title, String message) {
        AppNotification notification = new AppNotification(NotificationType.ALERT, title, message);
        showNotification(notification);
    }
    
    public void showInfoNotification(String title, String message) {
        AppNotification notification = new AppNotification(NotificationType.INFO, title, message);
        showNotification(notification);
    }
}