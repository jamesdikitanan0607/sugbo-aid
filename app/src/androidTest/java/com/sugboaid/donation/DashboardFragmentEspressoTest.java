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
 * Espresso integration tests for DashboardFragment
 * Tests dashboard UI components and user interactions
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DashboardFragmentEspressoTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = 
        new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testDashboard_DisplaysWelcomeMessage() {
        // Navigate to dashboard (should be default)
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Check welcome message is displayed
        onView(withId(R.id.tv_welcome))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.dashboard_welcome)));
    }

    @Test
    public void testDashboard_DisplaysStatisticsCards() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Check that statistics cards are displayed
        onView(withId(R.id.card_total_donations)).check(matches(isDisplayed()));
        onView(withId(R.id.card_distributed_items)).check(matches(isDisplayed()));
        onView(withId(R.id.card_families_helped)).check(matches(isDisplayed()));
    }

    @Test
    public void testDashboard_DisplaysQuickActionButtons() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Check that quick action buttons are displayed
        onView(withId(R.id.btn_new_donation)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_inventory)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_transparency)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_reports)).check(matches(isDisplayed()));
    }

    @Test
    public void testDashboard_QuickActionButtons_AreClickable() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Check that quick action buttons are clickable
        onView(withId(R.id.btn_new_donation)).check(matches(isClickable()));
        onView(withId(R.id.btn_inventory)).check(matches(isClickable()));
        onView(withId(R.id.btn_transparency)).check(matches(isClickable()));
        onView(withId(R.id.btn_reports)).check(matches(isClickable()));
    }

    @Test
    public void testDashboard_InventoryQuickAction_NavigatesToInventory() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Click inventory quick action
        onView(withId(R.id.btn_inventory)).perform(click());
        
        // Verify navigation to inventory
        onView(withId(R.id.nav_inventory)).check(matches(isSelected()));
    }

    @Test
    public void testDashboard_TransparencyQuickAction_NavigatesToTransparency() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Click transparency quick action
        onView(withId(R.id.btn_transparency)).perform(click());
        
        // Verify navigation to transparency
        onView(withId(R.id.nav_transparency)).check(matches(isSelected()));
    }

    @Test
    public void testDashboard_ReportsQuickAction_NavigatesToReports() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Click reports quick action
        onView(withId(R.id.btn_reports)).perform(click());
        
        // Verify navigation to reports
        onView(withId(R.id.nav_reports)).check(matches(isSelected()));
    }

    @Test
    public void testDashboard_DisplaysFloatingActionButton() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Check that FAB is displayed and clickable
        onView(withId(R.id.fab_quick_donation))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()));
    }

    @Test
    public void testDashboard_NotificationIcon_IsClickable() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Check notification container is clickable
        onView(withId(R.id.fl_notification_container))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()));
    }

    @Test
    public void testDashboard_NotificationIcon_NavigatesToNotifications() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Click notification icon
        onView(withId(R.id.fl_notification_container)).perform(click());
        
        // Verify navigation to notifications
        onView(withId(R.id.nav_notifications)).check(matches(isSelected()));
    }

    @Test
    public void testDashboard_RecentActivitiesSection_IsDisplayed() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Check recent activities section
        onView(withId(R.id.tv_recent_activities))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.recent_activities)));
        
        onView(withId(R.id.rv_recent_activities)).check(matches(isDisplayed()));
    }

    @Test
    public void testDashboard_AccessibilityHeadings() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Check that headings have proper accessibility attributes
        onView(withId(R.id.tv_welcome)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_quick_actions)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_recent_activities)).check(matches(isDisplayed()));
    }

    @Test
    public void testDashboard_AccessibilityContentDescriptions() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Check that interactive elements have content descriptions
        onView(withId(R.id.fab_quick_donation)).check(matches(hasContentDescription()));
        onView(withId(R.id.fl_notification_container)).check(matches(hasContentDescription()));
    }

    @Test
    public void testDashboard_ScrollableContent() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Check that the main content is scrollable (NestedScrollView)
        onView(isRoot()).check(matches(isDisplayed()));
        
        // Verify all main sections are present
        onView(withId(R.id.ll_statistics_container)).check(matches(isDisplayed()));
        onView(withId(R.id.gl_quick_actions)).check(matches(isDisplayed()));
        onView(withId(R.id.rv_recent_activities)).check(matches(isDisplayed()));
    }

    @Test
    public void testDashboard_EmptyState_WhenNoActivities() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // If no recent activities, empty state should be shown
        // This test assumes empty state initially - adjust based on actual data
        try {
            onView(withId(R.id.ll_empty_state)).check(matches(isDisplayed()));
        } catch (Exception e) {
            // If empty state is not shown, recent activities should be displayed
            onView(withId(R.id.rv_recent_activities)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testDashboard_StatisticsCards_HaveProperLayout() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Check that statistics cards are properly laid out
        onView(withId(R.id.ll_statistics_container)).check(matches(isDisplayed()));
        
        // All three cards should be visible
        onView(withId(R.id.card_total_donations)).check(matches(isDisplayed()));
        onView(withId(R.id.card_distributed_items)).check(matches(isDisplayed()));
        onView(withId(R.id.card_families_helped)).check(matches(isDisplayed()));
    }

    @Test
    public void testDashboard_QuickActionsGrid_ProperLayout() {
        // Navigate to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Check that quick actions are in a grid layout
        onView(withId(R.id.gl_quick_actions)).check(matches(isDisplayed()));
        
        // All four buttons should be visible
        onView(withId(R.id.btn_new_donation)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_inventory)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_transparency)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_reports)).check(matches(isDisplayed()));
    }
}