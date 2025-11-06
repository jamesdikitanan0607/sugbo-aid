package com.sugboaid.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sugboaid.donation.R;
import com.sugboaid.utils.OfflineQueueManager;

/**
 * Custom view for displaying offline status and sync progress
 * Shows different states: offline, syncing, sync completed, sync error
 */
public class OfflineBannerView extends LinearLayout {
    
    private ImageView iconView;
    private TextView messageView;
    private TextView detailView;
    private ProgressBar progressBar;
    private View syncButton;
    
    private BannerState currentState = BannerState.HIDDEN;
    private OnSyncRequestListener syncRequestListener;
    
    public enum BannerState {
        HIDDEN,
        OFFLINE,
        SYNCING,
        SYNC_COMPLETED,
        SYNC_ERROR
    }
    
    public interface OnSyncRequestListener {
        void onSyncRequested();
    }
    
    public OfflineBannerView(Context context) {
        super(context);
        init();
    }
    
    public OfflineBannerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public OfflineBannerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        LayoutInflater.from(getContext()).inflate(R.layout.view_offline_banner, this, true);
        
        iconView = findViewById(R.id.banner_icon);
        messageView = findViewById(R.id.banner_message);
        detailView = findViewById(R.id.banner_detail);
        progressBar = findViewById(R.id.banner_progress);
        syncButton = findViewById(R.id.banner_sync_button);
        
        setupSyncButton();
        
        // Initially hidden
        setVisibility(GONE);
    }
    
    private void setupSyncButton() {
        syncButton.setOnClickListener(v -> {
            if (syncRequestListener != null && currentState == BannerState.OFFLINE) {
                syncRequestListener.onSyncRequested();
            }
        });
    }
    
    /**
     * Show offline state with pending actions count
     */
    public void showOfflineState(int pendingActions) {
        currentState = BannerState.OFFLINE;
        
        iconView.setImageResource(R.drawable.ic_offline);
        iconView.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white));
        
        messageView.setText(R.string.offline_mode_active);
        
        if (pendingActions > 0) {
            detailView.setText(getContext().getString(R.string.pending_actions_count, pendingActions));
            detailView.setVisibility(VISIBLE);
            syncButton.setVisibility(VISIBLE);
        } else {
            detailView.setVisibility(GONE);
            syncButton.setVisibility(GONE);
        }
        
        progressBar.setVisibility(GONE);
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.warning_orange));
        
        animateShow();
    }
    
    /**
     * Show syncing state with progress
     */
    public void showSyncingState(int completed, int total) {
        currentState = BannerState.SYNCING;
        
        iconView.setImageResource(R.drawable.ic_sync);
        iconView.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white));
        
        messageView.setText(R.string.syncing_data);
        
        if (total > 0) {
            detailView.setText(getContext().getString(R.string.sync_progress, completed, total));
            detailView.setVisibility(VISIBLE);
            
            progressBar.setMax(total);
            progressBar.setProgress(completed);
            progressBar.setVisibility(VISIBLE);
        } else {
            detailView.setVisibility(GONE);
            progressBar.setVisibility(VISIBLE);
            progressBar.setIndeterminate(true);
        }
        
        syncButton.setVisibility(GONE);
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary_blue));
        
        if (getVisibility() != VISIBLE) {
            animateShow();
        }
    }
    
    /**
     * Show sync completed state
     */
    public void showSyncCompletedState(boolean success, int syncedActions) {
        currentState = success ? BannerState.SYNC_COMPLETED : BannerState.SYNC_ERROR;
        
        if (success) {
            iconView.setImageResource(R.drawable.ic_check_circle);
            iconView.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white));
            messageView.setText(R.string.sync_completed);
            detailView.setText(getContext().getString(R.string.actions_synced, syncedActions));
            setBackgroundColor(ContextCompat.getColor(getContext(), R.color.success_green));
        } else {
            iconView.setImageResource(R.drawable.ic_error);
            iconView.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white));
            messageView.setText(R.string.sync_failed);
            detailView.setText(R.string.sync_will_retry);
            setBackgroundColor(ContextCompat.getColor(getContext(), R.color.error_red));
        }
        
        detailView.setVisibility(VISIBLE);
        progressBar.setVisibility(GONE);
        syncButton.setVisibility(GONE);
        
        if (getVisibility() != VISIBLE) {
            animateShow();
        }
        
        // Auto-hide after 3 seconds
        postDelayed(this::animateHide, 3000);
    }
    
    /**
     * Show sync error state
     */
    public void showSyncErrorState(String errorMessage) {
        currentState = BannerState.SYNC_ERROR;
        
        iconView.setImageResource(R.drawable.ic_error);
        iconView.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white));
        
        messageView.setText(R.string.sync_error);
        detailView.setText(errorMessage);
        detailView.setVisibility(VISIBLE);
        
        progressBar.setVisibility(GONE);
        syncButton.setVisibility(VISIBLE); // Allow retry
        
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.error_red));
        
        if (getVisibility() != VISIBLE) {
            animateShow();
        }
    }
    
    /**
     * Hide the banner
     */
    public void hide() {
        currentState = BannerState.HIDDEN;
        animateHide();
    }
    
    /**
     * Update sync progress
     */
    public void updateSyncProgress(int completed, int total) {
        if (currentState == BannerState.SYNCING) {
            detailView.setText(getContext().getString(R.string.sync_progress, completed, total));
            
            if (progressBar.isIndeterminate()) {
                progressBar.setIndeterminate(false);
                progressBar.setMax(total);
            }
            progressBar.setProgress(completed);
        }
    }
    
    /**
     * Set sync request listener
     */
    public void setOnSyncRequestListener(OnSyncRequestListener listener) {
        this.syncRequestListener = listener;
    }
    
    /**
     * Get current banner state
     */
    public BannerState getCurrentState() {
        return currentState;
    }
    
    /**
     * Check if banner is currently visible
     */
    public boolean isShowing() {
        return getVisibility() == VISIBLE && currentState != BannerState.HIDDEN;
    }
    
    /**
     * Animate banner appearance
     */
    private void animateShow() {
        if (getVisibility() == VISIBLE) {
            return;
        }
        
        setAlpha(0f);
        setVisibility(VISIBLE);
        
        animate()
            .alpha(1f)
            .setDuration(300)
            .setListener(null);
    }
    
    /**
     * Animate banner disappearance
     */
    private void animateHide() {
        if (getVisibility() != VISIBLE) {
            return;
        }
        
        animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction(() -> setVisibility(GONE));
    }
    
    /**
     * Force immediate show without animation
     */
    public void showImmediately() {
        setVisibility(VISIBLE);
        setAlpha(1f);
    }
    
    /**
     * Force immediate hide without animation
     */
    public void hideImmediately() {
        setVisibility(GONE);
        setAlpha(1f);
    }
}