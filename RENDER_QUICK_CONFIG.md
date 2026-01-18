# ⚡ Quick Render Configuration (Current Page)

## You're on the "Configure" page - Here's what to change:

---

## 🔧 Configuration Fields (Fill These Now)

### 1. **Language** ⚠️ IMPORTANT
- **Current:** `Node` ❌
- **Change to:** `PHP` ✅
- **How:** Click dropdown → Select "PHP"

### 2. **Name**
- **Current:** `Ashcol_Web` ✅
- **Keep as is** (or change to `ashcol-backend`)

### 3. **Branch**
- **Current:** `main` ✅
- **Keep as is** (or select your main branch)

### 4. **Region**
- **Current:** `Singapore (Southeast Asia)` ✅
- **Keep as is** (or choose closest to you)

### 5. **Root Directory**
- **Current:** (empty) ✅
- **Keep empty** - Laravel is in repository root

### 6. **Build Command** ⚠️ IMPORTANT
- **Current:** `$ npm install; npm run build` ❌
- **Delete and replace with:**
  ```
  composer install --optimize-autoloader --no-dev
  ```

### 7. **Start Command** ⚠️ IMPORTANT
- **Current:** (probably empty) ❌
- **Add this:**
  ```
  php artisan serve --host=0.0.0.0 --port=$PORT
  ```

---

## 📝 Environment Variables (Add Before Creating)

Click **"Advanced"** or scroll to **"Environment Variables"** section.

### Add These (Copy-Paste Ready):

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

**Note:** We'll add `APP_KEY` and database variables after deployment.

---

## ✅ After Configuration

1. Click **"Create Web Service"**
2. Wait for build (2-5 minutes)
3. Follow **RENDER_DEPLOYMENT_GUIDE.md** for next steps

---

## 🎯 Summary

**Change 3 things:**
1. Language: `Node` → `PHP`
2. Build Command: Delete Node command → Add `composer install --optimize-autoloader --no-dev`
3. Start Command: Add `php artisan serve --host=0.0.0.0 --port=$PORT`

**Add environment variables** (see above)

**Then click "Create Web Service"!**

---

## 📖 Full Guide

See **RENDER_DEPLOYMENT_GUIDE.md** for complete step-by-step instructions including:
- Database setup
- Running migrations
- Updating Android app
- Troubleshooting
