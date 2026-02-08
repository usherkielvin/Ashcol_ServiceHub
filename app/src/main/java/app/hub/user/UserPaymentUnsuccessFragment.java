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
            android.content.Intent intent = requireActivity().getIntent();
            String ticketId = intent.getStringExtra(UserPaymentActivity.EXTRA_TICKET_ID);
            int paymentId = intent.getIntExtra(UserPaymentActivity.EXTRA_PAYMENT_ID, 0);
            double amount = intent.getDoubleExtra(UserPaymentActivity.EXTRA_AMOUNT, 0.0);
            String serviceName = intent.getStringExtra(UserPaymentActivity.EXTRA_SERVICE_NAME);
            String technicianName = intent.getStringExtra(UserPaymentActivity.EXTRA_TECHNICIAN_NAME);

            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, UserPaymentFragment.newInstance(
                    ticketId,
                    paymentId,
                    amount,
                    serviceName,
                    technicianName))
                .addToBackStack(null)
                .commit();
        });
    }
}
