# Quick Fix: Google Sign-In Developer Error

If you're seeing **"Developer error - check config"** when clicking "Continue with Google", follow these steps:

## Step 1: Get Your SHA-1 Fingerprint

### Method 1: Using Gradle (Easiest)
Open **PowerShell** or **Command Prompt** in the project root:

```powershell
cd C:\Users\usher\StudioProjects\Ashcol_ServiceHub
.\gradlew signingReport
```

Look for **Variant: debug** section and copy the **SHA1** value:

**Your SHA-1:** `29:77:F7:5E:1E:B9:19:C1:98:25:77:C4:07:F0:6F:7E:C1:65:8F:B8`

Example output:
```
Variant: debug
Config: debug
Store: C:\Users\usher\.android\debug.keystore
Alias: AndroidDebugKey
SHA1: 29:77:F7:5E:1E:B9:19:C1:98:25:77:C4:07:F0:6F:7E:C1:65:8F:B8 ← USE THIS
```

### Method 2: Using keytool (If Gradle doesn't work)
```powershell
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

Copy the **SHA1** value.

## Step 2: Configure Google Cloud Console

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. **Create a new project** (or select existing):
   - Click "Select a project" → "New Project"
   - Name: "ServiceHub" (or any name)
   - Click "Create"

3. **Create OAuth 2.0 Client ID**:
   - ⚠️ **Note**: You don't need to enable any API. OAuth 2.0 credentials are sufficient.
   - Go to "APIs & Services" → "Credentials"
   - Click **"+ CREATE CREDENTIALS"** → **"OAuth client ID"**
   - If prompted, configure OAuth consent screen first (select "External", fill required fields)
   - Select application type: **"Android"**
   - Enter:
     - **Name**: `ServiceHub Android`
     - **Package name**: `app.hub` (exactly as shown)
     - **SHA-1 certificate fingerprint**: 
       ```
       29:77:F7:5E:1E:B9:19:C1:98:25:77:C4:07:F0:6F:7E:C1:65:8F:B8
       ```
       (This is your SHA-1 from Step 1)
   - Click **"CREATE"**

5. **Done!** The Client ID is automatically configured. No need to copy it.

## Step 3: Test Again

1. Close and reopen your app (or restart the activity)
2. Click "Continue with Google"
3. It should now work!

## Important Notes

- ✅ **Package name must be exactly**: `app.hub`
- ✅ **SHA-1 must match** your debug keystore
- ✅ **Wait 5-10 minutes** after creating OAuth client (Google needs to propagate changes)
- ✅ For **release builds**, you'll need to add your release keystore's SHA-1 too

## Multiple Developers/Devices

### ⚠️ Important: Each PC/Developer Needs Their Own SHA-1

**The SHA-1 you have (`29:77:F7:5E:...`) is specific to YOUR machine's debug keystore.**

### For Other Developers/PCs:

1. **Each developer must:**
   - Get their own SHA-1 fingerprint from their machine
   - Run: `.\gradlew signingReport` on their PC
   - Copy their SHA-1 value

2. **Add Multiple SHA-1s to Same OAuth Client:**
   - Go to Google Cloud Console → Credentials
   - Click on your "ServiceHub Android" OAuth client
   - Click **"ADD SHA-1 CERTIFICATE FINGERPRINT"**
   - Add each developer's SHA-1
   - Click **"SAVE"**

3. **Or Create Separate OAuth Clients:**
   - Create one OAuth client per developer
   - Each with their own SHA-1
   - All using the same package name: `app.hub`

### For Physical Devices:

- **Physical devices use the same SHA-1 as the PC that builds the app**
- If you build on PC A → install on Device → uses PC A's SHA-1
- If you build on PC B → install on Device → uses PC B's SHA-1
- **Solution**: Add all developers' SHA-1s to the same OAuth client

### Best Practice for Teams:

**Option 1: Shared Keystore (Recommended for Teams)**
- Create a shared debug keystore file
- All developers use the same keystore
- Only one SHA-1 needed
- Store keystore in secure location (password-protected)

**Option 2: Multiple SHA-1s (Easier for Small Teams)**
- Add all team members' SHA-1s to one OAuth client
- Each developer uses their own keystore
- Works immediately for everyone

**Option 3: Separate OAuth Clients**
- Each developer has their own OAuth client
- More management overhead
- Not recommended for teams

## Still Not Working?

1. **Wait 10 minutes** after configuring (Google's servers need time)
2. **Uninstall and reinstall** the app
3. **Clear app data**:
   - Settings → Apps → ServiceHub → Storage → Clear Data
4. **Check package name** matches exactly: `app.hub`
5. **Verify SHA-1** is correct for your keystore

## Alternative: Use Email Registration

Until Google Sign-In is configured, users can:
- Click **"Continue with Email"** instead
- Complete the regular registration flow

The app will automatically show an error dialog with "Use Email Instead" button as a fallback.

---

**Need help?** See `GOOGLE_SIGNIN_SETUP.md` for detailed instructions.
