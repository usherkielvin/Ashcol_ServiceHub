# Google Sign-In Setup Guide

This guide explains how to set up Google Sign-In for the ServiceHub app.

## Prerequisites

1. Google Cloud Console account
2. Android project SHA-1 certificate fingerprint

## Step 1: Get Your SHA-1 Fingerprint

### For Debug Build:
```bash
# Windows (PowerShell)
cd C:\Users\usher\StudioProjects\Ashcol_ServiceHub
.\gradlew signingReport

# Or using keytool
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

### For Release Build:
```bash
keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
```

Copy the SHA-1 fingerprint (looks like: `AA:BB:CC:DD:EE:FF:...`)

## Step 2: Create OAuth 2.0 Client ID in Google Cloud Console

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable **Google Sign-In API**:
   - Navigate to "APIs & Services" > "Library"
   - Search for "Google Sign-In API"
   - Click "Enable"

4. Create OAuth 2.0 Client ID:
   - Go to "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "OAuth client ID"
   - Select "Android" as application type
   - Enter:
     - **Name**: ServiceHub Android
     - **Package name**: `app.hub` (from `applicationId` in `build.gradle.kts`)
     - **SHA-1 certificate fingerprint**: (paste your SHA-1 from Step 1)
   - Click "Create"

5. Copy the **Client ID** (looks like: `123456789-abcdefg.apps.googleusercontent.com`)

## Step 3: Configure Google Sign-In in the App

### Option A: Using `google-services.json` (Recommended for production)

1. Download `google-services.json` from Firebase Console:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Add your Android app with package name `app.hub`
   - Download `google-services.json`
   - Place it in `app/` directory

2. Add Firebase plugin to `build.gradle.kts`:
   ```kotlin
   plugins {
       id("com.android.application")
       id("com.google.gms.google-services") // Add this
   }
   ```

3. Add Firebase dependency:
   ```kotlin
   dependencies {
       implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
       implementation("com.google.firebase:firebase-auth")
   }
   ```

### Option B: Using Client ID directly (Current implementation)

The current implementation uses Google Sign-In SDK directly. To use a specific Client ID:

1. Update `CreateNewAccountFragment.java`:
   ```java
   private void setupGoogleSignIn() {
       // Replace with your Client ID from Google Cloud Console
       String clientId = "YOUR_CLIENT_ID.apps.googleusercontent.com";
       
       GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
           .requestEmail()
           .requestProfile()
           .requestIdToken(clientId) // Add this if you need ID token for backend
           .build();

       googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
   }
   ```

## Step 4: Backend Integration (Optional)

If you need to verify the Google ID token on your Laravel backend:

1. The `idToken` is available in `handleGoogleSignInSuccess()` method
2. Send this token to your backend API endpoint
3. Verify the token on the backend using Google's token verification API

Example backend endpoint:
```php
// In Laravel
Route::post('/api/v1/auth/google', function (Request $request) {
    $idToken = $request->input('id_token');
    // Verify token with Google
    // Create or login user
    // Return JWT token
});
```

## Testing

1. Build and run the app
2. Click "Continue with Google" button
3. Select a Google account
4. Grant permissions
5. The app should receive Google account information

## Troubleshooting

### Error: "10: DEVELOPER_ERROR"
- **Cause**: SHA-1 fingerprint mismatch or package name mismatch
- **Solution**: 
  - Verify SHA-1 in Google Cloud Console matches your keystore
  - Verify package name is exactly `app.hub`

### Error: "12501: SIGN_IN_CANCELLED"
- **Cause**: User cancelled the sign-in
- **Solution**: This is normal user behavior, handle gracefully

### Error: "7: NETWORK_ERROR"
- **Cause**: No internet connection
- **Solution**: Check device internet connection

### App crashes when clicking Google button
- **Cause**: Google Play Services not installed or outdated
- **Solution**: 
  - Install/update Google Play Services on device/emulator
  - For emulator: Use Google Play system image (not AOSP)

## Current Implementation

The current implementation:
- ✅ Requests email and profile information
- ✅ Handles sign-in result
- ✅ Extracts user data (email, name)
- ✅ Navigates to "Tell Us" screen to collect phone number
- ⚠️ Does not yet send ID token to backend (can be added)

## Next Steps

1. Get SHA-1 fingerprint from your keystore
2. Create OAuth client in Google Cloud Console
3. (Optional) Add Client ID to code if needed
4. Test Google Sign-In flow
5. Integrate with backend API if needed

---

**Note**: For production, consider using Firebase Authentication which provides additional features like account linking, email verification, and better security.
