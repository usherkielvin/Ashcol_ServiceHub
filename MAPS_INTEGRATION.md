# Google Maps Integration Setup

## Prerequisites

To use the map selection feature in the Ashcol ServiceHub app, you need to set up a Google Maps API key.

## Steps to Obtain a Google Maps API Key

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the following APIs:
   - Maps SDK for Android
   - Places API (optional, for enhanced location search)
   - Geocoding API
4. Create credentials:
   - Go to "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "API Key"
5. Secure your API key:
   - Click on the newly created API key
   - Under "Application restrictions", select "Android apps"
   - Add your app's package name: `app.hub`
   - Add your SHA-1 certificate fingerprint
   - Under "API restrictions", select "Restrict key" and choose the APIs you enabled

## Configure the API Key

1. Replace the placeholder in `gradle.properties`:
   ```
   MAPS_API_KEY=YOUR_ACTUAL_GOOGLE_MAPS_API_KEY_HERE
   ```

2. To get your SHA-1 fingerprint:
   ```bash
   # For debug keystore
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```

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