package ui.manager;

import dao.AppointmentDAO;
import dao.FeedbackDAO;
import dao.CommentDAO;
import dao.UserDAO;
import models.*;
import ui.common.BasePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

public class FeedbackPanel extends BasePanel {
    
    private FeedbackDAO techNoteDAO;
    private CommentDAO commentDAO;
    private AppointmentDAO appointmentDAO;
    private UserDAO userDAO;
    private JPanel feedbackContainer;
    private JPanel commentContainer;
    private JScrollPane feedbackScrollPane;
    private JScrollPane commentScrollPane;
    private JLabel feedbackCountLabel;
    private JLabel commentCountLabel;
    private JButton refreshButton;
    private JComboBox<String> ratingFilterCombo;
    private List<Comment> allComments;
    
    private final Color CARD_BORDER = new Color(226, 232, 240);
    private final Color LIGHT_BG = new Color(248, 250, 252);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color RATING_COLOR = new Color(234, 179, 8);
    
    public FeedbackPanel() {
        this.techNoteDAO = new FeedbackDAO();
        this.commentDAO = new CommentDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.userDAO = new UserDAO();
        
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initializeComponents();
        setupLayout();
        addEventHandlers();
        loadData();
    }
    
    @Override
    protected void initializeComponents() {
        feedbackContainer = new JPanel();
        feedbackContainer.setLayout(new BoxLayout(feedbackContainer, BoxLayout.Y_AXIS));
        feedbackContainer.setBackground(Color.WHITE);
        
        commentContainer = new JPanel();
        commentContainer.setLayout(new BoxLayout(commentContainer, BoxLayout.Y_AXIS));
        commentContainer.setBackground(Color.WHITE);
        
        feedbackScrollPane = new JScrollPane(feedbackContainer);
        commentScrollPane = new JScrollPane(commentContainer);
        
        feedbackScrollPane.setBorder(null);
        commentScrollPane.setBorder(null);
        
        feedbackCountLabel = new JLabel();
        feedbackCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        feedbackCountLabel.setForeground(TEXT_MUTED);
        
        commentCountLabel = new JLabel();
        commentCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        commentCountLabel.setForeground(TEXT_MUTED);
        
        // Rating filter combo box - for customer comments only
        String[] ratingOptions = {"All Ratings", "5 Stars", "4 Stars", "3 Stars", "2 Stars", "1 Star"};
        ratingFilterCombo = new JComboBox<>(ratingOptions);
        ratingFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ratingFilterCombo.setPreferredSize(new Dimension(110, 24));
        ratingFilterCombo.setMaximumSize(new Dimension(110, 26));
        ratingFilterCombo.setFocusable(false);
        ratingFilterCombo.addActionListener(e -> applyRatingFilter());
        
        refreshButton = createStyledButton("Refresh", new Color(34, 197, 94));
        refreshButton.addActionListener(e -> refreshData());
    }
    
    @Override
    protected void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(PANEL_BG);
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Two column layout with GridLayout for equal heights
        JPanel twoColumnPanel = new JPanel(new GridBagLayout());
        twoColumnPanel.setBackground(PANEL_BG);
        twoColumnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        twoColumnPanel.setPreferredSize(new Dimension(0, 520));
        twoColumnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 520));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        
        // Technician Notes Section (Left)
        gbc.gridx = 0;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 15);  
        JPanel feedbackSection = createTechnicianNotesSection();
        twoColumnPanel.add(feedbackSection, gbc);
        
        // Customer Comments Section (Right)
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 0);  
        JPanel commentSection = createCustomerCommentsSection();
        twoColumnPanel.add(commentSection, gbc);
                
        mainPanel.add(twoColumnPanel);
        
        add(mainPanel, BorderLayout.CENTER);
   }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("Technician Feedbacks & Customer Comments");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        panel.add(titleLabel, BorderLayout.WEST);
        
        panel.add(refreshButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createTechnicianNotesSection() {
        JPanel section = new JPanel();
        section.setLayout(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        section.setPreferredSize(new Dimension(0, 520));
        section.setMinimumSize(new Dimension(0, 520));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 520));
        
        // Header without rating filter
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LIGHT_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        header.setPreferredSize(new Dimension(0, 45));
        
        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerLeft.setBackground(LIGHT_BG);
        
        JLabel titleLabel = new JLabel("Technician Feedbacks");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(NAVY_BLUE);
        headerLeft.add(titleLabel);
        
        header.add(headerLeft, BorderLayout.WEST);
        
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setBackground(LIGHT_BG);
        headerRight.add(feedbackCountLabel);
        
        header.add(headerRight, BorderLayout.EAST);
        
        section.add(header, BorderLayout.NORTH);
        
        // Content - scroll pane fills remaining space
        feedbackScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        feedbackScrollPane.getViewport().setBackground(Color.WHITE);
        section.add(feedbackScrollPane, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createCustomerCommentsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        section.setPreferredSize(new Dimension(0, 520));
        section.setMinimumSize(new Dimension(0, 520));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 520));
        
        // Header with rating filter - same height as technician header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LIGHT_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        header.setPreferredSize(new Dimension(0, 45));
        
        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerLeft.setBackground(LIGHT_BG);
        
        JLabel titleLabel = new JLabel("Customer Comments");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(NAVY_BLUE);
        headerLeft.add(titleLabel);
        
        header.add(headerLeft, BorderLayout.WEST);
        
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setBackground(LIGHT_BG);
        
        JLabel filterLabel = new JLabel("Filter by Rating:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        filterLabel.setForeground(TEXT_MUTED);
        headerRight.add(filterLabel);
        headerRight.add(ratingFilterCombo);
        
        commentCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        headerRight.add(commentCountLabel);
        
        header.add(headerRight, BorderLayout.EAST);
        
        section.add(header, BorderLayout.NORTH);
        
        // Content - scroll pane fills remaining space
        commentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        commentScrollPane.getViewport().setBackground(Color.WHITE);
        section.add(commentScrollPane, BorderLayout.CENTER);
        
        return section;
    }
    
    private void loadData() {
        // Clear existing content
        feedbackContainer.removeAll();
        commentContainer.removeAll();
        
        // Load fresh data from files
        List<Feedback> allTechNotes = techNoteDAO.readAll();
        allComments = commentDAO.readAll();
        
        // Update count labels
        feedbackCountLabel.setText(allTechNotes.size() + " entries");
        commentCountLabel.setText(allComments.size() + " entries");
        
        // Load technician notes (no rating filter)
        if (allTechNotes.isEmpty()) {
            addEmptyMessage(feedbackContainer, "No technician feedbacks available yet.");
        } else {
            for (int i = 0; i < allTechNotes.size(); i++) {
                feedbackContainer.add(createTechNoteEntry(allTechNotes.get(i)));
                if (i < allTechNotes.size() - 1) {
                    addSeparator(feedbackContainer);
                }
            }
        }
        
        // Load customer comments (with rating filter applied)
        applyRatingFilter();
        
        // Reset scroll position to top
        SwingUtilities.invokeLater(() -> {
            feedbackScrollPane.getVerticalScrollBar().setValue(0);
            commentScrollPane.getVerticalScrollBar().setValue(0);
        });
        
        // Refresh UI
        feedbackContainer.revalidate();
        feedbackContainer.repaint();
        commentContainer.revalidate();
        commentContainer.repaint();
    }
    
    private void applyRatingFilter() {
        commentContainer.removeAll();
        
        String selectedFilter = (String) ratingFilterCombo.getSelectedItem();
        int filterRating = 0;
        
        if (selectedFilter != null) {
            switch (selectedFilter) {
                case "5 Stars":
                    filterRating = 5;
                    break;
                case "4 Stars":
                    filterRating = 4;
                    break;
                case "3 Stars":
                    filterRating = 3;
                    break;
                case "2 Stars":
                    filterRating = 2;
                    break;
                case "1 Star":
                    filterRating = 1;
                    break;
                default:
                    filterRating = 0;
                    break;
            }
        }
        
        final int finalFilterRating = filterRating;
        
        List<Comment> filteredComments = allComments;
        if (finalFilterRating > 0) {
            filteredComments = allComments.stream()
                .filter(c -> c.getRating() == finalFilterRating)
                .collect(Collectors.toList());
        }
        
        int displayCount = filteredComments.size();
        commentCountLabel.setText(displayCount + " entries");
        
        if (filteredComments.isEmpty()) {
            String message = finalFilterRating > 0 ? "No comments with " + finalFilterRating + " stars yet." : "No customer comments available yet.";
            addEmptyMessage(commentContainer, message);
        } else {
            for (int i = 0; i < filteredComments.size(); i++) {
                commentContainer.add(createCommentEntry(filteredComments.get(i)));
                if (i < filteredComments.size() - 1) {
                    addSeparator(commentContainer);
                }
            }
        }
        
        commentContainer.revalidate();
        commentContainer.repaint();
    }
    
    private void addEmptyMessage(JPanel container, String message) {
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setPreferredSize(new Dimension(0, 200));
        
        JLabel emptyLabel = new JLabel(message);
        emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        emptyLabel.setForeground(TEXT_MUTED);
        emptyPanel.add(emptyLabel);
        container.add(emptyPanel);
    }
    
    private void addSeparator(JPanel container) {
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(230, 230, 230));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        container.add(separator);
    }
    
    private JPanel createTechNoteEntry(Feedback techNote) {
        JPanel entry = new JPanel();
        entry.setLayout(new BoxLayout(entry, BoxLayout.Y_AXIS));
        entry.setBackground(Color.WHITE);
        entry.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        entry.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Row 1: Appointment ID and Technician Name
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);
        
        Appointment appointment = appointmentDAO.findById(techNote.getAppointmentId());
        User technician = userDAO.findById(techNote.getTechnicianId());
        
        String appointmentInfo = "Job #" + techNote.getAppointmentId();
        String technicianName = technician != null ? technician.getFullName() : "Unknown Technician";
        
        JLabel leftLabel = new JLabel(appointmentInfo);
        leftLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        leftLabel.setForeground(NAVY_BLUE);
        
        JLabel rightLabel = new JLabel("Technician: " + technicianName);
        rightLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rightLabel.setForeground(TEXT_MUTED);
        
        headerRow.add(leftLabel, BorderLayout.WEST);
        headerRow.add(rightLabel, BorderLayout.EAST);
        
        entry.add(headerRow);
        entry.add(Box.createVerticalStrut(6));
        
        // Row 2: Note Content - using JTextArea with proper sizing
        JTextArea contentArea = new JTextArea(techNote.getContent());
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentArea.setForeground(new Color(60, 60, 60));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(Color.WHITE);
        contentArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        contentArea.setMargin(new Insets(0, 0, 0, 0));
        
        // Calculate and set preferred size based on content
        int contentWidth = 400;
        contentArea.setSize(contentWidth, Integer.MAX_VALUE);
        int preferredHeight = contentArea.getPreferredSize().height;
        contentArea.setPreferredSize(new Dimension(contentWidth, preferredHeight));
        contentArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
        
        entry.add(contentArea);
        entry.add(Box.createVerticalStrut(4));
        
        // Row 3: Date
        JPanel dateRow = new JPanel(new BorderLayout());
        dateRow.setBackground(Color.WHITE);
        
        JLabel dateLabel = new JLabel("Date: " + techNote.getDate());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dateLabel.setForeground(TEXT_MUTED);
        
        dateRow.add(dateLabel, BorderLayout.WEST);
        
        entry.add(dateRow);
        
        entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, entry.getPreferredSize().height));
        
        return entry;
    }

    private JPanel createCommentEntry(Comment comment) {
        JPanel entry = new JPanel();
        entry.setLayout(new BoxLayout(entry, BoxLayout.Y_AXIS));
        entry.setBackground(Color.WHITE);
        entry.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        entry.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Row 1: Appointment ID and Target (Technician/Counter Staff)
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);
        
        Appointment appointment = appointmentDAO.findById(comment.getAppointmentId());
        
        String appointmentInfo = "Appointment #" + comment.getAppointmentId();
        
        JLabel leftLabel = new JLabel(appointmentInfo);
        leftLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        leftLabel.setForeground(NAVY_BLUE);
        
        // Determine who the comment is about
        String targetInfo = "";
        if (comment.getTechnicianId() != null && !comment.getTechnicianId().isEmpty()) {
            User technician = userDAO.findById(comment.getTechnicianId());
            targetInfo = "About Technician: " + (technician != null ? technician.getFullName() : "Unknown");
        } else if (comment.getCounterStaffId() != null && !comment.getCounterStaffId().isEmpty()) {
            User staff = userDAO.findById(comment.getCounterStaffId());
            targetInfo = "About Counter Staff: " + (staff != null ? staff.getFullName() : "Unknown");
        }
        
        JLabel rightLabel = new JLabel(targetInfo);
        rightLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rightLabel.setForeground(NAVY_BLUE);

        headerRow.add(leftLabel, BorderLayout.WEST);
        headerRow.add(rightLabel, BorderLayout.EAST);
        
        entry.add(headerRow);
        entry.add(Box.createVerticalStrut(6));
        
        // Row 2: Comment Content - using JTextArea with proper sizing
        JTextArea contentArea = new JTextArea(comment.getContent());
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentArea.setForeground(new Color(60, 60, 60));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(Color.WHITE);
        contentArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        contentArea.setMargin(new Insets(0, 0, 0, 0));
        
        // Calculate and set preferred size based on content
        int contentWidth = 400;
        contentArea.setSize(contentWidth, Integer.MAX_VALUE);
        int preferredHeight = contentArea.getPreferredSize().height;
        contentArea.setPreferredSize(new Dimension(contentWidth, preferredHeight));
        contentArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
        
        entry.add(contentArea);
        entry.add(Box.createVerticalStrut(4));
        
        // Row 3: Rating
        JPanel ratingRow = new JPanel(new BorderLayout());
        ratingRow.setBackground(Color.WHITE);
        
        JLabel ratingLabel = new JLabel("Rating: " + comment.getRating() + " / 5");
        ratingLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ratingLabel.setForeground(RATING_COLOR);
        
        ratingRow.add(ratingLabel, BorderLayout.WEST);
        
        entry.add(ratingRow);
        
        entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, entry.getPreferredSize().height));
        
        return entry;
    }
    
    public void refreshData() {
        // Re-initialize DAOs to ensure fresh data from files
        this.techNoteDAO = new FeedbackDAO();
        this.commentDAO = new CommentDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.userDAO = new UserDAO();
        
        loadData();
        
        JOptionPane.showMessageDialog(this, 
            "Data has been refreshed", 
            "Refresh Complete", 
            JOptionPane.INFORMATION_MESSAGE);
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
        button.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        button.setPreferredSize(new Dimension(90, 34));
        
        Color originalColor = color;
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(originalColor.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }
    
    @Override
    protected void addEventHandlers() {
    }
}