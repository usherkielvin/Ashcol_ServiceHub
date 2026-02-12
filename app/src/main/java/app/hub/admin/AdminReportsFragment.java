package app.hub.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.BranchReportsResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminReportsFragment extends Fragment {

    private RecyclerView rvBranches;
    private RecyclerView recyclerPayments;
    private RecyclerView recyclerComplete;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private LinearLayout cardReports;
    private LinearLayout cardPayments;
    private LinearLayout cardComplete;
    
    private BranchReportsAdapter adapter;
    private List<BranchReportsResponse.BranchReport> branchList;
    private TokenManager tokenManager;
    
    private String currentTab = "reports"; // reports, payments, complete

    public AdminReportsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_admin_reports, container, false);
            
            tokenManager = new TokenManager(requireContext());
            
            initViews(view);
            setupTabs();
            setupRecyclerView();
            loadBranchReports();
            
            return view;
        } catch (Exception e) {
            Log.e("AdminReportsFragment", "Error in onCreateView: " + e.getMessage(), e);
            return null;
        }
    }

    private void initViews(View view) {
        rvBranches = view.findViewById(R.id.rvBranches);
        recyclerPayments = view.findViewById(R.id.recyclerPayments);
        recyclerComplete = view.findViewById(R.id.recyclerComplete);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        cardReports = view.findViewById(R.id.cardReports);
        cardPayments = view.findViewById(R.id.cardPayments);
        cardComplete = view.findViewById(R.id.cardComplete);

        swipeRefreshLayout.setOnRefreshListener(this::refreshCurrentTab);
    }

    private void setupTabs() {
        cardReports.setOnClickListener(v -> showReportsTab());
        cardPayments.setOnClickListener(v -> showPaymentsTab());
        cardComplete.setOnClickListener(v -> showCompleteTab());
    }

    private void showReportsTab() {
        currentTab = "reports";
        updateTabIndicator(cardReports, true);
        updateTabIndicator(cardPayments, false);
        updateTabIndicator(cardComplete, false);
        
        rvBranches.setVisibility(View.VISIBLE);
        recyclerPayments.setVisibility(View.GONE);
        recyclerComplete.setVisibility(View.GONE);
        
        loadBranchReports();
    }

    private void showPaymentsTab() {
        currentTab = "payments";
        updateTabIndicator(cardReports, false);
        updateTabIndicator(cardPayments, true);
        updateTabIndicator(cardComplete, false);
        
        rvBranches.setVisibility(View.GONE);
        recyclerPayments.setVisibility(View.VISIBLE);
        recyclerComplete.setVisibility(View.GONE);
        
        loadPayments();
    }

    private void showCompleteTab() {
        currentTab = "complete";
        updateTabIndicator(cardReports, false);
        updateTabIndicator(cardPayments, false);
        updateTabIndicator(cardComplete, true);
        
        rvBranches.setVisibility(View.GONE);
        recyclerPayments.setVisibility(View.GONE);
        recyclerComplete.setVisibility(View.VISIBLE);
        
        loadCompleteTickets();
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

    private void refreshCurrentTab() {
        switch (currentTab) {
            case "reports":
                loadBranchReports();
                break;
            case "payments":
                loadPayments();
                break;
            case "complete":
                loadCompleteTickets();
                break;
        }
    }

    private void setupRecyclerView() {
        branchList = new ArrayList<>();
        adapter = new BranchReportsAdapter(branchList, this::onBranchClick);
        rvBranches.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBranches.setAdapter(adapter);
    }

    private void loadBranchReports() {
        showLoading(true);
        tvEmptyState.setVisibility(View.GONE);

        String token = tokenManager.getToken();
        if (token == null) {
            showError("Authentication required");
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<BranchReportsResponse> call = apiService.getBranchReports("Bearer " + token);

        call.enqueue(new Callback<BranchReportsResponse>() {
            @Override
            public void onResponse(Call<BranchReportsResponse> call, Response<BranchReportsResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BranchReportsResponse branchResponse = response.body();
                    
                    if (branchResponse.isSuccess()) {
                        List<BranchReportsResponse.BranchReport> branches = branchResponse.getBranches();
                        
                        if (branches != null && !branches.isEmpty()) {
                            branchList.clear();
                            branchList.addAll(branches);
                            adapter.updateData(branchList);
                            tvEmptyState.setVisibility(View.GONE);
                        } else {
                            tvEmptyState.setVisibility(View.VISIBLE);
                        }
                    } else {
                        showError(branchResponse.getMessage());
                    }
                } else {
                    showError("Failed to load branch reports");
                }
            }

            @Override
            public void onFailure(Call<BranchReportsResponse> call, Throwable t) {
                showLoading(false);
                Log.e("AdminReportsFragment", "API call failed: " + t.getMessage(), t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void onBranchClick(BranchReportsResponse.BranchReport branch) {
        Intent intent = new Intent(requireContext(), BranchReportDetailActivity.class);
        intent.putExtra("branch_id", branch.getId());
        intent.putExtra("branch_name", branch.getName());
        intent.putExtra("branch_location", branch.getLocation());
        startActivity(intent);
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
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPayments() {
        showLoading(true);
        tvEmptyState.setVisibility(View.GONE);
        
        // TODO: Implement payment loading from API
        // For now, show empty state
        showLoading(false);
        tvEmptyState.setText("Payment history coming soon");
        tvEmptyState.setVisibility(View.VISIBLE);
    }

    private void loadCompleteTickets() {
        showLoading(true);
        tvEmptyState.setVisibility(View.GONE);
        
        // TODO: Implement complete tickets loading from API
        // For now, show empty state
        showLoading(false);
        tvEmptyState.setText("Completed tickets coming soon");
        tvEmptyState.setVisibility(View.VISIBLE);
    }
}
