package app.hub.api;

public class ProfilePhotoResponse {
    private boolean success;
    private String message;
    private Data data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private String profile_photo;

        public String getProfilePhoto() {
            return profile_photo;
        }

        public void setProfilePhoto(String profile_photo) {
            this.profile_photo = profile_photo;
        }
    }
}
