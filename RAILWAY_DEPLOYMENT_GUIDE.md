# 🚂 Complete Railway.app Deployment Guide

## Step-by-Step: Deploy Laravel Backend to Railway

Railway is **easier** than Render for Laravel - it auto-detects PHP and handles everything!

---

## 📋 Step 1: Prepare GitHub Repository

### Make sure your Laravel code is on GitHub:

```powershell
cd C:\xampp\htdocs\Ashcol_Web

# Check if git is initialized
git status

# If not a git repo:
git init
git add .
git commit -m "Initial commit for Railway deployment"

# Create repo on GitHub, then:
git remote add origin https://github.com/YOUR_USERNAME/Ashcol_Web.git
git push -u origin main
```

**Or if already on GitHub:**
- Just make sure it's up to date:
  ```powershell
  git add .
  git commit -m "Prepare for Railway deployment"
  git push origin main
  ```

---

## 📋 Step 2: Sign Up for Railway

1. **Go to:** [railway.app](https://railway.app)
2. **Click:** "Start a New Project"
3. **Sign up with GitHub** (easiest - one click)
4. **Authorize Railway** to access your GitHub repos

---

## 📋 Step 3: Create New Project

1. **Click:** "New Project" (or "+ New Project")
2. **Select:** "Deploy from GitHub repo"
3. **Choose repository:** `Ashcol_Web` (or your repo name)
4. **Click:** "Deploy Now"

Railway will:
- ✅ Auto-detect Laravel/PHP
- ✅ Start building automatically
- ✅ Show build progress

**Wait 2-3 minutes** for initial build.

---

## 📋 Step 4: Add PostgreSQL Database

1. In your project dashboard, click **"+ New"**
2. Select **"Database"** → **"Add PostgreSQL"**
3. Railway creates database automatically
4. **Wait 30 seconds** for database to be ready

**Database is now connected!** Railway automatically adds database variables.

---

## 📋 Step 5: Configure Environment Variables

1. Click on your **Web Service** (the Laravel app)
2. Go to **"Variables"** tab
3. Railway already added database variables ✅
4. **Add these additional variables:**

### Required Variables:

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

### Get Your App URL:

1. Click on your **Web Service**
2. Go to **"Settings"** tab
3. Scroll to **"Domains"**
4. Copy the Railway domain (e.g., `ashcol-web-production.up.railway.app`)
5. Use it for `APP_URL`:
   ```env
   APP_URL=https://ashcol-web-production.up.railway.app
   ```

---

## 📋 Step 6: Generate APP_KEY

1. Click on your **Web Service**
2. Go to **"Deployments"** tab
3. Click on the latest deployment
4. Click **"View Logs"** or **"Shell"** tab
5. Run:
   ```bash
   php artisan key:generate --show
   ```
6. Copy the output (starts with `base64:`)
7. Go back to **"Variables"** tab
8. **Add new variable:**
   - **Key:** `APP_KEY`
   - **Value:** (paste the copied key)
9. Service will auto-restart

**Or use Railway CLI:**
```bash
# Install Railway CLI first
railway variables set APP_KEY="base64:xxxxx"
```

---

## 📋 Step 7: Run Migrations

1. In your **Web Service** dashboard
2. Go to **"Deployments"** → **"Shell"** tab
3. Run:
   ```bash
   php artisan migrate --force
   ```

**This creates all your tables in PostgreSQL!**

4. **If you have seeders:**
   ```bash
   php artisan db:seed
   ```

---

## 📋 Step 8: Configure Build Settings (If Needed)

Railway usually auto-detects everything, but check:

1. **Web Service** → **"Settings"** tab
2. **Build Command:** Should be:
   ```
   composer install --optimize-autoloader --no-dev
   ```
   (Railway auto-detects this, but verify)

3. **Start Command:** Should be:
   ```
   php artisan serve --host=0.0.0.0 --port=$PORT
   ```
   (Railway auto-detects this too)

**Usually you don't need to change anything!** ✅

---

## 📋 Step 9: Test Your Backend

1. **Get your Railway URL:**
   - Web Service → Settings → Domains
   - Copy the URL (e.g., `https://ashcol-web-production.up.railway.app`)

2. **Test API endpoint:**
   Open in browser:
   ```
   https://your-url.up.railway.app/api/v1/send-verification-code
   ```

   Should return JSON (even if error - means it's working!)

3. **Test with curl:**
   ```bash
   curl -X POST https://your-url.up.railway.app/api/v1/send-verification-code \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com"}'
   ```

---

## 📋 Step 10: Update Android App

### Edit `ApiClient.java`:

**File:** `app/src/main/java/app/hub/api/ApiClient.java`

**Line 24 - Change:**
```java
// OLD:
private static final String BASE_URL = "http://10.0.2.2:8000/";

// NEW (use your Railway URL):
private static final String BASE_URL = "https://ashcol-web-production.up.railway.app/api/v1/";
```

**Important:**
- ✅ Use `https://` (not `http://`)
- ✅ Remove `:8000` port
- ✅ Include `/api/v1/` (your routes are prefixed)
- ✅ Keep trailing `/`
- ✅ Replace `ashcol-web-production.up.railway.app` with YOUR actual Railway URL

### Rebuild APK:
```powershell
cd C:\Users\usher\StudioProjects\Ashcol_ServiceHub
.\gradlew.bat assembleDebug
```

### Install on Device:
- APK: `app/build/outputs/apk/debug/app-debug.apk`
- Transfer to phone and install
- **Test login/registration** - should work without PC! ✅

---

## 📋 Step 11: Export & Import Existing Data (Optional)

If you have existing users/data in XAMPP:

### Export from XAMPP:
```powershell
cd C:\xampp\htdocs\Ashcol_Web
C:\xampp\mysql\bin\mysqldump.exe -u root -p ashcol_portal > backup.sql
```

### Convert MySQL to PostgreSQL:
Railway uses PostgreSQL. You have options:

**Option A: Use migrations only** (if you don't need old data):
- Just run `php artisan migrate` (already done in Step 7)
- Start fresh with new database

**Option B: Convert and import:**
1. Use online converter: [mysql-to-postgresql.com](https://www.mysql-to-postgresql.com)
2. Or use `pgloader` tool
3. Connect to Railway database and import

**Option C: Use Railway CLI:**
```bash
# Connect to Railway database
railway connect postgres

# Import SQL file
psql < backup_converted.sql
```

---

## 🔧 Railway Configuration Summary

### Auto-Detected (You don't need to set):
- ✅ PHP version (8.2+)
- ✅ Build command (`composer install`)
- ✅ Start command (`php artisan serve`)
- ✅ Database connection (automatic)

### You Need to Set:
- ✅ Environment variables (Step 5)
- ✅ APP_KEY (Step 6)
- ✅ Run migrations (Step 7)

---

## 🎯 Quick Reference

### Your Railway URLs:
- **Web Service:** `https://your-app-name.up.railway.app`
- **API Base:** `https://your-app-name.up.railway.app/api/v1/`
- **Database:** Internal (not accessible externally)

### Android App:
- **BASE_URL:** `https://your-app-name.up.railway.app/api/v1/`

### Railway Dashboard:
- **Project:** [railway.app/dashboard](https://railway.app/dashboard)
- **Logs:** View in deployment logs
- **Shell:** Access via deployments tab

---

## 🚨 Troubleshooting

### Issue: "Build failed"
**Solution:**
- Check deployment logs in Railway
- Verify `composer.json` exists
- Check for PHP version compatibility

### Issue: "Database connection failed"
**Solution:**
- Railway auto-connects database
- Check if database is running (green status)
- Verify environment variables are set

### Issue: "APP_KEY not set"
**Solution:**
- Run `php artisan key:generate --show` in Shell
- Add output to `APP_KEY` variable
- Service will restart automatically

### Issue: "Routes not found"
**Solution:**
- Verify `APP_URL` matches your Railway URL
- Check `routes/api.php` has `/v1` prefix
- Test: `https://your-url.up.railway.app/api/v1/login`

### Issue: "CORS error in Android"
**Solution:**
- Update `config/cors.php`:
  ```php
  'allowed_origins' => ['*'],
  ```
- Or set specific origins in variables:
  ```env
  CORS_ALLOWED_ORIGINS=https://your-url.up.railway.app,capacitor://localhost
  ```

### Issue: "Service keeps restarting"
**Solution:**
- Check logs for errors
- Verify all required environment variables are set
- Check database connection

---

## ✅ Verification Checklist

After deployment:

- [ ] Project created on Railway
- [ ] Web service deployed (green status)
- [ ] PostgreSQL database created (green status)
- [ ] Environment variables configured
- [ ] APP_KEY generated and set
- [ ] Migrations run successfully
- [ ] API endpoint responds (test in browser)
- [ ] Android BASE_URL updated
- [ ] APK rebuilt and installed
- [ ] Login works from Android app
- [ ] Registration works from Android app
- [ ] No PC connection needed! ✅

---

## 💰 Railway Pricing

### Free Tier:
- ✅ $5 free credit monthly
- ✅ 500 hours runtime
- ✅ 1GB storage
- ✅ Perfect for testing/small apps

### Paid Plans:
- **Starter:** $5/month (more resources)
- **Developer:** $20/month (production-ready)

**For your app, free tier should be enough!** ✅

---

## 🎯 Complete Environment Variables List

Add all these in Railway → Variables:

```env
# App
APP_NAME=Ashcol_ServiceHub
APP_ENV=production
APP_DEBUG=false
APP_URL=https://your-app-name.up.railway.app
APP_KEY=base64:xxxxx (generate in Step 6)

# Logging
LOG_CHANNEL=stack
LOG_LEVEL=error

# Cache/Session
SESSION_DRIVER=file
CACHE_DRIVER=file
QUEUE_CONNECTION=sync

# Database (Railway auto-adds these, but verify):
DATABASE_URL=postgresql://user:pass@host:5432/db
# OR individual:
DB_CONNECTION=pgsql
DB_HOST=xxx
DB_PORT=5432
DB_DATABASE=xxx
DB_USERNAME=xxx
DB_PASSWORD=xxx

# Mail (if you use email):
MAIL_MAILER=smtp
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_ENCRYPTION=tls
MAIL_FROM_ADDRESS=your-email@gmail.com
MAIL_FROM_NAME=Ashcol_ServiceHub

# CORS
CORS_ALLOWED_ORIGINS=*
```

---

## 🎉 Result

After completing all steps:

✅ **Backend online** - Accessible from anywhere  
✅ **Database online** - PostgreSQL on Railway  
✅ **Android app works** - No PC needed  
✅ **HTTPS included** - Secure connection  
✅ **Free hosting** - Railway free tier  
✅ **Auto-deploy** - Updates from GitHub  
✅ **Easy management** - Simple dashboard  

**Your app now works completely independently! 🚀**

---

## 📞 Next Steps

1. ✅ Sign up for Railway
2. ✅ Connect GitHub repo
3. ✅ Deploy web service
4. ✅ Add PostgreSQL database
5. ✅ Configure environment variables
6. ✅ Generate APP_KEY
7. ✅ Run migrations
8. ✅ Update Android app
9. ✅ Test everything!

**You're ready to deploy! Follow the steps above. 🎯**

---

## 🔗 Useful Links

- **Railway Dashboard:** [railway.app/dashboard](https://railway.app/dashboard)
- **Railway Docs:** [docs.railway.app](https://docs.railway.app)
- **Railway CLI:** [docs.railway.app/develop/cli](https://docs.railway.app/develop/cli)

---

## 💡 Pro Tips

1. **Custom Domain:** Railway allows custom domains (free tier)
2. **Environment Variables:** Use Railway's variable management (easier than .env)
3. **Logs:** View real-time logs in Railway dashboard
4. **Rollback:** Easy rollback to previous deployments
5. **Monitoring:** Railway shows resource usage in dashboard

**Railway is the easiest option for Laravel! 🚂**
