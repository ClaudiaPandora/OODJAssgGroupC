package ui.customer;

import dao.AppointmentDAO;
import dao.PaymentDAO;
import dao.FeedbackDAO;
import dao.UserDAO;
import enums.AppointmentStatus;
import enums.ServiceType;
import models.Appointment;
import models.Payment;
import models.Feedback;
import models.User;
import ui.common.BasePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CustomerHistoryPanel extends BasePanel {

    private final User currentUser;
    private final AppointmentDAO appointmentDAO;
    private final PaymentDAO paymentDAO;
    private final FeedbackDAO techNoteDAO;
    private final UserDAO userDAO;

    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> serviceFilter;
    private JButton applyButton;
    private JButton resetButton;
    private JButton refreshButton;
    private JButton viewNoteButton;

    private JTable serviceTable;
    private JTable paymentTable;
    private DefaultTableModel serviceTableModel;
    private DefaultTableModel paymentTableModel;
    
    private Set<String> paidAppointmentIds;
    
    private final Color CARD_BORDER = new Color(221, 225, 231);
    private final Color TABLE_HEADER_BG = new Color(244, 246, 250);
    private final Color TABLE_SELECTION = new Color(232, 240, 254);
    private final Color ROW_SEPARATOR = new Color(231, 235, 240);
    private final Color GREEN = new Color(34, 197, 94);
    private final Color BLUE = new Color(59, 130, 246);
    private final Color ORANGE = new Color(234, 179, 8);
    private final Color PAID_GREEN = new Color(16, 185, 129);

    public CustomerHistoryPanel(User currentUser) {
        this.currentUser = currentUser;
        this.appointmentDAO = new AppointmentDAO();
        this.paymentDAO = new PaymentDAO();
        this.techNoteDAO = new FeedbackDAO();
        this.userDAO = new UserDAO();
        this.paidAppointmentIds = new HashSet<>();

        setBackground(PANEL_BG);
        initializeComponents();
        setupLayout();
        addEventHandlers();
        refreshTables();
    }

    @Override
    protected void initializeComponents() {
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(9, 12, 9, 12)
        ));

        statusFilter = new JComboBox<>(new String[]{
                "All Status", "PENDING", "ASSIGNED", "COMPLETED", "PAID"
        });
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.setFocusable(false);

        serviceFilter = new JComboBox<>(new String[]{
                "All Services", "NORMAL", "MAJOR"
        });
        serviceFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        serviceFilter.setFocusable(false);

        applyButton = createActionButton("Apply", BLUE);
        resetButton = createActionButton("Reset", new Color(156, 163, 175));
        refreshButton = createActionButton("Refresh", GREEN);
        viewNoteButton = createActionButton("View Full Note", new Color(124, 58, 237));

        // Service History columns
        serviceTableModel = new DefaultTableModel(
                new String[]{"Appointment ID", "Date", "Time", "Service Type", "Status", "Technician", "Technician Notes"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        paymentTableModel = new DefaultTableModel(
                new String[]{"Payment ID", "Appointment ID", "Payment Date", "Method", "Amount", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        serviceTable = createStyledTable(serviceTableModel);
        paymentTable = createStyledTable(paymentTableModel);
        
        setupTableStyle(serviceTable);
        setupTableStyle(paymentTable);
        
        serviceTable.getColumnModel().getColumn(4).setCellRenderer(new StatusColorRenderer());
        paymentTable.getColumnModel().getColumn(5).setCellRenderer(new StatusColorRenderer());
        
        setTableColumnWidths();
    }
    
    private void setupTableStyle(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(45);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);  // Change to true to show horizontal lines
        table.setGridColor(ROW_SEPARATOR);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(new Color(31, 41, 55));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
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
        
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        
        table.setDefaultRenderer(Object.class, new TableCellRenderer());
    }
    
    private void setTableColumnWidths() {
        serviceTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        serviceTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        serviceTable.getColumnModel().getColumn(2).setPreferredWidth(65);
        serviceTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        serviceTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        serviceTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        serviceTable.getColumnModel().getColumn(6).setPreferredWidth(300);
        
        paymentTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        paymentTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        paymentTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        paymentTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        paymentTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        paymentTable.getColumnModel().getColumn(5).setPreferredWidth(80);
    }

    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());

        JPanel pagePanel = new JPanel(new BorderLayout());
        pagePanel.setBackground(PANEL_BG);
        pagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = createHeaderPanel();
        pagePanel.add(headerPanel, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(PANEL_BG);
        content.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JPanel filterPanel = createFilterPanel();
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.addTab("Service History", createTablePanel(serviceTable));
        tabbedPane.addTab("Payment History", createTablePanel(paymentTable));
        tabbedPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(filterPanel);
        content.add(Box.createVerticalStrut(15));
        content.add(tabbedPane);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(PANEL_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pagePanel.add(scrollPane, BorderLayout.CENTER);
        add(pagePanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL_BG);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(PANEL_BG);
        
        JLabel titleLabel = new JLabel("Service & Payment History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(titleLabel);
        
        textPanel.add(Box.createVerticalStrut(5));
        
        JLabel subtitleLabel = new JLabel("View your service history, technician notes, and track payments.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(subtitleLabel);
        
        header.add(textPanel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(PANEL_BG);
        rightPanel.add(viewNoteButton);
        rightPanel.add(refreshButton);
        header.add(rightPanel, BorderLayout.EAST);
        
        return header;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(new Color(31, 41, 55));
        
        searchField.setPreferredSize(new Dimension(250, 34));
        searchField.setMinimumSize(new Dimension(200, 34));
        
        JLabel statusLabel = new JLabel("Status");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(31, 41, 55));
        
        JLabel serviceLabel = new JLabel("Service");
        serviceLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        serviceLabel.setForeground(new Color(31, 41, 55));

        panel.add(searchLabel);
        panel.add(searchField);
        panel.add(statusLabel);
        panel.add(statusFilter);
        panel.add(serviceLabel);
        panel.add(serviceFilter);
        panel.add(applyButton);
        panel.add(resetButton);

        return panel;
    }
    
    private JPanel createTablePanel(JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                new EmptyBorder(0, 0, 0, 0)
        ));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(40);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(new Color(31, 41, 55));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        return table;
    }
    
    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setPreferredSize(new Dimension(110, 34));
        
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

    private void refreshTables() {
        refreshPaidAppointmentIds();
        
        serviceTableModel.setRowCount(0);
        paymentTableModel.setRowCount(0);

        List<Appointment> appointments = getFilteredAppointments();
        Set<String> appointmentIds = new HashSet<>();

        for (Appointment appointment : appointments) {
            appointmentIds.add(appointment.getId());
            
            String displayStatus = getDisplayStatus(appointment);
            String techNote = getShortTechNote(appointment.getId());
            
            serviceTableModel.addRow(new Object[]{
                    appointment.getId(),
                    appointment.getDate(),
                    appointment.getStartTime(),
                    appointment.getServiceType(),
                    displayStatus,
                    resolveUserName(appointment.getTechnicianId()),
                    techNote
            });
        }

        for (Payment payment : paymentDAO.readAll()) {
            if (appointmentIds.contains(payment.getAppointmentId())) {
                paymentTableModel.addRow(new Object[]{
                        payment.getId(),
                        payment.getAppointmentId(),
                        payment.getPaymentDate(),
                        payment.getPaymentMethod(),
                        formatAmount(payment.getAmount()),
                        "PAID"
                });
            }
        }
    }
    
    private String getShortTechNote(String appointmentId) {
        String fullNote = getFullTechNote(appointmentId);
        if (fullNote.length() <= 80) {
            return fullNote;
        }
        return fullNote.substring(0, 77) + "...";
    }
    
    private String getFullTechNote(String appointmentId) {
        List<Feedback> techNotes = techNoteDAO.findByAppointmentId(appointmentId);
        if (techNotes.isEmpty()) {
            return "No service notes available";
        }
        
        StringBuilder notes = new StringBuilder();
        for (Feedback note : techNotes) {
            if (notes.length() > 0) {
                notes.append("\n\n---\n\n");
            }
            notes.append(note.getContent());
        }
        return notes.toString();
    }
    
    private void showFullNoteDialog() {
        int selectedRow = serviceTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select an appointment first to view the full technician note.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String appointmentId = (String) serviceTableModel.getValueAt(selectedRow, 0);
        String technicianName = (String) serviceTableModel.getValueAt(selectedRow, 5);
        String fullNote = getFullTechNote(appointmentId);
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                "Technician Notes - Appointment " + appointmentId, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 250, 252));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));
        
        JLabel titleLabel = new JLabel("Technician: " + technicianName);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(NAVY_BLUE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel idLabel = new JLabel("Appointment: " + appointmentId);
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        idLabel.setForeground(new Color(100, 116, 139));
        headerPanel.add(idLabel, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JTextArea noteArea = new JTextArea(fullNote);
        noteArea.setEditable(false);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        noteArea.setBackground(Color.WHITE);
        noteArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        JScrollPane scrollPane = new JScrollPane(noteArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        closeButton.setBackground(NAVY_BLUE);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setPreferredSize(new Dimension(80, 32));
        closeButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    private void refreshAll() {
        refreshTables();
    }
    
    private void refreshPaidAppointmentIds() {
        paidAppointmentIds.clear();
        for (Payment payment : paymentDAO.readAll()) {
            paidAppointmentIds.add(payment.getAppointmentId());
        }
    }
    
    private String getDisplayStatus(Appointment appointment) {
        if (paidAppointmentIds.contains(appointment.getId())) {
            return "PAID";
        }
        return appointment.getStatus().toString();
    }
    
    private double getAppointmentAmount(Appointment appointment) {
        for (Payment payment : paymentDAO.readAll()) {
            if (payment.getAppointmentId().equals(appointment.getId())) {
                return payment.getAmount();
            }
        }
        return appointment.getAmount();
    }

    private List<Appointment> getFilteredAppointments() {
        List<Appointment> result = new ArrayList<>();
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        String selectedStatus = (String) statusFilter.getSelectedItem();
        String selectedService = (String) serviceFilter.getSelectedItem();

        for (Appointment appointment : appointmentDAO.findByCustomerId(currentUser.getId())) {
            if (!matchesQuery(appointment, query)) {
                continue;
            }
            if (!matchesStatus(appointment, selectedStatus)) {
                continue;
            }
            if (!matchesService(appointment, selectedService)) {
                continue;
            }
            result.add(appointment);
        }

        return result;
    }

    private boolean matchesQuery(Appointment appointment, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return appointment.getId().toLowerCase().contains(query)
                || appointment.getDate().toLowerCase().contains(query);
    }

    private boolean matchesStatus(Appointment appointment, String selectedStatus) {
        if (selectedStatus == null || "All Status".equals(selectedStatus)) {
            return true;
        }
        
        if ("PAID".equals(selectedStatus)) {
            return paidAppointmentIds.contains(appointment.getId());
        }
        
        try {
            AppointmentStatus status = AppointmentStatus.valueOf(selectedStatus);
            return appointment.getStatus() == status;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean matchesService(Appointment appointment, String selectedService) {
        if (selectedService == null || "All Services".equals(selectedService)) {
            return true;
        }
        return appointment.getServiceType() == ServiceType.valueOf(selectedService);
    }

    private String resolveUserName(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return "-";
        }
        User user = userDAO.findById(userId);
        return user != null ? user.getFullName() : userId;
    }

    private String formatAmount(double amount) {
        return amount > 0 ? String.format("RM%.2f", amount) : "-";
    }

    @Override
    protected void addEventHandlers() {
        applyButton.addActionListener(e -> refreshTables());
        
        refreshButton.addActionListener(e -> {
            refreshAll();
            JOptionPane.showMessageDialog(this, "Data has been refreshed.", 
                "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
        });
        
        viewNoteButton.addActionListener(e -> showFullNoteDialog());

        resetButton.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            serviceFilter.setSelectedIndex(0);
            refreshTables();
        });

        searchField.addActionListener(e -> refreshTables());
        
        // Double-click to view note
        serviceTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showFullNoteDialog();
                }
            }
        });
    }

    private class TableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            JLabel label = (JLabel) c;
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
                    BorderFactory.createEmptyBorder(12, 18, 12, 10)
            ));
            
            if (!isSelected) {
                label.setBackground(Color.WHITE);
                label.setForeground(new Color(31, 41, 55));
            } else {
                label.setBackground(TABLE_SELECTION);
                label.setForeground(new Color(31, 41, 55));
            }
            
            return label;
        }
    }

    private static class StatusColorRenderer extends DefaultTableCellRenderer {
        private final Color GREEN = new Color(34, 197, 94);
        private final Color BLUE = new Color(59, 130, 246);
        private final Color ORANGE = new Color(234, 179, 8);
        private final Color PAID_GREEN = new Color(16, 185, 129);
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );

            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));

            if (!isSelected) {
                label.setBackground(Color.WHITE);
            }

            String status = value == null ? "" : value.toString().toUpperCase();

            if ("COMPLETED".equals(status)) {
                label.setForeground(GREEN);
            } else if ("PENDING".equals(status)) {
                label.setForeground(ORANGE);
            } else if ("ASSIGNED".equals(status)) {
                label.setForeground(BLUE);
            } else if ("PAID".equals(status)) {
                label.setForeground(PAID_GREEN);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else {
                label.setForeground(Color.BLACK);
            }

            return label;
        }
    }
}