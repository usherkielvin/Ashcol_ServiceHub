package app.hub.manager;

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

public class ManagerCompleteTicketsAdapter extends RecyclerView.Adapter<ManagerCompleteTicketsAdapter.CompleteTicketViewHolder> {

    private List<TicketListResponse.TicketItem> tickets;
    private OnTicketClickListener onTicketClickListener;

    public interface OnTicketClickListener {
        void onTicketClick(TicketListResponse.TicketItem ticket);
    }

    public ManagerCompleteTicketsAdapter(List<TicketListResponse.TicketItem> tickets) {
        this.tickets = tickets;
    }

    public void setOnTicketClickListener(OnTicketClickListener listener) {
        this.onTicketClickListener = listener;
    }

    @NonNull
    @Override
    public CompleteTicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manager_complete_ticket, parent, false);
        return new CompleteTicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompleteTicketViewHolder holder, int position) {
        TicketListResponse.TicketItem ticket = tickets.get(position);
        holder.bind(ticket);

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

    static class CompleteTicketViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvTicketId;
        private TextView tvServiceType;
        private TextView tvStatus;
        private TextView tvDate;
        private TextView tvCustomerName;
        private TextView tvAmount;

        public CompleteTicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }

        public void bind(TicketListResponse.TicketItem ticket) {
            tvTitle.setText(getTitleOrTicketId(ticket));
            tvTicketId.setText(ticket.getTicketId());
            tvServiceType.setText(buildServiceText(ticket));
            
            // Display actual status (Completed or Cancelled)
            String status = ticket.getStatus();
            if (status != null) {
                String normalizedStatus = status.toLowerCase().trim();
                if (normalizedStatus.contains("cancelled") || normalizedStatus.contains("canceled") || normalizedStatus.contains("rejected")) {
                    tvStatus.setText("Cancelled");
                    // Set status color to red for cancelled
                    int color = Color.parseColor("#F44336");
                    tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
                } else {
                    tvStatus.setText("Completed");
                    // Set status color to green for completed
                    int color = Color.parseColor("#4CAF50");
                    tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
                }
            } else {
                tvStatus.setText("Completed");
                int color = Color.parseColor("#4CAF50");
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
            }
            tvStatus.setTextColor(Color.WHITE);
            
            tvCustomerName.setText(ticket.getCustomerName() != null ? ticket.getCustomerName() : "Unknown");
            
            // Display amount
            tvAmount.setText(String.format(Locale.getDefault(), "Php %,.2f", ticket.getAmount()));

            // Format date
            String formattedDate = formatDate(getHistoryDate(ticket));
            tvDate.setText(formattedDate);
        }

        private String buildServiceText(TicketListResponse.TicketItem ticket) {
            String service = ticket.getServiceType();
            if (service == null || service.trim().isEmpty()) {
                service = ticket.getDescription();
            }
            if (service == null || service.trim().isEmpty()) {
                return "• Service";
            }
            return "• " + service.trim();
        }

        private String getHistoryDate(TicketListResponse.TicketItem ticket) {
            if (ticket.getUpdatedAt() != null && !ticket.getUpdatedAt().isEmpty()) {
                return ticket.getUpdatedAt();
            }
            return ticket.getCreatedAt();
        }

        private String getTitleOrTicketId(TicketListResponse.TicketItem ticket) {
            String title = ticket.getTitle();
            if (title != null && !title.trim().isEmpty()) {
                return title;
            }
            String ticketId = ticket.getTicketId();
            if (ticketId != null && !ticketId.trim().isEmpty()) {
                return ticketId;
            }
            return "Service";
        }

        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "";
            }

            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());

                Date date = inputFormat.parse(dateString);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException e) {
                try {
                    SimpleDateFormat altInputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                            Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());

                    Date date = altInputFormat.parse(dateString);
                    if (date != null) {
                        return outputFormat.format(date);
                    }
                } catch (ParseException ex) {
                    return dateString;
                }
            }

            return dateString;
        }
    }
}
