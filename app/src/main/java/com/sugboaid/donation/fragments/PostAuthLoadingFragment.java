package com.sugboaid.donation.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.sugboaid.donation.R;
import com.sugboaid.donation.viewmodels.DashboardViewModel;
import com.sugboaid.donation.viewmodels.NotificationViewModel;
import com.sugboaid.utils.DiagnosticLogger;

/**
 * Short, branded loading screen shown immediately after successful login/signup
 * Preloads Dashboard data before navigating to the Dashboard
 */
public class PostAuthLoadingFragment extends BaseFragment {

    private static final String TAG = "PostAuthLoading";
    private static final long MIN_DURATION_MS = 1200L; // 1.2s
    private static final long MAX_DURATION_MS = 2000L; // 2.0s

    private DashboardViewModel dashboardViewModel;
    private NotificationViewModel notificationViewModel;
    private NavController navController;

    private long startTs;
    private boolean proceeded = false;
    private volatile boolean dataReady = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_auth_loading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startTs = SystemClock.uptimeMillis();
        navController = Navigation.findNavController(view);

        try {
            // Activity-scoped so data is shared with DashboardFragment when it is created
            dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
            notificationViewModel = new ViewModelProvider(requireActivity()).get(NotificationViewModel.class);
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Failed to init view models", e);
        }

        // Kick off preloading immediately
        preloadDashboardData();

        // Guarded proceed when first data appears and min duration elapsed, or hard timeout
        scheduleProceedGuards();
    }

    private void preloadDashboardData() {
        try {
            if (dashboardViewModel != null) {
                dashboardViewModel.refreshData();
            }
            if (notificationViewModel != null) {
                notificationViewModel.refreshNotifications();
            }
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Preload error", e);
        }
    }

    private void scheduleProceedGuards() {
        Handler h = new Handler(Looper.getMainLooper());

        // Observe readiness from ViewModel
        try {
            if (dashboardViewModel != null) {
                dashboardViewModel.getDashboardStatistics().observe(getViewLifecycleOwner(), stats -> {
                    if (stats != null) {
                        dataReady = true;
                        tryProceedAfterMinDuration();
                    }
                });
                dashboardViewModel.getRecentActivities().observe(getViewLifecycleOwner(), list -> {
                    if (list != null) {
                        dataReady = true;
                        tryProceedAfterMinDuration();
                    }
                });
            }
        } catch (Exception ignored) { }

        // Hard timeout to ensure we never exceed MAX_DURATION_MS
        long elapsed = SystemClock.uptimeMillis() - startTs;
        long remainingToMax = Math.max(0, MAX_DURATION_MS - elapsed);
        h.postDelayed(this::proceedIfNeeded, remainingToMax);

        // Also ensure minimum duration before proceeding even if data is instant
        long remainingToMin = Math.max(0, MIN_DURATION_MS - elapsed);
        h.postDelayed(this::tryProceedAfterMinDuration, remainingToMin);
    }

    private void tryProceedAfterMinDuration() {
        long elapsed = SystemClock.uptimeMillis() - startTs;
        if (elapsed >= MIN_DURATION_MS && dataReady) {
            proceedIfNeeded();
        }
    }

    private void proceedIfNeeded() {
        if (proceeded) return;
        proceeded = true;
        try {
            // Navigate to Dashboard and clear this loading screen from back stack
            NavController nc = navController != null ? navController : Navigation.findNavController(requireView());
            androidx.navigation.NavOptions opts = new androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .setLaunchSingleTop(true)
                .build();
            nc.navigate(R.id.dashboardFragment, null, opts);
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Navigation to dashboard failed from loading", e);
        }
    }

    @Override
    protected void initViews(View view) { }

    @Override
    protected void setupListeners() { }

    @Override
    protected void observeData() { }

    @Override
    protected void refreshData() { }
}
