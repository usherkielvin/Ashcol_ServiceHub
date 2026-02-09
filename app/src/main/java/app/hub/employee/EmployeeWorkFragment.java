package app.hub.employee;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.TicketListResponse;
import app.hub.api.UpdateTicketStatusRequest;
import app.hub.api.UpdateTicketStatusResponse;
import app.hub.map.EmployeeMapActivity;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeWorkFragment extends Fragment implements OnMapReadyCallback {

    private SwipeRefreshLayout swipeRefreshLayout;
    private TokenManager tokenManager;
    private List<TicketListResponse.TicketItem> assignedTickets;

    private View activeContentContainer;
    private View mapContainer;
    private FrameLayout workStatusContainer;
    private FrameLayout stateOverlayContainer;
    private MapView mapViewActiveJob;
    private GoogleMap googleMap;
    private LatLng customerLatLng;
    private LatLng branchLatLng;
    private String cachedCustomerAddress;
    private String cachedBranchName;

    private TicketListResponse.TicketItem activeTicket;

    private static final String PREFS_NAME = "employee_work_steps";
    private static final int STEP_ASSIGNED = 0;
    private static final int STEP_ON_THE_WAY = 1;
    private static final int STEP_ARRIVED = 2;
    private static final int STEP_IN_PROGRESS = 3;
    private static final int STEP_COMPLETED = 4;

    private static final String EXTRA_OPEN_PAYMENT = "open_payment";

        private static final Map<String, String> BRANCH_ADDRESS_MAP = new HashMap<>();

        static {
        BRANCH_ADDRESS_MAP.put(
            "ASHCOL - CALAUAN LAGUNA",
            "Purok 4 Kalye Pogi, Brgy. Bangyas, Calauan, Laguna");
        BRANCH_ADDRESS_MAP.put(
            "ASHCOL - STA ROSA - TAGAYTAY RD BATANGAS",
            "2nd Flr Rheayanell Bldg., 9015 Pandan St., Sta. Rosa - Tagaytay Road, Brgy. Puting Kahoy, Silang, Cavite");
        BRANCH_ADDRESS_MAP.put(
            "ASHCOL - PAMPANGA",
            "202 CityCorp Business Center, San Isidro, City of San Fernando, Pampanga");
        }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_work, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadAssignedTickets();
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        activeContentContainer = view.findViewById(R.id.activeContentContainer);
        mapContainer = view.findViewById(R.id.mapContainer);
        workStatusContainer = view.findViewById(R.id.workStatusContainerInner);
        stateOverlayContainer = view.findViewById(R.id.stateOverlayContainer);
        mapViewActiveJob = view.findViewById(R.id.mapViewActiveJob);

        tokenManager = new TokenManager(getContext());
        assignedTickets = new ArrayList<>();

        setupMapView();
        setupSwipeRefresh();
    }

    private void setupSwipeRefresh() {
        // Check if fragment is still attached and context is valid
        if (!isAdded() || getContext() == null) {
            android.util.Log.w("EmployeeWork", "Fragment detached or context null, skipping swipe refresh setup");
            return;
        }

        if (swipeRefreshLayout != null) {
            // Set refresh colors
            swipeRefreshLayout.setColorSchemeResources(
                    R.color.green,
                    R.color.blue,
                    R.color.orange);

            // Set refresh listener
            swipeRefreshLayout.setOnRefreshListener(() -> {
                android.util.Log.d("EmployeeWork", "Pull-to-refresh triggered");
                loadAssignedTickets();
            });

            android.util.Log.d("EmployeeWork", "SwipeRefreshLayout configured");
        }
    }

    private void loadAssignedTickets() {
        // Check if fragment is still attached and context is valid
        if (!isAdded() || getContext() == null) {
            android.util.Log.w("EmployeeWork", "Fragment detached or context null, skipping load");
            return;
        }

        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(getContext(), "You are not logged in.", Toast.LENGTH_SHORT).show();
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            android.util.Log.e("EmployeeWork", "No token found - user not logged in");
            return;
        }

        android.util.Log.d("EmployeeWork", "Loading assigned tickets with token: "
                + (token.length() > 20 ? token.substring(0, 20) + "..." : token));

        // Check if we have user info
        String userEmail = tokenManager.getEmail();
        String userRole = tokenManager.getRole();
        String userName = tokenManager.getName();
        android.util.Log.d("EmployeeWork", "User Email: " + userEmail + ", Role: " + userRole + ", Name: " + userName);

        ApiService apiService = ApiClient.getApiService();
        Call<TicketListResponse> call = apiService.getEmployeeTickets("Bearer " + token);

        call.enqueue(new Callback<TicketListResponse>() {
            @Override
            public void onResponse(Call<TicketListResponse> call, Response<TicketListResponse> response) {
                // Check if fragment is still attached and context is valid
                if (!isAdded() || getContext() == null) {
                    android.util.Log.w("EmployeeWork", "Fragment detached or context null, ignoring response");
                    return;
                }

                swipeRefreshLayout.setRefreshing(false);

                android.util.Log.d("EmployeeWork",
                        "API Response - Success: " + response.isSuccessful() + ", Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    TicketListResponse ticketResponse = response.body();
                    android.util.Log.d("EmployeeWork",
                            "Ticket Response - Success: " + ticketResponse.isSuccess() + ", Tickets count: "
                                    + (ticketResponse.getTickets() != null ? ticketResponse.getTickets().size() : 0));

                    if (ticketResponse.isSuccess()) {
                        assignedTickets.clear();
                        if (ticketResponse.getTickets() != null) {
                            assignedTickets.addAll(ticketResponse.getTickets());
                            android.util.Log.d("EmployeeWork", "Loaded " + assignedTickets.size() + " tickets");
                        }
                        updateActiveJobUi();

                        // Don't show toast for empty list - it's normal if no tickets assigned yet
                        if (assignedTickets.isEmpty()) {
                            android.util.Log.d("EmployeeWork", "No assigned tickets found - this is normal");
                        }
                    } else {
                        String message = ticketResponse.getMessage() != null ? ticketResponse.getMessage()
                                : "Failed to load assigned tickets";
                        android.util.Log.e("EmployeeWork", "API Error: " + message);
                        // Only show error toast for actual errors, not empty lists
                        if (!message.toLowerCase().contains("no") && !message.toLowerCase().contains("empty")) {
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    String errorMessage = "Failed to load assigned tickets";
                    String errorBody = "";
                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                            android.util.Log.e("EmployeeWork", "Error body: " + errorBody);

                            // Try to parse JSON error message
                            try {
                                com.google.gson.JsonObject errorJson = new com.google.gson.Gson().fromJson(errorBody,
                                        com.google.gson.JsonObject.class);
                                if (errorJson.has("message")) {
                                    errorMessage = errorJson.get("message").getAsString();
                                }
                            } catch (Exception parseEx) {
                                // If JSON parsing fails, use raw error body
                                errorMessage = errorBody.length() > 100 ? errorBody.substring(0, 100) + "..."
                                        : errorBody;
                            }
                        } catch (Exception e) {
                            android.util.Log.e("EmployeeWork", "Error reading error body", e);
                        }
                    }

                    // Show user-friendly error based on status code
                    switch (response.code()) {
                        case 403:
                            errorMessage = "You don't have permission to view assigned tickets. Please contact your manager.";
                            break;
                        case 401:
                            errorMessage = "Session expired. Please log in again.";
                            break;
                        case 500:
                            errorMessage = "Server error. Please try again later.";
                            break;
                        default:
                            if (!errorMessage.isEmpty() && !errorMessage.equals("Failed to load assigned tickets")) {
                                // Use parsed error message
                            } else {
                                errorMessage = "Failed to load assigned tickets (Error " + response.code() + ")";
                            }
                    }

                    android.util.Log.e("EmployeeWork",
                            "HTTP Error: " + errorMessage + " (Code: " + response.code() + ")");
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<TicketListResponse> call, Throwable t) {
                // Check if fragment is still attached and context is valid
                if (!isAdded() || getContext() == null) {
                    android.util.Log.w("EmployeeWork", "Fragment detached or context null, ignoring failure");
                    return;
                }

                swipeRefreshLayout.setRefreshing(false);
                String errorMessage = "Network error: " + t.getMessage();
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                android.util.Log.e("EmployeeWork", "Network Error", t);
            }
        });
    }

    private void setupMapView() {
        if (mapViewActiveJob == null) {
            return;
        }
        mapViewActiveJob.onCreate(null);
        mapViewActiveJob.getMapAsync(this);
    }

    private void updateActiveJobUi() {
        activeTicket = findActiveTicket(assignedTickets);
        if (activeTicket == null) {
            showOverlay(R.layout.fragment_employee_work_nojob);
            return;
        }

        int step = resolveStep(activeTicket);
        if (step == STEP_COMPLETED) {
            View overlay = showOverlay(R.layout.item_employee_work_workdone);
            bindTicketDetails(overlay, activeTicket);
            setMapVisible(false);
            return;
        }

        hideOverlay();
        View statusView = showStatusLayout(getLayoutForStep(step));
        bindTicketDetails(statusView, activeTicket);
        bindStatusActions(statusView, step);
        updateMapMarker(activeTicket.getLatitude(), activeTicket.getLongitude());
    }

    private int getLayoutForStep(int step) {
        switch (step) {
            case STEP_ON_THE_WAY:
                return R.layout.item_employee_work_jobotw;
            case STEP_ARRIVED:
                return R.layout.item_employee_work_jobarrive;
            case STEP_IN_PROGRESS:
                return R.layout.item_employee_work_jobprogress;
            case STEP_ASSIGNED:
            default:
                return R.layout.fragment_employee_work_jobassign;
        }
    }

    private View showStatusLayout(int layoutResId) {
        if (workStatusContainer == null || getContext() == null) {
            return null;
        }
        workStatusContainer.removeAllViews();
        View statusView = LayoutInflater.from(getContext()).inflate(layoutResId, workStatusContainer, false);
        workStatusContainer.addView(statusView);
        return statusView;
    }

    private View showOverlay(int layoutResId) {
        if (stateOverlayContainer == null || getContext() == null) {
            return null;
        }
        stateOverlayContainer.removeAllViews();
        View overlay = LayoutInflater.from(getContext()).inflate(layoutResId, stateOverlayContainer, false);
        stateOverlayContainer.addView(overlay);
        stateOverlayContainer.setVisibility(View.VISIBLE);
        if (activeContentContainer != null) {
            activeContentContainer.setVisibility(View.GONE);
        }
        return overlay;
    }

    private void hideOverlay() {
        if (stateOverlayContainer != null) {
            stateOverlayContainer.setVisibility(View.GONE);
        }
        if (activeContentContainer != null) {
            activeContentContainer.setVisibility(View.VISIBLE);
        }
    }

    private void bindStatusActions(View statusView, int step) {
        if (statusView == null) {
            return;
        }

        if (step == STEP_ASSIGNED) {
            View onTheWay = statusView.findViewById(R.id.tvOnWayStatus);
            setStepAction(onTheWay, () -> advanceStep(STEP_ON_THE_WAY));
        } else if (step == STEP_ON_THE_WAY) {
            View arrived = statusView.findViewById(R.id.tvArrivedStatus);
            setStepAction(arrived, () -> advanceStep(STEP_ARRIVED));
        } else if (step == STEP_ARRIVED) {
            View startService = statusView.findViewById(R.id.btnStartService);
            if (startService != null) {
                startService.setOnClickListener(v -> {
                    if (activeTicket == null) {
                        return;
                    }
                    updateTicketStatus("ongoing", () -> advanceStep(STEP_IN_PROGRESS));
                });
            }
        } else if (step == STEP_IN_PROGRESS) {
            View complete = statusView.findViewById(R.id.btnPayment);
            if (complete != null) {
                complete.setOnClickListener(v -> openPaymentFlow());
            }
        }
    }

    private void setStepAction(View view, Runnable action) {
        if (view == null || action == null) {
            return;
        }
        view.setEnabled(true);
        view.setClickable(true);
        view.setOnClickListener(v -> action.run());
    }

    private void openPaymentFlow() {
        if (activeTicket == null || getContext() == null) {
            return;
        }
        Intent intent = new Intent(getContext(), EmployeeTicketDetailActivity.class);
        intent.putExtra("ticket_id", activeTicket.getTicketId());
        intent.putExtra(EXTRA_OPEN_PAYMENT, true);
        startActivity(intent);
    }

    private void bindTicketDetails(View root, TicketListResponse.TicketItem ticket) {
        if (root == null || ticket == null) {
            return;
        }

        setLabeledText(root, R.id.tvTicketId, ticket.getTicketId());
        setLabeledText(root, R.id.tvCustomerName, ticket.getCustomerName());
        setLabeledText(root, R.id.tvCustomerPhone, ticket.getContact());
        setLabeledText(root, R.id.tvPhone, ticket.getContact());
        setLabeledText(root, R.id.tvServiceType, getServiceText(ticket));
        setLabeledText(root, R.id.tvServiceName, getServiceText(ticket));
        setLabeledText(root, R.id.tvAddress, ticket.getAddress());

        String schedule = buildScheduleText(ticket.getScheduledDate(), ticket.getScheduledTime());
        setLabeledText(root, R.id.tvSchedule, schedule);
        setLabeledText(root, R.id.tvNote, ticket.getScheduleNotes());
    }

    private String getServiceText(TicketListResponse.TicketItem ticket) {
        String service = ticket.getServiceType();
        if (service == null || service.trim().isEmpty()) {
            service = ticket.getDescription();
        }
        return service;
    }

    private void setLabeledText(View root, int viewId, String value) {
        TextView view = root.findViewById(viewId);
        if (view == null) {
            return;
        }
        if (value == null || value.trim().isEmpty()) {
            view.setVisibility(View.GONE);
            return;
        }
        String existing = view.getText() != null ? view.getText().toString() : "";
        int colonIndex = existing.indexOf(":");
        if (colonIndex >= 0) {
            String prefix = existing.substring(0, colonIndex + 1);
            view.setText(prefix + " " + value);
        } else {
            view.setText(value);
        }
        view.setVisibility(View.VISIBLE);
    }

    private TicketListResponse.TicketItem findActiveTicket(List<TicketListResponse.TicketItem> tickets) {
        TicketListResponse.TicketItem best = null;
        for (TicketListResponse.TicketItem ticket : tickets) {
            if (ticket == null || ticket.getStatus() == null) {
                continue;
            }
            String status = ticket.getStatus().trim().toLowerCase();
            if (status.contains("ongoing") || status.contains("in progress")) {
                return ticket;
            }
            if (best == null && (status.contains("scheduled") || status.contains("pending"))) {
                best = ticket;
            }
        }
        return best;
    }

    private int resolveStep(TicketListResponse.TicketItem ticket) {
        if (ticket == null || ticket.getTicketId() == null) {
            return STEP_ASSIGNED;
        }

        String status = ticket.getStatus() != null ? ticket.getStatus().trim().toLowerCase() : "";
        int resolved = STEP_ASSIGNED;

        if (status.contains("completed")) {
            resolved = STEP_COMPLETED;
        } else if (status.contains("ongoing") || status.contains("in progress")) {
            resolved = STEP_IN_PROGRESS;
        } else if (status.contains("arrived")) {
            resolved = STEP_ARRIVED;
        } else if (status.contains("on the way") || status.contains("otw")) {
            resolved = STEP_ON_THE_WAY;
        }

        int saved = getSavedStep(ticket.getTicketId());
        int step = Math.max(saved, resolved);
        if (step != saved) {
            saveStep(ticket.getTicketId(), step);
        }
        return step;
    }

    private void advanceStep(int nextStep) {
        if (activeTicket == null || activeTicket.getTicketId() == null) {
            return;
        }
        saveStep(activeTicket.getTicketId(), nextStep);
        updateActiveJobUi();
    }

    private int getSavedStep(String ticketId) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        return prefs.getInt(ticketId, STEP_ASSIGNED);
    }

    private void saveStep(String ticketId, int step) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        prefs.edit().putInt(ticketId, step).apply();
    }

    private String buildScheduleText(String date, String time) {
        if (date == null && time == null) {
            return "";
        }
        if (date != null && time != null) {
            return date + " • " + time;
        }
        return date != null ? date : time;
    }

    private void updateMapMarker(double latitude, double longitude) {
        if (googleMap == null || mapViewActiveJob == null) {
            return;
        }
        setMapVisible(true);

        if (latitude != 0 && longitude != 0) {
            customerLatLng = new LatLng(latitude, longitude);
        } else {
            String address = activeTicket != null ? activeTicket.getAddress() : null;
            if (address != null && !address.equals(cachedCustomerAddress)) {
                cachedCustomerAddress = address;
                geocodeAndCacheLocation(address, true);
            }
        }

        String branchName = activeTicket != null ? activeTicket.getBranch() : null;
        if (branchName != null && !branchName.trim().isEmpty()) {
            if (!branchName.equals(cachedBranchName)) {
                cachedBranchName = branchName;
                geocodeAndCacheLocation(getBranchQuery(branchName), false);
            }
        }

        renderMapMarkers();
    }

    private void geocodeAndCacheLocation(String query, boolean isCustomer) {
        if (query == null || query.trim().isEmpty() || getContext() == null) {
            return;
        }

        new Thread(() -> {
            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(getContext(),
                        java.util.Locale.getDefault());
                java.util.List<android.location.Address> addresses = geocoder.getFromLocationName(query, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address location = addresses.get(0);
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (isCustomer) {
                                customerLatLng = new LatLng(lat, lng);
                            } else {
                                branchLatLng = new LatLng(lat, lng);
                            }
                            renderMapMarkers();
                        });
                    }
                }
            } catch (java.io.IOException ignored) {
                // Keep map as-is on failure.
            }
        }).start();
    }

    private void renderMapMarkers() {
        if (googleMap == null) {
            return;
        }

        googleMap.clear();
        int markerCount = 0;
        com.google.android.gms.maps.model.LatLngBounds.Builder boundsBuilder =
                new com.google.android.gms.maps.model.LatLngBounds.Builder();

        if (branchLatLng != null) {
            googleMap.addMarker(new MarkerOptions()
                    .position(branchLatLng)
                    .title("Branch")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            boundsBuilder.include(branchLatLng);
            markerCount++;
        }

        if (customerLatLng != null) {
            googleMap.addMarker(new MarkerOptions()
                    .position(customerLatLng)
                    .title("Customer"));
            boundsBuilder.include(customerLatLng);
            markerCount++;
        }

        if (markerCount == 0) {
            // Keep the map visible to avoid flashing/clearing when data is delayed.
            setMapVisible(true);
            return;
        }

        setMapVisible(true);
        if (markerCount == 1) {
            LatLng target = customerLatLng != null ? customerLatLng : branchLatLng;
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 15f));
        } else {
            int padding = 120;
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding));
        }
    }

    private String getBranchQuery(String branchName) {
        if (branchName == null) {
            return null;
        }
        String trimmed = branchName.trim();
        String address = BRANCH_ADDRESS_MAP.get(trimmed);
        return address != null ? address : trimmed;
    }

    private void setMapVisible(boolean visible) {
        if (mapContainer == null) {
            return;
        }
        mapContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }


    private void openMapForActiveTicket() {
        if (activeTicket == null || getContext() == null) {
            return;
        }
        double lat = activeTicket.getLatitude();
        double lng = activeTicket.getLongitude();
        if (lat == 0 || lng == 0) {
            Toast.makeText(getContext(), "Location not available", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getContext(), EmployeeMapActivity.class);
        intent.putExtra("customer_latitude", lat);
        intent.putExtra("customer_longitude", lng);
        intent.putExtra("customer_address", activeTicket.getAddress());
        intent.putExtra("ticket_id", activeTicket.getTicketId());
        startActivity(intent);
    }

    private void updateTicketStatus(String status, Runnable onSuccess) {
        String token = tokenManager.getToken();
        if (token == null || activeTicket == null) {
            return;
        }

        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest(status);
        ApiService apiService = ApiClient.getApiService();
        Call<UpdateTicketStatusResponse> call = apiService.updateTicketStatus(
                "Bearer " + token, activeTicket.getTicketId(), request);

        call.enqueue(new Callback<UpdateTicketStatusResponse>() {
            @Override
            public void onResponse(Call<UpdateTicketStatusResponse> call,
                    Response<UpdateTicketStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    activeTicket.setStatus(status);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    return;
                }
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateTicketStatusResponse> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (activeTicket != null) {
            updateMapMarker(activeTicket.getLatitude(), activeTicket.getLongitude());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapViewActiveJob != null) {
            mapViewActiveJob.onResume();
        }
        loadAssignedTickets();
    }

    @Override
    public void onPause() {
        if (mapViewActiveJob != null) {
            mapViewActiveJob.onPause();
        }
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapViewActiveJob != null) {
            mapViewActiveJob.onStart();
        }
    }

    @Override
    public void onStop() {
        if (mapViewActiveJob != null) {
            mapViewActiveJob.onStop();
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (mapViewActiveJob != null) {
            mapViewActiveJob.onDestroy();
        }
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapViewActiveJob != null) {
            mapViewActiveJob.onLowMemory();
        }
    }

    /**
     * Public method to manually refresh tickets (can be called from parent activity
     * if needed)
     */
    public void refreshTickets() {
        android.util.Log.d("EmployeeWork", "Manual refresh requested");
        loadAssignedTickets();
    }
}
