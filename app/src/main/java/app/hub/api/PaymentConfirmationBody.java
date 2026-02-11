package app.hub.api;

import com.google.gson.annotations.SerializedName;

public class PaymentConfirmationBody {
    @SerializedName("ticket_id")
    private String ticketId;

    @SerializedName("customer_id")
    private int customerId;

    @SerializedName("payment_method")
    private String paymentMethod;

    @SerializedName("amount")
    private double amount;

    public PaymentConfirmationBody(String ticketId, int customerId, String paymentMethod, double amount) {
        this.ticketId = ticketId;
        this.customerId = customerId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
