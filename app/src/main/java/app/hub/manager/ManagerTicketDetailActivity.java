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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

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
    private ActivityResultLauncher<Intent> assignEmployeeLauncher;

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
            if (mapView != null && canInitMap()) {
                mapView.onCreate(savedInstanceState);
                mapView.getMapAsync(this);
            } else {
                safeSetVisible(btnViewMap, View.GONE);
                safeSetVisible(mapCardContainer, View.GONE);
            }

                assignEmployeeLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        android.util.Log.d("ManagerTicketDetail",
                            "Assignment completed successfully, refreshing ticket details");
                        Toast.makeText(this, getString(R.string.manager_ticket_refreshing_status),
                            Toast.LENGTH_SHORT).show();
                        loadTicketDetails();
                    }
                    });

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
                Toast.makeText(this, getString(R.string.manager_ticket_invalid_id), Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            android.util.Log.e("ManagerTicketDetail", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this,
                    getString(R.string.manager_ticket_error_loading, e.getMessage()),
                    Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, getString(R.string.manager_ticket_layout_error), Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this,
                    getString(R.string.manager_ticket_init_error, e.getMessage()),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnViewMap != null) {
            btnViewMap.setOnClickListener(v -> {
                if (latitude != 0 && longitude != 0) {
                    Intent intent = new Intent(this, MapViewActivity.class);
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("address", tvAddress != null ? tvAddress.getText().toString() : "");
                    intent.putExtra("readonly", true);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, getString(R.string.manager_ticket_location_unavailable), Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }

        if (btnReject != null) {
            btnReject.setOnClickListener(v -> showRejectConfirmation());
        }
        if (btnAssignStaff != null) {
            btnAssignStaff.setOnClickListener(v -> launchAssignEmployeeActivity());
        }
    }

    private void loadTicketDetails() {
        String token = tokenManager.getToken();
        if (token == null) {
            android.util.Log.e("ManagerTicketDetail", "Token is null");
            Toast.makeText(this, getString(R.string.manager_ticket_not_logged_in), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ManagerTicketDetailActivity.this,
                                    getString(R.string.manager_ticket_invalid_data), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        android.util.Log.e("ManagerTicketDetail",
                                "API returned success=false: " + ticketResponse.getMessage());
                        Toast.makeText(ManagerTicketDetailActivity.this,
                                getString(R.string.manager_ticket_not_found, ticketResponse.getMessage()),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    android.util.Log.e("ManagerTicketDetail", "Response not successful or body is null");
                    if (response.errorBody() != null) {
                        try (okhttp3.ResponseBody errorBody = response.errorBody()) {
                            String errorText = errorBody != null ? errorBody.string() : "";
                            android.util.Log.e("ManagerTicketDetail", "Error body: " + errorText);
                            Toast.makeText(ManagerTicketDetailActivity.this,
                                    getString(R.string.manager_ticket_error_body, errorText), Toast.LENGTH_SHORT)
                                    .show();
                        } catch (Exception e) {
                            android.util.Log.e("ManagerTicketDetail", "Could not read error body", e);
                            Toast.makeText(ManagerTicketDetailActivity.this,
                                    getString(R.string.manager_ticket_load_failed), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ManagerTicketDetailActivity.this,
                                getString(R.string.manager_ticket_load_failed), Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TicketDetailResponse> call, @NonNull Throwable t) {
                // Check if activity is still valid
                if (isFinishing() || isDestroyed()) {
                    android.util.Log.w("ManagerTicketDetail", "Activity is finishing/destroyed, ignoring failure");
                    return;
                }

                android.util.Log.e("ManagerTicketDetail", "Network error: " + t.getMessage(), t);
                Toast.makeText(ManagerTicketDetailActivity.this,
                        getString(R.string.manager_ticket_network_error, t.getMessage()), Toast.LENGTH_SHORT).show();
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
            public void onResponse(@NonNull Call<EmployeeResponse> call, @NonNull Response<EmployeeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EmployeeResponse employeeResponse = response.body();
                    if (employeeResponse.isSuccess()) {
                        employees.clear();
                        employees.addAll(employeeResponse.getEmployees());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeResponse> call, @NonNull Throwable t) {
                // Handle error silently for now
            }
        });
    }

    private void displayTicketDetails(TicketDetailResponse.TicketDetail ticket) {
        if (ticket == null) {
            Toast.makeText(this, getString(R.string.manager_ticket_invalid_data), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String statusText = ticket.getStatus() != null ? ticket.getStatus() : "Unknown";
        String branchText = ticket.getBranch() != null ? ticket.getBranch() : getString(R.string.manager_ticket_unassigned);
        String customerText = ticket.getCustomerName() != null ? ticket.getCustomerName()
                : getString(R.string.manager_ticket_unknown);
        String createdText = ticket.getCreatedAt() != null ? ticket.getCreatedAt()
                : getString(R.string.manager_ticket_unknown);

        // Set text with null checks
        safeSetText(tvTicketId, ticket.getTicketId() != null ? ticket.getTicketId() : "N/A");
        safeSetText(tvTitle, ticket.getTitle() != null ? ticket.getTitle() : "No Title");
        safeSetText(tvDescription, ticket.getDescription() != null ? ticket.getDescription() : "No Description");
        safeSetText(tvServiceType, ticket.getServiceType() != null ? ticket.getServiceType() : "N/A");
        safeSetText(tvAddress, ticket.getAddress() != null ? ticket.getAddress() : "No Address");
        safeSetText(tvContact, ticket.getContact() != null ? ticket.getContact() : "No Contact");
        safeSetText(tvStatus, getString(R.string.manager_ticket_status_format, statusText));
        safeSetText(tvBranch, getString(R.string.manager_ticket_branch_format, branchText));
        safeSetText(tvCustomerName, getString(R.string.manager_ticket_customer_format, customerText));
        safeSetText(tvCreatedAt, getString(R.string.manager_ticket_created_format, createdText));

        // Display assigned technician
        if (ticket.getAssignedStaff() != null && !ticket.getAssignedStaff().isEmpty()) {
            safeSetText(tvAssignedTechnician,
                    getString(R.string.manager_ticket_technician_format, ticket.getAssignedStaff()));
            safeSetVisible(tvAssignedTechnician, View.VISIBLE);
        } else {
            safeSetText(tvAssignedTechnician, getString(R.string.manager_ticket_technician_not_assigned));
            safeSetVisible(tvAssignedTechnician, View.VISIBLE);
        }

        // Set status color
        if (tvStatus != null) {
            setStatusColor(tvStatus, ticket.getStatus(), ticket.getStatusColor());
        }

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
        if (btnReject == null || btnAssignStaff == null) {
            return;
        }
        if (status == null) {
            // Hide all buttons if status is null
            btnReject.setVisibility(View.GONE);
            btnAssignStaff.setVisibility(View.GONE);
            return;
        }

        // Check if ticket is already assigned to a technician
        boolean isAssigned = currentTicket != null && 
                            currentTicket.getAssignedStaff() != null && 
                            !currentTicket.getAssignedStaff().isEmpty() &&
                            !currentTicket.getAssignedStaff().equals("Not assigned");

        String normalizedStatus = status.toLowerCase().trim();
        
        switch (normalizedStatus) {
            case "pending":
            case "open":
                if (isAssigned) {
                    // Ticket already assigned - show locked state
                    btnAssignStaff.setVisibility(View.VISIBLE);
                    btnAssignStaff.setText(getString(R.string.manager_ticket_assigned_label,
                            currentTicket.getAssignedStaff()));
                    btnAssignStaff.setEnabled(false);
                    btnAssignStaff.setAlpha(0.6f);
                    btnAssignStaff.setBackgroundColor(
                            ContextCompat.getColor(this, android.R.color.darker_gray));
                } else {
                    // Not assigned yet - allow assignment
                    btnAssignStaff.setVisibility(View.VISIBLE);
                    btnAssignStaff.setText(getString(R.string.manager_ticket_assign_label));
                    btnAssignStaff.setEnabled(true);
                    btnAssignStaff.setAlpha(1.0f);
                }
                btnReject.setVisibility(View.VISIBLE);
                break;
                
            case "in progress":
            case "on going":
            case "ongoing":
                // Already in progress - show assigned technician, cannot reassign
                if (isAssigned) {
                    btnAssignStaff.setVisibility(View.VISIBLE);
                    btnAssignStaff.setText(getString(R.string.manager_ticket_assigned_label,
                            currentTicket.getAssignedStaff()));
                    btnAssignStaff.setEnabled(false);
                    btnAssignStaff.setAlpha(0.6f);
                    btnAssignStaff.setBackgroundColor(
                            ContextCompat.getColor(this, android.R.color.darker_gray));
                } else {
                    btnAssignStaff.setVisibility(View.GONE);
                }
                btnReject.setVisibility(View.GONE);
                break;
                
            case "completed":
            case "cancelled":
            case "resolved":
            case "closed":
                // Ticket is done - hide all action buttons
                btnReject.setVisibility(View.GONE);
                btnAssignStaff.setVisibility(View.GONE);
                break;
                
            default:
                // For unknown statuses, check assignment
                if (isAssigned) {
                    btnAssignStaff.setVisibility(View.VISIBLE);
                    btnAssignStaff.setText(getString(R.string.manager_ticket_assigned_label,
                            currentTicket.getAssignedStaff()));
                    btnAssignStaff.setEnabled(false);
                    btnAssignStaff.setAlpha(0.6f);
                } else {
                    btnAssignStaff.setVisibility(View.VISIBLE);
                    btnAssignStaff.setText(getString(R.string.manager_ticket_assign_label));
                    btnAssignStaff.setEnabled(true);
                    btnAssignStaff.setAlpha(1.0f);
                }
                btnReject.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateTicketStatus(String status) {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, getString(R.string.manager_ticket_not_logged_in), Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("ManagerTicketDetail", "Updating ticket status to: " + status);

        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest(status);
        ApiService apiService = ApiClient.getApiService();
        Call<UpdateTicketStatusResponse> call = apiService.updateTicketStatus("Bearer " + token, ticketId, request);

        call.enqueue(new Callback<UpdateTicketStatusResponse>() {
            @Override
                public void onResponse(@NonNull Call<UpdateTicketStatusResponse> call,
                    @NonNull Response<UpdateTicketStatusResponse> response) {
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
                                getString(R.string.manager_ticket_status_update_failed,
                                        statusResponse.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    android.util.Log.e("ManagerTicketDetail", "Status update response not successful");
                    if (response.errorBody() != null) {
                        try (okhttp3.ResponseBody errorBody = response.errorBody()) {
                            String errorText = errorBody != null ? errorBody.string() : "";
                            android.util.Log.e("ManagerTicketDetail", "Status update error body: " + errorText);
                            Toast.makeText(ManagerTicketDetailActivity.this,
                                    getString(R.string.manager_ticket_error_body, errorText), Toast.LENGTH_LONG)
                                    .show();
                        } catch (Exception e) {
                            android.util.Log.e("ManagerTicketDetail", "Could not read error body", e);
                            Toast.makeText(ManagerTicketDetailActivity.this,
                                    getString(R.string.manager_ticket_status_update_error), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ManagerTicketDetailActivity.this,
                                getString(R.string.manager_ticket_status_update_error), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UpdateTicketStatusResponse> call, @NonNull Throwable t) {
                android.util.Log.e("ManagerTicketDetail", "Status update network error: " + t.getMessage(), t);
                Toast.makeText(ManagerTicketDetailActivity.this,
                        getString(R.string.manager_ticket_network_error, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRejectConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.manager_ticket_reject_title)
            .setMessage(R.string.manager_ticket_reject_message)
            .setPositiveButton(R.string.manager_ticket_reject_action, (dialog, which) ->
                updateTicketStatus("cancelled"))
            .setNegativeButton(R.string.manager_ticket_cancel_action, null)
                .show();
    }

    private void launchAssignEmployeeActivity() {
        android.util.Log.d("ManagerTicketDetail", "Attempting to launch AssignEmployeeActivity");

        if (currentTicket == null) {
            android.util.Log.e("ManagerTicketDetail", "currentTicket is null");
            Toast.makeText(this, getString(R.string.manager_ticket_assign_error), Toast.LENGTH_SHORT).show();
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
            if (assignEmployeeLauncher != null) {
                assignEmployeeLauncher.launch(intent);
            } else {
                startActivity(intent);
            }
        } catch (Exception e) {
            android.util.Log.e("ManagerTicketDetail", "Error launching AssignEmployeeActivity", e);
            Toast.makeText(this, getString(R.string.manager_ticket_error_body, e.getMessage()), Toast.LENGTH_SHORT)
                    .show();
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
        if (!canInitMap()) {
            hideMap();
            return;
        }
        if (googleMap == null)
            return;

        if (latitude != 0 && longitude != 0) {
            // Use explicit coordinates
            showLocationOnMap(latitude, longitude);
        } else {
            // Fallback: Try to geocode the address
            String address = tvAddress != null ? tvAddress.getText().toString() : "";
            if (!address.isEmpty() && !address.equals("No Address")) {
                geocodeAndShowLocation(address);
            } else {
                hideMap();
            }
        }
    }

    private void safeSetText(TextView view, String text) {
        if (view != null) {
            view.setText(text);
        }
    }

    private void safeSetVisible(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
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
                    safeSetVisible(btnViewMap, View.VISIBLE);
            if (mapCardContainer != null)
                mapCardContainer.setVisibility(View.VISIBLE);
        }
    }

    private void hideMap() {
        safeSetVisible(btnViewMap, View.GONE);
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

    private boolean canInitMap() {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            return false;
        }
        try {
            Class.forName("org.apache.http.ProtocolVersion");
            return true;
        } catch (ClassNotFoundException e) {
            android.util.Log.w("ManagerTicketDetail", "Apache HTTP classes missing, disabling map");
            return false;
        }
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