package app.hub.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BranchResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("branches")
    private List<Branch> branches;
    
    @SerializedName("total_branches")
    private int totalBranches;

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

    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

    public int getTotalBranches() {
        return totalBranches;
    }

    public void setTotalBranches(int totalBranches) {
        this.totalBranches = totalBranches;
    }

    public static class Branch {
        @SerializedName("id")
        private int id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("location")
        private String location;
        
        @SerializedName("address")
        private String address;
        
        @SerializedName("latitude")
        private double latitude;
        
        @SerializedName("longitude")
        private double longitude;
        
        @SerializedName("manager")
        private String manager;
        
        @SerializedName("employee_count")
        private int employeeCount;
        
        @SerializedName("description")
        private String description;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public String getManager() { return manager; }
        public void setManager(String manager) { this.manager = manager; }
        
        public int getEmployeeCount() { return employeeCount; }
        public void setEmployeeCount(int employeeCount) { this.employeeCount = employeeCount; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}