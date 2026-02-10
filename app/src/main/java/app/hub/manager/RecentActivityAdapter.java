package app.hub.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.hub.R;
import app.hub.api.DashboardStatsResponse;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {
    private List<DashboardStatsResponse.RecentTicket> recentTickets;
    private Context context;

    public RecentActivityAdapter(Context context) {
        this.context = context;
        this.recentTickets = new ArrayList<>();
    }

    public void setRecentTickets(List<DashboardStatsResponse.RecentTicket> recentTickets) {
        this.recentTickets = recentTickets != null ? recentTickets : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DashboardStatsResponse.RecentTicket ticket = recentTickets.get(position);

        holder.tvTicketId.setText(ticket.getTicketId());
        holder.tvStatusBadge.setText(ticket.getStatus().toUpperCase());
        holder.tvDate.setText(ticket.getCreatedAt());
        holder.tvCustomerName.setText(ticket.getCustomerName());
        holder.tvServiceType.setText(ticket.getServiceType());
        holder.tvDescription.setText(ticket.getDescription());
        holder.tvAddress.setText(ticket.getAddress());

        // Set status badge color
        holder.tvStatusBadge.getBackground().setTint(resolveStatusColor(ticket));

        // Click listener to open ticket details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ManagerTicketDetailActivity.class);
            intent.putExtra("ticket_id", ticket.getTicketId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recentTickets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicketId, tvStatusBadge, tvDate, tvCustomerName, tvServiceType, tvDescription, tvAddress;

        ViewHolder(View itemView) {
            super(itemView);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAddress = itemView.findViewById(R.id.tvAddress);
        }
    }

    private int resolveStatusColor(DashboardStatsResponse.RecentTicket ticket) {
        if (ticket == null) {
            return Color.parseColor("#757575");
        }

        String status = ticket.getStatus() != null ? ticket.getStatus().trim().toLowerCase() : "";
        if (status.equals("ongoing") || status.equals("in progress") || status.equals("accepted")) {
            return Color.parseColor("#2196F3");
        }

        String statusColor = ticket.getStatusColor();
        if (statusColor != null && !statusColor.isEmpty()) {
            try {
                return Color.parseColor(statusColor);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return Color.parseColor("#757575");
    }
}
