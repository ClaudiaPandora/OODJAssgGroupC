package models;

import enums.ServiceType;
import enums.AppointmentStatus;

public class Appointment {
    private String id;
    private String customerId;
    private ServiceType serviceType;
    private String date;
    private String startTime;
    private int duration;
    private AppointmentStatus status;
    private String technicianId;
    private String counterStaffId;
    private double amount;
    
    public Appointment() {
    }
    
    public Appointment(String id, String customerId, ServiceType serviceType, 
                      String date, String startTime, int duration, 
                      AppointmentStatus status, String technicianId, 
                      String counterStaffId) {
        this.id = id;
        this.customerId = customerId;
        this.serviceType = serviceType;
        this.date = date;
        this.startTime = startTime;
        this.duration = duration;
        this.status = status;
        this.technicianId = technicianId;
        this.counterStaffId = counterStaffId;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public ServiceType getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getStartTime() {
        return startTime;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public AppointmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppointmentStatus status) {
        this.status = status;
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
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    @Override
    public String toString() {
        return id + "|" + customerId + "|" + serviceType + "|" + date + "|" + 
               startTime + "|" + duration + "|" + status + "|" + 
               (technicianId != null ? technicianId : "") + "|" + 
               (counterStaffId != null ? counterStaffId : "");
    }
    
    public static Appointment fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 9) {
            Appointment appointment = new Appointment();
            appointment.setId(parts[0].trim());
            appointment.setCustomerId(parts[1].trim());
            appointment.setServiceType(ServiceType.valueOf(parts[2].trim()));
            appointment.setDate(parts[3].trim());
            appointment.setStartTime(parts[4].trim());
            appointment.setDuration(Integer.parseInt(parts[5].trim()));
            appointment.setStatus(AppointmentStatus.valueOf(parts[6].trim()));
            appointment.setTechnicianId(parts[7].trim().isEmpty() ? null : parts[7].trim());
            appointment.setCounterStaffId(parts[8].trim().isEmpty() ? null : parts[8].trim());
            return appointment;
        }
        return null;
    }
}