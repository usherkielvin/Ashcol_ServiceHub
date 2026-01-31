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

    public interface OnTicketClickListener {
        void onTicketClick(TicketListResponse.TicketItem ticket);
    }

    public TicketsAdapter(List<TicketListResponse.TicketItem> tickets) {
        this.tickets = tickets;
    }

    public void setOnTicketClickListener(OnTicketClickListener listener) {
        this.onTicketClickListener = listener;
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

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
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
            
            // Set status with proper formatting and background color
            String status = ticket.getStatus();
            String displayStatus = "Status: " + (status != null ? status : "Unknown");
            tvStatus.setText(displayStatus);
            
            // Set status background color based on status
            setStatusBackgroundColor(tvStatus, status);
            
            android.util.Log.d("TicketsAdapter", "Bound ticket: " + ticketId + " - " + title);
        }

        private void setStatusBackgroundColor(TextView textView, String status) {
            if (status == null || textView == null) return;
            
            // Set text color to white for all status badges
            textView.setTextColor(Color.WHITE);
            
            // Set background color based on status
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
                    textView.setBackgroundColor(Color.parseColor("#4CAF50")); // Default Green for "New Requests"
                    break;
            }
            
            // Apply rounded corners
            textView.setBackground(createRoundedBackground(getBackgroundColorForStatus(status)));
        }
        
        private int getBackgroundColorForStatus(String status) {
            if (status == null) return Color.parseColor("#4CAF50");
            
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
                    return Color.parseColor("#4CAF50"); // Default Green
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