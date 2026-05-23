package models;

public class Feedback {
    private String id;
    private String appointmentId;
    private String technicianId;
    private String content;
    private String date;  // Add this field
    
    // Constructors
    public Feedback() {}
    
    public Feedback(String id, String appointmentId, String technicianId, String content, String date) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.technicianId = technicianId;
        this.content = content;
        this.date = date;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    
    public String getTechnicianId() { return technicianId; }
    public void setTechnicianId(String technicianId) { this.technicianId = technicianId; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getDate() { return date; }  // Add getter
    public void setDate(String date) { this.date = date; }  // Add setter
    
    // For file storage
    public static Feedback fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 5) {
            Feedback note = new Feedback();
            note.setId(parts[0]);
            note.setAppointmentId(parts[1]);
            note.setTechnicianId(parts[2]);
            note.setContent(parts[3]);
            note.setDate(parts[4]);
            return note;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return id + "|" + appointmentId + "|" + (technicianId != null ? technicianId : "") + "|" 
               + content + "|" + date;
    }
}