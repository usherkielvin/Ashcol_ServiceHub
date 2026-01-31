package app.hub.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

public class ManagerWorkFragment extends Fragment {

    private RecyclerView rvWorkLoadList;
    private SearchView searchViewWork;
    private FloatingActionButton filterWork;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipIncoming, chipOngoing, chipCompleted;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    private ManagerTicketsAdapter adapter;
    private TokenManager tokenManager;
    private List<TicketListResponse.TicketItem> tickets;
    private List<TicketListResponse.TicketItem> filteredTickets;
    private String currentFilter = "all";

    public ManagerWorkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_work, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupFilters();
        setupSearch();
        
        // Ensure "All" filter is selected by default
        chipAll.setChecked(true);
        currentFilter = "all";
        
        loadTickets();
    }

    private void initViews(View view) {
        rvWorkLoadList = view.findViewById(R.id.rvWorkLoadList);
        searchViewWork = view.findViewById(R.id.searchViewWork);
        filterWork = view.findViewById(R.id.filterWork);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
        chipAll = view.findViewById(R.id.chipAll);
        chipIncoming = view.findViewById(R.id.chipIncoming);
        chipOngoing = view.findViewById(R.id.chipOngoing);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        // Debug view initialization
        android.util.Log.d("ManagerWork", "RecyclerView found: " + (rvWorkLoadList != null));
        android.util.Log.d("ManagerWork", "SwipeRefreshLayout found: " + (swipeRefreshLayout != null));
        
        if (rvWorkLoadList != null) {
            android.util.Log.d("ManagerWork", "RecyclerView visibility: " + rvWorkLoadList.getVisibility());
        }
        
        tokenManager = new TokenManager(getContext());
        tickets = new ArrayList<>();
        filteredTickets = new ArrayList<>();
        
        // Setup SwipeRefreshLayout
        setupSwipeRefresh();
        
        android.util.Log.d("ManagerWork", "Lists initialized - tickets: " + tickets.size() + ", filtered: " + filteredTickets.size());
    }

    private void setupRecyclerView() {
        adapter = new ManagerTicketsAdapter(filteredTickets);
        rvWorkLoadList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWorkLoadList.setAdapter(adapter);
        
        android.util.Log.d("ManagerWork", "RecyclerView setup complete - Adapter: " + (adapter != null ? "OK" : "NULL"));
        android.util.Log.d("ManagerWork", "RecyclerView: " + (rvWorkLoadList != null ? "OK" : "NULL"));
        android.util.Log.d("ManagerWork", "Filtered tickets size: " + filteredTickets.size());

        // Set click listener for ticket items
        adapter.setOnTicketClickListener(ticket -> {
            android.util.Log.d("ManagerWork", "Ticket clicked: " + ticket.getTicketId());
            
            try {
                Intent intent = new Intent(getContext(), ManagerTicketDetailActivity.class);
                intent.putExtra("ticket_id", ticket.getTicketId());
                startActivity(intent);
            } catch (Exception e) {
                android.util.Log.e("ManagerWork", "Error starting ticket detail activity", e);
                Toast.makeText(getContext(), "Error opening ticket: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFilters() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                currentFilter = "all";
            } else if (checkedId == R.id.chipIncoming) {
                currentFilter = "pending";
            } else if (checkedId == R.id.chipOngoing) {
                currentFilter = "in_progress";
            } else if (checkedId == R.id.chipCompleted) {
                currentFilter = "completed";
            }
            
            filterTickets();
        });
    }

    private void setupSearch() {
        searchViewWork.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterTickets();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTickets();
                return true;
            }
        });
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            // Set refresh colors
            swipeRefreshLayout.setColorSchemeResources(
                R.color.green,
                R.color.blue,
                R.color.orange
            );
            
            // Set refresh listener
            swipeRefreshLayout.setOnRefreshListener(() -> {
                android.util.Log.d("ManagerWork", "Pull-to-refresh triggered");
                loadTickets();
            });
            
            android.util.Log.d("ManagerWork", "SwipeRefreshLayout configured");
        }
    }

    private void loadTickets() {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(getContext(), "You are not logged in.", Toast.LENGTH_SHORT).show();
            // Stop refresh animation if it's running
            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        // DEBUG: Log the token and user info
        android.util.Log.d("ManagerWork", "Loading tickets with token: " + token.substring(0, 10) + "...");

        ApiService apiService = ApiClient.getApiService();
        Call<TicketListResponse> call = apiService.getManagerTickets("Bearer " + token);

        call.enqueue(new Callback<TicketListResponse>() {
            @Override
            public void onResponse(Call<TicketListResponse> call, Response<TicketListResponse> response) {
                // Stop refresh animation
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                // DEBUG: Log response details
                android.util.Log.d("ManagerWork", "Response code: " + response.code());
                android.util.Log.d("ManagerWork", "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    TicketListResponse ticketResponse = response.body();
                    android.util.Log.d("ManagerWork", "Response success flag: " + ticketResponse.isSuccess());
                    android.util.Log.d("ManagerWork", "Tickets count: " + (ticketResponse.getTickets() != null ? ticketResponse.getTickets().size() : "null"));
                    
                    if (ticketResponse.isSuccess()) {
                        tickets.clear();
                        tickets.addAll(ticketResponse.getTickets());
                        android.util.Log.d("ManagerWork", "Loaded " + tickets.size() + " real tickets from API");
                        
                        // Apply filtering
                        filterTickets();
                        
                        // Show success message only if not from pull-to-refresh
                        if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
                            Toast.makeText(getContext(), "Loaded " + tickets.size() + " tickets", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        android.util.Log.e("ManagerWork", "API returned success=false");
                        Toast.makeText(getContext(), "Failed to load tickets", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    android.util.Log.e("ManagerWork", "Response not successful or body is null");
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("ManagerWork", "Error body: " + errorBody);
                        } catch (Exception e) {
                            android.util.Log.e("ManagerWork", "Could not read error body", e);
                        }
                    }
                    Toast.makeText(getContext(), "Failed to load tickets", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TicketListResponse> call, Throwable t) {
                // Stop refresh animation
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                android.util.Log.e("ManagerWork", "Network error: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTickets() {
        filteredTickets.clear();
        String searchQuery = searchViewWork.getQuery().toString().toLowerCase().trim();

        android.util.Log.d("ManagerWork", "Filtering tickets - Total: " + tickets.size() + ", Filter: " + currentFilter + ", Search: '" + searchQuery + "'");

        for (TicketListResponse.TicketItem ticket : tickets) {
            boolean matchesFilter = true;
            boolean matchesSearch = true;

            // Apply status filter
            if (!currentFilter.equals("all")) {
                String ticketStatus = ticket.getStatus().toLowerCase();
                switch (currentFilter) {
                    case "pending":
                        // Match both "pending" and "open" statuses for incoming tickets
                        matchesFilter = ticketStatus.contains("pending") || ticketStatus.contains("open");
                        break;
                    case "in_progress":
                        matchesFilter = ticketStatus.contains("progress") || ticketStatus.contains("accepted");
                        break;
                    case "completed":
                        matchesFilter = ticketStatus.contains("completed") || ticketStatus.contains("resolved") || ticketStatus.contains("closed");
                        break;
                }
            }

            // Apply search filter
            if (!searchQuery.isEmpty()) {
                matchesSearch = ticket.getTitle().toLowerCase().contains(searchQuery) ||
                               ticket.getDescription().toLowerCase().contains(searchQuery) ||
                               ticket.getTicketId().toLowerCase().contains(searchQuery);
            }

            if (matchesFilter && matchesSearch) {
                filteredTickets.add(ticket);
            }
        }

        android.util.Log.d("ManagerWork", "Filtered tickets count: " + filteredTickets.size());
        
        // Force RecyclerView to be visible and refresh
        if (rvWorkLoadList != null) {
            rvWorkLoadList.setVisibility(View.VISIBLE);
            rvWorkLoadList.post(() -> {
                adapter.notifyDataSetChanged();
                android.util.Log.d("ManagerWork", "RecyclerView forced refresh completed");
            });
        }
        
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Remove automatic refresh - users can now pull-to-refresh manually
        android.util.Log.d("ManagerWork", "Fragment resumed - no automatic refresh");
    }

    /**
     * Public method to manually refresh tickets (can be called from parent activity if needed)
     */
    public void refreshTickets() {
        android.util.Log.d("ManagerWork", "Manual refresh requested");
        loadTickets();
    }
}