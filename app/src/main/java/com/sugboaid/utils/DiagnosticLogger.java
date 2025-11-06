package com.sugboaid.utils;

import android.content.Context;
import android.util.Log;
import com.sugboaid.donation.BuildConfig;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Comprehensive diagnostic logging utility for SugboAid application
 * Provides structured logging with different levels and crash reporting capabilities
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5
 */
public class DiagnosticLogger {
    
    private static final String TAG_PREFIX = "SugboAid_";
    private static final String CRASH_TAG = TAG_PREFIX + "CRASH";
    private static final String NAVIGATION_TAG = TAG_PREFIX + "NAV";
    private static final String RESOURCE_TAG = TAG_PREFIX + "RESOURCE";
    private static final String STARTUP_TAG = TAG_PREFIX + "STARTUP";
    private static final String ERROR_TAG = TAG_PREFIX + "ERROR";
    
    private static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    
    private static boolean isDebugMode = BuildConfig.DEBUG;
    private static Context appContext;
    
    /**
     * Initialize the diagnostic logger with application context
     * @param context Application context
     */
    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
        logStartup("DiagnosticLogger initialized");
    }
    
    /**
     * Log application crash with complete stack trace
     * Requirement: 3.1 - Capture complete stack trace in Logcat
     * @param throwable The exception that caused the crash
     * @param context Additional context information
     */
    public static void logCrash(Throwable throwable, String context) {
        String timestamp = DATE_FORMAT.format(new Date());
        String stackTrace = getStackTrace(throwable);
        
        String crashReport = String.format(
            "[%s] CRASH DETECTED\n" +
            "Context: %s\n" +
            "Exception: %s\n" +
            "Message: %s\n" +
            "Stack Trace:\n%s",
            timestamp,
            context != null ? context : "Unknown",
            throwable.getClass().getSimpleName(),
            throwable.getMessage() != null ? throwable.getMessage() : "No message",
            stackTrace
        );
        
        Log.e(CRASH_TAG, crashReport);
        
        // Also log to error tag for filtering
        Log.e(ERROR_TAG, "Crash in " + context + ": " + throwable.getMessage());
    }
    
    /**
     * Log navigation events during startup
     * Requirement: 3.2 - Log navigation events during startup sequence
     * @param event Navigation event description
     * @param fromDestination Source destination
     * @param toDestination Target destination
     */
    public static void logNavigation(String event, String fromDestination, String toDestination) {
        String timestamp = DATE_FORMAT.format(new Date());
        String message = String.format(
            "[%s] NAV: %s | From: %s | To: %s",
            timestamp,
            event,
            fromDestination != null ? fromDestination : "None",
            toDestination != null ? toDestination : "None"
        );
        
        Log.i(NAVIGATION_TAG, message);
        
        if (isDebugMode) {
            Log.d(STARTUP_TAG, "Navigation: " + event);
        }
    }
    
    /**
     * Log resource loading failures with specific details
     * Requirement: 3.3 - Log specific resource identifiers and error details
     * @param resourceType Type of resource (layout, drawable, etc.)
     * @param resourceId Resource identifier
     * @param error Error details
     */
    public static void logResourceError(String resourceType, String resourceId, String error) {
        String timestamp = DATE_FORMAT.format(new Date());
        String message = String.format(
            "[%s] RESOURCE_ERROR | Type: %s | ID: %s | Error: %s",
            timestamp,
            resourceType,
            resourceId,
            error
        );
        
        Log.e(RESOURCE_TAG, message);
        Log.e(ERROR_TAG, "Resource error: " + resourceType + " - " + resourceId);
    }
    
    /**
     * Log resource validation results
     * Requirement: 3.4 - Validate all critical resources during initialization
     * @param resourceType Type of resource being validated
     * @param resourceId Resource identifier
     * @param isValid Whether the resource is valid
     * @param details Additional validation details
     */
    public static void logResourceValidation(String resourceType, String resourceId, 
                                           boolean isValid, String details) {
        String timestamp = DATE_FORMAT.format(new Date());
        String status = isValid ? "VALID" : "INVALID";
        String message = String.format(
            "[%s] RESOURCE_VALIDATION | Type: %s | ID: %s | Status: %s | Details: %s",
            timestamp,
            resourceType,
            resourceId,
            status,
            details != null ? details : "None"
        );
        
        if (isValid) {
            if (isDebugMode) {
                Log.d(RESOURCE_TAG, message);
            }
        } else {
            Log.w(RESOURCE_TAG, message);
        }
    }
    
    /**
     * Log startup sequence events
     * Requirement: 3.5 - Provide verbose logging for troubleshooting
     * @param event Startup event description
     */
    public static void logStartup(String event) {
        String timestamp = DATE_FORMAT.format(new Date());
        String message = String.format("[%s] STARTUP: %s", timestamp, event);
        
        Log.i(STARTUP_TAG, message);
        
        if (isDebugMode) {
            Log.d(STARTUP_TAG, "Startup event: " + event);
        }
    }
    
    /**
     * Log general errors with context
     * @param tag Custom tag for the error
     * @param message Error message
     * @param throwable Optional throwable
     */
    public static void logError(String tag, String message, Throwable throwable) {
        String timestamp = DATE_FORMAT.format(new Date());
        String fullTag = TAG_PREFIX + tag;
        
        if (throwable != null) {
            String stackTrace = getStackTrace(throwable);
            String errorMessage = String.format(
                "[%s] ERROR: %s\nException: %s\nStack Trace:\n%s",
                timestamp,
                message,
                throwable.getMessage(),
                stackTrace
            );
            Log.e(fullTag, errorMessage);
        } else {
            String errorMessage = String.format("[%s] ERROR: %s", timestamp, message);
            Log.e(fullTag, errorMessage);
        }
    }
    
    /**
     * Log warnings
     * @param tag Custom tag for the warning
     * @param message Warning message
     */
    public static void logWarning(String tag, String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String fullTag = TAG_PREFIX + tag;
        String warningMessage = String.format("[%s] WARNING: %s", timestamp, message);
        
        Log.w(fullTag, warningMessage);
    }
    
    /**
     * Log info messages (only in debug mode)
     * @param tag Custom tag for the info
     * @param message Info message
     */
    public static void logInfo(String tag, String message) {
        if (isDebugMode) {
            String timestamp = DATE_FORMAT.format(new Date());
            String fullTag = TAG_PREFIX + tag;
            String infoMessage = String.format("[%s] INFO: %s", timestamp, message);
            
            Log.i(fullTag, infoMessage);
        }
    }
    
    /**
     * Log debug messages (only in debug mode)
     * @param tag Custom tag for the debug message
     * @param message Debug message
     */
    public static void logDebug(String tag, String message) {
        if (isDebugMode) {
            String timestamp = DATE_FORMAT.format(new Date());
            String fullTag = TAG_PREFIX + tag;
            String debugMessage = String.format("[%s] DEBUG: %s", timestamp, message);
            
            Log.d(fullTag, debugMessage);
        }
    }
    
    /**
     * Log activity lifecycle events
     * @param activityName Name of the activity
     * @param lifecycleEvent Lifecycle event (onCreate, onResume, etc.)
     */
    public static void logActivityLifecycle(String activityName, String lifecycleEvent) {
        logStartup(activityName + " - " + lifecycleEvent);
    }
    
    /**
     * Log fragment lifecycle events
     * @param fragmentName Name of the fragment
     * @param lifecycleEvent Lifecycle event
     */
    public static void logFragmentLifecycle(String fragmentName, String lifecycleEvent) {
        logNavigation("Fragment lifecycle", fragmentName, lifecycleEvent);
    }
    
    /**
     * Log view binding events
     * @param className Class where view binding occurs
     * @param event Binding event description
     * @param success Whether the binding was successful
     */
    public static void logViewBinding(String className, String event, boolean success) {
        String status = success ? "SUCCESS" : "FAILED";
        String message = String.format("ViewBinding in %s: %s - %s", className, event, status);
        
        if (success) {
            logDebug("VIEW_BINDING", message);
        } else {
            logError("VIEW_BINDING", message, null);
        }
    }
    
    /**
     * Convert throwable to string representation
     * @param throwable The throwable to convert
     * @return String representation of the stack trace
     */
    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Enable or disable debug mode
     * @param enabled Whether debug mode should be enabled
     */
    public static void setDebugMode(boolean enabled) {
        isDebugMode = enabled;
        logInfo("CONFIG", "Debug mode " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Check if debug mode is enabled
     * @return true if debug mode is enabled
     */
    public static boolean isDebugMode() {
        return isDebugMode;
    }
}