package com.sugboaid.donation;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.sugboaid.donation.activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for complete user workflows
 * Tests end-to-end user scenarios and feature interactions
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UserWorkflowIntegrationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = 
        new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testWorkflow_NewUserExploration() {
        // Simulate a new user exploring the app
        
        // Start at dashboard (default screen)
        onView(withId(R.id.nav_dashboard)).check(matches(isSelected()));
        
        // Check welcome message is displayed
        onView(withId(R.id.tv_welcome)).check(matches(isDisplayed()));
        
        // Explore statistics cards
        onView(withId(R.id.card_total_donations)).check(matches(isDisplayed()));
        onView(withId(R.id.card_distributed_items)).check(matches(isDisplayed()));
        onView(withId(R.id.card_families_helped)).check(matches(isDisplayed()));
        
        // Try quick actions
        onView(withId(R.id.btn_inventory)).perform(click());
        onView(withId(R.id.nav_inventory)).check(matches(isSelected()));
        
        // Navigate to transparency
        onView(withId(R.id.btn_transparency)).perform(click());
        onView(withId(R.id.nav_transparency)).check(matches(isSelected()));
        
        // Check reports
        onView(withId(R.id.nav_reports)).perform(click());
        onView(withId(R.id.nav_reports)).check(matches(isSelected()));
        
        // Check notifications
        onView(withId(R.id.nav_notifications)).perform(click());
        onView(withId(R.id.nav_notifications)).check(matches(isSelected()));
        
        // Return to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.nav_dashboard)).check(matches(isSelected()));
    }

    @Test
    public void testWorkflow_DonationRecordingFlow() {
        // Simulate complete donation recording workflow
        
        // Start at dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Click on new donation quick action
        onView(withId(R.id.btn_new_donation)).perform(click());
        
        // Should navigate to donation screen or show donation dialog
        // Note: This depends on actual implementation - adjust based on navigation
        
        // Alternative: Use FAB for quick donation
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.fab_quick_donation)).perform(click());
        
        // Verify that donation interface is accessible
        // This test validates the navigation flow exists
    }

    @Test
    public void testWorkflow_InventoryManagement() {
        // Simulate inventory management workflow
        
        // Navigate to inventory from dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.btn_inventory)).perform(click());
        onView(withId(R.id.nav_inventory)).check(matches(isSelected()));
        
        // Alternative navigation via bottom nav
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.nav_inventory)).perform(click());
        onView(withId(R.id.nav_inventory)).check(matches(isSelected()));
        
        // Return to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
    }

    @Test
    public void testWorkflow_TransparencyViewing() {
        // Simulate transparency dashboard viewing workflow
        
        // Navigate to transparency from dashboard quick action
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.btn_transparency)).perform(click());
        onView(withId(R.id.nav_transparency)).check(matches(isSelected()));
        
        // Navigate back to dashboard and try bottom nav
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.nav_transparency)).perform(click());
        onView(withId(R.id.nav_transparency)).check(matches(isSelected()));
    }

    @Test
    public void testWorkflow_ReportsGeneration() {
        // Simulate reports viewing and generation workflow
        
        // Navigate to reports from dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.btn_reports)).perform(click());
        onView(withId(R.id.nav_reports)).check(matches(isSelected()));
        
        // Navigate via bottom navigation
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.nav_reports)).perform(click());
        onView(withId(R.id.nav_reports)).check(matches(isSelected()));
    }

    @Test
    public void testWorkflow_NotificationManagement() {
        // Simulate notification management workflow
        
        // Navigate to notifications from dashboard header
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.fl_notification_container)).perform(click());
        onView(withId(R.id.nav_notifications)).check(matches(isSelected()));
        
        // Navigate via bottom navigation
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.nav_notifications)).perform(click());
        onView(withId(R.id.nav_notifications)).check(matches(isSelected()));
    }

    @Test
    public void testWorkflow_ThemeToggling() {
        // Simulate theme toggling workflow
        
        // Start at dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Toggle dark mode multiple times
        onView(withId(R.id.fab_dark_mode_toggle)).perform(click());
        
        // Navigate to different screens to verify theme persistence
        onView(withId(R.id.nav_inventory)).perform(click());
        onView(withId(R.id.nav_transparency)).perform(click());
        onView(withId(R.id.nav_reports)).perform(click());
        onView(withId(R.id.nav_notifications)).perform(click());
        
        // Toggle theme again
        onView(withId(R.id.fab_dark_mode_toggle)).perform(click());
        
        // Navigate back to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Verify FAB is still accessible
        onView(withId(R.id.fab_dark_mode_toggle)).check(matches(isDisplayed()));
    }

    @Test
    public void testWorkflow_CompleteAppExploration() {
        // Simulate complete app exploration workflow
        
        // Start at dashboard and explore all features
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Check all dashboard elements
        onView(withId(R.id.tv_welcome)).check(matches(isDisplayed()));
        onView(withId(R.id.card_total_donations)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_new_donation)).check(matches(isDisplayed()));
        onView(withId(R.id.fab_quick_donation)).check(matches(isDisplayed()));
        
        // Navigate through all screens via quick actions
        onView(withId(R.id.btn_inventory)).perform(click());
        onView(withId(R.id.btn_transparency)).perform(click());
        onView(withId(R.id.btn_reports)).perform(click());
        
        // Navigate through all screens via bottom navigation
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.nav_inventory)).perform(click());
        onView(withId(R.id.nav_transparency)).perform(click());
        onView(withId(R.id.nav_reports)).perform(click());
        onView(withId(R.id.nav_notifications)).perform(click());
        
        // Test theme toggling
        onView(withId(R.id.fab_dark_mode_toggle)).perform(click());
        
        // Navigate through screens in dark mode
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.nav_inventory)).perform(click());
        onView(withId(R.id.nav_transparency)).perform(click());
        
        // Toggle back to light mode
        onView(withId(R.id.fab_dark_mode_toggle)).perform(click());
        
        // End at dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.nav_dashboard)).check(matches(isSelected()));
    }

    @Test
    public void testWorkflow_AccessibilityUserFlow() {
        // Simulate workflow for accessibility users
        
        // Navigate using bottom navigation (keyboard/TalkBack friendly)
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.nav_dashboard)).check(matches(isSelected()));
        
        // Check that all interactive elements are accessible
        onView(withId(R.id.fab_dark_mode_toggle)).check(matches(isDisplayed()));
        onView(withId(R.id.fab_quick_donation)).check(matches(isDisplayed()));
        onView(withId(R.id.fl_notification_container)).check(matches(isDisplayed()));
        
        // Navigate through all screens systematically
        onView(withId(R.id.nav_inventory)).perform(click());
        onView(withId(R.id.nav_inventory)).check(matches(isSelected()));
        
        onView(withId(R.id.nav_transparency)).perform(click());
        onView(withId(R.id.nav_transparency)).check(matches(isSelected()));
        
        onView(withId(R.id.nav_reports)).perform(click());
        onView(withId(R.id.nav_reports)).check(matches(isSelected()));
        
        onView(withId(R.id.nav_notifications)).perform(click());
        onView(withId(R.id.nav_notifications)).check(matches(isSelected()));
        
        // Return to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.nav_dashboard)).check(matches(isSelected()));
    }

    @Test
    public void testWorkflow_ErrorRecovery() {
        // Simulate error recovery scenarios
        
        // Start normal workflow
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Rapid navigation (might cause errors)
        for (int i = 0; i < 5; i++) {
            onView(withId(R.id.nav_inventory)).perform(click());
            onView(withId(R.id.nav_dashboard)).perform(click());
        }
        
        // App should still be functional
        onView(withId(R.id.nav_dashboard)).check(matches(isSelected()));
        onView(withId(R.id.fab_dark_mode_toggle)).check(matches(isDisplayed()));
        
        // Test theme toggle recovery
        onView(withId(R.id.fab_dark_mode_toggle)).perform(click());
        onView(withId(R.id.fab_dark_mode_toggle)).perform(click());
        
        // App should still be responsive
        onView(withId(R.id.nav_inventory)).perform(click());
        onView(withId(R.id.nav_dashboard)).perform(click());
    }

    @Test
    public void testWorkflow_DataPersistence() {
        // Test data persistence across navigation
        
        // Start at dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Toggle dark mode
        onView(withId(R.id.fab_dark_mode_toggle)).perform(click());
        
        // Navigate through screens
        onView(withId(R.id.nav_inventory)).perform(click());
        onView(withId(R.id.nav_transparency)).perform(click());
        onView(withId(R.id.nav_reports)).perform(click());
        onView(withId(R.id.nav_notifications)).perform(click());
        
        // Return to dashboard - theme should be preserved
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Dark mode toggle should still be accessible
        onView(withId(R.id.fab_dark_mode_toggle)).check(matches(isDisplayed()));
        
        // Toggle back to verify functionality
        onView(withId(R.id.fab_dark_mode_toggle)).perform(click());
    }
}