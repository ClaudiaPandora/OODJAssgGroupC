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
import java.util.Hashtable;
import java.util.List;

public class CustomerFeedbackPanel extends BasePanel {

    private final User currentUser;
    private final AppointmentDAO appointmentDAO;
    private final FeedbackDAO feedbackDAO;
    private final CommentDAO commentDAO;
    private final UserDAO userDAO;

    private JComboBox<AppointmentItem> appointmentCombo;
    private JLabel appointmentInfoLabel;
    private JLabel technicianInfoLabel;
    private JLabel counterStaffInfoLabel;

    private JSlider technicianRatingSlider;
    private JSlider counterStaffRatingSlider;
    private JTextArea technicianCommentArea;
    private JTextArea counterStaffCommentArea;
    private JButton technicianSubmitButton;
    private JButton counterStaffSubmitButton;

    private JTable technicianFeedbackTable;
    private JTable counterStaffFeedbackTable;
    private JTable commentsTable;
    private DefaultTableModel technicianFeedbackTableModel;
    private DefaultTableModel counterStaffFeedbackTableModel;
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

        appointmentInfoLabel = new JLabel("Select an appointment.");
        appointmentInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        appointmentInfoLabel.setForeground(new Color(100, 100, 100));

        technicianInfoLabel = new JLabel("Technician: -");
        technicianInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        technicianInfoLabel.setForeground(new Color(100, 100, 100));

        counterStaffInfoLabel = new JLabel("Counter Staff: -");
        counterStaffInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        counterStaffInfoLabel.setForeground(new Color(100, 100, 100));

        technicianRatingSlider = createRatingSlider();
        counterStaffRatingSlider = createRatingSlider();

        technicianCommentArea = createCommentArea();
        counterStaffCommentArea = createCommentArea();

        technicianSubmitButton = createActionButton("Submit Technician Feedback");
        counterStaffSubmitButton = createActionButton("Submit Counter Staff Feedback");

        technicianFeedbackTableModel = new DefaultTableModel(
                new String[]{"Appointment ID", "Technician", "Rating", "Feedback"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        counterStaffFeedbackTableModel = new DefaultTableModel(
                new String[]{"Appointment ID", "Counter Staff", "Rating", "Comment"}, 0
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

        technicianFeedbackTable = createStyledTable(technicianFeedbackTableModel);
        counterStaffFeedbackTable = createStyledTable(counterStaffFeedbackTableModel);
        commentsTable = createStyledTable(commentsTableModel);
    }

    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());

        JPanel pagePanel = new JPanel(new BorderLayout());
        pagePanel.setBackground(PANEL_BG);
        pagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_BG);

        JPanel headerTextPanel = new JPanel();
        headerTextPanel.setLayout(new BoxLayout(headerTextPanel, BoxLayout.Y_AXIS));
        headerTextPanel.setBackground(PANEL_BG);

        JLabel titleLabel = new JLabel("Feedback and Comments");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("View technician feedback and submit your comments.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerTextPanel.add(titleLabel);
        headerTextPanel.add(Box.createVerticalStrut(5));
        headerTextPanel.add(subtitleLabel);

        headerPanel.add(headerTextPanel, BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(PANEL_BG);
        content.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JPanel appointmentPanel = createAppointmentPanel();
        appointmentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        appointmentPanel.setMaximumSize(new Dimension(900, 90));
        appointmentPanel.setPreferredSize(new Dimension(900, 90));

        JPanel technicianCard = createRatingCard(
                "Rate Technician",
                technicianInfoLabel,
                technicianRatingSlider,
                technicianCommentArea,
                technicianSubmitButton
        );
        technicianCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel counterStaffCard = createRatingCard(
                "Rate Counter Staff",
                counterStaffInfoLabel,
                counterStaffRatingSlider,
                counterStaffCommentArea,
                counterStaffSubmitButton
        );
        counterStaffCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.addTab("Technician Feedback", createTablePanel(technicianFeedbackTable));
        tabbedPane.addTab("Counter Staff Feedback", createTablePanel(counterStaffFeedbackTable));
        tabbedPane.addTab("My Comments", createTablePanel(commentsTable));
        tabbedPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        tabbedPane.setPreferredSize(new Dimension(900, 280));
        tabbedPane.setMaximumSize(new Dimension(900, 300));

        content.add(appointmentPanel);
        content.add(Box.createVerticalStrut(20));
        content.add(technicianCard);
        content.add(Box.createVerticalStrut(20));
        content.add(counterStaffCard);
        content.add(Box.createVerticalStrut(20));
        content.add(tabbedPane);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(PANEL_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pagePanel.add(headerPanel, BorderLayout.NORTH);
        pagePanel.add(scrollPane, BorderLayout.CENTER);

        add(pagePanel, BorderLayout.CENTER);
    }

    private JPanel createAppointmentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 2),
                BorderFactory.createLineBorder(BORDER_DARK, 2)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 12, 10, 12);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0;
        g.gridy = 0;
        panel.add(createLabel("Appointment"), g);

        g.gridx = 1;
        g.weightx = 1;
        panel.add(appointmentCombo, g);

        g.gridx = 0;
        g.gridy = 1;
        g.gridwidth = 2;
        panel.add(appointmentInfoLabel, g);

        return panel;
    }

    private JPanel createRatingCard(String title, JLabel personLabel, JSlider slider, JTextArea commentArea, JButton submitButton) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 2),
                BorderFactory.createLineBorder(BORDER_DARK, 2)
        ));
        card.setMaximumSize(new Dimension(900, 320));
        card.setPreferredSize(new Dimension(900, 320));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(NAVY_BLUE);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(personLabel);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        JPanel ratingPanel = new JPanel();
        ratingPanel.setLayout(new BoxLayout(ratingPanel, BoxLayout.Y_AXIS));
        ratingPanel.setBackground(Color.WHITE);

        JLabel ratingLabel = createLabel("Rating");
        ratingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        slider.setAlignmentX(Component.LEFT_ALIGNMENT);

        ratingPanel.add(ratingLabel);
        ratingPanel.add(Box.createVerticalStrut(8));
        ratingPanel.add(slider);

        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setBorder(BorderFactory.createTitledBorder("Comment"));
        commentScroll.setPreferredSize(new Dimension(0, 140));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(submitButton);

        body.add(ratingPanel, BorderLayout.NORTH);
        body.add(commentScroll, BorderLayout.CENTER);
        body.add(buttonPanel, BorderLayout.SOUTH);

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
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

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Color.BLACK);
        return label;
    }

    private JTextArea createCommentArea() {
        JTextArea area = new JTextArea(5, 20);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setForeground(Color.BLACK);
        area.setBackground(Color.WHITE);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return area;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(NAVY_BLUE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(220, 32));
        return button;
    }

    private JSlider createRatingSlider() {
        JSlider slider = new JSlider(1, 5, 5);
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setSnapToTicks(true);
        slider.setBackground(Color.WHITE);
        slider.setForeground(NAVY_BLUE);
        slider.setPreferredSize(new Dimension(260, 55));
        slider.setMaximumSize(new Dimension(260, 55));

        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        for (int i = 1; i <= 5; i++) {
            JLabel label = new JLabel(String.valueOf(i));
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(NAVY_BLUE);
            labels.put(i, label);
        }
        slider.setLabelTable(labels);

        return slider;
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
            technicianInfoLabel.setText("Technician: -");
            counterStaffInfoLabel.setText("Counter Staff: -");
            return;
        }

        Appointment appointment = selected.getAppointment();
        appointmentInfoLabel.setText(
                "Selected: " + appointment.getId() + " | " + appointment.getDate() + " | " + appointment.getServiceType()
        );
        technicianInfoLabel.setText("Technician: " + resolveUserName(appointment.getTechnicianId()));
        counterStaffInfoLabel.setText("Counter Staff: " + resolveUserName(appointment.getCounterStaffId()));
    }

    private void refreshTables() {
        technicianFeedbackTableModel.setRowCount(0);
        counterStaffFeedbackTableModel.setRowCount(0);
        commentsTableModel.setRowCount(0);

        List<Appointment> appointments = appointmentDAO.findByCustomerId(currentUser.getId());
        List<String> myAppointmentIds = new ArrayList<>();

        for (Appointment appointment : appointments) {
            myAppointmentIds.add(appointment.getId());
        }

        for (Feedback feedback : feedbackDAO.readAll()) {
            if (myAppointmentIds.contains(feedback.getAppointmentId())) {
                technicianFeedbackTableModel.addRow(new Object[]{
                        feedback.getAppointmentId(),
                        resolveUserName(feedback.getTechnicianId()),
                        feedback.getRating() + "/5",
                        feedback.getContent()
                });
            }
        }

        for (Comment comment : commentDAO.findByCustomerId(currentUser.getId())) {
            if (comment.getCounterStaffId() != null && !comment.getCounterStaffId().trim().isEmpty()) {
                counterStaffFeedbackTableModel.addRow(new Object[]{
                        comment.getAppointmentId(),
                        resolveUserName(comment.getCounterStaffId()),
                        comment.getRating() + "/5",
                        comment.getContent()
                });
            }

            commentsTableModel.addRow(new Object[]{
                    comment.getId(),
                    comment.getAppointmentId(),
                    resolveCommentTarget(comment),
                    comment.getRating() + "/5",
                    comment.getContent()
            });
        }
    }

    private void submitComment(boolean forCounterStaff) {
        AppointmentItem selected = (AppointmentItem) appointmentCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a completed appointment first.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Appointment appointment = selected.getAppointment();
        JTextArea activeArea = forCounterStaff ? counterStaffCommentArea : technicianCommentArea;
        JSlider activeSlider = forCounterStaff ? counterStaffRatingSlider : technicianRatingSlider;

        String content = activeArea.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a comment before submitting.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (forCounterStaff && (appointment.getCounterStaffId() == null || appointment.getCounterStaffId().trim().isEmpty())) {
            JOptionPane.showMessageDialog(this,
                    "This appointment does not have counter staff assigned.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!forCounterStaff && (appointment.getTechnicianId() == null || appointment.getTechnicianId().trim().isEmpty())) {
            JOptionPane.showMessageDialog(this,
                    "This appointment does not have technician assigned.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Comment comment = new Comment();
        comment.setAppointmentId(appointment.getId());
        comment.setCustomerId(currentUser.getId());
        comment.setContent(content);
        comment.setRating(activeSlider.getValue());

        if (forCounterStaff) {
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
            activeArea.setText("");
            activeSlider.setValue(5);
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
        technicianSubmitButton.addActionListener(e -> submitComment(false));
        counterStaffSubmitButton.addActionListener(e -> submitComment(true));
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
