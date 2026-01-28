package app.hub.manager;

import android.os.Bundle;
import android.widget.Button;
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
    private TokenManager tokenManager;
    private String managerBranch = "";

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

        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> call = apiService.getUser("Bearer " + token);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    if (userResponse.isSuccess() && userResponse.getData() != null) {
                        managerBranch = userResponse.getData().getBranch();
                        if (managerBranch == null) {
                            managerBranch = "";
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                // Continue without branch info
            }
        });
    }

    private void createEmployee() {
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

        // Create employee request with manager's branch
        RegisterRequest registerRequest = new RegisterRequest(
            username, firstName, lastName, email, "", "", 
            password, confirmPassword, "employee", managerBranch
        );

        ApiService apiService = ApiClient.getApiService();
        Call<RegisterResponse> call = apiService.register(registerRequest);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    if (registerResponse.isSuccess()) {
                        Toast.makeText(ManagerAddEmployee.this, "Employee created successfully", Toast.LENGTH_SHORT).show();
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