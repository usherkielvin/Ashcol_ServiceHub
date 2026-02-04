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
import java.util.List;
import java.util.Locale;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.EmployeeScheduleResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeDashboardFragment extends Fragment {

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
                tvHeaderBranch.setText("Branch: " + branch);
                tvHeaderBranch.setVisibility(View.VISIBLE);
            } else {
                // Try to see if there is a general branch saved
                String cachedBranch = tokenManager.getCachedBranch();
                if (cachedBranch != null) {
                    tvHeaderBranch.setText("Branch: " + cachedBranch);
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
                    androidx.fragment.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    androidx.fragment.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.fragment_container, new EmployeeScheduleFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
        }

        // Load today's schedule data
        loadTodaySchedule();
    }

    private void loadTodaySchedule() {
        app.hub.util.TokenManager tokenManager = new app.hub.util.TokenManager(requireContext());
        String token = tokenManager.getToken();
        if (token == null) {
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<EmployeeScheduleResponse> call = apiService.getEmployeeSchedule("Bearer " + token);

        call.enqueue(new Callback<EmployeeScheduleResponse>() {
            @Override
            public void onResponse(Call<EmployeeScheduleResponse> call, Response<EmployeeScheduleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EmployeeScheduleResponse scheduleResponse = response.body();
                    if (scheduleResponse.isSuccess() && scheduleResponse.getTickets() != null) {
                        displayTodaySchedule(scheduleResponse.getTickets());
                    }
                }
            }

            @Override
            public void onFailure(Call<EmployeeScheduleResponse> call, Throwable t) {
                // Handle failure silently for home screen
            }
        });
    }

    private void displayTodaySchedule(List<EmployeeScheduleResponse.ScheduledTicket> tickets) {
        if (getView() == null) return;

        LinearLayout scheduleContainer = getView().findViewById(R.id.scheduleItemsContainer);
        TextView tvNoEventsToday = getView().findViewById(R.id.tvNoEventsToday);
        
        if (scheduleContainer == null || tvNoEventsToday == null) return;

        // Clear existing items
        scheduleContainer.removeAllViews();

        // Get today's date in the format used by the API
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String todayDate = apiDateFormat.format(new Date());

        // Filter tickets for today
        List<EmployeeScheduleResponse.ScheduledTicket> todayTickets = new java.util.ArrayList<>();
        if (tickets != null) {
            for (EmployeeScheduleResponse.ScheduledTicket ticket : tickets) {
                if (ticket.getScheduledDate() != null && ticket.getScheduledDate().equals(todayDate)) {
                    todayTickets.add(ticket);
                }
            }
        }

        if (todayTickets.isEmpty()) {
            tvNoEventsToday.setVisibility(View.VISIBLE);
        } else {
            tvNoEventsToday.setVisibility(View.GONE);
            
            // Add schedule items
            for (EmployeeScheduleResponse.ScheduledTicket ticket : todayTickets) {
                View scheduleItem = createScheduleItemView(ticket);
                if (scheduleItem != null) {
                    scheduleContainer.addView(scheduleItem);
                }
            }
        }
    }

    private View createScheduleItemView(EmployeeScheduleResponse.ScheduledTicket ticket) {
        if (getContext() == null) return null;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemView = inflater.inflate(R.layout.item_schedule_home, null);

        // Hide the date header since we're showing today's items
        TextView tvScheduleDate = itemView.findViewById(R.id.tvScheduleDate);
        if (tvScheduleDate != null) {
            tvScheduleDate.setVisibility(View.GONE);
        }

        TextView tvScheduleTitle = itemView.findViewById(R.id.tvScheduleTitle);
        TextView tvScheduleLocation = itemView.findViewById(R.id.tvScheduleLocation);
        TextView tvScheduleStatus = itemView.findViewById(R.id.tvScheduleStatus);
        TextView tvScheduleTime = itemView.findViewById(R.id.tvScheduleTime);

        if (tvScheduleTitle != null) {
            String title = ticket.getServiceType() != null ? ticket.getServiceType() : "Service Request";
            tvScheduleTitle.setText(title);
        }

        if (tvScheduleLocation != null) {
            String location = ticket.getAddress() != null ? ticket.getAddress() : "Location not specified";
            tvScheduleLocation.setText(location);
        }

        if (tvScheduleStatus != null) {
            String status = ticket.getStatus() != null ? ticket.getStatus() : "Pending";
            tvScheduleStatus.setText(status);
        }

        if (tvScheduleTime != null) {
            String time = ticket.getScheduledTime() != null ? ticket.getScheduledTime() : "Time TBD";
            tvScheduleTime.setText(time);
        }

        // Add click listener to open ticket detail
        LinearLayout scheduleItemLayout = itemView.findViewById(R.id.scheduleItemLayout);
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
        loadTodaySchedule();
    }
}
