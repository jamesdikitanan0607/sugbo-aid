package com.sugboaid.donation.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sugboaid.donation.R;
import com.sugboaid.donation.activities.QRScannerActivity;
import com.sugboaid.donation.adapters.InventoryAdapter;
import com.sugboaid.donation.utils.QRCodeUtils;
import com.sugboaid.donation.viewmodels.InventoryViewModel;
import com.sugboaid.models.InventoryItem;
import com.sugboaid.models.InventoryStatus;

import java.util.List;

/**
 * Fragment for inventory tracking and management system
 * Implements search functionality, filtering, and inventory item display
 */
public class InventoryFragment extends BaseFragment implements InventoryAdapter.OnInventoryItemClickListener {
    
    private InventoryViewModel inventoryViewModel;
    private InventoryAdapter inventoryAdapter;
    
    // UI Components
    private SearchView searchView;
    private RecyclerView recyclerInventory;
    private TextView tvTotalItems;
    private TextView tvCategoriesCount;
    private TextView tvLowStockCount;
    private ChipGroup chipGroupFilters;
    private LinearLayout layoutEmptyState;
    private ImageButton btnRefresh;
    private ImageButton btnScanQR;
    private FloatingActionButton fabAddItem;
    
    // Filter chips
    private Chip chipAll;
    private Chip chipHealthy;
    private Chip chipModerate;
    private Chip chipLow;
    private Chip chipCritical;
    
    // Current filter state
    private InventoryStatus currentStatusFilter = null;
    private String currentSearchQuery = "";
    
    // QR Scanner request code
    private static final int QR_SCANNER_REQUEST_CODE = 1001;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupViewModel();
        setupRecyclerView();
        setupSearchView();
        setupFilterChips();
        setupClickListeners();
        observeViewModel();
        
        // Load initial data
        inventoryViewModel.refreshData();
    }

    private void initializeViews(View view) {
        // Search and filter components
        searchView = view.findViewById(R.id.search_view);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        
        // Summary cards
        tvTotalItems = view.findViewById(R.id.tv_total_items);
        tvCategoriesCount = view.findViewById(R.id.tv_categories_count);
        tvLowStockCount = view.findViewById(R.id.tv_low_stock_count);
        
        // RecyclerView and empty state
        recyclerInventory = view.findViewById(R.id.recycler_inventory);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        
        // Action buttons
        btnRefresh = view.findViewById(R.id.btn_refresh);
        btnScanQR = view.findViewById(R.id.btn_scan_qr);
        fabAddItem = view.findViewById(R.id.fab_add_item);
        
        // Filter chips
        chipAll = view.findViewById(R.id.chip_all);
        chipHealthy = view.findViewById(R.id.chip_healthy);
        chipModerate = view.findViewById(R.id.chip_moderate);
        chipLow = view.findViewById(R.id.chip_low);
        chipCritical = view.findViewById(R.id.chip_critical);
    }

    private void setupViewModel() {
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
    }

    private void setupRecyclerView() {
        inventoryAdapter = new InventoryAdapter(getContext());
        inventoryAdapter.setOnInventoryItemClickListener(this);
        
        recyclerInventory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerInventory.setAdapter(inventoryAdapter);
        recyclerInventory.setHasFixedSize(true);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                performSearch(newText);
                return true;
            }
        });
        
        // Clear search when close button is clicked
        searchView.setOnCloseListener(() -> {
            currentSearchQuery = "";
            performSearch("");
            return false;
        });
    }

    private void setupFilterChips() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // No chip selected, show all items
                currentStatusFilter = null;
                applyFilters();
                return;
            }
            
            int checkedId = checkedIds.get(0);
            
            if (checkedId == R.id.chip_all) {
                currentStatusFilter = null;
            } else if (checkedId == R.id.chip_healthy) {
                currentStatusFilter = InventoryStatus.HEALTHY;
            } else if (checkedId == R.id.chip_moderate) {
                currentStatusFilter = InventoryStatus.MODERATE;
            } else if (checkedId == R.id.chip_low) {
                currentStatusFilter = InventoryStatus.LOW;
            } else if (checkedId == R.id.chip_critical) {
                currentStatusFilter = InventoryStatus.CRITICAL;
            }
            
            applyFilters();
        });
        
        // Set default selection
        chipAll.setChecked(true);
    }

    private void setupClickListeners() {
        btnRefresh.setOnClickListener(v -> {
            inventoryViewModel.refreshData();
            showToast("Inventory refreshed");
        });
        
        btnScanQR.setOnClickListener(v -> {
            startQRScanner();
        });
        
        fabAddItem.setOnClickListener(v -> {
            showAddItemDialog();
        });
    }

    @Override
    protected void initViews(View view) {
        initializeViews(view);
        setupRecyclerView();
        setupViewModel();
    }

    @Override
    protected void setupListeners() {
        setupClickListeners();
    }

    @Override
    protected void refreshData() {
        if (inventoryViewModel != null) {
            inventoryViewModel.refreshData();
        }
    }

    @Override
    protected void observeData() {
        observeViewModel();
    }

    private void observeViewModel() {
        // Observe inventory items
        inventoryViewModel.getInventoryItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                inventoryAdapter.setInventoryItems(items);
                updateEmptyState(items.isEmpty());
                applyFilters(); // Reapply current filters
            }
        });
        
        // Observe total items count
        inventoryViewModel.getTotalItems().observe(getViewLifecycleOwner(), totalItems -> {
            if (totalItems != null) {
                tvTotalItems.setText(String.valueOf(totalItems));
            }
        });
        
        // Observe categories count
        inventoryViewModel.getCategoriesCount().observe(getViewLifecycleOwner(), categoriesCount -> {
            if (categoriesCount != null) {
                tvCategoriesCount.setText(String.valueOf(categoriesCount));
            }
        });
        
        // Observe low stock count
        inventoryViewModel.getLowStockCount().observe(getViewLifecycleOwner(), lowStockCount -> {
            if (lowStockCount != null) {
                tvLowStockCount.setText(String.valueOf(lowStockCount));
            }
        });
        
        // Observe loading state
        inventoryViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Show/hide loading indicator if needed
        });
        
        // Observe error messages
        inventoryViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showToast("Error: " + errorMessage);
                inventoryViewModel.clearError();
            }
        });
    }

    private void performSearch(String query) {
        if (inventoryAdapter != null) {
            inventoryAdapter.filterByQuery(query);
            updateEmptyState(inventoryAdapter.isEmpty());
        }
    }

    private void applyFilters() {
        if (inventoryAdapter != null) {
            // First apply status filter
            inventoryAdapter.filterByStatus(currentStatusFilter);
            
            // Then apply search query if exists
            if (!currentSearchQuery.isEmpty()) {
                inventoryAdapter.filterByQuery(currentSearchQuery);
            }
            
            updateEmptyState(inventoryAdapter.isEmpty());
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerInventory.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerInventory.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_inventory_item, null);
        
        EditText etItemName = dialogView.findViewById(R.id.et_item_name);
        EditText etCapacity = dialogView.findViewById(R.id.et_capacity);
        EditText etUnit = dialogView.findViewById(R.id.et_unit);
        EditText etIconEmoji = dialogView.findViewById(R.id.et_icon_emoji);
        
        builder.setView(dialogView)
                .setTitle("Add Inventory Item")
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etItemName.getText().toString().trim();
                    String capacityStr = etCapacity.getText().toString().trim();
                    String unit = etUnit.getText().toString().trim();
                    String iconEmoji = etIconEmoji.getText().toString().trim();
                    
                    if (validateAddItemInput(name, capacityStr, unit)) {
                        int capacity = Integer.parseInt(capacityStr);
                        InventoryItem newItem = new InventoryItem(name, 0, capacity, unit, iconEmoji, null);
                        inventoryViewModel.addInventoryItem(newItem);
                        showToast("Item added successfully");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean validateAddItemInput(String name, String capacityStr, String unit) {
        if (name.isEmpty()) {
            showToast("Please enter item name");
            return false;
        }
        
        if (capacityStr.isEmpty()) {
            showToast("Please enter capacity");
            return false;
        }
        
        try {
            int capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                showToast("Capacity must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showToast("Please enter a valid capacity number");
            return false;
        }
        
        if (unit.isEmpty()) {
            showToast("Please enter unit");
            return false;
        }
        
        return true;
    }

    // InventoryAdapter.OnInventoryItemClickListener implementation
    @Override
    public void onItemClick(InventoryItem item) {
        showItemDetailsDialog(item);
    }

    @Override
    public void onAddStock(InventoryItem item) {
        showStockAdjustmentDialog(item, true);
    }

    @Override
    public void onRemoveStock(InventoryItem item) {
        showStockAdjustmentDialog(item, false);
    }

    @Override
    public void onItemLongClick(InventoryItem item) {
        showItemOptionsDialog(item);
    }

    @Override
    public void onLowStockAlert(InventoryItem item) {
        showLowStockAlertDialog(item);
    }

    @Override
    public void onRestockNotification(InventoryItem item) {
        showRestockNotificationDialog(item);
    }

    private void showItemDetailsDialog(InventoryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        
        String details = String.format(
                "Name: %s\n" +
                "Stock: %d %s\n" +
                "Capacity: %d %s\n" +
                "Status: %s\n" +
                "Stock Level: %.1f%%",
                item.getName(),
                item.getStock(),
                item.getUnit(),
                item.getCapacity(),
                item.getUnit(),
                item.getStatus().toString(),
                item.getStockPercentage()
        );
        
        builder.setTitle("Item Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showStockAdjustmentDialog(InventoryItem item, boolean isAdding) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_stock_adjustment, null);
        
        TextView tvTitle = dialogView.findViewById(R.id.tv_title);
        TextView tvCurrentStock = dialogView.findViewById(R.id.tv_current_stock);
        EditText etQuantity = dialogView.findViewById(R.id.et_quantity);
        
        String title = isAdding ? "Add Stock" : "Remove Stock";
        tvTitle.setText(title + " - " + item.getName());
        tvCurrentStock.setText("Current Stock: " + item.getFormattedStock());
        
        builder.setView(dialogView)
                .setTitle(title)
                .setPositiveButton(isAdding ? "Add" : "Remove", (dialog, which) -> {
                    String quantityStr = etQuantity.getText().toString().trim();
                    if (!quantityStr.isEmpty()) {
                        try {
                            int quantity = Integer.parseInt(quantityStr);
                            if (quantity > 0) {
                                if (isAdding) {
                                    inventoryViewModel.addStock(item.getName(), quantity);
                                } else {
                                    inventoryViewModel.removeStock(item.getName(), quantity);
                                }
                                showToast("Stock updated successfully");
                            } else {
                                showToast("Please enter a positive quantity");
                            }
                        } catch (NumberFormatException e) {
                            showToast("Please enter a valid number");
                        }
                    } else {
                        showToast("Please enter quantity");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showItemOptionsDialog(InventoryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        
        String[] options = {"Edit Item", "Delete Item", "View History"};
        
        builder.setTitle("Item Options - " + item.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit Item
                            showEditItemDialog(item);
                            break;
                        case 1: // Delete Item
                            showDeleteConfirmationDialog(item);
                            break;
                        case 2: // View History
                            showToast("History feature coming soon");
                            break;
                    }
                })
                .show();
    }

    private void showEditItemDialog(InventoryItem item) {
        // TODO: Implement edit item dialog
        showToast("Edit item feature coming soon");
    }

    private void showDeleteConfirmationDialog(InventoryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        
        builder.setTitle("Delete Item")
                .setMessage("Are you sure you want to delete " + item.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    inventoryViewModel.deleteInventoryItem(item.getName());
                    showToast("Item deleted successfully");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLowStockAlertDialog(InventoryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        
        String message = String.format(
                "%s is running low on stock!\n\n" +
                "Current Stock: %d %s\n" +
                "Capacity: %d %s\n" +
                "Stock Level: %.1f%%\n\n" +
                "Would you like to restock this item?",
                item.getName(),
                item.getStock(),
                item.getUnit(),
                item.getCapacity(),
                item.getUnit(),
                item.getStockPercentage()
        );
        
        builder.setTitle("Low Stock Alert")
                .setMessage(message)
                .setPositiveButton("Restock", (dialog, which) -> {
                    showStockAdjustmentDialog(item, true);
                })
                .setNegativeButton("Later", null)
                .show();
    }

    private void showRestockNotificationDialog(InventoryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        
        String message = String.format(
                "CRITICAL: %s needs immediate restocking!\n\n" +
                "Current Stock: %d %s\n" +
                "This item is critically low and needs urgent attention.",
                item.getName(),
                item.getStock(),
                item.getUnit()
        );
        
        builder.setTitle("Critical Stock Alert")
                .setMessage(message)
                .setPositiveButton("Restock Now", (dialog, which) -> {
                    showStockAdjustmentDialog(item, true);
                })
                .setNegativeButton("Acknowledge", null)
                .show();
    }

    protected void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        if (inventoryViewModel != null) {
            inventoryViewModel.refreshData();
        }
    }

    /**
     * Start QR scanner activity for inventory stock updates
     */
    private void startQRScanner() {
        try {
            Intent intent = new Intent(getActivity(), QRScannerActivity.class);
            startActivityForResult(intent, QR_SCANNER_REQUEST_CODE);
        } catch (Exception e) {
            showToast("Error starting QR scanner: " + e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == QR_SCANNER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                handleQRScanResult(data);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                showToast("QR scan cancelled");
            } else {
                showToast("QR scan failed");
            }
        }
    }

    /**
     * Handle QR scan result and process inventory update
     */
    private void handleQRScanResult(Intent data) {
        try {
            String scanResult = data.getStringExtra(QRScannerActivity.EXTRA_SCAN_RESULT);
            String scanFormat = data.getStringExtra(QRScannerActivity.EXTRA_SCAN_FORMAT);
            
            if (scanResult == null || scanResult.isEmpty()) {
                showToast("Invalid scan result");
                return;
            }
            
            // Parse the QR code data
            QRCodeUtils.InventoryQRData qrData = QRCodeUtils.parseInventoryQR(scanResult);
            
            if (qrData == null) {
                showQRErrorDialog("Invalid QR Code", 
                    "The scanned QR code is not a valid inventory QR code.\n\n" +
                    "Expected format: INVENTORY:ItemName:Quantity:Operation\n" +
                    "Scanned: " + scanResult);
                return;
            }
            
            // Show confirmation dialog before applying the update
            showQRConfirmationDialog(qrData);
            
        } catch (Exception e) {
            showQRErrorDialog("Scan Processing Error", 
                "Error processing QR scan result: " + e.getMessage());
        }
    }

    /**
     * Show confirmation dialog for QR code inventory update
     */
    private void showQRConfirmationDialog(QRCodeUtils.InventoryQRData qrData) {
        String title = "Confirm Inventory Update";
        String message = String.format(
            "QR Code Operation:\n\n" +
            "Item: %s\n" +
            "Quantity: %d\n" +
            "Operation: %s\n\n" +
            "Do you want to proceed with this inventory update?",
            qrData.getItemName(),
            qrData.getQuantity(),
            qrData.getOperation()
        );
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(QRCodeUtils.getOperationIcon(qrData))
                .setPositiveButton("Apply Update", (dialog, which) -> {
                    applyQRInventoryUpdate(qrData);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Apply inventory update from QR code data
     */
    private void applyQRInventoryUpdate(QRCodeUtils.InventoryQRData qrData) {
        try {
            String itemName = qrData.getItemName();
            int quantity = qrData.getQuantity();
            
            if (qrData.isAddOperation()) {
                // Add stock
                inventoryViewModel.addStock(itemName, quantity);
                showToast(String.format("Added %d units to %s", quantity, itemName));
            } else if (qrData.isRemoveOperation()) {
                // Remove stock
                inventoryViewModel.removeStock(itemName, quantity);
                showToast(String.format("Removed %d units from %s", quantity, itemName));
            }
            
            // Refresh the inventory display
            inventoryViewModel.refreshData();
            
            // Show success dialog with details
            showQRSuccessDialog(qrData);
            
        } catch (Exception e) {
            showQRErrorDialog("Update Error", 
                "Error applying inventory update: " + e.getMessage());
        }
    }

    /**
     * Show success dialog after QR inventory update
     */
    private void showQRSuccessDialog(QRCodeUtils.InventoryQRData qrData) {
        String title = "Update Successful";
        String message = String.format(
            "Inventory updated successfully!\n\n" +
            "%s\n\n" +
            "The inventory has been updated and the changes are now reflected in the system.",
            QRCodeUtils.getOperationDescription(qrData)
        );
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Show error dialog for QR scanning issues
     */
    private void showQRErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", null)
                .setNegativeButton("Try Again", (dialog, which) -> {
                    startQRScanner();
                })
                .show();
    }
}