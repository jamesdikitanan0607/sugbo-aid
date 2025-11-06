package com.sugboaid.donation.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.sugboaid.donation.R;

/**
 * Helper class for managing notification permissions, especially for Android 13+
 */
public class NotificationPermissionHelper {
    
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    
    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }
    
    /**
     * Checks if notification permission is granted
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, 
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        // For Android 12 and below, notifications are enabled by default
        return true;
    }
    
    /**
     * Requests notification permission for Android 13+
     */
    public static void requestNotificationPermission(Activity activity, PermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasNotificationPermission(activity)) {
                callback.onPermissionGranted();
                return;
            }
            
            // Check if we should show rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, 
                    Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionRationale(activity, () -> {
                    ActivityCompat.requestPermissions(activity, 
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
                }, callback);
            } else {
                ActivityCompat.requestPermissions(activity, 
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            callback.onPermissionGranted();
        }
    }
    
    /**
     * Handles permission result from onRequestPermissionsResult
     */
    public static void handlePermissionResult(int requestCode, String[] permissions, 
                                            int[] grantResults, PermissionCallback callback) {
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callback.onPermissionGranted();
            } else {
                callback.onPermissionDenied();
            }
        }
    }
    
    /**
     * Shows rationale dialog explaining why notification permission is needed
     */
    private static void showPermissionRationale(Context context, Runnable onAccept, 
                                              PermissionCallback callback) {
        new AlertDialog.Builder(context)
            .setTitle("Notification Permission")
            .setMessage("SugboAid needs notification permission to keep you updated about:\n\n" +
                       "• New donations received\n" +
                       "• Low inventory alerts\n" +
                       "• Distribution updates\n" +
                       "• Important system notifications\n\n" +
                       "You can manage these notifications in the app settings.")
            .setPositiveButton("Allow", (dialog, which) -> {
                if (onAccept != null) {
                    onAccept.run();
                }
            })
            .setNegativeButton("Not Now", (dialog, which) -> {
                callback.onPermissionDenied();
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * Shows dialog when permission is denied, offering to go to settings
     */
    public static void showPermissionDeniedDialog(Context context) {
        new AlertDialog.Builder(context)
            .setTitle("Notifications Disabled")
            .setMessage("To receive important updates about donations and inventory, " +
                       "please enable notifications in your device settings.\n\n" +
                       "You can still use the app, but you won't receive push notifications.")
            .setPositiveButton("Open Settings", (dialog, which) -> {
                // Open app settings
                android.content.Intent intent = new android.content.Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                android.net.Uri uri = android.net.Uri.fromParts("package", 
                    context.getPackageName(), null);
                intent.setData(uri);
                context.startActivity(intent);
            })
            .setNegativeButton("Continue", null)
            .show();
    }
    
    /**
     * Creates an ActivityResultLauncher for permission requests (for use with Fragment)
     */
    public static ActivityResultLauncher<String> createPermissionLauncher(Fragment fragment, 
                                                                         PermissionCallback callback) {
        return fragment.registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    callback.onPermissionGranted();
                } else {
                    callback.onPermissionDenied();
                }
            }
        );
    }
    
    /**
     * Requests permission using ActivityResultLauncher (for Fragment)
     */
    public static void requestPermissionWithLauncher(Fragment fragment, 
                                                   ActivityResultLauncher<String> launcher,
                                                   PermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasNotificationPermission(fragment.requireContext())) {
                callback.onPermissionGranted();
                return;
            }
            
            if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionRationale(fragment.requireContext(), () -> {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }, callback);
            } else {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            callback.onPermissionGranted();
        }
    }
}