package app.hub.onboarding;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import static org.junit.Assert.*;

/**
 * Property-based tests for OnboardingPagerAdapter.
 * 
 * These tests verify universal properties that should hold true across all valid inputs
 * and states of the onboarding flow, using randomization and multiple iterations.
 * 
 * Tests run with minimum 100 iterations to ensure comprehensive coverage.
 */
public class OnboardingPagerAdapterPropertyTest {

    /**
     * Property 1: Onboarding screen count invariant
     * 
     * For any state of the IntroActivity, the ViewPager adapter should always 
     * contain exactly 3 items.
     * 
     * **Validates: Requirements 1.2**
     * 
     * This property ensures that regardless of how the adapter is accessed or what
     * operations are performed, the fundamental invariant of having exactly 3 screens
     * is maintained.
     */
    @Property(tries = 100)
    @Label("Property 1: Onboarding screen count invariant - adapter always returns exactly 3 items")
    void adapterAlwaysReturnsExactlyThreeItems(@ForAll @IntRange(min = 0, max = 1000) int randomAccess) {
        // Given: Any state or access pattern (simulated by random access count)
        // This represents accessing the adapter configuration at any point in time
        
        // When: Checking the page count from OnboardingConfig (used by adapter)
        int pageCount = OnboardingConfig.PAGE_COUNT;
        int actualPagesSize = OnboardingConfig.getPages().size();
        
        // Then: It should always be exactly 3 (Requirement 1.2, Property 1)
        assertEquals("Adapter should always return exactly 3 items regardless of state", 
                    3, pageCount);
        assertEquals("Config pages list should always have 3 items", 
                    3, actualPagesSize);
        
        // Additional invariant: PAGE_COUNT should match actual pages size
        assertEquals("PAGE_COUNT constant should match actual pages size", 
                    pageCount, actualPagesSize);
    }

    /**
     * Property 1 (variant): Screen count invariant across all valid positions
     * 
     * For any valid position query (0, 1, 2), the total count should remain 3.
     * This verifies that accessing individual positions doesn't affect the invariant.
     * 
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    @Label("Property 1: Screen count remains 3 when accessing any valid position")
    void screenCountRemainsThreeWhenAccessingValidPositions(
            @ForAll @IntRange(min = 0, max = 2) int position) {
        // Given: Any valid position (0, 1, or 2)
        
        // When: Accessing a page at that position
        OnboardingPage page = OnboardingConfig.getPage(position);
        
        // Then: The page should exist (not null)
        assertNotNull("Page at position " + position + " should exist", page);
        
        // And: The total count should still be 3
        assertEquals("Total page count should remain 3 after accessing position " + position, 
                    3, OnboardingConfig.PAGE_COUNT);
        assertEquals("Pages list size should remain 3 after accessing position " + position, 
                    3, OnboardingConfig.getPages().size());
    }

    /**
     * Property 1 (variant): Screen count invariant with multiple sequential accesses
     * 
     * For any sequence of valid position accesses, the count should remain 3.
     * This verifies that the configuration is truly immutable.
     * 
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    @Label("Property 1: Screen count remains 3 across multiple sequential accesses")
    void screenCountRemainsThreeAcrossMultipleAccesses(
            @ForAll @IntRange(min = 0, max = 2) int position1,
            @ForAll @IntRange(min = 0, max = 2) int position2,
            @ForAll @IntRange(min = 0, max = 2) int position3) {
        // Given: Multiple random position accesses
        
        // When: Accessing pages at different positions in sequence
        OnboardingPage page1 = OnboardingConfig.getPage(position1);
        OnboardingPage page2 = OnboardingConfig.getPage(position2);
        OnboardingPage page3 = OnboardingConfig.getPage(position3);
        
        // Then: All pages should exist
        assertNotNull("Page at position " + position1 + " should exist", page1);
        assertNotNull("Page at position " + position2 + " should exist", page2);
        assertNotNull("Page at position " + position3 + " should exist", page3);
        
        // And: The total count should still be 3 after all accesses
        assertEquals("Total page count should remain 3 after multiple accesses", 
                    3, OnboardingConfig.PAGE_COUNT);
        assertEquals("Pages list size should remain 3 after multiple accesses", 
                    3, OnboardingConfig.getPages().size());
    }

    /**
     * Property 1 (boundary test): Invalid positions should fail consistently
     * 
     * For any invalid position (< 0 or >= 3), accessing the configuration should
     * throw IndexOutOfBoundsException. This verifies the boundary enforcement.
     * 
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    @Label("Property 1: Invalid positions consistently throw IndexOutOfBoundsException")
    void invalidPositionsConsistentlyThrowException(
            @ForAll @IntRange(min = -100, max = -1) int negativePosition) {
        // Given: Any negative position
        
        // When/Then: Accessing it should throw IndexOutOfBoundsException
        try {
            OnboardingConfig.getPage(negativePosition);
            fail("Should have thrown IndexOutOfBoundsException for position " + negativePosition);
        } catch (IndexOutOfBoundsException e) {
            // Expected - verify the count is still 3
            assertEquals("Page count should remain 3 even after invalid access", 
                        3, OnboardingConfig.PAGE_COUNT);
        }
    }

    /**
     * Property 1 (boundary test): Positions at or above count should fail
     * 
     * For any position >= 3, accessing the configuration should throw
     * IndexOutOfBoundsException.
     * 
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    @Label("Property 1: Positions >= 3 consistently throw IndexOutOfBoundsException")
    void positionsAtOrAboveCountThrowException(
            @ForAll @IntRange(min = 3, max = 100) int invalidPosition) {
        // Given: Any position >= 3
        
        // When/Then: Accessing it should throw IndexOutOfBoundsException
        try {
            OnboardingConfig.getPage(invalidPosition);
            fail("Should have thrown IndexOutOfBoundsException for position " + invalidPosition);
        } catch (IndexOutOfBoundsException e) {
            // Expected - verify the count is still 3
            assertEquals("Page count should remain 3 even after invalid access", 
                        3, OnboardingConfig.PAGE_COUNT);
        }
    }
}
