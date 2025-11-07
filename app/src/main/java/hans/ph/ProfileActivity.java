package hans.ph;

import hans.ph.R;
import hans.ph.api.ApiClient;
import hans.ph.api.ApiService;
import hans.ph.api.UserResponse;
import hans.ph.util.TokenManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

	public static final String EXTRA_EMAIL = "email";
	private TokenManager tokenManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		tokenManager = new TokenManager(this);

		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
		}

		// Get email from intent or token manager
		String email = getIntent().getStringExtra(EXTRA_EMAIL);
		if (email == null) {
			email = tokenManager.getEmail();
		}

		TextView emailTextView = findViewById(R.id.emailTextView);
		if (emailTextView != null) {
			emailTextView.setText(email != null ? email : "Loading...");
		}

		// Fetch user data from API
		fetchUserData();

		MaterialButton logoutButton = findViewById(R.id.logoutButton);
		if (logoutButton != null) {
			logoutButton.setOnClickListener(v -> logout());
		}
	}

	private void fetchUserData() {
		String token = tokenManager.getToken();
		if (token == null) {
			return;
		}

		ApiService apiService = ApiClient.getApiService();
		Call<UserResponse> call = apiService.getUser(token);
		call.enqueue(new Callback<UserResponse>() {
			@Override
			public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
				if (response.isSuccessful() && response.body() != null) {
					UserResponse userResponse = response.body();
					if (userResponse.isSuccess() && userResponse.getData() != null) {
						TextView emailTextView = findViewById(R.id.emailTextView);
						if (emailTextView != null) {
							emailTextView.setText(userResponse.getData().getEmail());
						}
					}
				}
			}

			@Override
			public void onFailure(Call<UserResponse> call, Throwable t) {
				// Silently fail - use cached email
			}
		});
	}

	private void logout() {
		String token = tokenManager.getToken();
		if (token != null) {
			ApiService apiService = ApiClient.getApiService();
			Call<hans.ph.api.LogoutResponse> call = apiService.logout(token);
			call.enqueue(new Callback<hans.ph.api.LogoutResponse>() {
				@Override
				public void onResponse(Call<hans.ph.api.LogoutResponse> call, Response<hans.ph.api.LogoutResponse> response) {
					// Clear token regardless of API response
					tokenManager.clear();
					navigateToLogin();
				}

				@Override
				public void onFailure(Call<hans.ph.api.LogoutResponse> call, Throwable t) {
					// Clear token even if API call fails
					tokenManager.clear();
					navigateToLogin();
				}
			});
		} else {
			tokenManager.clear();
			navigateToLogin();
		}
	}

	private void navigateToLogin() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		finish();
	}
}
