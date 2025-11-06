package com.sugboaid.donation.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sugboaid.donation.R;
import com.sugboaid.models.Transaction;
import com.sugboaid.models.TransactionType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying transaction items in the reports list
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    
    private Context context;
    private List<Transaction> transactions;
    private List<Transaction> filteredTransactions;
    private OnTransactionClickListener clickListener;
    private SimpleDateFormat dateFormat;
    
    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
        void onReceiptClick(Transaction transaction);
    }
    
    public TransactionAdapter(Context context) {
        this.context = context;
        this.transactions = new ArrayList<>();
        this.filteredTransactions = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
    }
    
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions != null ? new ArrayList<>(transactions) : new ArrayList<>();
        this.filteredTransactions = new ArrayList<>(this.transactions);
        notifyDataSetChanged();
    }
    
    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.clickListener = listener;
    }
    
    public void filterByType(TransactionType type) {
        filteredTransactions.clear();
        
        if (type == null) {
            // Show all transactions
            filteredTransactions.addAll(transactions);
        } else {
            // Filter by specific type
            for (Transaction transaction : transactions) {
                if (transaction.getType() == type) {
                    filteredTransactions.add(transaction);
                }
            }
        }
        
        notifyDataSetChanged();
    }
    
    public void filterByCashDonations() {
        filteredTransactions.clear();
        
        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.DONATION && 
                transaction.getAmount().contains("₱")) {
                filteredTransactions.add(transaction);
            }
        }
        
        notifyDataSetChanged();
    }
    
    public void filterByGoodsDonations() {
        filteredTransactions.clear();
        
        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.DONATION && 
                !transaction.getAmount().contains("₱")) {
                filteredTransactions.add(transaction);
            }
        }
        
        notifyDataSetChanged();
    }
    
    public void clearFilter() {
        filteredTransactions.clear();
        filteredTransactions.addAll(transactions);
        notifyDataSetChanged();
    }
    
    public List<Transaction> getFilteredTransactions() {
        return new ArrayList<>(filteredTransactions);
    }
    
    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = filteredTransactions.get(position);
        holder.bind(transaction);
    }
    
    @Override
    public int getItemCount() {
        return filteredTransactions.size();
    }
    
    public class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTransactionIcon;
        private TextView tvDonorName;
        private TextView tvTransactionType;
        private TextView tvAmount;
        private TextView tvDateTime;
        private TextView tvReceiptId;
        private ImageView ivVerifiedBadge;
        private TextView tvVerifiedLabel;
        private TextView tvCampaignBadge;
        
        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvTransactionIcon = itemView.findViewById(R.id.tv_transaction_icon);
            tvDonorName = itemView.findViewById(R.id.tv_donor_name);
            tvTransactionType = itemView.findViewById(R.id.tv_transaction_type);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvReceiptId = itemView.findViewById(R.id.tv_receipt_id);
            ivVerifiedBadge = itemView.findViewById(R.id.iv_verified_badge);
            tvVerifiedLabel = itemView.findViewById(R.id.tv_verified_label);
            tvCampaignBadge = itemView.findViewById(R.id.tv_campaign_badge);
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (clickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    clickListener.onTransactionClick(filteredTransactions.get(getAdapterPosition()));
                }
            });
            
            tvReceiptId.setOnClickListener(v -> {
                if (clickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Transaction transaction = filteredTransactions.get(getAdapterPosition());
                    
                    // Add visual feedback for click
                    v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();
                        })
                        .start();
                    
                    clickListener.onReceiptClick(transaction);
                }
            });
            
            // Add long click listener for additional transaction actions
            itemView.setOnLongClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Transaction transaction = filteredTransactions.get(getAdapterPosition());
                    showTransactionContextMenu(transaction);
                    return true;
                }
                return false;
            });
        }
        
        public void bind(Transaction transaction) {
            // Set transaction icon with enhanced styling
            tvTransactionIcon.setText(transaction.getTypeIcon());
            
            // Apply background color with proper error handling
            try {
                String colorHex = transaction.getTypeColor();
                if (colorHex != null && colorHex.startsWith("#")) {
                    int backgroundColor = Color.parseColor(colorHex + "33"); // 20% alpha for background
                    tvTransactionIcon.setBackgroundColor(backgroundColor);
                } else {
                    tvTransactionIcon.setBackgroundColor(context.getResources().getColor(R.color.primary_blue_20));
                }
            } catch (IllegalArgumentException e) {
                // Fallback to default color
                tvTransactionIcon.setBackgroundColor(context.getResources().getColor(R.color.primary_blue_20));
            }
            
            // Set donor name with enhanced formatting
            String donorName = transaction.getDonor();
            if (donorName == null || donorName.trim().isEmpty()) {
                donorName = "Anonymous Donor";
                tvDonorName.setTextColor(context.getResources().getColor(R.color.text_secondary));
            } else {
                tvDonorName.setTextColor(context.getResources().getColor(R.color.text_primary));
            }
            tvDonorName.setText(donorName);
            
            // Set transaction type with description fallback
            String typeText = getTransactionTypeText(transaction.getType());
            if (transaction.getDescription() != null && !transaction.getDescription().trim().isEmpty()) {
                typeText = transaction.getDescription();
            }
            tvTransactionType.setText(typeText);
            
            // Set amount with enhanced color coding and formatting
            String amount = transaction.getAmount();
            if (amount == null || amount.trim().isEmpty()) {
                amount = "No amount specified";
                tvAmount.setTextColor(context.getResources().getColor(R.color.text_tertiary));
            } else {
                tvAmount.setText(amount);
                
                // Apply color based on transaction type
                switch (transaction.getType()) {
                    case DONATION:
                        tvAmount.setTextColor(context.getResources().getColor(R.color.success_green));
                        break;
                    case DISTRIBUTION:
                        tvAmount.setTextColor(context.getResources().getColor(R.color.primary_blue));
                        break;
                    case INVENTORY_UPDATE:
                        tvAmount.setTextColor(context.getResources().getColor(R.color.warning_orange));
                        break;
                    case TRANSFER:
                        tvAmount.setTextColor(context.getResources().getColor(R.color.accent_yellow));
                        break;
                    default:
                        tvAmount.setTextColor(context.getResources().getColor(R.color.text_primary));
                        break;
                }
            }
            
            // Set date and time with enhanced formatting
            if (transaction.getDate() != null) {
                String formattedDate = dateFormat.format(transaction.getDate());
                tvDateTime.setText(formattedDate);
                tvDateTime.setTextColor(context.getResources().getColor(R.color.text_tertiary));
            } else {
                tvDateTime.setText("Date not available");
                tvDateTime.setTextColor(context.getResources().getColor(R.color.error_red));
            }
            
            // Set receipt ID with enhanced styling and QR code access indication
            String receiptId = transaction.getReceiptId();
            if (receiptId != null && !receiptId.trim().isEmpty()) {
                tvReceiptId.setText("Receipt: " + receiptId);
                tvReceiptId.setVisibility(View.VISIBLE);
                tvReceiptId.setTextColor(context.getResources().getColor(R.color.primary_blue));
                
                // Add visual indication that receipt is clickable
                tvReceiptId.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_qr_code, 0);
                tvReceiptId.setCompoundDrawablePadding(8);
            } else {
                tvReceiptId.setVisibility(View.GONE);
            }
            
            // Set verification status with enhanced visual feedback
            if (transaction.isVerified()) {
                ivVerifiedBadge.setVisibility(View.VISIBLE);
                tvVerifiedLabel.setVisibility(View.VISIBLE);
                tvVerifiedLabel.setText("Verified");
                tvVerifiedLabel.setTextColor(context.getResources().getColor(R.color.success_green));
                
                // Add subtle background to verified badge
                ivVerifiedBadge.setBackgroundResource(R.drawable.circle_background);
                ivVerifiedBadge.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                        context.getResources().getColor(R.color.success_green)
                    )
                );
            } else {
                ivVerifiedBadge.setVisibility(View.GONE);
                tvVerifiedLabel.setVisibility(View.GONE);
            }
            
            // Set campaign badge with enhanced styling
            String campaign = transaction.getCampaign();
            if (campaign != null && !campaign.trim().isEmpty()) {
                tvCampaignBadge.setText(campaign);
                tvCampaignBadge.setVisibility(View.VISIBLE);
                
                // Apply different colors based on campaign type
                if (campaign.toLowerCase().contains("relief")) {
                    tvCampaignBadge.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                            context.getResources().getColor(R.color.error_red)
                        )
                    );
                } else if (campaign.toLowerCase().contains("education")) {
                    tvCampaignBadge.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                            context.getResources().getColor(R.color.primary_blue)
                        )
                    );
                } else {
                    tvCampaignBadge.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                            context.getResources().getColor(R.color.primary_green)
                        )
                    );
                }
            } else {
                tvCampaignBadge.setVisibility(View.GONE);
            }
            
            // Apply visual emphasis for important transactions
            applyTransactionEmphasis(transaction);
            
            // Add accessibility content descriptions
            itemView.setContentDescription(
                String.format("Transaction by %s, %s, amount %s, on %s%s",
                    donorName,
                    typeText,
                    amount,
                    transaction.getFormattedDate(),
                    transaction.isVerified() ? ", verified" : ", pending verification"
                )
            );
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
        
        /**
         * Formats the transaction amount for display with proper currency formatting
         */
        private String formatAmount(String amount) {
            if (amount == null || amount.trim().isEmpty()) {
                return "No amount specified";
            }
            
            // If it's already formatted with currency symbol, return as is
            if (amount.contains("₱") || amount.contains("$")) {
                return amount;
            }
            
            // Try to parse as number and format with currency
            try {
                double value = Double.parseDouble(amount.replace(",", ""));
                return String.format(Locale.getDefault(), "₱%.2f", value);
            } catch (NumberFormatException e) {
                // Return original if parsing fails
                return amount;
            }
        }
        
        /**
         * Gets the appropriate status text based on verification and other factors
         */
        private String getStatusText(Transaction transaction) {
            if (transaction.isVerified()) {
                return "Verified";
            } else {
                // Check if transaction is recent (within 24 hours)
                long currentTime = System.currentTimeMillis();
                long transactionTime = transaction.getTimestamp();
                long timeDiff = currentTime - transactionTime;
                long hoursAgo = timeDiff / (1000 * 60 * 60);
                
                if (hoursAgo < 24) {
                    return "Pending";
                } else {
                    return "Under Review";
                }
            }
        }
        
        /**
         * Applies visual emphasis to important transactions
         */
        private void applyTransactionEmphasis(Transaction transaction) {
            // Reset background first
            itemView.setBackground(null);
            
            // Highlight high-value donations
            if (transaction.getType() == TransactionType.DONATION) {
                String amount = transaction.getAmount();
                if (amount != null && amount.contains("₱")) {
                    try {
                        String numericAmount = amount.replace("₱", "").replace(",", "").trim();
                        double value = Double.parseDouble(numericAmount);
                        
                        if (value >= 10000) { // High-value donation
                            itemView.setBackgroundResource(R.drawable.gradient_success);
                            itemView.getBackground().setAlpha(30); // 12% opacity
                        } else if (value >= 5000) { // Medium-value donation
                            itemView.setBackgroundResource(R.drawable.gradient_primary);
                            itemView.getBackground().setAlpha(20); // 8% opacity
                        }
                    } catch (NumberFormatException e) {
                        // No special emphasis for non-numeric amounts
                    }
                }
            }
        }
        
        /**
         * Shows context menu for additional transaction actions
         */
        private void showTransactionContextMenu(Transaction transaction) {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(context, itemView);
            popup.getMenuInflater().inflate(R.menu.transaction_context_menu, popup.getMenu());
            
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_view_details) {
                    if (clickListener != null) {
                        clickListener.onTransactionClick(transaction);
                    }
                    return true;
                } else if (itemId == R.id.action_view_receipt) {
                    if (clickListener != null && transaction.getReceiptId() != null) {
                        clickListener.onReceiptClick(transaction);
                    }
                    return true;
                } else if (itemId == R.id.action_share) {
                    shareTransaction(transaction);
                    return true;
                }
                return false;
            });
            
            popup.show();
        }
        
        /**
         * Shares transaction details via Android sharing intent
         */
        private void shareTransaction(Transaction transaction) {
            String shareText = String.format(
                "SugboAid Transaction\n\n" +
                "Donor: %s\n" +
                "Type: %s\n" +
                "Amount: %s\n" +
                "Date: %s\n" +
                "Receipt ID: %s\n" +
                "Status: %s",
                transaction.getDonor() != null ? transaction.getDonor() : "Anonymous",
                getTransactionTypeText(transaction.getType()),
                transaction.getAmount(),
                transaction.getFormattedDate(),
                transaction.getReceiptId() != null ? transaction.getReceiptId() : "N/A",
                transaction.isVerified() ? "Verified" : "Pending"
            );
            
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "SugboAid Transaction Details");
            
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Transaction"));
        }
    }
    
    // Utility methods for statistics
    public int getTotalTransactionCount() {
        return filteredTransactions.size();
    }
    
    public double getTotalValue() {
        double total = 0.0;
        
        for (Transaction transaction : filteredTransactions) {
            String amount = transaction.getAmount();
            if (amount != null && amount.contains("₱")) {
                try {
                    // Extract numeric value from amount string like "₱1,500.00"
                    String numericAmount = amount.replace("₱", "").replace(",", "").trim();
                    total += Double.parseDouble(numericAmount);
                } catch (NumberFormatException e) {
                    // Skip invalid amounts
                }
            }
        }
        
        return total;
    }
    
    public int getCashTransactionCount() {
        int count = 0;
        
        for (Transaction transaction : filteredTransactions) {
            if (transaction.getType() == TransactionType.DONATION && 
                transaction.getAmount().contains("₱")) {
                count++;
            }
        }
        
        return count;
    }
    
    public int getGoodsTransactionCount() {
        int count = 0;
        
        for (Transaction transaction : filteredTransactions) {
            if (transaction.getType() == TransactionType.DONATION && 
                !transaction.getAmount().contains("₱")) {
                count++;
            }
        }
        
        return count;
    }
    
    public int getDistributionCount() {
        int count = 0;
        
        for (Transaction transaction : filteredTransactions) {
            if (transaction.getType() == TransactionType.DISTRIBUTION) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Gets the count of verified transactions
     */
    public int getVerifiedTransactionCount() {
        int count = 0;
        
        for (Transaction transaction : filteredTransactions) {
            if (transaction.isVerified()) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Gets the count of pending transactions
     */
    public int getPendingTransactionCount() {
        return filteredTransactions.size() - getVerifiedTransactionCount();
    }
    
    /**
     * Gets transactions by date range
     */
    public void filterByDateRange(long startDate, long endDate) {
        filteredTransactions.clear();
        
        for (Transaction transaction : transactions) {
            long transactionTime = transaction.getTimestamp();
            if (transactionTime >= startDate && transactionTime <= endDate) {
                filteredTransactions.add(transaction);
            }
        }
        
        notifyDataSetChanged();
    }
    
    /**
     * Gets the most recent transaction
     */
    public Transaction getMostRecentTransaction() {
        if (filteredTransactions.isEmpty()) {
            return null;
        }
        
        Transaction mostRecent = filteredTransactions.get(0);
        for (Transaction transaction : filteredTransactions) {
            if (transaction.getTimestamp() > mostRecent.getTimestamp()) {
                mostRecent = transaction;
            }
        }
        
        return mostRecent;
    }
    
    /**
     * Gets transactions by verification status
     */
    public void filterByVerificationStatus(boolean verified) {
        filteredTransactions.clear();
        
        for (Transaction transaction : transactions) {
            if (transaction.isVerified() == verified) {
                filteredTransactions.add(transaction);
            }
        }
        
        notifyDataSetChanged();
    }
    
    /**
     * Searches transactions by donor name or receipt ID
     */
    public void searchTransactions(String query) {
        filteredTransactions.clear();
        
        if (query == null || query.trim().isEmpty()) {
            filteredTransactions.addAll(transactions);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            
            for (Transaction transaction : transactions) {
                boolean matches = false;
                
                // Search in donor name
                if (transaction.getDonor() != null && 
                    transaction.getDonor().toLowerCase().contains(lowerQuery)) {
                    matches = true;
                }
                
                // Search in receipt ID
                if (transaction.getReceiptId() != null && 
                    transaction.getReceiptId().toLowerCase().contains(lowerQuery)) {
                    matches = true;
                }
                
                // Search in description
                if (transaction.getDescription() != null && 
                    transaction.getDescription().toLowerCase().contains(lowerQuery)) {
                    matches = true;
                }
                
                // Search in campaign
                if (transaction.getCampaign() != null && 
                    transaction.getCampaign().toLowerCase().contains(lowerQuery)) {
                    matches = true;
                }
                
                if (matches) {
                    filteredTransactions.add(transaction);
                }
            }
        }
        
        notifyDataSetChanged();
    }
}