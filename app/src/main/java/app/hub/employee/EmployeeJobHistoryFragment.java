package app.hub.employee;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import app.hub.api.TicketListResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeJobHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private EmployeeTicketsAdapter adapter;
    private final List<TicketListResponse.TicketItem> tickets = new ArrayList<>();
    private TokenManager tokenManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_job_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = new TokenManager(requireContext());

        recyclerView = view.findViewById(R.id.recyclerJobHistory);
        tvEmpty = view.findViewById(R.id.tvJobHistoryEmpty);

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> navigateBack());
        }

        adapter = new EmployeeTicketsAdapter(tickets);
        adapter.setOnTicketClickListener(ticket -> {
            Intent intent = new Intent(getContext(), EmployeeTicketDetailActivity.class);
            intent.putExtra("ticket_id", ticket.getTicketId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadCompletedTickets();
    }

    private void loadCompletedTickets() {
        String token = tokenManager.getToken();
        if (token == null) {
            showEmptyState(true);
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<TicketListResponse> call = apiService.getEmployeeTicketsByStatus("Bearer " + token, "completed");
        call.enqueue(new Callback<TicketListResponse>() {
            @Override
            public void onResponse(@NonNull Call<TicketListResponse> call,
                    @NonNull Response<TicketListResponse> response) {
                if (!isAdded()) return;
                tickets.clear();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (response.body().getTickets() != null) {
                        tickets.addAll(response.body().getTickets());
                    }
                }
                adapter.notifyDataSetChanged();
                showEmptyState(tickets.isEmpty());
            }

            @Override
            public void onFailure(@NonNull Call<TicketListResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showEmptyState(true);
            }
        });
    }

    private void showEmptyState(boolean show) {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
}
