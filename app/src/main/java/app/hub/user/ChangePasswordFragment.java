package app.hub.user;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.widget.ImageView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.ChangePasswordRequest;
import app.hub.api.ChangePasswordResponse;
import app.hub.util.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends Fragment {

    private TokenManager tokenManager;
    private TextInputEditText currentPasswordInput;
    private TextInputEditText newPasswordInput;
    private TextInputEditText confirmPasswordInput;
    private TextInputLayout currentPasswordLayout;
    private TextInputLayout newPasswordLayout;
    private TextInputLayout confirmPasswordLayout;
    private MaterialButton btnContinue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile__changepass, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());
        initializeViews(view);
        setupClickListeners();
    }

    private void initializeViews(View view) {
        currentPasswordInput = view.findViewById(R.id.currentPasswordInput);
        newPasswordInput = view.findViewById(R.id.newPasswordInput);
        confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);
        currentPasswordLayout = view.findViewById(R.id.currentPasswordLayout);
        newPasswordLayout = view.findViewById(R.id.newPasswordLayout);
        confirmPasswordLayout = view.findViewById(R.id.confirmPasswordLayout);
        btnContinue = view.findViewById(R.id.btnContinue);

        // Setup back button
        ImageView btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }
    }

    private void setupClickListeners() {
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> handleChangePassword());
        }
    }

    private void handleChangePassword() {
        String currentPassword = currentPasswordInput != null ? currentPasswordInput.getText().toString() : "";
        String newPassword = newPasswordInput != null ? newPasswordInput.getText().toString() : "";
        String confirmPassword = confirmPasswordInput != null ? confirmPasswordInput.getText().toString() : "";

        // Clear previous errors
        if (currentPasswordLayout != null) currentPasswordLayout.setError(null);
        if (newPasswordLayout != null) newPasswordLayout.setError(null);
        if (confirmPasswordLayout != null) confirmPasswordLayout.setError(null);

        if (validateInputs(currentPassword, newPassword, confirmPassword)) {
            changePassword(currentPassword, newPassword);
        }
    }

    private boolean validateInputs(String currentPassword, String newPassword, String confirmPassword) {
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

        if (btnContinue != null) {
            btnContinue.setEnabled(false);
            btnContinue.setText("Changing...");
        }

        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword, newPassword);
        ApiService apiService = ApiClient.getApiService();
        Call<ChangePasswordResponse> call = apiService.changePassword(token, request);

        call.enqueue(new Callback<ChangePasswordResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChangePasswordResponse> call, @NonNull Response<ChangePasswordResponse> response) {
                if (btnContinue != null) {
                    btnContinue.setEnabled(true);
                    btnContinue.setText("Change Password");
                }

                if (response.isSuccessful() && response.body() != null) {
                    ChangePasswordResponse changePasswordResponse = response.body();
                    if (changePasswordResponse.isSuccess()) {
                        Toast.makeText(getContext(),
                            changePasswordResponse.getMessage() != null ?
                                changePasswordResponse.getMessage() : "Password changed successfully",
                            Toast.LENGTH_SHORT).show();
                        // Clear input fields
                        if (currentPasswordInput != null) currentPasswordInput.setText("");
                        if (newPasswordInput != null) newPasswordInput.setText("");
                        if (confirmPasswordInput != null) confirmPasswordInput.setText("");
                        // Navigate back after successful password change
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
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
                if (btnContinue != null) {
                    btnContinue.setEnabled(true);
                    btnContinue.setText("Change Password");
                }
                Log.e("ChangePasswordFragment", "Change password failed: " + t.getMessage());
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
                        if (currentPasswordLayout != null) {
                            currentPasswordLayout.setError(errors.getCurrent_password()[0]);
                        }
                        errorMsg.append(errors.getCurrent_password()[0]).append("\n");
                    }
                    if (errors.getNew_password() != null && errors.getNew_password().length > 0) {
                        if (newPasswordLayout != null) {
                            newPasswordLayout.setError(errors.getNew_password()[0]);
                        }
                        errorMsg.append(errors.getNew_password()[0]).append("\n");
                    }
                    if (errors.getNew_password_confirmation() != null && errors.getNew_password_confirmation().length > 0) {
                        if (confirmPasswordLayout != null) {
                            confirmPasswordLayout.setError(errors.getNew_password_confirmation()[0]);
                        }
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
}
