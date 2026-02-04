package app.hub.manager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.EmployeeResponse;
import app.hub.api.RegisterRequest;
import app.hub.api.RegisterResponse;
import app.hub.api.UserResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerAddEmployee extends AppCompatActivity {

    private TextInputEditText firstNameInput, lastNameInput, usernameInput, emailInput, passwordInput,
            confirmPasswordInput;
    private TextView branchInfoText;
    private TokenManager tokenManager;
    private String managerBranch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_manager_add_employee);

            android.util.Log.d("ManagerAddEmployee", "Activity created");

            tokenManager = new TokenManager(this);
            initializeViews();
            setupButtons();
            loadManagerInfo();

        } catch (Exception e) {
            android.util.Log.e("ManagerAddEmployee", "Exception in onCreate", e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            android.util.Log.d("ManagerAddEmployee", "Initializing views");

            firstNameInput = findViewById(R.id.firstNameInput);
            lastNameInput = findViewById(R.id.lastNameInput);
            usernameInput = findViewById(R.id.usernameInput);
            emailInput = findViewById(R.id.Email_val);
            passwordInput = findViewById(R.id.Pass_val);
            confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
            branchInfoText = findViewById(R.id.branchInfoText);

            // Check if all views were found
            if (firstNameInput == null)
                android.util.Log.e("ManagerAddEmployee", "firstNameInput is null");
            if (lastNameInput == null)
                android.util.Log.e("ManagerAddEmployee", "lastNameInput is null");
            if (usernameInput == null)
                android.util.Log.e("ManagerAddEmployee", "usernameInput is null");
            if (emailInput == null)
                android.util.Log.e("ManagerAddEmployee", "emailInput is null");
            if (passwordInput == null)
                android.util.Log.e("ManagerAddEmployee", "passwordInput is null");
            if (confirmPasswordInput == null)
                android.util.Log.e("ManagerAddEmployee", "confirmPasswordInput is null");
            if (branchInfoText == null)
                android.util.Log.e("ManagerAddEmployee", "branchInfoText is null");

            android.util.Log.d("ManagerAddEmployee", "Views initialized successfully");

        } catch (Exception e) {
            android.util.Log.e("ManagerAddEmployee", "Exception in initializeViews", e);
            throw e; // Re-throw to be caught by onCreate
        }
    }

    private void setupButtons() {
        try {
            android.util.Log.d("ManagerAddEmployee", "Setting up buttons");

            Button createEmployeeButton = findViewById(R.id.createEmployeeButton);
            if (createEmployeeButton != null) {
                createEmployeeButton.setOnClickListener(v -> {
                    try {
                        createEmployee();
                    } catch (Exception e) {
                        android.util.Log.e("ManagerAddEmployee", "Error in createEmployee click", e);
                        Toast.makeText(ManagerAddEmployee.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                android.util.Log.e("ManagerAddEmployee", "createEmployeeButton is null");
            }

            Button backButton = findViewById(R.id.closeButton);
            if (backButton != null) {
                backButton.setOnClickListener(v -> {
                    try {
                        finish();
                    } catch (Exception e) {
                        android.util.Log.e("ManagerAddEmployee", "Error in back button click", e);
                    }
                });
            } else {
                android.util.Log.e("ManagerAddEmployee", "backButton is null");
            }

            android.util.Log.d("ManagerAddEmployee", "Buttons setup completed");

        } catch (Exception e) {
            android.util.Log.e("ManagerAddEmployee", "Exception in setupButtons", e);
            throw e; // Re-throw to be caught by onCreate
        }
    }

    private void loadManagerInfo() {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        branchInfoText.setText("Loading your branch information...");

        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> call = apiService.getUser("Bearer " + token);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    if (userResponse.isSuccess() && userResponse.getData() != null) {
                        managerBranch = userResponse.getData().getBranch();

                        if (managerBranch != null && !managerBranch.isEmpty()) {
                            branchInfoText.setText("Employee will be assigned to: " + managerBranch);
                        } else {
                            branchInfoText.setText("Warning: You don't have a branch assigned. Please contact admin.");
                            managerBranch = null;
                        }
                    } else {
                        branchInfoText.setText("Error: Could not load your information");
                        managerBranch = null;
                    }
                } else {
                    branchInfoText.setText("Error: Could not load your information");
                    managerBranch = null;
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                branchInfoText.setText("Network error: Could not load branch information");
                managerBranch = null;
            }
        });
    }

    private void createEmployee() {
        try {
            android.util.Log.d("ManagerAddEmployee", "Starting employee creation process");

            String firstName = firstNameInput.getText().toString().trim();
            String lastName = lastNameInput.getText().toString().trim();
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            android.util.Log.d("ManagerAddEmployee",
                    "Form data collected - Username: " + username + ", Email: " + email);

            // Validation
            if (firstName.isEmpty()) {
                firstNameInput.setError("First name is required");
                firstNameInput.requestFocus();
                return;
            }

            if (lastName.isEmpty()) {
                lastNameInput.setError("Last name is required");
                lastNameInput.requestFocus();
                return;
            }

            if (username.isEmpty()) {
                usernameInput.setError("Username is required");
                usernameInput.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                emailInput.setError("Email is required");
                emailInput.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                passwordInput.setError("Password is required");
                passwordInput.requestFocus();
                return;
            }

            if (password.length() < 8) {
                passwordInput.setError("Password must be at least 8 characters");
                passwordInput.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordInput.setError("Passwords do not match");
                confirmPasswordInput.requestFocus();
                return;
            }

            // Check if manager has a branch
            if (managerBranch == null || managerBranch.isEmpty()) {
                Toast.makeText(this, "Cannot create employee: You don't have a branch assigned. Please contact admin.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            android.util.Log.d("ManagerAddEmployee", "Validation passed - Manager branch: " + managerBranch);

            // Create employee with manager's branch
            RegisterRequest registerRequest = new RegisterRequest(
                    username, firstName, lastName, email, "", "",
                    password, confirmPassword, "employee", managerBranch);

            android.util.Log.d("ManagerAddEmployee",
                    "RegisterRequest created - Role: employee, Branch: " + managerBranch);

            ApiService apiService = ApiClient.getApiService();
            if (apiService == null) {
                android.util.Log.e("ManagerAddEmployee", "ApiService is null");
                Toast.makeText(this, "API service error", Toast.LENGTH_SHORT).show();
                return;
            }

            Call<RegisterResponse> call = apiService.register(registerRequest);
            if (call == null) {
                android.util.Log.e("ManagerAddEmployee", "API call is null");
                Toast.makeText(this, "API call error", Toast.LENGTH_SHORT).show();
                return;
            }

            android.util.Log.d("ManagerAddEmployee", "Making API call to register employee");

            call.enqueue(new Callback<RegisterResponse>() {
                @Override
                public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                    try {
                        android.util.Log.d("ManagerAddEmployee", "Response received - Code: " + response.code());
                        android.util.Log.d("ManagerAddEmployee", "Response successful: " + response.isSuccessful());

                        if (response.isSuccessful() && response.body() != null) {
                            RegisterResponse registerResponse = response.body();
                            android.util.Log.d("ManagerAddEmployee",
                                    "Register response success: " + registerResponse.isSuccess());

                            if (registerResponse.isSuccess()) {
                                android.util.Log.d("ManagerAddEmployee", "Employee created successfully");

                                // Clear employee cache to refresh the list
                                try {
                                    // Clear the centralized cache
                                    ManagerDataManager.clearEmployeeCache();

                                    // Also clear TokenManager cache for compatibility
                                    if (tokenManager != null) {
                                        tokenManager.clearBranchCache();
                                    }

                                    // Immediately trigger a refresh to load the new employee
                                    // This will notify all listeners (including ManagerEmployeeFragment)
                                    android.util.Log.d("ManagerAddEmployee", "Triggering immediate employee refresh");
                                    ManagerDataManager.refreshEmployees(ManagerAddEmployee.this,
                                            new ManagerDataManager.DataLoadCallback() {
                                                @Override
                                                public void onEmployeesLoaded(String branchName,
                                                        List<EmployeeResponse.Employee> employees) {
                                                    android.util.Log.d("ManagerAddEmployee",
                                                            "Employees refreshed after creation: " + employees.size());
                                                }

                                                @Override
                                                public void onTicketsLoaded(
                                                        List<app.hub.api.TicketListResponse.TicketItem> tickets) {
                                                }

                                                @Override
                                                public void onLoadComplete() {
                                                    android.util.Log.d("ManagerAddEmployee",
                                                            "Refresh complete, new employee should be visible");
                                                }

                                                @Override
                                                public void onLoadError(String error) {
                                                    android.util.Log.e("ManagerAddEmployee",
                                                            "Error refreshing after creation: " + error);
                                                }
                                            });
                                } catch (Exception e) {
                                    android.util.Log.e("ManagerAddEmployee", "Error clearing cache", e);
                                }

                                Toast.makeText(ManagerAddEmployee.this,
                                        "Employee created successfully and assigned to " + managerBranch,
                                        Toast.LENGTH_LONG).show();

                                // Use a delayed finish to ensure toast is shown and data is refreshed
                                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                    try {
                                        android.util.Log.d("ManagerAddEmployee", "Attempting to finish activity");
                                        if (!isFinishing() && !isDestroyed()) {
                                            finish();
                                            android.util.Log.d("ManagerAddEmployee", "Activity finished successfully");
                                        } else {
                                            android.util.Log.w("ManagerAddEmployee",
                                                    "Activity already finishing or destroyed");
                                        }
                                    } catch (Exception e) {
                                        android.util.Log.e("ManagerAddEmployee", "Error finishing activity", e);
                                    }
                                }, 1500); // Increased delay to 1.5 seconds

                            } else {
                                android.util.Log.e("ManagerAddEmployee", "Registration failed - success=false");
                                String errorMessage = "Failed to create employee";
                                if (registerResponse.getErrors() != null) {
                                    StringBuilder sb = new StringBuilder();
                                    if (registerResponse.getErrors().getEmail() != null) {
                                        sb.append("Email: ")
                                                .append(String.join(", ", registerResponse.getErrors().getEmail()))
                                                .append("\n");
                                    }
                                    if (registerResponse.getErrors().getUsername() != null) {
                                        sb.append("Username: ")
                                                .append(String.join(", ", registerResponse.getErrors().getUsername()))
                                                .append("\n");
                                    }
                                    if (sb.length() > 0) {
                                        errorMessage = sb.toString().trim();
                                    }
                                }
                                Toast.makeText(ManagerAddEmployee.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            android.util.Log.e("ManagerAddEmployee", "Response not successful or body is null");
                            if (response.errorBody() != null) {
                                try {
                                    String errorBody = response.errorBody().string();
                                    android.util.Log.e("ManagerAddEmployee", "Error body: " + errorBody);
                                } catch (Exception e) {
                                    android.util.Log.e("ManagerAddEmployee", "Could not read error body", e);
                                }
                            }
                            Toast.makeText(ManagerAddEmployee.this, "Failed to create employee", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ManagerAddEmployee", "Exception in onResponse", e);
                        Toast.makeText(ManagerAddEmployee.this, "Error processing response: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<RegisterResponse> call, Throwable t) {
                    try {
                        android.util.Log.e("ManagerAddEmployee", "Network error", t);
                        Toast.makeText(ManagerAddEmployee.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    } catch (Exception e) {
                        android.util.Log.e("ManagerAddEmployee", "Exception in onFailure", e);
                    }
                }
            });

        } catch (Exception e) {
            android.util.Log.e("ManagerAddEmployee", "Exception in createEmployee method", e);
            Toast.makeText(this, "Error creating employee: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}