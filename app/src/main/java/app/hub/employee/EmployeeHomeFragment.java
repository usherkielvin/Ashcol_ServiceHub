package app.hub.employee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import app.hub.R;

public class EmployeeHomeFragment extends Fragment {

    public EmployeeHomeFragment() {
        // Required empty public constructor
    }

    public static EmployeeHomeFragment newInstance() {
        return new EmployeeHomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_employee_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Example setup for RecyclerViews if ids match
        // RecyclerView rvDayWork = view.findViewById(R.id.rvDayWork);
        // rvDayWork.setLayoutManager(new LinearLayoutManager(getContext()));
        // rvDayWork.setAdapter(new EmployeeHomeDayWorkAdapter(new ArrayList<>()));

        // RecyclerView rvSchedule = view.findViewById(R.id.rvSchedule);
        // rvSchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        // rvSchedule.setAdapter(new EmployeeHomeScheduleAdapter(new ArrayList<>()));
    }
}
