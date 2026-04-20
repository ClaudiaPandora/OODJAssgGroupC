package models;

import enums.ServiceType;

public class Service {
    private ServiceType type;
    private double price;
    
    public Service() {
    }
    
    public Service(ServiceType type, double price) {
        this.type = type;
        this.price = price;
    }
    
    // Getters and Setters
    public ServiceType getType() {
        return type;
    }
    
    public void setType(ServiceType type) {
        this.type = type;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    @Override
    public String toString() {
        return type + "|" + price;
    }
    
    public static Service fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 2) {
            return new Service(
                ServiceType.valueOf(parts[0].trim()),
                Double.parseDouble(parts[1].trim())
            );
        }
        return null;
    }
}