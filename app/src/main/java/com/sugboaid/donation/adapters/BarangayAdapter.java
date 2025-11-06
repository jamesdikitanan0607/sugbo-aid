package com.sugboaid.donation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sugboaid.donation.R;
import com.sugboaid.donation.models.BarangayLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying barangay locations with status indicators
 */
public class BarangayAdapter extends RecyclerView.Adapter<BarangayAdapter.BarangayViewHolder> {

    private List<BarangayLocation> barangays = new ArrayList<>();
    private OnBarangayClickListener onBarangayClickListener;

    public interface OnBarangayClickListener {
        void onBarangayClick(BarangayLocation barangay);
    }

    public void setOnBarangayClickListener(OnBarangayClickListener listener) {
        this.onBarangayClickListener = listener;
    }

    public void updateBarangays(List<BarangayLocation> newBarangays) {
        this.barangays.clear();
        if (newBarangays != null) {
            this.barangays.addAll(newBarangays);
        }
        notifyDataSetChanged();
    }

    public int getBarangayPosition(BarangayLocation barangay) {
        return barangays.indexOf(barangay);
    }

    @NonNull
    @Override
    public BarangayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_barangay, parent, false);
        return new BarangayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BarangayViewHolder holder, int position) {
        BarangayLocation barangay = barangays.get(position);
        holder.bind(barangay);
    }

    @Override
    public int getItemCount() {
        return barangays.size();
    }

    class BarangayViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView familiesText;
        private final TextView donationsText;
        private final TextView statusText;
        private final View statusIndicator;

        public BarangayViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.barangayNameText);
            familiesText = itemView.findViewById(R.id.familiesHelpedText);
            donationsText = itemView.findViewById(R.id.donationAmountText);
            statusText = itemView.findViewById(R.id.statusText);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onBarangayClickListener != null) {
                    onBarangayClickListener.onBarangayClick(barangays.get(position));
                }
            });
        }

        public void bind(BarangayLocation barangay) {
            nameText.setText(barangay.getName());
            // Match layout labels: "Families: X" and "Donations: ₱XXXX.XX"
            familiesText.setText(String.format("Families: %d", barangay.getFamiliesHelped()));
            donationsText.setText(String.format("Donations: %s", barangay.getFormattedDonations()));
            statusText.setText(barangay.getStatus().toUpperCase());

            // Determine chip color based on status (critical/moderate/stable(active)/low -> moderate)
            String status = barangay.getStatus() != null ? barangay.getStatus().toLowerCase() : "";
            int chipColorRes;
            switch (status) {
                case "critical":
                    chipColorRes = R.color.status_chip_critical;
                    break;
                case "moderate":
                case "low":
                    chipColorRes = R.color.status_chip_moderate;
                    break;
                case "active":
                default:
                    chipColorRes = R.color.status_chip_stable;
                    break;
            }
            int chipColor = ContextCompat.getColor(itemView.getContext(), chipColorRes);

            // Status chip: set rounded background fill color and white text for contrast
            if (statusText.getBackground() != null) {
                android.graphics.drawable.Drawable bg = statusText.getBackground().mutate();
                if (bg instanceof android.graphics.drawable.GradientDrawable) {
                    ((android.graphics.drawable.GradientDrawable) bg).setColor(chipColor);
                } else {
                    // Fallback to tint if not a GradientDrawable
                    DrawableCompat.setTint(bg, chipColor);
                }
                statusText.setBackground(bg);
            }
            statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));

            // Circular status indicator: tint the oval shape instead of replacing background
            if (statusIndicator.getBackground() != null && statusIndicator.getBackground() instanceof android.graphics.drawable.GradientDrawable) {
                ((android.graphics.drawable.GradientDrawable) statusIndicator.getBackground().mutate()).setColor(chipColor);
            } else {
                statusIndicator.setBackgroundColor(chipColor);
            }
        }
    }
}