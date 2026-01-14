package app.hub.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.LogoutResponse;
import app.hub.api.UserResponse;
import app.hub.common.MainActivity;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileFragment extends Fragment {

    private TokenManager tokenManager;
    private String currentName;
    private String currentUsername;
    private TextView tvName, tvUsername;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user__profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());

        // Initialize UI components
        tvName = view.findViewById(R.id.tv_name);
        tvUsername = view.findViewById(R.id.tv_username);

        // Pre-fill with cached data
        String cachedName = tokenManager.getName();
        if (cachedName != null && tvName != null) {
            tvName.setText(cachedName);
        }
        
        String cachedEmail = tokenManager.getEmail();
        if (cachedEmail != null && tvUsername != null) {
            tvUsername.setText(cachedEmail);
        }

        // Fetch user data from API
        fetchUserData();

        // Setup click listeners for menu items
        setupClickListeners(view);
    }

    private void setupClickListeners(View view) {
        // Sign Out
        View btnSignOut = view.findViewById(R.id.btn_sign_out);
        if (btnSignOut != null) {
            btnSignOut.setOnClickListener(v -> logout());
        }

        // Personal Information
        View btnPersonalInfo = view.findViewById(R.id.btn_personal_info);
        if (btnPersonalInfo != null) {
            btnPersonalInfo.setOnClickListener(v -> {
                // TODO: Navigate to Personal Info screen
                Toast.makeText(getContext(), "Personal Information clicked", Toast.LENGTH_SHORT).show();
            });
        }

        // Password & Privacy
        View btnPasswordPrivacy = view.findViewById(R.id.btn_password_privacy);
        if (btnPasswordPrivacy != null) {
            btnPasswordPrivacy.setOnClickListener(v -> {
                // TODO: Navigate to Password & Privacy screen
                Toast.makeText(getContext(), "Password & Privacy clicked", Toast.LENGTH_SHORT).show();
            });
        }

        // Help and Feedback
        View btnHelp = view.findViewById(R.id.btn_help);
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> {
                // TODO: Navigate to Help screen
                Toast.makeText(getContext(), "Help & Feedback clicked", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Edit Photo
        View btnEditPhoto = view.findViewById(R.id.btn_edit_photo);
        if (btnEditPhoto != null) {
            btnEditPhoto.setOnClickListener(v -> {
                // TODO: Implement photo editing
                Toast.makeText(getContext(), "Edit photo clicked", Toast.LENGTH_SHORT).show();
            });
        }

        // Appearance
        View btnAppearance = view.findViewById(R.id.btn_appearance);
        if (btnAppearance != null) {
            btnAppearance.setOnClickListener(v -> Toast.makeText(getContext(), "Appearance clicked", Toast.LENGTH_SHORT).show());
        }

        // Notifications
        View btnNotifications = view.findViewById(R.id.btn_notifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> Toast.makeText(getContext(), "Notifications clicked", Toast.LENGTH_SHORT).show());
        }

        // Language
        View btnLanguage = view.findViewById(R.id.btn_language);
        if (btnLanguage != null) {
            btnLanguage.setOnClickListener(v -> Toast.makeText(getContext(), "Language clicked", Toast.LENGTH_SHORT).show());
        }

        // Payroll
        View btnPayroll = view.findViewById(R.id.btn_payroll);
        if (btnPayroll != null) {
            btnPayroll.setOnClickListener(v -> Toast.makeText(getContext(), "Payroll clicked", Toast.LENGTH_SHORT).show());
        }
    }

    private void fetchUserData() {
        String token = tokenManager.getToken();
        if (token == null) {
            fallbackToCachedData();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> call = apiService.getUser(token);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    if (userResponse.isSuccess() && userResponse.getData() != null) {
                        UserResponse.Data userData = userResponse.getData();
                        
                        currentName = userData.getName();
                        if (currentName == null || currentName.trim().isEmpty()) {
                            currentName = userData.getFirstName() + " " + userData.getLastName();
                        }
                        
                        currentUsername = userData.getUsername();
                        if (currentUsername == null || currentUsername.isEmpty()) {
                            currentUsername = userData.getEmail();
                        }
                        
                        updateUI();
                        
                        // Update cache
                        tokenManager.saveName(currentName);
                        if (userData.getEmail() != null) {
                            tokenManager.saveEmail(userData.getEmail());
                        }
                    } else {
                        fallbackToCachedData();
                    }
                } else {
                    fallbackToCachedData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                fallbackToCachedData();
            }
        });
    }

    private void updateUI() {
        if (getActivity() == null || getView() == null) return;
        
        getActivity().runOnUiThread(() -> {
            if (currentName != null && tvName != null) tvName.setText(currentName);
            if (currentUsername != null && tvUsername != null) tvUsername.setText(currentUsername);
        });
    }

    private void logout() {
        String token = tokenManager.getToken();
        if (token != null) {
            ApiService apiService = ApiClient.getApiService();
            Call<LogoutResponse> call = apiService.logout(token);
            call.enqueue(new Callback<LogoutResponse>() {
                @Override
                public void onResponse(@NonNull Call<LogoutResponse> call, @NonNull Response<LogoutResponse> response) {
                    tokenManager.clear();
                    navigateToLogin();
                }

                @Override
                public void onFailure(@NonNull Call<LogoutResponse> call, @NonNull Throwable t) {
                    tokenManager.clear();
                    navigateToLogin();
                }
            });
        } else {
            tokenManager.clear();
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void fallbackToCachedData() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            String cachedName = tokenManager.getName();
            if (cachedName != null && tvName != null) tvName.setText(cachedName);
            
            String cachedEmail = tokenManager.getEmail();
            if (cachedEmail != null && tvUsername != null) tvUsername.setText(cachedEmail);
        });
    }
}
