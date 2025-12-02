package com.sugboaid.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.sugboaid.app.R;
import com.sugboaid.app.manager.AuthManager;
import com.sugboaid.app.manager.POSManager;
import com.sugboaid.app.manager.InventoryManager;
import com.sugboaid.app.ui.adapter.DashboardCardAdapter;
import com.sugboaid.app.data.model.DashboardCard;
import com.sugboaid.app.util.Constants;
import com.sugboaid.app.util.ThemeUtils;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    
    private RecyclerView dashboardRecyclerView;
    private TextView welcomeText, summaryText;
    private DashboardCardAdapter adapter;
    
    private AuthManager authManager;
    private POSManager posManager;
    private InventoryManager inventoryManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authManager = AuthManager.getInstance(requireContext());
        posManager = POSManager.getInstance(requireContext());
        inventoryManager = InventoryManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        loadDashboardData();
    }

    private void initViews(View view) {
        dashboardRecyclerView = view.findViewById(R.id.dashboardRecyclerView);
        welcomeText = view.findViewById(R.id.welcomeText);
        summaryText = view.findViewById(R.id.summaryText);
    }

    private void setupRecyclerView() {
        boolean isTablet = ThemeUtils.isTablet(requireContext());
        int spanCount = isTablet ? 3 : 2;
        
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), spanCount);
        dashboardRecyclerView.setLayoutManager(layoutManager);
        
        adapter = new DashboardCardAdapter(new ArrayList<>(), this::onDashboardCardClick);
        dashboardRecyclerView.setAdapter(adapter);
    }

    private void loadDashboardData() {
        // Update welcome message
        String userName = authManager.getCurrentUser().getName();
        String userRole = authManager.getCurrentUser().getRole();
        welcomeText.setText(String.format("Welcome back, %s", userName));
        summaryText.setText(String.format("Role: %s", userRole));

        // Load dashboard cards based on user role
        List<DashboardCard> cards = generateDashboardCards();
        adapter.updateCards(cards);
    }

    private List<DashboardCard> generateDashboardCards() {
        List<DashboardCard> cards = new ArrayList<>();
        String userRole = authManager.getCurrentUser().getRole();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

        // Total Donations Card
        if (authManager.canAccessFeature(Constants.FEATURE_DONATION_HISTORY)) {
            double totalAmount = posManager.getTotalDonationsAmount();
            int totalCount = posManager.getTotalDonationsCount();
            
            cards.add(new DashboardCard(
                "Total Donations",
                currencyFormat.format(totalAmount),
                String.format("%d donations", totalCount),
                R.drawable.ic_donation,
                R.color.emerald_green
            ));
        }

        // Inventory Summary Card
        if (authManager.canAccessFeature(Constants.FEATURE_INVENTORY_MANAGEMENT)) {
            double inventoryValue = inventoryManager.getTotalInventoryValue();
            int itemCount = inventoryManager.getTotalItemsCount();
            int lowStockCount = inventoryManager.getLowStockItems().size();
            
            cards.add(new DashboardCard(
                "Inventory Value",
                currencyFormat.format(inventoryValue),
                String.format("%d items", itemCount),
                R.drawable.ic_inventory,
                R.color.cebu_blue
            ));
            
            if (lowStockCount > 0) {
                cards.add(new DashboardCard(
                    "Low Stock Alert",
                    String.valueOf(lowStockCount),
                    "items need restocking",
                    R.drawable.ic_warning,
                    R.color.warning
                ));
            }
        }

        // Recent Activity Card
        cards.add(new DashboardCard(
            "Recent Activity",
            "View All",
            "Latest transactions",
            R.drawable.ic_history,
            R.color.info
        ));

        // Quick Actions based on role
        if (userRole.equals(Constants.ROLE_DONOR)) {
            cards.add(new DashboardCard(
                "Make Donation",
                "Quick Donate",
                "Help those in need",
                R.drawable.ic_add_donation,
                R.color.warm_yellow
            ));
        } else if (userRole.equals(Constants.ROLE_ORGANIZATION) || userRole.equals(Constants.ROLE_VOLUNTEER)) {
            cards.add(new DashboardCard(
                "Process Donation",
                "POS System",
                "Record new donations",
                R.drawable.ic_pos,
                R.color.emerald_green
            ));
            
            cards.add(new DashboardCard(
                "Scan QR Code",
                "Quick Scan",
                "Verify donations",
                R.drawable.ic_qr_scan,
                R.color.cebu_blue
            ));
        }

        // Transparency Card (available to all)
        cards.add(new DashboardCard(
            "Transparency",
            "View Reports",
            "Public accountability",
            R.drawable.ic_transparency,
            R.color.info
        ));

        return cards;
    }

    private void onDashboardCardClick(DashboardCard card) {
        // Handle card clicks based on card title
        switch (card.getTitle()) {
            case "Total Donations":
                // Navigate to donations history
                break;
            case "Inventory Value":
                // Navigate to inventory
                break;
            case "Make Donation":
                // Navigate to donation form
                break;
            case "Process Donation":
                // Navigate to POS system
                break;
            case "Scan QR Code":
                // Start QR scanner
                break;
            case "Transparency":
                // Navigate to transparency view
                break;
            case "Recent Activity":
                // Show recent activity
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadDashboardData();
    }
}