package app.hub.manager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import app.hub.R;
import app.hub.api.EmployeeResponse;

public class TechnicianAdapter extends ArrayAdapter<EmployeeResponse.Employee> {
    private Context context;
    private List<EmployeeResponse.Employee> technicians;
    private List<EmployeeResponse.Employee> techniciansFull;

    public TechnicianAdapter(@NonNull Context context, @NonNull List<EmployeeResponse.Employee> technicians) {
        super(context, 0, technicians);
        this.context = context;
        this.technicians = technicians;
        this.techniciansFull = new ArrayList<>(technicians);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_technician_dropdown, parent, false);
        }

        EmployeeResponse.Employee technician = getItem(position);
        if (technician == null) {
            return convertView;
        }

        TextView tvName = convertView.findViewById(R.id.tvTechnicianName);
        TextView tvStatus = convertView.findViewById(R.id.tvTechnicianStatus);

        // Set technician name
        String name = getTechnicianName(technician);
        tvName.setText(name);

        // Set status based on ticket count
        int ticketCount = technician.getTicketCount();
        boolean isBusy = ticketCount > 0;
        
        if (isBusy) {
            // Busy technician - show as disabled
            tvStatus.setText("Busy (" + ticketCount + ")");
            tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            tvName.setAlpha(0.5f);
            tvStatus.setAlpha(0.5f);
        } else {
            // Available technician - show as enabled
            tvStatus.setText("Available");
            tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            tvName.setAlpha(1.0f);
            tvStatus.setAlpha(1.0f);
        }

        return convertView;
    }

    private String getTechnicianName(EmployeeResponse.Employee technician) {
        String name = (technician.getFirstName() != null ? technician.getFirstName() : "") +
                " " + (technician.getLastName() != null ? technician.getLastName() : "");
        if (name.trim().isEmpty()) {
            name = technician.getEmail() != null ? technician.getEmail() : "Unknown Technician";
        }
        return name.trim();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return technicianFilter;
    }

    private Filter technicianFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<EmployeeResponse.Employee> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                // Show all technicians
                suggestions.addAll(techniciansFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (EmployeeResponse.Employee technician : techniciansFull) {
                    String name = getTechnicianName(technician).toLowerCase();
                    if (name.contains(filterPattern)) {
                        suggestions.add(technician);
                    }
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results.values != null) {
                addAll((List<EmployeeResponse.Employee>) results.values);
            }
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return getTechnicianName((EmployeeResponse.Employee) resultValue);
        }
    };
}
