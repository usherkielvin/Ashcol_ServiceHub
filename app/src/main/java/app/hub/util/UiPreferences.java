package app.hub.util;

import androidx.appcompat.app.AppCompatDelegate;

public final class UiPreferences {
    private UiPreferences() {
    }

    public static void applyTheme(String theme) {
        int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        if ("light".equalsIgnoreCase(theme)) {
            mode = AppCompatDelegate.MODE_NIGHT_NO;
        } else if ("dark".equalsIgnoreCase(theme)) {
            mode = AppCompatDelegate.MODE_NIGHT_YES;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static void applyLanguage(String language) {
        // No-op: keep system language and avoid background locale changes.
    }
}
