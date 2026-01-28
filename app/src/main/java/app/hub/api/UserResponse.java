package app.hub.api;

public class UserResponse {
    private boolean success;
    private Data data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private int id;
        private String username;
        private String firstName;
        private String lastName;
        private String name;
        private String email;
        private String role;
        private String profile_photo;
        private String location;
        private String branch;
        private boolean has_facebook_account;

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

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public boolean hasFacebookAccount() {
            return has_facebook_account;
        }

        public void setHas_facebook_account(boolean has_facebook_account) {
            this.has_facebook_account = has_facebook_account;
        }

        public String getProfilePhoto() {
            return profile_photo;
        }

        public void setProfilePhoto(String profile_photo) {
            this.profile_photo = profile_photo;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getBranch() {
            return branch;
        }

        public void setBranch(String branch) {
            this.branch = branch;
        }
    }
}

