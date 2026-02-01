package app.hub.api;

import com.google.gson.annotations.SerializedName;

public class UpdateLocationRequest {
    @SerializedName("location")
    private String location;
    
    @SerializedName("latitude")
    private double latitude;
    
    @SerializedName("longitude")
    private double longitude;

    public UpdateLocationRequest(String location) {
        this.location = location;
    }
    
    public UpdateLocationRequest(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}