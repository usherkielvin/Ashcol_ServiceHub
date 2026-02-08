package app.hub.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

/**
 * Activity tab - shows recent ticket activity from database.
 */
public class UserNotificationFragment extends Fragment {

    private RecyclerView rvActivity;
    private LinearLayout emptyStateContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TicketsAdapter adapter;
    private TokenManager tokenManager;
    private List<TicketListResponse.TicketItem> tickets = new ArrayList<>();

    public UserNotificationFragment() {
    }

    public static UserNotificationFragment newInstance() {
        return new UserNotificationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user__activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvActivity = view.findViewById(R.id.rvActivity);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        tokenManager = new TokenManager(getContext());

        adapter = new TicketsAdapter(tickets);
        rvActivity.setLayoutManager(new LinearLayoutManager(getContext()));
        rvActivity.setAdapter(adapter);

        adapter.setOnTicketClickListener(ticket -> {
            Intent intent = new Intent(getContext(), TicketDetailActivity.class);
            intent.putExtra("ticket_id", ticket.getTicketId());
            startActivity(intent);
        });

        adapter.setOnPaymentClickListener(ticket -> {
            if (getActivity() == null) {
                return;
            }

            startActivity(UserPaymentActivity.createIntent(
                getActivity(),
                ticket.getTicketId(),
                0,
                0.0,
                ticket.getServiceType(),
                ticket.getAssignedStaff()));
        });

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadTickets);
        }

        // Show newly created ticket instantly if pending (e.g. when switching to Activity tab right after creation)
        TicketListResponse.TicketItem pending = UserTicketsFragment.getPendingNewTicket();
        if (pending != null) {
            UserTicketsFragment.clearPendingNewTicket();
            tickets.add(0, pending);
            showTicketList();
            adapter.notifyItemInserted(0);
        }

        loadTickets();
    }

    private void loadTickets() {
        String token = tokenManager.getToken();
        if (token == null) {
            showEmptyState();
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            return;
        }

        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;
        ApiService apiService = ApiClient.getApiService();
        Call<TicketListResponse> call = apiService.getTickets(authToken);

        call.enqueue(new Callback<TicketListResponse>() {
            @Override
            public void onResponse(Call<TicketListResponse> call, Response<TicketListResponse> response) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<TicketListResponse.TicketItem> newTickets = response.body().getTickets();
                    tickets.clear();
                    if (newTickets != null && !newTickets.isEmpty()) {
                        tickets.addAll(newTickets);
                        showTicketList();
                    } else {
                        showEmptyState();
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<TicketListResponse> call, Throwable t) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                showEmptyState();
                // Log only, no popup/toast
                android.util.Log.e("UserNotification", "Failed to load activity: " + t.getMessage(), t);
            }
        });
    }

    private void showTicketList() {
        if (rvActivity != null) rvActivity.setVisibility(View.VISIBLE);
        if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        if (rvActivity != null) rvActivity.setVisibility(View.GONE);
        if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
    }

}
