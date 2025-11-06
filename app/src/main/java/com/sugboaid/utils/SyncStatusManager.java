package com.sugboaid.utils;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.sugboaid.donation.R;

/**
 * Manager class for tracking and broadcasting sync status across the application
 * Provides centralized sync state management and notifications
 */
public class SyncStatusManager {
    
    private static SyncStatusManager instance;
    private final MutableLiveData<SyncState> syncStateLiveData;
    private final MutableLiveData<Integer> pendingActionsLiveData;
    private final OfflineQueueManager offlineQueueManager;
    
    public enum SyncState {
        IDLE,
        SYNCING,
        SUCCESS,
        ERROR,
        OFFLINE
    }
    
    public static class SyncStatus {
        private final SyncState state;
        private final int pendingActions;
        private final String message;
        private final long lastSyncTime;
        
        public SyncStatus(SyncState state, int pendingActions, String message, long lastSyncTime) {
            this.state = state;
            this.pendingActions = pendingActions;
            this.message = message;
            this.lastSyncTime = lastSyncTime;
        }
        
        public SyncState getState() { return state; }
        public int getPendingActions() { return pendingActions; }
        public String getMessage() { return message; }
        public long getLastSyncTime() { return lastSyncTime; }
    }
    
    private SyncStatusManager(Context context) {
        syncStateLiveData = new MutableLiveData<>(SyncState.IDLE);
        pendingActionsLiveData = new MutableLiveData<>(0);
        offlineQueueManager = OfflineQueueManager.getInstance(context);
        
        // Initialize with current state
        updatePendingActionsCount();
    }
    
    public static synchronized SyncStatusManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncStatusManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Get LiveData for sync state changes
     */
    public LiveData<SyncState> getSyncState() {
        return syncStateLiveData;
    }
    
    /**
     * Get LiveData for pending actions count
     */
    public LiveData<Integer> getPendingActionsCount() {
        return pendingActionsLiveData;
    }
    
    /**
     * Update sync state
     */
    public void setSyncState(SyncState state) {
        syncStateLiveData.setValue(state);
    }
    
    /**
     * Update pending actions count
     */
    public void updatePendingActionsCount() {
        int count = offlineQueueManager.getPendingActionCount();
        pendingActionsLiveData.setValue(count);
    }
    
    /**
     * Get current sync status
     */
    public SyncStatus getCurrentSyncStatus(Context context) {
        SyncState currentState = syncStateLiveData.getValue();
        if (currentState == null) {
            currentState = SyncState.IDLE;
        }
        
        int pendingActions = offlineQueueManager.getPendingActionCount();
        String message = getSyncStateMessage(context, currentState, pendingActions);
        long lastSyncTime = SharedPreferencesHelper.getInstance(context).getLastSyncTime();
        
        return new SyncStatus(currentState, pendingActions, message, lastSyncTime);
    }
    
    /**
     * Check if sync is needed
     */
    public boolean isSyncNeeded() {
        return offlineQueueManager.hasPendingActions();
    }
    
    /**
     * Check if currently syncing
     */
    public boolean isSyncing() {
        SyncState currentState = syncStateLiveData.getValue();
        return currentState == SyncState.SYNCING;
    }
    
    /**
     * Mark sync as started
     */
    public void onSyncStarted() {
        setSyncState(SyncState.SYNCING);
    }
    
    /**
     * Mark sync as completed
     */
    public void onSyncCompleted(boolean success) {
        setSyncState(success ? SyncState.SUCCESS : SyncState.ERROR);
        updatePendingActionsCount();
        
        // Reset to idle after a delay
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (syncStateLiveData.getValue() == SyncState.SUCCESS || 
                syncStateLiveData.getValue() == SyncState.ERROR) {
                setSyncState(SyncState.IDLE);
            }
        }, 3000);
    }
    
    /**
     * Mark as offline
     */
    public void onNetworkLost() {
        setSyncState(SyncState.OFFLINE);
        updatePendingActionsCount();
    }
    
    /**
     * Mark as online
     */
    public void onNetworkRestored() {
        if (syncStateLiveData.getValue() == SyncState.OFFLINE) {
            setSyncState(SyncState.IDLE);
        }
        updatePendingActionsCount();
    }
    
    /**
     * Get appropriate message for sync state
     */
    private String getSyncStateMessage(Context context, SyncState state, int pendingActions) {
        switch (state) {
            case SYNCING:
                return context.getString(R.string.syncing_data);
            case SUCCESS:
                return context.getString(R.string.sync_completed);
            case ERROR:
                return context.getString(R.string.sync_failed);
            case OFFLINE:
                if (pendingActions > 0) {
                    return context.getString(R.string.pending_actions_count, pendingActions);
                } else {
                    return context.getString(R.string.offline_mode_active);
                }
            case IDLE:
            default:
                if (pendingActions > 0) {
                    return context.getString(R.string.pending_actions_count, pendingActions);
                } else {
                    return "";
                }
        }
    }
    
    /**
     * Reset sync status
     */
    public void reset() {
        setSyncState(SyncState.IDLE);
        updatePendingActionsCount();
    }
}