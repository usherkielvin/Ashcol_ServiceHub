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
    
    private TokenManager tokenManager;
    private Calendar currentCalendar;
    private CalendarAdapter calendarAdapter;
    private List<CalendarAdapter.CalendarDay> calendarDays;
    private Map<String, List<EmployeeScheduleResponse.ScheduledTicket>> scheduledTicketsMap;
    
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
        
        tokenManager = new TokenManager(getContext());
        currentCalendar = Calendar.getInstance();
        calendarDays = new ArrayList<>();
        scheduledTicketsMap = new HashMap<>();
        
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
                    if (scheduleResponse.isSuccess()) {
                        processScheduleData(scheduleResponse.getTickets());
                    } else {
                        showEmptyState();
                    }
                } else {
                    showEmptyState();
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
        scheduledTicketsMap.clear();
        
        if (tickets == null || tickets.isEmpty()) {
            showEmptyState();
            updateCalendar();
            return;
        }
        
        // Group tickets by date
        for (EmployeeScheduleResponse.ScheduledTicket ticket : tickets) {
            if (ticket.getScheduledDate() != null) {
                String dateKey = ticket.getScheduledDate(); // Format: YYYY-MM-DD
                if (!scheduledTicketsMap.containsKey(dateKey)) {
                    scheduledTicketsMap.put(dateKey, new ArrayList<>());
                }
                scheduledTicketsMap.get(dateKey).add(ticket);
            }
        }
        
        hideEmptyState();
        updateCalendar();
    }
    
    private void showDayScheduleDialog(CalendarAdapter.CalendarDay day) {
        List<EmployeeScheduleResponse.ScheduledTicket> tickets = day.getScheduledTickets();
        if (tickets == null || tickets.isEmpty()) return;
        
        // Create and show dialog with ticket details
        // This would show a bottom sheet or dialog with the scheduled tickets for that day
        StringBuilder message = new StringBuilder();
        message.append("Scheduled for ").append(day.getDateKey()).append("\n\n");
        
        for (int i = 0; i < tickets.size(); i++) {
            EmployeeScheduleResponse.ScheduledTicket ticket = tickets.get(i);
            message.append(i + 1).append(". ").append(ticket.getTitle()).append("\n");
            message.append("   Time: ").append(ticket.getScheduledTime()).append("\n");
            message.append("   Customer: ").append(ticket.getCustomerName()).append("\n");
            message.append("   Status: ").append(ticket.getStatus()).append("\n\n");
        }
        
        Toast.makeText(getContext(), message.toString(), Toast.LENGTH_LONG).show();
    }
    
    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        rvCalendarGrid.setVisibility(View.GONE);
    }
    
    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        rvCalendarGrid.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh schedule data when fragment becomes visible
        loadScheduleData();
    }
}
