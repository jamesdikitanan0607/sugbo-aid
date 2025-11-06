package com.sugboaid.donation.validation;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.sugboaid.models.Donation;
import com.sugboaid.models.DonationType;
import com.sugboaid.models.InventoryItem;
import com.sugboaid.models.InventoryStatus;
import com.sugboaid.models.AppNotification;
import com.sugboaid.models.NotificationType;
import com.sugboaid.repositories.DonationRepository;
import com.sugboaid.repositories.InventoryRepository;
import com.sugboaid.repositories.PreferencesRepository;
import com.sugboaid.utils.SharedPreferencesHelper;
import com.sugboaid.utils.OfflineQueueManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Data Persistence and Offline Functionality Validation Test
 * 
 * This test validates that the Android app correctly handles:
 * - SharedPreferences data storage and retrieval
 * - Offline donation recording and queuing
 * - Data synchronization when connectivity is restored
 * - User preferences persistence
 * - Repository pattern implementation
 * - Data integrity and validation
 * 
 * Validates Requirements 4.1-4.5 (Offline functionality and data synchronization)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DataPersistenceValidationTest {

    private Context context;
    private SharedPreferencesHelper prefsHelper;
    private DonationRepository donationRepository;
    private InventoryRepository inventoryRepository;
    private PreferencesRepository preferencesRepository;
    private OfflineQueueManager offlineQueueManager;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        prefsHelper = SharedPreferencesHelper.getInstance(context);
        donationRepository = DonationRepository.getInstance(context);
        inventoryRepository = InventoryRepository.getInstance(context);
        preferencesRepository = PreferencesRepository.getInstance(context);
        offlineQueueManager = OfflineQueueManager.getInstance(context);
        
        // Clear any existing test data
        clearAllTestData();
    }

    @After
    public void tearDown() {
        // Clean up test data
        clearAllTestData();
    }

    private void clearAllTestData() {
        // Clear SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("SugboAidPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        
        // Clear repositories
        donationRepository.clearAllDonations();
        inventoryRepository.clearAllItems();
        offlineQueueManager.clearAllActions();
    }

    /**
     * Test SharedPreferences data persistence (Requirement 4.1)
     * Validates that user preferences and app state are properly stored
     */
    @Test
    public void testSharedPreferencesDataPersistence() {
        
        // Test dark mode preference persistence
        prefsHelper.saveDarkModePreference(true);
        assertTrue("Dark mode preference should be saved and retrieved correctly",
            prefsHelper.getDarkModePreference());
        
        prefsHelper.saveDarkModePreference(false);
        assertFalse("Dark mode preference should be updated correctly",
            prefsHelper.getDarkModePreference());
        
        // Test user role persistence
        String testRole = "TestDonor";
        prefsHelper.saveUserRole(testRole);
        assertEquals("User role should be saved and retrieved correctly",
            testRole, prefsHelper.getUserRole());
        
        // Test offline mode persistence
        prefsHelper.saveOfflineMode(true);
        assertTrue("Offline mode should be saved correctly",
            prefsHelper.getOfflineMode());
        
        prefsHelper.saveOfflineMode(false);
        assertFalse("Offline mode should be updated correctly",
            prefsHelper.getOfflineMode());
        
        // Test first launch flag
        prefsHelper.setFirstLaunch(false);
        assertFalse("First launch flag should be saved correctly",
            prefsHelper.isFirstLaunch());
        
        // Test app version persistence
        String testVersion = "1.2.3";
        prefsHelper.saveAppVersion(testVersion);
        assertEquals("App version should be saved correctly",
            testVersion, prefsHelper.getAppVersion());
    }

    /**
     * Test donation data persistence (Requirement 4.3)
     * Validates that donations are properly stored and retrieved
     */
    @Test
    public void testDonationDataPersistence() {
        
        // Create test donations
        Donation cashDonation = new Donation(
            "cash-001",
            "John Doe",
            DonationType.CASH,
            5000.0,
            "Cash donation for relief operations",
            System.currentTimeMillis(),
            "General Relief",
            true
        );
        
        Donation goodsDonation = new Donation(
            "goods-001",
            "Jane Smith",
            DonationType.GOODS,
            0.0,
            "Rice: 50 packs, Water: 100 bottles",
            System.currentTimeMillis(),
            "Food Relief",
            false
        );
        
        // Save donations
        donationRepository.addDonation(cashDonation);
        donationRepository.addDonation(goodsDonation);
        
        // Retrieve and verify donations
        List<Donation> savedDonations = donationRepository.getAllDonations();
        assertNotNull("Saved donations should not be null", savedDonations);
        assertEquals("Should have 2 saved donations", 2, savedDonations.size());
        
        // Verify cash donation
        Donation retrievedCashDonation = findDonationById(savedDonations, "cash-001");
        assertNotNull("Cash donation should be found", retrievedCashDonation);
        assertEquals("Cash donation donor name should match", 
            cashDonation.getDonorName(), retrievedCashDonation.getDonorName());
        assertEquals("Cash donation amount should match", 
            cashDonation.getAmount(), retrievedCashDonation.getAmount(), 0.01);
        assertEquals("Cash donation type should match", 
            cashDonation.getType(), retrievedCashDonation.getType());
        assertTrue("Cash donation should be verified", 
            retrievedCashDonation.isVerified());
        
        // Verify goods donation
        Donation retrievedGoodsDonation = findDonationById(savedDonations, "goods-001");
        assertNotNull("Goods donation should be found", retrievedGoodsDonation);
        assertEquals("Goods donation donor name should match", 
            goodsDonation.getDonorName(), retrievedGoodsDonation.getDonorName());
        assertEquals("Goods donation type should match", 
            goodsDonation.getType(), retrievedGoodsDonation.getType());
        assertFalse("Goods donation should not be verified initially", 
            retrievedGoodsDonation.isVerified());
        
        // Test donation update
        retrievedGoodsDonation.setVerified(true);
        donationRepository.updateDonation(retrievedGoodsDonation);
        
        List<Donation> updatedDonations = donationRepository.getAllDonations();
        Donation updatedGoodsDonation = findDonationById(updatedDonations, "goods-001");
        assertTrue("Goods donation should be verified after update", 
            updatedGoodsDonation.isVerified());
    }

    /**
     * Test inventory data persistence (Requirement 4.3)
     * Validates that inventory items are properly stored and managed
     */
    @Test
    public void testInventoryDataPersistence() {
        
        // Create test inventory items
        InventoryItem riceItem = new InventoryItem(
            "Rice",
            150,
            200,
            "packs",
            InventoryStatus.HEALTHY,
            "🍚",
            "#10b981"
        );
        
        InventoryItem waterItem = new InventoryItem(
            "Water",
            25,
            100,
            "bottles",
            InventoryStatus.LOW,
            "💧",
            "#3b82f6"
        );
        
        InventoryItem medicineItem = new InventoryItem(
            "Medicine",
            5,
            50,
            "boxes",
            InventoryStatus.CRITICAL,
            "💊",
            "#ef4444"
        );
        
        // Save inventory items
        inventoryRepository.addItem(riceItem);
        inventoryRepository.addItem(waterItem);
        inventoryRepository.addItem(medicineItem);
        
        // Retrieve and verify items
        List<InventoryItem> savedItems = inventoryRepository.getAllItems();
        assertNotNull("Saved inventory items should not be null", savedItems);
        assertEquals("Should have 3 saved inventory items", 3, savedItems.size());
        
        // Verify rice item
        InventoryItem retrievedRice = findInventoryItemByName(savedItems, "Rice");
        assertNotNull("Rice item should be found", retrievedRice);
        assertEquals("Rice stock should match", riceItem.getStock(), retrievedRice.getStock());
        assertEquals("Rice capacity should match", riceItem.getCapacity(), retrievedRice.getCapacity());
        assertEquals("Rice status should match", riceItem.getStatus(), retrievedRice.getStatus());
        
        // Test stock update
        retrievedRice.setStock(180);
        retrievedRice.setStatus(InventoryStatus.HEALTHY);
        inventoryRepository.updateItem(retrievedRice);
        
        List<InventoryItem> updatedItems = inventoryRepository.getAllItems();
        InventoryItem updatedRice = findInventoryItemByName(updatedItems, "Rice");
        assertEquals("Rice stock should be updated", 180, updatedRice.getStock());
        assertEquals("Rice status should be updated", InventoryStatus.HEALTHY, updatedRice.getStatus());
        
        // Test low stock detection
        List<InventoryItem> lowStockItems = inventoryRepository.getLowStockItems();
        assertTrue("Should have low stock items", lowStockItems.size() >= 2); // Water and Medicine
        
        boolean hasWater = lowStockItems.stream().anyMatch(item -> "Water".equals(item.getName()));
        boolean hasMedicine = lowStockItems.stream().anyMatch(item -> "Medicine".equals(item.getName()));
        
        assertTrue("Water should be in low stock items", hasWater);
        assertTrue("Medicine should be in low stock items", hasMedicine);
    }

    /**
     * Test offline functionality (Requirement 4.2, 4.4)
     * Validates offline donation recording and queuing
     */
    @Test
    public void testOfflineFunctionality() {
        
        // Simulate offline mode
        prefsHelper.saveOfflineMode(true);
        assertTrue("Should be in offline mode", prefsHelper.getOfflineMode());
        
        // Create offline donations
        Donation offlineDonation1 = new Donation(
            "offline-001",
            "Offline Donor 1",
            DonationType.CASH,
            2500.0,
            "Offline cash donation",
            System.currentTimeMillis(),
            "Emergency Relief",
            false
        );
        
        Donation offlineDonation2 = new Donation(
            "offline-002",
            "Offline Donor 2",
            DonationType.GOODS,
            0.0,
            "Clothes: 20 pieces, Medicine: 10 boxes",
            System.currentTimeMillis(),
            "Medical Relief",
            false
        );
        
        // Queue offline donations
        offlineQueueManager.queueDonation(offlineDonation1);
        offlineQueueManager.queueDonation(offlineDonation2);
        
        // Verify donations are queued
        assertTrue("Should have pending actions", offlineQueueManager.hasPendingActions());
        assertEquals("Should have 2 pending actions", 2, offlineQueueManager.getPendingActionCount());
        
        // Verify donations are also stored locally
        donationRepository.addDonation(offlineDonation1);
        donationRepository.addDonation(offlineDonation2);
        
        List<Donation> localDonations = donationRepository.getAllDonations();
        assertEquals("Should have 2 local donations", 2, localDonations.size());
        
        // Simulate network restoration
        prefsHelper.saveOfflineMode(false);
        assertFalse("Should be back online", prefsHelper.getOfflineMode());
        
        // Test sync process
        testDataSynchronization();
    }

    /**
     * Test data synchronization (Requirement 4.5)
     * Validates sync process when connectivity is restored
     */
    @Test
    public void testDataSynchronization() {
        
        // Setup offline data
        prefsHelper.saveOfflineMode(true);
        
        Donation syncDonation = new Donation(
            "sync-001",
            "Sync Test Donor",
            DonationType.CASH,
            7500.0,
            "Sync test donation",
            System.currentTimeMillis(),
            "Sync Test",
            false
        );
        
        offlineQueueManager.queueDonation(syncDonation);
        donationRepository.addDonation(syncDonation);
        
        // Simulate network restoration and sync
        prefsHelper.saveOfflineMode(false);
        
        // Create a CountDownLatch to wait for async sync completion
        CountDownLatch syncLatch = new CountDownLatch(1);
        
        // Start sync process
        offlineQueueManager.startSync(context, new OfflineQueueManager.SyncCallback() {
            @Override
            public void onSyncStarted() {
                // Sync started
            }

            @Override
            public void onSyncProgress(int completed, int total) {
                // Progress update
            }

            @Override
            public void onSyncCompleted(boolean success, List<OfflineQueueManager.SyncConflict> conflicts) {
                assertTrue("Sync should complete successfully", success);
                assertTrue("Should have no conflicts", conflicts.isEmpty());
                syncLatch.countDown();
            }

            @Override
            public void onSyncError(String error) {
                // Sync error - fail the test
                assertTrue("Sync should not fail: " + error, false);
                syncLatch.countDown();
            }
        });
        
        // Wait for sync completion (with timeout)
        try {
            boolean syncCompleted = syncLatch.await(10, TimeUnit.SECONDS);
            assertTrue("Sync should complete within timeout", syncCompleted);
        } catch (InterruptedException e) {
            throw new RuntimeException("Sync test interrupted", e);
        }
        
        // Verify sync results
        assertFalse("Should have no pending actions after sync", offlineQueueManager.hasPendingActions());
        assertEquals("Should have 0 pending actions after sync", 0, offlineQueueManager.getPendingActionCount());
        
        // Verify donation is still in local storage
        List<Donation> postSyncDonations = donationRepository.getAllDonations();
        Donation syncedDonation = findDonationById(postSyncDonations, "sync-001");
        assertNotNull("Synced donation should still exist locally", syncedDonation);
    }

    /**
     * Test notification data persistence
     * Validates that notifications are properly stored and managed
     */
    @Test
    public void testNotificationDataPersistence() {
        
        // Create test notifications
        AppNotification lowStockNotification = new AppNotification(
            1,
            NotificationType.LOW_STOCK_ALERT,
            "Low Stock Alert",
            "Water supply is running low (25/100 bottles remaining)",
            System.currentTimeMillis(),
            false,
            "ic_inventory_alert",
            "#f59e0b"
        );
        
        AppNotification donationNotification = new AppNotification(
            2,
            NotificationType.DONATION_RECEIVED,
            "New Donation",
            "₱5,000 cash donation received from John Doe",
            System.currentTimeMillis(),
            false,
            "ic_donation_received",
            "#10b981"
        );
        
        // Save notifications using preferences repository
        preferencesRepository.addNotification(lowStockNotification);
        preferencesRepository.addNotification(donationNotification);
        
        // Retrieve and verify notifications
        List<AppNotification> savedNotifications = preferencesRepository.getAllNotifications();
        assertNotNull("Saved notifications should not be null", savedNotifications);
        assertEquals("Should have 2 saved notifications", 2, savedNotifications.size());
        
        // Verify low stock notification
        AppNotification retrievedLowStock = findNotificationById(savedNotifications, 1);
        assertNotNull("Low stock notification should be found", retrievedLowStock);
        assertEquals("Low stock notification title should match", 
            lowStockNotification.getTitle(), retrievedLowStock.getTitle());
        assertEquals("Low stock notification type should match", 
            lowStockNotification.getType(), retrievedLowStock.getType());
        assertFalse("Low stock notification should be unread initially", 
            retrievedLowStock.isRead());
        
        // Test notification read status update
        retrievedLowStock.setRead(true);
        preferencesRepository.updateNotification(retrievedLowStock);
        
        List<AppNotification> updatedNotifications = preferencesRepository.getAllNotifications();
        AppNotification updatedLowStock = findNotificationById(updatedNotifications, 1);
        assertTrue("Low stock notification should be marked as read", 
            updatedLowStock.isRead());
        
        // Test unread count
        int unreadCount = preferencesRepository.getUnreadNotificationCount();
        assertEquals("Should have 1 unread notification", 1, unreadCount);
    }

    /**
     * Test data integrity and validation
     * Validates that data validation rules are enforced
     */
    @Test
    public void testDataIntegrityAndValidation() {
        
        // Test donation validation
        try {
            // Invalid donation - negative amount
            Donation invalidDonation = new Donation(
                "invalid-001",
                "Invalid Donor",
                DonationType.CASH,
                -1000.0, // Invalid negative amount
                "Invalid donation",
                System.currentTimeMillis(),
                "Test",
                false
            );
            
            boolean addResult = donationRepository.addDonation(invalidDonation);
            assertFalse("Should not add donation with negative amount", addResult);
            
        } catch (IllegalArgumentException e) {
            // Expected exception for invalid data
            assertTrue("Should throw exception for invalid donation data", true);
        }
        
        // Test inventory validation
        try {
            // Invalid inventory item - negative stock
            InventoryItem invalidItem = new InventoryItem(
                "Invalid Item",
                -10, // Invalid negative stock
                100,
                "units",
                InventoryStatus.HEALTHY,
                "❌",
                "#000000"
            );
            
            boolean addResult = inventoryRepository.addItem(invalidItem);
            assertFalse("Should not add inventory item with negative stock", addResult);
            
        } catch (IllegalArgumentException e) {
            // Expected exception for invalid data
            assertTrue("Should throw exception for invalid inventory data", true);
        }
        
        // Test valid data acceptance
        Donation validDonation = new Donation(
            "valid-001",
            "Valid Donor",
            DonationType.CASH,
            1000.0,
            "Valid donation",
            System.currentTimeMillis(),
            "Test",
            false
        );
        
        boolean validAddResult = donationRepository.addDonation(validDonation);
        assertTrue("Should add valid donation", validAddResult);
        
        List<Donation> donations = donationRepository.getAllDonations();
        assertEquals("Should have 1 valid donation", 1, donations.size());
    }

    // Helper methods

    private Donation findDonationById(List<Donation> donations, String id) {
        return donations.stream()
            .filter(donation -> id.equals(donation.getId()))
            .findFirst()
            .orElse(null);
    }

    private InventoryItem findInventoryItemByName(List<InventoryItem> items, String name) {
        return items.stream()
            .filter(item -> name.equals(item.getName()))
            .findFirst()
            .orElse(null);
    }

    private AppNotification findNotificationById(List<AppNotification> notifications, int id) {
        return notifications.stream()
            .filter(notification -> notification.getId() == id)
            .findFirst()
            .orElse(null);
    }
}