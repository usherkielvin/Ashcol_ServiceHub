package app.hub.util;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Helper class to enable edge-to-edge display on Android devices.
 * Handles notches, cutouts, and system bars for immersive full-screen experience.
 */
public class EdgeToEdgeHelper {

    /**
     * Enable edge-to-edge display for the given activity.
     * Call this in onCreate() before setContentView().
     */
    public static void enable(Activity activity) {
        Window window = activity.getWindow();
        
        // Enable edge-to-edge display - content draws behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false);
        
        // Make status bar and navigation bar fully transparent
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        
        // Set system bar appearance (light or dark icons)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }
        
        // For Android P (API 28) and above, handle display cutouts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.layoutInDisplayCutoutMode = 
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(layoutParams);
        }
    }

    /**
     * Apply window insets to a view to handle system bars and cutouts.
     * Call this after setContentView() with your root view.
     * This version only applies padding to bottom (navigation bar) to keep content accessible.
     */
    public static void applyInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() | 
                WindowInsetsCompat.Type.displayCutout()
            );
            
            // Only apply bottom padding to avoid navigation bar overlap
            // Content draws behind status bar for true fullscreen
            v.setPadding(insets.left, 0, insets.right, insets.bottom);
            
            return windowInsets;
        });
    }
    
    /**
     * Apply full insets if you need padding on all sides.
     * Use this for screens where content shouldn't go behind status bar.
     */
    public static void applyInsetsWithStatusBar(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() | 
                WindowInsetsCompat.Type.displayCutout()
            );
            
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            
            return windowInsets;
        });
    }

    /**
     * Apply window insets with custom handling for specific edges.
     * Useful when you want to draw behind some system bars but not others.
     */
    public static void applyInsetsSelective(View view, boolean applyTop, boolean applyBottom, 
                                           boolean applyLeft, boolean applyRight) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() | 
                WindowInsetsCompat.Type.displayCutout()
            );
            
            v.setPadding(
                applyLeft ? insets.left : 0,
                applyTop ? insets.top : 0,
                applyRight ? insets.right : 0,
                applyBottom ? insets.bottom : 0
            );
            
            return windowInsets;
        });
    }
    
    /**
     * Get the status bar height for manual positioning.
     */
    public static int getStatusBarHeight(Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
