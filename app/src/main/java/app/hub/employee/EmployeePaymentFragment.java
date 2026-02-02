package app.hub.employee;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import app.hub.R;

/**
 * Full-width fragment version of the payment selection UI, so it behaves
 * like the rest of your app's fragments instead of a small dialog.
 */
public class EmployeePaymentFragment extends Fragment {

    public interface OnPaymentConfirmedListener {
        void onPaymentConfirmed(String paymentMethod, double amount, String notes);
    }

    private static final String ARG_TICKET_ID = "ticket_id";

    private String ticketId;
    private String selectedPaymentMethod = "cash";
    private double amount = 0.0;

    private MaterialButton btnCash;
    private MaterialButton btnOnline;
    private TextInputLayout tilAmount;
    private TextInputLayout tilNotes;
    private TextInputEditText etAmount;
    private TextInputEditText etNotes;
    private Button btnCancel;
    private Button btnComplete;

    public static EmployeePaymentFragment newInstance(String ticketId) {
        EmployeePaymentFragment fragment = new EmployeePaymentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TICKET_ID, ticketId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            ticketId = getArguments().getString(ARG_TICKET_ID);
        }
        return inflater.inflate(R.layout.dialog_payment_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupClickListeners();
        updatePaymentMethodButtons();
    }

    private void initViews(View view) {
        btnCash = view.findViewById(R.id.btnCash);
        btnOnline = view.findViewById(R.id.btnOnline);
        tilAmount = view.findViewById(R.id.tilAmount);
        tilNotes = view.findViewById(R.id.tilNotes);
        etAmount = view.findViewById(R.id.etAmount);
        etNotes = view.findViewById(R.id.etNotes);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnComplete = view.findViewById(R.id.btnComplete);
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

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

        btnCancel.setOnClickListener(v -> {
            // Simply pop this fragment off the back stack
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        btnComplete.setOnClickListener(v -> {
            if (!validateInputs()) {
                return;
            }

            String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

            if (getActivity() instanceof OnPaymentConfirmedListener) {
                ((OnPaymentConfirmedListener) getActivity())
                        .onPaymentConfirmed(selectedPaymentMethod, amount, notes);
            } else if (getParentFragment() instanceof OnPaymentConfirmedListener) {
                ((OnPaymentConfirmedListener) getParentFragment())
                        .onPaymentConfirmed(selectedPaymentMethod, amount, notes);
            } else {
                Toast.makeText(getContext(), "Payment callback not implemented", Toast.LENGTH_SHORT).show();
            }

            // Close the fragment after sending result
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }

    private void updatePaymentMethodButtons() {
        if (btnCash == null || btnOnline == null || getContext() == null) return;

        if ("cash".equals(selectedPaymentMethod)) {
            btnCash.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            btnCash.setTextColor(getResources().getColor(android.R.color.white));
            btnOnline.setBackgroundTintList(getResources().getColorStateList(R.color.light_gray));
            btnOnline.setTextColor(getResources().getColor(R.color.dark_gray));
        } else {
            btnOnline.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            btnOnline.setTextColor(getResources().getColor(android.R.color.white));
            btnCash.setBackgroundTintList(getResources().getColorStateList(R.color.light_gray));
            btnCash.setTextColor(getResources().getColor(R.color.dark_gray));
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

