# Google Cloud Console Setup - Quick Reference

## Your Certificate Fingerprints

**SHA-1:** `29:77:F7:5E:1E:B9:19:C1:98:25:77:C4:07:F0:6F:7E:C1:65:8F:B8`

**SHA-256:** `64:FA:A9:DD:6C:DF:0B:5B:1B:0D:D8:9E:02:EB:9C:F5:14:37:F4:B7:D3:48:1B:3B:F1:F7:80:55:EB:57:E8:2A`

**Valid until:** Thursday, December 2, 2055

## Quick Setup Steps

### 1. Go to Google Cloud Console
👉 [https://console.cloud.google.com/](https://console.cloud.google.com/)

### 2. Create or Select Project
- Click "Select a project" → "New Project"
- Name: **ServiceHub** (or any name)
- Click "Create"

### 3. Configure OAuth Consent Screen (First Time Only)
⚠️ **Note**: You don't need to enable any API. OAuth 2.0 credentials are sufficient.
- Go to **"APIs & Services"** → **"OAuth consent screen"**
- Select **"External"** → Click **"Create"**
- Fill required fields:
  - **App name**: `ServiceHub`
  - **User support email**: (your email)
  - **Developer contact email**: (your email)
- Click **"Save and Continue"**
- Skip scopes (click "Save and Continue")
- Add test users if needed (click "Save and Continue")
- Review and go back to dashboard

### 4. Create OAuth 2.0 Client ID
- Go to **"APIs & Services"** → **"Credentials"**
- Click **"+ CREATE CREDENTIALS"** → **"OAuth client ID"**
- Select application type: **"Android"**
- Enter:
  - **Name**: `ServiceHub Android Debug`
  - **Package name**: `app.hub` ⚠️ **Must be exactly this**
  - **SHA-1 certificate fingerprint**: 
    ```
    29:77:F7:5E:1E:B9:19:C1:98:25:77:C4:07:F0:6F:7E:C1:65:8F:B8
    ```
- Click **"CREATE"**

### 5. (Optional) Create Web Client for ID Token
If you want to verify Google ID tokens on your backend:
- Click **"+ CREATE CREDENTIALS"** → **"OAuth client ID"**
- Select application type: **"Web application"**
- **Name**: `ServiceHub Web Client`
- Click **"CREATE"**
- Copy the **Client ID** (looks like: `123456789-xxxxx.apps.googleusercontent.com`)
- Add this to `CreateNewAccountFragment.java`:
  ```java
  .requestIdToken("YOUR_WEB_CLIENT_ID_HERE")
  ```

## Testing

1. **Wait 5-10 minutes** after creating OAuth client (Google needs time to propagate)
2. **Uninstall and reinstall** the app (or clear app data)
3. Click **"Continue with Google"** in the app
4. It should now work! ✅

## Troubleshooting

### Still getting "Developer error"?
- ✅ Wait 10-15 minutes (Google propagation time)
- ✅ Verify package name is exactly: `app.hub`
- ✅ Verify SHA-1 matches exactly (no spaces, correct format)
- ✅ Uninstall and reinstall the app
- ✅ Clear app data: Settings → Apps → ServiceHub → Storage → Clear Data

### Need to add release keystore SHA-1?
When you create a release build, you'll need to:
1. Get SHA-1 from your release keystore
2. Add it to the same OAuth client in Google Cloud Console
3. Or create a separate OAuth client for release builds

---

**Your Setup Info:**
- Package: `app.hub`
- SHA-1: `29:77:F7:5E:1E:B9:19:C1:98:25:77:C4:07:F0:6F:7E:C1:65:8F:B8`
- API Endpoint: `/api/v1/google-signin` (already configured in backend)
