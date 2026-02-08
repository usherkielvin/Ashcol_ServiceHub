package app.hub.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import app.hub.R;

public class UserPaymentActivity extends AppCompatActivity {
    public static final String EXTRA_TICKET_ID = "ticket_id";
    public static final String EXTRA_PAYMENT_ID = "payment_id";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_SERVICE_NAME = "service_name";
    public static final String EXTRA_TECHNICIAN_NAME = "technician_name";

    public static Intent createIntent(Context context, String ticketId, int paymentId, double amount,
            String serviceName, String technicianName) {
        Intent intent = new Intent(context, UserPaymentActivity.class);
        intent.putExtra(EXTRA_TICKET_ID, ticketId);
        intent.putExtra(EXTRA_PAYMENT_ID, paymentId);
        intent.putExtra(EXTRA_AMOUNT, amount);
        intent.putExtra(EXTRA_SERVICE_NAME, serviceName);
        intent.putExtra(EXTRA_TECHNICIAN_NAME, technicianName);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_payment);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            String ticketId = intent.getStringExtra(EXTRA_TICKET_ID);
            int paymentId = intent.getIntExtra(EXTRA_PAYMENT_ID, 0);
            double amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0);
            String serviceName = intent.getStringExtra(EXTRA_SERVICE_NAME);
            String technicianName = intent.getStringExtra(EXTRA_TECHNICIAN_NAME);

            UserPaymentFragment fragment = UserPaymentFragment.newInstance(
                    ticketId,
                    paymentId,
                    amount,
                    serviceName,
                    technicianName);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, fragment)
                    .commit();
        }
    }
}
