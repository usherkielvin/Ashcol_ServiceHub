package app.hub.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.hub.R;

public class AdminEditManagerActivity extends AppCompatActivity {
    private static final String TAG = "AdminEditManager";

    private TextInputEditText etFirstName, etLastName, etEmail, etPassword;
    private AutoCompleteTextView spinnerBranch;
    private Button btnBack, btnEditManager;
    
    private FirebaseFirestore db;
    private String managerId;
    private List<String> branchNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_manager);

        db = FirebaseFirestore.getInstance();
        
        initializeViews();
        loadBranches();
        loadManagerData();
        setupButtons();
    }

    private void initializeViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spinnerBranch = findViewById(R.id.spinnerBranch);
        btnBack = findViewById(R.id.btnBack);
        btnEditManager = findViewById(R.id.btnEditManager);
    }

    private void loadBranches() {
        db.collection("branches")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                branchNames.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String branchName = document.getString("name");
                    if (branchName != null) {
                        branchNames.add(branchName);
                    }
                }
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    branchNames
                );
                spinnerBranch.setAdapter(adapter);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading branches", e);
                Toast.makeText(this, "Failed to load branches", Toast.LENGTH_SHORT).show();
            });
    }

    private void loadManagerData() {
        managerId = getIntent().getStringExtra("manager_id");
        String firstName = getIntent().getStringExtra("manager_first_name");
        String lastName = getIntent().getStringExtra("manager_last_name");
        String email = getIntent().getStringExtra("manager_email");
        String branch = getIntent().getStringExtra("manager_branch");

        if (firstName != null) etFirstName.setText(firstName);
        if (lastName != null) etLastName.setText(lastName);
        if (email != null) etEmail.setText(email);
        if (branch != null) spinnerBranch.setText(branch, false);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());
        
        // Close button in header
        findViewById(R.id.closeButton).setOnClickListener(v -> finish());
        
        btnEditManager.setOnClickListener(v -> updateManager());
    }

    private void updateManager() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String branch = spinnerBranch.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (branch.isEmpty()) {
            Toast.makeText(this, "Please select a branch", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare update data
        Map<String, Object> updates = new HashMap<>();
        updates.put("first_name", firstName);
        updates.put("last_name", lastName);
        updates.put("email", email);
        updates.put("branch", branch);
        
        // Only update password if provided
        if (!password.isEmpty()) {
            if (password.length() < 8) {
                etPassword.setError("Password must be at least 8 characters");
                etPassword.requestFocus();
                return;
            }
            // Note: In production, you should hash the password
            updates.put("password", password);
        }

        // Update in Firestore
        db.collection("users").document(managerId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Manager updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating manager", e);
                Toast.makeText(this, "Failed to update manager: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
