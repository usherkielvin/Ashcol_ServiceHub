package app.hub.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.hub.R;

public class SrfCancelRequestFragment extends Fragment {

    public SrfCancelRequestFragment() {
        // Required empty public constructor
    }

    public static SrfCancelRequestFragment newInstance() {
        return new SrfCancelRequestFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_srf_cancel_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        com.google.android.material.button.MaterialButton btnCancelRequest = view.findViewById(R.id.btnCancelRequest);
        com.google.android.material.button.MaterialButton btnBack = view.findViewById(R.id.btnBack);

        btnCancelRequest.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, SrfCancelReasonFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        });

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Unable to go back.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
