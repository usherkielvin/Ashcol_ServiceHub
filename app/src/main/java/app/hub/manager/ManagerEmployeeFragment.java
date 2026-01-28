package app.hub.manager;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.button.MaterialButton;

import app.hub.R;

public class ManagerEmployeeFragment extends Fragment {

    public ManagerEmployeeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manager_employee, container, false);
        
        setupButtons(view);
        
        return view;
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
}