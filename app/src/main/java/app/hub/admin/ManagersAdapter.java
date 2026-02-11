package app.hub.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.hub.R;

public class ManagersAdapter extends RecyclerView.Adapter<ManagersAdapter.ManagerViewHolder> {

    private List<ManagersActivity.Manager> managers;
    private OnManagerClickListener onManagerClickListener;
    private OnManagerActionListener onManagerActionListener;

    public interface OnManagerClickListener {
        void onManagerClick(ManagersActivity.Manager manager);
    }

    public interface OnManagerActionListener {
        void onEditManager(ManagersActivity.Manager manager);
        void onRemoveManager(ManagersActivity.Manager manager, int position);
    }

    public ManagersAdapter(List<ManagersActivity.Manager> managers, OnManagerClickListener listener) {
        this.managers = managers;
        this.onManagerClickListener = listener;
    }

    public void setOnManagerActionListener(OnManagerActionListener listener) {
        this.onManagerActionListener = listener;
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
        holder.bind(manager, position);
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
        private ImageButton btnManagerMenu;

        public ManagerViewHolder(@NonNull View itemView) {
            super(itemView);
            managerName = itemView.findViewById(R.id.managerName);
            managerBranch = itemView.findViewById(R.id.managerBranch);
            managerEmail = itemView.findViewById(R.id.managerEmail);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            btnManagerMenu = itemView.findViewById(R.id.btn_manager_menu);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onManagerClickListener != null) {
                    onManagerClickListener.onManagerClick(managers.get(position));
                }
            });
        }

        public void bind(ManagersActivity.Manager manager, int position) {
            managerName.setText(manager.getName());
            managerBranch.setText(manager.getBranch());
            managerEmail.setText(manager.getEmail());
            statusBadge.setText(manager.getStatus());

            // Setup menu button
            btnManagerMenu.setOnClickListener(v -> showPopupMenu(v, manager, position));
        }

        private void showPopupMenu(View view, ManagersActivity.Manager manager, int position) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.manager_options_menu);
            
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                
                if (itemId == R.id.action_edit_manager) {
                    if (onManagerActionListener != null) {
                        onManagerActionListener.onEditManager(manager);
                    }
                    return true;
                } else if (itemId == R.id.action_remove_manager) {
                    showRemoveConfirmation(view, manager, position);
                    return true;
                }
                return false;
            });
            
            popupMenu.show();
        }

        private void showRemoveConfirmation(View view, ManagersActivity.Manager manager, int position) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("Remove Manager")
                    .setMessage("Are you sure you want to remove " + manager.getName() + "?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        if (onManagerActionListener != null) {
                            onManagerActionListener.onRemoveManager(manager, position);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
}
