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
import app.hub.api.RegisterRequest;
import app.hub.api.RegisterResponse;
import app.hub.api.UpdateProfileRequest;
import app.hub.api.UserResponse;
import app.hub.api.VerifyEmailResponse;
import app.hub.user_emailOtp;
import app.hub.util.TokenManager;
import app.hub.util.UserLocationManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Activity for user registration - Multi-step flow container
public class RegisterActivity extends AppCompatActivity {

	private static final String TAG = "RegisterActivity";
	private TokenManager tokenManager;
	private UserLocationManager userLocationManager;
	private FragmentManager fragmentManager;

	// Registration data to pass between steps
	private String userEmail;
	private String userFirstName;
	private String userLastName;
	private String userName;
	private String userPhone;
	private String userLocation;
	private String userPassword;
	
	// Track if user signed in with Google (skip OTP for Google users)
	private boolean isGoogleSignIn = false;
	// Track if user signed in with Facebook (same flow as Google)
	private boolean isFacebookSignIn = false;
	// Store Facebook email separately to ensure it's available
	private String facebookEmail;

	// Views for activity_register.xml (Tell us step)
	private View fragmentContainer;
	private ConstraintLayout templateLayout;
	private TextInputEditText firstNameInput, lastNameInput, usernameInput, phoneInput, locationInput;
	private TextView fval, lval, uval, phoneVal;
	private MaterialButton registerButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		try {
			tokenManager = new TokenManager(this);
			userLocationManager = new UserLocationManager(this);
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
				// If template layout is visible (Tell Us step), handle back navigation
				if (templateLayout != null && templateLayout.getVisibility() == View.VISIBLE) {
					// For Google/Facebook users, skip email fragment and go back to CreateNewAccountFragment
					if (isGoogleSignIn || isFacebookSignIn) {
						// Clear sign-in states so user can select different account
						clearAllSignInStates();
						showCreateNewAccountFragment();
					} else {
						// For regular email users, go back to email fragment
						showEmailFragment();
					}
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
		locationInput = findViewById(R.id.etLocation);

		registerButton = findViewById(R.id.registerButton);

		fval = findViewById(R.id.fname_val);
		lval = findViewById(R.id.lname_val);
		uval = findViewById(R.id.Uname_val);
		phoneVal = findViewById(R.id.phone_val);

		// Auto-fill location if detected
		try {
			String detectedCity = tokenManager.getCurrentCity();
			if (detectedCity != null && !detectedCity.isEmpty() && locationInput != null) {
				locationInput.setText(detectedCity);
				Toast.makeText(this, "Location auto-detected: " + detectedCity, Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.e(TAG, "Error auto-filling location", e);
		}

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
				// For Google/Facebook users, skip email fragment and go back to CreateNewAccountFragment
				if (isGoogleSignIn || isFacebookSignIn) {
					// Clear sign-in states so user can select different account
					clearAllSignInStates();
					showCreateNewAccountFragment();
				} else {
					// For regular email users, use fragment back stack or go to email fragment
					if (fragmentManager.getBackStackEntryCount() > 0) {
						fragmentManager.popBackStack();
					} else {
						showEmailFragment();
					}
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
		try {
			setUserPersonalInfo(
				getText(firstNameInput),
				getText(lastNameInput),
				getText(usernameInput),
				getText(phoneInput),
				getText(locationInput)
			);
		} catch (Exception e) {
			Log.e(TAG, "Error saving personal info", e);
			// Continue anyway with empty location
			setUserPersonalInfo(
				getText(firstNameInput),
				getText(lastNameInput),
				getText(usernameInput),
				getText(phoneInput),
				""
			);
		}
		
		// If Google or Facebook Sign-In user, register/login with backend immediately
		if (isGoogleSignIn || isFacebookSignIn) {
			if (isFacebookSignIn) {
				// Get email - use getUserEmail() which is set by Email fragment (if user went through it)
				// Fallback to facebookEmail if Email fragment didn't set it
				String email = getUserEmail();
				if (email == null || email.isEmpty()) {
					email = facebookEmail;
				}
				
				Log.d(TAG, "Facebook user continuing from Tell Us:");
				Log.d(TAG, "  - Facebook ID: " + facebookId);
				Log.d(TAG, "  - getUserEmail(): " + (getUserEmail() != null ? getUserEmail() : "NULL"));
				Log.d(TAG, "  - facebookEmail: " + (facebookEmail != null ? facebookEmail : "NULL"));
				Log.d(TAG, "  - Using email: " + (email != null && !email.isEmpty() ? email : "NULL (pure FB auth)"));
				
				// Ensure we have Facebook ID (required for pure FB auth)
				if (facebookId == null || facebookId.isEmpty()) {
					Log.e(TAG, "Facebook ID is required for Facebook registration");
					Toast.makeText(this, "Authentication error. Please try again.", Toast.LENGTH_SHORT).show();
					return;
				}
				
				Log.d(TAG, "Registering Facebook user with ID: " + facebookId + ", Email: " + (email != null ? email : "NULL"));
				// Use stored access token from googleIdToken (we stored Facebook token there)
				registerFacebookUser(googleIdToken, facebookId, email, getUserFirstName(), getUserLastName());
			} else {
				registerGoogleUser();
			}
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
		
		Log.d(TAG, "Registering Google user with backend - Email: " + email);
		Log.d(TAG, "First Name: " + firstName + ", Last Name: " + lastName + ", Phone: " + phone);
		
		// Now actually register the user with the backend
		ApiService apiService = ApiClient.getApiService();
		String idToken = googleIdToken != null && !googleIdToken.isEmpty() ? googleIdToken : "";
		GoogleSignInRequest request = new GoogleSignInRequest(
			idToken,
			email,
			firstName != null ? firstName : "",
			lastName != null ? lastName : "",
			phone != null ? phone : ""
		);
		
		Log.d(TAG, "Sending Google registration request - Email: " + email + ", First: " + firstName + ", Last: " + lastName + ", Phone: " + phone);

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
							Log.e(TAG, "Google registration error response: " + errorBody);
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

	private void checkGoogleAccountExistsForRegistration(String email, String firstName, String lastName, String phone) {
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
		
		Log.d(TAG, "Checking Google account existence - Email: " + email + ", First: " + firstName + ", Last: " + lastName + ", Phone: " + phone);

		Call<GoogleSignInResponse> call = apiService.googleSignIn(request);
		call.enqueue(new Callback<>() {
			@Override
			public void onResponse(@NonNull Call<GoogleSignInResponse> call, @NonNull Response<GoogleSignInResponse> response) {
				if (response.isSuccessful() && response.body() != null) {
					GoogleSignInResponse signInResponse = response.body();
					if (signInResponse.isSuccess()) {
						// Account already exists - log user in automatically
						Log.d(TAG, "Google account exists, logging user in");
						handleGoogleLoginSuccess(signInResponse);
					} else {
						// Account doesn't exist - proceed with registration
						proceedWithGoogleRegistration(email, firstName, lastName, phone, idToken);
					}
				} else {
					// Handle error response - assume account doesn't exist and proceed
					Log.w(TAG, "Google account check failed, proceeding with registration");
					proceedWithGoogleRegistration(email, firstName, lastName, phone, idToken);
				}
			}

			@Override
			public void onFailure(@NonNull Call<GoogleSignInResponse> call, @NonNull Throwable t) {
				Log.e(TAG, "Error checking Google account existence: " + t.getMessage(), t);
				Toast.makeText(RegisterActivity.this, 
					"Failed to check account. Please check your connection and try again.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void proceedWithGoogleRegistration(String email, String firstName, String lastName, String phone, String idToken) {
		Log.d(TAG, "Account doesn't exist, proceeding to Tell Us form");
		
		// Navigate to "Tell Us" to collect phone number and other details
		Toast.makeText(this, "Welcome! Please provide your phone number to continue.", Toast.LENGTH_LONG).show();
		showTellUsFragment();
	}
	
	// Handle successful Facebook login (account already exists)
	private void handleFacebookLoginSuccess(FacebookSignInResponse response) {
		if (response.getData() == null || response.getData().getUser() == null) {
			Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
			return;
		}

		// Save user data and token
		FacebookSignInResponse.User user = response.getData().getUser();
		tokenManager.saveToken("Bearer " + response.getData().getToken());
		if (user.getEmail() != null && !user.getEmail().isEmpty()) {
			tokenManager.saveEmail(user.getEmail());
		}

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

		// Force immediate token persistence
		tokenManager.forceCommit();

		// Navigate to appropriate dashboard based on user role
		navigateToUserDashboard(user.getRole());
	}

	// Handle successful Google login (account already exists)
	private void handleGoogleLoginSuccess(GoogleSignInResponse response) {
		if (response.getData() == null || response.getData().getUser() == null) {
			Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
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

		// Force immediate token persistence
		tokenManager.forceCommit();

		// Navigate to appropriate dashboard based on user role
		navigateToUserDashboard(user.getRole());
	}

	// Navigate to appropriate dashboard based on user role
	private void navigateToUserDashboard(String role) {
		Intent intent;
		
		if (role == null) {
			role = "customer"; // Default role
		}
		
		switch (role.toLowerCase()) {
			case "admin":
				intent = new Intent(this, app.hub.admin.AdminDashboardActivity.class);
				break;
			case "manager":
				intent = new Intent(this, app.hub.manager.ManagerDashboardActivity.class);
				break;
			case "employee":
				intent = new Intent(this, app.hub.employee.EmployeeDashboardActivity.class);
				break;
			case "customer":
			default:
				intent = new Intent(this, app.hub.user.DashboardActivity.class);
				break;
		}
		
		Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
		startActivity(intent);
		finish();
	}

	// Clear all sign-in states (called from CreateNewAccountFragment)
	public void clearAllSignInStates() {
		clearGoogleSignInState();
		clearFacebookSignInState();
	}

	// Clear Facebook sign-in state to allow user to try different account
	private void clearFacebookSignInState() {
		isFacebookSignIn = false;
		isGoogleSignIn = false;
		facebookId = null;
		facebookEmail = null;
		googleIdToken = null;
		userEmail = null;
		userFirstName = null;
		userLastName = null;
		userName = null;
		userPhone = null;
		userLocation = null;
		userPassword = null;
	}

	// Clear Google sign-in state to allow user to select different account
	private void clearGoogleSignInState() {
		isGoogleSignIn = false;
		googleIdToken = null;
		userEmail = null;
		userFirstName = null;
		userLastName = null;
		userName = null;
		userPhone = null;
		userLocation = null;
		userPassword = null;
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

		// Force immediate token persistence
		tokenManager.forceCommit();

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
	private void registerFacebookUser(String accessToken, String facebookId, String email, String firstName, String lastName) {
		String phone = getUserPhone();

		// Validate Facebook ID (required for pure FB auth)
		if (facebookId == null || facebookId.isEmpty()) {
			Log.e(TAG, "registerFacebookUser: Facebook ID is null or empty");
			Toast.makeText(this, "Authentication error. Please try again.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		// Log all data being sent
		Log.d(TAG, "Registering Facebook user with backend:");
		Log.d(TAG, "  - Facebook ID: " + facebookId);
		Log.d(TAG, "  - Email: " + (email != null && !email.isEmpty() ? email : "NULL (pure FB auth)"));
		Log.d(TAG, "  - First Name: " + firstName);
		Log.d(TAG, "  - Last Name: " + lastName);
		Log.d(TAG, "  - Phone: " + phone);
		Log.d(TAG, "  - Access Token: " + (accessToken != null && !accessToken.isEmpty() ? "Present" : "Missing"));

		// Now actually register the user with the backend
		ApiService apiService = ApiClient.getApiService();
		String token = accessToken != null && !accessToken.isEmpty() ? accessToken : "";
		FacebookSignInRequest request = new FacebookSignInRequest(
			token,
			facebookId,
			email != null ? email : "", // Email can be empty for pure FB auth
			firstName != null ? firstName : "",
			lastName != null ? lastName : "",
			phone != null ? phone : ""
		);

		Log.d(TAG, "Sending Facebook registration request - Facebook ID: " + facebookId + ", Email: " + (email != null ? email : "NULL") + ", First: " + firstName + ", Last: " + lastName + ", Phone: " + phone);

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
							Log.e(TAG, "Facebook registration error response: " + errorBody);
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

	private void checkFacebookAccountExistsForRegistration(String accessToken, String facebookId, String email, String firstName, String lastName, String phone) {
		ApiService apiService = ApiClient.getApiService();
		String token = accessToken != null && !accessToken.isEmpty() ? accessToken : "";
		FacebookSignInRequest request = new FacebookSignInRequest(
			token,
			facebookId,
			email != null ? email : "", // Email can be empty for pure FB auth
			firstName != null ? firstName : "",
			lastName != null ? lastName : "",
			phone != null ? phone : ""
		);

		Log.d(TAG, "Checking Facebook account existence - Facebook ID: " + facebookId + ", Email: " + (email != null ? email : "NULL") + ", First: " + firstName + ", Last: " + lastName + ", Phone: " + phone);

		Call<FacebookSignInResponse> call = apiService.facebookSignIn(request);
		call.enqueue(new Callback<>() {
			@Override
			public void onResponse(@NonNull Call<FacebookSignInResponse> call, @NonNull Response<FacebookSignInResponse> response) {
				if (response.isSuccessful() && response.body() != null) {
					FacebookSignInResponse signInResponse = response.body();
					if (signInResponse.isSuccess()) {
						// Account already exists - log user in automatically
						Log.d(TAG, "Facebook account exists, logging user in");
						handleFacebookLoginSuccess(signInResponse);
					} else {
						// Account doesn't exist - proceed with registration
						proceedWithFacebookRegistration(accessToken, facebookId, email, firstName, lastName, phone);
					}
				} else {
					// Handle error response - assume account doesn't exist and proceed
					Log.w(TAG, "Facebook account check failed, proceeding with registration");
					proceedWithFacebookRegistration(accessToken, facebookId, email, firstName, lastName, phone);
				}
			}

			@Override
			public void onFailure(@NonNull Call<FacebookSignInResponse> call, @NonNull Throwable t) {
				Log.e(TAG, "Error checking Facebook account existence: " + t.getMessage(), t);
				Toast.makeText(RegisterActivity.this,
					"Failed to check account. Please check your connection and try again.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void proceedWithFacebookRegistration(String accessToken, String facebookId, String email, String firstName, String lastName, String phone) {
		Log.d(TAG, "Account doesn't exist, proceeding to registration flow");
		
		// If email is available, go to email fragment; otherwise skip directly to Tell Us
		if (email != null && !email.isEmpty()) {
			Log.d(TAG, "Email available, navigating to email fragment");
			Toast.makeText(this, "Please verify your email and continue.", Toast.LENGTH_SHORT).show();
			showEmailFragment();
		} else {
			Log.d(TAG, "No email from Facebook, skipping email fragment and going to Tell Us");
			Toast.makeText(this, "Welcome! Please provide your information to continue.", Toast.LENGTH_SHORT).show();
			showTellUsFragment();
		}
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
		if (user.getEmail() != null && !user.getEmail().isEmpty()) {
			tokenManager.saveEmail(user.getEmail());
		}

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

		// Force immediate token persistence
		tokenManager.forceCommit();

		// Navigate directly to Account Created (skip password creation for Facebook users)
		Log.d(TAG, "Facebook registration successful, navigating to Account Created");
		showAccountCreatedFragment();
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
		// Skip OTP for Google/Facebook Sign-In users
		if (isGoogleSignIn || isFacebookSignIn) {
			Log.d(TAG, "Skipping OTP for social Sign-In user");
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

		// Show OTP fragment instead of dialog
		try {
			Log.d(TAG, "Showing user_emailOtp fragment");
			// Hide template layout, show fragment container
			hideTemplateLayout();
			Fragment fragment = new user_emailOtp();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.replace(R.id.fragment_container, fragment);
			transaction.addToBackStack("otp_verification");
			transaction.commit();
		} catch (Exception e) {
			Log.e(TAG, "Error showing OTP fragment: " + e.getMessage(), e);
			Toast.makeText(this, "Error loading OTP screen", Toast.LENGTH_SHORT).show();
		}
	}
	
	// Step 6: Show Account Created fragment
	public void showAccountCreatedFragment() {
		try {
			Log.d(TAG, "Showing AccountCreatedFragment");

			// Update location for newly registered user
			updateLocationForNewUser();

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

	// Handle successful OTP verification (called from fragment)
	public void handleOtpVerificationSuccess(VerifyEmailResponse response) {
		if (!response.isSuccess() || response.getData() == null) {
			String errorMsg = response.getMessage() != null ?
				response.getMessage() :
				"Invalid verification code";
			Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
			return;
		}

		// Check if user exists (for resend scenario) or is being created (registration)
		VerifyEmailResponse.User user = response.getData().getUser();
		
		if (user != null) {
			// User exists - save user data and token
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
		} else {
			// User doesn't exist yet (registration flow) - OTP verified, now create account
			Log.d(TAG, "OTP verified for registration, creating account...");
			createAccountAfterOtpVerification();
			return; // Don't navigate yet, wait for account creation
		}

		// Navigate to Account Created fragment
		showAccountCreatedFragment();
	}
	
	// Create account after OTP verification
	private void createAccountAfterOtpVerification() {
		// Get all collected user data
		String email = getUserEmail();
		String firstName = getUserFirstName();
		String lastName = getUserLastName();
		String username = getUserName();
		String password = getUserPassword();
		String phone = getUserPhone() != null ? getUserPhone() : "";
		String location = getUserLocation() != null ? getUserLocation() : "";

		// Validate required fields
		if (email == null || email.isEmpty()) {
			Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
			return;
		}
		if (firstName == null || firstName.isEmpty()) {
			Toast.makeText(this, "First name is required", Toast.LENGTH_SHORT).show();
			return;
		}
		if (lastName == null || lastName.isEmpty()) {
			Toast.makeText(this, "Last name is required", Toast.LENGTH_SHORT).show();
			return;
		}
		if (username == null || username.isEmpty()) {
			Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show();
			return;
		}
		if (password == null || password.isEmpty()) {
			Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
			return;
		}

		// Default role to "customer" for regular registration
		String role = "customer";

		Log.d(TAG, "Creating account with - Email: " + email + ", Username: " + username + ", Role: " + role + ", Location: " + location);
		
		ApiService apiService = ApiClient.getApiService();
		RegisterRequest request = new RegisterRequest(username, firstName, lastName, email, phone, location, password, password, role);

		Call<RegisterResponse> call = apiService.register(request);
		call.enqueue(new Callback<>() {
			@Override
			public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
				if (response.isSuccessful() && response.body() != null) {
					RegisterResponse body = response.body();
					if (body.isSuccess() && body.getData() != null) {
						// Account created successfully
						RegisterResponse.User user = body.getData().getUser();
						String token = body.getData().getToken();
						
						// Save user data and token
						if (token != null) {
							tokenManager.saveToken("Bearer " + token);
						}
						tokenManager.saveEmail(user != null ? user.getEmail() : email);
						
						// Build and save name
						if (user != null) {
							String userFirstName = user.getFirstName();
							String userLastName = user.getLastName();
							StringBuilder nameBuilder = new StringBuilder();
							if (userFirstName != null && !userFirstName.trim().isEmpty()) {
								nameBuilder.append(userFirstName.trim());
							}
							if (userLastName != null && !userLastName.trim().isEmpty()) {
								if (nameBuilder.length() > 0) {
									nameBuilder.append(" ");
								}
								nameBuilder.append(userLastName.trim());
							}
							String fullName = nameBuilder.toString();
							if (!fullName.isEmpty()) {
								tokenManager.saveName(fullName);
								Log.d(TAG, "Saved name to cache: " + fullName);
							}
						}
						
						// Force immediate token persistence
						tokenManager.forceCommit();
						
						Log.d(TAG, "Account created successfully");
						// Navigate to Account Created fragment
						showAccountCreatedFragment();
					} else {
						// Registration failed
						String errorMsg = body.getMessage() != null ? body.getMessage() : "Failed to create account";
						Log.e(TAG, "Account creation failed: " + errorMsg);
						Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
					}
				} else {
					// Response not successful
					String errorMsg = "Failed to create account. Please try again.";
					try {
						if (response.errorBody() != null) {
							com.google.gson.Gson gson = new com.google.gson.Gson();
							java.io.BufferedReader reader = new java.io.BufferedReader(
								new java.io.InputStreamReader(response.errorBody().byteStream()));
							String errorJson = reader.readLine();
							if (errorJson != null) {
								RegisterResponse errorResponse = gson.fromJson(errorJson, RegisterResponse.class);
								if (errorResponse != null && errorResponse.getMessage() != null) {
									errorMsg = errorResponse.getMessage();
								}
							}
						}
					} catch (Exception e) {
						Log.e(TAG, "Error parsing error response: " + e.getMessage(), e);
					}
					Log.e(TAG, "Account creation failed with status: " + response.code() + ", message: " + errorMsg);
					Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
				Log.e(TAG, "Error creating account: " + t.getMessage(), t);
				String errorMsg = "Network error. Please check your connection and try again.";
				if (t.getMessage() != null) {
					if (t.getMessage().contains("timeout") || t.getMessage().contains("Timeout")) {
						errorMsg = "Request timeout. Please check your connection and try again.";
					} else if (t.getMessage().contains("Unable to resolve host")) {
						errorMsg = "Cannot reach server. Please check your internet connection.";
					}
				}
				Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
			}
		});
	}


	// Setters for registration data (called by fragments)
	public void setUserEmail(String email) {
		this.userEmail = email;
		Log.d(TAG, "setUserEmail called with: " + (email != null ? email : "NULL"));
		Log.d(TAG, "userEmail field is now: " + (this.userEmail != null ? this.userEmail : "NULL"));
	}

	public void setUserPersonalInfo(String firstName, String lastName, String username, String phone, String location) {
		this.userFirstName = firstName;
		this.userLastName = lastName;
		this.userName = username;
		this.userPhone = phone;
		this.userLocation = location;
		Log.d(TAG, "Personal info set - Name: " + firstName + " " + lastName + ", Location: " + location);
	}

	public void setUserPassword(String password) {
		this.userPassword = password;
		Log.d(TAG, "Password set");
	}

	// Getters for registration data (used by fragments - may be used in future fragments)
	@SuppressWarnings("unused")
	public String getUserEmail() {
		Log.d(TAG, "getUserEmail() called, returning: " + (userEmail != null ? userEmail : "NULL"));
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
	public String getUserLocation() {
		return userLocation;
	}

	@SuppressWarnings("unused")
	public String getUserPassword() {
		return userPassword;
	}

	// Handle Google Sign-In success
	public void handleGoogleSignInSuccess(String email, String givenName, String familyName, 
	                                     String displayName, String idToken) {
		Log.d(TAG, "Handling Google Sign-In success");
		
		// Show loading message
		Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show();
		
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
		setUserPersonalInfo(firstName, lastName, username, "", "");
		
		// Store Google ID token for backend API call
		googleIdToken = idToken;
		
		// Check if account already exists before proceeding
		checkGoogleAccountExistsForRegistration(email, firstName, lastName, "");
	}

	// Store Facebook ID
	private String facebookId;
	
	// Handle Facebook Sign-In success
	public void handleFacebookSignInSuccess(String facebookId, String email, String firstName, String lastName, 
	                                       String displayName, String accessToken) {
		Log.d(TAG, "Handling Facebook Sign-In success");
		Log.d(TAG, "Facebook ID: " + facebookId);
		Log.d(TAG, "Facebook email received: " + (email != null && !email.isEmpty() ? email : "NULL"));
		Log.d(TAG, "Facebook firstName: " + firstName + ", lastName: " + lastName);
		
		// Show loading message
		Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show();
		
		// Mark as Facebook Sign-In user (skip OTP after password creation)
		isFacebookSignIn = true;
		isGoogleSignIn = true; // Also set this for OTP skip logic
		
		// Store Facebook data
		this.facebookId = facebookId;
		googleIdToken = accessToken; // Store Facebook access token
		facebookEmail = email; // Store Facebook email if available
		
		// Use Facebook name if available, otherwise use display name
		String finalFirstName = firstName != null && !firstName.isEmpty() ? firstName : 
			(displayName != null && displayName.contains(" ") ? displayName.split(" ")[0] : displayName);
		String finalLastName = lastName != null && !lastName.isEmpty() ? lastName : 
			(displayName != null && displayName.contains(" ") ? 
				displayName.substring(displayName.indexOf(" ") + 1) : "");
		
		// Generate username from email if available, otherwise from Facebook ID
		String username;
		if (email != null && !email.isEmpty() && email.contains("@")) {
			username = email.substring(0, email.indexOf("@"));
		} else {
			username = "fb_" + facebookId;
		}
		
		// Store personal info from Facebook (will be overridden in Tell Us if user changes it)
		if (finalFirstName != null || finalLastName != null) {
			setUserPersonalInfo(finalFirstName, finalLastName, username, "", "");
		}
		
		// If email is available, set it
		if (email != null && !email.isEmpty()) {
			setUserEmail(email);
		}
		
		// Check if account already exists before proceeding
		checkFacebookAccountExistsForRegistration(accessToken, facebookId, email, finalFirstName, finalLastName, "");
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

	/**
	 * Update location for newly registered user
	 */
	private void updateLocationForNewUser() {
		if (!tokenManager.isLoggedIn()) {
			Log.d(TAG, "User not logged in, skipping location update");
			return;
		}

		userLocationManager.updateUserLocation(new UserLocationManager.LocationUpdateCallback() {
			@Override
			public void onLocationUpdated(String location) {
				Log.d(TAG, "Location updated successfully for new user: " + location);
			}

			@Override
			public void onLocationUpdateFailed(String error) {
				Log.e(TAG, "Location update failed for new user: " + error);
				// Don't show error to user, just log it
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (userLocationManager != null) {
			userLocationManager.cleanup();
		}
	}
}
