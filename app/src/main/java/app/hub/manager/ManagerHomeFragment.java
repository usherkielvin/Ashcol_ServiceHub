package app.hub.manager;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import app.hub.R;
import app.hub.api.EmployeeResponse;
import app.hub.api.TicketListResponse;

public class ManagerHomeFragment extends Fragment {

    public ManagerHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manager_home, container, false);

        // Set welcome message with branch info
        updateWelcomeMessage(view);

        // Secretly refresh data in background when on home tab
        refreshDataInBackground();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update welcome message when fragment becomes visible
        if (getView() != null) {
            updateWelcomeMessage(getView());
        }
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