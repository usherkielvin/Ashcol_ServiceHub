package app.hub.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TicketListResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("tickets")
    private List<TicketItem> tickets;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<TicketItem> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketItem> tickets) {
        this.tickets = tickets;
    }

    public static class TicketItem {
        @SerializedName("id")
        private int id;
        
        @SerializedName("ticket_id")
        private String ticketId;
        
        @SerializedName("title")
        private String title;
        
        @SerializedName("description")
        private String description;
        
        @SerializedName("service_type")
        private String serviceType;
        
        @SerializedName("address")
        private String address;
        
        @SerializedName("contact")
        private String contact;
        
        @SerializedName("priority")
        private String priority;
        
        @SerializedName("status")
        private String status;
        
        @SerializedName("status_color")
        private String statusColor;
        
        @SerializedName("customer_name")
        private String customerName;
        
        @SerializedName("assigned_staff")
        private String assignedStaff;
        
        @SerializedName("branch")
        private String branch;
        
        @SerializedName("image_path")
        private String imagePath;
        
        @SerializedName("created_at")
        private String createdAt;
        
        @SerializedName("updated_at")
        private String updatedAt;

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getTicketId() { return ticketId; }
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getContact() { return contact; }
        public void setContact(String contact) { this.contact = contact; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getStatusColor() { return statusColor; }
        public void setStatusColor(String statusColor) { this.statusColor = statusColor; }
        
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        
        public String getAssignedStaff() { return assignedStaff; }
        public void setAssignedStaff(String assignedStaff) { this.assignedStaff = assignedStaff; }
        
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
        
        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }
}