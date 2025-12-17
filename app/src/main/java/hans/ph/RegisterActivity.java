package hans.ph;

import hans.ph.api.ApiClient;
import hans.ph.api.ApiService;
import hans.ph.api.RegisterRequest;
import hans.ph.api.RegisterResponse;
import hans.ph.api.VerificationRequest;
import hans.ph.api.VerificationResponse;
import hans.ph.api.VerifyEmailRequest;
import hans.ph.api.VerifyEmailResponse;
import hans.ph.util.EmailValidator;
import hans.ph.util.TokenManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Activity for user registration
public class RegisterActivity extends AppCompatActivity {

	// UI Components
	private ProgressBar progressBar;

    private MaterialButton registerButton;
    private TextView messageTextView;
	private TextInputLayout emailInputLayout;

    private TextView fval, lval, uval, pval, cval, eval, passrate;
    private TextInputEditText firstNameInput, lastNameInput, usernameInput, passwordInput, confirmPasswordInput;
    private Button openOTPButton;
	// Data
	private TokenManager tokenManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		// Initialize components
		tokenManager = new TokenManager(this);
        initializeViews();
     //   setupEmailValidation(); // You already have this
        setupRealTimeValidationListeners(); // <-- ADD THIS NEW METHOD CALL
        setupRegisterButton();
        setupBackToLoginButton();

        setupOpenOTPButton();
    }
    private void setupOpenOTPButton() {
        // The button is initially hidden. We will set the listener later
        // when we have the email, but we can find the view now.
        openOTPButton = findViewById(R.id.OpenOTP);
        if (openOTPButton != null) {
            openOTPButton.setVisibility(View.GONE); // Ensure it's hidden at start
        }
    }
    /**
     * Sets up real-time validation listeners for all relevant input fields.
     */
    // Add these new methods to RegisterActivity.java

    /**
     * Sets up real-time validation listeners for all relevant input fields.
     */
    // In RegisterActivity.java

    private void setupRealTimeValidationListeners() {
        // First Name Listener
        firstNameInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                validateFirstName(s.toString());
            }
        });

        // Last Name Listener
        lastNameInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                validateLastName(s.toString());
            }
        });

        // Username Listener
        usernameInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                validateUsername(s.toString());
            }
        });

        // Password Listener
        passwordInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                validatePasswordAndStrength(s.toString());
                // Also validate confirm password whenever password changes
                validateConfirmPassword(s.toString(), getTextFromEditText(R.id.confirmPasswordInput));
            }
        });

        // Confirm Password Listener (MOVED TO THE CORRECT LOCATION)
        confirmPasswordInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                validateConfirmPassword(getTextFromEditText(R.id.passwordInput), s.toString());
            }
        });
        TextInputEditText emailInput = findViewById(R.id.emailInput);
        emailInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                validateEmail(s.toString());
            }
        });
    }

// Add this new method to RegisterActivity.java

    /**
     * Validates the email address in real-time as the user types.
     * Updates the 'eval' TextView.
     *
     * @param email The current text in the email input field.
     */
    private void validateEmail(String email) {
        if (eval == null) return; // 'eval' is the TextView with ID R.id.Email_val

        // Hide the validation message if the field is empty
        if (email.isEmpty()) {
            eval.setVisibility(View.GONE);
            return;
        }

        // Use Android's built-in pattern to check for a valid email format
        // This will correctly flag "d" as invalid but "d@g" as potentially valid
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // The format is valid, so hide the error message
            eval.setVisibility(View.GONE);
        } else {
            // The format is invalid, show the error message
            eval.setText("Invalid email format");
            eval.setVisibility(View.VISIBLE);
        }
    }

    private void validatePasswordAndStrength(java.lang.String password) {
        if (pval == null || passrate == null) return;

        // --- Part 1: Check for Specific Requirements (updates pval) ---

        // Define regex patterns for each requirement
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        if (password.isEmpty()) {
            pval.setVisibility(View.GONE); // Hide if empty
            passrate.setVisibility(View.GONE);
            return;
        }

        // Build the error message for pval
        StringBuilder requirements = new StringBuilder();
        if (password.length() < 8) {
            requirements.append("• At least 8 characters\n");
        }
        if (!hasUppercase) {
            requirements.append("• One uppercase letter\n");
        }
        if (!hasNumber) {
            requirements.append("• One number\n");
        }
        if (!hasSymbol) {
            requirements.append("• One symbol (@#$...)\n");
        }

        // Display or hide the requirements list (pval)
        if (requirements.length() > 0) {
            // Remove the last newline character for cleaner display
            pval.setText(requirements.toString().trim());
            pval.setVisibility(View.VISIBLE);
        } else {
            // All requirements are met, hide the validation message
            pval.setVisibility(View.GONE);
        }


        // --- Part 2: Calculate and Display Strength Score (updates passrate) ---

        int strengthScore = 0;
        if (password.length() >= 8) strengthScore++;
        if (password.length() > 10) strengthScore++; // Bonus for longer passwords
        if (hasUppercase) strengthScore++;
        if (hasNumber) strengthScore++;
        if (hasSymbol) strengthScore++;

        passrate.setVisibility(View.VISIBLE); // Show the strength meter

        // Set the text and color based on the score
        if (strengthScore < 3) {
            passrate.setText("Strength: Weak");
            passrate.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); // Red for weak
        } else if (strengthScore < 5) {
            passrate.setText("Strength: Good");
            passrate.setTextColor(getResources().getColor(android.R.color.holo_orange_dark)); // Orange for good
        } else {
            passrate.setText("Strength: Strong");
            passrate.setTextColor(getResources().getColor(android.R.color.holo_green_dark)); // Green for strong
        }
    }







// --- Validation Logic Methods ---

    private void validateFirstName(String firstName) {
        if (firstName.matches(".*\\d+.*")) {
            fval.setText("Name cannot contain numbers");
            fval.setVisibility(View.VISIBLE);
        } else if (!firstName.isEmpty() && firstName.length() < 2) {
            fval.setText("Name is too short");
            fval.setVisibility(View.VISIBLE);
        } else {
            fval.setVisibility(View.GONE);
        }
    }

    private void validateLastName(String lastName) {
        if (lastName.matches(".*\\d+.*")) {
            lval.setText("Name cannot contain numbers");
            lval.setVisibility(View.VISIBLE);
        } else {
            lval.setVisibility(View.GONE);
        }
    }

    private void validateUsername(String username) {
        if (username.contains(" ")) {
            uval.setText("Username cannot contain spaces");
            uval.setVisibility(View.VISIBLE);
        } else if (!username.isEmpty() && username.length() < 4) {
            uval.setText("Username is too short (min 4)");
            uval.setVisibility(View.VISIBLE);
        } else {
            uval.setVisibility(View.GONE);
        }
    }



    private void validateConfirmPassword(String password, String confirmPassword) {
        if (!confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
            cval.setText("Passwords do not match");
            cval.setVisibility(View.VISIBLE);
        } else {
            cval.setVisibility(View.GONE);
        }
    }


    // Initialize all view components
	private void initializeViews() {
		progressBar = findViewById(R.id.progressBar);
		messageTextView = findViewById(R.id.messageTextView);
		emailInputLayout = findViewById(R.id.emailInputLayout);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        openOTPButton = findViewById(R.id.OpenOTP);
        registerButton = findViewById(R.id.registerButton);

        // Initialize Validation TextViews
        fval = findViewById(R.id.fname_val);
        lval = findViewById(R.id.lname_val);
        uval = findViewById(R.id.Uname_val);
        pval = findViewById(R.id.Pass_val);
        cval = findViewById(R.id.CPass_val);
        eval = findViewById(R.id.Email_val); // You already had an 'eval'
        passrate = findViewById(R.id.passrate);

        // Hide all validation text views by default
        fval.setVisibility(View.GONE);
        lval.setVisibility(View.GONE);
        uval.setVisibility(View.GONE);
        pval.setVisibility(View.GONE);
        cval.setVisibility(View.GONE);
        eval.setVisibility(View.GONE);


	}

	// Setup real-time email validation as user types
	private void setupEmailValidation() {
		TextInputEditText emailInput = findViewById(R.id.emailInput);
		if (emailInput == null || emailInputLayout == null) {
			return;
		}

		emailInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				clearEmailErrorIfNeeded();
			}

			@Override
			public void afterTextChanged(Editable s) {
				validateEmailInRealTime(s.toString().trim());
			}
		});
	}

	// Clear email error if it's an "already used" error (user is fixing it)
	private void clearEmailErrorIfNeeded() {
		if (emailInputLayout == null || emailInputLayout.getError() == null) {
			return;
		}

		String error = emailInputLayout.getError().toString();
		boolean isEmailTakenError = error.contains("already") || 
		                           error.contains("taken") || 
		                           error.contains("exists");

		if (isEmailTakenError) {
			emailInputLayout.setError(null);
		}
	}

	// Validate email format in real-time as user types
    // In RegisterActivity.java, modify this method

    private void validateEmailInRealTime(String email) {
        // Use your 'eval' TextView instead of the TextInputLayout's error
        if (email.isEmpty()) {
            eval.setVisibility(View.GONE); // Hide when empty
            emailInputLayout.setError(null); // Also clear the layout's error
            return;
        }

        EmailValidator.ValidationResult result = EmailValidator.validate(email);

        // Check for "already used" error on the layout first
        String currentLayoutError = emailInputLayout.getError() != null ? emailInputLayout.getError().toString() : "";
        boolean isEmailTakenError = currentLayoutError.contains("already") || currentLayoutError.contains("taken");

        if (!result.isValid() && email.length() > 5) {
            eval.setText(result.getMessage());
            eval.setVisibility(View.VISIBLE);
            emailInputLayout.setError(null); // Clear layout error to avoid double messages
        } else if (isEmailTakenError) {
            // If the email format is now valid, but it's still "taken", hide our custom TextView
            eval.setVisibility(View.GONE);
        } else {
            // If valid, hide our custom TextView
            eval.setVisibility(View.GONE);
            emailInputLayout.setError(null); // Clear any old errors
        }
    }

	// Setup register button click listener
    private void setupRegisterButton() {
        if (registerButton == null) {return;
        }

        registerButton.setOnClickListener(v -> {
            hideMessage();

            // Get input values
            String username = getTextFromEditText(R.id.usernameInput);
            String firstName = getTextFromEditText(R.id.firstNameInput);
            String lastName = getTextFromEditText(R.id.lastNameInput);
            String email = getTextFromEditText(R.id.emailInput);
            String password = getTextFromEditText(R.id.passwordInput);
            String confirmPassword = getTextFromEditText(R.id.confirmPasswordInput);

            // Validate and register
            if (validateInput(username, firstName, lastName, email, password, confirmPassword)) {
                // ---> KEY CHANGE: Disable the button right before making the API call <---
                registerButton.setEnabled(false);
                registerUser(username, firstName, lastName, email, password, confirmPassword);
            }
        });
    }

	// Setup back to login button
	private void setupBackToLoginButton() {
		MaterialButton backButton = findViewById(R.id.backToLoginButton);
		if (backButton == null) {
			return;
		}

		backButton.setOnClickListener(v -> {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		});
	}

	// Helper method to get text from EditText
	private String getTextFromEditText(int viewId) {
		TextInputEditText editText = findViewById(viewId);
		if (editText == null || editText.getText() == null) {
			return "";
		}
		return editText.getText().toString().trim();
	}

	// Validate all input fields before registration
	private boolean validateInput(String username, String firstName, String lastName, String email, String password, String confirmPassword) {
		StringBuilder errors = new StringBuilder();

		// Validate username
		if (username.isEmpty()) {
			errors.append("• Please enter your username\n");
		}

		// Validate first name
		if (firstName.isEmpty()) {
			errors.append("• Please enter your first name\n");
		}

		// Validate last name
		if (lastName.isEmpty()) {
			errors.append("• Please enter your last name\n");
		}

		// Validate email
		if (email.isEmpty()) {
			errors.append("• Please enter your email\n");
		} else {
			EmailValidator.ValidationResult result = EmailValidator.validate(email);
			if (!result.isValid()) {
				errors.append("• ").append(result.getMessage()).append("\n");
			}
		}

		// Validate password
		if (password.isEmpty()) {
			errors.append("• Please enter a password\n");
		} else if (password.length() < 8) {
			errors.append("• Password must be at least 8 characters\n");
		}

		// Validate password confirmation
		if (confirmPassword.isEmpty()) {
			errors.append("• Please confirm your password\n");
		} else if (!password.equals(confirmPassword)) {
			errors.append("• Passwords do not match\n");
		}

		// Show errors if any
		if (errors.length() > 0) {
			showMessage(errors.toString().trim());
			return false;
		}

		return true;
	}

	// Show error message to user
	private void showMessage(String message) {
		if (messageTextView == null) {
			return;
		}

		messageTextView.setText(message);
		messageTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
		messageTextView.setVisibility(View.VISIBLE);
	}

	// Hide the message TextView
	private void hideMessage() {
		if (messageTextView != null) {
			messageTextView.setVisibility(View.GONE);
			messageTextView.setText("");
		}
	}

	// Register a new user with the API
	private void registerUser(String username, String firstName, String lastName, String email, String password, String confirmPassword) {
		setLoadingState(true);

		// Create API request
		ApiService apiService = ApiClient.getApiService();
		RegisterRequest request = new RegisterRequest(username, firstName, lastName, email, password, confirmPassword);

		// Make API call
		Call<RegisterResponse> call = apiService.register(request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                setLoadingState(false);
                // Note: We don't re-enable the button on success here because we want it to stay disabled.
                handleRegisterResponse(response, email);
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                setLoadingState(false);
                // ---> KEY CHANGE: Re-enable on failure <---
                registerButton.setEnabled(true);
                handleRegisterFailure(t);
            }
        });
	}

	// Handle successful registration response
    private void handleRegisterResponse(Response<RegisterResponse> response, String email) {
        if (response.isSuccessful() && response.body() != null) {
            RegisterResponse registerResponse = response.body();
            handleSuccessfulRegistration(registerResponse, email);
        } else {
            // ---> KEY CHANGE: Re-enable on error <---
            registerButton.setEnabled(true);
            handleRegistrationError(response);
        }
    }

	// Handle successful registration (with or without verification)
    private void handleSuccessfulRegistration(RegisterResponse response, String email) {
        if (!response.isSuccess() || response.getData() == null) {
            handleRegistrationErrors(response);
            return;
        }

        // Check if email verification is required
        if (response.getData().isRequires_verification()) {
            // --- KEY CHANGES START HERE ---

            // 1. Show the OTP dialog for the first time
            showVerificationDialog(email);

            // 2. Make the "Re-open OTP" button visible on the main screen
            if (openOTPButton != null) {
                openOTPButton.setVisibility(View.VISIBLE);

                // 3. Set its click listener so it can re-open the dialog
                openOTPButton.setOnClickListener(v -> {
                    // When clicked, simply show the verification dialog again
                    showVerificationDialog(email);
                });
            }
            // --- KEY CHANGES END HERE ---

        } else {
            // Auto-login if verification not required
            saveUserDataAndNavigate(response);
        }
    }

	// Save user data and navigate to dashboard
	private void saveUserDataAndNavigate(RegisterResponse response) {
		if (response.getData().getToken() == null) {
			return;
		}

		// Save user data
		RegisterResponse.User user = response.getData().getUser();
		tokenManager.saveToken("Bearer " + response.getData().getToken());
		tokenManager.saveEmail(user.getEmail());
		
		// Build name from firstName and lastName (RegisterResponse.User doesn't have getName())
		String firstName = user.getFirstName();
		String lastName = user.getLastName();
		StringBuilder nameBuilder = new StringBuilder();
		if (firstName != null && !firstName.trim().isEmpty()) {
			nameBuilder.append(firstName.trim());
		}
		if (lastName != null && !lastName.trim().isEmpty()) {
			if (nameBuilder.length() > 0) {
				nameBuilder.append(" ");
			}
			nameBuilder.append(lastName.trim());
		}
		
		String userName = nameBuilder.toString();
		if (userName != null && !userName.isEmpty()) {
			tokenManager.saveName(userName);
		}

		// Show success and navigate (reusing firstName and lastName from above)
		String userEmail = user.getEmail();
		String message = String.format("Welcome %s %s! You have been registered successfully.", firstName, lastName);
		
		runOnUiThread(() -> showSuccess("Registration Successful", message, userEmail));
	}

	// Handle registration errors from backend
	private void handleRegistrationError(Response<RegisterResponse> response) {
        String errorBody = null;
        try {
            if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
                Log.e("RegistrationError", "Response code: " + response.code() + ", Error body: " + errorBody);
            } else {
                Log.e("RegistrationError", "Response code: " + response.code() + ", No error body");
            }
        } catch (java.io.IOException e) {
            Log.e("RegistrationError", "Error parsing error body", e);
        }
		String errorMessage = getErrorMessage(response, errorBody);
		showErrorMessage(errorMessage, response.code());
	}

	// Handle errors in successful response body
	private void handleRegistrationErrors(RegisterResponse response) {
		// Check for email-specific errors first
		if (response.getErrors() != null && response.getErrors().getEmail() != null) {
			String[] emailErrors = response.getErrors().getEmail();
			if (emailErrors.length > 0) {
				String emailError = normalizeEmailError(emailErrors[0]);
				runOnUiThread(() -> {
					if (emailInputLayout != null) {
						emailInputLayout.setError(emailError);
					}
				});
				return;
			}
		}

		// Show general error message
		String errorMsg = response.getMessage();
		if (response.getErrors() != null) {
			String formattedErrors = formatErrors(response.getErrors());
			if (!formattedErrors.isEmpty()) {
				errorMsg = formattedErrors;
			}
		}

		final String finalMessage = errorMsg != null ? errorMsg : "Registration failed";
		runOnUiThread(() -> showMessage(finalMessage));
	}

	// Get error message from HTTP error response
	private String getErrorMessage(Response<RegisterResponse> response, String errorBody) {
		int statusCode = response.code();

		// Try to parse error message from response body first
		if (errorBody != null && !errorBody.isEmpty()) {
			try {
				com.google.gson.Gson gson = new com.google.gson.Gson();
				RegisterResponse errorResponse = gson.fromJson(errorBody, RegisterResponse.class);
				
				// Check if there's a message in the response
				if (errorResponse != null && errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty()) {
					String serverMessage = errorResponse.getMessage();
					Log.d("RegistrationError", "Server message: " + serverMessage);
					
					// Check for email errors in the response
					if (errorResponse.getErrors() != null && errorResponse.getErrors().getEmail() != null) {
						String[] emailErrors = errorResponse.getErrors().getEmail();
						if (emailErrors.length > 0) {
							String emailError = normalizeEmailError(emailErrors[0]);
							runOnUiThread(() -> {
								if (emailInputLayout != null) {
									emailInputLayout.setError(emailError);
								}
							});
							return emailError;
						}
					}
					
					// Return the server's error message
					return serverMessage;
				}
			} catch (Exception e) {
				Log.e("RegistrationError", "Failed to parse error body: " + errorBody, e);
			}
		}

		// Handle validation errors (422)
		if (statusCode == 422) {
			return parseValidationError(response);
		}

		// Handle email conflict (409)
		if (statusCode == 409) {
			runOnUiThread(() -> {
				if (emailInputLayout != null) {
					emailInputLayout.setError("Email already used");
				}
			});
			return "Email already exists. Please use a different email.";
		}

		// Handle server errors (500)
		if (statusCode == 500) {
			return "Server error. Please check your Laravel server logs and try again.";
		}

		// Handle other errors
		if (statusCode >= 400 && statusCode < 500) {
			return "Invalid request. Please check your input and try again.";
		}

		return "Registration failed. Please try again.";
	}

	// Parse validation error from error response body
	@SuppressWarnings("resource")
	private String parseValidationError(Response<RegisterResponse> response) {
		okhttp3.ResponseBody errorBody = response.errorBody();
		if (errorBody == null) {
			return "Validation error. Please check your input.";
		}

		try {
			com.google.gson.Gson gson = new com.google.gson.Gson();
			// ResponseBody.string() consumes the body, so we can't use try-with-resources here
			// The Retrofit library handles cleanup automatically
			String errorJson = errorBody.string();
			RegisterResponse errorResponse = gson.fromJson(errorJson, RegisterResponse.class);

			// Check for email errors
			if (errorResponse.getErrors() != null && errorResponse.getErrors().getEmail() != null) {
				String[] emailErrors = errorResponse.getErrors().getEmail();
				if (emailErrors.length > 0) {
					String emailError = normalizeEmailError(emailErrors[0]);
					runOnUiThread(() -> {
						if (emailInputLayout != null) {
							emailInputLayout.setError(emailError);
						}
					});
					return emailError;
				}
			}

			// Format other errors
			if (errorResponse.getErrors() != null) {
				String formattedErrors = formatErrors(errorResponse.getErrors());
				if (!formattedErrors.isEmpty()) {
					return formattedErrors;
				}
			}

			return errorResponse.getMessage() != null ? 
				errorResponse.getMessage() : 
				"Validation error. Please check your input.";

		} catch (Exception e) {
			return "Validation error. Please check your input.";
		}
	}

	// Normalize email error message to a standard format
	private String normalizeEmailError(String error) {
		if (error.contains("already") || error.contains("taken") || error.contains("unique")) {
			return "Email already used";
		}
		return error;
	}
	// Show error message to user
	private void showErrorMessage(String message, int statusCode) {
		runOnUiThread(() -> {
			showMessage(message);
			// Only show dialog if email error is not already shown in field
			boolean emailErrorShown = emailInputLayout != null && emailInputLayout.getError() != null;
			if (statusCode != 422 || !emailErrorShown) {
				showError("Registration Failed", message);
			}
		});
	}

	// Handle registration failure (network errors, etc.)
	private void handleRegisterFailure(Throwable t) {
		Log.e("RegistrationError", "Registration failed with exception", t);
		String errorMsg = getConnectionErrorMessage(t);
		runOnUiThread(() -> {
			showMessage(errorMsg);
			showError("Connection Error", errorMsg);
		});
	}

	// Get user-friendly connection error message with diagnostics
	private String getConnectionErrorMessage(Throwable t) {
		if (t.getMessage() == null) {
			return "Connection error. Please try again.";
		}

		String message = t.getMessage();
		String baseUrl = ApiClient.getBaseUrl();
		StringBuilder errorMsg = new StringBuilder();
		
		if (message.contains("Failed to connect") || message.contains("Unable to resolve host")) {
			errorMsg.append("❌ Cannot connect to server\n\n");
			errorMsg.append("Trying to reach: ").append(baseUrl).append("/api/v1/register\n\n");
			errorMsg.append("Please check:\n");
			errorMsg.append("1. Laravel server is running: php artisan serve\n");
			errorMsg.append("2. Server is on port 8000 (default)\n");
			errorMsg.append("3. For emulator, use: http://10.0.2.2:8000\n");
			errorMsg.append("4. For physical device, use your computer's IP\n");
			errorMsg.append("5. Check ApiClient.java BASE_URL setting\n");
			errorMsg.append("6. Verify network_security_config.xml allows cleartext\n\n");
			errorMsg.append("Test in browser: ").append(baseUrl).append("/api/v1/register");
		} else if (message.contains("timeout")) {
			errorMsg.append("⏱ Connection timeout\n\n");
			errorMsg.append("Server may be slow or unreachable.\n");
			errorMsg.append("Check if Laravel server is running and accessible.");
		} else if (message.contains("Connection refused")) {
			errorMsg.append("🔌 Connection refused\n\n");
			errorMsg.append("Server is not running or not accessible.\n");
			errorMsg.append("Please start Laravel server: php artisan serve\n");
			errorMsg.append("Expected URL: ").append(baseUrl);
		} else {
			errorMsg.append("❌ Connection error\n\n");
			errorMsg.append("Details: ").append(message);
		}

		return errorMsg.toString();
	}

	// Show or hide loading state during registration
	private void setLoadingState(boolean isLoading) {
		runOnUiThread(() -> {
			MaterialButton registerButton = findViewById(R.id.registerButton);
			if (registerButton != null) {
				registerButton.setEnabled(!isLoading);
				registerButton.setText(isLoading ? 
					getString(R.string.registering) : 
					getString(R.string.register));
			}

			if (progressBar != null) {
				progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
			}

			if (!isLoading) {
				hideMessage();
			}
		});
	}

	// Show email verification dialog
	private void showVerificationDialog(String email) {
		android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_verification_code, null);
		
		// Initialize dialog views
		TextInputEditText codeInput = dialogView.findViewById(R.id.codeInput);
		TextInputLayout codeInputLayout = dialogView.findViewById(R.id.codeInputLayout);
		MaterialButton verifyButton = dialogView.findViewById(R.id.verifyButton);
		MaterialButton resendCodeButton = dialogView.findViewById(R.id.resendCodeButton);
		android.widget.TextView messageTextView = dialogView.findViewById(R.id.verificationMessage);
		android.widget.ImageButton closeButton = dialogView.findViewById(R.id.closeButton);
		
		// Set dialog message
		if (messageTextView != null) {
			// Email is dynamic, so we format the string
			String message = String.format("Enter the 6-digit code sent to\n%s", email);
			messageTextView.setText(message);
		}

		// Create and show dialog
		AlertDialog dialog = new MaterialAlertDialogBuilder(this)
			.setView(dialogView)
			.setCancelable(true)
			.create();
		
		// Setup dialog components
		setupVerificationDialogCloseButton(closeButton, dialog);
		setupVerificationCodeInput(codeInput, codeInputLayout, verifyButton);
		setupVerifyButton(verifyButton, codeInput, codeInputLayout, email, dialog);
		setupResendButton(resendCodeButton, email);

		dialog.show();
	}

	// Setup close button for verification dialog
	private void setupVerificationDialogCloseButton(android.widget.ImageButton closeButton, AlertDialog dialog) {
		if (closeButton != null) {
			closeButton.setOnClickListener(v -> dialog.dismiss());
		}
	}

	// Setup code input field with real-time validation
	private void setupVerificationCodeInput(TextInputEditText codeInput, 
	                                       TextInputLayout codeInputLayout, 
	                                       MaterialButton verifyButton) {
		if (codeInput == null) {
			return;
		}

		// Clear any previous errors
		if (codeInputLayout != null) {
			codeInputLayout.setError(null);
		}

		// Auto-focus and show keyboard
		codeInput.post(() -> {
			codeInput.requestFocus();
			android.view.inputmethod.InputMethodManager imm = 
				(android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.showSoftInput(codeInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
			}
		});
		
		// Enable verify button when code is entered
		codeInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				if (codeInputLayout != null) {
					codeInputLayout.setError(null);
				}
				if (verifyButton != null) {
					verifyButton.setEnabled(s.length() > 0);
				}
			}
		});
	}

	// Setup verify button click handler
	@SuppressLint("SetTextI18n")
	private void setupVerifyButton(MaterialButton verifyButton, 
	                              TextInputEditText codeInput, 
	                              TextInputLayout codeInputLayout,
	                              String email, 
	                              AlertDialog dialog) {
		if (verifyButton == null) {
			return;
		}

		verifyButton.setEnabled(false);
		verifyButton.setOnClickListener(v -> {
			String code = "";
			if (codeInput != null && codeInput.getText() != null) {
				code = codeInput.getText().toString().trim();
			}
			
			// Validate code length
			if (code.length() != 6) {
				if (codeInputLayout != null) {
					codeInputLayout.setError("Please enter a 6-digit code");
				}
				return;
			}

			// Clear error and verify
			if (codeInputLayout != null) {
				codeInputLayout.setError(null);
			}
			verifyButton.setEnabled(false);
			verifyButton.setText("Verifying..."); // Temporary loading state
			verifyEmail(email, code, dialog, verifyButton);
		});
	}

	// Setup resend code button
	@SuppressLint("SetTextI18n")
	private void setupResendButton(MaterialButton resendButton, String email) {
		if (resendButton == null) {
			return;
		}

		resendButton.setOnClickListener(v -> {
			resendButton.setEnabled(false);
			resendButton.setText("Sending..."); // Temporary loading state
			resendVerificationCode(email, resendButton);
		});
	}

	// Verify email with verification code
	private void verifyEmail(String email, String code, AlertDialog dialog, MaterialButton verifyButton) {
		ApiService apiService = ApiClient.getApiService();
		VerifyEmailRequest request = new VerifyEmailRequest(email, code);

		Call<VerifyEmailResponse> call = apiService.verifyEmail(request);
		call.enqueue(new Callback<>() {
			@Override
			public void onResponse(@NonNull Call<VerifyEmailResponse> call, @NonNull Response<VerifyEmailResponse> response) {
				resetVerifyButton(verifyButton);

				if (response.isSuccessful() && response.body() != null) {
					handleVerificationSuccess(response.body(), dialog);
				} else {
					handleVerificationError(response.code());
			}
			}

			@Override
			public void onFailure(@NonNull Call<VerifyEmailResponse> call, @NonNull Throwable t) {
				resetVerifyButton(verifyButton);
				showError("Connection Error", 
					"Failed to verify code. Please check your connection and try again.");
			}
		});
	}

	// Reset verify button to original state
	@SuppressLint("SetTextI18n")
	private void resetVerifyButton(MaterialButton verifyButton) {
		runOnUiThread(() -> {
			if (verifyButton != null) {
				verifyButton.setEnabled(true);
				verifyButton.setText("Verify"); // Button label
			}
		});
	}

	// Handle successful verification response
	private void handleVerificationSuccess(VerifyEmailResponse response, AlertDialog dialog) {
		if (!response.isSuccess() || response.getData() == null) {
			String errorMsg = response.getMessage() != null ? 
				response.getMessage() : 
				"Invalid verification code";
			showError("Verification Failed", errorMsg);
			return;
		}

		// Save user data
		VerifyEmailResponse.User user = response.getData().getUser();
		tokenManager.saveToken("Bearer " + response.getData().getToken());
		tokenManager.saveEmail(user.getEmail());
		
		// Build name from firstName and lastName
		String firstName = user.getFirstName();
		String lastName = user.getLastName();
		StringBuilder nameBuilder = new StringBuilder();
		if (firstName != null && !firstName.trim().isEmpty()) {
			nameBuilder.append(firstName.trim());
		}
		if (lastName != null && !lastName.trim().isEmpty()) {
			if (nameBuilder.length() > 0) {
				nameBuilder.append(" ");
			}
			nameBuilder.append(lastName.trim());
		}
		
		String userName = nameBuilder.toString();
		if (userName != null && !userName.isEmpty()) {
			tokenManager.saveName(userName);
		}

		// Close dialog and show success (reusing firstName and lastName from above)
		dialog.dismiss();
		String userEmail = user.getEmail();
		String message = String.format("Welcome %s %s! Your email has been verified successfully.", firstName, lastName);
		
		runOnUiThread(() -> showSuccess("Email Verified", message, userEmail));
	}

	// Handle verification error
	private void handleVerificationError(int statusCode) {
		String errorMsg = (statusCode == 400) ? 
			"Invalid or expired verification code. Please request a new code." :
			"Invalid verification code";
		
		showError("Verification Failed", errorMsg);
	}

	// Resend verification code
	private void resendVerificationCode(String email, MaterialButton resendButton) {
		ApiService apiService = ApiClient.getApiService();
		VerificationRequest request = new VerificationRequest(email);

		Call<VerificationResponse> call = apiService.sendVerificationCode(request);
		call.enqueue(new Callback<>() {
			@Override
			public void onResponse(@NonNull Call<VerificationResponse> call, @NonNull Response<VerificationResponse> response) {
				resetResendButton(resendButton);

				if (response.isSuccessful() && response.body() != null) {
					handleResendSuccess(response.body());
				} else {
					showError("Error", "Failed to send verification code. Please try again.");
				}
			}

			@Override
			public void onFailure(@NonNull Call<VerificationResponse> call, @NonNull Throwable t) {
				resetResendButton(resendButton);
				String errorMsg = getConnectionErrorMessage(t);
				showError("Connection Error", errorMsg);
			}
		});
	}

	// Reset resend button to original state
	@SuppressLint("SetTextI18n")
	private void resetResendButton(MaterialButton resendButton) {
		runOnUiThread(() -> {
			if (resendButton != null) {
				resendButton.setEnabled(true);
				resendButton.setText("Resend Code"); // Button label
			}
		});
	}

	// Handle successful resend response
	private void handleResendSuccess(VerificationResponse response) {
		if (response.isSuccess()) {
			String message = response.getMessage() != null ? 
				response.getMessage() : 
				"Verification code sent to your email";
			
			runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
		} else {
			String errorMsg = response.getMessage() != null ? 
				response.getMessage() : 
				"Failed to send code";
			showError("Error", errorMsg);
		}
	}

	// Format errors into a readable string
	private String formatErrors(RegisterResponse.Errors errors) {
		if (errors == null) {
			return "Please check your input";
		}

		StringBuilder errorMessage = new StringBuilder();
		
		// Format email errors
		if (errors.getEmail() != null && errors.getEmail().length > 0) {
			errorMessage.append("Email: ").append(errors.getEmail()[0]).append("\n");
		}
		
		// Format password errors
		if (errors.getPassword() != null && errors.getPassword().length > 0) {
			errorMessage.append("Password: ").append(errors.getPassword()[0]).append("\n");
		}
		
		// Format username errors
		if (errors.getUsername() != null && errors.getUsername().length > 0) {
			errorMessage.append("Username: ").append(errors.getUsername()[0]).append("\n");
		}

		// Format first name errors
		if (errors.getFirstName() != null && errors.getFirstName().length > 0) {
			errorMessage.append("First Name: ").append(errors.getFirstName()[0]).append("\n");
		}

		// Format last name errors
		if (errors.getLastName() != null && errors.getLastName().length > 0) {
			errorMessage.append("Last Name: ").append(errors.getLastName()[0]).append("\n");
		}
		
		return errorMessage.length() > 0 ? 
			errorMessage.toString().trim() : 
			"Please check your input";
	}

	// Show error dialog to user
	private void showError(String title, String message) {
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton("OK", null)
			.show();
	}

	// Show success dialog to user
	private void showSuccess(String title, String message, String email) {
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton("OK", (dialog, which) -> navigateToDashboard(email))
			.setCancelable(false)
			.show();
	}

	// Navigate to dashboard activity
	private void navigateToDashboard(String email) {
		Intent intent = new Intent(this, DashboardActivity.class);
		intent.putExtra(DashboardActivity.EXTRA_EMAIL, email);
		startActivity(intent);
		finish();
	}
}