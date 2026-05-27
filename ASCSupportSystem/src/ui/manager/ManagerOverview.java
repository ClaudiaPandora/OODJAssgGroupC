package ui.manager;

import dao.AppointmentDAO;
import dao.CommentDAO;
import dao.PaymentDAO;
import dao.UserDAO;
import enums.AppointmentStatus;
import enums.ServiceType;
import models.Appointment;
import models.Comment;
import models.Payment;
import models.Technician;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ManagerOverview extends BasePanel {
    
    private static final long serialVersionUID = 1L;
    
    private User currentUser;
    private AppointmentDAO appointmentDAO;
    private UserDAO userDAO;
    private CommentDAO commentDAO;
    private PaymentDAO paymentDAO;
    private JButton refreshButton;
    private JPanel statsPanel;
    private JPanel alertsPanel;
    private JPanel techStatusPanel;
    private JPanel reviewPanel;
    private JPanel reviewContentPanel;
    private JPanel alertsContentPanel;
    private JPanel techStatusContentPanel;
    
    private final Color CARD_BORDER = new Color(226, 232, 240);
    private final Color LIGHT_BG = new Color(248, 250, 252);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color WARNING_ORANGE = new Color(234, 88, 12);
    private final Color DANGER_RED = new Color(220, 38, 38);
    private final Color SUCCESS_GREEN = new Color(34, 197, 94);
    private final Color INFO_BLUE = new Color(59, 130, 246);
    
    public ManagerOverview(User user) {
        this.currentUser = user;
        refreshData();
        setBackground(PANEL_BG);
        
        initializeComponents();
        setupLayout();
        addEventHandlers();
        
        refreshStats(statsPanel);
        refreshAlerts();
        refreshTechStatus(techStatusContentPanel);
        refreshReview();
    }
    
    private void refreshData() {
        this.appointmentDAO = new AppointmentDAO();
        this.userDAO = new UserDAO();
        this.commentDAO = new CommentDAO();
        this.paymentDAO = new PaymentDAO();
    }
    
    @Override
    protected void initializeComponents() {
        refreshButton = createStyledButton("Refresh", new Color(34, 197, 94));
        refreshButton.addActionListener(e -> refreshDashboard());
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(PANEL_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
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
        
        alertsPanel = createAlertsPanel();
        contentRow.add(alertsPanel);
        
        JPanel rightPanel = createRightPanel();
        contentRow.add(rightPanel);
        
        mainPanel.add(contentRow);
        
        // Remove JScrollPane - add mainPanel directly
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_BG);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        headerPanel.setPreferredSize(new Dimension(0, 40));
        
        JLabel titleLabel = new JLabel("System Overview");
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
        
        List<Payment> payments = paymentDAO.readAll();
        Set<String> paidAppointmentIds = new HashSet<>();
        
        for (Payment p : payments) {
            paidAppointmentIds.add(p.getAppointmentId());
        }
        
        // Calculate revenue based on TODAY's payment date
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
        
        panel.add(createStatCard("Appointments Today", String.valueOf(todayAppointments.size()), null, INFO_BLUE));
        panel.add(createStatCard("Ongoing Services", String.valueOf(ongoingCount), null, WARNING_ORANGE));
        panel.add(createStatCard("Completed Today", String.valueOf(completedCount), null, SUCCESS_GREEN));
        panel.add(createStatCard("Revenue Today", String.format("RM %.2f", revenue), null, new Color(168, 85, 247)));
        
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
        
        return card;
    }
    
    private JPanel createAlertsPanel() {
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
        
        JLabel titleLabel = new JLabel("Operational Alerts");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(WARNING_ORANGE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel descLabel = new JLabel("Critical issues requiring attention");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descLabel.setForeground(TEXT_MUTED);
        headerPanel.add(descLabel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        alertsContentPanel = new JPanel();
        alertsContentPanel.setLayout(new BoxLayout(alertsContentPanel, BoxLayout.Y_AXIS));
        alertsContentPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(alertsContentPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(0, 280));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void refreshAlerts() {
        if (alertsContentPanel == null) return;
        
        alertsContentPanel.removeAll();
        
        List<Appointment> allAppointments = appointmentDAO.readAll();
        List<Payment> payments = paymentDAO.readAll();
        String today = DateUtils.getCurrentDate();
        Set<String> paidAppointmentIds = new HashSet<>();
        
        for (Payment p : payments) {
            paidAppointmentIds.add(p.getAppointmentId());
        }
        
        // 1. Financial / Payment Risk - Completed appointments awaiting payment ONLY
        List<Appointment> completedUnpaid = new ArrayList<>();
        for (Appointment a : allAppointments) {
            if (a.getStatus() == AppointmentStatus.COMPLETED && !paidAppointmentIds.contains(a.getId())) {
                completedUnpaid.add(a);
            }
        }
        
        // Remove overdue logic - only show completed unpaid appointments
        int totalPending = completedUnpaid.size();
        
        // 2. Technician Issues - Appointments without technician assignment
        List<Appointment> unassignedAppointments = new ArrayList<>();
        for (Appointment a : allAppointments) {
            if (a.getStatus() == AppointmentStatus.PENDING && 
                (a.getTechnicianId() == null || a.getTechnicianId().trim().isEmpty())) {
                unassignedAppointments.add(a);
            }
        }
        
        // 3. Scheduling / Future Load - Upcoming appointments (next 7 days)
        LocalDate todayDate = LocalDate.parse(today);
        LocalDate weekLater = todayDate.plusDays(7);
        List<Appointment> upcomingAppointments = new ArrayList<>();
        for (Appointment a : allAppointments) {
            if (a.getDate() != null && a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.COMPLETED) {
                try {
                    LocalDate apptDate = LocalDate.parse(a.getDate());
                    if (!apptDate.isBefore(todayDate) && !apptDate.isAfter(weekLater)) {
                        upcomingAppointments.add(a);
                    }
                } catch (Exception e) {
                    // Skip invalid dates
                }
            }
        }
        
        // Find peak day
        String peakDay = "";
        int maxCount = 0;
        java.util.Map<String, Integer> dayCount = new java.util.HashMap<>();
        for (Appointment a : upcomingAppointments) {
            dayCount.put(a.getDate(), dayCount.getOrDefault(a.getDate(), 0) + 1);
        }
        for (java.util.Map.Entry<String, Integer> entry : dayCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                peakDay = entry.getKey();
            }
        }
        
        // Tomorrow's workload
        String tomorrow = todayDate.plusDays(1).toString();
        long tomorrowCount = upcomingAppointments.stream()
            .filter(a -> a.getDate() != null && a.getDate().equals(tomorrow))
            .count();
        
        // Create vertical cards container
        JPanel cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBackground(Color.WHITE);
        cardsContainer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cardsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Card 1: Financial / Payment Risk - Only completed appointments awaiting payment
        JPanel card1 = createAlertCard(
            "PAYMENT RISK",
            String.valueOf(totalPending),
            getFinancialDetail(completedUnpaid.size()),
            DANGER_RED
        );
        card1.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsContainer.add(card1);
        cardsContainer.add(Box.createVerticalStrut(10));
        
        // Card 2: Technician Issues
        JPanel card2 = createAlertCard(
            "TECHNICIAN ISSUES",
            String.valueOf(unassignedAppointments.size()),
            unassignedAppointments.size() + " appointment(s) without technician",
            unassignedAppointments.size() > 0 ? WARNING_ORANGE : SUCCESS_GREEN
        );
        card2.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsContainer.add(card2);
        cardsContainer.add(Box.createVerticalStrut(10));
        
        // Card 3: Scheduling Load
        String schedulingDetail = upcomingAppointments.size() + " upcoming appointment(s) for next 7 days";
        if (tomorrowCount > 0) {
            schedulingDetail += " · Tomorrow: " + tomorrowCount;
        }
        JPanel card3 = createAlertCard(
            "SCHEDULING LOAD",
            String.valueOf(upcomingAppointments.size()),
            schedulingDetail,
            INFO_BLUE
        );
        card3.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsContainer.add(card3);
        cardsContainer.add(Box.createVerticalStrut(10));
        
        // Card 4: Peak Day
        String peakInfo = peakDay.isEmpty() ? "No appointments this week" : formatDate(peakDay) + " (" + maxCount + " appointment(s))";
        JPanel card4 = createAlertCard(
            "PEAK DAY",
            peakDay.isEmpty() ? "0" : String.valueOf(maxCount),
            peakInfo,
            new Color(168, 85, 247)
        );
        card4.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsContainer.add(card4);
        
        alertsContentPanel.add(cardsContainer);
        
        alertsContentPanel.revalidate();
        alertsContentPanel.repaint();
    }

    private String getFinancialDetail(int completedUnpaid) {
        if (completedUnpaid == 0) {
            return "All payments up to date";
        }
        if (completedUnpaid == 1) {
            return completedUnpaid + " completed appointment awaiting payment";
        }
        return completedUnpaid + " completed appointments awaiting payment";
    }

    private String getFinancialDetail(int completedUnpaid, int overdueUnpaid) {
        if (completedUnpaid == 0 && overdueUnpaid == 0) {
            return "All payments up to date";
        }
        if (completedUnpaid > 0 && overdueUnpaid > 0) {
            return completedUnpaid + " pending · " + overdueUnpaid + " overdue";
        }
        if (completedUnpaid > 0) {
            return completedUnpaid + " pending payment(s)";
        }
        return overdueUnpaid + " overdue payment(s)";
    }

    private JPanel createAlertCard(String title, String value, String detail, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        card.setPreferredSize(new Dimension(0, 75));
        
        // Left accent bar
        JPanel accentBar = new JPanel();
        accentBar.setBackground(accentColor);
        accentBar.setPreferredSize(new Dimension(4, 0));
        card.add(accentBar, BorderLayout.WEST);
        
        // Content panel
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLabel.setForeground(new Color(100, 116, 139));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(4));
        
        // Value and detail row
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(accentColor);
        rowPanel.add(valueLabel);
        
        rowPanel.add(Box.createHorizontalStrut(12));
        
        // Create detail label with background - FIXED: wraps text only
        JLabel detailLabel = new JLabel(detail);
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        detailLabel.setForeground(getDetailTextColor(title));
        detailLabel.setOpaque(true);
        detailLabel.setBackground(getDetailBackgroundColor(title));
        detailLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        
        rowPanel.add(detailLabel);
        rowPanel.add(Box.createHorizontalGlue());
        
        content.add(rowPanel);
        
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }

    private Color getDetailBackgroundColor(String title) {
        switch (title) {
            case "PAYMENT RISK":
                return new Color(254, 242, 242);
            case "TECHNICIAN ISSUES":
                return new Color(255, 251, 235);
            case "SCHEDULING LOAD":
                return new Color(239, 246, 255);
            case "PEAK DAY":
                return new Color(245, 243, 255);
            default:
                return new Color(248, 250, 252);
        }
    }

    private Color getDetailTextColor(String title) {
        switch (title) {
            case "PAYMENT RISK":
                return new Color(185, 28, 28);
            case "TECHNICIAN ISSUES":
                return new Color(180, 83, 9);
            case "SCHEDULING LOAD":
                return new Color(29, 78, 216);
            case "PEAK DAY":
                return new Color(126, 34, 206);
            default:
                return new Color(55, 65, 81);
        }
    }

    private String formatDate(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            return date.format(formatter);
        } catch (Exception e) {
            return dateString;
        }
    }

    private void addAlertSection(String title, Color color) {
        JPanel sectionPanel = new JPanel(new BorderLayout());
        sectionPanel.setBackground(Color.WHITE);
        sectionPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
        sectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        sectionPanel.setPreferredSize(new Dimension(0, 30));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(color);
        sectionPanel.add(titleLabel, BorderLayout.WEST);
        
        alertsContentPanel.add(sectionPanel);
    }

    private void addAlertItem(String text, Color dotColor, JPanel panel) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(BorderFactory.createEmptyBorder(4, 24, 4, 12));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        itemPanel.setPreferredSize(new Dimension(0, 28));
        
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        textLabel.setForeground(new Color(60, 60, 60));
        itemPanel.add(textLabel, BorderLayout.WEST);
        
        panel.add(itemPanel);
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BG);
        
        techStatusPanel = createTechStatusPanel();
        panel.add(techStatusPanel);
        panel.add(Box.createVerticalStrut(8));
        
        reviewPanel = createReviewPanel();
        panel.add(reviewPanel);
        
        return panel;
    }
    
    private JPanel createTechStatusPanel() {
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
        
        JLabel titleLabel = new JLabel("Technician Status");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(SUCCESS_GREEN);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel descLabel = new JLabel("Current technician availability");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descLabel.setForeground(TEXT_MUTED);
        headerPanel.add(descLabel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        techStatusContentPanel = new JPanel();
        techStatusContentPanel.setLayout(new BoxLayout(techStatusContentPanel, BoxLayout.Y_AXIS));
        techStatusContentPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(techStatusContentPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void refreshTechStatus(JPanel contentPanel) {
        contentPanel.removeAll();
        
        List<Technician> techs = userDAO.readTechnicians();
        List<Appointment> appointments = appointmentDAO.readAll();
        String today = DateUtils.getCurrentDate();
        String currentTime = getCurrentTime();
        
        for (int i = 0; i < techs.size(); i++) {
            Technician tech = techs.get(i);
            boolean isBusy = false;
            int todayJobs = 0;
            
            for (Appointment a : appointments) {
                if (tech.getId().equals(a.getTechnicianId())) {
                    if (a.getDate() != null && a.getDate().equals(today)) {
                        todayJobs++;
                        if (a.getStatus() == AppointmentStatus.ASSIGNED && isOngoing(a.getStartTime(), currentTime, a.getServiceType())) {
                            isBusy = true;
                        }
                    }
                }
            }
            
            JPanel techItem = new JPanel(new BorderLayout());
            techItem.setBackground(Color.WHITE);
            techItem.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            techItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            techItem.setPreferredSize(new Dimension(0, 44));
            
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            leftPanel.setBackground(Color.WHITE);
            
            JPanel avatar = createAvatar(tech.getFullName());
            avatar.setPreferredSize(new Dimension(28, 28));
            leftPanel.add(avatar);
            
            JLabel nameLabel = new JLabel(tech.getFullName());
            nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            nameLabel.setForeground(new Color(31, 41, 55));
            leftPanel.add(nameLabel);
            
            JLabel jobsLabel = new JLabel(todayJobs + " jobs");
            jobsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            jobsLabel.setForeground(TEXT_MUTED);
            leftPanel.add(jobsLabel);
            
            techItem.add(leftPanel, BorderLayout.WEST);
            
            JLabel statusLabel = new JLabel(isBusy ? "BUSY" : "AVAILABLE");
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
            statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
            
            if (isBusy) {
                statusLabel.setForeground(WARNING_ORANGE);
                statusLabel.setBackground(new Color(255, 237, 213));
            } else {
                statusLabel.setForeground(SUCCESS_GREEN);
                statusLabel.setBackground(new Color(220, 252, 231));
            }
            statusLabel.setOpaque(true);
            techItem.add(statusLabel, BorderLayout.EAST);
            
            contentPanel.add(techItem);
            
            if (i < techs.size() - 1) {
                JSeparator separator = new JSeparator();
                separator.setForeground(new Color(230, 230, 230));
                separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                contentPanel.add(separator);
            }
        }
        
        if (techs.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setPreferredSize(new Dimension(0, 100));
            
            JLabel emptyLabel = new JLabel("No technicians available");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            emptyLabel.setForeground(TEXT_MUTED);
            emptyPanel.add(emptyLabel);
            contentPanel.add(emptyPanel);
        }
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
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
    
    private JPanel createReviewPanel() {
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
        
        JLabel titleLabel = new JLabel("Latest Customer Comments");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(NAVY_BLUE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel descLabel = new JLabel("Recent feedback and ratings");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descLabel.setForeground(TEXT_MUTED);
        headerPanel.add(descLabel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        reviewContentPanel = new JPanel();
        reviewContentPanel.setLayout(new BoxLayout(reviewContentPanel, BoxLayout.Y_AXIS));
        reviewContentPanel.setBackground(Color.WHITE);
        reviewContentPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        
        panel.add(reviewContentPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private void refreshReview() {
        if (reviewContentPanel == null) return;
        
        reviewContentPanel.removeAll();
        
        List<Comment> comments = commentDAO.readAll();
        
        List<Comment> ratedComments = new ArrayList<>();
        for (Comment c : comments) {
            if (c.getRating() > 0) {
                ratedComments.add(c);
            }
        }
        
        if (ratedComments.isEmpty()) {
            JLabel emptyLabel = new JLabel("No customer comments with ratings yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            emptyLabel.setForeground(TEXT_MUTED);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            reviewContentPanel.add(emptyLabel);
        } else {
            int start = Math.max(0, ratedComments.size() - 3);
            int itemCount = 0;
            for (int i = ratedComments.size() - 1; i >= start; i--) {
                Comment comment = ratedComments.get(i);
                
                String targetName = "";
                if (comment.getTechnicianId() != null && !comment.getTechnicianId().isEmpty()) {
                    User technician = userDAO.findById(comment.getTechnicianId());
                    targetName = technician != null ? technician.getFullName() : "Technician";
                } else if (comment.getCounterStaffId() != null && !comment.getCounterStaffId().isEmpty()) {
                    User staff = userDAO.findById(comment.getCounterStaffId());
                    targetName = staff != null ? staff.getFullName() : "Counter Staff";
                }
                
                JPanel reviewItem = new JPanel();
                reviewItem.setLayout(new BoxLayout(reviewItem, BoxLayout.Y_AXIS));
                reviewItem.setBackground(Color.WHITE);
                reviewItem.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
                reviewItem.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                JLabel ratingLabel = new JLabel("Rating: " + comment.getRating() + "/5");
                ratingLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                ratingLabel.setForeground(new Color(234, 179, 8));
                ratingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                reviewItem.add(ratingLabel);
                reviewItem.add(Box.createVerticalStrut(4));
                
                String commentText = comment.getContent();
                if (commentText == null || commentText.trim().isEmpty()) {
                    commentText = "No comment provided";
                }
                
                JTextArea commentArea = new JTextArea(commentText);
                commentArea.setEditable(false);
                commentArea.setLineWrap(true);
                commentArea.setWrapStyleWord(true);
                commentArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                commentArea.setForeground(new Color(80, 80, 80));
                commentArea.setBackground(Color.WHITE);
                commentArea.setBorder(null);
                commentArea.setAlignmentX(Component.LEFT_ALIGNMENT);
                reviewItem.add(commentArea);
                reviewItem.add(Box.createVerticalStrut(3));
                
                JLabel targetLabel = new JLabel("→ To: " + targetName);
                targetLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                targetLabel.setForeground(TEXT_MUTED);
                targetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                reviewItem.add(targetLabel);
                
                reviewContentPanel.add(reviewItem);
                itemCount++;
                
                if (itemCount < 3 && i > start) {
                    JSeparator separator = new JSeparator();
                    separator.setForeground(new Color(230, 230, 230));
                    separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    reviewContentPanel.add(separator);
                }
            }
        }
        
        reviewContentPanel.revalidate();
        reviewContentPanel.repaint();
    }
    
    private void refreshDashboard() {
        refreshData();
        refreshStats(statsPanel);
        refreshAlerts();
        refreshTechStatus(techStatusContentPanel);
        refreshReview();
        
        try {
            Component[] components = getComponents();
            if (components.length > 0 && components[0] instanceof JPanel) {
                JPanel mainPanel = (JPanel) components[0];
                Component[] mainComponents = mainPanel.getComponents();
                if (mainComponents.length > 0 && mainComponents[0] instanceof JPanel) {
                    JPanel headerPanel = (JPanel) mainComponents[0];
                    Component[] headerComponents = headerPanel.getComponents();
                    if (headerComponents.length > 1 && headerComponents[1] instanceof JPanel) {
                        JPanel rightPanel = (JPanel) headerComponents[1];
                        Component[] rightComponents = rightPanel.getComponents();
                        if (rightComponents.length > 0 && rightComponents[0] instanceof JLabel) {
                            ((JLabel) rightComponents[0]).setText(DateUtils.getCurrentDate());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not update date label: " + e.getMessage());
        }
        
        revalidate();
        repaint();
        
        JOptionPane.showMessageDialog(this, "Dashboard data has been refreshed.", 
            "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private JPanel createAvatar(String fullName) {
        String initials = getInitials(fullName);
        
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = Math.min(getWidth(), getHeight());
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                g2d.setColor(NAVY_BLUE);
                g2d.fillOval(x, y, size, size);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(initials)) / 2;
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(initials, textX, textY);
            }
        };
        
        avatarPanel.setPreferredSize(new Dimension(28, 28));
        avatarPanel.setMinimumSize(new Dimension(28, 28));
        avatarPanel.setMaximumSize(new Dimension(28, 28));
        avatarPanel.setBackground(Color.WHITE);
        avatarPanel.setOpaque(false);
        
        return avatarPanel;
    }
    
    private String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "?";
        String[] parts = fullName.split(" ");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        }
        return fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
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
    
    @Override
    protected void addEventHandlers() {
    }
}