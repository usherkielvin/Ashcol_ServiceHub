package app.hub.admin;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import app.hub.R;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_admin_dashboard);

            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setOnNavigationItemSelectedListener(navListener);
                disableNavigationTooltips(bottomNav);
            }

            // as soon as the activity is created, we want to show the All Tickets fragment
            try {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AdminAllTicketsFragment()).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void disableNavigationTooltips(BottomNavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            View view = navigationView.findViewById(item.getItemId());
            if (view != null) {
                TooltipCompat.setTooltipText(view, null);
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        try {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_all_tickets) {
                selectedFragment = new AdminAllTicketsFragment();
            } else if (item.getItemId() == R.id.nav_branches) {
                selectedFragment = new AdminBranchesFragment();
            } else if (item.getItemId() == R.id.nav_assignments) {
                selectedFragment = new AssignmentsFragment();
            } else if (item.getItemId() == R.id.nav_reports) {
                selectedFragment = new AdminReportsFragment();
            } else if (item.getItemId() == R.id.nav_users) {
                selectedFragment = new UsersFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    };
}
