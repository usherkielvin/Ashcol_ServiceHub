package hans.ph;

import hans.ph.R;
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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class RegisterActivity extends AppCompatActivity {

	private TokenManager tokenManager;
	private String registeredEmail;
	private String registeredName;
	private ProgressBar progressBar;
	private TextView messageTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		tokenManager = new TokenManager(this);

		final TextInputEditText nameInput = findViewById(R.id.nameInput);
		final TextInputEditText emailInput = findViewById(R.id.emailInput);
		TextInputEditText passwordInput = findViewById(R.id.passwordInput);
		TextInputEditText confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
		final TextInputLayout emailInputLayout = findViewById(R.id.emailInputLayout);
		progressBar = findViewById(R.id.progressBar);
		messageTextView = findViewById(R.id.messageTextView);
		MaterialButton registerButton = findViewById(R.id.registerButton);
		MaterialButton backToLoginButton = findViewById(R.id.backToLoginButton);

		// Real-time email validation
		if (emailInput != null) {
			emailInput.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// Clear email error when user starts typing (but keep format validation)
					if (emailInputLayout != null && emailInputLayout.getError() != null) {
						String currentError = emailInputLayout.getError().toString();
						// Only clear if it's an "already used" error, not format errors
						if (currentError.contains("already") || currentError.contains("taken") || currentError.contains("exists")) {
							// Clear error on first character change, format validation will show if needed
							if (s.length() > 0) {
								emailInputLayout.setError(null);
							}
						}
					}
				}

				@Override
				public void afterTextChanged(Editable s) {
					String email = s.toString().trim();
					if (email.isEmpty()) {
						if (emailInputLayout != null) {
							emailInputLayout.setError(null);
						}
						return;
					}

					EmailValidator.ValidationResult result = EmailValidator.validate(email);
					if (!result.isValid() && email.length() > 5) {
						// Only show error if user has typed enough characters
						if (emailInputLayout != null) {
							emailInputLayout.setError(result.getMessage());
						}
					} else {
						// Only clear error if it's a format error, not "already used" error
						if (emailInputLayout != null && emailInputLayout.getError() != null) {
							String currentError = emailInputLayout.getError().toString();
							if (!currentError.contains("already") && !currentError.contains("taken") && !currentError.contains("exists")) {
								emailInputLayout.setError(null);
							}
						}
					}
				}
			});
		}

		if (registerButton != null) {
			registerButton.setOnClickListener(v -> {
				// Clear previous messages
				hideMessage();

				String name = nameInput != null ? nameInput.getText().toString().trim() : "";
				String email = emailInput != null ? emailInput.getText().toString().trim() : "";
				String password = passwordInput != null ? passwordInput.getText().toString() : "";
				String confirmPassword = confirmPasswordInput != null ? confirmPasswordInput.getText().toString() : "";

				if (validateInput(name, email, password, confirmPassword)) {
					register(name, email, password, confirmPassword);
				}
			});
		}

		if (backToLoginButton != null) {
			backToLoginButton.setOnClickListener(v -> {
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				finish();
			});
		}
	}

	private boolean validateInput(String name, String email, String password, String confirmPassword) {
		boolean isValid = true;
		StringBuilder errorMessage = new StringBuilder();

		if (name.isEmpty()) {
			errorMessage.append("• Please enter your name\n");
			isValid = false;
		}

		if (email.isEmpty()) {
			errorMessage.append("• Please enter your email\n");
			isValid = false;
		} else {
			EmailValidator.ValidationResult emailValidation = EmailValidator.validate(email);
			if (!emailValidation.isValid()) {
				errorMessage.append("• ").append(emailValidation.getMessage()).append("\n");
				isValid = false;
			}
		}

		if (password.isEmpty()) {
			errorMessage.append("• Please enter a password\n");
			isValid = false;
		} else if (password.length() < 8) {
			errorMessage.append("• Password must be at least 8 characters\n");
			isValid = false;
		}

		if (confirmPassword.isEmpty()) {
			errorMessage.append("• Please confirm your password\n");
			isValid = false;
		} else if (!password.equals(confirmPassword)) {
			errorMessage.append("• Passwords do not match\n");
			isValid = false;
		}

		if (!isValid) {
			showMessage(errorMessage.toString().trim(), true);
		}

		return isValid;
	}

	private void showMessage(String message, boolean isError) {
		if (messageTextView != null) {
			messageTextView.setText(message);
			if (isError) {
				messageTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
			} else {
				messageTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
			}
			messageTextView.setVisibility(View.VISIBLE);
		}
	}

	private void hideMessage() {
		if (messageTextView != null) {
			messageTextView.setVisibility(View.GONE);
			messageTextView.setText("");
		}
	}

	private void register(String name, String email, String password, String confirmPassword) {
		// Show loading state
		setLoadingState(true);

		ApiService apiService = ApiClient.getApiService();
		RegisterRequest request = new RegisterRequest(name, email, password, confirmPassword);

		Call<RegisterResponse> call = apiService.register(request);
		call.enqueue(new Callback<RegisterResponse>() {
			@Override
			public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
				setLoadingState(false);

				if (response.isSuccessful() && response.body() != null) {
					RegisterResponse registerResponse = response.body();
					if (registerResponse.isSuccess() && registerResponse.getData() != null) {
						registeredEmail = email;
						registeredName = name;
						
						// Check if verification is required
						if (registerResponse.getData().isRequires_verification()) {
							// Show verification dialog
							runOnUiThread(() -> showVerificationDialog(email, name));
						} else {
							// Old flow - auto login (if verification not required)
							if (registerResponse.getData().getToken() != null) {
								tokenManager.saveToken("Bearer " + registerResponse.getData().getToken());
								tokenManager.saveEmail(registerResponse.getData().getUser().getEmail());
								tokenManager.saveName(registerResponse.getData().getUser().getName());

								runOnUiThread(() -> {
									showSuccess("Registration Successful", 
										"Welcome " + registerResponse.getData().getUser().getName() + "! You have been registered successfully.",
										registerResponse.getData().getUser().getEmail());
								});
							}
						}
					} else {
						// Handle validation errors from backend
						String errorMessage = registerResponse.getMessage();
						final TextInputLayout emailInputLayout = findViewById(R.id.emailInputLayout);
						
						// Check for email errors and show in email field
						if (registerResponse.getErrors() != null && registerResponse.getErrors().getEmail() != null) {
							String[] emailErrors = registerResponse.getErrors().getEmail();
							if (emailErrors.length > 0) {
								String emailError = emailErrors[0];
								// Normalize error message
								if (emailError.contains("already") || emailError.contains("taken") || emailError.contains("unique")) {
									emailError = "Email already used";
								}
								final String finalEmailError = emailError;
								runOnUiThread(() -> {
									if (emailInputLayout != null) {
										emailInputLayout.setError(finalEmailError);
									}
								});
								errorMessage = finalEmailError;
							}
						}
						
						if (registerResponse.getErrors() != null) {
							String formattedErrors = formatErrors(registerResponse.getErrors());
							if (formattedErrors != null && !formattedErrors.isEmpty()) {
								errorMessage = formattedErrors;
							}
						}
						
						final String finalMessage = errorMessage != null ? errorMessage : "Registration failed";
						runOnUiThread(() -> {
							showMessage(finalMessage, true);
							// Only show dialog if email error is not shown in field
							if (registerResponse.getErrors() == null || 
								registerResponse.getErrors().getEmail() == null ||
								registerResponse.getErrors().getEmail().length == 0) {
								showError("Registration Failed", finalMessage);
							}
						});
					}
				} else {
					// Handle HTTP error responses
					String errorMsg = "Registration failed";
					final TextInputLayout emailInputLayout = findViewById(R.id.emailInputLayout);
					
					if (response.code() == 422) {
						// Parse error response body
						try {
							com.google.gson.Gson gson = new com.google.gson.Gson();
							okhttp3.ResponseBody errorBody = response.errorBody();
							if (errorBody != null) {
								String errorJson = errorBody.string();
								RegisterResponse errorResponse = gson.fromJson(errorJson, RegisterResponse.class);
								
								// Check for email errors
								if (errorResponse.getErrors() != null && errorResponse.getErrors().getEmail() != null) {
									String[] emailErrors = errorResponse.getErrors().getEmail();
									if (emailErrors.length > 0) {
										String emailError = emailErrors[0];
										// Normalize error message
										if (emailError.contains("already") || emailError.contains("taken") || emailError.contains("unique")) {
											emailError = "Email already used";
										}
										final String finalEmailError = emailError;
										// Show error in email field
										runOnUiThread(() -> {
											if (emailInputLayout != null) {
												emailInputLayout.setError(finalEmailError);
											}
										});
										errorMsg = finalEmailError;
									}
								} else {
									errorMsg = errorResponse.getMessage() != null ? errorResponse.getMessage() : "Validation error. Please check your input.";
								}
								
								// Format and show other errors
								if (errorResponse.getErrors() != null) {
									String formattedErrors = formatErrors(errorResponse.getErrors());
									if (formattedErrors != null && !formattedErrors.isEmpty()) {
										errorMsg = formattedErrors;
									}
								}
							}
						} catch (Exception e) {
							errorMsg = "Validation error. Please check your input.";
						}
					} else if (response.code() == 409) {
						errorMsg = "Email already exists. Please use a different email.";
						runOnUiThread(() -> {
							if (emailInputLayout != null) {
								emailInputLayout.setError("Email already used");
							}
						});
					} else if (response.code() == 500) {
						errorMsg = "Server error. Please try again later.";
					}
					
					final String finalErrorMsg = errorMsg;
					runOnUiThread(() -> {
						showMessage(finalErrorMsg, true);
						if (response.code() != 422 || (emailInputLayout != null && emailInputLayout.getError() == null)) {
							showError("Registration Failed", finalErrorMsg);
						}
					});
				}
			}

			@Override
			public void onFailure(Call<RegisterResponse> call, Throwable t) {
				setLoadingState(false);

				String errorMsg = "Connection error";
				if (t.getMessage() != null) {
					if (t.getMessage().contains("Failed to connect") || t.getMessage().contains("Unable to resolve host")) {
						errorMsg = "Cannot connect to server. Please check:\n1. Laravel server is running\n2. Correct API URL in ApiClient.java\n3. Network connection";
					} else if (t.getMessage().contains("timeout")) {
						errorMsg = "Connection timeout. Server may be slow or unreachable.";
					} else {
						errorMsg = "Error: " + t.getMessage();
					}
				}
				final String finalErrorMsg = errorMsg;
				runOnUiThread(() -> {
					showMessage(finalErrorMsg, true);
					showError("Connection Error", finalErrorMsg);
				});
			}
		});
	}

	private void setLoadingState(boolean isLoading) {
		final MaterialButton registerButton = findViewById(R.id.registerButton);
		runOnUiThread(() -> {
			if (registerButton != null) {
				registerButton.setEnabled(!isLoading);
				registerButton.setText(isLoading ? getString(R.string.registering) : getString(R.string.register));
			}
			if (progressBar != null) {
				progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
			}
			if (!isLoading) {
				hideMessage();
			}
		});
	}

	private void showVerificationDialog(String email, String name) {
		android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_verification_code, null);
		
		TextInputEditText codeInput = dialogView.findViewById(R.id.codeInput);
		TextInputLayout codeInputLayout = dialogView.findViewById(R.id.codeInputLayout);
		MaterialButton verifyButton = dialogView.findViewById(R.id.verifyButton);
		MaterialButton resendCodeButton = dialogView.findViewById(R.id.resendCodeButton);
		android.widget.TextView messageTextView = dialogView.findViewById(R.id.verificationMessage);
		android.widget.ImageButton closeButton = dialogView.findViewById(R.id.closeButton);
		
		if (messageTextView != null) {
			messageTextView.setText("Enter the 6-digit code sent to\n" + email);
		}

		AlertDialog dialog = new MaterialAlertDialogBuilder(this)
			.setView(dialogView)
			.setCancelable(true)
			.create();
		
		// Handle close button click
		if (closeButton != null) {
			closeButton.setOnClickListener(v -> dialog.dismiss());
		}

		// Clear any previous errors
		if (codeInputLayout != null) {
			codeInputLayout.setError(null);
		}

		// Auto-focus on code input and show keyboard
		if (codeInput != null) {
			codeInput.post(() -> {
				codeInput.requestFocus();
				android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.showSoftInput(codeInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
				}
			});
			
			// Enable verify button when 6 digits are entered
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
					if (s.length() == 6) {
						// Code is complete, can verify
						if (verifyButton != null) {
							verifyButton.setEnabled(true);
						}
					} else {
						if (verifyButton != null) {
							verifyButton.setEnabled(s.length() > 0);
						}
					}
				}
			});
		}

		if (verifyButton != null) {
			verifyButton.setEnabled(false);
			verifyButton.setOnClickListener(v -> {
				String code = codeInput != null ? codeInput.getText().toString().trim() : "";
				if (code.length() != 6) {
					if (codeInputLayout != null) {
						codeInputLayout.setError("Please enter a 6-digit code");
					}
					return;
				}
				if (codeInputLayout != null) {
					codeInputLayout.setError(null);
				}
				verifyButton.setEnabled(false);
				verifyButton.setText("Verifying...");
				verifyEmail(email, code, dialog, verifyButton);
			});
		}

		if (resendCodeButton != null) {
			resendCodeButton.setOnClickListener(v -> {
				resendCodeButton.setEnabled(false);
				resendCodeButton.setText("Sending...");
				resendVerificationCode(email, resendCodeButton);
			});
		}

		dialog.show();
	}

	private void verifyEmail(String email, String code, AlertDialog dialog, MaterialButton verifyButton) {
		ApiService apiService = ApiClient.getApiService();
		VerifyEmailRequest request = new VerifyEmailRequest(email, code);

		Call<VerifyEmailResponse> call = apiService.verifyEmail(request);
		call.enqueue(new Callback<VerifyEmailResponse>() {
			@Override
			public void onResponse(Call<VerifyEmailResponse> call, Response<VerifyEmailResponse> response) {
				runOnUiThread(() -> {
					if (verifyButton != null) {
						verifyButton.setEnabled(true);
						verifyButton.setText("Verify");
					}
				});

				if (response.isSuccessful() && response.body() != null) {
					VerifyEmailResponse verifyResponse = response.body();
					if (verifyResponse.isSuccess() && verifyResponse.getData() != null) {
						// Save token and user data
						tokenManager.saveToken("Bearer " + verifyResponse.getData().getToken());
						tokenManager.saveEmail(verifyResponse.getData().getUser().getEmail());
						tokenManager.saveName(verifyResponse.getData().getUser().getName());

						dialog.dismiss();

						// Navigate to dashboard
						runOnUiThread(() -> {
							showSuccess("Email Verified", 
								"Welcome " + verifyResponse.getData().getUser().getName() + "! Your email has been verified successfully.",
								verifyResponse.getData().getUser().getEmail());
						});
					} else {
						final String errorMsg = verifyResponse.getMessage() != null ? verifyResponse.getMessage() : "Invalid verification code";
						runOnUiThread(() -> {
							showError("Verification Failed", errorMsg);
						});
					}
				} else {
					String errorMsg = "Invalid verification code";
					if (response.code() == 400) {
						errorMsg = "Invalid or expired verification code. Please request a new code.";
					}
					final String finalErrorMsg = errorMsg;
					runOnUiThread(() -> {
						showError("Verification Failed", finalErrorMsg);
					});
				}
			}

			@Override
			public void onFailure(Call<VerifyEmailResponse> call, Throwable t) {
				runOnUiThread(() -> {
					if (verifyButton != null) {
						verifyButton.setEnabled(true);
						verifyButton.setText("Verify");
					}
					showError("Connection Error", "Failed to verify code. Please check your connection and try again.");
				});
			}
		});
	}

	private void resendVerificationCode(String email, MaterialButton resendButton) {
		ApiService apiService = ApiClient.getApiService();
		VerificationRequest request = new VerificationRequest(email);

		Call<VerificationResponse> call = apiService.sendVerificationCode(request);
		call.enqueue(new Callback<VerificationResponse>() {
			@Override
			public void onResponse(Call<VerificationResponse> call, Response<VerificationResponse> response) {
				runOnUiThread(() -> {
					if (resendButton != null) {
						resendButton.setEnabled(true);
						resendButton.setText("Resend Code");
					}
				});

				if (response.isSuccessful() && response.body() != null) {
					VerificationResponse verifyResponse = response.body();
					if (verifyResponse.isSuccess()) {
						runOnUiThread(() -> {
							Toast.makeText(RegisterActivity.this, 
								verifyResponse.getMessage() != null ? verifyResponse.getMessage() : "Verification code sent to your email", 
								Toast.LENGTH_LONG).show();
						});
					} else {
						final String errorMsg = verifyResponse.getMessage() != null ? verifyResponse.getMessage() : "Failed to send code";
						runOnUiThread(() -> {
							showError("Error", errorMsg);
						});
					}
				} else {
					runOnUiThread(() -> {
						showError("Error", "Failed to send verification code. Please try again.");
					});
				}
			}

			@Override
			public void onFailure(Call<VerificationResponse> call, Throwable t) {
				runOnUiThread(() -> {
					if (resendButton != null) {
						resendButton.setEnabled(true);
						resendButton.setText("Resend Code");
					}
					String errorMsg = "Connection error";
					if (t.getMessage() != null && t.getMessage().contains("Failed to connect")) {
						errorMsg = "Cannot connect to server. Please check your connection.";
					}
					final String finalErrorMsg = errorMsg;
					showError("Connection Error", finalErrorMsg);
				});
			}
		});
	}

	private String formatErrors(RegisterResponse.Errors errors) {
		StringBuilder errorMessage = new StringBuilder();
		
		if (errors.getEmail() != null && errors.getEmail().length > 0) {
			errorMessage.append("Email: ").append(errors.getEmail()[0]).append("\n");
		}
		if (errors.getPassword() != null && errors.getPassword().length > 0) {
			errorMessage.append("Password: ").append(errors.getPassword()[0]).append("\n");
		}
		if (errors.getName() != null && errors.getName().length > 0) {
			errorMessage.append("Name: ").append(errors.getName()[0]).append("\n");
		}
		
		return errorMessage.length() > 0 ? errorMessage.toString().trim() : "Please check your input";
	}

	private void showError(String title, String message) {
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton("OK", null)
			.show();
	}

	private void showSuccess(String title, String message, String email) {
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton("OK", (dialog, which) -> {
				// Navigate to dashboard
				Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
				intent.putExtra(DashboardActivity.EXTRA_EMAIL, email);
				startActivity(intent);
				finish();
			})
			.setCancelable(false)
			.show();
	}
}
