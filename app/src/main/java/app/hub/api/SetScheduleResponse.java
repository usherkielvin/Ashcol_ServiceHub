package app.hub.api;

import com.google.gson.annotations.SerializedName;

public class SetScheduleResponse {
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
        @SerializedName("ticket_id")
        private String ticketId;
        
        @SerializedName("scheduled_date")
        private String scheduledDate;
        
        @SerializedName("scheduled_time")
        private String scheduledTime;
        
        @SerializedName("schedule_notes")
        private String scheduleNotes;
        
        @SerializedName("assigned_staff")
        private String assignedStaff;

        public String getTicketId() {
            return ticketId;
        }

        public void setTicketId(String ticketId) {
            this.ticketId = ticketId;
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

        public String getAssignedStaff() {
            return assignedStaff;
        }

        public void setAssignedStaff(String assignedStaff) {
            this.assignedStaff = assignedStaff;
        }
    }
}