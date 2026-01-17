package app.hub.manager;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;

import java.io.File;

import app.hub.R;
import app.hub.common.MainActivity;
import app.hub.user.ChangePasswordFragment;
import app.hub.util.TokenManager;

public class ManagerProfileFragment extends Fragment {

    private TokenManager tokenManager;

    public ManagerProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = new TokenManager(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manager_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            clearUserData();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        MaterialButton passwordPrivacyButton = view.findViewById(R.id.btn_password_privacy);
        if (passwordPrivacyButton != null) {
            passwordPrivacyButton.setOnClickListener(v -> navigateToChangePassword());
        }
    }

    private void navigateToChangePassword() {
        if (getActivity() != null) {
            ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, changePasswordFragment)
                .addToBackStack(null)
                .commit();
        }
    }

    private void clearUserData() {
        // Clear token manager data
        tokenManager.clear();
        
        // Delete locally stored profile photo
        try {
            File imageFile = new File(requireContext().getFilesDir(), "profile_image.jpg");
            if (imageFile.exists()) {
                imageFile.delete();
            }
        } catch (Exception e) {
            // Ignore errors when clearing profile photo
        }
    }
}