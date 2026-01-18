# 🔧 Railway Troubleshooting - Fix All Issues

## Common Issues & Solutions:

---

## ❌ Issue 1: API Returns 404 or Error

### Check These:

1. **Verify APP_URL matches Railway domain:**
   - Railway → Web Service → Variables
   - `APP_URL` should be: `https://k0cn5non.up.railway.app`
   - **NO trailing slash!**

2. **Check if service is running:**
   - Railway → Deployments tab
   - Latest deployment should be **green** (successful)
   - If red/failed, check logs

3. **Test API endpoint:**
   ```
   https://k0cn5non.up.railway.app/api/v1/send-verification-code
   ```
   - Should return JSON (even if error message)

---

## ❌ Issue 2: Database Connection Failed

### Solution:

1. **Check if PostgreSQL exists:**
   - Railway dashboard → Look for PostgreSQL service
   - If missing, create it: "+ New" → "Database" → "PostgreSQL"

2. **Verify database variables:**
   - Railway auto-adds: `DATABASE_URL` or `PGHOST`, `PGDATABASE`, etc.
   - Check Variables tab - should see database variables

3. **Run migrations:**
   - If Shell available: `php artisan migrate --force`
   - If not, use Railway CLI or run locally (see below)

---

## ❌ Issue 3: APP_KEY Not Set

### Solution:

1. **Check Variables tab:**
   - Should have `APP_KEY=base64:YCeKZLH+CK2U/ULxsHehFrUMpBcxLugk6HlJPuWtDCA=`
   - **NO quotes around value!**

2. **If missing, add it:**
   - Key: `APP_KEY`
   - Value: `base64:YCeKZLH+CK2U/ULxsHehFrUMpBcxLugk6HlJPuWtDCA=`

---

## ❌ Issue 4: Can't Run Migrations (No Shell)

### Solution: Run Migrations Locally

Since Railway Shell might not be available, run migrations locally and they'll sync:

1. **Update local `.env` to use Railway database:**
   - Get Railway database connection string
   - Railway → PostgreSQL service → Variables tab
   - Copy `DATABASE_URL` or individual DB variables

2. **Update local `.env`:**
   ```env
   DB_CONNECTION=pgsql
   DB_HOST=your-railway-db-host
   DB_PORT=5432
   DB_DATABASE=railway
   DB_USERNAME=postgres
   DB_PASSWORD=your-password
   ```

3. **Run migrations locally:**
   ```powershell
   cd C:\xampp\htdocs\Ashcol_Web
   php artisan migrate --force
   ```

4. **Tables will be created in Railway database!**

---

## ❌ Issue 5: Environment Variables Have Quotes

### Solution:

**Check Railway Variables tab:**
- If values have quotes like `"Ashcol_ServiceHub"`, **delete and re-add without quotes**
- Correct format: `Ashcol_ServiceHub` (no quotes)

---

## ✅ Complete Fix Checklist:

### Step 1: Verify All Variables (No Quotes!)

Go to Railway → Web Service → Variables tab, verify:

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

**Plus database variables** (auto-added by Railway):
- `DATABASE_URL` or
- `PGHOST`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`

### Step 2: Check Database Exists

- Railway dashboard → Look for PostgreSQL service
- If missing → Create it

### Step 3: Run Migrations

**Option A: Via Railway Shell (if available)**
```bash
php artisan migrate --force
```

**Option B: Via Local PC (if Shell not available)**
1. Get Railway database credentials
2. Update local `.env` with Railway DB
3. Run: `php artisan migrate --force`

### Step 4: Test API

Open in browser:
```
https://k0cn5non.up.railway.app/api/v1/send-verification-code
```

**Expected:** JSON response

### Step 5: Check Logs

- Railway → Deployments → Latest deployment → Logs
- Look for errors
- Common errors:
  - "APP_KEY not set" → Add APP_KEY variable
  - "Database connection failed" → Check database variables
  - "Route not found" → Check APP_URL matches domain

---

## 🚨 Quick Fix: Reset Everything

If nothing works, reset:

1. **Delete all variables** in Railway
2. **Re-add them one by one** (without quotes)
3. **Wait for service to restart**
4. **Check deployment logs** for errors
5. **Test API again**

---

## 📞 What Error Are You Seeing?

Tell me:
1. What happens when you test the API URL?
2. What error shows in Railway logs?
3. Are all variables added (check Variables tab)?
4. Does PostgreSQL service exist?

**I'll help fix the specific issue!**
