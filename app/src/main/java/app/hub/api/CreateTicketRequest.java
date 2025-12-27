package app.hub.api;

public class CreateTicketRequest {
    private String title;
    private String description;
    private String serviceType;
    private String address;
    private String contact;

    public CreateTicketRequest(String title, String description, String serviceType, String address, String contact) {
        this.title = title;
        this.description = description;
        this.serviceType = serviceType;
        this.address = address;
        this.contact = contact;
    }

    // Getters and setters for all fields

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

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
