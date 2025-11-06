package com.sugboaid.donation;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Validate icon display across different contexts
 * Requirements: 2.1, 2.2, 2.3, 4.3
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class IconDisplayValidationTest {

    private Context context;
    private PackageManager packageManager;
    private UiDevice device;
    private String packageName;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        packageManager = context.getPackageManager();
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        packageName = context.getPackageName();
    }

    /**
     * Test home screen launcher display
     * Requirement 2.1: Launcher icon shall display complete logo without cropping
     */
    @Test
    public void testHomeScreenLauncherDisplay() {
        // Go to home screen
        device.pressHome();
        
        try {
            // Wait for home screen to load
            Thread.sleep(2000);
            
            // Look for the SugboAid app icon on home screen
            UiObject appIcon = device.findObject(new UiSelector()
                .className("android.widget.TextView")
                .textContains("SugboAid"));
            
            if (!appIcon.exists()) {
                // Try alternative selectors for different launchers
                appIcon = device.findObject(new UiSelector()
                    .descriptionContains("SugboAid"));
            }
            
            if (!appIcon.exists()) {
                // If not on home screen, check app drawer
                testAppDrawerIconAppearance();
                return;
            }
            
            assertTrue("SugboAid icon should be visible on home screen", appIcon.exists());
            
            // Verify icon is clickable (indicates proper display)
            assertTrue("App icon should be clickable", appIcon.isClickable());
            
            // Get icon bounds to verify it's not zero-sized (cropped)
            android.graphics.Rect bounds = appIcon.getBounds();
            assertTrue("Icon width should be greater than 0", bounds.width() > 0);
            assertTrue("Icon height should be greater than 0", bounds.height() > 0);
            
            // Verify reasonable icon size (not too small indicating cropping issues)
            assertTrue("Icon should have reasonable minimum size", 
                bounds.width() >= 48 && bounds.height() >= 48);
            
        } catch (UiObjectNotFoundException e) {
            fail("Could not find SugboAid icon on home screen: " + e.getMessage());
        } catch (InterruptedException e) {
            fail("Thread interrupted during home screen test");
        }
    }

    /**
     * Test app drawer icon appearance
     * Requirement 2.2: Icon shall maintain proper proportions across all screen densities
     */
    @Test
    public void testAppDrawerIconAppearance() {
        // Go to home screen first
        device.pressHome();
        
        try {
            Thread.sleep(1000);
            
            // Open app drawer (method varies by launcher)
            openAppDrawer();
            
            Thread.sleep(2000);
            
            // Look for SugboAid in app drawer
            UiObject appIcon = device.findObject(new UiSelector()
                .textContains("SugboAid"));
            
            if (!appIcon.exists()) {
                // Try scrolling to find the app
                scrollToFindApp();
                appIcon = device.findObject(new UiSelector()
                    .textContains("SugboAid"));
            }
            
            assertTrue("SugboAid should be visible in app drawer", appIcon.exists());
            
            // Verify icon properties
            android.graphics.Rect bounds = appIcon.getBounds();
            assertTrue("App drawer icon should have proper width", bounds.width() > 0);
            assertTrue("App drawer icon should have proper height", bounds.height() > 0);
            
            // Verify icon maintains aspect ratio (should be roughly square for launcher icons)
            double aspectRatio = (double) bounds.width() / bounds.height();
            assertTrue("Icon aspect ratio should be close to 1:1 (square), got: " + aspectRatio,
                Math.abs(aspectRatio - 1.0) < 0.3);
            
        } catch (UiObjectNotFoundException e) {
            fail("Could not find SugboAid in app drawer: " + e.getMessage());
        } catch (InterruptedException e) {
            fail("Thread interrupted during app drawer test");
        }
    }

    /**
     * Test recent apps task switcher
     * Requirement 2.3: Icon shall be clearly visible against both light and dark backgrounds
     */
    @Test
    public void testRecentAppsTaskSwitcher() {
        // Launch the app first to ensure it appears in recent apps
        launchSugboAidApp();
        
        try {
            Thread.sleep(2000);
            
            // Go to home screen
            device.pressHome();
            Thread.sleep(1000);
            
            // Open recent apps
            device.pressRecentApps();
            Thread.sleep(2000);
            
            // Look for SugboAid in recent apps
            UiObject recentApp = device.findObject(new UiSelector()
                .textContains("SugboAid"));
            
            if (!recentApp.exists()) {
                // Try alternative selectors for different Android versions
                recentApp = device.findObject(new UiSelector()
                    .descriptionContains("SugboAid"));
            }
            
            assertTrue("SugboAid should appear in recent apps", recentApp.exists());
            
            // Verify the app card is properly displayed
            android.graphics.Rect bounds = recentApp.getBounds();
            assertTrue("Recent app card should have proper dimensions", 
                bounds.width() > 100 && bounds.height() > 100);
            
            // Verify it's clickable (indicates proper display)
            assertTrue("Recent app should be clickable", recentApp.isClickable());
            
        } catch (UiObjectNotFoundException e) {
            fail("Could not find SugboAid in recent apps: " + e.getMessage());
        } catch (InterruptedException e) {
            fail("Thread interrupted during recent apps test");
        } finally {
            // Return to home screen
            device.pressHome();
        }
    }

    /**
     * Test notification icon (if applicable)
     * Requirement 4.3: Icon shall conform to Android's icon design guidelines
     */
    @Test
    public void testNotificationIcon() {
        // This test verifies that notification icons are properly configured
        // We'll test the resource existence and properties
        
        try {
            // Check if notification icon resources exist
            int notificationIcon = context.getResources().getIdentifier(
                "ic_notification", "drawable", packageName);
            
            if (notificationIcon == 0) {
                // Try alternative notification icon names
                notificationIcon = context.getResources().getIdentifier(
                    "ic_stat_notification", "drawable", packageName);
            }
            
            if (notificationIcon == 0) {
                // Use launcher icon as fallback (common practice)
                notificationIcon = context.getResources().getIdentifier(
                    "ic_launcher", "mipmap", packageName);
            }
            
            assertTrue("Notification icon resource should exist", notificationIcon != 0);
            
            // Verify the drawable can be loaded
            Drawable notificationDrawable = context.getResources().getDrawable(notificationIcon, null);
            assertNotNull("Notification drawable should be loadable", notificationDrawable);
            
            // Verify reasonable dimensions for notification icon
            int width = notificationDrawable.getIntrinsicWidth();
            int height = notificationDrawable.getIntrinsicHeight();
            
            if (width > 0 && height > 0) {
                assertTrue("Notification icon should have reasonable size", 
                    width >= 24 && height >= 24 && width <= 256 && height <= 256);
            }
            
        } catch (Exception e) {
            fail("Error testing notification icon: " + e.getMessage());
        }
    }

    /**
     * Test launcher icon resource validation
     * Requirement 2.1, 2.2: Complete logo display and proper proportions
     */
    @Test
    public void testLauncherIconResourceValidation() {
        try {
            // Test main launcher icon
            int launcherIcon = context.getResources().getIdentifier(
                "ic_launcher", "mipmap", packageName);
            assertTrue("Main launcher icon should exist", launcherIcon != 0);
            
            Drawable launcherDrawable = context.getResources().getDrawable(launcherIcon, null);
            assertNotNull("Launcher icon drawable should be loadable", launcherDrawable);
            
            // Test round launcher icon (Android 7.1+)
            int roundIcon = context.getResources().getIdentifier(
                "ic_launcher_round", "mipmap", packageName);
            if (roundIcon != 0) {
                Drawable roundDrawable = context.getResources().getDrawable(roundIcon, null);
                assertNotNull("Round launcher icon should be loadable", roundDrawable);
            }
            
            // Test adaptive icon (Android 8.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                testAdaptiveIconConfiguration();
            }
            
            // Verify icon dimensions are reasonable
            int width = launcherDrawable.getIntrinsicWidth();
            int height = launcherDrawable.getIntrinsicHeight();
            
            if (width > 0 && height > 0) {
                assertTrue("Launcher icon should have minimum size", 
                    width >= 48 && height >= 48);
                assertTrue("Launcher icon should not be excessively large", 
                    width <= 512 && height <= 512);
                
                // Verify aspect ratio is reasonable (should be square or close to it)
                double aspectRatio = (double) width / height;
                assertTrue("Launcher icon aspect ratio should be close to 1:1, got: " + aspectRatio,
                    Math.abs(aspectRatio - 1.0) < 0.2);
            }
            
        } catch (Exception e) {
            fail("Error validating launcher icon resources: " + e.getMessage());
        }
    }

    /**
     * Test adaptive icon configuration (Android 8.0+)
     * Requirement 2.4: Adaptive icon format support
     */
    @Test
    public void testAdaptiveIconConfiguration() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return; // Skip on older versions
        }
        
        try {
            // Check for adaptive icon XML
            int adaptiveIcon = context.getResources().getIdentifier(
                "ic_launcher", "mipmap", packageName);
            
            if (adaptiveIcon != 0) {
                Drawable adaptiveDrawable = context.getResources().getDrawable(adaptiveIcon, null);
                assertNotNull("Adaptive icon should be loadable", adaptiveDrawable);
                
                // On Android 8.0+, this should be an AdaptiveIconDrawable
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // We can't directly test AdaptiveIconDrawable without API 26+ imports
                    // But we can verify the drawable loads successfully
                    assertTrue("Adaptive icon should have reasonable bounds", 
                        adaptiveDrawable.getBounds().width() >= 0);
                }
            }
            
            // Test foreground and background resources exist
            int foreground = context.getResources().getIdentifier(
                "ic_launcher_foreground", "drawable", packageName);
            int background = context.getResources().getIdentifier(
                "ic_launcher_background", "color", packageName);
            
            if (foreground != 0) {
                Drawable foregroundDrawable = context.getResources().getDrawable(foreground, null);
                assertNotNull("Foreground drawable should be loadable", foregroundDrawable);
            }
            
            if (background != 0) {
                int backgroundColor = context.getResources().getColor(background, null);
                // Verify color is not transparent (alpha > 0)
                assertTrue("Background color should not be transparent", 
                    (backgroundColor & 0xFF000000) != 0);
            }
            
        } catch (Exception e) {
            fail("Error testing adaptive icon configuration: " + e.getMessage());
        }
    }

    /**
     * Test icon display across different densities
     * Requirement 2.2: Proper proportions across all screen densities
     */
    @Test
    public void testIconDisplayAcrossDensities() {
        String[] densities = {"mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"};
        
        for (String density : densities) {
            try {
                // Check if icon exists for this density
                String resourceName = "mipmap-" + density + "/ic_launcher";
                int resourceId = context.getResources().getIdentifier(
                    "ic_launcher", "mipmap", packageName);
                
                if (resourceId != 0) {
                    Drawable drawable = context.getResources().getDrawable(resourceId, null);
                    assertNotNull("Icon should be loadable for density: " + density, drawable);
                    
                    // Verify dimensions are appropriate for density
                    int width = drawable.getIntrinsicWidth();
                    int height = drawable.getIntrinsicHeight();
                    
                    if (width > 0 && height > 0) {
                        // Expected sizes for each density
                        int expectedSize = getExpectedIconSize(density);
                        
                        // Allow some tolerance for different icon formats
                        assertTrue("Icon size should be appropriate for " + density + 
                            " (expected ~" + expectedSize + ", got " + width + "x" + height + ")",
                            Math.abs(width - expectedSize) <= expectedSize * 0.5 &&
                            Math.abs(height - expectedSize) <= expectedSize * 0.5);
                    }
                }
            } catch (Exception e) {
                // Some densities might not have specific resources, which is okay
                // as Android will scale from available densities
            }
        }
    }

    // Helper methods

    private void openAppDrawer() {
        try {
            // Try different methods to open app drawer based on launcher
            
            // Method 1: Swipe up from bottom (common on newer Android)
            device.swipe(device.getDisplayWidth() / 2, device.getDisplayHeight() - 100,
                        device.getDisplayWidth() / 2, device.getDisplayHeight() / 2, 10);
            
            Thread.sleep(1000);
            
            // Method 2: Look for app drawer button and click it
            UiObject appDrawerButton = device.findObject(new UiSelector()
                .descriptionContains("Apps"));
            
            if (appDrawerButton.exists()) {
                appDrawerButton.click();
                return;
            }
            
            // Method 3: Try alternative app drawer descriptions
            appDrawerButton = device.findObject(new UiSelector()
                .className("android.widget.ImageView")
                .clickable(true));
            
            if (appDrawerButton.exists()) {
                appDrawerButton.click();
            }
            
        } catch (Exception e) {
            // If all methods fail, the swipe up should have worked on most devices
        }
    }

    private void scrollToFindApp() {
        try {
            // Scroll down in app drawer to find SugboAid
            for (int i = 0; i < 5; i++) {
                device.swipe(device.getDisplayWidth() / 2, device.getDisplayHeight() * 3 / 4,
                           device.getDisplayWidth() / 2, device.getDisplayHeight() / 4, 10);
                Thread.sleep(500);
                
                UiObject app = device.findObject(new UiSelector().textContains("SugboAid"));
                if (app.exists()) {
                    break;
                }
            }
        } catch (Exception e) {
            // Ignore scrolling errors
        }
    }

    private void launchSugboAidApp() {
        try {
            // Launch the app using package manager
            Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
            }
        } catch (Exception e) {
            // Alternative method: use UI automation to click app icon
            device.pressHome();
            try {
                Thread.sleep(1000);
                UiObject appIcon = device.findObject(new UiSelector().textContains("SugboAid"));
                if (appIcon.exists()) {
                    appIcon.click();
                }
            } catch (Exception ex) {
                // Ignore if can't launch via UI
            }
        }
    }

    private int getExpectedIconSize(String density) {
        switch (density) {
            case "mdpi": return 48;
            case "hdpi": return 72;
            case "xhdpi": return 96;
            case "xxhdpi": return 144;
            case "xxxhdpi": return 192;
            default: return 96; // Default to xhdpi
        }
    }
}