package com.sugboaid.donation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Rect;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sugboaid.donation.R;
import com.sugboaid.donation.adapters.BarangayAdapter;
import com.sugboaid.donation.models.BarangayLocation;
import com.sugboaid.donation.viewmodels.TransparencyViewModel;
import com.sugboaid.donation.views.InteractiveMapView;

import java.util.List;

/**
 * Barangay Map fragment showing interactive map with location markers
 * Displays barangay locations with donation information and family counts
 */
public class TransparencyMapFragment extends BaseFragment {

    private InteractiveMapView mapView;
    private RecyclerView barangayListRecyclerView;
    private TextView totalBarangaysText;
    private TextView totalFamiliesText;
    private BarangayAdapter barangayAdapter;
    private TransparencyViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transparency_map, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TransparencyViewModel.class);
    }

    @Override
    protected void initViews(View view) {
        mapView = view.findViewById(R.id.mapView);
        barangayListRecyclerView = view.findViewById(R.id.barangayListRecyclerView);
        totalBarangaysText = view.findViewById(R.id.totalBarangaysText);
        totalFamiliesText = view.findViewById(R.id.totalFamiliesText);

        // Setup RecyclerView (horizontal scrolling cards)
        barangayAdapter = new BarangayAdapter();
        barangayListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        barangayListRecyclerView.setHasFixedSize(true);
        barangayListRecyclerView.setAdapter(barangayAdapter);
        // Equal spacing between cards
        final int spacingPx = (int) (8 * getResources().getDisplayMetrics().density + 0.5f);
        barangayListRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int itemCount = parent.getAdapter() != null ? parent.getAdapter().getItemCount() : 0;
                outRect.top = 0;
                outRect.bottom = 0;
                outRect.left = position == 0 ? spacingPx : spacingPx / 2;
                outRect.right = position == itemCount - 1 ? spacingPx : spacingPx / 2;
            }
        });

        // Prevent parent (ViewPager2/ScrollView) from intercepting when horizontally scrolling the list
        barangayListRecyclerView.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });

        // Initialize map
        if (mapView != null) {
            mapView.initialize();
            // Allow panning/zooming without parent scroll/viewpager intercept
            mapView.setOnTouchListener((v, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false; // let the map handle the gesture
            });
        }
    }

    @Override
    protected void setupListeners() {
        // Set up barangay item click listener
        barangayAdapter.setOnBarangayClickListener(barangay -> {
            if (mapView != null) {
                mapView.focusOnBarangay(barangay);
            }
        });

        // Set up map marker click listener
        if (mapView != null) {
            mapView.setOnMarkerClickListener(barangay -> {
                // Scroll to the barangay in the list
                int position = barangayAdapter.getBarangayPosition(barangay);
                if (position >= 0) {
                    barangayListRecyclerView.smoothScrollToPosition(position);
                }
            });
        }
    }

    @Override
    protected void observeData() {
        // Observe barangay data
        viewModel.getBarangayLocations().observe(getViewLifecycleOwner(), this::updateBarangayData);
        
        // Observe summary statistics
        viewModel.getTotalBarangays().observe(getViewLifecycleOwner(), total -> {
            if (totalBarangaysText != null) {
                totalBarangaysText.setText(String.valueOf(total));
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
            viewModel.refreshBarangayData();
        }
    }

    private void updateBarangayData(List<BarangayLocation> barangays) {
        if (barangayAdapter != null) {
            barangayAdapter.updateBarangays(barangays);
        }
        
        if (mapView != null) {
            mapView.updateMarkers(barangays);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDestroy();
        }
        
        // Clean up references
        mapView = null;
        barangayListRecyclerView = null;
        totalBarangaysText = null;
        totalFamiliesText = null;
        barangayAdapter = null;
    }
}