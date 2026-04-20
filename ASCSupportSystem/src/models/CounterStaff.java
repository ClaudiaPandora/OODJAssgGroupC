package models;

import enums.UserRole;

public class CounterStaff extends User {
    
    public CounterStaff() {
        super();
        this.role = UserRole.COUNTER_STAFF;
    }
    
    public CounterStaff(String id, String username, String password, 
                        String fullName, String email, String phone) {
        super(id, username, password, UserRole.COUNTER_STAFF, fullName, email, phone);
    }
    
    @Override
    public String getRoleDisplayName() {
        return "Counter Staff";
    }
    
    @Override
    public String toString() {
        return id + "|" + fullName + "|" + phone + "|" + email + "|" + username + "|" + password;
    }
    
    public static CounterStaff fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 6) {
            return new CounterStaff(parts[0].trim(), parts[4].trim(), parts[5].trim(), 
                                   parts[1].trim(), parts[3].trim(), parts[2].trim());
        }
        return null;
    }
}