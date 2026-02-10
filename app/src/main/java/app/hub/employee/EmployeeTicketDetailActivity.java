package app.hub.employee;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.TicketDetailResponse;
import app.hub.api.UpdateTicketStatusRequest;
import app.hub.api.UpdateTicketStatusResponse;
import app.hub.api.CompleteWorkRequest;
import app.hub.api.CompleteWorkResponse;
import app.hub.map.EmployeeMapActivity;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeTicketDetailActivity extends AppCompatActivity
        implements EmployeePaymentFragment.OnPaymentConfirmedListener, OnMapReadyCallback {

    private TextView tvTicketId, tvTitle, tvDescription, tvServiceType, tvAddress, tvContact, tvStatus, tvCustomerName,
            tvCreatedAt, tvScheduleDate, tvScheduleTime, tvScheduleNotes;
    private Button btnViewMap, btnBack, btnStartWork, btnCompleteWork;
    private View mapCardContainer;
    private TokenManager tokenManager;
    private String ticketId;
    private double customerLatitude, customerLongitude;
    private TicketDetailResponse.TicketDetail currentTicket;
    private FusedLocationProviderClient fusedLocationClient;

    private MapView mapView;
    private GoogleMap googleMap;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String EXTRA_OPEN_PAYMENT = "open_payment";
    public static final String EXTRA_FINISH_AFTER_PAYMENT = "finish_after_payment";

    private boolean openPaymentOnLoad = false;
    private boolean finishAfterPayment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_ticket_detail);

        initViews();

        // Initialize MapView
        mapView = findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        setupClickListeners();

        tokenManager = new TokenManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ticketId = getIntent().getStringExtra("ticket_id");
        openPaymentOnLoad = getIntent().getBooleanExtra(EXTRA_OPEN_PAYMENT, false);
        finishAfterPayment = getIntent().getBooleanExtra(EXTRA_FINISH_AFTER_PAYMENT, false);

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
        mapCardContainer = findViewById(R.id.mapCardContainer);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnViewMap.setOnClickListener(v -> {
            if (customerLatitude != 0 && customerLongitude != 0) {
                // Check location permission before opening map
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                            LOCATION_PERMISSION_REQUEST_CODE);
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

        btnStartWork.setOnClickListener(v -> updateTicketStatus("ongoing"));
        btnCompleteWork.setOnClickListener(v -> showPaymentFragment());
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
                        Toast.makeText(EmployeeTicketDetailActivity.this, "Failed to load ticket details",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(EmployeeTicketDetailActivity.this, "Failed to load ticket details",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<TicketDetailResponse> call, Throwable t) {
                Toast.makeText(EmployeeTicketDetailActivity.this, "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
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
        tvCustomerName
                .setText("Customer: " + (ticket.getCustomerName() != null ? ticket.getCustomerName() : "Unknown"));
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

        // Update map if ready and coordinates are valid
        updateMapLocation();

        // Show/hide action buttons based on ticket status
        updateActionButtons(ticket.getStatus());

        if (openPaymentOnLoad) {
            openPaymentOnLoad = false;
            showPaymentFragment();
        }
    }

    private void updateActionButtons(String status) {
        if (status == null)
            return;

        switch (status.toLowerCase()) {
            case "accepted":
            case "assigned":
            case "scheduled":
                btnStartWork.setVisibility(View.VISIBLE);
                btnCompleteWork.setVisibility(View.GONE);
                break;
            case "in progress":
            case "ongoing":
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
            public void onResponse(Call<UpdateTicketStatusResponse> call,
                    Response<UpdateTicketStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UpdateTicketStatusResponse statusResponse = response.body();
                    if (statusResponse.isSuccess()) {
                        String message = status.equals("ongoing") ? "Work started successfully"
                                : "Work completed successfully";
                        Toast.makeText(EmployeeTicketDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadTicketDetails(); // Refresh ticket details
                    } else {
                        Toast.makeText(EmployeeTicketDetailActivity.this, "Failed to update ticket status",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EmployeeTicketDetailActivity.this, "Failed to update ticket status",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateTicketStatusResponse> call, Throwable t) {
                Toast.makeText(EmployeeTicketDetailActivity.this, "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }

        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd",
                    java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd, yyyy",
                    java.util.Locale.getDefault());

            java.util.Date date = inputFormat.parse(dateString);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (java.text.ParseException e) {
            return dateString;
        }

        return dateString;
    }

    private String formatTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return "";
        }

        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("HH:mm",
                    java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("h:mm a",
                    java.util.Locale.getDefault());

            java.util.Date time = inputFormat.parse(timeString);
            if (time != null) {
                return outputFormat.format(time);
            }
        } catch (java.text.ParseException e) {
            return timeString;
        }

        return timeString;
    }

    private void setStatusColor(TextView textView, String status, String statusColor) {
        if (statusColor != null && !statusColor.isEmpty()) {
            try {
                textView.setTextColor(Color.parseColor(statusColor));
                return;
            } catch (IllegalArgumentException e) {
            }
        }

        if (status == null)
            return;

        switch (status.toLowerCase()) {
            case "pending":
                textView.setTextColor(Color.parseColor("#FFA500"));
                break;
            case "scheduled":
                textView.setTextColor(Color.parseColor("#6366F1"));
                break;
            case "accepted":
            case "in progress":
            case "ongoing":
                textView.setTextColor(Color.parseColor("#2196F3"));
                break;
            case "completed":
                textView.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case "cancelled":
            case "rejected":
                textView.setTextColor(Color.parseColor("#F44336"));
                break;
            default:
                textView.setTextColor(Color.parseColor("#757575"));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                btnViewMap.performClick();
            } else {
                Toast.makeText(this, "Location permission is required to view map", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPaymentFragment() {
        String customerName = currentTicket != null ? currentTicket.getCustomerName() : null;
        String serviceName = currentTicket != null ? currentTicket.getServiceType() : null;
        EmployeePaymentFragment fragment = EmployeePaymentFragment.newInstance(
            ticketId,
            customerName,
            serviceName,
            0.0);
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPaymentConfirmed(String paymentMethod, double amount, String notes) {
        completeWorkWithPayment(paymentMethod, amount, notes);
    }

    private void completeWorkWithPayment(String paymentMethod, double amount, String notes) {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCompleteWork.setEnabled(false);
        btnCompleteWork.setText("Processing...");

        CompleteWorkRequest request = new CompleteWorkRequest(paymentMethod, amount, notes);
        ApiService apiService = ApiClient.getApiService();
        Call<CompleteWorkResponse> call = apiService.completeWorkWithPayment("Bearer " + token, ticketId, request);

        call.enqueue(new Callback<CompleteWorkResponse>() {
            @Override
            public void onResponse(Call<CompleteWorkResponse> call, Response<CompleteWorkResponse> response) {
                btnCompleteWork.setEnabled(true);
                btnCompleteWork.setText("Complete Work");

                if (response.isSuccessful() && response.body() != null) {
                    CompleteWorkResponse workResponse = response.body();
                    if (workResponse.isSuccess()) {
                        String message = "Work completed successfully!";
                        if ("cash".equals(paymentMethod)) {
                            message += "\nPayment collected: ₱" + String.format("%.2f", amount);
                        }
                        Toast.makeText(EmployeeTicketDetailActivity.this, message, Toast.LENGTH_LONG).show();
                        Runnable finishAction = () -> {
                            if (finishAfterPayment) {
                                Intent result = new Intent();
                                result.putExtra("ticket_id", ticketId);
                                setResult(RESULT_OK, result);
                                finish();
                            } else {
                                loadTicketDetails();
                            }
                        };

                        if ("cash".equals(paymentMethod)) {
                            int paymentId = workResponse.getPayment() != null
                                    ? workResponse.getPayment().getId()
                                    : 0;
                            if (paymentId > 0) {
                                submitPaymentToManager(paymentId, finishAction);
                            } else {
                                finishAction.run();
                            }
                        } else {
                            finishAction.run();
                        }
                    } else {
                        Toast.makeText(EmployeeTicketDetailActivity.this, "Failed: " + workResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(EmployeeTicketDetailActivity.this, "Failed to complete work", Toast.LENGTH_LONG)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<CompleteWorkResponse> call, Throwable t) {
                btnCompleteWork.setEnabled(true);
                btnCompleteWork.setText("Complete Work");
                Toast.makeText(EmployeeTicketDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    private void submitPaymentToManager(int paymentId, Runnable onComplete) {
        String token = tokenManager.getToken();
        if (token == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<CompleteWorkResponse> call = apiService.submitPaymentToManager("Bearer " + token, paymentId);
        call.enqueue(new Callback<CompleteWorkResponse>() {
            @Override
            public void onResponse(Call<CompleteWorkResponse> call, Response<CompleteWorkResponse> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(EmployeeTicketDetailActivity.this,
                            "Payment submitted, but manager sync failed.",
                            Toast.LENGTH_SHORT).show();
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onFailure(Call<CompleteWorkResponse> call, Throwable t) {
                Toast.makeText(EmployeeTicketDetailActivity.this,
                        "Payment submitted, but manager sync failed.",
                        Toast.LENGTH_SHORT).show();
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    private void updateMapLocation() {
        if (googleMap == null)
            return;

        if (customerLatitude != 0 && customerLongitude != 0) {
            // Use explicit coordinates
            showLocationOnMap(customerLatitude, customerLongitude);
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
            LatLng location = new LatLng(lat, lng);
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(location).title("Service Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
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
                        this.customerLatitude = lat;
                        this.customerLongitude = lng;
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
    public void onMapReady(GoogleMap map) {
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
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null)
            mapView.onLowMemory();
    }
}