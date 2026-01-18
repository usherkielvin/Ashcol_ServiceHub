# ✅ Railway Deployment Online - Next Steps

## Your backend is now deployed! Here's what to do next:

---

## 📋 Step 1: Get Your Railway URL

1. In Railway dashboard, click on your **Web Service** (`Ashcol_Web`)
2. Go to **"Settings"** tab
3. Scroll to **"Domains"** section
4. Copy your Railway URL (e.g., `ashcol-web-production.up.railway.app`)
5. **Save this URL** - you'll need it!

**Your API base URL will be:**
```
https://your-url.up.railway.app/api/v1/
```

---

## 📋 Step 2: Add Environment Variables

1. In Railway dashboard, click on your **Web Service**
2. Go to **"Variables"** tab
3. **Add these variables:**

### Required Variables:

```env
APP_NAME=Ashcol_ServiceHub
APP_ENV=production
APP_DEBUG=false
APP_URL=https://your-url.up.railway.app
LOG_CHANNEL=stack
LOG_LEVEL=error
SESSION_DRIVER=file
CACHE_DRIVER=file
QUEUE_CONNECTION=sync
CORS_ALLOWED_ORIGINS=*
```

**Replace `your-url.up.railway.app` with your actual Railway URL from Step 1!**

---

## 📋 Step 3: Generate APP_KEY

1. In Railway dashboard, go to **"Deployments"** tab
2. Click on the latest deployment
3. Click **"Shell"** tab (or "View Logs" → "Shell")
4. Run this command:
   ```bash
   php artisan key:generate --show
   ```
5. **Copy the output** (starts with `base64:`)
6. Go back to **"Variables"** tab
7. **Add new variable:**
   - **Key:** `APP_KEY`
   - **Value:** (paste the copied key)
8. Service will **auto-restart** with new key

---

## 📋 Step 4: Verify Database Connection

Railway should have auto-connected the PostgreSQL database. Check:

1. In Railway dashboard, you should see a **PostgreSQL** service
2. If not, click **"+ New"** → **"Database"** → **"Add PostgreSQL"**
3. Railway automatically adds database variables to your web service

**Database variables should already be set** (Railway does this automatically):
- `DATABASE_URL` or
- `DB_HOST`, `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD`

---

## 📋 Step 5: Run Migrations

1. In Railway dashboard, go to **"Deployments"** → **"Shell"** tab
2. Run:
   ```bash
   php artisan migrate --force
   ```

   This creates all your database tables!

3. **If you have seeders** (optional):
   ```bash
   php artisan db:seed
   ```

---

## 📋 Step 6: Test Your API

### Test in Browser:

Open this URL (replace with your Railway URL):
```
https://your-url.up.railway.app/api/v1/send-verification-code
```

**Expected:** Should return JSON (even if error - means API is working!)

### Test with curl (optional):

```bash
curl -X POST https://your-url.up.railway.app/api/v1/send-verification-code \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'
```

**Expected:** JSON response with success/error message

---

## 📋 Step 7: Update Android App

### Edit `ApiClient.java`:

**File:** `app/src/main/java/app/hub/api/ApiClient.java`

**Line 24 - Change:**
```java
// OLD:
private static final String BASE_URL = "http://10.0.2.2:8000/";

// NEW (use your Railway URL):
private static final String BASE_URL = "https://your-url.up.railway.app/api/v1/";
```

**Important:**
- ✅ Replace `your-url.up.railway.app` with your actual Railway URL
- ✅ Use `https://` (not `http://`)
- ✅ Include `/api/v1/` (your routes are prefixed)
- ✅ Keep trailing `/`

### Example:
```java
private static final String BASE_URL = "https://ashcol-web-production.up.railway.app/api/v1/";
```

---

## 📋 Step 8: Rebuild Android APK

```powershell
cd C:\Users\usher\StudioProjects\Ashcol_ServiceHub
.\gradlew.bat assembleDebug
```

**APK location:**
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📋 Step 9: Install & Test on Device

1. **Transfer APK to your phone** (via USB, email, or cloud)
2. **Install APK** on your device
3. **Open the app**
4. **Test registration:**
   - Enter email
   - Fill personal info
   - Create password
   - OTP should be sent (check backend logs if needed)
   - Enter OTP
   - Account should be created!
5. **Test login:**
   - Use email and password
   - Should log in successfully! ✅

---

## ✅ Verification Checklist

After completing all steps:

- [ ] Railway URL copied
- [ ] Environment variables added
- [ ] APP_KEY generated and set
- [ ] Database connected (auto by Railway)
- [ ] Migrations run successfully
- [ ] API endpoint responds (tested in browser)
- [ ] Android BASE_URL updated
- [ ] APK rebuilt
- [ ] App installed on device
- [ ] Registration works
- [ ] Login works
- [ ] **No PC connection needed!** ✅

---

## 🎉 Result

Your app now:
- ✅ **Works without PC** - Backend is online
- ✅ **Works from anywhere** - Any device, any network
- ✅ **Secure** - HTTPS included
- ✅ **Scalable** - Railway handles traffic
- ✅ **Auto-deploys** - Updates from GitHub

---

## 🚨 Troubleshooting

### Issue: "API returns 404"
**Solution:**
- Check `APP_URL` matches your Railway URL
- Verify routes are prefixed with `/api/v1/`
- Test: `https://your-url.up.railway.app/api/v1/login`

### Issue: "Database connection failed"
**Solution:**
- Check PostgreSQL service is running (green status)
- Verify database variables are set
- Check logs in Railway dashboard

### Issue: "CORS error in Android"
**Solution:**
- Verify `CORS_ALLOWED_ORIGINS=*` is set
- Or update `config/cors.php` in Laravel

### Issue: "APP_KEY not set"
**Solution:**
- Run `php artisan key:generate --show` in Shell
- Add output to `APP_KEY` variable
- Service will restart

---

## 📊 Quick Reference

### Your URLs:
- **Web Service:** `https://your-url.up.railway.app`
- **API Base:** `https://your-url.up.railway.app/api/v1/`

### Android App:
- **BASE_URL:** `https://your-url.up.railway.app/api/v1/`

### Railway Dashboard:
- **Project:** [railway.app/dashboard](https://railway.app/dashboard)
- **Logs:** View in deployments
- **Shell:** Access via deployments tab

---

## 🎯 Summary

**You're almost done! Just:**

1. ✅ Get Railway URL
2. ✅ Add environment variables
3. ✅ Generate APP_KEY
4. ✅ Run migrations
5. ✅ Test API
6. ✅ Update Android app
7. ✅ Rebuild APK
8. ✅ Test on device

**Follow steps 1-9 above and you're done! 🚀**
