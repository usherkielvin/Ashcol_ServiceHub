package app.hub.api;

public class GoogleSignInRequest {
    private String id_token;
    private String email;
    private String first_name;
    private String last_name;
    private String phone;

    public GoogleSignInRequest(String idToken, String email, String firstName, String lastName, String phone) {
        this.id_token = idToken;
        this.email = email;
        this.first_name = firstName;
        this.last_name = lastName;
        this.phone = phone;
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
}
