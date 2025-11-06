package com.sugboaid.donation;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.sugboaid.donation.activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Performance and memory leak detection tests
 * Tests app performance under various conditions
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PerformanceTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = 
        new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testPerformance_NavigationSpeed() {
        long startTime, endTime;
        
        // Test navigation performance between screens
        for (int i = 0; i < 10; i++) {
            // Navigate to dashboard
            startTime = System.currentTimeMillis();
            onView(withId(R.id.nav_dashboard)).perform(click());
            endTime = System.currentTimeMillis();
            
            // Navigation should be fast (less than 500ms)
            assert(endTime - startTime < 500);
            
            // Navigate to inventory
            startTime = System.currentTimeMillis();
            onView(withId(R.id.nav_inventory)).perform(click());
            endTime = System.currentTimeMillis();
            
            assert(endTime - startTime < 500);
            
            // Navigate to transparency
            startTime = System.currentTimeMillis();
            onView(withId(R.id.nav_transparency)).perform(click());
            endTime = System.currentTimeMillis();
            
            assert(endTime - startTime < 500);
            
            // Navigate to reports
            startTime = System.currentTimeMillis();
            onView(withId(R.id.nav_reports)).perform(click());
            endTime = System.currentTimeMillis();
            
            assert(endTime - startTime < 500);
            
            // Navigate to notifications
            startTime = System.currentTimeMillis();
            onView(withId(R.id.nav_notifications)).perform(click());
            endTime = System.currentTimeMillis();
            
            assert(endTime - startTime < 500);
        }
    }

    @Test
    public void testPerformance_MemoryUsage() {
        // Get initial memory usage
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Perform navigation operations that might cause memory leaks
        for (int i = 0; i < 50; i++) {
            onView(withId(R.id.nav_dashboard)).perform(click());
            onView(withId(R.id.nav_inventory)).perform(click());
            onView(withId(R.id.nav_transparency)).perform(click());
            onView(withId(R.id.nav_reports)).perform(click());
            onView(withId(R.id.nav_notifications)).perform(click());
            
            // Force garbage collection every 10 iterations
            if (i % 10 == 0) {
                System.gc();
                try {
                    Thread.sleep(100); // Give GC time to work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        // Force final garbage collection
        System.gc();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check final memory usage
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // Memory increase should be reasonable (less than 50MB)
        // This is a rough check - actual thresholds would depend on app requirements
        assert(memoryIncrease < 50 * 1024 * 1024); // 50MB in bytes
    }

    @Test
    public void testPerformance_DarkModeToggleSpeed() {
        // Test dark mode toggle performance
        for (int i = 0; i < 20; i++) {
            long startTime = System.currentTimeMillis();
            onView(withId(R.id.fab_dark_mode_toggle)).perform(click());
            long endTime = System.currentTimeMillis();
            
            // Theme toggle should be fast (less than 200ms)
            assert(endTime - startTime < 200);
            
            // Small delay to allow theme change to complete
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    public void testPerformance_StressTestNavigation() {
        // Stress test with rapid navigation
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            onView(withId(R.id.nav_dashboard)).perform(click());
            onView(withId(R.id.nav_inventory)).perform(click());
            onView(withId(R.id.nav_transparency)).perform(click());
            onView(withId(R.id.nav_reports)).perform(click());
            onView(withId(R.id.nav_notifications)).perform(click());
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // 500 navigation operations should complete in reasonable time (less than 30 seconds)
        assert(totalTime < 30000);
        
        // Average per navigation should be fast (less than 60ms)
        double averageTime = totalTime / 500.0;
        assert(averageTime < 60);
    }

    @Test
    public void testPerformance_UIResponsiveness() {
        // Test UI responsiveness during operations
        
        // Navigate to dashboard and perform operations
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Test multiple quick clicks (should not cause ANR)
        for (int i = 0; i < 10; i++) {
            onView(withId(R.id.fab_quick_donation)).perform(click());
            
            // Small delay to prevent overwhelming the system
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // UI should still be responsive
        onView(withId(R.id.nav_inventory)).perform(click());
        onView(withId(R.id.nav_dashboard)).perform(click());
    }

    @Test
    public void testPerformance_AnimationPerformance() {
        // Test animation performance by triggering animations repeatedly
        
        for (int i = 0; i < 20; i++) {
            // Navigate between screens to trigger animations
            onView(withId(R.id.nav_dashboard)).perform(click());
            
            // Small delay to allow animation to start
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            onView(withId(R.id.nav_inventory)).perform(click());
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // App should still be responsive after animations
        onView(withId(R.id.fab_dark_mode_toggle)).perform(click());
    }

    @Test
    public void testPerformance_ConcurrentOperations() {
        // Test performance with concurrent operations
        
        // Simulate concurrent user actions
        for (int i = 0; i < 10; i++) {
            // Quick succession of different operations
            onView(withId(R.id.nav_dashboard)).perform(click());
            onView(withId(R.id.fab_dark_mode_toggle)).perform(click());
            onView(withId(R.id.nav_inventory)).perform(click());
            onView(withId(R.id.fab_dark_mode_toggle)).perform(click());
            
            // Very short delay
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // App should handle concurrent operations gracefully
        onView(withId(R.id.nav_dashboard)).perform(click());
    }

    @Test
    public void testPerformance_ResourceCleanup() {
        // Test that resources are properly cleaned up
        
        Runtime runtime = Runtime.getRuntime();
        
        // Perform operations that create and destroy resources
        for (int i = 0; i < 30; i++) {
            // Navigate through all screens
            onView(withId(R.id.nav_dashboard)).perform(click());
            onView(withId(R.id.nav_inventory)).perform(click());
            onView(withId(R.id.nav_transparency)).perform(click());
            onView(withId(R.id.nav_reports)).perform(click());
            onView(withId(R.id.nav_notifications)).perform(click());
            
            // Toggle theme (creates/destroys themed resources)
            onView(withId(R.id.fab_dark_mode_toggle)).perform(click());
            
            // Force garbage collection periodically
            if (i % 10 == 0) {
                System.gc();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Check that memory usage is reasonable
                long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                // Memory should not grow excessively (less than 100MB)
                assert(currentMemory < 100 * 1024 * 1024);
            }
        }
    }
}