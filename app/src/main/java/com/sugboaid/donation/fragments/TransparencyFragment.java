package com.sugboaid.donation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sugboaid.donation.R;

/**
 * TransparencyFragment with tab navigation for Overview, Barangay Map, and Impact Stories
 * Provides public-facing interface showing donation distribution and impact metrics
 */
public class TransparencyFragment extends BaseFragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageButton backButton;
    private TransparencyPagerAdapter pagerAdapter;

    // Tab titles
    private final String[] tabTitles = {"Overview", "Barangay Map", "Impact Stories"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transparency, container, false);
    }

    @Override
    protected void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        backButton = view.findViewById(R.id.backButton);

        // Setup ViewPager2 with adapter
        pagerAdapter = new TransparencyPagerAdapter(requireActivity());
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }

    @Override
    protected void setupListeners() {
        // Back button navigation
        backButton.setOnClickListener(v -> navigateBack());

        // Tab selection listener for analytics or additional actions
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Handle tab selection if needed
                int position = tab.getPosition();
                // Could add analytics tracking here
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Handle tab unselection if needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle tab reselection if needed
            }
        });
    }

    @Override
    protected void observeData() {
        // No specific data observation needed at this level
        // Individual tab fragments will handle their own data observation
    }

    @Override
    protected void refreshData() {
        // Refresh data for all tab fragments
        if (pagerAdapter != null) {
            // The individual fragments will handle their own data refresh
            // when they become visible through their onResume() methods
        }
    }

    /**
     * ViewPager2 adapter for transparency tabs
     */
    private static class TransparencyPagerAdapter extends FragmentStateAdapter {

        public TransparencyPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new TransparencyOverviewFragment();
                case 1:
                    return new TransparencyMapFragment();
                case 2:
                    return new TransparencyStoriesFragment();
                default:
                    return new TransparencyOverviewFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3; // Overview, Barangay Map, Impact Stories
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up references
        tabLayout = null;
        viewPager = null;
        backButton = null;
        pagerAdapter = null;
    }
}