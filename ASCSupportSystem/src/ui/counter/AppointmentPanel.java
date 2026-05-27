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
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    private JButton addButton;
    private JButton refreshButton;
    private JButton editButton;
    private JButton cancelAppointmentButton;
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> sortCombo;
    private JLabel statsLabel;
    private JLabel emptyLabel;
    private JPanel tableSwitcher;
    
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
        rowSorter = new TableRowSorter<>(tableModel);
        appointmentTable.setRowSorter(rowSorter);
        setupTableStyle();
        
        refreshDropdownData();
        
        // Create buttons ONCE here - do NOT recreate in createHeaderPanel
        addButton = createStyledButton("+ New Appointment", NAVY_BLUE);
        refreshButton = createStyledButton("Refresh", new Color(34, 197, 94));
        editButton = createStyledButton("Edit Appointment", new Color(59, 130, 246));
        cancelAppointmentButton = createStyledButton("Cancel Appointment", new Color(220, 38, 38));
        
        // Add action listeners HERE directly
        addButton.addActionListener(e -> showAppointmentDialog(null));
        refreshButton.addActionListener(e -> {
            refreshData();
            JOptionPane.showMessageDialog(this, "Data has been refreshed.", 
                "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
        });
        editButton.addActionListener(e -> editSelectedAppointment());
        cancelAppointmentButton.addActionListener(e -> cancelSelectedAppointment());
        
        // Search field
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(9, 12, 9, 12)
        ));
        searchField.setToolTipText("Search by Appointment ID or Customer Name");
        
        // Status filter combo
        statusFilterCombo = new JComboBox<>(new String[]{"All Status", "PENDING", "ASSIGNED", "COMPLETED", "CANCELLED"});
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilterCombo.setFocusable(false);
        
        // Sort options
        sortCombo = new JComboBox<>(new String[]{"Most Recent", "Oldest"});
        sortCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sortCombo.setBackground(Color.WHITE);
        sortCombo.setPreferredSize(new Dimension(130, 34));
        
        // Stats label
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(new Color(107, 114, 128));
        
        // Empty label
        emptyLabel = new JLabel("No appointments match the current filters.");
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emptyLabel.setForeground(new Color(107, 114, 128));
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
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
        
        // Set column widths
        appointmentTable.getColumnModel().getColumn(0).setPreferredWidth(80);   // ID
        appointmentTable.getColumnModel().getColumn(1).setPreferredWidth(180);  // Customer
        appointmentTable.getColumnModel().getColumn(2).setPreferredWidth(80);   // Type
        appointmentTable.getColumnModel().getColumn(3).setPreferredWidth(180);  // Technician
        appointmentTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Date
        appointmentTable.getColumnModel().getColumn(5).setPreferredWidth(80);   // Time
        appointmentTable.getColumnModel().getColumn(6).setPreferredWidth(100);  // Status
        appointmentTable.getColumnModel().getColumn(7).setPreferredWidth(100);  // Price

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
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(PANEL_BG);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(PANEL_BG);

        JLabel titleLabel = new JLabel("Appointment Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(titleLabel);

        titleRow.add(titleBlock, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(PANEL_BG);
        actions.add(addButton);  // Use existing button, don't recreate
        actions.add(refreshButton);  // Use existing button, don't recreate
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

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setPreferredSize(new Dimension(350, 34));

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(new Color(31, 41, 55));
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        // Status filter panel
        JPanel statusPanel = new JPanel(new BorderLayout(8, 0));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setPreferredSize(new Dimension(200, 34));

        JLabel statusLabel = new JLabel("Status");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(31, 41, 55));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(statusFilterCombo, BorderLayout.CENTER);

        // Sort panel
        JPanel sortPanel = new JPanel(new BorderLayout(8, 0));
        sortPanel.setBackground(Color.WHITE);
        sortPanel.setPreferredSize(new Dimension(180, 34));

        JLabel sortLabel = new JLabel("Sort by ID");
        sortLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sortLabel.setForeground(new Color(31, 41, 55));
        sortPanel.add(sortLabel, BorderLayout.WEST);
        sortPanel.add(sortCombo, BorderLayout.CENTER);

        // Reset button
        JButton resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        resetButton.setBackground(new Color(156, 163, 175));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.setBorderPainted(false);
        resetButton.setOpaque(true);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        resetButton.setPreferredSize(new Dimension(80, 34));

        resetButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                resetButton.setBackground(new Color(136, 143, 155));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                resetButton.setBackground(new Color(156, 163, 175));
            }
        });

        resetButton.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            sortCombo.setSelectedIndex(0);
            applyFilters();
            updateStatsLabel();
            updateTableVisibility();
        });

        controlsPanel.add(searchPanel);
        controlsPanel.add(statusPanel);
        controlsPanel.add(sortPanel);
        controlsPanel.add(resetButton);  // Add reset button right after sort panel
        toolbar.add(controlsPanel, BorderLayout.WEST);

        return toolbar;
    }
    
    private JPanel createTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createLineBorder(CARD_BORDER));

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
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
        buttonPanel.add(editButton);  // Use existing button
        buttonPanel.add(cancelAppointmentButton);  // Use existing button
        footer.add(buttonPanel, BorderLayout.EAST);
        
        return footer;
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
            currentUser,
            this::loadAppointmentData
        );
        dialog.setVisible(true);
    }
    
    private void loadAppointmentData() {
        tableModel.setRowCount(0);
        
        List<Appointment> appointments = appointmentDAO.readAll();
        
        if (appointments.isEmpty()) {
            updateTableVisibility();
            updateStatsLabel();
            return;
        }
        
        for (Appointment appt : appointments) {
            User customer = userDAO.findById(appt.getCustomerId());
            User technician = userDAO.findById(appt.getTechnicianId());
            
            String statusDisplay = getStatusDisplay(appt.getStatus());
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
        
        applyFilters();
        updateStatsLabel();
        updateTableVisibility();
        
        appointmentTable.revalidate();
        appointmentTable.repaint();
    }
    
    private void applyFilters() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();
        String selectedSort = (String) sortCombo.getSelectedItem();
        
        // Apply search and status filters
        RowFilter<DefaultTableModel, Integer> rowFilter = new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                // Status filter
                boolean statusMatches = "All Status".equals(selectedStatus) || 
                    entry.getValue(6).toString().equals(selectedStatus);
                
                // Search filter (search by ID or Customer name)
                boolean searchMatches = searchText.isEmpty();
                if (!searchText.isEmpty()) {
                    String id = entry.getValue(0).toString().toLowerCase();
                    String customer = entry.getValue(1).toString().toLowerCase();
                    searchMatches = id.contains(searchText) || customer.contains(searchText);
                }
                
                return statusMatches && searchMatches;
            }
        };
        
        rowSorter.setRowFilter(rowFilter);
        
        // Apply sort
        rowSorter.setSortKeys(null);
        if ("Most Recent".equals(selectedSort)) {
            rowSorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.DESCENDING)));
        } else {
            rowSorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        }
    }
    
    private void updateTableVisibility() {
        if (tableSwitcher == null) {
            return;
        }
        
        CardLayout layout = (CardLayout) tableSwitcher.getLayout();
        layout.show(tableSwitcher, appointmentTable.getRowCount() == 0 ? "EMPTY" : "TABLE");
    }
    
    private void updateStatsLabel() {
        int totalAppointments = appointmentDAO.readAll().size();
        int displayedAppointments = appointmentTable.getRowCount();
        
        statsLabel.setText(String.format(
            "Total Appointments: %d    Displaying: %d",
            totalAppointments, displayedAppointments
        ));
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
    
    private void editSelectedAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
            String apptId = (String) tableModel.getValueAt(modelRow, 0);
            Appointment appt = appointmentDAO.findById(apptId);
            
            if (appt != null) {
                if (appt.getStatus() == AppointmentStatus.CANCELLED) {
                    JOptionPane.showMessageDialog(this, "Cannot edit a cancelled appointment.");
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
            int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
            String apptId = (String) tableModel.getValueAt(modelRow, 0);
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
        button.setPreferredSize(null);
        button.setMinimumSize(new Dimension(140, 34));
        
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
        // Search field real-time filtering
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
                updateStatsLabel();
                updateTableVisibility();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
                updateStatsLabel();
                updateTableVisibility();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
                updateStatsLabel();
                updateTableVisibility();
            }
        });
        
        // Status filter
        statusFilterCombo.addActionListener(e -> {
            applyFilters();
            updateStatsLabel();
            updateTableVisibility();
        });
        
        // Sort combo
        sortCombo.addActionListener(e -> applyFilters());
        
        // Double-click to edit
        appointmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedAppointment();
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

// The AppointmentFormDialog class remains exactly the same as before
// (Keep your existing AppointmentFormDialog code here)

//Separate dialog class for appointment form
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
     this.currentUser = currentUser;
     this.onSuccess = onSuccess;
     
     setTitle(existingAppointment == null ? "New Appointment" : "Edit Appointment");
     setSize(560, 600);  // Increased size to prevent button cutoff
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
 
 private boolean isValidWorkingHour(String time) {
	 try {
		 String[] parts = time.split(":");
		 int hour = Integer.parseInt(parts[0]);
	     int minute = Integer.parseInt(parts[1]);
	       
	     // Working hours: 09:00 to 16:00 (since end time will be checked separately)
	     if (hour < 9 || hour > 16) {
	    	 return false;
	    	 }
	     if (hour == 16 && minute > 0) {
	         return false;
	         }
	     return true;
	     } catch (Exception e) {
	    	 return false;
	 }
}
 
 private boolean isValidEndTime(String date, String time, ServiceType serviceType) {
	try {
		LocalDateTime startDateTime = LocalDateTime.parse(date + "T" + time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		int durationHours = (serviceType == ServiceType.MAJOR) ? 3 : 1;
	    LocalDateTime endDateTime = startDateTime.plusHours(durationHours);
	        
	    int endHour = endDateTime.getHour();
	        
	    // End time must be <= 17:00 (5 PM)
	    if (endHour > 17 || (endHour == 17 && endDateTime.getMinute() > 0)) {
	        return false;
	    }
	    return true;
	    } catch (Exception e) {
	    	return false;
	    }
}

private int getDaysInMonth(int year, int month) {
	java.util.Calendar cal = java.util.Calendar.getInstance();
	cal.set(year, month - 1, 1);
	return cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
}

private boolean isValidDate(String date) {
	try {
		String[] parts = date.split("-");
	    if (parts.length != 3) return false;
	        
	    int year = Integer.parseInt(parts[0]);
	    int month = Integer.parseInt(parts[1]);
	    int day = Integer.parseInt(parts[2]);
	        
	    if (year < 2025 || year > 2030) return false;
	    if (month < 1 || month > 12) return false;
	    
	    int maxDay = getDaysInMonth(year, month);
	    if (day < 1 || day > maxDay) return false;
	        
	    return true;
	    } catch (Exception e) {
	    return false;
	 }
}
	
 private void styleTextField(JTextField field) {
     field.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
         BorderFactory.createEmptyBorder(8, 12, 8, 12)
     ));
     field.setPreferredSize(new Dimension(280, 36));
     field.setBackground(Color.WHITE);
 }
 
 private void styleComboBox(JComboBox<?> combo) {
     combo.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
         BorderFactory.createEmptyBorder(8, 12, 8, 12)
     ));
     combo.setPreferredSize(new Dimension(280, 36));
     combo.setBackground(Color.WHITE);
 }
 
 private void updateAvailability() {
	    String date = dateField.getText().trim();
	    String time = timeField.getText().trim();
	    if (!date.isEmpty() && !time.isEmpty() && isValidDate(date)) {
	        if (isValidWorkingHour(time)) {
	            ServiceType serviceType = ServiceType.valueOf((String) serviceTypeCombo.getSelectedItem());
	            if (isValidEndTime(date, time, serviceType)) {
	                updateTechnicianComboAvailability();
	            }
	        }
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
     
     JPanel contentPanel = new JPanel();
     contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
     contentPanel.setBackground(new Color(248, 250, 252));
     contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
     
     JPanel formCard = new JPanel();
     formCard.setBackground(Color.WHITE);
     formCard.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
         BorderFactory.createEmptyBorder(20, 20, 20, 20)
     ));
     formCard.setLayout(new GridBagLayout());
     
     GridBagConstraints gbc = new GridBagConstraints();
     gbc.fill = GridBagConstraints.HORIZONTAL;
     gbc.insets = new Insets(8, 10, 8, 10);
     gbc.weightx = 1.0;
     
     // Customer
     gbc.gridx = 0; gbc.gridy = 0;
     gbc.gridwidth = 1;
     gbc.weightx = 0.3;
     JLabel customerLabel = new JLabel("Customer");
     customerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
     customerLabel.setForeground(new Color(31, 41, 55));
     formCard.add(customerLabel, gbc);
     
     gbc.gridx = 1;
     gbc.weightx = 0.7;
     formCard.add(customerCombo, gbc);
     
     // Service Type
     gbc.gridx = 0; gbc.gridy = 1;
     gbc.weightx = 0.3;
     JLabel serviceLabel = new JLabel("Service Type");
     serviceLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
     serviceLabel.setForeground(new Color(31, 41, 55));
     formCard.add(serviceLabel, gbc);
     
     gbc.gridx = 1;
     gbc.weightx = 0.7;
     formCard.add(serviceTypeCombo, gbc);
     
     // Date
     gbc.gridx = 0; gbc.gridy = 2;
     gbc.weightx = 0.3;
     JLabel dateLabel = new JLabel("Date");
     dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
     dateLabel.setForeground(new Color(31, 41, 55));
     formCard.add(dateLabel, gbc);
     
     gbc.gridx = 1;
     gbc.weightx = 0.7;
     formCard.add(dateField, gbc);
     
     // Time
     gbc.gridx = 0; gbc.gridy = 3;
     gbc.weightx = 0.3;
     JLabel timeLabel = new JLabel("Time");
     timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
     timeLabel.setForeground(new Color(31, 41, 55));
     formCard.add(timeLabel, gbc);
     
     gbc.gridx = 1;
     gbc.weightx = 0.7;
     formCard.add(timeField, gbc);
     
     // Technician
     gbc.gridx = 0; gbc.gridy = 4;
     gbc.weightx = 0.3;
     JLabel techLabel = new JLabel("Technician");
     techLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
     techLabel.setForeground(new Color(31, 41, 55));
     formCard.add(techLabel, gbc);
     
     gbc.gridx = 1;
     gbc.weightx = 0.7;
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
     
     JLabel infoLabel = new JLabel("<html><b style='color:#1e40af;'>Note:</b><br>"
    		    + "• Working hours: 09:00 - 17:00 only<br>"
    		    + "• Normal service (1 hour): Latest start at 16:00<br>"
    		    + "• Major service (3 hours): Latest start at 14:00<br>"
    		    + "• Date must not be in the past<br>"
    		    + "• Technician availability checked automatically<br>"
    		    + "• 30 minute break buffer between appointments</html>");
     infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
     infoLabel.setForeground(new Color(30, 64, 175));
     infoPanel.add(infoLabel, BorderLayout.WEST);
     
     contentPanel.add(infoPanel);
     contentPanel.add(Box.createVerticalStrut(15));
     
     // Button Panel
     JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
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
     actionBtn.setBorder(BorderFactory.createEmptyBorder(10, 35, 10, 35));
     actionBtn.addActionListener(e -> handleSubmit());
     
     JButton cancelBtn = new JButton("Cancel");
     cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
     cancelBtn.setBackground(new Color(156, 163, 175));
     cancelBtn.setForeground(Color.WHITE);
     cancelBtn.setFocusPainted(false);
     cancelBtn.setBorderPainted(false);
     cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
     cancelBtn.setBorder(BorderFactory.createEmptyBorder(10, 35, 10, 35));
     cancelBtn.addActionListener(e -> dispose());
     
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
	    if (!isValidDate(date)) {
	        return "Please enter a valid date.";
	    }
	    if (!time.matches("\\d{2}:\\d{2}")) {
	        return "Please enter time in format: HH:MM";
	    }
	    if (!isValidWorkingHour(time)) {
	        return "Working hours are from 09:00 to 17:00 only.";
	    }
	    
	    ServiceType serviceType = ServiceType.valueOf((String) serviceTypeCombo.getSelectedItem());
	    if (!isValidEndTime(date, time, serviceType)) {
	        int durationHours = (serviceType == ServiceType.MAJOR) ? 3 : 1;
	        int latestStartHour = 17 - durationHours;
	        return String.format("%s service takes %d hour(s).\nPlease start by %d:00 to finish by 17:00.",
	            serviceType.toString(), durationHours, latestStartHour);
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
         
         double price = serviceDAO.getPrice(serviceType);
         
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
         appointment.setAmount(price);
         
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
         
         String customerSelection = (String) customerCombo.getSelectedItem();
         String customerId = customerSelection.split(" - ")[0];
         ServiceType serviceType = ServiceType.valueOf((String) serviceTypeCombo.getSelectedItem());
         String date = dateField.getText().trim();
         String time = timeField.getText().trim();
         int duration = serviceType == ServiceType.MAJOR ? 3 : 1;
         
         double price = serviceDAO.getPrice(serviceType);
         
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
         existingAppointment.setAmount(price);
         
         if (existingAppointment.getCounterStaffId() == null && currentUser != null) {
             existingAppointment.setCounterStaffId(currentUser.getId());
         }
         
         appointmentDAO.update(existingAppointment);
         
         JOptionPane.showMessageDialog(this, "Appointment updated successfully!", 
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