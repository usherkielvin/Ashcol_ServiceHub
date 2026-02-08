package app.hub.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import app.hub.R;
import app.hub.api.PaymentHistoryResponse;

public class ManagerPaymentsAdapter extends RecyclerView.Adapter<ManagerPaymentsAdapter.PaymentViewHolder> {

    private final List<PaymentHistoryResponse.PaymentItem> payments;

    public ManagerPaymentsAdapter(List<PaymentHistoryResponse.PaymentItem> payments) {
        this.payments = payments;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manager_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        if (position < 0 || position >= payments.size()) {
            return;
        }
        holder.bind(payments.get(position));
    }

    @Override
    public int getItemCount() {
        return payments != null ? payments.size() : 0;
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTicketId;
        private final TextView tvStatus;
        private final TextView tvCustomer;
        private final TextView tvTechnician;
        private final TextView tvMethod;
        private final TextView tvAmount;
        private final TextView tvDate;

        PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicketId = itemView.findViewById(R.id.tvPaymentTicketId);
            tvStatus = itemView.findViewById(R.id.tvPaymentStatus);
            tvCustomer = itemView.findViewById(R.id.tvPaymentCustomer);
            tvTechnician = itemView.findViewById(R.id.tvPaymentTechnician);
            tvMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvAmount = itemView.findViewById(R.id.tvPaymentAmount);
            tvDate = itemView.findViewById(R.id.tvPaymentDate);
        }

        void bind(PaymentHistoryResponse.PaymentItem item) {
            if (item == null) {
                return;
            }

            String ticketId = item.getTicketId();
            tvTicketId.setText("Ticket: " + (ticketId != null ? ticketId : "-"));

            String status = item.getStatus();
            tvStatus.setText(status != null ? status : "-");

            String customer = item.getCustomerName();
            tvCustomer.setText("Customer: " + (customer != null ? customer : "-"));

            String technician = item.getTechnicianName();
            tvTechnician.setText("Technician: " + (technician != null ? technician : "-"));

            String method = item.getPaymentMethod();
            tvMethod.setText("Method: " + (method != null ? method : "-"));

            tvAmount.setText(String.format(Locale.getDefault(), "Php %,.2f", item.getAmount()));

            String date = pickDate(item);
            tvDate.setText("Date: " + (date != null ? date : "-"));
        }

        private String pickDate(PaymentHistoryResponse.PaymentItem item) {
            if (item.getCompletedAt() != null && !item.getCompletedAt().isEmpty()) {
                return item.getCompletedAt();
            }
            if (item.getSubmittedAt() != null && !item.getSubmittedAt().isEmpty()) {
                return item.getSubmittedAt();
            }
            if (item.getCollectedAt() != null && !item.getCollectedAt().isEmpty()) {
                return item.getCollectedAt();
            }
            return item.getCreatedAt();
        }
    }
}
