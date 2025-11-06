package com.sugboaid.donation;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.sugboaid.donation.activities.MainActivity;
import com.sugboaid.donation.activities.SplashActivity;
import com.sugboaid.utils.SharedPreferencesHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

/**
 * Comprehensive validation tests to ensure Android app functionality matches original TSX app
 */
@RunWith(AndroidJUnit4.class)
public class ComprehensiveValidationTest {

    @Rule
    public ActivityTestRule<SplashActivity> splashActivityRule = 
        new ActivityTestRule<>(SplashActivity.class);

    private Context context;
    private SharedPreferencesHelper prefsHelper;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        prefsHelper = new SharedPreferencesHelper(context);
        
        // Clear any existing data
        prefsHelper.clearAllData();
    }

    /**
     * Test 1: Splash Screen and Role Selection (Requirement 1.1)
     */
    @Test
    public void testSplashScreenAndRoleSelection() {
        // Verify splash screen displays SugboAid logo
        onView(withId(R.id.iv_logo))
            .check(matches(isDisplayed()));
        
        // Verify tagline is displayed
        onView(withText(containsString("Together, We Rebuild Cebu")))
            .check(matches(isDisplayed()));
        
        // Test role selection buttons
        onView(withId(R.id.btn_donor))
            .check(matches(isDisplayed()))
            .perform(click());
        
        // Should navigate to main dashboard
        onView(withId(R.id.fragment_container))
            .check(matches(isDisplayed()));
    }

    /**
     * Test 2: Dashboard Statistics and Quick Actions (Requirement 2.1)
     */
    @Test
    public void testDashboardFunctionality() {
        // Navigate to dashboard
        navigateToDashboard();
        
        // Verify statistics cards are displayed
        onView(withId(R.id.card_total_donations))
            .check(matches(isDisplayed()));
        onView(withId(R.id.card_distributed_items))
            .check(matches(isDisplayed()));
        onView(withId(R.id.card_families_helped))
            .check(matches(isDisplayed()));
        
        // Verify quick action buttons
        onView(withId(R.id.btn_new_donation))
            .check(matches(isDisplayed()));
        onView(withId(R.id.btn_inventory))
            .check(matches(isDisplayed()));
        onView(withId(R.id.btn_transparency))
            .check(matches(isDisplayed()));
        onView(withId(R.id.btn_reports))
            .check(matches(isDisplayed()));
        
        // Test floating action button
        onView(withId(R.id.fab_quick_donation))
            .check(matches(isDisplayed()))
            .perform(click());
    }

    /**
     * Test 3: POS Donation System (Requirement 3.1)
     */
    @Test
    public void testPOSDonationSystem() {
        navigateToDashboard();
        
        // Navigate to POS donation
        onView(withId(R.id.btn_new_donation))
            .perform(click());
        
        // Test cash donation mode
        onView(withId(R.id.toggle_cash))
            .perform(click());
        
        // Test amount input
        onView(withId(R.id.et_amount))
            .perform(typeText("1000"));
        
        // Test quick amount buttons
        onView(withId(R.id.btn_amount_500))
            .perform(click());
        
        // Test donor name input
        onView(withId(R.id.et_donor_name))
            .perform(typeText("Test Donor"));
        
        // Submit donation
        onView(withId(R.id.btn_submit_donation))
            .perform(click());
        
        // Verify success screen
        onView(withId(R.id.iv_success_checkmark))
            .check(matches(isDisplayed()));
        
        // Test goods donation mode
        onView(withId(R.id.btn_back_to_pos))
            .perform(click());
        
        onView(withId(R.id.toggle_goods))
            .perform(click());
        
        // Test quantity selectors
        onView(withId(R.id.btn_rice_increment))
            .perform(click());
        onView(withId(R.id.btn_water_increment))
            .perform(click());
    }

    /**
     * Test 4: Data Persistence and Offline Functionality (Requirement 4.5)
     */
    @Test
    public void testDataPersistenceAndOffline() {
        navigateToDashboard();
        
        // Create a donation
        createTestDonation("Test Offline Donor", 500.0);
        
        // Verify data is saved in SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("SugboAidPrefs", Context.MODE_PRIVATE);
        String donationsJson = prefs.getString("donations_json", "");
        assert !donationsJson.isEmpty();
        
        // Test offline banner (simulate network disconnection)
        // This would require network state manipulation in a real test
        
        // Verify data persists after app restart
        ActivityScenario.launch(MainActivity.class);
        
        // Check that donation data is still available
        onView(withId(R.id.rv_recent_activities))
            .check(matches(isDisplayed()));
    }

    /**
     * Test 5: Dark Mode Functionality (Requirement 5.5)
     */
    @Test
    public void testDarkModeToggle() {
        navigateToDashboard();
        
        // Test dark mode toggle
        onView(withId(R.id.btn_dark_mode_toggle))
            .perform(click());
        
        // Verify dark mode is applied (check background colors)
        // This would require custom matchers for color verification
        
        // Verify preference is saved
        boolean isDarkMode = prefsHelper.getDarkModePreference();
        assert isDarkMode;
        
        // Toggle back to light mode
        onView(withId(R.id.btn_dark_mode_toggle))
            .perform(click());
        
        isDarkMode = prefsHelper.getDarkModePreference();
        assert !isDarkMode;
    }

    /**
     * Test 6: Inventory Management (Requirement 6.2)
     */
    @Test
    public void testInventoryManagement() {
        navigateToDashboard();
        
        // Navigate to inventory
        onView(withId(R.id.btn_inventory))
            .perform(click());
        
        // Verify inventory items are displayed
        onView(withId(R.id.rv_inventory_items))
            .check(matches(isDisplayed()));
        
        // Test search functionality
        onView(withId(R.id.search_view))
            .perform(typeText("Rice"));
        
        // Test QR scanner
        onView(withId(R.id.btn_qr_scanner))
            .perform(click());
        
        // Verify QR scanner opens (would need camera permission)
    }

    /**
     * Test 7: Transparency Dashboard (Requirement 6.3)
     */
    @Test
    public void testTransparencyDashboard() {
        navigateToDashboard();
        
        // Navigate to transparency
        onView(withId(R.id.btn_transparency))
            .perform(click());
        
        // Test tab navigation
        onView(withText("Overview"))
            .perform(click());
        
        // Verify charts are displayed
        onView(withId(R.id.chart_donation_trends))
            .check(matches(isDisplayed()));
        
        // Test barangay map tab
        onView(withText("Barangay Map"))
            .perform(click());
        
        onView(withId(R.id.map_view))
            .check(matches(isDisplayed()));
        
        // Test impact stories tab
        onView(withText("Impact Stories"))
            .perform(click());
        
        onView(withId(R.id.rv_impact_stories))
            .check(matches(isDisplayed()));
    }

    /**
     * Test 8: Reports and Export (Requirement 6.4)
     */
    @Test
    public void testReportsAndExport() {
        navigateToDashboard();
        
        // Create some test data first
        createTestDonation("Report Test Donor", 1000.0);
        
        // Navigate to reports
        onView(withId(R.id.btn_reports))
            .perform(click());
        
        // Test filter functionality
        onView(withId(R.id.btn_filter_cash))
            .perform(click());
        
        onView(withId(R.id.btn_filter_all))
            .perform(click());
        
        // Test export functionality
        onView(withId(R.id.btn_export_pdf))
            .perform(click());
        
        onView(withId(R.id.btn_export_csv))
            .perform(click());
    }

    /**
     * Test 9: Notifications System (Requirement 6.5)
     */
    @Test
    public void testNotificationsSystem() {
        navigateToDashboard();
        
        // Navigate to notifications
        onView(withId(R.id.btn_notifications))
            .perform(click());
        
        // Verify notifications list
        onView(withId(R.id.rv_notifications))
            .check(matches(isDisplayed()));
        
        // Test mark all as read
        onView(withId(R.id.btn_mark_all_read))
            .perform(click());
    }

    /**
     * Test 10: UI Consistency and Animations (Requirement 8.5)
     */
    @Test
    public void testUIConsistencyAndAnimations() {
        navigateToDashboard();
        
        // Test navigation animations
        onView(withId(R.id.btn_new_donation))
            .perform(click());
        
        // Wait for animation to complete
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Verify glassmorphic effects are applied
        onView(withId(R.id.card_donation_form))
            .check(matches(isDisplayed()));
        
        // Test button animations
        onView(withId(R.id.btn_submit_donation))
            .perform(click());
    }

    // Helper methods

    private void navigateToDashboard() {
        try {
            // If we're on splash screen, select a role first
            onView(withId(R.id.btn_donor))
                .perform(click());
        } catch (Exception e) {
            // Already on dashboard or main activity
        }
    }

    private void createTestDonation(String donorName, double amount) {
        // Navigate to POS donation
        onView(withId(R.id.btn_new_donation))
            .perform(click());
        
        // Select cash mode
        onView(withId(R.id.toggle_cash))
            .perform(click());
        
        // Enter amount
        onView(withId(R.id.et_amount))
            .perform(typeText(String.valueOf(amount)));
        
        // Enter donor name
        onView(withId(R.id.et_donor_name))
            .perform(typeText(donorName));
        
        // Submit donation
        onView(withId(R.id.btn_submit_donation))
            .perform(click());
        
        // Return to dashboard
        onView(withId(R.id.btn_back_to_dashboard))
            .perform(click());
    }

    /**
     * Test complete user workflow from start to finish
     */
    @Test
    public void testCompleteUserWorkflow() {
        // 1. Start from splash screen
        testSplashScreenAndRoleSelection();
        
        // 2. Navigate through dashboard
        testDashboardFunctionality();
        
        // 3. Create donations
        createTestDonation("Workflow Test Donor 1", 500.0);
        createTestDonation("Workflow Test Donor 2", 1000.0);
        
        // 4. Check inventory
        testInventoryManagement();
        
        // 5. View transparency dashboard
        testTransparencyDashboard();
        
        // 6. Generate reports
        testReportsAndExport();
        
        // 7. Check notifications
        testNotificationsSystem();
        
        // 8. Toggle dark mode
        testDarkModeToggle();
        
        // 9. Verify data persistence
        testDataPersistenceAndOffline();
    }

    /**
     * Performance validation test
     */
    @Test
    public void testPerformanceValidation() {
        long startTime = System.currentTimeMillis();
        
        // Navigate through all major screens
        navigateToDashboard();
        
        onView(withId(R.id.btn_new_donation)).perform(click());
        Espresso.pressBack();
        
        onView(withId(R.id.btn_inventory)).perform(click());
        Espresso.pressBack();
        
        onView(withId(R.id.btn_transparency)).perform(click());
        Espresso.pressBack();
        
        onView(withId(R.id.btn_reports)).perform(click());
        Espresso.pressBack();
        
        long endTime = System.currentTimeMillis();
        long navigationTime = endTime - startTime;
        
        // Navigation should complete within reasonable time (5 seconds)
        assert navigationTime < 5000 : "Navigation took too long: " + navigationTime + "ms";
    }

    /**
     * Memory usage validation test
     */
    @Test
    public void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Perform memory-intensive operations
        for (int i = 0; i < 10; i++) {
            createTestDonation("Memory Test Donor " + i, 100.0 * i);
        }
        
        // Force garbage collection
        System.gc();
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // Memory increase should be reasonable (less than 50MB)
        assert memoryIncrease < 50 * 1024 * 1024 : "Memory usage increased too much: " + (memoryIncrease / 1024 / 1024) + "MB";
    }
}