package ui.manager;

import dao.UserDAO;
import enums.Mode;
import enums.UserRole;
import models.*;
import javax.swing.*;
import java.awt.*;

public class StaffFormPanel extends JDialog {
    
    // Fields
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    
    // Data
    private UserDAO userDAO;
    private User user;
    private Mode mode;
    private Runnable onSuccess;
    private String originalUsername;
    private String originalRole; // Store original role for update mode
    
    // Colors
    private Color NAVY_BLUE = new Color(0, 0, 128);
    private Color YELLOW = new Color(252, 202, 12);
    
    public StaffFormPanel(Frame parent, Mode mode, User user, Runnable onSuccess) {
        super(parent, true);
        this.userDAO = new UserDAO();
        this.mode = mode;
        this.user = user;
        this.onSuccess = onSuccess;
        
        setSize(520, 580);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        if (mode == Mode.CREATE) {
            setTitle("Register New Staff");
        } else {
            setTitle("Edit Staff Information");
        }
        
        initComponents();
        setupLayout();
        
        if (mode == Mode.UPDATE && user != null) {
            populateFields();
        }
    }
    
    private void initComponents() {
        fullNameField = new JTextField(20);
        emailField = new JTextField(20);
        phoneField = new JTextField(20);
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        roleComboBox = new JComboBox<>(new String[]{"MANAGER", "COUNTER STAFF", "TECHNICIAN"});
        
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);
        fullNameField.setFont(fieldFont);
        emailField.setFont(fieldFont);
        phoneField.setFont(fieldFont);
        usernameField.setFont(fieldFont);
        passwordField.setFont(fieldFont);
        roleComboBox.setFont(fieldFont);
        
        styleTextField(fullNameField);
        styleTextField(emailField);
        styleTextField(phoneField);
        styleTextField(usernameField);
        stylePasswordField(passwordField);
        styleComboBox(roleComboBox);
        
        // Add placeholder hints
        fullNameField.setToolTipText("Enter full name (e.g., John Doe)");
        emailField.setToolTipText("Enter valid email (e.g., john@example.com)");
        phoneField.setToolTipText("Enter phone number (10-12 digits)");
        usernameField.setToolTipText("Enter unique username");
        passwordField.setToolTipText("Enter password (min 6 characters)");
        
        usernameField.setEditable(true);
        usernameField.setBackground(Color.WHITE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerContainer = new JPanel(new BorderLayout());
        JPanel dialogHeader = new JPanel(new BorderLayout());
        dialogHeader.setBackground(NAVY_BLUE);
        dialogHeader.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        String title = (mode == Mode.CREATE) ? "Register New Staff" : "Edit Staff Information";
        JLabel headerTitle = new JLabel(title);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerTitle.setForeground(Color.WHITE);
        dialogHeader.add(headerTitle, BorderLayout.WEST);
        
        JLabel headerSubtitle = new JLabel(mode == Mode.CREATE ? "Add a new staff member to the system" : "Update staff information");
        headerSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        headerSubtitle.setForeground(new Color(200, 200, 200));
        dialogHeader.add(headerSubtitle, BorderLayout.SOUTH);
        
        headerContainer.add(dialogHeader, BorderLayout.CENTER);
        
        JPanel yellowLine = new JPanel();
        yellowLine.setBackground(YELLOW);
        yellowLine.setPreferredSize(new Dimension(520, 4));
        headerContainer.add(yellowLine, BorderLayout.SOUTH);
        add(headerContainer, BorderLayout.NORTH);
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        // Full Name
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("Full Name *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(fullNameField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("Email *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(emailField, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("Phone *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(phoneField, gbc);
        
        // Username
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("Username *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("Password *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(passwordField, gbc);
        
        // Role
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("Role *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(roleComboBox, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        
        String btnText = (mode == Mode.CREATE) ? "REGISTER STAFF" : "UPDATE STAFF";
        Color btnColor = (mode == Mode.CREATE) ? new Color(59, 130, 246) : new Color(34, 197, 94);
        
        JButton actionBtn = createButton(btnText, btnColor);
        actionBtn.addActionListener(e -> handleSubmit());
        
        JButton cancelBtn = createButton("CANCEL", new Color(100, 100, 100));
        cancelBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(actionBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(60, 60, 60));
        return label;
    }
    
    private void styleTextField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setPreferredSize(new Dimension(300, 42));
    }
    
    private void stylePasswordField(JPasswordField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setPreferredSize(new Dimension(300, 42));
    }
    
    private void styleComboBox(JComboBox<String> combo) {
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        combo.setPreferredSize(new Dimension(300, 42));
        combo.setBackground(Color.WHITE);
    }
    
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void populateFields() {
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        usernameField.setText(user.getUsername());
        passwordField.setText(user.getPassword());
        originalUsername = user.getUsername();
        
        // Store original role for update mode
        if (user.getRole() == UserRole.MANAGER) {
            originalRole = "MANAGER";
            roleComboBox.setSelectedItem("MANAGER");
        } else if (user.getRole() == UserRole.COUNTER_STAFF) {
            originalRole = "COUNTER STAFF";
            roleComboBox.setSelectedItem("COUNTER STAFF");
        } else {
            originalRole = "TECHNICIAN";
            roleComboBox.setSelectedItem("TECHNICIAN");
        }
    }
    
    private void handleSubmit() {
        String validationMsg = validateFields();
        if (validationMsg != null) {
            JOptionPane.showMessageDialog(this, validationMsg, "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (mode == Mode.CREATE) {
            handleCreate();
        } else {
            handleUpdate();
        }
    }
    
    private String validateFields() {
        if (getFullName().isEmpty()) {
            return "Full Name is required.";
        }
        if (getEmail().isEmpty()) {
            return "Email is required.";
        }
        if (getPhone().isEmpty()) {
            return "Phone number is required.";
        }
        if (getUsername().isEmpty()) {
            return "Username is required.";
        }
        if (getPassword().isEmpty()) {
            return "Password is required.";
        }
        if (getPassword().length() < 6) {
            return "Password must be at least 6 characters.";
        }
        if (!getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "Please enter a valid email address.";
        }
        if (!getPhone().matches("\\d{10,12}")) {
            return "Please enter a valid phone number (10-12 digits).";
        }
        return null;
    }
    
    private void handleCreate() {
        if (userDAO.findByUsername(getUsername()) != null) {
            JOptionPane.showMessageDialog(this, 
                "Username '" + getUsername() + "' already exists.\nPlease choose a different username.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        User newUser = createUserFromForm();
        if (newUser != null && userDAO.save(newUser)) {
            JOptionPane.showMessageDialog(this, 
                String.format("Staff registered successfully!\n\nID: %s\nName: %s\nRole: %s", 
                    newUser.getId(), newUser.getFullName(), newUser.getRoleDisplayName()), 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            if (onSuccess != null) onSuccess.run();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleUpdate() {
        String newUsername = getUsername();
        String newRole = getRole();
        
        // Check if username changed and already exists
        if (!newUsername.equals(originalUsername) && userDAO.findByUsername(newUsername) != null) {
            JOptionPane.showMessageDialog(this, 
                "Username '" + newUsername + "' already exists.\nPlease choose a different username.", 
                "Update Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check if role is being changed
        if (!newRole.equals(originalRole)) {
            String originalRoleDisplay = getRoleDisplayName(originalRole);
            String newRoleDisplay = getRoleDisplayName(newRole);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "WARNING: You are trying to change the role from " + originalRoleDisplay + " to " + newRoleDisplay + ".\n\n" +
                    "The Staff ID is permanently tied to the role and CANNOT be changed.\n\n" +
                    "To change a staff member's role, please:\n" +
                    "1. Delete this staff member\n" +
                    "2. Register a new staff member with the correct role\n\n" +
                    "The role will remain as " + originalRoleDisplay + ".\n\n" +
                    "Do you want to continue updating other information?",
                    "Role Change Not Allowed",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            
            // Keep original role
            newRole = originalRole;
            roleComboBox.setSelectedItem(originalRole);
        }
        
        // Update user fields
        user.setFullName(getFullName());
        user.setEmail(getEmail());
        user.setPhone(getPhone());
        user.setUsername(newUsername);
        user.setPassword(getPassword());
        
        // Set the role (original role if role change was attempted)
        if (newRole.equals("MANAGER")) {
            user.setRole(UserRole.MANAGER);
        } else if (newRole.equals("COUNTER STAFF")) {
            user.setRole(UserRole.COUNTER_STAFF);
        } else {
            user.setRole(UserRole.TECHNICIAN);
        }
        
        if (userDAO.update(user)) {
            JOptionPane.showMessageDialog(this, 
                "Staff information updated successfully!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            if (onSuccess != null) onSuccess.run();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update staff information.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getRoleDisplayName(String role) {
        if (role.equals("MANAGER")) {
            return "Manager";
        } else if (role.equals("COUNTER STAFF")) {
            return "Counter Staff";
        } else {
            return "Technician";
        }
    }
    
    private User createUserFromForm() {
        String role = getRole();
        if (role.equals("MANAGER")) {
            return new Manager(null, getUsername(), getPassword(), getFullName(), getEmail(), getPhone());
        } else if (role.equals("COUNTER STAFF")) {
            return new CounterStaff(null, getUsername(), getPassword(), getFullName(), getEmail(), getPhone());
        } else {
            return new Technician(null, getUsername(), getPassword(), getFullName(), getEmail(), getPhone());
        }
    }
    
    // Getters
    public String getFullName() { return fullNameField.getText().trim(); }
    public String getEmail() { return emailField.getText().trim(); }
    public String getPhone() { return phoneField.getText().trim(); }
    public String getUsername() { return usernameField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public String getRole() { return (String) roleComboBox.getSelectedItem(); }
}