package app.hub.map;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import android.widget.ImageButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import app.hub.R;
import app.hub.util.LocationUtils;

public class MapSelectionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapSelectionActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private GoogleMap mMap;
    private Button btnFinish;
    private TextView tvSelectedAddress;
    private LinearLayout bottomSheet;
    private LinearLayout bottomSheetContent;
    private View dragHandle;
    private ImageButton fabCurrentLocation;
    private FusedLocationProviderClient fusedLocationClient;
    
    private LatLng currentCenterLatLng;
    private String selectedAddress = "";
    
    // Bottom sheet sliding variables
    private float initialY;
    private float initialTranslationY;
    private boolean isExpanded = false;
    private int peekHeight = 200; // dp
    private int expandedHeight = 400; // dp

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_selection);

        btnFinish = findViewById(R.id.btnFinish);
        tvSelectedAddress = findViewById(R.id.tvSelectedAddress);
        bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetContent = findViewById(R.id.bottomSheetContent);
        dragHandle = findViewById(R.id.dragHandle);
        fabCurrentLocation = findViewById(R.id.fabCurrentLocation);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup bottom sheet sliding
        setupBottomSheetSliding();

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Finish button click handler
        btnFinish.setOnClickListener(v -> {
            if (currentCenterLatLng != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", currentCenterLatLng.latitude);
                resultIntent.putExtra("longitude", currentCenterLatLng.longitude);
                resultIntent.putExtra("address", selectedAddress);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Map is not ready yet", Toast.LENGTH_SHORT).show();
            }
        });

        // Current location button click handler
        fabCurrentLocation.setOnClickListener(v -> getCurrentLocation());
    }

    private void setupBottomSheetSliding() {
        // Convert dp to pixels
        float density = getResources().getDisplayMetrics().density;
        peekHeight = (int) (peekHeight * density);
        expandedHeight = (int) (expandedHeight * density);
        
        // Set initial position (collapsed)
        bottomSheet.post(() -> {
            int fullHeight = bottomSheet.getHeight();
            bottomSheet.setTranslationY(fullHeight - peekHeight);
        });

        // Handle drag gestures
        dragHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialY = event.getRawY();
                        initialTranslationY = bottomSheet.getTranslationY();
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        float deltaY = event.getRawY() - initialY;
                        float newTranslationY = initialTranslationY + deltaY;
                        
                        // Constrain movement
                        int fullHeight = bottomSheet.getHeight();
                        float minTranslation = 0; // Fully expanded
                        float maxTranslation = fullHeight - peekHeight; // Collapsed
                        
                        newTranslationY = Math.max(minTranslation, Math.min(maxTranslation, newTranslationY));
                        bottomSheet.setTranslationY(newTranslationY);
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                        // Snap to expanded or collapsed based on position
                        float currentTranslation = bottomSheet.getTranslationY();
                        int fullHeight2 = bottomSheet.getHeight();
                        float midPoint = (fullHeight2 - peekHeight) / 2f;
                        
                        if (currentTranslation < midPoint) {
                            // Snap to expanded
                            animateBottomSheet(0);
                            isExpanded = true;
                        } else {
                            // Snap to collapsed
                            animateBottomSheet(fullHeight2 - peekHeight);
                            isExpanded = false;
                        }
                        return true;
                }
                return false;
            }
        });

        // Also handle taps on drag handle
        dragHandle.setOnClickListener(v -> {
            if (isExpanded) {
                // Collapse
                int fullHeight = bottomSheet.getHeight();
                animateBottomSheet(fullHeight - peekHeight);
                isExpanded = false;
            } else {
                // Expand
                animateBottomSheet(0);
                isExpanded = true;
            }
        });
    }

    private void animateBottomSheet(float targetTranslationY) {
        ValueAnimator animator = ValueAnimator.ofFloat(bottomSheet.getTranslationY(), targetTranslationY);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            float value = (Float) animation.getAnimatedValue();
            bottomSheet.setTranslationY(value);
        });
        animator.start();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Restrict map to Philippines only
        mMap.setMinZoomPreference(5.0f);
        
        // Set initial camera to center of Philippines
        LatLng philippinesCenter = new LatLng(12.8797, 121.7740);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(philippinesCenter, 10));
        currentCenterLatLng = philippinesCenter;
        
        // Get initial address
        getAddressFromCoordinates(philippinesCenter);

        // Listen for camera movements to update the center location
        mMap.setOnCameraIdleListener(() -> {
            LatLng centerLatLng = mMap.getCameraPosition().target;
            
            if (isWithinPhilippines(centerLatLng)) {
                currentCenterLatLng = centerLatLng;
                getAddressFromCoordinates(centerLatLng);
            } else {
                if (currentCenterLatLng != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(currentCenterLatLng));
                }
                Toast.makeText(this, "Please select a location within the Philippines", Toast.LENGTH_SHORT).show();
            }
        });

        // Disable map click listeners since we're using the pin approach
        mMap.setOnMapClickListener(null);
        mMap.setOnMapLongClickListener(null);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            // Request location permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        
                        // Check if user location is within Philippines
                        if (isWithinPhilippines(userLocation)) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                            currentCenterLatLng = userLocation;
                            getAddressFromCoordinates(userLocation);
                        } else {
                            Toast.makeText(this, "Your current location is outside the Philippines", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isWithinPhilippines(LatLng latLng) {
        return LocationUtils.isWithinPhilippines(latLng.latitude, latLng.longitude);
    }

    private void getAddressFromCoordinates(LatLng latLng) {
        tvSelectedAddress.setText("Loading address...");
        
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(MapSelectionActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                
                runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        StringBuilder addressBuilder = new StringBuilder();
                        
                        if (address.getFeatureName() != null && !address.getFeatureName().equals(address.getSubLocality())) {
                            addressBuilder.append(address.getFeatureName()).append(", ");
                        }
                        if (address.getSubLocality() != null) {
                            addressBuilder.append(address.getSubLocality()).append(", ");
                        }
                        if (address.getLocality() != null) {
                            addressBuilder.append(address.getLocality()).append(", ");
                        }
                        if (address.getSubAdminArea() != null) {
                            addressBuilder.append(address.getSubAdminArea()).append(", ");
                        }
                        if (address.getAdminArea() != null) {
                            addressBuilder.append(address.getAdminArea()).append(", ");
                        }
                        addressBuilder.append("Philippines");
                        
                        selectedAddress = addressBuilder.toString();
                        tvSelectedAddress.setText(selectedAddress);
                    } else {
                        selectedAddress = String.format("Coordinates: %.6f, %.6f", 
                                                      latLng.latitude, latLng.longitude);
                        tvSelectedAddress.setText(selectedAddress);
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, "Error getting address", e);
                runOnUiThread(() -> {
                    selectedAddress = String.format("Coordinates: %.6f, %.6f", 
                                                  latLng.latitude, latLng.longitude);
                    tvSelectedAddress.setText(selectedAddress);
                });
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (isExpanded) {
            // Collapse the bottom sheet first
            int fullHeight = bottomSheet.getHeight();
            animateBottomSheet(fullHeight - peekHeight);
            isExpanded = false;
        } else {
            super.onBackPressed();
        }
    }
}