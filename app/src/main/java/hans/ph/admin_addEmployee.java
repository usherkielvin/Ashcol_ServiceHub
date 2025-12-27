package hans.ph;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import hans.ph.api.ApiClient;
import hans.ph.api.ApiService;
import hans.ph.api.RegisterRequest;
import hans.ph.api.RegisterResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class admin_addEmployee extends AppCompatActivity {

    private TextInputEditText firstNameInput, lastNameInput, usernameInput, emailInput, passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_employee);

        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        Button createEmployeeButton = findViewById(R.id.createEmployeeButton);
        createEmployeeButton.setOnClickListener(v -> createEmployee());

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void createEmployee() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // The role is hardcoded to "employee"
        RegisterRequest registerRequest = new RegisterRequest(username, firstName, lastName, email, password, password, "employee");

        ApiService apiService = ApiClient.getApiService();
        Call<RegisterResponse> call = apiService.register(registerRequest);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(admin_addEmployee.this, "Employee created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(admin_addEmployee.this, "Failed to create employee", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(admin_addEmployee.this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
