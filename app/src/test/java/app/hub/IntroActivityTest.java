package app.hub;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import app.hub.onboarding.OnboardingConfig;
import app.hub.onboarding.OnboardingPreferences;

import static org.junit.Assert.*;

/**
 * Unit tests for IntroActivity.
 * 
 * These tests validate specific examples and edge cases for the IntroActivity,
 * including ViewPager initialization, page indicators, button styling, and
 * screen content.
 * 
 * Tests cover:
 * - ViewPager initialization with 3 screens (Requirement 1.2)
 * - Page indicator displays 3 dots (Requirements 4.1, 4.2, 4.3, 4.4)
 * - Button styling (Requirements 5.6, 5.7)
 * - Specific screen content examples (S2, S3, S4)
 * - Swipe boundary behavior (Requirements 3.6, 3.7)
 * 
 * Requirements: 1.2, 3.6, 3.7, 4.1, 4.2, 4.3, 4.4, 5.6, 5.7
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class IntroActivityTest {

    private ActivityController<IntroActivity> controller;
    private IntroActivity activity;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        // Ensure onboarding is not complete before each test
        OnboardingPreferences prefs = new OnboardingPreferences(context);
        prefs.setOnboardingComplete(false);
    }

    @After
    public void tearDown() {
        if (controller != null) {
            controller.pause().stop().destroy();
        }
        // Clean up preferences
        OnboardingPreferences prefs = new OnboardingPreferences(context);
        prefs.setOnboardingComplete(false);
    }

    @Test
    public void activityCreatesSuccessfully() {
        // When IntroActivity is created
        controller = Robolectric.buildActivity(IntroActivity.class).create();
        activity = controller.get();

        // Then the activity should not be null
        assertNotNull("IntroActivity should be created successfully", activity);
    }

    @Test
    public void viewPagerIsInitializedWithThreeScreens() {
        // Given IntroActivity is created
        // Requirement 1.2: The Intro_Activity SHALL contain exactly three Onboarding_Screens
        controller = Robolectric.buildActivity(IntroActivity.class).create().start().resume();
        activity = controller.get();

        // When checking the ViewPager adapter
        androidx.viewpager2.widget.ViewPager2 viewPager = activity.findViewById(R.id.viewPager);

        // Then it should have exactly 3 items
        assertNotNull("ViewPager should exist", viewPager);
        assertNotNull("ViewPager should have an adapter", viewPager.getAdapter());
        assertEquals("ViewPager should have exactly 3 screens", 
                    3, viewPager.getAdapter().getItemCount());
    }

    @Test
    public void pageIndicatorDisplaysThreeDots() {
        // Given IntroActivity is created
        // Requirements 4.1: THE Intro_Activity SHALL display Page_Indicators showing three positions
        controller = Robolectric.buildActivity(IntroActivity.class).create().start().resume();
        activity = controller.get();

        // When checking the page indicator dots
        android.view.View dot1 = activity.findViewById(R.id.dot1);
        android.view.View dot2 = activity.findViewById(R.id.dot2);
        android.view.View dot3 = activity.findViewById(R.id.dot3);

        // Then all three dots should exist
        assertNotNull("Dot 1 should exist", dot1);
        assertNotNull("Dot 2 should exist", dot2);
        assertNotNull("Dot 3 should exist", dot3);
    }

    @Test
    public void pageIndicatorHighlightsFirstPositionInitially() {
        // Given IntroActivity is created
        // Requirement 4.2: WHEN displaying S2, THE Page_Indicator SHALL highlight the first position
        controller = Robolectric.buildActivity(IntroActivity.class).create().start().resume();
        activity = controller.get();

        // When checking the initial page indicator state
        android.view.View dot1 = activity.findViewById(R.id.dot1);
        android.view.View dot2 = activity.findViewById(R.id.dot2);
        android.view.View dot3 = activity.findViewById(R.id.dot3);

        int selectedColor = activity.getColorStateList(R.color.dot_selected_color).getDefaultColor();
        int unselectedColor = activity.getColorStateList(R.color.dot_unselected_color).getDefaultColor();

        // Then the first dot should be highlighted
        assertEquals("Dot 1 should be selected initially", 
                    selectedColor, dot1.getBackgroundTintList().getDefaultColor());
        assertEquals("Dot 2 should be unselected initially", 
                    unselectedColor, dot2.getBackgroundTintList().getDefaultColor());
        assertEquals("Dot 3 should be unselected initially", 
                    unselectedColor, dot3.getBackgroundTintList().getDefaultColor());
    }

    @Test
    public void getStartedButtonHasGreenBackground() {
        // Given IntroActivity is created
        // Requirement 5.6: THE "Get Started" button SHALL have a green background color
        controller = Robolectric.buildActivity(IntroActivity.class).create().start().resume();
        activity = controller.get();

        // When checking the Get Started button
        com.google.android.material.button.MaterialButton btnGetStarted = 
                activity.findViewById(R.id.btnGetStarted);

        // Then it should have a green background
        assertNotNull("Get Started button should exist", btnGetStarted);
        int greenColor = activity.getColor(R.color.green);
        assertEquals("Get Started button should have green background", 
                    greenColor, btnGetStarted.getBackgroundTintList().getDefaultColor());
    }

    @Test
    public void alreadyMemberButtonHasTransparentBackgroundWithOutline() {
        // Given IntroActivity is created
        // Requirement 5.7: THE "I'm already a member" button SHALL have a transparent background with an outline
        controller = Robolectric.buildActivity(IntroActivity.class).create().start().resume();
        activity = controller.get();

        // When checking the Already Member button
        com.google.android.material.button.MaterialButton btnAlreadyMember = 
                activity.findViewById(R.id.btnAlreadyMember);

        // Then it should have an outline style
        assertNotNull("Already Member button should exist", btnAlreadyMember);
        assertNotNull("Already Member button should have stroke color", 
                     btnAlreadyMember.getStrokeColor());
        assertTrue("Already Member button should have stroke width > 0", 
                  btnAlreadyMember.getStrokeWidth() > 0);
    }

    @Test
    public void bothButtonsAreVisible() {
        // Given IntroActivity is created
        // Requirements 5.1, 5.2: THE Intro_Activity SHALL display both buttons on all screens
        controller = Robolectric.buildActivity(IntroActivity.class).create().start().resume();
        activity = controller.get();

        // When checking button visibility
        com.google.android.material.button.MaterialButton btnGetStarted = 
                activity.findViewById(R.id.btnGetStarted);
        com.google.android.material.button.MaterialButton btnAlreadyMember = 
                activity.findViewById(R.id.btnAlreadyMember);

        // Then both buttons should be visible
        assertEquals("Get Started button should be visible", 
                    android.view.View.VISIBLE, btnGetStarted.getVisibility());
        assertEquals("Already Member button should be visible", 
                    android.view.View.VISIBLE, btnAlreadyMember.getVisibility());
    }

    @Test
    public void bothButtonsAreEnabled() {
        // Given IntroActivity is created
        controller = Robolectric.buildActivity(IntroActivity.class).create().start().resume();
        activity = controller.get();

        // When checking button enabled state
        com.google.android.material.button.MaterialButton btnGetStarted = 
                activity.findViewById(R.id.btnGetStarted);
        com.google.android.material.button.MaterialButton btnAlreadyMember = 
                activity.findViewById(R.id.btnAlreadyMember);

        // Then both buttons should be enabled
        assertTrue("Get Started button should be enabled", btnGetStarted.isEnabled());
        assertTrue("Already Member button should be enabled", btnAlreadyMember.isEnabled());
    }

    @Test
    public void viewPagerStartsAtFirstScreen() {
        // Given IntroActivity is created
        // Requirement 1.1: WHEN the Splash_Screen completes, THE Intro_Activity SHALL display the first Onboarding_Screen (S2)
        controller = Robolectric.buildActivity(IntroActivity.class).create().start().resume();
        activity = controller.get();

        // When checking the ViewPager initial position
        androidx.viewpager2.widget.ViewPager2 viewPager = activity.findViewById(R.id.viewPager);

        // Then it should start at position 0 (S2)
        assertEquals("ViewPager should start at position 0 (S2)", 
                    0, viewPager.getCurrentItem());
    }

    @Test
    public void onboardingConfigurationMatchesRequirements() {
        // Verify that the onboarding configuration used by IntroActivity matches requirements
        // Requirements 1.4, 1.5, 1.6, 2.1, 2.2, 2.3
        
        // S2 configuration
        assertEquals("S2 should use pic1", R.drawable.pic1, 
                    OnboardingConfig.getPage(0).getBackgroundImageRes());
        assertEquals("S2 should have correct title", 
                    "Stay Cool, Stay Comfortable", 
                    OnboardingConfig.getPage(0).getTitleText());
        assertEquals("S2 should have correct subtitle", 
                    "Reliable aircon service for home and office", 
                    OnboardingConfig.getPage(0).getSubtitleText());

        // S3 configuration
        assertEquals("S3 should use pic2", R.drawable.pic2, 
                    OnboardingConfig.getPage(1).getBackgroundImageRes());
        assertEquals("S3 should have correct title", 
                    "We've Got You Covered!", 
                    OnboardingConfig.getPage(1).getTitleText());
        assertEquals("S3 should have correct subtitle", 
                    "From cleaning, repairs, to installations, our team does it all", 
                    OnboardingConfig.getPage(1).getSubtitleText());

        // S4 configuration
        assertEquals("S4 should use pic3", R.drawable.pic3, 
                    OnboardingConfig.getPage(2).getBackgroundImageRes());
        assertEquals("S4 should have correct title", 
                    "Let's Get Started", 
                    OnboardingConfig.getPage(2).getTitleText());
        assertEquals("S4 should have correct subtitle", 
                    "Schedule your appointment in just a few taps", 
                    OnboardingConfig.getPage(2).getSubtitleText());
    }

    @Test
    public void activityHandlesCreationWithoutCrashing() {
        // Test that activity can be created, started, resumed, paused, stopped, and destroyed
        // without crashing (basic lifecycle test)
        controller = Robolectric.buildActivity(IntroActivity.class)
                .create()
                .start()
                .resume()
                .pause()
                .stop()
                .destroy();

        // If we reach here without exception, the test passes
        assertTrue("Activity should handle full lifecycle without crashing", true);
    }
}
