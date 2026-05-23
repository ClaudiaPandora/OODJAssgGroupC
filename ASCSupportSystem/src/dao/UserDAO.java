package dao;

import models.*;
import enums.UserRole;
import utils.FileUtils;
import java.util.*;

public class UserDAO implements FileDAO<User> {
    
    @Override
    public List<User> readAll() {
        List<User> users = new ArrayList<>();
        users.addAll(readManagers());
        users.addAll(readCounterStaff());
        users.addAll(readTechnicians());
        users.addAll(readCustomers());
        return users;
    }
    
    public List<Manager> readManagers() {
        List<Manager> managers = new ArrayList<>();
        List<String> lines = FileUtils.readLines("src/data/managers.txt");
        
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 6) {
                Manager m = new Manager();
                m.setId(parts[0].trim());
                m.setFullName(parts[1].trim());
                m.setPhone(parts[2].trim());
                m.setEmail(parts[3].trim());
                m.setUsername(parts[4].trim());
                m.setPassword(parts[5].trim());
                managers.add(m);
            }
        }
        return managers;
    }
    
    public List<CounterStaff> readCounterStaff() {
        List<CounterStaff> staff = new ArrayList<>();
        List<String> lines = FileUtils.readLines("src/data/counter_staff.txt");
        
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 6) {
                CounterStaff c = new CounterStaff();
                c.setId(parts[0].trim());
                c.setFullName(parts[1].trim());
                c.setPhone(parts[2].trim());
                c.setEmail(parts[3].trim());
                c.setUsername(parts[4].trim());
                c.setPassword(parts[5].trim());
                staff.add(c);
            }
        }
        return staff;
    }
    
    public List<Technician> readTechnicians() {
        List<Technician> techs = new ArrayList<>();
        List<String> lines = FileUtils.readLines("src/data/technicians.txt");
        
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 6) {
                Technician t = new Technician();
                t.setId(parts[0].trim());
                t.setFullName(parts[1].trim());
                t.setPhone(parts[2].trim());
                t.setEmail(parts[3].trim());
                t.setUsername(parts[4].trim());
                t.setPassword(parts[5].trim());
                techs.add(t);
            }
        }
        return techs;
    }
    
    public List<Customer> readCustomers() {
        List<Customer> customers = new ArrayList<>();
        List<String> lines = FileUtils.readLines("src/data/customers.txt");
        
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 6) {
                Customer c = new Customer();
                c.setId(parts[0].trim());
                c.setFullName(parts[1].trim());
                c.setPhone(parts[2].trim());
                c.setEmail(parts[3].trim());
                c.setUsername(parts[4].trim());
                c.setPassword(parts[5].trim());
                customers.add(c);
            }
        }
        return customers;
    }
    
    @Override
    public User findById(String id) {
        for (User u : readAll()) {
            if (u.getId().equals(id)) {
                return u;
            }
        }
        return null;
    }
    
    public User findByUsername(String username) {
        for (Manager m : readManagers()) {
            if (m.getUsername().equals(username)) {
                return m;
            }
        }
        for (CounterStaff c : readCounterStaff()) {
            if (c.getUsername().equals(username)) {
                return c;
            }
        }
        for (Technician t : readTechnicians()) {
            if (t.getUsername().equals(username)) {
                return t;
            }
        }
        for (Customer c : readCustomers()) {
            if (c.getUsername().equals(username)) {
                return c;
            }
        }
        return null;
    }
    
    public Customer findCustomerById(String id) {
        for (Customer c : readCustomers()) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }
    
    // ADD THIS METHOD - Find Technician by ID
    public Technician findTechnicianById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        for (Technician t : readTechnicians()) {
            if (t.getId().equals(id)) {
                return t;
            }
        }
        return null;
    }
    
    // ADD THIS METHOD - Find Counter Staff by ID
    public CounterStaff findCounterStaffById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        for (CounterStaff cs : readCounterStaff()) {
            if (cs.getId().equals(id)) {
                return cs;
            }
        }
        return null;
    }
    
    // ADD THIS METHOD - Find Manager by ID
    public Manager findManagerById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        for (Manager m : readManagers()) {
            if (m.getId().equals(id)) {
                return m;
            }
        }
        return null;
    }
    
    private String generateNewId(UserRole role) {
        List<? extends User> users;
        String prefix;
        
        switch (role) {
            case MANAGER:
                users = readManagers();
                prefix = "M";
                break;
            case COUNTER_STAFF:
                users = readCounterStaff();
                prefix = "C";
                break;
            case TECHNICIAN:
                users = readTechnicians();
                prefix = "T";
                break;
            case CUSTOMER:
                users = readCustomers();
                prefix = "U";
                break;
            default:
                return "000";
        }
        
        int maxNum = 0;
        for (User user : users) {
            String id = user.getId();
            if (id != null && id.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        
        return String.format("%s%03d", prefix, maxNum + 1);
    }
    
    @Override
    public boolean save(User user) {
        String filePath = getFilePathByRole(user.getRole());
        if (filePath.isEmpty()) return false;
        
        // Generate new ID if not set
        if (user.getId() == null || user.getId().isEmpty()) {
            String newId = generateNewId(user.getRole());
            user.setId(newId);
        }
        
        // Read existing lines
        List<String> lines = FileUtils.readLines(filePath);
        
        // Create new line
        String newLine = user.getId() + "|" +
                        user.getFullName() + "|" +
                        user.getPhone() + "|" +
                        user.getEmail() + "|" +
                        user.getUsername() + "|" +
                        user.getPassword();
        
        // Add new line
        lines.add(newLine);
        
        // Write back to file
        return FileUtils.writeLines(filePath, lines);
    }
    
    @Override
    public boolean update(User user) {
        String filePath = getFilePathByRole(user.getRole());
        if (filePath.isEmpty()) return false;
        
        List<String> lines = FileUtils.readLines(filePath);
        List<String> updatedLines = new ArrayList<>();
        boolean found = false;
        
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 6 && parts[0].trim().equals(user.getId())) {
                // Update this line
                String updatedLine = user.getId() + "|" +
                                    user.getFullName() + "|" +
                                    user.getPhone() + "|" +
                                    user.getEmail() + "|" +
                                    user.getUsername() + "|" +
                                    user.getPassword();
                updatedLines.add(updatedLine);
                found = true;
            } else {
                updatedLines.add(line);
            }
        }
        
        if (found) {
            return FileUtils.writeLines(filePath, updatedLines);
        }
        return false;
    }
    
    @Override
    public boolean delete(String id) {
        User user = findById(id);
        if (user == null) return false;
        
        String filePath = getFilePathByRole(user.getRole());
        if (filePath.isEmpty()) return false;
        
        List<String> lines = FileUtils.readLines(filePath);
        List<String> updatedLines = new ArrayList<>();
        
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 6 && !parts[0].trim().equals(id)) {
                updatedLines.add(line);
            }
        }
        
        return FileUtils.writeLines(filePath, updatedLines);
    }
    
    private String getFilePathByRole(UserRole role) {
        switch (role) {
            case MANAGER:
                return "src/data/managers.txt";
            case COUNTER_STAFF:
                return "src/data/counter_staff.txt";
            case TECHNICIAN:
                return "src/data/technicians.txt";
            case CUSTOMER:
                return "src/data/customers.txt";
            default:
                return "";
        }
    }
    
    @Override
    public String getFileName() {
        return "";
    }
}