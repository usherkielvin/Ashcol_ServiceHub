# API Integration TODO Checklist

## ✅ Completed

### Laravel Backend:
- [x] Sanctum package added to composer.json
- [x] API routes created (`routes/api.php`)
- [x] API controllers created (AuthController, ProfileController)
- [x] User model updated with HasApiTokens trait
- [x] Bootstrap configured to load API routes

### Android App:
- [x] Retrofit dependencies added
- [x] API service interface created
- [x] API models created (LoginRequest, LoginResponse, UserResponse, LogoutResponse)
- [x] ApiClient configured
- [x] TokenManager for SharedPreferences
- [x] MainActivity updated for API login
- [x] ProfileActivity updated for API user fetch
- [x] Internet permission added to AndroidManifest
- [x] No linter errors

## ⚠️ TODO - Laravel Backend

### 1. Install/Publish Sanctum (REQUIRED)
```bash
cd C:\xampp\htdocs\ashcol_portal
composer install  # or composer update if needed
php artisan vendor:publish --provider="Laravel\Sanctum\SanctumServiceProvider"
```

### 2. Run Migrations (REQUIRED)
```bash
php artisan migrate
```
This will create the `personal_access_tokens` table needed for API authentication.

### 3. Configure CORS (REQUIRED for Android)
Laravel 11 may handle CORS differently. Check if you need to:
- Publish CORS config: `php artisan config:publish cors`
- Or update `config/cors.php` if it exists
- Make sure API routes allow cross-origin requests

### 4. Test API Endpoints
Test with Postman or curl:
```bash
# Login
curl -X POST http://localhost:8000/api/v1/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"user@example.com\",\"password\":\"password\"}"
```

### 5. Create Test User (if needed)
Make sure you have a user in the database to test with:
- Email: your email
- Password: your password (hashed)
- Role: customer/staff/admin

## ⚠️ TODO - Android App

### 1. Update API Base URL (REQUIRED)
Edit `app/src/main/java/hans/ph/api/ApiClient.java`:
- **For Android Emulator**: Already set to `http://10.0.2.2:8000/` ✅
- **For Physical Device**: Change to `http://YOUR_COMPUTER_IP:8000/`
  - Find IP: `ipconfig` (Windows) - look for IPv4 Address
  - Make sure phone and computer are on same Wi-Fi network

### 2. Start Laravel Server (REQUIRED)
```bash
cd C:\xampp\htdocs\ashcol_portal
php artisan serve
```
Server will run on `http://localhost:8000`

### 3. Test the App
1. Build and run Android app
2. Try to login with database credentials
3. Check if profile loads user data

## 🔍 Verification Steps

### Backend:
- [ ] Sanctum migrations run successfully
- [ ] API endpoints respond (test with Postman)
- [ ] CORS allows Android requests
- [ ] Database has at least one user

### Android:
- [ ] App builds without errors
- [ ] API base URL is correct for your setup
- [ ] Login works with database credentials
- [ ] Profile shows user email
- [ ] Logout works

## 🐛 Common Issues & Solutions

### Issue: "Connection error" in Android
- **Solution**: Check if Laravel server is running and BASE_URL is correct

### Issue: "401 Unauthorized"
- **Solution**: Check if credentials exist in database and password is hashed correctly

### Issue: "CORS error"
- **Solution**: Configure CORS in Laravel to allow your Android app origin

### Issue: "Token not found" or authentication fails
- **Solution**: Make sure Sanctum migrations have been run

## 📝 Quick Start Commands

```bash
# Laravel - Install dependencies and setup
cd C:\xampp\htdocs\ashcol_portal
composer install
php artisan vendor:publish --provider="Laravel\Sanctum\SanctumServiceProvider"
php artisan migrate
php artisan serve

# Android - Build and run
# Use Android Studio to build and run the app
```

## 🎯 Priority Order

1. **HIGH PRIORITY**: Install Sanctum and run migrations
2. **HIGH PRIORITY**: Start Laravel server
3. **HIGH PRIORITY**: Configure CORS (if needed)
4. **MEDIUM**: Update API base URL if using physical device
5. **LOW**: Test and verify everything works

