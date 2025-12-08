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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sugboaid.donation.R;
import com.sugboaid.donation.adapters.UserManagementAdapter;
import com.sugboaid.donation.viewmodels.AdminViewModel;
import com.sugboaid.donation.views.StatisticsCard;
import com.sugboaid.models.User;

import java.util.List;

public class AdminDashboardFragment extends BaseFragment {
    private AdminViewModel adminViewModel;
    private NavController navController;

    private TextView tvWelcome;
    private StatisticsCard cardTotalUsers;
    private StatisticsCard cardActiveUsers;
    private StatisticsCard cardTotalDonations;
    private StatisticsCard cardPendingApprovals;
    private RecyclerView rvUserList;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    protected void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tv_admin_welcome);
        cardTotalUsers = view.findViewById(R.id.card_total_users);
        cardActiveUsers = view.findViewById(R.id.card_active_users);
        cardTotalDonations = view.findViewById(R.id.card_total_donations);
        cardPendingApprovals = view.findViewById(R.id.card_pending_approvals);
        rvUserList = view.findViewById(R.id.rv_user_list);
        progressBar = view.findViewById(R.id.progress_bar);

        rvUserList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    protected void setupListeners() {
        // Quick actions can be wired later
    }

    @Override
    protected void observeData() {
        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        if (getView() != null) {
            navController = Navigation.findNavController(getView());
        }

        adminViewModel.isAdmin().observe(getViewLifecycleOwner(), isAdmin -> {
            if (isAdmin == null || !isAdmin) {
                if (navController != null) {
                    try { navController.navigate(R.id.dashboardFragment); } catch (Exception ignored) {}
                }
            }
        });

        adminViewModel.getTotalUsers().observe(getViewLifecycleOwner(), count -> {
            if (cardTotalUsers != null) cardTotalUsers.setStatisticsData(getString(R.string.total_users), String.valueOf(count != null ? count : 0), "+0%");
        });

        adminViewModel.getActiveUsers().observe(getViewLifecycleOwner(), count -> {
            if (cardActiveUsers != null) cardActiveUsers.setStatisticsData(getString(R.string.active_users), String.valueOf(count != null ? count : 0), "+0%");
        });

        adminViewModel.getSystemTotalDonations().observe(getViewLifecycleOwner(), total -> {
            String display = total != null ? ("\u20B1" + String.format("%.2f", total)) : "\u20B10.00";
            if (cardTotalDonations != null) cardTotalDonations.setStatisticsData(getString(R.string.donate), display, "+0%");
        });

        adminViewModel.getPendingApprovals().observe(getViewLifecycleOwner(), count -> {
            if (cardPendingApprovals != null) cardPendingApprovals.setStatisticsData(getString(R.string.pending_approvals), String.valueOf(count != null ? count : 0), "+0%");
        });

        adminViewModel.getUserList().observe(getViewLifecycleOwner(), this::setupUserListAdapter);
    }

    private void setupUserListAdapter(List<User> users) {
        if (rvUserList == null) return;
        UserManagementAdapter adapter = new UserManagementAdapter(users != null ? users : java.util.Collections.emptyList(), new UserManagementAdapter.OnUserActionListener() {
            @Override
            public void onRoleChange(User user) {
                // Placeholder for role change dialog
            }

            @Override
            public void onStatusToggle(User user) {
                // Placeholder for status toggle
            }
        });
        rvUserList.setAdapter(adapter);
    }

    @Override
    protected void refreshData() {
        // Refresh admin dashboard data
        if (adminViewModel != null) {
            adminViewModel.refreshData();
        }
    }
}
