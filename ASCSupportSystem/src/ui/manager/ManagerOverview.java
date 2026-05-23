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
import java.util.*;
import java.util.List;

public class ManagerOverview extends BasePanel {
    
    private static final long serialVersionUID = 1L;
    
    private User currentUser;
    private AppointmentDAO appointmentDAO;
    private UserDAO userDAO;
    private CommentDAO commentDAO;
    private PaymentDAO paymentDAO;
    private JButton refreshButton;
    private JPanel statsPanel;
    private JPanel activitiesPanel;
    private JPanel techStatusPanel;
    private JPanel reviewPanel;
    private JPanel reviewContentPanel;
    private JPanel activitiesContentPanel;
    private JPanel techStatusContentPanel;
    
    private final Color CARD_BORDER = new Color(226, 232, 240);
    private final Color LIGHT_BG = new Color(248, 250, 252);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    
    public ManagerOverview(User user) {
        this.currentUser = user;
        refreshData();
        setBackground(PANEL_BG);
        
        initializeComponents();
        setupLayout();
        addEventHandlers();
        
        refreshStats(statsPanel);
        refreshActivities();
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
        
        activitiesPanel = createActivitiesPanel();
        contentRow.add(activitiesPanel);
        
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
        Map<String, Double> paymentAmountMap = new HashMap<>();
        Map<String, String> paymentDateMap = new HashMap<>();
        
        for (Payment p : payments) {
            paidAppointmentIds.add(p.getAppointmentId());
            paymentAmountMap.put(p.getAppointmentId(), p.getAmount());
            paymentDateMap.put(p.getAppointmentId(), p.getPaymentDate());
        }
        
        // Calculate revenue based on TODAY's payment date
        for (Payment p : payments) {
            if (p.getPaymentDate() != null && p.getPaymentDate().equals(today)) {
                revenue += p.getAmount();
            }
        }
        
        for (Appointment a : todayAppointments) {
            if (a.getStatus() == AppointmentStatus.ASSIGNED) ongoingCount++;
            if (a.getStatus() == AppointmentStatus.COMPLETED) completedCount++;
        }
        
        panel.add(createStatCard("Appointments Today", String.valueOf(todayAppointments.size()), null, new Color(59, 130, 246)));
        panel.add(createStatCard("Ongoing Services", String.valueOf(ongoingCount), null, new Color(234, 88, 12)));
        panel.add(createStatCard("Completed Today", String.valueOf(completedCount), null, new Color(34, 197, 94)));
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
    
    private JPanel createActivitiesPanel() {
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
        
        JLabel titleLabel = new JLabel("Most Recent Activities");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(new Color(59, 130, 246));  // Blue color like the UI style
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel descLabel = new JLabel("Latest appointments and transactions");  // Added description
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descLabel.setForeground(TEXT_MUTED);
        headerPanel.add(descLabel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        activitiesContentPanel = new JPanel();
        activitiesContentPanel.setLayout(new BoxLayout(activitiesContentPanel, BoxLayout.Y_AXIS));
        activitiesContentPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(activitiesContentPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(0, 280));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private String getPaymentDate(String appointmentId, List<Payment> payments) {
        for (Payment p : payments) {
            if (p.getAppointmentId().equals(appointmentId)) {
                return p.getPaymentDate();
            }
        }
        return "0000-00-00"; // Default for unpaid
    }
    
    private void refreshActivities() {
        if (activitiesContentPanel == null) return;
        
        activitiesContentPanel.removeAll();
        
        List<Appointment> appointments = appointmentDAO.readAll();
        List<Payment> payments = paymentDAO.readAll();
        
        Set<String> paidAppointmentIds = new HashSet<>();
        Map<String, Double> paymentAmountMap = new HashMap<>();
        Map<String, String> paymentDateMap = new HashMap<>();
        
        for (Payment p : payments) {
            paidAppointmentIds.add(p.getAppointmentId());
            paymentAmountMap.put(p.getAppointmentId(), p.getAmount());
            paymentDateMap.put(p.getAppointmentId(), p.getPaymentDate());
        }
        
        // Create a list of activity items with their display date for sorting
        List<ActivityItem> activityItems = new ArrayList<>();
        
        // Add paid activities first (based on payment date)
        for (Payment p : payments) {
            Appointment appointment = null;
            for (Appointment a : appointments) {
                if (a.getId().equals(p.getAppointmentId())) {
                    appointment = a;
                    break;
                }
            }
            if (appointment != null) {
                ActivityItem item = new ActivityItem();
                item.type = "PAID";
                item.appointment = appointment;
                item.payment = p;
                item.displayDate = p.getPaymentDate(); // Sort by payment date
                activityItems.add(item);
            }
        }
        
        // Add non-paid activities (appointments without payment)
        for (Appointment a : appointments) {
            if (!paidAppointmentIds.contains(a.getId())) {
                ActivityItem item = new ActivityItem();
                item.type = "APPOINTMENT";
                item.appointment = a;
                item.displayDate = a.getDate(); // Sort by appointment date
                activityItems.add(item);
            }
        }
        
        // Sort by display date (newest first)
        activityItems.sort((i1, i2) -> i2.displayDate.compareTo(i1.displayDate));
        
        int count = 0;
        
        for (ActivityItem item : activityItems) {
            if (count >= 10) break;
            
            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setBackground(Color.WHITE);
            itemPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            itemPanel.setPreferredSize(new Dimension(0, 50));
            
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            leftPanel.setBackground(Color.WHITE);
            
            JLabel dotLabel = new JLabel("●");
            dotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            
            Color dotColor;
            String description;
            
            if (item.type.equals("PAID")) {
                dotColor = new Color(168, 85, 247); // Purple for paid
                User customer = userDAO.findById(item.appointment.getCustomerId());
                String customerName = customer != null ? customer.getFullName() : "Customer";
                String paymentDate = item.payment.getPaymentDate();
                String serviceDate = item.appointment.getDate();
                
                description = String.format("%s paid %.2f for %s service (scheduled: %s) on %s",
                    customerName,
                    item.payment.getAmount(),
                    item.appointment.getServiceType(),
                    serviceDate,
                    paymentDate
                );
            } else {
                // Appointment without payment
                if (item.appointment.getStatus() == AppointmentStatus.ASSIGNED) {
                    dotColor = new Color(59, 130, 246); // Blue
                    User customer = userDAO.findById(item.appointment.getCustomerId());
                    String customerName = customer != null ? customer.getFullName() : "Customer";
                    User technician = userDAO.findById(item.appointment.getTechnicianId());
                    String techName = technician != null ? technician.getFullName() : "Technician";
                    
                    description = String.format("%s assigned to %s for %s service on %s at %s",
                        customerName,
                        techName,
                        item.appointment.getServiceType(),
                        item.appointment.getDate(),
                        item.appointment.getStartTime()
                    );
                } else if (item.appointment.getStatus() == AppointmentStatus.COMPLETED) {
                    dotColor = new Color(34, 197, 94); // Green
                    User customer = userDAO.findById(item.appointment.getCustomerId());
                    String customerName = customer != null ? customer.getFullName() : "Customer";
                    
                    description = String.format("%s completed %s service on %s at %s (awaiting payment)",
                        customerName,
                        item.appointment.getServiceType(),
                        item.appointment.getDate(),
                        item.appointment.getStartTime()
                    );
                } else if (item.appointment.getStatus() == AppointmentStatus.PENDING) {
                    dotColor = new Color(234, 179, 8); // Yellow
                    User customer = userDAO.findById(item.appointment.getCustomerId());
                    String customerName = customer != null ? customer.getFullName() : "Customer";
                    
                    description = String.format("%s booked %s service for %s at %s",
                        customerName,
                        item.appointment.getServiceType(),
                        item.appointment.getDate(),
                        item.appointment.getStartTime()
                    );
                } else {
                    dotColor = new Color(107, 114, 128); // Gray
                    description = "Appointment " + item.appointment.getId() + " " + item.appointment.getStatus();
                }
            }
            
            dotLabel.setForeground(dotColor);
            leftPanel.add(dotLabel);
            
            JLabel textLabel = new JLabel(description);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            textLabel.setForeground(new Color(60, 60, 60));
            leftPanel.add(textLabel);
            
            itemPanel.add(leftPanel, BorderLayout.WEST);
            activitiesContentPanel.add(itemPanel);
            
            if (count < activityItems.size() - 1 && count < 9) {
                JSeparator separator = new JSeparator();
                separator.setForeground(new Color(230, 230, 230));
                separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                activitiesContentPanel.add(separator);
            }
            count++;
        }
        
        if (activityItems.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setPreferredSize(new Dimension(0, 150));
            
            JLabel emptyLabel = new JLabel("No recent activities");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            emptyLabel.setForeground(TEXT_MUTED);
            emptyPanel.add(emptyLabel);
            activitiesContentPanel.add(emptyPanel);
        }
        
        activitiesContentPanel.revalidate();
        activitiesContentPanel.repaint();
    }

    private class ActivityItem {
        String type;
        Appointment appointment;
        Payment payment;
        String displayDate;
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
        titleLabel.setForeground(new Color(34, 197, 94));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel descLabel = new JLabel("Current technician availability");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descLabel.setForeground(TEXT_MUTED);
        headerPanel.add(descLabel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        techStatusContentPanel = new JPanel();
        techStatusContentPanel.setLayout(new BoxLayout(techStatusContentPanel, BoxLayout.Y_AXIS));
        techStatusContentPanel.setBackground(Color.WHITE);
        
        // Use preferred size that fits content, scroll only when needed
        JScrollPane scrollPane = new JScrollPane(techStatusContentPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);
        // Set preferred height to fit 3 technicians (44px per row + separators)
        scrollPane.setPreferredSize(new Dimension(0, 150));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void refreshTechStatus(JPanel contentPanel) {
        contentPanel.removeAll();
        
        List<Technician> techs = userDAO.readTechnicians();
        List<Appointment> appointments = appointmentDAO.readAll();
        String today = DateUtils.getCurrentDate();
        
        for (int i = 0; i < techs.size(); i++) {
            Technician tech = techs.get(i);
            boolean isBusy = false;
            int todayJobs = 0;
            
            for (Appointment a : appointments) {
                if (tech.getId().equals(a.getTechnicianId())) {
                    if (a.getDate() != null && a.getDate().equals(today) && 
                        a.getStatus() == AppointmentStatus.ASSIGNED) {
                        isBusy = true;
                    }
                    if (a.getDate() != null && a.getDate().equals(today)) {
                        todayJobs++;
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
                statusLabel.setForeground(new Color(234, 88, 12));
                statusLabel.setBackground(new Color(255, 237, 213));
            } else {
                statusLabel.setForeground(new Color(34, 197, 94));
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
        reviewContentPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));  // Remove bottom padding
        
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
            // Show only last 3 comments (most recent)
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
                reviewItem.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));  // Remove bottom border, just padding
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
                
                // Add separator line only if this is NOT the last item (i.e., not the 3rd comment)
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
        refreshActivities();
        refreshTechStatus(techStatusContentPanel);
        refreshReview();
        
        revalidate();
        repaint();
        
        JOptionPane.showMessageDialog(this, "Dashboard data has been refreshed from files.", 
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