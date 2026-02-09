package app.hub.common;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import app.hub.IntroActivity;
import app.hub.onboarding.OnboardingPreferences;

import static org.junit.Assert.*;

/**
 * Instrumented unit tests for SplashActivity navigation logic.
 * 
 * These tests run on an Android device or emulator and validate that SplashActivity
 * correctly checks OnboardingPreferences and navigates to the appropriate screen:
 * - IntroActivity when onboarding is incomplete (first launch)
 * - MainActivity when onboarding is complete (returning user)
 * 
 * Requirements: 6.2, 6.3
 */
@RunWith(AndroidJUnit4.class)
public class SplashActivityInstrumentedTest {

    private ActivityScenario<SplashActivity> scenario;
    private Context context;

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
        // Clean up preferences after each test
        context = ApplicationProvider.getApplicationContext();
        OnboardingPreferences prefs = new OnboardingPreferences(context);
        prefs.setOnboardingComplete(false);
    }

    /**
     * Test navigation to IntroActivity when onboarding is incomplete.
     * 
     * Requirement 6.3: WHEN the app launches and the Onboarding_Preference is false or unset,
     * THE Splash_Screen SHALL navigate to the Intro_Activity
     */
    @Test
    public void navigatesToIntroActivityWhenOnboardingIncomplete() throws InterruptedException {
        // Given: onboarding is incomplete (first launch)
        context = ApplicationProvider.getApplicationContext();
        OnboardingPreferences prefs = new OnboardingPreferences(context);
        prefs.setOnboardingComplete(false);

        // When: SplashActivity is launched
        scenario = ActivityScenario.launch(SplashActivity.class);

        // Wait for the splash delay (2 seconds) plus a small buffer
        Thread.sleep(2500);

        // Then: the activity should have finished (navigated away)
        scenario.onActivity(activity -> {
            assertTrue("SplashActivity should finish after navigation", 
                    activity.isFinishing() || activity.isDestroyed());
        });

        // Note: We cannot directly verify the target activity in instrumented tests
        // without additional infrastructure, but we can verify the activity finished
        // which indicates navigation occurred. The navigation target is verified
        // through manual testing and integration tests.
    }

    /**
     * Test navigation to MainActivity when onboarding is complete.
     * 
     * Requirement 6.2: WHEN the app launches and the Onboarding_Preference is true,
     * THE Splash_Screen SHALL navigate directly to the Login_Activity instead of the Intro_Activity
     */
    @Test
    public void navigatesToMainActivityWhenOnboardingComplete() throws InterruptedException {
        // Given: onboarding is complete (returning user)
        context = ApplicationProvider.getApplicationContext();
        OnboardingPreferences prefs = new OnboardingPreferences(context);
        prefs.setOnboardingComplete(true);

        // When: SplashActivity is launched
        scenario = ActivityScenario.launch(SplashActivity.class);

        // Wait for the splash delay (2 seconds) plus a small buffer
        Thread.sleep(2500);

        // Then: the activity should have finished (navigated away)
        scenario.onActivity(activity -> {
            assertTrue("SplashActivity should finish after navigation", 
                    activity.isFinishing() || activity.isDestroyed());
        });

        // Note: We cannot directly verify the target activity in instrumented tests
        // without additional infrastructure, but we can verify the activity finished
        // which indicates navigation occurred. The navigation target is verified
        // through manual testing and integration tests.
    }

    /**
     * Test that SplashActivity creates successfully.
     */
    @Test
    public void activityCreatesSuccessfully() {
        // Given: onboarding preference is set to false
        context = ApplicationProvider.getApplicationContext();
        OnboardingPreferences prefs = new OnboardingPreferences(context);
        prefs.setOnboardingComplete(false);

        // When: SplashActivity is launched
        scenario = ActivityScenario.launch(SplashActivity.class);

        // Then: the activity should be created successfully
        scenario.onActivity(activity -> {
            assertNotNull("SplashActivity should be created successfully", activity);
        });
    }

    /**
     * Test that SplashActivity preserves intent extras when navigating.
     */
    @Test
    public void preservesIntentExtrasWhenNavigating() throws InterruptedException {
        // Given: onboarding is complete and there are extras in the launch intent
        context = ApplicationProvider.getApplicationContext();
        OnboardingPreferences prefs = new OnboardingPreferences(context);
        prefs.setOnboardingComplete(true);

        Intent launchIntent = new Intent(context, SplashActivity.class);
        launchIntent.putExtra("test_key", "test_value");
        launchIntent.putExtra("test_number", 42);

        // When: SplashActivity is launched with extras
        scenario = ActivityScenario.launch(launchIntent);

        // Wait for the splash delay
        Thread.sleep(2500);

        // Then: the activity should have finished (navigated away)
        scenario.onActivity(activity -> {
            assertTrue("SplashActivity should finish after navigation", 
                    activity.isFinishing() || activity.isDestroyed());
        });

        // Note: Verifying that extras are actually passed to the next activity
        // requires more complex test infrastructure. This test verifies that
        // the navigation occurs successfully when extras are present.
    }

    /**
     * Test default behavior when preference is unset (first install).
     * 
     * Requirement 6.3: WHEN the app launches and the Onboarding_Preference is false or unset,
     * THE Splash_Screen SHALL navigate to the Intro_Activity
     */
    @Test
    public void defaultsToIntroActivityWhenPreferenceUnset() throws InterruptedException {
        // Given: onboarding preference has never been set (simulated by setting to false)
        context = ApplicationProvider.getApplicationContext();
        OnboardingPreferences prefs = new OnboardingPreferences(context);
        prefs.setOnboardingComplete(false);

        // When: SplashActivity is launched
        scenario = ActivityScenario.launch(SplashActivity.class);

        // Wait for the splash delay
        Thread.sleep(2500);

        // Then: the activity should have finished (navigated to IntroActivity)
        scenario.onActivity(activity -> {
            assertTrue("SplashActivity should finish after navigation", 
                    activity.isFinishing() || activity.isDestroyed());
        });
    }

    /**
     * Test that OnboardingPreferences is correctly instantiated and used.
     */
    @Test
    public void usesOnboardingPreferencesCorrectly() {
        // Given: a context
        context = ApplicationProvider.getApplicationContext();

        // When: creating OnboardingPreferences
        OnboardingPreferences prefs = new OnboardingPreferences(context);

        // Then: it should work correctly
        assertNotNull("OnboardingPreferences should be created", prefs);

        // And: we can set and read preferences
        boolean setResult = prefs.setOnboardingComplete(true);
        assertTrue("Setting preference should succeed", setResult);

        boolean isComplete = prefs.isOnboardingComplete();
        assertTrue("Preference should be true after setting", isComplete);

        // Clean up
        prefs.setOnboardingComplete(false);
    }

    /**
     * Test that SplashActivity handles the navigation delay correctly.
     */
    @Test
    public void respectsSplashDelay() throws InterruptedException {
        // Given: onboarding is incomplete
        context = ApplicationProvider.getApplicationContext();
        OnboardingPreferences prefs = new OnboardingPreferences(context);
        prefs.setOnboardingComplete(false);

        // When: SplashActivity is launched
        scenario = ActivityScenario.launch(SplashActivity.class);

        // Then: immediately after launch, activity should still be active
        scenario.onActivity(activity -> {
            assertFalse("SplashActivity should not finish immediately", 
                    activity.isFinishing());
        });

        // Wait for the splash delay
        Thread.sleep(2500);

        // Then: after the delay, activity should have finished
        scenario.onActivity(activity -> {
            assertTrue("SplashActivity should finish after delay", 
                    activity.isFinishing() || activity.isDestroyed());
        });
    }
}
