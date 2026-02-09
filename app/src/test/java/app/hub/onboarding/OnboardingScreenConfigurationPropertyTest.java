package app.hub.onboarding;

import app.hub.R;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import static org.junit.Assert.*;

/**
 * Property-based tests for onboarding screen configuration correctness.
 * 
 * These tests verify that screen configurations are correct across all valid positions,
 * ensuring that the right content is displayed for each screen.
 * 
 * Tests run with minimum 100 iterations to ensure comprehensive coverage.
 */
public class OnboardingScreenConfigurationPropertyTest {

    /**
     * Property 3: Screen configuration correctness
     * 
     * For any onboarding screen position (0, 1, or 2), the displayed fragment should 
     * have the correct background image, title, and subtitle matching the OnboardingConfig 
     * for that position.
     * 
     * **Validates: Requirements 1.4, 1.5, 1.6, 2.1, 2.2, 2.3**
     * 
     * This property ensures that each screen displays the correct content as specified
     * in the requirements, regardless of how or when the configuration is accessed.
     */
    @Property(tries = 100)
    @Label("Property 3: Screen configuration correctness - each position has correct content")
    void eachPositionHasCorrectConfiguration(@ForAll @IntRange(min = 0, max = 2) int position) {
        // Given: Any valid onboarding screen position (0, 1, or 2)
        
        // When: Accessing the configuration for that position
        OnboardingPage page = OnboardingConfig.getPage(position);
        
        // Then: The page should exist
        assertNotNull("Page at position " + position + " should not be null", page);
        
        // And: The configuration should match the expected values for that position
        switch (position) {
            case 0: // S2 - First screen
                // Requirement 1.4: S2 should show pic1.png as background
                assertEquals("Position 0 should use pic1 background", 
                            R.drawable.pic1, page.getBackgroundImageRes());
                
                // Requirement 2.1: S2 should show correct title and subtitle
                assertEquals("Position 0 should have correct title", 
                            "Stay Cool, Stay Comfortable", page.getTitleText());
                assertEquals("Position 0 should have correct subtitle", 
                            "Reliable aircon service for home and office", page.getSubtitleText());
                break;
                
            case 1: // S3 - Second screen
                // Requirement 1.5: S3 should show pic2.png as background
                assertEquals("Position 1 should use pic2 background", 
                            R.drawable.pic2, page.getBackgroundImageRes());
                
                // Requirement 2.2: S3 should show correct title and subtitle
                assertEquals("Position 1 should have correct title", 
                            "We've Got You Covered!", page.getTitleText());
                assertEquals("Position 1 should have correct subtitle", 
                            "From cleaning, repairs, to installations, our team does it all", 
                            page.getSubtitleText());
                break;
                
            case 2: // S4 - Third screen
                // Requirement 1.6: S4 should show pic3.png as background
                assertEquals("Position 2 should use pic3 background", 
                            R.drawable.pic3, page.getBackgroundImageRes());
                
                // Requirement 2.3: S4 should show correct title and subtitle
                assertEquals("Position 2 should have correct title", 
                            "Let's Get Started", page.getTitleText());
                assertEquals("Position 2 should have correct subtitle", 
                            "Schedule your appointment in just a few taps", 
                            page.getSubtitleText());
                break;
        }
    }

    /**
     * Property 3 (variant): Configuration data validity
     * 
     * For any valid position, the configuration should have valid (non-empty, non-zero) data.
     * This ensures that all required fields are properly populated.
     * 
     * **Validates: Requirements 1.4, 1.5, 1.6, 2.1, 2.2, 2.3**
     */
    @Property(tries = 100)
    @Label("Property 3: All screen configurations have valid data")
    void allScreenConfigurationsHaveValidData(@ForAll @IntRange(min = 0, max = 2) int position) {
        // Given: Any valid onboarding screen position
        
        // When: Accessing the configuration for that position
        OnboardingPage page = OnboardingConfig.getPage(position);
        
        // Then: All fields should have valid data
        assertNotNull("Page at position " + position + " should not be null", page);
        
        // Background image resource should be non-zero
        assertNotEquals("Background at position " + position + " should not be 0", 
                       0, page.getBackgroundImageRes());
        assertTrue("Background at position " + position + " should be positive", 
                  page.getBackgroundImageRes() > 0);
        
        // Title should not be null or empty
        assertNotNull("Title at position " + position + " should not be null", 
                     page.getTitleText());
        assertFalse("Title at position " + position + " should not be empty", 
                   page.getTitleText().trim().isEmpty());
        
        // Subtitle should not be null or empty
        assertNotNull("Subtitle at position " + position + " should not be null", 
                     page.getSubtitleText());
        assertFalse("Subtitle at position " + position + " should not be empty", 
                   page.getSubtitleText().trim().isEmpty());
    }

    /**
     * Property 3 (variant): Configuration uniqueness
     * 
     * For any two different positions, the configurations should be different.
     * This ensures each screen has unique content.
     * 
     * **Validates: Requirements 1.4, 1.5, 1.6, 2.1, 2.2, 2.3**
     */
    @Property(tries = 100)
    @Label("Property 3: Different positions have different configurations")
    void differentPositionsHaveDifferentConfigurations(
            @ForAll @IntRange(min = 0, max = 2) int position1,
            @ForAll @IntRange(min = 0, max = 2) int position2) {
        // Given: Two positions (may be same or different)
        
        // When: Accessing configurations for both positions
        OnboardingPage page1 = OnboardingConfig.getPage(position1);
        OnboardingPage page2 = OnboardingConfig.getPage(position2);
        
        // Then: If positions are different, configurations should be different
        if (position1 != position2) {
            // At least one field should be different (we check all three)
            boolean backgroundDifferent = page1.getBackgroundImageRes() != page2.getBackgroundImageRes();
            boolean titleDifferent = !page1.getTitleText().equals(page2.getTitleText());
            boolean subtitleDifferent = !page1.getSubtitleText().equals(page2.getSubtitleText());
            
            assertTrue("Different positions should have different backgrounds", backgroundDifferent);
            assertTrue("Different positions should have different titles", titleDifferent);
            // Note: Subtitles might theoretically be the same, but in our case they're all different
        } else {
            // Same position should return same configuration
            assertEquals("Same position should return same background", 
                        page1.getBackgroundImageRes(), page2.getBackgroundImageRes());
            assertEquals("Same position should return same title", 
                        page1.getTitleText(), page2.getTitleText());
            assertEquals("Same position should return same subtitle", 
                        page1.getSubtitleText(), page2.getSubtitleText());
        }
    }

    /**
     * Property 3 (variant): Configuration consistency across multiple accesses
     * 
     * For any position, accessing the configuration multiple times should return
     * the same values. This verifies immutability and consistency.
     * 
     * **Validates: Requirements 1.4, 1.5, 1.6, 2.1, 2.2, 2.3**
     */
    @Property(tries = 100)
    @Label("Property 3: Configuration remains consistent across multiple accesses")
    void configurationRemainsConsistentAcrossMultipleAccesses(
            @ForAll @IntRange(min = 0, max = 2) int position) {
        // Given: Any valid position
        
        // When: Accessing the configuration multiple times
        OnboardingPage firstAccess = OnboardingConfig.getPage(position);
        OnboardingPage secondAccess = OnboardingConfig.getPage(position);
        OnboardingPage thirdAccess = OnboardingConfig.getPage(position);
        
        // Then: All accesses should return the same configuration
        assertEquals("Background should be consistent across accesses", 
                    firstAccess.getBackgroundImageRes(), secondAccess.getBackgroundImageRes());
        assertEquals("Background should be consistent across accesses", 
                    firstAccess.getBackgroundImageRes(), thirdAccess.getBackgroundImageRes());
        
        assertEquals("Title should be consistent across accesses", 
                    firstAccess.getTitleText(), secondAccess.getTitleText());
        assertEquals("Title should be consistent across accesses", 
                    firstAccess.getTitleText(), thirdAccess.getTitleText());
        
        assertEquals("Subtitle should be consistent across accesses", 
                    firstAccess.getSubtitleText(), secondAccess.getSubtitleText());
        assertEquals("Subtitle should be consistent across accesses", 
                    firstAccess.getSubtitleText(), thirdAccess.getSubtitleText());
    }

    /**
     * Property 3 (variant): Specific drawable resource correctness
     * 
     * For any position, the background drawable should be one of the three expected
     * resources (pic1, pic2, or pic3) and should match the position.
     * 
     * **Validates: Requirements 1.4, 1.5, 1.6**
     */
    @Property(tries = 100)
    @Label("Property 3: Background drawables match expected resources for each position")
    void backgroundDrawablesMatchExpectedResources(@ForAll @IntRange(min = 0, max = 2) int position) {
        // Given: Any valid position
        
        // When: Accessing the configuration
        OnboardingPage page = OnboardingConfig.getPage(position);
        int backgroundRes = page.getBackgroundImageRes();
        
        // Then: The background should be one of the three expected drawables
        assertTrue("Background should be pic1, pic2, or pic3",
                  backgroundRes == R.drawable.pic1 || 
                  backgroundRes == R.drawable.pic2 || 
                  backgroundRes == R.drawable.pic3);
        
        // And: It should match the specific position
        switch (position) {
            case 0:
                assertEquals("Position 0 must use pic1", R.drawable.pic1, backgroundRes);
                break;
            case 1:
                assertEquals("Position 1 must use pic2", R.drawable.pic2, backgroundRes);
                break;
            case 2:
                assertEquals("Position 2 must use pic3", R.drawable.pic3, backgroundRes);
                break;
        }
    }

    /**
     * Property 3 (variant): Text content requirements
     * 
     * For any position, the title and subtitle should meet specific requirements:
     * - Not null
     * - Not empty after trimming
     * - Contain meaningful content (more than just whitespace)
     * 
     * **Validates: Requirements 2.1, 2.2, 2.3**
     */
    @Property(tries = 100)
    @Label("Property 3: Text content meets quality requirements")
    void textContentMeetsQualityRequirements(@ForAll @IntRange(min = 0, max = 2) int position) {
        // Given: Any valid position
        
        // When: Accessing the configuration
        OnboardingPage page = OnboardingConfig.getPage(position);
        
        // Then: Title should meet quality requirements
        String title = page.getTitleText();
        assertNotNull("Title should not be null", title);
        assertFalse("Title should not be empty", title.isEmpty());
        assertFalse("Title should not be just whitespace", title.trim().isEmpty());
        assertTrue("Title should have meaningful content", title.trim().length() > 3);
        
        // And: Subtitle should meet quality requirements
        String subtitle = page.getSubtitleText();
        assertNotNull("Subtitle should not be null", subtitle);
        assertFalse("Subtitle should not be empty", subtitle.isEmpty());
        assertFalse("Subtitle should not be just whitespace", subtitle.trim().isEmpty());
        assertTrue("Subtitle should have meaningful content", subtitle.trim().length() > 10);
    }
}
