import dao.UserDAO;
import models.User;

public class TestData {
    public static void main(String[] args) {
        System.out.println("Testing data files...");
        
        UserDAO userDAO = new UserDAO();
        
        System.out.println("\n=== All Users ===");
        for (User u : userDAO.readAll()) {
            System.out.println("ID: " + u.getId());
            System.out.println("Username: " + u.getUsername());
            System.out.println("Password: " + u.getPassword());
            System.out.println("Name: " + u.getFullName());
            System.out.println("Role: " + u.getRole());
            System.out.println("---");
        }
        
        System.out.println("\n=== Testing Login ===");
        String[] usernames = {"manager1", "counter1", "tech1", "cust1"};
        
        for (String username : usernames) {
            User u = userDAO.findByUsername(username);
            if (u != null) {
                System.out.println("Found: " + username + " | Password: " + u.getPassword());
            } else {
                System.out.println("NOT FOUND: " + username);
            }
        }
    }
}