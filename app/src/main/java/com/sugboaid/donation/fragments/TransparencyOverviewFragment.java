package com.sugboaid.donation.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.sugboaid.donation.R;
import com.sugboaid.donation.viewmodels.TransparencyViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Overview tab fragment showing donation trends and distribution charts
 * Integrates MPAndroidChart library for data visualization
 */
public class TransparencyOverviewFragment extends BaseFragment {

    private LineChart donationTrendsChart;
    private BarChart distributionChart;
    private PieChart categoryBreakdownChart;
    private TextView totalDonationsText;
    private TextView totalDistributedText;
    private TextView totalFamiliesText;
    private TransparencyViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transparency_overview, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransparencyViewModel.class);
    }

    @Override
    protected void initViews(View view) {
        // Initialize charts
        donationTrendsChart = view.findViewById(R.id.donationTrendsChart);
        distributionChart = view.findViewById(R.id.distributionChart);
        categoryBreakdownChart = view.findViewById(R.id.categoryBreakdownChart);
        
        // Initialize summary text views
        totalDonationsText = view.findViewById(R.id.totalDonationsText);
        totalDistributedText = view.findViewById(R.id.totalDistributedText);
        totalFamiliesText = view.findViewById(R.id.totalFamiliesText);

        // Setup charts
        setupDonationTrendsChart();
        setupDistributionChart();
        setupCategoryBreakdownChart();
    }

    @Override
    protected void setupListeners() {
        // No specific listeners needed for charts
    }

    @Override
    protected void observeData() {
        // Observe donation statistics
        viewModel.getTotalDonations().observe(getViewLifecycleOwner(), total -> {
            if (totalDonationsText != null) {
                totalDonationsText.setText(String.format("₱%,.2f", total));
            }
        });

        viewModel.getTotalDistributed().observe(getViewLifecycleOwner(), distributed -> {
            if (totalDistributedText != null) {
                totalDistributedText.setText(String.valueOf(distributed));
            }
        });

        viewModel.getTotalFamiliesHelped().observe(getViewLifecycleOwner(), families -> {
            if (totalFamiliesText != null) {
                totalFamiliesText.setText(String.valueOf(families));
            }
        });

        // Observe chart data
        viewModel.getDonationTrends().observe(getViewLifecycleOwner(), this::updateDonationTrendsChart);
        viewModel.getDistributionData().observe(getViewLifecycleOwner(), this::updateDistributionChart);
        viewModel.getCategoryBreakdown().observe(getViewLifecycleOwner(), this::updateCategoryBreakdownChart);
    }

    @Override
    protected void refreshData() {
        if (viewModel != null) {
            viewModel.refreshData();
        }
    }

    private void setupDonationTrendsChart() {
        if (donationTrendsChart == null) return;

        // Configure chart appearance
        donationTrendsChart.getDescription().setEnabled(false);
        donationTrendsChart.setTouchEnabled(true);
        donationTrendsChart.setDragEnabled(true);
        donationTrendsChart.setScaleEnabled(true);
        donationTrendsChart.setPinchZoom(true);
        donationTrendsChart.setDrawGridBackground(false);

        // Configure X axis
        XAxis xAxis = donationTrendsChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);

        // Configure Y axis
        YAxis leftAxis = donationTrendsChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = donationTrendsChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Configure legend
        Legend legend = donationTrendsChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    private void setupDistributionChart() {
        if (distributionChart == null) return;

        // Configure chart appearance
        distributionChart.getDescription().setEnabled(false);
        distributionChart.setTouchEnabled(true);
        distributionChart.setDragEnabled(true);
        distributionChart.setScaleEnabled(true);
        distributionChart.setPinchZoom(true);
        distributionChart.setDrawGridBackground(false);

        // Configure X axis
        XAxis xAxis = distributionChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        // Configure Y axis
        YAxis leftAxis = distributionChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = distributionChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Configure legend
        Legend legend = distributionChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    private void setupCategoryBreakdownChart() {
        if (categoryBreakdownChart == null) return;

        // Configure chart appearance
        categoryBreakdownChart.getDescription().setEnabled(false);
        categoryBreakdownChart.setUsePercentValues(true);
        categoryBreakdownChart.setDrawHoleEnabled(true);
        categoryBreakdownChart.setHoleColor(Color.WHITE);
        categoryBreakdownChart.setTransparentCircleColor(Color.WHITE);
        categoryBreakdownChart.setTransparentCircleAlpha(110);
        categoryBreakdownChart.setHoleRadius(58f);
        categoryBreakdownChart.setTransparentCircleRadius(61f);
        categoryBreakdownChart.setDrawCenterText(true);
        categoryBreakdownChart.setCenterText("Distribution\nBreakdown");
        categoryBreakdownChart.setCenterTextSize(12f);
        categoryBreakdownChart.setRotationAngle(0);
        categoryBreakdownChart.setRotationEnabled(true);
        categoryBreakdownChart.setHighlightPerTapEnabled(true);

        // Configure legend
        Legend legend = categoryBreakdownChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);
    }

    private void updateDonationTrendsChart(List<Entry> entries) {
        if (donationTrendsChart == null || entries == null) return;

        LineDataSet dataSet = new LineDataSet(entries, "Donation Trends");
        dataSet.setColor(getResources().getColor(R.color.primary_blue, null));
        dataSet.setCircleColor(getResources().getColor(R.color.primary_blue, null));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.primary_blue_20, null));

        LineData lineData = new LineData(dataSet);
        donationTrendsChart.setData(lineData);
        
        // Set X-axis labels (last 7 days)
        String[] labels = {"6 days ago", "5 days ago", "4 days ago", "3 days ago", "2 days ago", "Yesterday", "Today"};
        donationTrendsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        
        donationTrendsChart.invalidate(); // refresh
    }

    private void updateDistributionChart(List<BarEntry> entries) {
        if (distributionChart == null || entries == null) return;

        BarDataSet dataSet = new BarDataSet(entries, "Distribution by Category");
        
        // Set colors for different categories
        int[] colors = {
            getResources().getColor(R.color.primary_blue, null),
            getResources().getColor(R.color.primary_green, null),
            getResources().getColor(R.color.accent_yellow, null),
            getResources().getColor(R.color.warning_orange, null)
        };
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);
        distributionChart.setData(barData);
        
        // Set X-axis labels
        String[] labels = {"Rice", "Water", "Medicine", "Clothes"};
        distributionChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        
        distributionChart.invalidate(); // refresh
    }

    private void updateCategoryBreakdownChart(List<PieEntry> entries) {
        if (categoryBreakdownChart == null || entries == null) return;

        PieDataSet dataSet = new PieDataSet(entries, "");
        
        // Set colors for pie chart
        int[] colors = {
            getResources().getColor(R.color.primary_blue, null),
            getResources().getColor(R.color.primary_green, null),
            getResources().getColor(R.color.accent_yellow, null),
            getResources().getColor(R.color.warning_orange, null)
        };
        dataSet.setColors(colors);
        dataSet.setValueLinePart1OffsetPercentage(80f);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueTextSize(10f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.BLACK);
        
        categoryBreakdownChart.setData(pieData);
        categoryBreakdownChart.invalidate(); // refresh
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up chart references
        donationTrendsChart = null;
        distributionChart = null;
        categoryBreakdownChart = null;
        totalDonationsText = null;
        totalDistributedText = null;
        totalFamiliesText = null;
    }
}