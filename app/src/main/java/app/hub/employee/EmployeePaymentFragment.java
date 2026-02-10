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

    private boolean isPaymentLoading = false;
    private String confirmButtonText = "Cash Received";
import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.PaymentDetailResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

        if (btnPaymentConfirmed != null && btnPaymentConfirmed.getText() != null) {
            confirmButtonText = btnPaymentConfirmed.getText().toString();
        }

/**
 * Full-width fragment version of the payment confirmation UI.
 */
public class EmployeePaymentFragment extends Fragment {

    public interface OnPaymentConfirmedListener {
        void onPaymentConfirmed(String paymentMethod, double amount, String notes);
            if (isPaymentLoading || totalAmount <= 0) {
                if (getContext() != null) {
                    android.widget.Toast.makeText(getContext(),
                            "Amount not ready yet. Please wait.",
                            android.widget.Toast.LENGTH_SHORT).show();
                }
                return;
            }
    }

    private static final String ARG_TICKET_ID = "ticket_id";
    private static final String ARG_CUSTOMER_NAME = "customer_name";
    private static final String ARG_SERVICE_NAME = "service_name";
    private static final String ARG_TOTAL_AMOUNT = "total_amount";

    private String ticketId;
    private String customerName;
    private String serviceName;
    private double totalAmount;

    private TextView tvTicketId;
    private TextView tvCustomerName;
    private TextView tvServiceName;
    private TextView tvTotalAmount;
    private MaterialButton btnPaymentConfirmed;
    private TokenManager tokenManager;

    public static EmployeePaymentFragment newInstance(String ticketId, String customerName,
            String serviceName, double totalAmount) {
        EmployeePaymentFragment fragment = new EmployeePaymentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TICKET_ID, ticketId);
        args.putString(ARG_CUSTOMER_NAME, customerName);
        args.putString(ARG_SERVICE_NAME, serviceName);
        args.putDouble(ARG_TOTAL_AMOUNT, totalAmount);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
                if (!isAdded() || response.body() == null || !response.body().isSuccess()) {
                    setPaymentLoading(false);
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            ticketId = getArguments().getString(ARG_TICKET_ID);
                    setPaymentLoading(false);
            customerName = getArguments().getString(ARG_CUSTOMER_NAME);
            serviceName = getArguments().getString(ARG_SERVICE_NAME);
            totalAmount = getArguments().getDouble(ARG_TOTAL_AMOUNT, 0.0);
        }
        return inflater.inflate(R.layout.fragment_employee_work_confirmpayment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupClickListeners();
    }

    private void initViews(View view) {
        tvTicketId = view.findViewById(R.id.tvTicketId);
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvServiceName = view.findViewById(R.id.tvServiceName);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        btnPaymentConfirmed = view.findViewById(R.id.btnPaymentConfirmed);
        tokenManager = new TokenManager(requireContext());

        if (tvTicketId != null) {
            tvTicketId.setText(ticketId != null ? ticketId : "");
        }
        if (tvCustomerName != null) {
            tvCustomerName.setText(customerName != null ? customerName : "");
        }
                setPaymentLoading(false);
        if (tvServiceName != null) {
            tvServiceName.setText(serviceName != null ? serviceName : "");
        }
        if (tvTotalAmount != null) {

    private void setPaymentLoading(boolean loading) {
        isPaymentLoading = loading;
        if (btnPaymentConfirmed == null) {
            return;
        }
        btnPaymentConfirmed.setEnabled(!loading);
        btnPaymentConfirmed.setText(loading ? "Loading..." : confirmButtonText);
    }
            String amountText = "Php " + String.format("%.2f", totalAmount);
            tvTotalAmount.setText(amountText);
        }
    }

    private void setupClickListeners() {
        loadPaymentDetailsIfNeeded();
        if (btnPaymentConfirmed == null) {
            return;
        }
        btnPaymentConfirmed.setOnClickListener(v -> {
            if (getActivity() instanceof OnPaymentConfirmedListener) {
                ((OnPaymentConfirmedListener) getActivity())
                        .onPaymentConfirmed("cash", totalAmount, "");
            } else if (getParentFragment() instanceof OnPaymentConfirmedListener) {
                ((OnPaymentConfirmedListener) getParentFragment())
                        .onPaymentConfirmed("cash", totalAmount, "");
            }

            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }

    private void loadPaymentDetailsIfNeeded() {
        if (ticketId == null || tokenManager == null) {
            return;
        }
        if (totalAmount > 0) {
            return;
        }

        setPaymentLoading(true);
        String token = tokenManager.getToken();
        if (token == null) {
            setPaymentLoading(false);
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<PaymentDetailResponse> call = apiService.getPaymentByTicketId("Bearer " + token, ticketId);
        call.enqueue(new Callback<PaymentDetailResponse>() {
            @Override
            public void onResponse(Call<PaymentDetailResponse> call, Response<PaymentDetailResponse> response) {
                if (!isAdded() || response.body() == null || !response.body().isSuccess()) {
                    return;
                }

                PaymentDetailResponse.PaymentDetail payment = response.body().getPayment();
                if (payment == null) {
                    return;
                }

                totalAmount = payment.getAmount();
                if (tvTotalAmount != null) {
                    String amountText = "Php " + String.format("%.2f", totalAmount);
                    tvTotalAmount.setText(amountText);
                }

                if (tvServiceName != null && (serviceName == null || serviceName.trim().isEmpty())) {
                    serviceName = payment.getServiceName();
                    if (serviceName != null) {
                        tvServiceName.setText(serviceName);
                    }
                }

                if (tvCustomerName != null && (customerName == null || customerName.trim().isEmpty())) {
                    customerName = payment.getCustomerName();
                    if (customerName != null) {
                        tvCustomerName.setText(customerName);
                    }
                }
            }

            @Override
            public void onFailure(Call<PaymentDetailResponse> call, Throwable t) {
                // Ignore to keep UI stable; amount will stay as-is.
            }
        });
    }
}

