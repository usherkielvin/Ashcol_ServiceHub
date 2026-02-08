package app.hub.user;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager2.widget.ViewPager2;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Arrays;
import java.util.List;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.UserResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserHomeFragment extends Fragment {

    private static final String TAG = "UserHomeFragment";
    private ViewPager2 bannerViewPager;
    private BannerAdapter bannerAdapter;
    private LinearLayout dotsLayout;
    private Handler handler;
    private Runnable slideRunnable;
    private List<Integer> images = Arrays.asList(R.drawable.banner_cleaning, R.drawable.banner_installation, R.drawable.banner_maintainance, R.drawable.banner_repair);
    private TextView tvAssignedBranch;
    private TokenManager tokenManager;

    public UserHomeFragment() {
        // Required empty public constructor
    }

    public static UserHomeFragment newInstance(String param1, String param2) {
        UserHomeFragment fragment = new UserHomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user__home, container, false);

        tokenManager = new TokenManager(requireContext());
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        dotsLayout = view.findViewById(R.id.dotsLayout);
        tvAssignedBranch = view.findViewById(R.id.tvAssignedBranch);

        setupViewPager();
        updateDots(0); // Initialize dots
        loadBranchInfo();

        return view;
    }

    private void setupViewPager() {
        bannerAdapter = new BannerAdapter(images);
        bannerViewPager.setAdapter(bannerAdapter);

        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(position);
                restartAutoSlide();
            }
        });

        handler = new Handler(Looper.getMainLooper());
        slideRunnable = new Runnable() {
            @Override
            public void run() {
                int next = (bannerViewPager.getCurrentItem() + 1) % images.size();
                bannerViewPager.setCurrentItem(next);
                handler.postDelayed(this, 4000); // 4 seconds
            }
        };
        handler.postDelayed(slideRunnable, 4000);
    }

    private void updateDots(int position) {
        for (int i = 0; i < dotsLayout.getChildCount(); i++) {
            View dot = dotsLayout.getChildAt(i);
            if (i == position) {
                dot.setBackgroundResource(R.drawable.dot_selected);
                // Adjust width for selected dot if needed, but here it's handled by drawable or
                // fixed in XML
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
                params.width = (int) (24 * getResources().getDisplayMetrics().density);
                params.height = (int) (8 * getResources().getDisplayMetrics().density);
                dot.setLayoutParams(params);
            } else {
                dot.setBackgroundResource(R.drawable.dot_unselected);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
                params.width = (int) (8 * getResources().getDisplayMetrics().density);
                params.height = (int) (8 * getResources().getDisplayMetrics().density);
                dot.setLayoutParams(params);
            }
        }
    }

    private void restartAutoSlide() {
        if (handler != null && slideRunnable != null) {
            handler.removeCallbacks(slideRunnable);
            handler.postDelayed(slideRunnable, 4000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null && slideRunnable != null) {
            handler.removeCallbacks(slideRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (handler != null && slideRunnable != null) {
            handler.postDelayed(slideRunnable, 4000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && slideRunnable != null) {
            handler.removeCallbacks(slideRunnable);
        }
    }


    private void loadBranchInfo() {
        // First try to load from cache
        String cachedBranch = tokenManager.getCachedBranch();
        if (cachedBranch != null && !cachedBranch.isEmpty()) {
            displayBranch(cachedBranch);
        }

        // Then fetch fresh data from API
        fetchUserData();
    }

    private void fetchUserData() {
        String token = tokenManager.getToken();
        if (token == null) {
            Log.w(TAG, "No auth token available");
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> call = apiService.getUser(token);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    if (userResponse.isSuccess() && userResponse.getData() != null) {
                        UserResponse.Data userData = userResponse.getData();
                        String branch = userData.getBranch();

                        if (branch != null && !branch.isEmpty()) {
                            // Cache the branch info
                            tokenManager.saveBranchInfo(branch, 0);
                            displayBranch(branch);
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to fetch user data: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    private void displayBranch(String branch) {
        if (tvAssignedBranch != null && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (tvAssignedBranch != null) {
                    tvAssignedBranch.setText("Assigned to: " + branch);
                    tvAssignedBranch.setVisibility(View.VISIBLE);
                }
            });
        }
    }

}
