package com.sugboaid.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import androidx.recyclerview.widget.RecyclerView;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Performance optimization utility class for memory management and RecyclerView optimization
 */
public class PerformanceOptimizer {
    
    private static PerformanceOptimizer instance;
    private final LruCache<String, Bitmap> bitmapCache;
    private final LruCache<String, Drawable> drawableCache;
    private final ExecutorService backgroundExecutor;
    private final Handler mainHandler;
    
    // Memory management constants
    private static final int MAX_MEMORY = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private static final int CACHE_SIZE = MAX_MEMORY / 8; // Use 1/8th of available memory
    
    private PerformanceOptimizer() {
        // Initialize bitmap cache
        bitmapCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
            
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if (oldValue != null && !oldValue.isRecycled()) {
                    oldValue.recycle();
                }
            }
        };
        
        // Initialize drawable cache
        drawableCache = new LruCache<String, Drawable>(100) {
            @Override
            protected int sizeOf(String key, Drawable drawable) {
                return 1; // Each drawable counts as 1
            }
        };
        
        backgroundExecutor = Executors.newFixedThreadPool(2);
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public static synchronized PerformanceOptimizer getInstance() {
        if (instance == null) {
            instance = new PerformanceOptimizer();
        }
        return instance;
    }
    
    /**
     * Optimizes RecyclerView performance with proper settings
     */
    public void optimizeRecyclerView(RecyclerView recyclerView) {
        if (recyclerView == null) return;
        
        // Enable hardware acceleration
        recyclerView.setLayerType(RecyclerView.LAYER_TYPE_HARDWARE, null);
        
        // Set item animator to null for better performance during updates
        recyclerView.setItemAnimator(null);
        
        // Enable nested scrolling optimization
        recyclerView.setNestedScrollingEnabled(true);
        
        // Set fixed size if the RecyclerView size won't change
        recyclerView.setHasFixedSize(true);
        
        // Enable drawing cache
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(RecyclerView.DRAWING_CACHE_QUALITY_HIGH);
        
        // Optimize scroll performance
        recyclerView.setItemViewCacheSize(20); // Increase cache size
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 20);
    }
    
    /**
     * Optimizes ViewHolder for better performance
     */
    public void optimizeViewHolder(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder == null || viewHolder.itemView == null) return;
        
        // Enable hardware acceleration for the item view
        viewHolder.itemView.setLayerType(RecyclerView.LAYER_TYPE_HARDWARE, null);
        
        // Optimize drawing cache
        viewHolder.itemView.setDrawingCacheEnabled(true);
    }
    
    /**
     * Caches bitmap with memory management
     */
    public void cacheBitmap(String key, Bitmap bitmap) {
        if (key != null && bitmap != null && !bitmap.isRecycled()) {
            bitmapCache.put(key, bitmap);
        }
    }
    
    /**
     * Retrieves cached bitmap
     */
    public Bitmap getCachedBitmap(String key) {
        if (key == null) return null;
        Bitmap bitmap = bitmapCache.get(key);
        return (bitmap != null && !bitmap.isRecycled()) ? bitmap : null;
    }
    
    /**
     * Caches drawable
     */
    public void cacheDrawable(String key, Drawable drawable) {
        if (key != null && drawable != null) {
            drawableCache.put(key, drawable);
        }
    }
    
    /**
     * Retrieves cached drawable
     */
    public Drawable getCachedDrawable(String key) {
        return key != null ? drawableCache.get(key) : null;
    }
    
    /**
     * Executes task in background thread
     */
    public void executeInBackground(Runnable task) {
        if (task != null) {
            backgroundExecutor.execute(task);
        }
    }
    
    /**
     * Executes task on main thread
     */
    public void executeOnMainThread(Runnable task) {
        if (task != null) {
            mainHandler.post(task);
        }
    }
    
    /**
     * Executes task on main thread with delay
     */
    public void executeOnMainThreadDelayed(Runnable task, long delayMillis) {
        if (task != null) {
            mainHandler.postDelayed(task, delayMillis);
        }
    }
    
    /**
     * Clears all caches to free memory
     */
    public void clearCaches() {
        bitmapCache.evictAll();
        drawableCache.evictAll();
    }
    
    /**
     * Gets memory usage information
     */
    public MemoryInfo getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return new MemoryInfo(maxMemory, totalMemory, usedMemory, freeMemory);
    }
    
    /**
     * Triggers garbage collection if memory usage is high
     */
    public void optimizeMemoryUsage() {
        MemoryInfo memInfo = getMemoryInfo();
        double memoryUsagePercent = (double) memInfo.usedMemory / memInfo.maxMemory * 100;
        
        if (memoryUsagePercent > 80) {
            // Clear some cache entries
            bitmapCache.trimToSize(bitmapCache.size() / 2);
            drawableCache.trimToSize(drawableCache.size() / 2);
            
            // Suggest garbage collection
            System.gc();
        }
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        clearCaches();
        backgroundExecutor.shutdown();
        mainHandler.removeCallbacksAndMessages(null);
    }
    
    /**
     * Memory information holder class
     */
    public static class MemoryInfo {
        public final long maxMemory;
        public final long totalMemory;
        public final long usedMemory;
        public final long freeMemory;
        
        public MemoryInfo(long maxMemory, long totalMemory, long usedMemory, long freeMemory) {
            this.maxMemory = maxMemory;
            this.totalMemory = totalMemory;
            this.usedMemory = usedMemory;
            this.freeMemory = freeMemory;
        }
        
        public double getUsagePercentage() {
            return (double) usedMemory / maxMemory * 100;
        }
        
        @Override
        public String toString() {
            return String.format("Memory Usage: %.1f%% (Used: %d KB, Free: %d KB, Max: %d KB)",
                getUsagePercentage(),
                usedMemory / 1024,
                freeMemory / 1024,
                maxMemory / 1024);
        }
    }
    
    /**
     * RecyclerView scroll listener for performance optimization
     */
    public static class PerformanceScrollListener extends RecyclerView.OnScrollListener {
        private final WeakReference<RecyclerView> recyclerViewRef;
        private boolean isScrolling = false;
        
        public PerformanceScrollListener(RecyclerView recyclerView) {
            this.recyclerViewRef = new WeakReference<>(recyclerView);
        }
        
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            
            isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE;
            
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                // Optimize memory when scrolling stops
                PerformanceOptimizer.getInstance().optimizeMemoryUsage();
            }
        }
        
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            
            // Disable complex animations during fast scrolling
            if (Math.abs(dy) > 20) {
                RecyclerView rv = recyclerViewRef.get();
                if (rv != null && rv.getItemAnimator() != null) {
                    rv.getItemAnimator().setChangeDuration(0);
                    rv.getItemAnimator().setMoveDuration(0);
                }
            }
        }
        
        public boolean isScrolling() {
            return isScrolling;
        }
    }
}