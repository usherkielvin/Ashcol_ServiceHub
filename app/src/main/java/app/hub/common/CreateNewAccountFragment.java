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

public class CreateNewAccountFragment extends Fragment {

    private static final String TAG = "CreateNewAccountFragment";
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient googleSignInClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_new_acc, container, false);
        
        setupGoogleSignIn();
        setupButtons(view);
        
        return view;
    }

    private void setupGoogleSignIn() {
        // Configure Google Sign-In
        // Note: To get ID token, you need to add .requestIdToken("YOUR_SERVER_CLIENT_ID")
        // For now, we'll use email-based registration without ID token verification
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            // .requestIdToken("YOUR_SERVER_CLIENT_ID") // Uncomment when you have server client ID
            .build();

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

        // Facebook button (optional - implement later)
        Button facebookButton = view.findViewById(R.id.btnFacebook);
        if (facebookButton != null) {
            facebookButton.setOnClickListener(v -> {
                showToast("Facebook login coming soon");
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
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                // Google Sign-In successful
                String email = account.getEmail();
                String displayName = account.getDisplayName();
                String givenName = account.getGivenName();
                String familyName = account.getFamilyName();
                String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;
                String idToken = account.getIdToken(); // May be null if not configured

                Log.d(TAG, "Google Sign-In successful");
                Log.d(TAG, "Email: " + email);
                Log.d(TAG, "Name: " + displayName);
                Log.d(TAG, "ID Token: " + (idToken != null ? "Present" : "Not available"));

                // Pass Google account data to RegisterActivity
                RegisterActivity activity = (RegisterActivity) getActivity();
                if (activity != null) {
                    activity.handleGoogleSignInSuccess(email, givenName, familyName, displayName, idToken);
                }
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google Sign-In failed: " + e.getStatusCode(), e);
            String errorMessage = "Google Sign-In failed";
            String detailedMessage = "";
            
            switch (e.getStatusCode()) {
                case 10: // DEVELOPER_ERROR
                    errorMessage = "Google Sign-In Configuration Required";
                    detailedMessage = "Google Sign-In needs to be configured in Google Cloud Console.\n\n" +
                        "To fix this:\n" +
                        "1. Get SHA-1 fingerprint: Run 'gradlew signingReport' in project root\n" +
                        "2. Go to: https://console.cloud.google.com/\n" +
                        "3. Create OAuth 2.0 Client ID for Android\n" +
                        "4. Package: app.hub\n" +
                        "5. Add your SHA-1 fingerprint\n\n" +
                        "See GOOGLE_SIGNIN_SETUP.md for details.";
                    showDetailedError(errorMessage, detailedMessage);
                    break;
                case 12501: // SIGN_IN_CANCELLED
                    errorMessage = "Sign-in was cancelled";
                    showToast(errorMessage);
                    break;
                case 7: // NETWORK_ERROR
                    errorMessage = "Network error. Please check your connection.";
                    showToast(errorMessage);
                    break;
                case 8: // INTERNAL_ERROR
                    errorMessage = "Google Sign-In internal error. Please try again.";
                    showToast(errorMessage);
                    break;
                default:
                    errorMessage = "Google Sign-In failed. Error code: " + e.getStatusCode();
                    showToast(errorMessage);
                    break;
            }
        }
    }

    private void showDetailedError(String title, String message) {
        if (getContext() != null) {
            new android.app.AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setNeutralButton("Use Email Instead", (dialog, which) -> {
                    // Navigate to email fragment as fallback
                    RegisterActivity activity = (RegisterActivity) getActivity();
                    if (activity != null) {
                        activity.showEmailFragment();
                    }
                })
                .show();
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
