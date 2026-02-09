package app.hub.onboarding;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import app.hub.R;

import static org.junit.Assert.*;

/**
 * Instrumented tests for OnboardingPagerAdapter.
 * These tests run on an Android device or emulator with real Android framework components.
 * 
 * Tests cover:
 * - Adapter item count (Requirement 1.2, Property 1)
 * - Fragment creation for all positions
 * - Fragment configuration correctness (Requirements 1.4, 1.5, 1.6, 2.1, 2.2, 2.3, Property 3)
 * - Error handling for invalid positions
 * 
 * Requirements: 1.2, 1.4, 1.5, 1.6, 2.1, 2.2, 2.3
 */
@RunWith(AndroidJUnit4.class)
public class OnboardingPagerAdapterInstrumentedTest {

    @Test
    public void adapterReturnsExactlyThreeItems() {
        // Given an activity and adapter
        // Requirement 1.2, Property 1: Adapter should always contain exactly 3 items
        ActivityScenario<FragmentActivity> scenario = ActivityScenario.launch(FragmentActivity.class);
        
        scenario.onActivity(activity -> {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(activity);
            
            // When getting the item count
            int itemCount = adapter.getItemCount();
            
            // Then it should return exactly 3
            assertEquals("Adapter should return exactly 3 items", 3, itemCount);
        });
    }

    @Test
    public void adapterCreatesFragmentForFirstPosition() {
        // Given an activity and adapter
        ActivityScenario<FragmentActivity> scenario = ActivityScenario.launch(FragmentActivity.class);
        
        scenario.onActivity(activity -> {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(activity);
            
            // When creating a fragment for position 0 (S2)
            Fragment fragment = adapter.createFragment(0);
            
            // Then it should return an OnboardingFragment
            assertNotNull("Fragment should not be null", fragment);
            assertTrue("Fragment should be OnboardingFragment", fragment instanceof OnboardingFragment);
        });
    }

    @Test
    public void adapterCreatesFragmentForSecondPosition() {
        // Given an activity and adapter
        ActivityScenario<FragmentActivity> scenario = ActivityScenario.launch(FragmentActivity.class);
        
        scenario.onActivity(activity -> {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(activity);
            
            // When creating a fragment for position 1 (S3)
            Fragment fragment = adapter.createFragment(1);
            
            // Then it should return an OnboardingFragment
            assertNotNull("Fragment should not be null", fragment);
            assertTrue("Fragment should be OnboardingFragment", fragment instanceof OnboardingFragment);
        });
    }

    @Test
    public void adapterCreatesFragmentForThirdPosition() {
        // Given an activity and adapter
        ActivityScenario<FragmentActivity> scenario = ActivityScenario.launch(FragmentActivity.class);
        
        scenario.onActivity(activity -> {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(activity);
            
            // When creating a fragment for position 2 (S4)
            Fragment fragment = adapter.createFragment(2);
            
            // Then it should return an OnboardingFragment
            assertNotNull("Fragment should not be null", fragment);
            assertTrue("Fragment should be OnboardingFragment", fragment instanceof OnboardingFragment);
        });
    }

    @Test
    public void adapterCreatesFragmentWithCorrectConfigurationForFirstPosition() {
        // Given an activity and adapter
        // Requirements 1.4, 2.1, Property 3: Position 0 should have correct configuration
        ActivityScenario<FragmentActivity> scenario = ActivityScenario.launch(FragmentActivity.class);
        
        scenario.onActivity(activity -> {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(activity);
            
            // When creating a fragment for position 0 (S2)
            OnboardingFragment fragment = (OnboardingFragment) adapter.createFragment(0);
            
            // Then it should have the correct configuration
            assertEquals("Position 0 should use pic1 background", 
                        R.drawable.pic1, fragment.getBackgroundImageRes());
            assertEquals("Position 0 should have correct title", 
                        "Stay Cool, Stay Comfortable", fragment.getTitleText());
            assertEquals("Position 0 should have correct subtitle", 
                        "Reliable aircon service for home and office", fragment.getSubtitleText());
        });
    }

    @Test
    public void adapterCreatesFragmentWithCorrectConfigurationForSecondPosition() {
        // Given an activity and adapter
        // Requirements 1.5, 2.2, Property 3: Position 1 should have correct configuration
        ActivityScenario<FragmentActivity> scenario = ActivityScenario.launch(FragmentActivity.class);
        
        scenario.onActivity(activity -> {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(activity);
            
            // When creating a fragment for position 1 (S3)
            OnboardingFragment fragment = (OnboardingFragment) adapter.createFragment(1);
            
            // Then it should have the correct configuration
            assertEquals("Position 1 should use pic2 background", 
                        R.drawable.pic2, fragment.getBackgroundImageRes());
            assertEquals("Position 1 should have correct title", 
                        "We've Got You Covered!", fragment.getTitleText());
            assertEquals("Position 1 should have correct subtitle", 
                        "From cleaning, repairs, to installations, our team does it all", 
                        fragment.getSubtitleText());
        });
    }

    @Test
    public void adapterCreatesFragmentWithCorrectConfigurationForThirdPosition() {
        // Given an activity and adapter
        // Requirements 1.6, 2.3, Property 3: Position 2 should have correct configuration
        ActivityScenario<FragmentActivity> scenario = ActivityScenario.launch(FragmentActivity.class);
        
        scenario.onActivity(activity -> {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(activity);
            
            // When creating a fragment for position 2 (S4)
            OnboardingFragment fragment = (OnboardingFragment) adapter.createFragment(2);
            
            // Then it should have the correct configuration
            assertEquals("Position 2 should use pic3 background", 
                        R.drawable.pic3, fragment.getBackgroundImageRes());
            assertEquals("Position 2 should have correct title", 
                        "Let's Get Started", fragment.getTitleText());
            assertEquals("Position 2 should have correct subtitle", 
                        "Schedule your appointment in just a few taps", fragment.getSubtitleText());
        });
    }

    @Test
    public void adapterCreatesNewFragmentInstancesForEachCall() {
        // Given an activity and adapter
        ActivityScenario<FragmentActivity> scenario = ActivityScenario.launch(FragmentActivity.class);
        
        scenario.onActivity(activity -> {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(activity);
            
            // When creating fragments for the same position multiple times
            Fragment fragment1 = adapter.createFragment(0);
            Fragment fragment2 = adapter.createFragment(0);
            
            // Then each call should return a new instance
            assertNotSame("Each call should return a new fragment instance", fragment1, fragment2);
        });
    }

    @Test
    public void adapterCreatesFragmentsForAllValidPositions() {
        // Given an activity and adapter
        ActivityScenario<FragmentActivity> scenario = ActivityScenario.launch(FragmentActivity.class);
        
        scenario.onActivity(activity -> {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(activity);
            
            // When creating fragments for all valid positions
            for (int position = 0; position < adapter.getItemCount(); position++) {
                Fragment fragment = adapter.createFragment(position);
                
                // Then each fragment should be created successfully
                assertNotNull("Fragment at position " + position + " should not be null", fragment);
                assertTrue("Fragment at position " + position + " should be OnboardingFragment", 
                          fragment instanceof OnboardingFragment);
            }
        });
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void adapterThrowsExceptionForInvalidPosition() {
        // Given an activity and adapter
        ActivityScenario<FragmentActivity> scenario = ActivityScenario.launch(FragmentActivity.class);
        
        scenario.onActivity(activity -> {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(activity);
            
            // When creating a fragment for an invalid position (3)
            adapter.createFragment(3);
            
            // Then it should throw IndexOutOfBoundsException
        });
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void adapterThrowsExceptionForNegativePosition() {
        // Given an activity and adapter
        ActivityScenario<FragmentActivity> scenario = ActivityScenario.launch(FragmentActivity.class);
        
        scenario.onActivity(activity -> {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(activity);
            
            // When creating a fragment for a negative position
            adapter.createFragment(-1);
            
            // Then it should throw IndexOutOfBoundsException
        });
    }

    @Test
    public void adapterItemCountMatchesConfigPageCount() {
        // Given an activity and adapter
        ActivityScenario<FragmentActivity> scenario = ActivityScenario.launch(FragmentActivity.class);
        
        scenario.onActivity(activity -> {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(activity);
            
            // When comparing adapter item count with OnboardingConfig page count
            // Then they should match
            assertEquals("Adapter item count should match OnboardingConfig.PAGE_COUNT", 
                        OnboardingConfig.PAGE_COUNT, adapter.getItemCount());
        });
    }

    @Test
    public void adapterCreatesFragmentsWithNonEmptyText() {
        // Given an activity and adapter
        ActivityScenario<FragmentActivity> scenario = ActivityScenario.launch(FragmentActivity.class);
        
        scenario.onActivity(activity -> {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(activity);
            
            // When creating fragments for all positions
            for (int position = 0; position < adapter.getItemCount(); position++) {
                OnboardingFragment fragment = (OnboardingFragment) adapter.createFragment(position);
                
                // Then each fragment should have non-empty title and subtitle
                assertNotNull("Title at position " + position + " should not be null", 
                             fragment.getTitleText());
                assertFalse("Title at position " + position + " should not be empty", 
                           fragment.getTitleText().trim().isEmpty());
                assertNotNull("Subtitle at position " + position + " should not be null", 
                             fragment.getSubtitleText());
                assertFalse("Subtitle at position " + position + " should not be empty", 
                           fragment.getSubtitleText().trim().isEmpty());
            }
        });
    }

    @Test
    public void adapterCreatesFragmentsWithValidBackgroundImages() {
        // Given an activity and adapter
        ActivityScenario<FragmentActivity> scenario = ActivityScenario.launch(FragmentActivity.class);
        
        scenario.onActivity(activity -> {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(activity);
            
            // When creating fragments for all positions
            for (int position = 0; position < adapter.getItemCount(); position++) {
                OnboardingFragment fragment = (OnboardingFragment) adapter.createFragment(position);
                
                // Then each fragment should have a valid background image resource
                assertNotEquals("Background image at position " + position + " should not be 0", 
                              0, fragment.getBackgroundImageRes());
            }
        });
    }
}
