package app.hub.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import app.hub.R;

public class AdminHomeFragment extends Fragment {
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);
        
        setupButtons(view);
        
        return view;
    }
    
    private void setupButtons(View view) {
        MaterialButton btnAddManager = view.findViewById(R.id.btnAddManager);
        if (btnAddManager != null) {
            btnAddManager.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AdminAddManager.class);
                startActivity(intent);
            });
        }
    }
}