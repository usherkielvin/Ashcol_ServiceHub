package app.hub.manager;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import app.hub.R;

public class ManagerDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // as soon as the activity is created, we want to show the Dashboard fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new ManagerDashboardFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.nav_dashboard) {
            selectedFragment = new ManagerDashboardFragment();
        } else if (item.getItemId() == R.id.nav_assigned_tickets) {
            selectedFragment = new ManagerAssignedTicketsFragment();
        } else if (item.getItemId() == R.id.nav_in_progress) {
            selectedFragment = new ManagerInProgressFragment();
        } else if (item.getItemId() == R.id.nav_completed) {
            selectedFragment = new ManagerCompletedFragment();
        } else if (item.getItemId() == R.id.nav_settings) {
            selectedFragment = new ManagerSettingsFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
        }

        return true;
    };
}