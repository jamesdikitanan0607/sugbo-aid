package com.sugboaid.utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for AccessibilityUtils
 * Tests accessibility helper methods and TalkBack support
 */
@RunWith(MockitoJUnitRunner.class)
public class AccessibilityUtilsTest {

    @Mock
    private Context mockContext;

    @Mock
    private AccessibilityManager mockAccessibilityManager;

    @Mock
    private View mockView;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getSystemService(Context.ACCESSIBILITY_SERVICE)).thenReturn(mockAccessibilityManager);
    }

    @Test
    public void testIsAccessibilityEnabled_WhenEnabled() {
        // Arrange
        when(mockAccessibilityManager.isEnabled()).thenReturn(true);

        // Act
        boolean result = AccessibilityUtils.isAccessibilityEnabled(mockContext);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsAccessibilityEnabled_WhenDisabled() {
        // Arrange
        when(mockAccessibilityManager.isEnabled()).thenReturn(false);

        // Act
        boolean result = AccessibilityUtils.isAccessibilityEnabled(mockContext);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testIsTalkBackEnabled_WhenEnabled() {
        // Arrange
        when(mockAccessibilityManager.isEnabled()).thenReturn(true);
        when(mockAccessibilityManager.isTouchExplorationEnabled()).thenReturn(true);

        // Act
        boolean result = AccessibilityUtils.isTalkBackEnabled(mockContext);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsTalkBackEnabled_WhenDisabled() {
        // Arrange
        when(mockAccessibilityManager.isEnabled()).thenReturn(true);
        when(mockAccessibilityManager.isTouchExplorationEnabled()).thenReturn(false);

        // Act
        boolean result = AccessibilityUtils.isTalkBackEnabled(mockContext);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testAnnounceForAccessibility_WithValidView() {
        // Arrange
        String message = "Test announcement";

        // Act
        AccessibilityUtils.announceForAccessibility(mockView, message);

        // Assert
        verify(mockView).announceForAccessibility(message);
    }

    @Test
    public void testAnnounceForAccessibility_WithNullView() {
        // Arrange
        String message = "Test announcement";

        // Act & Assert (should not throw exception)
        AccessibilityUtils.announceForAccessibility(null, message);
    }

    @Test
    public void testAnnounceForAccessibility_WithNullMessage() {
        // Act & Assert (should not throw exception)
        AccessibilityUtils.announceForAccessibility(mockView, null);
    }

    @Test
    public void testSetupFocusManagement_SetsCorrectProperties() {
        // Act
        AccessibilityUtils.setupFocusManagement(mockView);

        // Assert
        verify(mockView).setFocusable(true);
        verify(mockView).setFocusableInTouchMode(false);
    }

    @Test
    public void testFormatCurrencyForAccessibility_CorrectFormat() {
        // Arrange
        when(mockContext.getString(com.sugboaid.donation.R.string.accessibility_currency_format))
            .thenReturn("%.2f Philippine pesos");

        // Act
        String result = AccessibilityUtils.formatCurrencyForAccessibility(mockContext, 1234.56);

        // Assert
        assertEquals("1234.56 Philippine pesos", result);
    }

    @Test
    public void testFormatStatisticForAccessibility_CorrectFormat() {
        // Arrange
        when(mockContext.getString(com.sugboaid.donation.R.string.accessibility_statistic_format))
            .thenReturn("%1$s: %2$s, change of %3$s");

        // Act
        String result = AccessibilityUtils.formatStatisticForAccessibility(
            mockContext, "Total Donations", "₱5,000", "+10%");

        // Assert
        assertEquals("Total Donations: ₱5,000, change of +10%", result);
    }

    @Test
    public void testFormatDonationItemForAccessibility_CorrectFormat() {
        // Arrange
        when(mockContext.getString(com.sugboaid.donation.R.string.accessibility_donation_item_format))
            .thenReturn("%1$d %2$s selected for donation");

        // Act
        String result = AccessibilityUtils.formatDonationItemForAccessibility(
            mockContext, "Rice bags", 5);

        // Assert
        assertEquals("5 Rice bags selected for donation", result);
    }

    @Test
    public void testSetupClickableAccessibility_SetsCorrectProperties() {
        // Arrange
        String contentDescription = "Test button";
        String actionDescription = "Tap to test";

        // Act
        AccessibilityUtils.setupClickableAccessibility(mockView, contentDescription, actionDescription);

        // Assert
        verify(mockView).setContentDescription(contentDescription);
        verify(mockView).setFocusable(true);
        verify(mockView).setClickable(true);
    }

    @Test
    public void testSetupToggleAccessibility_EnabledState() {
        // Arrange
        String label = "Dark mode";
        boolean isChecked = true;

        // Act
        AccessibilityUtils.setupToggleAccessibility(mockView, label, isChecked);

        // Assert
        verify(mockView).setContentDescription("Dark mode. Enabled");
    }

    @Test
    public void testSetupToggleAccessibility_DisabledState() {
        // Arrange
        String label = "Dark mode";
        boolean isChecked = false;

        // Act
        AccessibilityUtils.setupToggleAccessibility(mockView, label, isChecked);

        // Assert
        verify(mockView).setContentDescription("Dark mode. Disabled");
    }

    @Test
    public void testSetupNavigationAccessibility_SelectedState() {
        // Arrange
        String destination = "Dashboard";
        boolean isSelected = true;

        // Act
        AccessibilityUtils.setupNavigationAccessibility(mockView, destination, isSelected);

        // Assert
        verify(mockView).setContentDescription("Dashboard. Currently selected");
        verify(mockView).setSelected(true);
    }

    @Test
    public void testSetupNavigationAccessibility_UnselectedState() {
        // Arrange
        String destination = "Reports";
        boolean isSelected = false;

        // Act
        AccessibilityUtils.setupNavigationAccessibility(mockView, destination, isSelected);

        // Assert
        verify(mockView).setContentDescription("Reports");
        verify(mockView).setSelected(false);
    }
}