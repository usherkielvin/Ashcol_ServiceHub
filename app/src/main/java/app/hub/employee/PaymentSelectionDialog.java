package app.hub.employee;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import app.hub.R;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class PaymentSelectionDialog extends Dialog {

    private String selectedPaymentMethod = "cash";
    private double amount = 0.0;
    private String notes = "";
    private OnPaymentSelectedListener listener;
    private boolean cashEnabled = true;

    private MaterialButton btnCash, btnOnline;
    private TextInputLayout tilAmount, tilNotes;
    private TextInputEditText etAmount, etNotes;
    private Button btnCancel, btnComplete;

    public interface OnPaymentSelectedListener {
        void onPaymentSelected(String paymentMethod, double amount, String notes);
    }

    public PaymentSelectionDialog(@NonNull Context context, OnPaymentSelectedListener listener) {
        super(context);
        this.listener = listener;
    }

    public PaymentSelectionDialog setDefaultPaymentMethod(String method) {
        if (method != null && ("cash".equalsIgnoreCase(method) || "online".equalsIgnoreCase(method))) {
            selectedPaymentMethod = method.toLowerCase();
        }
        return this;
    }

    public PaymentSelectionDialog setCashEnabled(boolean enabled) {
        cashEnabled = enabled;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_payment_selection);

        initViews();
        applyPaymentModeSettings();
        setupClickListeners();
        updatePaymentMethodButtons();
    }

    private void initViews() {
        btnCash = findViewById(R.id.btnCash);
        btnOnline = findViewById(R.id.btnOnline);
        tilAmount = findViewById(R.id.tilAmount);
        tilNotes = findViewById(R.id.tilNotes);
        etAmount = findViewById(R.id.etAmount);
        etNotes = findViewById(R.id.etNotes);
        btnCancel = findViewById(R.id.btnCancel);
        btnComplete = findViewById(R.id.btnComplete);

        // Set default selection
        selectedPaymentMethod = "cash";
    }

    private void setupClickListeners() {
        btnCash.setOnClickListener(v -> {
            selectedPaymentMethod = "cash";
            updatePaymentMethodButtons();
        });

        btnOnline.setOnClickListener(v -> {
            selectedPaymentMethod = "online";
            updatePaymentMethodButtons();
        });

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String amountStr = s.toString().trim();
                    if (!amountStr.isEmpty()) {
                        amount = Double.parseDouble(amountStr);
                    } else {
                        amount = 0.0;
                    }
                } catch (NumberFormatException e) {
                    amount = 0.0;
                }
                validateInputs();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        btnComplete.setOnClickListener(v -> {
            if (validateInputs()) {
                String notesText = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
                if (listener != null) {
                    listener.onPaymentSelected(selectedPaymentMethod, amount, notesText);
                }
                dismiss();
            }
        });
    }

    private void updatePaymentMethodButtons() {
        if (btnCash == null || btnOnline == null) return;

        if ("cash".equals(selectedPaymentMethod)) {
            btnCash.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.green));
            btnCash.setTextColor(getContext().getResources().getColor(android.R.color.white));
            btnOnline.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.light_gray));
            btnOnline.setTextColor(getContext().getResources().getColor(R.color.dark_gray));
        } else {
            btnOnline.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.green));
            btnOnline.setTextColor(getContext().getResources().getColor(android.R.color.white));
            btnCash.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.light_gray));
            btnCash.setTextColor(getContext().getResources().getColor(R.color.dark_gray));
        }
    }

    private void applyPaymentModeSettings() {
        if (!cashEnabled && btnCash != null) {
            btnCash.setVisibility(View.GONE);
        }
        if (!cashEnabled && "cash".equals(selectedPaymentMethod)) {
            selectedPaymentMethod = "online";
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (amount <= 0) {
            if (tilAmount != null) {
                tilAmount.setError("Please enter a valid amount");
            }
            isValid = false;
        } else {
            if (tilAmount != null) {
                tilAmount.setError(null);
            }
        }

        if (btnComplete != null) {
            btnComplete.setEnabled(isValid && amount > 0);
        }

        return isValid;
    }
}
