package app.hub.manager;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.UserResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerEmployeeFragment extends Fragment {

    private TextView locationTitle;
    private TokenManager tokenManager;

    public ManagerEmployeeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manager_employee, container, false);
        
        tokenManager = new TokenManager(getContext());
        initializeViews(view);
        setupButtons(view);
        loadManagerBranch();
        
        return view;
    }
    
    private void initializeViews(View view) {
        locationTitle = view.findViewById(R.id.nameBranch);
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
}