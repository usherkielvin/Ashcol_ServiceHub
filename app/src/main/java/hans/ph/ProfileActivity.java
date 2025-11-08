package hans.ph;

import hans.ph.R;
import hans.ph.api.ApiClient;
import hans.ph.api.ApiService;
import hans.ph.api.UserResponse;
import hans.ph.util.TokenManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

	public static final String EXTRA_EMAIL = "email";
	private TokenManager tokenManager;
	private String currentName;
	private String currentEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		tokenManager = new TokenManager(this);

		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
		}

		// Get email from intent or token manager
		String email = getIntent().getStringExtra(EXTRA_EMAIL);
		if (email == null) {
			email = tokenManager.getEmail();
		}

		TextView nameTextView = findViewById(R.id.nameTextView);
		TextView emailTextView = findViewById(R.id.emailTextView);
		
		if (nameTextView != null) {
			String name = tokenManager.getName();
			nameTextView.setText(name != null ? name : "Loading...");
		}
		
		if (emailTextView != null) {
			emailTextView.setText(email != null ? email : "Loading...");
		}

		// Fetch user data from API
		fetchUserData();

		MaterialButton editNameButton = findViewById(R.id.editNameButton);
		if (editNameButton != null) {
			editNameButton.setOnClickListener(v -> showEditNameDialog());
		}

		MaterialButton changePasswordButton = findViewById(R.id.changePasswordButton);
		if (changePasswordButton != null) {
			changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
		}

		MaterialButton logoutButton = findViewById(R.id.logoutButton);
		if (logoutButton != null) {
			logoutButton.setOnClickListener(v -> logout());
		}
	}

	private void fetchUserData() {
		String token = tokenManager.getToken();
		if (token == null) {
			return;
		}

		ApiService apiService = ApiClient.getApiService();
		Call<UserResponse> call = apiService.getUser(token);
		call.enqueue(new Callback<UserResponse>() {
			@Override
			public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
				if (response.isSuccessful() && response.body() != null) {
					UserResponse userResponse = response.body();
					if (userResponse.isSuccess() && userResponse.getData() != null) {
						currentName = userResponse.getData().getName();
						currentEmail = userResponse.getData().getEmail();
						
						TextView nameTextView = findViewById(R.id.nameTextView);
						TextView emailTextView = findViewById(R.id.emailTextView);
						
						if (nameTextView != null && currentName != null) {
							nameTextView.setText(currentName);
						}
						
						if (emailTextView != null && currentEmail != null) {
							emailTextView.setText(currentEmail);
						}
						
						// Update token manager with latest data
						tokenManager.saveName(currentName);
						tokenManager.saveEmail(currentEmail);
					}
				}
			}

			@Override
			public void onFailure(Call<UserResponse> call, Throwable t) {
				// Silently fail - use cached data
			}
		});
	}

	private void showEditNameDialog() {
		android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_name, null);
		
		TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
		TextInputLayout nameInputLayout = dialogView.findViewById(R.id.nameInputLayout);
		
		if (nameInput != null && currentName != null) {
			nameInput.setText(currentName);
			nameInput.selectAll();
		}

		AlertDialog dialog = new MaterialAlertDialogBuilder(this)
			.setTitle(getString(R.string.edit_name))
			.setView(dialogView)
			.setPositiveButton(getString(R.string.save), null)
			.setNegativeButton(getString(R.string.cancel), null)
			.create();

		dialog.setOnShowListener(d -> {
			android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(v -> {
				String newName = nameInput != null ? nameInput.getText().toString().trim() : "";
				if (newName.isEmpty()) {
					if (nameInputLayout != null) {
						nameInputLayout.setError("Name cannot be empty");
					}
				} else {
					updateName(newName);
					dialog.dismiss();
				}
			});
		});

		dialog.show();
		
		// Auto-focus and show keyboard
		if (nameInput != null) {
			nameInput.post(() -> {
				nameInput.requestFocus();
				android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.showSoftInput(nameInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
				}
			});
		}
	}

	private void showChangePasswordDialog() {
		android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
		
		TextInputEditText currentPasswordInput = dialogView.findViewById(R.id.currentPasswordInput);
		TextInputEditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
		TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);
		TextInputLayout currentPasswordLayout = dialogView.findViewById(R.id.currentPasswordLayout);
		TextInputLayout newPasswordLayout = dialogView.findViewById(R.id.newPasswordLayout);
		TextInputLayout confirmPasswordLayout = dialogView.findViewById(R.id.confirmPasswordLayout);

		AlertDialog dialog = new MaterialAlertDialogBuilder(this)
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
				
				// Clear previous errors
				if (currentPasswordLayout != null) {
					currentPasswordLayout.setError(null);
				}
				if (newPasswordLayout != null) {
					newPasswordLayout.setError(null);
				}
				if (confirmPasswordLayout != null) {
					confirmPasswordLayout.setError(null);
				}
				
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
				
				if (isValid) {
					changePassword(currentPassword, newPassword);
					dialog.dismiss();
				}
			});
		});

		dialog.show();
	}

	private void updateName(String newName) {
		// TODO: Implement API call to update name
		// For now, just update locally
		currentName = newName;
		TextView nameTextView = findViewById(R.id.nameTextView);
		if (nameTextView != null) {
			nameTextView.setText(newName);
		}
		tokenManager.saveName(newName);
		Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show();
	}

	private void changePassword(String currentPassword, String newPassword) {
		// TODO: Implement API call to change password
		Toast.makeText(this, "Password change functionality will be implemented soon", Toast.LENGTH_SHORT).show();
	}

	private void logout() {
		String token = tokenManager.getToken();
		if (token != null) {
			ApiService apiService = ApiClient.getApiService();
			Call<hans.ph.api.LogoutResponse> call = apiService.logout(token);
			call.enqueue(new Callback<hans.ph.api.LogoutResponse>() {
				@Override
				public void onResponse(Call<hans.ph.api.LogoutResponse> call, Response<hans.ph.api.LogoutResponse> response) {
					// Clear token regardless of API response
					tokenManager.clear();
					navigateToLogin();
				}

				@Override
				public void onFailure(Call<hans.ph.api.LogoutResponse> call, Throwable t) {
					// Clear token even if API call fails
					tokenManager.clear();
					navigateToLogin();
				}
			});
		} else {
			tokenManager.clear();
			navigateToLogin();
		}
	}

	private void navigateToLogin() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		finish();
	}
}
