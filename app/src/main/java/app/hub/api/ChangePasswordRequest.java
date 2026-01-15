package app.hub.api;

public class ChangePasswordRequest {
    private String current_password;
    private String new_password;
    private String new_password_confirmation;

    public ChangePasswordRequest(String currentPassword, String newPassword, String newPasswordConfirmation) {
        this.current_password = currentPassword;
        this.new_password = newPassword;
        this.new_password_confirmation = newPasswordConfirmation;
    }

    public String getCurrent_password() {
        return current_password;
    }

    public void setCurrent_password(String current_password) {
        this.current_password = current_password;
    }

    public String getNew_password() {
        return new_password;
    }

    public void setNew_password(String new_password) {
        this.new_password = new_password;
    }

    public String getNew_password_confirmation() {
        return new_password_confirmation;
    }

    public void setNew_password_confirmation(String new_password_confirmation) {
        this.new_password_confirmation = new_password_confirmation;
    }
}
