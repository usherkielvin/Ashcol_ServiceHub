package app.hub.onboarding;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.hub.R;

/**
 * A reusable fragment representing a single onboarding screen.
 * 
 * This fragment displays a background image, the Ashcol logo at the top,
 * and title/subtitle text overlaid on the background. The content is
 * configured through arguments passed via the newInstance() factory method.
 * 
 * Requirements: 1.3, 1.4, 1.5, 1.6, 2.1, 2.2, 2.3, 2.4, 7.1, 7.2, 7.3
 */
public class OnboardingFragment extends Fragment {
    private static final String TAG = "OnboardingFragment";
    
    // Argument keys
    private static final String ARG_BACKGROUND_IMAGE_RES = "background_image_res";
    private static final String ARG_TITLE_TEXT = "title_text";
    private static final String ARG_SUBTITLE_TEXT = "subtitle_text";
    
    // UI components
    private ImageView ivBackground;
    private TextView tvTitle;
    private TextView tvSubtitle;
    
    // Configuration data
    private int backgroundImageRes;
    private String titleText;
    private String subtitleText;

    /**
     * Factory method to create a new instance of OnboardingFragment with the specified configuration.
     * 
     * @param backgroundImageRes Drawable resource ID for the background image
     * @param titleText Main title text to display
     * @param subtitleText Subtitle text to display
     * @return A new instance of OnboardingFragment configured with the provided arguments
     */
    public static OnboardingFragment newInstance(@DrawableRes int backgroundImageRes, 
                                                  String titleText, 
                                                  String subtitleText) {
        OnboardingFragment fragment = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BACKGROUND_IMAGE_RES, backgroundImageRes);
        args.putString(ARG_TITLE_TEXT, titleText);
        args.putString(ARG_SUBTITLE_TEXT, subtitleText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Extract arguments
        if (getArguments() != null) {
            backgroundImageRes = getArguments().getInt(ARG_BACKGROUND_IMAGE_RES, 0);
            titleText = getArguments().getString(ARG_TITLE_TEXT, "");
            subtitleText = getArguments().getString(ARG_SUBTITLE_TEXT, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, 
                            @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_s2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        ivBackground = view.findViewById(R.id.ivBackground);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        
        // Setup views with configuration data
        setupViews();
    }

    /**
     * Populates the UI components with data from the fragment arguments.
     * Handles missing drawable resources gracefully by displaying a fallback color.
     * 
     * Requirements: 7.3 - Error handling for missing drawable resources
     */
    private void setupViews() {
        // Load background image with error handling
        loadBackgroundImage();
        
        // Set text content
        tvTitle.setText(titleText);
        tvSubtitle.setText(subtitleText);
    }

    /**
     * Loads the background image from the drawable resource.
     * If the resource is missing, displays a light green fallback color.
     * 
     * Requirements: 7.3 - Graceful error handling for missing resources
     */
    private void loadBackgroundImage() {
        try {
            if (backgroundImageRes != 0) {
                ivBackground.setImageResource(backgroundImageRes);
            } else {
                Log.w(TAG, "Background image resource ID is 0, using fallback color");
                ivBackground.setBackgroundColor(Color.parseColor("#E8F5E9")); // Light green fallback
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Background image not found: " + backgroundImageRes, e);
            // Display a light green fallback color
            ivBackground.setBackgroundColor(Color.parseColor("#E8F5E9"));
        }
    }

    /**
     * Gets the background image resource ID for this fragment.
     * Used for testing purposes.
     * 
     * @return The drawable resource ID for the background image
     */
    public int getBackgroundImageRes() {
        return backgroundImageRes;
    }

    /**
     * Gets the title text for this fragment.
     * Used for testing purposes.
     * 
     * @return The title text
     */
    public String getTitleText() {
        return titleText;
    }

    /**
     * Gets the subtitle text for this fragment.
     * Used for testing purposes.
     * 
     * @return The subtitle text
     */
    public String getSubtitleText() {
        return subtitleText;
    }
}
