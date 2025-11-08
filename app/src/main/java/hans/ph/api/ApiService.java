package hans.ph.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/v1/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/v1/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("api/v1/logout")
    Call<LogoutResponse> logout(@Header("Authorization") String token);

    @GET("api/v1/user")
    Call<UserResponse> getUser(@Header("Authorization") String token);
}

