package app.hub.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.EmployeeResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeFragment extends Fragment {
    
    private LinearLayout branchPreviewLayout;
    private RecyclerView managersRecyclerView;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);
        
        setupViews(view);
        setupButtons(view);
        loadPreviewData();
        
        return view;
    }
    
    private void setupViews(View view) {
        branchPreviewLayout = view.findViewById(R.id.branchPreviewLayout);
        managersRecyclerView = view.findViewById(R.id.managersRecyclerView);
        
        // Setup managers RecyclerView
        if (managersRecyclerView != null) {
            managersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            managersRecyclerView.setAdapter(new ManagersAdapter(new ArrayList<>(), null));
        }
    }
    
    private void setupButtons(View view) {
        // View All Managers text click listener - Navigate to Operations tab (Manager)
        TextView btnViewAllManagers = view.findViewById(R.id.btnViewAllManagers);
        if (btnViewAllManagers != null) {
            btnViewAllManagers.setOnClickListener(v -> {
                try {
                    // Navigate to Operations tab and show Manager tab
                    if (getActivity() instanceof AdminDashboardActivity) {
                        AdminDashboardActivity activity = (AdminDashboardActivity) getActivity();
                        activity.navigateToOperationsTab(true); // true = show manager tab
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error navigating to managers: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        }
        
        // View All Branches text click listener - Navigate to Operations tab (Branch)
        TextView viewAllBranches = view.findViewById(R.id.viewAllBranches);
        if (viewAllBranches != null) {
            viewAllBranches.setOnClickListener(v -> {
                try {
                    // Navigate to Operations tab and show Branch tab
                    if (getActivity() instanceof AdminDashboardActivity) {
                        AdminDashboardActivity activity = (AdminDashboardActivity) getActivity();
                        activity.navigateToOperationsTab(false); // false = show branch tab
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error navigating to branches: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        }
    }
    
    private void loadPreviewData() {
        android.util.Log.d("AdminHome", "Loading preview data from API...");
        
        TokenManager tokenManager = new TokenManager(getContext());
        String token = tokenManager.getToken();
        
        if (token == null) {
            android.util.Log.e("AdminHome", "No token available");
            showErrorInBranchPreview("Error: Not authenticated");
            return;
        }

        // Load branches data for preview
        ApiService apiService = ApiClient.getApiService();
        Call<app.hub.api.BranchResponse> call = apiService.getBranches("Bearer " + token);
        
        call.enqueue(new Callback<app.hub.api.BranchResponse>() {
            @Override
            public void onResponse(Call<app.hub.api.BranchResponse> call, Response<app.hub.api.BranchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    app.hub.api.BranchResponse branchResponse = response.body();
                    
                    if (branchResponse.isSuccess()) {
                        processBranchPreviewData(branchResponse.getBranches());
                        loadEmployeesForManagerPreview(tokenManager.getToken());
                    } else {
                        android.util.Log.e("AdminHome", "Branches API returned success=false: " + branchResponse.getMessage());
                        showErrorInBranchPreview("Error loading branches");
                    }
                } else {
                    android.util.Log.e("AdminHome", "Branches API response not successful");
                    showErrorInBranchPreview("Error loading branches");
                }
            }
            
            @Override
            public void onFailure(Call<app.hub.api.BranchResponse> call, Throwable t) {
                android.util.Log.e("AdminHome", "Branches API call failed: " + t.getMessage(), t);
                showErrorInBranchPreview("Error loading branches");
            }
        });
    }

    private void processBranchPreviewData(List<app.hub.api.BranchResponse.Branch> branches) {
        // Sort branches by employee count (descending) to show most active branches first
        branches.sort((a, b) -> Integer.compare(b.getEmployeeCount(), a.getEmployeeCount()));
        
        // Update UI on main thread
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                updateBranchPreview(branches);
            });
        }
    }

    private void updateBranchPreview(List<app.hub.api.BranchResponse.Branch> branches) {
        if (branchPreviewLayout != null) {
            branchPreviewLayout.removeAllViews();
            
            // Show top 3 branches or all if less than 3
            int maxBranches = Math.min(3, branches.size());
            int activeBranches = 0;
            
            for (int i = 0; i < branches.size() && activeBranches < maxBranches; i++) {
                app.hub.api.BranchResponse.Branch branch = branches.get(i);
                
                // Only show branches with employees
                if (branch.getEmployeeCount() > 0) {
                    createBranchPreviewItem(branch, activeBranches == 0);
                    activeBranches++;
                }
            }
            
            if (activeBranches == 0) {
                // No active branches
                TextView noBranchesText = new TextView(getContext());
                noBranchesText.setText("📍 No active branches yet");
                noBranchesText.setTextColor(getResources().getColor(android.R.color.darker_gray));
                noBranchesText.setTextSize(14);
                noBranchesText.setPadding(32, 16, 32, 16);
                noBranchesText.setBackgroundResource(R.drawable.bg_input_field);
                branchPreviewLayout.addView(noBranchesText);
            }
        }
    }

    private void createBranchPreviewItem(app.hub.api.BranchResponse.Branch branch, boolean isFirst) {
        // Create branch item container
        LinearLayout branchItem = new LinearLayout(getContext());
        branchItem.setOrientation(LinearLayout.VERTICAL);
        branchItem.setBackgroundResource(R.drawable.bg_input_field);
        branchItem.setPadding(32, 24, 32, 24);
        
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (!isFirst) {
            layoutParams.topMargin = 16;
        }
        branchItem.setLayoutParams(layoutParams);
        
        // Branch name
        TextView branchNameText = new TextView(getContext());
        String displayName = branch.getName().replace("ASHCOL ", ""); // Remove ASHCOL prefix for cleaner display
        branchNameText.setText(displayName);
        branchNameText.setTextColor(getResources().getColor(android.R.color.black));
        branchNameText.setTextSize(16);
        branchNameText.setTypeface(null, android.graphics.Typeface.BOLD);
        branchItem.addView(branchNameText);
        
        // Location
        TextView locationText = new TextView(getContext());
        locationText.setText("📍 " + (branch.getAddress() != null ? branch.getAddress() : branch.getLocation()));
        locationText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        locationText.setTextSize(12);
        locationText.setPadding(0, 4, 0, 8);
        branchItem.addView(locationText);
        
        // Stats layout
        LinearLayout statsLayout = new LinearLayout(getContext());
        statsLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        // Manager count
        int managerCount = !"No manager assigned".equals(branch.getManager()) ? 1 : 0;
        TextView managersText = new TextView(getContext());
        managersText.setText("👥 " + managerCount + " Manager" + (managerCount != 1 ? "s" : ""));
        managersText.setTextColor(getResources().getColor(R.color.green));
        managersText.setTextSize(12);
        managersText.setPadding(0, 0, 32, 0);
        statsLayout.addView(managersText);
        
        // Employee count
        TextView employeesText = new TextView(getContext());
        employeesText.setText("👤 " + branch.getEmployeeCount() + " Employee" + (branch.getEmployeeCount() != 1 ? "s" : ""));
        employeesText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        employeesText.setTextSize(12);
        employeesText.setPadding(0, 0, 32, 0);
        statsLayout.addView(employeesText);
        
        // Active jobs (placeholder for now)
        TextView jobsText = new TextView(getContext());
        jobsText.setText("📋 0 Active Jobs");
        jobsText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        jobsText.setTextSize(12);
        statsLayout.addView(jobsText);
        
        branchItem.addView(statsLayout);
        branchPreviewLayout.addView(branchItem);
    }

    private void loadEmployeesForManagerPreview(String token) {
        // Load employees for manager preview
        ApiService apiService = ApiClient.getApiService();
        Call<EmployeeResponse> call = apiService.getEmployees("Bearer " + token);
        
        call.enqueue(new Callback<EmployeeResponse>() {
            @Override
            public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EmployeeResponse employeeResponse = response.body();
                    
                    if (employeeResponse.isSuccess()) {
                        updateManagersPreview(employeeResponse.getEmployees());
                    }
                }
            }
            
            @Override
            public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                android.util.Log.e("AdminHome", "Employees API call failed: " + t.getMessage(), t);
            }
        });
    }

    private void showErrorInBranchPreview(String errorMessage) {
        if (branchPreviewLayout != null && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                branchPreviewLayout.removeAllViews();
                
                TextView errorText = new TextView(getContext());
                errorText.setText("📍 " + errorMessage);
                errorText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                errorText.setTextSize(14);
                errorText.setPadding(32, 16, 32, 16);
                errorText.setBackgroundResource(R.drawable.bg_input_field);
                branchPreviewLayout.addView(errorText);
            });
        }
    }

    private void updateManagersPreview(List<EmployeeResponse.Employee> employees) {
        if (managersRecyclerView != null) {
            List<ManagersActivity.Manager> managers = new ArrayList<>();
            
            // Get first 3 managers for preview
            int count = 0;
            for (EmployeeResponse.Employee employee : employees) {
                if ("manager".equalsIgnoreCase(employee.getRole()) && count < 3) {
                    String fullName = employee.getFirstName() + " " + employee.getLastName();
                    String branch = employee.getBranch() != null ? employee.getBranch() : "No branch assigned";
                    String email = employee.getEmail() != null ? employee.getEmail() : "No email";
                    String status = "Active";
                    String phone = "+63 9XX XXX XXXX";
                    String joinDate = "N/A";
                    
                    managers.add(new ManagersActivity.Manager(fullName, branch, email, status, phone, joinDate));
                    count++;
                }
            }
            
            ManagersAdapter adapter = new ManagersAdapter(managers, null);
            managersRecyclerView.setAdapter(adapter);
        }
    }
}