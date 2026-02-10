package app.hub.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import app.hub.IntroActivity;
import app.hub.R;
import app.hub.onboarding.OnboardingPreferences;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Use a Handler to delay the transition to the next screen
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextScreen, SPLASH_DELAY);
    }

    /**
     * Determines the next screen based on onboarding completion status.
     * 
     * This method checks OnboardingPreferences to decide navigation:
     * - If onboarding is incomplete (first launch): navigate to IntroActivity
     * - If onboarding is complete (returning user): navigate to MainActivity (Login)
     * 
     * Requirements: 1.1, 6.2, 6.3
     */
    private void navigateToNextScreen() {
        OnboardingPreferences prefs = new OnboardingPreferences(this);
        
        Intent intent;
        if (prefs.isOnboardingComplete()) {
            // Returning user - skip onboarding and go directly to login
            intent = new Intent(this, MainActivity.class);
        } else {
            // First-time user - show onboarding screens
            intent = new Intent(this, IntroActivity.class);
        }
        
        // Preserve any extras from the original intent
        if (getIntent() != null && getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        
        startActivity(intent);
        finish();
    }
}
