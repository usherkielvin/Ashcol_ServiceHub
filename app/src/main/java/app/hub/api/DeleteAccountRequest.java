package app.hub.api;

import com.google.gson.annotations.SerializedName;

public class DeleteAccountRequest {
    @SerializedName("password")
    private String password;

    public DeleteAccountRequest(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
