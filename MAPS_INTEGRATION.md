# Google Maps Integration Setup

## Prerequisites

To use the map selection feature in the Ashcol ServiceHub app, you need to set up a Google Maps API key.

## Steps to Obtain a Google Maps API Key

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. **Enable billing** - Google Maps requires billing to be enabled (free tier: $200/month credit)
4. Enable the following APIs:
   - **Maps SDK for Android** (required)
   - Places API (optional, for enhanced location search)
   - Geocoding API
5. Create credentials:
   - Go to "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "API Key"
6. Secure your API key (recommended for production):
   - Click on the newly created API key
   - Under "Application restrictions", select "Android apps"
   - Add your app's package name: `app.hub`
   - Add your SHA-1 certificate fingerprint (see below)
   - Under "API restrictions", select "Restrict key" and choose "Maps SDK for Android"

## Configure the API Key

1. Edit `gradle.properties` in the project root and set your API key:
   ```
   MAPS_API_KEY=YOUR_ACTUAL_GOOGLE_MAPS_API_KEY_HERE
   ```

2. To get your SHA-1 fingerprint (required if using Android app restrictions):
   ```bash
   # For debug keystore (Windows)
   keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android

   # For debug keystore (Mac/Linux)
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
   Copy the SHA-1 value and add it in Google Cloud Console under your API key's Android app restrictions.

## Map Won't Load? Troubleshooting

If you see a **gray/blank map** instead of the map tiles:

1. **Check API key** - Verify `MAPS_API_KEY` in `gradle.properties` is correct and has no extra spaces
2. **Enable Maps SDK for Android** - In [Google Cloud Console > APIs & Services > Library](https://console.cloud.google.com/apis/library), search for "Maps SDK for Android" and enable it
3. **Enable billing** - Maps requires billing enabled at [Console > Billing](https://console.cloud.google.com/billing)
4. **API key restrictions** - If you restricted the key:
   - Package name must be exactly `app.hub`
   - SHA-1 fingerprint must match your signing key (debug vs release)
   - For unrestricted key during testing: set "Application restrictions" to "None"
5. **Emulator** - Use an emulator with **Google Play** (has Play Store icon), not plain AOSP. Maps needs Google Play Services
6. **Sync and rebuild** - File > Sync Project with Gradle Files, then Build > Clean Project, Build > Rebuild Project

## Features Added

- Map selection activity allowing users to tap on the map to select a location
- Automatic restriction to Philippines boundaries only
- Address reverse-geocoding to convert coordinates to readable addresses
- Integration with the ticket creation form

## Usage

1. Open the ticket creation form
2. Click the "Map" button next to the location field
3. Tap on the map to select a location within the Philippines
4. The selected address will appear in the location field when you return to the form