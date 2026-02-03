package app.hub.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.imageview.ShapeableImageView;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.LogoutResponse;
import app.hub.api.ProfilePhotoResponse;
import app.hub.api.UserResponse;
import app.hub.common.MainActivity;
import app.hub.util.TokenManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProfileFragment extends Fragment {

    private TokenManager tokenManager;
    private String currentName;
    private String currentEmail;
    private String connectionStatus;
    private TextView tvName, tvUsername;
    private ShapeableImageView imgProfile;
    private Uri cameraImageUri;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeLaunchers();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());
        initializeViews(view);
        loadCachedData();
        loadProfileImage();
        fetchUserData();
        setupClickListeners(view);
    }

    private void initializeLaunchers() {
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    if (selectedImage != null) {
                        setProfileImage(selectedImage);
                    }
                }
            }
        );

        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && cameraImageUri != null) {
                    setProfileImage(cameraImageUri);
                }
            }
        );
    }

    private void initializeViews(View view) {
        tvName = view.findViewById(R.id.tv_name);
        tvUsername = view.findViewById(R.id.tv_username);
        imgProfile = view.findViewById(R.id.img_profile);
    }

    private void loadCachedData() {
        String cachedName = getCachedName();
        if (isValidName(cachedName)) {
            currentName = cachedName;
            if (tvName != null) {
                tvName.setText(cachedName);
            }
        }
        
        String cachedEmail = getCachedEmail();
        String cachedConnectionStatus = getCachedConnectionStatus();
        
        if (cachedConnectionStatus != null && !cachedConnectionStatus.isEmpty()) {
            currentEmail = cachedConnectionStatus;
            if (tvUsername != null) {
                tvUsername.setText(cachedConnectionStatus);
            }
        } else if (isValidEmail(cachedEmail)) {
            currentEmail = cachedEmail;
            if (tvUsername != null) {
                tvUsername.setText(cachedEmail);
            }
        }
    }

    private void fetchUserData() {
        String token = tokenManager.getToken();
        if (token == null) {
            fallbackToCachedData();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> call = apiService.getUser(token);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    if (userResponse.isSuccess() && userResponse.getData() != null) {
                        UserResponse.Data userData = userResponse.getData();
                        processUserData(userData);
                    } else {
                        fallbackToCachedData();
                    }
                } else {
                    fallbackToCachedData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                fallbackToCachedData();
            }
        });
    }

    private void processUserData(UserResponse.Data userData) {
        currentName = buildNameFromApi(userData);
        currentEmail = getEmailToDisplay(userData);
        
        connectionStatus = null;
        
        if (userData.getProfilePhoto() != null && !userData.getProfilePhoto().isEmpty()) {
            loadProfileImageFromUrl(userData.getProfilePhoto());
        } else {
            if (imgProfile != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (imgProfile != null) {
                        imgProfile.setImageResource(R.mipmap.ic_launchericons_round);
                    }
                });
            }
            try {
                File imageFile = new File(requireContext().getFilesDir(), "profile_image.jpg");
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            } catch (Exception e) {
            }
        }
        
        updateUI();
        updateCache(connectionStatus);
    }

    private String buildNameFromApi(UserResponse.Data userData) {
        String apiName = userData.getName();
        if (isValidName(apiName)) {
            return apiName.trim();
        }
        
        String firstName = userData.getFirstName();
        String lastName = userData.getLastName();
        
        if (isValidName(firstName) || isValidName(lastName)) {
            StringBuilder builder = new StringBuilder();
            if (isValidName(firstName)) {
                builder.append(firstName.trim());
            }
            if (isValidName(lastName)) {
                if (builder.length() > 0) {
                    builder.append(" ");
                }
                builder.append(lastName.trim());
            }
            if (builder.length() > 0) {
                return builder.toString();
            }
        }
        
        return null;
    }

    private String getEmailToDisplay(UserResponse.Data userData) {
        String cachedEmail = getCachedEmail();
        if (isValidEmail(cachedEmail)) {
            return cachedEmail;
        }

        String apiEmail = userData.getEmail();
        String apiUsername = userData.getUsername();
        
        if (isValidApiEmail(apiEmail, apiUsername)) {
            return apiEmail.trim();
        }
        
        return cachedEmail;
    }

    private boolean isValidApiEmail(String email, String username) {
        return email != null 
            && !email.trim().isEmpty() 
            && email.contains("@") 
            && !email.equals(username);
    }

    private void updateCache(String connectionStatus) {
        if (isValidName(currentName)) {
            tokenManager.saveName(currentName);
        }
        
        if (isValidEmail(currentEmail) && !currentEmail.equals(getCachedEmail())) {
            tokenManager.saveEmail(currentEmail);
        }
        
        if (connectionStatus != null && !connectionStatus.isEmpty()) {
            saveConnectionStatus(connectionStatus);
        } else {
            clearConnectionStatus();
        }
    }

    private void updateUI() {
        if (getActivity() == null || getView() == null) return;
        
        getActivity().runOnUiThread(() -> {
            updateNameDisplay();
            updateEmailDisplay();
        });
    }

    private void updateNameDisplay() {
        String displayName = isValidName(currentName) ? currentName : getCachedName();
        if (isValidName(displayName) && tvName != null) {
            tvName.setText(displayName.trim());
        }
    }

    private void updateEmailDisplay() {
        String displayEmail = currentEmail;
        
        if (connectionStatus != null && !connectionStatus.isEmpty()) {
            if (tvUsername != null) {
                tvUsername.setText(connectionStatus);
            }
            return;
        }
        
        if (!isValidEmail(displayEmail)) {
            displayEmail = getCachedEmail();
        }
        
        if (isValidEmail(displayEmail) && tvUsername != null) {
            tvUsername.setText(displayEmail);
        }
    }

    private void fallbackToCachedData() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            String cachedName = getCachedName();
            if (isValidName(cachedName) && tvName != null) {
                tvName.setText(cachedName);
                currentName = cachedName;
            }
            
            String cachedEmail = getCachedEmail();
            if (isValidEmail(cachedEmail) && tvUsername != null) {
                tvUsername.setText(cachedEmail);
                currentEmail = cachedEmail;
            }
        });
    }

    private boolean isValidName(String name) {
        return name != null 
            && !name.trim().isEmpty() 
            && !name.trim().equals("null") 
            && !name.trim().contains("null");
    }

    private boolean isValidEmail(String email) {
        return email != null 
            && email.contains("@") 
            && email.trim().length() > 3;
    }

    private String getCachedName() {
        try {
            String name = tokenManager.getName();
            return isValidName(name) ? name.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getCachedEmail() {
        try {
            String email = tokenManager.getEmail();
            return isValidEmail(email) ? email.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getCachedConnectionStatus() {
        try {
            return tokenManager.getConnectionStatus();
        } catch (Exception e) {
            return null;
        }
    }

    private void saveConnectionStatus(String status) {
        try {
            tokenManager.saveConnectionStatus(status);
        } catch (Exception e) {
        }
    }

    private void clearConnectionStatus() {
        try {
            tokenManager.clearConnectionStatus();
        } catch (Exception e) {
        }
    }

    private void setupClickListeners(View view) {
        setClickListener(view, R.id.btn_sign_out, () -> logout());
        setClickListener(view, R.id.btn_personal_info, () -> 
            showToast("Personal Information clicked"));
        setClickListener(view, R.id.btn_password_privacy, () -> 
            showToast("Password & Privacy clicked"));
        setClickListener(view, R.id.btn_help, () -> 
            showToast("Help & Feedback clicked"));
        setClickListener(view, R.id.btn_edit_photo, () -> showImagePickerDialog());
        setClickListener(view, R.id.btn_appearance, () -> 
            showToast("Appearance clicked"));
        setClickListener(view, R.id.btn_notifications, () -> 
            showNotificationSettings());
        setClickListener(view, R.id.btn_language, () -> 
            showToast("Language clicked"));
        setClickListener(view, R.id.btn_payroll, () -> 
            showToast("Payroll clicked"));
    }

    private void setClickListener(View view, int id, Runnable action) {
        View button = view.findViewById(id);
        if (button != null) {
            button.setOnClickListener(v -> action.run());
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showNotificationSettings() {
        if (getContext() == null) return;
        
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.user_notificationstoggler, null);
        
        com.google.android.material.switchmaterial.SwitchMaterial switchPush = view.findViewById(R.id.switch_push);
        com.google.android.material.switchmaterial.SwitchMaterial switchEmail = view.findViewById(R.id.switch_email);
        com.google.android.material.switchmaterial.SwitchMaterial switchSms = view.findViewById(R.id.switch_sms);
        
        if (switchPush != null) {
            switchPush.setChecked(tokenManager.isPushEnabled());
            switchPush.setOnCheckedChangeListener((buttonView, isChecked) -> 
                tokenManager.setPushEnabled(isChecked));
        }
        
        if (switchEmail != null) {
            switchEmail.setChecked(tokenManager.isEmailNotifEnabled());
            switchEmail.setOnCheckedChangeListener((buttonView, isChecked) -> 
                tokenManager.setEmailNotifEnabled(isChecked));
        }
        
        if (switchSms != null) {
            switchSms.setChecked(tokenManager.isSmsNotifEnabled());
            switchSms.setOnCheckedChangeListener((buttonView, isChecked) -> 
                tokenManager.setSmsNotifEnabled(isChecked));
        }
        
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
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
                
                // Navigate to login on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::navigateToLogin);
                }
            } catch (Exception e) {
                Log.e("AdminProfileFragment", "Error during logout cleanup: " + e.getMessage(), e);
                // Still navigate to login even if cleanup fails
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::navigateToLogin);
                }
            }
        }).start();
    }

    private void clearUserData() {
        try {
            // Clear token manager data
            if (tokenManager != null) {
                tokenManager.clear();
            }
            
            // Delete locally stored profile photo
            if (requireContext() != null) {
                File imageFile = new File(requireContext().getFilesDir(), "profile_image.jpg");
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            }
        } catch (Exception e) {
            Log.w("AdminProfileFragment", "Error clearing user data: " + e.getMessage());
            // Continue with logout even if clearing data fails
        }
    }

    private void navigateToLogin() {
        if (getActivity() == null) return;
        
        try {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        } catch (Exception e) {
            Log.e("AdminProfileFragment", "Error navigating to login: " + e.getMessage(), e);
            // If navigation fails, at least clear the activity stack
            getActivity().finish();
        }
    }

    private void showImagePickerDialog() {
        if (getContext() == null) return;

        String[] options = {"Camera", "Gallery", "Remove Photo", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Profile Photo");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else if (which == 1) {
                openGallery();
            } else if (which == 2) {
                showRemovePhotoConfirmation();
            }
        });
        builder.show();
    }

    private void showRemovePhotoConfirmation() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Remove Photo");
        builder.setMessage("Are you sure you want to remove your profile photo?");
        builder.setPositiveButton("Remove", (dialog, which) -> {
            deleteProfilePhoto();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void openCamera() {
        try {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = createImageFile();
            if (photoFile != null) {
                String authority = requireContext().getPackageName() + ".fileprovider";
                cameraImageUri = FileProvider.getUriForFile(requireContext(), authority, photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cameraLauncher.launch(cameraIntent);
            }
        } catch (Exception e) {
            showToast("Error opening camera: " + e.getMessage());
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        galleryLauncher.launch(galleryIntent);
    }

    private File createImageFile() throws IOException {
        String imageFileName = "profile_" + System.currentTimeMillis();
        File storageDir = requireContext().getFilesDir();
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        return imageFile;
    }

    private void setProfileImage(Uri imageUri) {
        try {
            Bitmap bitmap = getBitmapFromUri(imageUri);
            if (bitmap != null && imgProfile != null) {
                imgProfile.setImageBitmap(bitmap);
                saveProfileImage(bitmap);
                uploadProfilePhotoToServer(imageUri);
            }
        } catch (Exception e) {
            showToast("Error loading image: " + e.getMessage());
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }
            return resizeBitmap(bitmap, 500, 500);
        } catch (Exception e) {
            return null;
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if (bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private void saveProfileImage(Bitmap bitmap) {
        try {
            File imageFile = new File(requireContext().getFilesDir(), "profile_image.jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
        }
    }

    private void loadProfileImage() {
        try {
            File imageFile = new File(requireContext().getFilesDir(), "profile_image.jpg");
            if (imageFile.exists() && imgProfile != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                if (bitmap != null) {
                    imgProfile.setImageBitmap(bitmap);
                }
            }
        } catch (Exception e) {
        }
    }

    private void uploadProfilePhotoToServer(Uri imageUri) {
        String token = tokenManager.getToken();
        if (token == null) {
            showToast("Not authenticated. Please login again.");
            return;
        }

        try {
            File imageFile = createFileFromUri(imageUri);
            if (imageFile == null || !imageFile.exists()) {
                showToast("Error: Could not access image file");
                return;
            }

            RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/*"),
                imageFile
            );

            MultipartBody.Part photoPart = MultipartBody.Part.createFormData(
                "photo",
                imageFile.getName(),
                requestFile
            );

            ApiService apiService = ApiClient.getApiService();
            Call<ProfilePhotoResponse> call = apiService.uploadProfilePhoto("Bearer " + token, photoPart);
            call.enqueue(new Callback<ProfilePhotoResponse>() {
                @Override
                public void onResponse(@NonNull Call<ProfilePhotoResponse> call, @NonNull Response<ProfilePhotoResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        showToast("Profile photo saved");
                        fetchUserData();
                    } else {
                        showToast("Failed to upload photo. Please try again.");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ProfilePhotoResponse> call, @NonNull Throwable t) {
                    showToast("Failed to upload photo: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            showToast("Error uploading photo: " + e.getMessage());
        }
    }

    private File createFileFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = new File(requireContext().getFilesDir(), "temp_upload_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    private void loadProfileImageFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty() || imgProfile == null) {
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                if (bitmap != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (imgProfile != null) {
                            imgProfile.setImageBitmap(bitmap);
                            saveProfileImage(bitmap);
                        }
                    });
                }
            } catch (Exception e) {
                loadProfileImage();
            }
        }).start();
    }

    private void deleteProfilePhoto() {
        String token = tokenManager.getToken();
        if (token == null) {
            showToast("Not authenticated. Please login again.");
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<ProfilePhotoResponse> call = apiService.deleteProfilePhoto("Bearer " + token);
        call.enqueue(new Callback<ProfilePhotoResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfilePhotoResponse> call, @NonNull Response<ProfilePhotoResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showToast("Profile photo removed");
                    try {
                        File imageFile = new File(requireContext().getFilesDir(), "profile_image.jpg");
                        if (imageFile.exists()) {
                            imageFile.delete();
                        }
                    } catch (Exception e) {
                    }
                    if (imgProfile != null && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (imgProfile != null) {
                                imgProfile.setImageResource(R.mipmap.ic_launchericons_round);
                            }
                        });
                    }
                    fetchUserData();
                } else {
                    showToast("Failed to remove photo. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfilePhotoResponse> call, @NonNull Throwable t) {
                showToast("Failed to remove photo: " + t.getMessage());
            }
        });
    }
}
