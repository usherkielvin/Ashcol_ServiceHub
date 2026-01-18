# 🚀 Complete Render.com Deployment Guide

## Step-by-Step: Deploy Laravel Backend to Render

---

## 📋 Step 1: Fix Render Configuration (Current Page)

You're on the "Configure" page. Render detected Node.js, but you need **PHP** for Laravel.

### Update These Fields:

1. **Language:** 
   - Change from `Node` → Select **`PHP`** from dropdown

2. **Name:**
   - Keep: `Ashcol_Web` (or change to `ashcol-backend`)

3. **Branch:**
   - Keep: `main` (or your main branch name)

4. **Region:**
   - Keep: `Singapore (Southeast Asia)` (or choose closest to you)

5. **Root Directory:**
   - Leave **EMPTY** (Laravel is in root)

6. **Build Command:**
   - **DELETE** the Node.js command: `$ npm install; npm run build`
   - **REPLACE** with:
     ```
     composer install --optimize-autoloader --no-dev
     ```

7. **Start Command:**
   - **ADD** this (it's probably empty):
     ```
     php artisan serve --host=0.0.0.0 --port=$PORT
     ```

### Click "Advanced" (if visible) and add:
- **Environment:** PHP 8.2 (or latest available)

---

## 📋 Step 2: Add Environment Variables

Before clicking "Create Web Service", click **"Advanced"** or scroll to **"Environment Variables"** section.

### Add These Variables:

```env
APP_NAME=Ashcol_ServiceHub
APP_ENV=production
APP_DEBUG=false
APP_URL=https://ashcol-web.onrender.com
APP_KEY=
```

**For APP_KEY:**
1. **Don't add it yet** - We'll generate it after deployment
2. Or generate now:
   ```bash
   cd C:\xampp\htdocs\Ashcol_Web
   php artisan key:generate --show
   ```
   Copy the output and add as `APP_KEY`

### Add Logging:
```env
LOG_CHANNEL=stack
LOG_LEVEL=error
```

### Add Session/Cache:
```env
SESSION_DRIVER=file
CACHE_DRIVER=file
QUEUE_CONNECTION=sync
```

### Add Mail (if you use email):
```env
MAIL_MAILER=smtp
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_ENCRYPTION=tls
MAIL_FROM_ADDRESS=your-email@gmail.com
MAIL_FROM_NAME=Ashcol_ServiceHub
```

### Add CORS (for Android app):
```env
CORS_ALLOWED_ORIGINS=*
```

**Note:** We'll add database variables after creating the database.

---

## 📋 Step 3: Create Web Service

1. Click **"Create Web Service"** button
2. Render will start building (takes 2-5 minutes)
3. **Wait for build to complete**

---

## 📋 Step 4: Generate APP_KEY

After service is created:

1. Go to your service dashboard
2. Click **"Shell"** tab (or "Logs" → "Shell")
3. Run:
   ```bash
   php artisan key:generate --show
   ```
4. Copy the output (starts with `base64:`)
5. Go to **"Environment"** tab
6. Add/Update `APP_KEY` variable with the copied value
7. Service will auto-restart

---

## 📋 Step 5: Create PostgreSQL Database

1. In Render dashboard, click **"+ New"** → **"PostgreSQL"**

2. **Configure Database:**
   - **Name:** `ashcol-db` (or your choice)
   - **Database:** `ashcol_portal` (or your choice)
   - **User:** (auto-generated)
   - **Region:** Same as your web service (Singapore)
   - **Plan:** Free (Development)
   - Click **"Create Database"**

3. **Wait for database to be ready** (1-2 minutes)

4. **Get Connection Details:**
   - Click on your database
   - Go to **"Connections"** tab
   - You'll see:
     - **Internal Database URL** (for Render services)
     - **External Database URL** (for outside connections)
   
   **Copy the Internal Database URL** - it looks like:
   ```
   postgresql://user:password@host:5432/database
   ```

---

## 📋 Step 6: Connect Database to Web Service

1. Go back to your **Web Service** dashboard
2. Click **"Environment"** tab
3. **Add Database Variables:**

   **Option A: Use Internal Database URL (Easiest)**
   ```env
   DATABASE_URL=postgresql://user:password@host:5432/database
   ```
   (Paste the Internal Database URL you copied)

   **Option B: Use Individual Variables**
   ```env
   DB_CONNECTION=pgsql
   DB_HOST=your-db-host
   DB_PORT=5432
   DB_DATABASE=ashcol_portal
   DB_USERNAME=your-username
   DB_PASSWORD=your-password
   ```

4. **Service will auto-restart** with new database config

---

## 📋 Step 7: Run Migrations

1. In your **Web Service** dashboard
2. Click **"Shell"** tab
3. Run:
   ```bash
   php artisan migrate --force
   ```

   This creates all your tables in PostgreSQL.

4. **If you have seeders:**
   ```bash
   php artisan db:seed
   ```

---

## 📋 Step 8: Test Your Backend

1. **Get your service URL:**
   - In Web Service dashboard
   - URL is shown at top: `https://ashcol-web.onrender.com`

2. **Test API endpoint:**
   Open in browser:
   ```
   https://ashcol-web.onrender.com/api/v1/send-verification-code
   ```

   Should return JSON (even if error - means it's working!)

3. **Test with Postman/curl:**
   ```bash
   curl -X POST https://ashcol-web.onrender.com/api/v1/send-verification-code \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com"}'
   ```

---

## 📋 Step 9: Update Android App

### Edit `ApiClient.java`:

**File:** `app/src/main/java/app/hub/api/ApiClient.java`

**Line 24 - Change:**
```java
// OLD:
private static final String BASE_URL = "http://10.0.2.2:8000/";

// NEW:
private static final String BASE_URL = "https://ashcol-web.onrender.com/api/v1/";
```

**Important:**
- ✅ Use `https://` (not `http://`)
- ✅ Remove `:8000` port
- ✅ Include `/api/v1/` (your routes are prefixed)
- ✅ Keep trailing `/`

### Rebuild APK:
```powershell
cd C:\Users\usher\StudioProjects\Ashcol_ServiceHub
.\gradlew.bat assembleDebug
```

### Install on Device:
- APK location: `app/build/outputs/apk/debug/app-debug.apk`
- Transfer to phone and install
- **Test login/registration** - should work without PC! ✅

---

## 📋 Step 10: Export & Import Existing Data (Optional)

If you have existing users/data in XAMPP:

### Export from XAMPP:
```powershell
cd C:\xampp\htdocs\Ashcol_Web
C:\xampp\mysql\bin\mysqldump.exe -u root -p ashcol_portal > backup.sql
```

### Convert MySQL to PostgreSQL (if needed):
- Use online converter or
- Use `pgloader` tool
- Or manually adjust SQL syntax

### Import to Render PostgreSQL:
1. In Render database dashboard
2. Use **"Connect"** → **"External Connection"**
3. Connect with pgAdmin or DBeaver
4. Import SQL file

**Or use migrations only** (if you don't need old data):
- Just run `php artisan migrate` (already done in Step 7)

---

## 🔧 Configuration Summary

### Render Web Service Settings:
```
Name: ashcol-web
Language: PHP
Branch: main
Region: Singapore
Root Directory: (empty)
Build Command: composer install --optimize-autoloader --no-dev
Start Command: php artisan serve --host=0.0.0.0 --port=$PORT
```

### Environment Variables (All):
```env
APP_NAME=Ashcol_ServiceHub
APP_ENV=production
APP_DEBUG=false
APP_URL=https://ashcol-web.onrender.com
APP_KEY=base64:xxxxx (generate after deployment)

LOG_CHANNEL=stack
LOG_LEVEL=error

SESSION_DRIVER=file
CACHE_DRIVER=file
QUEUE_CONNECTION=sync

DB_CONNECTION=pgsql
DATABASE_URL=postgresql://user:pass@host:5432/db

MAIL_MAILER=smtp
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_ENCRYPTION=tls
MAIL_FROM_ADDRESS=your-email@gmail.com
MAIL_FROM_NAME=Ashcol_ServiceHub

CORS_ALLOWED_ORIGINS=*
```

---

## 🚨 Troubleshooting

### Issue: "Build failed"
**Solution:**
- Check build logs in Render dashboard
- Ensure `composer.json` is in root
- Verify PHP version compatibility

### Issue: "Database connection failed"
**Solution:**
- Check `DATABASE_URL` or DB variables
- Verify database is running (green status)
- Check Laravel logs: `storage/logs/laravel.log`

### Issue: "502 Bad Gateway"
**Solution:**
- Check if service is running (may have spun down)
- Free tier spins down after 15 min inactivity
- First request takes ~30 seconds to wake up
- Check logs for errors

### Issue: "APP_KEY not set"
**Solution:**
- Run `php artisan key:generate` in Shell
- Copy output to `APP_KEY` environment variable

### Issue: "Routes not found"
**Solution:**
- Verify `APP_URL` matches your Render URL
- Check `routes/api.php` has `/v1` prefix
- Test: `https://your-url.onrender.com/api/v1/login`

### Issue: "CORS error in Android"
**Solution:**
- Update `config/cors.php`:
  ```php
  'allowed_origins' => ['*'],
  ```
- Or set specific origins in `.env`:
  ```env
  CORS_ALLOWED_ORIGINS=https://your-url.onrender.com,capacitor://localhost
  ```

---

## ✅ Verification Checklist

After deployment:

- [ ] Web service is running (green status)
- [ ] Database is running (green status)
- [ ] APP_KEY is set
- [ ] Database variables configured
- [ ] Migrations run successfully
- [ ] API endpoint responds (test in browser)
- [ ] Android BASE_URL updated
- [ ] APK rebuilt and installed
- [ ] Login works from Android app
- [ ] Registration works from Android app
- [ ] No PC connection needed! ✅

---

## 🎯 Quick Reference

### Your Render URLs:
- **Web Service:** `https://ashcol-web.onrender.com`
- **API Base:** `https://ashcol-web.onrender.com/api/v1/`
- **Database:** Internal (not accessible externally)

### Android App:
- **BASE_URL:** `https://ashcol-web.onrender.com/api/v1/`

### Test Commands:
```bash
# In Render Shell:
php artisan migrate:status
php artisan tinker
php artisan route:list
```

### Check Logs:
- Render Dashboard → Logs tab
- Or: `storage/logs/laravel.log` (in Shell)

---

## 🎉 Result

After completing all steps:

✅ **Backend online** - Accessible from anywhere  
✅ **Database online** - PostgreSQL on Render  
✅ **Android app works** - No PC needed  
✅ **HTTPS included** - Secure connection  
✅ **Free hosting** - Render free tier  
✅ **Auto-deploy** - Updates from GitHub  

**Your app now works completely independently! 🚀**

---

## 📞 Next Steps

1. ✅ Complete Step 1 (Fix configuration)
2. ✅ Add environment variables
3. ✅ Create service
4. ✅ Create database
5. ✅ Run migrations
6. ✅ Update Android app
7. ✅ Test everything!

**You're ready to deploy! Follow the steps above. 🎯**
