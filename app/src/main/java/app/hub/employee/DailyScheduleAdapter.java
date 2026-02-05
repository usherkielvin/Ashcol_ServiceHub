package app.hub.employee;

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
import app.hub.api.EmployeeScheduleResponse;

public class DailyScheduleAdapter extends RecyclerView.Adapter<DailyScheduleAdapter.ViewHolder> {

    private List<EmployeeScheduleResponse.ScheduledTicket> tickets;
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onTicketClick(EmployeeScheduleResponse.ScheduledTicket ticket);
    }

    public DailyScheduleAdapter() {
        this.tickets = new ArrayList<>();
    }

    public void setTickets(List<EmployeeScheduleResponse.ScheduledTicket> tickets) {
        this.tickets = tickets != null ? tickets : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnTicketClickListener(OnTicketClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_schedule_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmployeeScheduleResponse.ScheduledTicket ticket = tickets.get(position);
        holder.bind(ticket);
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTitle, tvCustomer, tvServiceType, tvAddress, tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStatus = itemView.findViewById(R.id.tvStatus);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTicketClick(tickets.get(getAdapterPosition()));
                }
            });
        }

        void bind(EmployeeScheduleResponse.ScheduledTicket ticket) {
            tvTime.setText(ticket.getScheduledTime() != null ? ticket.getScheduledTime() : "Not set");
            tvTitle.setText(ticket.getTitle() != null ? ticket.getTitle() : "No title");
            tvCustomer.setText(ticket.getCustomerName() != null ? ticket.getCustomerName() : "Unknown");
            tvServiceType.setText(ticket.getServiceType() != null ? ticket.getServiceType() : "General Service");
            tvAddress.setText(ticket.getAddress() != null ? ticket.getAddress() : "No address");

            // Set status with color
            String status = ticket.getStatus() != null ? ticket.getStatus() : "Unknown";
            tvStatus.setText(status);

            // Color code by status
            switch (status.toLowerCase()) {
                case "in progress":
                    tvStatus.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3")));
                    break;
                case "completed":
                    tvStatus.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                    break;
                case "pending":
                    tvStatus.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800")));
                    break;
                default:
                    tvStatus.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#757575")));
                    break;
            }
        }
    }
}
