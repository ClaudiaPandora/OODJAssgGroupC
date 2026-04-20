package models;

import enums.PaymentMethod;

public class Payment {
    private String id;
    private String appointmentId;
    private double amount;
    private String paymentDate;
    private PaymentMethod paymentMethod;
    
    public Payment() {
    }
    
    public Payment(String id, String appointmentId, double amount, 
                   String paymentDate, PaymentMethod paymentMethod) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }
    
    // Getters and Setters
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
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    @Override
    public String toString() {
        return id + "|" + appointmentId + "|" + amount + "|" + paymentDate + "|" + paymentMethod;
    }
    
    public static Payment fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 5) {
            return new Payment(
                parts[0].trim(),
                parts[1].trim(),
                Double.parseDouble(parts[2].trim()),
                parts[3].trim(),
                PaymentMethod.valueOf(parts[4].trim())
            );
        }
        return null;
    }
}