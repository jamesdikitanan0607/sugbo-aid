package com.sugboaid.utils;

import static org.junit.Assert.*;

import android.graphics.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for ColorContrastUtils
 * Tests WCAG color contrast calculations and accessibility compliance
 */
@RunWith(MockitoJUnitRunner.class)
public class ColorContrastUtilsTest {

    @Test
    public void testCalculateContrastRatio_BlackOnWhite() {
        // Act
        double ratio = ColorContrastUtils.calculateContrastRatio(Color.BLACK, Color.WHITE);

        // Assert
        assertEquals(21.0, ratio, 0.1); // Maximum contrast ratio
    }

    @Test
    public void testCalculateContrastRatio_WhiteOnBlack() {
        // Act
        double ratio = ColorContrastUtils.calculateContrastRatio(Color.WHITE, Color.BLACK);

        // Assert
        assertEquals(21.0, ratio, 0.1); // Should be same as black on white
    }

    @Test
    public void testCalculateContrastRatio_SameColor() {
        // Act
        double ratio = ColorContrastUtils.calculateContrastRatio(Color.RED, Color.RED);

        // Assert
        assertEquals(1.0, ratio, 0.1); // Minimum contrast ratio
    }

    @Test
    public void testMeetsWCAG_AA_Normal_BlackOnWhite() {
        // Act
        boolean meets = ColorContrastUtils.meetsWCAG_AA_Normal(Color.BLACK, Color.WHITE);

        // Assert
        assertTrue(meets); // Should easily meet AA standards
    }

    @Test
    public void testMeetsWCAG_AA_Normal_LightGrayOnWhite() {
        // Arrange
        int lightGray = Color.rgb(200, 200, 200);

        // Act
        boolean meets = ColorContrastUtils.meetsWCAG_AA_Normal(lightGray, Color.WHITE);

        // Assert
        assertFalse(meets); // Should not meet AA standards
    }

    @Test
    public void testMeetsWCAG_AA_Large_LightGrayOnWhite() {
        // Arrange
        int lightGray = Color.rgb(150, 150, 150);

        // Act
        boolean meets = ColorContrastUtils.meetsWCAG_AA_Large(lightGray, Color.WHITE);

        // Assert
        // This might pass for large text (3:1 ratio) but not normal text (4.5:1)
        double ratio = ColorContrastUtils.calculateContrastRatio(lightGray, Color.WHITE);
        assertEquals(meets, ratio >= 3.0);
    }

    @Test
    public void testMeetsWCAG_AAA_Normal_RequiresHigherContrast() {
        // Arrange
        int darkGray = Color.rgb(100, 100, 100);

        // Act
        boolean meetsAA = ColorContrastUtils.meetsWCAG_AA_Normal(darkGray, Color.WHITE);
        boolean meetsAAA = ColorContrastUtils.meetsWCAG_AAA_Normal(darkGray, Color.WHITE);

        // Assert
        // AAA requires higher contrast than AA
        if (meetsAAA) {
            assertTrue(meetsAA); // If it meets AAA, it should also meet AA
        }
    }

    @Test
    public void testGetAccessibleTextColor_PreferDark() {
        // Arrange
        int lightBackground = Color.rgb(240, 240, 240);

        // Act
        int textColor = ColorContrastUtils.getAccessibleTextColor(lightBackground, true);

        // Assert
        assertEquals(Color.BLACK, textColor); // Should prefer dark text on light background
    }

    @Test
    public void testGetAccessibleTextColor_PreferLight() {
        // Arrange
        int darkBackground = Color.rgb(50, 50, 50);

        // Act
        int textColor = ColorContrastUtils.getAccessibleTextColor(darkBackground, false);

        // Assert
        assertEquals(Color.WHITE, textColor); // Should prefer light text on dark background
    }

    @Test
    public void testGetAccessibleTextColor_FallbackToBetterContrast() {
        // Arrange
        int mediumBackground = Color.rgb(128, 128, 128);

        // Act
        int textColorPreferDark = ColorContrastUtils.getAccessibleTextColor(mediumBackground, true);
        int textColorPreferLight = ColorContrastUtils.getAccessibleTextColor(mediumBackground, false);

        // Assert
        // Both should return a color that meets accessibility standards
        double darkContrast = ColorContrastUtils.calculateContrastRatio(Color.BLACK, mediumBackground);
        double lightContrast = ColorContrastUtils.calculateContrastRatio(Color.WHITE, mediumBackground);
        
        if (darkContrast >= 4.5) {
            assertEquals(Color.BLACK, textColorPreferDark);
        } else {
            assertEquals(Color.WHITE, textColorPreferDark);
        }
    }

    @Test
    public void testAdjustColorForContrast_AlreadyMeetsRequirement() {
        // Arrange
        int goodColor = Color.BLACK;
        int background = Color.WHITE;
        double targetRatio = 4.5;

        // Act
        int adjustedColor = ColorContrastUtils.adjustColorForContrast(goodColor, background, targetRatio);

        // Assert
        assertEquals(goodColor, adjustedColor); // Should not change if already meets requirement
    }

    @Test
    public void testAdjustColorForContrast_ImprovesContrast() {
        // Arrange
        int poorColor = Color.rgb(200, 200, 200);
        int background = Color.WHITE;
        double targetRatio = 4.5;
        double originalRatio = ColorContrastUtils.calculateContrastRatio(poorColor, background);

        // Act
        int adjustedColor = ColorContrastUtils.adjustColorForContrast(poorColor, background, targetRatio);
        double newRatio = ColorContrastUtils.calculateContrastRatio(adjustedColor, background);

        // Assert
        assertTrue(newRatio > originalRatio); // Should improve contrast
    }

    @Test
    public void testGetContrastDescription_ExcellentContrast() {
        // Act
        String description = ColorContrastUtils.getContrastDescription(8.0);

        // Assert
        assertEquals("AAA (Excellent)", description);
    }

    @Test
    public void testGetContrastDescription_GoodContrast() {
        // Act
        String description = ColorContrastUtils.getContrastDescription(5.0);

        // Assert
        assertEquals("AA (Good)", description);
    }

    @Test
    public void testGetContrastDescription_LargeTextOnly() {
        // Act
        String description = ColorContrastUtils.getContrastDescription(3.5);

        // Assert
        assertEquals("AA Large Text Only", description);
    }

    @Test
    public void testGetContrastDescription_InsufficientContrast() {
        // Act
        String description = ColorContrastUtils.getContrastDescription(2.0);

        // Assert
        assertEquals("Insufficient Contrast", description);
    }

    @Test
    public void testContrastRatio_CommonAppColors() {
        // Test common color combinations used in the app
        
        // Primary blue on white
        int primaryBlue = Color.rgb(30, 76, 130); // #1E4C82
        double blueWhiteRatio = ColorContrastUtils.calculateContrastRatio(primaryBlue, Color.WHITE);
        assertTrue("Primary blue should have good contrast on white", blueWhiteRatio >= 4.5);

        // Primary green on white
        int primaryGreen = Color.rgb(44, 182, 125); // #2CB67D
        double greenWhiteRatio = ColorContrastUtils.calculateContrastRatio(primaryGreen, Color.WHITE);
        assertTrue("Primary green should have good contrast on white", greenWhiteRatio >= 3.0);

        // Accent yellow on dark background
        int accentYellow = Color.rgb(253, 184, 19); // #FDB813
        int darkBackground = Color.rgb(50, 50, 50);
        double yellowDarkRatio = ColorContrastUtils.calculateContrastRatio(accentYellow, darkBackground);
        assertTrue("Accent yellow should have good contrast on dark background", yellowDarkRatio >= 3.0);
    }

    @Test
    public void testContrastRatio_EdgeCases() {
        // Test edge cases
        
        // Very similar colors
        int color1 = Color.rgb(100, 100, 100);
        int color2 = Color.rgb(101, 101, 101);
        double similarRatio = ColorContrastUtils.calculateContrastRatio(color1, color2);
        assertTrue("Very similar colors should have low contrast", similarRatio < 1.1);

        // Complementary colors
        int red = Color.RED;
        int cyan = Color.CYAN;
        double complementaryRatio = ColorContrastUtils.calculateContrastRatio(red, cyan);
        assertTrue("Complementary colors should have reasonable contrast", complementaryRatio > 1.0);
    }
}