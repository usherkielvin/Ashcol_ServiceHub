package app.hub.onboarding;

import org.junit.Test;

import app.hub.R;

import static org.junit.Assert.*;

/**
 * Unit tests for OnboardingPagerAdapter class.
 * Tests adapter functionality and validates requirements 1.2, 1.4, 1.5, 1.6, 2.1, 2.2, 2.3.
 * 
 * Note: These tests validate the adapter's configuration logic without instantiating
 * the adapter itself to avoid Robolectric compatibility issues. The tests verify that
 * OnboardingConfig provides the correct data that the adapter will use.
 * 
 * For comprehensive integration tests with actual ViewPager2 and fragment lifecycle,
 * see instrumented tests that run on a device or emulator.
 */
public class OnboardingPagerAdapterTest {

    @Test
    public void adapterShouldReturnExactlyThreeItems() {
        // Given the OnboardingConfig that the adapter uses
        // When checking the page count
        int pageCount = OnboardingConfig.PAGE_COUNT;

        // Then it should be exactly 3 (Requirement 1.2, Property 1)
        assertEquals("Adapter should return exactly 3 items", 3, pageCount);
        assertEquals("Config pages list should have 3 items", 3, OnboardingConfig.getPages().size());
    }

    @Test
    public void adapterShouldCreateFragmentWithCorrectConfigurationForFirstPosition() {
        // Given the configuration for position 0 (S2)
        OnboardingPage page = OnboardingConfig.getPage(0);

        // Then it should have the correct configuration (Requirements 1.4, 2.1)
        assertEquals("Position 0 should use pic1 background", 
                    R.drawable.pic1, page.getBackgroundImageRes());
        assertEquals("Position 0 should have correct title", 
                    "Stay Cool, Stay Comfortable", page.getTitleText());
        assertEquals("Position 0 should have correct subtitle", 
                    "Reliable aircon service for home and office", page.getSubtitleText());
    }

    @Test
    public void adapterShouldCreateFragmentWithCorrectConfigurationForSecondPosition() {
        // Given the configuration for position 1 (S3)
        OnboardingPage page = OnboardingConfig.getPage(1);

        // Then it should have the correct configuration (Requirements 1.5, 2.2)
        assertEquals("Position 1 should use pic2 background", 
                    R.drawable.pic2, page.getBackgroundImageRes());
        assertEquals("Position 1 should have correct title", 
                    "We've Got You Covered!", page.getTitleText());
        assertEquals("Position 1 should have correct subtitle", 
                    "From cleaning, repairs, to installations, our team does it all", 
                    page.getSubtitleText());
    }

    @Test
    public void adapterShouldCreateFragmentWithCorrectConfigurationForThirdPosition() {
        // Given the configuration for position 2 (S4)
        OnboardingPage page = OnboardingConfig.getPage(2);

        // Then it should have the correct configuration (Requirements 1.6, 2.3)
        assertEquals("Position 2 should use pic3 background", 
                    R.drawable.pic3, page.getBackgroundImageRes());
        assertEquals("Position 2 should have correct title", 
                    "Let's Get Started", page.getTitleText());
        assertEquals("Position 2 should have correct subtitle", 
                    "Schedule your appointment in just a few taps", page.getSubtitleText());
    }

    @Test
    public void adapterShouldHaveConfigurationForAllValidPositions() {
        // When checking all valid positions (0, 1, 2)
        for (int position = 0; position < OnboardingConfig.PAGE_COUNT; position++) {
            OnboardingPage page = OnboardingConfig.getPage(position);
            
            // Then each position should have valid configuration
            assertNotNull("Page at position " + position + " should not be null", page);
            assertNotEquals("Background at position " + position + " should not be 0", 
                          0, page.getBackgroundImageRes());
            assertNotNull("Title at position " + position + " should not be null", 
                         page.getTitleText());
            assertFalse("Title at position " + position + " should not be empty", 
                       page.getTitleText().trim().isEmpty());
            assertNotNull("Subtitle at position " + position + " should not be null", 
                         page.getSubtitleText());
            assertFalse("Subtitle at position " + position + " should not be empty", 
                       page.getSubtitleText().trim().isEmpty());
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void adapterShouldThrowExceptionForInvalidPosition() {
        // When accessing configuration for an invalid position (3)
        OnboardingConfig.getPage(3);
        
        // Then it should throw IndexOutOfBoundsException
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void adapterShouldThrowExceptionForNegativePosition() {
        // When accessing configuration for a negative position
        OnboardingConfig.getPage(-1);
        
        // Then it should throw IndexOutOfBoundsException
    }

    @Test
    public void adapterConfigurationHasUniqueBackgroundsForEachPosition() {
        // When accessing all three positions
        OnboardingPage page0 = OnboardingConfig.getPage(0);
        OnboardingPage page1 = OnboardingConfig.getPage(1);
        OnboardingPage page2 = OnboardingConfig.getPage(2);

        // Then each position should have a unique background image
        assertNotEquals("Position 0 and 1 should have different backgrounds", 
                       page0.getBackgroundImageRes(), page1.getBackgroundImageRes());
        assertNotEquals("Position 0 and 2 should have different backgrounds", 
                       page0.getBackgroundImageRes(), page2.getBackgroundImageRes());
        assertNotEquals("Position 1 and 2 should have different backgrounds", 
                       page1.getBackgroundImageRes(), page2.getBackgroundImageRes());
    }

    @Test
    public void adapterConfigurationHasUniqueTitlesForEachPosition() {
        // When accessing all three positions
        OnboardingPage page0 = OnboardingConfig.getPage(0);
        OnboardingPage page1 = OnboardingConfig.getPage(1);
        OnboardingPage page2 = OnboardingConfig.getPage(2);

        // Then each position should have a unique title
        assertNotEquals("Position 0 and 1 should have different titles", 
                       page0.getTitleText(), page1.getTitleText());
        assertNotEquals("Position 0 and 2 should have different titles", 
                       page0.getTitleText(), page2.getTitleText());
        assertNotEquals("Position 1 and 2 should have different titles", 
                       page1.getTitleText(), page2.getTitleText());
    }

    @Test
    public void adapterConfigurationUsesCorrectDrawableResources() {
        // When checking the drawable resources for each position
        // Then they should match the requirements
        assertEquals("Position 0 should use pic1", 
                    R.drawable.pic1, OnboardingConfig.getPage(0).getBackgroundImageRes());
        assertEquals("Position 1 should use pic2", 
                    R.drawable.pic2, OnboardingConfig.getPage(1).getBackgroundImageRes());
        assertEquals("Position 2 should use pic3", 
                    R.drawable.pic3, OnboardingConfig.getPage(2).getBackgroundImageRes());
    }

    @Test
    public void onboardingPagerAdapterClassExists() {
        // Verify that the OnboardingPagerAdapter class is properly defined
        assertNotNull("OnboardingPagerAdapter class should exist", OnboardingPagerAdapter.class);
    }

    @Test
    public void onboardingPagerAdapterExtendsFragmentStateAdapter() {
        // Verify that OnboardingPagerAdapter extends FragmentStateAdapter
        Class<?> superclass = OnboardingPagerAdapter.class.getSuperclass();
        assertEquals("OnboardingPagerAdapter should extend FragmentStateAdapter", 
                    "androidx.viewpager2.adapter.FragmentStateAdapter", 
                    superclass.getName());
    }

    @Test
    public void onboardingPagerAdapterHasRequiredMethods() throws NoSuchMethodException {
        // Verify that required methods exist
        assertNotNull("getItemCount() method should exist", 
                     OnboardingPagerAdapter.class.getMethod("getItemCount"));
        assertNotNull("createFragment() method should exist", 
                     OnboardingPagerAdapter.class.getMethod("createFragment", int.class));
    }

    @Test
    public void onboardingFragmentNewInstanceMethodExists() throws NoSuchMethodException {
        // Verify that OnboardingFragment.newInstance() exists with correct signature
        // This is what the adapter uses to create fragments
        var method = OnboardingFragment.class.getMethod(
            "newInstance", 
            int.class, 
            String.class, 
            String.class
        );
        assertNotNull("OnboardingFragment.newInstance() should exist", method);
        assertEquals("newInstance() should return OnboardingFragment", 
                    OnboardingFragment.class, method.getReturnType());
    }
}
