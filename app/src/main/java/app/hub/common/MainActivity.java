package app.hub.common;

import app.hub.ForgotPasswordActivity;
import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.FacebookSignInRequest;
import app.hub.api.FacebookSignInResponse;
import app.hub.api.GoogleSignInRequest;
import app.hub.api.GoogleSignInResponse;
import app.hub.api.LoginRequest;
import app.hub.api.LoginResponse;
import app.hub.api.UpdateProfileRequest;
import app.hub.api.UserResponse;
import app.hub.admin.AdminDashboardActivity;
import app.hub.employee.EmployeeDashboardActivity;
import app.hub.manager.ManagerDashboardActivity;
import app.hub.user.DashboardActivity;
import app.hub.util.EmailValidator;
import app.hub.util.TokenManager;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
	private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
	private TokenManager tokenManager;
	private GoogleSignInClient googleSignInClient;
	private CallbackManager facebookCallbackManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        // Install the splash screen
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        // Setup Forgot Password button
        setupForgotPasswordButton();
        
        // Setup Google Sign-In
        setupGoogleSignIn();
        
        // Setup Facebook Login
        setupFacebookLogin();
        
        // Setup social login buttons
        setupSocialLoginButtons();
        
        // Request location permission after splash screen
        getWindow().getDecorView().post(() -> requestLocationPermission());
    }

    private void signOutFromGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut();
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
        // Sign out first to ensure the user can select an account every time
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
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

                if (response.isSuccessful() && response.body() != null) {
                    FacebookSignInResponse signInResponse = response.body();
                    
                    if (signInResponse.isSuccess()) {
                        // Check if it's a new account (we want to block auto-registration during login)
                        String message = signInResponse.getMessage();
                        boolean isNewAccount = false;
                        if (message != null) {
                            String lowerMessage = message.toLowerCase();
                            if (lowerMessage.contains("created") || lowerMessage.contains("registered") || lowerMessage.contains("welcome")) {
                                isNewAccount = true;
                            }
                        }

                        // Also check if user data is missing which might imply failure or new account in some API designs
                        if (isNewAccount || signInResponse.getData() == null || signInResponse.getData().getUser() == null) {
                            runOnUiThread(() -> showError("Account Not Found", 
                                "Account not found. Please register first before signing in with Facebook."));
                            LoginManager.getInstance().logOut();
                            return;
                        }
                        
                        handleFacebookLoginSuccess(signInResponse);
                    } else {
                        // Success = false from API
                        String errorMsg = signInResponse.getMessage() != null ? signInResponse.getMessage() : "Login failed. Please try again.";
                        runOnUiThread(() -> showError("Login Failed", errorMsg));
                        LoginManager.getInstance().logOut();
                    }
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
                                if (jsonObject != null && jsonObject.has("message")) {
                                    errorMsg = jsonObject.get("message").getAsString();
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "Could not parse error JSON");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error response", e);
                    }
                    
                    final String finalErrorMsg = errorMsg;
                    runOnUiThread(() -> showError("Login Failed", finalErrorMsg));
                    LoginManager.getInstance().logOut();
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
                LoginManager.getInstance().logOut();
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
        
        // Save email or connection status
        String email = user.getEmail();
        if (email != null && email.contains("@")) {
            tokenManager.saveEmail(email);
            tokenManager.clearConnectionStatus();
        } else {
            // Facebook user without email - save connection status
            tokenManager.saveConnectionStatus("Facebook connected");
        }
        
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

        // Force immediate token persistence
        tokenManager.forceCommit();

        // Update location for signed-in user and navigate after completion
        updateLocationAndNavigate(user);
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

                // Call backend API to login with Google
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

                if (response.isSuccessful() && response.body() != null) {
                    GoogleSignInResponse signInResponse = response.body();
                    
                    if (signInResponse.isSuccess()) {
                        // Check if it's a new account (we want to block auto-registration during login)
                        String message = signInResponse.getMessage();
                        boolean isNewAccount = false;
                        if (message != null) {
                            String lowerMessage = message.toLowerCase();
                            if (lowerMessage.contains("created") || lowerMessage.contains("registered") || lowerMessage.contains("welcome")) {
                                isNewAccount = true;
                            }
                        }

                        // Check if user data exists
                        if (isNewAccount || signInResponse.getData() == null || signInResponse.getData().getUser() == null) {
                            runOnUiThread(() -> showError("Account Not Found", 
                                "Account not found. Please register first before signing in with Google."));
                            signOutFromGoogle();
                            return;
                        }
                        
                        handleGoogleLoginSuccess(signInResponse);
                    } else {
                        // Success = false from API
                        String errorMsg = signInResponse.getMessage() != null ? signInResponse.getMessage() : "Login failed. Please try again.";
                        runOnUiThread(() -> showError("Login Failed", errorMsg));
                        signOutFromGoogle();
                    }
                } else {
                    String errorMsg = "Login failed. Please try again.";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Google login error: " + errorBody);
                            
                            // Check for specific error message indicating account doesn't exist
                            if (errorBody.toLowerCase().contains("not found") || errorBody.toLowerCase().contains("does not exist")) {
                                errorMsg = "Account not found. Please register first.";
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error response", e);
                    }
                    final String finalErrorMsg = errorMsg;
                    runOnUiThread(() -> showError("Login Failed", finalErrorMsg));
                    signOutFromGoogle();
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
                signOutFromGoogle();
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

        // Force immediate token persistence
        tokenManager.forceCommit();

        // Update location for signed-in user and navigate after completion
        updateLocationAndNavigate(user);
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
						
						// Save email or connection status
						String email = user.getEmail();
						if (email != null && email.contains("@")) {
							tokenManager.saveEmail(email);
							tokenManager.clearConnectionStatus();
						} else if (user.hasFacebookAccount()) {
							tokenManager.saveConnectionStatus("Facebook connected");
						}
						
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

	private void showError(String title, String message) {
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton("OK", null)
			.show();
	}

    private void setupForgotPasswordButton() {
        TextView forgotPasswordBtn = findViewById(R.id.forgotpassbtn);
        if (forgotPasswordBtn != null) {
            forgotPasswordBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }
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

    // Request location permission when app starts
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            
            // Show rationale dialog if user previously denied permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, 
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Access Needed")
                        .setMessage("This app needs location access to detect your current location for registration.")
                        .setPositiveButton("OK", (dialog, which) -> 
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_PERMISSION_REQUEST_CODE))
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                // No explanation needed - request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            // Permission already granted
            detectLocation();
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted");
                detectLocation();
            } else {
                Log.d(TAG, "Location permission denied");
                // Optional: Show toast if user permanently denied
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, 
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "Location permission was denied permanently", 
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // Detect and display current location
    private void detectLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission not granted");
            return;
        }
        
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager != null) {
                // Check if providers are enabled
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                
                if (!isGPSEnabled && !isNetworkEnabled) {
                    Log.d(TAG, "Location providers disabled");
                    return;
                }
                
                Location location = null;
                // Try network provider first (faster)
                if (isNetworkEnabled) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                // Fall back to GPS
                if (location == null && isGPSEnabled) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                
                if (location != null) {
                    Log.d(TAG, "Location detected - Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
                    String city = reverseGeocodeLocation(location.getLatitude(), location.getLongitude());
                    if (city != null && !city.isEmpty()) {
                        // Store in SharedPreferences
                        tokenManager.saveCurrentCity(city);
                        Log.d(TAG, "Detected city: " + city);
                        // Update user's location in database if logged in
                        if (tokenManager.isLoggedIn()) {
                            updateLocation(city);
                        }
                    } else {
                        Log.d(TAG, "Reverse geocoding returned null or empty");
                    }
                } else {
                    Log.d(TAG, "Could not get current location");
                }
            } else {
                Log.e(TAG, "LocationManager is null");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied at runtime", e);
        } catch (Exception e) {
            Log.e(TAG, "Error getting location", e);
        }
    }
    
    private void updateLocation(String location) {
        if (!tokenManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping location update");
            return;
        }
        
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "No token available for location update");
            return;
        }
        
        // Get current user data to preserve other fields
        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> getUserCall = apiService.getUser(token);
        getUserCall.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserResponse.Data currentUser = response.body().getData();
                    if (currentUser != null) {
                        // Update user profile with new location
                        UpdateProfileRequest updateRequest = new UpdateProfileRequest(
                            currentUser.getFirstName(),
                            currentUser.getLastName(),
                            "", // Phone not available in current user data
                            location
                        );
                        
                        Call<UserResponse> updateCall = apiService.updateUser(token, updateRequest);
                        updateCall.enqueue(new Callback<UserResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Location updated successfully: " + location);
                                } else {
                                    Log.e(TAG, "Failed to update location: " + response.code() + " - " + (response.message() != null ? response.message() : "Unknown error"));
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                                Log.e(TAG, "Failed to update location", t);
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "Failed to get current user data: " + response.code() + " - " + (response.message() != null ? response.message() : "Unknown error"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to get current user data", t);
            }
        });
    }

    // Reverse geocode latitude and longitude to get city name
    private String reverseGeocodeLocation(double latitude, double longitude) {
        // Improved coordinate mapping for Metro Manila cities
        try {
            Log.d(TAG, "Reverse geocoding - Lat: " + latitude + ", Lng: " + longitude);
            
            // More precise coordinate ranges for Metro Manila cities
            if (latitude > 0 && longitude > 0) {
                // Manila City (14.5995° N, 120.9842° E)
                if (latitude >= 14.55 && latitude <= 14.65 && longitude >= 120.95 && longitude <= 121.05) {
                    return "Manila City";
                }
                // Makati City (14.5547° N, 121.0244° E)
                else if (latitude >= 14.52 && latitude <= 14.58 && longitude >= 121.00 && longitude <= 121.05) {
                    return "Makati City";
                }
                // Taguig City (14.5176° N, 121.0509° E)
                else if (latitude >= 14.48 && latitude <= 14.55 && longitude >= 121.03 && longitude <= 121.10) {
                    return "Taguig City";
                }
                // Pasay City (14.5378° N, 121.0016° E)
                else if (latitude >= 14.50 && latitude <= 14.55 && longitude >= 120.98 && longitude <= 121.02) {
                    return "Pasay City";
                }
                // Mandaluyong City (14.5832° N, 121.0409° E)
                else if (latitude >= 14.56 && latitude <= 14.60 && longitude >= 121.02 && longitude <= 121.06) {
                    return "Mandaluyong City";
                }
                // San Juan City (14.5995° N, 121.0359° E)
                else if (latitude >= 14.58 && latitude <= 14.62 && longitude >= 121.02 && longitude <= 121.05) {
                    return "San Juan City";
                }
                // Quezon City (14.6312° N, 121.0325° E)
                else if (latitude >= 14.60 && latitude <= 14.70 && longitude >= 121.00 && longitude <= 121.10) {
                    return "Quezon City";
                }
                // Pasig City (14.5832° N, 121.0832° E)
                else if (latitude >= 14.55 && latitude <= 14.60 && longitude >= 121.05 && longitude <= 121.12) {
                    return "Pasig City";
                }
                // Marikina City (14.6488° N, 121.1022° E)
                else if (latitude >= 14.62 && latitude <= 14.68 && longitude >= 121.08 && longitude <= 121.15) {
                    return "Marikina City";
                }
                // Caloocan City (14.7583° N, 120.9869° E)
                else if (latitude >= 14.70 && latitude <= 14.80 && longitude >= 120.95 && longitude <= 121.02) {
                    return "Caloocan City";
                }
                // Valenzuela City (14.6942° N, 120.9683° E)
                else if (latitude >= 14.65 && latitude <= 14.72 && longitude >= 120.93 && longitude <= 121.00) {
                    return "Valenzuela City";
                }
                // Las Piñas City (14.4496° N, 120.9986° E)
                else if (latitude >= 14.42 && latitude <= 14.48 && longitude >= 120.97 && longitude <= 121.02) {
                    return "Las Piñas City";
                }
                // Parañaque City (14.4611° N, 121.0176° E)
                else if (latitude >= 14.43 && latitude <= 14.48 && longitude >= 121.00 && longitude <= 121.04) {
                    return "Parañaque City";
                }
                // Muntinlupa City (14.3909° N, 121.0479° E)
                else if (latitude >= 14.35 && latitude <= 14.42 && longitude >= 121.02 && longitude <= 121.08) {
                    return "Muntinlupa City";
                }
                // Navotas City (14.6488° N, 120.9489° E)
                else if (latitude >= 14.62 && latitude <= 14.67 && longitude >= 120.92 && longitude <= 120.97) {
                    return "Navotas City";
                }
                // Malabon City (14.6686° N, 120.9489° E)
                else if (latitude >= 14.64 && latitude <= 14.69 && longitude >= 120.92 && longitude <= 120.97) {
                    return "Malabon City";
                }
                
                // Metro Manila area fallback
                if (latitude >= 14.3 && latitude <= 14.8 && longitude >= 120.9 && longitude <= 121.2) {
                    return "Metro Manila Area";
                }
            }
            
            Log.d(TAG, "Using default location: Philippines");
            return "Philippines";  // Default fallback
        } catch (Exception e) {
            Log.e(TAG, "Reverse geocoding failed", e);
            return "Metro Manila Area"; // Default fallback
        }
    }

    /**
     * Update user's location and then navigate to dashboard
     * Ensures location update completes before navigation
     */
    private void updateLocationAndNavigate(Object user) {
        // First, try to get cached location
        String detectedLocation = tokenManager.getCurrentCity();
        
        if (detectedLocation != null && !detectedLocation.isEmpty()) {
            Log.d(TAG, "Using cached location: " + detectedLocation);
            // Update location with callback to navigate after completion
            updateLocationWithCallback(detectedLocation, () -> navigateToDashboard(user));
        } else {
            Log.d(TAG, "No cached location, detecting now...");
            // Detect location and update, then navigate
            detectLocationWithCallback(() -> navigateToDashboard(user));
        }
    }

    /**
     * Update location with callback after completion
     */
    private void updateLocationWithCallback(String location, Runnable onComplete) {
        if (!tokenManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping location update");
            onComplete.run();
            return;
        }
        
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "No token available for location update");
            onComplete.run();
            return;
        }
        
        Log.d(TAG, "Updating location to: " + location);
        
        // Get current user data to preserve other fields
        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> getUserCall = apiService.getUser(token);
        getUserCall.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserResponse.Data currentUser = response.body().getData();
                    if (currentUser != null) {
                        // Update user profile with new location
                        UpdateProfileRequest updateRequest = new UpdateProfileRequest(
                            currentUser.getFirstName(),
                            currentUser.getLastName(),
                            "", // Phone not available in current user data
                            location
                        );
                        
                        Call<UserResponse> updateCall = apiService.updateUser(token, updateRequest);
                        updateCall.enqueue(new Callback<UserResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Location updated successfully: " + location);
                                } else {
                                    Log.e(TAG, "Failed to update location: " + response.code() + " - " + (response.message() != null ? response.message() : "Unknown error"));
                                }
                                // Always proceed to navigation regardless of location update success
                                onComplete.run();
                            }

                            @Override
                            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                                Log.e(TAG, "Failed to update location", t);
                                // Still navigate even if location update fails
                                onComplete.run();
                            }
                        });
                    } else {
                        Log.e(TAG, "Current user data is null");
                        onComplete.run();
                    }
                } else {
                    Log.e(TAG, "Failed to get current user data: " + response.code() + " - " + (response.message() != null ? response.message() : "Unknown error"));
                    onComplete.run();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to get current user data", t);
                onComplete.run();
            }
        });
    }

    /**
     * Detect location with callback after completion
     */
    private void detectLocationWithCallback(Runnable onComplete) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission not granted, proceeding without location update");
            onComplete.run();
            return;
        }
        
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager != null) {
                // Check if providers are enabled
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                
                if (!isGPSEnabled && !isNetworkEnabled) {
                    Log.d(TAG, "Location providers disabled, proceeding without location update");
                    onComplete.run();
                    return;
                }
                
                Location location = null;
                // Try network provider first (faster)
                if (isNetworkEnabled) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                // Fall back to GPS
                if (location == null && isGPSEnabled) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                
                if (location != null) {
                    Log.d(TAG, "Location detected - Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
                    String city = reverseGeocodeLocation(location.getLatitude(), location.getLongitude());
                    if (city != null && !city.isEmpty()) {
                        // Store in SharedPreferences
                        tokenManager.saveCurrentCity(city);
                        Log.d(TAG, "Detected city: " + city);
                        // Update user's location in database
                        updateLocationWithCallback(city, onComplete);
                    } else {
                        Log.d(TAG, "Reverse geocoding returned null or empty, proceeding without location update");
                        onComplete.run();
                    }
                } else {
                    Log.d(TAG, "Could not get current location, proceeding without location update");
                    onComplete.run();
                }
            } else {
                Log.e(TAG, "LocationManager is null");
                onComplete.run();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied at runtime", e);
            onComplete.run();
        } catch (Exception e) {
            Log.e(TAG, "Error getting location", e);
            onComplete.run();
        }
    }

    /**
     * Navigate to appropriate dashboard based on user role
     */
    private void navigateToDashboard(Object user) {
        runOnUiThread(() -> {
            final Intent intent;
            
            // Handle both GoogleSignInResponse.User and FacebookSignInResponse.User
            String role = null;
            String email = null;
            
            if (user instanceof GoogleSignInResponse.User) {
                GoogleSignInResponse.User googleUser = (GoogleSignInResponse.User) user;
                role = googleUser.getRole();
                email = googleUser.getEmail();
            } else if (user instanceof FacebookSignInResponse.User) {
                FacebookSignInResponse.User fbUser = (FacebookSignInResponse.User) user;
                role = fbUser.getRole();
                email = fbUser.getEmail();
            }
            
            if ("admin".equals(role)) {
                intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
            } else if ("manager".equals(role)) {
                intent = new Intent(MainActivity.this, ManagerDashboardActivity.class);
            } else if ("employee".equals(role) || "staff".equals(role)) {
                intent = new Intent(MainActivity.this, EmployeeDashboardActivity.class);
            } else {
                intent = new Intent(MainActivity.this, DashboardActivity.class);
                if (email != null) {
                    intent.putExtra(DashboardActivity.EXTRA_EMAIL, email);
                }
            }
            
            Log.d(TAG, "Navigating to dashboard for role: " + role);
            startActivity(intent);
            finish();
        });
    }
}
