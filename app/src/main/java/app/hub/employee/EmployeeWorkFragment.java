package app.hub.employee;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

public class EmployeeWorkFragment extends Fragment {

    private RecyclerView recyclerViewAssignedTickets;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EmployeeTicketsAdapter adapter;
    private TokenManager tokenManager;
    private List<TicketListResponse.TicketItem> assignedTickets;
    
    // Tab views
    private android.widget.TextView tabAll, tabPending, tabInProgress, tabCompleted;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_work, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        loadAssignedTickets();
    }

    private void initViews(View view) {
        recyclerViewAssignedTickets = view.findViewById(R.id.recyclerViewAssignedTickets);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        // Initialize tab views
        tabAll = view.findViewById(R.id.tabAll);
        tabPending = view.findViewById(R.id.tabPending);
        tabInProgress = view.findViewById(R.id.tabInProgress);
        tabCompleted = view.findViewById(R.id.tabCompleted);
        
        tokenManager = new TokenManager(getContext());
        assignedTickets = new ArrayList<>();
        
        setupTabClickListeners();
    }

    private void setupTabClickListeners() {
        // Initially select 'All' tab
        setSelectedTab(tabAll);
        
        tabAll.setOnClickListener(v -> {
            setSelectedTab(tabAll);
            loadAssignedTickets(); // Load all tickets
        });
        
        tabPending.setOnClickListener(v -> {
            setSelectedTab(tabPending);
            loadAssignedTicketsByStatus("pending");
        });
        
        tabInProgress.setOnClickListener(v -> {
            setSelectedTab(tabInProgress);
            loadAssignedTicketsByStatus("in_progress");
        });
        
        tabCompleted.setOnClickListener(v -> {
            setSelectedTab(tabCompleted);
            loadAssignedTicketsByStatus("completed");
        });
    }
    
    private void setSelectedTab(android.widget.TextView selectedTab) {
        // Reset all tabs to unselected state
        tabAll.setBackground(getResources().getDrawable(R.drawable.bg_input_field));
        tabPending.setBackground(getResources().getDrawable(R.drawable.bg_input_field));
        tabInProgress.setBackground(getResources().getDrawable(R.drawable.bg_input_field));
        tabCompleted.setBackground(getResources().getDrawable(R.drawable.bg_input_field));
        
        // Set selected tab to active state
        selectedTab.setBackground(getResources().getDrawable(R.drawable.bg_status_badge));
        
        // Update text colors
        tabAll.setTextColor(getResources().getColor(R.color.dark_gray));
        tabPending.setTextColor(getResources().getColor(R.color.dark_gray));
        tabInProgress.setTextColor(getResources().getColor(R.color.dark_gray));
        tabCompleted.setTextColor(getResources().getColor(R.color.dark_gray));
        
        selectedTab.setTextColor(getResources().getColor(android.R.color.white));
    }
    
    private void loadAssignedTicketsByStatus(String status) {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(getContext(), "You are not logged in.", Toast.LENGTH_SHORT).show();
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }
        
        // Show progress if not refreshing
        if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
            android.util.Log.d("EmployeeWork", "Showing progress bar");
        }
        
        ApiService apiService = ApiClient.getApiService();
        Call<TicketListResponse> call = apiService.getEmployeeTicketsByStatus("Bearer " + token, status);
        
        call.enqueue(new Callback<TicketListResponse>() {
            @Override
            public void onResponse(Call<TicketListResponse> call, Response<TicketListResponse> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                android.util.Log.d("EmployeeWork", "API Response - Success: " + response.isSuccessful() + ", Code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    TicketListResponse ticketResponse = response.body();
                    android.util.Log.d("EmployeeWork", "Ticket Response - Success: " + ticketResponse.isSuccess() + ", Tickets count: " + (ticketResponse.getTickets() != null ? ticketResponse.getTickets().size() : 0));
                    
                    if (ticketResponse.isSuccess()) {
                        assignedTickets.clear();
                        if (ticketResponse.getTickets() != null) {
                            assignedTickets.addAll(ticketResponse.getTickets());
                            android.util.Log.d("EmployeeWork", "Loaded " + assignedTickets.size() + " tickets with status: " + status);
                        } else {
                            android.util.Log.d("EmployeeWork", "Tickets list is null");
                        }
                        adapter.notifyDataSetChanged();
                        
                        if (assignedTickets.isEmpty()) {
                            // Show empty state if needed
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to load tickets: " + ticketResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        android.util.Log.e("EmployeeWork", "Ticket API error: " + ticketResponse.getMessage());
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load tickets. Please try again.", Toast.LENGTH_SHORT).show();
                    android.util.Log.e("EmployeeWork", "Ticket API response not successful");
                }
            }
            
            @Override
            public void onFailure(Call<TicketListResponse> call, Throwable t) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                String errorMessage = "Network error: " + t.getMessage();
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                android.util.Log.e("EmployeeWork", "Network Error", t);
            }
        });
    }

    private void setupRecyclerView() {
        // Check if fragment is still attached and context is valid
        if (!isAdded() || getContext() == null) {
            android.util.Log.w("EmployeeWork", "Fragment detached or context null, skipping recycler view setup");
            return;
        }
        
        adapter = new EmployeeTicketsAdapter(assignedTickets);
        recyclerViewAssignedTickets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAssignedTickets.setAdapter(adapter);

        // Set click listener for ticket items
        adapter.setOnTicketClickListener(ticket -> {
            // Check if context is still valid before starting activity
            if (getContext() == null) {
                android.util.Log.w("EmployeeWork", "Context null, cannot start ticket detail activity");
                return;
            }
            
            Intent intent = new Intent(getContext(), EmployeeTicketDetailActivity.class);
            intent.putExtra("ticket_id", ticket.getTicketId());
            startActivity(intent);
        });

        // Setup SwipeRefreshLayout
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
                R.color.orange
            );
            
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

        android.util.Log.d("EmployeeWork", "Loading assigned tickets with token: " + (token.length() > 20 ? token.substring(0, 20) + "..." : token));
        
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
                
                android.util.Log.d("EmployeeWork", "API Response - Success: " + response.isSuccessful() + ", Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    TicketListResponse ticketResponse = response.body();
                    android.util.Log.d("EmployeeWork", "Ticket Response - Success: " + ticketResponse.isSuccess() + ", Tickets count: " + (ticketResponse.getTickets() != null ? ticketResponse.getTickets().size() : 0));
                    
                    if (ticketResponse.isSuccess()) {
                        assignedTickets.clear();
                        if (ticketResponse.getTickets() != null) {
                            assignedTickets.addAll(ticketResponse.getTickets());
                            android.util.Log.d("EmployeeWork", "Loaded " + assignedTickets.size() + " tickets");
                        }
                        adapter.notifyDataSetChanged();
                        
                        if (assignedTickets.isEmpty()) {
                            Toast.makeText(getContext(), "No assigned tickets found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String message = ticketResponse.getMessage() != null ? ticketResponse.getMessage() : "Failed to load assigned tickets";
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        android.util.Log.e("EmployeeWork", "API Error: " + message);
                    }
                } else {
                    String errorMessage = "Failed to load assigned tickets (Code: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            android.util.Log.e("EmployeeWork", "Error reading error body", e);
                        }
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    android.util.Log.e("EmployeeWork", "HTTP Error: " + errorMessage);
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

    @Override
    public void onResume() {
        super.onResume();
        // Remove automatic refresh - users can now pull-to-refresh manually
        android.util.Log.d("EmployeeWork", "Fragment resumed - no automatic refresh");
    }

    /**
     * Public method to manually refresh tickets (can be called from parent activity if needed)
     */
    public void refreshTickets() {
        android.util.Log.d("EmployeeWork", "Manual refresh requested");
        loadAssignedTickets();
    }
}

