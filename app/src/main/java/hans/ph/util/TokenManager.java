package hans.ph.util;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "auth_pref";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_ROLE = "role";

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
}

