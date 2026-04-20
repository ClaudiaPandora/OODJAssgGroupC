package models;

import enums.UserRole;

public class Manager extends User {
    
    public Manager() {
        super();
        this.role = UserRole.MANAGER;
    }
    
    public Manager(String id, String username, String password, 
                   String fullName, String email, String phone) {
        super(id, username, password, UserRole.MANAGER, fullName, email, phone);
    }
    
    @Override
    public String getRoleDisplayName() {
        return "Manager";
    }
    
    @Override
    public String toString() {
        return id + "|" + fullName + "|" + phone + "|" + email + "|" + username + "|" + password;
    }
    
    public static Manager fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 6) {
            return new Manager(parts[0].trim(), parts[4].trim(), parts[5].trim(), 
                              parts[1].trim(), parts[3].trim(), parts[2].trim());
        }
        return null;
    }
}