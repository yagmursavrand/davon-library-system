package com.davon.library.debug;

/**
 * Simple test application to verify debug setup
 * This class doesn't use Lombok to avoid compilation issues
 */
public class DebugTestApp {
    
    public static void main(String[] args) {
        System.out.println("🔧 Debug Setup Test Application");
        System.out.println("================================");
        
        // Test debug helper methods
        DebugHelper.logInfo("Starting debug test application");
        
        // Test object inspection
        String testString = "Hello Debug World!";
        DebugHelper.inspectObject("testString", testString);
        
        // Test condition validation
        boolean condition = true;
        DebugHelper.validateCondition("Test condition is true", condition);
        
        // Test execution time measurement
        DebugHelper.measureExecutionTime("Simple operation", () -> {
            try {
                Thread.sleep(100); // Simulate some work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Test method entry/exit
        DebugHelper.methodEntry("main", "args", args);
        DebugHelper.methodExit("main", "SUCCESS");
        
        // Test breakpoint
        DebugHelper.breakpoint("main method end");
        
        System.out.println("✅ Debug setup test completed successfully!");
        System.out.println("🎯 You can now set breakpoints in Cursor IDE and debug this application");
    }
} 