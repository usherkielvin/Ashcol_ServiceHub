package hans.ph;

import hans.ph.R;
import hans.ph.api.ApiClient;
import hans.ph.api.ApiService;
import hans.ph.api.RegisterRequest;
import hans.ph.api.RegisterResponse;
import hans.ph.util.TokenManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

	private TokenManager tokenManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		tokenManager = new TokenManager(this);

		TextInputEditText nameInput = findViewById(R.id.nameInput);
		TextInputEditText emailInput = findViewById(R.id.emailInput);
		TextInputEditText passwordInput = findViewById(R.id.passwordInput);
		TextInputEditText confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
		MaterialButton registerButton = findViewById(R.id.registerButton);
		MaterialButton backToLoginButton = findViewById(R.id.backToLoginButton);

		if (registerButton != null) {
			registerButton.setOnClickListener(v -> {
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
		if (name.isEmpty()) {
			showError("Validation Error", "Please enter your name");
			return false;
		}

		if (email.isEmpty()) {
			showError("Validation Error", "Please enter your email");
			return false;
		}

		if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			showError("Validation Error", "Please enter a valid email address");
			return false;
		}

		if (password.isEmpty()) {
			showError("Validation Error", "Please enter a password");
			return false;
		}

		if (password.length() < 8) {
			showError("Validation Error", "Password must be at least 8 characters");
			return false;
		}

		if (confirmPassword.isEmpty()) {
			showError("Validation Error", "Please confirm your password");
			return false;
		}

		if (!password.equals(confirmPassword)) {
			showError("Validation Error", "Passwords do not match");
			return false;
		}

		return true;
	}

	private void register(String name, String email, String password, String confirmPassword) {
		final MaterialButton registerButton = findViewById(R.id.registerButton);
		if (registerButton != null) {
			registerButton.setEnabled(false);
			registerButton.setText("Registering...");
		}

		ApiService apiService = ApiClient.getApiService();
		RegisterRequest request = new RegisterRequest(name, email, password, confirmPassword);

		Call<RegisterResponse> call = apiService.register(request);
		call.enqueue(new Callback<RegisterResponse>() {
			@Override
			public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
				runOnUiThread(() -> {
					if (registerButton != null) {
						registerButton.setEnabled(true);
						registerButton.setText(getString(R.string.register));
					}
				});

				if (response.isSuccessful() && response.body() != null) {
					RegisterResponse registerResponse = response.body();
					if (registerResponse.isSuccess() && registerResponse.getData() != null) {
						// Save token and user data
						tokenManager.saveToken("Bearer " + registerResponse.getData().getToken());
						tokenManager.saveEmail(registerResponse.getData().getUser().getEmail());
						tokenManager.saveName(registerResponse.getData().getUser().getName());

						// Show success message and navigate to dashboard
						runOnUiThread(() -> {
							showSuccess("Registration Successful", 
								"Welcome " + registerResponse.getData().getUser().getName() + "! You have been registered successfully.",
								registerResponse.getData().getUser().getEmail());
						});
					} else {
						// Handle validation errors from backend
						String errorMessage = registerResponse.getMessage();
						if (registerResponse.getErrors() != null) {
							errorMessage = formatErrors(registerResponse.getErrors());
						}
						final String finalMessage = errorMessage != null ? errorMessage : "Registration failed";
						runOnUiThread(() -> showError("Registration Failed", finalMessage));
					}
				} else {
					// Handle HTTP error responses
					String errorMsg = "Registration failed";
					if (response.code() == 422) {
						errorMsg = "Validation error. Please check your input.";
					} else if (response.code() == 409) {
						errorMsg = "Email already exists. Please use a different email.";
					} else if (response.code() == 500) {
						errorMsg = "Server error. Please try again later.";
					}
					final String finalErrorMsg = errorMsg;
					runOnUiThread(() -> showError("Registration Failed", finalErrorMsg));
				}
			}

			@Override
			public void onFailure(Call<RegisterResponse> call, Throwable t) {
				runOnUiThread(() -> {
					if (registerButton != null) {
						registerButton.setEnabled(true);
						registerButton.setText(getString(R.string.register));
					}
				});

				String errorMsg = "Connection error";
				if (t.getMessage() != null) {
					if (t.getMessage().contains("Failed to connect") || t.getMessage().contains("Unable to resolve host")) {
						errorMsg = "Cannot connect to server.\n\nPlease check:\n1. Laravel server is running\n2. Correct API URL in ApiClient.java\n3. Network connection";
					} else if (t.getMessage().contains("timeout")) {
						errorMsg = "Connection timeout. Server may be slow or unreachable.";
					} else {
						errorMsg = "Error: " + t.getMessage();
					}
				}
				final String finalErrorMsg = errorMsg;
				runOnUiThread(() -> showError("Connection Error", finalErrorMsg));
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
