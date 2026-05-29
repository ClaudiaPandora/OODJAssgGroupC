package ui.customer;

import dao.AppointmentDAO;
import dao.CommentDAO;
import dao.UserDAO;
import enums.AppointmentStatus;
import enums.ServiceType;
import models.Appointment;
import models.Comment;
import models.User;
import ui.common.BasePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class CustomerFeedbackPanel extends BasePanel {

    private final User currentUser;
    private final AppointmentDAO appointmentDAO;
    private final CommentDAO commentDAO;
    private final UserDAO userDAO;

    private JComboBox<AppointmentItem> appointmentCombo;
    private JLabel technicianInfoLabel;
    private JLabel counterStaffInfoLabel;

    private JSlider technicianRatingSlider;
    private JSlider counterStaffRatingSlider;
    private JTextArea technicianCommentArea;
    private JTextArea counterStaffCommentArea;
    private JButton technicianSubmitButton;
    private JButton counterStaffSubmitButton;
    private JPanel technicianCard;
    private JPanel counterStaffCard;
    
    private JButton refreshButton;
    
    private final Color CARD_BORDER = new Color(221, 225, 231);
    private final Color GREEN = new Color(34, 197, 94);
    private final Color BLUE = new Color(37, 99, 235);
    private final Color ORANGE = new Color(234, 179, 8);
    private final Color LIGHT_BG = new Color(248, 250, 252);

    public CustomerFeedbackPanel(User currentUser) {
        this.currentUser = currentUser;
        this.appointmentDAO = new AppointmentDAO();
        this.commentDAO = new CommentDAO();
        this.userDAO = new UserDAO();

        setBackground(PANEL_BG);
        initializeComponents();
        setupLayout();
        addEventHandlers();
        loadAppointments();
    }

    @Override
    protected void initializeComponents() {
        appointmentCombo = new JComboBox<>();
        appointmentCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        appointmentCombo.setBackground(Color.WHITE);
        appointmentCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(8, 12, 8, 12)
        ));
        appointmentCombo.setPreferredSize(new Dimension(250, 34));

        technicianInfoLabel = new JLabel("Technician: -");
        technicianInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        technicianInfoLabel.setForeground(new Color(100, 116, 139));
        
        counterStaffInfoLabel = new JLabel("Counter Staff: -");
        counterStaffInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        counterStaffInfoLabel.setForeground(new Color(100, 116, 139));

        technicianRatingSlider = createRatingSlider();
        counterStaffRatingSlider = createRatingSlider();

        technicianCommentArea = createCommentArea();
        counterStaffCommentArea = createCommentArea();

        technicianSubmitButton = createSubmitButton("Submit Rating & Comment", GREEN);
        counterStaffSubmitButton = createSubmitButton("Submit Rating & Comment", BLUE);
        refreshButton = createRefreshButton();
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

        JPanel appointmentPanel = createAppointmentPanel();
        appointmentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        appointmentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        JPanel feedbackPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        feedbackPanel.setBackground(PANEL_BG);
        feedbackPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        technicianCard = new JPanel();
        technicianCard.setBackground(Color.WHITE);
        technicianCard.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        
        counterStaffCard = new JPanel();
        counterStaffCard.setBackground(Color.WHITE);
        counterStaffCard.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        
        feedbackPanel.add(technicianCard);
        feedbackPanel.add(counterStaffCard);

        content.add(appointmentPanel);
        content.add(Box.createVerticalStrut(15));
        content.add(feedbackPanel);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(PANEL_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pagePanel.add(scrollPane, BorderLayout.CENTER);
        add(pagePanel, BorderLayout.CENTER);
    }

    private void updateRatingCards(Appointment appointment) {
        List<Comment> existingComments = commentDAO.findByAppointmentId(appointment.getId());
        
        boolean hasTechnicianComment = false;
        boolean hasCounterStaffComment = false;
        
        for (Comment comment : existingComments) {
            if (currentUser.getId().equals(comment.getCustomerId())) {
                if (comment.getTechnicianId() != null && comment.getTechnicianId().equals(appointment.getTechnicianId())) {
                    hasTechnicianComment = true;
                }
                if (comment.getCounterStaffId() != null && comment.getCounterStaffId().equals(appointment.getCounterStaffId())) {
                    hasCounterStaffComment = true;
                }
            }
        }
        
        String techStatus = "";
        boolean techEnabled = true;
        if (appointment.getTechnicianId() == null || appointment.getTechnicianId().isEmpty()) {
            techStatus = "No Technician Assigned";
            techEnabled = false;
        } else if (hasTechnicianComment) {
            techStatus = "Already Rated";
            techEnabled = false;
        } else {
            techStatus = "Ready to Rate";
            techEnabled = true;
        }
        
        JPanel newTechCard = createRatingCard(
            "Rate Technician",
            techStatus,
            techEnabled,
            new JLabel("Technician: " + resolveUserName(appointment.getTechnicianId())),
            technicianRatingSlider,
            technicianCommentArea,
            technicianSubmitButton
        );
        
        String staffStatus = "";
        boolean staffEnabled = true;
        if (appointment.getCounterStaffId() == null || appointment.getCounterStaffId().isEmpty()) {
            staffStatus = "No Counter Staff Assigned";
            staffEnabled = false;
        } else if (hasCounterStaffComment) {
            staffStatus = "Already Rated";
            staffEnabled = false;
        } else {
            staffStatus = "Ready to Rate";
            staffEnabled = true;
        }
        
        JPanel newStaffCard = createRatingCard(
            "Rate Counter Staff",
            staffStatus,
            staffEnabled,
            new JLabel("Counter Staff: " + resolveUserName(appointment.getCounterStaffId())),
            counterStaffRatingSlider,
            counterStaffCommentArea,
            counterStaffSubmitButton
        );
        
        Container parent = technicianCard.getParent();
        if (parent != null) {
            int techIndex = getComponentIndex(parent, technicianCard);
            int staffIndex = getComponentIndex(parent, counterStaffCard);
            parent.remove(technicianCard);
            parent.remove(counterStaffCard);
            parent.add(newTechCard, techIndex);
            parent.add(newStaffCard, staffIndex);
            technicianCard = newTechCard;
            counterStaffCard = newStaffCard;
            parent.revalidate();
            parent.repaint();
        }
    }

    private int getComponentIndex(Container container, Component component) {
        for (int i = 0; i < container.getComponentCount(); i++) {
            if (container.getComponent(i) == component) {
                return i;
            }
        }
        return -1;
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL_BG);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(PANEL_BG);
        
        JLabel titleLabel = new JLabel("My Feedback");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(titleLabel);
        
        textPanel.add(Box.createVerticalStrut(5));
        
        JLabel subtitleLabel = new JLabel("Rate your experience and leave comments for technicians and counter staff.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(subtitleLabel);
        
        header.add(textPanel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(PANEL_BG);
        rightPanel.add(refreshButton);
        header.add(rightPanel, BorderLayout.EAST);
        
        return header;
    }

    private JPanel createAppointmentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                new EmptyBorder(10, 16, 10, 16)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 5, 0, 5);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.gridy = 0;
        JLabel appointmentLabel = new JLabel("Select Pending Appointments:");
        appointmentLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        appointmentLabel.setForeground(new Color(31, 41, 55));
        panel.add(appointmentLabel, g);

        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        panel.add(appointmentCombo, g);

        return panel;
    }

    private JPanel createRatingCard(String title, String statusText, boolean isEnabled, JLabel personLabel, JSlider slider, JTextArea commentArea, JButton submitButton) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                new EmptyBorder(0, 0, 0, 0)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LIGHT_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titlePanel.setBackground(LIGHT_BG);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(NAVY_BLUE);
        titlePanel.add(titleLabel);
        
        if (statusText != null && !statusText.isEmpty()) {
            JLabel statusLabel = new JLabel(" (" + statusText + ")");
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            if (statusText.equals("Ready to Rate")) {
                statusLabel.setForeground(BLUE);
            } else if (statusText.equals("Already Rated")) {
                statusLabel.setForeground(GREEN);
            } else if (statusText.equals("No Technician Assigned") || statusText.equals("No Counter Staff Assigned")) {
                statusLabel.setForeground(ORANGE);
            }
            titlePanel.add(statusLabel);
        }
        
        header.add(titlePanel, BorderLayout.CENTER);
        card.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(14, 16, 16, 16));

        JPanel ratingPanel = new JPanel();
        ratingPanel.setLayout(new BoxLayout(ratingPanel, BoxLayout.Y_AXIS));
        ratingPanel.setBackground(Color.WHITE);

        String personName = personLabel.getText();
        if (personName.startsWith("Technician: ")) {
            personName = personName.substring("Technician: ".length());
        } else if (personName.startsWith("Counter Staff: ")) {
            personName = personName.substring("Counter Staff: ".length());
        }
        
        JLabel nameLabel = new JLabel(personName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(new Color(30, 58, 138));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel ratingLabel = new JLabel("Rating (1-5)");
        ratingLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ratingLabel.setForeground(new Color(31, 41, 55));
        ratingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        slider.setAlignmentX(Component.CENTER_ALIGNMENT);
        slider.setEnabled(isEnabled);

        ratingPanel.add(nameLabel);
        ratingPanel.add(Box.createVerticalStrut(8));
        ratingPanel.add(ratingLabel);
        ratingPanel.add(Box.createVerticalStrut(6));
        ratingPanel.add(slider);

        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                "Comment",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11)
        ));
        commentScroll.setPreferredSize(new Dimension(0, 150));
        commentArea.setEnabled(isEnabled);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        buttonPanel.setBackground(Color.WHITE);
        submitButton.setEnabled(isEnabled);
        buttonPanel.add(submitButton);

        body.add(ratingPanel, BorderLayout.NORTH);
        body.add(commentScroll, BorderLayout.CENTER);
        body.add(buttonPanel, BorderLayout.SOUTH);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JTextArea createCommentArea() {
        JTextArea area = new JTextArea(3, 20);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setForeground(Color.BLACK);
        area.setBackground(Color.WHITE);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return area;
    }

    private JButton createSubmitButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setPreferredSize(new Dimension(160, 36));
        
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

    private JButton createRefreshButton() {
        JButton button = new JButton("Refresh");
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(GREEN);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setPreferredSize(new Dimension(100, 36));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(GREEN.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(GREEN);
            }
        });
        
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
        slider.setPreferredSize(new Dimension(260, 50));
        
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = 1; i <= 5; i++) {
            JLabel label = new JLabel(String.valueOf(i));
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(NAVY_BLUE);
            labelTable.put(i, label);
        }
        slider.setLabelTable(labelTable);
        
        return slider;
    }

    private List<Appointment> getEligibleAppointments() {
        List<Appointment> result = new ArrayList<>();
        List<Comment> allComments = commentDAO.readAll();
        
        for (Appointment appointment : appointmentDAO.findByCustomerId(currentUser.getId())) {
            if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
                // Check if both technician and counter staff have been rated by this customer
                boolean technicianRated = false;
                boolean counterStaffRated = false;
                
                for (Comment comment : allComments) {
                    if (currentUser.getId().equals(comment.getCustomerId()) && 
                        appointment.getId().equals(comment.getAppointmentId())) {
                        
                        if (comment.getTechnicianId() != null && 
                            comment.getTechnicianId().equals(appointment.getTechnicianId())) {
                            technicianRated = true;
                        }
                        
                        if (comment.getCounterStaffId() != null && 
                            comment.getCounterStaffId().equals(appointment.getCounterStaffId())) {
                            counterStaffRated = true;
                        }
                    }
                }
                
                // Only include appointment if NOT both have been rated
                if (!(technicianRated && counterStaffRated)) {
                    result.add(appointment);
                }
            }
        }
        return result;
    }

    private void loadAppointments() {
        appointmentCombo.removeAllItems();
        List<Appointment> eligibleAppointments = getEligibleAppointments();
        
        if (eligibleAppointments.isEmpty()) {
            appointmentCombo.addItem(new AppointmentItem(null));
            technicianCard.setVisible(false);
            counterStaffCard.setVisible(false);
            
            // Show message in the combo box
            JPanel parent = (JPanel) appointmentCombo.getParent();
            JOptionPane.showMessageDialog(this, 
                "No appointments available for feedback.\n\n" +
                "All completed appointments have already been rated for both technician and counter staff.",
                "No Appointments", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Appointment appointment : eligibleAppointments) {
                appointmentCombo.addItem(new AppointmentItem(appointment));
            }
            if (appointmentCombo.getItemCount() > 0) {
                appointmentCombo.setSelectedIndex(0);
            }
            technicianCard.setVisible(true);
            counterStaffCard.setVisible(true);
        }
        updateSelectionInfo();
    }

    private void updateSelectionInfo() {
        AppointmentItem selected = (AppointmentItem) appointmentCombo.getSelectedItem();
        if (selected == null || selected.getAppointment() == null) {
            technicianInfoLabel.setText("Technician: -");
            counterStaffInfoLabel.setText("Counter Staff: -");
            technicianCard.setVisible(false);
            counterStaffCard.setVisible(false);
            return;
        }

        technicianCard.setVisible(true);
        counterStaffCard.setVisible(true);
        
        Appointment appointment = selected.getAppointment();
        
        String technicianName = resolveUserName(appointment.getTechnicianId());
        technicianInfoLabel.setText("Technician: " + (technicianName != null ? technicianName : "-"));
        
        String counterStaffName = resolveUserName(appointment.getCounterStaffId());
        counterStaffInfoLabel.setText("Counter Staff: " + (counterStaffName != null ? counterStaffName : "-"));
        
        updateRatingCards(appointment);
    }

    private void submitComment(boolean forCounterStaff) {
        AppointmentItem selected = (AppointmentItem) appointmentCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment first.",
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

        List<Comment> existingComments = commentDAO.findByAppointmentId(appointment.getId());
        for (Comment existing : existingComments) {
            if (currentUser.getId().equals(existing.getCustomerId())) {
                if (forCounterStaff && existing.getCounterStaffId() != null && 
                    existing.getCounterStaffId().equals(appointment.getCounterStaffId())) {
                    JOptionPane.showMessageDialog(this,
                            "You have already rated the counter staff for this appointment.",
                            "Already Rated",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!forCounterStaff && existing.getTechnicianId() != null && 
                    existing.getTechnicianId().equals(appointment.getTechnicianId())) {
                    JOptionPane.showMessageDialog(this,
                            "You have already rated the technician for this appointment.",
                            "Already Rated",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
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
                    "Rating and comment submitted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            activeArea.setText("");
            activeSlider.setValue(5);
            updateSelectionInfo();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to save your comment.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshAll() {
        loadAppointments();
    }

    private String resolveUserName(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }
        User user = userDAO.findById(userId);
        return user != null ? user.getFullName() : userId;
    }

    @Override
    protected void addEventHandlers() {
        appointmentCombo.addActionListener(e -> updateSelectionInfo());
        technicianSubmitButton.addActionListener(e -> submitComment(false));
        counterStaffSubmitButton.addActionListener(e -> submitComment(true));
        refreshButton.addActionListener(e -> {
            refreshAll();
            JOptionPane.showMessageDialog(this, "Data has been refreshed from files.", 
                "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
        });
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
            if (appointment == null) {
                return "No appointments available";
            }
            String serviceType = appointment.getServiceType() == ServiceType.NORMAL ? "Normal" : "Major";
            return appointment.getId() + " - " + appointment.getDate() + " - " + serviceType;
        }
    }
}