# 🔧 Complete Railway Fix - Step by Step

## Let's Fix Everything Systematically:

---

## ✅ Step 1: Verify Service is Running

1. **Go to Railway dashboard**
2. **Click on your Web Service** (`Ashcol_Web`)
3. **Check "Deployments" tab:**
   - Latest deployment should be **green** ✅
   - If **red** ❌, click on it and check logs

**If deployment failed, share the error message!**

---

## ✅ Step 2: Fix Environment Variables

### Remove ALL Variables and Re-Add (One by One):

1. Railway → Web Service → **Variables** tab
2. **Delete all existing variables** (if any have quotes)
3. **Add these EXACTLY as shown** (NO quotes):

```
APP_NAME=Ashcol_ServiceHub
APP_ENV=production
APP_DEBUG=false
APP_URL=https://k0cn5non.up.railway.app
LOG_CHANNEL=stack
LOG_LEVEL=error
SESSION_DRIVER=file
CACHE_DRIVER=file
QUEUE_CONNECTION=sync
CORS_ALLOWED_ORIGINS=*
APP_KEY=base64:YCeKZLH+CK2U/ULxsHehFrUMpBcxLugk6HlJPuWtDCA=
```

**Important:**
- ✅ NO quotes around values
- ✅ NO spaces around `=`
- ✅ Add one at a time

---

## ✅ Step 3: Verify Database

1. **Check if PostgreSQL exists:**
   - Railway dashboard → Look for **PostgreSQL** service
   - Should be separate from Web Service

2. **If missing, create it:**
   - Click **"+ New"** → **"Database"** → **"Add PostgreSQL"**
   - Wait 30 seconds
   - Railway auto-connects it

3. **Verify database variables:**
   - Railway auto-adds: `DATABASE_URL` or `PGHOST`, etc.
   - Check Variables tab - should see database variables

---

## ✅ Step 4: Run Migrations (Local Method)

Since Railway Shell might not be available, run migrations locally:

### Get Railway Database Credentials:

1. Railway → PostgreSQL service → **Variables** tab
2. Copy these values:
   - `PGHOST` (or from `DATABASE_URL`)
   - `PGDATABASE`
   - `PGUSER`
   - `PGPASSWORD`
   - `PGPORT` (usually 5432)

### Update Local `.env`:

Edit `C:\xampp\htdocs\Ashcol_Web\.env`:

```env
DB_CONNECTION=pgsql
DB_HOST=your-pghost-value
DB_PORT=5432
DB_DATABASE=your-pgdatabase-value
DB_USERNAME=your-pguser-value
DB_PASSWORD=your-pgpassword-value
```

### Run Migrations:

```powershell
cd C:\xampp\htdocs\Ashcol_Web
php artisan migrate --force
```

**This creates tables in Railway database!**

---

## ✅ Step 5: Test API

### Test in Browser:

Open:
```
https://k0cn5non.up.railway.app/api/v1/send-verification-code
```

**Expected Results:**
- ✅ **JSON response** (even if error) = API is working!
- ❌ **404 Not Found** = Routes not configured
- ❌ **500 Error** = Check logs for specific error
- ❌ **Connection refused** = Service not running

### Test with POST Request:

Use Postman or curl:
```bash
curl -X POST https://k0cn5non.up.railway.app/api/v1/send-verification-code \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'
```

---

## ✅ Step 6: Check Railway Logs

1. Railway → Web Service → **Deployments** tab
2. Click **latest deployment**
3. Click **"Logs"** tab
4. **Look for errors:**
   - "APP_KEY not set" → Add APP_KEY
   - "Database connection failed" → Check DB variables
   - "Route not found" → Check APP_URL
   - "Class not found" → Check deployment logs

**Share the error message if you see one!**

---

## ✅ Step 7: Verify Routes

Check if routes are accessible:

1. **Test root:**
   ```
   https://k0cn5non.up.railway.app/
   ```
   Should show Laravel welcome or error (not 404)

2. **Test API:**
   ```
   https://k0cn5non.up.railway.app/api/v1/login
   ```
   Should return JSON

---

## 🚨 Common Issues & Quick Fixes:

### Issue: "APP_KEY not set"
**Fix:** Add `APP_KEY=base64:YCeKZLH+CK2U/ULxsHehFrUMpBcxLugk6HlJPuWtDCA=` to Variables

### Issue: "Database connection failed"
**Fix:** 
1. Create PostgreSQL service
2. Verify database variables exist
3. Run migrations

### Issue: "404 Not Found"
**Fix:**
1. Check `APP_URL=https://k0cn5non.up.railway.app` (no trailing slash)
2. Verify routes in `routes/api.php`
3. Check Laravel logs

### Issue: "500 Internal Server Error"
**Fix:**
1. Check Railway logs for specific error
2. Verify all environment variables are set
3. Check database connection

---

## 📋 Complete Checklist:

- [ ] Service is running (green deployment)
- [ ] All environment variables added (no quotes)
- [ ] APP_KEY is set
- [ ] PostgreSQL database exists
- [ ] Database variables are present
- [ ] Migrations run successfully
- [ ] API endpoint responds (test in browser)
- [ ] No errors in Railway logs

---

## 🎯 What to Tell Me:

**Please share:**
1. What happens when you open: `https://k0cn5non.up.railway.app/api/v1/send-verification-code`
2. Any error messages from Railway logs
3. Screenshot of Variables tab (if possible)
4. Status of PostgreSQL service (exists or not)

**I'll help fix the specific issue!**
