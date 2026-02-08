package app.hub.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.hub.R;

public class UserPaymentSuccessFragment extends Fragment {

    public UserPaymentSuccessFragment() {
        // Required empty public constructor
    }

    public static UserPaymentSuccessFragment newInstance() {
        return new UserPaymentSuccessFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_pay_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvPaymentMethod = view.findViewById(R.id.tvPaymentMethod);
        TextView tvTicketId = view.findViewById(R.id.tvTicketId);
        TextView tvServiceName = view.findViewById(R.id.tvServiceName);
        TextView tvTechnicianName = view.findViewById(R.id.tvTechnicianName);
        TextView tvAmountPaid = view.findViewById(R.id.tvAmountPaid);
        com.google.android.material.button.MaterialButton btnBackToDashboard = view.findViewById(R.id.btnBackToDashboard);

        Bundle args = getArguments();
        if (args != null) {
            String method = args.getString("payment_method");
            String ticketId = args.getString("ticket_id");
            String serviceName = args.getString("service_name");
            String technicianName = args.getString("technician_name");
            double amount = args.getDouble("amount", 0.0);

            if (tvPaymentMethod != null && method != null && !method.isEmpty()) {
                tvPaymentMethod.setText(method);
            }
            if (tvTicketId != null && ticketId != null && !ticketId.isEmpty()) {
                tvTicketId.setText(ticketId);
            }
            if (tvServiceName != null && serviceName != null && !serviceName.isEmpty()) {
                tvServiceName.setText(serviceName);
            }
            if (tvTechnicianName != null && technicianName != null && !technicianName.isEmpty()) {
                tvTechnicianName.setText(technicianName);
            }
            if (tvAmountPaid != null && amount > 0) {
                tvAmountPaid.setText("Php " + String.format(java.util.Locale.getDefault(), "%,.2f", amount));
            }
        }

        btnBackToDashboard.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, new UserHomeFragment())
                    .commit();

            com.google.android.material.bottomnavigation.BottomNavigationView navView =
                    requireActivity().findViewById(R.id.bottomNavigationView);
            if (navView != null) {
                navView.setSelectedItemId(R.id.homebtn);
            }
        });
    }
}
