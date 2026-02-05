package app.hub.manager;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.hub.R;

public class ManagerRecordsFragment extends Fragment {

    public ManagerRecordsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manager_records, container, false);
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view,
            @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.cardReports).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getContext(), ManagerReportsActivity.class);
            startActivity(intent);
        });
    }
}