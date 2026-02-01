package app.hub.user;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.TicketDetailResponse;
import app.hub.map.MapViewActivity;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketDetailActivity extends AppCompatActivity {

    private TextView tvTicketId, tvTitle, tvDescription, tvServiceType, tvAddress, tvContact, tvStatus, tvBranch, tvAssignedStaff, tvCreatedAt;
    private Button btnViewMap, btnBack;
    private TokenManager tokenManager;
    private String ticketId;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        initViews();
        setupClickListeners();
        
        tokenManager = new TokenManager(this);
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
        tvBranch = findViewById(R.id.tvBranch);
        tvAssignedStaff = findViewById(R.id.tvAssignedStaff);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        btnViewMap = findViewById(R.id.btnViewMap);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnViewMap.setOnClickListener(v -> {
            if (latitude != 0 && longitude != 0) {
                Intent intent = new Intent(this, MapViewActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("address", tvAddress.getText().toString());
                intent.putExtra("readonly", true);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            }
        });
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
                        displayTicketDetails(ticketResponse.getTicket());
                    } else {
                        Toast.makeText(TicketDetailActivity.this, "Failed to load ticket details", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(TicketDetailActivity.this, "Failed to load ticket details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<TicketDetailResponse> call, Throwable t) {
                Toast.makeText(TicketDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        tvBranch.setText("Branch: " + (ticket.getBranch() != null ? ticket.getBranch() : "Not assigned"));
        tvAssignedStaff.setText("Assigned to: " + (ticket.getAssignedStaff() != null ? ticket.getAssignedStaff() : "Not assigned"));
        tvCreatedAt.setText("Created: " + ticket.getCreatedAt());

        // Set status color
        setStatusColor(tvStatus, ticket.getStatus(), ticket.getStatusColor());

        // Store coordinates for map viewing
        latitude = ticket.getLatitude();
        longitude = ticket.getLongitude();

        // Show/hide map button based on location availability
        if (latitude != 0 && longitude != 0) {
            btnViewMap.setVisibility(View.VISIBLE);
        } else {
            btnViewMap.setVisibility(View.GONE);
        }

        // Hide assigned staff if not assigned
        if (ticket.getAssignedStaff() == null || ticket.getAssignedStaff().isEmpty()) {
            tvAssignedStaff.setVisibility(View.GONE);
        }
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
}