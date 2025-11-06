package com.sugboaid.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.sugboaid.models.InventoryItem;
import com.sugboaid.models.InventoryStatus;
import com.sugboaid.models.AppNotification;
import com.sugboaid.utils.SharedPreferencesHelper;
import com.sugboaid.utils.NetworkUtils;
import com.sugboaid.utils.OfflineQueueManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Repository class for managing inventory data with stock tracking capabilities
 */
public class InventoryRepository {
    private SharedPreferencesHelper prefsHelper;
    private OfflineQueueManager offlineQueueManager;
    private Context context;
    private MutableLiveData<List<InventoryItem>> inventoryLiveData;
    private MutableLiveData<Integer> totalItemsLiveData;
    private MutableLiveData<Integer> lowStockCountLiveData;
    private MutableLiveData<List<InventoryItem>> lowStockItemsLiveData;
    
    private static InventoryRepository instance;

    // Private constructor for singleton pattern
    private InventoryRepository(Context context) {
        this.context = context.getApplicationContext();
        prefsHelper = SharedPreferencesHelper.getInstance(context);
        offlineQueueManager = OfflineQueueManager.getInstance(context);
        inventoryLiveData = new MutableLiveData<>();
        totalItemsLiveData = new MutableLiveData<>();
        lowStockCountLiveData = new MutableLiveData<>();
        lowStockItemsLiveData = new MutableLiveData<>();
        
        // Initialize with existing data or default items
        loadInventory();
    }

    // Singleton instance getter
    public static synchronized InventoryRepository getInstance(Context context) {
        if (instance == null) {
            instance = new InventoryRepository(context.getApplicationContext());
        }
        return instance;
    }

    // LiveData getters
    public LiveData<List<InventoryItem>> getInventoryItems() {
        return inventoryLiveData;
    }

    public LiveData<Integer> getTotalItems() {
        return totalItemsLiveData;
    }

    public LiveData<Integer> getLowStockCount() {
        return lowStockCountLiveData;
    }

    public LiveData<List<InventoryItem>> getLowStockItems() {
        return lowStockItemsLiveData;
    }

    public LiveData<Integer> getTotalDistributedItems() {
        return totalItemsLiveData;
    }

    // Data operations
    public void addInventoryItem(InventoryItem item) {
        try {
            if (item == null) {
                throw new IllegalArgumentException("Inventory item cannot be null");
            }
            
            if (!item.isValid()) {
                throw new IllegalArgumentException("Invalid inventory item data");
            }
            
            prefsHelper.addInventoryItem(item);
            
            // Queue for sync
            if (!NetworkUtils.isNetworkAvailable(context)) {
                offlineQueueManager.queueInventoryAction(OfflineQueueManager.ActionType.ADD_INVENTORY_ITEM, item);
            } else {
                offlineQueueManager.queueInventoryAction(OfflineQueueManager.ActionType.ADD_INVENTORY_ITEM, item);
            }
            
            loadInventory();
            
            // Check for low stock notification
            if (item.isLowStock()) {
                createLowStockNotification(item);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to add inventory item: " + e.getMessage(), e);
        }
    }

    public void updateInventoryItem(InventoryItem item) {
        try {
            if (item == null || !item.isValid()) {
                throw new IllegalArgumentException("Invalid inventory item data");
            }
            
            List<InventoryItem> items = prefsHelper.getInventoryItems();
            boolean updated = false;
            
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getName().equals(item.getName())) {
                    InventoryItem oldItem = items.get(i);
                    items.set(i, item);
                    updated = true;
                    
                    // Check if stock became low
                    if (!oldItem.isLowStock() && item.isLowStock()) {
                        createLowStockNotification(item);
                    }
                    break;
                }
            }
            
            if (updated) {
                prefsHelper.saveInventoryItems(items);
                
                // Queue for sync
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    offlineQueueManager.queueInventoryAction(OfflineQueueManager.ActionType.UPDATE_INVENTORY_ITEM, item);
                } else {
                    offlineQueueManager.queueInventoryAction(OfflineQueueManager.ActionType.UPDATE_INVENTORY_ITEM, item);
                }
                
                loadInventory();
            } else {
                throw new IllegalArgumentException("Inventory item not found for update");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update inventory item: " + e.getMessage(), e);
        }
    }

    public void deleteInventoryItem(String itemName) {
        try {
            if (itemName == null || itemName.trim().isEmpty()) {
                throw new IllegalArgumentException("Item name cannot be null or empty");
            }
            
            // Get the item before deleting for queue purposes
            InventoryItem itemToDelete = getInventoryItemByName(itemName);
            if (itemToDelete != null) {
                // Queue for sync
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    offlineQueueManager.queueInventoryAction(OfflineQueueManager.ActionType.DELETE_INVENTORY_ITEM, itemToDelete);
                } else {
                    offlineQueueManager.queueInventoryAction(OfflineQueueManager.ActionType.DELETE_INVENTORY_ITEM, itemToDelete);
                }
            }
            
            prefsHelper.removeInventoryItem(itemName);
            loadInventory();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete inventory item: " + e.getMessage(), e);
        }
    }

    public InventoryItem getInventoryItemByName(String itemName) {
        if (itemName == null) return null;
        
        List<InventoryItem> items = prefsHelper.getInventoryItems();
        for (InventoryItem item : items) {
            if (item.getName().equals(itemName)) {
                return item;
            }
        }
        return null;
    }

    // Stock management operations
    public void addStock(String itemName, int quantity) {
        try {
            InventoryItem item = getInventoryItemByName(itemName);
            if (item == null) {
                throw new IllegalArgumentException("Inventory item not found: " + itemName);
            }
            
            item.addStock(quantity);
            updateInventoryItem(item);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to add stock: " + e.getMessage(), e);
        }
    }

    public void removeStock(String itemName, int quantity) {
        try {
            InventoryItem item = getInventoryItemByName(itemName);
            if (item == null) {
                throw new IllegalArgumentException("Inventory item not found: " + itemName);
            }
            
            if (item.getStock() < quantity) {
                throw new IllegalArgumentException("Insufficient stock available");
            }
            
            item.removeStock(quantity);
            updateInventoryItem(item);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove stock: " + e.getMessage(), e);
        }
    }

    public void updateStock(String itemName, int newStock) {
        try {
            InventoryItem item = getInventoryItemByName(itemName);
            if (item == null) {
                throw new IllegalArgumentException("Inventory item not found: " + itemName);
            }
            
            item.setStock(newStock);
            updateInventoryItem(item);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update stock: " + e.getMessage(), e);
        }
    }

    // Filter and search operations
    public LiveData<List<InventoryItem>> getInventoryByStatus(InventoryStatus status) {
        MutableLiveData<List<InventoryItem>> filteredLiveData = new MutableLiveData<>();
        
        List<InventoryItem> allItems = prefsHelper.getInventoryItems();
        List<InventoryItem> filtered = new ArrayList<>();
        
        for (InventoryItem item : allItems) {
            if (item.getStatus() == status) {
                filtered.add(item);
            }
        }
        
        filteredLiveData.setValue(filtered);
        return filteredLiveData;
    }

    public LiveData<List<InventoryItem>> searchInventory(String query) {
        MutableLiveData<List<InventoryItem>> searchLiveData = new MutableLiveData<>();
        
        if (query == null || query.trim().isEmpty()) {
            searchLiveData.setValue(prefsHelper.getInventoryItems());
            return searchLiveData;
        }
        
        List<InventoryItem> allItems = prefsHelper.getInventoryItems();
        List<InventoryItem> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();
        
        for (InventoryItem item : allItems) {
            if (item.getName().toLowerCase().contains(lowerQuery) ||
                item.getUnit().toLowerCase().contains(lowerQuery)) {
                filtered.add(item);
            }
        }
        
        searchLiveData.setValue(filtered);
        return searchLiveData;
    }

    // Statistics and analytics
    public int calculateTotalStock() {
        List<InventoryItem> items = prefsHelper.getInventoryItems();
        int total = 0;
        
        for (InventoryItem item : items) {
            total += item.getStock();
        }
        
        return total;
    }

    public int calculateTotalCapacity() {
        List<InventoryItem> items = prefsHelper.getInventoryItems();
        int total = 0;
        
        for (InventoryItem item : items) {
            total += item.getCapacity();
        }
        
        return total;
    }

    public double calculateOverallStockPercentage() {
        int totalStock = calculateTotalStock();
        int totalCapacity = calculateTotalCapacity();
        
        if (totalCapacity == 0) return 0.0;
        return (double) totalStock / totalCapacity * 100.0;
    }

    public List<InventoryItem> getCriticalStockItems() {
        List<InventoryItem> items = prefsHelper.getInventoryItems();
        List<InventoryItem> critical = new ArrayList<>();
        
        for (InventoryItem item : items) {
            if (item.isCriticalStock()) {
                critical.add(item);
            }
        }
        
        return critical;
    }

    // Default inventory setup
    private void initializeDefaultInventory() {
        List<InventoryItem> defaultItems = new ArrayList<>();
        
        // Create default inventory items based on the original app
        defaultItems.add(new InventoryItem("Rice", 150, 200, "sacks", "🍚", "#10b981,#059669"));
        defaultItems.add(new InventoryItem("Water", 80, 150, "bottles", "💧", "#3b82f6,#2563eb"));
        defaultItems.add(new InventoryItem("Medicine", 45, 100, "boxes", "💊", "#f59e0b,#d97706"));
        defaultItems.add(new InventoryItem("Clothes", 120, 180, "pieces", "👕", "#8b5cf6,#7c3aed"));
        
        prefsHelper.saveInventoryItems(defaultItems);
    }

    // Data refresh and synchronization
    public void refreshData() {
        loadInventory();
    }

    private void loadInventory() {
        try {
            List<InventoryItem> items = prefsHelper.getInventoryItems();
            
            // Initialize with default items if empty
            if (items.isEmpty()) {
                initializeDefaultInventory();
                items = prefsHelper.getInventoryItems();
            }
            
            // Sort by name
            Collections.sort(items, new Comparator<InventoryItem>() {
                @Override
                public int compare(InventoryItem i1, InventoryItem i2) {
                    return i1.getName().compareToIgnoreCase(i2.getName());
                }
            });
            
            inventoryLiveData.setValue(items);
            
            // Update statistics
            int totalItems = calculateTotalStock();
            totalItemsLiveData.setValue(totalItems);
            prefsHelper.saveTotalDistributedItems(totalItems);
            
            // Update low stock items
            List<InventoryItem> lowStockItems = new ArrayList<>();
            int lowStockCount = 0;
            
            for (InventoryItem item : items) {
                if (item.isLowStock()) {
                    lowStockItems.add(item);
                    lowStockCount++;
                }
            }
            
            lowStockItemsLiveData.setValue(lowStockItems);
            lowStockCountLiveData.setValue(lowStockCount);
            
        } catch (Exception e) {
            // In production, log this error
            inventoryLiveData.setValue(new ArrayList<>());
            totalItemsLiveData.setValue(0);
            lowStockCountLiveData.setValue(0);
            lowStockItemsLiveData.setValue(new ArrayList<>());
        }
    }

    // Notification helper
    private void createLowStockNotification(InventoryItem item) {
        try {
            AppNotification notification = AppNotification.createLowInventoryNotification(
                item.getName(), item.getStock());
            prefsHelper.addNotification(notification);
        } catch (Exception e) {
            // Log error in production
        }
    }

    // Data validation and cleanup
    public boolean validateAllInventoryItems() {
        try {
            List<InventoryItem> items = prefsHelper.getInventoryItems();
            for (InventoryItem item : items) {
                if (!item.isValid()) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void cleanupInvalidItems() {
        try {
            List<InventoryItem> items = prefsHelper.getInventoryItems();
            List<InventoryItem> validItems = new ArrayList<>();
            
            for (InventoryItem item : items) {
                if (item.isValid()) {
                    validItems.add(item);
                }
            }
            
            if (validItems.size() != items.size()) {
                prefsHelper.saveInventoryItems(validItems);
                loadInventory();
            }
        } catch (Exception e) {
            // Log error in production
        }
    }

    // Clear all data
    public void clearAllInventory() {
        try {
            prefsHelper.clearInventory();
            loadInventory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear inventory: " + e.getMessage(), e);
        }
    }

    // Reset to default inventory
    public void resetToDefaultInventory() {
        try {
            clearAllInventory();
            initializeDefaultInventory();
            loadInventory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset inventory: " + e.getMessage(), e);
        }
    }
}