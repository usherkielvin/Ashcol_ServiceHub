package app.hub.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.CreateTicketResponse;
import app.hub.util.TokenManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceSelectActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private TextInputEditText descriptionInput, addressInput, contactInput;
    private Button createTicketButton, uploadImageButton;
    private ImageView imagePreview;
    private TextView serviceTypeHeader;
    private TokenManager tokenManager;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_select);

        descriptionInput = findViewById(R.id.descriptionInput);
        addressInput = findViewById(R.id.addressInput);
        contactInput = findViewById(R.id.contactInput);
        createTicketButton = findViewById(R.id.createTicketButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        imagePreview = findViewById(R.id.imagePreview);
        serviceTypeHeader = findViewById(R.id.serviceTypeHeader);
        tokenManager = new TokenManager(this);

        // Get the selected service type from the intent
        String serviceType = getIntent().getStringExtra("SERVICE_TYPE");
        serviceTypeHeader.setText("Service Type: " + serviceType);

        uploadImageButton.setOnClickListener(v -> selectImage());
        createTicketButton.setOnClickListener(v -> createTicket());
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
            } else if (options[item].equals("Choose from Gallery")) {
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        } else {
            if (requestCode == CAMERA_PERMISSION_CODE) {
                openCamera();
            } else if (requestCode == STORAGE_PERMISSION_CODE) {
                openGallery();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                imagePreview.setImageBitmap(bitmap);
                imageUri = getImageUri(bitmap);
                imagePreview.setVisibility(View.VISIBLE);
            } else if (requestCode == REQUEST_GALLERY) {
                imageUri = data.getData();
                imagePreview.setImageURI(imageUri);
                imagePreview.setVisibility(View.VISIBLE);
            }
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void createTicket() {
        String description = descriptionInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String contact = contactInput.getText().toString().trim();
        String serviceType = getIntent().getStringExtra("SERVICE_TYPE");

        if (description.isEmpty() || address.isEmpty() || contact.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        MultipartBody.Part imagePart = null;
        if (imageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), byteArray);
                imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody addressBody = RequestBody.create(MediaType.parse("text/plain"), address);
        RequestBody contactBody = RequestBody.create(MediaType.parse("text/plain"), contact);
        RequestBody serviceTypeBody = RequestBody.create(MediaType.parse("text/plain"), serviceType);

        ApiService apiService = ApiClient.getApiService();
        String token = tokenManager.getToken();

        if (token == null) {
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<CreateTicketResponse> call = apiService.createTicket(token, descriptionBody, addressBody, contactBody, serviceTypeBody, imagePart);
        call.enqueue(new Callback<CreateTicketResponse>() {
            @Override
            public void onResponse(Call<CreateTicketResponse> call, Response<CreateTicketResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ServiceSelectActivity.this, "Ticket created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ServiceSelectActivity.this, "Failed to create ticket", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CreateTicketResponse> call, Throwable t) {
                Toast.makeText(ServiceSelectActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
