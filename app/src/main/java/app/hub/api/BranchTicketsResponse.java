package app.hub.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BranchTicketsResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("branch")
    private BranchInfo branch;
    
    @SerializedName("tickets")
    private List<Ticket> tickets;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public BranchInfo getBranch() {
        return branch;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public static class BranchInfo {
        @SerializedName("id")
        private int id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("location")
        private String location;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLocation() {
            return location;
        }
    }

    public static class Ticket {
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
        
        @SerializedName("amount")
        private double amount;
        
        @SerializedName("address")
        private String address;
        
        @SerializedName("contact")
        private String contact;
        
        @SerializedName("preferred_date")
        private String preferredDate;
        
        @SerializedName("status")
        private String status;
        
        @SerializedName("status_detail")
        private String statusDetail;
        
        @SerializedName("status_color")
        private String statusColor;
        
        @SerializedName("customer_name")
        private String customerName;
        
        @SerializedName("assigned_staff")
        private String assignedStaff;
        
        @SerializedName("image_path")
        private String imagePath;
        
        @SerializedName("created_at")
        private String createdAt;
        
        @SerializedName("updated_at")
        private String updatedAt;

        public int getId() {
            return id;
        }

        public String getTicketId() {
            return ticketId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getServiceType() {
            return serviceType;
        }

        public double getAmount() {
            return amount;
        }

        public String getAddress() {
            return address;
        }

        public String getContact() {
            return contact;
        }

        public String getPreferredDate() {
            return preferredDate;
        }

        public String getStatus() {
            return status;
        }

        public String getStatusDetail() {
            return statusDetail;
        }

        public String getStatusColor() {
            return statusColor;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getAssignedStaff() {
            return assignedStaff;
        }

        public String getImagePath() {
            return imagePath;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }
    }
}
