package com.sugboaid.donation.utils;

/**
 * Utility class for QR code parsing and validation
 * Handles inventory QR code format and data extraction
 */
public class QRCodeUtils {
    
    public static final String QR_PREFIX = "INVENTORY";
    public static final String QR_SEPARATOR = ":";
    public static final String OPERATION_ADD = "ADD";
    public static final String OPERATION_REMOVE = "REMOVE";
    
    /**
     * Represents parsed QR code data for inventory operations
     */
    public static class InventoryQRData {
        private final String itemName;
        private final int quantity;
        private final String operation;
        
        public InventoryQRData(String itemName, int quantity, String operation) {
            this.itemName = itemName;
            this.quantity = quantity;
            this.operation = operation;
        }
        
        public String getItemName() {
            return itemName;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public String getOperation() {
            return operation;
        }
        
        public boolean isAddOperation() {
            return OPERATION_ADD.equals(operation);
        }
        
        public boolean isRemoveOperation() {
            return OPERATION_REMOVE.equals(operation);
        }
        
        @Override
        public String toString() {
            return String.format("InventoryQRData{itemName='%s', quantity=%d, operation='%s'}", 
                    itemName, quantity, operation);
        }
    }
    
    /**
     * Parse QR code content into InventoryQRData
     * Expected format: "INVENTORY:item_name:quantity:operation"
     * 
     * @param qrContent The scanned QR code content
     * @return InventoryQRData object if valid, null if invalid
     */
    public static InventoryQRData parseInventoryQR(String qrContent) {
        if (qrContent == null || qrContent.trim().isEmpty()) {
            return null;
        }
        
        try {
            String[] parts = qrContent.split(QR_SEPARATOR);
            
            if (parts.length != 4) {
                return null;
            }
            
            String prefix = parts[0].trim();
            String itemName = parts[1].trim();
            String quantityStr = parts[2].trim();
            String operation = parts[3].trim().toUpperCase();
            
            // Validate prefix
            if (!QR_PREFIX.equals(prefix)) {
                return null;
            }
            
            // Validate item name
            if (itemName.isEmpty()) {
                return null;
            }
            
            // Validate and parse quantity
            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    return null;
                }
            } catch (NumberFormatException e) {
                return null;
            }
            
            // Validate operation
            if (!OPERATION_ADD.equals(operation) && !OPERATION_REMOVE.equals(operation)) {
                return null;
            }
            
            return new InventoryQRData(itemName, quantity, operation);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Validate if QR code content is a valid inventory QR code
     * 
     * @param qrContent The scanned QR code content
     * @return true if valid inventory QR code, false otherwise
     */
    public static boolean isValidInventoryQR(String qrContent) {
        return parseInventoryQR(qrContent) != null;
    }
    
    /**
     * Generate QR code content for inventory operations
     * 
     * @param itemName Name of the inventory item
     * @param quantity Quantity to add or remove
     * @param operation Operation type (ADD or REMOVE)
     * @return QR code content string
     */
    public static String generateInventoryQR(String itemName, int quantity, String operation) {
        if (itemName == null || itemName.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        
        if (!OPERATION_ADD.equals(operation) && !OPERATION_REMOVE.equals(operation)) {
            throw new IllegalArgumentException("Operation must be ADD or REMOVE");
        }
        
        return String.format("%s%s%s%s%d%s%s", 
                QR_PREFIX, QR_SEPARATOR, 
                itemName.trim(), QR_SEPARATOR, 
                quantity, QR_SEPARATOR, 
                operation.toUpperCase());
    }
    
    /**
     * Get user-friendly description of the QR operation
     * 
     * @param qrData Parsed QR data
     * @return Human-readable description
     */
    public static String getOperationDescription(InventoryQRData qrData) {
        if (qrData == null) {
            return "Invalid QR code";
        }
        
        String action = qrData.isAddOperation() ? "Add" : "Remove";
        return String.format("%s %d units of %s", action, qrData.getQuantity(), qrData.getItemName());
    }
    
    /**
     * Get operation icon resource based on operation type
     * 
     * @param qrData Parsed QR data
     * @return Resource ID for operation icon
     */
    public static int getOperationIcon(InventoryQRData qrData) {
        if (qrData == null) {
            return android.R.drawable.ic_dialog_alert;
        }
        
        return qrData.isAddOperation() ? 
                android.R.drawable.ic_input_add : 
                android.R.drawable.ic_delete;
    }
}