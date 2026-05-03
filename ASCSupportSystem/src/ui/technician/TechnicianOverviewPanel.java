package ui.technician;

import dao.AppointmentDAO;
import dao.CommentDAO;
import dao.FeedbackDAO;
import dao.UserDAO;
import dao.TechNoteDAO;
import enums.AppointmentStatus;
import enums.ServiceType;
import models.Appointment;
import models.Comment;
import models.Feedback;
import models.TechNote;
import models.User;
import ui.common.BasePanel;
import utils.DateUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class TechnicianOverviewPanel extends BasePanel {
    
    private static final long serialVersionUID = 1L;
    private User currentTechnician;
    private AppointmentDAO appointmentDAO;
    private UserDAO userDAO;
    private FeedbackDAO feedbackDAO;
    private CommentDAO commentDAO;
    private TechNoteDAO techNoteDAO;
    
    private JLabel completedJobsLabel;
    private JLabel ongoingJobsLabel;
    private JLabel revenueLabel;
    private JLabel todayJobsLabel;
    
    private JLabel avgRatingLabel;
    private JLabel completionRateLabel;
    private JLabel onTimeRateLabel;
    
    private JTable todayTable;
    private DefaultTableModel tableModel;
    private JPanel emptySchedulePanel;
    private JPanel scheduleContentPanel;
    private List<String> viewedAppointments = new ArrayList<>();
    
    public TechnicianOverviewPanel(User technician) {
        this.currentTechnician = technician;
        this.appointmentDAO = new AppointmentDAO();
        this.userDAO = new UserDAO();
        this.feedbackDAO = new FeedbackDAO();
        this.commentDAO = new CommentDAO();
        this.techNoteDAO = new TechNoteDAO();

        setBackground(PANEL_BG);
        initializeComponents();
        setupLayout();
        refreshDashboard();
    }
    
    @Override
    protected void initializeComponents() {
    	this.techNoteDAO = new TechNoteDAO();
    	
        completedJobsLabel = createKpiLabel("0");
        ongoingJobsLabel = createKpiLabel("0");
        revenueLabel = createKpiLabel("RM 0");
        todayJobsLabel = createKpiLabel("0");
        
        avgRatingLabel = createMetricLabel("0.0 / 5.0");
        completionRateLabel = createMetricLabel("0%");
        onTimeRateLabel = createMetricLabel("0%");
        
        tableModel = new DefaultTableModel(new String[]{
        	    "Appointment ID", "Date & Time", "Customer", "Service", "Status", "Action"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        todayTable = new JTable(tableModel);
        todayTable.setRowHeight(35);
        todayTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        todayTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        todayTable.getTableHeader().setBackground(new Color(240, 240, 240));
        todayTable.setSelectionBackground(new Color(0, 51, 153));
        todayTable.setSelectionForeground(Color.WHITE);
        todayTable.setShowGrid(true);
        todayTable.setGridColor(new Color(220, 220, 220));
        todayTable.getTableHeader().setReorderingAllowed(false);
        
        todayTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = todayTable.rowAtPoint(e.getPoint());
                int col = todayTable.columnAtPoint(e.getPoint());

                if (row >= 0 && col == 5) {
                    String id = (String) tableModel.getValueAt(row, 0);
                    Appointment a = appointmentDAO.findById(id);
                    if (a != null) {
                        showAppointmentDetails(a);

                        if (!viewedAppointments.contains(id)) {
                            viewedAppointments.add(id);
                        }
                    }
                }
            }
        });
        
        todayTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setOpaque(true);

                JButton btn = new JButton("VIEW");

                btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
                btn.setForeground(Color.WHITE);
                btn.setBackground(new Color(0, 51, 153));
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                panel.add(btn);

                return panel;
            }
        });
        
        todayTable.getColumnModel().getColumn(4).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value.toString();
                if (!isSelected) {
                    if (status.equals("ASSIGNED")) {
                        c.setForeground(new Color(255, 140, 0));
                    } else if (status.equals("COMPLETED")) {
                        c.setForeground(new Color(34, 197, 94));
                    } else {
                        c.setForeground(Color.GRAY);
                    }
                }
                return c;
            }
        });
        
        todayTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                JLabel c = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                String appointmentId = (String) table.getValueAt(row, 0);
                String time = String.valueOf(value);

                Appointment a = appointmentDAO.findById(appointmentId);

                boolean isNew24h = false;

                if (a != null) {
                    isNew24h = isWithin24Hours(a.getDate(), a.getStartTime())
                            && !viewedAppointments.contains(appointmentId);
                }

                if (isNew24h) {
                    c.setText("● " + time);
                } else {
                    c.setText(time);
                }

                if (isSelected) {
                    c.setForeground(Color.WHITE);
                } else {
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });
        
        emptySchedulePanel = new JPanel(new GridBagLayout());
        emptySchedulePanel.setBackground(new Color(250, 250, 250));
        
        JLabel emptyMessage = new JLabel("No appointments scheduled recently");
        emptyMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emptyMessage.setForeground(new Color(120, 120, 120));
        
        JLabel emptyHint = new JLabel("You are all caught up. Check back later for new jobs.");
        emptyHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        emptyHint.setForeground(new Color(160, 160, 160));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        emptySchedulePanel.add(emptyMessage, gbc);
        gbc.gridy = 1;
        emptySchedulePanel.add(emptyHint, gbc);
        
        scheduleContentPanel = new JPanel(new CardLayout());
        scheduleContentPanel.setBackground(Color.WHITE);
        scheduleContentPanel.add(emptySchedulePanel, "EMPTY");
        
        JScrollPane tableScrollPane = new JScrollPane(todayTable);
        tableScrollPane.setBorder(null);
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        scheduleContentPanel.add(tableScrollPane, "TABLE");
    }
    
    private JLabel createKpiLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 28));
        label.setForeground(NAVY_BLUE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
    
    private JLabel createMetricLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(new Color(50, 50, 50));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(PANEL_BG);
        
        mainPanel.add(createKpiRow());
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createMetricsPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createSchedulePanel());
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(PANEL_BG);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL_BG);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("My Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        
        JLabel dateLabel = new JLabel(DateUtils.getCurrentDate());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(100, 100, 100));
        
        JLabel liveLabel = new JLabel("LIVE");
        liveLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        liveLabel.setForeground(new Color(34, 197, 94));
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(PANEL_BG);
        rightPanel.add(liveLabel);
        rightPanel.add(dateLabel);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 15, 0));
        row.setBackground(PANEL_BG);
        row.setOpaque(false);
        
        row.add(createKpiCard("Completed Jobs", completedJobsLabel, "Total completed appointments"));
        row.add(createKpiCard("Ongoing Tasks", ongoingJobsLabel, "Currently in progress"));
        row.add(createKpiCard("Revenue", revenueLabel, "From completed jobs"));
        row.add(createKpiCard("Today's Jobs", todayJobsLabel, "Scheduled for today"));
        
        return row;
    }
    
    private JPanel createKpiCard(String title, JLabel valueLabel, String description) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(new Color(80, 80, 80));
        
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        valuePanel.setBackground(Color.WHITE);
        valuePanel.add(valueLabel);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descLabel.setForeground(new Color(120, 120, 120));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setBackground(PANEL_BG);
        panel.setOpaque(false);
        
        JPanel ratingCard = new JPanel(new BorderLayout());
        ratingCard.setBackground(Color.WHITE);
        ratingCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel ratingTitle = new JLabel("Customer Rating");
        ratingTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ratingTitle.setForeground(new Color(80, 80, 80));
        
        JPanel ratingCenter = new JPanel();
        ratingCenter.setBackground(Color.WHITE);
        ratingCenter.add(avgRatingLabel);
        
        ratingCard.add(ratingTitle, BorderLayout.NORTH);
        ratingCard.add(ratingCenter, BorderLayout.CENTER);
        
        JPanel completionCard = new JPanel(new BorderLayout());
        completionCard.setBackground(Color.WHITE);
        completionCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel completionTitle = new JLabel("Completion Rate");
        completionTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        completionTitle.setForeground(new Color(80, 80, 80));
        
        JPanel completionCenter = new JPanel();
        completionCenter.setBackground(Color.WHITE);
        completionCenter.add(completionRateLabel);
        
        completionCard.add(completionTitle, BorderLayout.NORTH);
        completionCard.add(completionCenter, BorderLayout.CENTER);
        
        JPanel onTimeCard = new JPanel(new BorderLayout());
        onTimeCard.setBackground(Color.WHITE);
        onTimeCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel onTimeTitle = new JLabel("On-Time Rate");
        onTimeTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        onTimeTitle.setForeground(new Color(80, 80, 80));
        
        JPanel onTimeCenter = new JPanel();
        onTimeCenter.setBackground(Color.WHITE);
        onTimeCenter.add(onTimeRateLabel);
        
        onTimeCard.add(onTimeTitle, BorderLayout.NORTH);
        onTimeCard.add(onTimeCenter, BorderLayout.CENTER);
        
        panel.add(ratingCard);
        panel.add(completionCard);
        panel.add(onTimeCard);
        
        return panel;
    }
    
    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        
        JLabel titleLabel = new JLabel("Recent Jobs");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(NAVY_BLUE);
        
        JButton viewAllBtn = new JButton("View All Jobs");
        viewAllBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        viewAllBtn.setBackground(NAVY_BLUE);
        viewAllBtn.setForeground(Color.WHITE);
        viewAllBtn.setFocusPainted(false);
        viewAllBtn.setBorderPainted(false);
        viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllBtn.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof ui.dashboard.DashboardFrame) {
                ((ui.dashboard.DashboardFrame) frame).switchToPanel("JOBS");
            }
        });
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(viewAllBtn, BorderLayout.EAST);
        
        panel.add(header, BorderLayout.NORTH);
        panel.add(scheduleContentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void refreshDashboard() {
        List<Appointment> allAppointments = appointmentDAO.findByTechnicianId(currentTechnician.getId());
        String today = DateUtils.getCurrentDate();
        
        int totalToday = 0;
        int ongoingCount = 0;
        int completedCount = 0;
        double revenue = 0;
        List<Appointment> todayAppointments = new ArrayList<>();
        
        for (Appointment a : allAppointments) {
        	if (a.getDate() != null) {
        	    if (DateUtils.isWithinLast7Days(a.getDate())) {
        	        todayAppointments.add(a);
        	    }

        	    if (a.getDate().equals(DateUtils.getCurrentDate())) {
        	        totalToday++;
        	    }
        	}
        	
            if (a.getStatus() == AppointmentStatus.ASSIGNED) {
                ongoingCount++;
            }
            if (a.getStatus() == AppointmentStatus.COMPLETED) {
                completedCount++;
                revenue += a.getAmount();
            }
            if (a.getStatus() == AppointmentStatus.PAID) {
                revenue += a.getAmount();
            }
        }
        
        completedJobsLabel.setText(String.valueOf(completedCount));
        ongoingJobsLabel.setText(String.valueOf(ongoingCount));
        revenueLabel.setText(String.format("RM %.0f", revenue));
        todayJobsLabel.setText(String.valueOf(totalToday));
        
        calculatePerformanceMetrics(allAppointments);
        updateScheduleTable(todayAppointments);
    }
    
    private void calculatePerformanceMetrics(List<Appointment> appointments) {
        List<Comment> allComments = commentDAO.readAll();
        int totalRating = 0;
        int ratingCount = 0;
        
        for (Comment c : allComments) {
            if (c.getTechnicianId() != null && c.getTechnicianId().equals(currentTechnician.getId())) {
                try {
                    totalRating += Integer.parseInt(c.getContent());
                    ratingCount++;
                } catch (Exception e) {
                }
            }
        }
        
        double avgRating = ratingCount > 0 ? (double) totalRating / ratingCount : 0;
        avgRatingLabel.setText(String.format("%.1f / 5.0", avgRating));
        
        int totalAssigned = 0;
        int totalCompleted = 0;
        for (Appointment a : appointments) {
            if (a.getStatus() == AppointmentStatus.ASSIGNED || 
                a.getStatus() == AppointmentStatus.COMPLETED ||
                a.getStatus() == AppointmentStatus.PAID) {
                totalAssigned++;
            }
            if (a.getStatus() == AppointmentStatus.COMPLETED || a.getStatus() == AppointmentStatus.PAID) {
                totalCompleted++;
            }
        }
        
        int completionRate = totalAssigned > 0 ? (totalCompleted * 100 / totalAssigned) : 0;
        completionRateLabel.setText(completionRate + "%");
        
        int onTimeRate = completionRate;
        onTimeRateLabel.setText(onTimeRate + "%");
    }
    
    private void updateScheduleTable(List<Appointment> todayAppointments) {
        tableModel.setRowCount(0);
        
        if (todayAppointments.isEmpty()) {
            CardLayout cl = (CardLayout) scheduleContentPanel.getLayout();
            cl.show(scheduleContentPanel, "EMPTY");
        } else {
            CardLayout cl = (CardLayout) scheduleContentPanel.getLayout();
            cl.show(scheduleContentPanel, "TABLE");
            
            todayAppointments.sort((a, b) -> {
                int dateCompare = b.getDate().compareTo(a.getDate());
                if (dateCompare != 0) return dateCompare;

                return b.getStartTime().compareTo(a.getStartTime());
            });
            
            for (Appointment a : todayAppointments) {
                User customer = userDAO.findById(a.getCustomerId());
                String customerName = customer != null ? customer.getFullName() : "Unknown";
                
                Object[] row = {
                	    a.getId(),
                	    a.getDate() + " " + a.getStartTime(),
                	    customerName,
                	    a.getServiceType().toString(),
                	    a.getStatus().toString(),
                	    "VIEW"
                	};
                
                tableModel.addRow(row);
            }
        }
    }
    
    private void showAppointmentDetails(Appointment a) {
        if (a == null) return;

        User customer = userDAO.findById(a.getCustomerId());
        String customerName = customer != null ? customer.getFullName() : "Unknown";

        List<Comment> allComments = commentDAO.readAll();
        StringBuilder customerComments = new StringBuilder();

        for (Comment c : allComments) {
            if (a.getId().equals(c.getAppointmentId())) {
                if (c.getCounterStaffId() == null) {
                    customerComments.append("Comment: ")
                                    .append(c.getContent())
                                    .append("\n");
                }
            }
        }

        if (customerComments.length() == 0) {
            customerComments.append("No comments.");
        }


        // ===== FEEDBACK (from feedback.txt) =====
        List<Feedback> allFeedbacks = feedbackDAO.readAll();
        StringBuilder customerFeedbacks = new StringBuilder();

        for (Feedback f : allFeedbacks) {
            if (a.getId().equals(f.getAppointmentId())) {
                customerFeedbacks.append("Rating: ")
                                 .append(f.getRating())
                                 .append("/5\n");
                customerFeedbacks.append("Feedback: ")
                                 .append(f.getContent())
                                 .append("\n");
            }
        }

        if (customerFeedbacks.length() == 0) {
            customerFeedbacks.append("No feedback.");
        }
        
        // Technician Notes
        List<TechNote> allNotes = techNoteDAO.readAll();
        StringBuilder techNotes = new StringBuilder();
        for (TechNote n : allNotes) {
            if (a.getId().equals(n.getAppointmentId()) &&
                currentTechnician.getId().equals(n.getTechnicianId())) {
                techNotes.append(n.getContent()).append("\n");
            }
        }
        if (techNotes.length() == 0) techNotes.append("No technician notes yet.");

        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setContentType("text/html");

        textPane.setText(
            "<html>" +
            "=======================================<br>" +
            "<b>APPOINTMENT DETAILS</b><br>" +
            "=======================================<br><br>" +

            "ID: " + a.getId() + "<br>" +
            "Date: " + a.getDate() + "<br>" +
            "Time: " + a.getStartTime() + "<br>" +
            "Service: " + a.getServiceType() + "<br>" +
            "Duration: " + a.getDuration() + " hour(s)<br>" +
            "Status: " + a.getStatus() + "<br>" +
            "Amount: RM " + String.format("%.2f", a.getAmount()) + "<br>" +
            "Customer: " + customerName + "<br><br>" +

            "---------------------------------------<br>" +
            "<b>CUSTOMER COMMENTS:</b><br>" +
            customerComments.toString().replace("\n","<br>") + "<br><br>" +

            "---------------------------------------<br>" +
            "<b>CUSTOMER FEEDBACK:</b><br>" +
            customerFeedbacks.toString().replace("\n","<br>") + "<br><br>" +

            "---------------------------------------<br>" +
            "<b>TECHNICIAN NOTES:</b><br>" +
            techNotes.toString().replace("\n","<br>") +

            "</html>"
        );

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(550, 500));

        JOptionPane.showMessageDialog(this, scrollPane,
                "Appointment Details", JOptionPane.INFORMATION_MESSAGE);
    }

    
    private boolean isWithin24Hours(String dateStr, String timeStr) {
        try {
            java.time.LocalDateTime appointmentTime =
                    java.time.LocalDateTime.parse(dateStr + "T" + timeStr);

            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            return !appointmentTime.isBefore(now.minusHours(24))
                    && !appointmentTime.isAfter(now);

        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    protected void addEventHandlers() {
    }
}