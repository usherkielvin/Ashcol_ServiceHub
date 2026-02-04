package app.hub.api;

public class RegisterRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String location;
    private String password;
    private String password_confirmation;
    private String role;
    private String branch;

    // Default constructor
    public RegisterRequest() {
    }

    public RegisterRequest(String username, String firstName, String lastName, String email, String phone, String location, String password, String passwordConfirmation, String role, String branch) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.password = password;
        this.password_confirmation = passwordConfirmation;
        this.role = role;
        this.branch = branch;
    }

    public RegisterRequest(String username, String firstName, String lastName, String email, String phone, String location, String password, String passwordConfirmation, String role) {
        this(username, firstName, lastName, email, phone, location, password, passwordConfirmation, role, null);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword_confirmation() {
        return password_confirmation;
    }

    public void setPassword_confirmation(String password_confirmation) {
        this.password_confirmation = password_confirmation;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
