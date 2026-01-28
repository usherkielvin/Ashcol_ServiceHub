package app.hub.map;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import app.hub.R;
import app.hub.util.LocationUtils;

public class MapSelectionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapSelectionActivity";
    
    private GoogleMap mMap;
    private Marker selectedMarker;
    private TextView tvSelectedLocation;
    private Button btnConfirmLocation;
    private LatLng selectedLatLng;
    private String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_selection);

        tvSelectedLocation = findViewById(R.id.tvSelectedLocation);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set initial text
        tvSelectedLocation.setText("Tap on the map to select a location");

        // Confirm button click handler
        btnConfirmLocation.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedLatLng.latitude);
                resultIntent.putExtra("longitude", selectedLatLng.longitude);
                resultIntent.putExtra("address", selectedAddress);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please select a location on the map first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Restrict map to Philippines only
        mMap.setMinZoomPreference(5.0f);
        
        // Set initial camera to center of Philippines
        LatLng philippinesCenter = new LatLng(12.8797, 121.7740);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(philippinesCenter, 6));

        // Add click listener to select location
        mMap.setOnMapClickListener(latLng -> {
            // Check if clicked location is within Philippines bounds
            if (isWithinPhilippines(latLng)) {
                selectLocation(latLng);
            } else {
                Toast.makeText(this, "Please select a location within the Philippines", Toast.LENGTH_SHORT).show();
            }
        });

        // Add long click listener for alternative selection
        mMap.setOnMapLongClickListener(latLng -> {
            if (isWithinPhilippines(latLng)) {
                selectLocation(latLng);
            }
        });
    }

    private boolean isWithinPhilippines(LatLng latLng) {
        return LocationUtils.isWithinPhilippines(latLng.latitude, latLng.longitude);
    }

    private void selectLocation(LatLng latLng) {
        // Remove previous marker if exists
        if (selectedMarker != null) {
            selectedMarker.remove();
        }

        // Add new marker
        selectedMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Selected Location"));

        selectedLatLng = latLng;
        
        // Move camera to selected location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        // Get address from coordinates
        getAddressFromCoordinates(latLng);
    }

    private void getAddressFromCoordinates(LatLng latLng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(MapSelectionActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                
                runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        StringBuilder addressBuilder = new StringBuilder();
                        
                        // Build address string
                        if (address.getSubLocality() != null) {
                            addressBuilder.append(address.getSubLocality()).append(", ");
                        }
                        if (address.getLocality() != null) {
                            addressBuilder.append(address.getLocality()).append(", ");
                        }
                        if (address.getAdminArea() != null) {
                            addressBuilder.append(address.getAdminArea()).append(", ");
                        }
                        addressBuilder.append("Philippines");
                        
                        selectedAddress = addressBuilder.toString();
                        tvSelectedLocation.setText("Selected: " + selectedAddress);
                    } else {
                        selectedAddress = "Lat: " + String.format("%.6f", latLng.latitude) + 
                                        ", Lng: " + String.format("%.6f", latLng.longitude);
                        tvSelectedLocation.setText("Selected: " + selectedAddress);
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, "Error getting address", e);
                runOnUiThread(() -> {
                    selectedAddress = "Lat: " + String.format("%.6f", latLng.latitude) + 
                                    ", Lng: " + String.format("%.6f", latLng.longitude);
                    tvSelectedLocation.setText("Selected: " + selectedAddress);
                });
            }
        }).start();
    }
}