package app.hub.api;

public class SetInitialPasswordRequest {
    private String password;
    private String password_confirmation;

    public SetInitialPasswordRequest(String password, String passwordConfirmation) {
        this.password = password;
        this.password_confirmation = passwordConfirmation;
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
}
