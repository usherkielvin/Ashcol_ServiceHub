package hans.ph;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class employee_DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // as soon as the activity is created, we want to show the Dashboard fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new EmployeeDashboardFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.nav_dashboard) {
            selectedFragment = new EmployeeDashboardFragment();
        } else if (item.getItemId() == R.id.nav_assigned_tickets) {
            selectedFragment = new EmployeeAssignedTicketsFragment();
        } else if (item.getItemId() == R.id.nav_in_progress) {
            selectedFragment = new InProgressFragment();
        } else if (item.getItemId() == R.id.nav_completed) {
            selectedFragment = new CompletedFragment();
        } else if (item.getItemId() == R.id.nav_settings) {
            selectedFragment = new EmployeeSettingsFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
        }

        return true;
    };
}
