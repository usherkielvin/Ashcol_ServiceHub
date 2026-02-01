package app.hub.common;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.LoginRequest;
import app.hub.api.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserAddEmailFragment extends Fragment {

    private static final String TAG = "UserAddEmailFragment";
    private EditText emailInput;
    private Button continueButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_add_email, container, false);
        
        initializeViews(view);
        setupValidation();
        setupButtons(view);
        
        // Pre-fill email if available (for Google users)
        prefillEmailIfAvailable();
        
        return view;
    }
    
    private void prefillEmailIfAvailable() {
        RegisterActivity activity = (RegisterActivity) getActivity();
        if (activity != null && emailInput != null) {
            String prefillEmail = activity.getUserEmail();
            if (prefillEmail != null && !prefillEmail.isEmpty()) {
                emailInput.setText(prefillEmail);
                // Move cursor to end
                if (emailInput.getText() != null) {
                    emailInput.setSelection(emailInput.getText().length());
                }
            }
        }
    }

    private void initializeViews(View view) {
        emailInput = view.findViewById(R.id.Email_val);
        continueButton = view.findViewById(R.id.OpenOTP);
    }

    private void setupValidation() {
        if (emailInput != null) {
            emailInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    validateEmail(s.toString());
                }
            });
        }
    }

    private void setupButtons(View view) {
        ImageButton backButton = view.findViewById(R.id.closeButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        if (continueButton != null) {
            continueButton.setOnClickListener(v -> {
                if (validateEmailField()) {
                    saveEmailAndContinue();
                }
            });
        }
    }

    private void validateEmail(String email) {
        // Real-time validation as user types
        if (email.isEmpty()) {
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Invalid email format
        }
    }

    private boolean validateEmailField() {
        String email = getText(emailInput);

        if (email.isEmpty()) {
            showError("Email is required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address");
            return false;
        }

        return true;
    }

    private void saveEmailAndContinue() {
        String email = getText(emailInput);
        
        // Show loading state
        if (continueButton != null) {
            continueButton.setEnabled(false);
            continueButton.setText("Checking email...");
        }
        
        // Check if email already exists in database
        checkEmailExists(email);
    }
    
    private void checkEmailExists(String email) {
        Log.d(TAG, "Checking if email exists in database: " + email);
        
        ApiService apiService = ApiClient.getApiService();
        // Use a dummy password to check if email exists
        LoginRequest request = new LoginRequest(email, "dummy_password_check_123");
        
        Call<LoginResponse> call = apiService.login(request);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                // Reset button state
                resetContinueButton();
                
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    
                    if (loginResponse.isSuccess()) {
                        // Login succeeded with dummy password - this shouldn't happen, but email exists
                        showEmailExistsError("Email already registered. Please login instead.");
                    } else {
                        // Login failed - check the error message
                        String message = loginResponse.getMessage();
                        if (message != null && message.toLowerCase().contains("invalid credentials")) {
                            // Wrong password but email exists (this is what we expect)
                            showEmailExistsError("Email already registered. Please login instead.");
                        } else {
                            // Other error - assume email doesn't exist and proceed
                            Log.d(TAG, "Login failed with message: " + message + " - assuming email doesn't exist");
                            proceedWithEmailRegistration(email);
                        }
                    }
                } else {
                    // HTTP error - check status code and error response
                    if (response.code() == 401) {
                        // Unauthorized - email exists but wrong password
                        showEmailExistsError("Email already registered. Please login instead.");
                    } else {
                        // Other HTTP error - check error body
                        handleLoginErrorResponse(response, email);
                    }
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                // Reset button state
                resetContinueButton();
                
                Log.e(TAG, "Network error checking email: " + t.getMessage(), t);
                Toast.makeText(getContext(), 
                    "Network error. Please check your connection and try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void handleLoginErrorResponse(Response<LoginResponse> response, String email) {
        String errorMessage = null;
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e(TAG, "Login error response: " + errorBody);
                
                // Try to parse error response
                com.google.gson.Gson gson = new com.google.gson.Gson();
                LoginResponse errorResponse = gson.fromJson(errorBody, LoginResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    errorMessage = errorResponse.getMessage();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing login error response", e);
        }
        
        if (errorMessage != null && errorMessage.toLowerCase().contains("invalid credentials")) {
            // Wrong password but email exists
            showEmailExistsError("Email already registered. Please login instead.");
        } else {
            // Other error or no specific message - assume email doesn't exist and proceed
            Log.w(TAG, "Unclear error response, proceeding with registration. Error: " + errorMessage);
            proceedWithEmailRegistration(email);
        }
    }
    
    private void showEmailExistsError(String message) {
        Log.d(TAG, "Email already exists: " + message);
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        
        // Navigate back to login screen
        if (getActivity() != null) {
            android.content.Intent intent = new android.content.Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }
    
    private void proceedWithEmailRegistration(String email) {
        Log.d(TAG, "Email doesn't exist, proceeding with registration");
        
        RegisterActivity activity = (RegisterActivity) getActivity();
        if (activity != null) {
            activity.setUserEmail(email);
            activity.showTellUsFragment();
        }
    }
    
    private void resetContinueButton() {
        if (continueButton != null) {
            continueButton.setEnabled(true);
            continueButton.setText("Continue");
        }
    }

    private String getText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
