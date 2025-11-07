# How to Test API Endpoints

## What is that curl command?

The curl command on lines 44-51 is a way to test if your Laravel API is working **before** testing with the Android app.

## Option 1: Test with curl (Command Line)

### Step 1: Make sure Laravel server is running
```bash
cd C:\xampp\htdocs\ashcol_portal
php artisan serve
```

### Step 2: Open a NEW terminal/command prompt
Keep the server running in one terminal, open another for testing.

### Step 3: Run the curl command
```bash
curl -X POST http://localhost:8000/api/v1/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"your-email@example.com\",\"password\":\"your-password\"}"
```

**Important:** 
- Replace `your-email@example.com` with an actual email from your database
- Replace `your-password` with the actual password
- On Windows, use `^` instead of `\` for line continuation
- Or put it all on one line

### What you should see:
If it works, you'll get a JSON response like:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "user@example.com",
      "role": "customer"
    },
    "token": "1|abc123def456..."
  }
}
```

If it fails, you'll get:
```json
{
  "success": false,
  "message": "Invalid credentials"
}
```

## Option 2: Test with Postman (Easier!)

### Step 1: Download Postman
- Go to https://www.postman.com/downloads/
- Download and install

### Step 2: Create a new request
1. Open Postman
2. Click "New" → "HTTP Request"
3. Set method to **POST**
4. Enter URL: `http://localhost:8000/api/v1/login`

### Step 3: Set headers
1. Click "Headers" tab
2. Add header:
   - Key: `Content-Type`
   - Value: `application/json`

### Step 4: Set body
1. Click "Body" tab
2. Select "raw" and "JSON"
3. Enter:
```json
{
  "email": "your-email@example.com",
  "password": "your-password"
}
```

### Step 5: Send request
Click "Send" button

## Option 3: Test with Android App (Final Test)

Once the API works with curl or Postman, test with your Android app:
1. Build and run the app
2. Enter email and password
3. Click login
4. Should navigate to dashboard

## Why Test First?

Testing with curl/Postman helps you:
- ✅ Verify API is working before Android app
- ✅ Check if credentials are correct
- ✅ See exact error messages
- ✅ Debug issues faster

## Common Issues

### "Connection refused"
- **Fix**: Make sure Laravel server is running (`php artisan serve`)

### "404 Not Found"
- **Fix**: Check URL is correct: `http://localhost:8000/api/v1/login`

### "401 Unauthorized" or "Invalid credentials"
- **Fix**: 
  - Check email exists in database
  - Check password is correct
  - Make sure password is hashed in database (Laravel does this automatically)

### "500 Internal Server Error"
- **Fix**: 
  - Check Laravel logs: `storage/logs/laravel.log`
  - Make sure Sanctum migrations are run
  - Check if database connection is working

## Quick Test Script

Save this as `test-api.bat` (Windows) in your Laravel project:

```batch
@echo off
echo Testing API Login...
curl -X POST http://localhost:8000/api/v1/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"your-email@example.com\",\"password\":\"your-password\"}"
pause
```

Replace the email and password, then double-click to run!

