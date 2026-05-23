package ui.counter;

import dao.UserDAO;
import models.Customer;
import ui.common.BasePanel;
import utils.ValidationUtils;
import exceptions.ValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class CustomerManagementPanel extends BasePanel {
    
    private UserDAO userDAO;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    
    private final Color CARD_BORDER = new Color(226, 232, 240);
    private final Color LIGHT_BG = new Color(248, 250, 252);
    private final Color TABLE_HEADER_BG = new Color(244, 246, 250);
    private final Color ROW_SEPARATOR = new Color(231, 235, 240);
    private final Color GREEN = new Color(34, 197, 94);
    private final Color BLUE = new Color(59, 130, 246);
    private final Color RED = new Color(220, 38, 38);
    
    public CustomerManagementPanel() {
        this.userDAO = new UserDAO();
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initializeComponents();
        setupLayout();
        addEventHandlers();
        loadCustomerData();
    }
    
    private void refreshData() {
        this.userDAO = new UserDAO();
        loadCustomerData();
    }
    
    @Override
    protected void initializeComponents() {
        String[] columns = {"ID", "Name", "Email", "Phone", "Username"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(tableModel);
        setupTableStyle();
        
        addButton = createStyledButton("+ New Customer", NAVY_BLUE);
        editButton = createStyledButton("Edit Customer", BLUE);
        deleteButton = createStyledButton("Delete Customer", RED);
        refreshButton = createStyledButton("Refresh", GREEN);
    }
    
    private void setupTableStyle() {
        customerTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        customerTable.setRowHeight(45);
        customerTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        customerTable.setFillsViewportHeight(true);
        customerTable.setFocusable(false);
        customerTable.setRowSelectionAllowed(true);
        customerTable.setColumnSelectionAllowed(false);
        customerTable.setShowVerticalLines(false);
        customerTable.setShowHorizontalLines(false);
        customerTable.setIntercellSpacing(new Dimension(0, 0));
        customerTable.setSelectionBackground(new Color(232, 240, 254));
        customerTable.setSelectionForeground(new Color(31, 41, 55));
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JTableHeader header = customerTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(new Color(31, 41, 55));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setBackground(TABLE_HEADER_BG);
                return label;
            }
        };
        
        for (int i = 0; i < customerTable.getColumnModel().getColumnCount(); i++) {
            customerTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        
        customerTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        customerTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        customerTable.getColumnModel().getColumn(2).setPreferredWidth(220);
        customerTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        customerTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        
        customerTable.setDefaultRenderer(Object.class, new TableCellRenderer());
    }
    
    @Override
    protected void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(PANEL_BG);
        
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        panel.setPreferredSize(new Dimension(0, 45));
        
        JLabel titleLabel = new JLabel("Customer Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        panel.add(titleLabel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(PANEL_BG);
        
        refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        refreshButton.setBackground(GREEN);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setOpaque(true);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        refreshButton.setPreferredSize(new Dimension(90, 34));
        refreshButton.addActionListener(e -> {
            refreshData();
            JOptionPane.showMessageDialog(this, "Customer list has been refreshed from file.", 
                "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
        });
        
        addButton = new JButton("+ New Customer");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        addButton.setBackground(NAVY_BLUE);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setOpaque(true);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        addButton.setPreferredSize(new Dimension(130, 34));
        addButton.addActionListener(e -> showCustomerDialog(null));
        
        // Hover effects
        refreshButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { refreshButton.setBackground(GREEN.darker()); }
            @Override
            public void mouseExited(MouseEvent e) { refreshButton.setBackground(GREEN); }
        });
        
        addButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { addButton.setBackground(NAVY_BLUE.darker()); }
            @Override
            public void mouseExited(MouseEvent e) { addButton.setBackground(NAVY_BLUE); }
        });
        
        rightPanel.add(addButton);
        rightPanel.add(refreshButton);
        
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        
        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionPanel.setBackground(LIGHT_BG);
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BORDER));
        
        // Create Edit button with proper sizing
        editButton = new JButton("Edit Customer");
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        editButton.setBackground(BLUE);
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);
        editButton.setBorderPainted(false);
        editButton.setOpaque(true);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        editButton.setPreferredSize(new Dimension(120, 34));
        editButton.addActionListener(e -> editSelectedCustomer());
        
        // Create Delete button with proper sizing
        deleteButton = new JButton("Delete Customer");
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        deleteButton.setBackground(RED);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorderPainted(false);
        deleteButton.setOpaque(true);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        deleteButton.setPreferredSize(new Dimension(130, 34));
        deleteButton.addActionListener(e -> deleteSelectedCustomer());
        
        // Hover effects for Edit button
        editButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { editButton.setBackground(BLUE.darker()); }
            @Override
            public void mouseExited(MouseEvent e) { editButton.setBackground(BLUE); }
        });
        
        // Hover effects for Delete button
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { deleteButton.setBackground(RED.darker()); }
            @Override
            public void mouseExited(MouseEvent e) { deleteButton.setBackground(RED); }
        });
        
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void showCustomerDialog(Customer existingCustomer) {
        CustomerDialog dialog = new CustomerDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            existingCustomer,
            userDAO,
            this::refreshData
        );
        dialog.setVisible(true);
    }
    
    private void loadCustomerData() {
        tableModel.setRowCount(0);
        List<Customer> customers = userDAO.readCustomers();
        
        for (Customer customer : customers) {
            Object[] row = {
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getUsername()
            };
            tableModel.addRow(row);
        }
    }
    
    private Customer getSelectedCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        int modelRow = customerTable.convertRowIndexToModel(selectedRow);
        String customerId = (String) tableModel.getValueAt(modelRow, 0);
        List<Customer> customers = userDAO.readCustomers();
        for (Customer c : customers) {
            if (c.getId().equals(customerId)) {
                return c;
            }
        }
        return null;
    }
    
    private void editSelectedCustomer() {
        Customer customer = getSelectedCustomer();
        if (customer == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer to edit.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showCustomerDialog(customer);
    }
    
    private void deleteSelectedCustomer() {
        Customer customer = getSelectedCustomer();
        if (customer == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete " + customer.getFullName() + "?\nThis action cannot be undone.", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = userDAO.delete(customer.getId());
            if (success) {
                refreshData();
                JOptionPane.showMessageDialog(this, "Customer deleted successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete customer.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setPreferredSize(new Dimension(110, 34));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    @Override
    protected void addEventHandlers() {
        customerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedCustomer();
                }
            }
        });
    }
    
    class TableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            JLabel label = (JLabel) c;
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
                BorderFactory.createEmptyBorder(10, 18, 10, 10)
            ));
            
            if (!isSelected) {
                label.setBackground(Color.WHITE);
                label.setForeground(new Color(31, 41, 55));
            } else {
                label.setBackground(new Color(232, 240, 254));
                label.setForeground(new Color(31, 41, 55));
            }
            
            return label;
        }
    }
}

//Separate dialog class for customer form
class CustomerDialog extends JDialog {
 
 // Fields
 private JTextField fullNameField;
 private JTextField emailField;
 private JTextField phoneField;
 private JTextField usernameField;
 private JPasswordField passwordField;
 
 // Data
 private Customer existingCustomer;
 private UserDAO userDAO;
 private Runnable onSuccess;
 private String originalUsername;
 
 // Colors
 private Color NAVY_BLUE = new Color(31, 66, 99);
 private Color YELLOW = new Color(252, 202, 12);
 
 public CustomerDialog(Frame parent, Customer existingCustomer, UserDAO userDAO, Runnable onSuccess) {
     super(parent, true);
     this.existingCustomer = existingCustomer;
     this.userDAO = userDAO;
     this.onSuccess = onSuccess;
     
     setSize(520, existingCustomer == null ? 540 : 480);
     setLocationRelativeTo(parent);
     setResizable(false);
     setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
     
     if (existingCustomer == null) {
         setTitle("Register New Customer");
     } else {
         setTitle("Edit Customer Information");
     }
     
     initComponents();
     setupLayout();
     
     if (existingCustomer != null) {
         populateFields();
     }
 }
 
 private void initComponents() {
     Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);
     
     fullNameField = new JTextField(20);
     emailField = new JTextField(20);
     phoneField = new JTextField(20);
     usernameField = new JTextField(20);
     passwordField = new JPasswordField(20);
     
     fullNameField.setFont(fieldFont);
     emailField.setFont(fieldFont);
     phoneField.setFont(fieldFont);
     usernameField.setFont(fieldFont);
     passwordField.setFont(fieldFont);
     
     styleTextField(fullNameField);
     styleTextField(emailField);
     styleTextField(phoneField);
     styleTextField(usernameField);
     stylePasswordField(passwordField);
     
     // Add placeholder hints
     fullNameField.setToolTipText("Enter full name (e.g., John Doe)");
     emailField.setToolTipText("Enter valid email (e.g., john@example.com)");
     phoneField.setToolTipText("Enter phone number (10-12 digits)");
     usernameField.setToolTipText("Enter unique username");
     passwordField.setToolTipText("Enter password (min 6 characters)");
     
     // For edit mode, make username non-editable
     if (existingCustomer != null) {
         usernameField.setEditable(false);
         usernameField.setBackground(new Color(245, 245, 245));
     }
 }
 
 private void styleTextField(JTextField field) {
     field.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
         BorderFactory.createEmptyBorder(10, 12, 10, 12)
     ));
     field.setPreferredSize(new Dimension(300, 42));
     field.setBackground(Color.WHITE);
 }
 
 private void stylePasswordField(JPasswordField field) {
     field.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
         BorderFactory.createEmptyBorder(10, 12, 10, 12)
     ));
     field.setPreferredSize(new Dimension(300, 42));
     field.setBackground(Color.WHITE);
 }
 
 private void setupLayout() {
     setLayout(new BorderLayout());
     
     // Header
     JPanel headerContainer = new JPanel(new BorderLayout());
     JPanel dialogHeader = new JPanel(new BorderLayout());
     dialogHeader.setBackground(NAVY_BLUE);
     dialogHeader.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
     
     String title = (existingCustomer == null) ? "Register New Customer" : "Edit Customer Information";
     JLabel headerTitle = new JLabel(title);
     headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
     headerTitle.setForeground(Color.WHITE);
     dialogHeader.add(headerTitle, BorderLayout.WEST);
     
     JLabel headerSubtitle = new JLabel(existingCustomer == null ? "Add a new customer to the system" : "Update customer information");
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
     
     // Password (only show for new customers)
     if (existingCustomer == null) {
         gbc.gridx = 0; gbc.gridy = 4;
         gbc.weightx = 0.3;
         formPanel.add(createLabel("Password *"), gbc);
         gbc.gridx = 1;
         gbc.weightx = 0.7;
         formPanel.add(passwordField, gbc);
     }
     
     add(formPanel, BorderLayout.CENTER);
     
     // Button Panel
     JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
     buttonPanel.setBackground(Color.WHITE);
     buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
     
     String btnText = (existingCustomer == null) ? "REGISTER CUSTOMER" : "UPDATE CUSTOMER";
     Color btnColor = (existingCustomer == null) ? new Color(59, 130, 246) : new Color(34, 197, 94);
     
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
     if (existingCustomer == null) return;
     
     fullNameField.setText(existingCustomer.getFullName());
     emailField.setText(existingCustomer.getEmail());
     phoneField.setText(existingCustomer.getPhone());
     usernameField.setText(existingCustomer.getUsername());
     originalUsername = existingCustomer.getUsername();
 }
 
 private void handleSubmit() {
     String validationMsg = validateFields();
     if (validationMsg != null) {
         JOptionPane.showMessageDialog(this, validationMsg, "Validation Error", JOptionPane.WARNING_MESSAGE);
         return;
     }
     
     if (existingCustomer == null) {
         handleCreate();
     } else {
         handleUpdate();
     }
 }
 
 private String validateFields() {
     String fullName = fullNameField.getText().trim();
     String email = emailField.getText().trim();
     String phone = phoneField.getText().trim();
     String username = usernameField.getText().trim();
     
     if (fullName.isEmpty()) {
         return "Full Name is required.";
     }
     if (email.isEmpty()) {
         return "Email is required.";
     }
     if (phone.isEmpty()) {
         return "Phone number is required.";
     }
     if (username.isEmpty()) {
         return "Username is required.";
     }
     
     // Password validation only for new customers
     if (existingCustomer == null) {
         String password = new String(passwordField.getPassword());
         if (password.isEmpty()) {
             return "Password is required.";
         }
         if (password.length() < 6) {
             return "Password must be at least 6 characters.";
         }
     }
     
     // Email format validation
     if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
         return "Please enter a valid email address.";
     }
     
     // Phone number validation
     if (!phone.matches("\\d{10,12}")) {
         return "Please enter a valid phone number (10-12 digits).";
     }
     
     // Check for duplicate username (exclude current customer in edit mode)
     for (Customer c : userDAO.readCustomers()) {
         if (existingCustomer != null && c.getId().equals(existingCustomer.getId())) {
             continue;
         }
         if (c.getUsername().equals(username)) {
             return "Username already exists. Please choose a different username.";
         }
     }
     
     return null;
 }
 
 private void handleCreate() {
     try {
         Customer customer = new Customer();
         customer.setFullName(fullNameField.getText().trim());
         customer.setEmail(emailField.getText().trim());
         customer.setPhone(phoneField.getText().trim());
         customer.setUsername(usernameField.getText().trim());
         customer.setPassword(new String(passwordField.getPassword()));
         
         boolean success = userDAO.save(customer);
         
         if (success) {
             JOptionPane.showMessageDialog(this, 
                 String.format("Customer registered successfully!\n\nID: %s\nName: %s", 
                     customer.getId(), customer.getFullName()), 
                 "Success", 
                 JOptionPane.INFORMATION_MESSAGE);
             if (onSuccess != null) onSuccess.run();
             dispose();
         } else {
             JOptionPane.showMessageDialog(this, "Registration failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
         }
     } catch (Exception e) {
         JOptionPane.showMessageDialog(this, "Error creating customer: " + e.getMessage(), 
             "Error", JOptionPane.ERROR_MESSAGE);
     }
 }
 
 private void handleUpdate() {
     try {
         String newUsername = usernameField.getText().trim();
         
         // Check if username changed and already exists (should not happen since username is disabled)
         if (!newUsername.equals(originalUsername)) {
             for (Customer c : userDAO.readCustomers()) {
                 if (!c.getId().equals(existingCustomer.getId()) && c.getUsername().equals(newUsername)) {
                     JOptionPane.showMessageDialog(this, 
                         "Username already exists. Please choose a different username.", 
                         "Update Error", 
                         JOptionPane.ERROR_MESSAGE);
                     return;
                 }
             }
         }
         
         // Update customer fields (password remains unchanged)
         existingCustomer.setFullName(fullNameField.getText().trim());
         existingCustomer.setEmail(emailField.getText().trim());
         existingCustomer.setPhone(phoneField.getText().trim());
         existingCustomer.setUsername(newUsername);
         // Password not updated in edit mode
         
         boolean success = userDAO.update(existingCustomer);
         
         if (success) {
             JOptionPane.showMessageDialog(this, 
                 "Customer information updated successfully!", 
                 "Success", 
                 JOptionPane.INFORMATION_MESSAGE);
             if (onSuccess != null) onSuccess.run();
             dispose();
         } else {
             JOptionPane.showMessageDialog(this, "Failed to update customer information.", "Error", JOptionPane.ERROR_MESSAGE);
         }
     } catch (Exception e) {
         JOptionPane.showMessageDialog(this, "Error updating customer: " + e.getMessage(), 
             "Error", JOptionPane.ERROR_MESSAGE);
     }
 }
}