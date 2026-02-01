package app.hub.api;

import com.google.gson.annotations.SerializedName;

public class TestResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("user")
    private UserInfo user;
    
    @SerializedName("timestamp")
    private String timestamp;

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

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public static class UserInfo {
        @SerializedName("id")
        private int id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("email")
        private String email;
        
        @SerializedName("role")
        private String role;
        
        @SerializedName("is_customer")
        private boolean isCustomer;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
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

        public boolean isCustomer() {
            return isCustomer;
        }

        public void setCustomer(boolean customer) {
            isCustomer = customer;
        }
    }
}