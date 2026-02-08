package app.hub.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.hub.R;

public class UserPaymentUnsuccessFragment extends Fragment {

    public UserPaymentUnsuccessFragment() {
        // Required empty public constructor
    }

    public static UserPaymentUnsuccessFragment newInstance() {
        return new UserPaymentUnsuccessFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment (Note: check layout spelling if needed,
        // using provided name)
        return inflater.inflate(R.layout.fragment_user_pay_unsucces, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        com.google.android.material.button.MaterialButton btnTryAgain = view.findViewById(R.id.btnTryAgain);
        btnTryAgain.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, UserPaymentFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        });
    }
}
