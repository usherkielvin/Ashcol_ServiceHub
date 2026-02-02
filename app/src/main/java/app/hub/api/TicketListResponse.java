package app.hub.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TicketListResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("tickets")
    private List<TicketItem> tickets;
    
    @SerializedName("message")
    private String message;

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
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
        
        @SerializedName("scheduled_date")
        private String scheduledDate;
        
        @SerializedName("scheduled_time")
        private String scheduledTime;
        
        @SerializedName("schedule_notes")
        private String scheduleNotes;

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
        
        public String getScheduledDate() { return scheduledDate; }
        public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }
        
        public String getScheduledTime() { return scheduledTime; }
        public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }
        
        public String getScheduleNotes() { return scheduleNotes; }
        public void setScheduleNotes(String scheduleNotes) { this.scheduleNotes = scheduleNotes; }
    }

    /**
     * Build a TicketItem from CreateTicketResponse for instant optimistic display.
     */
    public static TicketItem fromCreateResponse(CreateTicketResponse.TicketData data, String statusName, String statusColor) {
        if (data == null) return null;
        TicketItem item = new TicketItem();
        item.setId(data.getId());
        item.setTicketId(data.getTicketId());
        item.setTitle(data.getTitle());
        item.setDescription(data.getDescription());
        item.setServiceType(data.getServiceType());
        item.setAddress(data.getAddress());
        item.setContact(data.getContact());
        item.setPriority("medium");
        item.setStatus(statusName != null ? statusName : (data.getStatus() != null ? data.getStatus().getName() : "Pending"));
        item.setStatusColor(statusColor != null ? statusColor : (data.getStatus() != null ? data.getStatus().getColor() : "#gray"));
        if (data.getCustomer() != null) {
            String name = (data.getCustomer().getFirstName() != null ? data.getCustomer().getFirstName() + " " : "")
                    + (data.getCustomer().getLastName() != null ? data.getCustomer().getLastName() : "").trim();
            item.setCustomerName(name.trim().isEmpty() ? "Customer" : name.trim());
        } else {
            item.setCustomerName("Customer");
        }
        item.setBranch(data.getBranch() != null ? data.getBranch().getName() : null);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        String now = sdf.format(new java.util.Date());
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        return item;
    }
}