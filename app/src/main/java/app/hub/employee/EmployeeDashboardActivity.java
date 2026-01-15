package app.hub.employee;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import app.hub.R;

public class EmployeeDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        disableNavigationTooltips(bottomNav);

        // as soon as the activity is created, we want to show the Dashboard fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new EmployeeDashboardFragment()).commit();
    }

    private void disableNavigationTooltips(BottomNavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            View view = navigationView.findViewById(item.getItemId());
            if (view != null) {
                view.setOnLongClickListener(v -> true);
                TooltipCompat.setTooltipText(view, null);
            }
        }
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
