package app.hub.employee;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.hub.R;
import app.hub.api.TicketListResponse;

public class EmployeeTicketsAdapter extends RecyclerView.Adapter<EmployeeTicketsAdapter.EmployeeTicketViewHolder> {

    private List<TicketListResponse.TicketItem> tickets;
    private OnTicketClickListener onTicketClickListener;

    public interface OnTicketClickListener {
        void onTicketClick(TicketListResponse.TicketItem ticket);
    }

    public EmployeeTicketsAdapter(List<TicketListResponse.TicketItem> tickets) {
        this.tickets = tickets;
    }

    public void setOnTicketClickListener(OnTicketClickListener listener) {
        this.onTicketClickListener = listener;
    }

    @NonNull
    @Override
    public EmployeeTicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee_ticket, parent, false);
        return new EmployeeTicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeTicketViewHolder holder, int position) {
        TicketListResponse.TicketItem ticket = tickets.get(position);
        holder.bind(ticket);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onTicketClickListener != null) {
                onTicketClickListener.onTicketClick(ticket);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    static class EmployeeTicketViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvTicketId;
        private TextView tvServiceType;
        private TextView tvStatus;
        private TextView tvDate;
        private TextView tvDescription;
        private TextView tvCustomerName;
        private TextView tvAddress;

        private TextView tvScheduleDate;
        private TextView tvScheduleNotes;
        private LinearLayout scheduleContainer;

        public EmployeeTicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvAddress = itemView.findViewById(R.id.tvAddress);

            tvScheduleDate = itemView.findViewById(R.id.tvScheduleDate);
            tvScheduleNotes = itemView.findViewById(R.id.tvScheduleNotes);
            scheduleContainer = itemView.findViewById(R.id.scheduleContainer);
        }

        public void bind(TicketListResponse.TicketItem ticket) {
            tvTitle.setText(ticket.getTitle());
            tvTicketId.setText(ticket.getTicketId());
            tvServiceType.setText(ticket.getServiceType());
            tvStatus.setText("Status: " + ticket.getStatus());
            tvDescription.setText(ticket.getDescription());
            tvCustomerName
                    .setText("Customer: " + (ticket.getCustomerName() != null ? ticket.getCustomerName() : "Unknown"));
            tvAddress.setText("Location: " + (ticket.getAddress() != null ? ticket.getAddress() : "Not specified"));

            // Set status color
            String statusColor = ticket.getStatusColor();
            if (statusColor != null && !statusColor.isEmpty()) {
                try {
                    tvStatus.setTextColor(Color.parseColor(statusColor));
                } catch (IllegalArgumentException e) {
                    // Fallback to default colors based on status
                    setStatusColor(tvStatus, ticket.getStatus());
                }
            } else {
                setStatusColor(tvStatus, ticket.getStatus());
            }

            // Format date
            String formattedDate = formatDate(ticket.getCreatedAt());
            tvDate.setText("Assigned: " + formattedDate);

            // Display schedule information
            if (ticket.getScheduledDate() != null && !ticket.getScheduledDate().isEmpty()) {
                String scheduleText = "Scheduled: " + formatDate(ticket.getScheduledDate());
                if (ticket.getScheduledTime() != null && !ticket.getScheduledTime().isEmpty()) {
                    scheduleText += " at " + formatTime(ticket.getScheduledTime());
                }
                tvScheduleDate.setText(scheduleText);
                scheduleContainer.setVisibility(View.VISIBLE);

                if (ticket.getScheduleNotes() != null && !ticket.getScheduleNotes().isEmpty()) {
                    tvScheduleNotes.setText("Notes: " + ticket.getScheduleNotes());
                    tvScheduleNotes.setVisibility(View.VISIBLE);
                } else {
                    tvScheduleNotes.setVisibility(View.GONE);
                }
            } else {
                scheduleContainer.setVisibility(View.GONE);
            }
        }

        private void setStatusColor(TextView textView, String status) {
            if (status == null)
                return;

            switch (status.toLowerCase()) {
                case "pending":
                    textView.setTextColor(Color.parseColor("#FFA500")); // Orange
                    break;
                case "scheduled":
                    textView.setTextColor(Color.parseColor("#6366F1")); // Indigo
                    break;
                case "accepted":
                case "in progress":
                case "ongoing":
                    textView.setTextColor(Color.parseColor("#2196F3")); // Blue
                    break;
                case "completed":
                    textView.setTextColor(Color.parseColor("#4CAF50")); // Green
                    break;
                case "cancelled":
                case "rejected":
                    textView.setTextColor(Color.parseColor("#F44336")); // Red
                    break;
                default:
                    textView.setTextColor(Color.parseColor("#757575")); // Gray
                    break;
            }
        }

        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "";
            }

            try {
                // Parse the date string (assuming ISO format from API)
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                Date date = inputFormat.parse(dateString);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException e) {
                // If parsing fails, try alternative format
                try {
                    SimpleDateFormat altInputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                            Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                    Date date = altInputFormat.parse(dateString);
                    if (date != null) {
                        return outputFormat.format(date);
                    }
                } catch (ParseException ex) {
                    // Return original string if all parsing fails
                    return dateString;
                }
            }

            return dateString;
        }

        private String formatTime(String timeString) {
            if (timeString == null || timeString.isEmpty()) {
                return "";
            }

            try {
                // Parse the time string (assuming HH:mm format from API)
                SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

                Date time = inputFormat.parse(timeString);
                if (time != null) {
                    return outputFormat.format(time);
                }
            } catch (ParseException e) {
                return timeString; // Return original if parsing fails
            }

            return timeString;
        }
    }
}