package app.hub.manager;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import app.hub.R;

public class AssignConfirmBottomSheet extends BottomSheetDialogFragment {
    
    private String ticketId;
    private String serviceName;
    private String technicianName;
    private String dateTime;
    private OnDoneClickListener listener;
    
    public interface OnDoneClickListener {
        void onDone();
    }
    
    public static AssignConfirmBottomSheet newInstance(String ticketId, String serviceName, 
                                                       String technicianName, String dateTime) {
        AssignConfirmBottomSheet fragment = new AssignConfirmBottomSheet();
        Bundle args = new Bundle();
        args.putString("ticket_id", ticketId);
        args.putString("service_name", serviceName);
        args.putString("technician_name", technicianName);
        args.putString("date_time", dateTime);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ticketId = getArguments().getString("ticket_id");
            serviceName = getArguments().getString("service_name");
            technicianName = getArguments().getString("technician_name");
            dateTime = getArguments().getString("date_time");
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_customer_requestconfirm, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        TextView tvTicketId = view.findViewById(R.id.tvTicketId);
        TextView tvServiceName = view.findViewById(R.id.tvServiceName);
        TextView tvTechnicianName = view.findViewById(R.id.tvTechnicianName);
        TextView tvDateTime = view.findViewById(R.id.tvDateTime);
        MaterialButton btnDone = view.findViewById(R.id.btnDone);
        
        if (tvTicketId != null) tvTicketId.setText(ticketId);
        if (tvServiceName != null) tvServiceName.setText(serviceName);
        if (tvTechnicianName != null) tvTechnicianName.setText(technicianName);
        if (tvDateTime != null) tvDateTime.setText(dateTime);
        
        if (btnDone != null) {
            btnDone.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDone();
                }
                dismiss();
            });
        }
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
    
    public void setOnDoneClickListener(OnDoneClickListener listener) {
        this.listener = listener;
    }
}
