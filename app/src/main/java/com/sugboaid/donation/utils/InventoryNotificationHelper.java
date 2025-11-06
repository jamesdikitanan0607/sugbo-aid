package com.sugboaid.donation.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.sugboaid.donation.R;
import com.sugboaid.donation.activities.MainActivity;
import com.sugboaid.models.InventoryItem;

import java.util.List;

/**
 * Helper class for managing inventory-related notifications
 */
public class InventoryNotificationHelper {
    
    private static final String CHANNEL_ID = "inventory_alerts";
    private static final String CHANNEL_NAME = "Inventory Alerts";
    private static final String CHANNEL_DESCRIPTION = "Notifications for low stock and restock alerts";
    
    private static final int NOTIFICATION_ID_LOW_STOCK = 1001;
    private static final int NOTIFICATION_ID_CRITICAL_STOCK = 1002;
    private static final int NOTIFICATION_ID_RESTOCK_REMINDER = 1003;
    
    private Context context;
    private NotificationManagerCompat notificationManager;
    
    public InventoryNotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(this.context);
        createNotificationChannel();
    }
    
    /**
     * Create notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 250, 250, 250});
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Show low stock alert notification
     */
    public void showLowStockAlert(InventoryItem item) {
        String title = "Low Stock Alert";
        String message = String.format("%s is running low (%d/%d %s remaining)", 
            item.getName(), item.getStock(), item.getCapacity(), item.getUnit());
        
        NotificationCompat.Builder builder = createBaseNotification(title, message)
            .setSmallIcon(R.drawable.ic_warning)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(context.getResources().getColor(R.color.warning_orange, null));
        
        notificationManager.notify(NOTIFICATION_ID_LOW_STOCK, builder.build());
    }
    
    /**
     * Show critical stock alert notification
     */
    public void showCriticalStockAlert(InventoryItem item) {
        String title = "Critical Stock Alert!";
        String message = String.format("%s is critically low (%d/%d %s remaining). Immediate restock needed!", 
            item.getName(), item.getStock(), item.getCapacity(), item.getUnit());
        
        NotificationCompat.Builder builder = createBaseNotification(title, message)
            .setSmallIcon(R.drawable.ic_warning)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(context.getResources().getColor(R.color.error_red, null))
            .setVibrate(new long[]{0, 250, 250, 250})
            .setAutoCancel(false); // Keep notification until user dismisses
        
        notificationManager.notify(NOTIFICATION_ID_CRITICAL_STOCK, builder.build());
    }
    
    /**
     * Show restock reminder notification
     */
    public void showRestockReminder(List<InventoryItem> lowStockItems) {
        if (lowStockItems.isEmpty()) return;
        
        String title = "Restock Reminder";
        String message;
        
        if (lowStockItems.size() == 1) {
            InventoryItem item = lowStockItems.get(0);
            message = String.format("Don't forget to restock %s (%d %s remaining)", 
                item.getName(), item.getStock(), item.getUnit());
        } else {
            message = String.format("%d items need restocking", lowStockItems.size());
        }
        
        NotificationCompat.Builder builder = createBaseNotification(title, message)
            .setSmallIcon(R.drawable.ic_inventory)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(context.getResources().getColor(R.color.primary_blue, null));
        
        // Add action buttons for quick access
        builder.addAction(createRestockAction());
        
        notificationManager.notify(NOTIFICATION_ID_RESTOCK_REMINDER, builder.build());
    }
    
    /**
     * Show multiple low stock items notification
     */
    public void showMultipleLowStockAlert(List<InventoryItem> lowStockItems) {
        if (lowStockItems.isEmpty()) return;
        
        String title = "Multiple Low Stock Items";
        String message = String.format("%d items are running low on stock", lowStockItems.size());
        
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
            .setSummaryText("Tap to view inventory");
        
        // Add up to 5 items to the expanded notification
        int itemCount = Math.min(lowStockItems.size(), 5);
        for (int i = 0; i < itemCount; i++) {
            InventoryItem item = lowStockItems.get(i);
            String line = String.format("%s: %d/%d %s", 
                item.getName(), item.getStock(), item.getCapacity(), item.getUnit());
            inboxStyle.addLine(line);
        }
        
        if (lowStockItems.size() > 5) {
            inboxStyle.addLine(String.format("... and %d more items", lowStockItems.size() - 5));
        }
        
        NotificationCompat.Builder builder = createBaseNotification(title, message)
            .setSmallIcon(R.drawable.ic_inventory)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(context.getResources().getColor(R.color.warning_orange, null))
            .setStyle(inboxStyle);
        
        notificationManager.notify(NOTIFICATION_ID_LOW_STOCK, builder.build());
    }
    
    /**
     * Create base notification builder with common settings
     */
    private NotificationCompat.Builder createBaseNotification(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("navigate_to", "inventory");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis());
    }
    
    /**
     * Create restock action for notifications
     */
    private NotificationCompat.Action createRestockAction() {
        Intent restockIntent = new Intent(context, MainActivity.class);
        restockIntent.putExtra("navigate_to", "inventory");
        restockIntent.putExtra("action", "restock");
        
        PendingIntent restockPendingIntent = PendingIntent.getActivity(
            context, 1, restockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Action.Builder(
            R.drawable.ic_add,
            "Restock",
            restockPendingIntent
        ).build();
    }
    
    /**
     * Cancel all inventory notifications
     */
    public void cancelAllNotifications() {
        notificationManager.cancel(NOTIFICATION_ID_LOW_STOCK);
        notificationManager.cancel(NOTIFICATION_ID_CRITICAL_STOCK);
        notificationManager.cancel(NOTIFICATION_ID_RESTOCK_REMINDER);
    }
    
    /**
     * Cancel specific notification
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }
    
    /**
     * Check if notifications are enabled
     */
    public boolean areNotificationsEnabled() {
        return notificationManager.areNotificationsEnabled();
    }
    
    /**
     * Get notification IDs for external reference
     */
    public static class NotificationIds {
        public static final int LOW_STOCK = NOTIFICATION_ID_LOW_STOCK;
        public static final int CRITICAL_STOCK = NOTIFICATION_ID_CRITICAL_STOCK;
        public static final int RESTOCK_REMINDER = NOTIFICATION_ID_RESTOCK_REMINDER;
    }
}