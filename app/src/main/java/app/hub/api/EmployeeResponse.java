package app.hub.api;

import java.util.List;

public class EmployeeResponse {
    private boolean success;
    private String message;
    private Data data;

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

    public static class Data {
        private String branch;
        private int employee_count;
        private List<Employee> employees;

        public String getBranch() {
            return branch;
        }

        public void setBranch(String branch) {
            this.branch = branch;
        }

        public int getEmployeeCount() {
            return employee_count;
        }

        public void setEmployeeCount(int employee_count) {
            this.employee_count = employee_count;
        }

        public List<Employee> getEmployees() {
            return employees;
        }

        public void setEmployees(List<Employee> employees) {
            this.employees = employees;
        }
    }

    public static class Employee {
        private int id;
        private String username;
        private String firstName;
        private String lastName;
        private String name;
        private String email;
        private String profile_photo;
        private String created_at;

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

        public String getProfilePhoto() {
            return profile_photo;
        }

        public void setProfilePhoto(String profile_photo) {
            this.profile_photo = profile_photo;
        }

        public String getCreatedAt() {
            return created_at;
        }

        public void setCreatedAt(String created_at) {
            this.created_at = created_at;
        }
    }
}