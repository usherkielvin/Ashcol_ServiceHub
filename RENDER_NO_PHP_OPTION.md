# 🔧 Render: No PHP Option - Solution

## Problem
Render is detecting Node.js (because of `package.json`) and not showing PHP option.

## ✅ Solution: Use Docker Instead

Since PHP isn't showing in the dropdown, we'll use **Docker** which gives us full control.

---

## 📋 Step 1: Create Dockerfile

I've created `Dockerfile` in your Laravel root (`C:\xampp\htdocs\Ashcol_Web\Dockerfile`).

**Already created!** ✅

---

## 📋 Step 2: Update Render Configuration

### On the "Configure" page:

1. **Language:**
   - Select **"Docker"** from dropdown (instead of PHP)

2. **Name:**
   - Keep: `Ashcol_Web` (or `ashcol-backend`)

3. **Branch:**
   - Keep: `main`

4. **Region:**
   - Keep: `Singapore` (or closest)

5. **Root Directory:**
   - Leave **EMPTY**

6. **Build Command:**
   - **DELETE** the Node.js command
   - **LEAVE EMPTY** (Dockerfile handles it automatically)

7. **Start Command:**
   - **LEAVE EMPTY** (Dockerfile handles it automatically)

8. **Dockerfile Path:**
   - Should auto-detect `Dockerfile`
   - If not, enter: `Dockerfile`

---

## 📋 Step 3: Add Environment Variables

Click **"Advanced"** → **"Environment Variables"**:

```env
APP_NAME=Ashcol_ServiceHub
APP_ENV=production
APP_DEBUG=false
APP_URL=https://ashcol-web.onrender.com
LOG_CHANNEL=stack
LOG_LEVEL=error
SESSION_DRIVER=file
CACHE_DRIVER=file
QUEUE_CONNECTION=sync
CORS_ALLOWED_ORIGINS=*
```

**Note:** We'll add `APP_KEY` and database after deployment.

---

## 📋 Step 4: Commit Dockerfile to GitHub

```powershell
cd C:\xampp\htdocs\Ashcol_Web
git add Dockerfile
git commit -m "Add Dockerfile for Render deployment"
git push origin main
```

---

## 📋 Step 5: Create Web Service

1. Click **"Create Web Service"**
2. Render will:
   - Detect Dockerfile
   - Build Docker image
   - Deploy your Laravel app
3. Wait 3-5 minutes for build

---

## 📋 Step 6: Generate APP_KEY

After service is created:

1. Go to service dashboard
2. Click **"Shell"** tab
3. Run:
   ```bash
   php artisan key:generate --show
   ```
4. Copy the output
5. Go to **"Environment"** tab
6. Add `APP_KEY` variable with copied value

---

## 📋 Step 7: Create Database

1. In Render dashboard: **"+ New"** → **"PostgreSQL"**
2. **Name:** `ashcol-db`
3. **Plan:** Free
4. Click **"Create Database"**
5. Wait 1-2 minutes

---

## 📋 Step 8: Connect Database

1. Go to **Web Service** → **"Environment"** tab
2. Add database variables:

   **Option A: Use Internal Database URL**
   ```env
   DATABASE_URL=postgresql://user:password@host:5432/database
   ```
   (Copy from database "Connections" tab)

   **Option B: Individual Variables**
   ```env
   DB_CONNECTION=pgsql
   DB_HOST=your-db-host
   DB_PORT=5432
   DB_DATABASE=ashcol_portal
   DB_USERNAME=your-username
   DB_PASSWORD=your-password
   ```

3. Service will auto-restart

---

## 📋 Step 9: Run Migrations

1. Web Service → **"Shell"** tab
2. Run:
   ```bash
   php artisan migrate --force
   ```

---

## 📋 Step 10: Update Android App

Edit `ApiClient.java`:

**File:** `app/src/main/java/app/hub/api/ApiClient.java`

**Line 24:**
```java
// OLD:
private static final String BASE_URL = "http://10.0.2.2:8000/";

// NEW:
private static final String BASE_URL = "https://ashcol-web.onrender.com/api/v1/";
```

**Rebuild APK:**
```powershell
cd C:\Users\usher\StudioProjects\Ashcol_ServiceHub
.\gradlew.bat assembleDebug
```

---

## 🎯 Alternative: Use Railway.app (Easier)

If Docker seems complex, **Railway.app** has better PHP support:

1. **Sign up:** [railway.app](https://railway.app)
2. **New Project** → **"Deploy from GitHub"**
3. Select your repo
4. Railway auto-detects Laravel
5. Add PostgreSQL database
6. Done!

**Railway is simpler** - no Dockerfile needed!

---

## 🎯 Alternative: Use Heroku (Classic)

Heroku has excellent PHP support:

1. **Sign up:** [heroku.com](https://heroku.com)
2. **Create app**
3. **Add buildpack:** `heroku/php`
4. **Deploy from GitHub**
5. **Add PostgreSQL addon**

**Note:** Heroku free tier ended, but paid plans start at $7/month.

---

## 🎯 Alternative: Use 000webhost (Free PHP Hosting)

If you want free PHP hosting without Docker:

1. **Sign up:** [000webhost.com](https://000webhost.com)
2. **Create website**
3. **Upload files via FTP** (FileZilla)
4. **Configure database** (they provide MySQL)
5. **Update Android BASE_URL**

**Note:** Less control, but free and simple.

---

## ✅ Summary

**For Render (Current):**
1. ✅ Dockerfile created
2. Select **"Docker"** as language**
3. Leave Build/Start commands empty
4. Add environment variables
5. Create service
6. Follow steps 6-10 above

**Or use Railway.app** - easier, better PHP support!

---

## 🚨 Troubleshooting

### Issue: "Docker build failed"
**Solution:**
- Check Dockerfile syntax
- Verify `composer.json` exists
- Check build logs in Render

### Issue: "Port binding error"
**Solution:**
- Dockerfile uses port 80
- Render maps to `$PORT` automatically
- Should work without changes

### Issue: "Still detecting Node"
**Solution:**
- Use Docker option (bypasses auto-detection)
- Or delete `package.json` temporarily (not recommended)

---

## 📖 Next Steps

1. ✅ Dockerfile is ready
2. ✅ Commit to GitHub
3. ✅ Select "Docker" in Render
4. ✅ Create service
5. ✅ Follow deployment steps

**You're all set! Use Docker option in Render. 🚀**
