package models;

import enums.UserRole;

public class Technician extends User {
    
    public Technician() {
        super();
        this.role = UserRole.TECHNICIAN;
    }
    
    public Technician(String id, String username, String password, 
                     String fullName, String email, String phone) {
        super(id, username, password, UserRole.TECHNICIAN, fullName, email, phone);
    }
    
    @Override
    public String getRoleDisplayName() {
        return "Technician";
    }
    
    @Override
    public String toString() {
        return id + "|" + fullName + "|" + phone + "|" + email + "|" + username + "|" + password;
    }
    
    public static Technician fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 6) {
            return new Technician(parts[0].trim(), parts[4].trim(), parts[5].trim(), 
                                 parts[1].trim(), parts[3].trim(), parts[2].trim());
        }
        return null;
    }
}