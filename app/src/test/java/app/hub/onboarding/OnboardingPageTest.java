package app.hub.onboarding;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for OnboardingPage data class.
 * Tests validation rules and data integrity.
 */
public class OnboardingPageTest {

    @Test
    public void createOnboardingPageWithValidDataSucceeds() {
        // Given valid data
        int backgroundRes = 123;
        String title = "Test Title";
        String subtitle = "Test Subtitle";

        // When creating an OnboardingPage
        OnboardingPage page = new OnboardingPage(backgroundRes, title, subtitle);

        // Then all properties should be set correctly
        assertEquals(backgroundRes, page.getBackgroundImageRes());
        assertEquals(title, page.getTitleText());
        assertEquals(subtitle, page.getSubtitleText());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createOnboardingPageWithZeroBackgroundImageResThrowsException() {
        // When creating an OnboardingPage with backgroundImageRes = 0
        new OnboardingPage(0, "Test Title", "Test Subtitle");
        // Then IllegalArgumentException should be thrown
    }

    @Test(expected = IllegalArgumentException.class)
    public void createOnboardingPageWithEmptyTitleTextThrowsException() {
        // When creating an OnboardingPage with empty titleText
        new OnboardingPage(123, "", "Test Subtitle");
        // Then IllegalArgumentException should be thrown
    }

    @Test(expected = IllegalArgumentException.class)
    public void createOnboardingPageWithBlankTitleTextThrowsException() {
        // When creating an OnboardingPage with blank titleText (only whitespace)
        new OnboardingPage(123, "   ", "Test Subtitle");
        // Then IllegalArgumentException should be thrown
    }

    @Test(expected = IllegalArgumentException.class)
    public void createOnboardingPageWithNullTitleTextThrowsException() {
        // When creating an OnboardingPage with null titleText
        new OnboardingPage(123, null, "Test Subtitle");
        // Then IllegalArgumentException should be thrown
    }

    @Test(expected = IllegalArgumentException.class)
    public void createOnboardingPageWithEmptySubtitleTextThrowsException() {
        // When creating an OnboardingPage with empty subtitleText
        new OnboardingPage(123, "Test Title", "");
        // Then IllegalArgumentException should be thrown
    }

    @Test(expected = IllegalArgumentException.class)
    public void createOnboardingPageWithBlankSubtitleTextThrowsException() {
        // When creating an OnboardingPage with blank subtitleText (only whitespace)
        new OnboardingPage(123, "Test Title", "   ");
        // Then IllegalArgumentException should be thrown
    }

    @Test(expected = IllegalArgumentException.class)
    public void createOnboardingPageWithNullSubtitleTextThrowsException() {
        // When creating an OnboardingPage with null subtitleText
        new OnboardingPage(123, "Test Title", null);
        // Then IllegalArgumentException should be thrown
    }

    @Test
    public void onboardingPageEqualityWorksCorrectly() {
        // Given two OnboardingPage instances with same data
        OnboardingPage page1 = new OnboardingPage(123, "Test Title", "Test Subtitle");
        OnboardingPage page2 = new OnboardingPage(123, "Test Title", "Test Subtitle");

        // Then they should be equal
        assertEquals(page1, page2);
        assertEquals(page1.hashCode(), page2.hashCode());
    }

    @Test
    public void onboardingPageInequalityWorksCorrectly() {
        // Given two OnboardingPage instances with different data
        OnboardingPage page1 = new OnboardingPage(123, "Test Title", "Test Subtitle");
        OnboardingPage page2 = new OnboardingPage(456, "Different Title", "Different Subtitle");

        // Then they should not be equal
        assertNotEquals(page1, page2);
    }

    @Test
    public void onboardingPageToStringContainsAllFields() {
        // Given an OnboardingPage
        OnboardingPage page = new OnboardingPage(123, "Test Title", "Test Subtitle");

        // When calling toString
        String result = page.toString();

        // Then it should contain all field values
        assertTrue(result.contains("123"));
        assertTrue(result.contains("Test Title"));
        assertTrue(result.contains("Test Subtitle"));
    }
}
