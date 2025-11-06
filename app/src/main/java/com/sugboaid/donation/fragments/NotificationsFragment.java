package com.sugboaid.donation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.sugboaid.donation.R;
import com.sugboaid.donation.adapters.NotificationAdapter;
import com.sugboaid.donation.viewmodels.NotificationViewModel;
import com.sugboaid.donation.views.AnimatedGradientButton;
import com.sugboaid.models.AppNotification;
import java.util.List;

/**
 * Fragment for displaying and managing notifications
 */
public class NotificationsFragment extends BaseFragment {
    
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;
    
    // UI Components
    private RecyclerView rvNotifications;
    private TextView tvNotificationCount;
    private AnimatedGradientButton btnMarkAllRead;
    private View emptyStateLayout;
    private ProgressBar progressBar;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupSwipeToDelete();
        observeViewModel();
        setupClickListeners();
        
        // Generate sample notifications for testing if empty
        if (!viewModel.hasNotifications()) {
            viewModel.generateSampleNotifications();
        }
    }
    
    protected void initViews(View view) {
        rvNotifications = view.findViewById(R.id.rvNotifications);
        tvNotificationCount = view.findViewById(R.id.tvNotificationCount);
        btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        progressBar = view.findViewById(R.id.progressBar);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
    }
    
    private void setupRecyclerView() {
        adapter = new NotificationAdapter();
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(adapter);
        
        // Set up adapter listeners
        adapter.setOnNotificationClickListener(new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(AppNotification notification, int position) {
                viewModel.handleNotificationClick(notification);
            }
            
            @Override
            public void onNotificationLongClick(AppNotification notification, int position) {
                viewModel.handleNotificationLongClick(notification);
            }
        });
        
        adapter.setOnNotificationSwipeListener(new NotificationAdapter.OnNotificationSwipeListener() {
            @Override
            public void onNotificationSwiped(AppNotification notification, int position) {
                viewModel.handleNotificationSwipe(notification);
            }
        });
    }
    
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                List<AppNotification> notifications = adapter.getNotifications();
                
                if (position >= 0 && position < notifications.size()) {
                    AppNotification notification = notifications.get(position);
                    
                    // Animate the swipe out and then remove
                    if (viewHolder instanceof NotificationAdapter.NotificationViewHolder) {
                        NotificationAdapter.NotificationViewHolder holder = 
                            (NotificationAdapter.NotificationViewHolder) viewHolder;
                        holder.animateSwipeOut(() -> {
                            viewModel.removeNotification(notification.getId());
                        });
                    } else {
                        viewModel.removeNotification(notification.getId());
                    }
                }
            }
            
            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.3f; // Require 30% swipe to trigger
            }
        };
        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(rvNotifications);
    }
    
    @Override
    protected void setupListeners() {
        setupClickListeners();
    }

    @Override
    protected void refreshData() {
        if (viewModel != null) {
            viewModel.refreshNotifications();
        }
    }

    @Override
    protected void observeData() {
        observeViewModel();
    }

    private void observeViewModel() {
        // Observe notifications list
        viewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            adapter.setNotifications(notifications);
            updateNotificationCount(notifications != null ? notifications.size() : 0);
        });
        
        // Observe unread count
        viewModel.getUnreadCount().observe(getViewLifecycleOwner(), unreadCount -> {
            updateUnreadCount(unreadCount != null ? unreadCount : 0);
        });
        
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe empty state
        viewModel.getShowEmptyState().observe(getViewLifecycleOwner(), showEmpty -> {
            emptyStateLayout.setVisibility(showEmpty ? View.VISIBLE : View.GONE);
            rvNotifications.setVisibility(showEmpty ? View.GONE : View.VISIBLE);
        });
        
        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showToast(errorMessage);
                viewModel.clearErrorMessage();
            }
        });
    }
    
    private void setupClickListeners() {
        btnMarkAllRead.setOnClickListener(v -> {
            viewModel.markAllAsRead();
            showToast("All notifications marked as read");
        });
    }
    
    private void updateNotificationCount(int totalCount) {
        if (totalCount > 0) {
            tvNotificationCount.setText(String.valueOf(totalCount));
            tvNotificationCount.setVisibility(View.VISIBLE);
        } else {
            tvNotificationCount.setVisibility(View.GONE);
        }
    }
    
    private void updateUnreadCount(int unreadCount) {
        if (unreadCount > 0) {
            btnMarkAllRead.setVisibility(View.VISIBLE);
            btnMarkAllRead.setText(String.format("Mark All Read (%d)", unreadCount));
        } else {
            btnMarkAllRead.setVisibility(View.GONE);
        }
    }
    
    // Public methods for external notification management
    public void addDonationNotification(String donorName, String amount) {
        if (viewModel != null) {
            viewModel.addDonationNotification(donorName, amount);
        }
    }
    
    public void addLowInventoryNotification(String itemName, int stock) {
        if (viewModel != null) {
            viewModel.addLowInventoryNotification(itemName, stock);
        }
    }
    
    public void addDistributionNotification(String location, int families) {
        if (viewModel != null) {
            viewModel.addDistributionNotification(location, families);
        }
    }
    
    public void addSystemUpdateNotification(String title, String message) {
        if (viewModel != null) {
            viewModel.addSystemUpdateNotification(title, message);
        }
    }
    
    public void addAlertNotification(String title, String message) {
        if (viewModel != null) {
            viewModel.addAlertNotification(title, message);
        }
    }
    
    public void addInfoNotification(String title, String message) {
        if (viewModel != null) {
            viewModel.addInfoNotification(title, message);
        }
    }
    
    public int getUnreadNotificationCount() {
        if (viewModel != null) {
            Integer count = viewModel.getUnreadCount().getValue();
            return count != null ? count : 0;
        }
        return 0;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh notifications when fragment becomes visible
        if (viewModel != null) {
            viewModel.refreshNotifications();
        }
    }
    
    protected void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}