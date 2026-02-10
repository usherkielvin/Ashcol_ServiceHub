package app.hub.user;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.TicketListResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity tab - shows ongoing ticket with live technician tracking.
 */
public class UserNotificationFragment extends Fragment implements OnMapReadyCallback {

    private View emptyStateContainer;
    private View trackingContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TokenManager tokenManager;
    private TicketListResponse.TicketItem currentTicket;
    
    // Map and tracking views
    private GoogleMap googleMap;
    private TextView tvTechnicianName, tvTechnicianContact, tvTechnicianLocation;
    private TextView tvServiceType, tvTicketId, tvSpecificService, tvSchedule;
    private View vStepAssigned, vStepOtw, vStepArrived, vStepWorking, vStepCompleted;
    private View vConnectorAssignedOtw, vConnectorOtwArrived, vConnectorArrivedWorking, vConnectorWorkingCompleted;
    private TextView tvStepAssigned, tvStepOtw, tvStepArrived, tvStepWorking, tvStepCompleted;
    private Handler locationUpdateHandler;
    private Runnable locationUpdateRunnable;
    private FirebaseFirestore firestore;
    private ListenerRegistration ticketListener;

    public UserNotificationFragment() {
    }

    public static UserNotificationFragment newInstance() {
        return new UserNotificationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user__activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        tokenManager = new TokenManager(getContext());
        firestore = FirebaseFirestore.getInstance();

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadTickets);
        }

        loadTickets();
    }

    private void loadTickets() {
        android.util.Log.d("UserNotification", "Loading tickets...");
        
        String token = tokenManager.getToken();
        if (token == null) {
            android.util.Log.e("UserNotification", "Token is null");
            showEmptyState();
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            return;
        }

        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;
        ApiService apiService = ApiClient.getApiService();
        Call<TicketListResponse> call = apiService.getTickets(authToken);

        call.enqueue(new Callback<TicketListResponse>() {
            @Override
            public void onResponse(Call<TicketListResponse> call, Response<TicketListResponse> response) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

                android.util.Log.d("UserNotification", "API Response - Code: " + response.code() + ", Success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<TicketListResponse.TicketItem> tickets = response.body().getTickets();
                    android.util.Log.d("UserNotification", "Total tickets: " + (tickets != null ? tickets.size() : 0));
                    
                    if (tickets != null) {
                        for (TicketListResponse.TicketItem ticket : tickets) {
                            android.util.Log.d("UserNotification", "Ticket: " + ticket.getTicketId() + " - Status: " + ticket.getStatus());
                        }
                    }
                    
                    TicketListResponse.TicketItem inProgress = findInProgressTicket(tickets);
                    
                    if (inProgress != null) {
                        android.util.Log.d("UserNotification", "Found in-progress ticket: " + inProgress.getTicketId());
                        currentTicket = inProgress;
                        showTrackingView();
                    } else {
                        android.util.Log.d("UserNotification", "No in-progress ticket found, showing empty state");
                        showEmptyState();
                    }
                } else {
                    android.util.Log.e("UserNotification", "API response not successful or body is null");
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("UserNotification", "Error body: " + errorBody);
                        } catch (Exception e) {
                            android.util.Log.e("UserNotification", "Could not read error body", e);
                        }
                    }
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<TicketListResponse> call, Throwable t) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                showEmptyState();
                android.util.Log.e("UserNotification", "Failed to load activity: " + t.getMessage(), t);
            }
        });
    }

    private void showTrackingView() {
        if (getView() == null || currentTicket == null) {
            android.util.Log.e("UserNotification", "Cannot show tracking: view or ticket is null");
            return;
        }
        
        android.util.Log.d("UserNotification", "Showing tracking view for ticket: " + currentTicket.getTicketId());
        
        // Get the SwipeRefreshLayout
        SwipeRefreshLayout swipeRefresh = getView().findViewById(R.id.swipeRefreshLayout);
        if (swipeRefresh == null) {
            android.util.Log.e("UserNotification", "SwipeRefreshLayout not found");
            return;
        }
        
        // Find the FrameLayout inside SwipeRefreshLayout (skip CircleImageView)
        FrameLayout frameLayout = null;
        for (int i = 0; i < swipeRefresh.getChildCount(); i++) {
            View child = swipeRefresh.getChildAt(i);
            android.util.Log.d("UserNotification", "Child " + i + ": " + child.getClass().getSimpleName());
            if (child instanceof FrameLayout) {
                frameLayout = (FrameLayout) child;
                break;
            }
        }
        
        if (frameLayout == null) {
            android.util.Log.e("UserNotification", "FrameLayout not found in SwipeRefreshLayout");
            return;
        }
        
        android.util.Log.d("UserNotification", "FrameLayout found");
        
        // Remove RecyclerView if present
        View recyclerView = frameLayout.findViewById(R.id.rvActivity);
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
            android.util.Log.d("UserNotification", "RecyclerView hidden");
        }
        
        // Hide empty state
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisibility(View.GONE);
            android.util.Log.d("UserNotification", "Empty state hidden");
        }
        
        // Check if tracking container already exists - if so, just update data
        if (trackingContainer != null && trackingContainer.getParent() != null) {
            android.util.Log.d("UserNotification", "Tracking container already exists, updating data");
            populateTicketData();
            updateMapLocation();
            startTicketListener();
            return;
        }
        
        // Remove any existing map fragment to prevent duplicate ID error
        SupportMapFragment existingMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (existingMapFragment != null) {
            android.util.Log.d("UserNotification", "Removing existing map fragment");
            getChildFragmentManager().beginTransaction()
                    .remove(existingMapFragment)
                    .commitNow();
        }
        
        // Inflate tracking layout into FrameLayout
        trackingContainer = getLayoutInflater().inflate(R.layout.fragment_user__activity_item, frameLayout, false);
        frameLayout.addView(trackingContainer);
        
        android.util.Log.d("UserNotification", "Tracking layout inflated and added");
        
        // Initialize views
        initializeTrackingViews();
        
        // Setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            android.util.Log.d("UserNotification", "Map fragment found, getting map async");
            mapFragment.getMapAsync(this);
        } else {
            android.util.Log.e("UserNotification", "Map fragment not found!");
        }
        
        // Populate data
        populateTicketData();

        startTicketListener();
        
        // Start location updates
        startLocationUpdates();
    }

    private void initializeTrackingViews() {
        if (trackingContainer == null) return;
        
        tvTechnicianName = trackingContainer.findViewById(R.id.tvTechnicianName);
        tvTechnicianContact = trackingContainer.findViewById(R.id.tvTechnicianContact);
        tvTechnicianLocation = trackingContainer.findViewById(R.id.tvTechnicianLocation);
        tvServiceType = trackingContainer.findViewById(R.id.tvServiceType);
        tvTicketId = trackingContainer.findViewById(R.id.tvTicketId);
        tvSpecificService = trackingContainer.findViewById(R.id.tvSpecificService);
        tvSchedule = trackingContainer.findViewById(R.id.tvSchedule);

        vStepAssigned = trackingContainer.findViewById(R.id.vStepAssigned);
        vStepOtw = trackingContainer.findViewById(R.id.vStepOtw);
        vStepArrived = trackingContainer.findViewById(R.id.vStepArrived);
        vStepWorking = trackingContainer.findViewById(R.id.vStepWorking);
        vStepCompleted = trackingContainer.findViewById(R.id.vStepCompleted);
        vConnectorAssignedOtw = trackingContainer.findViewById(R.id.vConnectorAssignedOtw);
        vConnectorOtwArrived = trackingContainer.findViewById(R.id.vConnectorOtwArrived);
        vConnectorArrivedWorking = trackingContainer.findViewById(R.id.vConnectorArrivedWorking);
        vConnectorWorkingCompleted = trackingContainer.findViewById(R.id.vConnectorWorkingCompleted);
        tvStepAssigned = trackingContainer.findViewById(R.id.tvStepAssigned);
        tvStepOtw = trackingContainer.findViewById(R.id.tvStepOtw);
        tvStepArrived = trackingContainer.findViewById(R.id.tvStepArrived);
        tvStepWorking = trackingContainer.findViewById(R.id.tvStepWorking);
        tvStepCompleted = trackingContainer.findViewById(R.id.tvStepCompleted);
    }

    private void populateTicketData() {
        if (currentTicket == null) return;
        
        // Technician info
        String techName = currentTicket.getAssignedStaff();
        if (tvTechnicianName != null) {
            tvTechnicianName.setText(techName != null ? techName : "Not assigned");
        }
        if (tvTechnicianContact != null) {
            String phone = currentTicket.getAssignedStaffPhone();
            if (phone != null && !phone.trim().isEmpty()) {
                tvTechnicianContact.setText(phone.trim());
            } else {
                tvTechnicianContact.setText("Not available");
            }
        }
        if (tvTechnicianLocation != null) {
            tvTechnicianLocation.setText("En route to your location");
        }
        
        // Job details
        if (tvServiceType != null) {
            tvServiceType.setText(currentTicket.getServiceType());
        }
        if (tvTicketId != null) {
            tvTicketId.setText("Ticket ID: " + currentTicket.getTicketId());
        }
        if (tvSpecificService != null) {
            String specificService = currentTicket.getDescription();
            if (specificService == null || specificService.trim().isEmpty()) {
                specificService = "General";
            }
            tvSpecificService.setText(specificService);
        }
        if (tvSchedule != null) {
            String schedule = buildScheduleText(
                    currentTicket.getScheduledDate(),
                    currentTicket.getScheduledTime(),
                    currentTicket.getCreatedAt()
            );
            tvSchedule.setText(schedule != null ? schedule : "Not scheduled");
        }

        String status = currentTicket.getStatus();
        String statusDetail = currentTicket.getStatusDetail();
        boolean isCompleted = status != null && status.toLowerCase().contains("completed");
        updateTrackingSteps(isCompleted
            ? status
            : (statusDetail != null && !statusDetail.trim().isEmpty()
                ? statusDetail
                : status));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        updateMapLocation();
    }
    
    private void updateMapLocation() {
        if (googleMap == null || currentTicket == null) {
            android.util.Log.e("UserNotification", "Cannot update map: googleMap or ticket is null");
            return;
        }
        
        // Get customer location from ticket
        double customerLat = currentTicket.getLatitude();
        double customerLng = currentTicket.getLongitude();
        
        android.util.Log.d("UserNotification", "Ticket coordinates: lat=" + customerLat + ", lng=" + customerLng);
        
        // If no coordinates in ticket, try to geocode the address (Plus Code)
        if (customerLat == 0 && customerLng == 0) {
            String address = currentTicket.getAddress();
            android.util.Log.w("UserNotification", "No coordinates in ticket, trying to geocode address: " + address);
            
            if (address != null && !address.isEmpty()) {
                geocodeAddressAndShowMap(address);
                return;
            } else {
                // Use default location as last resort
                customerLat = 14.5995; // Default Manila
                customerLng = 120.9842;
                android.util.Log.w("UserNotification", "No address either, using default Manila location");
            }
        }
        
        showMarkersOnMap(customerLat, customerLng);
    }
    
    private void geocodeAddressAndShowMap(String address) {
        new Thread(() -> {
            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(getContext(), java.util.Locale.getDefault());
                java.util.List<android.location.Address> addresses = geocoder.getFromLocationName(address, 1);
                
                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address location = addresses.get(0);
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    
                    android.util.Log.d("UserNotification", "Geocoded address to: lat=" + lat + ", lng=" + lng);
                    
                    // Update UI on main thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> showMarkersOnMap(lat, lng));
                    }
                } else {
                    android.util.Log.e("UserNotification", "Geocoding failed - no results");
                    // Fallback to default
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> showMarkersOnMap(14.5995, 120.9842));
                    }
                }
            } catch (java.io.IOException e) {
                android.util.Log.e("UserNotification", "Geocoding error: " + e.getMessage(), e);
                // Fallback to default
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> showMarkersOnMap(14.5995, 120.9842));
                }
            }
        }).start();
    }
    
    private void showMarkersOnMap(double customerLat, double customerLng) {
        if (googleMap == null) {
            android.util.Log.e("UserNotification", "Cannot show markers: googleMap is null");
            return;
        }
        
        android.util.Log.d("UserNotification", "Showing markers at customer location: " + customerLat + ", " + customerLng);
        
        LatLng customerLocation = new LatLng(customerLat, customerLng);
        
        // Clear existing markers
        googleMap.clear();
        
        // Add customer location marker (RED)
        googleMap.addMarker(new MarkerOptions()
                .position(customerLocation)
                .title("Your Location")
                .snippet(currentTicket.getAddress())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        
        // Add technician location marker (BLUE) - simulated nearby for now
        // TODO: Replace with real-time technician location from Firebase
        double techLat = customerLat + 0.01; // Simulated nearby location
        double techLng = customerLng + 0.01;
        LatLng techLocation = new LatLng(techLat, techLng);
        
        String techName = currentTicket.getAssignedStaff();
        googleMap.addMarker(new MarkerOptions()
                .position(techLocation)
                .title("Technician: " + (techName != null ? techName : "Unknown"))
                .snippet("En route to your location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        
        // Zoom to show both markers
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(customerLocation);
        builder.include(techLocation);
        LatLngBounds bounds = builder.build();
        
        try {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            android.util.Log.d("UserNotification", "Map camera updated to show both markers");
        } catch (Exception e) {
            // If bounds calculation fails, just center on customer
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 15f));
            android.util.Log.e("UserNotification", "Error updating camera bounds, centering on customer", e);
        }
    }

    private void startLocationUpdates() {
        locationUpdateHandler = new Handler(Looper.getMainLooper());
        locationUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                // Update technician location from Firebase/Firestore
                updateTechnicianLocation();

                if (isAdded()) {
                    loadTickets();
                }
                
                // Schedule next update in 30 seconds
                locationUpdateHandler.postDelayed(this, 30000);
            }
        };
        
        // Start updates
        locationUpdateHandler.post(locationUpdateRunnable);
    }

    private void updateTechnicianLocation() {
        // TODO: Fetch real-time location from Firebase/Firestore
        // For now, just log
        android.util.Log.d("UserNotification", "Updating technician location...");
    }

    private void showEmptyState() {
        android.util.Log.d("UserNotification", "Showing empty state");
        
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            android.util.Log.d("UserNotification", "Empty state container made visible");
        } else {
            android.util.Log.e("UserNotification", "Empty state container is null!");
        }
        
        if (trackingContainer != null) {
            trackingContainer.setVisibility(View.GONE);
            android.util.Log.d("UserNotification", "Tracking container hidden");
        }

        stopTicketListener();
        
        // Also hide RecyclerView if present
        if (getView() != null) {
            View recyclerView = getView().findViewById(R.id.rvActivity);
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
            }
        }
    }

    private TicketListResponse.TicketItem findInProgressTicket(List<TicketListResponse.TicketItem> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        for (TicketListResponse.TicketItem ticket : list) {
            if (ticket == null) {
                continue;
            }
            String status = ticket.getStatus();
            if (isInProgressStatus(status)) {
                return ticket;
            }
        }
        return null;
    }

    private boolean isInProgressStatus(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.toLowerCase().trim();
        return normalized.equals("in progress")
                || normalized.equals("in-progress")
                || normalized.contains("progress")
                || normalized.equals("active")
                || normalized.equals("accepted")
                || normalized.equals("assigned")
                || normalized.equals("ongoing")
                || normalized.equals("on going");
    }

    private void updateTrackingSteps(@Nullable String status) {
        if (vStepAssigned == null || vStepOtw == null || vStepArrived == null
                || vStepWorking == null || vStepCompleted == null) {
            return;
        }

        int activeStep = resolveStepFromStatus(status);
        applyStepState(vStepAssigned, tvStepAssigned, activeStep > 1, activeStep == 1);
        applyStepState(vStepOtw, tvStepOtw, activeStep > 2, activeStep == 2);
        applyStepState(vStepArrived, tvStepArrived, activeStep > 3, activeStep == 3);
        applyStepState(vStepWorking, tvStepWorking, activeStep > 4, activeStep == 4);
        applyStepState(vStepCompleted, tvStepCompleted, activeStep >= 5, activeStep == 5);

        applyConnectorState(vConnectorAssignedOtw, activeStep > 1);
        applyConnectorState(vConnectorOtwArrived, activeStep > 2);
        applyConnectorState(vConnectorArrivedWorking, activeStep > 3);
        applyConnectorState(vConnectorWorkingCompleted, activeStep > 4);
    }

    private int resolveStepFromStatus(@Nullable String status) {
        if (status == null) {
            return 1;
        }

        String normalized = status.toLowerCase().trim().replace('_', ' ');
        if (normalized.contains("completed") || normalized.contains("done")) {
            return 5;
        }
        if (normalized.contains("in progress") || normalized.contains("ongoing")
                || normalized.contains("working") || normalized.contains("progress")) {
            return 4;
        }
        if (normalized.contains("arrived")) {
            return 3;
        }
        if (normalized.contains("on the way") || normalized.equals("otw")) {
            return 2;
        }
        return 1;
    }

    private void applyStepState(@NonNull View stepView, @Nullable TextView labelView,
            boolean completed, boolean active) {
        int green = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.status_green);
        int gray = 0xFFBDBDBD;
        int color = completed || active ? green : gray;

        stepView.setBackgroundResource(R.drawable.shape_circle_gray);
        stepView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));

        if (labelView != null) {
            labelView.setTextColor(color);
        }
    }

    private void applyConnectorState(@Nullable View connectorView, boolean active) {
        if (connectorView == null) {
            return;
        }
        int green = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.status_green);
        int gray = 0xFFBDBDBD;
        connectorView.setBackgroundColor(active ? green : gray);
    }

    @Nullable
    private String buildScheduleText(@Nullable String date, @Nullable String time, @Nullable String fallback) {
        Date parsedDateTime = parseScheduleDateTime(date, time, fallback);
        if (parsedDateTime != null) {
            SimpleDateFormat output = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault());
            return output.format(parsedDateTime);
        }

        Date parsedDate = parseScheduleDate(date);
        Date parsedTime = parseScheduleTime(time);

        if (parsedDate != null && parsedTime != null) {
            java.util.Calendar dateCal = java.util.Calendar.getInstance();
            dateCal.setTime(parsedDate);
            java.util.Calendar timeCal = java.util.Calendar.getInstance();
            timeCal.setTime(parsedTime);
            dateCal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY));
            dateCal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE));
            dateCal.set(java.util.Calendar.SECOND, 0);
            dateCal.set(java.util.Calendar.MILLISECOND, 0);
            SimpleDateFormat output = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault());
            return output.format(dateCal.getTime());
        }

        if (parsedDate != null) {
            SimpleDateFormat outputDate = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            return outputDate.format(parsedDate);
        }

        if (parsedTime != null) {
            SimpleDateFormat outputTime = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return outputTime.format(parsedTime);
        }

        if (date != null && time != null) {
            return date + " • " + time;
        }
        if (date != null && !date.trim().isEmpty()) {
            return date;
        }
        if (time != null && !time.trim().isEmpty()) {
            return time;
        }
        return fallback;
    }

    @Nullable
    private Date parseScheduleDateTime(@Nullable String date, @Nullable String time, @Nullable String fallback) {
        if (date != null && !date.trim().isEmpty()) {
            String trimmed = date.trim();
            if (time != null && !time.trim().isEmpty()) {
                String combined = trimmed + " " + time.trim();
                Date combinedParsed = tryParseDate(combined, "yyyy-MM-dd HH:mm:ss");
                if (combinedParsed != null) {
                    return combinedParsed;
                }
                combinedParsed = tryParseDate(combined, "yyyy-MM-dd HH:mm");
                if (combinedParsed != null) {
                    return combinedParsed;
                }
            }
            Date parsed = tryParseDate(trimmed, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            if (parsed != null) {
                return parsed;
            }
            parsed = tryParseDate(trimmed, "yyyy-MM-dd'T'HH:mm:ss'Z'");
            if (parsed != null) {
                return parsed;
            }
            parsed = tryParseDate(trimmed, "yyyy-MM-dd'T'HH:mm:ss");
            if (parsed != null) {
                return parsed;
            }
            parsed = tryParseDate(trimmed, "yyyy-MM-dd HH:mm:ss");
            if (parsed != null) {
                return parsed;
            }
        }
        if (fallback != null && !fallback.trim().isEmpty()) {
            Date parsed = tryParseDate(fallback.trim(), "yyyy-MM-dd HH:mm:ss");
            if (parsed != null) {
                return parsed;
            }
            parsed = tryParseDate(fallback.trim(), "yyyy-MM-dd'T'HH:mm:ss");
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    @Nullable
    private Date parseScheduleDate(@Nullable String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        Date parsed = tryParseDate(date.trim(), "yyyy-MM-dd");
        if (parsed != null) {
            return parsed;
        }
        return tryParseDate(date.trim(), "MMM d, yyyy");
    }

    @Nullable
    private Date parseScheduleTime(@Nullable String time) {
        if (time == null || time.trim().isEmpty()) {
            return null;
        }
        Date parsed = tryParseDate(time.trim(), "HH:mm:ss");
        if (parsed != null) {
            return parsed;
        }
        parsed = tryParseDate(time.trim(), "HH:mm");
        if (parsed != null) {
            return parsed;
        }
        return tryParseDate(time.trim(), "h:mm a");
    }

    @Nullable
    private Date tryParseDate(@NonNull String value, @NonNull String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
            format.setLenient(false);
            return format.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop location updates
        if (locationUpdateHandler != null && locationUpdateRunnable != null) {
            locationUpdateHandler.removeCallbacks(locationUpdateRunnable);
        }
        stopTicketListener();
    }

    private void startTicketListener() {
        if (currentTicket == null || firestore == null) {
            return;
        }

        stopTicketListener();

        String ticketId = currentTicket.getTicketId();
        if (ticketId == null || ticketId.trim().isEmpty()) {
            return;
        }

        ticketListener = firestore.collection("tickets")
                .document(ticketId)
                .addSnapshotListener((DocumentSnapshot snapshot, com.google.firebase.firestore.FirebaseFirestoreException error) -> {
                    if (error != null) {
                        android.util.Log.e("UserNotification", "Ticket listener error", error);
                        return;
                    }
                    if (snapshot == null || !snapshot.exists()) {
                        return;
                    }

                    String status = snapshot.getString("status");
                    String statusDetail = snapshot.getString("statusDetail");
                    if (currentTicket != null) {
                        if (status != null) {
                            currentTicket.setStatus(status);
                        }
                        if (statusDetail != null) {
                            currentTicket.setStatusDetail(statusDetail);
                        }
                        boolean isCompleted = status != null && status.toLowerCase().contains("completed");
                        String effective = isCompleted
                                ? status
                                : (statusDetail != null && !statusDetail.trim().isEmpty()
                                        ? statusDetail
                                        : status);
                        updateTrackingSteps(effective);
                    }
                });
    }

    private void stopTicketListener() {
        if (ticketListener != null) {
            ticketListener.remove();
            ticketListener = null;
        }
    }
}
