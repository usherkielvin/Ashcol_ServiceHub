package app.hub.manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.PaymentHistoryResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerRecordsFragment extends Fragment {

    private RecyclerView recyclerPayments;
    private ProgressBar paymentsLoading;
    private TextView tvPaymentsEmpty;
    private TextView locationTitle;

    private ManagerPaymentsAdapter adapter;
    private final List<PaymentHistoryResponse.PaymentItem> payments = new ArrayList<>();
    private TokenManager tokenManager;

    public ManagerRecordsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());

        recyclerPayments = view.findViewById(R.id.recyclerPayments);
        paymentsLoading = view.findViewById(R.id.paymentsLoading);
        tvPaymentsEmpty = view.findViewById(R.id.tvPaymentsEmpty);
        locationTitle = view.findViewById(R.id.locationTitle);

        adapter = new ManagerPaymentsAdapter(payments);
        recyclerPayments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPayments.setAdapter(adapter);

        String branch = tokenManager.getUserBranch();
        if (locationTitle != null && branch != null && !branch.isEmpty()) {
            locationTitle.setText(branch);
        }

        view.findViewById(R.id.cardReports).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getContext(), ManagerReportsActivity.class);
            startActivity(intent);
        });

        loadPaymentHistory();
    }

    private void loadPaymentHistory() {
        String token = tokenManager.getToken();
        if (token == null) {
            showEmptyState();
            return;
        }

        if (paymentsLoading != null) {
            paymentsLoading.setVisibility(View.VISIBLE);
        }
        if (tvPaymentsEmpty != null) {
            tvPaymentsEmpty.setVisibility(View.GONE);
        }

        ApiService apiService = ApiClient.getApiService();
        Call<PaymentHistoryResponse> call = apiService.getPaymentHistory("Bearer " + token);
        call.enqueue(new Callback<PaymentHistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaymentHistoryResponse> call,
                    @NonNull Response<PaymentHistoryResponse> response) {
                if (paymentsLoading != null) {
                    paymentsLoading.setVisibility(View.GONE);
                }

                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    showEmptyState();
                    return;
                }

                List<PaymentHistoryResponse.PaymentItem> data = response.body().getPayments();
                payments.clear();
                if (data != null) {
                    payments.addAll(data);
                }
                adapter.notifyDataSetChanged();

                if (payments.isEmpty()) {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaymentHistoryResponse> call, @NonNull Throwable t) {
                if (paymentsLoading != null) {
                    paymentsLoading.setVisibility(View.GONE);
                }
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        if (tvPaymentsEmpty != null) {
            tvPaymentsEmpty.setVisibility(View.VISIBLE);
        }
    }
}