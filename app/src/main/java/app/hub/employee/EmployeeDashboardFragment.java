package app.hub.employee;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.hub.R;

public class EmployeeDashboardFragment extends Fragment {

    public EmployeeDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_employee_home, container, false);
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view,
            @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        app.hub.util.TokenManager tokenManager = new app.hub.util.TokenManager(requireContext());

        android.widget.TextView tvHeaderName = view.findViewById(R.id.tvHeaderName);
        android.widget.TextView tvHeaderBranch = view.findViewById(R.id.tvHeaderBranch);

        if (tvHeaderName != null) {
            String name = tokenManager.getName();
            if (name != null) {
                // Extract first name for a friendlier greeting
                String[] parts = name.split(" ");
                if (parts.length > 0) {
                    name = parts[0];
                }
                tvHeaderName.setText("Hello, " + name);
            } else {
                tvHeaderName.setText("Hello, Employee");
            }
        }

        if (tvHeaderBranch != null) {
            String branch = tokenManager.getUserBranch();
            if (branch != null && !branch.isEmpty()) {
                tvHeaderBranch.setText("Branch: " + branch);
                tvHeaderBranch.setVisibility(View.VISIBLE);
            } else {
                // Try to see if there is a general branch saved
                String cachedBranch = tokenManager.getCachedBranch();
                if (cachedBranch != null) {
                    tvHeaderBranch.setText("Branch: " + cachedBranch);
                    tvHeaderBranch.setVisibility(View.VISIBLE);
                } else {
                    tvHeaderBranch.setVisibility(View.GONE);
                }
            }
        }
    }
}
