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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TechnicianOverviewPanel extends BasePanel {

    private final User currentTechnician;
    private final AppointmentDAO appointmentDAO;
    private final UserDAO userDAO;
    private final CommentDAO commentDAO;
    private final FeedbackDAO techNoteDAO;
    private final PaymentDAO paymentDAO;
    private JButton refreshButton;

    private JLabel completedJobsLabel;
    private JLabel activeJobsLabel;
    private JLabel revenueLabel;
    private JLabel avgRatingLabel;

    private JTable priorityTable;
    private JTable activityTable;
    private DefaultTableModel priorityTableModel;
    private DefaultTableModel activityTableModel;
    private JPanel notificationsListPanel;
    private JPanel statsPanel;
    
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
    private final Color PURPLE = new Color(139, 92, 246);
    private final Color PINK = new Color(236, 72, 153);

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
    }

    private void refreshData() {
        refreshPaidAppointmentIds();
        refreshDashboard();
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
        mainPanel.add(statsPanel);        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createMiddlePanel());
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createRecentActivityPanel());

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
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setBackground(PANEL_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        panel.setPreferredSize(new Dimension(0, 75));
        
        refreshStats(panel);
        
        return panel;
    }
    
    private void refreshStats(JPanel panel) {
        panel.removeAll();
        
        List<Appointment> allAppointments = appointmentDAO.findByTechnicianId(currentTechnician.getId());
        String today = DateUtils.getCurrentDate();
        
        List<Appointment> todayAppointments = new ArrayList<>();
        for (Appointment a : allAppointments) {
            if (a.getDate() != null && a.getDate().equals(today)) {
                todayAppointments.add(a);
            }
        }
        
        int ongoingCount = 0, completedCount = 0;
        double revenue = 0;
        
        List<Payment> payments = paymentDAO.readAll();
        Set<String> paidAppointmentIds = new HashSet<>();
        
        for (Payment p : payments) {
            paidAppointmentIds.add(p.getAppointmentId());
        }
        
        for (Appointment a : todayAppointments) {
            if (a.getStatus() == AppointmentStatus.ASSIGNED) ongoingCount++;
            if (a.getStatus() == AppointmentStatus.COMPLETED) completedCount++;
            if (paidAppointmentIds.contains(a.getId())) {
                revenue += getPaymentAmount(a);
            }
        }
        
        panel.add(createStatCard("Active Jobs", String.valueOf(ongoingCount), null, BLUE));
        panel.add(createStatCard("Completed Jobs", String.valueOf(completedCount), null, GREEN));
        panel.add(createStatCard("Revenue", String.format("RM %.0f", revenue), null, TEAL));
        panel.add(createStatCard("Avg Rating", String.format("%.1f", calculateAverageRating()), null, ORANGE));
        
        panel.revalidate();
        panel.repaint();
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
        
        // Update the label references
        if (label.equals("Active Jobs")) activeJobsLabel = valueText;
        else if (label.equals("Completed Jobs")) completedJobsLabel = valueText;
        else if (label.equals("Revenue")) revenueLabel = valueText;
        else if (label.equals("Avg Rating")) avgRatingLabel = valueText;
        
        return card;
    }

    private JPanel createMiddlePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setBackground(PANEL_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        panel.setPreferredSize(new Dimension(0, 300));

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

    private JPanel createRecentActivityPanel() {
        JPanel panel = createCardPanel("Recent Activity");
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        panel.setPreferredSize(new Dimension(0, 320));
        
        JScrollPane scrollPane = new JScrollPane(activityTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
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

    private JButton createPrimaryButton(String text) {
        return createStyledButton(text, NAVY_BLUE);
    }
    
    private JButton createSecondaryButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setPreferredSize(new Dimension(110, 32));
        
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
        if (paidAppointmentIds.contains(appointment.getId())) {
            return "COMPLETED";
        }
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
        
        refreshStats(statsPanel);
        
        List<Appointment> appointments = appointmentDAO.findByTechnicianId(currentTechnician.getId());

        updatePriorityTable(appointments);
        updateRecentActivityTable(appointments);
        updateNotifications(appointments);
    }
    
    private double calculateAverageRating() {
        int totalRating = 0;
        int ratingCount = 0;

        for (Comment comment : commentDAO.readAll()) {
            // Only count comments for this technician
            if (currentTechnician.getId().equals(comment.getTechnicianId())) {
                // Only count valid ratings (1-5), exclude 0
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

        sortNewestFirst(priorityJobs);

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
        sortNewestFirst(recent);

        int limit = Math.min(recent.size(), 8);
        for (int i = 0; i < limit; i++) {
            Appointment appointment = recent.get(i);
            double amount = getPaymentAmount(appointment);
            activityTableModel.addRow(new Object[]{
                    appointment.getId(),
                    appointment.getDate(),
                    resolveUserName(appointment.getCustomerId()),
                    appointment.getServiceType(),
                    getDisplayStatus(appointment),
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
            notificationsListPanel.add(createNotificationItem(
                    "All caught up",
                    "No urgent appointment updates or customer rating alerts.",
                    GREEN
            ));
        } else {
            for (String[] item : notifications) {
                notificationsListPanel.add(createNotificationItem(item[0], item[1], resolveNotificationColor(item[2])));
                // Add separator between notifications except last one
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
        Set<String> processedAppointments = new HashSet<>(); // Track processed appointments
        Set<String> processedComments = new HashSet<>(); // Track processed comment notifications

        for (Appointment appointment : appointments) {
            if (processedAppointments.contains(appointment.getId())) {
                continue; // Skip already processed appointments
            }
            
            if (appointment.getStatus() == AppointmentStatus.ASSIGNED && !isPaid(appointment.getId())) {
                items.add(new String[]{
                        "Assigned job waiting",
                        appointment.getId() + " is assigned for " + appointment.getDate() + " at " + appointment.getStartTime() + ".",
                        "BLUE"
                });
                processedAppointments.add(appointment.getId());
            }

            if (appointment.getStatus() == AppointmentStatus.PENDING && !isPaid(appointment.getId())) {
                items.add(new String[]{
                        "Pending job",
                        appointment.getId() + " is still pending and may need follow-up.",
                        "ORANGE"
                });
                processedAppointments.add(appointment.getId());
            }

            if ((appointment.getStatus() == AppointmentStatus.ASSIGNED
                    || appointment.getStatus() == AppointmentStatus.COMPLETED)
                    && !hasTechnicianNote(notes, appointment.getId()) && !isPaid(appointment.getId())) {
                items.add(new String[]{
                        "No service note",
                        appointment.getId() + " has no technician feedback or service note yet.",
                        "ORANGE"
                });
                processedAppointments.add(appointment.getId());
            }
        }

        // Process comments for this technician only, avoid duplicates
        for (Comment comment : commentDAO.readAll()) {
            // Only show comments for this specific technician
            if (currentTechnician.getId().equals(comment.getTechnicianId())) {
                String commentKey = comment.getAppointmentId() + "_" + comment.getRating();
                
                // Skip if already processed
                if (processedComments.contains(commentKey)) {
                    continue;
                }
                
                if (comment.getRating() <= 2) {
                    items.add(new String[]{
                            "Low customer rating",
                            "Appointment " + comment.getAppointmentId() + " received " + comment.getRating() + "/5. Review the comment.",
                            "RED"
                    });
                    processedComments.add(commentKey);
                } else if (comment.getRating() >= 4) {
                    items.add(new String[]{
                            "Positive feedback",
                            "Appointment " + comment.getAppointmentId() + " received " + comment.getRating() + "/5 from a customer.",
                            "GREEN"
                    });
                    processedComments.add(commentKey);
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
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel dot = new JPanel();
        dot.setBackground(accentColor);
        dot.setPreferredSize(new Dimension(4, 0));
        item.add(dot, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(31, 41, 55));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel detailLabel = new JLabel("<html>" + detail + "</html>");
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
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

    private void sortNewestFirst(List<Appointment> appointments) {
        appointments.sort((a, b) -> {
            String aDate = a.getDate() == null ? "" : a.getDate();
            String bDate = b.getDate() == null ? "" : b.getDate();
            int dateCompare = bDate.compareTo(aDate);
            if (dateCompare != 0) {
                return dateCompare;
            }

            String aTime = a.getStartTime() == null ? "" : a.getStartTime();
            String bTime = b.getStartTime() == null ? "" : b.getStartTime();
            return bTime.compareTo(aTime);
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

    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));

            if (!isSelected) {
                label.setBackground(Color.WHITE);
                String status = value == null ? "" : value.toString();

                if ("COMPLETED".equals(status)) {
                    label.setForeground(GREEN);
                } else if ("ASSIGNED".equals(status)) {
                    label.setForeground(BLUE);
                } else if ("PENDING".equals(status)) {
                    label.setForeground(ORANGE);
                } else {
                    label.setForeground(Color.BLACK);
                }
            }

            return label;
        }
    }
}