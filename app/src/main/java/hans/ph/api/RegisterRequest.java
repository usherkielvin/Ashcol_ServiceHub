package hans.ph.api;

public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String password_confirmation;
    private String role;

    public RegisterRequest(String name, String email, String password, String passwordConfirmation, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.password_confirmation = passwordConfirmation;
        this.role = role != null ? role : "customer";
    }

    public RegisterRequest(String name, String email, String password, String passwordConfirmation) {
        this(name, email, password, passwordConfirmation, "customer");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}

