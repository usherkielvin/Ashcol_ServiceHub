package app.hub.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import app.hub.R;

public class GoogleSignInHelper {
    private static final String TAG = "GoogleSignInHelper";

    private GoogleSignInClient googleSignInClient;
    private final Context context;

    public GoogleSignInHelper(Context context) {
        this.context = context;
        setupGoogleSignIn();
    }

    private void setupGoogleSignIn() {
        String serverClientId = getClientIdFromResources();

        GoogleSignInOptions.Builder gsoBuilder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile();

        if (serverClientId != null && !serverClientId.isEmpty() && !serverClientId.contains("TODO")) {
            gsoBuilder.requestIdToken(serverClientId);
        } else {
            Log.w(TAG, "server_client_id is empty or invalid; ID token will be null. Check strings.xml");
        }

        GoogleSignInOptions gso = gsoBuilder.build();
        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    private String getClientIdFromResources() {
        int resId = context.getResources().getIdentifier(
                "default_web_client_id",
                "string",
                context.getPackageName());
        if (resId != 0) {
            return context.getString(resId);
        }
        resId = context.getResources().getIdentifier(
                "server_client_id",
                "string",
                context.getPackageName());
        if (resId != 0) {
            return context.getString(resId);
        }
        return "";
    }

    public Intent getSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void signOut(Runnable onComplete) {
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public GoogleSignInAccount handleSignInResult(Task<GoogleSignInAccount> completedTask) throws ApiException {
        return completedTask.getResult(ApiException.class);
    }

    public GoogleSignInClient getClient() {
        return googleSignInClient;
    }
}
