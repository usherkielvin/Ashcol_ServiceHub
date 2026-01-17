package app.hub.employee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.ChangePasswordRequest;
import app.hub.api.ChangePasswordResponse;
import app.hub.common.MainActivity;
import app.hub.util.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeSettingsFragment extends Fragment {

    private TokenManager tokenManager;

    public EmployeeSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_employee_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(getContext());

        Button changePasswordButton = view.findViewById(R.id.changePasswordButton);
        if (changePasswordButton != null) {
            changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        }

        Button logoutButton = view.findViewById(R.id.logoutButton);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                clearUserData();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }
    }

    private void showChangePasswordDialog() {
        if (getContext() == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        
        TextInputEditText currentPasswordInput = dialogView.findViewById(R.id.currentPasswordInput);
        TextInputEditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);
        TextInputLayout currentPasswordLayout = dialogView.findViewById(R.id.currentPasswordLayout);
        TextInputLayout newPasswordLayout = dialogView.findViewById(R.id.newPasswordLayout);
        TextInputLayout confirmPasswordLayout = dialogView.findViewById(R.id.confirmPasswordLayout);

        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle(getString(R.string.change_password))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create();

        dialog.setOnShowListener(d -> {
            android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String currentPassword = currentPasswordInput != null ? currentPasswordInput.getText().toString() : "";
                String newPassword = newPasswordInput != null ? newPasswordInput.getText().toString() : "";
                String confirmPassword = confirmPasswordInput != null ? confirmPasswordInput.getText().toString() : "";
                
                if (currentPasswordLayout != null) currentPasswordLayout.setError(null);
                if (newPasswordLayout != null) newPasswordLayout.setError(null);
                if (confirmPasswordLayout != null) confirmPasswordLayout.setError(null);
                
                if (validatePasswordInputs(currentPassword, newPassword, confirmPassword, 
                        currentPasswordLayout, newPasswordLayout, confirmPasswordLayout)) {
                    changePassword(currentPassword, newPassword);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private boolean validatePasswordInputs(String currentPassword, String newPassword, String confirmPassword,
            TextInputLayout currentPasswordLayout, TextInputLayout newPasswordLayout, TextInputLayout confirmPasswordLayout) {
        boolean isValid = true;
        
        if (currentPassword.isEmpty()) {
            if (currentPasswordLayout != null) {
                currentPasswordLayout.setError("Current password is required");
            }
            isValid = false;
        }
        
        if (newPassword.isEmpty()) {
            if (newPasswordLayout != null) {
                newPasswordLayout.setError("New password is required");
            }
            isValid = false;
        } else if (newPassword.length() < 8) {
            if (newPasswordLayout != null) {
                newPasswordLayout.setError("Password must be at least 8 characters");
            }
            isValid = false;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            if (confirmPasswordLayout != null) {
                confirmPasswordLayout.setError("Passwords do not match");
            }
            isValid = false;
        }
        
        return isValid;
    }

    private void changePassword(String currentPassword, String newPassword) {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(getContext(), "Authentication error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword, newPassword);
        ApiService apiService = ApiClient.getApiService();
        Call<ChangePasswordResponse> call = apiService.changePassword(token, request);
        
        call.enqueue(new Callback<ChangePasswordResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChangePasswordResponse> call, @NonNull Response<ChangePasswordResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChangePasswordResponse changePasswordResponse = response.body();
                    if (changePasswordResponse.isSuccess()) {
                        Toast.makeText(getContext(), 
                            changePasswordResponse.getMessage() != null ? 
                                changePasswordResponse.getMessage() : "Password changed successfully", 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = changePasswordResponse.getMessage();
                        if (errorMessage == null || errorMessage.isEmpty()) {
                            errorMessage = "Failed to change password";
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    handlePasswordChangeError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChangePasswordResponse> call, @NonNull Throwable t) {
                Log.e("EmployeeSettingsFragment", "Change password failed: " + t.getMessage());
                Toast.makeText(getContext(), "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handlePasswordChangeError(Response<ChangePasswordResponse> response) {
        if (response.code() == 400 || response.code() == 422) {
            try {
                ChangePasswordResponse errorResponse = response.body();
                if (errorResponse != null && errorResponse.getErrors() != null) {
                    StringBuilder errorMsg = new StringBuilder();
                    ChangePasswordResponse.Errors errors = errorResponse.getErrors();
                    
                    if (errors.getCurrent_password() != null && errors.getCurrent_password().length > 0) {
                        errorMsg.append(errors.getCurrent_password()[0]).append("\n");
                    }
                    if (errors.getNew_password() != null && errors.getNew_password().length > 0) {
                        errorMsg.append(errors.getNew_password()[0]).append("\n");
                    }
                    if (errors.getNew_password_confirmation() != null && errors.getNew_password_confirmation().length > 0) {
                        errorMsg.append(errors.getNew_password_confirmation()[0]);
                    }
                    
                    if (errorMsg.length() > 0) {
                        Toast.makeText(getContext(), errorMsg.toString().trim(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), 
                            errorResponse.getMessage() != null ? errorResponse.getMessage() : "Invalid input", 
                            Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Invalid input. Please check your passwords.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Failed to change password. Please try again.", Toast.LENGTH_SHORT).show();
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
