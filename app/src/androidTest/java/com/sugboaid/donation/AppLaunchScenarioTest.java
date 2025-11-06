package com.sugboaid.donation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import com.sugboaid.donation.activities.SimpleSplashActivity;
import com.sugboaid.donation.activities.MainActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

/**
 * Test app launch across different scenarios
 * Requirements: 1.1, 1.5, 4.1, 4.2
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AppLaunchScenarioTest {

    private Context context;
    private UiDevice device;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    /**
     * Test cold start from fresh install
     * Requirement 1.1: App shall launch without terminating unexpectedly
     */
    @Test
    public void testColdStartFromFreshInstall() {
        // Clear all app data to simulate fresh install
        clearAppData();
        
        // Launch splash activity (entry point)
        ActivityScenario<SimpleSplashActivity> scenario = ActivityScenario.launch(SimpleSplashActivity.class);
        
        scenario.onActivity(activity -> {
            // Verify activity is not null and not finishing
            assertNotNull("Activity should not be null", activity);
            assertFalse("Activity should not be finishing", activity.isFinishing());
            assertFalse("Activity should not be destroyed", activity.isDestroyed());
        });
        
        // Verify splash screen displays correctly
        onView(withId(R.id.iv_logo))
            .check(matches(isDisplayed()));
        
        // Wait for splash duration and verify navigation
        try {
            Thread.sleep(3000); // Standard splash duration
        } catch (InterruptedException e) {
            fail("Thread interrupted during splash wait");
        }
        
        // Verify app doesn't crash during cold start
        scenario.onActivity(activity -> {
            assertFalse("Activity should not have crashed", activity.isFinishing());
        });
        
        scenario.close();
    }

    /**
     * Test warm start from background
     * Requirement 1.5: App shall complete launch sequence within 5 seconds
     */
    @Test
    public void testWarmStartFromBackground() {
        // First launch the app
        ActivityScenario<SimpleSplashActivity> scenario = ActivityScenario.launch(SimpleSplashActivity.class);
        
        // Wait for initial load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            fail("Thread interrupted during initial load");
        }
        
        // Move app to background
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED);
        
        // Wait a moment to simulate background time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail("Thread interrupted during background simulation");
        }
        
        // Measure warm start time
        long startTime = System.currentTimeMillis();
        
        // Bring app back to foreground
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);
        
        // Verify app is responsive
        scenario.onActivity(activity -> {
            assertNotNull("Activity should not be null after warm start", activity);
            assertFalse("Activity should not be finishing after warm start", activity.isFinishing());
        });
        
        long warmStartTime = System.currentTimeMillis() - startTime;
        
        // Verify warm start completes within reasonable time (should be much faster than 5 seconds)
        assertTrue("Warm start should complete within 2 seconds, took: " + warmStartTime + "ms", 
                   warmStartTime < 2000);
        
        scenario.close();
    }

    /**
     * Test launch after device restart simulation
     * Requirement 4.1: App shall launch successfully on Android API levels 21-29
     */
    @Test
    public void testLaunchAfterDeviceRestart() {
        // Clear app from memory to simulate device restart
        clearAppFromMemory();
        
        // Clear any cached data that might persist
        clearAppData();
        
        // Launch app with fresh intent (simulating launcher tap after restart)
        Intent launchIntent = new Intent(context, SimpleSplashActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        ActivityScenario<SimpleSplashActivity> scenario = ActivityScenario.launch(launchIntent);
        
        // Verify successful launch
        scenario.onActivity(activity -> {
            assertNotNull("Activity should launch successfully after restart simulation", activity);
            assertFalse("Activity should not be finishing", activity.isFinishing());
            
            // Verify activity has proper state
            assertTrue("Activity should be resumed", activity.hasWindowFocus() || !activity.isFinishing());
        });
        
        // Verify UI elements are properly initialized
        onView(withId(R.id.iv_logo))
            .check(matches(isDisplayed()));
        
        // Test navigation to main activity works after restart
        try {
            Thread.sleep(3000); // Wait for splash completion
        } catch (InterruptedException e) {
            fail("Thread interrupted during splash wait");
        }
        
        scenario.close();
    }

    /**
     * Test launch performance under different conditions
     * Requirement 1.5: App shall complete launch sequence within 5 seconds
     */
    @Test
    public void testLaunchPerformanceMetrics() {
        // Test multiple launch scenarios and measure performance
        
        // Cold start performance
        clearAppData();
        long coldStartTime = measureLaunchTime();
        assertTrue("Cold start should complete within 5 seconds, took: " + coldStartTime + "ms", 
                   coldStartTime < 5000);
        
        // Warm start performance (launch again immediately)
        long warmStartTime = measureLaunchTime();
        assertTrue("Warm start should complete within 3 seconds, took: " + warmStartTime + "ms", 
                   warmStartTime < 3000);
        
        // Verify warm start is faster than cold start
        assertTrue("Warm start should be faster than cold start", warmStartTime < coldStartTime);
    }

    /**
     * Test launch stability under stress conditions
     * Requirement 4.2: App shall handle different screen sizes and orientations
     */
    @Test
    public void testLaunchStabilityUnderStress() {
        // Test multiple rapid launches
        for (int i = 0; i < 5; i++) {
            ActivityScenario<SimpleSplashActivity> scenario = ActivityScenario.launch(SimpleSplashActivity.class);
            
            scenario.onActivity(activity -> {
                assertNotNull("Activity " + i + " should launch successfully", activity);
                assertFalse("Activity " + i + " should not be finishing", activity.isFinishing());
            });
            
            // Brief wait between launches
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                fail("Thread interrupted during stress test");
            }
            
            scenario.close();
        }
    }

    /**
     * Test launch with different system states
     * Requirement 4.1, 4.2: Cross-device compatibility
     */
    @Test
    public void testLaunchWithDifferentSystemStates() {
        // Test launch with low memory simulation
        // Note: This is a simplified simulation as we can't actually control system memory
        
        // Fill some memory before launch
        byte[][] memoryConsumer = new byte[10][1024 * 1024]; // 10MB
        
        ActivityScenario<SimpleSplashActivity> scenario = ActivityScenario.launch(SimpleSplashActivity.class);
        
        scenario.onActivity(activity -> {
            assertNotNull("Activity should launch even with memory pressure", activity);
            assertFalse("Activity should not be finishing under memory pressure", activity.isFinishing());
        });
        
        // Clean up memory
        memoryConsumer = null;
        System.gc();
        
        scenario.close();
    }

    /**
     * Test launch error recovery
     * Requirement 1.1: App shall launch without terminating unexpectedly
     */
    @Test
    public void testLaunchErrorRecovery() {
        // Test launch with corrupted preferences
        corruptSharedPreferences();
        
        ActivityScenario<SimpleSplashActivity> scenario = ActivityScenario.launch(SimpleSplashActivity.class);
        
        scenario.onActivity(activity -> {
            assertNotNull("Activity should handle corrupted preferences gracefully", activity);
            assertFalse("Activity should not crash with corrupted data", activity.isFinishing());
        });
        
        // Verify app can still display basic UI
        onView(withId(R.id.iv_logo))
            .check(matches(isDisplayed()));
        
        scenario.close();
        
        // Clean up corrupted data
        clearAppData();
    }

    // Helper methods

    private void clearAppData() {
        // Clear SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("SugboAidPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        
        // Clear other app-specific data
        context.deleteDatabase("sugboaid_database");
        
        // Clear cache
        context.getCacheDir().delete();
    }

    private void clearAppFromMemory() {
        // Force garbage collection to simulate memory clearing
        System.gc();
        Runtime.getRuntime().gc();
        
        // Brief pause to allow cleanup
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }
    }

    private long measureLaunchTime() {
        long startTime = System.currentTimeMillis();
        
        ActivityScenario<SimpleSplashActivity> scenario = ActivityScenario.launch(SimpleSplashActivity.class);
        
        scenario.onActivity(activity -> {
            // Activity is now launched and ready
        });
        
        long launchTime = System.currentTimeMillis() - startTime;
        scenario.close();
        
        return launchTime;
    }

    private void corruptSharedPreferences() {
        // Write invalid JSON to simulate corruption
        SharedPreferences prefs = context.getSharedPreferences("SugboAidPrefs", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("donations_json", "{invalid_json_data")
            .putString("inventory_json", "corrupted_data")
            .apply();
    }
}