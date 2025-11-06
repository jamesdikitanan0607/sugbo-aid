package com.sugboaid.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Memory leak detection utility for monitoring Activities and Fragments
 */
public class MemoryLeakDetector {
    
    private static final String TAG = "MemoryLeakDetector";
    private static final long LEAK_DETECTION_DELAY = 5000; // 5 seconds
    
    private static MemoryLeakDetector instance;
    private final ConcurrentHashMap<String, WeakReference<Object>> trackedObjects;
    private final Handler mainHandler;
    private boolean isEnabled = true;
    
    private MemoryLeakDetector() {
        trackedObjects = new ConcurrentHashMap<>();
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public static synchronized MemoryLeakDetector getInstance() {
        if (instance == null) {
            instance = new MemoryLeakDetector();
        }
        return instance;
    }
    
    /**
     * Initialize memory leak detection for the application
     */
    public void initialize(Application application) {
        if (!isEnabled) return;
        
        application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks());
        Log.d(TAG, "Memory leak detection initialized");
    }
    
    /**
     * Track an object for potential memory leaks
     */
    public void trackObject(Object object, String identifier) {
        if (!isEnabled || object == null || identifier == null) return;
        
        String key = object.getClass().getSimpleName() + "_" + identifier + "_" + System.currentTimeMillis();
        trackedObjects.put(key, new WeakReference<>(object));
        
        Log.d(TAG, "Tracking object: " + key);
    }
    
    /**
     * Stop tracking an object
     */
    public void stopTracking(String identifier) {
        if (!isEnabled || identifier == null) return;
        
        Iterator<String> iterator = trackedObjects.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (key.contains(identifier)) {
                iterator.remove();
                Log.d(TAG, "Stopped tracking: " + key);
            }
        }
    }
    
    /**
     * Check for memory leaks by examining weak references
     */
    public void checkForLeaks() {
        if (!isEnabled) return;
        
        mainHandler.postDelayed(() -> {
            System.gc(); // Suggest garbage collection
            
            List<String> leakedObjects = new ArrayList<>();
            
            for (String key : trackedObjects.keySet()) {
                WeakReference<Object> ref = trackedObjects.get(key);
                if (ref != null && ref.get() != null) {
                    leakedObjects.add(key);
                }
            }
            
            if (!leakedObjects.isEmpty()) {
                Log.w(TAG, "Potential memory leaks detected:");
                for (String leaked : leakedObjects) {
                    Log.w(TAG, "  - " + leaked);
                }
            } else {
                Log.d(TAG, "No memory leaks detected");
            }
            
            // Clean up null references
            cleanupNullReferences();
            
        }, LEAK_DETECTION_DELAY);
    }
    
    /**
     * Clean up null weak references
     */
    private void cleanupNullReferences() {
        Iterator<String> iterator = trackedObjects.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            WeakReference<Object> ref = trackedObjects.get(key);
            if (ref == null || ref.get() == null) {
                iterator.remove();
            }
        }
    }
    
    /**
     * Get current memory usage statistics
     */
    public MemoryStats getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return new MemoryStats(maxMemory, totalMemory, usedMemory, freeMemory, trackedObjects.size());
    }
    
    /**
     * Enable or disable memory leak detection
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        if (!enabled) {
            trackedObjects.clear();
        }
    }
    
    /**
     * Activity lifecycle callbacks for automatic tracking
     */
    private class ActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            trackObject(activity, activity.getClass().getSimpleName());
            
            // Track fragments if it's a FragmentActivity
            if (activity instanceof FragmentActivity) {
                FragmentActivity fragmentActivity = (FragmentActivity) activity;
                fragmentActivity.getSupportFragmentManager()
                    .registerFragmentLifecycleCallbacks(new FragmentLifecycleCallbacks(), true);
            }
        }
        
        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            // No action needed
        }
        
        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            // No action needed
        }
        
        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            // No action needed
        }
        
        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            // No action needed
        }
        
        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            // No action needed
        }
        
        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            stopTracking(activity.getClass().getSimpleName());
            checkForLeaks();
        }
    }
    
    /**
     * Fragment lifecycle callbacks for automatic tracking
     */
    private class FragmentLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks {
        
        @Override
        public void onFragmentCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
            trackObject(f, f.getClass().getSimpleName());
            
            // Add lifecycle observer for additional monitoring
            f.getLifecycle().addObserver(new FragmentLifecycleObserver(f));
        }
        
        @Override
        public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            stopTracking(f.getClass().getSimpleName());
            checkForLeaks();
        }
    }
    
    /**
     * Fragment lifecycle observer for detailed monitoring
     */
    private class FragmentLifecycleObserver implements LifecycleEventObserver {
        private final WeakReference<Fragment> fragmentRef;
        
        public FragmentLifecycleObserver(Fragment fragment) {
            this.fragmentRef = new WeakReference<>(fragment);
        }
        
        @Override
        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
            Fragment fragment = fragmentRef.get();
            if (fragment == null) return;
            
            switch (event) {
                case ON_CREATE:
                    Log.d(TAG, "Fragment created: " + fragment.getClass().getSimpleName());
                    break;
                case ON_DESTROY:
                    Log.d(TAG, "Fragment destroyed: " + fragment.getClass().getSimpleName());
                    source.getLifecycle().removeObserver(this);
                    break;
                default:
                    // No action for other events
                    break;
            }
        }
    }
    
    /**
     * Memory statistics holder class
     */
    public static class MemoryStats {
        public final long maxMemory;
        public final long totalMemory;
        public final long usedMemory;
        public final long freeMemory;
        public final int trackedObjectsCount;
        
        public MemoryStats(long maxMemory, long totalMemory, long usedMemory, long freeMemory, int trackedObjectsCount) {
            this.maxMemory = maxMemory;
            this.totalMemory = totalMemory;
            this.usedMemory = usedMemory;
            this.freeMemory = freeMemory;
            this.trackedObjectsCount = trackedObjectsCount;
        }
        
        public double getUsagePercentage() {
            return (double) usedMemory / maxMemory * 100;
        }
        
        public boolean isMemoryLow() {
            return getUsagePercentage() > 85;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Memory Stats: %.1f%% used (Max: %d MB, Used: %d MB, Free: %d MB), Tracked Objects: %d",
                getUsagePercentage(),
                maxMemory / (1024 * 1024),
                usedMemory / (1024 * 1024),
                freeMemory / (1024 * 1024),
                trackedObjectsCount
            );
        }
    }
    
    /**
     * Utility methods for common memory leak prevention
     */
    public static class LeakPrevention {
        
        /**
         * Safely clear context references from views
         */
        public static void clearViewReferences(android.view.View view) {
            if (view == null) return;
            
            // Clear background drawable
            view.setBackground(null);
            
            // Clear animation listeners
            view.clearAnimation();
            
            // Clear view tree observers
            if (view.getViewTreeObserver().isAlive()) {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(null);
            }
        }
        
        /**
         * Safely clear adapter references
         */
        public static void clearAdapterReferences(androidx.recyclerview.widget.RecyclerView recyclerView) {
            if (recyclerView == null) return;
            
            recyclerView.setAdapter(null);
            recyclerView.setLayoutManager(null);
            recyclerView.clearOnScrollListeners();
        }
        
        /**
         * Clear handler callbacks to prevent leaks
         */
        public static void clearHandlerCallbacks(Handler handler) {
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }
        
        /**
         * Clear context references from drawables
         */
        public static void clearDrawableCallbacks(android.graphics.drawable.Drawable drawable) {
            if (drawable != null) {
                drawable.setCallback(null);
            }
        }
    }
}