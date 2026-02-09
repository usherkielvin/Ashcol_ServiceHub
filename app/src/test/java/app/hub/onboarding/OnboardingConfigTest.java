package app.hub.onboarding;

import app.hub.R;
import org.junit.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for OnboardingConfig class.
 * Tests configuration correctness and validates requirements 1.4, 1.5, 1.6, 2.1, 2.2, 2.3.
 */
public class OnboardingConfigTest {

    @Test
    public void onboardingConfigContainsExactlyThreePages() {
        // When accessing the pages list
        List<OnboardingPage> pages = OnboardingConfig.getPages();

        // Then it should contain exactly 3 pages (Requirement 1.2)
        assertEquals(3, pages.size());
        assertEquals(3, OnboardingConfig.PAGE_COUNT);
    }

    @Test
    public void onboardingConfigFirstPageHasCorrectConfiguration() {
        // When accessing the first page (S2)
        OnboardingPage page = OnboardingConfig.getPage(0);

        // Then it should have pic1 background (Requirement 1.4)
        assertEquals(R.drawable.pic1, page.getBackgroundImageRes());
        
        // And correct title and subtitle (Requirement 2.1)
        assertEquals("Stay Cool, Stay Comfortable", page.getTitleText());
        assertEquals("Reliable aircon service for home and office", page.getSubtitleText());
    }

    @Test
    public void onboardingConfigSecondPageHasCorrectConfiguration() {
        // When accessing the second page (S3)
        OnboardingPage page = OnboardingConfig.getPage(1);

        // Then it should have pic2 background (Requirement 1.5)
        assertEquals(R.drawable.pic2, page.getBackgroundImageRes());
        
        // And correct title and subtitle (Requirement 2.2)
        assertEquals("We've Got You Covered!", page.getTitleText());
        assertEquals("From cleaning, repairs, to installations, our team does it all", page.getSubtitleText());
    }

    @Test
    public void onboardingConfigThirdPageHasCorrectConfiguration() {
        // When accessing the third page (S4)
        OnboardingPage page = OnboardingConfig.getPage(2);

        // Then it should have pic3 background (Requirement 1.6)
        assertEquals(R.drawable.pic3, page.getBackgroundImageRes());
        
        // And correct title and subtitle (Requirement 2.3)
        assertEquals("Let's Get Started", page.getTitleText());
        assertEquals("Schedule your appointment in just a few taps", page.getSubtitleText());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void onboardingConfigPagesListIsImmutable() {
        // When accessing the pages list
        List<OnboardingPage> pages = OnboardingConfig.getPages();

        // Then attempting to modify it should throw UnsupportedOperationException
        pages.add(new OnboardingPage(999, "New Title", "New Subtitle"));
    }

    @Test
    public void onboardingConfigAllPagesHaveValidData() {
        // When iterating through all pages
        List<OnboardingPage> pages = OnboardingConfig.getPages();
        
        for (OnboardingPage page : pages) {
            // Then each page should have valid data
            assertNotEquals(0, page.getBackgroundImageRes());
            assertNotNull(page.getTitleText());
            assertFalse(page.getTitleText().trim().isEmpty());
            assertNotNull(page.getSubtitleText());
            assertFalse(page.getSubtitleText().trim().isEmpty());
        }
    }

    @Test
    public void onboardingConfigPagesHaveUniqueBackgroundImages() {
        // When accessing all pages
        List<OnboardingPage> pages = OnboardingConfig.getPages();
        Set<Integer> backgroundImages = new HashSet<>();
        
        for (OnboardingPage page : pages) {
            backgroundImages.add(page.getBackgroundImageRes());
        }

        // Then all background images should be unique
        assertEquals(pages.size(), backgroundImages.size());
    }

    @Test
    public void onboardingConfigPagesHaveUniqueTitles() {
        // When accessing all pages
        List<OnboardingPage> pages = OnboardingConfig.getPages();
        Set<String> titles = new HashSet<>();
        
        for (OnboardingPage page : pages) {
            titles.add(page.getTitleText());
        }

        // Then all titles should be unique
        assertEquals(pages.size(), titles.size());
    }

    @Test
    public void onboardingConfigPageCountMatchesActualPageCount() {
        // When comparing PAGE_COUNT constant with actual pages size
        // Then they should match
        assertEquals(OnboardingConfig.PAGE_COUNT, OnboardingConfig.getPages().size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void onboardingConfigGetPageWithInvalidPositionThrowsException() {
        // When accessing a page with invalid position
        OnboardingConfig.getPage(3);
        // Then IndexOutOfBoundsException should be thrown
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void onboardingConfigGetPageWithNegativePositionThrowsException() {
        // When accessing a page with negative position
        OnboardingConfig.getPage(-1);
        // Then IndexOutOfBoundsException should be thrown
    }

    @Test
    public void onboardingConfigCannotBeInstantiated() throws Exception {
        // When attempting to instantiate OnboardingConfig via reflection
        java.lang.reflect.Constructor<OnboardingConfig> constructor = 
            OnboardingConfig.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        try {
            constructor.newInstance();
            fail("Expected AssertionError to be thrown");
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Then the cause should be an AssertionError
            assertTrue(e.getCause() instanceof AssertionError);
        }
    }
}
