package app.hub.api;

public class AboutResponse {
    private boolean success;
    private Data data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private String title;
        private String description;
        private String support_email;
        private String support_phone;
        private String support_hours;

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

        public String getSupportEmail() {
            return support_email;
        }

        public void setSupportEmail(String support_email) {
            this.support_email = support_email;
        }

        public String getSupportPhone() {
            return support_phone;
        }

        public void setSupportPhone(String support_phone) {
            this.support_phone = support_phone;
        }

        public String getSupportHours() {
            return support_hours;
        }

        public void setSupportHours(String support_hours) {
            this.support_hours = support_hours;
        }
    }
}
