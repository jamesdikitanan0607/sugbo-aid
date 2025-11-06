package com.sugboaid.donation.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sugboaid.donation.R;
import com.sugboaid.donation.viewmodels.DonationViewModel;
import com.sugboaid.models.Donation;
import com.sugboaid.models.DonationType;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * POS Donation Fragment for recording cash and goods donations
 * Implements toggle between cash and goods modes with dynamic UI changes
 */
public class POSDonationFragment extends BaseFragment {

    private DonationViewModel viewModel;

    // Toggle and Mode Views
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton btnCashMode;
    private MaterialButton btnGoodsMode;

    // Cash Donation Views
    private LinearLayout layoutCashDonation;
    private TextInputLayout tilAmount;
    private TextInputEditText etAmount;
    private LinearLayout layoutQuickAmounts;
    private MaterialButton btnAmount100;
    private MaterialButton btnAmount500;
    private MaterialButton btnAmount1000;
    private MaterialButton btnAmount5000;

    // Goods Donation Views
    private LinearLayout layoutGoodsDonation;
    private MaterialCardView cardRice;
    private MaterialCardView cardWater;
    private MaterialCardView cardMedicine;
    private MaterialCardView cardClothes;
    
    // Quantity controls for goods
    private TextView tvRiceQuantity;
    private TextView tvWaterQuantity;
    private TextView tvMedicineQuantity;
    private TextView tvClothesQuantity;
    
    private ImageButton btnRiceDecrease;
    private ImageButton btnRiceIncrease;
    private ImageButton btnWaterDecrease;
    private ImageButton btnWaterIncrease;
    private ImageButton btnMedicineDecrease;
    private ImageButton btnMedicineIncrease;
    private ImageButton btnClothesDecrease;
    private ImageButton btnClothesIncrease;

    // Common Views
    private TextInputLayout tilDonorName;
    private TextInputEditText etDonorName;
    private MaterialButton btnSubmitDonation;
    private MaterialButton btnCancel;

    // State variables
    private DonationType currentMode = DonationType.CASH;
    private double cashAmount = 0.0;
    private Map<String, Integer> goodsQuantities = new HashMap<>();
    private NumberFormat currencyFormat;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pos_donation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Initialize ViewModel BEFORE super to ensure observeData() has it ready
        viewModel = new ViewModelProvider(this).get(DonationViewModel.class);

        super.onViewCreated(view, savedInstanceState);
        
        // Initialize currency formatter
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        
        // Initialize goods quantities
        initializeGoodsQuantities();
    }

    @Override
    protected void initViews(View view) {
        // Toggle group
        toggleGroup = view.findViewById(R.id.toggle_donation_type);
        btnCashMode = view.findViewById(R.id.btn_cash_mode);
        btnGoodsMode = view.findViewById(R.id.btn_goods_mode);

        // Cash donation views
        layoutCashDonation = view.findViewById(R.id.layout_cash_donation);
        tilAmount = view.findViewById(R.id.til_amount);
        etAmount = view.findViewById(R.id.et_amount);
        layoutQuickAmounts = view.findViewById(R.id.layout_quick_amounts);
        btnAmount100 = view.findViewById(R.id.btn_amount_100);
        btnAmount500 = view.findViewById(R.id.btn_amount_500);
        btnAmount1000 = view.findViewById(R.id.btn_amount_1000);
        btnAmount5000 = view.findViewById(R.id.btn_amount_5000);

        // Goods donation views
        layoutGoodsDonation = view.findViewById(R.id.layout_goods_donation);
        cardRice = view.findViewById(R.id.card_rice);
        cardWater = view.findViewById(R.id.card_water);
        cardMedicine = view.findViewById(R.id.card_medicine);
        cardClothes = view.findViewById(R.id.card_clothes);

        // Quantity displays
        tvRiceQuantity = view.findViewById(R.id.tv_rice_quantity);
        tvWaterQuantity = view.findViewById(R.id.tv_water_quantity);
        tvMedicineQuantity = view.findViewById(R.id.tv_medicine_quantity);
        tvClothesQuantity = view.findViewById(R.id.tv_clothes_quantity);

        // Quantity controls
        btnRiceDecrease = view.findViewById(R.id.btn_rice_decrease);
        btnRiceIncrease = view.findViewById(R.id.btn_rice_increase);
        btnWaterDecrease = view.findViewById(R.id.btn_water_decrease);
        btnWaterIncrease = view.findViewById(R.id.btn_water_increase);
        btnMedicineDecrease = view.findViewById(R.id.btn_medicine_decrease);
        btnMedicineIncrease = view.findViewById(R.id.btn_medicine_increase);
        btnClothesDecrease = view.findViewById(R.id.btn_clothes_decrease);
        btnClothesIncrease = view.findViewById(R.id.btn_clothes_increase);

        // Common views
        tilDonorName = view.findViewById(R.id.til_donor_name);
        etDonorName = view.findViewById(R.id.et_donor_name);
        btnSubmitDonation = view.findViewById(R.id.btn_submit_donation);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Set initial mode
        updateUIForMode(currentMode);
    }

    @Override
    protected void setupListeners() {
        // Toggle group listener
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_cash_mode) {
                    switchToMode(DonationType.CASH);
                } else if (checkedId == R.id.btn_goods_mode) {
                    switchToMode(DonationType.GOODS);
                }
            }
        });

        // Cash amount input listener with currency formatting
        etAmount.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isFormatting) return;
                
                try {
                    String cleanString = s.toString().replaceAll("[₱,\\s]", "");
                    if (!cleanString.isEmpty()) {
                        cashAmount = Double.parseDouble(cleanString);
                        
                        // Format the input with currency symbol and commas
                        isFormatting = true;
                        String formatted = formatCurrency(cashAmount);
                        etAmount.setText(formatted);
                        etAmount.setSelection(formatted.length());
                        isFormatting = false;
                    } else {
                        cashAmount = 0.0;
                    }
                    
                    // Clear any previous errors
                    tilAmount.setError(null);
                    validateInput();
                } catch (NumberFormatException e) {
                    cashAmount = 0.0;
                    validateInput();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Quick amount buttons
        btnAmount100.setOnClickListener(v -> setQuickAmount(100));
        btnAmount500.setOnClickListener(v -> setQuickAmount(500));
        btnAmount1000.setOnClickListener(v -> setQuickAmount(1000));
        btnAmount5000.setOnClickListener(v -> setQuickAmount(5000));

        // Goods quantity controls
        setupQuantityControls();

        // Action buttons
        btnSubmitDonation.setOnClickListener(v -> submitDonation());
        btnCancel.setOnClickListener(v -> navigateBack());
    }

    @Override
    protected void observeData() {
        // Observe donation submission result
        viewModel.getDonationSubmissionResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result.isSuccess()) {
                    // Navigate to success screen
                    navigateToSuccessScreen(result.getDonation());
                } else {
                    showToast("Failed to record donation: " + result.getErrorMessage());
                }
                viewModel.clearSubmissionResult();
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnSubmitDonation.setEnabled(!isLoading);
            if (isLoading) {
                btnSubmitDonation.setText("Recording...");
            } else {
                btnSubmitDonation.setText("Record Donation");
            }
        });
    }

    @Override
    protected void refreshData() {
        // Reset form to initial state
        resetForm();
    }

    private void initializeGoodsQuantities() {
        goodsQuantities.put("Rice", 0);
        goodsQuantities.put("Water", 0);
        goodsQuantities.put("Medicine", 0);
        goodsQuantities.put("Clothes", 0);
        updateQuantityDisplays();
    }

    private void switchToMode(DonationType mode) {
        if (currentMode != mode) {
            currentMode = mode;
            updateUIForMode(mode);
            validateInput();
        }
    }

    private void updateUIForMode(DonationType mode) {
        if (mode == DonationType.CASH) {
            layoutCashDonation.setVisibility(View.VISIBLE);
            layoutGoodsDonation.setVisibility(View.GONE);
            btnCashMode.setChecked(true);
            btnGoodsMode.setChecked(false);
        } else {
            layoutCashDonation.setVisibility(View.GONE);
            layoutGoodsDonation.setVisibility(View.VISIBLE);
            btnCashMode.setChecked(false);
            btnGoodsMode.setChecked(true);
        }
        
        // Apply entrance animations for the visible layout
        animateLayoutChange(mode);
    }

    private void animateLayoutChange(DonationType mode) {
        View targetLayout = mode == DonationType.CASH ? layoutCashDonation : layoutGoodsDonation;
        
        // Fade in animation
        targetLayout.setAlpha(0f);
        targetLayout.animate()
            .alpha(1f)
            .setDuration(300)
            .start();
    }

    private void setQuickAmount(int amount) {
        cashAmount = amount;
        String formatted = formatCurrency(amount);
        etAmount.setText(formatted);
        validateInput();
        
        // Animate button press
        animateButtonPress(getQuickAmountButton(amount));
        
        // Clear any previous errors
        tilAmount.setError(null);
    }

    private MaterialButton getQuickAmountButton(int amount) {
        switch (amount) {
            case 100: return btnAmount100;
            case 500: return btnAmount500;
            case 1000: return btnAmount1000;
            case 5000: return btnAmount5000;
            default: return btnAmount100;
        }
    }

    protected void animateButtonPress(View button) {
        button.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction(() -> {
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
            })
            .start();
    }

    private void setupQuantityControls() {
        // Rice controls
        btnRiceDecrease.setOnClickListener(v -> adjustQuantity("Rice", -1));
        btnRiceIncrease.setOnClickListener(v -> adjustQuantity("Rice", 1));

        // Water controls
        btnWaterDecrease.setOnClickListener(v -> adjustQuantity("Water", -1));
        btnWaterIncrease.setOnClickListener(v -> adjustQuantity("Water", 1));

        // Medicine controls
        btnMedicineDecrease.setOnClickListener(v -> adjustQuantity("Medicine", -1));
        btnMedicineIncrease.setOnClickListener(v -> adjustQuantity("Medicine", 1));

        // Clothes controls
        btnClothesDecrease.setOnClickListener(v -> adjustQuantity("Clothes", -1));
        btnClothesIncrease.setOnClickListener(v -> adjustQuantity("Clothes", 1));
    }

    private void adjustQuantity(String item, int delta) {
        int currentQuantity = goodsQuantities.get(item);
        int newQuantity = currentQuantity + delta;
        
        // Apply constraints
        newQuantity = Math.max(0, newQuantity);
        newQuantity = Math.min(getMaxQuantityPerItem(), newQuantity);
        
        // Only update if quantity actually changed
        if (newQuantity != currentQuantity) {
            goodsQuantities.put(item, newQuantity);
            updateQuantityDisplays();
            validateInput();
            
            // Animate the quantity change
            animateQuantityChange(item, delta > 0);
            
            // Show feedback for limits
            if (newQuantity == getMaxQuantityPerItem() && delta > 0) {
                showToast("Maximum " + getMaxQuantityPerItem() + " items per type");
            }
        }
    }

    private void updateQuantityDisplays() {
        tvRiceQuantity.setText(String.valueOf(goodsQuantities.get("Rice")));
        tvWaterQuantity.setText(String.valueOf(goodsQuantities.get("Water")));
        tvMedicineQuantity.setText(String.valueOf(goodsQuantities.get("Medicine")));
        tvClothesQuantity.setText(String.valueOf(goodsQuantities.get("Clothes")));
        
        // Update card appearance based on quantity
        updateCardAppearance("Rice", cardRice, tvRiceQuantity);
        updateCardAppearance("Water", cardWater, tvWaterQuantity);
        updateCardAppearance("Medicine", cardMedicine, tvMedicineQuantity);
        updateCardAppearance("Clothes", cardClothes, tvClothesQuantity);
    }

    private void animateQuantityChange(String item, boolean isIncrease) {
        TextView quantityView = getQuantityTextView(item);
        if (quantityView != null) {
            // Scale animation for quantity change
            quantityView.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150)
                .withEndAction(() -> {
                    quantityView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start();
                })
                .start();
        }
    }

    private TextView getQuantityTextView(String item) {
        switch (item) {
            case "Rice": return tvRiceQuantity;
            case "Water": return tvWaterQuantity;
            case "Medicine": return tvMedicineQuantity;
            case "Clothes": return tvClothesQuantity;
            default: return null;
        }
    }

    private void validateInput() {
        boolean isValid = false;
        
        if (currentMode == DonationType.CASH) {
            isValid = cashAmount >= getMinimumDonationAmount() && 
                     cashAmount <= getMaximumDonationAmount();
        } else {
            // Check if at least one goods item has quantity > 0
            isValid = goodsQuantities.values().stream().anyMatch(quantity -> quantity > 0);
        }
        
        btnSubmitDonation.setEnabled(isValid);
        
        // Update button appearance based on validation
        if (isValid) {
            btnSubmitDonation.setAlpha(1.0f);
        } else {
            btnSubmitDonation.setAlpha(0.6f);
        }
    }

    private void submitDonation() {
        if (!validateDonationData()) {
            return;
        }

        // Get donor name or use "Anonymous"
        String donorName = etDonorName.getText() != null ? 
            etDonorName.getText().toString().trim() : "";
        if (donorName.isEmpty()) {
            donorName = "Anonymous";
        }

        // Create donation object
        Donation donation = new Donation();
        donation.setDonorName(donorName);
        donation.setType(currentMode);
        
        if (currentMode == DonationType.CASH) {
            donation.setAmount(cashAmount);
            donation.setDescription("Cash donation for relief operations");
        } else {
            // Calculate total items for goods donation
            int totalItems = goodsQuantities.values().stream().mapToInt(Integer::intValue).sum();
            donation.setAmount(totalItems);
            donation.setDescription(buildGoodsDescription());
        }

        // Submit through ViewModel
        viewModel.submitDonation(donation);
    }

    private boolean validateDonationData() {
        if (currentMode == DonationType.CASH) {
            return validateCashAmount(cashAmount);
        } else {
            return validateGoodsQuantities();
        }
    }

    private String buildGoodsDescription() {
        StringBuilder description = new StringBuilder("Goods donation: ");
        boolean first = true;
        
        for (Map.Entry<String, Integer> entry : goodsQuantities.entrySet()) {
            if (entry.getValue() > 0) {
                if (!first) {
                    description.append(", ");
                }
                description.append(entry.getValue()).append(" ").append(entry.getKey());
                first = false;
            }
        }
        
        return description.toString();
    }

    private void resetForm() {
        // Reset cash amount
        cashAmount = 0.0;
        etAmount.setText("");
        
        // Reset goods quantities
        initializeGoodsQuantities();
        
        // Reset donor name
        etDonorName.setText("");
        
        // Reset to cash mode
        currentMode = DonationType.CASH;
        updateUIForMode(currentMode);
        
        // Clear any errors
        tilAmount.setError(null);
        
        validateInput();
    }

    /**
     * Format amount as Philippine Peso currency
     * @param amount The amount to format
     * @return Formatted currency string
     */
    private String formatCurrency(double amount) {
        if (amount == 0) return "";
        return String.format("₱%,.0f", amount);
    }

    /**
     * Parse currency string to double value
     * @param currencyString The formatted currency string
     * @return Parsed double value
     */
    private double parseCurrency(String currencyString) {
        try {
            String cleanString = currencyString.replaceAll("[₱,\\s]", "");
            return cleanString.isEmpty() ? 0.0 : Double.parseDouble(cleanString);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Validate cash amount input
     * @param amount The amount to validate
     * @return true if valid, false otherwise
     */
    private boolean validateCashAmount(double amount) {
        if (amount <= 0) {
            tilAmount.setError("Please enter a valid amount");
            return false;
        }
        
        if (amount > 1000000) { // Maximum limit of 1 million pesos
            tilAmount.setError("Amount cannot exceed ₱1,000,000");
            return false;
        }
        
        tilAmount.setError(null);
        return true;
    }

    /**
     * Get minimum donation amount
     * @return Minimum donation amount
     */
    private double getMinimumDonationAmount() {
        return 1.0; // Minimum 1 peso
    }

    /**
     * Get maximum donation amount
     * @return Maximum donation amount
     */
    private double getMaximumDonationAmount() {
        return 1000000.0; // Maximum 1 million pesos
    }

    /**
     * Update card appearance based on quantity
     * @param itemName The item name
     * @param card The card view
     * @param quantityView The quantity text view
     */
    private void updateCardAppearance(String itemName, MaterialCardView card, TextView quantityView) {
        int quantity = goodsQuantities.get(itemName);
        
        if (quantity > 0) {
            // Highlight selected items
            card.setStrokeColor(getResources().getColor(R.color.primary_blue, null));
            card.setStrokeWidth(4);
            card.setCardBackgroundColor(getResources().getColor(R.color.primary_blue_20, null));
            quantityView.setTextColor(getResources().getColor(R.color.primary_blue, null));
        } else {
            // Reset to default appearance
            card.setStrokeColor(getResources().getColor(R.color.primary_blue_20, null));
            card.setStrokeWidth(2);
            card.setCardBackgroundColor(getResources().getColor(R.color.white, null));
            quantityView.setTextColor(getResources().getColor(R.color.text_primary, null));
        }
    }

    /**
     * Get total selected goods items
     * @return Total quantity of all selected items
     */
    private int getTotalGoodsQuantity() {
        return goodsQuantities.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Get formatted goods summary
     * @return Formatted string showing selected items
     */
    private String getGoodsSummary() {
        StringBuilder summary = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, Integer> entry : goodsQuantities.entrySet()) {
            if (entry.getValue() > 0) {
                if (!first) {
                    summary.append(", ");
                }
                summary.append(entry.getValue()).append(" ").append(entry.getKey());
                first = false;
            }
        }
        
        return summary.toString();
    }

    /**
     * Validate goods quantities
     * @return true if at least one item is selected
     */
    private boolean validateGoodsQuantities() {
        int totalItems = getTotalGoodsQuantity();
        
        if (totalItems == 0) {
            showToast(getString(R.string.please_select_at_least_one_item));
            return false;
        }
        
        if (totalItems > 1000) { // Maximum limit
            showToast("Total items cannot exceed 1000");
            return false;
        }
        
        return true;
    }

    /**
     * Get maximum quantity per item
     * @return Maximum quantity allowed per item
     */
    private int getMaxQuantityPerItem() {
        return 100; // Maximum 100 units per item type
    }

    /**
     * Navigate to success screen with donation details
     * @param donation The successfully submitted donation
     */
    private void navigateToSuccessScreen(Donation donation) {
        if (navController != null && donation != null) {
            try {
                Bundle args = new Bundle();
                args.putSerializable("donation", donation);
                navController.navigate(R.id.action_pos_to_success, args);
            } catch (Exception e) {
                // Fallback: show success message and navigate back
                showToast("Donation recorded successfully!");
                navigateBack();
            }
        } else {
            // Fallback: show success message and navigate back
            showToast("Donation recorded successfully!");
            navigateBack();
        }
    }
}