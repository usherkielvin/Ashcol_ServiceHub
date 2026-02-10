package app.hub.manager;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import app.hub.R;

public class CancelReasonBottomSheet extends BottomSheetDialogFragment {
    
    private String ticketId;
    private String customerName;
    private String service;
    private String schedule;
    private OnCancelConfirmListener listener;
    
    private RadioGroup rgCancellationReason;
    private RadioButton rbCustomerCancel, rbInvalidDetails, rbServiceNotAvailable, rbOther;
    private TextInputLayout layoutOtherReason;
    private TextInputEditText etOtherReason;
    
    public interface OnCancelConfirmListener {
        void onCancelConfirmed(String reason);
        void onBack();
    }
    
    public static CancelReasonBottomSheet newInstance(String ticketId, String customerName, 
                                                      String service, String schedule) {
        CancelReasonBottomSheet fragment = new CancelReasonBottomSheet();
        Bundle args = new Bundle();
        args.putString("ticket_id", ticketId);
        args.putString("customer_name", customerName);
        args.putString("service", service);
        args.putString("schedule", schedule);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ticketId = getArguments().getString("ticket_id");
            customerName = getArguments().getString("customer_name");
            service = getArguments().getString("service");
            schedule = getArguments().getString("schedule");
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_customer_requestcancel, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        TextView tvTicketId = view.findViewById(R.id.tvTicketId);
        TextView tvCustomerName = view.findViewById(R.id.tvCustomerName);
        TextView tvService = view.findViewById(R.id.tvService);
        TextView tvSchedule = view.findViewById(R.id.tvSchedule);
        
        rgCancellationReason = view.findViewById(R.id.rgCancellationReason);
        rbCustomerCancel = view.findViewById(R.id.rbCustomerCancel);
        rbInvalidDetails = view.findViewById(R.id.rbInvalidDetails);
        rbServiceNotAvailable = view.findViewById(R.id.rbServiceNotAvailable);
        rbOther = view.findViewById(R.id.rbOther);
        layoutOtherReason = view.findViewById(R.id.layoutOtherReason);
        etOtherReason = view.findViewById(R.id.etOtherReason);
        
        MaterialButton btnConfirmCancel = view.findViewById(R.id.btnConfirmCancel);
        MaterialButton btnBack = view.findViewById(R.id.btnBack);
        
        if (tvTicketId != null) tvTicketId.setText(ticketId);
        if (tvCustomerName != null) tvCustomerName.setText(customerName);
        if (tvService != null) tvService.setText(service);
        if (tvSchedule != null) tvSchedule.setText(schedule);
        
        // Initially hide the other reason field
        if (layoutOtherReason != null) {
            layoutOtherReason.setVisibility(View.GONE);
        }
        
        // Show/hide other reason field based on radio button selection
        if (rgCancellationReason != null) {
            rgCancellationReason.setOnCheckedChangeListener((group, checkedId) -> {
                if (layoutOtherReason != null) {
                    if (checkedId == R.id.rbOther) {
                        layoutOtherReason.setVisibility(View.VISIBLE);
                    } else {
                        layoutOtherReason.setVisibility(View.GONE);
                    }
                }
            });
        }
        
        if (btnConfirmCancel != null) {
            btnConfirmCancel.setOnClickListener(v -> {
                String reason = getSelectedReason();
                if (reason == null || reason.isEmpty()) {
                    Toast.makeText(getContext(), "Please select a cancellation reason", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (listener != null) {
                    listener.onCancelConfirmed(reason);
                }
                dismiss();
            });
        }
        
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBack();
                }
                dismiss();
            });
        }
    }
    
    private String getSelectedReason() {
        if (rgCancellationReason == null) return null;
        
        int selectedId = rgCancellationReason.getCheckedRadioButtonId();
        
        if (selectedId == R.id.rbCustomerCancel) {
            return "Customer Cancellation";
        } else if (selectedId == R.id.rbInvalidDetails) {
            return "Invalid Request Details";
        } else if (selectedId == R.id.rbServiceNotAvailable) {
            return "Service Not Available";
        } else if (selectedId == R.id.rbOther) {
            if (etOtherReason != null && etOtherReason.getText() != null) {
                String otherReason = etOtherReason.getText().toString().trim();
                if (otherReason.isEmpty()) {
                    Toast.makeText(getContext(), "Please state the reason for cancellation", Toast.LENGTH_SHORT).show();
                    return null;
                }
                return "Other: " + otherReason;
            }
        }
        
        return null;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), getTheme());
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            com.google.android.material.bottomsheet.BottomSheetBehavior<?> bottomSheetBehavior = d.getBehavior();
            bottomSheetBehavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            bottomSheetBehavior.setSkipCollapsed(true);
            bottomSheetBehavior.setPeekHeight(0);
        });
        return dialog;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                com.google.android.material.bottomsheet.BottomSheetBehavior<?> behavior = 
                    com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
                behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setPeekHeight(0);
            }
        }
    }
    
    public void setOnCancelConfirmListener(OnCancelConfirmListener listener) {
        this.listener = listener;
    }
}
