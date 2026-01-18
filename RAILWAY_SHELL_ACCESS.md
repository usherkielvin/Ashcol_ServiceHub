# 🔧 Railway Shell Access - How to Find It

## Where to Find Shell in Railway:

### Method 1: Via Deployments Tab

1. In Railway dashboard, click on your **Web Service** (`Ashcol_Web`)
2. Go to **"Deployments"** tab (top navigation)
3. Click on the **latest deployment** (most recent one)
4. You should see tabs: **"Logs"**, **"Metrics"**, **"Shell"**
5. Click **"Shell"** tab
6. A terminal window will open
7. Run: `php artisan key:generate --show`

### Method 2: Via Service Settings

1. Click on your **Web Service**
2. Look for **"Connect"** or **"Shell"** button (usually in top right)
3. Click it to open terminal

### Method 3: If Shell Tab Not Visible

Railway might not show Shell for all services. Use **Alternative Method** below.

---

## ✅ Alternative: Generate APP_KEY Locally

If you can't find Shell, generate the key on your PC:

### Step 1: Generate Key Locally

```powershell
cd C:\xampp\htdocs\Ashcol_Web
php artisan key:generate --show
```

**Copy the output** (starts with `base64:`)

### Step 2: Add to Railway

1. Railway dashboard → **Web Service** → **"Variables"** tab
2. Click **"+ New Variable"**
3. **Key:** `APP_KEY`
4. **Value:** (paste the copied key)
5. Click **"Add"**
6. Service will auto-restart

---

## 🎯 Quick Steps (If Shell Not Available):

1. **Open PowerShell** on your PC
2. **Navigate to Laravel folder:**
   ```powershell
   cd C:\xampp\htdocs\Ashcol_Web
   ```
3. **Generate key:**
   ```powershell
   php artisan key:generate --show
   ```
4. **Copy the output** (e.g., `base64:xxxxx...`)
5. **Go to Railway** → Web Service → Variables
6. **Add variable:**
   - Key: `APP_KEY`
   - Value: (paste the copied key)
7. **Done!** Service restarts automatically

---

## 📸 What to Look For:

In Railway dashboard, the Shell might be:
- A **terminal icon** in the top right
- A **"Shell"** button in the service header
- Under **"Deployments"** → Click deployment → **"Shell"** tab
- Sometimes labeled as **"Connect"** or **"Terminal"**

---

## 🚨 If Still Can't Find It:

**Just use the local method** - it works exactly the same!

1. Generate key locally (see above)
2. Add to Railway Variables
3. Done!

**The key is the same whether generated locally or on Railway!**
