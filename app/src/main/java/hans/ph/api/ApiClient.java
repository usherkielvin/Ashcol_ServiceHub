package hans.ph.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Change this to your Laravel API URL
    // For local XAMPP: http://10.0.2.2:8000 (Android emulator)
    // For physical device: http://YOUR_COMPUTER_IP:8000
    private static final String BASE_URL = "http://10.0.2.2:8000/";
    
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}

