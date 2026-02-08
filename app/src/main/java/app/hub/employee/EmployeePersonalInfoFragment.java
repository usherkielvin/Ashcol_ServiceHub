package app.hub.employee;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import app.hub.R;
import app.hub.BuildConfig;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.ProfilePhotoResponse;
import app.hub.api.UpdateProfileRequest;
import app.hub.api.UserResponse;
import app.hub.util.TokenManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeePersonalInfoFragment extends Fragment {

    private TokenManager tokenManager;
    private TextView tvEmail;
    private TextView tvRole;
    private TextView tvBranch;
    private TextInputEditText inputFirstName;
    private TextInputEditText inputLastName;
    private TextInputEditText inputPhone;
    private TextInputEditText inputLocation;
    private TextInputLayout layoutFirstName;
    private TextInputLayout layoutLastName;
    private MaterialButton btnSave;
    private ShapeableImageView imgProfile;
    private MaterialButton btnEditPhoto;
    private Uri cameraImageUri;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_personal_info, container, false);
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

        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvRole = view.findViewById(R.id.tvProfileRole);
        tvBranch = view.findViewById(R.id.tvProfileBranch);
        inputFirstName = view.findViewById(R.id.inputFirstName);
        inputLastName = view.findViewById(R.id.inputLastName);
        inputPhone = view.findViewById(R.id.inputPhone);
        inputLocation = view.findViewById(R.id.inputLocation);
        layoutFirstName = view.findViewById(R.id.layoutFirstName);
        layoutLastName = view.findViewById(R.id.layoutLastName);
        btnSave = view.findViewById(R.id.btnSaveProfile);
        imgProfile = view.findViewById(R.id.imgProfile);
        btnEditPhoto = view.findViewById(R.id.btnEditPhoto);

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> navigateBack());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfile());
        }

        if (btnEditPhoto != null) {
            btnEditPhoto.setOnClickListener(v -> showPhotoOptions());
        }

        loadCachedProfileImage();
        loadProfile();
    }

    private void initializeLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            uploadProfilePhoto(selectedImage);
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && cameraImageUri != null) {
                        uploadProfilePhoto(cameraImageUri);
                    }
                });
    }

    private void loadProfile() {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Authentication error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> call = apiService.getUser(token);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse.Data data = response.body().getData();
                    if (data != null) {
                        bindProfile(data);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Failed to load profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindProfile(UserResponse.Data data) {
        if (tvEmail != null) {
            tvEmail.setText(data.getEmail() != null ? data.getEmail() : "--");
        }
        if (tvRole != null) {
            tvRole.setText(data.getRole() != null ? data.getRole() : "--");
        }
        if (tvBranch != null) {
            String branch = data.getBranch() != null ? data.getBranch() : tokenManager.getCachedBranch();
            tvBranch.setText(branch != null ? branch : "--");
        }

        if (inputFirstName != null) {
            inputFirstName.setText(data.getFirstName() != null ? data.getFirstName() : "");
        }
        if (inputLastName != null) {
            inputLastName.setText(data.getLastName() != null ? data.getLastName() : "");
        }
        if (inputLocation != null) {
            inputLocation.setText(data.getLocation() != null ? data.getLocation() : "");
        }

        if (imgProfile != null && data.getProfilePhoto() != null && !data.getProfilePhoto().isEmpty()) {
            loadProfileImageFromUrl(data.getProfilePhoto());
        }
    }

    private void showPhotoOptions() {
        if (getContext() == null) return;

        String[] options = new String[] {"Take photo", "Choose from gallery", "Remove photo"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Profile photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else if (which == 1) {
                        openGallery();
                    } else {
                        showRemovePhotoConfirmation();
                    }
                })
                .show();
    }

    private void showRemovePhotoConfirmation() {
        if (getContext() == null) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Remove Photo")
                .setMessage("Are you sure you want to remove your profile photo?")
                .setPositiveButton("Remove", (dialog, which) -> deleteProfilePhoto())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void openCamera() {
        if (getContext() == null) return;

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireContext().getPackageManager()) == null) {
            Toast.makeText(requireContext(), "No camera app available.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File imageFile = File.createTempFile("profile_", ".jpg", requireContext().getCacheDir());
            cameraImageUri = FileProvider.getUriForFile(requireContext(),
                    BuildConfig.APPLICATION_ID + ".fileprovider", imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Unable to open camera.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfilePhoto(Uri imageUri) {
        if (getContext() == null) return;

        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Authentication error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        File tempFile = createTempFileFromUri(imageUri);
        if (tempFile == null) {
            Toast.makeText(requireContext(), "Unable to read image.", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), tempFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("photo", tempFile.getName(), requestBody);

        ApiService apiService = ApiClient.getApiService();
        Call<ProfilePhotoResponse> call = apiService.uploadProfilePhoto("Bearer " + token, body);
        call.enqueue(new Callback<ProfilePhotoResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfilePhotoResponse> call,
                    @NonNull Response<ProfilePhotoResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    if (imgProfile != null) {
                        imgProfile.setImageURI(imageUri);
                    }
                    saveProfileImage(imageUri);
                    Toast.makeText(requireContext(), "Profile photo updated.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to upload photo.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfilePhotoResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProfilePhoto() {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Authentication error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<ProfilePhotoResponse> call = apiService.deleteProfilePhoto("Bearer " + token);
        call.enqueue(new Callback<ProfilePhotoResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfilePhotoResponse> call,
                    @NonNull Response<ProfilePhotoResponse> response) {
                if (!isAdded()) return;
                if (imgProfile != null) {
                    imgProfile.setImageResource(R.mipmap.ic_launchericons_round);
                }
                clearCachedProfileImage();
                Toast.makeText(requireContext(), "Profile photo removed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<ProfilePhotoResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = File.createTempFile("profile_upload_", ".jpg", requireContext().getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (IOException e) {
            return null;
        }
    }

    private void loadProfileImageFromUrl(String url) {
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    if (bitmap != null && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (imgProfile != null) {
                                imgProfile.setImageBitmap(bitmap);
                            }
                        });
                    }
                    input.close();
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void saveProfileImage(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return;

            File imageFile = new File(requireContext().getFilesDir(), "profile_image.jpg");
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException ignored) {
        }
    }

    private void loadCachedProfileImage() {
        try {
            File imageFile = new File(requireContext().getFilesDir(), "profile_image.jpg");
            if (imageFile.exists() && imgProfile != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                if (bitmap != null) {
                    imgProfile.setImageBitmap(bitmap);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void clearCachedProfileImage() {
        try {
            File imageFile = new File(requireContext().getFilesDir(), "profile_image.jpg");
            if (imageFile.exists()) {
                imageFile.delete();
            }
        } catch (Exception ignored) {
        }
    }

    private void saveProfile() {
        if (layoutFirstName != null) layoutFirstName.setError(null);
        if (layoutLastName != null) layoutLastName.setError(null);

        String firstName = inputFirstName != null ? inputFirstName.getText().toString().trim() : "";
        String lastName = inputLastName != null ? inputLastName.getText().toString().trim() : "";
        String phone = inputPhone != null ? inputPhone.getText().toString().trim() : "";
        String location = inputLocation != null ? inputLocation.getText().toString().trim() : "";

        if (firstName.isEmpty()) {
            if (layoutFirstName != null) layoutFirstName.setError("First name is required");
            return;
        }

        if (lastName.isEmpty()) {
            if (layoutLastName != null) layoutLastName.setError("Last name is required");
            return;
        }

        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Authentication error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnSave != null) {
            btnSave.setEnabled(false);
            btnSave.setText("Saving...");
        }

        UpdateProfileRequest request = new UpdateProfileRequest(firstName, lastName, phone, location);
        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> call = apiService.updateUser(token, request);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (!isAdded()) return;
                restoreSaveButton();

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse.Data data = response.body().getData();
                    if (data != null) {
                        String fullName = (data.getFirstName() != null ? data.getFirstName() : "")
                                + " " + (data.getLastName() != null ? data.getLastName() : "");
                        tokenManager.saveName(fullName.trim());
                        Toast.makeText(requireContext(), "Profile updated.", Toast.LENGTH_SHORT).show();
                        bindProfile(data);
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                restoreSaveButton();
                Toast.makeText(requireContext(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restoreSaveButton() {
        if (btnSave != null) {
            btnSave.setEnabled(true);
            btnSave.setText("Save changes");
        }
    }

    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
}
