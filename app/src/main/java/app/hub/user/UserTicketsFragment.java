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
    private String currentFilter = "pending"; // Track current filter (default to pending)

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

        // Load tickets in background to refresh and sync with server (silent if we
        // already showed pending)
        Log.d(TAG, "Fragment view created, loading tickets...");
        view.post(() -> loadTickets(pending != null));
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        refreshWithTickets(updatedTickets);
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("UserTickets", "Firestore error: " + e.getMessage());
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

        // Set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        Log.d(TAG,
                "RecyclerView configured with adapter. Initial tickets count: " + tickets.size());

        // Set click listener for ticket items
        adapter.setOnTicketClickListener(ticket -> {
            Log.d(TAG, "Ticket clicked: " + ticket.getTicketId());
            Intent intent = new Intent(getContext(), TicketDetailActivity.class);
            intent.putExtra("ticket_id", ticket.getTicketId());
            startActivity(intent);
        });

        // Setup SwipeRefreshLayout
        setupSwipeRefresh();
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

                        // Clear existing tickets
                        allTickets.clear();
                        tickets.clear();

                        // Add new tickets if any
                        if (newTickets != null && !newTickets.isEmpty()) {
                            allTickets.addAll(newTickets);
                        }

                        // Apply current filter
                        filterTickets();

                        // Update adapter
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }

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
            tabRecent.setOnClickListener(v -> selectFilter("recent", tabRecent));
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

        // Set initial selection to pending only if tabPending exists
        if (tabPending != null) {
            selectFilter("pending", tabPending);
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
        selectedTab.setBackgroundResource(R.drawable.bg_status_badge);
        selectedTab.setTextColor(getResources().getColor(R.color.white));
        selectedTab.setTypeface(null, android.graphics.Typeface.BOLD);

        // Apply filter
        filterTickets();

        Log.d(TAG, "Filter selected: " + filter);
    }

    private void resetTabStyles() {
        TextView[] tabs = { tabRecent, tabPending, tabInProgress, tabCompleted };

        for (TextView tab : tabs) {
            if (tab != null) {
                tab.setBackgroundResource(R.drawable.bg_input_field);
                tab.setTextColor(getResources().getColor(R.color.dark_gray));
                tab.setTypeface(null, android.graphics.Typeface.NORMAL);
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
            if (currentFilter.equals("recent")) {
                matchesFilter = true; // Show all tickets for recent
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
                    if (normalizedFilter.equals("in progress")) {
                        matchesFilter = normalizedStatus.equals("in progress") ||
                                normalizedStatus.equals("in-progress") ||
                                normalizedStatus.contains("progress");
                    } else {
                        // Exact match for other statuses (pending, completed, etc.)
                        matchesFilter = normalizedStatus.equals(normalizedFilter);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firestoreManager != null) {
            firestoreManager.stopTicketListening();
        }
    }
}
