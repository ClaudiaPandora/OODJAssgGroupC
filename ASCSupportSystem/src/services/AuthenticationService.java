package services;

import dao.UserDAO;
import exceptions.AuthenticationException;
import models.User;

public class AuthenticationService {
    
    private UserDAO userDAO;
    private User currentUser;
    
    public AuthenticationService() {
        this.userDAO = new UserDAO();
        
        // Debug: Print all users on startup
        System.out.println("=== AuthenticationService Initialized ===");
        System.out.println("Total users found: " + userDAO.readAll().size());
        for (User u : userDAO.readAll()) {
            System.out.println("User: " + u.getUsername() + " | Password: " + u.getPassword() + " | Role: " + u.getRole());
        }
        System.out.println("=========================================");
    }
    
    public User login(String username, String password) throws AuthenticationException {
        System.out.println("Login attempt: username=" + username + ", password=" + password);
        
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username is required");
        }
        
        if (password == null || password.isEmpty()) {
            throw new AuthenticationException("Password is required");
        }
        
        User user = userDAO.findByUsername(username.trim());
        
        System.out.println("Found user: " + (user != null ? user.getUsername() : "null"));
        
        if (user == null) {
            throw new AuthenticationException("Invalid username or password");
        }
        
        System.out.println("Stored password: " + user.getPassword());
        System.out.println("Provided password: " + password);
        
        if (!password.equals(user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }
        
        this.currentUser = user;
        System.out.println("Login successful for: " + user.getFullName());
        return user;
    }
    
    public void logout() {
        this.currentUser = null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isAuthenticated() {
        return currentUser != null;
    }
}