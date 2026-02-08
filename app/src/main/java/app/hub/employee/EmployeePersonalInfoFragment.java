package app.hub.employee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.UpdateProfileRequest;
import app.hub.api.UserResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeePersonalInfoFragment extends Fragment {

    private TokenManager tokenManager;
    private TextView tvEmail;
    private TextView tvRole;
    private TextView tvBranch;
    private TextInputEditText inputFirstName;
    private TextInputEditText inputLastName;
    private TextInputEditText inputPhone;
    private TextInputEditText inputLocation;
    private TextInputLayout layoutFirstName;
    private TextInputLayout layoutLastName;
    private MaterialButton btnSave;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_personal_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = new TokenManager(requireContext());

        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvRole = view.findViewById(R.id.tvProfileRole);
        tvBranch = view.findViewById(R.id.tvProfileBranch);
        inputFirstName = view.findViewById(R.id.inputFirstName);
        inputLastName = view.findViewById(R.id.inputLastName);
        inputPhone = view.findViewById(R.id.inputPhone);
        inputLocation = view.findViewById(R.id.inputLocation);
        layoutFirstName = view.findViewById(R.id.layoutFirstName);
        layoutLastName = view.findViewById(R.id.layoutLastName);
        btnSave = view.findViewById(R.id.btnSaveProfile);

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> navigateBack());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfile());
        }

        loadProfile();
    }

    private void loadProfile() {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Authentication error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> call = apiService.getUser(token);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse.Data data = response.body().getData();
                    if (data != null) {
                        bindProfile(data);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Failed to load profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindProfile(UserResponse.Data data) {
        if (tvEmail != null) {
            tvEmail.setText(data.getEmail() != null ? data.getEmail() : "--");
        }
        if (tvRole != null) {
            tvRole.setText(data.getRole() != null ? data.getRole() : "--");
        }
        if (tvBranch != null) {
            String branch = data.getBranch() != null ? data.getBranch() : tokenManager.getCachedBranch();
            tvBranch.setText(branch != null ? branch : "--");
        }

        if (inputFirstName != null) {
            inputFirstName.setText(data.getFirstName() != null ? data.getFirstName() : "");
        }
        if (inputLastName != null) {
            inputLastName.setText(data.getLastName() != null ? data.getLastName() : "");
        }
        if (inputLocation != null) {
            inputLocation.setText(data.getLocation() != null ? data.getLocation() : "");
        }
    }

    private void saveProfile() {
        if (layoutFirstName != null) layoutFirstName.setError(null);
        if (layoutLastName != null) layoutLastName.setError(null);

        String firstName = inputFirstName != null ? inputFirstName.getText().toString().trim() : "";
        String lastName = inputLastName != null ? inputLastName.getText().toString().trim() : "";
        String phone = inputPhone != null ? inputPhone.getText().toString().trim() : "";
        String location = inputLocation != null ? inputLocation.getText().toString().trim() : "";

        if (firstName.isEmpty()) {
            if (layoutFirstName != null) layoutFirstName.setError("First name is required");
            return;
        }

        if (lastName.isEmpty()) {
            if (layoutLastName != null) layoutLastName.setError("Last name is required");
            return;
        }

        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Authentication error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnSave != null) {
            btnSave.setEnabled(false);
            btnSave.setText("Saving...");
        }

        UpdateProfileRequest request = new UpdateProfileRequest(firstName, lastName, phone, location);
        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> call = apiService.updateUser(token, request);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (!isAdded()) return;
                restoreSaveButton();

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse.Data data = response.body().getData();
                    if (data != null) {
                        String fullName = (data.getFirstName() != null ? data.getFirstName() : "")
                                + " " + (data.getLastName() != null ? data.getLastName() : "");
                        tokenManager.saveName(fullName.trim());
                        Toast.makeText(requireContext(), "Profile updated.", Toast.LENGTH_SHORT).show();
                        bindProfile(data);
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                restoreSaveButton();
                Toast.makeText(requireContext(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restoreSaveButton() {
        if (btnSave != null) {
            btnSave.setEnabled(true);
            btnSave.setText("Save changes");
        }
    }

    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
}
