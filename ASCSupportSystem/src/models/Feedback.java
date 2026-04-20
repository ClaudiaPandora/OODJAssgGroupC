package models;

public class Feedback {
    private String id;
    private String appointmentId;
    private String technicianId;
    private String content;
    private int rating;
    
    public Feedback() {
        this.rating = 5;
    }
    
    public Feedback(String id, String appointmentId, String technicianId, String content, int rating) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.technicianId = technicianId;
        this.content = content;
        this.rating = rating;
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
    
    public String getTechnicianId() {
        return technicianId;
    }
    
    public void setTechnicianId(String technicianId) {
        this.technicianId = technicianId;
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
        return id + "|" + appointmentId + "|" + technicianId + "|" + content + "|" + rating;
    }
}