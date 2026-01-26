# Google Sign-In Setup for Collaborators

This guide explains how to set up Google Sign-In on your development machine so you can test Google authentication.

## Why It's Needed

Google Sign-In requires your app's **SHA-1 fingerprint** to be registered in Google Cloud Console. Each developer's machine has a different debug keystore, so each needs their SHA-1 added.

## Step 1: Get Your SHA-1 Fingerprint

### For Windows (PowerShell):

```powershell
cd $env:USERPROFILE\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Look for:** `SHA1:` line - copy that value (it looks like: `AA:BB:CC:DD:EE:FF:...`)

### If debug.keystore doesn't exist:

The first time you build an Android app, Android Studio creates it automatically. If it doesn't exist:

1. Build the app once in Android Studio
2. Or create it manually:
```powershell
keytool -genkey -v -keystore $env:USERPROFILE\.android\debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
```

### Alternative: Get SHA-1 from Android Studio

1. Open Android Studio
2. Open your project
3. Click **Gradle** (right sidebar)
4. Navigate: `app` → `Tasks` → `android` → `signingReport`
5. Double-click `signingReport`
6. Look for `SHA1:` in the output
7. Copy the SHA-1 value

## Step 2: Add SHA-1 to Google Cloud Console

1. **Go to Google Cloud Console:**
   - https://console.cloud.google.com/
   - Sign in with the Google account that has access to the project

2. **Select the Project:**
   - Make sure you're in the correct Google Cloud project
   - (Ask the project owner which project to use)

3. **Navigate to OAuth Credentials:**
   - Go to **APIs & Services** → **Credentials**
   - Find your **OAuth 2.0 Client ID** (Android type)
   - Click **Edit** (pencil icon)

4. **Add Your SHA-1:**
   - In the **SHA-1 certificate fingerprints** section
   - Click **+ ADD SHA-1**
   - Paste your SHA-1 fingerprint (from Step 1)
   - Click **SAVE**

## Step 3: Wait for Propagation

- Changes can take **5-10 minutes** to propagate
- Try Google Sign-In after waiting

## Step 4: Test Google Sign-In

1. Build and run the app on your device/emulator
2. Try signing in with Google
3. It should work now!

## Troubleshooting

### Error: "10: DEVELOPER_ERROR" or "12500: Sign in failed"

**Solution:**
- Your SHA-1 is not registered or not yet propagated
- Double-check SHA-1 was added correctly
- Wait 10 minutes and try again
- Make sure you're using the correct Google Cloud project

### Error: "7: NETWORK_ERROR"

**Solution:**
- Check internet connection
- Make sure Google Play Services is updated on device
- Try on a different network

### Can't Find OAuth Credentials

**Solution:**
- Ask the project owner to:
  1. Share the Google Cloud project name
  2. Give you access to the project (IAM permissions)
  3. Or create a new OAuth client ID for you

## For Project Owner: Adding Collaborators

1. **Share Google Cloud Project:**
   - Go to Google Cloud Console
   - IAM & Admin → IAM
   - Click **+ ADD**
   - Add collaborator's email
   - Role: **Editor** or **Viewer** (Editor can add SHA-1)
   - Click **SAVE**

2. **Or Create Separate OAuth Client:**
   - APIs & Services → Credentials
   - Create Credentials → OAuth client ID
   - Application type: **Android**
   - Package name: `app.hub`
   - Add collaborator's SHA-1
   - Click **CREATE**

## Quick Reference

**Package Name:** `app.hub`  
**Keystore Location:** `C:\Users\YOUR_USERNAME\.android\debug.keystore`  
**Keystore Password:** `android`  
**Key Alias:** `androiddebugkey`  
**Key Password:** `android`

## Notes

- **Debug keystore** is for development only
- **Release keystore** (for production) needs different SHA-1
- Each developer needs their own SHA-1 added
- SHA-1 is unique per machine/keystore

## Need Help?

1. Verify SHA-1 is correct: Run the keytool command again
2. Check Google Cloud Console: Make sure SHA-1 is saved
3. Wait 10 minutes: Changes need time to propagate
4. Contact project owner: For Google Cloud access
