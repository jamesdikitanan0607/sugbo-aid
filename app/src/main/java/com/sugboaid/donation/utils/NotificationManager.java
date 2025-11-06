package com.sugboaid.donation.utils;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.sugboaid.donation.R;
import com.sugboaid.models.AppNotification;
import com.sugboaid.models.NotificationType;

/**
 * Utility class for managing notification UI interactions and badge display
 */
public class NotificationManager {
    
    private Context context;
    
    public NotificationManager(Context context) {
        this.context = context;
    }
    
    /**
     * Updates a notification badge with the unread count
     */
    public void updateNotificationBadge(TextView badgeView, int unreadCount) {
        if (badgeView == null) return;
        
        if (unreadCount > 0) {
            badgeView.setVisibility(View.VISIBLE);
            
            // Format count display
            String countText;
            if (unreadCount > 99) {
                countText = "99+";
            } else {
                countText = String.valueOf(unreadCount);
            }
            badgeView.setText(countText);
            
            // Apply badge styling
            badgeView.setBackgroundResource(R.drawable.notification_badge_background);
            badgeView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            
            // Add pulsing animation for new notifications
            if (unreadCount > 0) {
                animateBadge(badgeView);
            }
        } else {
            badgeView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Creates a notification badge view programmatically
     */
    public TextView createNotificationBadge(Context context) {
        TextView badge = new TextView(context);
        badge.setBackgroundResource(R.drawable.notification_badge_background);
        badge.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        badge.setTextSize(10);
        badge.setPadding(8, 4, 8, 4);
        badge.setVisibility(View.GONE);
        return badge;
    }
    
    /**
     * Animates the notification badge with a pulsing effect
     */
    private void animateBadge(View badgeView) {
        android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(badgeView, "scaleX", 1f, 1.2f, 1f);
        android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(badgeView, "scaleY", 1f, 1.2f, 1f);
        
        scaleX.setDuration(600);
        scaleY.setDuration(600);
        scaleX.setRepeatCount(2);
        scaleY.setRepeatCount(2);
        
        scaleX.start();
        scaleY.start();
    }
    
    /**
     * Gets the appropriate color for a notification type
     */
    public int getNotificationTypeColor(NotificationType type) {
        switch (type) {
            case DONATION_RECEIVED:
                return ContextCompat.getColor(context, R.color.success_green);
            case INVENTORY_LOW:
                return ContextCompat.getColor(context, R.color.warning_orange);
            case DISTRIBUTION_COMPLETE:
                return ContextCompat.getColor(context, R.color.primary_blue);
            case SYSTEM_UPDATE:
                return ContextCompat.getColor(context, R.color.light_blue);
            case ALERT:
                return ContextCompat.getColor(context, R.color.error_red);
            case INFO:
            default:
                return ContextCompat.getColor(context, R.color.text_secondary);
        }
    }
    
    /**
     * Gets the appropriate icon for a notification type
     */
    public String getNotificationTypeIcon(NotificationType type) {
        switch (type) {
            case DONATION_RECEIVED:
                return "💝";
            case INVENTORY_LOW:
                return "⚠️";
            case DISTRIBUTION_COMPLETE:
                return "✅";
            case SYSTEM_UPDATE:
                return "🔄";
            case ALERT:
                return "🚨";
            case INFO:
            default:
                return "ℹ️";
        }
    }
    
    /**
     * Categorizes notifications by type for better organization
     */
    public String getCategoryName(NotificationType type) {
        switch (type) {
            case DONATION_RECEIVED:
                return "Donations";
            case INVENTORY_LOW:
                return "Inventory";
            case DISTRIBUTION_COMPLETE:
                return "Distribution";
            case SYSTEM_UPDATE:
                return "System";
            case ALERT:
                return "Alerts";
            case INFO:
            default:
                return "General";
        }
    }
    
    /**
     * Determines if a notification type is considered important/urgent
     */
    public boolean isImportantNotification(NotificationType type) {
        return type == NotificationType.ALERT || type == NotificationType.INVENTORY_LOW;
    }
    
    /**
     * Formats notification time for display
     */
    public String formatNotificationTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 7) {
            return new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                    .format(new java.util.Date(timestamp));
        } else if (days > 0) {
            return days + "d ago";
        } else if (hours > 0) {
            return hours + "h ago";
        } else if (minutes > 0) {
            return minutes + "m ago";
        } else {
            return "Just now";
        }
    }
    
    /**
     * Creates a notification dismissal animation
     */
    public void animateNotificationDismissal(View notificationView, Runnable onComplete) {
        android.animation.ObjectAnimator slideOut = android.animation.ObjectAnimator.ofFloat(
            notificationView, "translationX", 0f, notificationView.getWidth());
        android.animation.ObjectAnimator fadeOut = android.animation.ObjectAnimator.ofFloat(
            notificationView, "alpha", 1f, 0f);
        
        slideOut.setDuration(250);
        fadeOut.setDuration(250);
        
        slideOut.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        
        slideOut.start();
        fadeOut.start();
    }
    
    /**
     * Creates a notification mark-as-read animation
     */
    public void animateMarkAsRead(View notificationView, View unreadIndicator) {
        // Fade out unread indicator
        android.animation.ObjectAnimator fadeOutIndicator = android.animation.ObjectAnimator.ofFloat(
            unreadIndicator, "alpha", 1f, 0f);
        fadeOutIndicator.setDuration(200);
        
        // Slightly fade the notification
        android.animation.ObjectAnimator fadeNotification = android.animation.ObjectAnimator.ofFloat(
            notificationView, "alpha", 1f, 0.7f);
        fadeNotification.setDuration(300);
        
        fadeOutIndicator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                unreadIndicator.setVisibility(View.GONE);
                unreadIndicator.setAlpha(1f); // Reset for future use
            }
        });
        
        fadeOutIndicator.start();
        fadeNotification.start();
    }
    
    /**
     * Creates a notification entrance animation for new notifications
     */
    public void animateNotificationEntrance(View notificationView) {
        notificationView.setTranslationX(notificationView.getWidth());
        notificationView.setAlpha(0f);
        
        android.animation.ObjectAnimator slideIn = android.animation.ObjectAnimator.ofFloat(
            notificationView, "translationX", notificationView.getWidth(), 0f);
        android.animation.ObjectAnimator fadeIn = android.animation.ObjectAnimator.ofFloat(
            notificationView, "alpha", 0f, 1f);
        
        slideIn.setDuration(300);
        fadeIn.setDuration(300);
        
        slideIn.start();
        fadeIn.start();
    }
}