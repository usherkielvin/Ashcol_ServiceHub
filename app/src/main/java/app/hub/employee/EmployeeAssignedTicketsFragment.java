package app.hub.employee;

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
import app.hub.common.Ticket;
import app.hub.common.TicketAdapter;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeAssignedTicketsFragment extends Fragment {

    private RecyclerView ticketsRecyclerView;
    private TextView noTicketsTextView;
    private TicketAdapter ticketAdapter;
    private List<Ticket> ticketList = new ArrayList<>();
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee_assigned_tickets, container, false);

        ticketsRecyclerView = view.findViewById(R.id.ticketsRecyclerView);
        noTicketsTextView = view.findViewById(R.id.noTicketsTextView);
        tokenManager = new TokenManager(getContext());

        setupRecyclerView();
        loadTickets();

        return view;
    }

    private void setupRecyclerView() {
        ticketAdapter = new TicketAdapter(ticketList);
        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ticketsRecyclerView.setAdapter(ticketAdapter);
    }

    private void loadTickets() {
        // This is where you would make an API call to get the tickets.
        // For now, let's use the same sample data.
        ticketList.add(new Ticket("Fix the printer", "The printer in the main office is jammed.", "Open"));
        ticketList.add(new Ticket("Install new software", "Please install Photoshop on the new marketing computer.", "In Progress"));
        ticketList.add(new Ticket("Network is down", "The entire office has lost internet connectivity.", "Open"));

        if (ticketList.isEmpty()) {
            noTicketsTextView.setVisibility(View.VISIBLE);
            ticketsRecyclerView.setVisibility(View.GONE);
        } else {
            noTicketsTextView.setVisibility(View.GONE);
            ticketsRecyclerView.setVisibility(View.VISIBLE);
            ticketAdapter.notifyDataSetChanged();
        }
    }
}
