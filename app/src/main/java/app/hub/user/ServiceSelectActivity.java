package app.hub.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.CreateTicketRequest;
import app.hub.api.CreateTicketResponse;
import app.hub.map.MapSelectionActivity;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceSelectActivity extends AppCompatActivity {

    private static final String TAG = "ServiceSelectActivity";
    private EditText titleInput, descriptionInput, addressInput, contactInput;
    private Button createTicketButton;
    private Button mapButton;
    private TextView serviceTypeDisplay;
    private TokenManager tokenManager;
    private String selectedServiceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_create_ticket);

        Log.d(TAG, "onCreate: Activity started");

        // Initialize views based on fragment_user_create_ticket.xml IDs
        titleInput = findViewById(R.id.etTitle);
        descriptionInput = findViewById(R.id.etDescription);
        addressInput = findViewById(R.id.etLocation);
        contactInput = findViewById(R.id.etContact);
        serviceTypeDisplay = findViewById(R.id.tvServiceType);
        createTicketButton = findViewById(R.id.btnSubmit);
        mapButton = findViewById(R.id.btnMap);

        // Hide the Spinner since we already have the service type from intent
        View spinnerLabel = findViewById(R.id.tvServiceTypeLabel2);
        View spinner = findViewById(R.id.spinnerServiceType);
        if (spinnerLabel != null) spinnerLabel.setVisibility(View.GONE);
        if (spinner != null) spinner.setVisibility(View.GONE);

        tokenManager = new TokenManager(this);

        // Get the selected service type from the intent
        selectedServiceType = getIntent().getStringExtra("serviceType");
        if (serviceTypeDisplay != null && selectedServiceType != null) {
            serviceTypeDisplay.setText(selectedServiceType);
        }

        if (mapButton != null) {
            Log.d(TAG, "onCreate: mapButton found, setting onClickListener");
            mapButton.setOnClickListener(v -> {
                Log.d(TAG, "onClick: mapButton clicked");
                try {
                    Intent intent = new Intent(ServiceSelectActivity.this, MapSelectionActivity.class);
                    startActivityForResult(intent, 1001);
                } catch (Exception e) {
                    Log.e(TAG, "onClick: Error starting MapSelectionActivity", e);
                    Toast.makeText(this, "Error opening map", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "onCreate: mapButton NOT FOUND in layout");
        }

        if (createTicketButton != null) {
            createTicketButton.setOnClickListener(v -> createTicket());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String address = data.getStringExtra("address");
            if (addressInput != null && address != null) {
                addressInput.setText(address);
                Log.d(TAG, "onActivityResult: Address set to " + address);
            }
            Toast.makeText(this, "Location selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void createTicket() {
        String title = titleInput != null ? titleInput.getText().toString().trim() : "";
        String description = descriptionInput != null ? descriptionInput.getText().toString().trim() : "";
        String address = addressInput != null ? addressInput.getText().toString().trim() : "";
        String contact = contactInput != null ? contactInput.getText().toString().trim() : "";

        if (title.isEmpty() || description.isEmpty() || address.isEmpty() || contact.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateTicketRequest request = new CreateTicketRequest(title, description, selectedServiceType, address, contact);
        ApiService apiService = ApiClient.getApiService();
        String token = tokenManager.getToken();

        if (token == null) {
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<CreateTicketResponse> call = apiService.createTicket(token, request);
        call.enqueue(new Callback<CreateTicketResponse>() {
            @Override
            public void onResponse(Call<CreateTicketResponse> call, Response<CreateTicketResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ServiceSelectActivity.this, "Ticket created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ServiceSelectActivity.this, "Failed to create ticket", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CreateTicketResponse> call, Throwable t) {
                Toast.makeText(ServiceSelectActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
