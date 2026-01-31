package app.hub.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import app.hub.R;
import app.hub.api.EmployeeResponse;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    private List<EmployeeResponse.Employee> employees;

    public EmployeeAdapter(List<EmployeeResponse.Employee> employees) {
        this.employees = employees;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee_manager, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        EmployeeResponse.Employee employee = employees.get(position);
        
        // Set employee name using firstName and lastName
        String displayName = (employee.getFirstName() + " " + employee.getLastName()).trim();
        if (displayName.isEmpty()) {
            displayName = employee.getEmail(); // Fallback to email
        }
        holder.employeeName.setText(displayName);
        
        // Set department (role)
        holder.employeeDept.setText(employee.getRole() != null ? employee.getRole() : "Employee");
        
        // Set branch
        holder.employeeStatus.setText(employee.getBranch() != null ? employee.getBranch() : "No Branch");
        
        // Load profile photo
//        if (employee.getProfilePhoto() != null && !employee.getProfilePhoto().isEmpty()) {
//            Picasso.get()
//                    .load(employee.getProfilePhoto())
//                    .placeholder(R.drawable.profile_icon)
//                    .error(R.drawable.profile_icon)
//                    .into(holder.employeeImage);
//        } else {
//            holder.employeeImage.setImageResource(R.drawable.profile_icon);
//        }
    }

    @Override
    public int getItemCount() {
        return employees != null ? employees.size() : 0;
    }

    public void updateEmployees(List<EmployeeResponse.Employee> newEmployees) {
        this.employees = newEmployees;
        notifyDataSetChanged();
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView employeeImage;
        TextView employeeName;
        TextView employeeDept;
        TextView employeeStatus;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            employeeImage = itemView.findViewById(R.id.employeeImage);
            employeeName = itemView.findViewById(R.id.employeeName);
            employeeDept = itemView.findViewById(R.id.employeeDept);
            employeeStatus = itemView.findViewById(R.id.employeeStatus);
        }
    }
}