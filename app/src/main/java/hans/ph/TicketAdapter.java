package hans.ph;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Ticket> ticketList;

    public TicketAdapter(List<Ticket> ticketList) {
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);
        holder.ticketTitle.setText(ticket.getTitle());
        holder.ticketDescription.setText(ticket.getDescription());
        holder.ticketStatus.setText("Status: " + ticket.getStatus());
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView ticketTitle;
        TextView ticketDescription;
        TextView ticketStatus;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ticketTitle = itemView.findViewById(R.id.ticketTitle);
            ticketDescription = itemView.findViewById(R.id.ticketDescription);
            ticketStatus = itemView.findViewById(R.id.ticketStatus);
        }
    }
}
