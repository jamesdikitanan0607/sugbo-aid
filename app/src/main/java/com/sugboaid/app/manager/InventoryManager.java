package com.sugboaid.app.manager;

import android.content.Context;
import com.sugboaid.app.data.repository.DataRepository;
import com.sugboaid.app.data.model.InventoryItem;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InventoryManager {
    private DataRepository repository;
    private AuthManager authManager;
    private static InventoryManager instance;

    private InventoryManager(Context context) {
        repository = DataRepository.getInstance(context);
        authManager = AuthManager.getInstance(context);
    }

    public static synchronized InventoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new InventoryManager(context.getApplicationContext());
        }
        return instance;
    }

    public InventoryItem createInventoryItem(String name, String category, int initialStock, 
                                           String unit, double unitValue) {
        InventoryItem item = new InventoryItem(name, category, initialStock, unit);
        item.setId(UUID.randomUUID().toString());
        item.setUnitValue(unitValue);
        item.setLastUpdated(System.currentTimeMillis());
        
        if (authManager.getCurrentUser() != null) {
            item.setUpdatedBy(authManager.getCurrentUser().getName());
        }
        
        repository.saveInventoryItem(item);
        return item;
    }

    public void updateStock(String itemId, int newStock, String reason) {
        InventoryItem item = repository.getInventoryItemById(itemId);
        if (item != null) {
            item.setCurrentStock(newStock);
            item.setLastUpdated(System.currentTimeMillis());
            
            if (authManager.getCurrentUser() != null) {
                item.setUpdatedBy(authManager.getCurrentUser().getName());
            }
            
            repository.saveInventoryItem(item);
        }
    }

    public void addStock(String itemId, int quantity, String reason) {
        InventoryItem item = repository.getInventoryItemById(itemId);
        if (item != null) {
            int newStock = item.getCurrentStock() + quantity;
            updateStock(itemId, newStock, reason);
        }
    }

    public void removeStock(String itemId, int quantity, String reason) {
        InventoryItem item = repository.getInventoryItemById(itemId);
        if (item != null) {
            int newStock = Math.max(0, item.getCurrentStock() - quantity);
            updateStock(itemId, newStock, reason);
        }
    }

    public void setStockLimits(String itemId, int minStock, int maxStock) {
        InventoryItem item = repository.getInventoryItemById(itemId);
        if (item != null) {
            item.setMinimumStock(minStock);
            item.setMaximumStock(maxStock);
            item.updateLowStockStatus();
            repository.saveInventoryItem(item);
        }
    }

    public List<InventoryItem> getAllItems() {
        return repository.getInventoryItems();
    }

    public List<InventoryItem> getItemsByCategory(String category) {
        List<InventoryItem> allItems = repository.getInventoryItems();
        List<InventoryItem> categoryItems = new ArrayList<>();
        
        for (InventoryItem item : allItems) {
            if (item.getCategory().equals(category)) {
                categoryItems.add(item);
            }
        }
        
        return categoryItems;
    }

    public List<InventoryItem> getLowStockItems() {
        return repository.getLowStockItems();
    }

    public List<InventoryItem> searchItems(String query) {
        List<InventoryItem> allItems = repository.getInventoryItems();
        List<InventoryItem> results = new ArrayList<>();
        
        String lowerQuery = query.toLowerCase();
        
        for (InventoryItem item : allItems) {
            if (item.getName().toLowerCase().contains(lowerQuery) ||
                item.getCategory().toLowerCase().contains(lowerQuery) ||
                (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerQuery))) {
                results.add(item);
            }
        }
        
        return results;
    }

    public InventoryItem getItemById(String id) {
        return repository.getInventoryItemById(id);
    }

    public void deleteItem(String itemId) {
        List<InventoryItem> items = repository.getInventoryItems();
        items.removeIf(item -> item.getId().equals(itemId));
        // Note: This is a simplified approach. In a real app, you'd have a proper delete method
    }

    public void updateItemDetails(String itemId, String name, String category, String description, 
                                String location, String condition) {
        InventoryItem item = repository.getInventoryItemById(itemId);
        if (item != null) {
            item.setName(name);
            item.setCategory(category);
            item.setDescription(description);
            item.setLocation(location);
            item.setCondition(condition);
            item.setLastUpdated(System.currentTimeMillis());
            
            if (authManager.getCurrentUser() != null) {
                item.setUpdatedBy(authManager.getCurrentUser().getName());
            }
            
            repository.saveInventoryItem(item);
        }
    }

    public double getTotalInventoryValue() {
        List<InventoryItem> items = repository.getInventoryItems();
        double totalValue = 0.0;
        
        for (InventoryItem item : items) {
            totalValue += item.getCurrentStock() * item.getUnitValue();
        }
        
        return totalValue;
    }

    public int getTotalItemsCount() {
        List<InventoryItem> items = repository.getInventoryItems();
        int totalCount = 0;
        
        for (InventoryItem item : items) {
            totalCount += item.getCurrentStock();
        }
        
        return totalCount;
    }

    public List<String> getCategories() {
        List<InventoryItem> items = repository.getInventoryItems();
        List<String> categories = new ArrayList<>();
        
        for (InventoryItem item : items) {
            if (!categories.contains(item.getCategory())) {
                categories.add(item.getCategory());
            }
        }
        
        return categories;
    }
}