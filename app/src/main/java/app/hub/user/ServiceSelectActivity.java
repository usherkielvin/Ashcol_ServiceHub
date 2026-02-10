package app.hub.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.CreateTicketRequest;
import app.hub.api.CreateTicketResponse;
import app.hub.api.UserResponse;
import app.hub.api.TicketListResponse;
import app.hub.map.MapSelectionActivity;
import app.hub.util.TokenManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceSelectActivity extends AppCompatActivity {

    private static final String TAG = "ServiceSelectActivity";
    private static final SimpleDateFormat DATE_FORMAT_API = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat DATE_FORMAT_DISPLAY = new SimpleDateFormat("MMM dd, yyyy",
            Locale.getDefault());
    private static final int PICK_IMAGE_REQUEST = 1002;
    private static final int MAX_IMAGE_SIZE_MB = 5;

    private EditText fullNameInput, contactInput, landmarkInput, descriptionInput, dateInput;
    private Button submitButton;
    private RelativeLayout mapLocationButton;
    private HorizontalScrollView imageScrollView;
    private LinearLayout uploadButton, imagePreviewContainer;
    private RelativeLayout imagePreview1Container, imagePreview2Container;
    private ImageView imagePreview1, imagePreview2;
    private ImageButton btnRemoveImage1, btnRemoveImage2;
    private Spinner serviceTypeSpinner, unitTypeSpinner;
    private TextView locationHintText;
    private TokenManager tokenManager;
    private String selectedServiceType;
    private String selectedUnitType;
    private Long selectedDateMillis = null;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private String selectedAddress = "";
    private Uri selectedImageUri1 = null;
    private Uri selectedImageUri2 = null;
    private int currentImageSlot = 0; // 0 = none, 1 = first slot, 2 = second slot

    private final String[] serviceTypes = {"Cleaning", "Maintenance", "Repair", "Installation"};
    private final String[] unitTypes = {
         "Split","Window","ARF"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_service_request_form);

        // Initialize views
        fullNameInput = findViewById(R.id.etTitle);
        contactInput = findViewById(R.id.etContact);
        landmarkInput = findViewById(R.id.etLandmark);
        descriptionInput = findViewById(R.id.etDescription);
        dateInput = findViewById(R.id.etDate);
        submitButton = findViewById(R.id.btnSubmit);
        
        mapLocationButton = findViewById(R.id.btnMapLocation);
        uploadButton = findViewById(R.id.btnUpload);
        serviceTypeSpinner = findViewById(R.id.spinnerServiceType);
        unitTypeSpinner = findViewById(R.id.spinnerUnitType);
        
        imageScrollView = findViewById(R.id.imageScrollView);
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        imagePreview1Container = findViewById(R.id.imagePreview1Container);
        imagePreview2Container = findViewById(R.id.imagePreview2Container);
        imagePreview1 = findViewById(R.id.imagePreview1);
        imagePreview2 = findViewById(R.id.imagePreview2);
        btnRemoveImage1 = findViewById(R.id.btnRemoveImage1);
        btnRemoveImage2 = findViewById(R.id.btnRemoveImage2);
        
        locationHintText = findViewById(R.id.tvLocationHint);

        tokenManager = new TokenManager(this);

        String registeredName = getRegisteredName();
        if (fullNameInput != null && registeredName != null) {
            fullNameInput.setText(registeredName);
        }

        prefillContactFromProfile();

        // Get the selected service type from the intent
        selectedServiceType = getIntent().getStringExtra("serviceType");

        // Set up service type spinner
        setupServiceTypeSpinner();
        
        // Set up unit type spinner
        setupUnitTypeSpinner();

        // Set up back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Set up map location button
        if (mapLocationButton != null) {
            mapLocationButton.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(ServiceSelectActivity.this, MapSelectionActivity.class);
                    startActivityForResult(intent, 1001);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting MapSelectionActivity", e);
                    Toast.makeText(this, "Error opening map", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Set up date picker
        if (dateInput != null) {
            dateInput.setOnClickListener(v -> showDatePicker());
            dateInput.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) showDatePicker();
            });
        }

        // Set up image upload
        if (uploadButton != null) {
            uploadButton.setOnClickListener(v -> openImagePicker());
        }
        
        // Set up remove image buttons
        if (btnRemoveImage1 != null) {
            btnRemoveImage1.setOnClickListener(v -> removeImage(1));
        }
        if (btnRemoveImage2 != null) {
            btnRemoveImage2.setOnClickListener(v -> removeImage(2));
        }

        // Set up submit button
        if (submitButton != null) {
            submitButton.setOnClickListener(v -> showCheckingScreen());
        }
    }

    private void setupServiceTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, serviceTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceTypeSpinner.setAdapter(adapter);
        
        // Set the pre-selected service type from intent
        if (selectedServiceType != null) {
            for (int i = 0; i < serviceTypes.length; i++) {
                if (serviceTypes[i].equalsIgnoreCase(selectedServiceType)) {
                    serviceTypeSpinner.setSelection(i);
                    break;
                }
            }
        }
        
        serviceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedServiceType = serviceTypes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupUnitTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitTypeSpinner.setAdapter(adapter);
        
        unitTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedUnitType = null; // "Select Unit Type" option
                } else {
                    selectedUnitType = unitTypes[position];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUnitType = null;
            }
        });
    }

    private void prefillContactFromProfile() {
        if (contactInput == null || tokenManager == null) {
            return;
        }

        String currentValue = contactInput.getText() != null
                ? contactInput.getText().toString().trim()
                : "";
        if (!currentValue.isEmpty()) {
            return;
        }

        String token = tokenManager.getToken();
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;
        ApiService apiService = ApiClient.getApiService();
        Call<UserResponse> call = apiService.getUser(authToken);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (isFinishing()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse.Data data = response.body().getData();
                    if (data != null && data.getPhone() != null) {
                        String phone = data.getPhone().trim();
                        if (!phone.isEmpty() && contactInput != null) {
                            String current = contactInput.getText() != null
                                    ? contactInput.getText().toString().trim()
                                    : "";
                            if (current.isEmpty()) {
                                contactInput.setText(phone);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                // Ignore prefill failures.
            }
        });
    }

    private void showDatePicker() {
        long today = MaterialDatePicker.todayInUtcMilliseconds();
        long minDate = today;
        Calendar maxCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        maxCal.add(Calendar.YEAR, 2);
        long maxDate = maxCal.getTimeInMillis();

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select preferred service date");
        builder.setSelection(selectedDateMillis != null ? selectedDateMillis : today);
        builder.setCalendarConstraints(new CalendarConstraints.Builder()
                .setStart(minDate)
                .setEnd(maxDate)
                .build());

        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            selectedDateMillis = selection;
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            cal.setTimeInMillis(selection);
            String dateStr = DATE_FORMAT_DISPLAY.format(cal.getTime());
            if (dateInput != null)
                dateInput.setText(dateStr);
        });
        picker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void openImagePicker() {
        // Check if we already have 2 images
        if (selectedImageUri1 != null && selectedImageUri2 != null) {
            Toast.makeText(this, "Maximum 2 images allowed. Remove one to add another.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Determine which slot to fill
        currentImageSlot = (selectedImageUri1 == null) ? 1 : 2;
        
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }
    
    private void removeImage(int slot) {
        if (slot == 1) {
            selectedImageUri1 = null;
            imagePreview1Container.setVisibility(View.GONE);
            imagePreview1.setImageURI(null);
        } else if (slot == 2) {
            selectedImageUri2 = null;
            imagePreview2Container.setVisibility(View.GONE);
            imagePreview2.setImageURI(null);
        }
        
        // Hide container if no images
        if (selectedImageUri1 == null && selectedImageUri2 == null) {
            imageScrollView.setVisibility(View.GONE);
        }
        
        Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
    }
    
    private void updateImagePreviews() {
        // Show/hide preview container
        if (selectedImageUri1 != null || selectedImageUri2 != null) {
            imageScrollView.setVisibility(View.VISIBLE);
        } else {
            imageScrollView.setVisibility(View.GONE);
        }
        
        // Update image 1
        if (selectedImageUri1 != null) {
            imagePreview1Container.setVisibility(View.VISIBLE);
            imagePreview1.setImageURI(selectedImageUri1);
        } else {
            imagePreview1Container.setVisibility(View.GONE);
        }
        
        // Update image 2
        if (selectedImageUri2 != null) {
            imagePreview2Container.setVisibility(View.VISIBLE);
            imagePreview2.setImageURI(selectedImageUri2);
        } else {
            imagePreview2Container.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String address = data.getStringExtra("address");
            selectedLatitude = data.getDoubleExtra("latitude", 0.0);
            selectedLongitude = data.getDoubleExtra("longitude", 0.0);
            selectedAddress = address != null ? address : "";

            if (locationHintText != null && address != null) {
                locationHintText.setText(address);
                locationHintText.setTextColor(getResources().getColor(android.R.color.black));
                Log.d(TAG, "Location set: " + address);
                Log.d(TAG, "Coordinates - lat: " + selectedLatitude + ", lng: " + selectedLongitude);
            }
            Toast.makeText(this, "Location selected", Toast.LENGTH_SHORT).show();
        }
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedUri = data.getData();
            
            // Check file size
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedUri);
                if (inputStream != null) {
                    int fileSize = inputStream.available();
                    inputStream.close();
                    
                    int fileSizeMB = fileSize / (1024 * 1024);
                    if (fileSizeMB > MAX_IMAGE_SIZE_MB) {
                        Toast.makeText(this, "Image size must be less than " + MAX_IMAGE_SIZE_MB + "MB", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    // Assign to the appropriate slot
                    if (currentImageSlot == 1) {
                        selectedImageUri1 = selectedUri;
                    } else if (currentImageSlot == 2) {
                        selectedImageUri2 = selectedUri;
                    }
                    
                    updateImagePreviews();
                    
                    int imageCount = (selectedImageUri1 != null ? 1 : 0) + (selectedImageUri2 != null ? 1 : 0);
                    Toast.makeText(this, "Image " + imageCount + " added (" + fileSizeMB + "MB)", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking file size", e);
                Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showCheckingScreen() {
        // Validate inputs first
        String fullName = getRegisteredName();
        if (fullName == null) {
            fullName = fullNameInput != null ? fullNameInput.getText().toString().trim() : "";
        }
        String contact = contactInput != null ? contactInput.getText().toString().trim() : "";
        String description = descriptionInput != null ? descriptionInput.getText().toString().trim() : "";
        String date = dateInput != null ? dateInput.getText().toString().trim() : "";
        
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (contact.isEmpty()) {
            Toast.makeText(this, "Please enter your contact number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedAddress == null || selectedAddress.isEmpty()) {
            Toast.makeText(this, "Please select your address", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (description.isEmpty()) {
            Toast.makeText(this, "Please provide service details", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a preferred date", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Prepare data for checking screen
        String serviceType = selectedServiceType != null ? selectedServiceType : "Service";
        String specificService = description; // Use description as specific service
        String unitType = selectedUnitType != null ? selectedUnitType : "";
        String landmark = landmarkInput != null ? landmarkInput.getText().toString().trim() : "";
        
        // Show checking fragment
        SRFCheckingFragment fragment = SRFCheckingFragment.newInstance(
                fullName, contact, selectedAddress, landmark,
                serviceType, specificService, unitType, description,
                date, selectedImageUri1, selectedImageUri2
        );
        
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }
    
    public void confirmAndCreateTicket() {
        // This method is called from SRFCheckingFragment when user confirms
        // Pop the checking fragment from back stack
        getSupportFragmentManager().popBackStack();
        // Create the ticket
        createTicket();
    }

    private void createTicket() {
        String fullName = getRegisteredName();
        if (fullName == null) {
            fullName = fullNameInput != null ? fullNameInput.getText().toString().trim() : "";
        }
        String contact = contactInput != null ? contactInput.getText().toString().trim() : "";
        String landmark = landmarkInput != null ? landmarkInput.getText().toString().trim() : "";
        String description = descriptionInput != null ? descriptionInput.getText().toString().trim() : "";

        if (fullName.isEmpty() || contact.isEmpty() || selectedAddress.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedServiceType == null || selectedServiceType.isEmpty()) {
            selectedServiceType = "General Service";
        }

        // Don't combine address with landmark - keep address as is from map
        String fullAddress = selectedAddress;

        // Format preferred date for API (yyyy-MM-dd)
        String preferredDate = null;
        if (selectedDateMillis != null) {
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            cal.setTimeInMillis(selectedDateMillis);
            preferredDate = DATE_FORMAT_API.format(cal.getTime());
        }

        // Build full description with unit type and landmark
        String fullDescription = "";
        
        // Add unit type if selected
        if (selectedUnitType != null && !selectedUnitType.isEmpty()) {
            fullDescription = "Unit Type: " + selectedUnitType + "\n";
        }
        
        // Add landmark/additional location info if provided
        if (!landmark.isEmpty()) {
            fullDescription += "Landmark/Additional Info: " + landmark + "\n";
        }
        
        // Add main description
        if (!description.isEmpty()) {
            fullDescription += description;
        }
        
        // Clean up if only whitespace
        fullDescription = fullDescription.trim();
        if (fullDescription.isEmpty()) {
            fullDescription = "Service request";
        }

        ApiService apiService = ApiClient.getApiService();
        String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;

        Log.d(TAG, "Creating ticket with: Lat=" + selectedLatitude + ", Lng=" + selectedLongitude);

        submitButton.setEnabled(false);
        submitButton.setText("Creating...");

        // Check if we have any images to upload
        if (selectedImageUri1 != null || selectedImageUri2 != null) {
            createTicketWithImage(apiService, authToken, fullName, fullDescription, selectedServiceType, 
                    fullAddress, contact, preferredDate);
        } else {
            createTicketWithoutImage(apiService, authToken, fullName, fullDescription, selectedServiceType, 
                    fullAddress, contact, preferredDate);
        }
    }

    private void createTicketWithoutImage(ApiService apiService, String authToken, String fullName, 
            String fullDescription, String serviceType, String fullAddress, String contact, String preferredDate) {
        
        CreateTicketRequest request = new CreateTicketRequest(
                fullName, 
                fullDescription, 
                serviceType, 
                fullAddress, 
                contact,
                preferredDate, 
                selectedLatitude != 0.0 ? selectedLatitude : null,
                selectedLongitude != 0.0 ? selectedLongitude : null
        );

        Call<CreateTicketResponse> call = apiService.createTicket(authToken, request);
        call.enqueue(new Callback<CreateTicketResponse>() {
            @Override
            public void onResponse(Call<CreateTicketResponse> call, Response<CreateTicketResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pushTicketToFirestore(response.body());
                }
                handleTicketCreationResponse(response);
            }

            @Override
            public void onFailure(Call<CreateTicketResponse> call, Throwable t) {
                handleTicketCreationFailure(t);
            }
        });
    }

    private void createTicketWithImage(ApiService apiService, String authToken, String fullName, 
            String fullDescription, String serviceType, String fullAddress, String contact, String preferredDate) {
        
        try {
            // Use the first available image (prioritize image 1)
            Uri imageToUpload = selectedImageUri1 != null ? selectedImageUri1 : selectedImageUri2;
            
            if (imageToUpload == null) {
                // Fallback to no image
                createTicketWithoutImage(apiService, authToken, fullName, fullDescription, serviceType, fullAddress, contact, preferredDate);
                return;
            }
            
            // Get the file from URI
            InputStream inputStream = getContentResolver().openInputStream(imageToUpload);
            if (inputStream == null) {
                Toast.makeText(this, "Failed to read image file", Toast.LENGTH_SHORT).show();
                submitButton.setEnabled(true);
                submitButton.setText("Submit");
                return;
            }

            // Get the actual MIME type from the content resolver
            String mimeType = getContentResolver().getType(imageToUpload);
            if (mimeType == null) {
                mimeType = "image/jpeg"; // Default fallback
            }
            
            // Determine file extension
            String extension = ".jpg";
            if (mimeType.contains("png")) {
                extension = ".png";
            } else if (mimeType.contains("gif")) {
                extension = ".gif";
            }

            // Create a temporary file with proper extension
            File tempFile = new File(getCacheDir(), "upload_" + System.currentTimeMillis() + extension);
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.close();
            inputStream.close();

            Log.d(TAG, "Image prepared: " + tempFile.getName() + ", size: " + tempFile.length() + " bytes, type: " + mimeType);

            // Add note about second image if present
            String finalDescription = fullDescription;
            if (selectedImageUri1 != null && selectedImageUri2 != null) {
                finalDescription += "\n\nNote: 2 images attached (only first image uploaded due to API limitation)";
            }

            // Create RequestBody instances
            RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), fullName);
            RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), finalDescription);
            RequestBody addressBody = RequestBody.create(MediaType.parse("text/plain"), fullAddress);
            RequestBody contactBody = RequestBody.create(MediaType.parse("text/plain"), contact);
            RequestBody serviceTypeBody = RequestBody.create(MediaType.parse("text/plain"), serviceType);
            RequestBody unitTypeBody = RequestBody.create(MediaType.parse("text/plain"), 
                    selectedUnitType != null ? selectedUnitType : "");
            RequestBody preferredDateBody = RequestBody.create(MediaType.parse("text/plain"), 
                    preferredDate != null ? preferredDate : "");
            RequestBody latitudeBody = RequestBody.create(MediaType.parse("text/plain"), 
                    String.valueOf(selectedLatitude));
            RequestBody longitudeBody = RequestBody.create(MediaType.parse("text/plain"), 
                    String.valueOf(selectedLongitude));

            // Create image part with proper MIME type
            RequestBody imageBody = RequestBody.create(MediaType.parse(mimeType), tempFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", tempFile.getName(), imageBody);

            Log.d(TAG, "Sending ticket with image: title=" + fullName + ", service=" + serviceType);

            // Make the API call
            Call<CreateTicketResponse> call = apiService.createTicketWithImage(
                    authToken, titleBody, descriptionBody, addressBody, contactBody, 
                    serviceTypeBody, unitTypeBody, preferredDateBody, latitudeBody, longitudeBody, imagePart);
            
            final File finalTempFile = tempFile;
            call.enqueue(new Callback<CreateTicketResponse>() {
                @Override
                public void onResponse(Call<CreateTicketResponse> call, Response<CreateTicketResponse> response) {
                    // Delete temp file
                    if (finalTempFile.exists()) {
                        finalTempFile.delete();
                    }
                    if (response.isSuccessful() && response.body() != null) {
                        pushTicketToFirestore(response.body());
                    }
                    handleTicketCreationResponse(response);
                }

                @Override
                public void onFailure(Call<CreateTicketResponse> call, Throwable t) {
                    // Delete temp file
                    if (finalTempFile.exists()) {
                        finalTempFile.delete();
                    }
                    handleTicketCreationFailure(t);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error preparing image upload", e);
            Toast.makeText(this, "Error preparing image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            submitButton.setEnabled(true);
            submitButton.setText("Submit");
        }
    }

    private void pushTicketToFirestore(CreateTicketResponse ticketResponse) {
        if (ticketResponse == null) {
            return;
        }

        CreateTicketResponse.TicketData ticketData = ticketResponse.getTicket();
        if (ticketData == null) {
            return;
        }

        String ticketId = ticketData.getTicketId();
        String status = ticketResponse.getStatus();
        if (status == null && ticketData.getStatus() != null) {
            status = ticketData.getStatus().getName();
        }

        String branchName = null;
        if (ticketData.getBranch() != null) {
            branchName = ticketData.getBranch().getName();
        }

        if (ticketId == null || ticketId.trim().isEmpty() || branchName == null || branchName.trim().isEmpty()) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("ticketId", ticketId);
        payload.put("status", status != null ? status : "pending");
        payload.put("branch", branchName);
        payload.put("updatedAt", System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection("tickets")
                .document(ticketId)
                .set(payload);
    }

    private void handleTicketCreationResponse(Response<CreateTicketResponse> response) {
        submitButton.setEnabled(true);
        submitButton.setText("Submit");

        if (response.isSuccessful() && response.body() != null) {
            CreateTicketResponse ticketResponse = response.body();
            String ticketId = ticketResponse.getTicketId();
            String status = ticketResponse.getStatus();

            // Store ticket for instant display
            CreateTicketResponse.TicketData ticketData = ticketResponse.getTicket();
            if (ticketData != null) {
                TicketListResponse.TicketItem item = TicketListResponse.fromCreateResponse(
                        ticketData, status,
                        ticketData.getStatus() != null ? ticketData.getStatus().getColor() : null);
                if (item != null) {
                    UserTicketsFragment.setPendingNewTicket(item);
                }
            }

            Toast.makeText(ServiceSelectActivity.this, "Ticket created successfully!", Toast.LENGTH_SHORT).show();

            // Navigate to confirmation screen
            Intent intent = new Intent(ServiceSelectActivity.this, TicketConfirmationActivity.class);
            intent.putExtra("ticket_id", ticketId != null ? ticketId : "");
            intent.putExtra("status", status != null ? status : "Pending");
            startActivity(intent);
            finish();
        } else {
            String errorMsg = "Failed to create ticket";
            try {
                if (response.errorBody() != null) {
                    String errBody = response.errorBody().string();
                    Log.e(TAG, "Error response body: " + errBody);
                    
                    // Check if it's HTML (server error page)
                    if (errBody.contains("<!DOCTYPE") || errBody.contains("<html")) {
                        errorMsg = "Server error (HTTP " + response.code() + "). Please check your connection and try again.";
                    } else if (errBody.length() > 200) {
                        errorMsg = errBody.substring(0, 200) + "...";
                    } else {
                        errorMsg = errBody;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading error body", e);
            }
            Log.e(TAG, "Ticket creation failed with code: " + response.code());
            Toast.makeText(ServiceSelectActivity.this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void handleTicketCreationFailure(Throwable t) {
        submitButton.setEnabled(true);
        submitButton.setText("Submit");
        Log.e(TAG, "Ticket creation failed", t);
        Toast.makeText(ServiceSelectActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
    }

    private String getRegisteredName() {
        if (tokenManager == null) {
            return null;
        }
        String name = tokenManager.getName();
        return isValidName(name) ? name.trim() : null;
    }

    private boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && !"null".equalsIgnoreCase(name.trim());
    }
}
