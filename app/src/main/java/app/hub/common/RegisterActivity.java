package app.hub.common;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.FacebookSignInRequest;
import app.hub.api.FacebookSignInResponse;
import app.hub.api.GoogleSignInRequest;
import app.hub.api.GoogleSignInResponse;
import app.hub.api.SetInitialPasswordRequest;
import app.hub.api.SetInitialPasswordResponse;
import app.hub.api.VerificationRequest;
import app.hub.api.VerificationResponse;
import app.hub.api.VerifyEmailRequest;
import app.hub.api.VerifyEmailResponse;
import app.hub.user.DashboardActivity;
import app.hub.util.TokenManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Activity for user registration - Multi-step flow container
public class RegisterActivity extends AppCompatActivity {

	private static final String TAG = "RegisterActivity";
	private TokenManager tokenManager;
	private FragmentManager fragmentManager;

	// Registration data to pass between steps
	private String userEmail;
	private String userFirstName;
	private String userLastName;
	private String userName;
	private String userPhone;
	private String userPassword;
	
	// Track if user signed in with Google (skip OTP for Google users)
	private boolean isGoogleSignIn = false;

	// Views for activity_register.xml (Tell us step)
	private View fragmentContainer;
	private ConstraintLayout templateLayout;
	private TextInputEditText firstNameInput, lastNameInput, usernameInput, phoneInput;
	private TextView fval, lval, uval, phoneVal;
	private MaterialButton registerButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		try {
			tokenManager = new TokenManager(this);
			fragmentManager = getSupportFragmentManager();

			// Setup back press handling using OnBackPressedDispatcher
			setupBackPressHandler();

			// Start with Step 1: Create New Account welcome screen
			if (savedInstanceState == null) {
				showCreateNewAccountFragment();
			}
		} catch (Exception e) {
			Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
			Toast.makeText(this, "Error loading registration", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	// Setup modern back press handling
	private void setupBackPressHandler() {
		OnBackPressedCallback callback = new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				// If template layout is visible (Tell Us step), go back to email fragment
				if (templateLayout != null && templateLayout.getVisibility() == View.VISIBLE) {
					showEmailFragment();
					return;
				}

				// Otherwise handle fragment back stack
				if (fragmentManager.getBackStackEntryCount() > 0) {
					fragmentManager.popBackStack();
				} else {
					// If at first fragment, go back to login
					Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				}
			}
		};
		getOnBackPressedDispatcher().addCallback(this, callback);
	}

	// Step 1: Show create new account welcome fragment
	public void showCreateNewAccountFragment() {
		try {
			Log.d(TAG, "Showing CreateNewAccountFragment");
			// Hide template layout, show fragment container
			hideTemplateLayout();
			Fragment fragment = new CreateNewAccountFragment();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.replace(R.id.fragment_container, fragment);
			transaction.commit();
		} catch (Exception e) {
			Log.e(TAG, "Error showing create new account fragment: " + e.getMessage(), e);
			Toast.makeText(this, "Error loading screen", Toast.LENGTH_SHORT).show();
		}
	}

	// Step 2: Show email input fragment
	public void showEmailFragment() {
		try {
			Log.d(TAG, "Showing UserAddEmailFragment");
			// Hide template layout, show fragment container
			hideTemplateLayout();
			Fragment fragment = new UserAddEmailFragment();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.replace(R.id.fragment_container, fragment);
			transaction.addToBackStack("email");
			transaction.commit();
		} catch (Exception e) {
			Log.e(TAG, "Error showing email fragment: " + e.getMessage(), e);
			Toast.makeText(this, "Error loading form", Toast.LENGTH_SHORT).show();
		}
	}

	// Step 3: Show personal info using activity_register.xml directly (Tell us about yourself)
	public void showTellUsFragment() {
		try {
			Log.d(TAG, "Showing Tell Us form using activity_register.xml");
			// Hide fragment container, show template layout
			if (fragmentContainer != null) {
				fragmentContainer.setVisibility(View.GONE);
			}
			if (templateLayout != null) {
				templateLayout.setVisibility(View.VISIBLE);
			}
			initializeTellUsViews();
			setupTellUsValidation();
			setupTellUsButtons();
		} catch (Exception e) {
			Log.e(TAG, "Error showing tell us form: " + e.getMessage(), e);
			Toast.makeText(this, "Error loading form", Toast.LENGTH_SHORT).show();
		}
	}

	// Initialize views from activity_register.xml
	private void initializeTellUsViews() {
		fragmentContainer = findViewById(R.id.fragment_container);
		templateLayout = findViewById(R.id.template_layout);

		firstNameInput = findViewById(R.id.firstNameInput);
		lastNameInput = findViewById(R.id.lastNameInput);
		usernameInput = findViewById(R.id.usernameInput);
		phoneInput = findViewById(R.id.etPhone);

		registerButton = findViewById(R.id.registerButton);

		fval = findViewById(R.id.fname_val);
		lval = findViewById(R.id.lname_val);
		uval = findViewById(R.id.Uname_val);
		phoneVal = findViewById(R.id.phone_val);

		// Hide validation messages initially
		if (fval != null) fval.setVisibility(View.GONE);
		if (lval != null) lval.setVisibility(View.GONE);
		if (uval != null) uval.setVisibility(View.GONE);
		if (phoneVal != null) phoneVal.setVisibility(View.GONE);
	}

	// Setup validation listeners for Tell Us form
	private void setupTellUsValidation() {
		if (firstNameInput != null) {
			firstNameInput.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
				@Override
				public void afterTextChanged(Editable s) {
					validateFirstName(s.toString());
				}
			});
		}

		if (lastNameInput != null) {
			lastNameInput.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
				@Override
				public void afterTextChanged(Editable s) {
					validateLastName(s.toString());
				}
			});
		}

		if (usernameInput != null) {
			usernameInput.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
				@Override
				public void afterTextChanged(Editable s) {
					validateUsername(s.toString());
				}
			});
		}

		if (phoneInput != null) {
			phoneInput.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
				@Override
				public void afterTextChanged(Editable s) {
					validatePhone(s.toString());
				}
			});
		}
	}

	// Setup buttons for Tell Us form
	private void setupTellUsButtons() {
		ImageButton backButton = findViewById(R.id.backToLoginButton);
		if (backButton != null) {
			backButton.setOnClickListener(v -> {
				if (fragmentManager.getBackStackEntryCount() > 0) {
					fragmentManager.popBackStack();
				} else {
					Intent intent = new Intent(this, MainActivity.class);
					startActivity(intent);
					finish();
				}
			});
		}

		if (registerButton != null) {
			registerButton.setOnClickListener(v -> {
				if (validateTellUsForm()) {
					savePersonalInfoAndContinue();
				}
			});
		}
	}

	// Validation helper methods
	private void showValidationError(TextView errorView, String message) {
		if (errorView != null) {
			errorView.setText(message);
			errorView.setVisibility(View.VISIBLE);
		}
	}

	private void hideValidationError(TextView errorView) {
		if (errorView != null) {
			errorView.setVisibility(View.GONE);
		}
	}

	private boolean containsNumbers(String text) {
		return text != null && text.matches(".*\\d+.*");
	}

	private int getPhoneDigitCount(String phone) {
		if (phone == null) return 0;
		return phone.replaceAll("[^0-9]", "").length();
	}

	// Validation methods
	private void validateFirstName(String firstName) {
		if (fval == null) return;

		if (containsNumbers(firstName)) {
			showValidationError(fval, "Name no numbers");
		} else if (firstName != null && firstName.length() < 2) {
			showValidationError(fval, "Name too short");
		} else {
			hideValidationError(fval);
		}
	}

	private void validateLastName(String lastName) {
		if (lval == null) return;

		if (containsNumbers(lastName)) {
			showValidationError(lval, "Name no numbers");
		} else {
			hideValidationError(lval);
		}
	}

	private void validateUsername(String username) {
		if (uval == null) return;

		if (username == null || username.isEmpty()) {
			hideValidationError(uval);
		} else if (username.contains(" ")) {
			showValidationError(uval, "Username no spaces");
		} else if (username.length() < 4) {
			showValidationError(uval, "Username min 4 chars");
		} else {
			hideValidationError(uval);
		}
	}

	private void validatePhone(String phone) {
		if (phoneVal == null) return;

		if (phone == null || phone.isEmpty()) {
			hideValidationError(phoneVal);
			return;
		}

		int digitCount = getPhoneDigitCount(phone);
		if (digitCount >= 10 && digitCount <= 15) {
			hideValidationError(phoneVal);
		} else {
			showValidationError(phoneVal, "Phone 10 digits min");
		}
	}

	// Validate all fields in Tell Us form
	private boolean validateTellUsForm() {
		String firstName = getText(firstNameInput);
		String lastName = getText(lastNameInput);
		String username = getText(usernameInput);
		String phone = getText(phoneInput);

		boolean isValid = true;

		if (firstName.isEmpty()) {
			showValidationError(fval, "First name required");
			isValid = false;
		}

		if (lastName.isEmpty()) {
			showValidationError(lval, "Last name required");
			isValid = false;
		}

		if (username.isEmpty()) {
			showValidationError(uval, "Username required");
			isValid = false;
		} else if (username.length() < 4) {
			showValidationError(uval, "Username min 4 chars");
			isValid = false;
		}

		if (phone.isEmpty()) {
			showValidationError(phoneVal, "Phone required");
			isValid = false;
		} else if (getPhoneDigitCount(phone) < 10) {
			showValidationError(phoneVal, "Phone 10 digits min");
			isValid = false;
		}

		return isValid;
	}

	// Save personal info and continue to password step
	private void savePersonalInfoAndContinue() {
		setUserPersonalInfo(
			getText(firstNameInput),
			getText(lastNameInput),
			getText(usernameInput),
			getText(phoneInput)
		);
		
		// If Google Sign-In user, register/login with backend immediately
		if (isGoogleSignIn) {
			registerGoogleUser();
			return;
		}
		
		// Hide template layout, show fragment container for next step
		if (templateLayout != null) {
			templateLayout.setVisibility(View.GONE);
		}
		if (fragmentContainer != null) {
			fragmentContainer.setVisibility(View.VISIBLE);
		}
		showCreatePasswordFragment();
	}
	
	// Register/Login Google user with backend
	private void registerGoogleUser() {
		// Ensure we have required data
		String email = getUserEmail();
		String firstName = getUserFirstName();
		String lastName = getUserLastName();
		String phone = getUserPhone();
		
		if (email == null || email.isEmpty()) {
			Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Log.d(TAG, "Registering Google user - Email: " + email + ", Phone: " + phone);
		Log.d(TAG, "First Name: " + firstName + ", Last Name: " + lastName);
		
		ApiService apiService = ApiClient.getApiService();
		// id_token may be null if not configured, that's okay
		String idToken = googleIdToken != null && !googleIdToken.isEmpty() ? googleIdToken : "";
		GoogleSignInRequest request = new GoogleSignInRequest(
			idToken,
			email,
			firstName != null ? firstName : "",
			lastName != null ? lastName : "",
			phone != null ? phone : ""
		);
		
		Log.d(TAG, "Sending Google Sign-In request - Email: " + email + ", First: " + firstName + ", Last: " + lastName + ", Phone: " + phone);

		Call<GoogleSignInResponse> call = apiService.googleSignIn(request);
		call.enqueue(new Callback<>() {
			@Override
			public void onResponse(@NonNull Call<GoogleSignInResponse> call, @NonNull Response<GoogleSignInResponse> response) {
				if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
					handleGoogleRegistrationSuccess(response.body());
				} else {
					// Log response body for debugging
					String errorBody = "";
					try {
						if (response.errorBody() != null) {
							errorBody = response.errorBody().string();
							Log.e(TAG, "Google Sign-In error response: " + errorBody);
						}
					} catch (Exception e) {
						Log.e(TAG, "Error reading error response", e);
					}
					handleGoogleRegistrationError(response.code(), errorBody);
				}
			}

			@Override
			public void onFailure(@NonNull Call<GoogleSignInResponse> call, @NonNull Throwable t) {
				Log.e(TAG, "Error registering Google user: " + t.getMessage(), t);
				Toast.makeText(RegisterActivity.this, 
					"Failed to register. Please check your connection and try again.", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	// Handle successful Google registration
	private void handleGoogleRegistrationSuccess(GoogleSignInResponse response) {
		if (response.getData() == null || response.getData().getUser() == null) {
			Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
			return;
		}

		// Save user data and token
		GoogleSignInResponse.User user = response.getData().getUser();
		tokenManager.saveToken("Bearer " + response.getData().getToken());
		tokenManager.saveEmail(user.getEmail());

		// Build and save name
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
		String fullName = nameBuilder.toString();
		if (!fullName.isEmpty()) {
			tokenManager.saveName(fullName);
			Log.d(TAG, "Saved name to cache: " + fullName);
		}

		// Navigate to password creation (Google users still need to set a password)
		if (templateLayout != null) {
			templateLayout.setVisibility(View.GONE);
		}
		if (fragmentContainer != null) {
			fragmentContainer.setVisibility(View.VISIBLE);
		}
		showCreatePasswordFragment();
	}
	
	// Register/login Facebook user
	private void registerFacebookUser(String accessToken, String email, String firstName, String lastName) {
		String phone = getUserPhone();

		if (email == null || email.isEmpty()) {
			Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
			return;
		}

		Log.d(TAG, "Registering Facebook user - Email: " + email + ", Phone: " + phone);
		Log.d(TAG, "First Name: " + firstName + ", Last Name: " + lastName);

		ApiService apiService = ApiClient.getApiService();
		String token = accessToken != null && !accessToken.isEmpty() ? accessToken : "";
		FacebookSignInRequest request = new FacebookSignInRequest(
			token,
			email,
			firstName != null ? firstName : "",
			lastName != null ? lastName : "",
			phone != null ? phone : ""
		);

		Log.d(TAG, "Sending Facebook Sign-In request - Email: " + email + ", First: " + firstName + ", Last: " + lastName + ", Phone: " + phone);

		Call<FacebookSignInResponse> call = apiService.facebookSignIn(request);
		call.enqueue(new Callback<>() {
			@Override
			public void onResponse(@NonNull Call<FacebookSignInResponse> call, @NonNull Response<FacebookSignInResponse> response) {
				if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
					handleFacebookRegistrationSuccess(response.body());
				} else {
					String errorBody = "";
					try {
						if (response.errorBody() != null) {
							errorBody = response.errorBody().string();
							Log.e(TAG, "Facebook Sign-In error response: " + errorBody);
						}
					} catch (Exception e) {
						Log.e(TAG, "Error reading error response", e);
					}
					handleFacebookRegistrationError(response.code(), errorBody);
				}
			}

			@Override
			public void onFailure(@NonNull Call<FacebookSignInResponse> call, @NonNull Throwable t) {
				Log.e(TAG, "Error registering Facebook user: " + t.getMessage(), t);
				Toast.makeText(RegisterActivity.this,
					"Failed to register. Please check your connection and try again.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	// Handle successful Facebook registration
	private void handleFacebookRegistrationSuccess(FacebookSignInResponse response) {
		if (response.getData() == null || response.getData().getUser() == null) {
			Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
			return;
		}

		// Save user data and token
		FacebookSignInResponse.User user = response.getData().getUser();
		tokenManager.saveToken("Bearer " + response.getData().getToken());
		tokenManager.saveEmail(user.getEmail());

		// Build and save name
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
		String fullName = nameBuilder.toString();
		if (!fullName.isEmpty()) {
			tokenManager.saveName(fullName);
			Log.d(TAG, "Saved name to cache: " + fullName);
		}

		// Navigate to password creation (Facebook users still need to set a password)
		if (templateLayout != null) {
			templateLayout.setVisibility(View.GONE);
		}
		if (fragmentContainer != null) {
			fragmentContainer.setVisibility(View.VISIBLE);
		}
		showCreatePasswordFragment();
	}

	// Handle Facebook registration error
	private void handleFacebookRegistrationError(int statusCode, String errorBody) {
		String errorMsg;
		if (statusCode == 422) {
			errorMsg = "Invalid data. Please check your information.";
			// Try to parse validation errors from response
			if (errorBody != null && !errorBody.isEmpty()) {
				Log.e(TAG, "Validation errors: " + errorBody);
			}
		} else if (statusCode == 500) {
			errorMsg = "Server error. Please try again later.";
		} else {
			errorMsg = "Registration failed. Please try again.";
		}
		Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
	}

	// Handle Google registration error
	private void handleGoogleRegistrationError(int statusCode, String errorBody) {
		String errorMsg;
		if (statusCode == 422) {
			errorMsg = "Invalid data. Please check your information.";
			// Try to parse validation errors from response
			if (errorBody != null && !errorBody.isEmpty()) {
				Log.e(TAG, "Validation errors: " + errorBody);
			}
		} else if (statusCode == 500) {
			errorMsg = "Server error. Please try again later.";
		} else {
			errorMsg = "Registration failed. Please try again.";
		}
		Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
	}

	private String getText(TextInputEditText editText) {
		if (editText == null || editText.getText() == null) {
			return "";
		}
		return editText.getText().toString().trim();
	}

	// Step 4: Show password creation fragment
	public void showCreatePasswordFragment() {
		try {
			Log.d(TAG, "Showing UserCreatePasswordFragment");
			// Hide template layout, show fragment container
			hideTemplateLayout();
			Fragment fragment = new UserCreatePasswordFragment();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.replace(R.id.fragment_container, fragment);
			transaction.addToBackStack("password");
			transaction.commit();
		} catch (Exception e) {
			Log.e(TAG, "Error showing password fragment: " + e.getMessage(), e);
			Toast.makeText(this, "Error loading form", Toast.LENGTH_SHORT).show();
		}
	}

	// Helper method to hide template layout and show fragment container
	private void hideTemplateLayout() {
		if (fragmentContainer == null) {
			fragmentContainer = findViewById(R.id.fragment_container);
		}
		if (templateLayout == null) {
			templateLayout = findViewById(R.id.template_layout);
		}
		if (fragmentContainer != null) {
			fragmentContainer.setVisibility(View.VISIBLE);
		}
		if (templateLayout != null) {
			templateLayout.setVisibility(View.GONE);
		}
	}

	// Step 5: Show OTP verification (Almost there)
	public void showOtpVerification() {
		// Skip OTP for Google Sign-In users
		if (isGoogleSignIn) {
			Log.d(TAG, "Skipping OTP for Google Sign-In user");
			showAccountCreatedFragment();
			return;
		}
		
		// Get email from stored data (from email fragment)
		String email = getUserEmail();
		if (email == null || email.isEmpty()) {
			Toast.makeText(this, "Email not found. Please start over.", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		// Send OTP to the email first
		sendOtpToEmail(email);
	}
	
	// Step 6: Show Account Created fragment
	public void showAccountCreatedFragment() {
		try {
			Log.d(TAG, "Showing AccountCreatedFragment");
			// Hide template layout, show fragment container
			hideTemplateLayout();
			Fragment fragment = new AccountCreatedFragment();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.replace(R.id.fragment_container, fragment);
			transaction.addToBackStack("account_created");
			transaction.commit();
		} catch (Exception e) {
			Log.e(TAG, "Error showing account created fragment: " + e.getMessage(), e);
			Toast.makeText(this, "Error loading screen", Toast.LENGTH_SHORT).show();
		}
	}

	// Send OTP to email and show verification dialog
	private void sendOtpToEmail(String email) {
		ApiService apiService = ApiClient.getApiService();
		VerificationRequest request = new VerificationRequest(email);

		Call<VerificationResponse> call = apiService.sendVerificationCode(request);
		call.enqueue(new Callback<>() {
			@Override
			public void onResponse(@NonNull Call<VerificationResponse> call, @NonNull Response<VerificationResponse> response) {
				if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
					// OTP sent successfully, show dialog
					showVerificationDialog(email);
				} else {
					Toast.makeText(RegisterActivity.this, "Failed to send OTP. Please try again.", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(@NonNull Call<VerificationResponse> call, @NonNull Throwable t) {
				Log.e(TAG, "Error sending OTP: " + t.getMessage(), t);
				Toast.makeText(RegisterActivity.this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	// Show OTP verification dialog
	private void showVerificationDialog(String email) {
		View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_verification_code, null);

		// Initialize dialog views
		TextInputEditText codeInput = dialogView.findViewById(R.id.codeInput);
		TextInputLayout codeInputLayout = dialogView.findViewById(R.id.codeInputLayout);
		Button verifyButton = dialogView.findViewById(R.id.verifyButton);
		MaterialButton resendCodeButton = dialogView.findViewById(R.id.resendCodeButton);
		TextView messageTextView = dialogView.findViewById(R.id.verificationMessage);
		ImageButton closeButton = dialogView.findViewById(R.id.closeButton);

		// Set email message (mask email for privacy)
		if (messageTextView != null && email != null) {
			String maskedEmail = maskEmail(email);
			String message = String.format("We've sent a 6-digit code to your\nemail %s.", maskedEmail);
			messageTextView.setText(message);
		}

		// Create and show dialog (as full screen)
		AlertDialog dialog = new MaterialAlertDialogBuilder(this)
			.setView(dialogView)
			.setCancelable(true)
			.create();

		android.view.Window window = dialog.getWindow();
		if (window != null) {
			window.setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT);
		}

		// Setup dialog components
		setupOtpDialogCloseButton(closeButton, dialog);
		setupOtpCodeInput(codeInput, codeInputLayout, verifyButton);
		setupOtpVerifyButton(verifyButton, codeInput, codeInputLayout, email, dialog);
		setupOtpResendButton(resendCodeButton, email);

		dialog.show();
	}

	// Mask email for display (e.g., user@example.com -> use***@example.com)
	private String maskEmail(String email) {
		if (email == null || !email.contains("@")) {
			return "*******@example.com";
		}
		int atIndex = email.indexOf("@");
		String localPart = email.substring(0, Math.min(3, atIndex));
		String domain = email.substring(atIndex);
		return localPart + "***" + domain;
	}

	// Setup close button for OTP dialog
	private void setupOtpDialogCloseButton(ImageButton closeButton, AlertDialog dialog) {
		if (closeButton != null) {
			closeButton.setOnClickListener(v -> dialog.dismiss());
		}
	}

	// Setup code input field with real-time validation
	private void setupOtpCodeInput(TextInputEditText codeInput, TextInputLayout codeInputLayout, Button verifyButton) {
		if (codeInput == null) return;

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
						verifyButton.setEnabled(s.length() == 6);
					}
				}
			});
	}

	// Setup verify button click handler
	@SuppressLint("SetTextI18n")
	private void setupOtpVerifyButton(Button verifyButton, TextInputEditText codeInput,
	                                 TextInputLayout codeInputLayout, String email, AlertDialog dialog) {
		if (verifyButton == null) return;

		verifyButton.setEnabled(false);
		verifyButton.setOnClickListener(v -> {
			String code = "";
			if (codeInput != null && codeInput.getText() != null) {
				code = codeInput.getText().toString().trim();
			}

			// Validate code length
			if (code.length() != 6) {
				if (codeInputLayout != null) {
					codeInputLayout.setError("Enter 6-digit code");
				}
				return;
			}

			// Clear error and verify
			if (codeInputLayout != null) {
				codeInputLayout.setError(null);
			}
			verifyButton.setEnabled(false);
			verifyButton.setText("Verifying...");
			verifyOtpCode(email, code, dialog, verifyButton);
		});
	}

	// Verify OTP code
	private void verifyOtpCode(String email, String code, AlertDialog dialog, Button verifyButton) {
		ApiService apiService = ApiClient.getApiService();
		VerifyEmailRequest request = new VerifyEmailRequest(email, code);

		Call<VerifyEmailResponse> call = apiService.verifyEmail(request);
		call.enqueue(new Callback<>() {
			@Override
			public void onResponse(@NonNull Call<VerifyEmailResponse> call, @NonNull Response<VerifyEmailResponse> response) {
				resetVerifyButton(verifyButton);

				if (response.isSuccessful() && response.body() != null) {
					handleOtpVerificationSuccess(response.body(), dialog);
				} else {
					handleOtpVerificationError(response.code());
				}
			}

			@Override
			public void onFailure(@NonNull Call<VerifyEmailResponse> call, @NonNull Throwable t) {
				resetVerifyButton(verifyButton);
				Log.e(TAG, "Error verifying OTP: " + t.getMessage(), t);
				Toast.makeText(RegisterActivity.this,
					"Failed to verify code. Please check your connection and try again.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	// Handle successful OTP verification
	private void handleOtpVerificationSuccess(VerifyEmailResponse response, AlertDialog dialog) {
		if (!response.isSuccess() || response.getData() == null) {
			String errorMsg = response.getMessage() != null ?
				response.getMessage() :
				"Invalid verification code";
			Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
			return;
		}

		// Save user data and navigate
		VerifyEmailResponse.User user = response.getData().getUser();
		tokenManager.saveToken("Bearer " + response.getData().getToken());
		tokenManager.saveEmail(user.getEmail());

		// Build and save name
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
		String fullName = nameBuilder.toString();
		if (!fullName.isEmpty()) {
			tokenManager.saveName(fullName);
			Log.d(TAG, "Saved name to cache: " + fullName);
		}

		// Close dialog and navigate to Account Created fragment
		dialog.dismiss();
		showAccountCreatedFragment();
	}


	// Handle OTP verification error
	private void handleOtpVerificationError(int statusCode) {
		String errorMsg = (statusCode == 400) ?
			"Invalid or expired code" :
			"Invalid verification code";
		Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
	}

	// Reset verify button to original state
	@SuppressLint("SetTextI18n")
	private void resetVerifyButton(Button verifyButton) {
		if (verifyButton != null) {
			verifyButton.setEnabled(true);
			verifyButton.setText("Continue");
		}
	}

	// Setup resend code button
	@SuppressLint("SetTextI18n")
	private void setupOtpResendButton(MaterialButton resendButton, String email) {
		if (resendButton == null) return;

		resendButton.setOnClickListener(v -> {
			resendButton.setEnabled(false);
			resendButton.setText("Sending...");
			resendOtpCode(email, resendButton);
		});
	}

	// Resend OTP code
	private void resendOtpCode(String email, MaterialButton resendButton) {
		ApiService apiService = ApiClient.getApiService();
		VerificationRequest request = new VerificationRequest(email);

		Call<VerificationResponse> call = apiService.sendVerificationCode(request);
		call.enqueue(new Callback<>() {
			@Override
			public void onResponse(@NonNull Call<VerificationResponse> call, @NonNull Response<VerificationResponse> response) {
				resetResendButton(resendButton);

				if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
					String message = response.body().getMessage() != null ?
						response.body().getMessage() :
						"Verification code sent to your email";
					Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(RegisterActivity.this, "Failed to send code", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(@NonNull Call<VerificationResponse> call, @NonNull Throwable t) {
				resetResendButton(resendButton);
				Log.e(TAG, "Error resending OTP: " + t.getMessage(), t);
				Toast.makeText(RegisterActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	// Reset resend button to original state
	@SuppressLint("SetTextI18n")
	private void resetResendButton(MaterialButton resendButton) {
		if (resendButton != null) {
			resendButton.setEnabled(true);
			resendButton.setText("resend");
		}
	}


	// Setters for registration data (called by fragments)
	public void setUserEmail(String email) {
		this.userEmail = email;
		Log.d(TAG, "Email set: " + email);
	}

	public void setUserPersonalInfo(String firstName, String lastName, String username, String phone) {
		this.userFirstName = firstName;
		this.userLastName = lastName;
		this.userName = username;
		this.userPhone = phone;
		Log.d(TAG, "Personal info set - Name: " + firstName + " " + lastName);
	}

	public void setUserPassword(String password) {
		this.userPassword = password;
		Log.d(TAG, "Password set");
	}

	// Getters for registration data (used by fragments - may be used in future fragments)
	@SuppressWarnings("unused")
	public String getUserEmail() {
		return userEmail;
	}

	@SuppressWarnings("unused")
	public String getUserFirstName() {
		return userFirstName;
	}

	@SuppressWarnings("unused")
	public String getUserLastName() {
		return userLastName;
	}

	@SuppressWarnings("unused")
	public String getUserName() {
		return userName;
	}

	@SuppressWarnings("unused")
	public String getUserPhone() {
		return userPhone;
	}

	@SuppressWarnings("unused")
	public String getUserPassword() {
		return userPassword;
	}

	// Handle Google Sign-In success
	public void handleGoogleSignInSuccess(String email, String givenName, String familyName, 
	                                     String displayName, String idToken) {
		Log.d(TAG, "Handling Google Sign-In success");
		
		// Mark as Google Sign-In user (skip OTP)
		isGoogleSignIn = true;
		
		// Store Google account data
		setUserEmail(email);
		
		// Use Google name if available, otherwise use display name
		String firstName = givenName != null && !givenName.isEmpty() ? givenName : 
			(displayName != null && displayName.contains(" ") ? displayName.split(" ")[0] : displayName);
		String lastName = familyName != null && !familyName.isEmpty() ? familyName : 
			(displayName != null && displayName.contains(" ") ? 
				displayName.substring(displayName.indexOf(" ") + 1) : "");
		
		// Generate username from email (before @)
		String username = email != null && email.contains("@") ? 
			email.substring(0, email.indexOf("@")) : "user_" + System.currentTimeMillis();
		
		// Store personal info from Google
		setUserPersonalInfo(firstName, lastName, username, "");
		
		// Store Google ID token for backend API call
		googleIdToken = idToken;
		
		// Navigate to "Tell Us" to collect phone number
		Toast.makeText(this, "Welcome! Please provide your phone number to continue.", Toast.LENGTH_LONG).show();
		showTellUsFragment();
	}

	// Handle Facebook Sign-In success
	public void handleFacebookSignInSuccess(String email, String firstName, String lastName, 
	                                       String displayName, String accessToken) {
		Log.d(TAG, "Handling Facebook Sign-In success");
		
		// Mark as Google Sign-In user (same flow - skip OTP)
		isGoogleSignIn = true;
		
		// Store Facebook account data
		setUserEmail(email);
		
		// Use Facebook name if available, otherwise use display name
		String finalFirstName = firstName != null && !firstName.isEmpty() ? firstName : 
			(displayName != null && displayName.contains(" ") ? displayName.split(" ")[0] : displayName);
		String finalLastName = lastName != null && !lastName.isEmpty() ? lastName : 
			(displayName != null && displayName.contains(" ") ? 
				displayName.substring(displayName.indexOf(" ") + 1) : "");
		
		// Generate username from email (before @)
		String username = email != null && email.contains("@") ? 
			email.substring(0, email.indexOf("@")) : "user_" + System.currentTimeMillis();
		
		// Store personal info from Facebook
		setUserPersonalInfo(finalFirstName, finalLastName, username, "");
		
		// Store Facebook access token for backend API call (we'll use it later in registerFacebookUser)
		googleIdToken = accessToken;
		
		// Navigate to "Tell Us" to collect phone number first (same flow as Google)
		Toast.makeText(this, "Welcome! Please provide your phone number to continue.", Toast.LENGTH_LONG).show();
		showTellUsFragment();
	}
	
	// Store Google ID token
	private String googleIdToken;
	
	// Check if user signed in with Google
	public boolean isGoogleSignInUser() {
		return isGoogleSignIn;
	}

	// Update password for Google user (set initial password)
	public void updateGoogleUserPassword(String password, String confirmPassword) {
		String token = tokenManager.getToken();
		if (token == null || token.isEmpty()) {
			Toast.makeText(this, "Not authenticated. Please try again.", Toast.LENGTH_SHORT).show();
			return;
		}

		Log.d(TAG, "Updating password for Google user");

		ApiService apiService = ApiClient.getApiService();
		SetInitialPasswordRequest request = new SetInitialPasswordRequest(password, confirmPassword);

		Call<SetInitialPasswordResponse> call = apiService.setInitialPassword(token, request);
		call.enqueue(new Callback<>() {
			@Override
			public void onResponse(@NonNull Call<SetInitialPasswordResponse> call, @NonNull Response<SetInitialPasswordResponse> response) {
				if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
					Log.d(TAG, "Password updated successfully for Google user");
					// Navigate to Account Created
					showAccountCreatedFragment();
				} else {
					String errorMsg = "Failed to set password. Please try again.";
					if (response.body() != null && response.body().getMessage() != null) {
						errorMsg = response.body().getMessage();
					}
					Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(@NonNull Call<SetInitialPasswordResponse> call, @NonNull Throwable t) {
				Log.e(TAG, "Error updating password: " + t.getMessage(), t);
				Toast.makeText(RegisterActivity.this, 
					"Failed to set password. Please check your connection and try again.", Toast.LENGTH_SHORT).show();
			}
		});
	}

}
