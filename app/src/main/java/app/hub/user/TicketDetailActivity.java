package app.hub.user;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.OnMapReadyCallback;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.TicketDetailResponse;
import app.hub.map.MapViewActivity;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Locale;

public class TicketDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView tvTicketId, tvTitle, tvDescription, tvServiceType, tvAddress, tvContact, tvStatus, tvBranch,
            tvAssignedStaff, tvCreatedAt;
    private Button btnViewMap, btnBack;
    private View mapCardContainer;
    private TokenManager tokenManager;
    private String ticketId;
    private double latitude, longitude;

    private com.google.android.gms.maps.MapView mapView;
    private com.google.android.gms.maps.GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        initViews();

        // Initialize MapView
        mapView = findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

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
        mapCardContainer = findViewById(R.id.mapCardContainer);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnViewMap.setOnClickListener(v -> {
            if (latitude != 0 && longitude != 0) {
                // Open in external map app (Google Maps)
                openInMapApp(latitude, longitude, tvAddress.getText().toString());
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
                        Toast.makeText(TicketDetailActivity.this, "Failed to load ticket details", Toast.LENGTH_SHORT)
                                .show();
                        finish();
                    }
                } else {
                    Toast.makeText(TicketDetailActivity.this, "Failed to load ticket details", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<TicketDetailResponse> call, Throwable t) {
                Toast.makeText(TicketDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT)
                        .show();
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
        tvAssignedStaff.setText(
            "Assigned technician: "
                + (ticket.getAssignedStaff() != null ? ticket.getAssignedStaff() : "Not assigned"));
        tvCreatedAt.setText("Created: " + ticket.getCreatedAt());

        // Set status color
        setStatusColor(tvStatus, ticket.getStatus(), ticket.getStatusColor());

        // Store coordinates for map viewing
        latitude = ticket.getLatitude();
        longitude = ticket.getLongitude();

        // Update map if ready and coordinates are valid
        updateMapLocation();

        // Hide assigned technician if not assigned
        if (ticket.getAssignedStaff() == null || ticket.getAssignedStaff().isEmpty()) {
            tvAssignedStaff.setVisibility(View.GONE);
        }
    }

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
                        // Update the stored coordinates so the "View Map" button works correctly
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
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null)
            mapView.onLowMemory();
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
            case "accepted":
            case "in progress":
            case "in-progress":
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

    private void openInMapApp(double latitude, double longitude, String address) {
        try {
            // Try to open in Google Maps first
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", 
                                     latitude, longitude, latitude, longitude, 
                                     android.net.Uri.encode(address));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
            mapIntent.setPackage("com.google.android.apps.maps");
            
            // Check if Google Maps is installed
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback to any map app
                String fallbackUri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", 
                                                 latitude, longitude, latitude, longitude, 
                                                 android.net.Uri.encode(address));
                Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(fallbackUri));
                
                if (fallbackIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(fallbackIntent);
                } else {
                    // Last resort - open in browser with Google Maps
                    String webUri = String.format(Locale.ENGLISH, 
                                                "https://www.google.com/maps?q=%f,%f", 
                                                latitude, longitude);
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(webUri));
                    startActivity(webIntent);
                }
            }
        } catch (Exception e) {
            Log.e("TicketDetailActivity", "Error opening map", e);
            Toast.makeText(this, "Unable to open map app", Toast.LENGTH_SHORT).show();
        }
    }
}