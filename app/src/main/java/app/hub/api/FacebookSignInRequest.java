package app.hub.api;

public class FacebookSignInRequest {
    private String access_token;
    private String email;
    private String first_name;
    private String last_name;
    private String phone;

    public FacebookSignInRequest(String accessToken, String email, String firstName, String lastName, String phone) {
        this.access_token = accessToken;
        this.email = email;
        this.first_name = firstName;
        this.last_name = lastName;
        this.phone = phone;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
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
