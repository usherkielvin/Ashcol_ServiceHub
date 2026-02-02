package app.hub.api;

import com.google.gson.annotations.SerializedName;

public class CreateTicketRequest {
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
    
    @SerializedName("preferred_date")
    private String preferredDate;
    
    @SerializedName("priority")
    private String priority;

    public CreateTicketRequest(String title, String description, String serviceType, String address, String contact) {
        this(title, description, serviceType, address, contact, null, "medium");
    }

    public CreateTicketRequest(String title, String description, String serviceType, String address, String contact, String preferredDate, String priority) {
        this.title = title;
        this.description = description;
        this.serviceType = serviceType;
        this.address = address;
        this.contact = contact;
        this.preferredDate = preferredDate;
        this.priority = priority != null ? priority : "medium";
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getPreferredDate() { return preferredDate; }
    public void setPreferredDate(String preferredDate) { this.preferredDate = preferredDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
