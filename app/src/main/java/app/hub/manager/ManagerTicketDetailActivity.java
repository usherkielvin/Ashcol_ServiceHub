package app.hub.manager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.EmployeeResponse;
import app.hub.api.TicketDetailResponse;
import app.hub.api.UpdateTicketStatusRequest;
import app.hub.api.UpdateTicketStatusResponse;
import app.hub.map.MapViewActivity;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerTicketDetailActivity extends AppCompatActivity
        implements com.google.android.gms.maps.OnMapReadyCallback {

    private TextView tvTicketId, tvTitle, tvDescription, tvServiceType, tvAddress, tvContact, tvStatus, tvBranch,
            tvCustomerName, tvCreatedAt, tvAssignedTechnician;
    private Button btnViewMap, btnBack, btnReject, btnAssignStaff;
    private View mapCardContainer;
    private TokenManager tokenManager;
    private String ticketId;
    private double latitude, longitude;
    private TicketDetailResponse.TicketDetail currentTicket;
    private List<EmployeeResponse.Employee> employees;

    private com.google.android.gms.maps.MapView mapView;
    private com.google.android.gms.maps.GoogleMap googleMap;

    private static final int REQUEST_CODE_ASSIGN_EMPLOYEE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_manager_ticket_detail);
            android.util.Log.d("ManagerTicketDetail", "Layout set successfully");

            initViews();
            android.util.Log.d("ManagerTicketDetail", "Views initialized successfully");

            // Initialize MapView
            mapView = findViewById(R.id.mapView);
            if (mapView != null) {
                mapView.onCreate(savedInstanceState);
                mapView.getMapAsync(this);
            }

            setupClickListeners();
            android.util.Log.d("ManagerTicketDetail", "Click listeners set up successfully");

            tokenManager = new TokenManager(this);
            ticketId = getIntent().getStringExtra("ticket_id");
            employees = new ArrayList<>();

            android.util.Log.d("ManagerTicketDetail", "Ticket ID: " + ticketId);

            if (ticketId != null) {
                loadTicketDetails();
                loadEmployees();
            } else {
                android.util.Log.e("ManagerTicketDetail", "Ticket ID is null");
                Toast.makeText(this, "Invalid ticket ID", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            android.util.Log.e("ManagerTicketDetail", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading ticket details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        try {
            tvTicketId = findViewById(R.id.tvTicketId);
            tvTitle = findViewById(R.id.tvTitle);
            tvDescription = findViewById(R.id.tvDescription);
            tvServiceType = findViewById(R.id.tvServiceType);
            tvAddress = findViewById(R.id.tvAddress);
            tvContact = findViewById(R.id.tvContact);
            tvStatus = findViewById(R.id.tvStatus);
            tvBranch = findViewById(R.id.tvBranch);
            tvCustomerName = findViewById(R.id.tvCustomerName);
            tvCreatedAt = findViewById(R.id.tvCreatedAt);
            tvAssignedTechnician = findViewById(R.id.tvAssignedTechnician);
            btnViewMap = findViewById(R.id.btnViewMap);
            btnBack = findViewById(R.id.btnBack);
            btnReject = findViewById(R.id.btnReject);
            btnAssignStaff = findViewById(R.id.btnAssignStaff);
            mapCardContainer = findViewById(R.id.mapCardContainer);

            // Check if any critical views are null
            if (tvTicketId == null || tvTitle == null || btnBack == null) {
                Toast.makeText(this, "Layout error - missing views", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
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

        btnReject.setOnClickListener(v -> showRejectConfirmation());
        btnAssignStaff.setOnClickListener(v -> launchAssignEmployeeActivity());
    }

    private void loadTicketDetails() {
        String token = tokenManager.getToken();
        if (token == null) {
            android.util.Log.e("ManagerTicketDetail", "Token is null");
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        android.util.Log.d("ManagerTicketDetail", "Loading ticket details for: " + ticketId);

        ApiService apiService = ApiClient.getApiService();
        Call<TicketDetailResponse> call = apiService.getTicketDetail("Bearer " + token, ticketId);

        call.enqueue(new Callback<TicketDetailResponse>() {
            @Override
            public void onResponse(Call<TicketDetailResponse> call, Response<TicketDetailResponse> response) {
                // Check if activity is still valid
                if (isFinishing() || isDestroyed()) {
                    android.util.Log.w("ManagerTicketDetail", "Activity is finishing/destroyed, ignoring response");
                    return;
                }

                android.util.Log.d("ManagerTicketDetail", "API Response received - Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    TicketDetailResponse ticketResponse = response.body();
                    android.util.Log.d("ManagerTicketDetail", "Response success: " + ticketResponse.isSuccess());

                    if (ticketResponse.isSuccess()) {
                        currentTicket = ticketResponse.getTicket();
                        if (currentTicket != null) {
                            displayTicketDetails(currentTicket);
                        } else {
                            android.util.Log.e("ManagerTicketDetail", "Ticket data is null");
                            Toast.makeText(ManagerTicketDetailActivity.this, "Invalid ticket data", Toast.LENGTH_SHORT)
                                    .show();
                            finish();
                        }
                    } else {
                        android.util.Log.e("ManagerTicketDetail",
                                "API returned success=false: " + ticketResponse.getMessage());
                        Toast.makeText(ManagerTicketDetailActivity.this,
                                "Ticket not found: " + ticketResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    android.util.Log.e("ManagerTicketDetail", "Response not successful or body is null");
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("ManagerTicketDetail", "Error body: " + errorBody);
                            Toast.makeText(ManagerTicketDetailActivity.this, "Error: " + errorBody, Toast.LENGTH_SHORT)
                                    .show();
                        } catch (Exception e) {
                            android.util.Log.e("ManagerTicketDetail", "Could not read error body", e);
                            Toast.makeText(ManagerTicketDetailActivity.this, "Failed to load ticket details",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ManagerTicketDetailActivity.this, "Failed to load ticket details",
                                Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
            }

            @Override
            public void onFailure(Call<TicketDetailResponse> call, Throwable t) {
                // Check if activity is still valid
                if (isFinishing() || isDestroyed()) {
                    android.util.Log.w("ManagerTicketDetail", "Activity is finishing/destroyed, ignoring failure");
                    return;
                }

                android.util.Log.e("ManagerTicketDetail", "Network error: " + t.getMessage(), t);
                Toast.makeText(ManagerTicketDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        });
    }

    private void loadEmployees() {
        String token = tokenManager.getToken();
        if (token == null)
            return;

        ApiService apiService = ApiClient.getApiService();
        Call<EmployeeResponse> call = apiService.getEmployees("Bearer " + token);

        call.enqueue(new Callback<EmployeeResponse>() {
            @Override
            public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EmployeeResponse employeeResponse = response.body();
                    if (employeeResponse.isSuccess()) {
                        employees.clear();
                        employees.addAll(employeeResponse.getEmployees());
                    }
                }
            }

            @Override
            public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                // Handle error silently for now
            }
        });
    }

    private void displayTicketDetails(TicketDetailResponse.TicketDetail ticket) {
        if (ticket == null) {
            Toast.makeText(this, "Invalid ticket data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set text with null checks
        tvTicketId.setText(ticket.getTicketId() != null ? ticket.getTicketId() : "N/A");
        tvTitle.setText(ticket.getTitle() != null ? ticket.getTitle() : "No Title");
        tvDescription.setText(ticket.getDescription() != null ? ticket.getDescription() : "No Description");
        tvServiceType.setText(ticket.getServiceType() != null ? ticket.getServiceType() : "N/A");
        tvAddress.setText(ticket.getAddress() != null ? ticket.getAddress() : "No Address");
        tvContact.setText(ticket.getContact() != null ? ticket.getContact() : "No Contact");
        tvStatus.setText("Status: " + (ticket.getStatus() != null ? ticket.getStatus() : "Unknown"));
        tvBranch.setText("Branch: " + (ticket.getBranch() != null ? ticket.getBranch() : "Not assigned"));
        tvCustomerName
                .setText("Customer: " + (ticket.getCustomerName() != null ? ticket.getCustomerName() : "Unknown"));
        tvCreatedAt.setText("Created: " + (ticket.getCreatedAt() != null ? ticket.getCreatedAt() : "Unknown"));

        // Display assigned technician
        if (ticket.getAssignedStaff() != null && !ticket.getAssignedStaff().isEmpty()) {
            tvAssignedTechnician.setText("Technician: " + ticket.getAssignedStaff());
            tvAssignedTechnician.setVisibility(View.VISIBLE);
        } else {
            tvAssignedTechnician.setText("Technician: Not assigned");
            tvAssignedTechnician.setVisibility(View.VISIBLE);
        }

        // Set status color
        setStatusColor(tvStatus, ticket.getStatus(), ticket.getStatusColor());

        // Store coordinates for map viewing with null checks
        try {
            latitude = ticket.getLatitude();
            longitude = ticket.getLongitude();
        } catch (Exception e) {
            latitude = 0;
            longitude = 0;
        }

        // Update map if ready and coordinates are valid
        updateMapLocation();

        // Show/hide action buttons based on ticket status
        updateActionButtons(ticket.getStatus());
    }

    private void updateActionButtons(String status) {
        if (status == null) {
            // Hide all buttons if status is null
            btnReject.setVisibility(View.GONE);
            btnAssignStaff.setVisibility(View.GONE);
            return;
        }

        switch (status.toLowerCase()) {
            case "pending":
            case "open": // Also handle "open" status
                // Show both Assign Staff and Reject buttons for pending tickets
                btnReject.setVisibility(View.VISIBLE);
                btnAssignStaff.setVisibility(View.VISIBLE);
                btnAssignStaff.setText("Assign Staff");
                break;
            case "in progress":
            case "completed":
            case "cancelled":
                btnReject.setVisibility(View.GONE);
                btnAssignStaff.setVisibility(View.GONE);
                break;
            default:
                // For unknown statuses, show assign and reject buttons
                btnReject.setVisibility(View.VISIBLE);
                btnAssignStaff.setVisibility(View.VISIBLE);
                btnAssignStaff.setText("Assign Staff");
                break;
        }
    }

    private void updateTicketStatus(String status) {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("ManagerTicketDetail", "Updating ticket status to: " + status);

        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest(status);
        ApiService apiService = ApiClient.getApiService();
        Call<UpdateTicketStatusResponse> call = apiService.updateTicketStatus("Bearer " + token, ticketId, request);

        call.enqueue(new Callback<UpdateTicketStatusResponse>() {
            @Override
            public void onResponse(Call<UpdateTicketStatusResponse> call,
                    Response<UpdateTicketStatusResponse> response) {
                android.util.Log.d("ManagerTicketDetail", "Status update response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    UpdateTicketStatusResponse statusResponse = response.body();
                    android.util.Log.d("ManagerTicketDetail", "Status update success: " + statusResponse.isSuccess());

                    if (statusResponse.isSuccess()) {
                        // Clear ticket cache so the list will refresh with updated status
                        ManagerDataManager.clearTicketCache();

                        Toast.makeText(ManagerTicketDetailActivity.this, "Ticket status updated successfully",
                                Toast.LENGTH_SHORT).show();
                        loadTicketDetails(); // Refresh ticket details
                    } else {
                        android.util.Log.e("ManagerTicketDetail",
                                "Status update failed: " + statusResponse.getMessage());
                        Toast.makeText(ManagerTicketDetailActivity.this,
                                "Failed to update: " + statusResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    android.util.Log.e("ManagerTicketDetail", "Status update response not successful");
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("ManagerTicketDetail", "Status update error body: " + errorBody);
                            Toast.makeText(ManagerTicketDetailActivity.this, "Error: " + errorBody, Toast.LENGTH_LONG)
                                    .show();
                        } catch (Exception e) {
                            android.util.Log.e("ManagerTicketDetail", "Could not read error body", e);
                            Toast.makeText(ManagerTicketDetailActivity.this, "Failed to update ticket status",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ManagerTicketDetailActivity.this, "Failed to update ticket status",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UpdateTicketStatusResponse> call, Throwable t) {
                android.util.Log.e("ManagerTicketDetail", "Status update network error: " + t.getMessage(), t);
                Toast.makeText(ManagerTicketDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void showRejectConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Reject Ticket")
                .setMessage("Are you sure you want to reject this ticket? This action cannot be undone.")
                .setPositiveButton("Reject", (dialog, which) -> updateTicketStatus("cancelled"))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void launchAssignEmployeeActivity() {
        android.util.Log.d("ManagerTicketDetail", "Attempting to launch AssignEmployeeActivity");

        if (currentTicket == null) {
            android.util.Log.e("ManagerTicketDetail", "currentTicket is null");
            Toast.makeText(this, "Ticket details not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("ManagerTicketDetail", "Ticket ID: " + currentTicket.getTicketId());
        android.util.Log.d("ManagerTicketDetail", "Ticket Title: " + currentTicket.getTitle());

        try {
            Intent intent = new Intent(this, AssignEmployeeActivity.class);
            intent.putExtra("ticket_id", currentTicket.getTicketId());
            intent.putExtra("ticket_title", currentTicket.getTitle());
            intent.putExtra("ticket_description", currentTicket.getDescription());
            intent.putExtra("ticket_address", currentTicket.getAddress());

            android.util.Log.d("ManagerTicketDetail", "Starting AssignEmployeeActivity");
            startActivityForResult(intent, REQUEST_CODE_ASSIGN_EMPLOYEE);
        } catch (Exception e) {
            android.util.Log.e("ManagerTicketDetail", "Error launching AssignEmployeeActivity", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ASSIGN_EMPLOYEE && resultCode == RESULT_OK) {
            // Assignment was successful, refresh ticket details to show updated status
            android.util.Log.d("ManagerTicketDetail", "Assignment completed successfully, refreshing ticket details");
            Toast.makeText(this, "Refreshing ticket status...", Toast.LENGTH_SHORT).show();
            loadTicketDetails();
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
        if (status == null)
            return;

        switch (status.toLowerCase()) {
            case "pending":
                textView.setTextColor(Color.parseColor("#FFA500")); // Orange
                break;
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

    // MapView Lifecycle methods
    private void updateMapLocation() {
        if (googleMap == null)
            return;

        if (latitude != 0 && longitude != 0) {
            // Use explicit coordinates
            showLocationOnMap(latitude, longitude);
        } else {
            // Fallback: Try to geocode the address
            String address = tvAddress.getText().toString();
            if (!address.isEmpty() && !address.equals("No Address")) {
                geocodeAndShowLocation(address);
            } else {
                hideMap();
            }
        }
    }

    private void showLocationOnMap(double lat, double lng) {
        if (googleMap != null) {
            com.google.android.gms.maps.model.LatLng location = new com.google.android.gms.maps.model.LatLng(lat, lng);
            googleMap.clear();
            googleMap.addMarker(
                    new com.google.android.gms.maps.model.MarkerOptions().position(location).title("Service Location"));
            googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 15f));
            googleMap.getUiSettings().setMapToolbarEnabled(false);

            // Ensure map is visible
            btnViewMap.setVisibility(View.VISIBLE);
            if (mapCardContainer != null)
                mapCardContainer.setVisibility(View.VISIBLE);
        }
    }

    private void hideMap() {
        btnViewMap.setVisibility(View.GONE);
        if (mapCardContainer != null)
            mapCardContainer.setVisibility(View.GONE);
    }

    private void geocodeAndShowLocation(String addressStr) {
        new Thread(() -> {
            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
                java.util.List<android.location.Address> addresses = geocoder.getFromLocationName(addressStr, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address location = addresses.get(0);
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    // Update UI on main thread
                    runOnUiThread(() -> {
                        // Update the stored coordinates
                        this.latitude = lat;
                        this.longitude = lng;
                        showLocationOnMap(lat, lng);
                    });
                } else {
                    runOnUiThread(this::hideMap);
                }
            } catch (java.io.IOException e) {
                runOnUiThread(this::hideMap);
            }
        }).start();
    }

    @Override
    public void onMapReady(com.google.android.gms.maps.GoogleMap map) {
        this.googleMap = map;
        updateMapLocation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null)
            mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null)
            mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null)
            mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null)
            mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null)
            mapView.onDestroy();
        android.util.Log.d("ManagerTicketDetail", "Activity destroyed");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null)
            mapView.onLowMemory();
    }
}