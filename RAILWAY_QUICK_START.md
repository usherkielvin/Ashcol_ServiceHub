# ⚡ Railway Quick Start Guide

## 🚀 Fast Deployment (5 Steps)

---

## Step 1: Sign Up & Connect GitHub

1. Go to [railway.app](https://railway.app)
2. Click **"Start a New Project"**
3. **Sign up with GitHub** (one click)
4. Authorize Railway

---

## Step 2: Deploy from GitHub

1. **"New Project"** → **"Deploy from GitHub repo"**
2. Select **`Ashcol_Web`** repository
3. Click **"Deploy Now"**
4. **Wait 2-3 minutes** - Railway auto-detects Laravel!

---

## Step 3: Add Database

1. Click **"+ New"** → **"Database"** → **"Add PostgreSQL"**
2. **Wait 30 seconds** - Database auto-connects!

---

## Step 4: Add Environment Variables

1. Click on **Web Service** → **"Variables"** tab
2. **Add these:**

```env
APP_NAME=Ashcol_ServiceHub
APP_ENV=production
APP_DEBUG=false
APP_URL=https://your-app-name.up.railway.app
LOG_CHANNEL=stack
LOG_LEVEL=error
SESSION_DRIVER=file
CACHE_DRIVER=file
QUEUE_CONNECTION=sync
CORS_ALLOWED_ORIGINS=*
```

3. **Get APP_KEY:**
   - Go to **"Deployments"** → **"Shell"**
   - Run: `php artisan key:generate --show`
   - Copy output
   - Add as `APP_KEY` variable

4. **Get APP_URL:**
   - Go to **"Settings"** → **"Domains"**
   - Copy Railway URL
   - Update `APP_URL` variable

---

## Step 5: Run Migrations

1. **"Deployments"** → **"Shell"**
2. Run: `php artisan migrate --force`

---

## ✅ Done!

**Your backend is now online!**

### Update Android App:

**File:** `app/src/main/java/app/hub/api/ApiClient.java`

**Line 24:**
```java
private static final String BASE_URL = "https://your-app-name.up.railway.app/api/v1/";
```

**Rebuild APK:**
```powershell
.\gradlew.bat assembleDebug
```

---

## 🎯 That's It!

Railway is **much easier** than Render for Laravel:
- ✅ Auto-detects PHP/Laravel
- ✅ Auto-connects database
- ✅ No Dockerfile needed
- ✅ Simple dashboard

**See `RAILWAY_DEPLOYMENT_GUIDE.md` for detailed steps!**
