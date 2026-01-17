package app.hub.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.hub.R;

public class CreateNewAccountFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_new_acc, container, false);
        
        setupButtons(view);
        
        return view;
    }

    private void setupButtons(View view) {
        // Back button
        ImageButton backButton = view.findViewById(R.id.closeButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
        }

        // Continue with Email button
        Button continueWithEmailButton = view.findViewById(R.id.OpenOTP);
        if (continueWithEmailButton != null) {
            continueWithEmailButton.setOnClickListener(v -> {
                RegisterActivity activity = (RegisterActivity) getActivity();
                if (activity != null) {
                    activity.showEmailFragment();
                }
            });
        }

        // Facebook button (optional - implement later)
        Button facebookButton = view.findViewById(R.id.btnFacebook);
        if (facebookButton != null) {
            facebookButton.setOnClickListener(v -> {
                showToast("Facebook login coming soon");
            });
        }

        // Google button (optional - implement later)
        Button googleButton = view.findViewById(R.id.btnGoogle);
        if (googleButton != null) {
            googleButton.setOnClickListener(v -> {
                showToast("Google login coming soon");
            });
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
