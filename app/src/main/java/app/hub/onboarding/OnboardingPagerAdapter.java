package app.hub.onboarding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * FragmentStateAdapter that provides OnboardingFragment instances to ViewPager2.
 * 
 * This adapter creates three onboarding screens (S2, S3, S4) using configuration
 * data from OnboardingConfig. Each screen is represented by an OnboardingFragment
 * with specific background images, titles, and subtitles.
 * 
 * The adapter manages the fragment lifecycle and ensures exactly 3 screens are
 * always available, satisfying the onboarding screen count invariant (Property 1).
 * 
 * Requirements: 1.2, 1.4, 1.5, 1.6, 2.1, 2.2, 2.3
 */
public class OnboardingPagerAdapter extends FragmentStateAdapter {
    
    /**
     * Constructs a new OnboardingPagerAdapter.
     * 
     * @param activity The FragmentActivity that hosts the ViewPager2
     */
    public OnboardingPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    /**
     * Returns the number of onboarding screens.
     * This method always returns 3 to satisfy Requirement 1.2 and Property 1.
     * 
     * @return The number of items (always 3)
     */
    @Override
    public int getItemCount() {
        return OnboardingConfig.PAGE_COUNT;
    }

    /**
     * Creates an OnboardingFragment for the specified position.
     * 
     * This method retrieves the configuration for the requested position from
     * OnboardingConfig and creates the appropriate fragment instance with its
     * specific layout file.
     * 
     * Position mapping:
     * - Position 0: S2 (OnboardingFragmentS2 with fragment_onboarding_s2.xml)
     * - Position 1: S3 (OnboardingFragmentS3 with fragment_onboarding_s3.xml)
     * - Position 2: S4 (OnboardingFragmentS4 with fragment_onboarding_s4.xml)
     * 
     * Requirements: 1.4, 1.5, 1.6, 2.1, 2.2, 2.3
     * Property 3: Screen configuration correctness
     * 
     * @param position The position of the screen to create (0-2)
     * @return A new OnboardingFragment configured for the specified position
     * @throws IndexOutOfBoundsException if position is out of range (0-2)
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Get the page configuration for this position
        OnboardingPage page = OnboardingConfig.getPage(position);
        
        // Create and return the appropriate fragment based on position
        switch (position) {
            case 0:
                return OnboardingFragmentS2.newInstance(
                    page.getBackgroundImageRes(),
                    page.getTitleText(),
                    page.getSubtitleText()
                );
            case 1:
                return OnboardingFragmentS3.newInstance(
                    page.getBackgroundImageRes(),
                    page.getTitleText(),
                    page.getSubtitleText()
                );
            case 2:
                return OnboardingFragmentS4.newInstance(
                    page.getBackgroundImageRes(),
                    page.getTitleText(),
                    page.getSubtitleText()
                );
            default:
                // Fallback to S2 fragment if position is invalid
                return OnboardingFragmentS2.newInstance(
                    page.getBackgroundImageRes(),
                    page.getTitleText(),
                    page.getSubtitleText()
                );
        }
    }
}
