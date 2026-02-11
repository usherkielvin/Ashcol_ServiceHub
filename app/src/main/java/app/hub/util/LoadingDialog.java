package app.hub.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import app.hub.R;

/**
 * Utility class for showing a loading dialog with animated GIF.
 * Displays ac_loading.gif centered on a light grey background.
 */
public class LoadingDialog {
    
    private Dialog dialog;
    private Context context;
    
    public LoadingDialog(Context context) {
        this.context = context;
    }
    
    /**
     * Show the loading dialog.
     */
    public void show() {
        if (dialog != null && dialog.isShowing()) {
            return; // Already showing
        }
        
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        dialog.setContentView(view);
        
        // Make dialog fullscreen with light grey background
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
        
        // Load GIF using Glide
        ImageView loadingGif = view.findViewById(R.id.loadingGif);
        Glide.with(context)
                .asGif()
                .load(R.drawable.ac_loading)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(loadingGif);
        
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        
        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Dismiss the loading dialog.
     */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        dialog = null;
    }
    
    /**
     * Check if the dialog is currently showing.
     */
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}
