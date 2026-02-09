package app.hub.onboarding;

import app.hub.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration class containing the three onboarding screen definitions.
 * 
 * This class provides a centralized, immutable configuration for all onboarding screens
 * displayed in the IntroActivity. Each screen includes a background image, title, and subtitle
 * that communicate the app's key value propositions to new users.
 * 
 * The screens are:
 * - S2 (Screen 2): Introduces the core service offering (aircon service)
 * - S3 (Screen 3): Highlights the comprehensive service range
 * - S4 (Screen 4): Encourages user action (getting started)
 */
public class OnboardingConfig {
    /**
     * The number of onboarding screens.
     * This value is always 3 as per the requirements.
     */
    public static final int PAGE_COUNT = 3;

    private static final List<OnboardingPage> PAGES;

    static {
        List<OnboardingPage> pages = new ArrayList<>();
        
        // S2: First onboarding screen
        pages.add(new OnboardingPage(
            R.drawable.pic1,
            "Stay Cool," + "\nStay Comfortable",
            "Reliable aircon service for home and office"
        ));
        
        // S3: Second onboarding screen
        pages.add(new OnboardingPage(
            R.drawable.pic2,
            "We've Got You Covered!",
            "From cleaning, repairs, to installations, our team does it all"
        ));
        
        // S4: Third onboarding screen
        pages.add(new OnboardingPage(
            R.drawable.pic3,
            "Let's Get Started",
            "Schedule your appointment in just a few taps"
        ));
        
        PAGES = Collections.unmodifiableList(pages);
    }

    /**
     * Returns the list of onboarding pages in display order.
     * This list is immutable and always contains exactly 3 pages.
     *
     * @return An unmodifiable list of OnboardingPage objects
     */
    public static List<OnboardingPage> getPages() {
        return PAGES;
    }

    /**
     * Returns the onboarding page at the specified position.
     *
     * @param position The position (0-2) of the page to retrieve
     * @return The OnboardingPage at the specified position
     * @throws IndexOutOfBoundsException if position is out of range
     */
    public static OnboardingPage getPage(int position) {
        return PAGES.get(position);
    }

    // Private constructor to prevent instantiation
    private OnboardingConfig() {
        throw new AssertionError("OnboardingConfig should not be instantiated");
    }
}
