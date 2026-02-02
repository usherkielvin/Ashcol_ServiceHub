package app.hub.employee;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.TicketDetailResponse;
import app.hub.api.UpdateTicketStatusRequest;
import app.hub.api.UpdateTicketStatusResponse;
import app.hub.map.EmployeeMapActivity;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeTicketDetailActivity extends AppCompatActivity {

    private TextView tvTicketId, tvTitle, tvDescription, tvServiceType, tvAddress, tvContact, tvStatus, tvCustomerName, tvCreatedAt, tvScheduleDate, tvScheduleTime, tvScheduleNotes;
    private Button btnViewMap, btnBack, btnStartWork, btnCompleteWork;
    private TokenManager tokenManager;
    private String ticketId;
    private double customerLatitude, customerLongitude;
    private TicketDetailResponse.TicketDetail currentTicket;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_ticket_detail);

        initViews();
        setupClickListeners();
        
        tokenManager = new TokenManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ticketId = getIntent().getStringExtra("ticket_id");
        
        if (ticketId != null) {
            loadTicketDetails();
        } else {
            Toast.makeText(this, "Invalid ticket ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvTicketId = findViewById(R.id.tvTicketId);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvServiceType = findViewById(R.id.tvServiceType);
        tvAddress = findViewById(R.id.tvAddress);
        tvContact = findViewById(R.id.tvContact);
        tvStatus = findViewById(R.id.tvStatus);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvScheduleDate = findViewById(R.id.tvScheduleDate);
        tvScheduleTime = findViewById(R.id.tvScheduleTime);
        tvScheduleNotes = findViewById(R.id.tvScheduleNotes);
        btnViewMap = findViewById(R.id.btnViewMap);
        btnBack = findViewById(R.id.btnBack);
        btnStartWork = findViewById(R.id.btnStartWork);
        btnCompleteWork = findViewById(R.id.btnCompleteWork);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnViewMap.setOnClickListener(v -> {
            if (customerLatitude != 0 && customerLongitude != 0) {
                // Check location permission before opening map
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    return;
                }
                
                Intent intent = new Intent(this, EmployeeMapActivity.class);
                intent.putExtra("customer_latitude", customerLatitude);
                intent.putExtra("customer_longitude", customerLongitude);
                intent.putExtra("customer_address", tvAddress.getText().toString());
                intent.putExtra("ticket_id", ticketId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Customer location not available", Toast.LENGTH_SHORT).show();
            }
        });

        btnStartWork.setOnClickListener(v -> updateTicketStatus("in_progress"));
        btnCompleteWork.setOnClickListener(v -> updateTicketStatus("completed"));
    }

    private void loadTicketDetails() {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<TicketDetailResponse> call = apiService.getTicketDetail("Bearer " + token, ticketId);

        call.enqueue(new Callback<TicketDetailResponse>() {
            @Override
            public void onResponse(Call<TicketDetailResponse> call, Response<TicketDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TicketDetailResponse ticketResponse = response.body();
                    if (ticketResponse.isSuccess()) {
                        currentTicket = ticketResponse.getTicket();
                        displayTicketDetails(currentTicket);
                    } else {
                        Toast.makeText(EmployeeTicketDetailActivity.this, "Failed to load ticket details", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(EmployeeTicketDetailActivity.this, "Failed to load ticket details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<TicketDetailResponse> call, Throwable t) {
                Toast.makeText(EmployeeTicketDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayTicketDetails(TicketDetailResponse.TicketDetail ticket) {
        tvTicketId.setText(ticket.getTicketId());
        tvTitle.setText(ticket.getTitle());
        tvDescription.setText(ticket.getDescription());
        tvServiceType.setText(ticket.getServiceType());
        tvAddress.setText(ticket.getAddress());
        tvContact.setText(ticket.getContact());
        tvStatus.setText("Status: " + ticket.getStatus());
        tvCustomerName.setText("Customer: " + (ticket.getCustomerName() != null ? ticket.getCustomerName() : "Unknown"));
        tvCreatedAt.setText("Created: " + ticket.getCreatedAt());

        // Display schedule information
        if (ticket.getScheduledDate() != null && !ticket.getScheduledDate().isEmpty()) {
            tvScheduleDate.setText("Scheduled Date: " + formatDate(ticket.getScheduledDate()));
            tvScheduleDate.setVisibility(View.VISIBLE);
        } else {
            tvScheduleDate.setVisibility(View.GONE);
        }
        
        if (ticket.getScheduledTime() != null && !ticket.getScheduledTime().isEmpty()) {
            tvScheduleTime.setText("Scheduled Time: " + formatTime(ticket.getScheduledTime()));
            tvScheduleTime.setVisibility(View.VISIBLE);
        } else {
            tvScheduleTime.setVisibility(View.GONE);
        }
        
        if (ticket.getScheduleNotes() != null && !ticket.getScheduleNotes().isEmpty()) {
            tvScheduleNotes.setText("Notes: " + ticket.getScheduleNotes());
            tvScheduleNotes.setVisibility(View.VISIBLE);
        } else {
            tvScheduleNotes.setVisibility(View.GONE);
        }

        // Set status color
        setStatusColor(tvStatus, ticket.getStatus(), ticket.getStatusColor());

        // Store customer coordinates for map viewing
        customerLatitude = ticket.getLatitude();
        customerLongitude = ticket.getLongitude();

        // Show/hide map button based on location availability
        if (customerLatitude != 0 && customerLongitude != 0) {
            btnViewMap.setVisibility(View.VISIBLE);
        } else {
            btnViewMap.setVisibility(View.GONE);
        }

        // Show/hide action buttons based on ticket status
        updateActionButtons(ticket.getStatus());
    }

    private void updateActionButtons(String status) {
        if (status == null) return;

        switch (status.toLowerCase()) {
            case "accepted":
            case "assigned":
                btnStartWork.setVisibility(View.VISIBLE);
                btnCompleteWork.setVisibility(View.GONE);
                break;
            case "in progress":
                btnStartWork.setVisibility(View.GONE);
                btnCompleteWork.setVisibility(View.VISIBLE);
                break;
            case "completed":
            case "cancelled":
                btnStartWork.setVisibility(View.GONE);
                btnCompleteWork.setVisibility(View.GONE);
                break;
            default:
                btnStartWork.setVisibility(View.GONE);
                btnCompleteWork.setVisibility(View.GONE);
                break;
        }
    }

    private void updateTicketStatus(String status) {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest(status);
        ApiService apiService = ApiClient.getApiService();
        Call<UpdateTicketStatusResponse> call = apiService.updateTicketStatus("Bearer " + token, ticketId, request);

        call.enqueue(new Callback<UpdateTicketStatusResponse>() {
            @Override
            public void onResponse(Call<UpdateTicketStatusResponse> call, Response<UpdateTicketStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UpdateTicketStatusResponse statusResponse = response.body();
                    if (statusResponse.isSuccess()) {
                        String message = status.equals("in_progress") ? "Work started successfully" : "Work completed successfully";
                        Toast.makeText(EmployeeTicketDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadTicketDetails(); // Refresh ticket details
                    } else {
                        Toast.makeText(EmployeeTicketDetailActivity.this, "Failed to update ticket status", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EmployeeTicketDetailActivity.this, "Failed to update ticket status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateTicketStatusResponse> call, Throwable t) {
                Toast.makeText(EmployeeTicketDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }
        
        try {
            // Parse the date string (assuming YYYY-MM-DD format from API)
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
            
            java.util.Date date = inputFormat.parse(dateString);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (java.text.ParseException e) {
            return dateString; // Return original if parsing fails
        }
        
        return dateString;
    }
    
    private String formatTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return "";
        }
        
        try {
            // Parse the time string (assuming HH:mm format from API)
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault());
            
            java.util.Date time = inputFormat.parse(timeString);
            if (time != null) {
                return outputFormat.format(time);
            }
        } catch (java.text.ParseException e) {
            return timeString; // Return original if parsing fails
        }
        
        return timeString;
    }
    
    private void setStatusColor(TextView textView, String status, String statusColor) {
        if (statusColor != null && !statusColor.isEmpty()) {
            try {
                textView.setTextColor(Color.parseColor(statusColor));
                return;
            } catch (IllegalArgumentException e) {
                // Fallback to default colors
            }
        }

        // Default color mapping
        if (status == null) return;
        
        switch (status.toLowerCase()) {
            case "pending":
                textView.setTextColor(Color.parseColor("#FFA500")); // Orange
                break;
            case "accepted":
            case "in progress":
                textView.setTextColor(Color.parseColor("#2196F3")); // Blue
                break;
            case "completed":
                textView.setTextColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "cancelled":
            case "rejected":
                textView.setTextColor(Color.parseColor("#F44336")); // Red
                break;
            default:
                textView.setTextColor(Color.parseColor("#757575")); // Gray
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open map
                btnViewMap.performClick();
            } else {
                Toast.makeText(this, "Location permission is required to view map", Toast.LENGTH_SHORT).show();
            }
        }
    }
}