package app.hub.api;

import com.google.gson.annotations.SerializedName;

public class PaymentRequestBody {
    @SerializedName("ticket_id")
    private String ticketId;

    @SerializedName("technician_id")
    private int technicianId;

    public PaymentRequestBody(String ticketId, int technicianId) {
        this.ticketId = ticketId;
        this.technicianId = technicianId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public int getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(int technicianId) {
        this.technicianId = technicianId;
    }
}
