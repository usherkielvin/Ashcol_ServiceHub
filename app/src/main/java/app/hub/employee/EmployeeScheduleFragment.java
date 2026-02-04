package app.hub.employee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.EmployeeScheduleResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeScheduleFragment extends Fragment {

    private TextView tvMonthYear;
    private ImageButton btnPreviousMonth, btnNextMonth;
    private RecyclerView rvCalendarGrid;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView tabAll, tabPending, tabInProgress, tabCompleted;
    private String currentStatusFilter = "all"; // all, pending, in_progress, completed

    private TokenManager tokenManager;
    private Calendar currentCalendar;
    private CalendarAdapter calendarAdapter;
    private List<CalendarAdapter.CalendarDay> calendarDays;
    private Map<String, List<EmployeeScheduleResponse.ScheduledTicket>> allBufferedTickets = new HashMap<>(); // Store
                                                                                                              // all
                                                                                                              // fetched
                                                                                                              // tickets
    private Map<String, List<EmployeeScheduleResponse.ScheduledTicket>> scheduledTicketsMap; // Displayed tickets
    private FirebaseEmployeeListener firebaseListener;

    public EmployeeScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee_schedule, container, false);

        initViews(view);
        setupClickListeners();
        initializeCalendar();
        loadScheduleData();

        return view;
    }

    private void initViews(View view) {
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        rvCalendarGrid = view.findViewById(R.id.rvCalendarGrid);
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        tabAll = view.findViewById(R.id.tabAll);
        tabPending = view.findViewById(R.id.tabPending);
        tabInProgress = view.findViewById(R.id.tabInProgress);
        tabCompleted = view.findViewById(R.id.tabCompleted);

        tokenManager = new TokenManager(getContext());
        currentCalendar = Calendar.getInstance();
        calendarDays = new ArrayList<>();
        scheduledTicketsMap = new HashMap<>();

        setupTabClickListeners();

        firebaseListener = new FirebaseEmployeeListener(getContext());
        firebaseListener.setOnScheduleChangeListener(new FirebaseEmployeeListener.OnScheduleChangeListener() {
            @Override
            public void onScheduleChanged() {
                android.util.Log.d("EmployeeSchedule", "Schedule changed detected via Firebase");
                loadScheduleData();
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("EmployeeSchedule", "Firebase listener error: " + error);
            }
        });

        // Setup RecyclerView
        rvCalendarGrid.setLayoutManager(new GridLayoutManager(getContext(), 7));
        calendarAdapter = new CalendarAdapter(calendarDays,
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.YEAR));
        rvCalendarGrid.setAdapter(calendarAdapter);

        calendarAdapter.setOnDayClickListener(day -> {
            if (day.getScheduledTickets() != null && !day.getScheduledTickets().isEmpty()) {
                showDayScheduleDialog(day);
            }
        });

        // Setup pull-to-refresh
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                android.util.Log.d("EmployeeSchedule", "Pull-to-refresh triggered");
                loadScheduleData();
            });
        }
    }

    private void setupClickListeners() {
        btnPreviousMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
            loadScheduleData();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
            loadScheduleData();
        });
    }

    private void initializeCalendar() {
        updateCalendar();
    }

    private void updateCalendar() {
        calendarDays.clear();

        // Set calendar to first day of month
        Calendar calendar = (Calendar) currentCalendar.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // Get day of week for first day (0 = Sunday, 1 = Monday, etc.)
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Convert to 0-based

        // Add empty cells for days before the first day of month
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarDays.add(new CalendarAdapter.CalendarDay(0, 0, 0, false));
        }

        // Add days of current month
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        for (int day = 1; day <= daysInMonth; day++) {
            CalendarAdapter.CalendarDay calendarDay = new CalendarAdapter.CalendarDay(
                    day, currentMonth, currentYear, true);

            // Check for scheduled tickets
            String dateKey = calendarDay.getDateKey();
            if (scheduledTicketsMap.containsKey(dateKey)) {
                calendarDay.setScheduledTickets(scheduledTicketsMap.get(dateKey));
            }

            calendarDays.add(calendarDay);
        }

        // Add empty cells to complete the grid (6 rows * 7 days = 42 cells)
        while (calendarDays.size() < 42) {
            calendarDays.add(new CalendarAdapter.CalendarDay(0, 0, 0, false));
        }

        // Update month/year display
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(monthYearFormat.format(calendar.getTime()));

        calendarAdapter.notifyDataSetChanged();
    }

    private void loadScheduleData() {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(getContext(), "You are not logged in.", Toast.LENGTH_SHORT).show();
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        // Show progress bar if not refreshing
        if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ApiService apiService = ApiClient.getApiService();
        Call<EmployeeScheduleResponse> call = apiService.getEmployeeSchedule("Bearer " + token);

        call.enqueue(new Callback<EmployeeScheduleResponse>() {
            @Override
            public void onResponse(Call<EmployeeScheduleResponse> call, Response<EmployeeScheduleResponse> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    EmployeeScheduleResponse scheduleResponse = response.body();
                    android.util.Log.d("EmployeeSchedule", "API Success, Success: " + scheduleResponse.isSuccess());

                    if (scheduleResponse.getTickets() != null) {
                        android.util.Log.d("EmployeeSchedule", "Ticket count: " + scheduleResponse.getTickets().size());
                    } else {
                        android.util.Log.d("EmployeeSchedule", "Tickets list is null");
                    }

                    if (scheduleResponse.isSuccess()) {
                        processScheduleData(scheduleResponse.getTickets());
                    } else {
                        // Even if strictly not success, if we have no data, show empty calendar
                        processScheduleData(new ArrayList<>());
                    }
                } else {
                    android.util.Log.e("EmployeeSchedule", "API Error: " + response.code());
                    // Show empty calendar on error
                    processScheduleData(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<EmployeeScheduleResponse> call, Throwable t) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load schedule: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void processScheduleData(List<EmployeeScheduleResponse.ScheduledTicket> tickets) {
        // Store all tickets
        allBufferedTickets.clear();
        if (tickets != null) {
            // Group tickets by date for allBufferedTickets
            for (EmployeeScheduleResponse.ScheduledTicket ticket : tickets) {
                if (ticket.getScheduledDate() != null) {
                    String dateKey = ticket.getScheduledDate();
                    if (!allBufferedTickets.containsKey(dateKey)) {
                        allBufferedTickets.put(dateKey, new ArrayList<>());
                    }
                    allBufferedTickets.get(dateKey).add(ticket);
                }
            }
        }

        applyFilter();
    }

    private void applyFilter() {
        scheduledTicketsMap.clear();
        hideEmptyState(); // Always show calendar

        if (allBufferedTickets.isEmpty()) {
            android.util.Log.d("EmployeeSchedule", "No tickets to display");
            updateCalendar();
            return;
        }

        // Apply filter
        int count = 0;
        for (Map.Entry<String, List<EmployeeScheduleResponse.ScheduledTicket>> entry : allBufferedTickets.entrySet()) {
            String dateKey = entry.getKey();
            List<EmployeeScheduleResponse.ScheduledTicket> dayTickets = entry.getValue();
            List<EmployeeScheduleResponse.ScheduledTicket> filteredDayTickets = new ArrayList<>();

            for (EmployeeScheduleResponse.ScheduledTicket ticket : dayTickets) {
                boolean matches = false;
                String status = ticket.getStatus() != null ? ticket.getStatus().toLowerCase().trim() : "";

                if (currentStatusFilter.equals("all")) {
                    matches = true;
                } else if (currentStatusFilter.equals("pending")) {
                    matches = status.contains("pending") || status.contains("open");
                } else if (currentStatusFilter.equals("in_progress")) {
                    matches = status.contains("progress") || status.contains("accepted") || status.contains("ongoing");
                } else if (currentStatusFilter.equals("completed")) {
                    matches = status.contains("completed") || status.contains("resolved") || status.contains("closed");
                }

                android.util.Log.d("ScheduleFilter", "Ticket: " + ticket.getTicketId() + " Status: '" + status
                        + "' Filter: " + currentStatusFilter + " Match: " + matches);

                if (matches) {
                    filteredDayTickets.add(ticket);
                    count++;
                }
            }

            if (!filteredDayTickets.isEmpty()) {
                scheduledTicketsMap.put(dateKey, filteredDayTickets);
            }
        }

        android.util.Log.d("EmployeeSchedule", "Filtered tickets: " + count + " (Filter: " + currentStatusFilter + ")");
        updateCalendar();
    }

    private void setupTabClickListeners() {
        // Initially select 'All'
        updateTabStyles(tabAll);

        tabAll.setOnClickListener(v -> {
            updateTabStyles(tabAll);
            currentStatusFilter = "all";
            applyFilter();
        });

        tabPending.setOnClickListener(v -> {
            updateTabStyles(tabPending);
            currentStatusFilter = "pending";
            applyFilter();
        });

        tabInProgress.setOnClickListener(v -> {
            updateTabStyles(tabInProgress);
            currentStatusFilter = "in_progress";
            applyFilter();
        });

        tabCompleted.setOnClickListener(v -> {
            updateTabStyles(tabCompleted);
            currentStatusFilter = "completed";
            applyFilter();
        });
    }

    private void updateTabStyles(TextView selectedTab) {
        // Reset all tabs
        TextView[] tabs = { tabAll, tabPending, tabInProgress, tabCompleted };
        for (TextView tab : tabs) {
            tab.setBackground(getResources().getDrawable(R.drawable.bg_input_field));
            tab.setTextColor(getResources().getColor(R.color.dark_gray));
        }

        // Highlight selected
        selectedTab.setBackground(getResources().getDrawable(R.drawable.bg_status_badge));
        selectedTab.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void showDayScheduleDialog(CalendarAdapter.CalendarDay day) {
        List<EmployeeScheduleResponse.ScheduledTicket> tickets = day.getScheduledTickets();
        if (tickets == null || tickets.isEmpty())
            return;

        // Create bottom sheet dialog
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                getContext());

        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_daily_schedule, null);

        // Setup dialog views
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvJobCount = dialogView.findViewById(R.id.tvJobCount);
        RecyclerView rvDailyJobs = dialogView.findViewById(R.id.rvDailyJobs);

        // Set title with date
        tvDialogTitle.setText("Jobs for " + day.getDateKey());
        tvJobCount.setText(tickets.size() + (tickets.size() == 1 ? " job" : " jobs"));

        // Setup RecyclerView
        rvDailyJobs.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        DailyScheduleAdapter adapter = new DailyScheduleAdapter();
        adapter.setTickets(tickets);
        adapter.setOnTicketClickListener(ticket -> {
            // Open ticket detail activity
            dialog.dismiss();
            openTicketDetail(ticket.getTicketId());
        });
        rvDailyJobs.setAdapter(adapter);

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void openTicketDetail(String ticketId) {
        if (ticketId == null)
            return;

        android.content.Intent intent = new android.content.Intent(getContext(),
                app.hub.employee.EmployeeTicketDetailActivity.class);
        intent.putExtra("ticket_id", ticketId);
        startActivity(intent);
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        // Do NOT hide the calendar grid
        rvCalendarGrid.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        rvCalendarGrid.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start Firebase listener
        if (firebaseListener != null) {
            firebaseListener.startListening();
        }
        // Refresh schedule data when fragment becomes visible
        loadScheduleData();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop Firebase listener
        if (firebaseListener != null) {
            firebaseListener.stopListening();
        }
    }
}
