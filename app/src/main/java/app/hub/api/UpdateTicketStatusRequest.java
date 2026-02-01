package app.hub.api;

import com.google.gson.annotations.SerializedName;

public class UpdateTicketStatusRequest {
    @SerializedName("status")
    private String status;
    
    @SerializedName("assigned_staff_id")
    private Integer assignedStaffId;

    public UpdateTicketStatusRequest(String status) {
        this.status = status;
    }

    public UpdateTicketStatusRequest(String status, Integer assignedStaffId) {
        this.status = status;
        this.assignedStaffId = assignedStaffId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAssignedStaffId() {
        return assignedStaffId;
    }

    public void setAssignedStaffId(Integer assignedStaffId) {
        this.assignedStaffId = assignedStaffId;
    }
}