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
            continueButton.setText("Please wait...");
        }

        // Directly proceed with registration flow.
        // Backend will perform the real uniqueness check and return a clear error if needed.
        proceedWithEmailRegistration(email);
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
