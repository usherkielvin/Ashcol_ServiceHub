package app.hub.admin;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.hub.R;

public class ReportsFragment extends Fragment {

    public ReportsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_reports, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
