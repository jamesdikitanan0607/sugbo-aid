package com.sugboaid.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.sugboaid.app.R;
import com.sugboaid.app.data.model.InventoryItem;
import com.sugboaid.app.util.Constants;
import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<InventoryItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    public InventoryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<InventoryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_card, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class InventoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView unitText;
        private final TextView stockText;
        private final TextView statusChip;
        private final TextView updatedText;
        private final ProgressBar stockProgress;
        private final ImageView iconImage;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            unitText = itemView.findViewById(R.id.unitText);
            stockText = itemView.findViewById(R.id.stockText);
            statusChip = itemView.findViewById(R.id.statusChip);
            updatedText = itemView.findViewById(R.id.updatedText);
            stockProgress = itemView.findViewById(R.id.stockProgress);
            iconImage = itemView.findViewById(R.id.iconImage);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(items.get(position));
                }
            });
        }

        public void bind(InventoryItem item) {
            nameText.setText(item.getName());
            unitText.setText(item.getUnit());
            stockText
                    .setText(String.format("%d/%d %s", item.getCurrentStock(), item.getMaximumStock(), item.getUnit()));

            // Calculate progress
            int progress = 0;
            if (item.getMaximumStock() > 0) {
                progress = (int) ((float) item.getCurrentStock() / item.getMaximumStock() * 100);
            }
            stockProgress.setProgress(progress);

            // Set status
            if (item.isLowStock()) {
                statusChip.setText("LOW STOCK");
                statusChip
                        .setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.warning));
                stockProgress
                        .setProgressTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.warning));
            } else {
                statusChip.setText("MODERATE"); // Simplified logic
                statusChip.setBackgroundTintList(
                        ContextCompat.getColorStateList(itemView.getContext(), R.color.emerald_green));
                stockProgress.setProgressTintList(
                        ContextCompat.getColorStateList(itemView.getContext(), R.color.emerald_green));
            }

            // Set icon based on category (simplified)
            if (Constants.CATEGORY_FOOD.equals(item.getCategory())) {
                iconImage.setImageResource(R.drawable.ic_inventory); // Placeholder
            } else if (Constants.CATEGORY_MEDICAL.equals(item.getCategory())) {
                iconImage.setImageResource(R.drawable.ic_inventory); // Placeholder
            } else {
                iconImage.setImageResource(R.drawable.ic_inventory);
            }

            updatedText.setText("Updated recently"); // Placeholder for actual date logic
        }
    }
}
