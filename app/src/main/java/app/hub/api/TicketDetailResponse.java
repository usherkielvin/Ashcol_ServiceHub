package app.hub.api;

import com.google.gson.annotations.SerializedName;

public class TicketDetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("ticket")
    private TicketDetail ticket;

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

    public TicketDetail getTicket() {
        return ticket;
    }

    public void setTicket(TicketDetail ticket) {
        this.ticket = ticket;
    }

    public static class TicketDetail {
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

        @SerializedName("amount")
        private double amount;

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

        @SerializedName("latitude")
        private double latitude;

        @SerializedName("longitude")
        private double longitude;

        @SerializedName("scheduled_date")
        private String scheduledDate;

        @SerializedName("scheduled_time")
        private String scheduledTime;

        @SerializedName("schedule_notes")
        private String scheduleNotes;

        // Getters and setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTicketId() {
            return ticketId;
        }

        public void setTicketId(String ticketId) {
            this.ticketId = ticketId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getServiceType() {
            return serviceType;
        }

        public void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getContact() {
            return contact;
        }

        public void setContact(String contact) {
            this.contact = contact;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getStatusColor() {
            return statusColor;
        }

        public void setStatusColor(String statusColor) {
            this.statusColor = statusColor;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getAssignedStaff() {
            return assignedStaff;
        }

        public void setAssignedStaff(String assignedStaff) {
            this.assignedStaff = assignedStaff;
        }

        public String getBranch() {
            return branch;
        }

        public void setBranch(String branch) {
            this.branch = branch;
        }

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String getScheduledDate() {
            return scheduledDate;
        }

        public void setScheduledDate(String scheduledDate) {
            this.scheduledDate = scheduledDate;
        }

        public String getScheduledTime() {
            return scheduledTime;
        }

        public void setScheduledTime(String scheduledTime) {
            this.scheduledTime = scheduledTime;
        }

        public String getScheduleNotes() {
            return scheduleNotes;
        }

        public void setScheduleNotes(String scheduleNotes) {
            this.scheduleNotes = scheduleNotes;
        }
    }
}