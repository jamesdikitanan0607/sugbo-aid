package com.sugboaid.donation.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.sugboaid.donation.R;
import com.sugboaid.models.Donation;
import com.sugboaid.models.DonationType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying recent donation activities
 */
public class RecentActivitiesAdapter extends ListAdapter<Donation, RecentActivitiesAdapter.RecentActivityViewHolder> {

    private Context context;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Donation donation);
        void onItemLongClick(Donation donation);
    }

    public RecentActivitiesAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public RecentActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_activity, parent, false);
        return new RecentActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentActivityViewHolder holder, int position) {
        Donation donation = getItem(position);
        holder.bind(donation);
    }

    class RecentActivityViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivDonationIcon;
        private TextView tvDonorName;
        private TextView tvDonationDescription;
        private TextView tvAmount;
        private TextView tvTimestamp;
        private ImageView ivVerifiedBadge;
        private TextView tvVerifiedLabel;

        public RecentActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivDonationIcon = itemView.findViewById(R.id.iv_donation_icon);
            tvDonorName = itemView.findViewById(R.id.tv_donor_name);
            tvDonationDescription = itemView.findViewById(R.id.tv_donation_description);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            ivVerifiedBadge = itemView.findViewById(R.id.iv_verified_badge);
            tvVerifiedLabel = itemView.findViewById(R.id.tv_verified_label);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(getItem(getAdapterPosition()));
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (onItemClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemLongClick(getItem(getAdapterPosition()));
                    return true;
                }
                return false;
            });
        }

        public void bind(Donation donation) {
            // Set donor name
            String donorName = donation.getDonorName();
            if (donorName == null || donorName.trim().isEmpty() || "Anonymous".equals(donorName)) {
                tvDonorName.setText(context.getString(R.string.anonymous_donor));
            } else {
                tvDonorName.setText(donorName);
            }

            // Set donation type icon and description
            if (donation.getType() == DonationType.CASH) {
                ivDonationIcon.setImageResource(R.drawable.ic_money);
                ivDonationIcon.setBackgroundTintList(
                    ContextCompat.getColorStateList(context, R.color.primary_blue_20));
                ivDonationIcon.setImageTintList(
                    ContextCompat.getColorStateList(context, R.color.primary_blue));
                
                tvDonationDescription.setText(context.getString(R.string.cash_donation_description));
            } else {
                ivDonationIcon.setImageResource(R.drawable.ic_inventory);
                ivDonationIcon.setBackgroundTintList(
                    ContextCompat.getColorStateList(context, R.color.primary_green_20));
                ivDonationIcon.setImageTintList(
                    ContextCompat.getColorStateList(context, R.color.primary_green));
                
                String description = donation.getDescription();
                if (description != null && !description.trim().isEmpty()) {
                    tvDonationDescription.setText(description);
                } else {
                    tvDonationDescription.setText(context.getString(R.string.goods_donation_description));
                }
            }

            // Set amount with proper formatting
            tvAmount.setText(donation.getFormattedAmount());

            // Set timestamp
            tvTimestamp.setText(getRelativeTimeString(donation.getTimestamp()));

            // Set verification badge
            if (donation.isVerified()) {
                ivVerifiedBadge.setVisibility(View.VISIBLE);
                tvVerifiedLabel.setVisibility(View.VISIBLE);
            } else {
                ivVerifiedBadge.setVisibility(View.GONE);
                tvVerifiedLabel.setVisibility(View.GONE);
            }
        }

        private String getRelativeTimeString(long timestamp) {
            try {
                long now = System.currentTimeMillis();
                long diff = now - timestamp;

                // If less than 1 minute
                if (diff < DateUtils.MINUTE_IN_MILLIS) {
                    return context.getString(R.string.just_now);
                }
                
                // If less than 1 hour
                if (diff < DateUtils.HOUR_IN_MILLIS) {
                    long minutes = diff / DateUtils.MINUTE_IN_MILLIS;
                    return context.getResources().getQuantityString(
                        R.plurals.minutes_ago, (int) minutes, minutes);
                }
                
                // If less than 1 day
                if (diff < DateUtils.DAY_IN_MILLIS) {
                    long hours = diff / DateUtils.HOUR_IN_MILLIS;
                    return context.getResources().getQuantityString(
                        R.plurals.hours_ago, (int) hours, hours);
                }
                
                // If less than 1 week
                if (diff < DateUtils.WEEK_IN_MILLIS) {
                    long days = diff / DateUtils.DAY_IN_MILLIS;
                    return context.getResources().getQuantityString(
                        R.plurals.days_ago, (int) days, days);
                }
                
                // For older dates, show formatted date
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return dateFormat.format(new Date(timestamp));
                
            } catch (Exception e) {
                // Fallback to simple date format
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return dateFormat.format(new Date(timestamp));
            }
        }
    }

    // DiffUtil callback for efficient list updates
    private static final DiffUtil.ItemCallback<Donation> DIFF_CALLBACK = new DiffUtil.ItemCallback<Donation>() {
        @Override
        public boolean areItemsTheSame(@NonNull Donation oldItem, @NonNull Donation newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Donation oldItem, @NonNull Donation newItem) {
            return oldItem.equals(newItem) &&
                   oldItem.getDonorName().equals(newItem.getDonorName()) &&
                   oldItem.getAmount() == newItem.getAmount() &&
                   oldItem.getType() == newItem.getType() &&
                   oldItem.getTimestamp() == newItem.getTimestamp() &&
                   oldItem.isVerified() == newItem.isVerified();
        }
    };
}