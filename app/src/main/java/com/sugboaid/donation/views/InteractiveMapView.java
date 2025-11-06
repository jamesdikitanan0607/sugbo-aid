package com.sugboaid.donation.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sugboaid.donation.R;
import com.sugboaid.donation.models.BarangayLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom view for displaying an interactive map with barangay locations
 * Supports pan, zoom, and marker interactions
 */
public class InteractiveMapView extends View {

    private Paint mapPaint;
    private Paint markerPaint;
    private Paint textPaint;
    private Paint borderPaint;
    
    private List<BarangayLocation> barangays = new ArrayList<>();
    private OnMarkerClickListener onMarkerClickListener;
    
    // Map transformation
    private float scaleFactor = 1.0f;
    private float translateX = 0f;
    private float translateY = 0f;
    private final float minScale = 0.5f;
    private final float maxScale = 3.0f;
    
    // Gesture detection
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    
    // Map bounds (Cebu City approximate coordinates)
    private final float mapMinLat = 10.2500f;
    private final float mapMaxLat = 10.3500f;
    private final float mapMinLng = 123.8500f;
    private final float mapMaxLng = 123.9500f;
    
    // Marker properties
    private final float markerRadius = 12f;
    private final float textSize = 24f;

    public interface OnMarkerClickListener {
        void onMarkerClick(BarangayLocation barangay);
    }

    public InteractiveMapView(Context context) {
        super(context);
        init();
    }

    public InteractiveMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InteractiveMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Initialize paints
        mapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mapPaint.setColor(ContextCompat.getColor(getContext(), R.color.light_gray));
        mapPaint.setStyle(Paint.Style.FILL);

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(ContextCompat.getColor(getContext(), R.color.medium_gray));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);

        // Initialize gesture detectors
        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
        
        // Initialize with sample barangay data
        initializeSampleData();
    }

    public void initialize() {
        // Additional initialization if needed
        invalidate();
    }

    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        this.onMarkerClickListener = listener;
    }

    public void updateMarkers(List<BarangayLocation> barangays) {
        this.barangays.clear();
        if (barangays != null) {
            this.barangays.addAll(barangays);
        }
        invalidate();
    }

    public void focusOnBarangay(BarangayLocation barangay) {
        if (barangay == null) return;
        
        // Calculate the position of the barangay on the map
        PointF position = latLngToScreen(barangay.getLatitude(), barangay.getLongitude());
        
        // Center the map on this barangay
        translateX = getWidth() / 2f - position.x * scaleFactor;
        translateY = getHeight() / 2f - position.y * scaleFactor;
        
        // Ensure we don't go beyond bounds
        constrainTranslation();
        
        invalidate();
    }

    private void initializeSampleData() {
        // Sample barangay data for Cebu City
        barangays.add(new BarangayLocation("Lahug", 10.3167, 123.8833, 45, 25000.0, "active", 12));
        barangays.add(new BarangayLocation("Capitol Site", 10.3000, 123.8900, 32, 18500.0, "moderate", 8));
        barangays.add(new BarangayLocation("Guadalupe", 10.2833, 123.8667, 28, 15200.0, "low", 6));
        barangays.add(new BarangayLocation("Banilad", 10.3333, 123.9000, 38, 22800.0, "active", 10));
        barangays.add(new BarangayLocation("Talamban", 10.3500, 123.9167, 15, 8900.0, "critical", 3));
        barangays.add(new BarangayLocation("Mabolo", 10.3167, 123.9000, 41, 24600.0, "active", 11));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        canvas.save();
        
        // Apply transformations
        canvas.translate(translateX, translateY);
        canvas.scale(scaleFactor, scaleFactor);
        
        // Draw map background
        drawMapBackground(canvas);
        
        // Draw barangay markers
        drawBarangayMarkers(canvas);
        
        canvas.restore();
    }

    private void drawMapBackground(Canvas canvas) {
        // Draw a simple map outline (simplified Cebu City shape)
        Path mapPath = new Path();
        
        // Create a simplified outline of Cebu City
        float width = getWidth() / scaleFactor;
        float height = getHeight() / scaleFactor;
        
        RectF mapBounds = new RectF(width * 0.1f, height * 0.1f, width * 0.9f, height * 0.9f);
        
        // Draw main city area
        mapPath.addRoundRect(mapBounds, 20f, 20f, Path.Direction.CW);
        
        canvas.drawPath(mapPath, mapPaint);
        canvas.drawPath(mapPath, borderPaint);
        
        // Draw some geographical features (simplified)
        drawGeographicalFeatures(canvas, mapBounds);
    }

    private void drawGeographicalFeatures(Canvas canvas, RectF bounds) {
        Paint featurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        featurePaint.setColor(ContextCompat.getColor(getContext(), R.color.primary_blue_20));
        featurePaint.setStyle(Paint.Style.FILL);
        
        // Draw some water bodies (simplified)
        canvas.drawCircle(bounds.left + bounds.width() * 0.2f, 
                         bounds.top + bounds.height() * 0.3f, 
                         bounds.width() * 0.08f, featurePaint);
        
        canvas.drawCircle(bounds.left + bounds.width() * 0.7f, 
                         bounds.top + bounds.height() * 0.6f, 
                         bounds.width() * 0.06f, featurePaint);
    }

    private void drawBarangayMarkers(Canvas canvas) {
        for (BarangayLocation barangay : barangays) {
            PointF position = latLngToScreen(barangay.getLatitude(), barangay.getLongitude());
            
            // Set marker color based on status
            int markerColor = ContextCompat.getColor(getContext(), barangay.getStatusColorResource());
            markerPaint.setColor(markerColor);
            
            // Draw marker circle
            canvas.drawCircle(position.x, position.y, markerRadius, markerPaint);
            
            // Draw marker border
            Paint markerBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            markerBorderPaint.setColor(Color.WHITE);
            markerBorderPaint.setStyle(Paint.Style.STROKE);
            markerBorderPaint.setStrokeWidth(3f);
            canvas.drawCircle(position.x, position.y, markerRadius, markerBorderPaint);
            
            // Draw barangay name (if zoom level is appropriate)
            if (scaleFactor > 1.2f) {
                canvas.drawText(barangay.getName(), 
                               position.x, 
                               position.y + markerRadius + textSize, 
                               textPaint);
            }
        }
    }

    private PointF latLngToScreen(double lat, double lng) {
        // Convert lat/lng to screen coordinates
        float width = getWidth() / scaleFactor;
        float height = getHeight() / scaleFactor;
        
        float x = (float) ((lng - mapMinLng) / (mapMaxLng - mapMinLng)) * width * 0.8f + width * 0.1f;
        float y = (float) ((mapMaxLat - lat) / (mapMaxLat - mapMinLat)) * height * 0.8f + height * 0.1f;
        
        return new PointF(x, y);
    }

    private BarangayLocation screenToBarangay(float screenX, float screenY) {
        // Convert screen coordinates back to find clicked barangay
        float mapX = (screenX - translateX) / scaleFactor;
        float mapY = (screenY - translateY) / scaleFactor;
        
        for (BarangayLocation barangay : barangays) {
            PointF position = latLngToScreen(barangay.getLatitude(), barangay.getLongitude());
            
            float distance = (float) Math.sqrt(
                Math.pow(mapX - position.x, 2) + Math.pow(mapY - position.y, 2)
            );
            
            if (distance <= markerRadius * 1.5f) { // Add some tolerance
                return barangay;
            }
        }
        
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = scaleDetector.onTouchEvent(event);
        handled = gestureDetector.onTouchEvent(event) || handled;
        
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // Check for marker clicks
            BarangayLocation clickedBarangay = screenToBarangay(event.getX(), event.getY());
            if (clickedBarangay != null && onMarkerClickListener != null) {
                onMarkerClickListener.onMarkerClick(clickedBarangay);
                return true;
            }
        }
        
        return handled || super.onTouchEvent(event);
    }

    private void constrainTranslation() {
        float maxTranslateX = 0;
        float minTranslateX = getWidth() - (getWidth() * scaleFactor);
        float maxTranslateY = 0;
        float minTranslateY = getHeight() - (getHeight() * scaleFactor);
        
        translateX = Math.max(minTranslateX, Math.min(maxTranslateX, translateX));
        translateY = Math.max(minTranslateY, Math.min(maxTranslateY, translateY));
    }

    public void onResume() {
        // Handle resume if needed
    }

    public void onPause() {
        // Handle pause if needed
    }

    public void onDestroy() {
        // Clean up resources
        barangays.clear();
        onMarkerClickListener = null;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(minScale, Math.min(scaleFactor, maxScale));
            
            constrainTranslation();
            invalidate();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            translateX -= distanceX;
            translateY -= distanceY;
            
            constrainTranslation();
            invalidate();
            return true;
        }
        
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
}