# Android API Configuration

## Update API Base URL

Edit `app/src/main/java/hans/ph/api/ApiClient.java` and update the `BASE_URL`:

### For Android Emulator:
```java
private static final String BASE_URL = "http://10.0.2.2:8000/";
```

### For Physical Device:
1. Find your computer's IP address:
   - Windows: Run `ipconfig` in Command Prompt
   - Mac/Linux: Run `ifconfig` in Terminal
   - Look for IPv4 address (e.g., 192.168.1.100)

2. Update the BASE_URL:
```java
private static final String BASE_URL = "http://192.168.1.100:8000/";
```

3. Make sure your Android device and computer are on the same Wi-Fi network.

## Testing

1. Make sure Laravel server is running: `php artisan serve`
2. Make sure you have a user in the database
3. Use the email and password to login from the Android app

## Troubleshooting

- **Connection Error**: Check if Laravel server is running and BASE_URL is correct
- **401 Unauthorized**: Check if credentials are correct in database
- **CORS Error**: Make sure CORS is configured in Laravel (see API_SETUP.md)

