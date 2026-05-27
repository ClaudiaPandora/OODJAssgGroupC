package ui.technician;

import dao.AppointmentDAO;
import dao.CommentDAO;
import dao.PaymentDAO;
import dao.FeedbackDAO;
import dao.UserDAO;
import enums.AppointmentStatus;
import models.Appointment;
import models.Comment;
import models.Payment;
import models.Feedback;
import models.User;
import ui.common.BasePanel;
import utils.DateUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TechnicianOverviewPanel extends BasePanel {

    private final User currentTechnician;
    private AppointmentDAO appointmentDAO;
    private UserDAO userDAO;
    private CommentDAO commentDAO;
    private FeedbackDAO techNoteDAO;
    private PaymentDAO paymentDAO;
    private JButton refreshButton;

    private JLabel completedJobsLabel;
    private JLabel activeJobsLabel;
    private JLabel revenueLabel;
    private JLabel avgRatingLabel;

    private JTable scheduleTable;
    private DefaultTableModel scheduleTableModel;
    private JTable priorityTable;
    private JTable activityTable;
    private DefaultTableModel priorityTableModel;
    private DefaultTableModel activityTableModel;
    private JPanel notificationsListPanel;
    private JPanel statsPanel;
    private JComboBox<String> dateCombo;
    
    private Set<String> paidAppointmentIds;
    
    private final Color CARD_BORDER = new Color(226, 232, 240);
    private final Color LIGHT_BG = new Color(248, 250, 252);
    private final Color TABLE_HEADER_BG = new Color(244, 246, 250);
    private final Color ROW_SEPARATOR = new Color(231, 235, 240);
    private final Color TABLE_SELECTION = new Color(232, 240, 254);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color GREEN = new Color(34, 197, 94);
    private final Color BLUE = new Color(59, 130, 246);
    private final Color ORANGE = new Color(234, 179, 8);
    private final Color TEAL = new Color(16, 185, 129);
    private final Color AVAILABLE_COLOR = new Color(34, 197, 94);
    private final Color BUSY_COLOR = new Color(220, 38, 38);
    private final Color NAVY_DARK = new Color(31, 66, 99);

    public TechnicianOverviewPanel(User technician) {
        this.currentTechnician = technician;
        this.appointmentDAO = new AppointmentDAO();
        this.userDAO = new UserDAO();
        this.commentDAO = new CommentDAO();
        this.techNoteDAO = new FeedbackDAO();
        this.paymentDAO = new PaymentDAO();
        this.paidAppointmentIds = new HashSet<>();

        setBackground(PANEL_BG);
        initializeComponents();
        setupLayout();
        addEventHandlers();
        refreshDashboard();
        
        // Load schedule automatically when panel is created
        SwingUtilities.invokeLater(() -> loadSchedule());
    }

    private void refreshData() {
        this.appointmentDAO = new AppointmentDAO();
        this.userDAO = new UserDAO();
        this.commentDAO = new CommentDAO();
        this.techNoteDAO = new FeedbackDAO();
        this.paymentDAO = new PaymentDAO();
        refreshPaidAppointmentIds();
        refreshDashboard();
        loadSchedule();
    }

    @Override
    protected void initializeComponents() {
        completedJobsLabel = createStatValueLabel("0");
        activeJobsLabel = createStatValueLabel("0");
        revenueLabel = createStatValueLabel("RM 0");
        avgRatingLabel = createStatValueLabel("0.0");

        refreshButton = createStyledButton("Refresh", GREEN);
        refreshButton.addActionListener(e -> {
            refreshData();
            JOptionPane.showMessageDialog(this, "Dashboard data has been refreshed.", 
                "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
        });

        // Schedule components
        dateCombo = new JComboBox<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i <= 30; i++) {
            LocalDate date = today.plusDays(i);
            String label = date.format(formatter);
            if (i == 0) {
                label += "  (Today)";
            } else if (i == 1) {
                label += "  (Tomorrow)";
            }
            dateCombo.addItem(label);
        }
        dateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateCombo.setBackground(Color.WHITE);
        dateCombo.setPreferredSize(new Dimension(200, 34));
        
        // Add listener to auto-load schedule when date changes
        dateCombo.addActionListener(e -> loadSchedule());

        // Schedule table model
        String[] scheduleColumns = new String[10];
        scheduleColumns[0] = "Time / Technician";
        for (int hour = 9; hour <= 17; hour++) {
            scheduleColumns[hour - 8] = String.format("%02d:00", hour);
        }
        
        scheduleTableModel = new DefaultTableModel(scheduleColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scheduleTable = new JTable(scheduleTableModel);
        setupScheduleTableStyle();

        priorityTableModel = new DefaultTableModel(
                new String[]{"Appointment", "Date & Time", "Customer", "Service", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        activityTableModel = new DefaultTableModel(
                new String[]{"Appointment", "Date", "Customer", "Service", "Status", "Amount"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        priorityTable = createStyledTable(priorityTableModel);
        activityTable = createStyledTable(activityTableModel);
        
        setupTableStyle(priorityTable, new int[]{80, 130, 150, 80, 100});
        setupTableStyle(activityTable, new int[]{80, 90, 150, 80, 100, 100});

        notificationsListPanel = new JPanel();
        notificationsListPanel.setLayout(new BoxLayout(notificationsListPanel, BoxLayout.Y_AXIS));
        notificationsListPanel.setBackground(Color.WHITE);
    }
    
    private void setupScheduleTableStyle() {
        scheduleTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        scheduleTable.setRowHeight(42);
        scheduleTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        scheduleTable.setFillsViewportHeight(true);
        scheduleTable.setFocusable(false);
        scheduleTable.setRowSelectionAllowed(false);
        scheduleTable.setShowVerticalLines(true);
        scheduleTable.setShowHorizontalLines(false);
        scheduleTable.setGridColor(ROW_SEPARATOR);
        scheduleTable.setIntercellSpacing(new Dimension(1, 0));
        
        JTableHeader header = scheduleTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(NAVY_DARK);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));

        scheduleTable.setDefaultRenderer(Object.class, new ScheduleCellRenderer());
        
        // Set column widths
        scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        for (int i = 1; i < scheduleTable.getColumnCount(); i++) {
            scheduleTable.getColumnModel().getColumn(i).setPreferredWidth(72);
        }
    }
    
    private void setupTableStyle(JTable table, int[] columnWidths) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(45);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setGridColor(ROW_SEPARATOR);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(new Color(31, 41, 55));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(NAVY_DARK);
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
            if (i < columnWidths.length) {
                table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
            }
        }
        
        table.setDefaultRenderer(Object.class, new TableCellRenderer());
    }

    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(PANEL_BG);

        mainPanel.add(createHeaderPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        statsPanel.setBackground(PANEL_BG);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        statsPanel.setPreferredSize(new Dimension(0, 75));
        mainPanel.add(statsPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Middle panel with Priority Jobs and Notifications
        mainPanel.add(createMiddlePanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Schedule panel
        JPanel schedulePanel = createSchedulePanel();
        schedulePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        schedulePanel.setPreferredSize(new Dimension(0, 250));
        schedulePanel.setMinimumSize(new Dimension(0, 250));
        mainPanel.add(schedulePanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(PANEL_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_BG);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        headerPanel.setPreferredSize(new Dimension(0, 40));
        
        JLabel titleLabel = new JLabel("My Dashboard");
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
    
    private void refreshStats(JPanel panel) {
        panel.removeAll();
        
        List<Appointment> allAppointments = appointmentDAO.findByTechnicianId(currentTechnician.getId());
        String today = DateUtils.getCurrentDate();
        String currentTime = getCurrentTime();
        
        int activeCount = 0;
        int completedCount = 0;
        double revenue = 0;
        
        List<Payment> payments = paymentDAO.readAll();
        Set<String> paidIds = new HashSet<>();
        for (Payment p : payments) {
            paidIds.add(p.getAppointmentId());
        }
        
        for (Appointment a : allAppointments) {
            if (a.getStatus() == AppointmentStatus.ASSIGNED && a.getDate().equals(today) && isOngoing(a.getStartTime(), currentTime)) {
                activeCount++;
            }
            if (a.getStatus() == AppointmentStatus.COMPLETED) {
                completedCount++;
            }
            if (a.getStatus() == AppointmentStatus.COMPLETED && paidIds.contains(a.getId())) {
                revenue += getPaymentAmount(a);
            }
        }
        
        panel.add(createStatCard("Active Jobs", String.valueOf(activeCount), null, BLUE));
        panel.add(createStatCard("Completed Jobs", String.valueOf(completedCount), null, GREEN));
        panel.add(createStatCard("Revenue", String.format("RM %.2f", revenue), null, TEAL));
        panel.add(createStatCard("Avg Rating", String.format("%.1f", calculateAverageRating()), null, ORANGE));
        
        panel.revalidate();
        panel.repaint();
    }
    
    private boolean isOngoing(String startTime, String currentTime) {
        if (startTime == null || startTime.isEmpty()) return false;
        
        try {
            String[] startParts = startTime.split(":");
            String[] currentParts = currentTime.split(":");
            
            int startMinutes = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
            int currentMinutes = Integer.parseInt(currentParts[0]) * 60 + Integer.parseInt(currentParts[1]);
            
            return currentMinutes >= startMinutes && currentMinutes < startMinutes + 180;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String getCurrentTime() {
        LocalTime now = LocalTime.now();
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
        
        if (label.equals("Active Jobs")) activeJobsLabel = valueText;
        else if (label.equals("Completed Jobs")) completedJobsLabel = valueText;
        else if (label.equals("Revenue")) revenueLabel = valueText;
        else if (label.equals("Avg Rating")) avgRatingLabel = valueText;
        
        return card;
    }

    private JPanel createMiddlePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setBackground(PANEL_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        panel.setPreferredSize(new Dimension(0, 280));

        panel.add(createPriorityJobsPanel());
        panel.add(createNotificationsPanel());
        return panel;
    }

    private JPanel createPriorityJobsPanel() {
        JPanel panel = createCardPanel("Priority Jobs");
        
        JScrollPane scrollPane = new JScrollPane(priorityTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton viewAllButton = createSecondaryButton("View All Jobs", BLUE);
        viewAllButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof ui.dashboard.DashboardFrame) {
                ((ui.dashboard.DashboardFrame) frame).switchToPanel("JOBS");
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(Color.WHITE);
        footer.add(viewAllButton);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createNotificationsPanel() {
        JPanel panel = createCardPanel("Notifications");
        JScrollPane scrollPane = new JScrollPane(notificationsListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // Header - with date selector
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LIGHT_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        JLabel titleLabel = new JLabel("Daily Schedule");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(NAVY_BLUE);
        header.add(titleLabel, BorderLayout.WEST);
        
        // Date selector panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controlsPanel.setBackground(LIGHT_BG);
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        dateCombo.setPreferredSize(new Dimension(180, 28));
        dateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        controlsPanel.add(dateCombo);
        header.add(controlsPanel, BorderLayout.EAST);
        
        panel.add(header, BorderLayout.NORTH);
        
        // Legend panel
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        legendPanel.setBackground(Color.WHITE);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(6, 16, 4, 16));
        legendPanel.add(makeLegendDot(AVAILABLE_COLOR));
        legendPanel.add(makeLegendText("Available"));
        legendPanel.add(Box.createHorizontalStrut(16));
        legendPanel.add(makeLegendDot(BUSY_COLOR));
        legendPanel.add(makeLegendText("Busy / Booked"));
        
        // Table with scroll pane (to show date row properly)
        JScrollPane tableScrollPane = new JScrollPane(scheduleTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        tableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        tableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tableScrollPane.setPreferredSize(new Dimension(0, 80));
        
        JSeparator topLine = new JSeparator();
        topLine.setForeground(ROW_SEPARATOR);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.add(topLine, BorderLayout.NORTH);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(legendPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JLabel makeLegendDot(Color color) {
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dot.setForeground(color);
        return dot;
    }

    private JLabel makeLegendText(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        label.setForeground(new Color(55, 65, 81));
        return label;
    }
    
    private void loadSchedule() {
        scheduleTableModel.setRowCount(0);

        String rawDate = (String) dateCombo.getSelectedItem();
        if (rawDate == null) {
            return;
        }

        String selectedDate = rawDate.trim().split("\\s")[0];
        List<Appointment> allAppointments = appointmentDAO.readAll();
        
        // Find appointments for this technician on this date
        List<Appointment> technicianAppointments = new ArrayList<>();
        for (Appointment appt : allAppointments) {
            if (appt.getTechnicianId() != null && 
                appt.getTechnicianId().equals(currentTechnician.getId()) &&
                appt.getDate() != null &&
                appt.getDate().equals(selectedDate) &&
                appt.getStatus() != AppointmentStatus.CANCELLED) {
                technicianAppointments.add(appt);
            }
        }
        
        // Create a row for the schedule
        Object[] row = new Object[10];
        row[0] = currentTechnician.getFullName();

        // Check each hour slot from 9 AM to 5 PM (no lunch break)
        for (int hour = 9; hour <= 17; hour++) {
            boolean isBusy = false;
            
            for (Appointment appt : technicianAppointments) {
                try {
                    String[] timeParts = appt.getStartTime().split(":");
                    int appointmentStartHour = Integer.parseInt(timeParts[0]);
                    int appointmentDuration = appt.getDuration();
                    int appointmentEndHour = appointmentStartHour + appointmentDuration;
                    
                    if (hour >= appointmentStartHour && hour < appointmentEndHour) {
                        isBusy = true;
                        break;
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            }
            
            row[hour - 8] = isBusy ? "BUSY" : "FREE";
        }

        scheduleTableModel.addRow(row);
        scheduleTable.revalidate();
        scheduleTable.repaint();
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LIGHT_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(NAVY_BLUE);
        header.add(titleLabel, BorderLayout.WEST);

        panel.add(header, BorderLayout.NORTH);
        return panel;
    }

    private JLabel createStatValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(new Color(31, 41, 55));
        return label;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(new Color(31, 41, 55));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }
    
    private JButton createSecondaryButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        button.setPreferredSize(new Dimension(100, 32));
        
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
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setPreferredSize(new Dimension(90, 34));
        
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

    private void refreshPaidAppointmentIds() {
        paidAppointmentIds.clear();
        for (Payment payment : paymentDAO.readAll()) {
            paidAppointmentIds.add(payment.getAppointmentId());
        }
    }
    
    private String getDisplayStatus(Appointment appointment) {
        return appointment.getStatus().toString();
    }
    
    private boolean isPaid(String appointmentId) {
        return paidAppointmentIds.contains(appointmentId);
    }
    
    private double getPaymentAmount(Appointment appointment) {
        for (Payment payment : paymentDAO.readAll()) {
            if (payment.getAppointmentId().equals(appointment.getId())) {
                return payment.getAmount();
            }
        }
        return appointment.getAmount();
    }

    private void refreshDashboard() {
        refreshPaidAppointmentIds();
        
        if (statsPanel != null) {
            refreshStats(statsPanel);
        }
        
        List<Appointment> appointments = appointmentDAO.findByTechnicianId(currentTechnician.getId());

        updatePriorityTable(appointments);
        updateRecentActivityTable(appointments);
        updateNotifications(appointments);
    }
    
    private double calculateAverageRating() {
        int totalRating = 0;
        int ratingCount = 0;

        for (Comment comment : commentDAO.readAll()) {
            if (currentTechnician.getId().equals(comment.getTechnicianId())) {
                int rating = comment.getRating();
                if (rating >= 1 && rating <= 5) {
                    totalRating += rating;
                    ratingCount++;
                }
            }
        }

        return ratingCount > 0 ? (double) totalRating / ratingCount : 0.0;
    }
    
    private void updatePriorityTable(List<Appointment> appointments) {
        priorityTableModel.setRowCount(0);

        List<Appointment> priorityJobs = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (appointment.getStatus() == AppointmentStatus.ASSIGNED
                    || appointment.getStatus() == AppointmentStatus.PENDING) {
                priorityJobs.add(appointment);
            }
        }

        sortOldestFirst(priorityJobs); // Changed from sortNewestFirst

        int limit = Math.min(priorityJobs.size(), 6);
        for (int i = 0; i < limit; i++) {
            Appointment appointment = priorityJobs.get(i);
            priorityTableModel.addRow(new Object[]{
                    appointment.getId(),
                    appointment.getDate() + " " + appointment.getStartTime(),
                    resolveUserName(appointment.getCustomerId()),
                    appointment.getServiceType(),
                    getDisplayStatus(appointment)
            });
        }

        if (priorityJobs.isEmpty()) {
            priorityTableModel.addRow(new Object[]{"-", "-", "No active jobs", "-", "-"});
        }
    }

    private void updateRecentActivityTable(List<Appointment> appointments) {
        activityTableModel.setRowCount(0);
        List<Appointment> recent = new ArrayList<>(appointments);
        sortOldestFirst(recent);

        int limit = Math.min(recent.size(), 8);
        for (int i = 0; i < limit; i++) {
            Appointment appointment = recent.get(i);
            double amount = getPaymentAmount(appointment);
            String status = appointment.getStatus().toString();
            
            if (status.equals("COMPLETED") && !isPaid(appointment.getId())) {
                status = "COMPLETED (Unpaid)";
            }
            
            activityTableModel.addRow(new Object[]{
                    appointment.getId(),
                    appointment.getDate(),
                    resolveUserName(appointment.getCustomerId()),
                    appointment.getServiceType(),
                    status,
                    amount > 0 ? String.format("RM %.2f", amount) : "-"
            });
        }

        if (recent.isEmpty()) {
            activityTableModel.addRow(new Object[]{"-", "-", "No appointment history", "-", "-", "-"});
        }
    }

    private void updateNotifications(List<Appointment> appointments) {
        notificationsListPanel.removeAll();
        List<String[]> notifications = buildNotifications(appointments);

        if (notifications.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setPreferredSize(new Dimension(0, 150));
            
            JLabel emptyLabel = new JLabel("All caught up - No pending notifications");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            emptyLabel.setForeground(TEXT_MUTED);
            emptyPanel.add(emptyLabel);
            notificationsListPanel.add(emptyPanel);
        } else {
            for (String[] item : notifications) {
                notificationsListPanel.add(createNotificationItem(item[0], item[1], resolveNotificationColor(item[2])));
                if (notifications.indexOf(item) < notifications.size() - 1) {
                    JSeparator separator = new JSeparator();
                    separator.setForeground(new Color(230, 230, 230));
                    separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    notificationsListPanel.add(separator);
                }
            }
        }

        notificationsListPanel.revalidate();
        notificationsListPanel.repaint();
    }

    private List<String[]> buildNotifications(List<Appointment> appointments) {
        List<String[]> items = new ArrayList<>();
        List<Feedback> notes = techNoteDAO.readAll();
        Set<String> processedAppointments = new HashSet<>();

        for (Appointment appointment : appointments) {
            if (processedAppointments.contains(appointment.getId())) {
                continue;
            }
            
            // Check for missing service note on COMPLETED appointments
            if (appointment.getStatus() == AppointmentStatus.COMPLETED && !hasTechnicianNote(notes, appointment.getId())) {
                items.add(new String[]{
                        "Missing service note",
                        "Appointment " + appointment.getId() + " is completed but no service notes have been added",
                        "ORANGE"
                });
                processedAppointments.add(appointment.getId());
                continue; // Skip other notifications for this appointment
            }
            
            // Check for new assigned jobs
            if (appointment.getStatus() == AppointmentStatus.ASSIGNED) {
                items.add(new String[]{
                        "New job assigned",
                        "Appointment " + appointment.getId() + " assigned for " + appointment.getDate() + " at " + appointment.getStartTime(),
                        "BLUE"
                });
                processedAppointments.add(appointment.getId());
                continue;
            }

            // Check for pending jobs
            if (appointment.getStatus() == AppointmentStatus.PENDING) {
                items.add(new String[]{
                        "Pending job awaiting assignment",
                        "Appointment " + appointment.getId() + " is pending and needs technician assignment",
                        "ORANGE"
                });
                processedAppointments.add(appointment.getId());
                continue;
            }
        }

        // Add rating notifications separately
        for (Comment comment : commentDAO.readAll()) {
            if (currentTechnician.getId().equals(comment.getTechnicianId())) {
                if (comment.getRating() <= 2) {
                    items.add(new String[]{
                            "Low rating alert",
                            "Appointment " + comment.getAppointmentId() + " received " + comment.getRating() + "/5 rating",
                            "RED"
                    });
                } else if (comment.getRating() >= 4) {
                    items.add(new String[]{
                            "Positive feedback",
                            "Appointment " + comment.getAppointmentId() + " received " + comment.getRating() + "/5 rating",
                            "GREEN"
                    });
                }
            }
        }

        return limitNotifications(items, 8);
    }
    
    private List<String[]> limitNotifications(List<String[]> items, int limit) {
        List<String[]> limited = new ArrayList<>();
        int count = Math.min(items.size(), limit);
        for (int i = 0; i < count; i++) {
            limited.add(items.get(i));
        }
        return limited;
    }

    private JPanel createNotificationItem(String title, String detail, Color accentColor) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel dot = new JPanel();
        dot.setBackground(accentColor);
        dot.setPreferredSize(new Dimension(4, 0));
        item.add(dot, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(new Color(31, 41, 55));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel detailLabel = new JLabel(detail);
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        detailLabel.setForeground(TEXT_MUTED);
        detailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(titleLabel);
        text.add(Box.createVerticalStrut(2));
        text.add(detailLabel);
        item.add(text, BorderLayout.CENTER);

        return item;
    }

    private boolean hasTechnicianNote(List<Feedback> notes, String appointmentId) {
        for (Feedback note : notes) {
            if (appointmentId.equals(note.getAppointmentId())
                    && currentTechnician.getId().equals(note.getTechnicianId())) {
                return true;
            }
        }
        return false;
    }

    private Color resolveNotificationColor(String type) {
        if ("GREEN".equals(type)) return GREEN;
        if ("RED".equals(type)) return new Color(220, 38, 38);
        if ("BLUE".equals(type)) return BLUE;
        return ORANGE;
    }

    private void sortOldestFirst(List<Appointment> appointments) {
        appointments.sort((a, b) -> {
            String aDate = a.getDate() == null ? "" : a.getDate();
            String bDate = b.getDate() == null ? "" : b.getDate();
            int dateCompare = aDate.compareTo(bDate);
            if (dateCompare != 0) {
                return dateCompare;
            }

            String aTime = a.getStartTime() == null ? "" : a.getStartTime();
            String bTime = b.getStartTime() == null ? "" : b.getStartTime();
            return aTime.compareTo(bTime);
        });
    }

    private String resolveUserName(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return "-";
        }
        User user = userDAO.findById(userId);
        return user != null ? user.getFullName() : userId;
    }

    @Override
    protected void addEventHandlers() {
    }
    
    private class ScheduleCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            Component component = super.getTableCellRendererComponent(
                    table,
                    value,
                    false,
                    false,
                    row,
                    column
            );

            JLabel label = (JLabel) component;
            label.setHorizontalAlignment(column == 0 ? JLabel.LEFT : JLabel.CENTER);
            
            // Add top border for the first row (date row / technician row)
            if (row == 0) {
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 1, 0, ROW_SEPARATOR),
                        BorderFactory.createEmptyBorder(10, column == 0 ? 12 : 4, 10, 4)
                ));
            } else {
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
                        BorderFactory.createEmptyBorder(10, column == 0 ? 12 : 4, 10, 4)
                ));
            }
            
            label.setFont(new Font("Segoe UI", column == 0 ? Font.BOLD : Font.PLAIN, 11));

            String text = value == null ? "" : value.toString().trim();

            if (column == 0) {
                label.setBackground(LIGHT_BG);
                label.setForeground(NAVY_DARK);
                label.setText(text);
            } else if (text.equalsIgnoreCase("BUSY")) {
                label.setBackground(new Color(254, 226, 226));
                label.setForeground(BUSY_COLOR);
                label.setText("● BUSY");
                label.setFont(new Font("Segoe UI", Font.BOLD, 10));
            } else if (text.equalsIgnoreCase("FREE")) {
                label.setBackground(new Color(220, 252, 231));
                label.setForeground(AVAILABLE_COLOR);
                label.setText("● FREE");
                label.setFont(new Font("Segoe UI", Font.BOLD, 10));
            } else {
                label.setBackground(Color.WHITE);
                label.setText(text);
            }

            return label;
        }
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
            
            if (column == 4 && value != null) {
                String status = value.toString();
                if (status.equals("ASSIGNED")) {
                    label.setForeground(BLUE);
                } else if (status.equals("PENDING")) {
                    label.setForeground(ORANGE);
                } else if (status.equals("COMPLETED")) {
                    label.setForeground(GREEN);
                } else if (status.contains("Unpaid")) {
                    label.setForeground(ORANGE);
                } else {
                    label.setForeground(new Color(31, 41, 55));
                }
            } else {
                label.setForeground(new Color(31, 41, 55));
            }
            
            if (!isSelected) {
                label.setBackground(Color.WHITE);
            } else {
                label.setBackground(TABLE_SELECTION);
            }
            
            return label;
        }
    }
}