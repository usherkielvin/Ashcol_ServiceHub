# Google Sign-In Quick Fix - Action Items

## What I Just Fixed

✅ **Fixed OAuth Client ID mismatch** in `strings.xml`
- Changed from incorrect ID to the correct Web Client ID from your Firebase project

## What YOU Need to Do

### 1. Get Your SHA-1 Fingerprint

Open PowerShell and run:

```powershell
cd $env:USERPROFILE\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Copy the `SHA1:` value (looks like: `AA:BB:CC:DD:EE:FF:...`)

### 2. Add SHA-1 to Firebase

1. Go to: https://console.firebase.google.com/
2. Select project: **ashcol-hub**
3. Click gear icon → **Project settings**
4. Scroll to "Your apps" → find `app.hub`
5. Scroll to **"SHA certificate fingerprints"**
6. Click **"Add fingerprint"**
7. Paste your SHA-1
8. Click **"Save"**

### 3. Download Updated google-services.json

1. Still in Firebase Console Project Settings
2. Scroll to your app (`app.hub`)
3. Click **"Download google-services.json"**
4. Replace the file at: `Ashcol_ServiceHub/app/google-services.json`

### 4. Rebuild the App

In Android Studio:
1. **Build** → **Clean Project**
2. **Build** → **Rebuild Project**
3. **Uninstall** the app from your device/emulator
4. **Run** the app again

### 5. Test

Try Google Sign-In - it should work now!

## Why This Fixes It

**Two issues were causing the problem:**

1. **Missing SHA-1**: Your machine's SHA-1 wasn't registered in Firebase
   - Google Sign-In requires ALL developers' SHA-1s to be registered
   - Your friend's SHA-1 is already there (that's why it works for them)
   - You need to add yours too

2. **Wrong OAuth Client ID**: The `server_client_id` in strings.xml was incorrect
   - I fixed this for you
   - It now points to the correct Web Client ID from your Firebase project

## Need Help?

If you can't access Firebase Console:
1. Ask your friend to add you as an owner/editor
2. Or ask them to add your SHA-1 for you
3. Or share your SHA-1 with them and they can add it

## Verification

After following these steps, you should see:
- Google Sign-In account picker appears
- After selecting account, sign-in completes successfully
- You're logged into the app
- No more "Sign in was cancelled" error

## Still Having Issues?

Check the full guide: `GOOGLE_SIGNIN_FIX_GUIDE.md`
