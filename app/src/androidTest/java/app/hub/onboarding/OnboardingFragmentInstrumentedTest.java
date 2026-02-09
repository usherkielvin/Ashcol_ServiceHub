package app.hub.onboarding;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import app.hub.R;

import static org.junit.Assert.*;

/**
 * Instrumented tests for OnboardingFragment UI rendering and error handling.
 * These tests run on an Android device or emulator with real Android framework components.
 * 
 * Tests cover:
 * - UI elements display correct content (Requirements 1.3, 2.1, 2.2, 2.3)
 * - Error handling for missing drawables (Requirement 7.3)
 * - Fragment lifecycle and view creation
 * - All three screen configurations (S2, S3, S4)
 * 
 * Requirements: 1.3, 2.1, 2.2, 2.3, 7.3
 */
@RunWith(AndroidJUnit4.class)
public class OnboardingFragmentInstrumentedTest {

    @Test
    public void fragmentDisplaysTitleTextInUI() {
        // Given a fragment with specific title text
        // Requirement 2.1: Display screen-specific content
        String expectedTitle = "Stay Cool, Stay Comfortable";
        OnboardingFragment fragment = OnboardingFragment.newInstance(
            R.drawable.pic1,
            expectedTitle,
            "Test Subtitle"
        );

        // When the fragment is launched
        FragmentScenario<OnboardingFragment> scenario = FragmentScenario.launch(
            OnboardingFragment.class,
            fragment.getArguments()
        );

        // Then the title TextView should display the correct text
        scenario.onFragment(f -> {
            View view = f.getView();
            assertNotNull(view);
            TextView tvTitle = view.findViewById(R.id.tvTitle);
            assertNotNull(tvTitle);
            assertEquals(expectedTitle, tvTitle.getText().toString());
        });
    }

    @Test
    public void fragmentDisplaysSubtitleTextInUI() {
        // Given a fragment with specific subtitle text
        // Requirement 2.1: Display screen-specific content
        String expectedSubtitle = "Reliable aircon service for home and office";
        OnboardingFragment fragment = OnboardingFragment.newInstance(
            R.drawable.pic1,
            "Test Title",
            expectedSubtitle
        );

        // When the fragment is launched
        FragmentScenario<OnboardingFragment> scenario = FragmentScenario.launch(
            OnboardingFragment.class,
            fragment.getArguments()
        );

        // Then the subtitle TextView should display the correct text
        scenario.onFragment(f -> {
            View view = f.getView();
            assertNotNull(view);
            TextView tvSubtitle = view.findViewById(R.id.tvSubtitle);
            assertNotNull(tvSubtitle);
            assertEquals(expectedSubtitle, tvSubtitle.getText().toString());
        });
    }

    @Test
    public void fragmentDisplaysLogoInUI() {
        // Given a fragment
        // Requirement 1.3: Display Ashcol logo at the top
        OnboardingFragment fragment = OnboardingFragment.newInstance(
            R.drawable.pic1,
            "Test Title",
            "Test Subtitle"
        );

        // When the fragment is launched
        FragmentScenario<OnboardingFragment> scenario = FragmentScenario.launch(
            OnboardingFragment.class,
            fragment.getArguments()
        );

        // Then the logo ImageView should be present and have a drawable
        scenario.onFragment(f -> {
            View view = f.getView();
            assertNotNull(view);
            ImageView ivLogo = view.findViewById(R.id.ivLogo);
            assertNotNull("Logo ImageView should exist", ivLogo);
            assertNotNull("Logo should have a drawable", ivLogo.getDrawable());
        });
    }

    @Test
    public void fragmentDisplaysBackgroundImageInUI() {
        // Given a fragment with a valid background resource
        // Requirement 1.4: Display background image
        OnboardingFragment fragment = OnboardingFragment.newInstance(
            R.drawable.pic1,
            "Test Title",
            "Test Subtitle"
        );

        // When the fragment is launched
        FragmentScenario<OnboardingFragment> scenario = FragmentScenario.launch(
            OnboardingFragment.class,
            fragment.getArguments()
        );

        // Then the background ImageView should have a drawable
        scenario.onFragment(f -> {
            View view = f.getView();
            assertNotNull(view);
            ImageView ivBackground = view.findViewById(R.id.ivBackground);
            assertNotNull("Background ImageView should exist", ivBackground);
            assertNotNull("Background should have a drawable", ivBackground.getDrawable());
        });
    }

    @Test
    public void fragmentScreen2DisplaysCorrectContent() {
        // Given the configuration for Screen 2 (S2)
        // Requirements 1.4, 2.1: S2 should show pic1 with specific title and subtitle
        OnboardingPage page = OnboardingConfig.getPage(0);
        OnboardingFragment fragment = OnboardingFragment.newInstance(
            page.getBackgroundImageRes(),
            page.getTitleText(),
            page.getSubtitleText()
        );

        // When the fragment is launched
        FragmentScenario<OnboardingFragment> scenario = FragmentScenario.launch(
            OnboardingFragment.class,
            fragment.getArguments()
        );

        // Then all UI elements should display the correct S2 content
        scenario.onFragment(f -> {
            View view = f.getView();
            assertNotNull(view);
            
            TextView tvTitle = view.findViewById(R.id.tvTitle);
            TextView tvSubtitle = view.findViewById(R.id.tvSubtitle);
            ImageView ivBackground = view.findViewById(R.id.ivBackground);
            ImageView ivLogo = view.findViewById(R.id.ivLogo);
            
            assertEquals("Stay Cool, Stay Comfortable", tvTitle.getText().toString());
            assertEquals("Reliable aircon service for home and office", tvSubtitle.getText().toString());
            assertNotNull("Background should be displayed", ivBackground.getDrawable());
            assertNotNull("Logo should be displayed", ivLogo.getDrawable());
        });
    }

    @Test
    public void fragmentScreen3DisplaysCorrectContent() {
        // Given the configuration for Screen 3 (S3)
        // Requirements 1.5, 2.2: S3 should show pic2 with specific title and subtitle
        OnboardingPage page = OnboardingConfig.getPage(1);
        OnboardingFragment fragment = OnboardingFragment.newInstance(
            page.getBackgroundImageRes(),
            page.getTitleText(),
            page.getSubtitleText()
        );

        // When the fragment is launched
        FragmentScenario<OnboardingFragment> scenario = FragmentScenario.launch(
            OnboardingFragment.class,
            fragment.getArguments()
        );

        // Then all UI elements should display the correct S3 content
        scenario.onFragment(f -> {
            View view = f.getView();
            assertNotNull(view);
            
            TextView tvTitle = view.findViewById(R.id.tvTitle);
            TextView tvSubtitle = view.findViewById(R.id.tvSubtitle);
            
            assertEquals("We've Got You Covered!", tvTitle.getText().toString());
            assertEquals("From cleaning, repairs, to installations, our team does it all", 
                        tvSubtitle.getText().toString());
        });
    }

    @Test
    public void fragmentScreen4DisplaysCorrectContent() {
        // Given the configuration for Screen 4 (S4)
        // Requirements 1.6, 2.3: S4 should show pic3 with specific title and subtitle
        OnboardingPage page = OnboardingConfig.getPage(2);
        OnboardingFragment fragment = OnboardingFragment.newInstance(
            page.getBackgroundImageRes(),
            page.getTitleText(),
            page.getSubtitleText()
        );

        // When the fragment is launched
        FragmentScenario<OnboardingFragment> scenario = FragmentScenario.launch(
            OnboardingFragment.class,
            fragment.getArguments()
        );

        // Then all UI elements should display the correct S4 content
        scenario.onFragment(f -> {
            View view = f.getView();
            assertNotNull(view);
            
            TextView tvTitle = view.findViewById(R.id.tvTitle);
            TextView tvSubtitle = view.findViewById(R.id.tvSubtitle);
            
            assertEquals("Let's Get Started", tvTitle.getText().toString());
            assertEquals("Schedule your appointment in just a few taps", 
                        tvSubtitle.getText().toString());
        });
    }

    @Test
    public void fragmentHandlesMissingBackgroundDrawableGracefully() {
        // Given a fragment with an invalid background resource (0)
        // Requirement 7.3: Handle missing resources gracefully
        OnboardingFragment fragment = OnboardingFragment.newInstance(
            0, // Invalid resource ID
            "Test Title",
            "Test Subtitle"
        );

        // When the fragment is launched
        FragmentScenario<OnboardingFragment> scenario = FragmentScenario.launch(
            OnboardingFragment.class,
            fragment.getArguments()
        );

        // Then the fragment should display without crashing
        scenario.onFragment(f -> {
            View view = f.getView();
            assertNotNull("Fragment view should be created despite missing drawable", view);
            
            ImageView ivBackground = view.findViewById(R.id.ivBackground);
            assertNotNull("Background ImageView should exist", ivBackground);
            
            // The fragment should handle the error gracefully (may show fallback color or empty)
            // The key is that it doesn't crash
        });
    }

    @Test
    public void fragmentAllUIElementsArePresent() {
        // Given a fragment with valid configuration
        OnboardingFragment fragment = OnboardingFragment.newInstance(
            R.drawable.pic1,
            "Test Title",
            "Test Subtitle"
        );

        // When the fragment is launched
        FragmentScenario<OnboardingFragment> scenario = FragmentScenario.launch(
            OnboardingFragment.class,
            fragment.getArguments()
        );

        // Then all required UI elements should be present
        scenario.onFragment(f -> {
            View view = f.getView();
            assertNotNull("Fragment view should exist", view);
            
            ImageView ivBackground = view.findViewById(R.id.ivBackground);
            ImageView ivLogo = view.findViewById(R.id.ivLogo);
            TextView tvTitle = view.findViewById(R.id.tvTitle);
            TextView tvSubtitle = view.findViewById(R.id.tvSubtitle);
            
            assertNotNull("Background ImageView should exist", ivBackground);
            assertNotNull("Logo ImageView should exist", ivLogo);
            assertNotNull("Title TextView should exist", tvTitle);
            assertNotNull("Subtitle TextView should exist", tvSubtitle);
        });
    }

    @Test
    public void fragmentTextViewsAreNotEmpty() {
        // Given a fragment with valid text content
        OnboardingFragment fragment = OnboardingFragment.newInstance(
            R.drawable.pic1,
            "Test Title",
            "Test Subtitle"
        );

        // When the fragment is launched
        FragmentScenario<OnboardingFragment> scenario = FragmentScenario.launch(
            OnboardingFragment.class,
            fragment.getArguments()
        );

        // Then the text views should not be empty
        scenario.onFragment(f -> {
            View view = f.getView();
            assertNotNull(view);
            
            TextView tvTitle = view.findViewById(R.id.tvTitle);
            TextView tvSubtitle = view.findViewById(R.id.tvSubtitle);
            
            assertFalse("Title should not be empty", tvTitle.getText().toString().isEmpty());
            assertFalse("Subtitle should not be empty", tvSubtitle.getText().toString().isEmpty());
        });
    }
}
