package app.hub.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.hub.R;

public class UserAddressAdapter extends RecyclerView.Adapter<UserAddressAdapter.ViewHolder> {

    public static class AddressItem {
        public final String id;
        public final String name;
        public final String phone;
        public final String locationDetails;
        public final String postalCode;
        public final String streetDetails;
        public final boolean isDefault;

        public AddressItem(String id, String name, String phone, String locationDetails,
                String postalCode, String streetDetails, boolean isDefault) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.locationDetails = locationDetails;
            this.postalCode = postalCode;
            this.streetDetails = streetDetails;
            this.isDefault = isDefault;
        }

        public String getDisplayDetails() {
            StringBuilder builder = new StringBuilder();
            if (locationDetails != null && !locationDetails.isEmpty()) {
                builder.append(locationDetails);
            }
            if (streetDetails != null && !streetDetails.isEmpty()) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(streetDetails);
            }
            if (postalCode != null && !postalCode.isEmpty()) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(postalCode);
            }
            return builder.toString();
        }
    }

    public interface OnAddressActionListener {
        void onEdit(AddressItem item);
    }

    private final List<AddressItem> addressList;
    private OnAddressActionListener actionListener;

    public UserAddressAdapter(List<AddressItem> addressList) {
        this.addressList = addressList;
    }

    public void setOnAddressActionListener(OnAddressActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setItems(List<AddressItem> items) {
        addressList.clear();
        if (items != null) {
            addressList.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AddressItem item = addressList.get(position);
        holder.tvName.setText(item.name);
        holder.tvPhone.setText(item.phone);
        holder.tvAddressDetails.setText(item.getDisplayDetails());
        holder.btnDefaultBadge.setVisibility(item.isDefault ? View.VISIBLE : View.GONE);

        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEdit(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList != null ? addressList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvPhone;
        private final TextView tvAddressDetails;
        private final TextView btnEdit;
        private final View btnDefaultBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddressDetails = itemView.findViewById(R.id.tvAddressDetails);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDefaultBadge = itemView.findViewById(R.id.btnDefaultBadge);
        }
    }
}
