package app.hub.common;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.FacebookSignInRequest;
import app.hub.api.FacebookSignInResponse;
import app.hub.api.GoogleSignInRequest;
import app.hub.api.GoogleSignInResponse;
import app.hub.api.LoginRequest;
import app.hub.api.LoginResponse;
import app.hub.admin.AdminDashboardActivity;
import app.hub.employee.EmployeeDashboardActivity;
import app.hub.manager.ManagerDashboardActivity;
import app.hub.user.DashboardActivity;
import app.hub.util.EmailValidator;
import app.hub.util.TokenManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "MainActivity";
	private static final int RC_SIGN_IN = 9001;
	private TokenManager tokenManager;
	private GoogleSignInClient googleSignInClient;
	private CallbackManager facebookCallbackManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        // Install the splash screen
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenManager = new TokenManager(this);

        // Check if already logged in
        if (tokenManager.isLoggedIn()) {
            final Intent intent;
            if ("admin".equals(tokenManager.getRole())) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else if ("manager".equals(tokenManager.getRole())) {
                intent = new Intent(this, ManagerDashboardActivity.class);
            } else if ("employee".equals(tokenManager.getRole()) || "staff".equals(tokenManager.getRole())) {
                intent = new Intent(this, EmployeeDashboardActivity.class);
            } else {
                intent = new Intent(this, DashboardActivity.class);
                intent.putExtra(DashboardActivity.EXTRA_EMAIL, tokenManager.getEmail());
            }
            startActivity(intent);
            finish();
            return;
        }

        TextInputEditText emailInput = findViewById(R.id.Email_val);
        TextInputEditText passwordInput = findViewById(R.id.Pass_val);
        TextInputLayout emailInputLayout = findViewById(R.id.emailInputLayout);
        MaterialButton loginButton = findViewById(R.id.loginButton);
        TextView registerButton = findViewById(R.id.registerButton);

        // Real-time email validation
        if (emailInput != null) {
            emailInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
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

                    // Allow static admin and manager usernames without validation
                    if ("Admin1204".equals(email) || "Manager".equals(email)) {
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
                        if (emailInputLayout != null) {
                            emailInputLayout.setError(null);
                        }
                    }
                }
            });
        }

        if (registerButton != null) {
            registerButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
            });
        }

        // Handle Enter key press on password field to trigger login
        if (passwordInput != null) {
            passwordInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                    performLogin();
                    return true;
                }
                // Also handle physical Enter key press
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    performLogin();
                    return true;
                }
                return false;
            });
        }

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> performLogin());
        }

        // Setup Google Sign-In
        setupGoogleSignIn();
        
        // Setup Facebook Login
        setupFacebookLogin();
        
        // Setup social login buttons
        setupSocialLoginButtons();
    }

    private void setupFacebookLogin() {
        facebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
            new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    AccessToken accessToken = loginResult.getAccessToken();
                    Log.d(TAG, "Facebook login successful");
                    
                    // Get user info from Facebook Graph API
                    GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        (object, response) -> {
                            try {
                                String email = object.optString("email");
                                String firstName = object.optString("first_name");
                                String lastName = object.optString("last_name");
                                String name = object.optString("name");
                                String id = object.optString("id");
                                
                                Log.d(TAG, "Facebook user info - ID: " + id + ", Email: " + (email != null && !email.isEmpty() ? email : "NULL") + ", Name: " + name);
                                
                                // Call backend API to login/register with Facebook (pass Facebook ID for pure FB auth)
                                loginWithFacebook(accessToken.getToken(), id, email, firstName, lastName);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing Facebook user info", e);
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error getting Facebook user info", Toast.LENGTH_SHORT).show());
                            }
                        });
                    
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,first_name,last_name");
                    request.setParameters(parameters);
                    request.executeAsync();
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "Facebook login cancelled");
                    Toast.makeText(MainActivity.this, "Facebook login was cancelled", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException exception) {
                    Log.e(TAG, "Facebook login error: " + exception.getMessage(), exception);
                    String errorMsg = "Facebook login failed";
                    if (exception.getMessage() != null) {
                        if (exception.getMessage().contains("CONNECTION_FAILURE")) {
                            errorMsg = "Network error. Please check your connection.";
                        } else if (exception.getMessage().contains("INVALID_APP_ID")) {
                            errorMsg = "Facebook login not configured. Please contact support.";
                        }
                    }
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupSocialLoginButtons() {
        // Facebook button
        Button facebookButton = findViewById(R.id.btnFacebook);
        if (facebookButton != null) {
            facebookButton.setOnClickListener(v -> {
                signInWithFacebook();
            });
        }

        // Google Sign-In button
        Button googleButton = findViewById(R.id.btnGoogle);
        if (googleButton != null) {
            googleButton.setOnClickListener(v -> {
                signInWithGoogle();
            });
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Handle Facebook callback
        if (facebookCallbackManager != null) {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void signInWithFacebook() {
        // Use only public_profile permission - email is automatically included via Graph API
        LoginManager.getInstance().logInWithReadPermissions(this, 
            java.util.Arrays.asList("public_profile"));
    }

    private void loginWithFacebook(String accessToken, String facebookId, String email, String firstName, String lastName) {
        // Validate Facebook ID (required for pure FB auth)
        if (facebookId == null || facebookId.isEmpty()) {
            Log.e(TAG, "Facebook login failed: Facebook ID is required but not available");
            showError("Login Failed", "Facebook authentication error. Please try again.");
            return;
        }
        
        MaterialButton loginButton = findViewById(R.id.loginButton);
        if (loginButton != null) {
            loginButton.setEnabled(false);
            loginButton.setText("Logging in...");
        }

        Log.d(TAG, "Attempting Facebook login with:");
        Log.d(TAG, "  - Facebook ID: " + facebookId);
        Log.d(TAG, "  - Email: " + (email != null && !email.isEmpty() ? email : "NULL (pure FB auth)"));
        Log.d(TAG, "  - First Name: " + firstName);
        Log.d(TAG, "  - Last Name: " + lastName);
        Log.d(TAG, "  - Access Token: " + (accessToken != null && !accessToken.isEmpty() ? "Present" : "Missing"));

        ApiService apiService = ApiClient.getApiService();
        FacebookSignInRequest request = new FacebookSignInRequest(
            accessToken != null ? accessToken : "",
            facebookId,
            email != null ? email : "", // Email can be empty for pure FB auth
            firstName != null ? firstName : "",
            lastName != null ? lastName : "",
            "" // No phone on login
        );

        Call<FacebookSignInResponse> call = apiService.facebookSignIn(request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<FacebookSignInResponse> call, @NonNull Response<FacebookSignInResponse> response) {
                runOnUiThread(() -> {
                    if (loginButton != null) {
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");
                    }
                });

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    handleFacebookLoginSuccess(response.body());
                } else {
                    String errorMsg = "Login failed. Please try again.";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Facebook login error response (" + response.code() + "): " + errorBody);
                            
                            // Try to parse error message from response
                            try {
                                Gson gson = new Gson();
                                JsonObject jsonObject = gson.fromJson(errorBody, JsonObject.class);
                                if (jsonObject != null) {
                                    if (jsonObject.has("message") && jsonObject.get("message").isJsonPrimitive()) {
                                        errorMsg = jsonObject.get("message").getAsString();
                                    } else if (jsonObject.has("errors") && jsonObject.get("errors").isJsonObject()) {
                                        // Get first validation error
                                        JsonObject errorsObj = jsonObject.getAsJsonObject("errors");
                                        if (errorsObj.has("email") && errorsObj.get("email").isJsonArray()) {
                                            errorMsg = errorsObj.get("email").getAsJsonArray().get(0).getAsString();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "Could not parse error JSON, using default message");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error response", e);
                    }
                    
                    // Provide more specific error messages based on status code
                    if (response.code() == 422) {
                        errorMsg = "Invalid email or Facebook account. Please register first or use email/password login.";
                    } else if (response.code() == 401) {
                        errorMsg = "Authentication failed. Please try again.";
                    } else if (response.code() == 500) {
                        errorMsg = "Server error. Please try again later.";
                    }
                    
                    final String finalErrorMsg = errorMsg;
                    runOnUiThread(() -> showError("Login Failed", finalErrorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<FacebookSignInResponse> call, @NonNull Throwable t) {
                runOnUiThread(() -> {
                    if (loginButton != null) {
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");
                    }
                });
                Log.e(TAG, "Error logging in with Facebook: " + t.getMessage(), t);
                String errorMsg = getConnectionErrorMessage(t);
                runOnUiThread(() -> showError("Connection Error", errorMsg));
            }
        });
    }

    private void handleFacebookLoginSuccess(FacebookSignInResponse response) {
        if (response.getData() == null || response.getData().getUser() == null) {
            showError("Login Failed", "Invalid response from server.");
            return;
        }

        // Save token and user data
        FacebookSignInResponse.User user = response.getData().getUser();
        tokenManager.saveToken("Bearer " + response.getData().getToken());
        tokenManager.saveEmail(user.getEmail());
        tokenManager.saveRole(user.getRole());

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
        }

        // Navigate to dashboard
        runOnUiThread(() -> {
            final Intent intent;
            if ("admin".equals(user.getRole())) {
                intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
            } else if ("manager".equals(user.getRole())) {
                intent = new Intent(MainActivity.this, ManagerDashboardActivity.class);
            } else if ("employee".equals(user.getRole()) || "staff".equals(user.getRole())) {
                intent = new Intent(MainActivity.this, EmployeeDashboardActivity.class);
            } else {
                intent = new Intent(MainActivity.this, DashboardActivity.class);
                intent.putExtra(DashboardActivity.EXTRA_EMAIL, user.getEmail());
            }
            startActivity(intent);
            finish();
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String email = account.getEmail();
                String displayName = account.getDisplayName();
                String givenName = account.getGivenName();
                String familyName = account.getFamilyName();
                String idToken = account.getIdToken();

                Log.d(TAG, "Google Sign-In successful - Email: " + email);

                // Call backend API to login/register with Google
                loginWithGoogle(email, givenName, familyName, idToken);
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google Sign-In failed: " + e.getStatusCode(), e);
            String errorMessage = "Google Sign-In failed";
            
            switch (e.getStatusCode()) {
                case 10: // DEVELOPER_ERROR
                    errorMessage = "Google Sign-In not configured. Please contact support.";
                    break;
                case 12501: // SIGN_IN_CANCELLED
                    errorMessage = "Sign-in was cancelled";
                    break;
                case 7: // NETWORK_ERROR
                    errorMessage = "Network error. Please check your connection.";
                    break;
                case 8: // INTERNAL_ERROR
                    errorMessage = "Google Sign-In error. Please try again.";
                    break;
                default:
                    errorMessage = "Google Sign-In failed. Error code: " + e.getStatusCode();
                    break;
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void loginWithGoogle(String email, String firstName, String lastName, String idToken) {
        MaterialButton loginButton = findViewById(R.id.loginButton);
        if (loginButton != null) {
            loginButton.setEnabled(false);
            loginButton.setText("Logging in...");
        }

        ApiService apiService = ApiClient.getApiService();
        GoogleSignInRequest request = new GoogleSignInRequest(
            idToken != null ? idToken : "",
            email,
            firstName != null ? firstName : "",
            lastName != null ? lastName : "",
            "" // No phone on login
        );

        Call<GoogleSignInResponse> call = apiService.googleSignIn(request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GoogleSignInResponse> call, @NonNull Response<GoogleSignInResponse> response) {
                runOnUiThread(() -> {
                    if (loginButton != null) {
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");
                    }
                });

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    handleGoogleLoginSuccess(response.body());
                } else {
                    String errorMsg = "Login failed. Please try again.";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Google login error: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error response", e);
                    }
                    final String finalErrorMsg = errorMsg;
                    runOnUiThread(() -> showError("Login Failed", finalErrorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<GoogleSignInResponse> call, @NonNull Throwable t) {
                runOnUiThread(() -> {
                    if (loginButton != null) {
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");
                    }
                });
                Log.e(TAG, "Error logging in with Google: " + t.getMessage(), t);
                runOnUiThread(() -> showError("Connection Error", 
                    "Failed to login. Please check your connection and try again."));
            }
        });
    }

    private void handleGoogleLoginSuccess(GoogleSignInResponse response) {
        if (response.getData() == null || response.getData().getUser() == null) {
            showError("Login Failed", "Invalid response from server.");
            return;
        }

        // Save token and user data
        GoogleSignInResponse.User user = response.getData().getUser();
        tokenManager.saveToken("Bearer " + response.getData().getToken());
        tokenManager.saveEmail(user.getEmail());
        tokenManager.saveRole(user.getRole());

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
        }

        // Navigate to dashboard
        runOnUiThread(() -> {
            final Intent intent;
            if ("admin".equals(user.getRole())) {
                intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
            } else if ("manager".equals(user.getRole())) {
                intent = new Intent(MainActivity.this, ManagerDashboardActivity.class);
            } else if ("employee".equals(user.getRole()) || "staff".equals(user.getRole())) {
                intent = new Intent(MainActivity.this, EmployeeDashboardActivity.class);
            } else {
                intent = new Intent(MainActivity.this, DashboardActivity.class);
                intent.putExtra(DashboardActivity.EXTRA_EMAIL, user.getEmail());
            }
            startActivity(intent);
            finish();
        });
    }

    /**
     * Performs login action - validates inputs and calls login method
     */
    private void performLogin() {
        TextInputEditText emailInput = findViewById(R.id.Email_val);
        TextInputEditText passwordInput = findViewById(R.id.Pass_val);
        
        String email = emailInput != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput != null ? passwordInput.getText().toString() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        login(email, password);
    }

	private void login(String email, String password) {
		// Check for static admin credentials
		if ("Admin1204".equals(email.trim()) && "@Admin1234".equals(password)) {
			handleStaticAdminLogin();
			return;
		}

		// Check for static manager credentials
		if ("Manager".equals(email.trim()) && "Management1234".equals(password)) {
			handleStaticManagerLogin();
			return;
		}

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
						LoginResponse.User user = loginResponse.getData().getUser();
						tokenManager.saveToken("Bearer " + loginResponse.getData().getToken());
						tokenManager.saveEmail(user.getEmail());
                        tokenManager.saveRole(user.getRole());
						
						// Get name - prefer name field, fallback to firstName + lastName
						String userName = user.getName();
						if (userName == null || userName.trim().isEmpty()) {
							String firstName = user.getFirstName();
							String lastName = user.getLastName();
							
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
							
							userName = nameBuilder.toString();
						} else {
							userName = userName.trim();
						}
						
						if (userName != null && !userName.isEmpty()) {
							tokenManager.saveName(userName);
						}

						// Navigate to dashboard
						runOnUiThread(() -> {
                            final Intent intent;
                            if ("admin".equals(user.getRole())) {
                                intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                            } else if ("manager".equals(user.getRole())) {
                                intent = new Intent(MainActivity.this, ManagerDashboardActivity.class);
                            } else if ("employee".equals(user.getRole()) || "staff".equals(user.getRole())) {
                                intent = new Intent(MainActivity.this, EmployeeDashboardActivity.class);
                            } else {
                                intent = new Intent(MainActivity.this, DashboardActivity.class);
                                intent.putExtra(DashboardActivity.EXTRA_EMAIL, user.getEmail());
                            }
                            startActivity(intent);
                            finish();
						});
					} else {
						final String message = loginResponse != null && loginResponse.getMessage() != null 
							? loginResponse.getMessage() : "Login failed";
						runOnUiThread(() -> showError("Login Failed", message));
					}
				} else {
					// Handle error response - try to parse error body
					String errorMsg = parseErrorResponse(response);
					if (errorMsg == null || errorMsg.isEmpty()) {
						if (response.code() == 401) {
							errorMsg = "Invalid email or password";
						} else if (response.code() == 422) {
							errorMsg = "Invalid input. Please check your email and password.";
						} else if (response.code() == 500) {
							errorMsg = "Server error. Please try again later.";
						} else {
							errorMsg = "Login failed. Please try again.";
						}
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

				// Check if it's a JSON parsing error
				String errorMsg;
				if (t.getMessage() != null && t.getMessage().contains("Expected BEGIN_OBJECT but was STRING")) {
					errorMsg = "Server returned an unexpected response format.\n\n" +
						"Please check:\n" +
						"1. Laravel server is running correctly\n" +
						"2. Server is returning JSON responses\n" +
						"3. Check server logs for errors";
				} else {
					errorMsg = getConnectionErrorMessage(t);
				}
				runOnUiThread(() -> showError("Connection Error", errorMsg));
			}
		});
	}

	private void handleStaticAdminLogin() {
		final MaterialButton loginButton = findViewById(R.id.loginButton);
		if (loginButton != null) {
			loginButton.setEnabled(false);
			loginButton.setText("Logging in...");
		}

		// Save admin credentials to TokenManager
		tokenManager.saveToken("Bearer static_admin_token");
		tokenManager.saveEmail("admin1204@ashcol.com");
		tokenManager.saveName("Admin");
		tokenManager.saveRole("admin");

		// Navigate to admin dashboard
		Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
		startActivity(intent);
		finish();
	}

	private void handleStaticManagerLogin() {
		final MaterialButton loginButton = findViewById(R.id.loginButton);
		if (loginButton != null) {
			loginButton.setEnabled(false);
			loginButton.setText("Logging in...");
		}

		// Save manager credentials to TokenManager
		tokenManager.saveToken("Bearer static_manager_token");
		tokenManager.saveEmail("manager@ashcol.com");
		tokenManager.saveName("Manager");
		tokenManager.saveRole("manager");

		// Navigate to manager dashboard
		Intent intent = new Intent(MainActivity.this, ManagerDashboardActivity.class);
		startActivity(intent);
		finish();
	}

	private void showError(String title, String message) {
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton("OK", null)
			.show();
	}

	/**
	 * Parse error response from server
	 * Handles cases where server returns JSON error or plain text
	 */
	private String parseErrorResponse(Response<LoginResponse> response) {
		ResponseBody errorBody = response.errorBody();
		if (errorBody == null) {
			return null;
		}

		try {
			String errorString = errorBody.string();
			if (errorString == null || errorString.isEmpty()) {
				return null;
			}

			// Try to parse as JSON first
			try {
				// Use Gson to parse the JSON string (compatible with all Gson versions)
				Gson gson = new Gson();
				JsonObject jsonObject = gson.fromJson(errorString, JsonObject.class);
				
				if (jsonObject != null) {
					// Try to get message field
					if (jsonObject.has("message") && jsonObject.get("message").isJsonPrimitive()) {
						String message = jsonObject.get("message").getAsString();
						if (message != null && !message.isEmpty()) {
							return message;
						}
					}
					
					// Try to get error field
					if (jsonObject.has("error") && jsonObject.get("error").isJsonPrimitive()) {
						String error = jsonObject.get("error").getAsString();
						if (error != null && !error.isEmpty()) {
							return error;
						}
					}
					
					// Try to get errors object (validation errors)
					if (jsonObject.has("errors") && jsonObject.get("errors").isJsonObject()) {
						StringBuilder errorMessages = new StringBuilder();
						JsonObject errorsObj = jsonObject.getAsJsonObject("errors");
						errorsObj.entrySet().forEach(entry -> {
							if (errorMessages.length() > 0) {
								errorMessages.append("\n");
							}
							try {
								if (entry.getValue().isJsonArray() && entry.getValue().getAsJsonArray().size() > 0) {
									errorMessages.append(entry.getValue().getAsJsonArray().get(0).getAsString());
								} else if (entry.getValue().isJsonPrimitive()) {
									errorMessages.append(entry.getValue().getAsString());
								}
							} catch (Exception e) {
								// Skip invalid error entries
							}
						});
						if (errorMessages.length() > 0) {
							return errorMessages.toString();
						}
					}
				}
			} catch (Exception e) {
				// Not JSON, treat as plain text
				Log.d("MainActivity", "Error response is not JSON: " + e.getMessage());
			}

			// If it's not JSON or doesn't have expected fields, check if it's HTML
			if (errorString.trim().startsWith("<!DOCTYPE") || errorString.trim().startsWith("<html>")) {
				return "Server returned an HTML error page. Please check server logs.";
			}

			// Return plain text (truncate if too long)
			if (errorString.length() > 200) {
				return errorString.substring(0, 200) + "...";
			}
			return errorString;

		} catch (IOException e) {
			Log.e("MainActivity", "Error reading error response: " + e.getMessage());
			return null;
		}
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
			errorMsg.append("Trying to reach: ").append(baseUrl).append("/api/v1/login\n\n");
			errorMsg.append("Please check:\n");
			errorMsg.append("1. Laravel server is running: php artisan serve\n");
			errorMsg.append("2. Server is on port 8000 (default)\n");
			errorMsg.append("3. For emulator, use: http://10.0.2.2:8000\n");
			errorMsg.append("4. For physical device, use your computer's IP\n");
			errorMsg.append("5. Check ApiClient.java BASE_URL setting\n");
			errorMsg.append("6. Verify network_security_config.xml allows cleartext\n\n");
			errorMsg.append("Test in browser: ").append(baseUrl).append("/api/v1/login");
		} else if (message.contains("timeout")) {
			errorMsg.append("⏱ Connection timeout\n\n");
			errorMsg.append("Server may be slow or unreachable.\n");
			errorMsg.append("Check if Laravel server is running and accessible.");
		} else if (message.contains("Connection refused")) {
			errorMsg.append("🔌 Connection refused\n\n");
			errorMsg.append("Server is not running or not accessible.\n ");
			errorMsg.append("Please start Laravel server: php artisan serve\n");
			errorMsg.append("Expected URL: ").append(baseUrl);
		} else {
			errorMsg.append("❌ Connection error\n\n");
			errorMsg.append("Details: ").append(message);
		}

		return errorMsg.toString();
	}
}
