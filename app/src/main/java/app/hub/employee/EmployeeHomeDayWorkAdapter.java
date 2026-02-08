package app.hub.employee;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.hub.R;

public class EmployeeHomeDayWorkAdapter extends RecyclerView.Adapter<EmployeeHomeDayWorkAdapter.ViewHolder> {

    // Placeholder data list
    private List<String> workList;

    public EmployeeHomeDayWorkAdapter(List<String> workList) {
        this.workList = workList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee_home_daywork, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data
    }

    @Override
    public int getItemCount() {
        return workList != null ? workList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
