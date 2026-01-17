package app.hub.common;

import app.hub.R;
import app.hub.util.TokenManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

	// Views for activity_register.xml (Tell us step)
	private View fragmentContainer;
	private ConstraintLayout templateLayout;
	private TextInputLayout firstNameInputLayout, lastNameInputLayout, usernameInputLayout, phoneInputLayout;
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

		firstNameInputLayout = findViewById(R.id.firstNameInputLayout);
		lastNameInputLayout = findViewById(R.id.lastNameInputLayout);
		usernameInputLayout = findViewById(R.id.usernameInputLayout);
		phoneInputLayout = findViewById(R.id.phoneInputLayout);

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

	// Validation methods
	private void validateFirstName(String firstName) {
		if (fval == null) return;
		if (firstName.matches(".*\\d+.*")) {
			fval.setText("Name no numbers");
			fval.setVisibility(View.VISIBLE);
		} else if (!firstName.isEmpty() && firstName.length() < 2) {
			fval.setText("Name too short");
			fval.setVisibility(View.VISIBLE);
		} else {
			fval.setVisibility(View.GONE);
		}
	}

	private void validateLastName(String lastName) {
		if (lval == null) return;
		if (lastName.matches(".*\\d+.*")) {
			lval.setText("Name no numbers");
			lval.setVisibility(View.VISIBLE);
		} else {
			lval.setVisibility(View.GONE);
		}
	}

	private void validateUsername(String username) {
		if (uval == null) return;
		if (username.contains(" ")) {
			uval.setText("Username no spaces");
			uval.setVisibility(View.VISIBLE);
		} else if (!username.isEmpty() && username.length() < 4) {
			uval.setText("Username min 4 chars");
			uval.setVisibility(View.VISIBLE);
		} else {
			uval.setVisibility(View.GONE);
		}
	}

	private void validatePhone(String phone) {
		if (phoneVal == null) return;
		if (phone.isEmpty()) {
			phoneVal.setVisibility(View.GONE);
			return;
		}
		String digitOnly = phone.replaceAll("[^0-9]", "");
		if (digitOnly.length() >= 10 && digitOnly.length() <= 15) {
			phoneVal.setVisibility(View.GONE);
		} else {
			phoneVal.setText("Phone 10 digits min");
			phoneVal.setVisibility(View.VISIBLE);
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
			if (fval != null) {
				fval.setText("First name required");
				fval.setVisibility(View.VISIBLE);
			}
			isValid = false;
		}

		if (lastName.isEmpty()) {
			if (lval != null) {
				lval.setText("Last name required");
				lval.setVisibility(View.VISIBLE);
			}
			isValid = false;
		}

		if (username.isEmpty()) {
			if (uval != null) {
				uval.setText("Username required");
				uval.setVisibility(View.VISIBLE);
			}
			isValid = false;
		} else if (username.length() < 4) {
			if (uval != null) {
				uval.setText("Username min 4 chars");
				uval.setVisibility(View.VISIBLE);
			}
			isValid = false;
		}

		if (phone.isEmpty()) {
			if (phoneVal != null) {
				phoneVal.setText("Phone required");
				phoneVal.setVisibility(View.VISIBLE);
			}
			isValid = false;
		} else {
			String digitOnly = phone.replaceAll("[^0-9]", "");
			if (digitOnly.length() < 10) {
				if (phoneVal != null) {
					phoneVal.setText("Phone 10 digits min");
					phoneVal.setVisibility(View.VISIBLE);
				}
				isValid = false;
			}
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
		// Hide template layout, show fragment container for next step
		if (templateLayout != null) {
			templateLayout.setVisibility(View.GONE);
		}
		if (fragmentContainer != null) {
			fragmentContainer.setVisibility(View.VISIBLE);
		}
		showCreatePasswordFragment();
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
		// TODO: Implement OTP verification UI
		Toast.makeText(this, "Show OTP verification", Toast.LENGTH_SHORT).show();
	}

	// Step 6: Account created - navigate to dashboard
	public void showAccountCreated() {
		Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();
		// TODO: Navigate to appropriate dashboard based on user role
		finish();
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

	// Getters for registration data (used by fragments)
	public String getUserEmail() {
		return userEmail;
	}

	public String getUserFirstName() {
		return userFirstName;
	}

	public String getUserLastName() {
		return userLastName;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public String getUserPassword() {
		return userPassword;
	}

	@Override
	public void onBackPressed() {
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
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}
}
