package app.hub.user;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.hub.R;
import app.hub.api.TicketListResponse;

public class TicketsAdapter extends RecyclerView.Adapter<TicketsAdapter.TicketViewHolder> {

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

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvTicketId;
        private TextView tvServiceType;
        private TextView tvStatus;
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
            tvStatus.setText(displayStatus);

            // Set status background color based on normalized status
            setStatusBackgroundColor(tvStatus, normalizedStatus);

            if (btnPayNow != null) {
                boolean showPay = normalizedStatus != null
                        && normalizedStatus.equalsIgnoreCase("completed");
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
            if (lowerStatus.equals("open")) {
                return "Pending";
            }
            if (lowerStatus.equals("active") ||
                    lowerStatus.equals("accepted") ||
                    lowerStatus.equals("assigned") ||
                    lowerStatus.equals("ongoing")) {
                return "In Progress";
            }
            return status; // Return original status for other values
        }

        private void setStatusBackgroundColor(TextView textView, String status) {
            if (status == null || textView == null)
                return;

            // Set text color to white for all status badges
            textView.setTextColor(Color.WHITE);

            // Set background color based on status (status is already normalized)
            switch (status.toLowerCase()) {
                case "pending":
                    textView.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
                    break;
                case "accepted":
                case "in progress":
                    textView.setBackgroundColor(Color.parseColor("#2196F3")); // Blue
                    break;
                case "completed":
                    textView.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
                    break;
                case "cancelled":
                case "rejected":
                    textView.setBackgroundColor(Color.parseColor("#F44336")); // Red
                    break;
                default:
                    // Default to orange for unknown statuses (treat as pending)
                    textView.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
                    break;
            }

            // Apply rounded corners
            textView.setBackground(createRoundedBackground(getBackgroundColorForStatus(status)));
        }

        private int getBackgroundColorForStatus(String status) {
            if (status == null)
                return Color.parseColor("#FF9800"); // Default to orange (pending)

            switch (status.toLowerCase()) {
                case "pending":
                    return Color.parseColor("#FF9800"); // Orange
                case "accepted":
                case "in progress":
                    return Color.parseColor("#2196F3"); // Blue
                case "completed":
                    return Color.parseColor("#4CAF50"); // Green
                case "cancelled":
                case "rejected":
                    return Color.parseColor("#F44336"); // Red
                default:
                    return Color.parseColor("#FF9800"); // Default Orange (pending)
            }
        }

        private android.graphics.drawable.GradientDrawable createRoundedBackground(int color) {
            android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
            drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            drawable.setColor(color);
            drawable.setCornerRadius(20f); // Rounded corners
            return drawable;
        }
    }
}