package app.hub.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.GoogleSignInRequest;
import app.hub.api.GoogleSignInResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateNewAccountFragment extends Fragment {
	
	private static final String TAG = "CreateNewAccountFragment";
	private static final int RC_SIGN_IN = 9001;
	private GoogleSignInClient googleSignInClient;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_user_new_acc, container, false);
		
		// Setup Google Sign-In
		setupGoogleSignIn();
		
		// Setup buttons
		setupButtons(view);
		
		return view;
	}
	
	private void setupGoogleSignIn() {
		// Configure Google Sign-In
		String serverClientId = getString(R.string.server_client_id);
		GoogleSignInOptions.Builder gsoBuilder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestEmail()
			.requestProfile();

		if (serverClientId != null && !serverClientId.trim().isEmpty()) {
			gsoBuilder.requestIdToken(serverClientId.trim());
		} else {
			Log.w(TAG, "server_client_id is empty; ID token will be null.");
		}

		GoogleSignInOptions gso = gsoBuilder.build();
		
		googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
	}
	
	private void setupButtons(View view) {
		// Back button
		ImageButton backButton = view.findViewById(R.id.closeButton);
		if (backButton != null) {
			backButton.setOnClickListener(v -> {
				if (getActivity() != null) {
					getActivity().finish();
				}
			});
		}
		
		// Continue with Email button
		Button continueWithEmailButton = view.findViewById(R.id.OpenOTP);
		if (continueWithEmailButton != null) {
			continueWithEmailButton.setOnClickListener(v -> {
				RegisterActivity activity = (RegisterActivity) getActivity();
				if (activity != null) {
					activity.showEmailFragment();
				}
			});
		}
		
		// Google Sign-In button
		Button googleButton = view.findViewById(R.id.btnGoogle);
		if (googleButton != null) {
			googleButton.setOnClickListener(v -> {
				signInWithGoogle();
			});
		}
	}
	
	private void signInWithGoogle() {
		// Clear any cached Google account to allow user to select different account
		googleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
			// After signing out, start the sign-in flow
			Intent signInIntent = googleSignInClient.getSignInIntent();
			startActivityForResult(signInIntent, RC_SIGN_IN);
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// Handle Google Sign-In result
		if (requestCode == RC_SIGN_IN) {
			Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
			handleGoogleSignInResult(task);
		}
	}
	
	private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
		try {
			GoogleSignInAccount account = completedTask.getResult(ApiException.class);
			if (account != null) {
				String email = account.getEmail();
				String displayName = account.getDisplayName();
				String givenName = account.getGivenName();
				String familyName = account.getFamilyName();
				String idToken = account.getIdToken();
				
				Log.d(TAG, "Google Sign-In successful - Email: " + email);
				
				// Send success to parent activity
				RegisterActivity activity = (RegisterActivity) getActivity();
				if (activity != null) {
					activity.handleGoogleSignInSuccess(email, givenName, familyName, displayName, idToken);
				}
			}
		} catch (ApiException e) {
			Log.e(TAG, "Google Sign-In failed: " + e.getStatusCode(), e);
			String errorMessage = "Google Sign-In failed";
			
			switch (e.getStatusCode()) {
				case 10: // DEVELOPER_ERROR
					errorMessage = "Google Sign-In not configured. Please contact support.";
					break;
				case 12501: // SIGN_IN_CANCELLED
					errorMessage = "Sign-in was cancelled";
					break;
				case 7: // NETWORK_ERROR
					errorMessage = "Network error. Please check your connection.";
					break;
				case 8: // INTERNAL_ERROR
					errorMessage = "Google Sign-In error. Please try again.";
					break;
				default:
					errorMessage = "Google Sign-In failed. Error code: " + e.getStatusCode();
					break;
			}
			showToast(errorMessage);
		}
	}
	
	private void showToast(String message) {
		if (getContext() != null) {
			Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
		}
	}
	
	private void clearSocialSignInStates() {
		// Clear Google sign-in cache
		if (googleSignInClient != null) {
			googleSignInClient.signOut();
		}
		
		// Clear any cached data in RegisterActivity
		RegisterActivity activity = (RegisterActivity) getActivity();
		if (activity != null) {
			activity.clearAllSignInStates();
		}
	}
}