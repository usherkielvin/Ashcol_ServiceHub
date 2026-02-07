package app.hub.admin;

import android.app.AlertDialog;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAddEmployee extends AppCompatActivity {

    private TextInputEditText firstNameInput, lastNameInput, usernameInput, emailInput, passwordInput, confirmPasswordInput, roleInput, branchInput;
    private String selectedRole = "";
    private String selectedBranch = "";

    // Available roles
    private final String[] roles = {"technician", "manager"};
    
    // Available branches
    private final String[] branches = {
        "ASHCOL TAGUIG",
        "ASHCOL VALENZUELA",
        "ASHCOL RODRIGUEZ RIZAL",
        "ASHCOL PAMPANGA",
        "ASHCOL BULACAN",
        "ASHCOL GENTRI CAVITE",
        "ASHCOL DASMARINAS CAVITE",
        "ASHCOL STA ROSA – TAGAYTAY RD",
        "ASHCOL LAGUNA",
        "ASHCOL BATANGAS",
        "ASHCOL CANDELARIA QUEZON PROVINCE"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_employee);

        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.Email_val);
        passwordInput = findViewById(R.id.Pass_val);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        roleInput = findViewById(R.id.roleInput);
        branchInput = findViewById(R.id.branchInput);

        // Set up role selection
        roleInput.setOnClickListener(v -> showRoleSelection());
        
        // Set up branch selection
        branchInput.setOnClickListener(v -> showBranchSelection());

        Button createEmployeeButton = findViewById(R.id.createEmployeeButton);
        createEmployeeButton.setOnClickListener(v -> createEmployee());

        Button backButton = findViewById(R.id.closeButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void showRoleSelection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Role");
        builder.setItems(roles, (dialog, which) -> {
            selectedRole = roles[which];
            roleInput.setText(selectedRole);
        });
        builder.show();
    }

    private void showBranchSelection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Branch");
        builder.setItems(branches, (dialog, which) -> {
            selectedBranch = branches[which];
            branchInput.setText(selectedBranch);
        });
        builder.show();
    }

    private void createEmployee() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || 
            email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRole.isEmpty()) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedBranch.isEmpty()) {
            Toast.makeText(this, "Please select a branch", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create register request with role and branch
        RegisterRequest registerRequest = new RegisterRequest(
            username, firstName, lastName, email, "", "", 
            password, confirmPassword, selectedRole, selectedBranch
        );

        ApiService apiService = ApiClient.getApiService();
        Call<RegisterResponse> call = apiService.register(registerRequest);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    if (registerResponse.isSuccess()) {
                        Toast.makeText(AdminAddEmployee.this, "Technician created successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AdminAddEmployee.this, "Failed to create technician: " + registerResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AdminAddEmployee.this, "Failed to create technician: Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(AdminAddEmployee.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
