package app.hub.api;

import com.google.gson.annotations.SerializedName;

public class SetScheduleRequest {
    @SerializedName("scheduled_date")
    private String scheduledDate;
    
    @SerializedName("scheduled_time")
    private String scheduledTime;
    
    @SerializedName("schedule_notes")
    private String scheduleNotes;
    
    @SerializedName("assigned_staff_id")
    private int assignedStaffId;

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

    public int getAssignedStaffId() {
        return assignedStaffId;
    }

    public void setAssignedStaffId(int assignedStaffId) {
        this.assignedStaffId = assignedStaffId;
    }
}