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
        
        // Secretly refresh data in background when on home tab
        refreshDataInBackground();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Also refresh when home tab becomes visible again
        refreshDataInBackground();
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