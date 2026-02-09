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
 * Fragment for onboarding screen S3 (second screen).
 * Uses fragment_onboarding_s3.xml layout with pic2 background.
 */
public class OnboardingFragmentS3 extends Fragment {
    private static final String TAG = "OnboardingFragmentS3";
    
    private static final String ARG_BACKGROUND_IMAGE_RES = "background_image_res";
    private static final String ARG_TITLE_TEXT = "title_text";
    private static final String ARG_SUBTITLE_TEXT = "subtitle_text";
    
    private ImageView ivBackground;
    private TextView tvTitle;
    private TextView tvSubtitle;
    
    private int backgroundImageRes;
    private String titleText;
    private String subtitleText;

    public static OnboardingFragmentS3 newInstance(@DrawableRes int backgroundImageRes, 
                                                    String titleText, 
                                                    String subtitleText) {
        OnboardingFragmentS3 fragment = new OnboardingFragmentS3();
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
        return inflater.inflate(R.layout.fragment_onboarding_s3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        ivBackground = view.findViewById(R.id.ivBackground);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        
        setupViews();
    }

    private void setupViews() {
        loadBackgroundImage();
        tvTitle.setText(titleText);
        tvSubtitle.setText(subtitleText);
    }

    private void loadBackgroundImage() {
        try {
            if (backgroundImageRes != 0) {
                ivBackground.setImageResource(backgroundImageRes);
            } else {
                Log.w(TAG, "Background image resource ID is 0, using fallback color");
                ivBackground.setBackgroundColor(Color.parseColor("#E8F5E9"));
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Background image not found: " + backgroundImageRes, e);
            ivBackground.setBackgroundColor(Color.parseColor("#E8F5E9"));
        }
    }

    public int getBackgroundImageRes() {
        return backgroundImageRes;
    }

    public String getTitleText() {
        return titleText;
    }

    public String getSubtitleText() {
        return subtitleText;
    }
}
