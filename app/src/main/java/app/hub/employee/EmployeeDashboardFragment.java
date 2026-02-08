package app.hub.employee;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.TicketListResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeDashboardFragment extends Fragment {

    private LinearLayout todayWorkContainer;
    private FirebaseEmployeeListener firebaseEmployeeListener;

    public EmployeeDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_employee_home, container, false);
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view,
            @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        app.hub.util.TokenManager tokenManager = new app.hub.util.TokenManager(requireContext());

        android.widget.TextView tvHeaderName = view.findViewById(R.id.tvHeaderName);
        android.widget.TextView tvHeaderBranch = view.findViewById(R.id.tvHeaderBranch);
        android.widget.TextView tvTodayDate = view.findViewById(R.id.tvTodayDate);

        if (tvHeaderName != null) {
            String name = tokenManager.getName();
            if (name != null) {
                // Extract first name for a friendlier greeting
                String[] parts = name.split(" ");
                if (parts.length > 0) {
                    name = parts[0];
                }
                tvHeaderName.setText("Hello, " + name);
            } else {
                tvHeaderName.setText("Hello, Employee");
            }
        }

        if (tvHeaderBranch != null) {
            String branch = tokenManager.getUserBranch();
            if (branch != null && !branch.isEmpty()) {
                tvHeaderBranch.setText(branch);
                tvHeaderBranch.setVisibility(View.VISIBLE);
            } else {
                // Try to see if there is a general branch saved
                String cachedBranch = tokenManager.getCachedBranch();
                if (cachedBranch != null) {
                    tvHeaderBranch.setText(cachedBranch);
                    tvHeaderBranch.setVisibility(View.VISIBLE);
                } else {
                    tvHeaderBranch.setVisibility(View.GONE);
                }
            }
        }

        // Set current date
        if (tvTodayDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.ENGLISH);
            String currentDate = dateFormat.format(new Date()).toUpperCase();
            tvTodayDate.setText(currentDate);
        }

        // Setup View All Schedules button click
        com.google.android.material.button.MaterialButton btnViewAllSchedules = view.findViewById(R.id.btnViewAllSchedules);
        if (btnViewAllSchedules != null) {
            btnViewAllSchedules.setOnClickListener(v -> {
                // Navigate to EmployeeScheduleFragment
                if (getActivity() != null) {
                    // Update navigation indicator
                    if (getActivity() instanceof EmployeeDashboardActivity) {
                        ((EmployeeDashboardActivity) getActivity()).updateNavigationIndicator(R.id.nav_sched);
                    }
                    
                    androidx.fragment.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    androidx.fragment.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.fragment_container, new EmployeeScheduleFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
        }

        todayWorkContainer = view.findViewById(R.id.todayWorkContainer);

        // Load all assigned tickets for schedules section
        loadAssignedSchedules();

        // Load today's in-progress work immediately
        loadTodayWork();

        // Start real-time listener for updates
        setupRealtimeListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firebaseEmployeeListener != null && !firebaseEmployeeListener.isListening()) {
            firebaseEmployeeListener.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firebaseEmployeeListener != null) {
            firebaseEmployeeListener.stopListening();
        }
    }

    private void setupRealtimeListener() {
        if (getContext() == null) return;

        firebaseEmployeeListener = new FirebaseEmployeeListener(getContext());
        firebaseEmployeeListener.setOnScheduleChangeListener(new FirebaseEmployeeListener.OnScheduleChangeListener() {
            @Override
            public void onScheduleChanged() {
                if (!isAdded()) return;
                loadTodayWork();
                loadAssignedSchedules();
            }

            @Override
            public void onError(String error) {
                // No UI noise needed; keep home quiet
            }
        });
        firebaseEmployeeListener.startListening();
    }

    private void loadAssignedSchedules() {
        if (!isAdded() || getContext() == null) return;

        TokenManager tokenManager = new TokenManager(requireContext());
        String token = tokenManager.getToken();
        if (token == null) return;

        ApiService apiService = ApiClient.getApiService();
        Call<TicketListResponse> call = apiService.getEmployeeTickets("Bearer " + token);

        call.enqueue(new Callback<TicketListResponse>() {
            @Override
            public void onResponse(Call<TicketListResponse> call, Response<TicketListResponse> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<TicketListResponse.TicketItem> tickets = response.body().getTickets();
                    displayAssignedSchedules(tickets != null ? tickets : new ArrayList<>());
                } else {
                    displayAssignedSchedules(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<TicketListResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                displayAssignedSchedules(new ArrayList<>());
            }
        });
    }

    private void displayAssignedSchedules(List<TicketListResponse.TicketItem> tickets) {
        if (getView() == null) return;

        LinearLayout scheduleContainer = getView().findViewById(R.id.scheduleItemsContainer);
        TextView tvNoEventsToday = getView().findViewById(R.id.tvNoEventsToday);

        if (scheduleContainer == null || tvNoEventsToday == null) return;

        scheduleContainer.removeAllViews();

        List<TicketListResponse.TicketItem> activeTickets = new ArrayList<>();
        for (TicketListResponse.TicketItem ticket : tickets) {
            String status = ticket.getStatus();
            if (status == null) continue;

            String normalized = status.trim().toLowerCase(Locale.ENGLISH);
            if ("pending".equals(normalized)
                    || "in_progress".equals(normalized)
                    || "in progress".equals(normalized)
                    || "scheduled".equals(normalized)
                    || "assigned".equals(normalized)) {
                activeTickets.add(ticket);
            }
        }

        if (activeTickets.isEmpty()) {
            tvNoEventsToday.setText("No assigned tickets yet.");
            tvNoEventsToday.setVisibility(View.VISIBLE);
            return;
        }

        tvNoEventsToday.setVisibility(View.GONE);
        for (TicketListResponse.TicketItem ticket : activeTickets) {
            View scheduleItem = createScheduleItemView(ticket);
            if (scheduleItem != null) {
                scheduleContainer.addView(scheduleItem);
            }
        }
    }

    private void loadTodayWork() {
        if (!isAdded() || getContext() == null) return;

        TokenManager tokenManager = new TokenManager(requireContext());
        String token = tokenManager.getToken();
        if (token == null) return;

        ApiService apiService = ApiClient.getApiService();
        Call<TicketListResponse> call = apiService.getEmployeeTickets("Bearer " + token);

        call.enqueue(new Callback<TicketListResponse>() {
            @Override
            public void onResponse(Call<TicketListResponse> call, Response<TicketListResponse> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<TicketListResponse.TicketItem> tickets = response.body().getTickets();
                    displayTodayWork(tickets != null ? tickets : new ArrayList<>());
                } else {
                    displayTodayWork(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<TicketListResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                displayTodayWork(new ArrayList<>());
            }
        });
    }

    private void displayTodayWork(List<TicketListResponse.TicketItem> tickets) {
        if (todayWorkContainer == null || getContext() == null) return;

        todayWorkContainer.removeAllViews();

        List<TicketListResponse.TicketItem> inProgress = new ArrayList<>();

        for (TicketListResponse.TicketItem ticket : tickets) {
            String status = ticket.getStatus();
            boolean statusMatch = status != null && ("in_progress".equalsIgnoreCase(status)
                    || "in progress".equalsIgnoreCase(status));

            if (statusMatch) {
                inProgress.add(ticket);
            }
        }

        if (inProgress.isEmpty()) {
            TextView emptyView = new TextView(getContext());
            emptyView.setText("No in-progress work yet.");
            emptyView.setTextColor(getResources().getColor(R.color.dark_gray));
            emptyView.setTextSize(14f);
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            emptyView.setPadding(padding, padding, padding, padding);
            todayWorkContainer.addView(emptyView);
            return;
        }

        for (TicketListResponse.TicketItem ticket : inProgress) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_employee_home_daywork,
                    todayWorkContainer, false);

            TextView tvWorkTitle = itemView.findViewById(R.id.tvWorkTitle);
            TextView tvWorkDetail = itemView.findViewById(R.id.tvWorkDetail);
            TextView tvRequestedById = itemView.findViewById(R.id.tvRequestedById);
            TextView tvScheduleValue = itemView.findViewById(R.id.tvScheduleValue);
            TextView tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            TextView tvContactNumber = itemView.findViewById(R.id.tvContactNumber);
            com.google.android.material.button.MaterialButton btnWorkStatus = itemView.findViewById(R.id.btnWorkStatus);

            String title = ticket.getServiceType() != null ? ticket.getServiceType() :
                    (ticket.getTitle() != null ? ticket.getTitle() : "Service Request");
            if (tvWorkTitle != null) tvWorkTitle.setText(title);

            String detail = ticket.getDescription() != null && !ticket.getDescription().isEmpty()
                    ? ticket.getDescription()
                    : "In progress";
            if (tvWorkDetail != null) tvWorkDetail.setText("• " + detail);

            if (tvRequestedById != null) {
                String ticketId = ticket.getTicketId();
                tvRequestedById.setText(ticketId != null ? ticketId : "--");
            }

            if (tvScheduleValue != null) {
                String scheduleDisplay = formatScheduleDisplay(ticket.getScheduledDate(), ticket.getScheduledTime());
                tvScheduleValue.setText(scheduleDisplay);
            }

            if (tvCustomerName != null) {
                tvCustomerName.setText(ticket.getCustomerName() != null ? ticket.getCustomerName() : "--");
            }

            if (tvContactNumber != null) {
                tvContactNumber.setText(ticket.getContact() != null ? ticket.getContact() : "--");
            }

            if (btnWorkStatus != null) {
                String status = ticket.getStatus() != null ? ticket.getStatus() : "In progress";
                btnWorkStatus.setText("Status: " + status.replace('_', ' '));

                String statusColor = ticket.getStatusColor();
                if (statusColor != null && statusColor.startsWith("#")) {
                    try {
                        int color = android.graphics.Color.parseColor(statusColor);
                        btnWorkStatus.setTextColor(color);
                        btnWorkStatus.setStrokeColor(android.content.res.ColorStateList.valueOf(color));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            itemView.setOnClickListener(v -> {
                if (ticket.getTicketId() != null && getContext() != null) {
                    android.content.Intent intent = new android.content.Intent(getContext(),
                            EmployeeTicketDetailActivity.class);
                    intent.putExtra("ticket_id", ticket.getTicketId());
                    startActivity(intent);
                }
            });

            todayWorkContainer.addView(itemView);
        }
    }

    private String formatScheduleDisplay(String date, String time) {
        if (date == null && time == null) return "Schedule: --";
        if (date == null) return time;
        if (time == null) return date;
        return date + " - " + time;
    }

    private View createScheduleItemView(TicketListResponse.TicketItem ticket) {
        if (getContext() == null) return null;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemView = inflater.inflate(R.layout.item_employee_home_schedule, null);

        // Hide the date header since schedules are list-based
        TextView tvScheduleDate = itemView.findViewById(R.id.tvScheduleDate);
        if (tvScheduleDate != null) {
            tvScheduleDate.setVisibility(View.GONE);
        }

        TextView tvScheduleTitle = itemView.findViewById(R.id.tvScheduleTitle);
        TextView tvScheduleDetail = itemView.findViewById(R.id.tvScheduleDetail);
        TextView tvRequestedById = itemView.findViewById(R.id.tvRequestedById);
        TextView tvScheduleTime = itemView.findViewById(R.id.tvScheduleTime);
        com.google.android.material.button.MaterialButton btnScheduleStatus = itemView.findViewById(R.id.btnScheduleStatus);

        if (tvScheduleTitle != null) {
            String title = ticket.getServiceType() != null ? ticket.getServiceType()
                    : (ticket.getTitle() != null ? ticket.getTitle() : "Service Request");
            tvScheduleTitle.setText(title);
        }

        if (tvScheduleDetail != null) {
            String detail = ticket.getDescription() != null && !ticket.getDescription().isEmpty()
                    ? ticket.getDescription()
                    : "Assigned ticket";
            tvScheduleDetail.setText("• " + detail);
        }

        if (tvRequestedById != null) {
            String ticketId = ticket.getTicketId();
            tvRequestedById.setText(ticketId != null ? ticketId : "--");
        }

        if (tvScheduleTime != null) {
            String scheduleDisplay = formatScheduleDisplay(ticket.getScheduledDate(), ticket.getScheduledTime());
            tvScheduleTime.setText(scheduleDisplay);
        }

        if (btnScheduleStatus != null) {
            String status = ticket.getStatus() != null ? ticket.getStatus() : "Pending";
            btnScheduleStatus.setText("Status: " + status.replace('_', ' '));

            String statusColor = ticket.getStatusColor();
            if (statusColor != null && statusColor.startsWith("#")) {
                try {
                    int color = android.graphics.Color.parseColor(statusColor);
                    btnScheduleStatus.setTextColor(color);
                    btnScheduleStatus.setStrokeColor(android.content.res.ColorStateList.valueOf(color));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        // Add click listener to open ticket detail
        View scheduleItemLayout = itemView.findViewById(R.id.scheduleItemLayout);
        if (scheduleItemLayout != null) {
            scheduleItemLayout.setOnClickListener(v -> {
                if (ticket.getTicketId() != null) {
                    android.content.Intent intent = new android.content.Intent(getContext(),
                            app.hub.employee.EmployeeTicketDetailActivity.class);
                    intent.putExtra("ticket_id", ticket.getTicketId());
                    startActivity(intent);
                }
            });
        }

        return itemView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh schedule data when fragment becomes visible
        loadAssignedSchedules();
        loadTodayWork();
    }
}
