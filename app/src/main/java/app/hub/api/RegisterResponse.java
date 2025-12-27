package app.hub.api;

public class RegisterResponse {
    private boolean success;
    private String message;
    private Data data;
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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
    }

    public static class Data {
        private User user;
        private String token;
        private boolean requires_verification;

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public boolean isRequires_verification() {
            return requires_verification;
        }

        public void setRequires_verification(boolean requires_verification) {
            this.requires_verification = requires_verification;
        }
    }

    public static class User {
        private int id;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private String role;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
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

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class Errors {
        private String[] email;
        private String[] password;
        private String[] username;
        private String[] firstName;
        private String[] lastName;

        public String[] getEmail() {
            return email;
        }

        public void setEmail(String[] email) {
            this.email = email;
        }

        public String[] getPassword() {
            return password;
        }

        public void setPassword(String[] password) {
            this.password = password;
        }

        public String[] getUsername() {
            return username;
        }

        public void setUsername(String[] username) {
            this.username = username;
        }

        public String[] getFirstName() {
            return firstName;
        }

        public void setFirstName(String[] firstName) {
            this.firstName = firstName;
        }

        public String[] getLastName() {
            return lastName;
        }

        public void setLastName(String[] lastName) {
            this.lastName = lastName;
        }
    }
}
