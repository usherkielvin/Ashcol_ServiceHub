package app.hub.manager;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;

import java.io.File;

import app.hub.R;
import app.hub.common.MainActivity;
import app.hub.user.ChangePasswordFragment;
import app.hub.util.TokenManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.LogoutResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerProfileFragment extends Fragment {

    private TokenManager tokenManager;

    public ManagerProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = new TokenManager(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manager_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            logout();
        });

        MaterialButton passwordPrivacyButton = view.findViewById(R.id.btn_password_privacy);
        if (passwordPrivacyButton != null) {
            passwordPrivacyButton.setOnClickListener(v -> navigateToChangePassword());
        }
    }

    private void navigateToChangePassword() {
        if (getActivity() != null) {
            ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, changePasswordFragment)
                .addToBackStack(null)
                .commit();
        }
    }

    private void logout() {
        // Show progress indicator
        if (getActivity() == null) return;
        
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(getContext());
        progressDialog.setMessage("Logging out...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        String token = tokenManager.getToken();
        if (token != null) {
            ApiService apiService = ApiClient.getApiService();
            Call<LogoutResponse> call = apiService.logout(token);
            call.enqueue(new Callback<LogoutResponse>() {
                @Override
                public void onResponse(@NonNull Call<LogoutResponse> call, @NonNull Response<LogoutResponse> response) {
                    // Dismiss progress dialog
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    
                    // Perform cleanup operations asynchronously
                    performLogoutCleanup();
                }

                @Override
                public void onFailure(@NonNull Call<LogoutResponse> call, @NonNull Throwable t) {
                    // Dismiss progress dialog
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    
                    // Still perform cleanup even if API call fails
                    performLogoutCleanup();
                }
            });
        } else {
            // Dismiss progress dialog
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            
            // No token, just perform cleanup
            performLogoutCleanup();
        }
    }
    
    private void performLogoutCleanup() {
        // Run cleanup operations in background to prevent blocking UI
        new Thread(() -> {
            try {
                // Clear user data first (fast operation)
                clearUserData();
                
                // Sign out from Google (this can be slow)
                signOutFromGoogle();
                
                // Navigate to login on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::navigateToLogin);
                }
            } catch (Exception e) {
                Log.e("ManagerProfileFragment", "Error during logout cleanup: " + e.getMessage(), e);
                // Still navigate to login even if cleanup fails
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::navigateToLogin);
                }
            }
        }).start();
    }

    private void signOutFromGoogle() {
        try {
            if (getActivity() != null) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestProfile()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
                
                // Perform sign out with timeout
                java.util.concurrent.CompletableFuture<Void> signOutFuture = 
                    java.util.concurrent.CompletableFuture.runAsync(() -> {
                        try {
                            googleSignInClient.signOut().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("ManagerProfileFragment", "Google sign out successful");
                                } else {
                                    Log.w("ManagerProfileFragment", "Google sign out failed: " + task.getException());
                                }
                            }).addOnFailureListener(e -> {
                                Log.w("ManagerProfileFragment", "Google sign out error: " + e.getMessage());
                            });
                        } catch (Exception e) {
                            Log.w("ManagerProfileFragment", "Google sign out exception: " + e.getMessage());
                        }
                    });
                
                // Wait for sign out with timeout (don't block forever)
                try {
                    signOutFuture.get(3, java.util.concurrent.TimeUnit.SECONDS);
                } catch (java.util.concurrent.TimeoutException e) {
                    Log.w("ManagerProfileFragment", "Google sign out timed out");
                } catch (Exception e) {
                    Log.w("ManagerProfileFragment", "Google sign out interrupted: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e("ManagerProfileFragment", "Error during Google sign out: " + e.getMessage(), e);
        }
    }

    private void navigateToLogin() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void clearUserData() {
        // Clear token manager data
        tokenManager.clear();
        
        // Delete locally stored profile photo
        try {
            File imageFile = new File(requireContext().getFilesDir(), "profile_image.jpg");
            if (imageFile.exists()) {
                imageFile.delete();
            }
        } catch (Exception e) {
            // Ignore errors when clearing profile photo
        }
    }
}