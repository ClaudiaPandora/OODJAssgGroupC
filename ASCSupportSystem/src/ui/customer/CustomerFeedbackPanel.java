package ui.customer;

import dao.AppointmentDAO;
import dao.CommentDAO;
import dao.FeedbackDAO;
import dao.UserDAO;
import enums.AppointmentStatus;
import models.Appointment;
import models.Comment;
import models.Feedback;
import models.User;
import ui.common.BasePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerFeedbackPanel extends BasePanel {

    private final User currentUser;
    private final AppointmentDAO appointmentDAO;
    private final FeedbackDAO feedbackDAO;
    private final CommentDAO commentDAO;
    private final UserDAO userDAO;

    private JComboBox<AppointmentItem> appointmentCombo;
    private JComboBox<String> targetCombo;
    private JComboBox<Integer> ratingCombo;
    private JTextArea commentArea;
    private JButton submitButton;
    private JLabel appointmentInfoLabel;
    private JLabel personInfoLabel;
    private JTable feedbackTable;
    private JTable commentsTable;
    private DefaultTableModel feedbackTableModel;
    private DefaultTableModel commentsTableModel;

    public CustomerFeedbackPanel(User currentUser) {
        this.currentUser = currentUser;
        this.appointmentDAO = new AppointmentDAO();
        this.feedbackDAO = new FeedbackDAO();
        this.commentDAO = new CommentDAO();
        this.userDAO = new UserDAO();

        setBackground(PANEL_BG);
        initializeComponents();
        setupLayout();
        addEventHandlers();
        loadAppointments();
        refreshTables();
    }

    @Override
    protected void initializeComponents() {
        appointmentCombo = new JComboBox<>();
        appointmentCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        appointmentCombo.setBackground(Color.WHITE);
        appointmentCombo.setForeground(Color.BLACK);

        targetCombo = new JComboBox<>(new String[]{"Technician", "Counter Staff"});
        targetCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        targetCombo.setBackground(Color.WHITE);
        targetCombo.setForeground(Color.BLACK);

        ratingCombo = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        ratingCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ratingCombo.setBackground(Color.WHITE);
        ratingCombo.setForeground(Color.BLACK);

        commentArea = new JTextArea(6, 20);
        commentArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setForeground(Color.BLACK);
        commentArea.setBackground(Color.WHITE);
        commentArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        submitButton = new JButton("Submit Comment");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        submitButton.setBackground(NAVY_BLUE);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBorderPainted(false);
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        appointmentInfoLabel = new JLabel("Select an appointment.");
        appointmentInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        appointmentInfoLabel.setForeground(new Color(100, 100, 100));

        personInfoLabel = new JLabel("Target person: -");
        personInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        personInfoLabel.setForeground(new Color(100, 100, 100));

        feedbackTableModel = new DefaultTableModel(
                new String[]{"Appointment ID", "Technician", "Rating", "Feedback"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        commentsTableModel = new DefaultTableModel(
                new String[]{"Comment ID", "Appointment ID", "Target", "Rating", "Comment"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        feedbackTable = createStyledTable(feedbackTableModel);
        commentsTable = createStyledTable(commentsTableModel);
    }

    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(PANEL_BG);

        JLabel titleLabel = new JLabel("Feedback and Comments");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("View technician feedback and submit your own comments.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(subtitleLabel);
        topPanel.add(Box.createVerticalStrut(15));
        topPanel.add(createCommentFormPanel());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Technician Feedback", createTablePanel(feedbackTable));
        tabbedPane.addTab("My Comments", createTablePanel(commentsTable));

        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createCommentFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(PANEL_BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 2),
                BorderFactory.createLineBorder(BORDER_DARK, 2)
        ));
        formPanel.setPreferredSize(new Dimension(760, 420));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 12, 10, 12);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0;
        g.gridy = 0;
        formPanel.add(createLabel("Appointment"), g);

        g.gridx = 1;
        g.weightx = 1;
        formPanel.add(appointmentCombo, g);

        g.gridx = 2;
        g.weightx = 0;
        formPanel.add(createLabel("Target"), g);

        g.gridx = 3;
        formPanel.add(targetCombo, g);

        g.gridx = 0;
        g.gridy = 1;
        formPanel.add(createLabel("Rating"), g);

        g.gridx = 1;
        formPanel.add(ratingCombo, g);

        g.gridx = 2;
        g.gridwidth = 2;
        formPanel.add(appointmentInfoLabel, g);

        g.gridx = 2;
        g.gridy = 2;
        formPanel.add(personInfoLabel, g);

        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        JScrollPane textScroll = new JScrollPane(commentArea);
        textScroll.setBorder(BorderFactory.createEmptyBorder());
        formPanel.add(textScroll, g);

        g.gridx = 3;
        g.gridy = 3;
        g.gridwidth = 1;
        g.weightx = 0;
        g.weighty = 0;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.EAST;
        formPanel.add(submitButton, g);

        wrapper.add(formPanel);
        return wrapper;
    }

    private JPanel createTablePanel(JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Color.BLACK);
        return label;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(0, 102, 204));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false);

        return table;
    }

    private void loadAppointments() {
        appointmentCombo.removeAllItems();
        for (Appointment appointment : getEligibleAppointments()) {
            appointmentCombo.addItem(new AppointmentItem(appointment));
        }
        updateSelectionInfo();
    }

    private List<Appointment> getEligibleAppointments() {
        List<Appointment> result = new ArrayList<>();
        for (Appointment appointment : appointmentDAO.findByCustomerId(currentUser.getId())) {
            if (appointment.getStatus() == AppointmentStatus.COMPLETED
                    || appointment.getStatus() == AppointmentStatus.PAID) {
                result.add(appointment);
            }
        }
        return result;
    }

    private void updateSelectionInfo() {
        AppointmentItem selected = (AppointmentItem) appointmentCombo.getSelectedItem();
        if (selected == null) {
            appointmentInfoLabel.setText("No completed or paid appointment available.");
            personInfoLabel.setText("Target person: -");
            return;
        }

        Appointment appointment = selected.getAppointment();
        appointmentInfoLabel.setText(
                "Selected: " + appointment.getId() + " | " + appointment.getDate() + " | " + appointment.getServiceType()
        );

        String target = (String) targetCombo.getSelectedItem();
        if ("Counter Staff".equals(target)) {
            personInfoLabel.setText("Target person: " + resolveUserName(appointment.getCounterStaffId()));
        } else {
            personInfoLabel.setText("Target person: " + resolveUserName(appointment.getTechnicianId()));
        }
    }

    private void refreshTables() {
        feedbackTableModel.setRowCount(0);
        commentsTableModel.setRowCount(0);

        List<Appointment> appointments = appointmentDAO.findByCustomerId(currentUser.getId());
        List<String> myAppointmentIds = new ArrayList<>();

        for (Appointment appointment : appointments) {
            myAppointmentIds.add(appointment.getId());
        }

        for (Feedback feedback : feedbackDAO.readAll()) {
            if (myAppointmentIds.contains(feedback.getAppointmentId())) {
                feedbackTableModel.addRow(new Object[]{
                        feedback.getAppointmentId(),
                        resolveUserName(feedback.getTechnicianId()),
                        feedback.getRating() + "/5",
                        feedback.getContent()
                });
            }
        }

        for (Comment comment : commentDAO.findByCustomerId(currentUser.getId())) {
            commentsTableModel.addRow(new Object[]{
                    comment.getId(),
                    comment.getAppointmentId(),
                    resolveCommentTarget(comment),
                    comment.getRating() + "/5",
                    comment.getContent()
            });
        }
    }

    private void submitComment() {
        AppointmentItem selected = (AppointmentItem) appointmentCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a completed appointment first.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String content = commentArea.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a comment before submitting.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Appointment appointment = selected.getAppointment();
        Comment comment = new Comment();
        comment.setAppointmentId(appointment.getId());
        comment.setCustomerId(currentUser.getId());
        comment.setContent(content);
        comment.setRating((Integer) ratingCombo.getSelectedItem());

        if ("Counter Staff".equals(targetCombo.getSelectedItem())) {
            comment.setCounterStaffId(appointment.getCounterStaffId());
            comment.setTechnicianId(null);
        } else {
            comment.setTechnicianId(appointment.getTechnicianId());
            comment.setCounterStaffId(null);
        }

        boolean success = commentDAO.save(comment);
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Comment submitted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            commentArea.setText("");
            ratingCombo.setSelectedIndex(0);
            refreshTables();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to save your comment.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String resolveUserName(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return "-";
        }
        User user = userDAO.findById(userId);
        return user != null ? user.getFullName() : userId;
    }

    private String resolveCommentTarget(Comment comment) {
        if (comment.getTechnicianId() != null && !comment.getTechnicianId().trim().isEmpty()) {
            return "Technician - " + resolveUserName(comment.getTechnicianId());
        }
        if (comment.getCounterStaffId() != null && !comment.getCounterStaffId().trim().isEmpty()) {
            return "Counter Staff - " + resolveUserName(comment.getCounterStaffId());
        }
        return "-";
    }

    @Override
    protected void addEventHandlers() {
        appointmentCombo.addActionListener(e -> updateSelectionInfo());
        targetCombo.addActionListener(e -> updateSelectionInfo());
        submitButton.addActionListener(e -> submitComment());
    }

    private static class AppointmentItem {
        private final Appointment appointment;

        private AppointmentItem(Appointment appointment) {
            this.appointment = appointment;
        }

        public Appointment getAppointment() {
            return appointment;
        }

        @Override
        public String toString() {
            return appointment.getId() + " - " + appointment.getDate() + " - " + appointment.getServiceType();
        }
    }
}
