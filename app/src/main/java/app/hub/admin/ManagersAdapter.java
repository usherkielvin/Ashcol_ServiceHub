package app.hub.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.hub.R;

public class ManagersAdapter extends RecyclerView.Adapter<ManagersAdapter.ManagerViewHolder> {

    private List<ManagersActivity.Manager> managers;
    private OnManagerClickListener onManagerClickListener;

    public interface OnManagerClickListener {
        void onManagerClick(ManagersActivity.Manager manager);
    }

    public ManagersAdapter(List<ManagersActivity.Manager> managers, OnManagerClickListener listener) {
        this.managers = managers;
        this.onManagerClickListener = listener;
    }

    @NonNull
    @Override
    public ManagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manager, parent, false);
        return new ManagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManagerViewHolder holder, int position) {
        ManagersActivity.Manager manager = managers.get(position);
        holder.bind(manager);
    }

    @Override
    public int getItemCount() {
        return managers.size();
    }

    class ManagerViewHolder extends RecyclerView.ViewHolder {
        private TextView managerName;
        private TextView managerBranch;
        private TextView managerEmail;
        private TextView statusBadge;

        public ManagerViewHolder(@NonNull View itemView) {
            super(itemView);
            managerName = itemView.findViewById(R.id.managerName);
            managerBranch = itemView.findViewById(R.id.managerBranch);
            managerEmail = itemView.findViewById(R.id.managerEmail);
            statusBadge = itemView.findViewById(R.id.statusBadge);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onManagerClickListener != null) {
                    onManagerClickListener.onManagerClick(managers.get(position));
                }
            });
        }

        public void bind(ManagersActivity.Manager manager) {
            managerName.setText(manager.getName());
            managerBranch.setText(manager.getBranch());
            managerEmail.setText(manager.getEmail());
            statusBadge.setText(manager.getStatus());
        }
    }
}