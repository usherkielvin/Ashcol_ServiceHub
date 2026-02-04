package app.hub.manager;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.hub.R;
import app.hub.api.DashboardStatsResponse;
import app.hub.api.EmployeeResponse;
import app.hub.api.TicketListResponse;

public class ManagerHomeFragment extends Fragment implements ManagerDataManager.EmployeeDataChangeListener {

    private RecyclerView recentActivityRecyclerView;
    private RecentActivityAdapter recentActivityAdapter;

    private RecyclerView rvEmployeePreview;
    private EmployeePreviewAdapter employeePreviewAdapter;
    private TextView tvEmployeeCount;

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

        // Load and display dashboard data AFTER RecyclerView is set up
        loadDashboardData();

        // Refresh data in background to get latest (this will update the UI when
        // complete)
        refreshDataInBackground();

        return view;
    }

    private void initializeViews(View view) {
        // You may need to add these TextViews to your layout if they don't exist
        // For now, dashboard stats will be shown in recent activity section
        recentActivityRecyclerView = view.findViewById(R.id.recentActivityFlow);
        rvEmployeePreview = view.findViewById(R.id.rvEmployeePreview);
        tvEmployeeCount = view.findViewById(R.id.tvEmployeeCount);
    }

    private void setupRecyclerView() {
        recentActivityAdapter = new RecentActivityAdapter(getContext());
        recentActivityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recentActivityRecyclerView.setAdapter(recentActivityAdapter);
        recentActivityRecyclerView.setNestedScrollingEnabled(false);

        // Setup employee preview RecyclerView
        employeePreviewAdapter = new EmployeePreviewAdapter(getContext());
        rvEmployeePreview.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEmployeePreview.setAdapter(employeePreviewAdapter);
        rvEmployeePreview.setNestedScrollingEnabled(false);

        // Register as listener for real-time employee updates
        ManagerDataManager.registerEmployeeListener(this);

        // Load employee data immediately
        loadEmployeeData();
    }

    private void loadDashboardData() {
        // Try to get cached data first
        DashboardStatsResponse.Stats stats = ManagerDataManager.getCachedDashboardStats();
        List<DashboardStatsResponse.RecentTicket> recentTickets = ManagerDataManager.getCachedRecentTickets();

        android.util.Log.d("ManagerHome", "Loading dashboard data - Stats: " + (stats != null) +
                ", Recent tickets: " + recentTickets.size());

        if (stats != null) {
            updateDashboardStats(stats);
        }

        if (!recentTickets.isEmpty()) {
            android.util.Log.d("ManagerHome", "Setting " + recentTickets.size() + " recent tickets to adapter");
            recentActivityAdapter.setRecentTickets(recentTickets);
        } else {
            android.util.Log.w("ManagerHome", "No recent tickets available to display");
            // Set empty list to clear any old data
            recentActivityAdapter.setRecentTickets(new ArrayList<>());
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
                tvWelcome.setText(branchName.toUpperCase());
            } else {
                tvWelcome.setText(R.string.default_branch_name);
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        employeePreviewAdapter.setEmployees(employees);
                        updateEmployeeCount(employees.size());
                    });
                }
            }

            @Override
            public void onTicketsLoaded(List<TicketListResponse.TicketItem> tickets) {
                android.util.Log.d("ManagerHome", "Background refresh: Tickets loaded (" + tickets.size() + ")");
            }

            @Override
            public void onDashboardStatsLoaded(DashboardStatsResponse.Stats stats,
                    List<DashboardStatsResponse.RecentTicket> recentTickets) {
                android.util.Log.d("ManagerHome", "Dashboard stats loaded in background - Recent tickets: " +
                        (recentTickets != null ? recentTickets.size() : "null"));
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateDashboardStats(stats);
                        if (recentTickets != null && !recentTickets.isEmpty()) {
                            android.util.Log.d("ManagerHome",
                                    "Updating recent activity with " + recentTickets.size() + " tickets");
                            recentActivityAdapter.setRecentTickets(recentTickets);
                        }
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

    /**
     * Load and display employee data
     */
    private void loadEmployeeData() {
        List<EmployeeResponse.Employee> employees = ManagerDataManager.getCachedEmployees();

        if (!employees.isEmpty()) {
            employeePreviewAdapter.setEmployees(employees);
            updateEmployeeCount(employees.size());
        } else {
            updateEmployeeCount(0);
        }
    }

    /**
     * Update employee count display
     */
    private void updateEmployeeCount(int count) {
        if (tvEmployeeCount != null) {
            String countText = count + (count == 1 ? " employee" : " employees");
            tvEmployeeCount.setText(countText);
        }
    }

    @Override
    public void onEmployeeDataChanged(String branchName, List<EmployeeResponse.Employee> employees) {
        // Real-time update when employee data changes
        android.util.Log.d("ManagerHome", "Employee data changed - updating preview");

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                employeePreviewAdapter.setEmployees(employees);
                updateEmployeeCount(employees.size());
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister listener to prevent memory leaks
        ManagerDataManager.unregisterEmployeeListener(this);
    }
}
