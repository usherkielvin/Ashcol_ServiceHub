package app.hub.employee;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.view.animation.AccelerateDecelerateInterpolator;
import app.hub.R;

public class EmployeeDashboardActivity extends AppCompatActivity {

    private View navIndicator;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        navIndicator = findViewById(R.id.navIndicator);
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        disableNavigationTooltips(bottomNav);

        // as soon as the activity is created, we want to show the Dashboard fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new EmployeeDashboardFragment()).commit();
            
            // Set initial indicator position
            bottomNav.post(() -> moveIndicatorToItem(R.id.nav_home, false));
        }
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
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            selectedFragment = new EmployeeDashboardFragment();
        } else if (itemId == R.id.nav_work) {
            selectedFragment = new EmployeeWorkFragment();
        } else if (itemId == R.id.nav_sched) {
            selectedFragment = new EmployeeScheduleFragment();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new EmployeeProfileFragment();
        }

        if (selectedFragment != null) {
            moveIndicatorToItem(itemId, true);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
            return true;
        }

        return false;
    };

    private void moveIndicatorToItem(int itemId, boolean animate) {
        View itemView = bottomNav.findViewById(itemId);
        if (itemView == null || navIndicator == null) return;

        int itemWidth = itemView.getWidth();
        int indicatorWidth = navIndicator.getWidth();
        float targetX = itemView.getLeft() + (itemWidth / 2f) - (indicatorWidth / 2f);
        float targetY = 0f;

        if (animate) {
            navIndicator.animate()
                    .translationX(targetX)
                    .translationY(targetY)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        } else {
            navIndicator.setTranslationX(targetX);
            navIndicator.setTranslationY(targetY);
        }
    }
}
