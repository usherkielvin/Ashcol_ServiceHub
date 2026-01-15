package app.hub.api;

public class ChangePasswordResponse {
    private boolean success;
    private String message;
    private Errors errors;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
    }

    public static class Errors {
        private String[] current_password;
        private String[] new_password;
        private String[] new_password_confirmation;

        public String[] getCurrent_password() {
            return current_password;
        }

        public void setCurrent_password(String[] current_password) {
            this.current_password = current_password;
        }

        public String[] getNew_password() {
            return new_password;
        }

        public void setNew_password(String[] new_password) {
            this.new_password = new_password;
        }

        public String[] getNew_password_confirmation() {
            return new_password_confirmation;
        }

        public void setNew_password_confirmation(String[] new_password_confirmation) {
            this.new_password_confirmation = new_password_confirmation;
        }
    }
}
