package ui.counter;

import dao.AppointmentDAO;
import dao.UserDAO;
import dao.ServiceDAO;
import dao.PaymentDAO;
import enums.AppointmentStatus;
import enums.ServiceType;
import models.Appointment;
import models.Customer;
import models.Technician;
import models.User;
import models.Payment;
import ui.common.BasePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentPanel extends BasePanel {
    
    private AppointmentDAO appointmentDAO;
    private UserDAO userDAO;
    private ServiceDAO serviceDAO;
    private PaymentDAO paymentDAO;
    private User currentUser;
    
    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    
    private JButton addButton;
    private JButton refreshButton;
    private JButton editButton;
    private JButton cancelAppointmentButton;
    
    private List<Technician> technicians;
    private List<Customer> customers;
    
    private final Color CARD_BORDER = new Color(226, 232, 240);
    private final Color LIGHT_BG = new Color(248, 250, 252);
    private final Color TABLE_HEADER_BG = new Color(244, 246, 250);
    private final Color ROW_SEPARATOR = new Color(231, 235, 240);
    private final Color NAVY = new Color(31, 66, 99);
    
    public AppointmentPanel(User currentUser) {
        this.appointmentDAO = new AppointmentDAO();
        this.userDAO = new UserDAO();
        this.serviceDAO = new ServiceDAO();
        this.paymentDAO = new PaymentDAO();
        this.currentUser = currentUser;
        
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initializeComponents();
        setupLayout();
        addEventHandlers();
        loadAppointmentData();
    }
    
    private void refreshData() {
        this.appointmentDAO = new AppointmentDAO();
        this.userDAO = new UserDAO();
        this.serviceDAO = new ServiceDAO();
        this.paymentDAO = new PaymentDAO();
        refreshDropdownData();
        loadAppointmentData();
    }
    
    @Override
    protected void initializeComponents() {
        String[] columns = {"ID", "Customer", "Type", "Technician", "Date", "Time", "Status", "Price"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        appointmentTable = new JTable(tableModel);
        setupTableStyle();
        
        refreshDropdownData();
        
        addButton = createStyledButton("+ New Appointment", NAVY_BLUE);
        refreshButton = createStyledButton("Refresh", new Color(34, 197, 94));
        editButton = createStyledButton("Edit Appointment", new Color(59, 130, 246));
        cancelAppointmentButton = createStyledButton("Cancel Appointment", new Color(220, 38, 38));
    }
    
    private void setupTableStyle() {
        appointmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        appointmentTable.setRowHeight(45);
        appointmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        appointmentTable.setFillsViewportHeight(true);
        appointmentTable.setFocusable(false);
        appointmentTable.setRowSelectionAllowed(true);
        appointmentTable.setColumnSelectionAllowed(false);
        appointmentTable.setShowVerticalLines(false);
        appointmentTable.setShowHorizontalLines(false);
        appointmentTable.setIntercellSpacing(new Dimension(0, 0));
        appointmentTable.setSelectionBackground(new Color(232, 240, 254));
        appointmentTable.setSelectionForeground(new Color(31, 41, 55));
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JTableHeader header = appointmentTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(new Color(31, 41, 55));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));
        
        // Only set widths for columns 0-7 (8 columns total)
        appointmentTable.getColumnModel().getColumn(0).setPreferredWidth(80);   // ID
        appointmentTable.getColumnModel().getColumn(1).setPreferredWidth(180);  // Customer
        appointmentTable.getColumnModel().getColumn(2).setPreferredWidth(80);   // Type
        appointmentTable.getColumnModel().getColumn(3).setPreferredWidth(180);  // Technician
        appointmentTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Date
        appointmentTable.getColumnModel().getColumn(5).setPreferredWidth(80);   // Time
        appointmentTable.getColumnModel().getColumn(6).setPreferredWidth(100);  // Status
        appointmentTable.getColumnModel().getColumn(7).setPreferredWidth(100);  // Price (amount)
        // Remove the line for column 8 - it doesn't exist

        appointmentTable.setDefaultRenderer(Object.class, new TableCellRenderer());
        
        // Set custom header renderer for left padding
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
        
        for (int i = 0; i < appointmentTable.getColumnModel().getColumnCount(); i++) {
            appointmentTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
    }
    
    private void refreshDropdownData() {
        customers = userDAO.readCustomers();
        technicians = userDAO.readTechnicians();
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
        
        JLabel titleLabel = new JLabel("Appointment Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        panel.add(titleLabel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(PANEL_BG);
        
        addButton.addActionListener(e -> showAppointmentDialog(null));
        rightPanel.add(addButton);
        
        refreshButton.addActionListener(e -> {
            refreshData();
            JOptionPane.showMessageDialog(this, "Data has been refreshed from files.", 
                "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
        });
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
        
        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionPanel.setBackground(LIGHT_BG);
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BORDER));
        
        editButton.addActionListener(e -> editSelectedAppointment());
        cancelAppointmentButton.addActionListener(e -> cancelSelectedAppointment());
        
        actionPanel.add(editButton);
        actionPanel.add(cancelAppointmentButton);
        
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void showAppointmentDialog(Appointment existingAppointment) {
        AppointmentFormDialog dialog = new AppointmentFormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            existingAppointment,
            customers,
            technicians,
            serviceDAO,
            appointmentDAO,
            paymentDAO,
            userDAO,
            currentUser,  // Make sure this is passed
            this::loadAppointmentData
        );
        dialog.setVisible(true);
    }
    
    private void loadAppointmentData() {
        tableModel.setRowCount(0);
        List<Appointment> appointments = appointmentDAO.readAll();
        appointments.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        
        for (Appointment appt : appointments) {
            User customer = userDAO.findById(appt.getCustomerId());
            User technician = userDAO.findById(appt.getTechnicianId());
            
            String statusDisplay = getStatusDisplay(appt.getStatus());
            
            // Format the amount (price)
            String amountDisplay = appt.getAmount() > 0 ? String.format("RM %.2f", appt.getAmount()) : "-";
            
            Object[] row = {
                appt.getId(),
                customer != null ? customer.getFullName() : "Unknown",
                appt.getServiceType().toString(),
                technician != null ? technician.getFullName() : "Unassigned",
                appt.getDate(),
                appt.getStartTime(),
                statusDisplay,
                amountDisplay
            };
            tableModel.addRow(row);
        }
    }
    
    private String getStatusDisplay(AppointmentStatus status) {
        switch (status) {
            case PENDING: return "PENDING";
            case ASSIGNED: return "ASSIGNED";
            case COMPLETED: return "COMPLETED";
            case CANCELLED: return "CANCELLED";
            default: return status.toString();
        }
    }
    
    private boolean isValidDateTime(String date, String time) {
        try {
            LocalDateTime inputDateTime = LocalDateTime.parse(date + "T" + time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return inputDateTime.isAfter(LocalDateTime.now());
        } catch (Exception e) {
            return false;
        }
    }
    
    private List<Technician> getAvailableTechnicians(String date, String time, int duration, String excludeAppointmentId) {
        LocalDateTime start = LocalDateTime.parse(date + "T" + time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime end = start.plusHours(duration).plusMinutes(30);
        
        List<Appointment> allAppointments = appointmentDAO.readAll();
        
        return technicians.stream()
            .filter(tech -> isTechnicianAvailable(tech, start, end, allAppointments, excludeAppointmentId))
            .collect(Collectors.toList());
    }
    
    private boolean isTechnicianAvailable(Technician tech, LocalDateTime newStart, LocalDateTime newEnd, 
                                          List<Appointment> allAppointments, String excludeAppointmentId) {
        for (Appointment appt : allAppointments) {
            if (excludeAppointmentId != null && appt.getId().equals(excludeAppointmentId)) {
                continue;
            }
            
            if (tech.getId().equals(appt.getTechnicianId()) && appt.getStatus() != AppointmentStatus.CANCELLED) {
                try {
                    LocalDateTime existingStart = LocalDateTime.parse(appt.getDate() + "T" + appt.getStartTime(), 
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    int existingDuration = appt.getDuration();
                    LocalDateTime existingEnd = existingStart.plusHours(existingDuration).plusMinutes(30);
                    
                    if (newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd)) {
                        return false;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return true;
    }
    
    private void editSelectedAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow >= 0) {
            String apptId = (String) tableModel.getValueAt(selectedRow, 0);
            Appointment appt = appointmentDAO.findById(apptId);
            
            if (appt != null) {
                if (appt.getStatus() == AppointmentStatus.CANCELLED) {
                    JOptionPane.showMessageDialog(this, "Cannot edit a cancelled appointment.");
                    return;
                }
                
                if (isPaid(apptId)) {
                    JOptionPane.showMessageDialog(this, "Cannot edit a paid appointment.");
                    return;
                }
                
                showAppointmentDialog(appt);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an appointment to edit.");
        }
    }
    
    private boolean isPaid(String appointmentId) {
        for (Payment payment : paymentDAO.readAll()) {
            if (payment.getAppointmentId().equals(appointmentId)) {
                return true;
            }
        }
        return false;
    }
    
    private void cancelSelectedAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow >= 0) {
            String apptId = (String) tableModel.getValueAt(selectedRow, 0);
            Appointment appt = appointmentDAO.findById(apptId);
            
            if (appt != null) {
                if (appt.getStatus() == AppointmentStatus.CANCELLED) {
                    JOptionPane.showMessageDialog(this, "Appointment is already cancelled.");
                    return;
                }
                
                if (isPaid(apptId)) {
                    JOptionPane.showMessageDialog(this, "Cannot cancel a paid appointment.");
                    return;
                }
                
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel appointment " + apptId + "?\nThis action cannot be undone.",
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    appt.setStatus(AppointmentStatus.CANCELLED);
                    appointmentDAO.update(appt);
                    loadAppointmentData();
                    JOptionPane.showMessageDialog(this, "Appointment cancelled successfully.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.");
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
        // Event handlers
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
            
            if (column == 6 && value != null) {
                String status = value.toString();
                switch (status) {
                    case "PENDING":
                        label.setForeground(new Color(234, 179, 8));
                        break;
                    case "ASSIGNED":
                        label.setForeground(new Color(59, 130, 246));
                        break;
                    case "COMPLETED":
                        label.setForeground(new Color(34, 197, 94));
                        break;
                    case "CANCELLED":
                        label.setForeground(new Color(220, 38, 38));
                        break;
                }
                if (isSelected) {
                    label.setForeground(new Color(31, 41, 55));
                }
            }
            
            return label;
        }
    }
}

// Separate dialog class for appointment form
class AppointmentFormDialog extends JDialog {

 private JComboBox<String> customerCombo;
 private JComboBox<String> serviceTypeCombo;
 private JTextField dateField;
 private JTextField timeField;
 private JComboBox<String> technicianCombo;
 
 private Appointment existingAppointment;
 private List<Customer> customers;
 private List<Technician> technicians;
 private ServiceDAO serviceDAO;
 private AppointmentDAO appointmentDAO;
 private PaymentDAO paymentDAO;
 private UserDAO userDAO;
 private Runnable onSuccess;
 private User currentUser;
 
 public AppointmentFormDialog(Frame parent, Appointment existingAppointment, 
         List<Customer> customers, List<Technician> technicians,
         ServiceDAO serviceDAO, AppointmentDAO appointmentDAO,
         PaymentDAO paymentDAO, UserDAO userDAO, 
         User currentUser, Runnable onSuccess) {
	super(parent, true);
	this.existingAppointment = existingAppointment;
	this.customers = customers;
	this.technicians = technicians;
	this.serviceDAO = serviceDAO;
	this.appointmentDAO = appointmentDAO;
	this.paymentDAO = paymentDAO;
	this.userDAO = userDAO;
	this.currentUser = currentUser;  // Store the current user
	this.onSuccess = onSuccess;
	
	setTitle(existingAppointment == null ? "New Appointment" : "Edit Appointment");
	setSize(480, 520);
	setLocationRelativeTo(parent);
	setResizable(false);
	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	
	initComponents();
	setupLayout();
	
	if (existingAppointment != null) {
	populateFields();
	}
}
 
 private void initComponents() {
     Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);
     Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
     
     // Customer Combo
     customerCombo = new JComboBox<>();
     for (Customer c : customers) {
         customerCombo.addItem(c.getId() + " - " + c.getFullName());
     }
     customerCombo.setFont(fieldFont);
     
     // Service Type Combo
     serviceTypeCombo = new JComboBox<>(new String[]{"NORMAL", "MAJOR"});
     serviceTypeCombo.setFont(fieldFont);
     
     // Date Field
     dateField = new JTextField();
     dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
     dateField.setFont(fieldFont);
     
     // Time Field
     timeField = new JTextField();
     timeField.setText("10:00");
     timeField.setFont(fieldFont);
     
     // Technician Combo
     technicianCombo = new JComboBox<>();
     technicianCombo.addItem("-- Select Technician (Optional) --");
     for (Technician t : technicians) {
         technicianCombo.addItem(t.getId() + " - " + t.getFullName());
     }
     technicianCombo.setFont(fieldFont);
     
     // Style all input fields
     styleComboBox(customerCombo);
     styleComboBox(serviceTypeCombo);
     styleTextField(dateField);
     styleTextField(timeField);
     styleComboBox(technicianCombo);
     
     // Add document listeners for availability update
     javax.swing.event.DocumentListener docListener = new javax.swing.event.DocumentListener() {
         public void changedUpdate(javax.swing.event.DocumentEvent e) { updateAvailability(); }
         public void insertUpdate(javax.swing.event.DocumentEvent e) { updateAvailability(); }
         public void removeUpdate(javax.swing.event.DocumentEvent e) { updateAvailability(); }
     };
     
     dateField.getDocument().addDocumentListener(docListener);
     timeField.getDocument().addDocumentListener(docListener);
     serviceTypeCombo.addActionListener(e -> updateAvailability());
     
     updateAvailability();
 }
 
 private void styleTextField(JTextField field) {
     field.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
         BorderFactory.createEmptyBorder(8, 12, 8, 12)
     ));
     field.setPreferredSize(new Dimension(260, 36));
     field.setBackground(Color.WHITE);
 }
 
 private void styleComboBox(JComboBox<?> combo) {
     combo.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
         BorderFactory.createEmptyBorder(8, 12, 8, 12)
     ));
     combo.setPreferredSize(new Dimension(260, 36));
     combo.setBackground(Color.WHITE);
 }
 
 private void updateAvailability() {
     String date = dateField.getText().trim();
     String time = timeField.getText().trim();
     if (!date.isEmpty() && !time.isEmpty() && isValidDateTime(date, time)) {
         updateTechnicianComboAvailability();
     }
 }
 
 private void updateTechnicianComboAvailability() {
     String date = dateField.getText().trim();
     String time = timeField.getText().trim();
     
     if (date.isEmpty() || time.isEmpty()) return;
     
     try {
         String selectedService = (String) serviceTypeCombo.getSelectedItem();
         int duration = "MAJOR".equals(selectedService) ? 3 : 1;
         
         LocalDateTime start = LocalDateTime.parse(date + "T" + time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
         LocalDateTime end = start.plusHours(duration).plusMinutes(30);
         
         List<Appointment> allAppointments = appointmentDAO.readAll();
         String excludeId = existingAppointment != null ? existingAppointment.getId() : null;
         
         List<Technician> availableTechs = technicians.stream()
             .filter(tech -> isTechnicianAvailable(tech, start, end, allAppointments, excludeId))
             .collect(Collectors.toList());
         
         String selectedTech = (String) technicianCombo.getSelectedItem();
         
         technicianCombo.removeAllItems();
         technicianCombo.addItem("-- Select Technician (Optional) --");
         
         for (Technician tech : availableTechs) {
             technicianCombo.addItem(tech.getId() + " - " + tech.getFullName());
         }
         
         if (selectedTech != null && !selectedTech.equals("-- Select Technician (Optional) --")) {
             for (int i = 0; i < technicianCombo.getItemCount(); i++) {
                 String item = technicianCombo.getItemAt(i);
                 if (item != null && item.equals(selectedTech)) {
                     technicianCombo.setSelectedIndex(i);
                     break;
                 }
             }
         }
     } catch (Exception e) {
         // Invalid date/time format - ignore
     }
 }
 
 private boolean isTechnicianAvailable(Technician tech, LocalDateTime newStart, LocalDateTime newEnd, 
                                       List<Appointment> allAppointments, String excludeAppointmentId) {
     for (Appointment appt : allAppointments) {
         if (excludeAppointmentId != null && appt.getId().equals(excludeAppointmentId)) {
             continue;
         }
         
         if (tech.getId().equals(appt.getTechnicianId()) && appt.getStatus() != AppointmentStatus.CANCELLED) {
             try {
                 LocalDateTime existingStart = LocalDateTime.parse(appt.getDate() + "T" + appt.getStartTime(), 
                     DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                 int existingDuration = appt.getDuration();
                 LocalDateTime existingEnd = existingStart.plusHours(existingDuration).plusMinutes(30);
                 
                 if (newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd)) {
                     return false;
                 }
             } catch (Exception e) {
                 continue;
             }
         }
     }
     return true;
 }
 
 private boolean isValidDateTime(String date, String time) {
     try {
         LocalDateTime inputDateTime = LocalDateTime.parse(date + "T" + time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
         return inputDateTime.isAfter(LocalDateTime.now());
     } catch (Exception e) {
         return false;
     }
 }
 
 private void setupLayout() {
     setLayout(new BorderLayout());
     
     // Main content panel with padding
     JPanel contentPanel = new JPanel();
     contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
     contentPanel.setBackground(new Color(248, 250, 252));
     contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
     
     // Form Card
     JPanel formCard = new JPanel();
     formCard.setBackground(Color.WHITE);
     formCard.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
         BorderFactory.createEmptyBorder(20, 20, 20, 20)
     ));
     formCard.setLayout(new GridBagLayout());
     
     GridBagConstraints gbc = new GridBagConstraints();
     gbc.fill = GridBagConstraints.HORIZONTAL;
     gbc.insets = new Insets(6, 8, 6, 8);
     gbc.weightx = 1.0;
     
     // Customer
     gbc.gridx = 0; gbc.gridy = 0;
     gbc.gridwidth = 1;
     gbc.weightx = 0.32;
     JLabel customerLabel = new JLabel("Customer");
     customerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
     customerLabel.setForeground(new Color(31, 41, 55));
     formCard.add(customerLabel, gbc);
     
     gbc.gridx = 1;
     gbc.weightx = 0.68;
     formCard.add(customerCombo, gbc);
     
     // Service Type
     gbc.gridx = 0; gbc.gridy = 1;
     gbc.weightx = 0.32;
     JLabel serviceLabel = new JLabel("Service Type");
     serviceLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
     serviceLabel.setForeground(new Color(31, 41, 55));
     formCard.add(serviceLabel, gbc);
     
     gbc.gridx = 1;
     gbc.weightx = 0.68;
     formCard.add(serviceTypeCombo, gbc);
     
     // Date
     gbc.gridx = 0; gbc.gridy = 2;
     gbc.weightx = 0.32;
     JLabel dateLabel = new JLabel("Date");
     dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
     dateLabel.setForeground(new Color(31, 41, 55));
     formCard.add(dateLabel, gbc);
     
     gbc.gridx = 1;
     gbc.weightx = 0.68;
     formCard.add(dateField, gbc);
     
     // Time
     gbc.gridx = 0; gbc.gridy = 3;
     gbc.weightx = 0.32;
     JLabel timeLabel = new JLabel("Time");
     timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
     timeLabel.setForeground(new Color(31, 41, 55));
     formCard.add(timeLabel, gbc);
     
     gbc.gridx = 1;
     gbc.weightx = 0.68;
     formCard.add(timeField, gbc);
     
     // Technician
     gbc.gridx = 0; gbc.gridy = 4;
     gbc.weightx = 0.32;
     JLabel techLabel = new JLabel("Technician");
     techLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
     techLabel.setForeground(new Color(31, 41, 55));
     formCard.add(techLabel, gbc);
     
     gbc.gridx = 1;
     gbc.weightx = 0.68;
     formCard.add(technicianCombo, gbc);
     
     contentPanel.add(formCard);
     contentPanel.add(Box.createVerticalStrut(12));
     
     // Info Panel
     JPanel infoPanel = new JPanel();
     infoPanel.setBackground(new Color(239, 246, 255));
     infoPanel.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(new Color(191, 219, 254), 1),
         BorderFactory.createEmptyBorder(12, 15, 12, 15)
     ));
     infoPanel.setLayout(new BorderLayout());
     
     JLabel infoLabel = new JLabel("<html><b style='color:#1e40af;'>Note:</b><br>• Date must not be in the past<br>• Technician availability checked automatically<br>• 30 minute break buffer between appointments</html>");
     infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
     infoLabel.setForeground(new Color(30, 64, 175));
     infoPanel.add(infoLabel, BorderLayout.WEST);
     
     contentPanel.add(infoPanel);
     contentPanel.add(Box.createVerticalStrut(15));
     
     // Button Panel
     JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
     buttonPanel.setBackground(new Color(248, 250, 252));
     
     String btnText = (existingAppointment == null) ? "Create Appointment" : "Update Appointment";
     Color btnColor = (existingAppointment == null) ? new Color(59, 130, 246) : new Color(34, 197, 94);
     
     JButton actionBtn = new JButton(btnText);
     actionBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
     actionBtn.setBackground(btnColor);
     actionBtn.setForeground(Color.WHITE);
     actionBtn.setFocusPainted(false);
     actionBtn.setBorderPainted(false);
     actionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
     actionBtn.setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 28));
     actionBtn.addActionListener(e -> handleSubmit());
     
     JButton cancelBtn = new JButton("Cancel");
     cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
     cancelBtn.setBackground(new Color(156, 163, 175));
     cancelBtn.setForeground(Color.WHITE);
     cancelBtn.setFocusPainted(false);
     cancelBtn.setBorderPainted(false);
     cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
     cancelBtn.setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 28));
     cancelBtn.addActionListener(e -> dispose());
     
     // Hover effects
     actionBtn.addMouseListener(new MouseAdapter() {
         public void mouseEntered(MouseEvent e) { actionBtn.setBackground(btnColor.darker()); }
         public void mouseExited(MouseEvent e) { actionBtn.setBackground(btnColor); }
     });
     
     cancelBtn.addMouseListener(new MouseAdapter() {
         public void mouseEntered(MouseEvent e) { cancelBtn.setBackground(new Color(136, 143, 155)); }
         public void mouseExited(MouseEvent e) { cancelBtn.setBackground(new Color(156, 163, 175)); }
     });
     
     buttonPanel.add(actionBtn);
     buttonPanel.add(cancelBtn);
     
     contentPanel.add(buttonPanel);
     
     add(contentPanel, BorderLayout.CENTER);
 }
 
 private void populateFields() {
     if (existingAppointment == null) return;
     
     for (int i = 0; i < customerCombo.getItemCount(); i++) {
         String item = customerCombo.getItemAt(i);
         if (item != null && item.startsWith(existingAppointment.getCustomerId())) {
             customerCombo.setSelectedIndex(i);
             break;
         }
     }
     
     serviceTypeCombo.setSelectedItem(existingAppointment.getServiceType().toString());
     dateField.setText(existingAppointment.getDate());
     timeField.setText(existingAppointment.getStartTime());
     
     if (existingAppointment.getTechnicianId() != null) {
         for (int i = 0; i < technicianCombo.getItemCount(); i++) {
             String item = technicianCombo.getItemAt(i);
             if (item != null && item.startsWith(existingAppointment.getTechnicianId())) {
                 technicianCombo.setSelectedIndex(i);
                 break;
             }
         }
     }
 }
 
 private void handleSubmit() {
     String validationMsg = validateFields();
     if (validationMsg != null) {
         JOptionPane.showMessageDialog(this, validationMsg, "Validation Error", JOptionPane.WARNING_MESSAGE);
         return;
     }
     
     if (existingAppointment == null) {
         handleCreate();
     } else {
         handleUpdate();
     }
 }
 
 private String validateFields() {
     String date = dateField.getText().trim();
     String time = timeField.getText().trim();
     
     if (customerCombo.getSelectedItem() == null) {
         return "Please select a customer.";
     }
     if (date.isEmpty()) {
         return "Please enter a date.";
     }
     if (time.isEmpty()) {
         return "Please enter a time.";
     }
     if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
         return "Please enter date in format: YYYY-MM-DD";
     }
     if (!time.matches("\\d{2}:\\d{2}")) {
         return "Please enter time in format: HH:MM";
     }
     if (!isValidDateTime(date, time)) {
         return "Appointment date/time cannot be in the past!";
     }
     return null;
 }
 
 private void handleCreate() {
	    try {
	        String customerSelection = (String) customerCombo.getSelectedItem();
	        String customerId = customerSelection.split(" - ")[0];
	        ServiceType serviceType = ServiceType.valueOf((String) serviceTypeCombo.getSelectedItem());
	        String date = dateField.getText().trim();
	        String time = timeField.getText().trim();
	        int duration = serviceType == ServiceType.MAJOR ? 3 : 1;
	        
	        // Get the current price from services.txt
	        double price = serviceDAO.getPrice(serviceType);
	        
	        // Debug output to verify the price
	        System.out.println("Creating appointment - Service Type: " + serviceType + ", Price: " + price);
	        
	        String techSelection = (String) technicianCombo.getSelectedItem();
	        String technicianId = null;
	        AppointmentStatus status = AppointmentStatus.PENDING;
	        
	        if (techSelection != null && !techSelection.equals("-- Select Technician (Optional) --")) {
	            technicianId = techSelection.split(" - ")[0];
	            status = AppointmentStatus.ASSIGNED;
	            
	            boolean available = verifyTechnicianAvailability(technicianId, date, time, duration, null);
	            if (!available) {
	                JOptionPane.showMessageDialog(this, "Selected technician is not available at this time!", 
	                    "Not Available", JOptionPane.WARNING_MESSAGE);
	                return;
	            }
	        }
	        
	        Appointment appointment = new Appointment();
	        appointment.setCustomerId(customerId);
	        appointment.setServiceType(serviceType);
	        appointment.setDate(date);
	        appointment.setStartTime(time);
	        appointment.setDuration(duration);
	        appointment.setTechnicianId(technicianId);
	        appointment.setStatus(status);
	        appointment.setAmount(price);  // Use the price from services.txt
	        
	        if (currentUser != null) {
	            appointment.setCounterStaffId(currentUser.getId());
	        }
	        
	        appointmentDAO.save(appointment);
	        
	        JOptionPane.showMessageDialog(this, "Appointment created successfully!\nID: " + appointment.getId() + "\nAmount: RM " + price, 
	            "Success", JOptionPane.INFORMATION_MESSAGE);
	        
	        if (onSuccess != null) onSuccess.run();
	        dispose();
	        
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(this, "Error creating appointment: " + e.getMessage(), 
	            "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}

 private void handleUpdate() {
	    try {
	        if (existingAppointment.getStatus() == AppointmentStatus.CANCELLED) {
	            JOptionPane.showMessageDialog(this, "Cannot edit a cancelled appointment.");
	            dispose();
	            return;
	        }
	        
	        if (isPaid(existingAppointment.getId())) {
	            JOptionPane.showMessageDialog(this, "Cannot edit a paid appointment.");
	            dispose();
	            return;
	        }
	        
	        String customerSelection = (String) customerCombo.getSelectedItem();
	        String customerId = customerSelection.split(" - ")[0];
	        ServiceType serviceType = ServiceType.valueOf((String) serviceTypeCombo.getSelectedItem());
	        String date = dateField.getText().trim();
	        String time = timeField.getText().trim();
	        int duration = serviceType == ServiceType.MAJOR ? 3 : 1;
	        
	        // Get the current price from services.txt
	        double price = serviceDAO.getPrice(serviceType);
	        
	        System.out.println("Updating appointment - Service Type: " + serviceType + ", Price: " + price);
	        
	        String techSelection = (String) technicianCombo.getSelectedItem();
	        String technicianId = null;
	        AppointmentStatus status = AppointmentStatus.PENDING;
	        
	        if (techSelection != null && !techSelection.equals("-- Select Technician (Optional) --")) {
	            technicianId = techSelection.split(" - ")[0];
	            status = AppointmentStatus.ASSIGNED;
	            
	            boolean available = verifyTechnicianAvailability(technicianId, date, time, duration, existingAppointment.getId());
	            if (!available) {
	                JOptionPane.showMessageDialog(this, "Selected technician is not available at this time!", 
	                    "Not Available", JOptionPane.WARNING_MESSAGE);
	                return;
	            }
	        }
	        
	        existingAppointment.setCustomerId(customerId);
	        existingAppointment.setServiceType(serviceType);
	        existingAppointment.setDate(date);
	        existingAppointment.setStartTime(time);
	        existingAppointment.setDuration(duration);
	        existingAppointment.setTechnicianId(technicianId);
	        existingAppointment.setStatus(status);
	        existingAppointment.setAmount(price);  // Update with current price from services.txt
	        
	        // Keep existing counter staff ID or set if not set
	        if (existingAppointment.getCounterStaffId() == null && currentUser != null) {
	            existingAppointment.setCounterStaffId(currentUser.getId());
	        }
	        
	        appointmentDAO.update(existingAppointment);
	        
	        JOptionPane.showMessageDialog(this, "Appointment updated successfully!\nAmount: RM " + price, 
	            "Success", JOptionPane.INFORMATION_MESSAGE);
	        
	        if (onSuccess != null) onSuccess.run();
	        dispose();
	        
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(this, "Error updating appointment: " + e.getMessage(), 
	            "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}
	
 private boolean verifyTechnicianAvailability(String technicianId, String date, String time, int duration, String excludeId) {
     Technician tech = userDAO.findTechnicianById(technicianId);
     if (tech == null) return false;
     
     LocalDateTime start = LocalDateTime.parse(date + "T" + time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
     LocalDateTime end = start.plusHours(duration).plusMinutes(30);
     
     List<Appointment> allAppointments = appointmentDAO.readAll();
     
     for (Appointment appt : allAppointments) {
         if (excludeId != null && appt.getId().equals(excludeId)) {
             continue;
         }
         
         if (tech.getId().equals(appt.getTechnicianId()) && appt.getStatus() != AppointmentStatus.CANCELLED) {
             try {
                 LocalDateTime existingStart = LocalDateTime.parse(appt.getDate() + "T" + appt.getStartTime(), 
                     DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                 int existingDuration = appt.getDuration();
                 LocalDateTime existingEnd = existingStart.plusHours(existingDuration).plusMinutes(30);
                 
                 if (start.isBefore(existingEnd) && existingStart.isBefore(end)) {
                     return false;
                 }
             } catch (Exception e) {
                 continue;
             }
         }
     }
     return true;
 }
 
 private boolean isPaid(String appointmentId) {
     for (Payment payment : paymentDAO.readAll()) {
         if (payment.getAppointmentId().equals(appointmentId)) {
             return true;
         }
     }
     return false;
 }
}