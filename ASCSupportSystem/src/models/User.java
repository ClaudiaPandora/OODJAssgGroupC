package models;

import enums.UserRole;

public abstract class User {
    protected String id;
    protected String username;
    protected String password;
    protected UserRole role;
    protected String fullName;
    protected String email;
    protected String phone;
    
    public User() {
    }
    
    public User(String id, String username, String password, UserRole role, 
                String fullName, String email, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }
    
    // Abstract method to be implemented by subclasses
    public abstract String getRoleDisplayName();
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    @Override
    public String toString() {
        return id + "|" + fullName + "|" + phone + "|" + email + "|" + username + "|" + password;
    }
}