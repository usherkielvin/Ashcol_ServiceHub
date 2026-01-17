package app.hub.common;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import app.hub.R;

public class UserCreatePasswordFragment extends Fragment {

    private TextInputEditText passwordInput, confirmPasswordInput;
    private TextView passValError, passRate, confirmPassValError;
    private Button continueButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_create_pass, container, false);
        
        initializeViews(view);
        setupValidation();
        setupButtons(view);
        
        return view;
    }

    private void initializeViews(View view) {
        passwordInput = view.findViewById(R.id.Pass_val);
        confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);
        continueButton = view.findViewById(R.id.verifyButton);

        // Validation displays
        passValError = view.findViewById(R.id.val_password);
        passRate = view.findViewById(R.id.passrate);
        confirmPassValError = view.findViewById(R.id.val_confirm_password);
    }

    private void setupValidation() {
        if (passwordInput != null) {
            passwordInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    validatePasswordAndStrength(s.toString());
                    if (confirmPasswordInput != null && confirmPasswordInput.getText() != null) {
                        validateConfirmPassword(s.toString(), confirmPasswordInput.getText().toString());
                    }
                }
            });
        }

        if (confirmPasswordInput != null) {
            confirmPasswordInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (passwordInput != null && passwordInput.getText() != null) {
                        validateConfirmPassword(passwordInput.getText().toString(), s.toString());
                    }
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
                if (validateAllFields()) {
                    savePasswordAndContinue();
                }
            });
        }
    }

    private boolean validateAllFields() {
        String password = getText(passwordInput);
        String confirmPassword = getText(confirmPasswordInput);

        if (password.isEmpty()) {
            showError("Password required");
            return false;
        }

        if (password.length() < 8) {
            showError("Password must be at least 8 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void savePasswordAndContinue() {
        RegisterActivity activity = (RegisterActivity) getActivity();
        if (activity != null) {
            activity.setUserPassword(getText(passwordInput));
            // TODO: Now make API call with all collected data or show OTP
            activity.showOtpVerification();
        }
    }

    private void validatePasswordAndStrength(String password) {
        if (passValError == null || passRate == null) return;

        if (password.isEmpty()) {
            passValError.setVisibility(View.GONE);
            passRate.setVisibility(View.GONE);
            return;
        }

        // Check requirements
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        // Show first unmet requirement
        if (password.length() < 8) {
            passValError.setText("At least 8 characters");
            passValError.setVisibility(View.VISIBLE);
        } else if (!hasUppercase) {
            passValError.setText("One uppercase letter");
            passValError.setVisibility(View.VISIBLE);
        } else if (!hasNumber) {
            passValError.setText("One number");
            passValError.setVisibility(View.VISIBLE);
        } else if (!hasSymbol) {
            passValError.setText("One symbol");
            passValError.setVisibility(View.VISIBLE);
        } else {
            passValError.setVisibility(View.GONE);
        }

        // Show strength
        int strength = 0;
        if (password.length() >= 8) strength++;
        if (password.length() > 10) strength++;
        if (hasUppercase) strength++;
        if (hasNumber) strength++;
        if (hasSymbol) strength++;

        passRate.setVisibility(View.VISIBLE);
        if (strength < 3) {
            passRate.setText("Weak");
            passRate.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if (strength < 5) {
            passRate.setText("Good");
            passRate.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            passRate.setText("Strong");
            passRate.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void validateConfirmPassword(String password, String confirmPassword) {
        if (confirmPassValError == null) return;

        if (!confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
            confirmPassValError.setText("Passwords do not match");
            confirmPassValError.setVisibility(View.VISIBLE);
        } else {
            confirmPassValError.setVisibility(View.GONE);
        }
    }

    private String getText(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void showError(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
