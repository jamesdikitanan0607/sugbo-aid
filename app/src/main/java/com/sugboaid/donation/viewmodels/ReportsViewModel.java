package com.sugboaid.donation.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.sugboaid.models.Transaction;
import com.sugboaid.models.TransactionType;
import com.sugboaid.repositories.DonationRepository;
import com.sugboaid.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ViewModel for managing reports and transaction history data
 */
public class ReportsViewModel extends AndroidViewModel {
    
    private SharedPreferencesHelper prefsHelper;
    private DonationRepository donationRepository;
    
    private MutableLiveData<List<Transaction>> transactionsLiveData;
    private MutableLiveData<List<Transaction>> filteredTransactionsLiveData;
    private MutableLiveData<Integer> totalTransactionsLiveData;
    private MutableLiveData<Double> totalValueLiveData;
    private MutableLiveData<Boolean> isLoadingLiveData;
    private MutableLiveData<String> errorMessageLiveData;
    
    private TransactionType currentFilter;
    private String currentFilterType; // "all", "cash", "goods", "distribution"
    
    public ReportsViewModel(@NonNull Application application) {
        super(application);
        
        prefsHelper = SharedPreferencesHelper.getInstance(application);
        donationRepository = DonationRepository.getInstance(application);
        
        transactionsLiveData = new MutableLiveData<>();
        filteredTransactionsLiveData = new MutableLiveData<>();
        totalTransactionsLiveData = new MutableLiveData<>();
        totalValueLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>();
        errorMessageLiveData = new MutableLiveData<>();
        
        currentFilter = null;
        currentFilterType = "all";
        
        // Initialize with empty data
        transactionsLiveData.setValue(new ArrayList<>());
        filteredTransactionsLiveData.setValue(new ArrayList<>());
        totalTransactionsLiveData.setValue(0);
        totalValueLiveData.setValue(0.0);
        isLoadingLiveData.setValue(false);
        
        // Load initial data
        loadTransactions();
    }
    
    // LiveData getters
    public LiveData<List<Transaction>> getTransactions() {
        return transactionsLiveData;
    }
    
    public LiveData<List<Transaction>> getFilteredTransactions() {
        return filteredTransactionsLiveData;
    }
    
    public LiveData<Integer> getTotalTransactions() {
        return totalTransactionsLiveData;
    }
    
    public LiveData<Double> getTotalValue() {
        return totalValueLiveData;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }
    
    // Data loading methods
    public void loadTransactions() {
        isLoadingLiveData.setValue(true);
        errorMessageLiveData.setValue(null);
        
        try {
            List<Transaction> transactions = prefsHelper.getTransactions();
            
            // Sort transactions by date (newest first)
            Collections.sort(transactions, new Comparator<Transaction>() {
                @Override
                public int compare(Transaction t1, Transaction t2) {
                    if (t1.getDate() == null && t2.getDate() == null) return 0;
                    if (t1.getDate() == null) return 1;
                    if (t2.getDate() == null) return -1;
                    return t2.getDate().compareTo(t1.getDate());
                }
            });
            
            transactionsLiveData.setValue(transactions);
            
            // Apply current filter
            applyCurrentFilter();
            
            isLoadingLiveData.setValue(false);
            
        } catch (Exception e) {
            errorMessageLiveData.setValue("Failed to load transactions: " + e.getMessage());
            isLoadingLiveData.setValue(false);
        }
    }
    
    public void refreshData() {
        loadTransactions();
    }
    
    // Filtering methods
    public void filterAllTransactions() {
        currentFilterType = "all";
        currentFilter = null;
        applyCurrentFilter();
    }
    
    public void filterCashDonations() {
        currentFilterType = "cash";
        currentFilter = null;
        applyCurrentFilter();
    }
    
    public void filterGoodsDonations() {
        currentFilterType = "goods";
        currentFilter = null;
        applyCurrentFilter();
    }
    
    public void filterDistributions() {
        currentFilterType = "distribution";
        currentFilter = TransactionType.DISTRIBUTION;
        applyCurrentFilter();
    }
    
    public void filterByType(TransactionType type) {
        currentFilter = type;
        currentFilterType = type != null ? type.getValue() : "all";
        applyCurrentFilter();
    }
    
    private void applyCurrentFilter() {
        List<Transaction> allTransactions = transactionsLiveData.getValue();
        if (allTransactions == null) {
            allTransactions = new ArrayList<>();
        }
        
        List<Transaction> filtered = new ArrayList<>();
        
        switch (currentFilterType) {
            case "all":
                filtered.addAll(allTransactions);
                break;
                
            case "cash":
                for (Transaction transaction : allTransactions) {
                    if (transaction.getType() == TransactionType.DONATION && 
                        transaction.getAmount() != null && 
                        transaction.getAmount().contains("₱")) {
                        filtered.add(transaction);
                    }
                }
                break;
                
            case "goods":
                for (Transaction transaction : allTransactions) {
                    if (transaction.getType() == TransactionType.DONATION && 
                        transaction.getAmount() != null && 
                        !transaction.getAmount().contains("₱")) {
                        filtered.add(transaction);
                    }
                }
                break;
                
            case "distribution":
                for (Transaction transaction : allTransactions) {
                    if (transaction.getType() == TransactionType.DISTRIBUTION) {
                        filtered.add(transaction);
                    }
                }
                break;
                
            default:
                if (currentFilter != null) {
                    for (Transaction transaction : allTransactions) {
                        if (transaction.getType() == currentFilter) {
                            filtered.add(transaction);
                        }
                    }
                } else {
                    filtered.addAll(allTransactions);
                }
                break;
        }
        
        filteredTransactionsLiveData.setValue(filtered);
        updateStatistics(filtered);
    }
    
    private void updateStatistics(List<Transaction> transactions) {
        totalTransactionsLiveData.setValue(transactions.size());
        
        double totalValue = 0.0;
        for (Transaction transaction : transactions) {
            String amount = transaction.getAmount();
            if (amount != null && amount.contains("₱")) {
                try {
                    // Extract numeric value from amount string like "₱1,500.00"
                    String numericAmount = amount.replace("₱", "").replace(",", "").trim();
                    totalValue += Double.parseDouble(numericAmount);
                } catch (NumberFormatException e) {
                    // Skip invalid amounts
                }
            }
        }
        
        totalValueLiveData.setValue(totalValue);
    }
    
    // Export methods
    public List<Transaction> getTransactionsForExport() {
        List<Transaction> filtered = filteredTransactionsLiveData.getValue();
        return filtered != null ? new ArrayList<>(filtered) : new ArrayList<>();
    }
    
    public String generateCSVData() {
        List<Transaction> transactions = getTransactionsForExport();
        StringBuilder csv = new StringBuilder();
        
        // CSV Header
        csv.append("ID,Donor,Type,Amount,Date,Campaign,Verified,Description,Receipt ID\n");
        
        // CSV Data
        for (Transaction transaction : transactions) {
            csv.append(escapeCSV(transaction.getId())).append(",");
            csv.append(escapeCSV(transaction.getDonor())).append(",");
            csv.append(escapeCSV(getTransactionTypeText(transaction.getType()))).append(",");
            csv.append(escapeCSV(transaction.getAmount())).append(",");
            csv.append(escapeCSV(transaction.getFormattedDate())).append(",");
            csv.append(escapeCSV(transaction.getCampaign())).append(",");
            csv.append(transaction.isVerified() ? "Yes" : "No").append(",");
            csv.append(escapeCSV(transaction.getDescription())).append(",");
            csv.append(escapeCSV(transaction.getReceiptId())).append("\n");
        }
        
        return csv.toString();
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    private String getTransactionTypeText(TransactionType type) {
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
    
    // Statistics methods
    public int getCashTransactionCount() {
        List<Transaction> filtered = filteredTransactionsLiveData.getValue();
        if (filtered == null) return 0;
        
        int count = 0;
        for (Transaction transaction : filtered) {
            if (transaction.getType() == TransactionType.DONATION && 
                transaction.getAmount() != null && 
                transaction.getAmount().contains("₱")) {
                count++;
            }
        }
        return count;
    }
    
    public int getGoodsTransactionCount() {
        List<Transaction> filtered = filteredTransactionsLiveData.getValue();
        if (filtered == null) return 0;
        
        int count = 0;
        for (Transaction transaction : filtered) {
            if (transaction.getType() == TransactionType.DONATION && 
                transaction.getAmount() != null && 
                !transaction.getAmount().contains("₱")) {
                count++;
            }
        }
        return count;
    }
    
    public int getDistributionCount() {
        List<Transaction> filtered = filteredTransactionsLiveData.getValue();
        if (filtered == null) return 0;
        
        int count = 0;
        for (Transaction transaction : filtered) {
            if (transaction.getType() == TransactionType.DISTRIBUTION) {
                count++;
            }
        }
        return count;
    }
    
    public String getCurrentFilterType() {
        return currentFilterType;
    }
    
    // Error handling
    public void clearError() {
        errorMessageLiveData.setValue(null);
    }
}