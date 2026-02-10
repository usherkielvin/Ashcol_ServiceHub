package app.hub.util;

import android.app.Activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public final class GooglePlayServicesUtils {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private GooglePlayServicesUtils() {
    }

    public static boolean ensureAvailable(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);

        if (resultCode == ConnectionResult.SUCCESS) {
            return true;
        }

        if (apiAvailability.isUserResolvableError(resultCode)) {
            apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
        }

        return false;
    }
}
