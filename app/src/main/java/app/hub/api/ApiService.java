package app.hub.api;

import com.servicehub.model.ChatRequest;
import com.servicehub.model.ChatResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @POST("api/v1/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/v1/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("api/v1/send-verification-code")
    Call<VerificationResponse> sendVerificationCode(@Body VerificationRequest request);

    @POST("api/v1/verify-email")
    Call<VerifyEmailResponse> verifyEmail(@Body VerifyEmailRequest request);

    @POST("api/v1/logout")
    Call<LogoutResponse> logout(@Header("Authorization") String token);

    @POST("api/v1/change-password")
    Call<ChangePasswordResponse> changePassword(@Header("Authorization") String token, @Body ChangePasswordRequest request);

    @GET("api/v1/user")
    Call<UserResponse> getUser(@Header("Authorization") String token);

    @POST("api/v1/chatbot")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);

    @POST("api/v1/tickets")
    Call<CreateTicketResponse> createTicket(@Header("Authorization") String token, @Body CreateTicketRequest request);

    @Multipart
    @POST("api/v1/tickets")
    Call<CreateTicketResponse> createTicket(
            @Header("Authorization") String token,
            @Part("description") RequestBody description,
            @Part("address") RequestBody address,
            @Part("contact") RequestBody contact,
            @Part("service_type") RequestBody serviceType,
            @Part MultipartBody.Part image
    );
}
