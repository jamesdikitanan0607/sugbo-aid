package com.sugboaid.donation.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.sugboaid.donation.R;
import com.sugboaid.donation.adapters.TransactionAdapter;
import com.sugboaid.donation.viewmodels.ReportsViewModel;
import com.sugboaid.donation.views.AnimatedGradientButton;
import com.sugboaid.models.Transaction;
import com.sugboaid.utils.ExportUtils;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for displaying reports and transaction history with filtering and export functionality
 */
public class ReportsFragment extends BaseFragment implements TransactionAdapter.OnTransactionClickListener {
    
    private ReportsViewModel viewModel;
    private TransactionAdapter adapter;
    
    // UI Components
    private TextView tvTotalTransactions;
    private TextView tvTotalValue;
    private TextView tvTransactionCount;
    private ChipGroup chipGroupFilters;
    private Chip chipAll, chipCash, chipGoods, chipDistribution;
    private RecyclerView recyclerTransactions;
    private LinearLayout layoutEmptyState;
    private LinearLayout layoutLoading;
    private ImageButton btnRefresh;
    private AnimatedGradientButton btnExportPdf;
    private AnimatedGradientButton btnExportCsv;
    
    private NumberFormat currencyFormat;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        currencyFormat.setCurrency(java.util.Currency.getInstance("PHP"));
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupViewModel();
        setupRecyclerView();
        setupFilterChips();
        setupClickListeners();
        observeViewModel();
        
        // Load initial data
        viewModel.loadTransactions();
    }
    
    private void initializeViews(View view) {
        tvTotalTransactions = view.findViewById(R.id.tv_total_transactions);
        tvTotalValue = view.findViewById(R.id.tv_total_value);
        tvTransactionCount = view.findViewById(R.id.tv_transaction_count);
        
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        chipAll = view.findViewById(R.id.chip_all);
        chipCash = view.findViewById(R.id.chip_cash);
        chipGoods = view.findViewById(R.id.chip_goods);
        chipDistribution = view.findViewById(R.id.chip_distribution);
        
        recyclerTransactions = view.findViewById(R.id.recycler_transactions);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        layoutLoading = view.findViewById(R.id.layout_loading);
        
        btnRefresh = view.findViewById(R.id.btn_refresh);
        btnExportPdf = view.findViewById(R.id.btn_export_pdf);
        btnExportCsv = view.findViewById(R.id.btn_export_csv);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ReportsViewModel.class);
    }
    
    private void setupRecyclerView() {
        adapter = new TransactionAdapter(requireContext());
        adapter.setOnTransactionClickListener(this);
        
        recyclerTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTransactions.setAdapter(adapter);
        recyclerTransactions.setHasFixedSize(true);
    }
    
    private void setupFilterChips() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // No chip selected, show all
                viewModel.filterAllTransactions();
                return;
            }
            
            int checkedId = checkedIds.get(0);
            
            if (checkedId == R.id.chip_all) {
                viewModel.filterAllTransactions();
            } else if (checkedId == R.id.chip_cash) {
                viewModel.filterCashDonations();
            } else if (checkedId == R.id.chip_goods) {
                viewModel.filterGoodsDonations();
            } else if (checkedId == R.id.chip_distribution) {
                viewModel.filterDistributions();
            }
        });
        
        // Set initial selection
        chipAll.setChecked(true);
    }
    
    private void setupClickListeners() {
        btnRefresh.setOnClickListener(v -> {
            viewModel.refreshData();
            showToast("Refreshing transaction data...");
        });
        
        btnExportPdf.setOnClickListener(v -> {
            if (validateExportConditions()) {
                exportToPdf();
            }
        });
        
        btnExportCsv.setOnClickListener(v -> {
            if (validateExportConditions()) {
                exportToCsv();
            }
        });
    }

    @Override
    protected void initViews(View view) {
        initializeViews(view);
        setupRecyclerView();
        setupViewModel();
    }
    
    @Override
    protected void setupListeners() {
        setupClickListeners();
    }

    @Override
    protected void refreshData() {
        if (viewModel != null) {
            // Use existing method name
            viewModel.loadTransactions();
        }
    }

    @Override
    protected void observeData() {
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getFilteredTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                adapter.setTransactions(transactions);
                updateTransactionCount(transactions.size());
                updateEmptyState(transactions.isEmpty());
            }
        });
        
        viewModel.getTotalTransactions().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                tvTotalTransactions.setText(String.valueOf(count));
            }
        });
        
        viewModel.getTotalValue().observe(getViewLifecycleOwner(), value -> {
            if (value != null) {
                String formattedValue = String.format(Locale.getDefault(), "₱%.2f", value);
                tvTotalValue.setText(formattedValue);
            }
        });
        
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                layoutLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });
        
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showToast("Error: " + errorMessage);
                viewModel.clearError();
            }
        });
    }
    
    private void updateTransactionCount(int count) {
        String countText = count == 1 ? "1 transaction" : count + " transactions";
        tvTransactionCount.setText(countText);
    }
    
    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerTransactions.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerTransactions.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }
    
    private void exportToPdf() {
        try {
            // Get transactions for export (validation already done)
            List<Transaction> transactions = viewModel.getTransactionsForExport();
            
            // Show loading state
            btnExportPdf.setEnabled(false);
            btnExportPdf.setText("Generating PDF...");
            
            // Generate PDF in background thread
            new Thread(() -> {
                try {
                    String filterType = viewModel.getCurrentFilterType();
                    Intent shareIntent = ExportUtils.exportToPDF(requireContext(), transactions, filterType);
                    
                    // Switch back to main thread for UI updates
                    requireActivity().runOnUiThread(() -> {
                        btnExportPdf.setEnabled(true);
                        btnExportPdf.setText("Export PDF");
                        
                        if (shareIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                            startActivity(shareIntent);
                            showExportSuccessDialog("PDF", transactions.size(), filterType);
                        } else {
                            showExportErrorDialog("No App Available", 
                                "No app is available to handle PDF export. Please install a PDF viewer or file manager app.");
                        }
                    });
                    
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        btnExportPdf.setEnabled(true);
                        btnExportPdf.setText("Export PDF");
                        showExportErrorDialog("Export Failed", 
                            "Failed to generate PDF report: " + e.getMessage() + 
                            "\n\nPlease try again or contact support if the problem persists.");
                    });
                }
            }).start();
            
        } catch (Exception e) {
            btnExportPdf.setEnabled(true);
            btnExportPdf.setText("Export PDF");
            showExportErrorDialog("Export Failed", 
                "Failed to start PDF export: " + e.getMessage() + 
                "\n\nPlease check your device storage and try again.");
        }
    }
    
    private void exportToCsv() {
        try {
            // Get transactions for export (validation already done)
            List<Transaction> transactions = viewModel.getTransactionsForExport();
            
            // Show loading state
            btnExportCsv.setEnabled(false);
            btnExportCsv.setText("Generating CSV...");
            
            // Generate CSV in background thread
            new Thread(() -> {
                try {
                    String filterType = viewModel.getCurrentFilterType();
                    Intent shareIntent = ExportUtils.exportToCSV(requireContext(), transactions, filterType);
                    
                    // Switch back to main thread for UI updates
                    requireActivity().runOnUiThread(() -> {
                        btnExportCsv.setEnabled(true);
                        btnExportCsv.setText("Export CSV");
                        
                        if (shareIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                            startActivity(shareIntent);
                            showExportSuccessDialog("CSV", transactions.size(), filterType);
                        } else {
                            showExportErrorDialog("No App Available", 
                                "No app is available to handle CSV export. Please install a spreadsheet app or file manager.");
                        }
                    });
                    
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        btnExportCsv.setEnabled(true);
                        btnExportCsv.setText("Export CSV");
                        showExportErrorDialog("Export Failed", 
                            "Failed to generate CSV report: " + e.getMessage() + 
                            "\n\nPlease try again or contact support if the problem persists.");
                    });
                }
            }).start();
            
        } catch (Exception e) {
            btnExportCsv.setEnabled(true);
            btnExportCsv.setText("Export CSV");
            showExportErrorDialog("Export Failed", 
                "Failed to start CSV export: " + e.getMessage() + 
                "\n\nPlease check your device storage and try again.");
        }
    }
    
    @Override
    public void onTransactionClick(Transaction transaction) {
        if (transaction != null) {
            showTransactionDetails(transaction);
        }
    }
    
    @Override
    public void onReceiptClick(Transaction transaction) {
        if (transaction != null && transaction.getReceiptId() != null) {
            showReceiptDetails(transaction);
        }
    }
    
    private void showTransactionDetails(Transaction transaction) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Transaction Details");
        
        String details = String.format(Locale.getDefault(),
            "Transaction ID: %s\n\n" +
            "Donor Information:\n" +
            "• Name: %s\n" +
            "• Type: %s\n\n" +
            "Transaction Information:\n" +
            "• Amount: %s\n" +
            "• Date: %s\n" +
            "• Campaign: %s\n" +
            "• Description: %s\n\n" +
            "Verification Status:\n" +
            "• Status: %s\n" +
            "• Receipt ID: %s\n\n" +
            "Additional Information:\n" +
            "• Transaction Type: %s\n" +
            "• Processing Time: %s",
            transaction.getId(),
            transaction.getDonor() != null ? transaction.getDonor() : "Anonymous Donor",
            getTransactionTypeText(transaction.getType()),
            transaction.getAmount(),
            transaction.getFormattedDate(),
            transaction.getCampaign() != null ? transaction.getCampaign() : "General Fund",
            transaction.getDescription() != null ? transaction.getDescription() : "No description provided",
            transaction.isVerified() ? "Verified ✓" : "Pending Verification ⏳",
            transaction.getReceiptId() != null ? transaction.getReceiptId() : "Not available",
            transaction.getTypeIcon() + " " + getTransactionTypeText(transaction.getType()),
            getProcessingTimeText(transaction)
        );
        
        builder.setMessage(details);
        builder.setPositiveButton("Share Details", (dialog, which) -> shareTransactionDetails(transaction));
        builder.setNegativeButton("Close", null);
        
        if (transaction.getReceiptId() != null) {
            builder.setNeutralButton("View Receipt", (dialog, which) -> showReceiptDetails(transaction));
        }
        
        builder.show();
    }
    
    private String getProcessingTimeText(Transaction transaction) {
        long currentTime = System.currentTimeMillis();
        long transactionTime = transaction.getTimestamp();
        long timeDiff = currentTime - transactionTime;
        
        long minutes = timeDiff / (1000 * 60);
        long hours = timeDiff / (1000 * 60 * 60);
        long days = timeDiff / (1000 * 60 * 60 * 24);
        
        if (days > 0) {
            return days + " day(s) ago";
        } else if (hours > 0) {
            return hours + " hour(s) ago";
        } else if (minutes > 0) {
            return minutes + " minute(s) ago";
        } else {
            return "Just now";
        }
    }
    
    private void shareTransactionDetails(Transaction transaction) {
        String shareText = String.format(Locale.getDefault(),
            "SugboAid Transaction Report\n" +
            "==========================\n\n" +
            "Transaction ID: %s\n" +
            "Date: %s\n\n" +
            "Donor: %s\n" +
            "Type: %s\n" +
            "Amount: %s\n" +
            "Campaign: %s\n\n" +
            "Status: %s\n" +
            "Receipt: %s\n\n" +
            "Generated by SugboAid Donation Management System\n" +
            "For more information, visit sugboaid.org",
            transaction.getId(),
            transaction.getFormattedDate(),
            transaction.getDonor() != null ? transaction.getDonor() : "Anonymous Donor",
            getTransactionTypeText(transaction.getType()),
            transaction.getAmount(),
            transaction.getCampaign() != null ? transaction.getCampaign() : "General Fund",
            transaction.isVerified() ? "Verified" : "Pending Verification",
            transaction.getReceiptId() != null ? transaction.getReceiptId() : "Not available"
        );
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "SugboAid Transaction Details - " + transaction.getId());
        
        Intent chooser = Intent.createChooser(shareIntent, "Share Transaction Details");
        if (shareIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            showToast("No app available to share transaction details");
        }
    }
    
    private void showReceiptDetails(Transaction transaction) {
        // Create a more detailed receipt dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Receipt Details");
        
        String receiptInfo = String.format(Locale.getDefault(),
            "Receipt ID: %s\n\n" +
            "Transaction Details:\n" +
            "• Donor: %s\n" +
            "• Type: %s\n" +
            "• Amount: %s\n" +
            "• Date: %s\n" +
            "• Campaign: %s\n" +
            "• Status: %s\n\n" +
            "QR Code: Available for scanning\n" +
            "Verification: %s",
            transaction.getReceiptId(),
            transaction.getDonor() != null ? transaction.getDonor() : "Anonymous",
            getTransactionTypeText(transaction.getType()),
            transaction.getAmount(),
            transaction.getFormattedDate(),
            transaction.getCampaign() != null ? transaction.getCampaign() : "General",
            transaction.isVerified() ? "Verified ✓" : "Pending Verification",
            transaction.isVerified() ? "Transaction has been verified and processed" : "Transaction is pending verification"
        );
        
        builder.setMessage(receiptInfo);
        builder.setPositiveButton("Share Receipt", (dialog, which) -> shareReceipt(transaction));
        builder.setNegativeButton("Close", null);
        
        if (transaction.getReceiptId() != null) {
            builder.setNeutralButton("View QR Code", (dialog, which) -> showQRCode(transaction));
        }
        
        builder.show();
    }
    
    private void shareReceipt(Transaction transaction) {
        String receiptText = String.format(Locale.getDefault(),
            "SugboAid Official Receipt\n" +
            "========================\n\n" +
            "Receipt ID: %s\n" +
            "Date: %s\n\n" +
            "Transaction Details:\n" +
            "Donor: %s\n" +
            "Type: %s\n" +
            "Amount: %s\n" +
            "Campaign: %s\n\n" +
            "Status: %s\n\n" +
            "Thank you for your generous donation!\n" +
            "Visit sugboaid.org for more information.",
            transaction.getReceiptId(),
            transaction.getFormattedDate(),
            transaction.getDonor() != null ? transaction.getDonor() : "Anonymous Donor",
            getTransactionTypeText(transaction.getType()),
            transaction.getAmount(),
            transaction.getCampaign() != null ? transaction.getCampaign() : "General Fund",
            transaction.isVerified() ? "Verified" : "Pending Verification"
        );
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, receiptText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "SugboAid Receipt - " + transaction.getReceiptId());
        
        Intent chooser = Intent.createChooser(shareIntent, "Share Receipt");
        if (shareIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            showToast("No app available to share receipt");
        }
    }
    
    private void showQRCode(Transaction transaction) {
        // For now, show a placeholder message about QR code
        // In a full implementation, this would generate and display an actual QR code
        String qrInfo = String.format(Locale.getDefault(),
            "QR Code for Receipt: %s\n\n" +
            "This QR code contains:\n" +
            "• Receipt verification data\n" +
            "• Transaction timestamp\n" +
            "• Digital signature\n\n" +
            "Scan this code to verify the transaction authenticity.",
            transaction.getReceiptId()
        );
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("QR Code Access");
        builder.setMessage(qrInfo);
        builder.setPositiveButton("Generate QR", (dialog, which) -> {
            showToast("QR Code generation feature coming soon!");
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }
    
    private String getTransactionTypeText(com.sugboaid.models.TransactionType type) {
        switch (type) {
            case DONATION:
                return "Donation";
            case DISTRIBUTION:
                return "Distribution";
            case INVENTORY_UPDATE:
                return "Inventory Update";
            case TRANSFER:
                return "Transfer";
            default:
                return "Transaction";
        }
    }
    
    private boolean validateExportConditions() {
        // Check if transactions are available
        List<Transaction> transactions = viewModel.getTransactionsForExport();
        if (transactions == null || transactions.isEmpty()) {
            showExportErrorDialog("No Data Available", 
                "There are no transactions to export. Please add some transactions first.");
            return false;
        }
        
        // Check external storage availability
        if (!ExportUtils.isExternalStorageWritable()) {
            showExportErrorDialog("Storage Not Available", 
                "External storage is not available for writing. Please check your device storage permissions and try again.");
            return false;
        }
        
        return true;
    }
    
    private void showExportErrorDialog(String title, String message) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        
        if (title.contains("Storage")) {
            builder.setNeutralButton("View Export Location", (dialog, which) -> {
                String exportPath = ExportUtils.getExportDirectoryPath(requireContext());
                showToast("Export location: " + exportPath);
            });
        }
        
        builder.show();
    }
    
    private void showExportSuccessDialog(String format, int transactionCount, String filterType) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Export Successful");
        builder.setMessage(String.format(Locale.getDefault(),
            "%s report has been generated successfully!\n\n" +
            "Report Details:\n" +
            "• Format: %s\n" +
            "• Transactions: %d\n" +
            "• Filter: %s\n\n" +
            "The file has been saved and is ready to share.",
            format, format, transactionCount, getFilterDisplayName(filterType)));
        builder.setPositiveButton("OK", null);
        builder.setNeutralButton("View Location", (dialog, which) -> {
            String exportPath = ExportUtils.getExportDirectoryPath(requireContext());
            showToast("Files saved to: " + exportPath);
        });
        builder.show();
    }
    
    private String getFilterDisplayName(String filterType) {
        switch (filterType.toLowerCase()) {
            case "all":
                return "All Transactions";
            case "cash":
                return "Cash Donations";
            case "goods":
                return "Goods Donations";
            case "distribution":
                return "Distributions";
            default:
                return "Filtered Transactions";
        }
    }
    
    protected void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        if (viewModel != null) {
            viewModel.refreshData();
        }
    }
}