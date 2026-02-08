package app.hub.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.hub.R;

public class SrfCancelReasonFragment extends Fragment {

    public SrfCancelReasonFragment() {
        // Required empty public constructor
    }

    public static SrfCancelReasonFragment newInstance() {
        return new SrfCancelReasonFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_srf_cancel_reason, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RadioGroup rgCancelReasons = view.findViewById(R.id.rgCancelReasons);
        com.google.android.material.button.MaterialButton btnSubmit = view.findViewById(R.id.btnSubmitCancellation);
        com.google.android.material.button.MaterialButton btnBack = view.findViewById(R.id.btnBack);

        btnSubmit.setOnClickListener(v -> {
            int checkedId = rgCancelReasons.getCheckedRadioButtonId();
            if (checkedId == -1) {
                Toast.makeText(getContext(), "Please select a reason.", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getContext(), "Cancellation submitted.", Toast.LENGTH_SHORT).show();
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }
}
