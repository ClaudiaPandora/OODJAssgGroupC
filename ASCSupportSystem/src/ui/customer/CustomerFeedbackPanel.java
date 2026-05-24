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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
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
    
    private JTable commentsTable;
    private DefaultTableModel commentsTableModel;
    private JComboBox<String> commentFilterCombo;
    
    private JButton refreshButton;
    
    private final Color CARD_BORDER = new Color(221, 225, 231);
    private final Color TABLE_HEADER_BG = new Color(244, 246, 250);
    private final Color TABLE_SELECTION = new Color(232, 240, 254);
    private final Color ROW_SEPARATOR = new Color(231, 235, 240);
    private final Color GREEN = new Color(34, 197, 94);
    private final Color BLUE = new Color(37, 99, 235);
    private final Color ORANGE = new Color(234, 179, 8);
    private final Color GRAY = new Color(156, 163, 175);
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
        refreshCommentsTable();
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

        commentsTableModel = new DefaultTableModel(
                new String[]{"Appointment", "Target", "Rating", "Comment"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        commentsTable = createStyledTable(commentsTableModel);
        setupTableStyle(commentsTable);
        
        commentFilterCombo = new JComboBox<>(new String[]{"All Comments", "Technician Comments", "Counter Staff Comments"});
        commentFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        commentFilterCombo.setBackground(Color.WHITE);
        commentFilterCombo.setPreferredSize(new Dimension(150, 30));
        
        commentsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        commentsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        commentsTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        commentsTable.getColumnModel().getColumn(3).setPreferredWidth(350);
    }
    
    private void setupTableStyle(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
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

        // Appointment panel
        JPanel appointmentPanel = createAppointmentPanel();
        appointmentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        appointmentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        // Two column layout for rating cards - create empty placeholders
        JPanel feedbackPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        feedbackPanel.setBackground(PANEL_BG);
        feedbackPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Create empty placeholder cards
        technicianCard = new JPanel();
        technicianCard.setBackground(Color.WHITE);
        technicianCard.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        
        counterStaffCard = new JPanel();
        counterStaffCard.setBackground(Color.WHITE);
        counterStaffCard.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        
        feedbackPanel.add(technicianCard);
        feedbackPanel.add(counterStaffCard);

        // Comments history section
        JPanel historyPanel = createHistoryPanel();
        historyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        historyPanel.setPreferredSize(new Dimension(0, 280));

        content.add(appointmentPanel);
        content.add(Box.createVerticalStrut(15));
        content.add(feedbackPanel);
        content.add(Box.createVerticalStrut(15));
        content.add(historyPanel);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(PANEL_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pagePanel.add(scrollPane, BorderLayout.CENTER);
        add(pagePanel, BorderLayout.CENTER);
    }

    private void updateRatingCards(Appointment appointment) {
        // Check existing comments
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
        
        // Technician card
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
        
        // Counter Staff card
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
        
        // Replace cards
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
        
        JLabel titleLabel = new JLabel("Feedback & Comments");
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
        JLabel appointmentLabel = new JLabel("Select Appointment:");
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
        
        // Status label beside title
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
        
        header.add(titlePanel, BorderLayout.WEST);
        
        // Update person label text
        personLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        personLabel.setForeground(new Color(100, 116, 139));
        header.add(personLabel, BorderLayout.EAST);
        
        card.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(14, 16, 16, 16));

        JPanel ratingPanel = new JPanel();
        ratingPanel.setLayout(new BoxLayout(ratingPanel, BoxLayout.Y_AXIS));
        ratingPanel.setBackground(Color.WHITE);

        JLabel ratingLabel = new JLabel("Rating (1-5)");
        ratingLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ratingLabel.setForeground(new Color(31, 41, 55));
        ratingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider.setEnabled(isEnabled);

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
        commentScroll.setPreferredSize(new Dimension(0, 100));
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
    
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                new EmptyBorder(0, 0, 0, 0)
        ));
        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LIGHT_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                new EmptyBorder(10, 16, 10, 16)
        ));
        
        JLabel titleLabel = new JLabel("My Comments History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(NAVY_BLUE);
        header.add(titleLabel, BorderLayout.WEST);
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setBackground(LIGHT_BG);
        
        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        filterLabel.setForeground(new Color(100, 116, 139));
        filterPanel.add(filterLabel);
        filterPanel.add(commentFilterCombo);
        
        header.add(filterPanel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(commentsTable);
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
        for (Appointment appointment : appointmentDAO.findByCustomerId(currentUser.getId())) {
            if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
                result.add(appointment);
            }
        }
        return result;
    }

    private void loadAppointments() {
        appointmentCombo.removeAllItems();
        for (Appointment appointment : getEligibleAppointments()) {
            appointmentCombo.addItem(new AppointmentItem(appointment));
        }
        if (appointmentCombo.getItemCount() > 0) {
            appointmentCombo.setSelectedIndex(0);
        }
        updateSelectionInfo();
    }

    private void updateSelectionInfo() {
        AppointmentItem selected = (AppointmentItem) appointmentCombo.getSelectedItem();
        if (selected == null) {
            technicianInfoLabel.setText("Technician: -");
            counterStaffInfoLabel.setText("Counter Staff: -");
            return;
        }

        Appointment appointment = selected.getAppointment();
        
        String technicianName = resolveUserName(appointment.getTechnicianId());
        technicianInfoLabel.setText("Technician: " + (technicianName != null ? technicianName : "-"));
        
        String counterStaffName = resolveUserName(appointment.getCounterStaffId());
        counterStaffInfoLabel.setText("Counter Staff: " + (counterStaffName != null ? counterStaffName : "-"));
        
        // Update the rating cards dynamically
        updateRatingCards(appointment);
    }
    
    private void updateButtonWithStatus(JButton button, String baseText, boolean enabled, String status) {
        if (status != null && !status.isEmpty()) {
            button.setText(baseText + " (" + status + ")");
        } else {
            button.setText(baseText);
        }
        button.setEnabled(enabled);
    }

    private void refreshCommentsTable() {
        commentsTableModel.setRowCount(0);
        
        List<Comment> allComments = commentDAO.findByCustomerId(currentUser.getId());
        String filter = (String) commentFilterCombo.getSelectedItem();
        
        for (Comment comment : allComments) {
            if ("Technician Comments".equals(filter) && comment.getTechnicianId() == null) continue;
            if ("Counter Staff Comments".equals(filter) && comment.getCounterStaffId() == null) continue;
            
            String target = "";
            String staffName = "";
            if (comment.getTechnicianId() != null && !comment.getTechnicianId().isEmpty()) {
                staffName = resolveUserName(comment.getTechnicianId());
                target = "Technician - " + (staffName != null ? staffName : "Unknown");
            } else if (comment.getCounterStaffId() != null && !comment.getCounterStaffId().isEmpty()) {
                staffName = resolveUserName(comment.getCounterStaffId());
                target = "Counter Staff - " + (staffName != null ? staffName : "Unknown");
            } else {
                target = "-";
            }
            
            commentsTableModel.addRow(new Object[]{
                    comment.getAppointmentId(),
                    target,
                    comment.getRating() + "/5",
                    comment.getContent()
            });
        }
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

        // Check if already submitted
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
            refreshCommentsTable();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to save your comment.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshAll() {
        loadAppointments();
        refreshCommentsTable();
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
        commentFilterCombo.addActionListener(e -> refreshCommentsTable());
        refreshButton.addActionListener(e -> {
            refreshAll();
            JOptionPane.showMessageDialog(this, "Data has been refreshed", 
                "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
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
                    BorderFactory.createEmptyBorder(12, 15, 12, 10)
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
            String serviceType = appointment.getServiceType() == ServiceType.NORMAL ? "Normal" : "Major";
            return appointment.getId() + " - " + appointment.getDate() + " - " + serviceType;
        }
    }
}