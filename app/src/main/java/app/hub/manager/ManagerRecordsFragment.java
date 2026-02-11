package app.hub.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import app.hub.api.EmployeeResponse;
import app.hub.api.PaymentHistoryResponse;
import app.hub.api.TicketListResponse;
import app.hub.util.TokenManager;
import app.hub.manager.ManagerDataManager.TicketDataChangeListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerRecordsFragment extends Fragment implements TicketDataChangeListener {

    // Sub-tab UI
    private LinearLayout cardReports;
    private LinearLayout cardPayments;
    
    // Reports content
    private RecyclerView rvReportsList;
    private SwipeRefreshLayout swipeRefreshRecords;
    private ManagerTicketsAdapter reportsAdapter;
    private List<TicketListResponse.TicketItem> allTickets;
    private List<TicketListResponse.TicketItem> filteredReports;
    private String currentReportsFilter = "all";
    
    // Payments content
    private RecyclerView recyclerPayments;
    private ProgressBar paymentsLoading;
    private TextView tvPaymentsEmpty;
    private TextView locationTitle;
    private ManagerPaymentsAdapter paymentsAdapter;
    private final List<PaymentHistoryResponse.PaymentItem> payments = new ArrayList<>();
    
    private TokenManager tokenManager;
    private boolean showingReports = true;

    public ManagerRecordsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_records_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());
        allTickets = new ArrayList<>();
        filteredReports = new ArrayList<>();

        initViews(view);
        setupSubTabs();
        setupReportsRecyclerView();
        setupPaymentsRecyclerView();
        
        // Show Reports tab by default
        showReportsTab();
        
        // Register for ticket updates
        ManagerDataManager.registerTicketListener(this);
        loadReportsData();
    }

    private void initViews(View view) {
        // Sub-tabs
        cardReports = view.findViewById(R.id.cardReports);
        cardPayments = view.findViewById(R.id.cardPayments);
        
        // Reports views
        rvReportsList = view.findViewById(R.id.rvDailyJobs);
        swipeRefreshRecords = view.findViewById(R.id.swipeRefreshRecords);
        
        // Payments views
        recyclerPayments = view.findViewById(R.id.recyclerPayments);
        paymentsLoading = view.findViewById(R.id.paymentsLoading);
        tvPaymentsEmpty = view.findViewById(R.id.tvPaymentsEmpty);
        locationTitle = view.findViewById(R.id.locationTitle);
        
        // Setup SwipeRefreshLayout
        if (swipeRefreshRecords != null) {
            swipeRefreshRecords.setColorSchemeResources(
                    R.color.green,
                    R.color.blue,
                    R.color.orange);
            swipeRefreshRecords.setOnRefreshListener(() -> {
                if (showingReports) {
                    refreshReports();
                } else {
                    loadPaymentHistory();
                }
            });
        }
    }

    private void setupSubTabs() {
        cardReports.setOnClickListener(v -> showReportsTab());
        cardPayments.setOnClickListener(v -> showPaymentsTab());
    }

    private void showReportsTab() {
        showingReports = true;
        
        // Update tab indicators
        updateTabIndicator(cardReports, true);
        updateTabIndicator(cardPayments, false);
        
        // Show/hide content
        if (rvReportsList != null) {
            rvReportsList.setVisibility(View.VISIBLE);
        }
        if (recyclerPayments != null) {
            recyclerPayments.setVisibility(View.GONE);
        }
        if (paymentsLoading != null) {
            paymentsLoading.setVisibility(View.GONE);
        }
        if (tvPaymentsEmpty != null) {
            tvPaymentsEmpty.setVisibility(View.GONE);
        }
        
        loadReportsData();
    }

    private void showPaymentsTab() {
        showingReports = false;
        
        // Update tab indicators
        updateTabIndicator(cardReports, false);
        updateTabIndicator(cardPayments, true);
        
        // Show/hide content
        if (rvReportsList != null) {
            rvReportsList.setVisibility(View.GONE);
        }
        if (recyclerPayments != null) {
            recyclerPayments.setVisibility(View.VISIBLE);
        }
        
        loadPaymentHistory();
    }

    private void updateTabIndicator(LinearLayout tab, boolean isSelected) {
        TextView textView = (TextView) ((ViewGroup) tab).getChildAt(0);
        View indicator = ((ViewGroup) tab).getChildAt(1);
        
        if (isSelected) {
            textView.setTextColor(getResources().getColor(R.color.apps_green, null));
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
            indicator.setBackgroundColor(getResources().getColor(R.color.apps_green, null));
        } else {
            textView.setTextColor(getResources().getColor(R.color.gray, null));
            textView.setTypeface(null, android.graphics.Typeface.NORMAL);
            indicator.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }
    }

    private void setupReportsRecyclerView() {
        reportsAdapter = new ManagerTicketsAdapter(filteredReports);
        rvReportsList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReportsList.setAdapter(reportsAdapter);
        
        // Set click listener for ticket items
        reportsAdapter.setOnTicketClickListener(ticket -> {
            if (ticket == null || ticket.getTicketId() == null || ticket.getTicketId().trim().isEmpty()) {
                Toast.makeText(getContext(), "Ticket ID missing", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Intent intent = new Intent(getContext(), ManagerTicketDetailActivity.class);
            intent.putExtra("ticket_id", ticket.getTicketId());
            startActivity(intent);
        });
    }

    private void setupPaymentsRecyclerView() {
        paymentsAdapter = new ManagerPaymentsAdapter(payments);
        recyclerPayments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPayments.setAdapter(paymentsAdapter);
    }

    private void loadReportsData() {
        // Get data from centralized manager
        List<TicketListResponse.TicketItem> cachedTickets = ManagerDataManager.getCachedTickets();
        
        if (cachedTickets != null && !cachedTickets.isEmpty()) {
            allTickets.clear();
            allTickets.addAll(cachedTickets);
            filterReports();
            android.util.Log.d("ManagerRecords", "Loaded " + allTickets.size() + " tickets");
        } else {
            android.util.Log.d("ManagerRecords", "No cached tickets available");
            allTickets.clear();
            filterReports();
        }
    }

    private void refreshReports() {
        ManagerDataManager.refreshTickets(getContext(), new ManagerDataManager.DataLoadCallback() {
            @Override
            public void onEmployeesLoaded(String branchName, List<EmployeeResponse.Employee> employees) {
            }

            @Override
            public void onTicketsLoaded(List<TicketListResponse.TicketItem> tickets) {
                loadReportsData();
                stopSwipeRefresh();
                Toast.makeText(getContext(), "Reports refreshed", Toast.LENGTH_SHORT).show();
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
                stopSwipeRefresh();
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterReports() {
        filteredReports.clear();
        
        for (TicketListResponse.TicketItem ticket : allTickets) {
            if (ticket == null) continue;
            
            String status = ticket.getStatus() != null ? ticket.getStatus().toLowerCase() : "";
            
            // Only show historical tickets (completed/cancelled)
            if (isHistoricalTicket(status)) {
                filteredReports.add(ticket);
            }
        }
        
        android.util.Log.d("ManagerRecords", "Filtered reports count: " + filteredReports.size());
        
        if (rvReportsList != null) {
            rvReportsList.post(() -> {
                reportsAdapter.notifyDataSetChanged();
            });
        }
    }

    private boolean isHistoricalTicket(String status) {
        if (status == null) return false;
        String normalized = status.toLowerCase().trim();
        return normalized.contains("completed") 
            || normalized.contains("resolved")
            || normalized.contains("closed")
            || normalized.contains("cancelled")
            || normalized.contains("rejected")
            || normalized.contains("failed");
    }

    private void loadPaymentHistory() {
        String token = tokenManager.getToken();
        if (token == null) {
            showPaymentsEmptyState();
            return;
        }

        if (paymentsLoading != null) {
            paymentsLoading.setVisibility(View.VISIBLE);
        }
        if (tvPaymentsEmpty != null) {
            tvPaymentsEmpty.setVisibility(View.GONE);
        }

        ApiService apiService = ApiClient.getApiService();
        Call<PaymentHistoryResponse> call = apiService.getPaymentHistory("Bearer " + token);
        call.enqueue(new Callback<PaymentHistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaymentHistoryResponse> call,
                    @NonNull Response<PaymentHistoryResponse> response) {
                stopSwipeRefresh();
                if (paymentsLoading != null) {
                    paymentsLoading.setVisibility(View.GONE);
                }

                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    showPaymentsEmptyState();
                    return;
                }

                List<PaymentHistoryResponse.PaymentItem> data = response.body().getPayments();
                payments.clear();
                if (data != null) {
                    payments.addAll(data);
                }
                paymentsAdapter.notifyDataSetChanged();

                if (payments.isEmpty()) {
                    showPaymentsEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaymentHistoryResponse> call, @NonNull Throwable t) {
                stopSwipeRefresh();
                if (paymentsLoading != null) {
                    paymentsLoading.setVisibility(View.GONE);
                }
                showPaymentsEmptyState();
            }
        });
    }

    private void showPaymentsEmptyState() {
        if (tvPaymentsEmpty != null) {
            tvPaymentsEmpty.setVisibility(View.VISIBLE);
            tvPaymentsEmpty.setText("No payment history available");
        }
    }

    private void stopSwipeRefresh() {
        if (swipeRefreshRecords != null && swipeRefreshRecords.isRefreshing()) {
            swipeRefreshRecords.setRefreshing(false);
        }
    }

    @Override
    public void onTicketDataChanged(List<TicketListResponse.TicketItem> tickets) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (showingReports) {
                    loadReportsData();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ManagerDataManager.unregisterTicketListener(this);
    }
}
