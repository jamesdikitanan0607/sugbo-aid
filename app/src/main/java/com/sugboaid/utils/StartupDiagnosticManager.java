package com.sugboaid.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.navigation.NavController;

/**
 * Startup diagnostic manager that coordinates all diagnostic activities during app initialization
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5
 */
public class StartupDiagnosticManager {
    
    private static final String TAG = "STARTUP_DIAGNOSTIC";
    private static StartupDiagnosticManager instance;
    
    private Context context;
    private ResourceValidator resourceValidator;
    private boolean isInitialized = false;
    private long startupStartTime;
    
    private StartupDiagnosticManager(Context context) {
        this.context = context.getApplicationContext();
        this.resourceValidator = new ResourceValidator(context);
        this.startupStartTime = System.currentTimeMillis();
    }
    
    /**
     * Get singleton instance of StartupDiagnosticManager
     * @param context Application context
     * @return StartupDiagnosticManager instance
     */
    public static synchronized StartupDiagnosticManager getInstance(Context context) {
        if (instance == null) {
            instance = new StartupDiagnosticManager(context);
        }
        return instance;
    }
    
    /**
     * Initialize diagnostic system
     * Should be called early in Application.onCreate() or first Activity.onCreate()
     */
    public void initialize() {
        if (isInitialized) {
            DiagnosticLogger.logWarning(TAG, "StartupDiagnosticManager already initialized");
            return;
        }
        
        DiagnosticLogger.initialize(context);
        DiagnosticLogger.logStartup("StartupDiagnosticManager initialization started");
        
        // Log system information
        logSystemInformation();
        
        // Validate critical resources
        validateCriticalResources();
        
        isInitialized = true;
        
        long initTime = System.currentTimeMillis() - startupStartTime;
        DiagnosticLogger.logStartup("StartupDiagnosticManager initialization completed in " + initTime + "ms");
    }
    
    /**
     * Log system and app information for diagnostic purposes
     */
    private void logSystemInformation() {
        try {
            DiagnosticLogger.logInfo(TAG, "Android Version: " + android.os.Build.VERSION.RELEASE);
            DiagnosticLogger.logInfo(TAG, "API Level: " + android.os.Build.VERSION.SDK_INT);
            DiagnosticLogger.logInfo(TAG, "Device: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);
            DiagnosticLogger.logInfo(TAG, "App Package: " + context.getPackageName());
            
            // Log memory information
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory() / (1024 * 1024); // MB
            long totalMemory = runtime.totalMemory() / (1024 * 1024); // MB
            long freeMemory = runtime.freeMemory() / (1024 * 1024); // MB
            
            DiagnosticLogger.logInfo(TAG, String.format(
                "Memory - Max: %dMB, Total: %dMB, Free: %dMB", 
                maxMemory, totalMemory, freeMemory
            ));
            
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Failed to log system information", e);
        }
    }
    
    /**
     * Validate critical resources during startup
     */
    private void validateCriticalResources() {
        DiagnosticLogger.logStartup("Starting critical resource validation");
        
        try {
            ResourceValidator.ValidationSummary summary = resourceValidator.validateAllCriticalResources();
            
            if (summary.isAllValid()) {
                DiagnosticLogger.logStartup("All critical resources validated successfully");
            } else {
                DiagnosticLogger.logWarning(TAG, 
                    "Resource validation found " + summary.getInvalidCount() + " issues");
                
                for (String error : summary.getErrors()) {
                    DiagnosticLogger.logError(TAG, "Resource validation error: " + error, null);
                }
            }
        } catch (Exception e) {
            DiagnosticLogger.logError(TAG, "Critical resource validation failed", e);
        }
    }
    
    /**
     * Log activity startup with diagnostic information
     * @param activityName Name of the activity starting
     * @param intent Intent that started the activity (optional)
     */
    public void logActivityStartup(String activityName, android.content.Intent intent) {
        DiagnosticLogger.logActivityLifecycle(activityName, "onCreate");
        
        if (intent != null) {
            String action = intent.getAction();
            String extras = getIntentExtrasString(intent);
            
            DiagnosticLogger.logInfo(TAG, String.format(
                "Activity %s started with action: %s, extras: %s",
                activityName, action != null ? action : "None", extras
            ));
        }
        
        // Log timing information
        long currentTime = System.currentTimeMillis();
        long timeSinceStartup = currentTime - startupStartTime;
        DiagnosticLogger.logInfo(TAG, String.format(
            "Activity %s started %dms after app startup", activityName, timeSinceStartup
        ));
    }
    
    /**
     * Log navigation events with comprehensive details
     * @param event Navigation event description
     * @param fromDestination Source destination
     * @param toDestination Target destination
     * @param navController NavController instance (optional)
     */
    public void logNavigationEvent(String event, String fromDestination, 
                                 String toDestination, NavController navController) {
        DiagnosticLogger.logNavigation(event, fromDestination, toDestination);
        
        if (navController != null) {
            try {
                int currentDestId = navController.getCurrentDestination() != null ? 
                    navController.getCurrentDestination().getId() : -1;
                
                DiagnosticLogger.logDebug(TAG, String.format(
                    "Navigation state - Current destination ID: %d, Graph ID: %d",
                    currentDestId, navController.getGraph().getId()
                ));
            } catch (Exception e) {
                DiagnosticLogger.logError(TAG, "Failed to log navigation state", e);
            }
        }
    }
    
    /**
     * Log view binding events with error handling
     * @param className Class performing view binding
     * @param viewIds Array of view IDs being bound
     * @param success Whether binding was successful
     * @param error Error message if binding failed
     */
    public void logViewBindingEvent(String className, int[] viewIds, boolean success, String error) {
        if (success) {
            DiagnosticLogger.logViewBinding(className, 
                "Bound " + (viewIds != null ? viewIds.length : 0) + " views", true);
            
            if (DiagnosticLogger.isDebugMode() && viewIds != null) {
                for (int viewId : viewIds) {
                    String resourceName = getResourceName(viewId);
                    DiagnosticLogger.logDebug(TAG, 
                        "View bound in " + className + ": " + resourceName);
                }
            }
        } else {
            DiagnosticLogger.logViewBinding(className, "View binding failed: " + error, false);
            
            if (viewIds != null) {
                for (int viewId : viewIds) {
                    String resourceName = getResourceName(viewId);
                    DiagnosticLogger.logResourceError("view", resourceName, 
                        "View binding failed in " + className);
                }
            }
        }
    }
    
    /**
     * Log fragment lifecycle events
     * @param fragmentName Name of the fragment
     * @param lifecycleEvent Lifecycle event
     * @param isAttached Whether fragment is attached to activity
     */
    public void logFragmentLifecycle(String fragmentName, String lifecycleEvent, boolean isAttached) {
        DiagnosticLogger.logFragmentLifecycle(fragmentName, lifecycleEvent);
        
        if (!isAttached && ("onViewCreated".equals(lifecycleEvent) || 
                           "onResume".equals(lifecycleEvent))) {
            DiagnosticLogger.logWarning(TAG, 
                "Fragment " + fragmentName + " " + lifecycleEvent + " called while not attached");
        }
    }
    
    /**
     * Log startup completion with timing information
     * @param activityName Name of the activity that completed startup
     */
    public void logStartupCompletion(String activityName) {
        long totalStartupTime = System.currentTimeMillis() - startupStartTime;
        
        DiagnosticLogger.logStartup(String.format(
            "App startup completed in %s - Total time: %dms", 
            activityName, totalStartupTime
        ));
        
        // Log performance metrics
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        
        DiagnosticLogger.logInfo(TAG, String.format(
            "Startup metrics - Time: %dms, Memory used: %dMB", 
            totalStartupTime, usedMemory
        ));
    }
    
    /**
     * Handle and log uncaught exceptions
     * @param thread Thread where exception occurred
     * @param throwable The uncaught exception
     */
    public void handleUncaughtException(Thread thread, Throwable throwable) {
        String context = String.format(
            "Uncaught exception in thread: %s (ID: %d)", 
            thread.getName(), thread.getId()
        );
        
        DiagnosticLogger.logCrash(throwable, context);
        
        // Log additional context if available
        DiagnosticLogger.logError(TAG, "App state at crash: " + getCurrentAppState(), null);
    }
    
    /**
     * Get current app state for diagnostic purposes
     * @return String describing current app state
     */
    private String getCurrentAppState() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long freeMemory = runtime.freeMemory() / (1024 * 1024);
            long totalMemory = runtime.totalMemory() / (1024 * 1024);
            
            return String.format(
                "Memory: %dMB free of %dMB total, Initialized: %s", 
                freeMemory, totalMemory, isInitialized
            );
        } catch (Exception e) {
            return "Unable to determine app state: " + e.getMessage();
        }
    }
    
    /**
     * Get resource name from resource ID
     * @param resourceId Resource ID
     * @return Resource name or ID as string
     */
    private String getResourceName(int resourceId) {
        try {
            return context.getResources().getResourceName(resourceId);
        } catch (Exception e) {
            return "ID:" + resourceId;
        }
    }
    
    /**
     * Get intent extras as string for logging
     * @param intent Intent to extract extras from
     * @return String representation of extras
     */
    private String getIntentExtrasString(android.content.Intent intent) {
        try {
            android.os.Bundle extras = intent.getExtras();
            if (extras == null || extras.isEmpty()) {
                return "None";
            }
            
            StringBuilder sb = new StringBuilder();
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                sb.append(key).append("=").append(value).append(", ");
            }
            
            return sb.length() > 2 ? sb.substring(0, sb.length() - 2) : sb.toString();
        } catch (Exception e) {
            return "Error reading extras: " + e.getMessage();
        }
    }
    
    /**
     * Check if diagnostic manager is initialized
     * @return true if initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
}