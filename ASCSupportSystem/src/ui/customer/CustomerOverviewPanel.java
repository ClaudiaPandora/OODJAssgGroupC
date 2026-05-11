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
    private final FeedbackDAO feedbackDAO;
    private final UserDAO userDAO;

    private JPanel highlightCardContainer;
    private CardLayout highlightCardLayout;
    private JLabel highlightCounterLabel;
    private Timer carouselTimer;
    private List<JPanel> highlightCards;
    private int highlightIndex = 0;

    public CustomerOverviewPanel(User currentUser) {
        this.currentUser = currentUser;
        this.appointmentDAO = new AppointmentDAO();
        this.paymentDAO = new PaymentDAO();
        this.commentDAO = new CommentDAO();
        this.feedbackDAO = new FeedbackDAO();
        this.userDAO = new UserDAO();

        setBackground(PANEL_BG);
        initializeComponents();
        setupLayout();
        addEventHandlers();
        startCarousel();
    }

    @Override
    protected void initializeComponents() {
        highlightCardLayout = new CardLayout();
        highlightCardContainer = new JPanel(highlightCardLayout);
        highlightCardContainer.setBackground(Color.WHITE);

        highlightCounterLabel = new JLabel("1 / 1");
        highlightCounterLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        highlightCounterLabel.setForeground(new Color(120, 120, 120));

        highlightCards = buildHighlightCards();
        for (int i = 0; i < highlightCards.size(); i++) {
            highlightCardContainer.add(highlightCards.get(i), "CARD_" + i);
        }
        highlightCounterLabel.setText("1 / " + highlightCards.size());
    }

    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(PANEL_BG);

        JPanel topPanel = new JPanel(new BorderLayout(0, 14));
        topPanel.setBackground(PANEL_BG);
        topPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        topPanel.add(createStatsPanel(), BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 14, 14));
        centerPanel.setBackground(PANEL_BG);
        centerPanel.add(createUpcomingSchedulePanel());
        centerPanel.add(createCompletedSchedulePanel());
        centerPanel.add(createWaitingForRatingPanel());
        centerPanel.add(createMonthlySpendingPanel());

        JPanel bottomPanel = createHighlightsPanel();
        bottomPanel.setPreferredSize(new Dimension(0, 190));

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_BG);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(PANEL_BG);

        JLabel titleLabel = new JLabel("My Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Track your appointments, ratings, payments, and service highlights.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(120, 120, 120));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(subtitleLabel);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 12, 0));
        panel.setBackground(PANEL_BG);
        panel.setPreferredSize(new Dimension(0, 98));

        List<Appointment> appointments = getCustomerAppointments();

        int upcomingCount = 0;
        int completedCount = 0;
        int waitingRatingCount = 0;

        for (Appointment appointment : appointments) {
            if (appointment.getStatus() == AppointmentStatus.PENDING || appointment.getStatus() == AppointmentStatus.ASSIGNED) {
                upcomingCount++;
            }
            if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.PAID) {
                completedCount++;
            }
            if (isWaitingForRating(appointment)) {
                waitingRatingCount++;
            }
        }

        panel.add(createStatCard("Upcoming Schedule", String.valueOf(upcomingCount), "Pending or assigned appointments", new Color(59, 130, 246)));
        panel.add(createStatCard("Completed Schedule", String.valueOf(completedCount), "Finished service visits", new Color(34, 197, 94)));
        panel.add(createStatCard("Waiting for Rating", String.valueOf(waitingRatingCount), "Appointments still needing feedback", new Color(234, 88, 12)));

        return panel;
    }

    private JPanel createStatCard(String label, String value, String subValue, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 2),
                BorderFactory.createLineBorder(BORDER_DARK, 2)
        ));

        JPanel accentBar = new JPanel();
        accentBar.setBackground(accentColor);
        accentBar.setPreferredSize(new Dimension(4, 0));
        card.add(accentBar, BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.BOLD, 10));
        labelText.setForeground(new Color(120, 120, 120));
        labelText.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueText = new JLabel(value);
        valueText.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueText.setForeground(Color.BLACK);
        valueText.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subText = new JLabel(subValue);
        subText.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subText.setForeground(new Color(130, 130, 130));
        subText.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(labelText);
        content.add(Box.createVerticalStrut(4));
        content.add(valueText);
        content.add(Box.createVerticalStrut(2));
        content.add(subText);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createUpcomingSchedulePanel() {
        JPanel panel = createCardPanel("My Upcoming Schedule", "Your next pending or assigned appointments.");
        panel.add(createScrollableBulletList(buildUpcomingItems(), "No upcoming appointments found."), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCompletedSchedulePanel() {
        JPanel panel = createCardPanel("Completed Schedule", "Your recent completed or paid appointments.");
        panel.add(createScrollableBulletList(buildCompletedItems(), "No completed appointments found."), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createWaitingForRatingPanel() {
        JPanel panel = createCardPanel("Appointments Waiting for Rating", "Completed visits that still need your feedback.");
        panel.add(createScrollableBulletList(buildWaitingRatingItems(), "Everything is rated. Nice work."), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMonthlySpendingPanel() {
        JPanel panel = createCardPanel("Monthly Payment Summary", "Calculated from your payment history records.");

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(10, 15, 12, 15));

        double monthlySpent = calculateMonthlySpent();
        YearMonth currentMonth = YearMonth.now();
        String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        JLabel totalLabel = new JLabel(String.format("RM %.2f", monthlySpent));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        totalLabel.setForeground(NAVY_BLUE);
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel noteLabel = new JLabel("Total paid in " + monthName);
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        noteLabel.setForeground(new Color(120, 120, 120));
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea detailsArea = new JTextArea(buildMonthlyPaymentBreakdown());
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setBackground(Color.WHITE);
        detailsArea.setBorder(null);
        detailsArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(totalLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(noteLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(detailsArea);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHighlightsPanel() {
        JPanel panel = createCardPanel("Customer Feedback Highlights", "Recent comments and feedback connected to your text files.");

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 12, 6, 12));

        JButton previousButton = new JButton("Previous");
        previousButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        previousButton.setBackground(new Color(230, 230, 230));
        previousButton.setForeground(Color.BLACK);
        previousButton.setFocusPainted(false);

        JButton nextButton = new JButton("Next");
        nextButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nextButton.setBackground(new Color(230, 230, 230));
        nextButton.setForeground(Color.BLACK);
        nextButton.setFocusPainted(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(highlightCounterLabel);
        buttonPanel.add(previousButton);
        buttonPanel.add(nextButton);

        topBar.add(buttonPanel, BorderLayout.EAST);

        previousButton.addActionListener(e -> showPreviousHighlight());
        nextButton.addActionListener(e -> showNextHighlight());

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(highlightCardContainer, BorderLayout.CENTER);

        return panel;
    }

    private List<JPanel> buildHighlightCards() {
        List<JPanel> cards = new ArrayList<>();
        Set<String> myAppointmentIds = getCustomerAppointmentIds();

        for (Comment comment : commentDAO.findByCustomerId(currentUser.getId())) {
            String title;
            String person;

            if (comment.getTechnicianId() != null && !comment.getTechnicianId().trim().isEmpty()) {
                title = "Your Technician Comment";
                person = resolveUserName(comment.getTechnicianId());
            } else {
                title = "Your Counter Staff Comment";
                person = resolveUserName(comment.getCounterStaffId());
            }

            cards.add(createHighlightCard(
                    title,
                    "Appointment " + comment.getAppointmentId() + "  •  " + person + "  •  Rating " + comment.getRating() + "/5",
                    comment.getContent(),
                    new Color(255, 247, 238)
            ));
        }

        for (Feedback feedback : feedbackDAO.readAll()) {
            if (myAppointmentIds.contains(feedback.getAppointmentId())) {
                cards.add(createHighlightCard(
                        "Technician Feedback Comment",
                        "Appointment " + feedback.getAppointmentId() + "  •  " + resolveUserName(feedback.getTechnicianId()) + "  •  Rating " + feedback.getRating() + "/5",
                        feedback.getContent(),
                        new Color(240, 247, 255)
                ));
            }
        }

        if (cards.isEmpty()) {
            cards.add(createHighlightCard(
                    "No Feedback Yet",
                    "Feedback highlights will appear here after ratings and comments are submitted.",
                    "Use the My Feedback page to rate recent appointments and review technician comments.",
                    new Color(245, 245, 245)
            ));
        }

        return cards;
    }

    private JPanel createHighlightCard(String title, String subtitle, String text, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(bgColor);
        leftPanel.setPreferredSize(new Dimension(270, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(NAVY_BLUE);

        JLabel subtitleLabel = new JLabel("<html>" + subtitle + "</html>");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(new Color(90, 90, 90));

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(6));
        leftPanel.add(subtitleLabel);

        JTextArea body = new JTextArea(text == null || text.trim().isEmpty() ? "No comment text available." : text);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        body.setBackground(bgColor);
        body.setBorder(null);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(bgColor);
        rightPanel.add(body, BorderLayout.CENTER);

        card.add(leftPanel, BorderLayout.WEST);
        card.add(rightPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCardPanel(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 2),
                BorderFactory.createLineBorder(BORDER_DARK, 2)
        ));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 6, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(new Color(120, 120, 120));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(3));
        header.add(subtitleLabel);

        panel.add(header, BorderLayout.NORTH);
        return panel;
    }

    private JScrollPane createScrollableBulletList(List<String[]> items, String emptyText) {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(BorderFactory.createEmptyBorder(4, 15, 10, 15));

        if (items.isEmpty()) {
            JLabel label = new JLabel(emptyText);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setForeground(new Color(130, 130, 130));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            listPanel.add(label);
        } else {
            for (String[] item : items) {
                listPanel.add(createBulletItem(item[0], item[1]));
            }
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        return scrollPane;
    }

    private JPanel createBulletItem(String heading, String details) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setBackground(Color.WHITE);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel headingLabel = new JLabel("• " + heading);
        headingLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        headingLabel.setForeground(new Color(40, 40, 40));
        headingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel detailLabel = new JLabel("<html><div style='margin-left:14px;'>" + details + "</div></html>");
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailLabel.setForeground(new Color(90, 90, 90));
        detailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        item.add(headingLabel);
        item.add(Box.createVerticalStrut(2));
        item.add(detailLabel);
        return item;
    }

    private List<String[]> buildUpcomingItems() {
        List<String[]> items = new ArrayList<>();
        List<Appointment> appointments = getUpcomingAppointments();
        int limit = Math.min(appointments.size(), 20);

        for (int i = 0; i < limit; i++) {
            Appointment appointment = appointments.get(i);
            items.add(new String[]{
                    appointment.getDate(),
                    "Appointment " + appointment.getId() + "  •  " + appointment.getStartTime()
                            + "  •  " + appointment.getServiceType()
                            + "  •  " + appointment.getStatus()
            });
        }
        return items;
    }

    private List<String[]> buildCompletedItems() {
        List<String[]> items = new ArrayList<>();
        List<Appointment> appointments = getCompletedAppointments();
        int limit = Math.min(appointments.size(), 20);

        for (int i = 0; i < limit; i++) {
            Appointment appointment = appointments.get(i);
            items.add(new String[]{
                    appointment.getDate(),
                    "Appointment " + appointment.getId()
                            + "  •  " + appointment.getServiceType()
                            + "  •  Technician: " + resolveUserName(appointment.getTechnicianId())
            });
        }
        return items;
    }

    private List<String[]> buildWaitingRatingItems() {
        List<String[]> items = new ArrayList<>();
        List<Appointment> appointments = getWaitingRatingAppointments();
        int limit = Math.min(appointments.size(), 20);

        for (int i = 0; i < limit; i++) {
            Appointment appointment = appointments.get(i);
            items.add(new String[]{
                    appointment.getDate(),
                    "Appointment " + appointment.getId()
                            + "  •  Tech: " + resolveUserName(appointment.getTechnicianId())
                            + "  •  Staff: " + resolveUserName(appointment.getCounterStaffId())
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
        for (Appointment appointment : getCustomerAppointments()) {
            if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.PAID) {
                list.add(appointment);
            }
        }
        list.sort(Comparator.comparing(Appointment::getDate).reversed().thenComparing(Appointment::getStartTime));
        return list;
    }

    private List<Appointment> getWaitingRatingAppointments() {
        List<Appointment> list = new ArrayList<>();
        for (Appointment appointment : getCustomerAppointments()) {
            if (isWaitingForRating(appointment)) {
                list.add(appointment);
            }
        }
        list.sort(Comparator.comparing(Appointment::getDate).reversed().thenComparing(Appointment::getStartTime));
        return list;
    }

    private boolean isWaitingForRating(Appointment appointment) {
        if (appointment.getStatus() != AppointmentStatus.COMPLETED && appointment.getStatus() != AppointmentStatus.PAID) {
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
                        .append("  •  Appointment ")
                        .append(payment.getAppointmentId())
                        .append("  •  RM ")
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

    private void startCarousel() {
        if (highlightCards.size() <= 1) {
            return;
        }
        carouselTimer = new Timer(3200, e -> showNextHighlight());
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
    }
}
