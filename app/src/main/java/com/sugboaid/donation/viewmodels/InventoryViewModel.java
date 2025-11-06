package com.sugboaid.donation.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.sugboaid.models.InventoryItem;
import com.sugboaid.models.InventoryStatus;
import com.sugboaid.repositories.InventoryRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for managing inventory data and business logic
 */
public class InventoryViewModel extends AndroidViewModel {
    
    private InventoryRepository inventoryRepository;
    private ExecutorService executorService;
    
    // LiveData for UI observations
    private LiveData<List<InventoryItem>> inventoryItems;
    private LiveData<Integer> totalItems;
    private LiveData<Integer> lowStockCount;
    private LiveData<List<InventoryItem>> lowStockItems;
    
    // Filter and search state
    private MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private MutableLiveData<InventoryStatus> statusFilter = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    // Statistics
    private MutableLiveData<Integer> categoriesCount = new MutableLiveData<>(0);
    private MutableLiveData<Double> overallStockPercentage = new MutableLiveData<>(0.0);

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        
        inventoryRepository = InventoryRepository.getInstance(application);
        executorService = Executors.newFixedThreadPool(2);
        
        // Initialize LiveData from repository
        inventoryItems = inventoryRepository.getInventoryItems();
        totalItems = inventoryRepository.getTotalItems();
        lowStockCount = inventoryRepository.getLowStockCount();
        lowStockItems = inventoryRepository.getLowStockItems();
        
        // Calculate categories count when inventory changes
        Transformations.map(inventoryItems, items -> {
            categoriesCount.setValue(items != null ? items.size() : 0);
            return items;
        });
        
        // Calculate overall stock percentage
        Transformations.map(inventoryItems, items -> {
            if (items != null && !items.isEmpty()) {
                double percentage = inventoryRepository.calculateOverallStockPercentage();
                overallStockPercentage.setValue(percentage);
            } else {
                overallStockPercentage.setValue(0.0);
            }
            return items;
        });
    }

    // Getters for LiveData
    public LiveData<List<InventoryItem>> getInventoryItems() {
        return inventoryItems;
    }

    public LiveData<Integer> getTotalItems() {
        return totalItems;
    }

    public LiveData<Integer> getLowStockCount() {
        return lowStockCount;
    }

    public LiveData<List<InventoryItem>> getLowStockItems() {
        return lowStockItems;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public LiveData<InventoryStatus> getStatusFilter() {
        return statusFilter;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getCategoriesCount() {
        return categoriesCount;
    }

    public LiveData<Double> getOverallStockPercentage() {
        return overallStockPercentage;
    }

    // Search and filter operations
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setStatusFilter(InventoryStatus status) {
        statusFilter.setValue(status);
    }

    public void clearFilters() {
        searchQuery.setValue("");
        statusFilter.setValue(null);
    }

    public LiveData<List<InventoryItem>> getFilteredInventory() {
        return Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                InventoryStatus status = statusFilter.getValue();
                if (status != null) {
                    return inventoryRepository.getInventoryByStatus(status);
                } else {
                    return inventoryItems;
                }
            } else {
                return inventoryRepository.searchInventory(query);
            }
        });
    }

    // Inventory management operations
    public void addInventoryItem(InventoryItem item) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                inventoryRepository.addInventoryItem(item);
                errorMessage.postValue(null);
            } catch (Exception e) {
                errorMessage.postValue("Failed to add inventory item: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void updateInventoryItem(InventoryItem item) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                inventoryRepository.updateInventoryItem(item);
                errorMessage.postValue(null);
            } catch (Exception e) {
                errorMessage.postValue("Failed to update inventory item: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void deleteInventoryItem(String itemName) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                inventoryRepository.deleteInventoryItem(itemName);
                errorMessage.postValue(null);
            } catch (Exception e) {
                errorMessage.postValue("Failed to delete inventory item: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Stock management operations
    public void addStock(String itemName, int quantity) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                inventoryRepository.addStock(itemName, quantity);
                errorMessage.postValue(null);
            } catch (Exception e) {
                errorMessage.postValue("Failed to add stock: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void removeStock(String itemName, int quantity) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                inventoryRepository.removeStock(itemName, quantity);
                errorMessage.postValue(null);
            } catch (Exception e) {
                errorMessage.postValue("Failed to remove stock: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void updateStock(String itemName, int newStock) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                inventoryRepository.updateStock(itemName, newStock);
                errorMessage.postValue(null);
            } catch (Exception e) {
                errorMessage.postValue("Failed to update stock: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Quick stock adjustment methods
    public void incrementStock(String itemName) {
        addStock(itemName, 1);
    }

    public void decrementStock(String itemName) {
        removeStock(itemName, 1);
    }

    public void incrementStockBy(String itemName, int amount) {
        if (amount > 0) {
            addStock(itemName, amount);
        }
    }

    public void decrementStockBy(String itemName, int amount) {
        if (amount > 0) {
            removeStock(itemName, amount);
        }
    }

    // Data refresh and synchronization
    public void refreshData() {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                inventoryRepository.refreshData();
                errorMessage.postValue(null);
            } catch (Exception e) {
                errorMessage.postValue("Failed to refresh data: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Statistics and analytics
    public LiveData<List<InventoryItem>> getCriticalStockItems() {
        return Transformations.map(inventoryItems, items -> {
            return inventoryRepository.getCriticalStockItems();
        });
    }

    public int calculateTotalStock() {
        return inventoryRepository.calculateTotalStock();
    }

    public int calculateTotalCapacity() {
        return inventoryRepository.calculateTotalCapacity();
    }

    // Validation and utility methods
    public boolean validateInventoryItem(InventoryItem item) {
        if (item == null) {
            errorMessage.setValue("Inventory item cannot be null");
            return false;
        }
        
        if (!item.isValid()) {
            errorMessage.setValue("Invalid inventory item data");
            return false;
        }
        
        return true;
    }

    public InventoryItem getInventoryItemByName(String itemName) {
        return inventoryRepository.getInventoryItemByName(itemName);
    }

    // Data management operations
    public void resetToDefaultInventory() {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                inventoryRepository.resetToDefaultInventory();
                errorMessage.postValue(null);
            } catch (Exception e) {
                errorMessage.postValue("Failed to reset inventory: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void clearAllInventory() {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                inventoryRepository.clearAllInventory();
                errorMessage.postValue(null);
            } catch (Exception e) {
                errorMessage.postValue("Failed to clear inventory: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void validateAndCleanupInventory() {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                inventoryRepository.cleanupInvalidItems();
                errorMessage.postValue(null);
            } catch (Exception e) {
                errorMessage.postValue("Failed to cleanup inventory: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Error handling
    public void clearError() {
        errorMessage.setValue(null);
    }

    // Lifecycle management
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    // Batch operations
    public void batchUpdateStock(List<InventoryItem> items) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                for (InventoryItem item : items) {
                    inventoryRepository.updateInventoryItem(item);
                }
                errorMessage.postValue(null);
            } catch (Exception e) {
                errorMessage.postValue("Failed to batch update stock: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // QR Code integration support
    public void processQRCodeData(String qrData) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                
                // Parse QR code data (assuming format: "itemName:quantity")
                String[] parts = qrData.split(":");
                if (parts.length == 2) {
                    String itemName = parts[0].trim();
                    int quantity = Integer.parseInt(parts[1].trim());
                    
                    InventoryItem item = inventoryRepository.getInventoryItemByName(itemName);
                    if (item != null) {
                        inventoryRepository.addStock(itemName, quantity);
                        errorMessage.postValue(null);
                    } else {
                        errorMessage.postValue("Inventory item not found: " + itemName);
                    }
                } else {
                    errorMessage.postValue("Invalid QR code format");
                }
            } catch (NumberFormatException e) {
                errorMessage.postValue("Invalid quantity in QR code");
            } catch (Exception e) {
                errorMessage.postValue("Failed to process QR code: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
}