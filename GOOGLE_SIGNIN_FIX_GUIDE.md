# Google Sign-In "Cancelled" Error - Fix Guide

## Problem
You're getting "Sign in was cancelled" error when trying to sign in with Google, even though it works on your friend's PC.

## Root Cause
**Your machine's SHA-1 certificate fingerprint is not registered in Firebase.**

Each developer's computer has a unique debug keystore with a different SHA-1 fingerprint. Google Sign-In requires ALL developers' SHA-1 fingerprints to be registered in the Firebase project.

## Solution: Add Your SHA-1 to Firebase

### Step 1: Get Your SHA-1 Fingerprint

#### Option A: Using PowerShell (Recommended for Windows)

1. Open PowerShell
2. Run this command:

```powershell
cd $env:USERPROFILE\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

3. Look for the line that says `SHA1:` - it will look like:
   ```
   SHA1: 29:77:F7:5E:1E:B9:19:C1:98:25:77:C4:07:F0:6F:7E:C1:65:8F:B8
   ```

4. **Copy this entire SHA-1 value** (including the colons)

#### Option B: Using Android Studio Gradle

1. Open Android Studio
2. Open the Gradle panel (right sidebar)
3. Navigate: `Ashcol_ServiceHub` → `app` → `Tasks` → `android` → `signingReport`
4. Double-click `signingReport`
5. In the output, find the `SHA1:` line under "Variant: debug"
6. Copy the SHA-1 value

#### If debug.keystore doesn't exist:

Run this command to create it:

```powershell
keytool -genkey -v -keystore $env:USERPROFILE\.android\debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
```

Then run the first command again to get the SHA-1.

### Step 2: Add SHA-1 to Firebase Console

1. **Go to Firebase Console:**
   - Visit: https://console.firebase.google.com/
   - Sign in with your Google account

2. **Select Your Project:**
   - Click on the **"ashcol-hub"** project

3. **Open Project Settings:**
   - Click the gear icon (⚙️) next to "Project Overview"
   - Select **"Project settings"**

4. **Find Your Android App:**
   - Scroll down to "Your apps" section
   - Find the app with package name: `app.hub`
   - Click on it to expand

5. **Add Your SHA-1:**
   - Scroll down to the **"SHA certificate fingerprints"** section
   - Click **"Add fingerprint"**
   - Paste your SHA-1 fingerprint (from Step 1)
   - Click **"Save"**

### Step 3: Download Updated google-services.json (IMPORTANT!)

After adding your SHA-1:

1. In Firebase Console, stay in Project Settings
2. Scroll down to your app (`app.hub`)
3. Click **"Download google-services.json"**
4. **Replace** the existing file at:
   ```
   Ashcol_ServiceHub/app/google-services.json
   ```

**Note:** This step is CRITICAL! The google-services.json file contains OAuth client IDs that are linked to your SHA-1 fingerprints.

### Step 4: Clean and Rebuild

1. In Android Studio, click **Build** → **Clean Project**
2. Wait for it to finish
3. Click **Build** → **Rebuild Project**
4. Wait for the build to complete

### Step 5: Test Google Sign-In

1. Uninstall the app from your device/emulator (important!)
2. Run the app again from Android Studio
3. Try Google Sign-In
4. It should work now!

## Why This Happens

- Google Sign-In uses OAuth 2.0 for authentication
- OAuth requires the app's signature (SHA-1) to be pre-registered
- This prevents unauthorized apps from impersonating your app
- Each developer has a different debug keystore = different SHA-1
- Your friend's SHA-1 is already registered (that's why it works for them)
- Your SHA-1 needs to be added too

## Current Registered SHA-1

Looking at your `google-services.json`, there's currently one SHA-1 registered:
```
2977f75e1eb919c1982577c407f06f7ec1658fb8
```

This is likely your friend's SHA-1. You need to add yours alongside it.

## Troubleshooting

### Still getting "Sign in cancelled"?

1. **Wait 5-10 minutes** after adding SHA-1 (changes need to propagate)
2. **Verify SHA-1 was saved** in Firebase Console
3. **Make sure you downloaded** the updated google-services.json
4. **Uninstall and reinstall** the app completely
5. **Check Firebase project access** - make sure you're added as an owner/editor

### Error: "DEVELOPER_ERROR" (Error code 10)

This means SHA-1 mismatch. Double-check:
- You copied the correct SHA-1
- You added it to the correct Firebase project
- You downloaded the updated google-services.json
- You rebuilt the app

### Can't access Firebase Console?

Ask your friend to:
1. Go to Firebase Console → Project Settings
2. Click "Users and permissions" tab
3. Click "Add member"
4. Add your email address
5. Give you "Owner" or "Editor" role

## Quick Checklist

- [ ] Got my SHA-1 fingerprint
- [ ] Added SHA-1 to Firebase Console
- [ ] Downloaded updated google-services.json
- [ ] Replaced the file in `Ashcol_ServiceHub/app/`
- [ ] Cleaned and rebuilt the project
- [ ] Uninstalled old app from device
- [ ] Tested Google Sign-In

## Need Help?

If you're still having issues:

1. Share your SHA-1 with your friend
2. Ask them to add it to Firebase Console
3. Ask them to send you the updated google-services.json
4. Replace the file and rebuild

## Additional Notes

- **Multiple SHA-1s are normal** - Firebase supports multiple fingerprints
- **Debug vs Release** - You'll need different SHA-1s for release builds
- **Each device** - If you test on multiple devices, they all use the same debug keystore
- **CI/CD** - Continuous integration systems need their SHA-1s added too

## CRITICAL: Fix Mismatched OAuth Client ID

**There's also a configuration mismatch in your code!**

Your `strings.xml` has an incorrect OAuth client ID. Here's how to fix it:

1. Open: `Ashcol_ServiceHub/app/src/main/res/values/strings.xml`

2. Find this line (around line 356):
   ```xml
   <string name="server_client_id">700225042346-1tkoait6tbj7eeo7ijvmodb0upa7siuu.apps.googleusercontent.com</string>
   ```

3. Replace it with the correct Web Client ID from your google-services.json:
   ```xml
   <string name="server_client_id">927228841081-p3q144ul75esbuagua8vvjdbsa1mroa2.apps.googleusercontent.com</string>
   ```

**This mismatch could also cause sign-in failures!**

## Reference

- Package Name: `app.hub`
- Firebase Project: `ashcol-hub`
- Project Number: `927228841081`
- Android OAuth Client: `927228841081-lu20bjd2le9sig11knelnqqek8o8k943.apps.googleusercontent.com`
- Web OAuth Client (for ID token): `927228841081-p3q144ul75esbuagua8vvjdbsa1mroa2.apps.googleusercontent.com`
