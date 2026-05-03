package models;

public class Comment {
    private String id;
    private String appointmentId;
    private String customerId;
    private String technicianId;
    private String counterStaffId;
    private String content;
    private int rating;

    public Comment() {
        this.rating = 5;
    }

    public Comment(String id, String appointmentId, String customerId,
                   String technicianId, String counterStaffId, String content) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.technicianId = technicianId;
        this.counterStaffId = counterStaffId;
        this.content = content;
        this.rating = 5;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(String technicianId) {
        this.technicianId = technicianId;
    }

    public String getCounterStaffId() {
        return counterStaffId;
    }

    public void setCounterStaffId(String counterStaffId) {
        this.counterStaffId = counterStaffId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return id + "|" + appointmentId + "|" + customerId + "|"
                + (technicianId != null ? technicianId : "") + "|"
                + (counterStaffId != null ? counterStaffId : "") + "|"
                + content + "|" + rating;
    }

    public static Comment fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 6) {
            Comment comment = new Comment(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim().isEmpty() ? null : parts[3].trim(),
                    parts[4].trim().isEmpty() ? null : parts[4].trim(),
                    parts[5].trim()
            );

            if (parts.length >= 7) {
                try {
                    comment.setRating(Integer.parseInt(parts[6].trim()));
                } catch (NumberFormatException e) {
                    comment.setRating(5);
                }
            }

            return comment;
        }
        return null;
    }
}
