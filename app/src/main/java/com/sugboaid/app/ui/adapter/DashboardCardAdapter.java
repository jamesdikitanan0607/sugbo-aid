package com.sugboaid.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.sugboaid.app.R;
import com.sugboaid.app.data.model.DashboardCard;
import java.util.List;

public class DashboardCardAdapter extends RecyclerView.Adapter<DashboardCardAdapter.CardViewHolder> {
    
    private List<DashboardCard> cards;
    private OnCardClickListener clickListener;

    public interface OnCardClickListener {
        void onCardClick(DashboardCard card);
    }

    public DashboardCardAdapter(List<DashboardCard> cards, OnCardClickListener clickListener) {
        this.cards = cards;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        DashboardCard card = cards.get(position);
        holder.bind(card);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void updateCards(List<DashboardCard> newCards) {
        this.cards = newCards;
        notifyDataSetChanged();
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView iconView;
        private TextView titleText;
        private TextView valueText;
        private TextView subtitleText;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            iconView = itemView.findViewById(R.id.iconView);
            titleText = itemView.findViewById(R.id.titleText);
            valueText = itemView.findViewById(R.id.valueText);
            subtitleText = itemView.findViewById(R.id.subtitleText);

            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onCardClick(cards.get(position));
                }
            });
        }

        public void bind(DashboardCard card) {
            titleText.setText(card.getTitle());
            valueText.setText(card.getValue());
            subtitleText.setText(card.getSubtitle());
            iconView.setImageResource(card.getIconResource());
            
            // Set card color
            int color = ContextCompat.getColor(itemView.getContext(), card.getColorResource());
            iconView.setColorFilter(color);
            
            // Add ripple effect
            cardView.setRippleColor(ContextCompat.getColorStateList(itemView.getContext(), R.color.ripple_light));
        }
    }
}