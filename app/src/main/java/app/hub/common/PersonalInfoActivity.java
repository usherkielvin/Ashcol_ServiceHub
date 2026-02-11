package app.hub.common;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import app.hub.R;
import app.hub.employee.EmployeePersonalInfoFragment;

/**
 * Fullscreen activity wrapper for Personal Info fragment.
 * Overlays navigation bar and provides back button functionality.
 */
public class PersonalInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make activity fullscreen, overlaying navigation bar
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        
        setContentView(R.layout.activity_fullscreen_fragment);
        
        // Set up back button
        View backButton = findViewById(R.id.btnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        
        // Load the Personal Info fragment
        if (savedInstanceState == null) {
            Fragment fragment = new EmployeePersonalInfoFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
