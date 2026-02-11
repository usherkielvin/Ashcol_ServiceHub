package app.hub.employee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import app.hub.R;

/**
 * Fragment shown to technician after requesting payment from customer.
 * Displays ticket details and confirmation that payment request was sent.
 */
public class EmployeeWorkConfirmPaymentFragment extends Fragment {

    private static final String ARG_TICKET_ID = "ticket_id";
    private static final String ARG_CUSTOMER_NAME = "customer_name";
    private static final String ARG_SERVICE_NAME = "service_name";
    private static final String ARG_AMOUNT = "amount";

    private String ticketId;
    private String customerName;
    private String serviceName;
    private double amount;

    public EmployeeWorkConfirmPaymentFragment() {
        // Required empty public constructor
    }

    public static EmployeeWorkConfirmPaymentFragment newInstance(String ticketId, String customerName,
                                                                   String serviceName, double amount) {
        EmployeeWorkConfirmPaymentFragment fragment = new EmployeeWorkConfirmPaymentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TICKET_ID, ticketId);
        args.putString(ARG_CUSTOMER_NAME, customerName);
        args.putString(ARG_SERVICE_NAME, serviceName);
        args.putDouble(ARG_AMOUNT, amount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ticketId = getArguments().getString(ARG_TICKET_ID);
            customerName = getArguments().getString(ARG_CUSTOMER_NAME);
            serviceName = getArguments().getString(ARG_SERVICE_NAME);
            amount = getArguments().getDouble(ARG_AMOUNT, 0.0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_work_confirmpayment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTicketId = view.findViewById(R.id.tvTicketId);
        TextView tvCustomerName = view.findViewById(R.id.tvCustomerName);
        TextView tvServiceName = view.findViewById(R.id.tvServiceName);
        TextView tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        MaterialButton btnPaymentConfirmed = view.findViewById(R.id.btnPaymentConfirmed);

        // Set ticket details
        if (ticketId != null) {
            tvTicketId.setText(ticketId);
        }
        if (customerName != null) {
            tvCustomerName.setText(customerName);
        }
        if (serviceName != null) {
            tvServiceName.setText(serviceName);
        }
        if (amount > 0) {
            tvTotalAmount.setText(formatAmount(amount));
        }

        // Back button to return to work fragment
        if (btnPaymentConfirmed != null) {
            btnPaymentConfirmed.setText("Back to Work");
            btnPaymentConfirmed.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
    }

    private String formatAmount(double value) {
        return "Php " + String.format(java.util.Locale.getDefault(), "%,.2f", value);
    }
}
