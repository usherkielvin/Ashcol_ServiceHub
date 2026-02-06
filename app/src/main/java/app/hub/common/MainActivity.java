package app.hub.common;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import app.hub.ForgotPasswordActivity;
import app.hub.R;
import app.hub.admin.AdminDashboardActivity;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.GoogleSignInRequest;
import app.hub.api.GoogleSignInResponse;
import app.hub.api.LoginRequest;
import app.hub.api.LoginResponse;
import app.hub.employee.EmployeeDashboardActivity;
import app.hub.manager.ManagerDashboardActivity;
import app.hub.user.DashboardActivity;
import app.hub.util.EmailValidator;
import app.hub.util.FCMTokenHelper;
import app.hub.util.TokenManager;
import app.hub.util.UserLocationManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private TokenManager tokenManager;
    private GoogleSignInClient googleSignInClient;
    private UserLocationManager userLocationManager;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install the splash screen
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(this);
        userLocationManager = new UserLocationManager(this);

        // Register Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleGoogleSignInResult(task);
                    }
                });

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
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
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

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupSocialLoginButtons() {
        Button googleButton = findViewById(R.id.btnGoogle);
        if (googleButton != null) {
            googleButton.setOnClickListener(v -> signInWithGoogle());
        }
    }

    private void signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String email = account.getEmail();
                String givenName = account.getGivenName();
                String familyName = account.getFamilyName();
                String idToken = account.getIdToken();

                Log.d(TAG, "Google Sign-In successful - Email: " + email);

                // Call backend API to login with Google
                loginWithGoogle(email, givenName, familyName, idToken);
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google Sign-In failed: " + e.getStatusCode(), e);
            String errorMessage = switch (e.getStatusCode()) {
                case 10 -> "Google Sign-In not configured. Please contact support."; // DEVELOPER_ERROR
                case 12501 -> "Sign-in was cancelled"; // SIGN_IN_CANCELLED
                case 7 -> "Network error. Please check your connection."; // NETWORK_ERROR
                case 8 -> "Google Sign-In error. Please try again."; // INTERNAL_ERROR
                default -> "Google Sign-In failed. Error code: " + e.getStatusCode();
            };
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
            public void onResponse(@NonNull Call<GoogleSignInResponse> call,
                    @NonNull Response<GoogleSignInResponse> response) {
                runOnUiThread(() -> {
                    if (loginButton != null) {
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");
                    }
                });

                if (response.isSuccessful() && response.body() != null) {
                    GoogleSignInResponse signInResponse = response.body();

                    if (signInResponse.isSuccess()) {
                        handleGoogleLoginSuccess(signInResponse);
                    } else {
                        // API returned success: false
                        String errorMsg = signInResponse.getMessage() != null ? signInResponse.getMessage()
                                : "Account not found. Please register first.";
                        runOnUiThread(() -> showError("Login Failed", errorMsg));
                        signOutFromGoogle();
                    }
                } else {
                    String errorMsg = "Login failed. Please try again.";
                    try (ResponseBody body = response.errorBody()) {
                        if (body != null) {
                            String errorBody = body.string();
                            Log.e(TAG, "Google login error: " + errorBody);

                            String lowerError = errorBody.toLowerCase();
                            if (lowerError.contains("not found") || lowerError.contains("does not exist")
                                    || lowerError.contains("account")) {
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
        if (user.getBranch() != null) {
            tokenManager.saveUserBranch(user.getBranch());
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
        }

        // Force immediate token persistence
        tokenManager.forceCommit();

        // Register FCM token for push notifications
        FCMTokenHelper.registerTokenWithBackend(MainActivity.this);

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
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
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
                        }

                        tokenManager.saveRole(user.getRole());
                        if (user.getBranch() != null) {
                            tokenManager.saveUserBranch(user.getBranch());
                        }

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

                        if (!userName.isEmpty()) {
                            tokenManager.saveName(userName);
                        }

                        // Register FCM token for push notifications
                        FCMTokenHelper.registerTokenWithBackend(MainActivity.this);

                        // Navigate to dashboard
                        updateLocationAndNavigate(user.getRole(), user.getEmail());
                    } else {
                        final String message = loginResponse.getMessage() != null
                                ? loginResponse.getMessage()
                                : "Login failed";
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
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                runOnUiThread(() -> {
                    if (loginButton != null) {
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");
                    }
                });

                String errorMsg;
                if (t.getMessage() != null && t.getMessage().contains("Expected BEGIN_OBJECT but was STRING")) {
                    errorMsg = """
                            Server returned an unexpected response format.

                            Please check:
                            1. Laravel server is running correctly
                            2. Server is returning JSON responses
                            3. Check server logs for errors""";
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
        try (ResponseBody errorBody = response.errorBody()) {
            if (errorBody == null) {
                return null;
            }

            String errorString = errorBody.string();
            if (errorString.isEmpty()) {
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

        if (message.contains("Failed to connect") || message.contains("Unable to resolve host")) {
            return String.format("""
                    ❌ Cannot connect to server

                    Trying to reach: %s/api/v1/login

                    Please check:
                    1. Laravel server is running: php artisan serve
                    2. Server is on port 8000 (default)
                    3. For emulator, use: http://10.0.2.2:8000
                    4. For physical device, use your computer's IP
                    5. Check ApiClient.java BASE_URL setting
                    6. Verify network_security_config.xml allows cleartext

                    Test in browser: %s/api/v1/login""", baseUrl, baseUrl);
        } else if (message.contains("timeout")) {
            return """
                    ⏱ Connection timeout

                    Server may be slow or unreachable.
                    Check if Laravel server is running and accessible.""";
        } else if (message.contains("Connection refused")) {
            return String.format("""
                    🔌 Connection refused

                    Server is not running or not accessible.
                    Please start Laravel server: php artisan serve
                    Expected URL: %s""", baseUrl);
        } else {
            return String.format("""
                    ❌ Connection error

                    Details: %s""", message);
        }
    }

    // Request location permission when app starts
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Show rationale dialog if user previously denied permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Access Needed")
                        .setMessage("This app needs location access to detect your current location for registration.")
                        .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this,
                                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                                LOCATION_PERMISSION_REQUEST_CODE))
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                // No explanation needed - request the permission
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
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
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission not granted");
            return;
        }

        // Use the new UserLocationManager for location detection
        userLocationManager.updateUserLocation(new UserLocationManager.LocationUpdateCallback() {
            @Override
            public void onLocationUpdated(String location) {
                Log.d(TAG, "Location detected and updated: " + location);
            }

            @Override
            public void onLocationUpdateFailed(String error) {
                Log.e(TAG, "Location detection failed: " + error);
            }
        });
    }

    /**
     * Update user's location and then navigate to dashboard
     * Ensures location update completes before navigation
     */
    private void updateLocationAndNavigate(Object user) {
        // Navigate immediately for instant feedback
        navigateToDashboard(user);

        // Update location in the background
        userLocationManager.updateUserLocation(new UserLocationManager.LocationUpdateCallback() {
            @Override
            public void onLocationUpdated(String location) {
                Log.d(TAG, "Background location update successful: " + location);
            }

            @Override
            public void onLocationUpdateFailed(String error) {
                Log.e(TAG, "Background location update failed: " + error);
            }
        });
    }

    /**
     * Update user's location and then navigate to dashboard (for regular login)
     * Overloaded method for role and email parameters
     */
    private void updateLocationAndNavigate(String role, String email) {
        // Navigate immediately for instant feedback
        navigateToRoleDashboard(role, email);

        // Update location in the background
        userLocationManager.updateUserLocation(new UserLocationManager.LocationUpdateCallback() {
            @Override
            public void onLocationUpdated(String location) {
                Log.d(TAG, "Background location update successful: " + location);
            }

            @Override
            public void onLocationUpdateFailed(String error) {
                Log.e(TAG, "Background location update failed: " + error);
            }
        });
    }

    /**
     * Navigate to dashboard based on role and email
     */
    private void navigateToRoleDashboard(String role, String email) {
        runOnUiThread(() -> {
            final Intent intent;
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

    /**
     * Navigate to appropriate dashboard based on user role
     */
    private void navigateToDashboard(Object user) {
        runOnUiThread(() -> {
            final Intent intent;

            // Handle GoogleSignInResponse.User
            String role = null;
            String email = null;

            if (user instanceof GoogleSignInResponse.User) {
                GoogleSignInResponse.User googleUser = (GoogleSignInResponse.User) user;
                role = googleUser.getRole();
                email = googleUser.getEmail();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userLocationManager != null) {
            userLocationManager.cleanup();
        }
    }
}
