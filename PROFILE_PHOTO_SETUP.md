# Profile Photo Feature Setup

## Backend Setup (Completed)

### Database
- ✅ Added `profile_photo` column to `users` table
- ✅ Column type: `string` (nullable) - stores file path
- ✅ Photos stored in: `storage/app/public/profile_photos/`

### API Endpoints

All endpoints require authentication (`Bearer token`)

#### 1. Upload Profile Photo
```
POST /api/v1/profile/photo
```
**Request:**
- Content-Type: `multipart/form-data`
- Body: `photo` (file) - JPEG, PNG, JPG, or GIF (max 5MB)

**Response:**
```json
{
  "success": true,
  "message": "Profile photo uploaded successfully",
  "data": {
    "profile_photo": "http://your-domain.com/storage/profile_photos/profile_1_1234567890.jpg"
  }
}
```

#### 2. Update Profile Photo
```
PUT /api/v1/profile/photo
```
Same as upload (alias endpoint)

#### 3. Delete Profile Photo
```
DELETE /api/v1/profile/photo
```
**Response:**
```json
{
  "success": true,
  "message": "Profile photo deleted successfully"
}
```

#### 4. Get User Profile (includes photo)
```
GET /api/v1/user
GET /api/v1/profile
```
**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "john_doe",
    "firstName": "John",
    "lastName": "Doe",
    "name": "John Doe",
    "email": "john@example.com",
    "role": "customer",
    "profile_photo": "http://your-domain.com/storage/profile_photos/profile_1_1234567890.jpg"
  }
}
```

**Note:** `profile_photo` will be `null` if no photo is uploaded.

### All User Responses Include Photo
The following API responses now include `profile_photo`:
- Login (`/api/v1/login`)
- Register (`/api/v1/register`)
- Google Sign-In (`/api/v1/google-signin`)
- Facebook Sign-In (`/api/v1/facebook-signin`)
- Email Verification (`/api/v1/verify-email`)
- Get User Profile (`/api/v1/user`)

---

## Android Implementation

### 1. Add Permissions to AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.CAMERA" />
```

### 2. Add Dependencies to build.gradle

```gradle
dependencies {
    // For image loading
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'
    
    // For image picking (optional - if using third-party library)
    // implementation 'com.github.dhaval2404:imagepicker:2.1'
}
```

### 3. Update API Service

Add to `ApiService.java`:

```java
@Multipart
@POST("v1/profile/photo")
Call<ProfilePhotoResponse> uploadProfilePhoto(
    @Header("Authorization") String token,
    @Part MultipartBody.Part photo
);

@HTTP(method = "DELETE", path = "v1/profile/photo", hasBody = false)
Call<BasicResponse> deleteProfilePhoto(
    @Header("Authorization") String token
);
```

### 4. Create Response Models

**ProfilePhotoResponse.java:**
```java
public class ProfilePhotoResponse {
    private boolean success;
    private String message;
    private Data data;

    public static class Data {
        private String profile_photo;
        
        public String getProfilePhoto() {
            return profile_photo;
        }
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Data getData() { return data; }
}
```

**BasicResponse.java:**
```java
public class BasicResponse {
    private boolean success;
    private String message;
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
```

### 5. Image Selection and Upload

**In your Fragment/Activity:**

```java
// Request permission (Android 13+)
private static final int PERMISSION_REQUEST_CODE = 100;
private static final int PICK_IMAGE_REQUEST = 101;

private void requestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.READ_MEDIA_IMAGES},
            PERMISSION_REQUEST_CODE);
    } else {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
            PERMISSION_REQUEST_CODE);
    }
}

// Open image picker
private void openImagePicker() {
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType("image/*");
    startActivityForResult(intent, PICK_IMAGE_REQUEST);
}

// Handle selected image
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    
    if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
        Uri imageUri = data.getData();
        uploadPhoto(imageUri);
    }
}

// Upload photo to server
private void uploadPhoto(Uri imageUri) {
    try {
        // Get real path from URI
        File file = new File(getRealPathFromURI(imageUri));
        
        // Create request body
        RequestBody requestFile = RequestBody.create(
            MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData(
            "photo", file.getName(), requestFile);
        
        // Get token
        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        
        // API call
        ApiService apiService = ApiClient.getApiService();
        Call<ProfilePhotoResponse> call = apiService.uploadProfilePhoto(token, body);
        
        call.enqueue(new Callback<ProfilePhotoResponse>() {
            @Override
            public void onResponse(Call<ProfilePhotoResponse> call, 
                                 Response<ProfilePhotoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String photoUrl = response.body().getData().getProfilePhoto();
                    // Update UI with new photo
                    loadProfilePhoto(photoUrl);
                    Toast.makeText(this, "Photo uploaded successfully", 
                                 Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Upload failed", 
                                 Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ProfilePhotoResponse> call, Throwable t) {
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    } catch (Exception e) {
        Log.e("ProfilePhoto", "Error uploading photo", e);
    }
}

// Helper method to get real path from URI
private String getRealPathFromURI(Uri uri) {
    String result = null;
    String[] projection = {MediaStore.Images.Media.DATA};
    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
    if (cursor != null) {
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            result = cursor.getString(columnIndex);
        }
        cursor.close();
    }
    return result;
}
```

### 6. Display Profile Photo with Glide

```java
private void loadProfilePhoto(String photoUrl) {
    ImageView profileImageView = findViewById(R.id.profileImageView);
    
    if (photoUrl != null && !photoUrl.isEmpty()) {
        Glide.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.ic_default_avatar) // Default placeholder
            .error(R.drawable.ic_default_avatar) // Error fallback
            .circleCrop() // Make it circular
            .into(profileImageView);
    } else {
        // Show default avatar
        profileImageView.setImageResource(R.drawable.ic_default_avatar);
    }
}
```

### 7. Delete Profile Photo

```java
private void deleteProfilePhoto() {
    TokenManager tokenManager = new TokenManager(this);
    String token = tokenManager.getToken();
    
    ApiService apiService = ApiClient.getApiService();
    Call<BasicResponse> call = apiService.deleteProfilePhoto(token);
    
    call.enqueue(new Callback<BasicResponse>() {
        @Override
        public void onResponse(Call<BasicResponse> call, 
                             Response<BasicResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                // Clear photo from UI
                ImageView profileImageView = findViewById(R.id.profileImageView);
                profileImageView.setImageResource(R.drawable.ic_default_avatar);
                Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show();
            }
        }
        
        @Override
        public void onFailure(Call<BasicResponse> call, Throwable t) {
            Toast.makeText(this, "Failed to delete photo", 
                         Toast.LENGTH_SHORT).show();
        }
    });
}
```

---

## Testing

1. **Upload Photo:**
   - Select an image from gallery
   - Verify it uploads successfully
   - Check that profile displays the new photo

2. **Display Photo:**
   - Logout and login again
   - Verify photo persists and displays correctly

3. **Delete Photo:**
   - Delete the profile photo
   - Verify it shows default avatar

4. **Different Auth Methods:**
   - Test with regular registration
   - Test with Google Sign-In
   - Test with Facebook Sign-In

---

## Notes

- Photos are stored at: `storage/app/public/profile_photos/`
- Max file size: 5MB
- Supported formats: JPEG, PNG, JPG, GIF
- Photos are automatically deleted when uploading a new one
- The `profile_photo` field in API responses will be:
  - Full URL if photo exists
  - `null` if no photo uploaded

---

## Troubleshooting

**"Storage link not found" error:**
```bash
php artisan storage:link
```

**Permission denied on Android:**
- Request runtime permissions for Android 6.0+
- For Android 13+, use `READ_MEDIA_IMAGES` instead of `READ_EXTERNAL_STORAGE`

**Image not displaying:**
- Check network permissions
- Verify image URL is correct
- Ensure Glide dependency is added

**Upload fails:**
- Check file size (max 5MB)
- Verify file type is supported
- Check authentication token is valid
