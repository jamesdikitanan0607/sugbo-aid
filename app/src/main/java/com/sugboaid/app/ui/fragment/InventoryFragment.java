package com.sugboaid.app.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sugboaid.app.R;
import com.sugboaid.app.data.model.InventoryItem;
import com.sugboaid.app.ui.adapter.InventoryAdapter;
import com.sugboaid.app.ui.viewmodel.InventoryViewModel;
import java.util.List;

public class InventoryFragment extends Fragment implements InventoryAdapter.OnItemClickListener {

    private InventoryViewModel viewModel;
    private InventoryAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchInput;
    private ChipGroup filterChipGroup;
    private FloatingActionButton addItemFab;

    // Summary cards views (simplified for this implementation)
    private View cardTotalItems;
    private View cardCategories;
    private View cardLowStock;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupViewModel();
        setupListeners();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.inventoryRecyclerView);
        searchInput = view.findViewById(R.id.searchInput);
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
        addItemFab = view.findViewById(R.id.addItemFab);

        cardTotalItems = view.findViewById(R.id.cardTotalItems);
        cardCategories = view.findViewById(R.id.cardCategories);
        cardLowStock = view.findViewById(R.id.cardLowStock);
    }

    private void setupRecyclerView() {
        adapter = new InventoryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        // Observe inventory items
        viewModel.getInventoryItems().observe(getViewLifecycleOwner(), items -> {
            adapter.setItems(items);
        });

        // Observe stats
        viewModel.getInventoryStats().observe(getViewLifecycleOwner(), stats -> {
            updateSummaryCard(cardTotalItems, "Total Items", String.valueOf(stats.totalItems), "items");
            updateSummaryCard(cardCategories, "Categories", String.valueOf(stats.categories), "types");
            updateSummaryCard(cardLowStock, "Low Stock", String.valueOf(stats.lowStock), "items");
        });

        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void updateSummaryCard(View cardView, String title, String value, String subtitle) {
        TextView titleView = cardView.findViewById(R.id.cardTitle);
        TextView valueView = cardView.findViewById(R.id.cardValue);
        TextView subtitleView = cardView.findViewById(R.id.cardSubtitle);

        if (titleView != null)
            titleView.setText(title);
        if (valueView != null)
            valueView.setText(value);
        if (subtitleView != null)
            subtitleView.setText(subtitle);
    }

    private void setupListeners() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchInventory(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String category = "All Items";
            if (checkedId == R.id.chipHealthy)
                category = "Healthy";
            else if (checkedId == R.id.chipModerate)
                category = "Moderate";
            else if (checkedId == R.id.chipLowStock)
                category = "Low Stock";

            viewModel.filterByCategory(category);
        });

        addItemFab.setOnClickListener(v -> {
            Snackbar.make(v, "Add Item feature coming soon", Snackbar.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onItemClick(InventoryItem item) {
        // Handle item click
        Snackbar.make(requireView(), "Clicked: " + item.getName(), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadInventory();
    }
}