# Gradle Build Issues - Fixed

## Issues Resolved

### 1. ClassCastException (HashMap$Node vs HashMap$TreeNode)
**Cause:** Corrupt Gradle cache or incompatible dependency versions

**Fix Applied:**
- Cleaned Gradle cache and daemon processes
- Updated Java compatibility to version 17 (required for AGP 8.13.2)
- Added proper JVM arguments for G1GC and memory management
- Disabled build cache to prevent corruption

### 2. Gradle Sync Issues
**Cause:** Corrupt build directories and cached files

**Fix Applied:**
- Cleaned all build directories (app/build, build, .gradle)
- Stopped all Gradle daemons
- Cleaned Gradle user cache

## Changes Made

### Files Modified:

1. **gradle.properties**
   - Updated JVM args: Added `-XX:+UseG1GC -XX:MaxMetaspaceSize=512m`
   - Disabled build cache: `org.gradle.caching=false`
   - Added daemon configuration

2. **app/build.gradle.kts**
   - Updated Java version: Changed from Java 11 to Java 17
   - This is required for Android Gradle Plugin 8.13.2

3. **fix-gradle-issues.ps1** (NEW)
   - Automated script to clean Gradle cache and build directories
   - Stops Gradle daemons
   - Verifies Java installation

## Next Steps

1. **Close Android Studio completely**
   - File → Exit (don't just close the window)

2. **Kill Java processes (if needed)**
   - Open Task Manager
   - End any `java.exe` or `javaw.exe` processes related to Gradle

3. **Reopen Android Studio**

4. **Sync Project**
   - Click "Sync Project with Gradle Files" (elephant icon)
   - Or: File → Sync Project with Gradle Files

5. **If issues persist:**
   - File → Invalidate Caches / Restart
   - Select "Invalidate and Restart"

6. **Rebuild Project**
   - Build → Clean Project
   - Build → Rebuild Project

## Verification

After syncing, check:
- ✅ No red error messages in Build tab
- ✅ Gradle sync completes successfully
- ✅ Project builds without ClassCastException
- ✅ All dependencies resolve correctly

## If Problems Continue

Run the fix script again:
```powershell
.\fix-gradle-issues.ps1
```

Or manually:
1. Delete `.gradle` folder in project root
2. Delete `build` folders
3. In Android Studio: File → Invalidate Caches / Restart

## Technical Details

- **Gradle Version:** 9.2.1
- **AGP Version:** 8.13.2
- **Java Version Required:** 17+ (you have Java 21 ✓)
- **Compile SDK:** 36
- **Target SDK:** 36
- **Min SDK:** 24
