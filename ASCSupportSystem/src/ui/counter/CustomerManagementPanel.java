package ui.counter;

import dao.UserDAO;
import models.Customer;
import ui.common.BasePanel;
import utils.ValidationUtils;
import exceptions.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class CustomerManagementPanel extends BasePanel {
    
    private UserDAO userDAO;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private JLabel statsLabel;
    private JLabel emptyLabel;
    private JPanel tableSwitcher;
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
        rowSorter = new TableRowSorter<>(tableModel);
        rowSorter.setSortsOnUpdates(false);
        for (int i = 0; i < columns.length; i++) {
            rowSorter.setSortable(i, false);
        }
        customerTable.setRowSorter(rowSorter);
        
        setupTableStyle();
        
        // Search field
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(9, 12, 9, 12)
        ));
        
        // Stats label
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(new Color(107, 114, 128));
        
        // Empty label
        emptyLabel = new JLabel("No customers match the current search.");
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emptyLabel.setForeground(new Color(107, 114, 128));
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Buttons
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
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(PANEL_BG);
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createTableCard(), BorderLayout.CENTER);
        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(PANEL_BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(PANEL_BG);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(PANEL_BG);

        JLabel titleLabel = new JLabel("Customer Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(titleLabel);

        titleRow.add(titleBlock, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        actions.setBackground(PANEL_BG);
        actions.add(addButton);
        actions.add(refreshButton);
        titleRow.add(actions, BorderLayout.EAST);

        wrapper.add(titleRow, BorderLayout.NORTH);
        wrapper.add(createToolbar(), BorderLayout.CENTER);

        return wrapper;
    }
    
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        controlsPanel.setBackground(Color.WHITE);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setPreferredSize(new Dimension(430, 34));

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(new Color(31, 41, 55));
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        controlsPanel.add(searchPanel);
        toolbar.add(controlsPanel, BorderLayout.WEST);

        return toolbar;
    }
    
    private JPanel createTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createLineBorder(CARD_BORDER));

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.add(emptyLabel);

        tableSwitcher = new JPanel(new CardLayout());
        tableSwitcher.add(scrollPane, "TABLE");
        tableSwitcher.add(emptyPanel, "EMPTY");

        tableCard.add(tableSwitcher, BorderLayout.CENTER);
        return tableCard;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(PANEL_BG);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Left side - stats label
        footer.add(statsLabel, BorderLayout.WEST);
        
        // Right side - action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(PANEL_BG);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        footer.add(buttonPanel, BorderLayout.EAST);
        
        return footer;
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
        
        applySearchFilter();
        updateStatsLabel();
        updateTableVisibility();
        customerTable.revalidate();
        customerTable.repaint();
    }
    
    private void applySearchFilter() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        
        rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                boolean searchMatches = search.isEmpty();
                for (int i = 0; i < 5 && !searchMatches; i++) {
                    Object value = entry.getValue(i);
                    searchMatches = value != null && value.toString().toLowerCase().contains(search);
                }
                return searchMatches;
            }
        });
    }
    
    private void updateTableVisibility() {
        if (tableSwitcher == null) {
            return;
        }
        
        CardLayout layout = (CardLayout) tableSwitcher.getLayout();
        layout.show(tableSwitcher, customerTable.getRowCount() == 0 ? "EMPTY" : "TABLE");
    }
    
    private void updateStatsLabel() {
        List<Customer> customers = userDAO.readCustomers();
        int totalCustomers = customers.size();
        int displayedCustomers = customerTable.getRowCount();
        
        statsLabel.setText(String.format(
            "Total Customers: %d    Displaying: %d",
            totalCustomers, displayedCustomers
        ));
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
        button.setPreferredSize(new Dimension(120, 36));
        
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
        
        // Search field document listener for real-time filtering
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtersChanged();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                filtersChanged();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                filtersChanged();
            }
            private void filtersChanged() {
                applySearchFilter();
                updateStatsLabel();
                updateTableVisibility();
            }
        });
        
        // Refresh button
        refreshButton.addActionListener(e -> {
            refreshData();
            JOptionPane.showMessageDialog(this, "Customer list has been refreshed.", 
                "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Add button
        addButton.addActionListener(e -> showCustomerDialog(null));
        
        // Edit button
        editButton.addActionListener(e -> editSelectedCustomer());
        
        // Delete button
        deleteButton.addActionListener(e -> deleteSelectedCustomer());
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