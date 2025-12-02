package com.sugboaid.app.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.sugboaid.app.data.model.InventoryItem;
import com.sugboaid.app.manager.InventoryManager;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InventoryViewModel extends AndroidViewModel {

    private final InventoryManager inventoryManager;
    private final ExecutorService executorService;

    private final MutableLiveData<List<InventoryItem>> inventoryItems = new MutableLiveData<>();
    private final MutableLiveData<InventoryStats> inventoryStats = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        inventoryManager = InventoryManager.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
        loadInventory();
    }

    public LiveData<List<InventoryItem>> getInventoryItems() {
        return inventoryItems;
    }

    public LiveData<InventoryStats> getInventoryStats() {
        return inventoryStats;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadInventory() {
        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                // Simulate network/db delay
                Thread.sleep(500);

                List<InventoryItem> items = inventoryManager.getAllItems();
                InventoryStats stats = new InventoryStats(
                        inventoryManager.getTotalItemsCount(),
                        inventoryManager.getCategories().size(),
                        inventoryManager.getLowStockItems().size());

                // Use postValue for background thread updates to avoid "Cannot invoke setValue
                // on a background thread"
                inventoryItems.postValue(items);
                inventoryStats.postValue(stats);
                isLoading.postValue(false);
            } catch (Exception e) {
                error.postValue("Failed to load inventory: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void searchInventory(String query) {
        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                List<InventoryItem> results = inventoryManager.searchItems(query);
                inventoryItems.postValue(results);
                isLoading.postValue(false);
            } catch (Exception e) {
                error.postValue("Search failed: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void filterByCategory(String category) {
        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                List<InventoryItem> results;
                if (category.equals("All Items")) {
                    results = inventoryManager.getAllItems();
                } else if (category.equals("Low Stock")) {
                    results = inventoryManager.getLowStockItems();
                } else {
                    results = inventoryManager.getItemsByCategory(category);
                }
                inventoryItems.postValue(results);
                isLoading.postValue(false);
            } catch (Exception e) {
                error.postValue("Filter failed: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    public static class InventoryStats {
        public final int totalItems;
        public final int categories;
        public final int lowStock;

        public InventoryStats(int totalItems, int categories, int lowStock) {
            this.totalItems = totalItems;
            this.categories = categories;
            this.lowStock = lowStock;
        }
    }
}
