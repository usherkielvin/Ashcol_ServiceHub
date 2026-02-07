package app.hub.manager;

import android.content.Context;
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
import app.hub.api.EmployeeResponse;

public class EmployeePreviewAdapter extends RecyclerView.Adapter<EmployeePreviewAdapter.ViewHolder> {

    private Context context;
    private List<EmployeeResponse.Employee> employees;

    public EmployeePreviewAdapter(Context context) {
        this.context = context;
        this.employees = new ArrayList<>();
    }

    public void setEmployees(List<EmployeeResponse.Employee> employees) {
        this.employees = employees != null ? employees : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_employee_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmployeeResponse.Employee employee = employees.get(position);

        // Set employee name
        String fullName = employee.getFirstName() + " " + employee.getLastName();
        holder.tvEmployeeName.setText(fullName);

        // Set role
        holder.tvEmployeeRole.setText(employee.getRole() != null ? employee.getRole() : "Technician");

        // Set ticket count
        int ticketCount = employee.getTicketCount();
        holder.tvTicketCount.setText(ticketCount + " ticket" + (ticketCount != 1 ? "s" : ""));

        // Set status indicator color based on online status
        // For now, we'll use a simple logic: show green if employee exists, gray if
        // inactive
        // You can enhance this with actual online/offline status from Firebase or API
        boolean isOnline = isEmployeeOnline(employee);

        if (isOnline) {
            // Green for online/active
            holder.statusIndicator.setBackgroundResource(R.drawable.shape_circle_gray);
            try {
                holder.statusIndicator.getBackground().setTint(Color.parseColor("#4CAF50"));
            } catch (Exception e) {
                holder.statusIndicator.setBackgroundColor(Color.parseColor("#4CAF50"));
            }
        } else {
            // Gray for offline
            holder.statusIndicator.setBackgroundResource(R.drawable.shape_circle_gray);
            try {
                holder.statusIndicator.getBackground().setTint(Color.parseColor("#9E9E9E"));
            } catch (Exception e) {
                holder.statusIndicator.setBackgroundColor(Color.parseColor("#9E9E9E"));
            }
        }

        // Set ticket count badge color
        try {
            if (ticketCount > 0) {
                holder.tvTicketCount.getBackground().setTint(Color.parseColor("#FF9800")); // Orange for active tickets
            } else {
                holder.tvTicketCount.getBackground().setTint(Color.parseColor("#4CAF50")); // Green for no tickets
            }
        } catch (Exception e) {
            // Ignore tinting errors
        }
    }

    /**
     * Determine if employee is online
     * This is a placeholder - you can enhance with real-time Firebase presence
     */
    private boolean isEmployeeOnline(EmployeeResponse.Employee employee) {
        // For now, randomly show some as online for demo purposes
        // In production, check Firebase presence or last_active timestamp
        return employee.getId() % 2 == 0; // Simple demo logic
    }

    @Override
    public int getItemCount() {
        return employees.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View statusIndicator;
        TextView tvEmployeeName, tvEmployeeRole, tvTicketCount;

        ViewHolder(View itemView) {
            super(itemView);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvEmployeeRole = itemView.findViewById(R.id.tvEmployeeRole);
            tvTicketCount = itemView.findViewById(R.id.tvTicketCount);
        }
    }
}
