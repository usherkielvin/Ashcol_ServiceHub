package app.hub.admin;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import app.hub.R;

public class AdminDashboardActivity extends AppCompatActivity {

    private View navIndicator;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_admin_dashboard);

            navIndicator = findViewById(R.id.navIndicator);
            bottomNav = findViewById(R.id.bottom_navigation);
            
            if (bottomNav != null) {
                bottomNav.setOnNavigationItemSelectedListener(navListener);
                disableNavigationTooltips(bottomNav);
            }

            // Default fragment
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AdminHomeFragment()).commit();
                
                // Set initial indicator position
                if (bottomNav != null) {
                    bottomNav.post(() -> moveIndicatorToItem(R.id.admin_home, false));
                }
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
                view.setOnLongClickListener(v -> true);
                TooltipCompat.setTooltipText(view, null);
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.admin_home) {
            selectedFragment = new AdminHomeFragment();
        }  else if (itemId == R.id.admin_operations) {
            selectedFragment = new AdminOperationsFragment();
        } else if (itemId == R.id.admin_reports) {
            selectedFragment = new AdminReportsFragment();
        } else if (itemId == R.id.admin_profile) {
            selectedFragment = new AdminProfileFragment();
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

    /**
     * Navigate to Operations tab and optionally show Manager or Branch tab
     * @param showManagerTab true to show Manager tab, false to show Branch tab
     */
    public void navigateToOperationsTab(boolean showManagerTab) {
        // Create the fragment with the tab selection
        AdminOperationsFragment fragment = AdminOperationsFragment.newInstance(showManagerTab);
        
        // Switch to operations tab in bottom navigation
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.admin_operations);
        }
        
        // Replace fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        
        // Move indicator
        moveIndicatorToItem(R.id.admin_operations, true);
    }
}
