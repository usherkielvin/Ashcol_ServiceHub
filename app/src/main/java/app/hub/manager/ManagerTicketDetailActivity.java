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

    private TextView tvTicketId, tvDescription, tvServiceType, tvAddress, tvContact, tvStatus, tvBranch,
            tvCustomerName, tvCreatedAt, tvAssignedTechnician, tvUnitType;
    private Button btnViewMap, btnReject, btnAssignStaff;
    private android.widget.ImageButton btnBack;
    private android.widget.AutoCompleteTextView spinnerTechnician;
    private View mapCardContainer;
    private TokenManager tokenManager;
    private String ticketId;
    private double latitude, longitude;
    private TicketDetailResponse.TicketDetail currentTicket;
    private List<EmployeeResponse.Employee> employees;
    private TechnicianAdapter technicianAdapter;

    private com.google.android.gms.maps.MapView mapView;
    private com.google.android.gms.maps.GoogleMap googleMap;
    private ActivityResultLauncher<Intent> assignEmployeeLauncher;
    private boolean mapEnabled = false;

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
            mapEnabled = mapView != null && canInitMap();
            if (mapEnabled) {
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
                                ManagerDataManager.refreshTickets(getApplicationContext(), new ManagerDataManager.DataLoadCallback() {
                                    @Override
                                    public void onEmployeesLoaded(String branchName, List<EmployeeResponse.Employee> employees) {
                                    }

                                    @Override
                                    public void onTicketsLoaded(List<app.hub.api.TicketListResponse.TicketItem> tickets) {
                                        runOnUiThread(ManagerTicketDetailActivity.this::finish);
                                    }

                                    @Override
                                    public void onDashboardStatsLoaded(app.hub.api.DashboardStatsResponse.Stats stats,
                                            List<app.hub.api.DashboardStatsResponse.RecentTicket> recentTickets) {
                                    }

                                    @Override
                                    public void onLoadComplete() {
                                    }

                                    @Override
                                    public void onLoadError(String error) {
                                        runOnUiThread(ManagerTicketDetailActivity.this::finish);
                                    }
                                });
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
            tvDescription = findViewById(R.id.tvDescription);
            tvServiceType = findViewById(R.id.tvServiceType);
            tvUnitType = findViewById(R.id.tvUnitType);
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
            spinnerTechnician = findViewById(R.id.spinnerTechnician);
            mapCardContainer = findViewById(R.id.mapCardContainer);

            // Check if any critical views are null
            if (tvTicketId == null || btnBack == null) {
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
            btnReject.setOnClickListener(v -> showCancelConfirmDialog());
        }
        if (btnAssignStaff != null) {
            btnAssignStaff.setOnClickListener(v -> assignTechnicianFromDetail());
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
                        
                        android.util.Log.d("ManagerTicketDetail", "Loaded " + employees.size() + " technicians");
                        
                        // Set up the technician adapter
                        setupTechnicianSpinner();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeResponse> call, @NonNull Throwable t) {
                // Handle error silently for now
                android.util.Log.e("ManagerTicketDetail", "Failed to load employees: " + t.getMessage());
            }
        });
    }
    
    private void setupTechnicianSpinner() {
        if (spinnerTechnician == null || employees == null) {
            android.util.Log.e("ManagerTicketDetail", "Cannot setup spinner - spinner or employees is null");
            return;
        }
        
        // Use custom adapter with status display
        technicianAdapter = new TechnicianAdapter(this, employees);
        spinnerTechnician.setAdapter(technicianAdapter);
        
        // Set threshold to 0 to show all items immediately
        spinnerTechnician.setThreshold(0);
        
        // Disable default click behavior to prevent stuttering
        spinnerTechnician.setOnClickListener(null);
        spinnerTechnician.setOnFocusChangeListener(null);
        
        // Set up proper touch listener to show dropdown
        spinnerTechnician.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                spinnerTechnician.showDropDown();
            }
            return false;
        });
        
        // Also show dropdown when focused
        spinnerTechnician.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                spinnerTechnician.showDropDown();
            }
        });
        
        // Add item click listener to prevent selection of busy technicians
        spinnerTechnician.setOnItemClickListener((parent, view, position, id) -> {
            EmployeeResponse.Employee selectedTech = (EmployeeResponse.Employee) parent.getItemAtPosition(position);
            if (selectedTech != null && selectedTech.getTicketCount() > 0) {
                // Technician is busy - prevent selection
                Toast.makeText(this, 
                    "This technician is busy with " + selectedTech.getTicketCount() + 
                    " ticket(s). Please select an available technician.", 
                    Toast.LENGTH_LONG).show();
                spinnerTechnician.setText(""); // Clear the selection
                spinnerTechnician.dismissDropDown();
            }
        });
        
        android.util.Log.d("ManagerTicketDetail", "Technician spinner setup complete with " + employees.size() + " technicians");
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
        String createdText = formatPreferredDate(ticket.getCreatedAt());

        // Get unit type from ticket (now stored separately in database)
        String unitType = ticket.getUnitType() != null && !ticket.getUnitType().isEmpty() 
                ? ticket.getUnitType() 
                : "N/A";
        String otherDetails = ticket.getDescription() != null ? ticket.getDescription() : "";

        // Set text with null checks
        safeSetText(tvTicketId, ticket.getTicketId() != null ? ticket.getTicketId() : "N/A");
        safeSetText(tvDescription, !otherDetails.isEmpty() ? otherDetails : "No Description");
        safeSetText(tvServiceType, ticket.getServiceType() != null ? ticket.getServiceType() : "N/A");
        safeSetText(tvUnitType, unitType);
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
            case "scheduled":
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
                        String updatedStatus = statusResponse.getTicket() != null
                                ? statusResponse.getTicket().getStatus()
                                : status;

                        ManagerDataManager.updateTicketStatusInCache(ticketId, updatedStatus);

                        Toast.makeText(ManagerTicketDetailActivity.this, "Ticket status updated successfully",
                                Toast.LENGTH_SHORT).show();

                        ManagerDataManager.refreshTickets(getApplicationContext(), new ManagerDataManager.DataLoadCallback() {
                            @Override
                            public void onEmployeesLoaded(String branchName, List<EmployeeResponse.Employee> employees) {
                            }

                            @Override
                            public void onTicketsLoaded(List<app.hub.api.TicketListResponse.TicketItem> tickets) {
                                runOnUiThread(ManagerTicketDetailActivity.this::finish);
                            }

                            @Override
                            public void onDashboardStatsLoaded(app.hub.api.DashboardStatsResponse.Stats stats,
                                    List<app.hub.api.DashboardStatsResponse.RecentTicket> recentTickets) {
                            }

                            @Override
                            public void onLoadComplete() {
                            }

                            @Override
                            public void onLoadError(String error) {
                                runOnUiThread(ManagerTicketDetailActivity.this::finish);
                            }
                        });
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

    private void assignTechnicianFromDetail() {
        // Validate that a technician is selected
        if (spinnerTechnician == null || spinnerTechnician.getText() == null || 
            spinnerTechnician.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select a technician", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Find the selected technician
        String selectedTechnicianName = spinnerTechnician.getText().toString().trim();
        EmployeeResponse.Employee selectedTechnician = null;
        
        for (EmployeeResponse.Employee emp : employees) {
            String name = (emp.getFirstName() != null ? emp.getFirstName() : "") +
                    " " + (emp.getLastName() != null ? emp.getLastName() : "");
            if (name.trim().isEmpty()) {
                name = emp.getEmail() != null ? emp.getEmail() : "Unknown Technician";
            }
            if (name.trim().equals(selectedTechnicianName)) {
                selectedTechnician = emp;
                break;
            }
        }
        
        if (selectedTechnician == null) {
            Toast.makeText(this, "Please select a valid technician", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if the technician is busy
        if (selectedTechnician.getTicketCount() > 0) {
            Toast.makeText(this, "This technician is currently busy. Please select an available technician.", 
                          Toast.LENGTH_LONG).show();
            spinnerTechnician.setText(""); // Clear the selection
            return;
        }
        
        // Get the notes
        com.google.android.material.textfield.TextInputEditText etNotes = findViewById(R.id.etNotes);
        String notes = etNotes != null && etNotes.getText() != null ? 
                       etNotes.getText().toString().trim() : "";
        
        // Use current date and time as default
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        java.text.SimpleDateFormat stf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        String scheduledDate = sdf.format(new java.util.Date());
        String scheduledTime = stf.format(new java.util.Date());
        
        // Call API to assign technician
        assignTechnicianToTicket(selectedTechnician.getId(), scheduledDate, scheduledTime, notes, selectedTechnicianName);
    }
    
    private void assignTechnicianToTicket(int technicianId, String scheduledDate, String scheduledTime, 
                                          String notes, String technicianName) {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        app.hub.api.SetScheduleRequest request = new app.hub.api.SetScheduleRequest();
        request.setScheduledDate(scheduledDate);
        request.setScheduledTime(scheduledTime);
        request.setScheduleNotes(notes);
        request.setAssignedStaffId(technicianId);
        
        android.util.Log.d("ManagerTicketDetail", "Assigning technician ID: " + technicianId);
        
        ApiService apiService = ApiClient.getApiService();
        Call<app.hub.api.SetScheduleResponse> call = apiService.setTicketSchedule("Bearer " + token, ticketId, request);
        
        call.enqueue(new Callback<app.hub.api.SetScheduleResponse>() {
            @Override
            public void onResponse(@NonNull Call<app.hub.api.SetScheduleResponse> call, 
                                 @NonNull Response<app.hub.api.SetScheduleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    app.hub.api.SetScheduleResponse scheduleResponse = response.body();
                    if (scheduleResponse.isSuccess()) {
                        // Show confirmation dialog
                        showAssignmentConfirmation(technicianName, scheduledDate, scheduledTime);
                    } else {
                        Toast.makeText(ManagerTicketDetailActivity.this, 
                                "Failed to assign: " + scheduleResponse.getMessage(), 
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ManagerTicketDetailActivity.this, 
                            "Failed to assign technician. Please try again.", 
                            Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<app.hub.api.SetScheduleResponse> call, @NonNull Throwable t) {
                Toast.makeText(ManagerTicketDetailActivity.this, 
                        "Network error: " + t.getMessage(), 
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void showAssignmentConfirmation(String technicianName, String scheduledDate, String scheduledTime) {
        // Format the service name (only service type, no customer name)
        String serviceName = currentTicket.getServiceType() != null ? currentTicket.getServiceType() : "";
        
        // Format date and time as "Feb 11, 2026 7:34AM"
        String dateTime = formatAssignmentDateTime(scheduledDate, scheduledTime);
        
        AssignConfirmBottomSheet bottomSheet = AssignConfirmBottomSheet.newInstance(
            ticketId, serviceName, technicianName, dateTime
        );
        
        bottomSheet.setOnDoneClickListener(() -> {
            // Refresh data and close activity
            ManagerDataManager.refreshTickets(getApplicationContext(), new ManagerDataManager.DataLoadCallback() {
                @Override
                public void onEmployeesLoaded(String branchName, List<EmployeeResponse.Employee> employees) {
                }

                @Override
                public void onTicketsLoaded(List<app.hub.api.TicketListResponse.TicketItem> tickets) {
                    runOnUiThread(ManagerTicketDetailActivity.this::finish);
                }

                @Override
                public void onDashboardStatsLoaded(app.hub.api.DashboardStatsResponse.Stats stats,
                        List<app.hub.api.DashboardStatsResponse.RecentTicket> recentTickets) {
                }

                @Override
                public void onLoadComplete() {
                }

                @Override
                public void onLoadError(String error) {
                    runOnUiThread(ManagerTicketDetailActivity.this::finish);
                }
            });
        });
        
        bottomSheet.show(getSupportFragmentManager(), "AssignConfirmBottomSheet");
    }
    
    private void showCancelConfirmDialog() {
        CancelConfirmDialog dialog = CancelConfirmDialog.newInstance();
        dialog.setOnConfirmClickListener(new CancelConfirmDialog.OnConfirmClickListener() {
            @Override
            public void onConfirmYes() {
                // Show the cancellation reason bottom sheet
                showCancelReasonBottomSheet();
            }
            
            @Override
            public void onConfirmNo() {
                // Do nothing, dialog will dismiss
            }
        });
        dialog.show(getSupportFragmentManager(), "CancelConfirmDialog");
    }
    
    private void showCancelReasonBottomSheet() {
        String service = (currentTicket.getServiceType() != null ? currentTicket.getServiceType() : "") + 
                        " - " + (currentTicket.getTitle() != null ? currentTicket.getTitle() : "");
        String schedule = currentTicket.getCreatedAt() != null ? currentTicket.getCreatedAt() : "N/A";
        String customerName = currentTicket.getCustomerName() != null ? currentTicket.getCustomerName() : "Unknown";
        
        CancelReasonBottomSheet bottomSheet = CancelReasonBottomSheet.newInstance(
            ticketId, customerName, service, schedule
        );
        
        bottomSheet.setOnCancelConfirmListener(new CancelReasonBottomSheet.OnCancelConfirmListener() {
            @Override
            public void onCancelConfirmed(String reason) {
                // Update ticket status to cancelled
                updateTicketStatus("cancelled");
            }
            
            @Override
            public void onBack() {
                // Do nothing, bottom sheet will dismiss
            }
        });
        
        bottomSheet.show(getSupportFragmentManager(), "CancelReasonBottomSheet");
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
            case "scheduled":
                textView.setTextColor(Color.parseColor("#6366F1")); // Indigo
                break;
            case "in progress":
            case "ongoing":
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
        if (!mapEnabled || !canInitMap()) {
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

    private String formatPreferredDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return getString(R.string.manager_ticket_unknown);
        }
        try {
            // Parse the incoming date format (e.g., "2026-02-11 23:09:45" or "2026-02-11")
            java.text.SimpleDateFormat inputFormat;
            if (dateStr.contains(" ")) {
                inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            } else {
                inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            }
            java.util.Date date = inputFormat.parse(dateStr);
            // Format to "MMM dd, yyyy" (e.g., "Feb 11, 2026")
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            android.util.Log.e("ManagerTicketDetail", "Error formatting date: " + dateStr, e);
            return dateStr; // Return original if parsing fails
        }
    }

    private String formatAssignmentDateTime(String dateStr, String timeStr) {
        try {
            // Parse date "2026-02-11" and time "23:24"
            String dateTimeStr = dateStr + " " + timeStr;
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
            java.util.Date dateTime = inputFormat.parse(dateTimeStr);
            // Format to "MMM dd, yyyy h:mma" (e.g., "Feb 11, 2026 7:34AM")
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd, yyyy h:mma", java.util.Locale.getDefault());
            return outputFormat.format(dateTime);
        } catch (Exception e) {
            android.util.Log.e("ManagerTicketDetail", "Error formatting date/time: " + dateStr + " " + timeStr, e);
            return dateStr + " " + timeStr;
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
        if (mapEnabled && mapView != null)
            mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapEnabled && mapView != null)
            mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapEnabled && mapView != null)
            mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapEnabled && mapView != null)
            mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapEnabled && mapView != null)
            mapView.onDestroy();
        android.util.Log.d("ManagerTicketDetail", "Activity destroyed");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapEnabled && mapView != null)
            mapView.onLowMemory();
    }
}