package app.hub.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.hub.R;
import app.hub.api.BranchReportsResponse;

public class BranchReportsAdapter extends RecyclerView.Adapter<BranchReportsAdapter.ViewHolder> {

    private List<BranchReportsResponse.BranchReport> branches;
    private OnBranchClickListener listener;

    public interface OnBranchClickListener {
        void onBranchClick(BranchReportsResponse.BranchReport branch);
    }

    public BranchReportsAdapter(List<BranchReportsResponse.BranchReport> branches, OnBranchClickListener listener) {
        this.branches = branches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_branch_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BranchReportsResponse.BranchReport branch = branches.get(position);
        holder.bind(branch, listener);
    }

    @Override
    public int getItemCount() {
        return branches.size();
    }

    public void updateData(List<BranchReportsResponse.BranchReport> newBranches) {
        this.branches = newBranches;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBranchName;
        private TextView tvBranchLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBranchName = itemView.findViewById(R.id.tvBranchName);
            tvBranchLocation = itemView.findViewById(R.id.tvBranchLocation);
        }

        public void bind(BranchReportsResponse.BranchReport branch, OnBranchClickListener listener) {
            tvBranchName.setText(branch.getName());
            tvBranchLocation.setText(branch.getLocation());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBranchClick(branch);
                }
            });
        }
    }
}
