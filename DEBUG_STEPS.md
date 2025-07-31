# 🐛 Java Debugging in Cursor IDE - Step by Step Guide

## ✅ **Problem Fixed!**

The issue was that the test classes didn't have `main` methods and the launch.json was trying to use JUnit Console Launcher directly. Now everything is fixed!

## 🚀 **How to Start Debugging:**

### **Step 1: Open Run and Debug Panel**
1. Press `Cmd+Shift+D` (Mac) or `Ctrl+Shift+D` (Windows/Linux)
2. You should see the debug configurations in the dropdown

### **Step 2: Choose a Debug Configuration**
Select one of these from the dropdown:
- **`Debug VerySimpleDebugTest`** ← **Start with this one!**
- **`Debug BasicDebugTest`**
- **`Debug SimpleDebuggingTest`**
- **`Debug Current File`**

### **Step 3: Set Breakpoints**
1. Open `library-backend/src/test/java/com/davon/library/debug/VerySimpleDebugTest.java`
2. Click in the left margin next to line numbers to set breakpoints
3. Look for the `// BREAKPOINT 1:` comments - set breakpoints there!

### **Step 4: Start Debugging**
1. Click the **green play button** ▶️ in the Run and Debug panel
2. The debugger will start and stop at your first breakpoint

### **Step 5: Use Debug Controls**
When stopped at a breakpoint:
- **Step Over (F10)**: Execute current line and move to next
- **Step Into (F11)**: Go inside method calls
- **Step Out (Shift+F11)**: Exit current method
- **Continue (F5)**: Continue to next breakpoint

### **Step 6: Inspect Variables**
- **Variables Panel**: Shows all local variables
- **Watch Panel**: Add expressions to monitor
- **Call Stack**: See the method call chain

## 🎯 **What You'll See:**

### **Breakpoint Locations in VerySimpleDebugTest:**
```java
// BREAKPOINT 1: Set breakpoint here
System.out.println("=== VERY SIMPLE DEBUG TEST ===");

// BREAKPOINT 2: Set breakpoint here to see variables
int days = 10;
double fine = days * FINE_RATE;

// BREAKPOINT 3: Set breakpoint here to see result
System.out.println("Days: " + days);
```

### **Variables to Inspect:**
- `FINE_RATE` = 1.5 (BUG: Should be 2.0)
- `GRACE_DAYS` = 5 (BUG: Should be 7)
- `days` = 10
- `fine` = calculated value

## 🔍 **Debugging Tips:**

### **1. Variable Inspection:**
- Hover over variables to see their values
- Use the Variables panel to see all local variables
- Right-click variables to "Add to Watch"

### **2. Expression Evaluation:**
- In the Debug Console, type expressions like:
  - `days * 2.0` (correct calculation)
  - `FINE_RATE` (see the bug)
  - `fine > 15.0` (boolean expression)

### **3. Step Through Code:**
- Use F10 to step over lines
- Use F11 to step into method calls
- Watch how variables change as you step

## 🐛 **Bugs to Find:**

### **In VerySimpleDebugTest:**
1. `FINE_RATE = 1.5` (should be 2.0)
2. `GRACE_DAYS = 5` (should be 7)

### **In BasicDebugTest:**
1. `DAILY_FINE_RATE = 1.5` (should be 2.0)
2. `GRACE_PERIOD_DAYS = 5` (should be 7)
3. `MAX_FINE_AMOUNT = 30.0` (should be 50.0)

### **In SimpleDebuggingTest:**
Many more bugs to discover!

## 🎉 **Success Indicators:**

✅ **Debug toolbar appears**  
✅ **Variables panel shows values**  
✅ **Breakpoints are hit**  
✅ **You can step through code**  
✅ **You can inspect variables**  

## 🆘 **If Something Doesn't Work:**

1. **Restart Cursor IDE**
2. **Reload the window** (`Cmd+Shift+P` → "Developer: Reload Window")
3. **Check Java extension is installed**
4. **Make sure you're in the workspace root**

---

## 🎯 **Next Steps:**

Once you master basic debugging:
1. Try the other debug configurations
2. Practice with the other test classes
3. Move on to debugging the actual features with bugs
4. Document the bugs you find!

**Happy Debugging! 🐛✨** 