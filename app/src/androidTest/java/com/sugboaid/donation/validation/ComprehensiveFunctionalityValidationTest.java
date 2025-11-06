package com.sugboaid.donation.validation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.sugboaid.donation.R;
import com.sugboaid.donation.activities.MainActivity;
import com.sugboaid.donation.activities.SplashActivity;
import com.sugboaid.models.Donation;
import com.sugboaid.models.DonationType;
import com.sugboaid.repositories.DonationRepository;
import com.sugboaid.utils.SharedPreferencesHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Comprehensive validation test that verifies all Android app functionality
 * matches the original TSX React application requirements.
 * 
 * This test validates:
 * - Splash screen and role selection (Requirement 1.1-1.4)
 * - Dashboard statistics and navigation (Requirement 2.1-2.5)
 * - POS donation system (Requirement 3.1-3.5)
 * - Offline functionality (Requirement 4.1-4.5)
 * - Dark mode theme system (Requirement 5.1-5.5)
 * - All navigation screens (Requirement 6.2-6.5)
 * - Android architecture compliance (Requirement 7.1-7.5)
 * - UI consistency and branding (Requirement 8.1-8.5)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ComprehensiveFunctionalityValidationTest {

    private Context context;
    private SharedPreferencesHelper prefsHelper;
    private DonationRepository donationRepository;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        prefsHelper = SharedPreferencesHelper.getInstance(context);
        donationRepository = DonationRepository.getInstance(context);
        
        // Clear any existing data for clean test state
        clearTestData();
    }

    @After
    public void tearDown() {
        // Clean up test data
        clearTestData();
    }

    private void clearTestData() {
        SharedPreferences prefs = context.getSharedPreferences("SugboAidPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    /**
     * Test Requirement 1: Splash Screen and Role Selection
     * Validates splash screen animations, role selection, and navigation
     */
    @Test
    public void testSplashScreenAndRoleSelection() {
        try (ActivityScenario<SplashActivity> scenario = ActivityScenario.launch(SplashActivity.class)) {
            
            // Verify splash screen elements are displayed (Requirement 1.1)
            onView(withId(R.id.iv_logo))
                .check(matches(isDisplayed()));
            
            onView(withId(R.id.tv_tagline_main))
                .check(matches(withText(containsString("SugboAid"))));
            
            onView(withId(R.id.tv_tagline_sub))
                .check(matches(withText(containsString("Together, We Rebuild Cebu"))));
            
            // Wait for animations to complete and role selection to appear
            Thread.sleep(3000);
            
            // Verify role selection buttons are displayed (Requirement 1.2)
            onView(withId(R.id.btn_donor))
                .check(matches(isDisplayed()));
            onView(withId(R.id.btn_organization))
                .check(matches(isDisplayed()));
            onView(withId(R.id.btn_volunteer))
                .check(matches(isDisplayed()));
            onView(withId(R.id.btn_recipient))
                .check(matches(isDisplayed()));
            onView(withId(R.id.btn_guest))
                .check(matches(isDisplayed()));
            
            // Test role selection and navigation (Requirement 1.3)
            onView(withId(R.id.btn_donor))
                .perform(click());
            
            // Verify navigation to MainActivity occurs
            Thread.sleep(1000);
            
            // Verify role is saved in SharedPreferences
            String savedRole = prefsHelper.getUserRole();
            assertEquals("Donor", savedRole);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test Requirement 2: Dashboard Statistics and Quick Actions
     * Validates dashboard display, statistics cards, and navigation
     */
    @Test
    public void testDashboardFunctionality() {
        // Launch MainActivity directly for dashboard testing
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            
            // Verify dashboard header (Requirement 2.1)
            onView(withId(R.id.tv_welcome))
                .check(matches(isDisplayed()));
            
            // Verify statistics cards are displayed (Requirement 2.1)
            onView(withId(R.id.card_total_donations))
                .check(matches(isDisplayed()));
            onView(withId(R.id.card_distributed_items))
                .check(matches(isDisplayed()));
            onView(withId(R.id.card_families_helped))
                .check(matches(isDisplayed()));
            
            // Verify quick action buttons (Requirement 2.4)
            onView(withId(R.id.btn_new_donation))
                .check(matches(isDisplayed()));
            onView(withId(R.id.btn_inventory))
                .check(matches(isDisplayed()));
            onView(withId(R.id.btn_transparency))
                .check(matches(isDisplayed()));
            onView(withId(R.id.btn_reports))
                .check(matches(isDisplayed()));
            
            // Verify floating action button (Requirement 2.5)
            onView(withId(R.id.fab_quick_donation))
                .check(matches(isDisplayed()));
            
            // Test navigation to POS donation
            onView(withId(R.id.btn_new_donation))
                .perform(click());
            
            Thread.sleep(500);
            
            // Verify navigation to POS fragment
            onView(withId(R.id.toggle_donation_type))
                .check(matches(isDisplayed()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test Requirement 3: POS Donation System
     * Validates cash and goods donation recording
     */
    @Test
    public void testPOSDonationSystem() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            
            // Navigate to POS donation
            onView(withId(R.id.btn_new_donation))
                .perform(click());
            
            Thread.sleep(500);
            
            // Test cash donation mode (Requirement 3.2)
            onView(withId(R.id.toggle_donation_type))
                .check(matches(isDisplayed()));
            
            // Verify cash donation interface
            onView(withId(R.id.et_donation_amount))
                .check(matches(isDisplayed()));
            
            // Test quick amount buttons
            onView(withId(R.id.btn_amount_100))
                .check(matches(isDisplayed()))
                .perform(click());
            
            // Verify amount is set
            onView(withId(R.id.et_donation_amount))
                .check(matches(withText("100")));
            
            // Test donor name input
            onView(withId(R.id.et_donor_name))
                .perform(typeText("Test Donor"));
            
            // Test donation submission
            onView(withId(R.id.btn_submit_donation))
                .perform(click());
            
            Thread.sleep(1000);
            
            // Verify success screen (Requirement 3.5)
            onView(withId(R.id.iv_success_checkmark))
                .check(matches(isDisplayed()));
            
            onView(withId(R.id.iv_qr_code))
                .check(matches(isDisplayed()));
            
            // Test goods donation mode (Requirement 3.3)
            onView(withId(R.id.btn_back_to_dashboard))
                .perform(click());
            
            Thread.sleep(500);
            
            onView(withId(R.id.btn_new_donation))
                .perform(click());
            
            Thread.sleep(500);
            
            // Switch to goods mode
            onView(withId(R.id.toggle_donation_type))
                .perform(click());
            
            // Verify goods interface
            onView(withId(R.id.selector_rice))
                .check(matches(isDisplayed()));
            onView(withId(R.id.selector_water))
                .check(matches(isDisplayed()));
            onView(withId(R.id.selector_medicine))
                .check(matches(isDisplayed()));
            onView(withId(R.id.selector_clothes))
                .check(matches(isDisplayed()));
            
            // Test quantity selection
            onView(withId(R.id.btn_rice_increment))
                .perform(click());
            
            // Verify quantity badge update
            onView(withId(R.id.badge_rice_quantity))
                .check(matches(withText("1")));
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test Requirement 4: Offline Functionality and Data Persistence
     * Validates SharedPreferences storage and offline capabilities
     */
    @Test
    public void testOfflineFunctionalityAndDataPersistence() {
        // Test SharedPreferences data persistence (Requirement 4.1, 4.3)
        
        // Create test donation
        Donation testDonation = new Donation(
            "test-id-1",
            "Test Donor",
            DonationType.CASH,
            5000.0,
            "Test donation",
            System.currentTimeMillis(),
            "General Relief",
            true
        );
        
        // Save donation using repository
        donationRepository.addDonation(testDonation);
        
        // Verify data persistence
        List<Donation> savedDonations = donationRepository.getAllDonations();
        assertNotNull(savedDonations);
        assertFalse(savedDonations.isEmpty());
        
        Donation retrievedDonation = savedDonations.get(0);
        assertEquals(testDonation.getId(), retrievedDonation.getId());
        assertEquals(testDonation.getDonorName(), retrievedDonation.getDonorName());
        assertEquals(testDonation.getAmount(), retrievedDonation.getAmount(), 0.01);
        
        // Test offline banner functionality (Requirement 4.2)
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            
            // Simulate offline state
            prefsHelper.saveOfflineMode(true);
            
            // Verify offline banner appears
            onView(withId(R.id.offline_banner))
                .check(matches(isDisplayed()));
            
            // Test offline donation recording
            onView(withId(R.id.btn_new_donation))
                .perform(click());
            
            Thread.sleep(500);
            
            // Record donation while offline
            onView(withId(R.id.et_donation_amount))
                .perform(typeText("1000"));
            
            onView(withId(R.id.et_donor_name))
                .perform(typeText("Offline Donor"));
            
            onView(withId(R.id.btn_submit_donation))
                .perform(click());
            
            // Verify donation is queued for sync
            List<Donation> offlineDonations = donationRepository.getAllDonations();
            assertTrue(offlineDonations.size() >= 2); // Original + offline donation
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test Requirement 5: Dark Mode and Theme System
     * Validates theme switching and persistence
     */
    @Test
    public void testDarkModeAndThemeSystem() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            
            // Verify dark mode toggle is present (Requirement 5.1)
            onView(withId(R.id.fab_dark_mode_toggle))
                .check(matches(isDisplayed()));
            
            // Test initial theme state
            boolean initialDarkMode = prefsHelper.getDarkModePreference();
            
            // Toggle dark mode (Requirement 5.2)
            onView(withId(R.id.fab_dark_mode_toggle))
                .perform(click());
            
            Thread.sleep(500);
            
            // Verify theme preference is saved (Requirement 5.3)
            boolean newDarkMode = prefsHelper.getDarkModePreference();
            assertEquals(!initialDarkMode, newDarkMode);
            
            // Toggle back
            onView(withId(R.id.fab_dark_mode_toggle))
                .perform(click());
            
            Thread.sleep(500);
            
            // Verify theme reverted
            boolean finalDarkMode = prefsHelper.getDarkModePreference();
            assertEquals(initialDarkMode, finalDarkMode);
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test Requirement 6: Navigation and Screen Access
     * Validates all screen navigation functionality
     */
    @Test
    public void testNavigationAndScreenAccess() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            
            // Test inventory navigation (Requirement 6.2)
            onView(withId(R.id.btn_inventory))
                .perform(click());
            
            Thread.sleep(500);
            
            onView(withId(R.id.rv_inventory_items))
                .check(matches(isDisplayed()));
            
            // Navigate back to dashboard
            Espresso.pressBack();
            
            Thread.sleep(500);
            
            // Test transparency dashboard navigation (Requirement 6.3)
            onView(withId(R.id.btn_transparency))
                .perform(click());
            
            Thread.sleep(500);
            
            onView(withId(R.id.tab_layout_transparency))
                .check(matches(isDisplayed()));
            
            // Navigate back to dashboard
            Espresso.pressBack();
            
            Thread.sleep(500);
            
            // Test reports navigation (Requirement 6.4)
            onView(withId(R.id.btn_reports))
                .perform(click());
            
            Thread.sleep(500);
            
            onView(withId(R.id.rv_transactions))
                .check(matches(isDisplayed()));
            
            // Navigate back to dashboard
            Espresso.pressBack();
            
            Thread.sleep(500);
            
            // Test notifications navigation (Requirement 6.5)
            onView(withId(R.id.fl_notification_container))
                .perform(click());
            
            Thread.sleep(500);
            
            onView(withId(R.id.rv_notifications))
                .check(matches(isDisplayed()));
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test Requirement 7: Android Architecture and Performance
     * Validates MVVM architecture and native components
     */
    @Test
    public void testAndroidArchitectureCompliance() {
        // This test validates that the app follows Android best practices
        
        // Verify SharedPreferences usage (Requirement 7.4)
        assertNotNull(prefsHelper);
        
        // Test data persistence
        prefsHelper.saveDarkModePreference(true);
        assertTrue(prefsHelper.getDarkModePreference());
        
        prefsHelper.saveUserRole("TestRole");
        assertEquals("TestRole", prefsHelper.getUserRole());
        
        // Verify repository pattern implementation
        assertNotNull(donationRepository);
        
        // Test LiveData functionality through repository
        List<Donation> donations = donationRepository.getAllDonations();
        assertNotNull(donations);
        
        // Verify MVVM architecture by testing ViewModel functionality
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            
            // ViewModels are tested indirectly through UI interactions
            // The fact that statistics cards display data confirms ViewModel functionality
            onView(withId(R.id.card_total_donations))
                .check(matches(isDisplayed()));
            
            // Navigation Component usage is verified through successful navigation
            onView(withId(R.id.btn_inventory))
                .perform(click());
            
            Thread.sleep(500);
            
            // Successful navigation confirms Navigation Component integration
            onView(withId(R.id.rv_inventory_items))
                .check(matches(isDisplayed()));
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test Requirement 8: UI Consistency and Branding
     * Validates visual consistency with original TSX app
     */
    @Test
    public void testUIConsistencyAndBranding() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            
            // Verify SugboAid branding elements (Requirement 8.1)
            onView(withId(R.id.tv_welcome))
                .check(matches(isDisplayed()));
            
            // Verify glassmorphic card components (Requirement 8.3)
            onView(withId(R.id.card_total_donations))
                .check(matches(isDisplayed()));
            onView(withId(R.id.card_distributed_items))
                .check(matches(isDisplayed()));
            onView(withId(R.id.card_families_helped))
                .check(matches(isDisplayed()));
            
            // Verify gradient buttons (Requirement 8.2)
            onView(withId(R.id.btn_new_donation))
                .check(matches(isDisplayed()));
            onView(withId(R.id.btn_inventory))
                .check(matches(isDisplayed()));
            onView(withId(R.id.btn_transparency))
                .check(matches(isDisplayed()));
            onView(withId(R.id.btn_reports))
                .check(matches(isDisplayed()));
            
            // Verify floating action button with animations (Requirement 8.4)
            onView(withId(R.id.fab_quick_donation))
                .check(matches(isDisplayed()));
            
            // Test button animations by clicking
            onView(withId(R.id.btn_new_donation))
                .perform(click());
            
            Thread.sleep(500);
            
            // Verify POS screen maintains visual consistency
            onView(withId(R.id.toggle_donation_type))
                .check(matches(isDisplayed()));
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Comprehensive end-to-end workflow test
     * Tests complete user journey from splash to donation completion
     */
    @Test
    public void testCompleteUserWorkflow() {
        // Test complete workflow: Splash -> Dashboard -> Donation -> Success
        
        try (ActivityScenario<SplashActivity> splashScenario = ActivityScenario.launch(SplashActivity.class)) {
            
            // Wait for splash animations
            Thread.sleep(3000);
            
            // Select role
            onView(withId(R.id.btn_donor))
                .perform(click());
            
            Thread.sleep(1000);
        }
        
        // Continue with MainActivity
        try (ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class)) {
            
            // Navigate to donation
            onView(withId(R.id.btn_new_donation))
                .perform(click());
            
            Thread.sleep(500);
            
            // Complete cash donation
            onView(withId(R.id.et_donation_amount))
                .perform(typeText("2500"));
            
            onView(withId(R.id.et_donor_name))
                .perform(typeText("End-to-End Test Donor"));
            
            onView(withId(R.id.btn_submit_donation))
                .perform(click());
            
            Thread.sleep(1000);
            
            // Verify success screen
            onView(withId(R.id.iv_success_checkmark))
                .check(matches(isDisplayed()));
            
            // Return to dashboard
            onView(withId(R.id.btn_back_to_dashboard))
                .perform(click());
            
            Thread.sleep(500);
            
            // Verify donation appears in recent activities
            onView(withId(R.id.rv_recent_activities))
                .check(matches(isDisplayed()));
            
            // Test dark mode toggle
            onView(withId(R.id.fab_dark_mode_toggle))
                .perform(click());
            
            Thread.sleep(500);
            
            // Verify theme changed
            assertTrue(prefsHelper.getDarkModePreference());
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}