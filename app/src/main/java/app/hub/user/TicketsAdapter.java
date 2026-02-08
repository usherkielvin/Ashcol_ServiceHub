package app.hub.user;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import app.hub.R;
import app.hub.api.TicketListResponse;

public class TicketsAdapter extends RecyclerView.Adapter<TicketsAdapter.TicketViewHolder> {

    private Set<String> pendingPaymentTicketIds = Collections.emptySet();
    private Set<String> paidTicketIds = Collections.emptySet();
    private List<TicketListResponse.TicketItem> tickets;
    private OnTicketClickListener onTicketClickListener;
    private OnPaymentClickListener onPaymentClickListener;

    public interface OnTicketClickListener {
        void onTicketClick(TicketListResponse.TicketItem ticket);
    }

    public interface OnPaymentClickListener {
        void onPaymentClick(TicketListResponse.TicketItem ticket);
    }

    public TicketsAdapter(List<TicketListResponse.TicketItem> tickets) {
        this.tickets = tickets;
    }

    public void setOnTicketClickListener(OnTicketClickListener listener) {
        this.onTicketClickListener = listener;
    }

    public void setOnPaymentClickListener(OnPaymentClickListener listener) {
        this.onPaymentClickListener = listener;
    }

    public void setPendingPaymentTicketIds(Set<String> ticketIds) {
        pendingPaymentTicketIds = ticketIds != null ? ticketIds : Collections.emptySet();
    }

    public void setPaidTicketIds(Set<String> ticketIds) {
        paidTicketIds = ticketIds != null ? ticketIds : Collections.emptySet();
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
        if (position < 0 || position >= tickets.size()) {
            android.util.Log.w("TicketsAdapter", "Invalid position: " + position + ", tickets size: " + tickets.size());
            return;
        }

        TicketListResponse.TicketItem ticket = tickets.get(position);
        android.util.Log.d("TicketsAdapter", "Binding ticket at position " + position + ": " +
                (ticket != null ? ticket.getTicketId() : "null"));

        holder.bind(ticket);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onTicketClickListener != null && ticket != null) {
                android.util.Log.d("TicketsAdapter", "Ticket clicked: " + ticket.getTicketId());
                onTicketClickListener.onTicketClick(ticket);
            }
        });

        if (holder.btnPayNow != null) {
            holder.btnPayNow.setOnClickListener(v -> {
                if (onPaymentClickListener != null && ticket != null) {
                    onPaymentClickListener.onPaymentClick(ticket);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        int count = tickets != null ? tickets.size() : 0;
        android.util.Log.d("TicketsAdapter", "getItemCount: " + count);
        return count;
    }

    class TicketViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvTicketId;
        private TextView tvServiceType;
        private com.google.android.material.button.MaterialButton tvStatus;
        private com.google.android.material.button.MaterialButton btnPayNow;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnPayNow = itemView.findViewById(R.id.btnPayNow);
        }

        public void bind(TicketListResponse.TicketItem ticket) {
            if (ticket == null) {
                android.util.Log.w("TicketsAdapter", "Ticket is null, cannot bind");
                return;
            }

            // Set title with null check
            String title = ticket.getTitle();
            tvTitle.setText(title != null ? title : "Service Request");

            // Set service type with null check and bullet point
            String serviceType = ticket.getServiceType();
            tvServiceType.setText("• " + (serviceType != null ? serviceType : "Service Type"));

            // Set ticket ID with "Requested by:" prefix
            String ticketId = ticket.getTicketId();
            tvTicketId.setText("Requested by: " + (ticketId != null ? ticketId : "Unknown ID"));

            // Normalize status: "Open" should display as "Pending" with orange color
            String status = ticket.getStatus();
            String normalizedStatus = normalizeStatus(status);
            String displayStatus = normalizedStatus != null ? normalizedStatus : "Unknown";

            boolean isPaid = ticketId != null && paidTicketIds.contains(ticketId);
            if (isPaid && "completed".equalsIgnoreCase(displayStatus)) {
                displayStatus = "Paid";
            }

            if (tvStatus != null) {
                tvStatus.setText(displayStatus);
                applyStatusStyle(tvStatus, normalizedStatus);
            }

            if (btnPayNow != null) {
                boolean showPay = false;
                if (normalizedStatus != null && normalizedStatus.equalsIgnoreCase("completed")) {
                    showPay = ticketId != null && pendingPaymentTicketIds.contains(ticketId);
                }
                btnPayNow.setVisibility(showPay ? View.VISIBLE : View.GONE);
            }

            android.util.Log.d("TicketsAdapter", "Bound ticket: " + ticketId + " - " + title + " (status: " + status
                    + " -> " + normalizedStatus + ")");
        }

        /**
         * Normalize status values: "Open" maps to "Pending" for consistent display
         */
        private String normalizeStatus(String status) {
            if (status == null)
                return null;
            String lowerStatus = status.toLowerCase().trim();
            // Map "Open" to "Pending" since they represent the same state for customers
            if (lowerStatus.equals("open") || lowerStatus.equals("submitted")) {
                return "Pending";
            }
            if (lowerStatus.equals("active") ||
                    lowerStatus.equals("accepted") ||
                    lowerStatus.equals("assigned") ||
                    lowerStatus.equals("ongoing") ||
                    lowerStatus.equals("on the way") ||
                    lowerStatus.equals("arrived")) {
                return "In Progress";
            }
            if (lowerStatus.equals("canceled")) {
                return "Cancelled";
            }
            // Capitalize first letter for others
            if (status.length() > 0) {
                return status.substring(0, 1).toUpperCase() + status.substring(1);
            }
            return status;
        }

        private void applyStatusStyle(com.google.android.material.button.MaterialButton button, String status) {
            if (button == null || status == null) {
                return;
            }

            int color = getBackgroundColorForStatus(status);
            button.setTextColor(Color.WHITE);
            button.setBackgroundTintList(ColorStateList.valueOf(color));
        }

        private int getBackgroundColorForStatus(String status) {
            if (status == null)
                return Color.parseColor("#FF9800"); // Default to orange (pending)

            switch (status.toLowerCase()) {
                case "pending":
                case "open":
                case "submitted":
                    return Color.parseColor("#FF9800"); // Orange
                case "in progress":
                case "accepted":
                case "active":
                case "assigned":
                case "ongoing":
                case "on the way":
                case "arrived":
                    return Color.parseColor("#2196F3"); // Blue
                case "completed":
                case "paid":
                case "closed": // Treat closed as completed/history
                    return Color.parseColor("#4CAF50"); // Green
                case "rejected":
                case "cancelled":
                case "canceled": // Handle spelling var
                    return Color.parseColor("#F44336"); // Red
                default:
                    return Color.parseColor("#FF9800"); // Default Orange (pending)
            }
        }

    }
}