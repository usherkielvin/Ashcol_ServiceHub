package app.hub.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.imageview.ShapeableImageView;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.LogoutResponse;
import app.hub.api.UserResponse;
import app.hub.common.MainActivity;
import app.hub.util.TokenManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileFragment extends Fragment {

    private TokenManager tokenManager;
    private String currentName;
    private String currentEmail;
    private TextView tvName, tvUsername;
    private ShapeableImageView imgProfile;
    private Uri cameraImageUri;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user__profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());
        initializeViews(view);
        loadCachedData();
        fetchUserData();
        setupClickListeners(view);
    }

    private void initializeViews(View view) {
        tvName = view.findViewById(R.id.tv_name);
        tvUsername = view.findViewById(R.id.tv_username);
    }

    private void loadCachedData() {
        String cachedName = getCachedName();
        if (isValidName(cachedName) && tvName != null) {
            tvName.setText(cachedName);
            currentName = cachedName;
        }
        
        String cachedEmail = getCachedEmail();
        if (isValidEmail(cachedEmail) && tvUsername != null) {
            tvUsername.setText(cachedEmail);
            currentEmail = cachedEmail;
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
                        processUserData(userData);
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

    private void processUserData(UserResponse.Data userData) {
        currentName = buildNameFromApi(userData);
        currentEmail = getEmailToDisplay(userData);
        
        updateUI();
        updateCache();
    }

    private String buildNameFromApi(UserResponse.Data userData) {
        String apiName = userData.getName();
        if (isValidName(apiName)) {
            return apiName.trim();
        }
        
        String firstName = userData.getFirstName();
        String lastName = userData.getLastName();
        
        if (isValidName(firstName) || isValidName(lastName)) {
            StringBuilder builder = new StringBuilder();
            if (isValidName(firstName)) {
                builder.append(firstName.trim());
            }
            if (isValidName(lastName)) {
                if (builder.length() > 0) {
                    builder.append(" ");
                }
                builder.append(lastName.trim());
            }
            if (builder.length() > 0) {
                return builder.toString();
            }
        }
        
        return null;
    }

    private String getEmailToDisplay(UserResponse.Data userData) {
        String cachedEmail = getCachedEmail();
        if (isValidEmail(cachedEmail)) {
            return cachedEmail;
        }

        String apiEmail = userData.getEmail();
        String apiUsername = userData.getUsername();
        
        if (isValidApiEmail(apiEmail, apiUsername)) {
            return apiEmail.trim();
        }
        
        return cachedEmail;
    }

    private boolean isValidApiEmail(String email, String username) {
        return email != null 
            && !email.trim().isEmpty() 
            && email.contains("@") 
            && !email.equals(username);
    }

    private void updateCache() {
        if (isValidName(currentName)) {
            tokenManager.saveName(currentName);
        }
        if (isValidEmail(currentEmail) && !currentEmail.equals(getCachedEmail())) {
            tokenManager.saveEmail(currentEmail);
        }
    }

    private void updateUI() {
        if (getActivity() == null || getView() == null) return;
        
        getActivity().runOnUiThread(() -> {
            updateNameDisplay();
            updateEmailDisplay();
        });
    }

    private void updateNameDisplay() {
        String displayName = isValidName(currentName) ? currentName : getCachedName();
        if (isValidName(displayName) && tvName != null) {
            tvName.setText(displayName.trim());
        }
    }

    private void updateEmailDisplay() {
        String displayEmail = isValidEmail(currentEmail) ? currentEmail : getCachedEmail();
        if (isValidEmail(displayEmail) && tvUsername != null) {
            tvUsername.setText(displayEmail);
        }
    }

    private void fallbackToCachedData() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            String cachedName = getCachedName();
            if (isValidName(cachedName) && tvName != null) {
                tvName.setText(cachedName);
                currentName = cachedName;
            }
            
            String cachedEmail = getCachedEmail();
            if (isValidEmail(cachedEmail) && tvUsername != null) {
                tvUsername.setText(cachedEmail);
                currentEmail = cachedEmail;
            }
        });
    }

    private boolean isValidName(String name) {
        return name != null 
            && !name.trim().isEmpty() 
            && !name.trim().equals("null") 
            && !name.trim().contains("null");
    }

    private boolean isValidEmail(String email) {
        return email != null 
            && email.contains("@") 
            && email.trim().length() > 3;
    }

    private String getCachedName() {
        try {
            String name = tokenManager.getName();
            return isValidName(name) ? name.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getCachedEmail() {
        try {
            String email = tokenManager.getEmail();
            return isValidEmail(email) ? email.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void setupClickListeners(View view) {
        setClickListener(view, R.id.btn_sign_out, () -> logout());
        setClickListener(view, R.id.btn_personal_info, () -> 
            showToast("Personal Information clicked"));
        setClickListener(view, R.id.btn_password_privacy, () -> 
            showToast("Password & Privacy clicked"));
        setClickListener(view, R.id.btn_help, () -> 
            showToast("Help & Feedback clicked"));
        setClickListener(view, R.id.btn_edit_photo, () -> 
            showToast("Edit photo clicked"));
        setClickListener(view, R.id.btn_appearance, () -> 
            showToast("Appearance clicked"));
        setClickListener(view, R.id.btn_notifications, () -> 
            showToast("Notifications clicked"));
        setClickListener(view, R.id.btn_language, () -> 
            showToast("Language clicked"));
        setClickListener(view, R.id.btn_payroll, () -> 
            showToast("Payroll clicked"));
    }

    private void setClickListener(View view, int id, Runnable action) {
        View button = view.findViewById(id);
        if (button != null) {
            button.setOnClickListener(v -> action.run());
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
}
