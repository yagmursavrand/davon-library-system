# 🔧 Debug Setup Guide for Library Management System

## 📋 **Overview**
This guide explains how to set up and use debugging in Cursor IDE for the Java Quarkus backend.

## 🚀 **Step 1: Prerequisites**

### Required Extensions in Cursor IDE:
1. **Extension Pack for Java** (Microsoft)
2. **Language Support for Java by Red Hat**
3. **Debugger for Java**
4. **Maven for Java**
5. **Project Manager for Java**

### Java Environment:
- JDK 17 or higher
- Maven 3.6+
- Quarkus CLI (optional)

## ⚙️ **Step 2: Configuration Files**

### `.vscode/launch.json`
Contains debug configurations:
- **Debug Library Backend (Quarkus)**: Main application debugging
- **Debug Tests**: Run all tests in debug mode
- **Debug Specific Test**: Debug individual test files

### `.vscode/settings.json`
Contains Java development settings:
- Auto-build configuration
- Source paths
- Test configuration
- File exclusions

## 🐛 **Step 3: How to Debug**

### 3.1 **Setting Breakpoints**
1. Open any Java file in the project
2. Click in the left margin next to line numbers (red dot appears)
3. Or use `F9` to toggle breakpoints

### 3.2 **Starting Debug Session**
1. Press `F5` or go to Run → Start Debugging
2. Select "Debug Library Backend (Quarkus)"
3. Application will start and pause at breakpoints

### 3.3 **Debug Controls**
- **F5**: Continue execution
- **F10**: Step Over (execute current line)
- **F11**: Step Into (go into method calls)
- **Shift+F11**: Step Out (exit current method)
- **Shift+F5**: Stop debugging

### 3.4 **Debug Views**
- **Variables**: Inspect local variables and parameters
- **Watch**: Monitor specific expressions
- **Call Stack**: See method call hierarchy
- **Breakpoints**: Manage all breakpoints

## 🔍 **Step 4: Debug Helper Usage**

### 4.1 **Import Debug Helper**
```java
import com.davon.library.debug.DebugHelper;
```

### 4.2 **Available Methods**
```java
// Log debug messages
DebugHelper.logDebug("Processing user request");

// Inspect objects
DebugHelper.inspectObject("user", user);

// Set breakpoints programmatically
DebugHelper.breakpoint("UserService.createUser");

// Validate conditions
DebugHelper.validateCondition("User is not null", user != null);

// Measure execution time
DebugHelper.measureExecutionTime("Database query", () -> {
    // Your code here
});

// Method entry/exit logging
DebugHelper.methodEntry("createUser", "username", username, "email", email);
DebugHelper.methodExit("createUser", result);
```

## 🧪 **Step 5: Debugging Tests**

### 5.1 **Debug All Tests**
1. Select "Debug Tests" configuration
2. Press F5
3. Tests will run and pause at breakpoints

### 5.2 **Debug Specific Test**
1. Open the test file you want to debug
2. Select "Debug Specific Test" configuration
3. Press F5
4. Only that test will run in debug mode

## 📊 **Step 6: Debugging Features**

### 6.1 **Variable Inspection**
- Hover over variables to see values
- Use Variables panel to inspect all local variables
- Add expressions to Watch panel

### 6.2 **Conditional Breakpoints**
1. Right-click on breakpoint
2. Select "Edit Breakpoint"
3. Add condition (e.g., `user.getId() == 5`)

### 6.3 **Logpoints**
1. Right-click on breakpoint
2. Select "Add Logpoint"
3. Enter log message (e.g., `User created: {user.getName()}`)

## 🔧 **Step 7: Common Debug Scenarios**

### 7.1 **Debugging REST Endpoints**
1. Set breakpoint in Resource class method
2. Start debug session
3. Send HTTP request (use Postman or curl)
4. Debugger will pause at breakpoint

### 7.2 **Debugging Service Methods**
1. Set breakpoint in Service class
2. Start debug session
3. Trigger the service method
4. Step through business logic

### 7.3 **Debugging Database Operations**
1. Set breakpoint in Repository class
2. Start debug session
3. Execute database operation
4. Inspect query results

## 🚨 **Step 8: Troubleshooting**

### Common Issues:
1. **Breakpoints not hitting**: Ensure application is running in debug mode
2. **Variables not showing**: Check if code is compiled with debug info
3. **Hot reload not working**: Restart debug session after code changes

### Solutions:
1. **Clean and rebuild**: `mvn clean compile`
2. **Restart debug session**: Stop and restart debugging
3. **Check Java version**: Ensure JDK version matches project requirements

## 📝 **Step 9: Best Practices**

1. **Use meaningful breakpoints**: Set breakpoints at logical points
2. **Inspect variables systematically**: Check input, output, and intermediate values
3. **Use conditional breakpoints**: Avoid hitting breakpoints unnecessarily
4. **Document debugging sessions**: Note what you find and fix
5. **Use Debug Helper**: Leverage utility methods for consistent logging

## 🎯 **Step 10: Next Steps**

After setting up debugging:
1. Implement the required features with deliberate bugs
2. Use debugging tools to find and fix bugs
3. Document the debugging process
4. Create comprehensive tests
5. Verify all functionality works correctly

---

**Happy Debugging! 🐛✨** 