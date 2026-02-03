package app.hub.api;

public class GoogleSignInRequest {
    private String id_token;
    private String email;
    private String first_name;
    private String last_name;
    private String phone;
    private String region;
    private String city;
    private String location;

    public GoogleSignInRequest(String idToken, String email, String firstName, String lastName, String phone,
            String region, String city, String location) {
        this.id_token = idToken;
        this.email = email;
        this.first_name = firstName;
        this.last_name = lastName;
        this.phone = phone;
        this.region = region;
        this.city = city;
        this.location = location;
    }

    public GoogleSignInRequest(String idToken, String email, String firstName, String lastName, String phone) {
        this(idToken, email, firstName, lastName, phone, null, null, null);
    }

    public String getId_token() {
        return id_token;
    }

    public void setId_token(String id_token) {
        this.id_token = id_token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
