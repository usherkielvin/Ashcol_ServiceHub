package app.hub.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.BranchTicketsResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BranchReportDetailActivity extends AppCompatActivity {

    private TextView tvBranchName;
    private TabLayout tabLayout;
    private RecyclerView rvTickets;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    
    private BranchTicketsAdapter adapter;
    private List<BranchTicketsResponse.Ticket> ticketList;
    private TokenManager tokenManager;
    
    private int branchId;
    private String branchName;
    private String branchLocation;
    private String currentStatus = "completed"; // Default to completed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_report_detail);

        tokenManager = new TokenManager(this);
        
        // Get branch data from intent
        branchId = getIntent().getIntExtra("branch_id", 0);
        branchName = getIntent().getStringExtra("branch_name");
        branchLocation = getIntent().getStringExtra("branch_location");

        setupToolbar();
        initViews();
        setupRecyclerView();
        setupTabs();
        loadTickets();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        tvBranchName = findViewById(R.id.tvBranchName);
        tabLayout = findViewById(R.id.tabLayout);
        rvTickets = findViewById(R.id.rvTickets);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        tvBranchName.setText(branchName);
        swipeRefreshLayout.setOnRefreshListener(this::loadTickets);
    }

    private void setupRecyclerView() {
        ticketList = new ArrayList<>();
        adapter = new BranchTicketsAdapter(ticketList);
        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        rvTickets.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentStatus = tab.getPosition() == 0 ? "completed" : "cancelled";
                loadTickets();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void loadTickets() {
        showLoading(true);
        tvEmptyState.setVisibility(View.GONE);

        String token = tokenManager.getToken();
        if (token == null) {
            showError("Authentication required");
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<BranchTicketsResponse> call = apiService.getBranchTickets(
                "Bearer " + token, 
                branchId, 
                currentStatus
        );

        call.enqueue(new Callback<BranchTicketsResponse>() {
            @Override
            public void onResponse(Call<BranchTicketsResponse> call, Response<BranchTicketsResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BranchTicketsResponse ticketsResponse = response.body();
                    
                    if (ticketsResponse.isSuccess()) {
                        List<BranchTicketsResponse.Ticket> tickets = ticketsResponse.getTickets();
                        
                        if (tickets != null && !tickets.isEmpty()) {
                            ticketList.clear();
                            ticketList.addAll(tickets);
                            adapter.updateData(ticketList);
                            tvEmptyState.setVisibility(View.GONE);
                        } else {
                            ticketList.clear();
                            adapter.updateData(ticketList);
                            tvEmptyState.setVisibility(View.VISIBLE);
                        }
                    } else {
                        showError(ticketsResponse.getMessage());
                    }
                } else {
                    showError("Failed to load tickets");
                }
            }

            @Override
            public void onFailure(Call<BranchTicketsResponse> call, Throwable t) {
                showLoading(false);
                Log.e("BranchReportDetail", "API call failed: " + t.getMessage(), t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        } else {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
