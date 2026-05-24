package ui.counter;

import dao.AppointmentDAO;
import dao.PaymentDAO;
import dao.UserDAO;
import enums.AppointmentStatus;
import enums.ServiceType;
import models.Appointment;
import models.Payment;
import models.User;
import ui.common.BasePanel;
import utils.DateUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CounterOverview extends BasePanel {
    
    private User currentUser;
    private AppointmentDAO appointmentDAO;
    private PaymentDAO paymentDAO;
    private UserDAO userDAO;
    private JButton refreshButton;
    private JPanel statsPanel;
    
    private JTable pendingPaymentsTable;
    private JTable pendingAppointmentsTable;
    private DefaultTableModel pendingPaymentsModel;
    private DefaultTableModel pendingAppointmentsModel;
    
    private Set<String> paidAppointmentIds;
    
    private final Color CARD_BORDER = new Color(226, 232, 240);
    private final Color LIGHT_BG = new Color(248, 250, 252);
    private final Color TABLE_HEADER_BG = new Color(244, 246, 250);
    private final Color ROW_SEPARATOR = new Color(231, 235, 240);
    private final Color TABLE_SELECTION = new Color(232, 240, 254);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    
    public CounterOverview(User user) {
        this.currentUser = user;
        this.appointmentDAO = new AppointmentDAO();
        this.paymentDAO = new PaymentDAO();
        this.userDAO = new UserDAO();
        this.paidAppointmentIds = new HashSet<>();
        
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initializeComponents();
        setupLayout();
        refreshDashboard();
    }
    
    private void refreshData() {
        this.appointmentDAO = new AppointmentDAO();
        this.paymentDAO = new PaymentDAO();
        this.userDAO = new UserDAO();
        refreshPaidAppointmentIds();
    }
    
    private void refreshPaidAppointmentIds() {
        paidAppointmentIds.clear();
        List<Payment> payments = paymentDAO.readAll();
        System.out.println("Refreshing paid IDs. Total payments: " + payments.size());
        for (Payment payment : payments) {
            paidAppointmentIds.add(payment.getAppointmentId());
            System.out.println("Added paid appointment: " + payment.getAppointmentId());
        }
    }
    
    private boolean isAppointmentPaid(String appointmentId) {
        if (paidAppointmentIds.isEmpty()) {
            refreshPaidAppointmentIds();
        }
        return paidAppointmentIds.contains(appointmentId);
    }
    
    private void refreshDashboard() {
        refreshPaidAppointmentIds();
        
        List<Appointment> allAppointments = appointmentDAO.readAll();
        String today = DateUtils.getCurrentDate();
        String currentTime = getCurrentTime();
        
        List<Appointment> todayAppointments = new ArrayList<>();
        for (Appointment a : allAppointments) {
            if (a.getDate() != null && a.getDate().equals(today)) {
                todayAppointments.add(a);
            }
        }
        
        int ongoingCount = 0, completedCount = 0;
        double revenue = 0;
        
        // Calculate revenue based on TODAY's payment date (same as ManagerOverview)
        List<Payment> payments = paymentDAO.readAll();
        for (Payment p : payments) {
            if (p.getPaymentDate() != null && p.getPaymentDate().equals(today)) {
                revenue += p.getAmount();
            }
        }
        
        for (Appointment a : todayAppointments) {
            if (a.getStatus() == AppointmentStatus.COMPLETED) {
                completedCount++;
            } else if (a.getStatus() == AppointmentStatus.ASSIGNED && isOngoing(a.getStartTime(), currentTime, a.getServiceType())) {
                ongoingCount++;
            }
        }
        
        refreshStats(todayAppointments.size(), ongoingCount, completedCount, revenue);
        refreshTables();
    }
    
    private void refreshStats(int totalToday, int ongoingCount, int completedCount, double revenue) {
        statsPanel.removeAll();
        
        statsPanel.add(createStatCard("Appointments Today", String.valueOf(totalToday), null, new Color(59, 130, 246)));
        statsPanel.add(createStatCard("Ongoing Services", String.valueOf(ongoingCount), null, new Color(234, 88, 12)));
        statsPanel.add(createStatCard("Completed Today", String.valueOf(completedCount), null, new Color(34, 197, 94)));
        statsPanel.add(createStatCard("Revenue Today", String.format("RM %.2f", revenue), null, new Color(168, 85, 247)));
        
        statsPanel.revalidate();
        statsPanel.repaint();
    }
    
    // Helper methods for ongoing service checking
    private boolean isOngoing(String startTime, String currentTime, ServiceType serviceType) {
        if (startTime == null || startTime.isEmpty()) return false;
        
        int durationHours = (serviceType == ServiceType.MAJOR) ? 3 : 1;
        String endTime = addHoursToTime(startTime, durationHours);
        
        return isTimeBetween(currentTime, startTime, endTime);
    }
    
    private boolean isTimeBetween(String current, String start, String end) {
        try {
            String[] currentParts = current.split(":");
            String[] startParts = start.split(":");
            String[] endParts = end.split(":");
            
            int currentMinutes = Integer.parseInt(currentParts[0]) * 60 + Integer.parseInt(currentParts[1]);
            int startMinutes = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
            int endMinutes = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);
            
            return currentMinutes >= startMinutes && currentMinutes < endMinutes;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String addHoursToTime(String time, int hours) {
        try {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            
            hour += hours;
            
            return String.format("%02d:%02d", hour, minute);
        } catch (Exception e) {
            return time;
        }
    }
    
    private String getCurrentTime() {
        java.time.LocalTime now = java.time.LocalTime.now();
        return String.format("%02d:%02d", now.getHour(), now.getMinute());
    }
    
    private JPanel createStatCard(String label, String value, String subValue, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        
        JPanel accentBar = new JPanel();
        accentBar.setBackground(accentColor);
        accentBar.setPreferredSize(new Dimension(4, 0));
        card.add(accentBar, BorderLayout.WEST);
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
        JLabel valueText = new JLabel(value);
        valueText.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueText.setForeground(accentColor);
        valueText.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(valueText);
        content.add(Box.createVerticalStrut(2));
        
        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        labelText.setForeground(TEXT_MUTED);
        labelText.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(labelText);
        
        if (subValue != null && !subValue.isEmpty()) {
            content.add(Box.createVerticalStrut(2));
            JLabel subText = new JLabel(subValue);
            subText.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            subText.setForeground(TEXT_MUTED);
            subText.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(subText);
        }
        
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    private void setupTableStyle(JTable table, int[] columnWidths) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(42);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setGridColor(ROW_SEPARATOR);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(new Color(31, 41, 55));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        table.setDefaultRenderer(Object.class, new NonSelectableTableCellRenderer());
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(new Color(31, 41, 55));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setBackground(TABLE_HEADER_BG);
                return label;
            }
        };
        
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            if (i < columnWidths.length) {
                table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
                table.getColumnModel().getColumn(i).setMinWidth(columnWidths[i]);
                table.getColumnModel().getColumn(i).setMaxWidth(columnWidths[i]);
            }
        }
        
        // Set preferred viewport size to enable horizontal scrolling
        int totalWidth = 0;
        for (int i = 0; i < table.getColumnCount(); i++) {
            totalWidth += columnWidths[i];
        }
        table.setPreferredScrollableViewportSize(new Dimension(totalWidth, table.getRowHeight() * 5));
    }
    
    // Custom renderer that prevents selection highlighting
    private class NonSelectableTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, false, false, row, column);
            
            JLabel label = (JLabel) c;
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
                    BorderFactory.createEmptyBorder(10, 12, 10, 10)
            ));
            label.setBackground(Color.WHITE);
            label.setForeground(new Color(31, 41, 55));
            
            return label;
        }
    }
    
    @Override
    protected void initializeComponents() {
        refreshButton = createStyledButton("Refresh", new Color(34, 197, 94));
        refreshButton.addActionListener(e -> {
            refreshData();
            refreshDashboard();
            JOptionPane.showMessageDialog(this, "Dashboard data has been refreshed from files.", 
                "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Pending Payments Table
        String[] pendingPaymentsColumns = {"Appointment ID", "Customer", "Service", "Amount", "Date"};
        pendingPaymentsModel = new DefaultTableModel(pendingPaymentsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pendingPaymentsTable = new JTable(pendingPaymentsModel);
        setupTableStyle(pendingPaymentsTable, new int[]{110, 140, 100, 100, 120});
        
        // Pending Appointments Table - narrower customer column, appointment ID fully visible
        String[] pendingAppointmentsColumns = {"Appointment ID", "Customer", "Service", "Technician", "Date", "Time", "Status"};
        pendingAppointmentsModel = new DefaultTableModel(pendingAppointmentsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pendingAppointmentsTable = new JTable(pendingAppointmentsModel);
        setupTableStyle(pendingAppointmentsTable, new int[]{120, 160, 110, 200, 110, 90, 110});
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(PANEL_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Stats Cards
        statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        statsPanel.setBackground(PANEL_BG);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        statsPanel.setPreferredSize(new Dimension(0, 75));
        mainPanel.add(statsPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Main content row
        JPanel contentRow = new JPanel(new GridLayout(1, 2, 15, 0));
        contentRow.setBackground(PANEL_BG);
        
        contentRow.add(createTableCard("Pending Payments", pendingPaymentsTable, 
            "Appointments waiting for payment collection", new Color(234, 179, 8), 280));
        
        contentRow.add(createTableCard("Active Appointments", pendingAppointmentsTable, 
            "Appointments currently pending, scheduled, or assigned to technicians", new Color(59, 130, 246), 320));
        
        mainPanel.add(contentRow);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_BG);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        headerPanel.setPreferredSize(new Dimension(0, 40));
        
        JLabel titleLabel = new JLabel("Counter Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 23));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(-2, 0, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(PANEL_BG);
        
        JLabel dateLabel = new JLabel(DateUtils.getCurrentDate());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateLabel.setForeground(TEXT_MUTED);
        rightPanel.add(dateLabel);
        
        rightPanel.add(refreshButton);
        
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createTableCard(String title, JTable table, String description, Color accentColor, int preferredHeight) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(LIGHT_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(accentColor);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descLabel.setForeground(TEXT_MUTED);
        headerPanel.add(descLabel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Create a new JScrollPane with both scrollbars as needed
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(0, preferredHeight));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void refreshTables() {
        pendingPaymentsModel.setRowCount(0);
        pendingAppointmentsModel.setRowCount(0);
        
        List<Appointment> allAppointments = appointmentDAO.readAll();
        List<Payment> allPayments = paymentDAO.readAll();
        
        // Create a set of paid appointment IDs
        Set<String> paidIds = new HashSet<>();
        for (Payment p : allPayments) {
            paidIds.add(p.getAppointmentId());
        }
        
        String today = DateUtils.getCurrentDate();
        
        for (Appointment appt : allAppointments) {
            if (appt.getStatus() == AppointmentStatus.CANCELLED) {
                continue;
            }
            
            User customer = userDAO.findById(appt.getCustomerId());
            String customerName = customer != null ? customer.getFullName() : "Unknown";
            boolean isPaid = paidIds.contains(appt.getId());
            
            // Pending payments: Show appointments that are COMPLETED or ASSIGNED (not paid yet)
            // This includes appointments that are ready for payment or currently being serviced
            if ((appt.getStatus() == AppointmentStatus.COMPLETED || appt.getStatus() == AppointmentStatus.ASSIGNED) && !isPaid) {
                pendingPaymentsModel.addRow(new Object[]{
                    appt.getId(),
                    customerName,
                    appt.getServiceType().toString(),
                    String.format("RM %.2f", appt.getAmount()),
                    appt.getDate()
                });
            }
            
            // Pending appointments: Show appointments that are PENDING or ASSIGNED (not COMPLETED or CANCELLED)
            // This shows all active appointments that haven't been completed yet
            if (appt.getStatus() == AppointmentStatus.PENDING || appt.getStatus() == AppointmentStatus.ASSIGNED) {
                String statusDisplay = appt.getStatus() == AppointmentStatus.ASSIGNED ? "ASSIGNED" : "PENDING";
                String technicianName = resolveUserName(appt.getTechnicianId());
                
                pendingAppointmentsModel.addRow(new Object[]{
                    appt.getId(),
                    customerName,
                    appt.getServiceType().toString(),
                    technicianName,
                    appt.getDate(),
                    appt.getStartTime(),
                    statusDisplay
                });
            }
        }
        
        // Handle empty states
        if (pendingPaymentsModel.getRowCount() == 0) {
            pendingPaymentsModel.setRowCount(0);
            pendingPaymentsModel.addRow(new Object[]{"", "", "No pending payments", "", ""});
            pendingPaymentsTable.setEnabled(false);
            
            pendingPaymentsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    JLabel label = (JLabel) c;
                    
                    if (row == 0 && table.getRowCount() == 1) {
                        if (column == 2 && "No pending payments".equals(value)) {
                            label.setHorizontalAlignment(JLabel.CENTER);
                            label.setVerticalAlignment(JLabel.CENTER);
                            label.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                            label.setForeground(TEXT_MUTED);
                            label.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
                        } else {
                            label.setText("");
                            label.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
                        }
                    }
                    return label;
                }
            });
        } else {
            pendingPaymentsTable.setEnabled(true);
            pendingPaymentsTable.setDefaultRenderer(Object.class, new NonSelectableTableCellRenderer());
        }
        
        if (pendingAppointmentsModel.getRowCount() == 0) {
            pendingAppointmentsModel.setRowCount(0);
            pendingAppointmentsModel.addRow(new Object[]{"", "", "", "No pending appointments", "", "", ""});
            pendingAppointmentsTable.setEnabled(false);
            
            pendingAppointmentsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    JLabel label = (JLabel) c;
                    
                    if (row == 0 && table.getRowCount() == 1) {
                        if (column == 3 && "No pending appointments".equals(value)) {
                            label.setHorizontalAlignment(JLabel.CENTER);
                            label.setVerticalAlignment(JLabel.CENTER);
                            label.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                            label.setForeground(TEXT_MUTED);
                            label.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
                        } else {
                            label.setText("");
                            label.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
                        }
                    }
                    return label;
                }
            });
        } else {
            pendingAppointmentsTable.setEnabled(true);
            pendingAppointmentsTable.setDefaultRenderer(Object.class, new NonSelectableTableCellRenderer());
        }
        
        // Refresh table display
        pendingPaymentsTable.revalidate();
        pendingPaymentsTable.repaint();
        pendingAppointmentsTable.revalidate();
        pendingAppointmentsTable.repaint();
    }
    
    private void setEmptyOverlay(JTable table, String message) {

        JViewport viewport = (JViewport) table.getParent();

        JPanel overlay = new JPanel(new GridBagLayout());
        overlay.setBackground(Color.WHITE);

        JLabel label = new JLabel(message);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(TEXT_MUTED);

        overlay.add(label);

        viewport.setView(overlay);
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
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.setPreferredSize(new Dimension(100, 34));
        
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
    
    private String resolveUserName(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return "Unassigned";
        }
        User user = userDAO.findById(userId);
        return user != null ? user.getFullName() : userId;
    }
    
    public void refresh() {
        refreshDashboard();
    }
    
    @Override
    protected void addEventHandlers() {
    }
}