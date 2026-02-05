package app.hub.api;

public class FCMTokenRequest {
    private String fcm_token;

    public FCMTokenRequest(String fcmToken) {
        this.fcm_token = fcmToken;
    }

    public String getFcmToken() {
        return fcm_token;
    }

    public void setFcmToken(String fcmToken) {
        this.fcm_token = fcmToken;
    }
}
