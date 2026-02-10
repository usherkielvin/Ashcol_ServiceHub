package app.hub.manager;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;

import app.hub.R;

public class CancelConfirmDialog extends DialogFragment {
    
    private OnConfirmClickListener listener;
    
    public interface OnConfirmClickListener {
        void onConfirmYes();
        void onConfirmNo();
    }
    
    public static CancelConfirmDialog newInstance() {
        return new CancelConfirmDialog();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_customer_requestcancelconfirm, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        MaterialButton btnConfirmYes = view.findViewById(R.id.btnConfirmYes);
        MaterialButton btnConfirmNo = view.findViewById(R.id.btnConfirmNo);
        
        if (btnConfirmYes != null) {
            btnConfirmYes.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConfirmYes();
                }
                dismiss();
            });
        }
        
        if (btnConfirmNo != null) {
            btnConfirmNo.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConfirmNo();
                }
                dismiss();
            });
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
    
    public void setOnConfirmClickListener(OnConfirmClickListener listener) {
        this.listener = listener;
    }
}
