package com.sugboaid.donation.utils;

import com.sugboaid.models.InventoryItem;
import com.sugboaid.models.InventoryStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for testing inventory card functionality
 */
public class InventoryCardTestHelper {
    
    /**
     * Create sample inventory items for testing different status levels
     */
    public static List<InventoryItem> createSampleInventoryItems() {
        List<InventoryItem> items = new ArrayList<>();
        
        // Healthy stock item
        InventoryItem rice = new InventoryItem("Rice", 180, 200, "sacks", "🍚", "#10b981,#059669");
        items.add(rice);
        
        // Moderate stock item
        InventoryItem water = new InventoryItem("Water", 100, 150, "bottles", "💧", "#3b82f6,#2563eb");
        items.add(water);
        
        // Low stock item
        InventoryItem medicine = new InventoryItem("Medicine", 30, 100, "boxes", "💊", "#f59e0b,#d97706");
        items.add(medicine);
        
        // Critical stock item
        InventoryItem clothes = new InventoryItem("Clothes", 15, 180, "pieces", "👕", "#8b5cf6,#7c3aed");
        items.add(clothes);
        
        return items;
    }
    
    /**
     * Test status badge functionality
     */
    public static boolean testStatusBadges() {
        List<InventoryItem> items = createSampleInventoryItems();
        
        // Verify status calculations
        return items.get(0).getStatus() == InventoryStatus.HEALTHY &&
               items.get(1).getStatus() == InventoryStatus.MODERATE &&
               items.get(2).getStatus() == InventoryStatus.LOW &&
               items.get(3).getStatus() == InventoryStatus.CRITICAL;
    }
    
    /**
     * Test progress indicator calculations
     */
    public static boolean testProgressIndicators() {
        List<InventoryItem> items = createSampleInventoryItems();
        
        // Test percentage calculations
        double ricePercentage = items.get(0).getStockPercentage(); // Should be 90%
        double waterPercentage = items.get(1).getStockPercentage(); // Should be ~66.7%
        double medicinePercentage = items.get(2).getStockPercentage(); // Should be 30%
        double clothesPercentage = items.get(3).getStockPercentage(); // Should be ~8.3%
        
        return ricePercentage == 90.0 &&
               Math.abs(waterPercentage - 66.67) < 0.1 &&
               medicinePercentage == 30.0 &&
               Math.abs(clothesPercentage - 8.33) < 0.1;
    }
    
    /**
     * Test low stock detection
     */
    public static boolean testLowStockDetection() {
        List<InventoryItem> items = createSampleInventoryItems();
        
        // Only medicine and clothes should be low stock
        return !items.get(0).isLowStock() &&  // Rice - healthy
               !items.get(1).isLowStock() &&  // Water - moderate
               items.get(2).isLowStock() &&   // Medicine - low
               items.get(3).isLowStock();     // Clothes - critical
    }
    
    /**
     * Test critical stock detection
     */
    public static boolean testCriticalStockDetection() {
        List<InventoryItem> items = createSampleInventoryItems();
        
        // Only clothes should be critical stock
        return !items.get(0).isCriticalStock() &&  // Rice
               !items.get(1).isCriticalStock() &&  // Water
               !items.get(2).isCriticalStock() &&  // Medicine
               items.get(3).isCriticalStock();     // Clothes
    }
    
    /**
     * Run all tests
     */
    public static boolean runAllTests() {
        return testStatusBadges() &&
               testProgressIndicators() &&
               testLowStockDetection() &&
               testCriticalStockDetection();
    }
    
    /**
     * Get test results summary
     */
    public static String getTestResultsSummary() {
        StringBuilder results = new StringBuilder();
        results.append("Inventory Card Test Results:\n");
        results.append("Status Badges: ").append(testStatusBadges() ? "PASS" : "FAIL").append("\n");
        results.append("Progress Indicators: ").append(testProgressIndicators() ? "PASS" : "FAIL").append("\n");
        results.append("Low Stock Detection: ").append(testLowStockDetection() ? "PASS" : "FAIL").append("\n");
        results.append("Critical Stock Detection: ").append(testCriticalStockDetection() ? "PASS" : "FAIL").append("\n");
        results.append("Overall: ").append(runAllTests() ? "ALL TESTS PASS" : "SOME TESTS FAILED");
        
        return results.toString();
    }
}