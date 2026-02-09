package app.hub.onboarding;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Helper class for managing onboarding state persistence using SharedPreferences.
 * 
 * This class provides a clean interface for reading and writing the onboarding completion flag,
 * which determines whether a user should see the intro screens or skip directly to login.
 * 
 * Error handling is implemented to ensure the app remains functional even if SharedPreferences
 * operations fail. In case of errors, the default behavior is to show onboarding (fail-safe approach).
 */
public class OnboardingPreferences {
    private static final String TAG = "OnboardingPreferences";
    private static final String PREF_NAME = "onboarding_prefs";
    private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";

    private final SharedPreferences sharedPreferences;

    /**
     * Creates a new OnboardingPreferences instance.
     *
     * @param context The application context used to access SharedPreferences
     * @throws IllegalArgumentException if context is null
     */
    public OnboardingPreferences(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null");
        }
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Checks whether the user has completed the onboarding flow.
     * 
     * This method is used by SplashActivity to determine navigation:
     * - If true: navigate directly to LoginActivity
     * - If false: navigate to IntroActivity to show onboarding screens
     *
     * @return true if onboarding has been completed, false otherwise
     *         Returns false if an error occurs (fail-safe default)
     */
    public boolean isOnboardingComplete() {
        try {
            return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETE, false);
        } catch (Exception e) {
            Log.e(TAG, "Error reading onboarding completion preference", e);
            // Default to showing onboarding if we can't read the preference
            return false;
        }
    }

    /**
     * Sets the onboarding completion status.
     * 
     * This method should be called when the user taps either authentication button
     * (Get Started or I'm already a member) to mark that they have seen the onboarding.
     *
     * @param complete true to mark onboarding as complete, false to reset it
     * @return true if the preference was successfully saved, false if an error occurred
     */
    public boolean setOnboardingComplete(boolean complete) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_ONBOARDING_COMPLETE, complete);
            boolean success = editor.commit();
            
            if (!success) {
                Log.e(TAG, "Failed to commit onboarding completion preference");
            }
            
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error writing onboarding completion preference", e);
            return false;
        }
    }
}
