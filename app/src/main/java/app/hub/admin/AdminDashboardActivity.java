package app.hub.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import app.hub.R;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // as soon as the activity is created, we want to show the All Tickets fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new AdminAllTicketsFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.nav_all_tickets) {
            selectedFragment = new AdminAllTicketsFragment();
        } else if (item.getItemId() == R.id.nav_branches) {
            selectedFragment = new BranchesFragment();
        } else if (item.getItemId() == R.id.nav_assignments) {
            selectedFragment = new AssignmentsFragment();
        } else if (item.getItemId() == R.id.nav_reports) {
            selectedFragment = new ReportsFragment();
        } else if (item.getItemId() == R.id.nav_users) {
            selectedFragment = new UsersFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
        }

        return true;
    };
}
