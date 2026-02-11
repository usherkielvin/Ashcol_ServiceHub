package app.hub.manager;

import android.content.Context;
import android.graphics.Color;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;
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
        holder.tvEmployeeName.setText(fullName.trim().isEmpty() ? employee.getEmail() : fullName);

        // Set role
        holder.tvEmployeeRole.setText(employee.getRole() != null ? employee.getRole() : "Technician");

        // Set ticket count with label and color
        int ticketCount = employee.getTicketCount();
        holder.tvTicketCount.setText("Tickets:" + ticketCount);
        try {
            if (ticketCount == 0) {
                holder.tvTicketCount.getBackground().setTint(Color.parseColor("#4CAF50")); // Green for no tickets
            } else if (ticketCount == 1) {
                holder.tvTicketCount.getBackground().setTint(Color.parseColor("#4CAF50")); // Green for 1 ticket
            } else {
                holder.tvTicketCount.getBackground().setTint(Color.parseColor("#FF9800")); // Orange for more than 1
            }
        } catch (Exception e) {
            // Ignore tinting errors
        }

        // Load profile image with Picasso - enhanced with better error handling and caching
        if (holder.employeeImage != null) {
            String imageUrl = employee.getProfilePhoto();
            
            // Clear any previous image first
            holder.employeeImage.setImageResource(R.drawable.profile_icon);
            
            if (imageUrl != null && !imageUrl.isEmpty()) {
                android.util.Log.d("EmployeePreview", "Loading image for " + employee.getFirstName() + ": " + imageUrl);
                
                Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.profile_icon)
                    .error(R.drawable.profile_icon)
                    .fit()
                    .centerCrop()
                    .into(holder.employeeImage);
            } else {
                android.util.Log.d("EmployeePreview", "No profile photo for " + employee.getFirstName());
                holder.employeeImage.setImageResource(R.drawable.profile_icon);
            }
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
        ShapeableImageView employeeImage;
        TextView tvEmployeeName, tvEmployeeRole, tvTicketCount;

        ViewHolder(View itemView) {
            super(itemView);
            employeeImage = itemView.findViewById(R.id.employeeImage);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvEmployeeRole = itemView.findViewById(R.id.tvEmployeeRole);
            tvTicketCount = itemView.findViewById(R.id.tvTicketCount);
        }
    }
}
