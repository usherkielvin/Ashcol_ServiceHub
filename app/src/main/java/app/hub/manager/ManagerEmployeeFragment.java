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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manager_employee, container, false);
        
        tokenManager = new TokenManager(getContext());
        employeeList = new ArrayList<>();
        
        initializeViews(view);
        setupRecyclerView();
        setupButtons(view);
        loadManagerBranch();
        loadEmployees();
        
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
    
    private void loadManagerBranch() {
        // Clear cache to get fresh data
        tokenManager.clearBranchCache();
        
        String token = tokenManager.getToken();
        if (token == null) {
            locationTitle.setText("Authentication Error");
            return;
        }

        // Show loading state
        locationTitle.setText("Loading...");
        android.util.Log.d("ManagerEmployee", "Loading manager branch info");

        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> call = apiService.getUser("Bearer " + token);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                android.util.Log.d("ManagerEmployee", "User API response - Code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    android.util.Log.d("ManagerEmployee", "User response success: " + userResponse.isSuccess());
                    
                    if (userResponse.isSuccess() && userResponse.getData() != null) {
                        String branch = userResponse.getData().getBranch();
                        android.util.Log.d("ManagerEmployee", "Manager branch: " + branch);
                        
                        if (branch != null && !branch.isEmpty()) {
                            locationTitle.setText(branch);
                            // Save to cache
                            tokenManager.saveBranchInfo(branch, 0); // Will be updated when employees load
                        } else {
                            locationTitle.setText("No Branch Assigned");
                        }
                    } else {
                        locationTitle.setText("Error Loading Branch");
                    }
                } else {
                    android.util.Log.e("ManagerEmployee", "User API response failed");
                    locationTitle.setText("Error Loading Branch");
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                android.util.Log.e("ManagerEmployee", "User API call failed", t);
                locationTitle.setText("Network Error");
            }
        });
    }
    
    private void loadEmployees() {
        // First check if we have cached employee count
        Integer cachedCount = tokenManager.getCachedEmployeeCount();
        if (cachedCount != null && cachedCount >= 0) {
            employeeCount.setText(cachedCount + " Employee" + (cachedCount != 1 ? "s" : ""));
        } else {
            employeeCount.setText("Loading...");
        }

        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(getContext(), "Authentication error", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<EmployeeResponse> call = apiService.getEmployees("Bearer " + token);

        call.enqueue(new Callback<EmployeeResponse>() {
            @Override
            public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                android.util.Log.d("ManagerEmployee", "Employee API response - Code: " + response.code());
                android.util.Log.d("ManagerEmployee", "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    EmployeeResponse employeeResponse = response.body();
                    android.util.Log.d("ManagerEmployee", "Employee response success: " + employeeResponse.isSuccess());
                    
                    if (employeeResponse.isSuccess() && employeeResponse.getEmployees() != null) {
                        List<EmployeeResponse.Employee> employees = employeeResponse.getEmployees();
                        android.util.Log.d("ManagerEmployee", "Found " + employees.size() + " employees");
                        
                        // Update employee count
                        int count = employees.size();
                        employeeCount.setText(count + " Employee" + (count != 1 ? "s" : ""));
                        
                        // Update employee list
                        employeeList.clear();
                        employeeList.addAll(employees);
                        employeeAdapter.notifyDataSetChanged();
                        
                        // Save to cache
                        String cachedBranch = tokenManager.getCachedBranch();
                        String branch = cachedBranch != null ? cachedBranch : "Unknown Branch";
                        tokenManager.saveBranchInfo(branch, count);
                        
                    } else {
                        android.util.Log.e("ManagerEmployee", "Employee response failed or no employees");
                        employeeCount.setText("0 Employees");
                        Toast.makeText(getContext(), "Could not load employees", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    android.util.Log.e("ManagerEmployee", "Employee API response not successful");
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("ManagerEmployee", "Error body: " + errorBody);
                        } catch (Exception e) {
                            android.util.Log.e("ManagerEmployee", "Could not read error body", e);
                        }
                    }
                    employeeCount.setText("0 Employees");
                    Toast.makeText(getContext(), "Failed to load employees", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                android.util.Log.e("ManagerEmployee", "Employee API call failed", t);
                // If we have cached data, keep showing it
                Integer cachedCount = tokenManager.getCachedEmployeeCount();
                if (cachedCount != null && cachedCount >= 0) {
                    employeeCount.setText(cachedCount + " Employee" + (cachedCount != 1 ? "s" : ""));
                } else {
                    employeeCount.setText("0 Employees");
                }
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Only refresh employee list when returning from add employee activity
        // Branch info will use cached data for instant loading
        loadEmployees();
    }
}