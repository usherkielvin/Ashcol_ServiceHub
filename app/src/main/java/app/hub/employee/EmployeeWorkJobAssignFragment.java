package app.hub.employee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.hub.R;

public class EmployeeWorkJobAssignFragment extends Fragment {

    public EmployeeWorkJobAssignFragment() {
        // Required empty public constructor
    }

    public static EmployeeWorkJobAssignFragment newInstance() {
        return new EmployeeWorkJobAssignFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_employee_work_jobassign, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize job assignment logic
    }
}
