package app.hub.util;

import android.content.Context;
import android.util.Log;

import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.UpdateLocationRequest;
import app.hub.api.UpdateLocationResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserLocationManager {
    private static final String TAG = "UserLocationManager";
    private Context context;
    private TokenManager tokenManager;
    private LocationHelper locationHelper;

    public interface LocationUpdateCallback {
        void onLocationUpdated(String location);
        void onLocationUpdateFailed(String error);
    }

    public UserLocationManager(Context context) {
        this.context = context;
        this.tokenManager = new TokenManager(context);
        this.locationHelper = new LocationHelper(context);
    }

    public void updateUserLocation(LocationUpdateCallback callback) {
        String token = tokenManager.getToken();
        if (token == null) {
            if (callback != null) {
                callback.onLocationUpdateFailed("User not authenticated");
            }
            return;
        }

        // Get current location
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(String cityName) {
                Log.d(TAG, "Location received: " + cityName);
                
                // Update location on server
                updateLocationOnServer(token, cityName, callback);
            }

            @Override
            public void onLocationError(String error) {
                Log.e(TAG, "Location error: " + error);
                if (callback != null) {
                    callback.onLocationUpdateFailed("Could not get location: " + error);
                }
            }
        });
    }

    private void updateLocationOnServer(String token, String location, LocationUpdateCallback callback) {
        ApiService apiService = ApiClient.getApiService();
        UpdateLocationRequest request = new UpdateLocationRequest(location);
        Call<UpdateLocationResponse> call = apiService.updateLocation(token, request);

        call.enqueue(new Callback<UpdateLocationResponse>() {
            @Override
            public void onResponse(Call<UpdateLocationResponse> call, Response<UpdateLocationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UpdateLocationResponse locationResponse = response.body();
                    if (locationResponse.isSuccess()) {
                        Log.d(TAG, "Location updated successfully: " + location);
                        if (callback != null) {
                            callback.onLocationUpdated(location);
                        }
                    } else {
                        Log.e(TAG, "Location update failed: " + locationResponse.getMessage());
                        if (callback != null) {
                            callback.onLocationUpdateFailed("Server error: " + locationResponse.getMessage());
                        }
                    }
                } else {
                    Log.e(TAG, "Location update HTTP error: " + response.code());
                    if (callback != null) {
                        callback.onLocationUpdateFailed("Network error");
                    }
                }
            }

            @Override
            public void onFailure(Call<UpdateLocationResponse> call, Throwable t) {
                Log.e(TAG, "Location update network failure: " + t.getMessage());
                if (callback != null) {
                    callback.onLocationUpdateFailed("Network error: " + t.getMessage());
                }
            }
        });
    }

    public void cleanup() {
        if (locationHelper != null) {
            locationHelper.cleanup();
        }
    }
}