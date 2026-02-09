package app.hub;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import app.hub.common.MainActivity;
import app.hub.common.RegisterActivity;
import app.hub.onboarding.OnboardingPagerAdapter;
import app.hub.onboarding.OnboardingPreferences;

/**
 * IntroActivity hosts the onboarding experience for first-time users.
 * 
 * This activity displays three swipeable onboarding screens (S2, S3, S4) that introduce
 * the app's key value propositions. Users can swipe between screens or navigate directly
 * to registration or login via static buttons at the bottom.
 * 
 * The activity integrates with OnboardingPreferences to track whether a user has completed
 * onboarding, ensuring returning users skip directly to authentication.
 * 
 * Requirements: 1.1, 3.1, 4.5, 5.3, 5.4, 6.1, 6.4
 */
public class IntroActivity extends AppCompatActivity {
    private static final String TAG = "IntroActivity";
    
    private ViewPager2 viewPager;
    private View dot1, dot2, dot3;
    private MaterialButton btnGetStarted;
    private MaterialButton btnAlreadyMember;
    private OnboardingPreferences onboardingPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Initialize preferences helper
        onboardingPreferences = new OnboardingPreferences(this);

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        btnGetStarted = findViewById(R.id.btnGetStarted);
        btnAlreadyMember = findViewById(R.id.btnAlreadyMember);

        // Setup components
        setupViewPager();
        setupPageIndicator();
        setupButtons();
    }

    /**
     * Initializes ViewPager2 with OnboardingPagerAdapter.
     * 
     * This method creates and configures the ViewPager2 that enables horizontal swiping
     * between the three onboarding screens. The adapter provides OnboardingFragment instances
     * configured with the appropriate background images, titles, and subtitles.
     * 
     * Requirements: 1.1, 3.1
     */
    private void setupViewPager() {
        try {
            OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(this);
            viewPager.setAdapter(adapter);
            
            Log.d(TAG, "ViewPager initialized with " + adapter.getItemCount() + " screens");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up ViewPager", e);
            // Show error to user and provide fallback navigation
            Toast.makeText(this, "Error loading onboarding screens", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets up page indicator synchronization with ViewPager position.
     * 
     * This method registers a callback that updates the page indicator dots whenever
     * the ViewPager position changes. The active dot is highlighted while inactive dots
     * are dimmed, providing visual feedback about the current screen position.
     * 
     * Position mapping:
     * - Position 0 (S2): dot1 highlighted
     * - Position 1 (S3): dot2 highlighted
     * - Position 2 (S4): dot3 highlighted
     * 
     * Requirements: 4.5
     * Property 5: Page indicator synchronization
     */
    private void setupPageIndicator() {
        try {
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updatePageIndicator(position);
                }
            });
            
            // Set initial indicator state (position 0)
            updatePageIndicator(0);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up page indicator", e);
        }
    }

    /**
     * Updates the page indicator to highlight the current position.
     * 
     * @param position The current ViewPager position (0-2)
     */
    private void updatePageIndicator(int position) {
        try {
            // Reset all dots to unselected color
            if (dot1 != null) {
                dot1.setBackgroundTintList(getColorStateList(R.color.dot_unselected_color));
            }
            if (dot2 != null) {
                dot2.setBackgroundTintList(getColorStateList(R.color.dot_unselected_color));
            }
            if (dot3 != null) {
                dot3.setBackgroundTintList(getColorStateList(R.color.dot_unselected_color));
            }

            // Highlight the current dot
            switch (position) {
                case 0:
                    if (dot1 != null) {
                        dot1.setBackgroundTintList(getColorStateList(R.color.dot_selected_color));
                    }
                    break;
                case 1:
                    if (dot2 != null) {
                        dot2.setBackgroundTintList(getColorStateList(R.color.dot_selected_color));
                    }
                    break;
                case 2:
                    if (dot3 != null) {
                        dot3.setBackgroundTintList(getColorStateList(R.color.dot_selected_color));
                    }
                    break;
                default:
                    Log.w(TAG, "Invalid page position: " + position);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating page indicator for position " + position, e);
        }
    }

    /**
     * Sets up button click handlers for navigation to authentication screens.
     * 
     * This method configures the two static buttons that remain visible across all
     * onboarding screens:
     * - "Get Started" button: navigates to RegistrationActivity
     * - "I'm already a member" button: navigates to LoginActivity (MainActivity)
     * 
     * Both buttons trigger markOnboardingComplete() before navigation to ensure
     * the user doesn't see onboarding screens again.
     * 
     * Requirements: 5.3, 5.4, 6.1, 6.4
     */
    private void setupButtons() {
        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> navigateToRegistration());
        }

        if (btnAlreadyMember != null) {
            btnAlreadyMember.setOnClickListener(v -> navigateToLogin());
        }
    }

    /**
     * Navigates to the RegistrationActivity.
     * 
     * This method is called when the user taps the "Get Started" button. It marks
     * onboarding as complete before launching the registration flow, ensuring the
     * user won't see the intro screens again even if they don't complete registration.
     * 
     * Error handling is implemented to gracefully handle cases where the activity
     * cannot be launched (e.g., activity not found in manifest).
     * 
     * Requirements: 5.3, 6.1, 6.4
     * Property 8: Get Started navigation
     * Property 10: Onboarding completion persistence
     */
    private void navigateToRegistration() {
        try {
            // Mark onboarding as complete before navigation
            markOnboardingComplete();
            
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
            
            Log.d(TAG, "Navigated to RegistrationActivity");
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "RegistrationActivity not found", e);
            Toast.makeText(this, "Unable to open registration. Please try again.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to registration", e);
            Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigates to the LoginActivity (MainActivity).
     * 
     * This method is called when the user taps the "I'm already a member" button.
     * It marks onboarding as complete before launching the login screen, ensuring
     * the user won't see the intro screens again.
     * 
     * Error handling is implemented to gracefully handle cases where the activity
     * cannot be launched.
     * 
     * Requirements: 5.4, 6.1, 6.4
     * Property 9: Already member navigation
     * Property 10: Onboarding completion persistence
     */
    private void navigateToLogin() {
        try {
            // Mark onboarding as complete before navigation
            markOnboardingComplete();
            
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            
            Log.d(TAG, "Navigated to MainActivity (Login)");
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "MainActivity not found", e);
            Toast.makeText(this, "Unable to open login. Please try again.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to login", e);
            Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Marks onboarding as complete in SharedPreferences.
     * 
     * This method sets the onboarding completion flag to true, which is checked by
     * SplashActivity to determine whether to show the intro screens or skip directly
     * to login on subsequent app launches.
     * 
     * This method is called before navigating to either registration or login to ensure
     * the preference is persisted before the activity finishes.
     * 
     * Requirements: 6.1, 6.4
     * Property 10: Onboarding completion persistence
     */
    private void markOnboardingComplete() {
        try {
            boolean success = onboardingPreferences.setOnboardingComplete(true);
            
            if (success) {
                Log.d(TAG, "Onboarding marked as complete");
            } else {
                Log.w(TAG, "Failed to mark onboarding as complete");
                // Continue with navigation even if preference save fails
                // User may see onboarding again, but app remains functional
            }
        } catch (Exception e) {
            Log.e(TAG, "Error marking onboarding complete", e);
            // Continue with navigation even if an exception occurs
        }
    }
}
