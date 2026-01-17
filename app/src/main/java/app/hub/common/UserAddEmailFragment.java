package app.hub.common;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.hub.R;

public class UserAddEmailFragment extends Fragment {

    private EditText emailInput;
    private Button continueButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_add_email, container, false);
        
        initializeViews(view);
        setupValidation();
        setupButtons(view);
        
        return view;
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
        RegisterActivity activity = (RegisterActivity) getActivity();
        if (activity != null) {
            activity.setUserEmail(getText(emailInput));
            activity.showTellUsFragment();
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
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
