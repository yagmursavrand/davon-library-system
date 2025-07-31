package com.davon.library.debug;

import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Debug Helper Class
 * 
 * This class provides utility methods for debugging the Library Management System.
 * It includes methods for logging, variable inspection, and debugging state.
 */
public class DebugHelper {
    
    private static final Logger logger = Logger.getLogger(DebugHelper.class.getName());
    private static final String DEBUG_PREFIX = "🐛 [DEBUG]";
    private static final String ERROR_PREFIX = "❌ [ERROR]";
    private static final String INFO_PREFIX = "ℹ️ [INFO]";
    
    // Private constructor to prevent instantiation
    private DebugHelper() {
        // Utility class - no instantiation needed
    }
    
    /**
     * Log a debug message with timestamp
     */
    public static void logDebug(String message) {
        logger.info(DEBUG_PREFIX + " " + new Date() + " - " + message);
    }
    
    /**
     * Log an error message with timestamp
     */
    public static void logError(String message, Throwable throwable) {
        logger.log(Level.SEVERE, ERROR_PREFIX + " " + new Date() + " - " + message + ": " + throwable.getMessage(), throwable);
    }
    
    /**
     * Log an info message with timestamp
     */
    public static void logInfo(String message) {
        logger.info(INFO_PREFIX + " " + new Date() + " - " + message);
    }
    
    /**
     * Inspect an object and log its details
     */
    public static void inspectObject(String name, Object obj) {
        if (obj == null) {
            logDebug(name + " = null");
        } else {
            logDebug(name + " = " + obj.toString() + " (type: " + obj.getClass().getSimpleName() + ")");
        }
    }
    
    /**
     * Create a debug breakpoint marker
     * This method can be used to set breakpoints in Cursor IDE
     */
    public static void breakpoint(String location) {
        logDebug("BREAKPOINT at: " + location);
        // This line is intentionally here for setting breakpoints
        int debugMarker = 1; // Set breakpoint here in Cursor IDE
    }
    
    /**
     * Validate a condition and log the result
     */
    public static boolean validateCondition(String condition, boolean result) {
        String status = result ? "✅ PASS" : "❌ FAIL";
        logDebug("Validation: " + condition + " - " + status);
        return result;
    }
    
    /**
     * Measure execution time of a block
     */
    public static long measureExecutionTime(String operation, Runnable task) {
        long startTime = System.currentTimeMillis();
        task.run();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        logDebug(operation + " completed in " + duration + "ms");
        return duration;
    }
    
    /**
     * Debug method entry
     */
    public static void methodEntry(String methodName, Object... params) {
        StringBuilder paramString = new StringBuilder();
        for (int i = 0; i < params.length; i += 2) {
            if (i + 1 < params.length) {
                paramString.append(params[i]).append("=").append(params[i + 1]);
                if (i + 2 < params.length) {
                    paramString.append(", ");
                }
            }
        }
        logDebug("Entering method: " + methodName + "(" + paramString.toString() + ")");
    }
    
    /**
     * Debug method exit
     */
    public static void methodExit(String methodName, Object result) {
        logDebug("Exiting method: " + methodName + " with result: " + result);
    }
} 