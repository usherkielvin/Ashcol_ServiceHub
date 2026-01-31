package app.hub.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import app.hub.R;

public class TicketConfirmationActivity extends AppCompatActivity {

    private TextView tvTicketId;
    private TextView tvStatus;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_confirmation);

        initViews();
        setupData();
        setupClickListeners();
    }

    private void initViews() {
        tvTicketId = findViewById(R.id.tvTicketId);
        tvStatus = findViewById(R.id.tvStatus);
        btnDone = findViewById(R.id.btnDone);
    }

    private void setupData() {
        // Get data from intent
        Intent intent = getIntent();
        String ticketId = intent.getStringExtra("ticket_id");
        String status = intent.getStringExtra("status");

        if (ticketId != null) {
            tvTicketId.setText(ticketId);
        }

        if (status != null) {
            tvStatus.setText("Status: " + status);
        } else {
            tvStatus.setText("Status: Pending");
        }
    }

    private void setupClickListeners() {
        btnDone.setOnClickListener(v -> {
            // Navigate back to main activity with user home fragment
            Intent intent = new Intent(this, app.hub.common.MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("navigate_to", "user_home"); // Signal to show user home fragment
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to ticket creation form
        Intent intent = new Intent(this, app.hub.common.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("navigate_to", "user_home"); // Signal to show user home fragment
        startActivity(intent);
        finish();
    }
}