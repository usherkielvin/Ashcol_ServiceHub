package app.hub.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EmployeeScheduleResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("tickets")
    private List<ScheduledTicket> tickets;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<ScheduledTicket> getTickets() {
        return tickets;
    }

    public void setTickets(List<ScheduledTicket> tickets) {
        this.tickets = tickets;
    }

    public static class ScheduledTicket {
        @SerializedName("ticket_id")
        private String ticketId;

        @SerializedName("title")
        private String title;

        @SerializedName("description")
        private String description;

        @SerializedName("scheduled_date")
        private String scheduledDate;

        @SerializedName("scheduled_time")
        private String scheduledTime;

        @SerializedName("schedule_notes")
        private String scheduleNotes;

        @SerializedName("status")
        private String status;

        @SerializedName("status_color")
        private String statusColor;

        @SerializedName("customer_name")
        private String customerName;

        @SerializedName("address")
        private String address;

        @SerializedName("service_type")
        private String serviceType;

        @SerializedName("branch")
        private String branch;

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

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getServiceType() {
            return serviceType;
        }

        public void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }

        public String getBranch() {
            return branch;
        }

        public void setBranch(String branch) {
            this.branch = branch;
        }
    }
}