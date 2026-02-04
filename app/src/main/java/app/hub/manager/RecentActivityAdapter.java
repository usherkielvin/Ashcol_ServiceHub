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

        // Format the text to show ticket ID and status
        String displayText = "• " + ticket.getTicketId() + " - " + ticket.getStatus();
        holder.tvTicketId.setText(displayText);

        // Set status color
        try {
            int color = Color.parseColor(ticket.getStatusColor());
            holder.tvTicketId.setTextColor(color);
        } catch (Exception e) {
            // Fallback to default color if parsing fails
            holder.tvTicketId.setTextColor(Color.BLACK);
        }

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
        TextView tvTicketId;

        ViewHolder(View itemView) {
            super(itemView);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
        }
    }
}
