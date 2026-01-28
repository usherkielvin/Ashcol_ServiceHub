package app.hub.manager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.RegisterRequest;
import app.hub.api.RegisterResponse;
import app.hub.api.UserResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerAddEmployee extends AppCompatActivity {

    private TextInputEditText firstNameInput, lastNameInput, usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private TextView branchInfoText;
    private TokenManager tokenManager;
    private String managerBranch = "";
    private boolean branchLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_add_employee);

        tokenManager = new TokenManager(this);
        initializeViews();
        setupButtons();
        getManagerBranch();
    }

    private void initializeViews() {
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.Email_val);
        passwordInput = findViewById(R.id.Pass_val);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        branchInfoText = findViewById(R.id.branchInfoText);
    }

    private void setupButtons() {
        Button createEmployeeButton = findViewById(R.id.createEmployeeButton);
        createEmployeeButton.setOnClickListener(v -> createEmployee());

        Button backButton = findViewById(R.id.closeButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void getManagerBranch() {
        // Get current manager's branch from API
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show loading state
        branchInfoText.setText("Loading branch information...");

        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> call = apiService.getUser("Bearer " + token);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    if (userResponse.isSuccess() && userResponse.getData() != null) {
                        managerBranch = userResponse.getData().getBranch();
                        if (managerBranch == null || managerBranch.isEmpty()) {
                            managerBranch = "No branch assigned";
                            branchInfoText.setText("Warning: You don't have a branch assigned. Employee will be created without branch.");
                        } else {
                            branchInfoText.setText("Employee will be assigned to: " + managerBranch);
                        }
                        branchLoaded = true;
                    } else {
                        branchInfoText.setText("Could not load branch information");
                        managerBranch = "";
                        branchLoaded = true;
                    }
                } else {
                    branchInfoText.setText("Could not load branch information");
                    managerBranch = "";
                    branchLoaded = true;
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                branchInfoText.setText("Error loading branch information");
                managerBranch = "";
                branchLoaded = true;
            }
        });
    }

    private void createEmployee() {
        // Check if branch information is loaded
        if (!branchLoaded) {
            Toast.makeText(this, "Please wait, loading branch information...", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

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

        // Create employee request with manager's branch (can be null/empty if manager has no branch)
        RegisterRequest registerRequest = new RegisterRequest(
            username, firstName, lastName, email, "", "", 
            password, confirmPassword, "employee", managerBranch.isEmpty() ? null : managerBranch
        );

        ApiService apiService = ApiClient.getApiService();
        Call<RegisterResponse> call = apiService.register(registerRequest);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    if (registerResponse.isSuccess()) {
                        String successMessage = "Employee created successfully";
                        if (!managerBranch.isEmpty() && !managerBranch.equals("No branch assigned")) {
                            successMessage += " and assigned to " + managerBranch;
                        }
                        Toast.makeText(ManagerAddEmployee.this, successMessage, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMessage = "Failed to create employee";
                        if (registerResponse.getErrors() != null) {
                            // Handle validation errors
                            StringBuilder sb = new StringBuilder();
                            if (registerResponse.getErrors().getEmail() != null) {
                                sb.append("Email: ").append(String.join(", ", registerResponse.getErrors().getEmail())).append("\n");
                            }
                            if (registerResponse.getErrors().getUsername() != null) {
                                sb.append("Username: ").append(String.join(", ", registerResponse.getErrors().getUsername())).append("\n");
                            }
                            if (sb.length() > 0) {
                                errorMessage = sb.toString().trim();
                            }
                        }
                        Toast.makeText(ManagerAddEmployee.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ManagerAddEmployee.this, "Failed to create employee", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(ManagerAddEmployee.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}