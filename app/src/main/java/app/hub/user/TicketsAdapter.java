package app.hub.user;

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
import app.hub.api.TicketListResponse;

public class TicketsAdapter extends RecyclerView.Adapter<TicketsAdapter.TicketViewHolder> {

    private List<TicketListResponse.TicketItem> tickets;

    public TicketsAdapter(List<TicketListResponse.TicketItem> tickets) {
        this.tickets = tickets;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        TicketListResponse.TicketItem ticket = tickets.get(position);
        holder.bind(ticket);
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvTicketId;
        private TextView tvServiceType;
        private TextView tvStatus;
        private TextView tvDate;
        private TextView tvDescription;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }

        public void bind(TicketListResponse.TicketItem ticket) {
            tvTitle.setText(ticket.getTitle());
            tvTicketId.setText("Requested by: " + ticket.getTicketId());
            tvServiceType.setText(ticket.getServiceType());
            tvStatus.setText("Status: " + ticket.getStatus());
            tvDescription.setText(ticket.getDescription());

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
            tvDate.setText(formattedDate);
        }

        private void setStatusColor(TextView textView, String status) {
            if (status == null) return;
            
            switch (status.toLowerCase()) {
                case "pending":
                    textView.setTextColor(Color.parseColor("#FFA500")); // Orange
                    break;
                case "accepted":
                case "in progress":
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
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return dateString; // Return original if parsing fails
            }
        }
    }
}