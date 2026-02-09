package app.hub.common;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import app.hub.IntroActivity;
import app.hub.onboarding.OnboardingPreferences;

import static org.junit.Assert.*;

/**
 * Unit tests for SplashActivity navigation logic.
 * 
 * These tests validate that SplashActivity correctly checks OnboardingPreferences
 * and navigates to the appropriate screen:
 * - IntroActivity when onboarding is incomplete (first launch)
 * - MainActivity when onboarding is complete (returning user)
 * 
 * Requirements: 6.2, 6.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class SplashActivityTest {

    private ActivityController<SplashActivity> controller;
    private SplashActivity activity;
    private Context context;
    private OnboardingPreferences preferences;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preferences = new OnboardingPreferences(context);
        // Reset onboarding preference before each test
        preferences.setOnboardingComplete(false);
    }

    @After
    public void tearDown() {
        if (controller != null) {
            controller.pause().stop().destroy();
        }
        // Clean up preferences
        preferences.setOnboardingComplete(false);
    }

    @Test
    public void navigatesToIntroActivityWhenOnboardingIncomplete() {
        // Given onboarding is incomplete (first launch)
        // Requirement 6.3: WHEN the app launches and the Onboarding_Preference is false or unset,
        // THE Splash_Screen SHALL navigate to the Intro_Activity
        preferences.setOnboardingComplete(false);

        // When SplashActivity is created and the delay completes
        controller = Robolectric.buildActivity(SplashActivity.class).create().start().resume();
        activity = controller.get();
        
        // Advance the looper to trigger the delayed navigation
        org.robolectric.shadows.ShadowLooper.idleMainLooper(2100, java.util.concurrent.TimeUnit.MILLISECONDS);

        // Then the activity should navigate to IntroActivity
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent nextIntent = shadowActivity.getNextStartedActivity();
        
        assertNotNull("An intent should be started", nextIntent);
        assertEquals("Should navigate to IntroActivity when onboarding is incomplete",
                IntroActivity.class.getName(),
                nextIntent.getComponent().getClassName());
        
        // And the activity should finish
        assertTrue("SplashActivity should finish after navigation", activity.isFinishing());
    }

    @Test
    public void navigatesToMainActivityWhenOnboardingComplete() {
        // Given onboarding is complete (returning user)
        // Requirement 6.2: WHEN the app launches and the Onboarding_Preference is true,
        // THE Splash_Screen SHALL navigate directly to the Login_Activity instead of the Intro_Activity
        preferences.setOnboardingComplete(true);

        // When SplashActivity is created and the delay completes
        controller = Robolectric.buildActivity(SplashActivity.class).create().start().resume();
        activity = controller.get();
        
        // Advance the looper to trigger the delayed navigation
        org.robolectric.shadows.ShadowLooper.idleMainLooper(2100, java.util.concurrent.TimeUnit.MILLISECONDS);

        // Then the activity should navigate to MainActivity (Login)
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent nextIntent = shadowActivity.getNextStartedActivity();
        
        assertNotNull("An intent should be started", nextIntent);
        assertEquals("Should navigate to MainActivity when onboarding is complete",
                MainActivity.class.getName(),
                nextIntent.getComponent().getClassName());
        
        // And the activity should finish
        assertTrue("SplashActivity should finish after navigation", activity.isFinishing());
    }

    @Test
    public void preservesIntentExtrasWhenNavigating() {
        // Given onboarding is complete and there are extras in the original intent
        preferences.setOnboardingComplete(true);
        
        Intent launchIntent = new Intent(context, SplashActivity.class);
        launchIntent.putExtra("test_key", "test_value");
        launchIntent.putExtra("test_number", 42);

        // When SplashActivity is created with extras
        controller = Robolectric.buildActivity(SplashActivity.class, launchIntent)
                .create().start().resume();
        activity = controller.get();
        
        // Advance the looper to trigger the delayed navigation
        org.robolectric.shadows.ShadowLooper.idleMainLooper(2100, java.util.concurrent.TimeUnit.MILLISECONDS);

        // Then the extras should be preserved in the next intent
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent nextIntent = shadowActivity.getNextStartedActivity();
        
        assertNotNull("An intent should be started", nextIntent);
        assertTrue("Intent should contain test_key extra", 
                nextIntent.hasExtra("test_key"));
        assertEquals("Intent should preserve string extra value",
                "test_value", nextIntent.getStringExtra("test_key"));
        assertEquals("Intent should preserve int extra value",
                42, nextIntent.getIntExtra("test_number", 0));
    }

    @Test
    public void appliesTransitionAnimationWhenNavigating() {
        // Given onboarding is incomplete
        preferences.setOnboardingComplete(false);

        // When SplashActivity navigates
        controller = Robolectric.buildActivity(SplashActivity.class).create().start().resume();
        activity = controller.get();
        
        // Advance the looper to trigger the delayed navigation
        org.robolectric.shadows.ShadowLooper.idleMainLooper(2100, java.util.concurrent.TimeUnit.MILLISECONDS);

        // Then transition animation should be applied
        // Note: Robolectric doesn't fully support overridePendingTransition verification,
        // but we can verify the navigation occurred successfully
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent nextIntent = shadowActivity.getNextStartedActivity();
        
        assertNotNull("Navigation should occur with transition", nextIntent);
        assertTrue("Activity should finish after applying transition", activity.isFinishing());
    }

    @Test
    public void handlesNullIntentExtrasGracefully() {
        // Given onboarding is complete and there are no extras
        preferences.setOnboardingComplete(true);

        // When SplashActivity is created without extras
        controller = Robolectric.buildActivity(SplashActivity.class).create().start().resume();
        activity = controller.get();
        
        // Advance the looper to trigger the delayed navigation
        org.robolectric.shadows.ShadowLooper.idleMainLooper(2100, java.util.concurrent.TimeUnit.MILLISECONDS);

        // Then navigation should still work correctly
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent nextIntent = shadowActivity.getNextStartedActivity();
        
        assertNotNull("Navigation should occur even without extras", nextIntent);
        assertEquals("Should navigate to MainActivity",
                MainActivity.class.getName(),
                nextIntent.getComponent().getClassName());
    }

    @Test
    public void defaultsToIntroActivityWhenPreferenceUnset() {
        // Given onboarding preference has never been set (first install)
        // The preference should default to false
        // Requirement 6.3: WHEN the app launches and the Onboarding_Preference is false or unset,
        // THE Splash_Screen SHALL navigate to the Intro_Activity

        // When SplashActivity is created
        controller = Robolectric.buildActivity(SplashActivity.class).create().start().resume();
        activity = controller.get();
        
        // Advance the looper to trigger the delayed navigation
        org.robolectric.shadows.ShadowLooper.idleMainLooper(2100, java.util.concurrent.TimeUnit.MILLISECONDS);

        // Then it should navigate to IntroActivity (fail-safe default)
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent nextIntent = shadowActivity.getNextStartedActivity();
        
        assertNotNull("An intent should be started", nextIntent);
        assertEquals("Should default to IntroActivity when preference is unset",
                IntroActivity.class.getName(),
                nextIntent.getComponent().getClassName());
    }

    @Test
    public void activityCreatesSuccessfully() {
        // When SplashActivity is created
        controller = Robolectric.buildActivity(SplashActivity.class).create();
        activity = controller.get();

        // Then the activity should not be null
        assertNotNull("SplashActivity should be created successfully", activity);
    }

    @Test
    public void activitySetsContentView() {
        // When SplashActivity is created
        controller = Robolectric.buildActivity(SplashActivity.class).create();
        activity = controller.get();

        // Then the content view should be set (activity should have a root view)
        assertNotNull("Activity should have a root view", activity.findViewById(android.R.id.content));
    }
}
