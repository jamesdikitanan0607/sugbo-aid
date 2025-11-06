package com.sugboaid.donation.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.sugboaid.donation.R;
import com.sugboaid.models.InventoryItem;
import com.sugboaid.models.InventoryStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * RecyclerView adapter for displaying inventory items with stock management controls
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {
    
    private List<InventoryItem> inventoryItems;
    private List<InventoryItem> filteredItems;
    private Context context;
    private OnInventoryItemClickListener listener;
    private SimpleDateFormat dateFormat;
    private Map<String, Integer> previousStockLevels; // For trend tracking

    public interface OnInventoryItemClickListener {
        void onItemClick(InventoryItem item);
        void onAddStock(InventoryItem item);
        void onRemoveStock(InventoryItem item);
        void onItemLongClick(InventoryItem item);
        void onLowStockAlert(InventoryItem item);
        void onRestockNotification(InventoryItem item);
    }

    public InventoryAdapter(Context context) {
        this.context = context;
        this.inventoryItems = new ArrayList<>();
        this.filteredItems = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        this.previousStockLevels = new HashMap<>();
    }

    public void setOnInventoryItemClickListener(OnInventoryItemClickListener listener) {
        this.listener = listener;
    }

    public void setInventoryItems(List<InventoryItem> items) {
        this.inventoryItems = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.filteredItems = new ArrayList<>(this.inventoryItems);
        notifyDataSetChanged();
    }

    public void filterByStatus(InventoryStatus status) {
        filteredItems.clear();
        
        if (status == null) {
            // Show all items
            filteredItems.addAll(inventoryItems);
        } else {
            // Filter by specific status
            for (InventoryItem item : inventoryItems) {
                if (item.getStatus() == status) {
                    filteredItems.add(item);
                }
            }
        }
        
        notifyDataSetChanged();
    }

    public void filterByQuery(String query) {
        filteredItems.clear();
        
        if (query == null || query.trim().isEmpty()) {
            filteredItems.addAll(inventoryItems);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (InventoryItem item : inventoryItems) {
                if (item.getName().toLowerCase().contains(lowerQuery) ||
                    item.getUnit().toLowerCase().contains(lowerQuery)) {
                    filteredItems.add(item);
                }
            }
        }
        
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = filteredItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    public class InventoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItemIcon;
        private TextView tvItemName;
        private TextView tvItemUnit;
        private TextView tvStatusBadge;
        private TextView tvStockInfo;
        private TextView tvStockPercentage;
        private ProgressBar progressStock;
        private Button btnRemoveStock;
        private Button btnAddStock;
        private TextView tvCurrentStock;
        private TextView tvLastUpdated;
        private ImageView ivTrendIndicator;
        private ImageView ivLowStockAlert;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvItemIcon = itemView.findViewById(R.id.tv_item_icon);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvItemUnit = itemView.findViewById(R.id.tv_item_unit);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvStockInfo = itemView.findViewById(R.id.tv_stock_info);
            tvStockPercentage = itemView.findViewById(R.id.tv_stock_percentage);
            progressStock = itemView.findViewById(R.id.progress_stock);
            btnRemoveStock = itemView.findViewById(R.id.btn_remove_stock);
            btnAddStock = itemView.findViewById(R.id.btn_add_stock);
            tvCurrentStock = itemView.findViewById(R.id.tv_current_stock);
            tvLastUpdated = itemView.findViewById(R.id.tv_last_updated);
            ivTrendIndicator = itemView.findViewById(R.id.iv_trend_indicator);
            ivLowStockAlert = itemView.findViewById(R.id.iv_low_stock_alert);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(filteredItems.get(getAdapterPosition()));
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemLongClick(filteredItems.get(getAdapterPosition()));
                    return true;
                }
                return false;
            });

            btnAddStock.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onAddStock(filteredItems.get(getAdapterPosition()));
                }
            });

            btnRemoveStock.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onRemoveStock(filteredItems.get(getAdapterPosition()));
                }
            });
            
            // Low stock alert click listener
            ivLowStockAlert.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    InventoryItem item = filteredItems.get(getAdapterPosition());
                    if (item.isCriticalStock()) {
                        listener.onRestockNotification(item);
                    } else {
                        listener.onLowStockAlert(item);
                    }
                }
            });
        }

        public void bind(InventoryItem item) {
            // Set item icon (emoji)
            tvItemIcon.setText(item.getIconEmoji() != null ? item.getIconEmoji() : "📦");
            
            // Set item name and unit
            tvItemName.setText(item.getName());
            tvItemUnit.setText(item.getUnit());
            
            // Set enhanced status badge with appropriate colors
            setStatusBadge(item);
            
            // Set stock information
            tvStockInfo.setText(item.getFormattedStock());
            tvCurrentStock.setText(String.valueOf(item.getStock()));
            
            // Set stock percentage with dynamic styling
            double percentage = item.getStockPercentage();
            tvStockPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
            setPercentageBadgeStyle(tvStockPercentage, item.getStatus());
            
            // Set enhanced progress bar with status-based colors
            setProgressBar(item, percentage);
            
            // Set trend indicator
            setTrendIndicator(item);
            
            // Set low stock alert
            setLowStockAlert(item);
            
            // Set last updated time
            Date lastUpdated = new Date(item.getLastUpdated());
            tvLastUpdated.setText("Updated " + getRelativeTime(lastUpdated));
            
            // Enable/disable buttons based on stock levels with enhanced styling
            setButtonStates(item);
        }

        private void setStatusBadge(InventoryItem item) {
            InventoryStatus status = item.getStatus();
            tvStatusBadge.setText(getStatusText(status));
            
            // Create gradient background for status badge
            GradientDrawable badgeBackground = new GradientDrawable();
            badgeBackground.setShape(GradientDrawable.RECTANGLE);
            badgeBackground.setCornerRadius(12f);
            badgeBackground.setColor(ContextCompat.getColor(context, getStatusColor(status)));
            
            // Add subtle gradient effect
            int[] colors = getStatusGradientColors(status);
            if (colors.length > 1) {
                badgeBackground.setColors(colors);
                badgeBackground.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            }
            
            tvStatusBadge.setBackground(badgeBackground);
            tvStatusBadge.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }
        
        private void setProgressBar(InventoryItem item, double percentage) {
            progressStock.setProgress((int) percentage);
            
            // Set progress bar color based on status
            int progressColor = getStatusColor(item.getStatus());
            progressStock.setProgressTintList(ColorStateList.valueOf(
                ContextCompat.getColor(context, progressColor)
            ));
            
            // Set background color for progress bar
            progressStock.setProgressBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.glass_white_20)
            ));
        }
        
        private void setTrendIndicator(InventoryItem item) {
            String itemName = item.getName();
            Integer previousStock = previousStockLevels.get(itemName);
            
            if (previousStock != null && previousStock != item.getStock()) {
                ivTrendIndicator.setVisibility(View.VISIBLE);
                
                if (item.getStock() > previousStock) {
                    // Stock increased
                    ivTrendIndicator.setImageResource(R.drawable.ic_trend_up);
                    ivTrendIndicator.setColorFilter(ContextCompat.getColor(context, R.color.success_green));
                } else {
                    // Stock decreased
                    ivTrendIndicator.setImageResource(R.drawable.ic_trend_down);
                    ivTrendIndicator.setColorFilter(ContextCompat.getColor(context, R.color.error_red));
                }
            } else {
                ivTrendIndicator.setVisibility(View.GONE);
            }
            
            // Update previous stock level for next comparison
            previousStockLevels.put(itemName, item.getStock());
        }
        
        private void setLowStockAlert(InventoryItem item) {
            if (item.isLowStock()) {
                ivLowStockAlert.setVisibility(View.VISIBLE);
                
                // Set different colors based on criticality
                if (item.isCriticalStock()) {
                    ivLowStockAlert.setColorFilter(ContextCompat.getColor(context, R.color.error_red));
                } else {
                    ivLowStockAlert.setColorFilter(ContextCompat.getColor(context, R.color.warning_orange));
                }
                
                // Add pulsing animation for critical stock
                if (item.isCriticalStock()) {
                    startPulseAnimation(ivLowStockAlert);
                }
            } else {
                ivLowStockAlert.setVisibility(View.GONE);
                ivLowStockAlert.clearAnimation();
            }
        }
        
        private void setButtonStates(InventoryItem item) {
            // Enable/disable buttons based on stock levels
            btnRemoveStock.setEnabled(item.getStock() > 0);
            btnAddStock.setEnabled(item.getStock() < item.getCapacity());
            
            // Enhanced button styling
            setButtonStyle(btnRemoveStock, btnRemoveStock.isEnabled(), R.color.error_red);
            setButtonStyle(btnAddStock, btnAddStock.isEnabled(), R.color.success_green);
        }
        
        private void setButtonStyle(Button button, boolean enabled, int colorRes) {
            float alpha = enabled ? 1.0f : 0.5f;
            button.setAlpha(alpha);
            
            if (enabled) {
                button.setTextColor(ContextCompat.getColor(context, colorRes));
            } else {
                button.setTextColor(ContextCompat.getColor(context, R.color.text_tertiary));
            }
        }
        
        private void startPulseAnimation(View view) {
            view.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(500)
                .withEndAction(() -> {
                    view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(500)
                        .withEndAction(() -> {
                            if (view.getVisibility() == View.VISIBLE) {
                                startPulseAnimation(view);
                            }
                        });
                });
        }

        private int getStatusColor(InventoryStatus status) {
            switch (status) {
                case HEALTHY:
                    return R.color.status_healthy;
                case MODERATE:
                    return R.color.status_moderate;
                case LOW:
                    return R.color.status_low;
                case CRITICAL:
                    return R.color.status_critical;
                default:
                    return R.color.text_secondary;
            }
        }
        
        private String getStatusText(InventoryStatus status) {
            switch (status) {
                case HEALTHY:
                    return "HEALTHY";
                case MODERATE:
                    return "MODERATE";
                case LOW:
                    return "LOW STOCK";
                case CRITICAL:
                    return "CRITICAL";
                default:
                    return "UNKNOWN";
            }
        }
        
        private int[] getStatusGradientColors(InventoryStatus status) {
            switch (status) {
                case HEALTHY:
                    return new int[]{
                        ContextCompat.getColor(context, R.color.status_healthy),
                        ContextCompat.getColor(context, R.color.success_green)
                    };
                case MODERATE:
                    return new int[]{
                        ContextCompat.getColor(context, R.color.status_moderate),
                        ContextCompat.getColor(context, R.color.warning_yellow)
                    };
                case LOW:
                    return new int[]{
                        ContextCompat.getColor(context, R.color.status_low),
                        ContextCompat.getColor(context, R.color.warning_orange)
                    };
                case CRITICAL:
                    return new int[]{
                        ContextCompat.getColor(context, R.color.status_critical),
                        ContextCompat.getColor(context, R.color.error_red)
                    };
                default:
                    return new int[]{ContextCompat.getColor(context, R.color.text_secondary)};
            }
        }
        
        private void setPercentageBadgeStyle(TextView percentageView, InventoryStatus status) {
            GradientDrawable badgeBackground = new GradientDrawable();
            badgeBackground.setShape(GradientDrawable.RECTANGLE);
            badgeBackground.setCornerRadius(8f);
            
            // Set background color based on status
            int backgroundColor = ContextCompat.getColor(context, getStatusColor(status));
            badgeBackground.setColor(backgroundColor);
            
            // Set border
            badgeBackground.setStroke(2, ContextCompat.getColor(context, R.color.glass_border));
            
            percentageView.setBackground(badgeBackground);
            percentageView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }

        private String getRelativeTime(Date date) {
            long diff = System.currentTimeMillis() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                return days + " day" + (days > 1 ? "s" : "") + " ago";
            } else if (hours > 0) {
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else if (minutes > 0) {
                return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
            } else {
                return "Just now";
            }
        }
    }

    // Utility methods
    public List<InventoryItem> getCurrentItems() {
        return new ArrayList<>(filteredItems);
    }

    public void updateItem(InventoryItem updatedItem) {
        // Update in original list
        for (int i = 0; i < inventoryItems.size(); i++) {
            if (inventoryItems.get(i).getName().equals(updatedItem.getName())) {
                inventoryItems.set(i, updatedItem);
                break;
            }
        }
        
        // Update in filtered list
        for (int i = 0; i < filteredItems.size(); i++) {
            if (filteredItems.get(i).getName().equals(updatedItem.getName())) {
                filteredItems.set(i, updatedItem);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeItem(String itemName) {
        // Remove from original list
        inventoryItems.removeIf(item -> item.getName().equals(itemName));
        
        // Remove from filtered list and notify
        for (int i = 0; i < filteredItems.size(); i++) {
            if (filteredItems.get(i).getName().equals(itemName)) {
                filteredItems.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void addItem(InventoryItem newItem) {
        inventoryItems.add(newItem);
        filteredItems.add(newItem);
        notifyItemInserted(filteredItems.size() - 1);
        
        // Check for low stock notification
        checkForLowStockNotification(newItem);
    }

    public boolean isEmpty() {
        return filteredItems.isEmpty();
    }

    public void clearFilter() {
        filteredItems.clear();
        filteredItems.addAll(inventoryItems);
        notifyDataSetChanged();
    }
    
    // Enhanced notification methods
    public void checkForLowStockNotifications() {
        for (InventoryItem item : inventoryItems) {
            checkForLowStockNotification(item);
        }
    }
    
    private void checkForLowStockNotification(InventoryItem item) {
        if (item.isLowStock() && listener != null) {
            listener.onLowStockAlert(item);
            
            // Trigger restock notification for critical items
            if (item.isCriticalStock()) {
                listener.onRestockNotification(item);
            }
        }
    }
    
    public List<InventoryItem> getLowStockItems() {
        List<InventoryItem> lowStockItems = new ArrayList<>();
        for (InventoryItem item : inventoryItems) {
            if (item.isLowStock()) {
                lowStockItems.add(item);
            }
        }
        return lowStockItems;
    }
    
    public List<InventoryItem> getCriticalStockItems() {
        List<InventoryItem> criticalItems = new ArrayList<>();
        for (InventoryItem item : inventoryItems) {
            if (item.isCriticalStock()) {
                criticalItems.add(item);
            }
        }
        return criticalItems;
    }
    
    public int getLowStockCount() {
        return getLowStockItems().size();
    }
    
    public int getCriticalStockCount() {
        return getCriticalStockItems().size();
    }
    
    // Method to update stock levels and track trends
    public void updateItemStock(InventoryItem updatedItem) {
        String itemName = updatedItem.getName();
        
        // Store previous stock level for trend tracking
        InventoryItem existingItem = getItemByName(itemName);
        if (existingItem != null) {
            previousStockLevels.put(itemName, existingItem.getStock());
        }
        
        // Update the item
        updateItem(updatedItem);
        
        // Check for notifications
        checkForLowStockNotification(updatedItem);
    }
    
    private InventoryItem getItemByName(String itemName) {
        for (InventoryItem item : inventoryItems) {
            if (item.getName().equals(itemName)) {
                return item;
            }
        }
        return null;
    }
}