package models;

import enums.UserRole;

public class Customer extends User {
    
    public Customer() {
        super();
        this.role = UserRole.CUSTOMER;
    }
    
    public Customer(String id, String username, String password, 
                   String fullName, String email, String phone) {
        super(id, username, password, UserRole.CUSTOMER, fullName, email, phone);
    }
    
    @Override
    public String getRoleDisplayName() {
        return "Customer";
    }
    
    @Override
    public String toString() {
        return id + "|" + fullName + "|" + phone + "|" + email + "|" + username + "|" + password;
    }
    
    public static Customer fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 6) {
            return new Customer(parts[0].trim(), parts[4].trim(), parts[5].trim(), 
                               parts[1].trim(), parts[3].trim(), parts[2].trim());
        }
        return null;
    }
}