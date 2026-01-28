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
        String token = tokenManager.getToken();
        if (token == null) {
            locationTitle.setText("Authentication Error");
            return;
        }

        // Show loading state
        locationTitle.setText("Loading...");

        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> call = apiService.getUser("Bearer " + token);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    if (userResponse.isSuccess() && userResponse.getData() != null) {
                        String branch = userResponse.getData().getBranch();
                        if (branch != null && !branch.isEmpty()) {
                            locationTitle.setText(branch);
                        } else {
                            locationTitle.setText("No Branch Assigned");
                        }
                    } else {
                        locationTitle.setText("Error Loading Branch");
                    }
                } else {
                    locationTitle.setText("Error Loading Branch");
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                locationTitle.setText("Network Error");
            }
        });
    }
    
    private void loadEmployees() {
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
                if (response.isSuccessful() && response.body() != null) {
                    EmployeeResponse employeeResponse = response.body();
                    if (employeeResponse.isSuccess() && employeeResponse.getData() != null) {
                        EmployeeResponse.Data data = employeeResponse.getData();
                        
                        // Update employee count
                        int count = data.getEmployeeCount();
                        employeeCount.setText(count + " Employee" + (count != 1 ? "s" : ""));
                        
                        // Update employee list
                        employeeList.clear();
                        if (data.getEmployees() != null) {
                            employeeList.addAll(data.getEmployees());
                        }
                        employeeAdapter.notifyDataSetChanged();
                        
                    } else {
                        employeeCount.setText("0 Employees");
                        Toast.makeText(getContext(), "Could not load employees", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    employeeCount.setText("0 Employees");
                    Toast.makeText(getContext(), "Failed to load employees", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                employeeCount.setText("0 Employees");
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh employee list when returning from add employee activity
        loadEmployees();
    }
}