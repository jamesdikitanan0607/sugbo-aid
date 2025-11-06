package com.sugboaid.donation.adapters;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sugboaid.donation.R;
import com.sugboaid.donation.views.GlassmorphicCardView;
import com.sugboaid.donation.utils.NotificationManager;
import com.sugboaid.models.AppNotification;
import com.sugboaid.models.NotificationType;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying notifications with swipe-to-dismiss functionality
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<AppNotification> notifications;
    private OnNotificationClickListener clickListener;
    private OnNotificationSwipeListener swipeListener;
    private NotificationManager notificationManager;

    public interface OnNotificationClickListener {
        void onNotificationClick(AppNotification notification, int position);
        void onNotificationLongClick(AppNotification notification, int position);
    }

    public interface OnNotificationSwipeListener {
        void onNotificationSwiped(AppNotification notification, int position);
    }

    public NotificationAdapter() {
        this.notifications = new ArrayList<>();
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnNotificationSwipeListener(OnNotificationSwipeListener listener) {
        this.swipeListener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        
        // Initialize notification manager if not already done
        if (notificationManager == null) {
            notificationManager = new NotificationManager(parent.getContext());
        }
        
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        AppNotification notification = notifications.get(position);
        holder.bind(notification, position);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setNotifications(List<AppNotification> notifications) {
        this.notifications.clear();
        if (notifications != null) {
            this.notifications.addAll(notifications);
        }
        notifyDataSetChanged();
    }

    public void addNotification(AppNotification notification) {
        notifications.add(0, notification); // Add to top
        notifyItemInserted(0);
    }

    public void removeNotification(int position) {
        if (position >= 0 && position < notifications.size()) {
            notifications.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void markAsRead(int position) {
        if (position >= 0 && position < notifications.size()) {
            AppNotification notification = notifications.get(position);
            notification.markAsRead();
            notifyItemChanged(position);
        }
    }

    public void markAllAsRead() {
        for (AppNotification notification : notifications) {
            notification.markAsRead();
        }
        notifyDataSetChanged();
    }

    public int getUnreadCount() {
        int count = 0;
        for (AppNotification notification : notifications) {
            if (!notification.isRead()) {
                count++;
            }
        }
        return count;
    }

    public List<AppNotification> getNotifications() {
        return new ArrayList<>(notifications);
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        private GlassmorphicCardView cardNotification;
        private TextView tvNotificationIcon;
        private TextView tvNotificationTitle;
        private TextView tvNotificationMessage;
        private TextView tvNotificationTime;
        private TextView tvNotificationType;
        private View iconBackground;
        private View unreadIndicator;
        private View swipeBackground;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardNotification = itemView.findViewById(R.id.cardNotification);
            tvNotificationIcon = itemView.findViewById(R.id.tvNotificationIcon);
            tvNotificationTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvNotificationMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvNotificationTime = itemView.findViewById(R.id.tvNotificationTime);
            tvNotificationType = itemView.findViewById(R.id.tvNotificationType);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
            swipeBackground = itemView.findViewById(R.id.swipeBackground);
        }

        public void bind(AppNotification notification, int position) {
            // Set basic content
            tvNotificationTitle.setText(notification.getTitle());
            tvNotificationMessage.setText(notification.getMessage());
            tvNotificationTime.setText(notificationManager != null ? 
                notificationManager.formatNotificationTime(notification.getTimestamp()) : 
                notification.getFormattedTimestamp());
            tvNotificationType.setText(notification.getTypeDisplayName());
            tvNotificationIcon.setText(notification.getIconResource());

            // Set unread indicator visibility with enhanced styling
            updateUnreadIndicator(notification);

            // Apply visual styling based on read status and importance
            updateNotificationStyling(notification);

            // Set icon background gradient
            setIconGradient(notification.getColorGradient());

            // Set type badge color based on notification type
            setTypeBadgeColor(notification.getType());

            // Set enhanced click listeners
            setupClickListeners(notification, position);

            // Add entrance animation for new items
            if (position == 0 && !notification.isRead()) {
                animateEntrance();
            }
        }

        private void setIconGradient(String colorGradient) {
            if (colorGradient != null && colorGradient.contains(",")) {
                String[] colors = colorGradient.split(",");
                if (colors.length >= 2) {
                    try {
                        int startColor = Color.parseColor(colors[0]);
                        int endColor = Color.parseColor(colors[1]);
                        
                        GradientDrawable gradient = new GradientDrawable();
                        gradient.setShape(GradientDrawable.OVAL);
                        gradient.setColors(new int[]{startColor, endColor});
                        gradient.setOrientation(GradientDrawable.Orientation.TL_BR);
                        
                        iconBackground.setBackground(gradient);
                    } catch (IllegalArgumentException e) {
                        // Fallback to default gradient
                        iconBackground.setBackgroundResource(R.drawable.circle_gradient_background);
                    }
                }
            } else {
                iconBackground.setBackgroundResource(R.drawable.circle_gradient_background);
            }
        }

        private void setTypeBadgeColor(NotificationType type) {
            int backgroundColor;
            switch (type) {
                case DONATION_RECEIVED:
                    backgroundColor = itemView.getContext().getColor(R.color.success_green);
                    break;
                case INVENTORY_LOW:
                    backgroundColor = itemView.getContext().getColor(R.color.warning_orange);
                    break;
                case DISTRIBUTION_COMPLETE:
                    backgroundColor = itemView.getContext().getColor(R.color.primary_blue);
                    break;
                case SYSTEM_UPDATE:
                    backgroundColor = itemView.getContext().getColor(R.color.light_blue);
                    break;
                case ALERT:
                    backgroundColor = itemView.getContext().getColor(R.color.error_red);
                    break;
                case INFO:
                default:
                    backgroundColor = itemView.getContext().getColor(R.color.text_secondary);
                    break;
            }

            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.RECTANGLE);
            background.setColor(backgroundColor);
            background.setCornerRadius(12f);
            tvNotificationType.setBackground(background);
        }

        private void markAsReadWithAnimation(AppNotification notification, int position) {
            if (notificationManager != null) {
                notificationManager.animateMarkAsRead(cardNotification, unreadIndicator);
            } else {
                // Fallback animation
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(unreadIndicator, "alpha", 1f, 0f);
                fadeOut.setDuration(200);
                fadeOut.start();

                ObjectAnimator alphaChange = ObjectAnimator.ofFloat(cardNotification, "alpha", 1f, 0.7f);
                alphaChange.setDuration(300);
                alphaChange.start();

                unreadIndicator.postDelayed(() -> {
                    unreadIndicator.setVisibility(View.GONE);
                    unreadIndicator.setAlpha(1f);
                }, 200);
            }

            // Update the notification
            notification.markAsRead();
        }

        private void animateEntrance() {
            // Scale animation
            cardNotification.setScaleX(0.8f);
            cardNotification.setScaleY(0.8f);
            cardNotification.setAlpha(0f);

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardNotification, "scaleX", 0.8f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardNotification, "scaleY", 0.8f, 1f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(cardNotification, "alpha", 0f, 1f);

            scaleX.setDuration(300);
            scaleY.setDuration(300);
            alpha.setDuration(300);

            scaleX.start();
            scaleY.start();
            alpha.start();
        }

        private void updateUnreadIndicator(AppNotification notification) {
            if (notification.isRead()) {
                unreadIndicator.setVisibility(View.GONE);
            } else {
                unreadIndicator.setVisibility(View.VISIBLE);
                
                // Add pulsing animation for important notifications
                if (notificationManager != null && notificationManager.isImportantNotification(notification.getType())) {
                    animatePulse(unreadIndicator);
                }
            }
        }
        
        private void updateNotificationStyling(AppNotification notification) {
            float alpha = notification.isRead() ? 0.7f : 1.0f;
            cardNotification.setAlpha(alpha);
            
            // Add subtle border for important unread notifications
            if (!notification.isRead() && notificationManager != null && 
                notificationManager.isImportantNotification(notification.getType())) {
                cardNotification.setCardBackgroundColor(
                    itemView.getContext().getColor(R.color.glass_white_60));
            } else {
                cardNotification.setCardBackgroundColor(
                    itemView.getContext().getColor(R.color.card_background));
            }
        }
        
        private void setupClickListeners(AppNotification notification, int position) {
            cardNotification.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onNotificationClick(notification, position);
                }
                
                // Mark as read with animation if unread
                if (!notification.isRead()) {
                    markAsReadWithAnimation(notification, position);
                }
            });

            cardNotification.setOnLongClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onNotificationLongClick(notification, position);
                }
                
                // Show context menu or additional options
                showNotificationOptions(notification, position);
                return true;
            });
        }
        
        private void showNotificationOptions(AppNotification notification, int position) {
            // Create a simple popup menu for notification options
            android.widget.PopupMenu popup = new android.widget.PopupMenu(itemView.getContext(), cardNotification);
            popup.getMenuInflater().inflate(R.menu.notification_context_menu, popup.getMenu());
            
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_mark_unread) {
                    notification.markAsUnread();
                    notifyItemChanged(position);
                    return true;
                } else if (itemId == R.id.action_delete) {
                    if (swipeListener != null) {
                        swipeListener.onNotificationSwiped(notification, position);
                    }
                    return true;
                }
                return false;
            });
            
            popup.show();
        }
        
        private void animatePulse(View view) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f, 1f);
            
            scaleX.setDuration(1000);
            scaleY.setDuration(1000);
            scaleX.setRepeatCount(ObjectAnimator.INFINITE);
            scaleY.setRepeatCount(ObjectAnimator.INFINITE);
            
            scaleX.start();
            scaleY.start();
        }

        public void animateSwipeOut(Runnable onComplete) {
            if (notificationManager != null) {
                notificationManager.animateNotificationDismissal(cardNotification, onComplete);
            } else {
                ObjectAnimator slideOut = ObjectAnimator.ofFloat(cardNotification, "translationX", 0f, cardNotification.getWidth());
                slideOut.setDuration(250);
                slideOut.addListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });
                slideOut.start();
            }
        }
    }
}