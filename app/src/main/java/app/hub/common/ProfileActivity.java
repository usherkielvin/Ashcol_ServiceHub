package app.hub.common;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
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

		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			// Enable back button
			if (getSupportActionBar() != null) {
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				getSupportActionBar().setDisplayShowHomeEnabled(true);
			}
			toolbar.setNavigationOnClickListener(v -> finish());
		}

		// Get email from intent or token manager
		String email = getIntent().getStringExtra(EXTRA_EMAIL);
		if (email == null || email.trim().isEmpty()) {
			email = tokenManager.getEmail();
		}

		TextView nameTextView = findViewById(R.id.nameTextView);
		TextView emailTextView = findViewById(R.id.emailTextView);
		
		if (nameTextView != null) {
			String name = tokenManager.getName();
			if (name != null && !name.trim().isEmpty()) {
				nameTextView.setText(name.trim());
			} else {
				nameTextView.setText("Loading...");
			}
		}
		
		if (emailTextView != null) {
			if (email != null && !email.trim().isEmpty()) {
				emailTextView.setText(email.trim());
			} else {
				emailTextView.setText("Loading...");
			}
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
			Log.e("ProfileActivity", "No token available, using cached data only");
			fallbackToCachedData();
			return;
		}

		// Log cached data before API call
		String cachedName = tokenManager.getName();
		String cachedEmail = tokenManager.getEmail();
		Log.d("ProfileActivity", "Cached data before API call - Name: " + cachedName + ", Email: " + cachedEmail);

		ApiService apiService = ApiClient.getApiService();
		Call<UserResponse> call = apiService.getUser(token);
		call.enqueue(new Callback<UserResponse>() {
			@Override
			public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
				if (response.isSuccessful() && response.body() != null) {
					UserResponse userResponse = response.body();
					Log.d("ProfileActivity", "API Response - Success: " + userResponse.isSuccess());
					
					if (userResponse.isSuccess() && userResponse.getData() != null) {
						UserResponse.Data userData = userResponse.getData();
						
						// Log the raw data from API
						Log.d("ProfileActivity", "API Data - Name: " + userData.getName() + 
							", FirstName: " + userData.getFirstName() + 
							", LastName: " + userData.getLastName() + 
							", Email: " + userData.getEmail());
						
						// Get name - prefer name field, fallback to firstName + lastName
						currentName = userData.getName();
						if (currentName == null || currentName.trim().isEmpty()) {
							String firstName = userData.getFirstName();
							String lastName = userData.getLastName();
							
							Log.d("ProfileActivity", "Name field is empty, combining firstName and lastName");
							
							// Build name from firstName and lastName
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
							
							currentName = nameBuilder.toString();
							Log.d("ProfileActivity", "Combined name: " + currentName);
						} else {
							currentName = currentName.trim();
						}
						
						currentEmail = userData.getEmail();
						if (currentEmail != null) {
							currentEmail = currentEmail.trim();
						}
						
						Log.d("ProfileActivity", "Final values - Name: " + currentName + ", Email: " + currentEmail);
						
						// If API returned empty data, fallback to cached data
						if ((currentName == null || currentName.isEmpty()) && 
							(currentEmail == null || currentEmail.isEmpty())) {
							Log.w("ProfileActivity", "API returned empty data, falling back to cached data");
							fallbackToCachedData();
							return;
						}
						
						// If name is empty but email exists, try to get name from cache
						if ((currentName == null || currentName.isEmpty()) && 
							(currentEmail != null && !currentEmail.isEmpty())) {
							String cachedName = tokenManager.getName();
							if (cachedName != null && !cachedName.trim().isEmpty()) {
								currentName = cachedName.trim();
								Log.d("ProfileActivity", "Using cached name: " + currentName);
							}
						}
						
						// If email is empty but name exists, try to get email from cache
						if ((currentEmail == null || currentEmail.isEmpty()) && 
							(currentName != null && !currentName.isEmpty())) {
							String cachedEmail = tokenManager.getEmail();
							if (cachedEmail != null && !cachedEmail.trim().isEmpty()) {
								currentEmail = cachedEmail.trim();
								Log.d("ProfileActivity", "Using cached email: " + currentEmail);
							}
						}
						
						// Update UI on main thread
						runOnUiThread(() -> {
							TextView nameTextView = findViewById(R.id.nameTextView);
							TextView emailTextView = findViewById(R.id.emailTextView);
							
							if (nameTextView != null) {
								if (currentName != null && !currentName.isEmpty()) {
									nameTextView.setText(currentName);
								} else {
									// Try cached data one more time
									String cachedName = tokenManager.getName();
									if (cachedName != null && !cachedName.trim().isEmpty()) {
										nameTextView.setText(cachedName.trim());
										currentName = cachedName.trim();
									} else {
										nameTextView.setText("No name available");
									}
								}
							}
							
							if (emailTextView != null) {
								if (currentEmail != null && !currentEmail.isEmpty()) {
									emailTextView.setText(currentEmail);
								} else {
									// Try cached data one more time
									String cachedEmail = tokenManager.getEmail();
									if (cachedEmail != null && !cachedEmail.trim().isEmpty()) {
										emailTextView.setText(cachedEmail.trim());
										currentEmail = cachedEmail.trim();
									} else {
										emailTextView.setText("No email available");
									}
								}
							}
						});
						
						// Update token manager with latest data
						if (currentName != null && !currentName.isEmpty()) {
							tokenManager.saveName(currentName);
						}
						if (currentEmail != null && !currentEmail.isEmpty()) {
							tokenManager.saveEmail(currentEmail);
						}
					} else {
						// Handle case where response is not successful - fallback to cached data
						Log.e("ProfileActivity", "API response not successful or data is null");
						fallbackToCachedData();
					}
				} else {
					// Handle error response - fallback to cached data
					Log.e("ProfileActivity", "API call not successful. Code: " + response.code());
					if (response.errorBody() != null) {
						try {
							Log.e("ProfileActivity", "Error body: " + response.errorBody().string());
						} catch (java.io.IOException e) {
							Log.e("ProfileActivity", "Error reading error body", e);
						}
					}
					fallbackToCachedData();
				}
			}

			@Override
			public void onFailure(Call<UserResponse> call, Throwable t) {
				// Show error but fallback to cached data
				Log.e("ProfileActivity", "API call failed: " + t.getMessage());
				fallbackToCachedData();
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
			Call<LogoutResponse> call = apiService.logout(token);
			call.enqueue(new Callback<LogoutResponse>() {
				@Override
				public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {
					// Clear token regardless of API response
					tokenManager.clear();
					navigateToLogin();
				}

				@Override
				public void onFailure(Call<LogoutResponse> call, Throwable t) {
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

	private String trim(String str) {
		return str != null ? str.trim() : "";
	}

	private void fallbackToCachedData() {
		// Fallback to cached data from TokenManager if API fails
		runOnUiThread(() -> {
			TextView nameTextView = findViewById(R.id.nameTextView);
			TextView emailTextView = findViewById(R.id.emailTextView);
			
			// Get cached data
			String cachedName = tokenManager.getName();
			String cachedEmail = tokenManager.getEmail();
			
			Log.d("ProfileActivity", "Using cached data - Name: " + cachedName + ", Email: " + cachedEmail);
			
			if (nameTextView != null) {
				if (cachedName != null && !cachedName.trim().isEmpty()) {
					nameTextView.setText(cachedName.trim());
					currentName = cachedName.trim();
				} else {
					nameTextView.setText("No name available");
				}
			}
			
			if (emailTextView != null) {
				if (cachedEmail != null && !cachedEmail.trim().isEmpty()) {
					emailTextView.setText(cachedEmail.trim());
					currentEmail = cachedEmail.trim();
				} else {
					emailTextView.setText("No email available");
				}
			}
			
			// Show a subtle message that we're using cached data
			if (cachedName == null || cachedName.trim().isEmpty() || 
				cachedEmail == null || cachedEmail.trim().isEmpty()) {
				Toast.makeText(ProfileActivity.this, "Using cached data. Please check your connection.", Toast.LENGTH_SHORT).show();
			}
		});
	}
}
