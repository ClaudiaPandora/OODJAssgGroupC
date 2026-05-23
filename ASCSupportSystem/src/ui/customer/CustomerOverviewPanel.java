package ui.customer;

import dao.AppointmentDAO;
import dao.CommentDAO;
import dao.FeedbackDAO;
import dao.PaymentDAO;
import dao.UserDAO;
import enums.AppointmentStatus;
import models.Appointment;
import models.Comment;
import models.Feedback;
import models.Payment;
import models.User;
import ui.common.BasePanel;
import utils.DateUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CustomerOverviewPanel extends BasePanel {

    private final User currentUser;
    private final AppointmentDAO appointmentDAO;
    private final PaymentDAO paymentDAO;
    private final CommentDAO commentDAO;
    private final FeedbackDAO techNoteDAO;
    private final UserDAO userDAO;
    private JButton refreshButton;
    private JPanel statsPanel;

    private JPanel highlightCardContainer;
    private CardLayout highlightCardLayout;
    private JLabel highlightCounterLabel;
    private Timer carouselTimer;
    private List<JPanel> highlightCards;
    private int highlightIndex = 0;

    private final Color CARD_BORDER = new Color(226, 232, 240);
    private final Color LIGHT_BG = new Color(248, 250, 252);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color GREEN = new Color(34, 197, 94);
    private final Color BLUE = new Color(59, 130, 246);
    private final Color ORANGE = new Color(234, 179, 8);
    private final Color TEAL = new Color(16, 185, 129);
    private final Color PURPLE = new Color(139, 92, 246);

    public CustomerOverviewPanel(User currentUser) {
        this.currentUser = currentUser;
        this.appointmentDAO = new AppointmentDAO();
        this.paymentDAO = new PaymentDAO();
        this.commentDAO = new CommentDAO();
        this.techNoteDAO = new FeedbackDAO();
        this.userDAO = new UserDAO();

        setBackground(PANEL_BG);
        initializeComponents();
        setupLayout();
        addEventHandlers();
        startCarousel();
    }

    private void refreshData() {
        refreshDashboard();
    }

    @Override
    protected void initializeComponents() {
        refreshButton = createStyledButton("Refresh", GREEN);
        refreshButton.addActionListener(e -> {
            refreshData();
            JOptionPane.showMessageDialog(this, "Dashboard data has been refreshed.", 
                "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
        });

        highlightCardLayout = new CardLayout();
        highlightCardContainer = new JPanel(highlightCardLayout);
        highlightCardContainer.setBackground(Color.WHITE);
        highlightCardContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            new EmptyBorder(0, 0, 0, 0)
        ));

        highlightCounterLabel = new JLabel("1 / 1");
        highlightCounterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        highlightCounterLabel.setForeground(TEXT_MUTED);

        highlightCards = buildHighlightCards();
        for (int i = 0; i < highlightCards.size(); i++) {
            highlightCardContainer.add(highlightCards.get(i), "CARD_" + i);
        }
        highlightCounterLabel.setText("1 / " + highlightCards.size());
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
        content.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Stats Cards - 3 cards in a row (full width)
        statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        statsPanel.setBackground(PANEL_BG);
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshStats();
        content.add(statsPanel);
        content.add(Box.createVerticalStrut(20));

        // Middle section - 2x2 grid
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        centerPanel.setBackground(PANEL_BG);
        centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerPanel.add(createUpcomingSchedulePanel());
        centerPanel.add(createCompletedSchedulePanel());
        centerPanel.add(createWaitingForRatingPanel());
        centerPanel.add(createMonthlySpendingPanel());
        
        content.add(centerPanel);
        content.add(Box.createVerticalStrut(20));

        // Highlights Panel
        JPanel highlightsPanel = createHighlightsPanel();
        highlightsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(highlightsPanel);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(PANEL_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        SwingUtilities.invokeLater(() -> {
            scrollPane.getVerticalScrollBar().setValue(0);
        });

        pagePanel.add(scrollPane, BorderLayout.CENTER);
        add(pagePanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_BG);
        headerPanel.setMaximumSize(new Dimension(1000, 50));
        headerPanel.setPreferredSize(new Dimension(1000, 50));
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(PANEL_BG);

        JLabel titleLabel = new JLabel("My Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Track your appointments, ratings, payments, and service highlights.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(subtitleLabel);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        
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

    private void refreshStats() {
        statsPanel.removeAll();

        List<Appointment> appointments = getCustomerAppointments();
        Set<String> paidAppointmentIds = getPaidAppointmentIds();

        int upcomingCount = 0;
        int completedCount = 0;
        int waitingRatingCount = 0;

        for (Appointment appointment : appointments) {
            boolean isPaid = paidAppointmentIds.contains(appointment.getId());
            
            if (appointment.getStatus() == AppointmentStatus.PENDING || appointment.getStatus() == AppointmentStatus.ASSIGNED) {
                upcomingCount++;
            }
            if (appointment.getStatus() == AppointmentStatus.COMPLETED || isPaid) {
                completedCount++;
            }
            if (isWaitingForRating(appointment, paidAppointmentIds)) {
                waitingRatingCount++;
            }
        }

        statsPanel.add(createStatCard("Upcoming Schedule", String.valueOf(upcomingCount), BLUE));
        statsPanel.add(createStatCard("Completed Schedule", String.valueOf(completedCount), GREEN));
        statsPanel.add(createStatCard("Waiting for Rating", String.valueOf(waitingRatingCount), ORANGE));

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private JPanel createStatCard(String label, String value, Color accentColor) {
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
        content.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        
        JLabel valueText = new JLabel(value);
        valueText.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueText.setForeground(accentColor);
        valueText.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(valueText);
        content.add(Box.createVerticalStrut(4));
        
        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        labelText.setForeground(TEXT_MUTED);
        labelText.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(labelText);
        
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }

    private JPanel createUpcomingSchedulePanel() {
        JPanel panel = createCardPanel("My Upcoming Schedule");
        panel.add(createScrollableBulletList(buildUpcomingItems(), "No upcoming appointments found."), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCompletedSchedulePanel() {
        JPanel panel = createCardPanel("Completed Schedule");
        panel.add(createScrollableBulletList(buildCompletedItems(), "No completed appointments found."), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createWaitingForRatingPanel() {
        JPanel panel = createCardPanel("Appointments Waiting for Rating");
        panel.add(createScrollableBulletList(buildWaitingRatingItems(), "Everything is rated. Nice work."), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMonthlySpendingPanel() {
        JPanel panel = createCardPanel("Monthly Payment Summary");

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        double monthlySpent = calculateMonthlySpent();
        YearMonth currentMonth = YearMonth.now();
        String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        JLabel totalLabel = new JLabel(String.format("RM %.2f", monthlySpent));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalLabel.setForeground(TEAL);
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel noteLabel = new JLabel("Total paid in " + monthName);
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        noteLabel.setForeground(TEXT_MUTED);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel amountSection = new JPanel();
        amountSection.setLayout(new BoxLayout(amountSection, BoxLayout.Y_AXIS));
        amountSection.setBackground(Color.WHITE);
        amountSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountSection.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        
        amountSection.add(totalLabel);
        amountSection.add(Box.createVerticalStrut(4));
        amountSection.add(noteLabel);

        JTextArea detailsArea = new JTextArea(buildMonthlyPaymentBreakdown());
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setBackground(Color.WHITE);
        detailsArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BORDER),
            new EmptyBorder(12, 0, 0, 0)
        ));
        detailsArea.setForeground(new Color(71, 85, 105));
        detailsArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(amountSection);
        content.add(detailsArea);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createHighlightsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            new EmptyBorder(0, 0, 0, 0)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LIGHT_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLabel = new JLabel("Customer Feedback Highlights");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(NAVY_BLUE);
        header.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setBackground(LIGHT_BG);
        
        JButton prevButton = createNavButton("Previous");
        JButton nextButton = createNavButton("Next");
        
        prevButton.addActionListener(e -> showPreviousHighlight());
        nextButton.addActionListener(e -> showNextHighlight());
        
        buttonPanel.add(highlightCounterLabel);
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        header.add(buttonPanel, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);
        panel.add(highlightCardContainer, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 200));

        return panel;
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(new Color(230, 230, 230));
        button.setForeground(new Color(31, 41, 55));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(70, 28));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(200, 200, 200));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(230, 230, 230));
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
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setPreferredSize(new Dimension(90, 34));
        
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

    private Set<String> getPaidAppointmentIds() {
        Set<String> paidIds = new HashSet<>();
        for (Payment payment : paymentDAO.readAll()) {
            paidIds.add(payment.getAppointmentId());
        }
        return paidIds;
    }

    private List<JPanel> buildHighlightCards() {
        List<JPanel> cards = new ArrayList<>();
        Set<String> myAppointmentIds = getCustomerAppointmentIds();

        for (Comment comment : commentDAO.findByCustomerId(currentUser.getId())) {
            String title;
            String person;

            if (comment.getTechnicianId() != null && !comment.getTechnicianId().trim().isEmpty()) {
                title = "Your Rating for Technician";
                person = resolveUserName(comment.getTechnicianId());
            } else {
                title = "Your Rating for Counter Staff";
                person = resolveUserName(comment.getCounterStaffId());
            }

            cards.add(createHighlightCard(
                    title,
                    "Appointment " + comment.getAppointmentId() + "  •  " + person + "  •  Rating " + comment.getRating() + "/5",
                    comment.getContent(),
                    new Color(255, 251, 235)
            ));
        }

        for (Feedback techNote : techNoteDAO.readAll()) {
            if (myAppointmentIds.contains(techNote.getAppointmentId())) {
                String technicianName = resolveUserName(techNote.getTechnicianId());
                cards.add(createHighlightCard(
                        "Technician Service Note",
                        "Appointment " + techNote.getAppointmentId() + "  •  Technician: " + technicianName,
                        techNote.getContent(),
                        new Color(239, 246, 255)
                ));
            }
        }

        if (cards.isEmpty()) {
            cards.add(createHighlightCard(
                    "No Feedback Yet",
                    "Feedback highlights will appear here after ratings and comments are submitted.",
                    "Use the My Feedback page to rate recent appointments and review technician comments.",
                    new Color(248, 250, 252)
            ));
        }

        return cards;
    }

    private JPanel createHighlightCard(String title, String subtitle, String text, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(bgColor);
        leftPanel.setPreferredSize(new Dimension(280, 0));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("<html>" + subtitle + "</html>");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(6));
        leftPanel.add(subtitleLabel);

        JTextArea body = new JTextArea(text == null || text.trim().isEmpty() ? "No comment text available." : text);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        body.setBackground(bgColor);
        body.setBorder(null);
        body.setForeground(new Color(51, 65, 85));

        card.add(leftPanel, BorderLayout.WEST);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            new EmptyBorder(0, 0, 0, 0)
        ));
        panel.setPreferredSize(new Dimension(480, 220));
        panel.setMinimumSize(new Dimension(480, 220));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(LIGHT_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(titleLabel);
        panel.add(header, BorderLayout.NORTH);
        return panel;
    }

    private JScrollPane createScrollableBulletList(List<String[]> items, String emptyText) {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(new EmptyBorder(8, 16, 12, 16));

        if (items.isEmpty()) {
            JLabel emptyLabel = new JLabel(emptyText);
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            emptyLabel.setForeground(new Color(156, 163, 175));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            listPanel.add(emptyLabel);
        } else {
            for (String[] item : items) {
                listPanel.add(createListItem(item[0], item[1]));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        
        return scrollPane;
    }

    private JPanel createListItem(String primary, String secondary) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setBackground(Color.WHITE);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(231, 235, 240)),
            new EmptyBorder(8, 0, 8, 0)
        ));

        JLabel primaryLabel = new JLabel(primary);
        primaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        primaryLabel.setForeground(new Color(31, 41, 55));
        primaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel secondaryLabel = new JLabel(secondary);
        secondaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        secondaryLabel.setForeground(TEXT_MUTED);
        secondaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        item.add(primaryLabel);
        item.add(Box.createVerticalStrut(4));
        item.add(secondaryLabel);
        return item;
    }

    private List<String[]> buildUpcomingItems() {
        List<String[]> items = new ArrayList<>();
        List<Appointment> appointments = getUpcomingAppointments();
        int limit = Math.min(appointments.size(), 15);

        for (int i = 0; i < limit; i++) {
            Appointment a = appointments.get(i);
            items.add(new String[]{
                    a.getDate(),
                    "Appointment " + a.getId() + " | " + a.getStartTime() + " | " + a.getServiceType() + " | " + a.getStatus()
            });
        }
        return items;
    }

    private List<String[]> buildCompletedItems() {
        List<String[]> items = new ArrayList<>();
        List<Appointment> appointments = getCompletedAppointments();
        int limit = Math.min(appointments.size(), 15);

        for (int i = 0; i < limit; i++) {
            Appointment a = appointments.get(i);
            items.add(new String[]{
                    a.getDate(),
                    "Appointment " + a.getId() + " | " + a.getServiceType() + " | Technician: " + resolveUserName(a.getTechnicianId())
            });
        }
        return items;
    }

    private List<String[]> buildWaitingRatingItems() {
        List<String[]> items = new ArrayList<>();
        List<Appointment> appointments = getWaitingRatingAppointments();
        int limit = Math.min(appointments.size(), 15);

        for (int i = 0; i < limit; i++) {
            Appointment a = appointments.get(i);
            items.add(new String[]{
                    a.getDate(),
                    "Appointment " + a.getId() + " | " + a.getServiceType()
            });
        }
        return items;
    }

    private List<Appointment> getCustomerAppointments() {
        return appointmentDAO.findByCustomerId(currentUser.getId());
    }

    private Set<String> getCustomerAppointmentIds() {
        Set<String> ids = new HashSet<>();
        for (Appointment appointment : getCustomerAppointments()) {
            ids.add(appointment.getId());
        }
        return ids;
    }

    private List<Appointment> getUpcomingAppointments() {
        List<Appointment> list = new ArrayList<>();
        for (Appointment appointment : getCustomerAppointments()) {
            if (appointment.getStatus() == AppointmentStatus.PENDING || appointment.getStatus() == AppointmentStatus.ASSIGNED) {
                list.add(appointment);
            }
        }
        list.sort(Comparator.comparing(Appointment::getDate).thenComparing(Appointment::getStartTime));
        return list;
    }

    private List<Appointment> getCompletedAppointments() {
        List<Appointment> list = new ArrayList<>();
        Set<String> paidAppointmentIds = getPaidAppointmentIds();
        
        for (Appointment appointment : getCustomerAppointments()) {
            boolean isPaid = paidAppointmentIds.contains(appointment.getId());
            if (appointment.getStatus() == AppointmentStatus.COMPLETED || isPaid) {
                list.add(appointment);
            }
        }
        list.sort(Comparator.comparing(Appointment::getDate).reversed().thenComparing(Appointment::getStartTime));
        return list;
    }

    private List<Appointment> getWaitingRatingAppointments() {
        List<Appointment> list = new ArrayList<>();
        Set<String> paidAppointmentIds = getPaidAppointmentIds();
        
        for (Appointment appointment : getCustomerAppointments()) {
            if (isWaitingForRating(appointment, paidAppointmentIds)) {
                list.add(appointment);
            }
        }
        list.sort(Comparator.comparing(Appointment::getDate).reversed().thenComparing(Appointment::getStartTime));
        return list;
    }

    private boolean isWaitingForRating(Appointment appointment, Set<String> paidAppointmentIds) {
        boolean isCompleted = appointment.getStatus() == AppointmentStatus.COMPLETED || paidAppointmentIds.contains(appointment.getId());
        if (!isCompleted) {
            return false;
        }

        boolean technicianRated = false;
        boolean counterStaffRated = false;

        for (Comment comment : commentDAO.findByAppointmentId(appointment.getId())) {
            if (currentUser.getId().equals(comment.getCustomerId())) {
                if (comment.getTechnicianId() != null && !comment.getTechnicianId().trim().isEmpty()) {
                    technicianRated = true;
                }
                if (comment.getCounterStaffId() != null && !comment.getCounterStaffId().trim().isEmpty()) {
                    counterStaffRated = true;
                }
            }
        }

        boolean needTechnician = appointment.getTechnicianId() != null && !appointment.getTechnicianId().trim().isEmpty() && !technicianRated;
        boolean needCounterStaff = appointment.getCounterStaffId() != null && !appointment.getCounterStaffId().trim().isEmpty() && !counterStaffRated;

        return needTechnician || needCounterStaff;
    }

    private double calculateMonthlySpent() {
        double total = 0.0;
        Set<String> myAppointmentIds = getCustomerAppointmentIds();

        for (Payment payment : paymentDAO.readAll()) {
            if (myAppointmentIds.contains(payment.getAppointmentId()) && isCurrentMonth(payment.getPaymentDate())) {
                total += payment.getAmount();
            }
        }
        return total;
    }

    private String buildMonthlyPaymentBreakdown() {
        StringBuilder builder = new StringBuilder();
        Set<String> myAppointmentIds = getCustomerAppointmentIds();
        int count = 0;

        for (Payment payment : paymentDAO.readAll()) {
            if (myAppointmentIds.contains(payment.getAppointmentId()) && isCurrentMonth(payment.getPaymentDate())) {
                builder.append("• ")
                        .append(payment.getPaymentDate())
                        .append(" - Appointment ")
                        .append(payment.getAppointmentId())
                        .append(": RM ")
                        .append(String.format("%.2f", payment.getAmount()))
                        .append("\n");
                count++;
            }
        }

        if (count == 0) {
            builder.append("No payments recorded for this month.");
        }
        return builder.toString();
    }

    private boolean isCurrentMonth(String dateText) {
        try {
            YearMonth currentMonth = YearMonth.now();
            YearMonth paymentMonth = YearMonth.from(DateUtils.parseDate(dateText));
            return currentMonth.equals(paymentMonth);
        } catch (Exception e) {
            return false;
        }
    }

    private String resolveUserName(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return "-";
        }
        User user = userDAO.findById(userId);
        return user != null ? user.getFullName() : userId;
    }

    private void refreshDashboard() {
        refreshStats();
        
        highlightCards = buildHighlightCards();
        highlightCardContainer.removeAll();
        for (int i = 0; i < highlightCards.size(); i++) {
            highlightCardContainer.add(highlightCards.get(i), "CARD_" + i);
        }
        highlightIndex = 0;
        highlightCardLayout.show(highlightCardContainer, "CARD_0");
        updateHighlightCounter();
        
        revalidate();
        repaint();
    }

    private void startCarousel() {
        if (highlightCards.size() <= 1) {
            return;
        }
        carouselTimer = new Timer(2000, e -> showNextHighlight());
        carouselTimer.start();
    }

    private void showNextHighlight() {
        highlightIndex = (highlightIndex + 1) % highlightCards.size();
        highlightCardLayout.show(highlightCardContainer, "CARD_" + highlightIndex);
        updateHighlightCounter();
    }

    private void showPreviousHighlight() {
        highlightIndex = (highlightIndex - 1 + highlightCards.size()) % highlightCards.size();
        highlightCardLayout.show(highlightCardContainer, "CARD_" + highlightIndex);
        updateHighlightCounter();
    }

    private void updateHighlightCounter() {
        highlightCounterLabel.setText((highlightIndex + 1) + " / " + highlightCards.size());
    }

    @Override
    protected void addEventHandlers() {
        refreshButton.addActionListener(e -> refreshDashboard());
    }
}