package app.hub.api;

import com.google.gson.annotations.SerializedName;

public class UpdateTicketStatusResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("ticket")
    private TicketData ticket;

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

    public TicketData getTicket() {
        return ticket;
    }

    public void setTicket(TicketData ticket) {
        this.ticket = ticket;
    }

    public static class TicketData {
        @SerializedName("id")
        private int id;
        
        @SerializedName("ticket_id")
        private String ticketId;
        
        @SerializedName("status")
        private String status;
        
        @SerializedName("assigned_staff_id")
        private Integer assignedStaffId;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getTicketId() { return ticketId; }
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Integer getAssignedStaffId() { return assignedStaffId; }
        public void setAssignedStaffId(Integer assignedStaffId) { this.assignedStaffId = assignedStaffId; }
    }
}