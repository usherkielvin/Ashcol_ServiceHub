# Facebook Login Setup Guide

This guide will help you set up Facebook Login for your Android app.

## Step 1: Create a Facebook App

1. Go to [Facebook Developers Console](https://developers.facebook.com/apps/)
2. Click **"Create App"** button
3. Select **"Consumer"** as the app type
4. Fill in the app details:
   - **App Display Name**: Ashcol (or your app name)
   - **App Contact Email**: Your email address
   - Click **"Create App"**

## Step 2: Add Facebook Login Product

**Option 1: Using the Products Menu (Recommended)**
1. In your app dashboard, look for **"Products"** or **"Add Products"** in the left sidebar menu
2. If you see a list of products, find **"Facebook Login"** and click **"Set Up"** or **"Get Started"**
3. If you don't see it, look for a **"+"** button or **"Add Product"** button near the top
4. Choose **"Android"** as the platform when prompted

**Option 2: Direct Configuration**
1. In the left sidebar, click **"Settings"** → **"Basic"**
2. Scroll down to find **"Add Platform"** button
3. Click **"Add Platform"** → Select **"Android"**
4. Then go to **"Products"** → **"Facebook Login"** → **"Settings"**
5. Click **"Settings"** under Facebook Login to configure

**Option 3: If You Still Can't Find It**
1. Make sure you've created a **"Consumer"** app type (not Business)
2. Try this direct link after creating your app:
   - Go to: `https://developers.facebook.com/apps/[YOUR_APP_ID]/fb-login/quickstart/`
   - Replace `[YOUR_APP_ID]` with your actual App ID
3. Or navigate: **Products** (left menu) → **Facebook Login** → **Settings** → **Quickstart**

## Step 3: Configure Android Settings

**Where to Configure:**
- Go to **Settings** → **Basic** in left sidebar
- Scroll down to **"Platform"** section
- Click **"Add Platform"** → Select **"Android"** (if not already added)
- Or click on **"Android"** if it's already in your platforms list

**What to Enter:**

1. **Package Name**: `app.hub` (from your `AndroidManifest.xml`)
2. **Class Name**: `app.hub.common.MainActivity` (your main activity) - *Optional but recommended*
3. **Key Hashes**: 
   - For **Debug**: Get your SHA-1 key and convert it to Base64
   - For **Release**: Get your release SHA-1 key (for production builds)
   
   **To get SHA-1 on Windows (PowerShell):**
   ```powershell
   cd C:\Users\usher\StudioProjects\Ashcol_ServiceHub
   .\gradlew signingReport
   ```
   
   Look for `SHA1:` under `Variant: debug` and copy it:
   **Your SHA-1:** `29:77:F7:5E:1E:B9:19:C1:98:25:77:C4:07:F0:6F:7E:C1:65:8F:B8`
   
   **Convert SHA-1 to Base64** (Facebook uses Base64, not hex with colons):
   - Use online tool: https://base64.guru/converter/encode/hex
   - Or use this method:
     1. Remove colons from SHA-1: `2977F75E1EB919C1982577C407F06F7EC1658FB8`
     2. Go to: https://base64.guru/converter/encode/hex
     3. Paste the hex string (without colons) and convert to Base64
     4. Copy the Base64 result and paste it in Facebook's "Key Hashes" field

4. Click **"Save Changes"** at the bottom

## Step 4: Get Your App ID and Client Token

1. In your app dashboard, go to **Settings** → **Basic**
2. Copy your **App ID** (looks like: `1234567890123456`)
3. Copy your **App Secret** (click "Show" to reveal it)
   - Note: You'll need the **Client Token** instead for Android
4. For **Client Token**:
   - Go to **Settings** → **Advanced** → **Security**
   - Find **"Client Token"** (or generate one if not shown)
   - Copy this token

## Step 5: Update Your App Configuration

Open `app/src/main/res/values/strings.xml` and replace the placeholder values:

```xml
<!-- Replace YOUR_FACEBOOK_APP_ID with your actual Facebook App ID -->
<string name="facebook_app_id">1234567890123456</string>

<!-- Replace YOUR_FACEBOOK_CLIENT_TOKEN with your actual Client Token -->
<string name="facebook_client_token">your_client_token_here</string>

<!-- Replace YOUR_FACEBOOK_APP_ID in the scheme (keep the "fb" prefix) -->
<string name="fb_login_protocol_scheme">fb1234567890123456</string>
```

**Example:**
```xml
<string name="facebook_app_id">1234567890123456</string>
<string name="facebook_client_token">a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6</string>
<string name="fb_login_protocol_scheme">fb1234567890123456</string>
```

## Step 6: Verify AndroidManifest.xml

The AndroidManifest.xml is already configured, but verify it includes:

```xml
<meta-data 
    android:name="com.facebook.sdk.ApplicationId" 
    android:value="@string/facebook_app_id"/>
<meta-data 
    android:name="com.facebook.sdk.ClientToken" 
    android:value="@string/facebook_client_token"/>
```

## Step 7: Test Facebook Login

1. Build and run your app
2. Click "Continue with Facebook" button
3. You should see Facebook login dialog
4. After authorization, user should be logged in

## Troubleshooting

### "Facebook login not configured" error:
- Verify `facebook_app_id` and `facebook_client_token` are correct in `strings.xml`
- Make sure AndroidManifest.xml has the meta-data tags
- Check that package name in Facebook Console matches `app.hub`

### "Invalid key hash" error:
- Make sure you added your SHA-1 hash to Facebook Console
- For debug: Use debug keystore SHA-1
- For release: Use release keystore SHA-1
- Convert SHA-1 to Base64 format correctly

### App crashes on Facebook login:
- Check that Facebook SDK is added to `build.gradle.kts`
- Verify dependencies are synced
- Check logcat for specific error messages

## Important Notes

- **App ID** and **Client Token** are public (can be in strings.xml)
- **App Secret** should NEVER be in the app (it's for server-side only)
- For production, consider using different App IDs for debug and release
- Facebook requires your app to be reviewed for certain permissions

## Next Steps

After setting up, Facebook login will work the same as Google login:
- Users can login/register with Facebook
- Existing users will be logged in
- New users will be registered automatically
- Same flow as Google Sign-In (Tell Us → Create Password → Account Created)

## Useful Links

- [Facebook Developers Console](https://developers.facebook.com/apps/)
- [Facebook Login for Android Documentation](https://developers.facebook.com/docs/facebook-login/android)
- [Getting Started with Facebook SDK](https://developers.facebook.com/docs/android/getting-started)
