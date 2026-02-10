package app.hub.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import app.hub.common.FirestoreManager;

import java.util.ArrayList;
import java.util.List;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.TicketListResponse;
import app.hub.employee.EmployeeTicketDetailActivity;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserTicketsFragment extends Fragment {
    private static final String TAG = "UserTicketsFragment";

    private RecyclerView recyclerView;

    private SwipeRefreshLayout swipeRefreshLayout;
    private TicketsAdapter adapter;
    private TokenManager tokenManager;
    private FirestoreManager firestoreManager;
    private List<TicketListResponse.TicketItem> tickets;
    private List<TicketListResponse.TicketItem> allTickets; // Store all tickets for filtering
    private String currentFilter = "active"; // Track current filter (default to active)
    private java.util.Set<String> pendingPaymentTicketIds = new java.util.HashSet<>();
    private java.util.Set<String> paidTicketIds = new java.util.HashSet<>();

    // Filter tabs
    private TextView tabRecent, tabPending, tabInProgress, tabCompleted;
    private EditText etSearch;

    /** Pending ticket for instant display after creation (cleared after shown) */
    private static volatile TicketListResponse.TicketItem pendingNewTicket = null;

    public static void setPendingNewTicket(TicketListResponse.TicketItem ticket) {
        pendingNewTicket = ticket;
    }

    public static TicketListResponse.TicketItem getPendingNewTicket() {
        return pendingNewTicket;
    }

    public static void clearPendingNewTicket() {
        pendingNewTicket = null;
    }

    public UserTicketsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_tickets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();

        // Show newly created ticket instantly (optimistic), then load from API in
        // background
        TicketListResponse.TicketItem pending = pendingNewTicket;
        if (pending != null) {
            pendingNewTicket = null;
            allTickets.add(0, pending);
            tickets.add(0, pending);
            if (adapter != null)
                adapter.notifyItemInserted(0);
            Log.d(TAG, "Showing new ticket instantly: " + pending.getTicketId());
        }

        // Load tickets immediately to avoid delayed tab display
        Log.d(TAG, "Fragment view created, loading tickets...");
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        loadTickets(pending != null);
    }

    private void initViews(View view) {
        Log.d(TAG, "Initializing views");

        recyclerView = view.findViewById(R.id.recyclerViewTickets);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        tokenManager = new TokenManager(getContext());
        firestoreManager = new FirestoreManager(getContext());
        tickets = new ArrayList<>();
        allTickets = new ArrayList<>();

        // Start listening to Firestore
        firestoreManager.listenToMyTickets(new FirestoreManager.TicketListListener() {
            @Override
            public void onTicketsUpdated(List<TicketListResponse.TicketItem> updatedTickets) {
                if (updatedTickets == null || updatedTickets.isEmpty()) {
                    Log.d(TAG, "Firestore tickets empty; keeping current list");
                    return;
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        mergeTickets(updatedTickets);
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("UserTickets", "Firestore error: " + e.getMessage());
            }
        });

        firestoreManager.listenToPendingPayments(new FirestoreManager.PendingPaymentsListener() {
            @Override
            public void onPaymentsUpdated(java.util.List<FirestoreManager.PendingPayment> payments) {
                java.util.Set<String> ids = new java.util.HashSet<>();
                if (payments != null) {
                    for (FirestoreManager.PendingPayment payment : payments) {
                        if (payment != null && payment.ticketId != null) {
                            ids.add(payment.ticketId);
                        }
                    }
                }
                pendingPaymentTicketIds = ids;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (adapter != null) {
                            adapter.setPendingPaymentTicketIds(pendingPaymentTicketIds);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("UserTickets", "Pending payments listener error: " + e.getMessage());
            }
        });

        firestoreManager.listenToCompletedPayments(new FirestoreManager.PendingPaymentsListener() {
            @Override
            public void onPaymentsUpdated(java.util.List<FirestoreManager.PendingPayment> payments) {
                java.util.Set<String> ids = new java.util.HashSet<>();
                if (payments != null) {
                    for (FirestoreManager.PendingPayment payment : payments) {
                        if (payment != null && payment.ticketId != null) {
                            ids.add(payment.ticketId);
                        }
                    }
                }
                paidTicketIds = ids;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (adapter != null) {
                            adapter.setPaidTicketIds(paidTicketIds);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("UserTickets", "Completed payments listener error: " + e.getMessage());
            }
        });

        // Initialize filter tabs
        tabRecent = view.findViewById(R.id.tabRecent);
        tabPending = view.findViewById(R.id.tabPending);
        tabInProgress = view.findViewById(R.id.tabInProgress);
        tabCompleted = view.findViewById(R.id.tabCompleted);
        etSearch = view.findViewById(R.id.etSearch);

        // Setup filter tab click listeners
        setupFilterTabs();

        // Setup search functionality
        setupSearch();

        Log.d(TAG, "Views initialized - RecyclerView: " + (recyclerView != null) +
                ", SwipeRefresh: " + (swipeRefreshLayout != null) +
                ", TokenManager: " + (tokenManager != null));
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");

        if (recyclerView == null) {
            Log.e("UserTickets", "RecyclerView is null!");
            return;
        }

        if (tickets == null) {
            Log.e("UserTickets", "Tickets list is null!");
            tickets = new ArrayList<>();
        }

        // Initialize adapter with empty list
        adapter = new TicketsAdapter(tickets);
        adapter.setPendingPaymentTicketIds(pendingPaymentTicketIds);
        adapter.setPaidTicketIds(paidTicketIds);

        // Set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        Log.d(TAG,
                "RecyclerView configured with adapter. Initial tickets count: " + tickets.size());

        // Set click listener for ticket items
        adapter.setOnTicketClickListener(ticket -> {
            Log.d(TAG, "Ticket clicked: " + ticket.getTicketId());
            boolean isCompleted = isCompletedStatus(ticket != null ? ticket.getStatus() : null);
            Intent intent = new Intent(getContext(),
                    isCompleted ? EmployeeTicketDetailActivity.class : TicketDetailActivity.class);
            intent.putExtra("ticket_id", ticket.getTicketId());
            if (isCompleted) {
                intent.putExtra(EmployeeTicketDetailActivity.EXTRA_READ_ONLY, true);
            }
            startActivity(intent);
        });

        adapter.setOnPaymentClickListener(ticket -> {
            if (getActivity() == null) {
                return;
            }

            startActivity(UserPaymentActivity.createIntent(
                    getActivity(),
                    ticket.getTicketId(),
                    0,
                    0.0,
                    ticket.getServiceType(),
                    ticket.getAssignedStaff()));
        });

        // Setup SwipeRefreshLayout
        setupSwipeRefresh();
    }

    private boolean isCompletedStatus(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toLowerCase(java.util.Locale.ENGLISH);
        return normalized.contains("completed")
                || normalized.contains("paid")
                || normalized.contains("resolved")
                || normalized.contains("closed");
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            // Set refresh colors
            swipeRefreshLayout.setColorSchemeResources(
                    R.color.green,
                    R.color.blue,
                    R.color.orange);

            // Set refresh listener
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Log.d(TAG, "Pull-to-refresh triggered");
                loadTickets();
            });

            Log.d(TAG, "SwipeRefreshLayout configured");
        }
    }

    private void loadTickets() {
        loadTickets(false);
    }

    private void loadTickets(boolean silentRefresh) {
        String token = tokenManager.getToken();
        if (token == null) {
            Log.e("UserTickets", "No token found - user not logged in");
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        Log.d(TAG, "Loading tickets for user");

        ApiService apiService = ApiClient.getApiService();
        Call<TicketListResponse> call = apiService.getTickets("Bearer " + token);

        call.enqueue(new Callback<TicketListResponse>() {
            @Override
            public void onResponse(Call<TicketListResponse> call, Response<TicketListResponse> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                Log.d(TAG, "API Response - Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    TicketListResponse ticketResponse = response.body();

                    if (ticketResponse.isSuccess()) {
                        List<TicketListResponse.TicketItem> newTickets = ticketResponse.getTickets();
                        int ticketCount = (newTickets != null) ? newTickets.size() : 0;
                        Log.d(TAG, "Tickets received: " + ticketCount);

                        // Merge new tickets with existing ones instead of clearing
                        if (newTickets != null) {
                            mergeTickets(newTickets);
                        } else {
                            // Only clear if explicitly empty/null response which implies no tickets
                            if (allTickets != null)
                                allTickets.clear();
                            if (tickets != null)
                                tickets.clear();
                            if (adapter != null)
                                adapter.notifyDataSetChanged();
                        }

                        // Filter is already called inside mergeTickets
                        // filterTickets();

                        // Adapter update is already called inside mergeTickets
                        // if (adapter != null) {
                        // adapter.notifyDataSetChanged();
                        // }

                        // No popups/toasts here – UI updates silently
                    } else {
                        String message = ticketResponse.getMessage();
                        Log.e("UserTickets", "API returned success=false. Message: " + message);
                    }
                } else {
                    Log.e("UserTickets", "Response not successful - Code: " + response.code());

                    String errorMessage = "Failed to load tickets";
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("UserTickets", "Error body: " + errorBody);
                            errorMessage = "Server error: " + errorBody;
                        } catch (Exception e) {
                            errorMessage = "Server error (Code: " + response.code() + ")";
                        }
                    }
                    // Log only, no popup
                    Log.e("UserTickets", errorMessage);
                }
            }

            @Override
            public void onFailure(Call<TicketListResponse> call, Throwable t) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                Log.e("UserTickets", "Network error: " + t.getMessage(), t);

                String errorMessage;
                if (t instanceof java.net.ConnectException) {
                    errorMessage = "Cannot connect to server. Please check your internet connection.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Request timed out. Please try again.";
                } else if (t instanceof java.net.UnknownHostException) {
                    errorMessage = "Cannot reach server. Please check your internet connection.";
                } else {
                    errorMessage = "Network error: " + t.getMessage();
                }

                // Log only, no popup
                Log.e("UserTickets", errorMessage);
            }
        });
    }

    private void setupFilterTabs() {
        // Add null checks to prevent NullPointerException
        if (tabRecent != null) {
            tabRecent.setOnClickListener(v -> selectFilter("active", tabRecent));
        } else {
            Log.e(TAG, "tabRecent is null - check layout file for R.id.tabRecent");
        }

        if (tabPending != null) {
            tabPending.setOnClickListener(v -> selectFilter("pending", tabPending));
        } else {
            Log.e(TAG, "tabPending is null - check layout file for R.id.tabPending");
        }

        if (tabInProgress != null) {
            tabInProgress.setOnClickListener(v -> selectFilter("in progress", tabInProgress));
        } else {
            Log.e(TAG, "tabInProgress is null - check layout file for R.id.tabInProgress");
        }

        if (tabCompleted != null) {
            tabCompleted.setOnClickListener(v -> selectFilter("completed", tabCompleted));
        } else {
            Log.e(TAG, "tabCompleted is null - check layout file for R.id.tabCompleted");
        }

        // Set initial selection to Active tab
        if (tabRecent != null) {
            selectFilter("active", tabRecent);
        }
    }

    private void selectFilter(String filter, TextView selectedTab) {
        if (selectedTab == null) {
            Log.e(TAG, "selectedTab is null in selectFilter()");
            return;
        }

        currentFilter = filter;

        // Reset all tabs to inactive state
        resetTabStyles();

        // Set selected tab to active state
        selectedTab.setSelected(true);
        selectedTab.setTypeface(null, android.graphics.Typeface.BOLD);

        // Apply filter
        filterTickets();

        Log.d(TAG, "Filter selected: " + filter);
    }

    private void resetTabStyles() {
        TextView[] tabs = { tabRecent, tabPending, tabInProgress, tabCompleted };

        for (TextView tab : tabs) {
            if (tab != null) {
                tab.setSelected(false);
                tab.setTypeface(null, android.graphics.Typeface.BOLD);
            }
        }
    }

    private void setupSearch() {
        if (etSearch == null) {
            Log.e(TAG, "etSearch is null - check layout file for R.id.etSearch");
            return;
        }

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTickets();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private void filterTickets() {
        if (allTickets == null || allTickets.isEmpty()) {
            return;
        }

        String searchQuery = "";
        if (etSearch != null) {
            searchQuery = etSearch.getText().toString().toLowerCase().trim();
        }

        List<TicketListResponse.TicketItem> filteredTickets = new ArrayList<>();

        for (TicketListResponse.TicketItem ticket : allTickets) {
            boolean matchesFilter = false;
            boolean matchesSearch = true;

            // Apply status filter (exact match, case-insensitive)
            if (currentFilter.equals("all")) {
                matchesFilter = true;
            } else {
                String ticketStatus = ticket.getStatus();
                if (ticketStatus != null) {
                    String normalizedStatus = ticketStatus.toLowerCase().trim();
                    String normalizedFilter = currentFilter.toLowerCase().trim();

                    // Normalize "Open" to "Pending" for filtering (same as display logic)
                    if (normalizedStatus.equals("open")) {
                        normalizedStatus = "pending";
                    }

                    // Handle "in progress" filter matching "In Progress" status
                        if (normalizedFilter.equals("active")) {
                        matchesFilter = normalizedStatus.equals("pending") ||
                            normalizedStatus.equals("scheduled") ||
                                normalizedStatus.equals("in progress") ||
                                normalizedStatus.equals("in-progress") ||
                                normalizedStatus.contains("progress") ||
                                normalizedStatus.equals("active") ||
                                normalizedStatus.equals("accepted") ||
                                normalizedStatus.equals("assigned") ||
                                normalizedStatus.equals("ongoing");
                    } else if (normalizedFilter.equals("in progress")) {
                        matchesFilter = normalizedStatus.equals("in progress") ||
                                normalizedStatus.equals("in-progress") ||
                                normalizedStatus.contains("progress") ||
                                normalizedStatus.equals("active") ||
                                normalizedStatus.equals("accepted") ||
                                normalizedStatus.equals("assigned") ||
                                normalizedStatus.equals("ongoing");
                    } else if (normalizedFilter.equals("completed")) {
                        boolean isCompleted = normalizedStatus.equals("completed")
                                || normalizedStatus.equals("closed")
                                || normalizedStatus.equals("resolved")
                                || normalizedStatus.equals("paid");
                        String ticketId = ticket.getTicketId();
                        boolean isPaid = ticketId != null && paidTicketIds.contains(ticketId);
                        matchesFilter = isCompleted || isPaid;
                    } else {
                        // Exact match for other statuses (pending, etc.)
                        matchesFilter = normalizedStatus.equals(normalizedFilter)
                            || (normalizedFilter.equals("pending") && normalizedStatus.equals("scheduled"));

                        // Handle "cancelled" filter if we add a tab for it, or ensures it doesn't show
                        // in other tabs
                        if (normalizedFilter.equals("cancelled") || normalizedFilter.equals("rejected")) {
                            matchesFilter = normalizedStatus.equals("cancelled") || normalizedStatus.equals("rejected");
                        }
                    }
                }
            }

            // Apply search filter
            if (!searchQuery.isEmpty()) {
                String title = ticket.getTitle();
                String ticketId = ticket.getTicketId();
                String serviceType = ticket.getServiceType();
                String description = ticket.getDescription();

                matchesSearch = (title != null && title.toLowerCase().contains(searchQuery)) ||
                        (ticketId != null && ticketId.toLowerCase().contains(searchQuery)) ||
                        (serviceType != null && serviceType.toLowerCase().contains(searchQuery)) ||
                        (description != null && description.toLowerCase().contains(searchQuery));
            }

            if (matchesFilter && matchesSearch) {
                filteredTickets.add(ticket);
            }
        }

        // Update the displayed tickets
        tickets.clear();
        tickets.addAll(filteredTickets);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        Log.d(TAG, "Filtered tickets: " + filteredTickets.size() + " (filter: " + currentFilter
                + ", search: '" + searchQuery + "')");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed");
        // Refresh tickets when fragment resumes to catch updates from branch managers
        // This ensures tickets are updated when edited by branch managers
        loadTickets(true); // Silent refresh to avoid toast spam
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            Log.d(TAG, "Fragment became visible to user");
            // Refresh tickets when tab becomes visible
            loadTickets();
        }
    }

    /**
     * Public method to manually refresh tickets (can be called from parent activity
     * if needed)
     */
    public void refreshTickets() {
        Log.d(TAG, "Manual refresh requested");
        loadTickets();
    }

    /**
     * Update tickets with data from activity (called when activity pre-loads in
     * background)
     */
    public void refreshWithTickets(List<TicketListResponse.TicketItem> ticketsFromActivity) {
        if (ticketsFromActivity == null || allTickets == null)
            return;
        allTickets.clear();
        allTickets.addAll(ticketsFromActivity);
        filterTickets();
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    private void mergeTickets(List<TicketListResponse.TicketItem> incomingTickets) {
        if (incomingTickets == null || incomingTickets.isEmpty()) {
            return;
        }

        if (allTickets == null) {
            allTickets = new ArrayList<>();
        }

        if (allTickets.isEmpty()) {
            allTickets.addAll(incomingTickets);
        } else {
            for (TicketListResponse.TicketItem incoming : incomingTickets) {
                if (incoming == null) {
                    continue;
                }

                String incomingKey = getTicketKey(incoming);
                if (incomingKey == null) {
                    continue;
                }

                TicketListResponse.TicketItem existing = findTicketByKey(incomingKey);
                if (existing == null) {
                    allTickets.add(incoming);
                } else {
                    mergeTicketFields(existing, incoming);
                }
            }
        }

        filterTickets();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private TicketListResponse.TicketItem findTicketByKey(String ticketKey) {
        if (ticketKey == null || allTickets == null) {
            return null;
        }

        for (TicketListResponse.TicketItem ticket : allTickets) {
            if (ticket == null) {
                continue;
            }
            String key = getTicketKey(ticket);
            if (ticketKey.equals(key)) {
                return ticket;
            }
        }

        return null;
    }

    private String getTicketKey(TicketListResponse.TicketItem ticket) {
        if (ticket == null) {
            return null;
        }
        String ticketId = ticket.getTicketId();
        if (ticketId != null && !ticketId.isEmpty()) {
            return "ticket_id:" + ticketId;
        }
        if (ticket.getId() > 0) {
            return "id:" + ticket.getId();
        }
        return null;
    }

    private void mergeTicketFields(TicketListResponse.TicketItem target, TicketListResponse.TicketItem source) {
        if (target == null || source == null) {
            return;
        }

        if (source.getStatus() != null) {
            target.setStatus(source.getStatus());
        }
        if (source.getAssignedStaff() != null) {
            target.setAssignedStaff(source.getAssignedStaff());
        }
        if (source.getServiceType() != null) {
            target.setServiceType(source.getServiceType());
        }
        if (source.getTitle() != null) {
            target.setTitle(source.getTitle());
        }
        if (source.getDescription() != null) {
            target.setDescription(source.getDescription());
        }
        if (source.getAddress() != null) {
            target.setAddress(source.getAddress());
        }
        if (source.getContact() != null) {
            target.setContact(source.getContact());
        }
        if (source.getUpdatedAt() != null) {
            target.setUpdatedAt(source.getUpdatedAt());
        }
        if (source.getScheduledDate() != null) {
            target.setScheduledDate(source.getScheduledDate());
        }
        if (source.getScheduledTime() != null) {
            target.setScheduledTime(source.getScheduledTime());
        }
        if (source.getScheduleNotes() != null) {
            target.setScheduleNotes(source.getScheduleNotes());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firestoreManager != null) {
            firestoreManager.stopTicketListening();
        }
    }
}
