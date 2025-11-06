package com.sugboaid.utils;

import android.content.Context;

/**
 * Global exception handler for catching and logging uncaught exceptions
 * Requirements: 3.1 - Capture complete stack trace in Logcat
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    
    private static final String TAG = "GLOBAL_EXCEPTION";
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final Context context;
    private StartupDiagnosticManager diagnosticManager;
    
    public GlobalExceptionHandler(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.diagnosticManager = StartupDiagnosticManager.getInstance(context);
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            // Log the uncaught exception with comprehensive details
            DiagnosticLogger.logCrash(throwable, "Uncaught exception in thread: " + thread.getName());
            
            // Use diagnostic manager if available
            if (diagnosticManager != null) {
                diagnosticManager.handleUncaughtException(thread, throwable);
            }
            
            // Log additional system state information
            logSystemStateAtCrash();
            
        } catch (Exception e) {
            // If our logging fails, at least try to log that
            android.util.Log.e(TAG, "Failed to log uncaught exception", e);
        } finally {
            // Call the default handler to maintain normal crash behavior
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        }
    }
    
    /**
     * Log system state information at the time of crash
     */
    private void logSystemStateAtCrash() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory() / (1024 * 1024);
            long totalMemory = runtime.totalMemory() / (1024 * 1024);
            long freeMemory = runtime.freeMemory() / (1024 * 1024);
            long usedMemory = totalMemory - freeMemory;
            
            DiagnosticLogger.logError(TAG, String.format(
                "System state at crash - Memory: %dMB used, %dMB free, %dMB total, %dMB max",
                usedMemory, freeMemory, totalMemory, maxMemory
            ), null);
            
            // Log thread information
            Thread currentThread = Thread.currentThread();
            DiagnosticLogger.logError(TAG, String.format(
                "Thread info - Name: %s, ID: %d, Priority: %d, State: %s",
                currentThread.getName(), currentThread.getId(), 
                currentThread.getPriority(), currentThread.getState()
            ), null);
            
        } catch (Exception e) {
            android.util.Log.e(TAG, "Failed to log system state at crash", e);
        }
    }
    
    /**
     * Install the global exception handler
     * @param context Application context
     */
    public static void install(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(context));
        DiagnosticLogger.logInfo("EXCEPTION_HANDLER", "Global exception handler installed");
    }
}