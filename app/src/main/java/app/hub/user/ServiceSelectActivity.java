package app.hub.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.CreateTicketRequest;
import app.hub.api.CreateTicketResponse;
import app.hub.api.TicketListResponse;
import app.hub.map.MapSelectionActivity;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceSelectActivity extends AppCompatActivity {

    private static final String TAG = "ServiceSelectActivity";
    private static final SimpleDateFormat DATE_FORMAT_API = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat DATE_FORMAT_DISPLAY = new SimpleDateFormat("MMM dd, yyyy",
            Locale.getDefault());

    private EditText titleInput, descriptionInput, addressInput, contactInput, dateInput;
    private Button createTicketButton;
    private Button mapButton;
    private TextView serviceTypeDisplay;
    private TokenManager tokenManager;
    private String selectedServiceType;
    private Long selectedDateMillis = null;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_create_ticket);

        // Initialize views based on fragment_user_create_ticket.xml IDs
        titleInput = findViewById(R.id.etTitle);
        descriptionInput = findViewById(R.id.etDescription);
        addressInput = findViewById(R.id.etLocation);
        contactInput = findViewById(R.id.etContact);
        dateInput = findViewById(R.id.etDate);
        serviceTypeDisplay = findViewById(R.id.tvServiceType);
        createTicketButton = findViewById(R.id.btnSubmit);
        mapButton = findViewById(R.id.btnMap);

        // Hide the Spinner since we already have the service type from intent

        tokenManager = new TokenManager(this);

        // Get the selected service type from the intent
        selectedServiceType = getIntent().getStringExtra("serviceType");
        if (serviceTypeDisplay != null && selectedServiceType != null) {
            serviceTypeDisplay.setText(selectedServiceType);
        }

        // Date picker - open when date field is clicked
        if (dateInput != null) {
            dateInput.setOnClickListener(v -> showDatePicker());
            dateInput.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus)
                    showDatePicker();
            });
        }

        if (mapButton != null) {
            mapButton.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(ServiceSelectActivity.this, MapSelectionActivity.class);
                    startActivityForResult(intent, 1001);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting MapSelectionActivity", e);
                    Toast.makeText(this, "Error opening map", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (createTicketButton != null) {
            createTicketButton.setOnClickListener(v -> createTicket());
        }
    }

    private void showDatePicker() {
        long today = MaterialDatePicker.todayInUtcMilliseconds();
        long minDate = today;
        Calendar maxCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        maxCal.add(Calendar.YEAR, 2);
        long maxDate = maxCal.getTimeInMillis();

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select preferred service date");
        builder.setSelection(selectedDateMillis != null ? selectedDateMillis : today);
        builder.setCalendarConstraints(new CalendarConstraints.Builder()
                .setStart(minDate)
                .setEnd(maxDate)
                .build());

        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            selectedDateMillis = selection;
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            cal.setTimeInMillis(selection);
            String dateStr = DATE_FORMAT_DISPLAY.format(cal.getTime());
            if (dateInput != null)
                dateInput.setText(dateStr);
        });
        picker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String address = data.getStringExtra("address");
            selectedLatitude = data.getDoubleExtra("latitude", 0.0);
            selectedLongitude = data.getDoubleExtra("longitude", 0.0);

            if (addressInput != null && address != null) {
                addressInput.setText(address);
                Log.d(TAG, "onActivityResult: Address set to " + address);
                Log.d(TAG, "onActivityResult: Coordinates - lat: " + selectedLatitude + ", lng: " + selectedLongitude);
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

        if (selectedServiceType == null || selectedServiceType.isEmpty()) {
            selectedServiceType = "General Service";
        }

        // Format preferred date for API (yyyy-MM-dd)
        String preferredDate = null;
        if (selectedDateMillis != null) {
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            cal.setTimeInMillis(selectedDateMillis);
            preferredDate = DATE_FORMAT_API.format(cal.getTime());
        }

        CreateTicketRequest request = new CreateTicketRequest(title, description, selectedServiceType, address, contact,
                preferredDate, selectedLatitude != 0.0 ? selectedLatitude : null,
                selectedLongitude != 0.0 ? selectedLongitude : null);
        ApiService apiService = ApiClient.getApiService();
        String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure token has Bearer prefix for API authentication
        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;

        // Debug: Show coordinates being sent
        // Toast.makeText(this, "Sending: " + selectedLatitude + ", " +
        // selectedLongitude, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Creating ticket with: Lat=" + selectedLatitude + ", Lng=" + selectedLongitude);

        createTicketButton.setEnabled(false);
        createTicketButton.setText("Creating...");

        Call<CreateTicketResponse> call = apiService.createTicket(authToken, request);
        call.enqueue(new Callback<CreateTicketResponse>() {
            @Override
            public void onResponse(Call<CreateTicketResponse> call, Response<CreateTicketResponse> response) {
                createTicketButton.setEnabled(true);
                createTicketButton.setText("Submit");

                if (response.isSuccessful() && response.body() != null) {
                    CreateTicketResponse ticketResponse = response.body();
                    String ticketId = ticketResponse.getTicketId();
                    String status = ticketResponse.getStatus();

                    // Store ticket for instant display when user opens My Tickets
                    CreateTicketResponse.TicketData ticketData = ticketResponse.getTicket();
                    if (ticketData != null) {
                        TicketListResponse.TicketItem item = TicketListResponse.fromCreateResponse(
                                ticketData, status,
                                ticketData.getStatus() != null ? ticketData.getStatus().getColor() : null);
                        if (item != null) {
                            UserTicketsFragment.setPendingNewTicket(item);
                        }
                    }

                    Toast.makeText(ServiceSelectActivity.this, "Ticket created successfully!", Toast.LENGTH_SHORT)
                            .show();

                    // Navigate to confirmation screen
                    Intent intent = new Intent(ServiceSelectActivity.this, TicketConfirmationActivity.class);
                    intent.putExtra("ticket_id", ticketId != null ? ticketId : "");
                    intent.putExtra("status", status != null ? status : "Pending");
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Failed to create ticket";
                    try {
                        if (response.errorBody() != null) {
                            String errBody = response.errorBody().string();
                            if (errBody != null && !errBody.isEmpty()) {
                                errorMsg = errBody.length() > 100 ? errBody.substring(0, 100) + "..." : errBody;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(ServiceSelectActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CreateTicketResponse> call, Throwable t) {
                createTicketButton.setEnabled(true);
                createTicketButton.setText("Submit");
                Log.e(TAG, "Ticket creation failed", t);
                Toast.makeText(ServiceSelectActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        });
    }
}
