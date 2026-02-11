package app.hub.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BranchReportsResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("branches")
    private List<BranchReport> branches;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<BranchReport> getBranches() {
        return branches;
    }

    public static class BranchReport {
        @SerializedName("id")
        private int id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("location")
        private String location;
        
        @SerializedName("completed_count")
        private int completedCount;
        
        @SerializedName("cancelled_count")
        private int cancelledCount;
        
        @SerializedName("total_tickets")
        private int totalTickets;
        
        @SerializedName("manager")
        private String manager;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLocation() {
            return location;
        }

        public int getCompletedCount() {
            return completedCount;
        }

        public int getCancelledCount() {
            return cancelledCount;
        }

        public int getTotalTickets() {
            return totalTickets;
        }

        public String getManager() {
            return manager;
        }
    }
}
