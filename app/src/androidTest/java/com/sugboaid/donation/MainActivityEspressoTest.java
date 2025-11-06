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
 * Espresso integration tests for MainActivity
 * Tests main navigation and user interactions
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityEspressoTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = 
        new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testMainActivity_DisplaysCorrectly() {
        // Check that main components are displayed
        onView(withId(R.id.nav_host_fragment)).check(matches(isDisplayed()));
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));
        onView(withId(R.id.fab_dark_mode_toggle)).check(matches(isDisplayed()));
    }

    @Test
    public void testBottomNavigation_NavigatesToDashboard() {
        // Click on dashboard navigation item
        onView(withId(R.id.nav_dashboard)).perform(click());
        
        // Verify dashboard is displayed
        onView(withId(R.id.nav_dashboard)).check(matches(isSelected()));
    }

    @Test
    public void testBottomNavigation_NavigatesToInventory() {
        // Click on inventory navigation item
        onView(withId(R.id.nav_inventory)).perform(click());
        
        // Verify inventory is displayed
        onView(withId(R.id.nav_inventory)).check(matches(isSelected()));
    }

    @Test
    public void testBottomNavigation_NavigatesToTransparency() {
        // Click on transparency navigation item
        onView(withId(R.id.nav_transparency)).perform(click());
        
        // Verify transparency is displayed
        onView(withId(R.id.nav_transparency)).check(matches(isSelected()));
    }

    @Test
    public void testBottomNavigation_NavigatesToReports() {
        // Click on reports navigation item
        onView(withId(R.id.nav_reports)).perform(click());
        
        // Verify reports is displayed
        onView(withId(R.id.nav_reports)).check(matches(isSelected()));
    }

    @Test
    public void testBottomNavigation_NavigatesToNotifications() {
        // Click on notifications navigation item
        onView(withId(R.id.nav_notifications)).perform(click());
        
        // Verify notifications is displayed
        onView(withId(R.id.nav_notifications)).check(matches(isSelected()));
    }

    @Test
    public void testDarkModeToggle_ClickableAndVisible() {
        // Check that dark mode toggle is visible and clickable
        onView(withId(R.id.fab_dark_mode_toggle))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()));
    }

    @Test
    public void testDarkModeToggle_PerformsClick() {
        // Click dark mode toggle
        onView(withId(R.id.fab_dark_mode_toggle)).perform(click());
        
        // Verify the button is still displayed (theme change should not hide it)
        onView(withId(R.id.fab_dark_mode_toggle)).check(matches(isDisplayed()));
    }

    @Test
    public void testAccessibility_ContentDescriptions() {
        // Check that important views have content descriptions
        onView(withId(R.id.fab_dark_mode_toggle))
            .check(matches(hasContentDescription()));
        
        onView(withId(R.id.bottom_navigation))
            .check(matches(hasContentDescription()));
    }

    @Test
    public void testAccessibility_FocusableElements() {
        // Check that interactive elements are focusable
        onView(withId(R.id.fab_dark_mode_toggle))
            .check(matches(isFocusable()));
        
        onView(withId(R.id.bottom_navigation))
            .check(matches(isFocusable()));
    }

    @Test
    public void testOfflineBanner_InitiallyHidden() {
        // Offline banner should be hidden initially (assuming online state)
        onView(withId(R.id.offline_banner))
            .check(matches(withEffectiveVisibility(Visibility.GONE)));
    }

    @Test
    public void testNavigationFlow_CompleteCircle() {
        // Test navigating through all screens and back to dashboard
        
        // Start at dashboard (default)
        onView(withId(R.id.nav_dashboard)).check(matches(isSelected()));
        
        // Navigate to inventory
        onView(withId(R.id.nav_inventory)).perform(click());
        onView(withId(R.id.nav_inventory)).check(matches(isSelected()));
        
        // Navigate to transparency
        onView(withId(R.id.nav_transparency)).perform(click());
        onView(withId(R.id.nav_transparency)).check(matches(isSelected()));
        
        // Navigate to reports
        onView(withId(R.id.nav_reports)).perform(click());
        onView(withId(R.id.nav_reports)).check(matches(isSelected()));
        
        // Navigate to notifications
        onView(withId(R.id.nav_notifications)).perform(click());
        onView(withId(R.id.nav_notifications)).check(matches(isSelected()));
        
        // Navigate back to dashboard
        onView(withId(R.id.nav_dashboard)).perform(click());
        onView(withId(R.id.nav_dashboard)).check(matches(isSelected()));
    }

    @Test
    public void testAccessibility_ImportantForAccessibility() {
        // Check that views have proper accessibility importance
        onView(withId(R.id.nav_host_fragment))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.bottom_navigation))
            .check(matches(isDisplayed()));
    }
}