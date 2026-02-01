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
        
        tokenManager = new TokenManager(getContext());
        assignedTickets = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new EmployeeTicketsAdapter(assignedTickets);
        recyclerViewAssignedTickets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAssignedTickets.setAdapter(adapter);

        // Set click listener for ticket items
        adapter.setOnTicketClickListener(ticket -> {
            Intent intent = new Intent(getContext(), EmployeeTicketDetailActivity.class);
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
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(getContext(), "You are not logged in.", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
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

