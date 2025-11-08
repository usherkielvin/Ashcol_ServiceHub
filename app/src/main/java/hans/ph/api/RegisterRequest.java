package hans.ph.api;

public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String password_confirmation;
    private String role;
    private String phone;
    private String department;
    private String address;

    public RegisterRequest(String name, String email, String password, String passwordConfirmation, String role, String phone, String department, String address) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.password_confirmation = passwordConfirmation;
        this.role = role != null ? role : "employee";
        this.phone = phone;
        this.department = department;
        this.address = address;
    }

    public RegisterRequest(String name, String email, String password, String passwordConfirmation, String role) {
        this(name, email, password, passwordConfirmation, role, null, null, null);
    }

    public RegisterRequest(String name, String email, String password, String passwordConfirmation) {
        this(name, email, password, passwordConfirmation, "employee", null, null, null);
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

