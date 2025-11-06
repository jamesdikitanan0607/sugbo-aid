package com.sugboaid.donation.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for QRCodeUtils class
 * Tests QR code parsing and validation functionality
 */
public class QRCodeUtilsTest {

    @Test
    public void testValidInventoryQRParsing() {
        // Test valid ADD operation
        String validAddQR = "INVENTORY:Rice:50:ADD";
        QRCodeUtils.InventoryQRData result = QRCodeUtils.parseInventoryQR(validAddQR);
        
        assertNotNull("Should parse valid ADD QR code", result);
        assertEquals("Item name should match", "Rice", result.getItemName());
        assertEquals("Quantity should match", 50, result.getQuantity());
        assertEquals("Operation should match", "ADD", result.getOperation());
        assertTrue("Should be ADD operation", result.isAddOperation());
        assertFalse("Should not be REMOVE operation", result.isRemoveOperation());
    }

    @Test
    public void testValidRemoveQRParsing() {
        // Test valid REMOVE operation
        String validRemoveQR = "INVENTORY:Water:25:REMOVE";
        QRCodeUtils.InventoryQRData result = QRCodeUtils.parseInventoryQR(validRemoveQR);
        
        assertNotNull("Should parse valid REMOVE QR code", result);
        assertEquals("Item name should match", "Water", result.getItemName());
        assertEquals("Quantity should match", 25, result.getQuantity());
        assertEquals("Operation should match", "REMOVE", result.getOperation());
        assertFalse("Should not be ADD operation", result.isAddOperation());
        assertTrue("Should be REMOVE operation", result.isRemoveOperation());
    }

    @Test
    public void testInvalidQRFormats() {
        // Test null input
        assertNull("Should return null for null input", 
                QRCodeUtils.parseInventoryQR(null));
        
        // Test empty input
        assertNull("Should return null for empty input", 
                QRCodeUtils.parseInventoryQR(""));
        
        // Test wrong prefix
        assertNull("Should return null for wrong prefix", 
                QRCodeUtils.parseInventoryQR("WRONG:Rice:50:ADD"));
        
        // Test insufficient parts
        assertNull("Should return null for insufficient parts", 
                QRCodeUtils.parseInventoryQR("INVENTORY:Rice:50"));
        
        // Test too many parts
        assertNull("Should return null for too many parts", 
                QRCodeUtils.parseInventoryQR("INVENTORY:Rice:50:ADD:EXTRA"));
        
        // Test invalid quantity
        assertNull("Should return null for invalid quantity", 
                QRCodeUtils.parseInventoryQR("INVENTORY:Rice:abc:ADD"));
        
        // Test zero quantity
        assertNull("Should return null for zero quantity", 
                QRCodeUtils.parseInventoryQR("INVENTORY:Rice:0:ADD"));
        
        // Test negative quantity
        assertNull("Should return null for negative quantity", 
                QRCodeUtils.parseInventoryQR("INVENTORY:Rice:-5:ADD"));
        
        // Test invalid operation
        assertNull("Should return null for invalid operation", 
                QRCodeUtils.parseInventoryQR("INVENTORY:Rice:50:INVALID"));
        
        // Test empty item name
        assertNull("Should return null for empty item name", 
                QRCodeUtils.parseInventoryQR("INVENTORY::50:ADD"));
    }

    @Test
    public void testQRValidation() {
        // Test valid QR codes
        assertTrue("Should validate correct ADD QR", 
                QRCodeUtils.isValidInventoryQR("INVENTORY:Rice:50:ADD"));
        assertTrue("Should validate correct REMOVE QR", 
                QRCodeUtils.isValidInventoryQR("INVENTORY:Water:25:REMOVE"));
        
        // Test invalid QR codes
        assertFalse("Should not validate null QR", 
                QRCodeUtils.isValidInventoryQR(null));
        assertFalse("Should not validate empty QR", 
                QRCodeUtils.isValidInventoryQR(""));
        assertFalse("Should not validate wrong format", 
                QRCodeUtils.isValidInventoryQR("WRONG:Rice:50:ADD"));
    }

    @Test
    public void testQRGeneration() {
        // Test ADD operation generation
        String addQR = QRCodeUtils.generateInventoryQR("Rice", 50, "ADD");
        assertEquals("Should generate correct ADD QR", "INVENTORY:Rice:50:ADD", addQR);
        
        // Test REMOVE operation generation
        String removeQR = QRCodeUtils.generateInventoryQR("Water", 25, "REMOVE");
        assertEquals("Should generate correct REMOVE QR", "INVENTORY:Water:25:REMOVE", removeQR);
        
        // Test with whitespace trimming
        String trimmedQR = QRCodeUtils.generateInventoryQR("  Rice  ", 50, "add");
        assertEquals("Should trim whitespace and uppercase operation", "INVENTORY:Rice:50:ADD", trimmedQR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQRGenerationWithNullItemName() {
        QRCodeUtils.generateInventoryQR(null, 50, "ADD");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQRGenerationWithEmptyItemName() {
        QRCodeUtils.generateInventoryQR("", 50, "ADD");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQRGenerationWithZeroQuantity() {
        QRCodeUtils.generateInventoryQR("Rice", 0, "ADD");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQRGenerationWithNegativeQuantity() {
        QRCodeUtils.generateInventoryQR("Rice", -5, "ADD");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQRGenerationWithInvalidOperation() {
        QRCodeUtils.generateInventoryQR("Rice", 50, "INVALID");
    }

    @Test
    public void testOperationDescription() {
        // Test ADD operation description
        QRCodeUtils.InventoryQRData addData = QRCodeUtils.parseInventoryQR("INVENTORY:Rice:50:ADD");
        String addDescription = QRCodeUtils.getOperationDescription(addData);
        assertEquals("Should generate correct ADD description", "Add 50 units of Rice", addDescription);
        
        // Test REMOVE operation description
        QRCodeUtils.InventoryQRData removeData = QRCodeUtils.parseInventoryQR("INVENTORY:Water:25:REMOVE");
        String removeDescription = QRCodeUtils.getOperationDescription(removeData);
        assertEquals("Should generate correct REMOVE description", "Remove 25 units of Water", removeDescription);
        
        // Test null data
        String nullDescription = QRCodeUtils.getOperationDescription(null);
        assertEquals("Should handle null data", "Invalid QR code", nullDescription);
    }

    @Test
    public void testOperationIcon() {
        // Test ADD operation icon
        QRCodeUtils.InventoryQRData addData = QRCodeUtils.parseInventoryQR("INVENTORY:Rice:50:ADD");
        int addIcon = QRCodeUtils.getOperationIcon(addData);
        assertEquals("Should return ADD icon", android.R.drawable.ic_input_add, addIcon);
        
        // Test REMOVE operation icon
        QRCodeUtils.InventoryQRData removeData = QRCodeUtils.parseInventoryQR("INVENTORY:Water:25:REMOVE");
        int removeIcon = QRCodeUtils.getOperationIcon(removeData);
        assertEquals("Should return REMOVE icon", android.R.drawable.ic_delete, removeIcon);
        
        // Test null data
        int nullIcon = QRCodeUtils.getOperationIcon(null);
        assertEquals("Should return alert icon for null", android.R.drawable.ic_dialog_alert, nullIcon);
    }

    @Test
    public void testCaseInsensitiveOperations() {
        // Test lowercase operations
        QRCodeUtils.InventoryQRData lowerAdd = QRCodeUtils.parseInventoryQR("INVENTORY:Rice:50:add");
        assertNotNull("Should parse lowercase ADD", lowerAdd);
        assertEquals("Should convert to uppercase", "ADD", lowerAdd.getOperation());
        
        QRCodeUtils.InventoryQRData lowerRemove = QRCodeUtils.parseInventoryQR("INVENTORY:Water:25:remove");
        assertNotNull("Should parse lowercase REMOVE", lowerRemove);
        assertEquals("Should convert to uppercase", "REMOVE", lowerRemove.getOperation());
        
        // Test mixed case operations
        QRCodeUtils.InventoryQRData mixedAdd = QRCodeUtils.parseInventoryQR("INVENTORY:Rice:50:Add");
        assertNotNull("Should parse mixed case ADD", mixedAdd);
        assertEquals("Should convert to uppercase", "ADD", mixedAdd.getOperation());
    }
}