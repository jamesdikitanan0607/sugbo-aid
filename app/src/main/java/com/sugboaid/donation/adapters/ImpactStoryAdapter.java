package com.sugboaid.donation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sugboaid.donation.R;
import com.sugboaid.donation.models.ImpactStory;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying impact stories with family information
 */
public class ImpactStoryAdapter extends RecyclerView.Adapter<ImpactStoryAdapter.StoryViewHolder> {

    private List<ImpactStory> stories = new ArrayList<>();
    private OnStoryClickListener onStoryClickListener;

    public interface OnStoryClickListener {
        void onStoryClick(ImpactStory story);
    }

    public void setOnStoryClickListener(OnStoryClickListener listener) {
        this.onStoryClickListener = listener;
    }

    public void updateStories(List<ImpactStory> newStories) {
        this.stories.clear();
        if (newStories != null) {
            this.stories.addAll(newStories);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_impact_story, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        ImpactStory story = stories.get(position);
        holder.bind(story);
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    class StoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView familyImageView;
        private final TextView familyNameText;
        private final TextView locationText;
        private final TextView dateText;
        private final TextView familySizeText;
        private final TextView storyText;
        private final TextView assistanceText;
        private final ImageView verifiedIcon;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            familyImageView = itemView.findViewById(R.id.familyImageView);
            familyNameText = itemView.findViewById(R.id.familyNameText);
            locationText = itemView.findViewById(R.id.locationText);
            dateText = itemView.findViewById(R.id.dateText);
            familySizeText = itemView.findViewById(R.id.familySizeText);
            storyText = itemView.findViewById(R.id.storyText);
            assistanceText = itemView.findViewById(R.id.assistanceText);
            verifiedIcon = itemView.findViewById(R.id.verifiedIcon);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onStoryClickListener != null) {
                    onStoryClickListener.onStoryClick(stories.get(position));
                }
            });
        }

        public void bind(ImpactStory story) {
            familyNameText.setText(story.getFamilyName());
            locationText.setText(story.getFullLocation());
            dateText.setText(story.getFormattedDate());
            familySizeText.setText(story.getFamilySizeDescription());
            storyText.setText(story.getStory());
            assistanceText.setText(story.getAssistanceReceived());

            // Set verified icon visibility
            verifiedIcon.setVisibility(story.isVerified() ? View.VISIBLE : View.GONE);

            // Set placeholder image (in a real app, you'd load actual images)
            familyImageView.setImageResource(R.drawable.ic_family);
        }
    }
}