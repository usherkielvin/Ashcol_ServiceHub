package app.hub.util;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "auth_pref";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_ROLE = "role";
    private static final String KEY_CONNECTION_STATUS = "connection_status";
    private static final String KEY_CURRENT_CITY = "current_city";
    
    // Notification Settings
    private static final String KEY_PUSH_NOTIF = "push_notifications";
    private static final String KEY_EMAIL_NOTIF = "email_notifications";
    private static final String KEY_SMS_NOTIF = "sms_notifications";

    private final SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void saveEmail(String email) {
        sharedPreferences.edit().putString(KEY_EMAIL, email).apply();
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public void saveName(String name) {
        sharedPreferences.edit().putString(KEY_NAME, name).apply();
    }

    public String getName() {
        return sharedPreferences.getString(KEY_NAME, null);
    }

    public void saveRole(String role) {
        sharedPreferences.edit().putString(KEY_ROLE, role).apply();
    }

    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, null);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void saveConnectionStatus(String status) {
        sharedPreferences.edit().putString(KEY_CONNECTION_STATUS, status).apply();
    }

    public String getConnectionStatus() {
        return sharedPreferences.getString(KEY_CONNECTION_STATUS, null);
    }

    public void clearConnectionStatus() {
        sharedPreferences.edit().remove(KEY_CONNECTION_STATUS).apply();
    }

    public void saveCurrentCity(String city) {
        sharedPreferences.edit().putString(KEY_CURRENT_CITY, city).apply();
    }

    public String getCurrentCity() {
        return sharedPreferences.getString(KEY_CURRENT_CITY, null);
    }
    
    // Notification Settings Methods
    public void setPushEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_PUSH_NOTIF, enabled).apply();
    }
    
    public boolean isPushEnabled() {
        return sharedPreferences.getBoolean(KEY_PUSH_NOTIF, true);
    }
    
    public void setEmailNotifEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_EMAIL_NOTIF, enabled).apply();
    }
    
    public boolean isEmailNotifEnabled() {
        return sharedPreferences.getBoolean(KEY_EMAIL_NOTIF, true);
    }
    
    public void setSmsNotifEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_SMS_NOTIF, enabled).apply();
    }
    
    public boolean isSmsNotifEnabled() {
        return sharedPreferences.getBoolean(KEY_SMS_NOTIF, false);
    }
}
