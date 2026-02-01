package app.hub.api;

import com.google.gson.annotations.SerializedName;

public class TicketStatusResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("ticket")
    private TicketStatusData ticket;

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

    public TicketStatusData getTicket() {
        return ticket;
    }

    public void setTicket(TicketStatusData ticket) {
        this.ticket = ticket;
    }

    public static class TicketStatusData {
        @SerializedName("ticket_id")
        private String ticketId;
        
        @SerializedName("status")
        private String status;
        
        @SerializedName("status_color")
        private String statusColor;
        
        @SerializedName("assigned_staff")
        private String assignedStaff;

        public String getTicketId() { return ticketId; }
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getStatusColor() { return statusColor; }
        public void setStatusColor(String statusColor) { this.statusColor = statusColor; }
        
        public String getAssignedStaff() { return assignedStaff; }
        public void setAssignedStaff(String assignedStaff) { this.assignedStaff = assignedStaff; }
    }
}