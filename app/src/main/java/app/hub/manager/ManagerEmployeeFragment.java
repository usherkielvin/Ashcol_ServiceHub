package app.hub.manager;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.EmployeeResponse;
import app.hub.api.UserResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerEmployeeFragment extends Fragment {

    private TextView locationTitle, employeeCount;
    private RecyclerView rvEmployees;
    private TokenManager tokenManager;
    private EmployeeAdapter employeeAdapter;
    private List<EmployeeResponse.Employee> employeeList;

    public ManagerEmployeeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manager_employee, container, false);
        
        tokenManager = new TokenManager(getContext());
        employeeList = new ArrayList<>();
        
        initializeViews(view);
        setupRecyclerView();
        setupButtons(view);
        
        // Display data immediately if available
        displayEmployeeData();
        
        return view;
    }
    
    private void initializeViews(View view) {
        locationTitle = view.findViewById(R.id.locationTitle);
        employeeCount = view.findViewById(R.id.employeeCount);
        rvEmployees = view.findViewById(R.id.rvEmployees);
    }
    
    private void setupRecyclerView() {
        employeeAdapter = new EmployeeAdapter(employeeList);
        rvEmployees.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEmployees.setAdapter(employeeAdapter);
    }
    
    private void setupButtons(View view) {
        MaterialButton btnAddEmployee = view.findViewById(R.id.btnAddEmployee);
        if (btnAddEmployee != null) {
            btnAddEmployee.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ManagerAddEmployee.class);
                startActivity(intent);
            });
        }
    }
    
    private void displayEmployeeData() {
        if (!isFragmentReady()) {
            return;
        }
        
        // Get data from centralized manager
        String branchName = ManagerDataManager.getCachedBranchName();
        List<EmployeeResponse.Employee> employees = ManagerDataManager.getCachedEmployees();
        
        if (branchName != null && employees != null) {
            // Display cached data immediately
            locationTitle.setText(branchName);
            employeeCount.setText(employees.size() + " Employee" + (employees.size() != 1 ? "s" : ""));
            
            employeeList.clear();
            employeeList.addAll(employees);
            employeeAdapter.notifyDataSetChanged();
            
            android.util.Log.d("ManagerEmployee", "Displayed cached data: " + branchName + " with " + employees.size() + " employees");
        } else {
            // No data available yet
            locationTitle.setText("Loading...");
            employeeCount.setText("Loading...");
            android.util.Log.d("ManagerEmployee", "No cached data available");
        }
    }
    
    private boolean isFragmentReady() {
        return isAdded() && getContext() != null && 
               locationTitle != null && employeeCount != null && 
               employeeList != null && employeeAdapter != null;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Always display latest data when fragment becomes visible
        displayEmployeeData();
    }
    
    /**
     * Only call this when you actually add a new employee
     */
    public static void clearCache() {
        ManagerDataManager.clearEmployeeCache();
    }
}