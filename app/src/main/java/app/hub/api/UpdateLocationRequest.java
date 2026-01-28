package app.hub.api;

public class UpdateLocationRequest {
    private String location;

    public UpdateLocationRequest(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}