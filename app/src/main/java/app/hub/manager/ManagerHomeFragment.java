package app.hub.manager;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import app.hub.R;
import app.hub.api.DashboardStatsResponse;
import app.hub.api.EmployeeResponse;
import app.hub.api.TicketListResponse;

public class ManagerHomeFragment extends Fragment {

    private TextView tvTotalTickets, tvPendingTickets, tvInProgressTickets, tvCompletedTickets;
    private RecyclerView recentActivityRecyclerView;
    private RecentActivityAdapter recentActivityAdapter;

    public ManagerHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manager_home, container, false);

        // Initialize views
        initializeViews(view);

        // Set welcome message with branch info
        updateWelcomeMessage(view);

        // Setup RecyclerView for recent activity
        setupRecyclerView();

        // Load and display dashboard data
        loadDashboardData();

        // Secretly refresh data in background when on home tab
        refreshDataInBackground();

        return view;
    }

    private void initializeViews(View view) {
        // You may need to add these TextViews to your layout if they don't exist
        // For now, dashboard stats will be shown in recent activity section
        recentActivityRecyclerView = view.findViewById(R.id.recentActivityFlow);
    }

    private void setupRecyclerView() {
        recentActivityAdapter = new RecentActivityAdapter(getContext());
        recentActivityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recentActivityRecyclerView.setAdapter(recentActivityAdapter);
        recentActivityRecyclerView.setNestedScrollingEnabled(false);
    }

    private void loadDashboardData() {
        // Try to get cached data first
        DashboardStatsResponse.Stats stats = ManagerDataManager.getCachedDashboardStats();
        List<DashboardStatsResponse.RecentTicket> recentTickets = ManagerDataManager.getCachedRecentTickets();

        if (stats != null) {
            updateDashboardStats(stats);
        }

        if (recentTickets != null && !recentTickets.isEmpty()) {
            recentActivityAdapter.setRecentTickets(recentTickets);
        }
    }

    private void updateDashboardStats(DashboardStatsResponse.Stats stats) {
        // Log stats for now - you can add TextViews to your layout to display these
        android.util.Log.d("ManagerHome", "Total Tickets: " + stats.getTotalTickets());
        android.util.Log.d("ManagerHome", "Pending: " + stats.getPending());
        android.util.Log.d("ManagerHome", "In Progress: " + stats.getInProgress());
        android.util.Log.d("ManagerHome", "Completed: " + stats.getCompleted());
        android.util.Log.d("ManagerHome", "Cancelled: " + stats.getCancelled());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update welcome message when fragment becomes visible
        if (getView() != null) {
            updateWelcomeMessage(getView());
        }
        // Reload dashboard data
        loadDashboardData();
        // Also refresh when home tab becomes visible again
        refreshDataInBackground();
    }

    /**
     * Update welcome message with branch information
     */
    private void updateWelcomeMessage(View view) {
        android.widget.TextView tvWelcome = view.findViewById(R.id.tv_manager_welcome);
        if (tvWelcome != null) {
            String branchName = ManagerDataManager.getCachedBranchName();
            if (branchName != null && !branchName.isEmpty() && !branchName.equals("No Branch Assigned")) {
                tvWelcome.setText("Manager of " + branchName);
            } else {
                tvWelcome.setText("Manager Dashboard");
            }
        }
    }

    /**
     * Secretly refresh all manager data in background
     */
    private void refreshDataInBackground() {
        android.util.Log.d("ManagerHome", "Secretly refreshing data in background");

        ManagerDataManager.loadAllData(getContext(), new ManagerDataManager.DataLoadCallback() {
            @Override
            public void onEmployeesLoaded(String branchName, List<EmployeeResponse.Employee> employees) {
                android.util.Log.d("ManagerHome", "Background refresh: Employees loaded (" + employees.size() + ")");
            }

            @Override
            public void onTicketsLoaded(List<TicketListResponse.TicketItem> tickets) {
                android.util.Log.d("ManagerHome", "Background refresh: Tickets loaded (" + tickets.size() + ")");
            }

            @Override
            public void onDashboardStatsLoaded(DashboardStatsResponse.Stats stats,
                    List<DashboardStatsResponse.RecentTicket> recentTickets) {
                android.util.Log.d("ManagerHome", "Dashboard stats loaded in background");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateDashboardStats(stats);
                        recentActivityAdapter.setRecentTickets(recentTickets);
                    });
                }
            }

            @Override
            public void onLoadComplete() {
                android.util.Log.d("ManagerHome", "Background refresh completed - tabs are now ready");
            }

            @Override
            public void onLoadError(String error) {
                android.util.Log.e("ManagerHome", "Background refresh error: " + error);
                // Silent error - don't show to user since it's background refresh
            }
        });
    }
}
