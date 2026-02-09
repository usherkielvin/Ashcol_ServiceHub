package app.hub.employee;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import app.hub.R;

public class EmployeeWorkJobAssignFragment extends Fragment {

    private static final String ARG_TICKET_ID = "ticket_id";
    private static final String ARG_CUSTOMER_NAME = "customer_name";
    private static final String ARG_CUSTOMER_PHONE = "customer_phone";
    private static final String ARG_SERVICE_TYPE = "service_type";
    private static final String ARG_SCHEDULE = "schedule";
    private static final String ARG_SCHEDULE_DATE = "schedule_date";
    private static final String ARG_SCHEDULE_TIME = "schedule_time";
    private static final String ARG_NOTE = "note";
    private static final String ARG_STATUS = "status";

    private TextView tvTicketId;
    private TextView tvCustomerName;
    private TextView tvCustomerPhone;
    private TextView tvServiceType;
    private TextView tvSchedule;
    private TextView tvNote;
    private TextView tvOnWayStatus;
    private TextView tvArrivedStatus;
    private TextView tvInProgressStatus;
    private TextView tvCompletedStatus;
    private ImageView ivStep1;
    private ImageView ivStep2;
    private ImageView ivStep3;
    private ImageView ivStep4;
    private ImageView ivStep5;
    private View vLine;
    private MaterialButton btnCancelJob;

    public EmployeeWorkJobAssignFragment() {
        // Required empty public constructor
    }

    public static EmployeeWorkJobAssignFragment newInstance() {
        return new EmployeeWorkJobAssignFragment();
    }

    public static EmployeeWorkJobAssignFragment newInstance(String ticketId, String customerName,
            String customerPhone, String serviceType, String schedule, String note, String status) {
        EmployeeWorkJobAssignFragment fragment = new EmployeeWorkJobAssignFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TICKET_ID, ticketId);
        args.putString(ARG_CUSTOMER_NAME, customerName);
        args.putString(ARG_CUSTOMER_PHONE, customerPhone);
        args.putString(ARG_SERVICE_TYPE, serviceType);
        args.putString(ARG_SCHEDULE, schedule);
        args.putString(ARG_NOTE, note);
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_employee_work_jobassign, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        bindJobDetails(getArguments());

        btnCancelJob.setOnClickListener(v -> showCancelDialog());
    }

    private void bindViews(@NonNull View view) {
        tvTicketId = view.findViewById(R.id.tvTicketId);
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvCustomerPhone = view.findViewById(R.id.tvCustomerPhone);
        tvServiceType = view.findViewById(R.id.tvServiceType);
        tvSchedule = view.findViewById(R.id.tvSchedule);
        tvNote = view.findViewById(R.id.tvNote);
        tvOnWayStatus = view.findViewById(R.id.tvOnWayStatus);
        tvArrivedStatus = view.findViewById(R.id.tvArrivedStatus);
        tvInProgressStatus = view.findViewById(R.id.tvInProgressStatus);
        tvCompletedStatus = view.findViewById(R.id.tvCompletedStatus);
        ivStep1 = view.findViewById(R.id.ivStep1);
        ivStep2 = view.findViewById(R.id.ivStep2);
        ivStep3 = view.findViewById(R.id.ivStep3);
        ivStep4 = view.findViewById(R.id.ivStep4);
        ivStep5 = view.findViewById(R.id.ivStep5);
        vLine = view.findViewById(R.id.vLine);
        btnCancelJob = view.findViewById(R.id.btnCancelJob);
    }

    private void bindJobDetails(@Nullable Bundle args) {
        String ticketId = getArg(args, ARG_TICKET_ID, "CLN1005");
        String customerName = getArg(args, ARG_CUSTOMER_NAME, "Juan Dela Cruz");
        String customerPhone = getArg(args, ARG_CUSTOMER_PHONE, "09123456789");
        String serviceType = getArg(args, ARG_SERVICE_TYPE, "AC Cleaning");
        String scheduleText = getArg(args, ARG_SCHEDULE, null);
        String scheduleDate = getArg(args, ARG_SCHEDULE_DATE, null);
        String scheduleTime = getArg(args, ARG_SCHEDULE_TIME, null);
        String noteText = getArg(args, ARG_NOTE, "Please call before arriving.");
        String status = getArg(args, ARG_STATUS, "assigned");

        tvTicketId.setText("Ticket ID: " + ticketId);
        tvCustomerName.setText("Name: " + customerName);
        tvCustomerPhone.setText("Phone number: " + customerPhone);
        tvServiceType.setText("Service Type: " + serviceType);

        if (scheduleText == null || scheduleText.trim().isEmpty()) {
            scheduleText = buildSchedule(scheduleDate, scheduleTime);
        }
        if (scheduleText == null || scheduleText.trim().isEmpty()) {
            scheduleText = "Feb 4, 2026 - 2:00 PM";
        }
        tvSchedule.setText("Schedule: " + scheduleText);

        if (noteText == null || noteText.trim().isEmpty()) {
            noteText = "None";
        }
        tvNote.setText("Note: \"" + noteText + "\"");

        updateProgressUi(resolveStepFromStatus(status));
    }

    private String buildSchedule(@Nullable String date, @Nullable String time) {
        if (date == null && time == null) {
            return null;
        }
        if (date == null) {
            return time;
        }
        if (time == null) {
            return date;
        }
        return date + " - " + time;
    }

    private String getArg(@Nullable Bundle args, String key, String fallback) {
        if (args == null) {
            return fallback;
        }
        String value = args.getString(key);
        return value != null ? value : fallback;
    }

    private int resolveStepFromStatus(@Nullable String status) {
        if (status == null) {
            return 1;
        }

        String normalized = status.toLowerCase().trim().replace('_', ' ');
        if (normalized.contains("on the way") || normalized.equals("otw")) {
            return 2;
        }
        if (normalized.contains("arrived")) {
            return 3;
        }
        if (normalized.contains("in progress")) {
            return 4;
        }
        if (normalized.contains("completed") || normalized.contains("done")) {
            return 5;
        }
        return 1;
    }

    private void updateProgressUi(int activeStep) {
        applyStepState(ivStep1, null, activeStep > 1, activeStep == 1);
        applyStepState(ivStep2, tvOnWayStatus, activeStep > 2, activeStep == 2);
        applyStepState(ivStep3, tvArrivedStatus, activeStep > 3, activeStep == 3);
        applyStepState(ivStep4, tvInProgressStatus, activeStep > 4, activeStep == 4);
        applyStepState(ivStep5, tvCompletedStatus, activeStep >= 5, activeStep == 5);

        int lineColor = MaterialColors.getColor(vLine, com.google.android.material.R.attr.colorOutline);
        if (activeStep > 1) {
            int green = ContextCompat.getColor(requireContext(), R.color.status_green);
            lineColor = ColorUtils.setAlphaComponent(green, 120);
        }
        vLine.setBackgroundTintList(ColorStateList.valueOf(lineColor));
    }

    private void applyStepState(@NonNull ImageView stepView, @Nullable TextView labelView,
            boolean completed, boolean active) {
        int onSurface = MaterialColors.getColor(stepView, com.google.android.material.R.attr.colorOnSurface);
        int onSurfaceVariant = MaterialColors.getColor(stepView,
                com.google.android.material.R.attr.colorOnSurfaceVariant);
        int statusGreen = ContextCompat.getColor(requireContext(), R.color.status_green);

        stepView.setImageResource(R.drawable.ic_completed);
        stepView.setBackgroundTintList(null);

        if (completed) {
            stepView.setBackgroundResource(R.drawable.bg_step_solid_green);
            stepView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)));
        } else if (active) {
            stepView.setBackgroundResource(R.drawable.bg_step_outline_green);
            stepView.setImageTintList(ColorStateList.valueOf(statusGreen));
        } else {
            stepView.setBackgroundResource(R.drawable.shape_circle_gray);
            stepView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)));
        }

        if (labelView != null) {
            labelView.setTextColor(completed || active ? onSurface : onSurfaceVariant);
            labelView.setAlpha(completed || active ? 1f : 0.75f);
        }
    }

    private void showCancelDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cancel job?")
                .setMessage("This will mark the job as cancelled. You can change this later if needed.")
                .setPositiveButton("Yes, cancel", (dialog, which) ->
                        Toast.makeText(requireContext(), "Job cancellation requested.", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Keep job", null)
                .show();
    }
}
