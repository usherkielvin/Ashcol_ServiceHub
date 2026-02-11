package app.hub.admin;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.hub.R;
import app.hub.api.BranchTicketsResponse;

public class BranchTicketsAdapter extends RecyclerView.Adapter<BranchTicketsAdapter.ViewHolder> {

    private List<BranchTicketsResponse.Ticket> tickets;

    public BranchTicketsAdapter(List<BranchTicketsResponse.Ticket> tickets) {
        this.tickets = tickets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BranchTicketsResponse.Ticket ticket = tickets.get(position);
        holder.bind(ticket);
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    public void updateData(List<BranchTicketsResponse.Ticket> newTickets) {
        this.tickets = newTickets;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvServiceType;
        private TextView tvStatus;
        private TextView tvUnitType;
        private TextView tvTicketIdValue;
        private TextView tvDateValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvUnitType = itemView.findViewById(R.id.tvUnitType);
            tvTicketIdValue = itemView.findViewById(R.id.tvTicketIdValue);
            tvDateValue = itemView.findViewById(R.id.tvDateValue);
        }

        public void bind(BranchTicketsResponse.Ticket ticket) {
            // Set service type
            tvServiceType.setText(ticket.getServiceType());

            // Set status with color
            String status = ticket.getStatus();
            tvStatus.setText("Status: " + status);
            
            // Set status color
            String statusColor = ticket.getStatusColor();
            if (statusColor != null && !statusColor.isEmpty()) {
                try {
                    tvStatus.setBackgroundColor(Color.parseColor(statusColor));
                } catch (IllegalArgumentException e) {
                    // Default color if parsing fails
                    if ("Completed".equalsIgnoreCase(status)) {
                        tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
                    } else if ("Cancelled".equalsIgnoreCase(status)) {
                        tvStatus.setBackgroundColor(Color.parseColor("#F44336"));
                    }
                }
            }

            // Set unit type (extract from title if available)
            String title = ticket.getTitle();
            if (title != null && !title.isEmpty()) {
                tvUnitType.setText("- " + title);
            } else {
                tvUnitType.setText("- 1 unit");
            }

            // Set ticket ID
            tvTicketIdValue.setText(ticket.getTicketId());

            // Format and set date
            String createdAt = ticket.getCreatedAt();
            if (createdAt != null && !createdAt.isEmpty()) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy - h:mm a", Locale.getDefault());
                    Date date = inputFormat.parse(createdAt);
                    if (date != null) {
                        tvDateValue.setText(outputFormat.format(date));
                    } else {
                        tvDateValue.setText(createdAt);
                    }
                } catch (ParseException e) {
                    tvDateValue.setText(createdAt);
                }
            } else {
                tvDateValue.setText("N/A");
            }
        }
    }
}
