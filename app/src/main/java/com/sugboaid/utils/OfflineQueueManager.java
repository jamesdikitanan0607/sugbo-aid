package com.sugboaid.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sugboaid.models.Donation;
import com.sugboaid.models.InventoryItem;
import com.sugboaid.models.Transaction;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manager class for handling offline action queuing and synchronization
 * Queues actions when offline and processes them when connectivity is restored
 */
public class OfflineQueueManager {
    
    private static final String TAG = "OfflineQueueManager";
    private static final String PREF_OFFLINE_QUEUE = "offline_queue";
    private static final String PREF_SYNC_CONFLICTS = "sync_conflicts";
    private static final String PREF_LAST_SYNC_ATTEMPT = "last_sync_attempt";
    
    private static OfflineQueueManager instance;
    private final SharedPreferencesHelper prefsHelper;
    private final Gson gson;
    private final ConcurrentLinkedQueue<OfflineAction> actionQueue;
    private final List<SyncConflict> conflicts;
    private boolean isSyncing = false;
    
    // Sync callback interface
    public interface SyncCallback {
        void onSyncStarted();
        void onSyncProgress(int completed, int total);
        void onSyncCompleted(boolean success, List<SyncConflict> conflicts);
        void onSyncError(String error);
    }
    
    // Offline action types
    public enum ActionType {
        ADD_DONATION,
        UPDATE_DONATION,
        DELETE_DONATION,
        ADD_INVENTORY_ITEM,
        UPDATE_INVENTORY_ITEM,
        DELETE_INVENTORY_ITEM,
        ADD_TRANSACTION,
        UPDATE_TRANSACTION,
        DELETE_TRANSACTION
    }
    
    // Offline action data structure
    public static class OfflineAction {
        private String id;
        private ActionType type;
        private String data;
        private long timestamp;
        private int retryCount;
        private boolean processed;
        
        public OfflineAction(ActionType type, String data) {
            this.id = UUID.randomUUID().toString();
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.retryCount = 0;
            this.processed = false;
        }
        
        // Getters and setters
        public String getId() { return id; }
        public ActionType getType() { return type; }
        public String getData() { return data; }
        public long getTimestamp() { return timestamp; }
        public int getRetryCount() { return retryCount; }
        public boolean isProcessed() { return processed; }
        
        public void incrementRetryCount() { this.retryCount++; }
        public void markAsProcessed() { this.processed = true; }
    }
    
    // Sync conflict data structure
    public static class SyncConflict {
        private String actionId;
        private ActionType actionType;
        private String localData;
        private String serverData;
        private long conflictTimestamp;
        private boolean resolved;
        
        public SyncConflict(String actionId, ActionType actionType, String localData, String serverData) {
            this.actionId = actionId;
            this.actionType = actionType;
            this.localData = localData;
            this.serverData = serverData;
            this.conflictTimestamp = System.currentTimeMillis();
            this.resolved = false;
        }
        
        // Getters and setters
        public String getActionId() { return actionId; }
        public ActionType getActionType() { return actionType; }
        public String getLocalData() { return localData; }
        public String getServerData() { return serverData; }
        public long getConflictTimestamp() { return conflictTimestamp; }
        public boolean isResolved() { return resolved; }
        
        public void markAsResolved() { this.resolved = true; }
    }
    
    private OfflineQueueManager(Context context) {
        this.prefsHelper = SharedPreferencesHelper.getInstance(context);
        this.gson = new Gson();
        this.actionQueue = new ConcurrentLinkedQueue<>();
        this.conflicts = new ArrayList<>();
        
        // Load existing queue and conflicts from storage
        loadQueueFromStorage();
        loadConflictsFromStorage();
    }
    
    public static synchronized OfflineQueueManager getInstance(Context context) {
        if (instance == null) {
            instance = new OfflineQueueManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Queue a donation action for offline processing
     */
    public void queueDonationAction(ActionType actionType, Donation donation) {
        String donationJson = gson.toJson(donation);
        OfflineAction action = new OfflineAction(actionType, donationJson);
        actionQueue.offer(action);
        saveQueueToStorage();
        
        Log.d(TAG, "Queued donation action: " + actionType + " for donation: " + donation.getId());
    }
    
    /**
     * Queue an inventory action for offline processing
     */
    public void queueInventoryAction(ActionType actionType, InventoryItem item) {
        String itemJson = gson.toJson(item);
        OfflineAction action = new OfflineAction(actionType, itemJson);
        actionQueue.offer(action);
        saveQueueToStorage();
        
        Log.d(TAG, "Queued inventory action: " + actionType + " for item: " + item.getName());
    }
    
    /**
     * Queue a transaction action for offline processing
     */
    public void queueTransactionAction(ActionType actionType, Transaction transaction) {
        String transactionJson = gson.toJson(transaction);
        OfflineAction action = new OfflineAction(actionType, transactionJson);
        actionQueue.offer(action);
        saveQueueToStorage();
        
        Log.d(TAG, "Queued transaction action: " + actionType + " for transaction: " + transaction.getId());
    }
    
    /**
     * Get the number of pending actions in the queue
     */
    public int getPendingActionCount() {
        return (int) actionQueue.stream().filter(action -> !action.isProcessed()).count();
    }
    
    /**
     * Check if there are pending actions to sync
     */
    public boolean hasPendingActions() {
        return getPendingActionCount() > 0;
    }
    
    /**
     * Get all pending actions
     */
    public List<OfflineAction> getPendingActions() {
        return actionQueue.stream()
                .filter(action -> !action.isProcessed())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Start synchronization process when network is available
     */
    public void startSync(Context context, SyncCallback callback) {
        if (isSyncing) {
            Log.w(TAG, "Sync already in progress");
            return;
        }
        
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.w(TAG, "Cannot sync: Network not available");
            callback.onSyncError("Network not available");
            return;
        }
        
        isSyncing = true;
        callback.onSyncStarted();
        
        // Process actions in background thread
        new Thread(() -> processSyncQueue(callback)).start();
    }
    
    /**
     * Process the sync queue
     */
    private void processSyncQueue(SyncCallback callback) {
        List<OfflineAction> pendingActions = getPendingActions();
        int totalActions = pendingActions.size();
        int completedActions = 0;
        
        Log.d(TAG, "Starting sync of " + totalActions + " actions");
        
        for (OfflineAction action : pendingActions) {
            try {
                boolean success = processAction(action);
                
                if (success) {
                    action.markAsProcessed();
                    completedActions++;
                    Log.d(TAG, "Successfully processed action: " + action.getId());
                } else {
                    action.incrementRetryCount();
                    
                    // If retry count exceeds limit, mark as failed
                    if (action.getRetryCount() >= 3) {
                        Log.e(TAG, "Action failed after 3 retries: " + action.getId());
                        // Could add to failed actions list for manual review
                    }
                }
                
                // Update progress
                callback.onSyncProgress(completedActions, totalActions);
                
                // Small delay between actions to avoid overwhelming the system
                Thread.sleep(100);
                
            } catch (Exception e) {
                Log.e(TAG, "Error processing action: " + action.getId(), e);
                action.incrementRetryCount();
            }
        }
        
        // Save updated queue and update sync timestamp
        saveQueueToStorage();
        prefsHelper.saveLastSyncTime(System.currentTimeMillis());
        
        isSyncing = false;
        
        // Notify completion
        boolean allSuccessful = completedActions == totalActions;
        callback.onSyncCompleted(allSuccessful, new ArrayList<>(conflicts));
        
        Log.d(TAG, "Sync completed. " + completedActions + "/" + totalActions + " actions processed");
    }
    
    /**
     * Process individual action
     */
    private boolean processAction(OfflineAction action) {
        try {
            switch (action.getType()) {
                case ADD_DONATION:
                    return processDonationAction(action, "add");
                case UPDATE_DONATION:
                    return processDonationAction(action, "update");
                case DELETE_DONATION:
                    return processDonationAction(action, "delete");
                case ADD_INVENTORY_ITEM:
                    return processInventoryAction(action, "add");
                case UPDATE_INVENTORY_ITEM:
                    return processInventoryAction(action, "update");
                case DELETE_INVENTORY_ITEM:
                    return processInventoryAction(action, "delete");
                case ADD_TRANSACTION:
                    return processTransactionAction(action, "add");
                case UPDATE_TRANSACTION:
                    return processTransactionAction(action, "update");
                case DELETE_TRANSACTION:
                    return processTransactionAction(action, "delete");
                default:
                    Log.w(TAG, "Unknown action type: " + action.getType());
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing action: " + action.getId(), e);
            return false;
        }
    }
    
    /**
     * Process donation-related actions
     */
    private boolean processDonationAction(OfflineAction action, String operation) {
        try {
            Donation donation = gson.fromJson(action.getData(), Donation.class);
            
            // In a real implementation, this would make API calls to sync with server
            // For now, we'll simulate the process and update local storage
            
            switch (operation) {
                case "add":
                    // Simulate API call to add donation
                    Log.d(TAG, "Syncing new donation: " + donation.getId());
                    // If successful, donation is already in local storage
                    return true;
                    
                case "update":
                    // Simulate API call to update donation
                    Log.d(TAG, "Syncing donation update: " + donation.getId());
                    return true;
                    
                case "delete":
                    // Simulate API call to delete donation
                    Log.d(TAG, "Syncing donation deletion: " + donation.getId());
                    return true;
                    
                default:
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing donation action", e);
            return false;
        }
    }
    
    /**
     * Process inventory-related actions
     */
    private boolean processInventoryAction(OfflineAction action, String operation) {
        try {
            InventoryItem item = gson.fromJson(action.getData(), InventoryItem.class);
            
            switch (operation) {
                case "add":
                    Log.d(TAG, "Syncing new inventory item: " + item.getName());
                    return true;
                    
                case "update":
                    Log.d(TAG, "Syncing inventory item update: " + item.getName());
                    return true;
                    
                case "delete":
                    Log.d(TAG, "Syncing inventory item deletion: " + item.getName());
                    return true;
                    
                default:
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing inventory action", e);
            return false;
        }
    }
    
    /**
     * Process transaction-related actions
     */
    private boolean processTransactionAction(OfflineAction action, String operation) {
        try {
            Transaction transaction = gson.fromJson(action.getData(), Transaction.class);
            
            switch (operation) {
                case "add":
                    Log.d(TAG, "Syncing new transaction: " + transaction.getId());
                    return true;
                    
                case "update":
                    Log.d(TAG, "Syncing transaction update: " + transaction.getId());
                    return true;
                    
                case "delete":
                    Log.d(TAG, "Syncing transaction deletion: " + transaction.getId());
                    return true;
                    
                default:
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing transaction action", e);
            return false;
        }
    }
    
    /**
     * Handle sync conflicts
     */
    public void resolveConflict(String conflictId, boolean useLocalData) {
        for (SyncConflict conflict : conflicts) {
            if (conflict.getActionId().equals(conflictId)) {
                if (useLocalData) {
                    // Keep local changes, mark conflict as resolved
                    conflict.markAsResolved();
                    Log.d(TAG, "Conflict resolved using local data: " + conflictId);
                } else {
                    // Use server data, update local storage
                    // Implementation would depend on conflict type
                    conflict.markAsResolved();
                    Log.d(TAG, "Conflict resolved using server data: " + conflictId);
                }
                break;
            }
        }
        saveConflictsToStorage();
    }
    
    /**
     * Clear processed actions from queue
     */
    public void clearProcessedActions() {
        actionQueue.removeIf(OfflineAction::isProcessed);
        saveQueueToStorage();
    }
    
    /**
     * Clear all actions from queue
     */
    public void clearAllActions() {
        actionQueue.clear();
        saveQueueToStorage();
    }
    
    /**
     * Save queue to persistent storage
     */
    private void saveQueueToStorage() {
        String queueJson = gson.toJson(new ArrayList<>(actionQueue));
        prefsHelper.getSharedPreferences().edit()
                .putString(PREF_OFFLINE_QUEUE, queueJson)
                .apply();
    }
    
    /**
     * Load queue from persistent storage
     */
    private void loadQueueFromStorage() {
        String queueJson = prefsHelper.getSharedPreferences().getString(PREF_OFFLINE_QUEUE, "[]");
        Type listType = new TypeToken<List<OfflineAction>>(){}.getType();
        List<OfflineAction> loadedActions = gson.fromJson(queueJson, listType);
        
        if (loadedActions != null) {
            actionQueue.addAll(loadedActions);
        }
    }
    
    /**
     * Save conflicts to persistent storage
     */
    private void saveConflictsToStorage() {
        String conflictsJson = gson.toJson(conflicts);
        prefsHelper.getSharedPreferences().edit()
                .putString(PREF_SYNC_CONFLICTS, conflictsJson)
                .apply();
    }
    
    /**
     * Load conflicts from persistent storage
     */
    private void loadConflictsFromStorage() {
        String conflictsJson = prefsHelper.getSharedPreferences().getString(PREF_SYNC_CONFLICTS, "[]");
        Type listType = new TypeToken<List<SyncConflict>>(){}.getType();
        List<SyncConflict> loadedConflicts = gson.fromJson(conflictsJson, listType);
        
        if (loadedConflicts != null) {
            conflicts.addAll(loadedConflicts);
        }
    }
    
    /**
     * Get sync status information
     */
    public SyncStatus getSyncStatus() {
        return new SyncStatus(
            isSyncing,
            getPendingActionCount(),
            conflicts.size(),
            prefsHelper.getLastSyncTime()
        );
    }
    
    /**
     * Sync status data structure
     */
    public static class SyncStatus {
        private final boolean syncing;
        private final int pendingActions;
        private final int conflicts;
        private final long lastSyncTime;
        
        public SyncStatus(boolean syncing, int pendingActions, int conflicts, long lastSyncTime) {
            this.syncing = syncing;
            this.pendingActions = pendingActions;
            this.conflicts = conflicts;
            this.lastSyncTime = lastSyncTime;
        }
        
        public boolean isSyncing() { return syncing; }
        public int getPendingActions() { return pendingActions; }
        public int getConflicts() { return conflicts; }
        public long getLastSyncTime() { return lastSyncTime; }
    }
}