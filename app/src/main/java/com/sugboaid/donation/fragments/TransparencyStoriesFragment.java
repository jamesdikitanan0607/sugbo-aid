package com.sugboaid.donation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sugboaid.donation.R;
import com.sugboaid.donation.adapters.ImpactStoryAdapter;
import com.sugboaid.donation.models.ImpactStory;
import com.sugboaid.donation.viewmodels.TransparencyViewModel;

import java.util.List;

/**
 * Impact Stories fragment showing family stories and assistance details
 * Displays story cards with images, narratives, and location information
 */
public class TransparencyStoriesFragment extends BaseFragment {

    private RecyclerView storiesRecyclerView;
    private TextView totalStoriesText;
    private TextView totalFamiliesText;
    private ImpactStoryAdapter storyAdapter;
    private TransparencyViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transparency_stories, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TransparencyViewModel.class);
    }

    @Override
    protected void initViews(View view) {
        storiesRecyclerView = view.findViewById(R.id.storiesRecyclerView);
        totalStoriesText = view.findViewById(R.id.totalStoriesText);
        totalFamiliesText = view.findViewById(R.id.totalFamiliesText);

        // Setup RecyclerView
        storyAdapter = new ImpactStoryAdapter();
        storiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        storiesRecyclerView.setAdapter(storyAdapter);
    }

    @Override
    protected void setupListeners() {
        // Set up story item click listener if needed
        storyAdapter.setOnStoryClickListener(story -> {
            // Handle story click - could open detailed view
            showToast("Story: " + story.getFamilyName());
        });
    }

    @Override
    protected void observeData() {
        // Observe impact stories
        viewModel.getImpactStories().observe(getViewLifecycleOwner(), this::updateStories);
        
        // Observe summary statistics
        viewModel.getTotalStories().observe(getViewLifecycleOwner(), total -> {
            if (totalStoriesText != null) {
                totalStoriesText.setText(String.valueOf(total));
            }
        });

        viewModel.getTotalFamiliesHelped().observe(getViewLifecycleOwner(), families -> {
            if (totalFamiliesText != null) {
                totalFamiliesText.setText(String.valueOf(families));
            }
        });
    }

    @Override
    protected void refreshData() {
        if (viewModel != null) {
            viewModel.refreshStoriesData();
        }
    }

    private void updateStories(List<ImpactStory> stories) {
        if (storyAdapter != null) {
            storyAdapter.updateStories(stories);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up references
        storiesRecyclerView = null;
        totalStoriesText = null;
        totalFamiliesText = null;
        storyAdapter = null;
    }
}