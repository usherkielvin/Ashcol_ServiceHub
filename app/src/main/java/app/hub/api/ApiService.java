package app.hub.api;

import com.servicehub.model.ChatRequest;
import com.servicehub.model.ChatResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ApiService {
    @POST("api/v1/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/v1/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("api/v1/google-signin")
    Call<GoogleSignInResponse> googleSignIn(@Body GoogleSignInRequest request);

    @POST("api/v1/google-register")
    Call<GoogleSignInResponse> googleRegister(@Body GoogleSignInRequest request);

    @POST("api/v1/send-verification-code")
    Call<VerificationResponse> sendVerificationCode(@Body VerificationRequest request);

    @POST("api/v1/verify-email")
    Call<VerifyEmailResponse> verifyEmail(@Body VerifyEmailRequest request);

    @POST("api/v1/forgot-password")
    Call<VerificationResponse> forgotPassword(@Body VerificationRequest request);

    @POST("api/v1/logout")
    Call<LogoutResponse> logout(@Header("Authorization") String token);

    @POST("api/v1/change-password")
    Call<ChangePasswordResponse> changePassword(@Header("Authorization") String token, @Body ChangePasswordRequest request);

    @POST("api/v1/set-initial-password")
    Call<SetInitialPasswordResponse> setInitialPassword(@Header("Authorization") String token, @Body SetInitialPasswordRequest request);

    @POST("api/v1/reset-password")
    Call<ResetPasswordResponse> resetPassword(@Body ResetPasswordRequest request);

    @GET("api/v1/user")
    Call<UserResponse> getUser(@Header("Authorization") String token);

    @POST("api/v1/user/update")
    Call<UserResponse> updateUser(@Header("Authorization") String token, @Body UpdateProfileRequest request);

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

    @Multipart
    @POST("api/v1/profile/photo")
    Call<ProfilePhotoResponse> uploadProfilePhoto(
            @Header("Authorization") String token,
            @Part MultipartBody.Part photo
    );

    @DELETE("api/v1/profile/photo")
    Call<ProfilePhotoResponse> deleteProfilePhoto(
            @Header("Authorization") String token
    );

    @GET("api/v1/employees")
    Call<EmployeeResponse> getEmployees(@Header("Authorization") String token);

    @POST("api/v1/update-location")
    Call<UpdateLocationResponse> updateLocation(@Header("Authorization") String token, @Body UpdateLocationRequest request);

    @GET("api/v1/test")
    Call<TestResponse> test(@Header("Authorization") String token);

    @GET("api/v1/tickets")
    Call<TicketListResponse> getTickets(@Header("Authorization") String token);

    @GET("api/v1/tickets")
    Call<TicketListResponse> getTickets(@Header("Authorization") String token, @retrofit2.http.Query("status") String status);

    @GET("api/v1/tickets/{ticketId}")
    Call<TicketDetailResponse> getTicketDetail(@Header("Authorization") String token, @retrofit2.http.Path("ticketId") String ticketId);

    @GET("api/v1/manager/tickets")
    Call<TicketListResponse> getManagerTickets(@Header("Authorization") String token);

    @PUT("api/v1/tickets/{ticketId}/status")
    Call<UpdateTicketStatusResponse> updateTicketStatus(@Header("Authorization") String token, @retrofit2.http.Path("ticketId") String ticketId, @Body UpdateTicketStatusRequest request);

    @GET("api/v1/employee/tickets")
    Call<TicketListResponse> getEmployeeTickets(@Header("Authorization") String token);
        
    @GET("api/v1/employee/tickets")
    Call<TicketListResponse> getEmployeeTicketsByStatus(@Header("Authorization") String token, @retrofit2.http.Query("status") String status);
        
    @POST("api/v1/tickets/{ticketId}/accept")
    Call<TicketStatusResponse> acceptTicket(@Header("Authorization") String token, @retrofit2.http.Path("ticketId") String ticketId);

    @POST("api/v1/tickets/{ticketId}/reject")
    Call<TicketStatusResponse> rejectTicket(@Header("Authorization") String token, @retrofit2.http.Path("ticketId") String ticketId);
    
    @PUT("api/v1/tickets/{ticketId}/schedule")
    Call<SetScheduleResponse> setTicketSchedule(@Header("Authorization") String token, @retrofit2.http.Path("ticketId") String ticketId, @Body SetScheduleRequest request);
    
    @GET("api/v1/employee/schedule")
    Call<EmployeeScheduleResponse> getEmployeeSchedule(@Header("Authorization") String token);
    
    @POST("api/v1/tickets/{ticketId}/complete-work")
    Call<app.hub.api.CompleteWorkResponse> completeWorkWithPayment(@Header("Authorization") String token, @retrofit2.http.Path("ticketId") String ticketId, @Body app.hub.api.CompleteWorkRequest request);
    
    @GET("api/v1/manager/payments")
    Call<app.hub.api.PaymentHistoryResponse> getPaymentHistory(@Header("Authorization") String token);
    
    @POST("api/v1/payments/{paymentId}/submit")
    Call<app.hub.api.CompleteWorkResponse> submitPaymentToManager(@Header("Authorization") String token, @retrofit2.http.Path("paymentId") int paymentId);
    
    @POST("api/v1/payments/{paymentId}/complete")
    Call<app.hub.api.CompleteWorkResponse> completePayment(@Header("Authorization") String token, @retrofit2.http.Path("paymentId") int paymentId);
}
