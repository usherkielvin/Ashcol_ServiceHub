package hans.ph;

import hans.ph.R;
import hans.ph.api.ApiClient;
import hans.ph.api.ApiService;
import hans.ph.api.LoginRequest;
import hans.ph.api.LoginResponse;
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

public class MainActivity extends AppCompatActivity {

	private TokenManager tokenManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tokenManager = new TokenManager(this);

		// Check if already logged in
		if (tokenManager.isLoggedIn()) {
			Intent intent = new Intent(this, DashboardActivity.class);
			intent.putExtra(DashboardActivity.EXTRA_EMAIL, tokenManager.getEmail());
			startActivity(intent);
			finish();
			return;
		}

		TextInputEditText emailInput = findViewById(R.id.emailInput);
		TextInputEditText passwordInput = findViewById(R.id.passwordInput);
		MaterialButton loginButton = findViewById(R.id.loginButton);
		MaterialButton registerButton = findViewById(R.id.registerButton);

		if (registerButton != null) {
			registerButton.setOnClickListener(v -> {
				Intent intent = new Intent(this, RegisterActivity.class);
				startActivity(intent);
			});
		}

		if (loginButton != null) {
			loginButton.setOnClickListener(v -> {
				String email = emailInput != null ? emailInput.getText().toString().trim() : "";
				String password = passwordInput != null ? passwordInput.getText().toString() : "";

				if (email.isEmpty() || password.isEmpty()) {
					Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
					return;
				}

				login(email, password);
			});
		}
	}

	private void login(String email, String password) {
		final MaterialButton loginButton = findViewById(R.id.loginButton);
		if (loginButton != null) {
			loginButton.setEnabled(false);
			loginButton.setText("Logging in...");
		}

		ApiService apiService = ApiClient.getApiService();
		LoginRequest request = new LoginRequest(email, password);

		Call<LoginResponse> call = apiService.login(request);
		call.enqueue(new Callback<LoginResponse>() {
			@Override
			public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
				runOnUiThread(() -> {
					if (loginButton != null) {
						loginButton.setEnabled(true);
						loginButton.setText("Login");
					}
				});

				if (response.isSuccessful() && response.body() != null) {
					LoginResponse loginResponse = response.body();
					if (loginResponse.isSuccess() && loginResponse.getData() != null) {
						// Save token and user data
						tokenManager.saveToken("Bearer " + loginResponse.getData().getToken());
						tokenManager.saveEmail(loginResponse.getData().getUser().getEmail());
						tokenManager.saveName(loginResponse.getData().getUser().getName());

						// Navigate to dashboard
						runOnUiThread(() -> {
							Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
							intent.putExtra(DashboardActivity.EXTRA_EMAIL, loginResponse.getData().getUser().getEmail());
							startActivity(intent);
							finish();
						});
					} else {
						final String message = loginResponse.getMessage() != null ? loginResponse.getMessage() : "Login failed";
						runOnUiThread(() -> showError("Login Failed", message));
					}
				} else {
					String errorMsg;
					if (response.code() == 401) {
						errorMsg = "Invalid email or password";
					} else if (response.code() == 500) {
						errorMsg = "Server error. Please try again later.";
					} else {
						errorMsg = "Invalid credentials";
					}
					final String finalErrorMsg = errorMsg;
					runOnUiThread(() -> showError("Login Failed", finalErrorMsg));
				}
			}

			@Override
			public void onFailure(Call<LoginResponse> call, Throwable t) {
				runOnUiThread(() -> {
					if (loginButton != null) {
						loginButton.setEnabled(true);
						loginButton.setText("Login");
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

	private void showError(String title, String message) {
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton("OK", null)
			.show();
	}
}
