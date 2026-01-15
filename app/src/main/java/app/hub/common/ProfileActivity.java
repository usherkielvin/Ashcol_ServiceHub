package app.hub.common;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.ChangePasswordRequest;
import app.hub.api.ChangePasswordResponse;
import app.hub.api.LogoutResponse;
import app.hub.api.UserResponse;
import app.hub.util.TokenManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
		setupToolbar();
		loadCachedData();
		fetchUserData();
		setupButtons();
	}

	private void setupToolbar() {
		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			if (getSupportActionBar() != null) {
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				getSupportActionBar().setDisplayShowHomeEnabled(true);
			}
			toolbar.setNavigationOnClickListener(v -> finish());
		}
	}

	private void setupButtons() {
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

	private void loadCachedData() {
		TextView nameTextView = findViewById(R.id.nameTextView);
		TextView emailTextView = findViewById(R.id.emailTextView);
		
		if (nameTextView != null) {
			String name = getCachedName();
			if (isValidName(name)) {
				nameTextView.setText(name);
				currentName = name;
			} else {
				nameTextView.setText("Name not set");
			}
		}
		
		if (emailTextView != null) {
			String email = getCachedEmail();
			if (isValidEmail(email)) {
				emailTextView.setText(email);
				currentEmail = email;
			} else {
				emailTextView.setText("Email not set");
			}
		}
	}

	private void fetchUserData() {
		String token = tokenManager.getToken();
		if (token == null) {
			return;
		}

		final String cachedName = getCachedName();
		final String cachedEmail = getCachedEmail();

		ApiService apiService = ApiClient.getApiService();
		Call<UserResponse> call = apiService.getUser(token);
		call.enqueue(new Callback<UserResponse>() {
			@Override
			public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
				if (response.isSuccessful() && response.body() != null) {
					UserResponse userResponse = response.body();
					
					if (userResponse.isSuccess() && userResponse.getData() != null) {
						UserResponse.Data userData = response.body().getData();
						String apiName = buildNameFromApi(userData);
						String emailToDisplay = getEmailToDisplay(userData, cachedEmail);
						
						updateCache(apiName, emailToDisplay, cachedName, cachedEmail);
						updateDisplay(isValidName(apiName) ? apiName : cachedName, emailToDisplay);
					} else {
						updateDisplay(cachedName, cachedEmail);
					}
				} else {
					updateDisplay(cachedName, cachedEmail);
				}
			}

			@Override
			public void onFailure(Call<UserResponse> call, Throwable t) {
				Log.e("ProfileActivity", "API call failed: " + t.getMessage());
				updateDisplay(cachedName, cachedEmail);
			}
		});
	}

	private String buildNameFromApi(UserResponse.Data userData) {
		String name = userData.getName();
		if (isValidName(name)) {
			return name.trim();
		}
		
		String firstName = userData.getFirstName();
		String lastName = userData.getLastName();
		
		if (isValidName(firstName) || isValidName(lastName)) {
			StringBuilder builder = new StringBuilder();
			if (isValidName(firstName)) {
				builder.append(firstName.trim());
			}
			if (isValidName(lastName)) {
				if (builder.length() > 0) {
					builder.append(" ");
				}
				builder.append(lastName.trim());
			}
			if (builder.length() > 0) {
				return builder.toString();
			}
		}
		
		return null;
	}

	private String getEmailToDisplay(UserResponse.Data userData, String cachedEmail) {
		if (isValidEmail(cachedEmail)) {
			return cachedEmail;
		}

		String apiEmail = userData.getEmail();
		String apiUsername = userData.getUsername();
		String apiFirstName = userData.getFirstName();
		
		if (isValidApiEmail(apiEmail, apiUsername, apiFirstName)) {
			return apiEmail.trim();
		}
		
		return cachedEmail;
	}

	private boolean isValidApiEmail(String email, String username, String firstName) {
		if (email == null || email.trim().isEmpty()) {
			return false;
		}
		String trimmed = email.trim();
		return trimmed.contains("@") 
			&& !trimmed.equals("null")
			&& !trimmed.equals(username)
			&& !trimmed.equals(firstName)
			&& trimmed.length() > 3;
	}

	private void updateCache(String apiName, String emailToDisplay, String cachedName, String cachedEmail) {
		if (isValidName(apiName)) {
			tokenManager.saveName(apiName);
			currentName = apiName;
		} else if (isValidName(cachedName)) {
			currentName = cachedName;
		}

		if (isValidEmail(emailToDisplay) && !emailToDisplay.equals(cachedEmail)) {
			tokenManager.saveEmail(emailToDisplay);
			currentEmail = emailToDisplay;
		} else if (isValidEmail(cachedEmail)) {
			currentEmail = cachedEmail;
		}
	}

	private void updateDisplay(String name, String email) {
		runOnUiThread(() -> {
			TextView nameTextView = findViewById(R.id.nameTextView);
			TextView emailTextView = findViewById(R.id.emailTextView);
			
			if (nameTextView != null) {
				String displayName = isValidName(name) ? name : getCachedName();
				if (isValidName(displayName)) {
					nameTextView.setText(displayName);
					currentName = displayName;
				} else {
					nameTextView.setText("Name not set");
				}
			}
			
			if (emailTextView != null) {
				String displayEmail = getCachedEmail();
				if (!isValidEmail(displayEmail) && isValidEmail(email)) {
					displayEmail = email;
				}
				
				if (isValidEmail(displayEmail)) {
					emailTextView.setText(displayEmail);
					currentEmail = displayEmail;
				} else {
					emailTextView.setText("Email not set");
				}
			}
		});
	}

	private boolean isValidName(String name) {
		return name != null 
			&& !name.trim().isEmpty() 
			&& !name.trim().equals("null") 
			&& !name.trim().contains("null");
	}

	private boolean isValidEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			return false;
		}
		String trimmed = email.trim();
		return trimmed.contains("@") 
			&& trimmed.length() > 3 
			&& !trimmed.equals("null");
	}

	private String getCachedName() {
		try {
			String name = tokenManager.getName();
			return isValidName(name) ? name.trim() : null;
		} catch (Exception e) {
			Log.e("ProfileActivity", "Error getting cached name", e);
			return null;
		}
	}

	private String getCachedEmail() {
		try {
			String email = tokenManager.getEmail();
			if (isValidEmail(email)) {
				return email.trim();
			}
			return null;
		} catch (Exception e) {
			Log.e("ProfileActivity", "Error getting cached email", e);
			return null;
		}
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
		
		if (nameInput != null) {
			nameInput.post(() -> {
				nameInput.requestFocus();
				android.view.inputmethod.InputMethodManager imm = 
					(android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
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
				
				if (currentPasswordLayout != null) currentPasswordLayout.setError(null);
				if (newPasswordLayout != null) newPasswordLayout.setError(null);
				if (confirmPasswordLayout != null) confirmPasswordLayout.setError(null);
				
				if (validatePasswordInputs(currentPassword, newPassword, confirmPassword, 
						currentPasswordLayout, newPasswordLayout, confirmPasswordLayout)) {
					changePassword(currentPassword, newPassword);
					dialog.dismiss();
				}
			});
		});

		dialog.show();
	}

	private boolean validatePasswordInputs(String currentPassword, String newPassword, String confirmPassword,
			TextInputLayout currentPasswordLayout, TextInputLayout newPasswordLayout, TextInputLayout confirmPasswordLayout) {
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

	private void updateName(String newName) {
		currentName = newName;
		TextView nameTextView = findViewById(R.id.nameTextView);
		if (nameTextView != null) {
			nameTextView.setText(newName);
		}
		tokenManager.saveName(newName);
		Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show();
	}

	private void changePassword(String currentPassword, String newPassword) {
		String token = tokenManager.getToken();
		if (token == null) {
			Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_SHORT).show();
			return;
		}

		ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword, newPassword);
		ApiService apiService = ApiClient.getApiService();
		Call<ChangePasswordResponse> call = apiService.changePassword(token, request);
		
		call.enqueue(new Callback<ChangePasswordResponse>() {
			@Override
			public void onResponse(Call<ChangePasswordResponse> call, Response<ChangePasswordResponse> response) {
				if (response.isSuccessful() && response.body() != null) {
					ChangePasswordResponse changePasswordResponse = response.body();
					if (changePasswordResponse.isSuccess()) {
						Toast.makeText(ProfileActivity.this, 
							changePasswordResponse.getMessage() != null ? 
								changePasswordResponse.getMessage() : "Password changed successfully", 
							Toast.LENGTH_SHORT).show();
					} else {
						String errorMessage = changePasswordResponse.getMessage();
						if (errorMessage == null || errorMessage.isEmpty()) {
							errorMessage = "Failed to change password";
						}
						Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
					}
				} else {
					if (response.code() == 400 || response.code() == 422) {
						try {
							ChangePasswordResponse errorResponse = response.body();
							if (errorResponse != null && errorResponse.getErrors() != null) {
								StringBuilder errorMsg = new StringBuilder();
								ChangePasswordResponse.Errors errors = errorResponse.getErrors();
								
								if (errors.getCurrent_password() != null && errors.getCurrent_password().length > 0) {
									errorMsg.append(errors.getCurrent_password()[0]).append("\n");
								}
								if (errors.getNew_password() != null && errors.getNew_password().length > 0) {
									errorMsg.append(errors.getNew_password()[0]).append("\n");
								}
								if (errors.getNew_password_confirmation() != null && errors.getNew_password_confirmation().length > 0) {
									errorMsg.append(errors.getNew_password_confirmation()[0]);
								}
								
								if (errorMsg.length() > 0) {
									Toast.makeText(ProfileActivity.this, errorMsg.toString().trim(), Toast.LENGTH_LONG).show();
								} else {
									Toast.makeText(ProfileActivity.this, 
										errorResponse.getMessage() != null ? errorResponse.getMessage() : "Invalid input", 
										Toast.LENGTH_LONG).show();
								}
							} else {
								Toast.makeText(ProfileActivity.this, "Invalid input. Please check your passwords.", Toast.LENGTH_LONG).show();
							}
						} catch (Exception e) {
							Toast.makeText(ProfileActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(ProfileActivity.this, "Failed to change password. Please try again.", Toast.LENGTH_SHORT).show();
					}
				}
			}

			@Override
			public void onFailure(Call<ChangePasswordResponse> call, Throwable t) {
				Log.e("ProfileActivity", "Change password failed: " + t.getMessage());
				Toast.makeText(ProfileActivity.this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void logout() {
		String token = tokenManager.getToken();
		if (token != null) {
			ApiService apiService = ApiClient.getApiService();
			Call<LogoutResponse> call = apiService.logout(token);
			call.enqueue(new Callback<LogoutResponse>() {
				@Override
				public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {
					tokenManager.clear();
					navigateToLogin();
				}

				@Override
				public void onFailure(Call<LogoutResponse> call, Throwable t) {
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
