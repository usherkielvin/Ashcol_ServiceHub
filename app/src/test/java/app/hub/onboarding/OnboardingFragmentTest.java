package app.hub.onboarding;

import org.junit.Test;

import app.hub.R;

import static org.junit.Assert.*;

/**
 * Unit tests for OnboardingFragment.
 * Tests fragment configuration and data integrity.
 * 
 * Note: UI rendering and lifecycle tests are implemented as instrumented tests in
 * OnboardingFragmentInstrumentedTest.java, which run on a device or emulator with
 * real Android framework components. This approach avoids Robolectric compatibility
 * issues with ConstraintLayout.
 * 
 * These unit tests validate:
 * - Configuration correctness for all three screens (S2, S3, S4)
 * - OnboardingConfig data integrity
 * - Edge cases and validation
 * 
 * For comprehensive UI and error handling tests, see: OnboardingFragmentInstrumentedTest.java
 * 
 * Requirements: 1.3, 2.1, 2.2, 2.3, 7.3
 */
public class OnboardingFragmentTest {

    @Test
    public void onboardingConfigScreen2HasCorrectConfiguration() {
        // Given the configuration for Screen 2 (S2)
        // Requirements 1.4, 2.1: S2 should show pic1 with specific title and subtitle
        OnboardingPage page = OnboardingConfig.getPage(0);

        // Then it should have the correct configuration
        assertEquals("S2 should use pic1 background", R.drawable.pic1, page.getBackgroundImageRes());
        assertEquals("S2 should have correct title", 
                    "Stay Cool, Stay Comfortable", page.getTitleText());
        assertEquals("S2 should have correct subtitle", 
                    "Reliable aircon service for home and office", page.getSubtitleText());
    }

    @Test
    public void onboardingConfigScreen3HasCorrectConfiguration() {
        // Given the configuration for Screen 3 (S3)
        // Requirements 1.5, 2.2: S3 should show pic2 with specific title and subtitle
        OnboardingPage page = OnboardingConfig.getPage(1);

        // Then it should have the correct configuration
        assertEquals("S3 should use pic2 background", R.drawable.pic2, page.getBackgroundImageRes());
        assertEquals("S3 should have correct title", 
                    "We've Got You Covered!", page.getTitleText());
        assertEquals("S3 should have correct subtitle", 
                    "From cleaning, repairs, to installations, our team does it all", 
                    page.getSubtitleText());
    }

    @Test
    public void onboardingConfigScreen4HasCorrectConfiguration() {
        // Given the configuration for Screen 4 (S4)
        // Requirements 1.6, 2.3: S4 should show pic3 with specific title and subtitle
        OnboardingPage page = OnboardingConfig.getPage(2);

        // Then it should have the correct configuration
        assertEquals("S4 should use pic3 background", R.drawable.pic3, page.getBackgroundImageRes());
        assertEquals("S4 should have correct title", 
                    "Let's Get Started", page.getTitleText());
        assertEquals("S4 should have correct subtitle", 
                    "Schedule your appointment in just a few taps", page.getSubtitleText());
    }

    @Test
    public void onboardingConfigHasExactlyThreeScreens() {
        // When accessing the onboarding configuration
        // Then it should contain exactly 3 screens
        assertEquals("OnboardingConfig should have exactly 3 screens", 
                    3, OnboardingConfig.getPages().size());
        assertEquals("PAGE_COUNT constant should be 3", 
                    3, OnboardingConfig.PAGE_COUNT);
    }

    @Test
    public void onboardingConfigAllScreensHaveValidData() {
        // When iterating through all onboarding screens
        for (int i = 0; i < OnboardingConfig.PAGE_COUNT; i++) {
            OnboardingPage page = OnboardingConfig.getPage(i);
            
            // Then each screen should have valid data
            assertNotEquals("Screen " + i + " should have non-zero background resource", 
                          0, page.getBackgroundImageRes());
            assertNotNull("Screen " + i + " should have non-null title", 
                         page.getTitleText());
            assertFalse("Screen " + i + " should have non-empty title", 
                       page.getTitleText().trim().isEmpty());
            assertNotNull("Screen " + i + " should have non-null subtitle", 
                         page.getSubtitleText());
            assertFalse("Screen " + i + " should have non-empty subtitle", 
                       page.getSubtitleText().trim().isEmpty());
        }
    }

    @Test
    public void onboardingConfigScreensHaveUniqueBackgrounds() {
        // When accessing all three screens
        OnboardingPage page1 = OnboardingConfig.getPage(0);
        OnboardingPage page2 = OnboardingConfig.getPage(1);
        OnboardingPage page3 = OnboardingConfig.getPage(2);

        // Then each screen should have a unique background image
        assertNotEquals("S2 and S3 should have different backgrounds", 
                       page1.getBackgroundImageRes(), page2.getBackgroundImageRes());
        assertNotEquals("S2 and S4 should have different backgrounds", 
                       page1.getBackgroundImageRes(), page3.getBackgroundImageRes());
        assertNotEquals("S3 and S4 should have different backgrounds", 
                       page2.getBackgroundImageRes(), page3.getBackgroundImageRes());
    }

    @Test
    public void onboardingConfigScreensHaveUniqueTitles() {
        // When accessing all three screens
        OnboardingPage page1 = OnboardingConfig.getPage(0);
        OnboardingPage page2 = OnboardingConfig.getPage(1);
        OnboardingPage page3 = OnboardingConfig.getPage(2);

        // Then each screen should have a unique title
        assertNotEquals("S2 and S3 should have different titles", 
                       page1.getTitleText(), page2.getTitleText());
        assertNotEquals("S2 and S4 should have different titles", 
                       page1.getTitleText(), page3.getTitleText());
        assertNotEquals("S3 and S4 should have different titles", 
                       page2.getTitleText(), page3.getTitleText());
    }

    @Test
    public void onboardingConfigUsesCorrectDrawableResources() {
        // When accessing the onboarding configuration
        // Then it should use the correct drawable resources for each screen
        assertEquals("S2 should use pic1", R.drawable.pic1, OnboardingConfig.getPage(0).getBackgroundImageRes());
        assertEquals("S3 should use pic2", R.drawable.pic2, OnboardingConfig.getPage(1).getBackgroundImageRes());
        assertEquals("S4 should use pic3", R.drawable.pic3, OnboardingConfig.getPage(2).getBackgroundImageRes());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void onboardingConfigThrowsExceptionForInvalidIndex() {
        // When accessing an invalid screen index
        // Then it should throw IndexOutOfBoundsException
        OnboardingConfig.getPage(3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void onboardingConfigThrowsExceptionForNegativeIndex() {
        // When accessing a negative screen index
        // Then it should throw IndexOutOfBoundsException
        OnboardingConfig.getPage(-1);
    }

    @Test
    public void onboardingPageDataClassWorksCorrectly() {
        // Given an OnboardingPage instance
        OnboardingPage page = new OnboardingPage(
            R.drawable.pic1,
            "Test Title",
            "Test Subtitle"
        );

        // Then it should store and return the correct values
        assertEquals(R.drawable.pic1, page.getBackgroundImageRes());
        assertEquals("Test Title", page.getTitleText());
        assertEquals("Test Subtitle", page.getSubtitleText());
    }

    @Test(expected = IllegalArgumentException.class)
    public void onboardingPageRejectsZeroBackgroundResource() {
        // When creating an OnboardingPage with zero background resource
        // Then it should throw IllegalArgumentException
        new OnboardingPage(0, "Title", "Subtitle");
    }

    @Test(expected = IllegalArgumentException.class)
    public void onboardingPageRejectsEmptyTitle() {
        // When creating an OnboardingPage with empty title
        // Then it should throw IllegalArgumentException
        new OnboardingPage(R.drawable.pic1, "", "Subtitle");
    }

    @Test(expected = IllegalArgumentException.class)
    public void onboardingPageRejectsEmptySubtitle() {
        // When creating an OnboardingPage with empty subtitle
        // Then it should throw IllegalArgumentException
        new OnboardingPage(R.drawable.pic1, "Title", "");
    }

    @Test
    public void onboardingFragmentClassExists() {
        // Verify that the OnboardingFragment class is properly defined
        assertNotNull("OnboardingFragment class should exist", OnboardingFragment.class);
    }

    @Test
    public void onboardingFragmentHasNewInstanceMethod() throws NoSuchMethodException {
        // Verify that newInstance() factory method exists with correct signature
        var method = OnboardingFragment.class.getMethod(
            "newInstance", 
            int.class, 
            String.class, 
            String.class
        );
        assertNotNull("newInstance() method should exist", method);
        assertEquals("newInstance() should return OnboardingFragment", 
                    OnboardingFragment.class, method.getReturnType());
    }

    @Test
    public void onboardingFragmentHasGetterMethods() throws NoSuchMethodException {
        // Verify that getter methods exist for testing purposes
        assertNotNull("getBackgroundImageRes() should exist", 
                     OnboardingFragment.class.getMethod("getBackgroundImageRes"));
        assertNotNull("getTitleText() should exist", 
                     OnboardingFragment.class.getMethod("getTitleText"));
        assertNotNull("getSubtitleText() should exist", 
                     OnboardingFragment.class.getMethod("getSubtitleText"));
    }
}
