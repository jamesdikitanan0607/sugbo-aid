package com.sugboaid.donation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sugboaid.donation.R;
import com.sugboaid.donation.adapters.RecentActivitiesAdapter;
import com.sugboaid.donation.viewmodels.DashboardViewModel;
import com.sugboaid.donation.viewmodels.NotificationViewModel;
import com.sugboaid.donation.views.AnimatedGradientButton;
import com.sugboaid.donation.views.StatisticsCard;
import com.sugboaid.donation.utils.AnimationUtils;
import com.sugboaid.donation.utils.NotificationManager;
import com.sugboaid.donation.utils.MicroInteractionHelper;
import com.sugboaid.donation.utils.LoadingStateManager;
import com.sugboaid.models.Donation;
import com.sugboaid.models.User;
import com.sugboaid.viewmodels.AuthViewModel;

import java.util.List;

/**
 * Dashboard fragment displaying statistics, quick actions, and recent activities
 */
public class DashboardFragment extends BaseFragment {

    private DashboardViewModel viewModel;
    private NotificationViewModel notificationViewModel;
    private AuthViewModel authViewModel;
    private NavController navController;
    private NotificationManager notificationManager;

    // Views
    private TextView tvWelcome;
    private StatisticsCard cardTotalDonations;
    private StatisticsCard cardDistributedItems;
    private StatisticsCard cardFamiliesHelped;
    
    private AnimatedGradientButton btnNewDonation;
    private AnimatedGradientButton btnInventory;
    private AnimatedGradientButton btnTransparency;
    private AnimatedGradientButton btnReports;
    
    private RecyclerView rvRecentActivities;
    private LinearLayout llEmptyState;
    private FloatingActionButton fabQuickDonation;
    
    // Notification views
    private View flNotificationContainer;
    private TextView tvNotificationBadge;
    
    // Logout button
    private ImageView ivLogout;
    
    private RecentActivitiesAdapter recentActivitiesAdapter;
    private LoadingStateManager loadingStateManager;
    private boolean firstLoad = true;
    
    // Fragment visibility state
    private boolean isViewCreated = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "onCreateView started");
            View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
            com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "Layout inflated successfully");
            return view;
        } catch (Exception e) {
            com.sugboaid.utils.DiagnosticLogger.logError("DashboardFragment", "Error inflating layout", e);
            throw e;
        }
    }
    // Handle first load in the dashboard
    private void handleFirstLoad() {
        com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "handleFirstLoad called. firstLoad=" + firstLoad + ", isAdded=" + isAdded() + ", isDetached=" + isDetached());
        if (firstLoad && isAdded() && !isDetached()) {
            firstLoad = false;
            boolean needInitialLoad = true;
            try {
                if (viewModel != null) {
                    boolean hasStats = viewModel.getDashboardStatistics().getValue() != null;
                    boolean hasActivities = viewModel.hasRecentActivities();
                    needInitialLoad = !(hasStats && hasActivities);
                }
            } catch (Exception ignored) { }
            if (needInitialLoad) {
                com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "First load -> calling loadData()");
                loadData();
            } else {
                com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "First load -> data already present, skipping loadData()");
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "onViewCreated started");
            
            // Initialize ViewModels with error handling
            try {
                // Ensure ViewModels are ready before calling super so BaseFragment can attach observers prior to any data load
                // Use activity-scoped ViewModel so preloaded data from MainActivity is reused
                viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
                notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
                authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
                com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "ViewModels initialized");
                
                // Filter recent activities by current user (only for non-admin users)
                User currentUser = authViewModel.getCurrentUser();
                if (currentUser != null) {
                    if (currentUser.isAdmin()) {
                        // Admin users see all activities (no filtering needed)
                        com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "Admin user detected - showing all activities");
                    } else {
                        // Regular users see only their own activities
                        com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "Regular user detected - filtering activities for user: " + currentUser.getId());
                        viewModel.refreshRecentActivities(currentUser.getId());
                    }
                }
            } catch (Exception e) {
                com.sugboaid.utils.DiagnosticLogger.logError("DashboardFragment", "Error initializing ViewModels", e);
                throw e;
            }
            
            super.onViewCreated(view, savedInstanceState);
            
            // Get NavController with error handling
            try {
                navController = Navigation.findNavController(view);
                com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "NavController obtained");
            } catch (Exception e) {
                com.sugboaid.utils.DiagnosticLogger.logError("DashboardFragment", "Error getting NavController", e);
                // Continue without NavController - some features may not work but fragment won't crash
            }
            
            isViewCreated = true;
            com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "onViewCreated completed successfully");
            handleFirstLoad();

            // Trigger a refresh only if data isn't already present (avoids double load after preloading)
            view.post(() -> {
                try {
                    boolean shouldRefresh = true;
                    if (viewModel != null) {
                        boolean hasStats = viewModel.getDashboardStatistics().getValue() != null;
                        boolean hasActivities = viewModel.hasRecentActivities();
                        shouldRefresh = !(hasStats && hasActivities);
                        if (shouldRefresh) {
                            viewModel.forceRefresh();
                        }
                    }
                    if (notificationViewModel != null) {
                        notificationViewModel.refreshNotifications();
                    }
                    com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "Initial refresh check complete; shouldRefresh=" + shouldRefresh);
                } catch (Exception ignored) { }
            });
            
        } catch (Exception e) {
            com.sugboaid.utils.DiagnosticLogger.logError("DashboardFragment", "Critical error in onViewCreated", e);
            // Show error to user
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "Dashboard loading error. Please restart the app.", 
                    android.widget.Toast.LENGTH_LONG).show();
            }
            throw e;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (viewModel != null) {
                boolean needsEmit = viewModel.getDashboardStatistics().getValue() == null || !viewModel.hasRecentActivities();
                if (needsEmit) {
                    viewModel.forceRefresh();
                    if (notificationViewModel != null) {
                        notificationViewModel.refreshNotifications();
                    }
                    com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "onResume trigger: forceRefresh due to missing data");
                }
            }
        } catch (Exception ignored) { }
    }

    private void loadData() {
        com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "loadData() -> triggering ViewModel.refreshData and notifications refresh");
        if (viewModel != null) {
            viewModel.refreshData();
            if (notificationViewModel != null) {
                notificationViewModel.refreshNotifications();
            }
        }
    }

    @Override
    protected void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tv_welcome);
        
        // Statistics cards
        cardTotalDonations = view.findViewById(R.id.card_total_donations);
        cardDistributedItems = view.findViewById(R.id.card_distributed_items);
        cardFamiliesHelped = view.findViewById(R.id.card_families_helped);
        
        // Quick action buttons
        btnNewDonation = view.findViewById(R.id.btn_new_donation);
        btnInventory = view.findViewById(R.id.btn_inventory);
        btnTransparency = view.findViewById(R.id.btn_transparency);
        btnReports = view.findViewById(R.id.btn_reports);
        
        // Notification views
        flNotificationContainer = view.findViewById(R.id.fl_notification_container);
        tvNotificationBadge = view.findViewById(R.id.tv_notification_badge);
        
        // Logout button
        ivLogout = view.findViewById(R.id.iv_logout);
        
        // Recent activities
        rvRecentActivities = view.findViewById(R.id.rv_recent_activities);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        
        // Floating action button
        fabQuickDonation = view.findViewById(R.id.fab_quick_donation);

        // Initialize managers before observing/loading
        notificationManager = new NotificationManager(requireContext());
        loadingStateManager = new LoadingStateManager(requireContext());

        // Setup RecyclerView once
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        recentActivitiesAdapter = new RecentActivitiesAdapter(requireContext());
        rvRecentActivities.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentActivities.setAdapter(recentActivitiesAdapter);
        rvRecentActivities.setNestedScrollingEnabled(false);
        
        // Set item click listener
        recentActivitiesAdapter.setOnItemClickListener(new RecentActivitiesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Donation donation) {
                // Navigate to donation details or show details dialog
                showDonationDetails(donation);
            }

            @Override
            public void onItemLongClick(Donation donation) {
                // Show context menu or additional options
                showDonationContextMenu(donation);
            }
        });
    }

    @Override
    protected void setupListeners() {
        setupClickListeners();
    }

    protected void setupClickListeners() {
        // Apply micro-interactions to all buttons
        MicroInteractionHelper.applyPressAnimation(btnNewDonation, true);
        MicroInteractionHelper.applyPressAnimation(btnInventory, true);
        MicroInteractionHelper.applyPressAnimation(btnTransparency, true);
        MicroInteractionHelper.applyPressAnimation(btnReports, true);
        MicroInteractionHelper.applyPressAnimation(fabQuickDonation, true);
        
        // Apply hover effects for accessibility
        MicroInteractionHelper.applyHoverEffect(btnNewDonation);
        MicroInteractionHelper.applyHoverEffect(btnInventory);
        MicroInteractionHelper.applyHoverEffect(btnTransparency);
        MicroInteractionHelper.applyHoverEffect(btnReports);
        
        // Quick action buttons
        btnNewDonation.setOnClickListener(v -> {
            animateButtonPress(v);
            navigateToNewDonation();
        });

        btnInventory.setOnClickListener(v -> {
            animateButtonPress(v);
            navigateToInventory();
        });

        btnTransparency.setOnClickListener(v -> {
            animateButtonPress(v);
            navigateToTransparency();
        });

        btnReports.setOnClickListener(v -> {
            animateButtonPress(v);
            navigateToReports();
        });

        // Floating action button
        fabQuickDonation.setOnClickListener(v -> {
            MicroInteractionHelper.animateFabRotation(v);
            v.postDelayed(this::navigateToNewDonation, 200);
        });

        // Statistics cards click listeners (for detailed views)
        cardTotalDonations.setOnClickListener(v -> {
            cardTotalDonations.animateClick();
            navigateToReports(); // Show donation reports
        });

        cardDistributedItems.setOnClickListener(v -> {
            cardDistributedItems.animateClick();
            navigateToInventory(); // Show inventory details
        });

        cardFamiliesHelped.setOnClickListener(v -> {
            cardFamiliesHelped.animateClick();
            navigateToTransparency(); // Show impact details
        });
        
        // Notification icon click listener
        flNotificationContainer.setOnClickListener(v -> {
            animateButtonPress(v);
            navigateToNotifications();
        });
        
        // Logout button click listener
        ivLogout.setOnClickListener(v -> {
            animateButtonPress(v);
            showLogoutConfirmation();
        });
    }

    private void observeViewModel() {
        com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "observeViewModel() attaching observers with viewLifecycleOwner");
        // Observe combined dashboard statistics so UI updates atomically
        viewModel.getDashboardStatistics().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                cardTotalDonations.setValue(stats.getFormattedTotalDonations());
                cardTotalDonations.setPercentage(stats.getDonationsChange());

                cardDistributedItems.setValue(stats.getFormattedDistributedItems());
                cardDistributedItems.setPercentage(stats.getItemsChange());

                cardFamiliesHelped.setValue(stats.getFormattedFamiliesHelped());
                cardFamiliesHelped.setPercentage(stats.getFamiliesChange());
            }
        });

        // Observe recent activities
        viewModel.getRecentActivities().observe(getViewLifecycleOwner(), this::updateRecentActivities);

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showError(errorMessage);
                viewModel.clearError();
            }
        });
        
        // Observe notification unread count
        notificationViewModel.getUnreadCount().observe(getViewLifecycleOwner(), unreadCount -> {
            updateNotificationBadge(unreadCount != null ? unreadCount : 0);
        });
        
        // Observe authentication state
        authViewModel.authState.observe(getViewLifecycleOwner(), authState -> {
            if (authState != null && !authState.isAuthenticated()) {
                // User has been logged out, navigate to login
                navigateToLogin();
            }
        });
        
        // Observe auth success messages
        authViewModel.successMessage.observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show();
                authViewModel.clearSuccessMessage();
            }
        });
        
        // Observe auth error messages
        authViewModel.errorMessage.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showError(errorMessage);
                authViewModel.clearErrorMessage();
            }
        });
    }

    private void updateRecentActivities(List<Donation> donations) {
        if (donations != null && !donations.isEmpty()) {
            rvRecentActivities.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
            recentActivitiesAdapter.submitList(donations);
        } else {
            rvRecentActivities.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void applyEntranceAnimations(View rootView) {
        // Call super for base fade-in
        super.applyEntranceAnimations(rootView);
        
        // Animate statistics cards with staggered entrance
        AnimationUtils.animateViewEntrance(cardTotalDonations, AnimationUtils.AnimationType.SLIDE_UP, 0);
        AnimationUtils.animateViewEntrance(cardDistributedItems, AnimationUtils.AnimationType.SLIDE_UP, AnimationUtils.DELAY_MEDIUM);
        AnimationUtils.animateViewEntrance(cardFamiliesHelped, AnimationUtils.AnimationType.SLIDE_UP, AnimationUtils.DELAY_MEDIUM * 2);

        // Animate quick action buttons with stagger
        ViewGroup buttonContainer = rootView.findViewById(R.id.gl_quick_actions);
        if (buttonContainer != null) {
            animateCardEntrance(buttonContainer);
        }

        // Animate recent activities section
        AnimationUtils.animateViewEntrance(rvRecentActivities, AnimationUtils.AnimationType.SLIDE_UP, AnimationUtils.DELAY_LONG);
        
        // Animate FAB entrance
        animateFabEntrance(fabQuickDonation);
    }

    // Navigation methods
    private void navigateToNewDonation() {
        try {
            navController.navigate(R.id.action_dashboard_to_pos);
        } catch (Exception e) {
            showError("Unable to navigate to donation screen");
        }
    }

    private void navigateToInventory() {
        try {
            navController.navigate(R.id.action_dashboard_to_inventory);
        } catch (Exception e) {
            showError("Unable to navigate to inventory screen");
        }
    }

    private void navigateToTransparency() {
        try {
            navController.navigate(R.id.action_dashboard_to_transparency);
        } catch (Exception e) {
            showError("Unable to navigate to transparency screen");
        }
    }

    private void navigateToReports() {
        try {
            navController.navigate(R.id.action_dashboard_to_reports);
        } catch (Exception e) {
            showError("Unable to navigate to reports screen");
        }
    }

    // Donation interaction methods
    private void showDonationDetails(Donation donation) {
        // Create and show donation details dialog
        String details = String.format(
            "Donor: %s\nAmount: %s\nType: %s\nDate: %s\nVerified: %s",
            donation.getDonorName(),
            donation.getFormattedAmount(),
            donation.getType().toString(),
            new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                .format(new java.util.Date(donation.getTimestamp())),
            donation.isVerified() ? "Yes" : "No"
        );

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Donation Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show();
    }

    private void showDonationContextMenu(Donation donation) {
        // Show context menu with options like "View Details", "Edit", "Delete"
        String[] options = {"View Details", "Mark as Verified", "Delete"};
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Donation Options")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        showDonationDetails(donation);
                        break;
                    case 1:
                        // Toggle verification status
                        donation.setVerified(!donation.isVerified());
                        viewModel.refreshData();
                        break;
                    case 2:
                        // Confirm deletion
                        confirmDeleteDonation(donation);
                        break;
                }
            })
            .show();
    }

    private void confirmDeleteDonation(Donation donation) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Donation")
            .setMessage("Are you sure you want to delete this donation?")
            .setPositiveButton("Delete", (dialog, which) -> {
                // Delete donation through repository
                // This would need to be implemented in the repository
                showMessage("Donation deleted");
                viewModel.refreshData();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void navigateToNotifications() {
        try {
            navController.navigate(R.id.action_dashboard_to_notifications);
        } catch (Exception e) {
            showError("Unable to navigate to notifications screen");
        }
    }
    
    // Authentication methods
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.logout, (dialog, which) -> {
                performLogout();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    private void performLogout() {
        if (authViewModel != null) {
            authViewModel.logout();
        }
    }
    
    private void navigateToLogin() {
        try {
            // Navigate to login fragment and clear back stack
            navController.navigate(R.id.action_dashboard_to_login);
        } catch (Exception e) {
            // If navigation fails, try alternative approach
            androidx.navigation.NavController controller = null;
            try {
                controller = ((com.sugboaid.donation.activities.MainActivity) requireActivity()).getNavController();
            } catch (Exception ignore) {}
            if (controller != null) {
                try { controller.setGraph(R.navigation.auth_nav_graph); } catch (Exception ignore) {}
            }
        }
    }
    
    private void updateNotificationBadge(int unreadCount) {
        if (notificationManager != null && tvNotificationBadge != null) {
            notificationManager.updateNotificationBadge(tvNotificationBadge, unreadCount);
        }
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setAction("Retry", v -> viewModel.refreshData())
                .show();
        }
    }

    private void showMessage(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    private String formatCurrency(double amount) {
        return String.format("₱%.2f", amount);
    }

    @Override
    protected void observeData() {
        // Delegate to a single observer setup to avoid duplicates
        observeViewModel();
    }

    @Override
    protected void refreshData() {
        com.sugboaid.utils.DiagnosticLogger.logDebug("DashboardFragment", "refreshData() override -> ViewModel.refreshData and notifications refresh");
        viewModel.refreshData();
        notificationViewModel.refreshNotifications();
    }

    @Override
    protected void showLoading() {
        if (rvRecentActivities != null) {
            loadingStateManager.showShimmerLoading((ViewGroup) rvRecentActivities.getParent(), 3);
        }
    }

    @Override
    protected void hideLoading() {
        if (rvRecentActivities != null) {
            loadingStateManager.hideLoading((ViewGroup) rvRecentActivities.getParent());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cleanup loading state manager
        if (loadingStateManager != null) {
            loadingStateManager.cleanup();
        }
    }
}