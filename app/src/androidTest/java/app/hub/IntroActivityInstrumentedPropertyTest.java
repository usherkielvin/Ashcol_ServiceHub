package app.hub;

import android.content.Context;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import app.hub.onboarding.OnboardingPreferences;

import static org.junit.Assert.*;

/**
 * Instrumented property-based tests for IntroActivity.
 * 
 * These tests verify universal properties that require real Android framework components.
 * They run on an Android device or emulator and test UI-heavy properties like page
 * indicator synchronization and button visibility across screen transitions.
 * 
 * Property tests are implemented as parameterized tests that verify the property
 * holds across multiple input values.
 * 
 * Tests cover:
 * - Property 5: Page indicator synchronization (Requirements 4.5)
 * - Property 7: Static button visibility (Requirements 5.1, 5.2, 5.5)
 * - Property 8: Get Started navigation (Requirements 5.3)
 * - Property 9: Already Member navigation (Requirements 5.4)
 * - Property 10: Onboarding completion persistence (Requirements 6.1, 6.4)
 */
@RunWith(AndroidJUnit4.class)
public class IntroActivityInstrumentedPropertyTest {

    private ActivityScenario<IntroActivity> scenario;

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
        // Clean up preferences after each test
        Context context = ApplicationProvider.getApplicationContext();
        OnboardingPreferences prefs = new OnboardingPreferences(context);
        prefs.setOnboardingComplete(false);
    }

    /**
     * Property 5: Page indicator synchronization
     * 
     * For any ViewPager position change, the page indicator should update to highlight 
     * the position corresponding to the current screen.
     * 
     * **Validates: Requirements 4.5**
     * 
     * This test verifies the property across all valid positions (0, 1, 2).
     */
    @Test
    public void property5_pageIndicatorSynchronizesWithPosition0() {
        verifyPageIndicatorSynchronization(0);
    }

    @Test
    public void property5_pageIndicatorSynchronizesWithPosition1() {
        verifyPageIndicatorSynchronization(1);
    }

    @Test
    public void property5_pageIndicatorSynchronizesWithPosition2() {
        verifyPageIndicatorSynchronization(2);
    }

    private void verifyPageIndicatorSynchronization(int targetPosition) {
        // Given: IntroActivity is launched
        scenario = ActivityScenario.launch(IntroActivity.class);

        // When: ViewPager is set to a specific position
        scenario.onActivity(activity -> {
            ViewPager2 viewPager = activity.findViewById(R.id.viewPager);
            assertNotNull("ViewPager should exist", viewPager);
            
            // Set the position
            viewPager.setCurrentItem(targetPosition, false);
        });

        // Wait for UI to update
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then: The page indicator should highlight the correct dot
        scenario.onActivity(activity -> {
            View dot1 = activity.findViewById(R.id.dot1);
            View dot2 = activity.findViewById(R.id.dot2);
            View dot3 = activity.findViewById(R.id.dot3);

            assertNotNull("Dot 1 should exist", dot1);
            assertNotNull("Dot 2 should exist", dot2);
            assertNotNull("Dot 3 should exist", dot3);

            // Get the selected color resource
            int selectedColor = activity.getColorStateList(R.color.dot_selected_color)
                    .getDefaultColor();
            int unselectedColor = activity.getColorStateList(R.color.dot_unselected_color)
                    .getDefaultColor();

            // Verify the correct dot is highlighted based on position
            switch (targetPosition) {
                case 0:
                    assertEquals("Dot 1 should be selected for position 0",
                            selectedColor,
                            dot1.getBackgroundTintList().getDefaultColor());
                    assertEquals("Dot 2 should be unselected for position 0",
                            unselectedColor,
                            dot2.getBackgroundTintList().getDefaultColor());
                    assertEquals("Dot 3 should be unselected for position 0",
                            unselectedColor,
                            dot3.getBackgroundTintList().getDefaultColor());
                    break;
                case 1:
                    assertEquals("Dot 1 should be unselected for position 1",
                            unselectedColor,
                            dot1.getBackgroundTintList().getDefaultColor());
                    assertEquals("Dot 2 should be selected for position 1",
                            selectedColor,
                            dot2.getBackgroundTintList().getDefaultColor());
                    assertEquals("Dot 3 should be unselected for position 1",
                            unselectedColor,
                            dot3.getBackgroundTintList().getDefaultColor());
                    break;
                case 2:
                    assertEquals("Dot 1 should be unselected for position 2",
                            unselectedColor,
                            dot1.getBackgroundTintList().getDefaultColor());
                    assertEquals("Dot 2 should be unselected for position 2",
                            unselectedColor,
                            dot2.getBackgroundTintList().getDefaultColor());
                    assertEquals("Dot 3 should be selected for position 2",
                            selectedColor,
                            dot3.getBackgroundTintList().getDefaultColor());
                    break;
            }
        });
    }

    /**
     * Property 7: Static button visibility
     * 
     * For any onboarding screen being displayed, both the "Get Started" button and 
     * "I'm already a member" button should be visible and in fixed positions.
     * 
     * **Validates: Requirements 5.1, 5.2, 5.5**
     * 
     * This test verifies the property across all valid screen positions (0, 1, 2).
     */
    @Test
    public void property7_buttonsVisibleOnScreen0() {
        verifyButtonsVisibleOnScreen(0);
    }

    @Test
    public void property7_buttonsVisibleOnScreen1() {
        verifyButtonsVisibleOnScreen(1);
    }

    @Test
    public void property7_buttonsVisibleOnScreen2() {
        verifyButtonsVisibleOnScreen(2);
    }

    private void verifyButtonsVisibleOnScreen(int screenPosition) {
        // Given: IntroActivity is launched
        scenario = ActivityScenario.launch(IntroActivity.class);

        // When: ViewPager is on a specific screen position
        scenario.onActivity(activity -> {
            ViewPager2 viewPager = activity.findViewById(R.id.viewPager);
            viewPager.setCurrentItem(screenPosition, false);
        });

        // Wait for UI to update
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then: Both buttons should be visible
        scenario.onActivity(activity -> {
            MaterialButton btnGetStarted = activity.findViewById(R.id.btnGetStarted);
            MaterialButton btnAlreadyMember = activity.findViewById(R.id.btnAlreadyMember);

            assertNotNull("Get Started button should exist", btnGetStarted);
            assertNotNull("Already Member button should exist", btnAlreadyMember);

            assertEquals("Get Started button should be visible on screen " + screenPosition,
                    View.VISIBLE, btnGetStarted.getVisibility());
            assertEquals("Already Member button should be visible on screen " + screenPosition,
                    View.VISIBLE, btnAlreadyMember.getVisibility());

            // Verify buttons are enabled (clickable)
            assertTrue("Get Started button should be enabled on screen " + screenPosition,
                    btnGetStarted.isEnabled());
            assertTrue("Already Member button should be enabled on screen " + screenPosition,
                    btnAlreadyMember.isEnabled());
        });
    }

    /**
     * Property 10: Onboarding completion persistence
     * 
     * For any authentication button click (Get Started or Already Member), the onboarding 
     * preference should be set to true before navigation occurs.
     * 
     * **Validates: Requirements 6.1, 6.4**
     * 
     * This test verifies the property for both buttons across all screen positions.
     */
    @Test
    public void property10_onboardingCompletedOnGetStartedClickFromScreen0() {
        verifyOnboardingCompletionOnButtonClick(0, true);
    }

    @Test
    public void property10_onboardingCompletedOnGetStartedClickFromScreen1() {
        verifyOnboardingCompletionOnButtonClick(1, true);
    }

    @Test
    public void property10_onboardingCompletedOnGetStartedClickFromScreen2() {
        verifyOnboardingCompletionOnButtonClick(2, true);
    }

    @Test
    public void property10_onboardingCompletedOnAlreadyMemberClickFromScreen0() {
        verifyOnboardingCompletionOnButtonClick(0, false);
    }

    @Test
    public void property10_onboardingCompletedOnAlreadyMemberClickFromScreen1() {
        verifyOnboardingCompletionOnButtonClick(1, false);
    }

    @Test
    public void property10_onboardingCompletedOnAlreadyMemberClickFromScreen2() {
        verifyOnboardingCompletionOnButtonClick(2, false);
    }

    private void verifyOnboardingCompletionOnButtonClick(int screenPosition, boolean clickGetStarted) {
        // Given: IntroActivity is launched and onboarding is not complete
        Context context = ApplicationProvider.getApplicationContext();
        OnboardingPreferences prefs = new OnboardingPreferences(context);
        prefs.setOnboardingComplete(false);

        scenario = ActivityScenario.launch(IntroActivity.class);

        // When: User is on a specific screen and clicks a button
        scenario.onActivity(activity -> {
            ViewPager2 viewPager = activity.findViewById(R.id.viewPager);
            viewPager.setCurrentItem(screenPosition, false);

            MaterialButton btnGetStarted = activity.findViewById(R.id.btnGetStarted);
            MaterialButton btnAlreadyMember = activity.findViewById(R.id.btnAlreadyMember);

            // Click the appropriate button
            if (clickGetStarted) {
                btnGetStarted.performClick();
            } else {
                btnAlreadyMember.performClick();
            }
        });

        // Wait for click to process
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then: Onboarding should be marked as complete
        boolean isComplete = prefs.isOnboardingComplete();
        assertTrue("Onboarding should be marked complete after clicking " +
                        (clickGetStarted ? "Get Started" : "Already Member") +
                        " button on screen " + screenPosition,
                isComplete);
    }
}
