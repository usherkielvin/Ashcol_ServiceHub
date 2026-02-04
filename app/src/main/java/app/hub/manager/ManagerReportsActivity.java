package app.hub.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import app.hub.R;
import app.hub.api.TicketListResponse;

public class ManagerReportsActivity extends AppCompatActivity {

    private TextView tvTotalTickets;
    private View rowPending, rowOngoing, rowCompleted, rowCancelled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_reports);

        initViews();
        loadStats();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvTotalTickets = findViewById(R.id.tvTotalTickets);
        rowPending = findViewById(R.id.rowPending);
        rowOngoing = findViewById(R.id.rowOngoing);
        rowCompleted = findViewById(R.id.rowCompleted);
        rowCancelled = findViewById(R.id.rowCancelled);

        setupRow(rowPending, "Pending", "#FFC107"); // Amber
        setupRow(rowOngoing, "Ongoing", "#2196F3"); // Blue
        setupRow(rowCompleted, "Completed", "#4CAF50"); // Green
        setupRow(rowCancelled, "Cancelled/Rejected", "#F44336"); // Red
    }

    private void setupRow(View row, String label, String colorHex) {
        TextView tvLabel = row.findViewById(R.id.tvStatusLabel);
        View indicator = row.findViewById(R.id.statusIndicator);
        tvLabel.setText(label);
        indicator.setBackgroundColor(android.graphics.Color.parseColor(colorHex));
    }

    private void loadStats() {
        List<TicketListResponse.TicketItem> tickets = ManagerDataManager.getCachedTickets();

        int total = tickets.size();
        int pending = 0;
        int ongoing = 0;
        int completed = 0;
        int cancelled = 0;

        for (TicketListResponse.TicketItem ticket : tickets) {
            String status = ticket.getStatus().toLowerCase();
            if (status.contains("pending") || status.contains("open")) {
                pending++;
            } else if (status.contains("progress") || status.contains("accepted") || status.contains("ongoing")) {
                ongoing++;
            } else if (status.contains("completed") || status.contains("resolved") || status.contains("closed")) {
                completed++;
            } else if (status.contains("cancelled") || status.contains("rejected") || status.contains("failed")) {
                cancelled++;
            }
        }

        tvTotalTickets.setText(String.valueOf(total));
        updateRowCount(rowPending, pending);
        updateRowCount(rowOngoing, ongoing);
        updateRowCount(rowCompleted, completed);
        updateRowCount(rowCancelled, cancelled);
    }

    private void updateRowCount(View row, int count) {
        TextView tvCount = row.findViewById(R.id.tvStatusCount);
        tvCount.setText(String.valueOf(count));
    }
}
