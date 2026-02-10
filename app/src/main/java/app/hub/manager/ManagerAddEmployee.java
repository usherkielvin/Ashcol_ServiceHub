package app.hub.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
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

    private TextInputEditText firstNameInput, lastNameInput, emailInput, passwordInput;
    private AutoCompleteTextView roleSpinner;
    private TextView branchDisplay;
    private MaterialButton btnBack, btnCreate;
    private TokenManager tokenManager;
    private String selectedRole = "technician";
    private String selectedBranch = null;
    private String[] roles = {"technician"};

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

            firstNameInput = findViewById(R.id.etFirstName);
            lastNameInput = findViewById(R.id.etLastName);
            emailInput = findViewById(R.id.etEmail);
            passwordInput = findViewById(R.id.etPassword);
            roleSpinner = findViewById(R.id.spinnerRole);
            branchDisplay = findViewById(R.id.tvBranchDisplay);
            btnBack = findViewById(R.id.btnBack);
            btnCreate = findViewById(R.id.btnCreate);

            // Setup role spinner (only technician)
            ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, roles);
            roleSpinner.setAdapter(roleAdapter);
            roleSpinner.setText("technician", false);
            roleSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedRole = roles[position];
                }
            });

            // Check if all views were found
            if (firstNameInput == null)
                android.util.Log.e("ManagerAddEmployee", "firstNameInput is null");
            if (lastNameInput == null)
                android.util.Log.e("ManagerAddEmployee", "lastNameInput is null");
            if (emailInput == null)
                android.util.Log.e("ManagerAddEmployee", "emailInput is null");
            if (passwordInput == null)
                android.util.Log.e("ManagerAddEmployee", "passwordInput is null");
            if (roleSpinner == null)
                android.util.Log.e("ManagerAddEmployee", "roleSpinner is null");
            if (branchDisplay == null)
                android.util.Log.e("ManagerAddEmployee", "branchDisplay is null");
            if (btnBack == null)
                android.util.Log.e("ManagerAddEmployee", "btnBack is null");
            if (btnCreate == null)
                android.util.Log.e("ManagerAddEmployee", "btnCreate is null");

            android.util.Log.d("ManagerAddEmployee", "Views initialized successfully");

        } catch (Exception e) {
            android.util.Log.e("ManagerAddEmployee", "Exception in initializeViews", e);
            throw e; // Re-throw to be caught by onCreate
        }
    }

    private void setupButtons() {
        try {
            android.util.Log.d("ManagerAddEmployee", "Setting up buttons");

            if (btnCreate != null) {
                btnCreate.setOnClickListener(v -> {
                    try {
                        createEmployee();
                    } catch (Exception e) {
                        android.util.Log.e("ManagerAddEmployee", "Error in createEmployee click", e);
                        Toast.makeText(ManagerAddEmployee.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                android.util.Log.e("ManagerAddEmployee", "btnCreate is null");
            }

            if (btnBack != null) {
                btnBack.setOnClickListener(v -> {
                    try {
                        finish();
                    } catch (Exception e) {
                        android.util.Log.e("ManagerAddEmployee", "Error in back button click", e);
                    }
                });
            } else {
                android.util.Log.e("ManagerAddEmployee", "btnBack is null");
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

        // Load and display manager's branch (non-editable)
        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> call = apiService.getUser("Bearer " + token);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    if (userResponse.isSuccess() && userResponse.getData() != null) {
                        String managerBranch = userResponse.getData().getBranch();
                        if (managerBranch != null && !managerBranch.isEmpty()) {
                            // Display the manager's branch (non-editable)
                            branchDisplay.setText(managerBranch);
                            selectedBranch = managerBranch;
                        } else {
                            branchDisplay.setText("No branch assigned");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                android.util.Log.w("ManagerAddEmployee", "Could not load manager branch: " + t.getMessage());
                branchDisplay.setText("Error loading branch");
            }
        });
    }

    private void createEmployee() {
        try {
            android.util.Log.d("ManagerAddEmployee", "Starting employee creation process");

            String firstName = firstNameInput.getText().toString().trim();
            String lastName = lastNameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = password; // Using same password field

            android.util.Log.d("ManagerAddEmployee",
                    "Form data collected - Role: " + selectedRole + ", Branch: " + selectedBranch);

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

            if (selectedRole == null || selectedRole.isEmpty()) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedBranch == null || selectedBranch.isEmpty()) {
                Toast.makeText(this, "Please select a branch", Toast.LENGTH_SHORT).show();
                return;
            }

            android.util.Log.d("ManagerAddEmployee", "Validation passed - Role: " + selectedRole + ", Branch: " + selectedBranch);

            // Create username from first name + last name
            String username = (firstName.toLowerCase() + "." + lastName.toLowerCase()).replaceAll("\\s+", "");

            // Create employee with selected role and branch
            RegisterRequest registerRequest = new RegisterRequest(
                    username, firstName, lastName, email, "", "",
                    password, confirmPassword, selectedRole, selectedBranch);

            android.util.Log.d("ManagerAddEmployee",
                    "RegisterRequest created - Role: " + selectedRole + ", Branch: " + selectedBranch);

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
                                                public void onDashboardStatsLoaded(
                                                        app.hub.api.DashboardStatsResponse.Stats stats,
                                                        List<app.hub.api.DashboardStatsResponse.RecentTicket> recentTickets) {
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
                                        selectedRole.substring(0, 1).toUpperCase() + selectedRole.substring(1) + 
                                        " created successfully and assigned to " + selectedBranch,
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
                                String errorMessage = "Failed to create " + selectedRole;
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
                                Toast.makeText(ManagerAddEmployee.this, "Failed to create " + selectedRole, Toast.LENGTH_SHORT)
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