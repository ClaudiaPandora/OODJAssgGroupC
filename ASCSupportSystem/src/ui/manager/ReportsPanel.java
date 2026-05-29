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
import models.Customer;
import models.CounterStaff;
import ui.common.BasePanel;
import utils.DateUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.ArrayList;

public class ReportsPanel extends BasePanel {

    private final Color TEXT_DARK = new Color(31, 41, 55);
    private final Color TEXT_MUTED = new Color(107, 114, 128);
    private final Color CARD_BORDER = new Color(209, 213, 219);
    private final Color BORDER_LIGHT = new Color(229, 231, 235);
    private final Color LIGHT_BG = new Color(248, 250, 252);
    private final Color SELECTED_BLUE_BG = new Color(219, 234, 254);
    private final Color WARNING_YELLOW = new Color(252, 202, 12);
    private final Color WARNING_YELLOW_BG = new Color(255, 251, 235);
    private final Color GREEN = new Color(34, 197, 94);
    private final Color RED = new Color(220, 38, 38);
    
    private AppointmentDAO appointmentDAO;
    private UserDAO userDAO;
    private CommentDAO commentDAO;
    private PaymentDAO paymentDAO;
    private JButton filterButton;
    private JWindow filterWindow;
    private AWTEventListener filterOutsideClickListener;
    private JPanel monthPanel;
    private JPanel datePanel;
    private JComboBox<String> monthCombo;
    private JComboBox<String> yearCombo;
    private JComboBox<Integer> dayCombo;
    private JComboBox<String> dateMonthCombo;
    private JComboBox<String> dateYearCombo;
    private JPanel calendarGridPanel;
    private JPanel mainFilterPanel;
    private JTextArea reportSummaryArea;
    private JPanel summaryBlock;
    private JPanel mainPanel;
    private JPanel contentPanel;
    private CardLayout contentCardLayout;
    private JButton generateButton;
    private boolean reportVisible = false;
    
    private JButton allBtn, serviceBtn, revenueBtn, staffBtn, customerBtn, ratingBtn;
    private String currentCategory = "ALL";
    private String selectedFilter = "All";
    
    public ReportsPanel() {
        refreshData();
        
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initializeComponents();
        setupLayout();
        addEventHandlers();
    }
    
    private void refreshData() {
        this.appointmentDAO = new AppointmentDAO();
        this.userDAO = new UserDAO();
        this.commentDAO = new CommentDAO();
        this.paymentDAO = new PaymentDAO();
    }
    
    private List<Appointment> getFilteredAppointmentsByTimeRange() {
        List<Appointment> allAppointments = appointmentDAO.readAll();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        if ("All".equals(selectedFilter)) {
            return allAppointments;
        }
        
        List<Appointment> filtered = new ArrayList<>();
        
        for (Appointment a : allAppointments) {
            try {
                java.util.Date appointmentDate = sdf.parse(a.getDate());
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(appointmentDate);
                
                if ("Choose Month".equals(selectedFilter)) {
                    int selectedMonth = monthCombo.getSelectedIndex() + 1;
                    int selectedYear = Integer.parseInt((String) yearCombo.getSelectedItem());
                    int appointmentMonth = cal.get(java.util.Calendar.MONTH) + 1;
                    int appointmentYear = cal.get(java.util.Calendar.YEAR);
                    
                    if (appointmentMonth == selectedMonth && appointmentYear == selectedYear) {
                        filtered.add(a);
                    }
                } else if ("Choose Date".equals(selectedFilter)) {
                    if (dateMonthCombo != null && dateYearCombo != null) {
                        int selectedDay = (int) dayCombo.getSelectedItem();
                        int selectedMonth = dateMonthCombo.getSelectedIndex() + 1;
                        int selectedYear = Integer.parseInt((String) dateYearCombo.getSelectedItem());
                        
                        int appointmentDay = cal.get(java.util.Calendar.DAY_OF_MONTH);
                        int appointmentMonth = cal.get(java.util.Calendar.MONTH) + 1;
                        int appointmentYear = cal.get(java.util.Calendar.YEAR);
                        
                        if (appointmentDay == selectedDay && appointmentMonth == selectedMonth && appointmentYear == selectedYear) {
                            filtered.add(a);
                        }
                    }
                }
            } catch (java.text.ParseException e) {
                filtered.add(a);
            }
        }
        
        return filtered;
    }
    
    private List<Appointment> getCompletedAppointments(List<Appointment> appointments) {
        return appointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
            .collect(Collectors.toList());
    }
    
    private void updateDayCombo(JComboBox<String> monthCombo, JComboBox<String> yearCombo) {
        int month = monthCombo.getSelectedIndex() + 1;
        int year = Integer.parseInt((String) yearCombo.getSelectedItem());
        Integer selectedDay = (Integer) dayCombo.getSelectedItem();
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month - 1, 1);
        int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        
        dayCombo.removeAllItems();
        for (int i = 1; i <= daysInMonth; i++) {
            dayCombo.addItem(i);
        }
        if (selectedDay != null) {
            dayCombo.setSelectedItem(Math.min(selectedDay, daysInMonth));
        }
    }
    
    private void refreshReportAfterFilterChange() {
        if (reportVisible) {
            refreshData();
            generateReportSummary(currentCategory);
        }
        refreshContentPanels();
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private void selectFilter(String filter) {
        selectedFilter = filter;
        updateFilterButtonText();
        monthPanel.setVisible("Choose Month".equals(filter));
        datePanel.setVisible("Choose Date".equals(filter));
        buildFilterPopup();
        refreshReportAfterFilterChange();
        if (!"All".equals(filter)) {
            SwingUtilities.invokeLater(this::showFilterPopup);
        }
    }
    
    private void updateFilterButtonText() {
        if ("Choose Month".equals(selectedFilter)) {
            filterButton.setText(monthCombo.getSelectedItem() + " " + yearCombo.getSelectedItem());
        } else if ("Choose Date".equals(selectedFilter)) {
            filterButton.setText(dayCombo.getSelectedItem() + " " + dateMonthCombo.getSelectedItem() + " " + dateYearCombo.getSelectedItem());
        } else {
            filterButton.setText("All");
        }
    }
    
    private void buildFilterPopup() {
        JPanel popupPanel = new JPanel();
        popupPanel.setLayout(new BoxLayout(popupPanel, BoxLayout.Y_AXIS));
        popupPanel.setBackground(Color.WHITE);
        popupPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        popupPanel.add(createFilterOptionButton("All"));
        popupPanel.add(createFilterOptionButton("Choose Month"));
        popupPanel.add(monthPanel);
        popupPanel.add(Box.createVerticalStrut(4));
        popupPanel.add(createFilterOptionButton("Choose Date"));
        popupPanel.add(datePanel);
        
        JPanel popupWrapper = new JPanel(new BorderLayout());
        popupWrapper.setBackground(Color.WHITE);
        popupWrapper.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));
        popupWrapper.add(popupPanel, BorderLayout.CENTER);
        
        if (filterWindow != null) {
            filterWindow.setContentPane(popupWrapper);
            filterWindow.pack();
        }
    }
    
    private void showFilterPopup() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner == null) {
            return;
        }
        if (filterWindow == null || filterWindow.getOwner() != owner) {
            filterWindow = new JWindow(owner);
        }
        buildFilterPopup();
        Point buttonLocation = filterButton.getLocationOnScreen();
        filterWindow.setLocation(buttonLocation.x, buttonLocation.y + filterButton.getHeight());
        filterWindow.setVisible(true);
        installFilterOutsideClickListener();
    }
    
    private void hideFilterPopup() {
        if (filterWindow != null) {
            filterWindow.setVisible(false);
        }
        removeFilterOutsideClickListener();
    }
    
    private void installFilterOutsideClickListener() {
        if (filterOutsideClickListener != null) {
            return;
        }
        filterOutsideClickListener = event -> {
            if (!(event instanceof MouseEvent)) {
                return;
            }
            MouseEvent mouseEvent = (MouseEvent) event;
            if (mouseEvent.getID() != MouseEvent.MOUSE_PRESSED || isFilterAreaClick(mouseEvent)) {
                return;
            }
            hideFilterPopup();
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(filterOutsideClickListener, AWTEvent.MOUSE_EVENT_MASK);
    }
    
    private void removeFilterOutsideClickListener() {
        if (filterOutsideClickListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(filterOutsideClickListener);
            filterOutsideClickListener = null;
        }
    }
    
    private boolean isFilterAreaClick(MouseEvent event) {
        Component component = event.getComponent();
        if (component == null) {
            return false;
        }
        if (SwingUtilities.isDescendingFrom(component, filterButton)) {
            return true;
        }
        if (filterWindow != null && SwingUtilities.isDescendingFrom(component, filterWindow)) {
            return true;
        }
        
        Window eventWindow = SwingUtilities.getWindowAncestor(component);
        while (eventWindow != null) {
            if (eventWindow == filterWindow) {
                return true;
            }
            eventWindow = eventWindow.getOwner();
        }
        return false;
    }
    
    private JButton createFilterOptionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setRolloverEnabled(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(text.equals(selectedFilter) ? SELECTED_BLUE_BG : Color.WHITE);
        button.setForeground(text.equals(selectedFilter) ? NAVY_BLUE : TEXT_DARK);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(e -> {
            selectFilter(text);
            if ("All".equals(text)) {
                hideFilterPopup();
            }
        });
        return button;
    }
    
    private void updateCalendarGrid() {
        if (calendarGridPanel == null || dateMonthCombo == null || dateYearCombo == null || dayCombo == null) {
            return;
        }
        
        calendarGridPanel.removeAll();
        
        int month = dateMonthCombo.getSelectedIndex();
        int year = Integer.parseInt((String) dateYearCombo.getSelectedItem());
        Integer selectedDay = (Integer) dayCombo.getSelectedItem();
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month, 1);
        int firstDayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        
        String[] dayHeaders = {"S", "M", "T", "W", "T", "F", "S"};
        for (String header : dayHeaders) {
            JLabel label = new JLabel(header, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 10));
            label.setForeground(TEXT_MUTED);
            calendarGridPanel.add(label);
        }
        
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarGridPanel.add(Box.createRigidArea(new Dimension(26, 24)));
        }
        
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("Segoe UI", Font.BOLD, 10));
            dayButton.setMargin(new Insets(1, 1, 1, 1));
            dayButton.setPreferredSize(new Dimension(28, 24));
            dayButton.setFocusPainted(false);
            dayButton.setRolloverEnabled(false);
            dayButton.setContentAreaFilled(false);
            dayButton.getModel().setArmed(false);
            dayButton.getModel().setPressed(false);
            dayButton.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
            boolean isSelected = selectedDay != null && selectedDay == day;
            dayButton.setBackground(isSelected ? SELECTED_BLUE_BG : Color.WHITE);
            dayButton.setForeground(isSelected ? NAVY_BLUE : TEXT_DARK);
            dayButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            int chosenDay = day;
            dayButton.addActionListener(e -> {
                dayCombo.setSelectedItem(chosenDay);
                selectFilter("Choose Date");
            });
            calendarGridPanel.add(dayButton);
        }
        
        calendarGridPanel.revalidate();
        calendarGridPanel.repaint();
    }
    
    @Override
    protected void initializeComponents() {
        filterButton = new JButton("All");
        filterButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterButton.setPreferredSize(new Dimension(135, 30));
        filterButton.setHorizontalAlignment(SwingConstants.LEFT);
        filterButton.setFocusPainted(false);
        filterButton.setRolloverEnabled(false);
        filterButton.setContentAreaFilled(false);
        filterButton.setOpaque(true);
        filterButton.setBackground(Color.WHITE);
        filterButton.setForeground(TEXT_DARK);
        filterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        // Month selection panel
        monthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        monthPanel.setBackground(Color.WHITE);
        monthPanel.setBorder(BorderFactory.createEmptyBorder(4, 18, 8, 4));
        monthPanel.setVisible(false);
        monthPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        String[] months = {"January", "February", "March", "April", "May", "June", 
                           "July", "August", "September", "October", "November", "December"};
        monthCombo = new JComboBox<>(months);
        monthCombo.setLightWeightPopupEnabled(false);
        monthCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        monthCombo.setPreferredSize(new Dimension(120, 30));
        
        String[] years = {"2025", "2026", "2027"};
        yearCombo = new JComboBox<>(years);
        yearCombo.setLightWeightPopupEnabled(false);
        yearCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        yearCombo.setPreferredSize(new Dimension(80, 30));
        yearCombo.setSelectedItem("2026");
        
        monthPanel.add(monthCombo);
        monthPanel.add(yearCombo);
        
        // Date selection panel
        datePanel = new JPanel();
        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
        datePanel.setBackground(Color.WHITE);
        datePanel.setBorder(BorderFactory.createEmptyBorder(4, 18, 8, 4));
        datePanel.setVisible(false);
        datePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        dayCombo = new JComboBox<>();
        dayCombo.setLightWeightPopupEnabled(false);
        dayCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dayCombo.setPreferredSize(new Dimension(60, 30));
        
        // Initialize day combo with 31 days
        for (int i = 1; i <= 31; i++) {
            dayCombo.addItem(i);
        }
        
        dateMonthCombo = new JComboBox<>(months);
        dateMonthCombo.setLightWeightPopupEnabled(false);
        dateMonthCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateMonthCombo.setPreferredSize(new Dimension(120, 30));
        
        dateYearCombo = new JComboBox<>(years);
        dateYearCombo.setLightWeightPopupEnabled(false);
        dateYearCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateYearCombo.setPreferredSize(new Dimension(80, 30));
        dateYearCombo.setSelectedItem("2026");
        
        JPanel dateControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dateControlsPanel.setBackground(Color.WHITE);
        dateControlsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateControlsPanel.add(dayCombo);
        dateControlsPanel.add(dateMonthCombo);
        dateControlsPanel.add(dateYearCombo);
        
        calendarGridPanel = new JPanel(new GridLayout(0, 7, 3, 3));
        calendarGridPanel.setBackground(Color.WHITE);
        calendarGridPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        calendarGridPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        datePanel.add(dateControlsPanel);
        datePanel.add(calendarGridPanel);
        
        reportSummaryArea = new JTextArea();
        reportSummaryArea.setEditable(false);
        reportSummaryArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reportSummaryArea.setForeground(TEXT_DARK);
        reportSummaryArea.setBackground(WARNING_YELLOW_BG);
        reportSummaryArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        reportSummaryArea.setLineWrap(true);
        reportSummaryArea.setWrapStyleWord(true);
        
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(PANEL_BG);
        
        updateCalendarGrid();
    }
    
    @Override
    protected void setupLayout() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(PANEL_BG);
        
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        summaryBlock = createSummaryBlock();
        summaryBlock.setVisible(false);
        summaryBlock.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(summaryBlock);
        
        JPanel spacer = new JPanel();
        spacer.setBackground(PANEL_BG);
        spacer.setPreferredSize(new Dimension(0, 15));
        spacer.setVisible(false);
        spacer.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(spacer);
        summaryBlock.putClientProperty("spacer", spacer);
        
        contentPanel.add(createAllCategoriesPanel(), "ALL");
        contentPanel.add(createServiceAnalysisPanel(), "SERVICE");
        contentPanel.add(createRevenueAnalysisPanel(), "REVENUE");
        contentPanel.add(createCustomerAnalysisPanel(), "CUSTOMER");
        contentPanel.add(createStaffPerformancePanel(), "STAFF");
        contentPanel.add(createRatingAnalysisPanel(), "RATING");
        
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(contentPanel);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(50);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_BG);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("Analytical Reports");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setVerticalAlignment(SwingConstants.TOP);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightPanel.setBackground(PANEL_BG);
        
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        categoryPanel.setBackground(Color.WHITE);
        categoryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        
        allBtn = createCategoryButton("ALL");
        serviceBtn = createCategoryButton("SERVICE");
        revenueBtn = createCategoryButton("REVENUE");
        staffBtn = createCategoryButton("STAFF");
        customerBtn = createCategoryButton("CUSTOMER");
        ratingBtn = createCategoryButton("RATING");
        
        categoryPanel.add(allBtn);
        categoryPanel.add(serviceBtn);
        categoryPanel.add(revenueBtn);
        categoryPanel.add(staffBtn);
        categoryPanel.add(customerBtn);
        categoryPanel.add(ratingBtn);
        
        updateCategoryButtonStyle("ALL");
        
        rightPanel.add(categoryPanel);
        
        // Main Filter Panel
        mainFilterPanel = new JPanel();
        mainFilterPanel.setLayout(new BoxLayout(mainFilterPanel, BoxLayout.Y_AXIS));
        mainFilterPanel.setBackground(PANEL_BG);
        
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        topRow.setBackground(PANEL_BG);
        
        JLabel filterLabel = new JLabel("Date Filter:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        filterLabel.setForeground(TEXT_DARK);
        topRow.add(filterLabel);
        topRow.add(filterButton);
        
        mainFilterPanel.add(topRow);
        
        filterButton.addActionListener(e -> {
            showFilterPopup();
        });
        
        monthCombo.addActionListener(e -> {
            selectedFilter = "Choose Month";
            updateFilterButtonText();
            monthPanel.setVisible(true);
            datePanel.setVisible(false);
            refreshReportAfterFilterChange();
            SwingUtilities.invokeLater(this::showFilterPopup);
        });
        
        yearCombo.addActionListener(e -> {
            selectedFilter = "Choose Month";
            updateFilterButtonText();
            monthPanel.setVisible(true);
            datePanel.setVisible(false);
            refreshReportAfterFilterChange();
            SwingUtilities.invokeLater(this::showFilterPopup);
        });
        
        dateMonthCombo.addActionListener(e -> {
            updateDayCombo(dateMonthCombo, dateYearCombo);
            updateCalendarGrid();
            selectedFilter = "Choose Date";
            updateFilterButtonText();
            monthPanel.setVisible(false);
            datePanel.setVisible(true);
            refreshReportAfterFilterChange();
            SwingUtilities.invokeLater(this::showFilterPopup);
        });
        
        dateYearCombo.addActionListener(e -> {
            updateDayCombo(dateMonthCombo, dateYearCombo);
            updateCalendarGrid();
            selectedFilter = "Choose Date";
            updateFilterButtonText();
            monthPanel.setVisible(false);
            datePanel.setVisible(true);
            refreshReportAfterFilterChange();
            SwingUtilities.invokeLater(this::showFilterPopup);
        });
        
        dayCombo.addActionListener(e -> {
            if (dayCombo.getSelectedItem() == null) {
                return;
            }
            updateCalendarGrid();
            selectedFilter = "Choose Date";
            updateFilterButtonText();
            monthPanel.setVisible(false);
            datePanel.setVisible(true);
            refreshReportAfterFilterChange();
            SwingUtilities.invokeLater(this::showFilterPopup);
        });
        
        rightPanel.add(mainFilterPanel);
        
        generateButton = new JButton("Generate Conclusion");
        generateButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        generateButton.setBackground(NAVY_BLUE);
        generateButton.setForeground(Color.WHITE);
        generateButton.setFocusPainted(false);
        generateButton.setBorderPainted(false);
        generateButton.setOpaque(true);
        generateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        generateButton.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        generateButton.setPreferredSize(new Dimension(150, 34));
        
        generateButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (generateButton.getBackground() == NAVY_BLUE) {
                    generateButton.setBackground(NAVY_BLUE.darker());
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (generateButton.getBackground() == NAVY_BLUE.darker()) {
                    generateButton.setBackground(NAVY_BLUE);
                }
            }
        });
        
        generateButton.addActionListener(e -> {
            if (reportVisible) {
                summaryBlock.setVisible(false);
                JPanel spacer = (JPanel) summaryBlock.getClientProperty("spacer");
                if (spacer != null) spacer.setVisible(false);
                generateButton.setText("Generate Conclusion");
                generateButton.setBackground(NAVY_BLUE);
                reportVisible = false;
            } else {
                refreshData();
                generateReportSummary(currentCategory);
                summaryBlock.setVisible(true);
                JPanel spacer = (JPanel) summaryBlock.getClientProperty("spacer");
                if (spacer != null) spacer.setVisible(true);
                refreshContentPanels();
                generateButton.setText("Close Conclusion");
                generateButton.setBackground(RED);
                reportVisible = true;
            }
            mainPanel.revalidate();
            mainPanel.repaint();
        });
        rightPanel.add(generateButton);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        refreshButton.setBackground(GREEN);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setOpaque(true);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        refreshButton.setPreferredSize(new Dimension(90, 34));
        
        refreshButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                refreshButton.setBackground(GREEN.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                refreshButton.setBackground(GREEN);
            }
        });
        
        refreshButton.addActionListener(e -> {
            refreshData();
            if (reportVisible) {
                generateReportSummary(currentCategory);
                refreshContentPanels();
                mainPanel.revalidate();
                mainPanel.repaint();
                JOptionPane.showMessageDialog(this, "Data has been refreshed.", 
                    "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                refreshContentPanels();
                JOptionPane.showMessageDialog(ReportsPanel.this, "Data has been refreshed.", 
                    "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        rightPanel.add(refreshButton);
        
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JButton createCategoryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));
        btn.addActionListener(e -> {
            currentCategory = text;
            updateCategoryButtonStyle(text);
            contentCardLayout.show(contentPanel, text);
            
            if (reportVisible) {
                refreshData();
                generateReportSummary(currentCategory);
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });
        return btn;
    }
    
    private void updateCategoryButtonStyle(String active) {
        JButton[] buttons = {allBtn, serviceBtn, revenueBtn, staffBtn, customerBtn, ratingBtn};
        String[] texts = {"ALL", "SERVICE", "REVENUE", "STAFF", "CUSTOMER", "RATING"};
        for (int i = 0; i < buttons.length; i++) {
            if (texts[i].equals(active)) {
                buttons[i].setBackground(NAVY_BLUE);
                buttons[i].setForeground(Color.WHITE);
            } else {
                buttons[i].setBackground(Color.WHITE);
                buttons[i].setForeground(TEXT_DARK);
            }
        }
    }
    
    private void refreshContentPanels() {
        contentPanel.removeAll();
        contentPanel.add(createAllCategoriesPanel(), "ALL");
        contentPanel.add(createServiceAnalysisPanel(), "SERVICE");
        contentPanel.add(createRevenueAnalysisPanel(), "REVENUE");
        contentPanel.add(createCustomerAnalysisPanel(), "CUSTOMER");
        contentPanel.add(createStaffPerformancePanel(), "STAFF");
        contentPanel.add(createRatingAnalysisPanel(), "RATING");  
        contentCardLayout.show(contentPanel, currentCategory);
    }
    
    private JPanel createSummaryBlock() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(WARNING_YELLOW_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNING_YELLOW, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("Report Conclusion");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        
        reportSummaryArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        reportSummaryArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel.add(reportSummaryArea);
        
        return panel;
    }
    
    private JPanel createAllCategoriesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BG);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(createServiceAnalysisContent());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createRevenueAnalysisContent());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createCustomerAnalysisContent());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createStaffPerformanceContent());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createRatingAnalysisContent());
        
        return panel;
    }
    
    private JPanel createServiceAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(createServiceAnalysisContent(), BorderLayout.NORTH);
        return panel;
    }
    
    private JPanel createServiceAnalysisContent() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(PANEL_BG);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel header = createSectionHeader("Service Analysis");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(header);
        wrapper.add(Box.createVerticalStrut(10));
        
        JPanel cardsRow = new JPanel(new GridLayout(1, 2, 15, 0));
        cardsRow.setBackground(PANEL_BG);
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        cardsRow.add(createServiceDistributionCard());
        cardsRow.add(createOperationalInsightsCard());
        
        wrapper.add(cardsRow);
        return wrapper;
    }
    
    private JPanel createServiceDistributionCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());
        
        JLabel title = new JLabel("Service Type Distribution");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(NAVY_BLUE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        card.add(title, BorderLayout.NORTH);
        
        JPanel content = new JPanel(new GridLayout(1, 2, 10, 0));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
        
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawServiceDonutChart(g, getWidth(), getHeight());
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(140, 140));
        content.add(chartPanel);
        
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(Color.WHITE);
        
        // Only count completed appointments from all filtered appointments
        String today = DateUtils.getCurrentDate();
        List<Appointment> appointments = getFilteredAppointmentsByTimeRange();
        List<Appointment> pastAppointments = appointments.stream()
            .filter(a -> a.getDate() != null && a.getDate().compareTo(today) <= 0)
            .collect(Collectors.toList());
        List<Appointment> completedAppointments = getCompletedAppointments(pastAppointments);
        
        long total = completedAppointments.size();
        long normal = completedAppointments.stream().filter(a -> a.getServiceType() == ServiceType.NORMAL).count();
        long major = completedAppointments.stream().filter(a -> a.getServiceType() == ServiceType.MAJOR).count();
        
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        totalPanel.setBackground(Color.WHITE);
        totalPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel totalLabel = new JLabel("Total: ");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel totalValue = new JLabel(String.valueOf(total));
        totalValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalValue.setForeground(NAVY_BLUE);
        totalPanel.add(totalLabel);
        totalPanel.add(totalValue);
        statsPanel.add(totalPanel);
        statsPanel.add(Box.createVerticalStrut(10));
        
        JPanel normalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        normalPanel.setBackground(Color.WHITE);
        normalPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JPanel normalBox = new JPanel();
        normalBox.setBackground(NAVY_BLUE);
        normalBox.setPreferredSize(new Dimension(10, 10));
        normalPanel.add(normalBox);
        JLabel normalLabel = new JLabel("Normal: " + normal + " (" + (total > 0 ? (normal * 100 / total) : 0) + "%)");
        normalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        normalPanel.add(normalLabel);
        statsPanel.add(normalPanel);
        statsPanel.add(Box.createVerticalStrut(5));
        
        JPanel majorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        majorPanel.setBackground(Color.WHITE);
        majorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JPanel majorBox = new JPanel();
        majorBox.setBackground(WARNING_YELLOW);
        majorBox.setPreferredSize(new Dimension(10, 10));
        majorPanel.add(majorBox);
        JLabel majorLabel = new JLabel("Major: " + major + " (" + (total > 0 ? (major * 100 / total) : 0) + "%)");
        majorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        majorPanel.add(majorLabel);
        statsPanel.add(majorPanel);
        
        content.add(statsPanel);
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    private void drawServiceDonutChart(Graphics g, int w, int h) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        String today = DateUtils.getCurrentDate();
        List<Appointment> appointments = getFilteredAppointmentsByTimeRange();
        List<Appointment> pastAppointments = appointments.stream()
            .filter(a -> a.getDate() != null && a.getDate().compareTo(today) <= 0)
            .collect(Collectors.toList());
        List<Appointment> completedAppointments = getCompletedAppointments(pastAppointments);
        
        long normal = completedAppointments.stream().filter(a -> a.getServiceType() == ServiceType.NORMAL).count();
        long major = completedAppointments.stream().filter(a -> a.getServiceType() == ServiceType.MAJOR).count();
        long total = normal + major;
        
        int size = Math.min(w, h) - 20;
        int x = (w - size) / 2;
        int y = (h - size) / 2;
        
        if (total == 0) {
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillOval(x, y, size, size);
            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            String noDataMsg = "No completed services";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(noDataMsg, (w - fm.stringWidth(noDataMsg)) / 2, h / 2);
            return;
        }
        
        int angleNormal = (int) (360.0 * normal / total);
        
        g2.setColor(NAVY_BLUE);
        g2.fillArc(x, y, size, size, 90, angleNormal);
        
        g2.setColor(WARNING_YELLOW);
        g2.fillArc(x, y, size, size, 90 + angleNormal, 360 - angleNormal);
        
        g2.setColor(Color.WHITE);
        int holeSize = size / 2;
        g2.fillOval(x + (size - holeSize) / 2, y + (size - holeSize) / 2, holeSize, holeSize);
    }
    
    private JPanel createOperationalInsightsCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());
        
        JLabel title = new JLabel("Operational Insights");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(NAVY_BLUE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        card.add(title, BorderLayout.NORTH);
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        
        List<Appointment> appointments = getFilteredAppointmentsByTimeRange();
        
        // Get today's date for comparison
        String today = DateUtils.getCurrentDate();
        
        // Only count appointments that are in the past 
        List<Appointment> pastAppointments = appointments.stream()
            .filter(a -> a.getDate() != null && a.getDate().compareTo(today) <= 0)
            .collect(Collectors.toList());
        
        long totalEligible = pastAppointments.size();
        long completed = pastAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
            .count();
        
        String completionRate = totalEligible > 0 ? (completed * 100 / totalEligible) + "%" : "0%";
        
        JPanel completionPanel = createMetricPanel("Completion Rate", completionRate, GREEN);
        completionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        completionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        content.add(completionPanel);
        content.add(Box.createVerticalStrut(10));
        
        // Calculate peak service hours from completed appointments from past appointments only
        List<Appointment> completedPastAppointments = pastAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
            .collect(Collectors.toList());
        String peakHours = calculatePeakHours(completedPastAppointments);
        JPanel peakPanel = createMetricPanel("Peak Service Hours", peakHours, GREEN);
        peakPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        peakPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        content.add(peakPanel);
        
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    private String calculatePeakHours(List<Appointment> appointments) {
        if (appointments.isEmpty()) {
            return "No data available";
        }
        
        // Count appointments per hour 
        int[] hourCounts = new int[24];
        // Track service type for each hour
        Map<Integer, Integer> majorCountPerHour = new HashMap<>();
        Map<Integer, Integer> normalCountPerHour = new HashMap<>();
        
        for (Appointment a : appointments) {
            try {
                String timeStr = a.getStartTime();
                if (timeStr != null && !timeStr.isEmpty()) {
                    String[] parts = timeStr.split(":");
                    int hour = Integer.parseInt(parts[0]);
                    if (hour >= 0 && hour < 24) {
                        hourCounts[hour]++;
                        if (a.getServiceType() == ServiceType.MAJOR) {
                            majorCountPerHour.put(hour, majorCountPerHour.getOrDefault(hour, 0) + 1);
                        } else {
                            normalCountPerHour.put(hour, normalCountPerHour.getOrDefault(hour, 0) + 1);
                        }
                    }
                }
            } catch (Exception e) {
                // Skip invalid time formats
            }
        }
        
        // Find the hour with maximum frequency
        int maxCount = 0;
        int peakHourStart = 0;
        for (int i = 0; i < 24; i++) {
            if (hourCounts[i] > maxCount) {
                maxCount = hourCounts[i];
                peakHourStart = i;
            }
        }
        
        if (maxCount == 0) {
            return "No time data available";
        }
        
        // Determine the dominant service type at the peak hour
        int majorCount = majorCountPerHour.getOrDefault(peakHourStart, 0);
        int normalCount = normalCountPerHour.getOrDefault(peakHourStart, 0);
        
        // Peak duration is based on the dominant service type at that hour
        int peakDuration = (majorCount > normalCount) ? 3 : 1;
        int peakHourEnd = peakHourStart + peakDuration - 1;
        
        // Ensure not going beyond 23 (11 PM)
        if (peakHourEnd >= 24) {
            peakHourEnd = 23;
        }
        
        String startTime = formatHour(peakHourStart);
        String endTime = formatHour(peakHourEnd + 1);
        
        return startTime + " - " + endTime;
    }
    
    private String formatHour(int hour) {
        if (hour == 0) return "12:00 AM";
        if (hour < 12) return hour + ":00 AM";
        if (hour == 12) return "12:00 PM";
        return (hour - 12) + ":00 PM";
    }
    
    private JPanel createRevenueAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(createRevenueAnalysisContent(), BorderLayout.NORTH);
        return panel;
    }
    
    private JPanel createRevenueAnalysisContent() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(PANEL_BG);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel header = createSectionHeader("Revenue Analysis");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        wrapper.add(header);
        wrapper.add(Box.createVerticalStrut(10));
        
        JPanel card = createCard();
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setLayout(new BorderLayout());
        
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 15, 0));
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        
        List<Appointment> appointments = getFilteredAppointmentsByTimeRange();
        List<Payment> payments = paymentDAO.readAll();
        Map<String, Payment> paymentMap = payments.stream()
            .collect(Collectors.toMap(Payment::getAppointmentId, p -> p, (p1, p2) -> p1));
        
        // Revenue from completed paid appointments only (from past appointments)
        String today = DateUtils.getCurrentDate();
        List<Appointment> pastAppointments = appointments.stream()
            .filter(a -> a.getDate() != null && a.getDate().compareTo(today) <= 0)
            .collect(Collectors.toList());
        
        double totalRevenue = pastAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED && paymentMap.containsKey(a.getId()))
            .mapToDouble(a -> paymentMap.get(a.getId()).getAmount())
            .sum();
        
        DecimalFormat df = new DecimalFormat("#,###.00");
        
        JLabel incomeValue = new JLabel("RM " + df.format(totalRevenue));
        incomeValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        incomeValue.setForeground(NAVY_BLUE);
        incomeValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(incomeValue);
        
        JLabel incomeLabel = new JLabel("Total Revenue");
        incomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        incomeLabel.setForeground(TEXT_MUTED);
        incomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(incomeLabel);
        leftPanel.add(Box.createVerticalStrut(15));
        
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawRevenueBarChart(g, getWidth(), getHeight());
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(220, 140));
        chartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(chartPanel);
        
        mainContent.add(leftPanel);
        
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(245, 248, 250));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        double normalRevenue = pastAppointments.stream()
            .filter(a -> a.getServiceType() == ServiceType.NORMAL && a.getStatus() == AppointmentStatus.COMPLETED && paymentMap.containsKey(a.getId()))
            .mapToDouble(a -> paymentMap.get(a.getId()).getAmount())
            .sum();
        double majorRevenue = pastAppointments.stream()
            .filter(a -> a.getServiceType() == ServiceType.MAJOR && a.getStatus() == AppointmentStatus.COMPLETED && paymentMap.containsKey(a.getId()))
            .mapToDouble(a -> paymentMap.get(a.getId()).getAmount())
            .sum();
        
        JLabel breakdownTitle = new JLabel("REVENUE BREAKDOWN");
        breakdownTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        breakdownTitle.setForeground(NAVY_BLUE);
        breakdownTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(breakdownTitle);
        rightPanel.add(Box.createVerticalStrut(15));
        
        JPanel normalRevenuePanel = new JPanel();
        normalRevenuePanel.setLayout(new BoxLayout(normalRevenuePanel, BoxLayout.Y_AXIS));
        normalRevenuePanel.setBackground(Color.WHITE);
        normalRevenuePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        normalRevenuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        normalRevenuePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        JLabel normalTitle = new JLabel("Normal Service");
        normalTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        normalTitle.setForeground(NAVY_BLUE);
        normalTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        normalRevenuePanel.add(normalTitle);
        normalRevenuePanel.add(Box.createVerticalStrut(5));
        
        JLabel normalAmount = new JLabel("RM " + df.format(normalRevenue));
        normalAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));
        normalAmount.setForeground(NAVY_BLUE);
        normalAmount.setAlignmentX(Component.LEFT_ALIGNMENT);
        normalRevenuePanel.add(normalAmount);
        
        rightPanel.add(normalRevenuePanel);
        rightPanel.add(Box.createVerticalStrut(12));
        
        JPanel majorRevenuePanel = new JPanel();
        majorRevenuePanel.setLayout(new BoxLayout(majorRevenuePanel, BoxLayout.Y_AXIS));
        majorRevenuePanel.setBackground(Color.WHITE);
        majorRevenuePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        majorRevenuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        majorRevenuePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        JLabel majorTitle = new JLabel("Major Service");
        majorTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        majorTitle.setForeground(new Color(180, 130, 0));
        majorTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        majorRevenuePanel.add(majorTitle);
        majorRevenuePanel.add(Box.createVerticalStrut(5));
        
        JLabel majorAmount = new JLabel("RM " + df.format(majorRevenue));
        majorAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));
        majorAmount.setForeground(new Color(180, 130, 0));
        majorAmount.setAlignmentX(Component.LEFT_ALIGNMENT);
        majorRevenuePanel.add(majorAmount);
        
        rightPanel.add(majorRevenuePanel);
        
        mainContent.add(rightPanel);
        
        card.add(mainContent, BorderLayout.CENTER);
        wrapper.add(card);
        
        return wrapper;
    }
    
    private void drawRevenueBarChart(Graphics g, int w, int h) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        List<Appointment> appointments = getFilteredAppointmentsByTimeRange();
        List<Payment> payments = paymentDAO.readAll();
        Map<String, Payment> paymentMap = payments.stream()
            .collect(Collectors.toMap(Payment::getAppointmentId, p -> p, (p1, p2) -> p1));
        
        String today = DateUtils.getCurrentDate();
        List<Appointment> pastAppointments = appointments.stream()
            .filter(a -> a.getDate() != null && a.getDate().compareTo(today) <= 0)
            .collect(Collectors.toList());
        
        double normalRevenue = pastAppointments.stream()
            .filter(a -> a.getServiceType() == ServiceType.NORMAL && a.getStatus() == AppointmentStatus.COMPLETED && paymentMap.containsKey(a.getId()))
            .mapToDouble(a -> paymentMap.get(a.getId()).getAmount())
            .sum();
        double majorRevenue = pastAppointments.stream()
            .filter(a -> a.getServiceType() == ServiceType.MAJOR && a.getStatus() == AppointmentStatus.COMPLETED && paymentMap.containsKey(a.getId()))
            .mapToDouble(a -> paymentMap.get(a.getId()).getAmount())
            .sum();
        
        double maxRevenue = Math.max(normalRevenue, majorRevenue);
        if (maxRevenue == 0) maxRevenue = 1;
        
        int margin = 40;
        int chartH = h - 2 * margin;
        int barWidth = 60;
        int spacing = 40;
        int startX = (w - (barWidth * 2 + spacing)) / 2;
        
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(margin, h - margin, w - margin, h - margin);
        g2.drawLine(margin, margin, margin, h - margin);
        
        int normalHeight = (int) (normalRevenue * chartH / maxRevenue);
        int normalY = h - margin - normalHeight;
        g2.setColor(NAVY_BLUE);
        g2.fillRect(startX, normalY, barWidth, normalHeight);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g2.drawString("Normal", startX + 15, h - margin + 15);
        
        int majorHeight = (int) (majorRevenue * chartH / maxRevenue);
        int majorY = h - margin - majorHeight;
        g2.setColor(WARNING_YELLOW);
        g2.fillRect(startX + barWidth + spacing, majorY, barWidth, majorHeight);
        g2.drawString("Major", startX + barWidth + spacing + 20, h - margin + 15);
    }
    
    private JPanel createCustomerAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(createCustomerAnalysisContent(), BorderLayout.NORTH);
        return panel;
    }
    
    private JPanel createCustomerAnalysisContent() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(PANEL_BG);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel header = createSectionHeader("Customer Analysis");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(header);
        wrapper.add(Box.createVerticalStrut(10));
        
        JPanel cardsRow = new JPanel(new GridLayout(1, 2, 15, 0));
        cardsRow.setBackground(PANEL_BG);
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        cardsRow.add(createRetentionCard());
        cardsRow.add(createLoyaltyCard());
        
        wrapper.add(cardsRow);
        return wrapper;
    }
    
    private JPanel createRetentionCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());
        
        JLabel title = new JLabel("Customer Retention");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(NAVY_BLUE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        card.add(title, BorderLayout.NORTH);
        
        JPanel content = new JPanel(new GridLayout(1, 2, 10, 0));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
        
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCustomerPieChart(g, getWidth(), getHeight());
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(120, 120));
        content.add(chartPanel);
        
        // Use past appointments only for customer analysis
        String today = DateUtils.getCurrentDate();
        List<Appointment> appointments = getFilteredAppointmentsByTimeRange();
        List<Appointment> pastAppointments = appointments.stream()
            .filter(a -> a.getDate() != null && a.getDate().compareTo(today) <= 0)
            .collect(Collectors.toList());
        
        // Only consider customers who have completed appointments from past
        List<Appointment> completedAppointments = getCompletedAppointments(pastAppointments);
        long activeCustomers = completedAppointments.stream()
            .map(Appointment::getCustomerId)
            .distinct()
            .count();
        
        long returning = completedAppointments.stream()
            .collect(Collectors.groupingBy(Appointment::getCustomerId, Collectors.counting()))
            .entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .count();
        
        long newCust = activeCustomers - returning;
        
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(Color.WHITE);

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        totalPanel.setBackground(Color.WHITE);
        totalPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel totalLabel = new JLabel("Total: ");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel totalValue = new JLabel(String.valueOf(activeCustomers));
        totalValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalValue.setForeground(NAVY_BLUE);

        totalPanel.add(totalLabel);
        totalPanel.add(totalValue);

        statsPanel.add(totalPanel);
        statsPanel.add(Box.createVerticalStrut(10));

        JPanel newPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        newPanel.setBackground(Color.WHITE);
        newPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JPanel newBox = new JPanel();
        newBox.setBackground(GREEN);
        newBox.setPreferredSize(new Dimension(10, 10));
        newPanel.add(newBox);

        JLabel newLabel = new JLabel("New: " + newCust);
        newLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        newPanel.add(newLabel);

        statsPanel.add(newPanel);
        statsPanel.add(Box.createVerticalStrut(5));

        JPanel retPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        retPanel.setBackground(Color.WHITE);
        retPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JPanel retBox = new JPanel();
        retBox.setBackground(new Color(59, 130, 246));
        retBox.setPreferredSize(new Dimension(10, 10));
        retPanel.add(retBox);

        JLabel retLabel = new JLabel("Returning: " + returning);
        retLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        retPanel.add(retLabel);

        statsPanel.add(retPanel);
        
        content.add(statsPanel);
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    private void drawCustomerPieChart(Graphics g, int w, int h) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        String today = DateUtils.getCurrentDate();
        List<Appointment> appointments = getFilteredAppointmentsByTimeRange();
        List<Appointment> pastAppointments = appointments.stream()
            .filter(a -> a.getDate() != null && a.getDate().compareTo(today) <= 0)
            .collect(Collectors.toList());
        List<Appointment> completedAppointments = getCompletedAppointments(pastAppointments);
        
        // Only consider customers who have completed appointments
        Map<String, Long> customerVisits = completedAppointments.stream()
            .collect(Collectors.groupingBy(Appointment::getCustomerId, Collectors.counting()));
        
        long returning = customerVisits.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .count();
        long newCust = customerVisits.size() - returning;
        long total = customerVisits.size();
        
        int size = Math.min(w, h) - 5;
        int x = (w - size) / 2;
        int y = (h - size) / 2;
        
        if (total == 0) {
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillOval(x, y, size, size);
            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            String noDataMsg = "No customer data";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(noDataMsg, (w - fm.stringWidth(noDataMsg)) / 2, h / 2);
            return;
        }
        
        int angleNew = (int) (360.0 * newCust / total);
        
        g2.setColor(GREEN);
        g2.fillArc(x, y, size, size, 90, angleNew);
        
        g2.setColor(new Color(59, 130, 246));
        g2.fillArc(x, y, size, size, 90 + angleNew, 360 - angleNew);
    }
    
    private JPanel createLoyaltyCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());
        
        JLabel title = new JLabel("Top Customers");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(NAVY_BLUE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        card.add(title, BorderLayout.NORTH);
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        
        // Use past appointments only
        String today = DateUtils.getCurrentDate();
        List<Appointment> appointments = getFilteredAppointmentsByTimeRange();
        List<Appointment> pastAppointments = appointments.stream()
            .filter(a -> a.getDate() != null && a.getDate().compareTo(today) <= 0)
            .collect(Collectors.toList());
        List<Appointment> completedAppointments = getCompletedAppointments(pastAppointments);
        
        if (completedAppointments.isEmpty()) {
            JLabel emptyLabel = new JLabel("No appointment data available for selected period");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            emptyLabel.setForeground(TEXT_MUTED);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(emptyLabel);
            card.add(content, BorderLayout.CENTER);
            return card;
        }
        
        // Count visits per customer based on completed appointments only from past
        Map<String, Long> customerVisitCount = completedAppointments.stream()
            .collect(Collectors.groupingBy(Appointment::getCustomerId, Collectors.counting()));
        
        // Sort by visit count descending
        List<Map.Entry<String, Long>> sortedCustomers = new ArrayList<>(customerVisitCount.entrySet());
        sortedCustomers.sort((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));
        
        int count = 0;
        for (Map.Entry<String, Long> entry : sortedCustomers) {
            if (count >= 5) break;
            
            String customerId = entry.getKey();
            long visits = entry.getValue();
            
            Customer customer = userDAO.findCustomerById(customerId);
            if (customer != null) {
                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(Color.WHITE);
                row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
                row.setPreferredSize(new Dimension(0, 45));
                row.setMinimumSize(new Dimension(0, 45));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
                
                JLabel nameLabel = new JLabel((count + 1) + ". " + customer.getFullName());
                nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                row.add(nameLabel, BorderLayout.WEST);
                
                JLabel visitsLabel = new JLabel(visits + " visits");
                visitsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                visitsLabel.setForeground(NAVY_BLUE);
                row.add(visitsLabel, BorderLayout.EAST);
                
                content.add(row);
                count++;
            }
        }
        
        if (count == 0) {
            JLabel emptyLabel = new JLabel("No customer data available for selected period");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            emptyLabel.setForeground(TEXT_MUTED);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(emptyLabel);
        }
        
        card.add(content, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createStaffPerformancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(createStaffPerformanceContent(), BorderLayout.NORTH);
        return panel;
    }
    
    private JPanel createStaffPerformanceContent() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(PANEL_BG);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel header = createSectionHeader("Staff Performance");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(header);
        wrapper.add(Box.createVerticalStrut(10));
        
        JPanel cardsRow = new JPanel(new GridLayout(1, 2, 15, 0));
        cardsRow.setBackground(PANEL_BG);
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        cardsRow.add(createTechnicianPerformanceCard());
        cardsRow.add(createCounterStaffPerformanceCard());
        
        wrapper.add(cardsRow);
        
        return wrapper;
    }
    
    private JPanel createTechnicianPerformanceCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());
        
        JLabel title = new JLabel("Top Technicians");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(NAVY_BLUE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        card.add(title, BorderLayout.NORTH);
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        
        List<Technician> techs = userDAO.readTechnicians();
        
        // Use past appointments only
        String today = DateUtils.getCurrentDate();
        List<Appointment> appointments = getFilteredAppointmentsByTimeRange();
        List<Appointment> pastAppointments = appointments.stream()
            .filter(a -> a.getDate() != null && a.getDate().compareTo(today) <= 0)
            .collect(Collectors.toList());
        List<Appointment> completedAppointments = getCompletedAppointments(pastAppointments);
        
        // Count completed jobs per technician
        Map<String, Long> technicianCompletedJobs = new HashMap<>();
        for (Technician t : techs) {
            long completed = completedAppointments.stream()
                .filter(a -> t.getId().equals(a.getTechnicianId()))
                .count();
            if (completed > 0) {
                technicianCompletedJobs.put(t.getFullName(), completed);
            }
        }
        
        // Sort by completed jobs count descending
        List<Map.Entry<String, Long>> sortedTechs = new ArrayList<>(technicianCompletedJobs.entrySet());
        sortedTechs.sort((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));
        
        int count = 0;
        for (Map.Entry<String, Long> entry : sortedTechs) {
            if (count >= 5) break;
            
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
            row.setPreferredSize(new Dimension(0, 45));
            
            JLabel nameLabel = new JLabel((count + 1) + ". " + entry.getKey());
            nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            row.add(nameLabel, BorderLayout.WEST);
            
            JLabel jobsLabel = new JLabel(entry.getValue() + " jobs");
            jobsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            jobsLabel.setForeground(NAVY_BLUE);
            row.add(jobsLabel, BorderLayout.EAST);
            
            content.add(row);
            count++;
        }
        
        if (count == 0) {
            JLabel emptyLabel = new JLabel("No technician data available");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            emptyLabel.setForeground(TEXT_MUTED);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(emptyLabel);
        }
        
        card.add(content, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createCounterStaffPerformanceCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());
        
        JLabel title = new JLabel("Counter Staff Activity");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(NAVY_BLUE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        card.add(title, BorderLayout.NORTH);
        
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        
        String today = DateUtils.getCurrentDate();
        List<Appointment> allAppointments = getFilteredAppointmentsByTimeRange();
        List<Appointment> pastAppointments = allAppointments.stream()
            .filter(a -> a.getDate() != null && a.getDate().compareTo(today) <= 0)
            .collect(Collectors.toList());
        
        if (pastAppointments.isEmpty()) {
            JLabel emptyLabel = new JLabel("No appointment data available");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            emptyLabel.setForeground(TEXT_MUTED);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(emptyLabel);
            card.add(content, BorderLayout.CENTER);
            return card;
        }
        
        // Count all appointments per counter staff (including ASSIGNED and COMPLETED)
        Map<String, Long> staffAppointmentCount = new HashMap<>();
        for (CounterStaff cs : userDAO.readCounterStaff()) {
            long handledAppointments = pastAppointments.stream()
                .filter(a -> cs.getId().equals(a.getCounterStaffId()))
                .count();
            if (handledAppointments > 0) {
                staffAppointmentCount.put(cs.getFullName(), handledAppointments);
            }
        }
        
        // Sort by count descending
        List<Map.Entry<String, Long>> sortedStaff = new ArrayList<>(staffAppointmentCount.entrySet());
        sortedStaff.sort((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));
        
        boolean hasData = false;
        for (Map.Entry<String, Long> entry : sortedStaff) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
            row.setPreferredSize(new Dimension(0, 45));
            
            JLabel nameLabel = new JLabel(entry.getKey());
            nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            row.add(nameLabel, BorderLayout.WEST);
            
            JLabel countLabel = new JLabel(entry.getValue() + " appointments");
            countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            countLabel.setForeground(NAVY_BLUE);
            row.add(countLabel, BorderLayout.EAST);
            
            listPanel.add(row);
            hasData = true;
        }
        content.add(listPanel, BorderLayout.NORTH);
        
        if (!hasData) {
            JLabel emptyLabel = new JLabel("No counter staff activity for selected period");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            emptyLabel.setForeground(TEXT_MUTED);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(emptyLabel);
        }
        
        card.add(content, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createRatingAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(createRatingAnalysisContent(), BorderLayout.NORTH);
        return panel;
    }

    private JPanel createRatingAnalysisContent() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(PANEL_BG);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel header = createSectionHeader("Rating Analysis");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        wrapper.add(header);
        wrapper.add(Box.createVerticalStrut(10));
        
        JPanel card = createCard();
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setLayout(new BorderLayout());
        
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 15, 0));
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        
        JLabel chartTitle = new JLabel("Rating Distribution (1-5 Stars)");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chartTitle.setForeground(NAVY_BLUE);
        chartTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        leftPanel.add(chartTitle, BorderLayout.NORTH);
        
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawRatingBarChart(g, getWidth(), getHeight());
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(220, 180));
        leftPanel.add(chartPanel, BorderLayout.CENTER);
        
        mainContent.add(leftPanel);
        
        JPanel rightPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        rightPanel.setBackground(Color.WHITE);
        
        List<Appointment> filteredAppointments = getFilteredAppointmentsByTimeRange();
        
        // Get appointment IDs for the filtered time range
        List<String> filteredAppointmentIds = filteredAppointments.stream()
            .map(Appointment::getId)
            .collect(Collectors.toList());
        
        // Get all comments for appointments in the filtered time range (including counter staff comments)
        List<Comment> allComments = commentDAO.readAll();
        List<Comment> filteredComments = allComments.stream()
            .filter(c -> filteredAppointmentIds.contains(c.getAppointmentId()) && c.getRating() > 0)
            .collect(Collectors.toList());
        
        double avgRating = 0;
        int totalRatings = filteredComments.size();
        
        if (totalRatings > 0) {
            avgRating = filteredComments.stream().mapToInt(Comment::getRating).average().orElse(0);
        }
        
        JPanel avgCard = new JPanel(new GridBagLayout());
        avgCard.setBackground(new Color(240, 248, 255));
        avgCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237, 100), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        GridBagConstraints innerGbc = new GridBagConstraints();
        innerGbc.gridx = 0;
        innerGbc.gridy = 0;
        innerGbc.anchor = GridBagConstraints.CENTER;
        
        JPanel avgTextPanel = new JPanel();
        avgTextPanel.setLayout(new BoxLayout(avgTextPanel, BoxLayout.Y_AXIS));
        avgTextPanel.setBackground(new Color(240, 248, 255));
        
        String avgDisplay = totalRatings > 0 ? String.format("%.1f", avgRating) : "N/A";
        
        JLabel avgValue = new JLabel(avgDisplay);
        avgValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        avgValue.setForeground(totalRatings == 0 ? TEXT_MUTED : NAVY_BLUE);
        avgValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        avgTextPanel.add(avgValue);
        
        JLabel avgLabel = new JLabel("Average Rating");
        avgLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        avgLabel.setForeground(new Color(80, 100, 120));
        avgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avgTextPanel.add(avgLabel);
        
        avgCard.add(avgTextPanel, innerGbc);
        
        rightPanel.add(avgCard);
        
        JPanel totalCard = new JPanel(new GridBagLayout());
        totalCard.setBackground(new Color(240, 255, 240));
        totalCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(34, 197, 94, 100), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        JPanel totalTextPanel = new JPanel();
        totalTextPanel.setLayout(new BoxLayout(totalTextPanel, BoxLayout.Y_AXIS));
        totalTextPanel.setBackground(new Color(240, 255, 240));
        
        String totalDisplay = String.valueOf(totalRatings);
        
        JLabel totalValue = new JLabel(totalDisplay);
        totalValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        totalValue.setForeground(totalRatings == 0 ? TEXT_MUTED : GREEN);
        totalValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        totalTextPanel.add(totalValue);
        
        JLabel totalLabel = new JLabel("Total Reviews");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        totalLabel.setForeground(new Color(60, 100, 60));
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        totalTextPanel.add(totalLabel);
        
        totalCard.add(totalTextPanel, innerGbc);
        
        rightPanel.add(totalCard);
        
        mainContent.add(rightPanel);
        card.add(mainContent, BorderLayout.CENTER);
        wrapper.add(card);
        
        return wrapper;
    }
    
    private void drawRatingBarChart(Graphics g, int w, int h) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        List<Appointment> filteredAppointments = getFilteredAppointmentsByTimeRange();
        
        // Get appointment IDs for the filtered time range
        List<String> filteredAppointmentIds = filteredAppointments.stream()
            .map(Appointment::getId)
            .collect(Collectors.toList());
        
        // Get all comments for appointments in the filtered time range
        List<Comment> allComments = commentDAO.readAll();
        List<Comment> filteredComments = allComments.stream()
            .filter(c -> filteredAppointmentIds.contains(c.getAppointmentId()) && c.getRating() > 0)
            .collect(Collectors.toList());
        
        int[] ratings = new int[6];
        for (Comment c : filteredComments) {
            int r = c.getRating();
            if (r >= 1 && r <= 5) ratings[r]++;
        }
        
        int maxCount = 1;
        for (int i = 1; i <= 5; i++) {
            if (ratings[i] > maxCount) maxCount = ratings[i];
        }
        
        if (filteredComments.isEmpty()) {
            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            String noDataMsg = "No review data for selected period";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(noDataMsg, (w - fm.stringWidth(noDataMsg)) / 2, h / 2);
            return;
        }
        
        int margin = 50;
        int chartH = h - 2 * margin - 20;
        int barWidth = 45;
        int startX = margin + 15;
        
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(margin, h - margin - 10, w - margin, h - margin - 10);
        g2.drawLine(margin, margin, margin, h - margin - 10);
        
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        for (int i = 0; i <= 4; i++) {
            int val = i * maxCount / 4;
            int y = h - margin - 10 - (val * chartH / maxCount);
            g2.drawString(String.valueOf(val), margin - 25, y + 3);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(margin + 2, y, w - margin, y);
            g2.setColor(Color.BLACK);
        }
        
        for (int i = 1; i <= 5; i++) {
            int barHeight = ratings[i] * chartH / maxCount;
            int x = startX + (i - 1) * (barWidth + 20);
            int y = h - margin - 10 - barHeight;
            
            g2.setColor(WARNING_YELLOW);
            g2.fillRect(x, y, barWidth, barHeight);
            g2.setColor(NAVY_BLUE);
            g2.drawRect(x, y, barWidth, barHeight);
            
            if (ratings[i] > 0) {
                g2.setColor(TEXT_DARK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String countStr = String.valueOf(ratings[i]);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(countStr, x + (barWidth - fm.stringWidth(countStr)) / 2, y - 3);
            }
            
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String label = String.valueOf(i);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, x + (barWidth - fm.stringWidth(label)) / 2, h - margin);
        }
    }
    
    private JPanel createCard() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220), 1),
            BorderFactory.createLineBorder(CARD_BORDER, 1)
        ));
        return panel;
    }
    
    private JPanel createSectionHeader(String text) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL_BG);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(NAVY_BLUE);
        header.add(label, BorderLayout.WEST);
        
        JSeparator sep = new JSeparator();
        sep.setForeground(CARD_BORDER);
        header.add(sep, BorderLayout.SOUTH);
        
        return header;
    }
    
    private JPanel createMetricPanel(String label, String value, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(valueLabel);
        
        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        labelText.setForeground(TEXT_MUTED);
        labelText.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(labelText);
        
        return panel;
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
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setPreferredSize(new Dimension(120, 34));
        
        Color originalColor = color;
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (originalColor != RED) {
                    button.setBackground(originalColor.darker());
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }
    
    private void generateReportSummary(String category) {
        List<Appointment> allFilteredAppointments = getFilteredAppointmentsByTimeRange();
        List<Technician> technicians = userDAO.readTechnicians();
        List<CounterStaff> counterStaff = userDAO.readCounterStaff();
        List<Payment> payments = paymentDAO.readAll();
        Map<String, Payment> paymentMap = payments.stream()
            .collect(Collectors.toMap(Payment::getAppointmentId, p -> p, (p1, p2) -> p1));
        
        // Get today's date for comparison
        String today = DateUtils.getCurrentDate();
        
        // Only consider past appointments for all calculations
        List<Appointment> pastAppointments = allFilteredAppointments.stream()
            .filter(a -> a.getDate() != null && a.getDate().compareTo(today) <= 0)
            .collect(Collectors.toList());
        
        // Completed appointments from past appointments only
        List<Appointment> completedAppointments = pastAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
            .collect(Collectors.toList());
        
        // Get all comments for the filtered time range (use past appointment IDs for ratings)
        Set<String> pastAppointmentIds = pastAppointments.stream()
            .map(Appointment::getId)
            .collect(Collectors.toSet());
        
        // Get comments for past appointments only
        List<Comment> allComments = commentDAO.readAll();
        List<Comment> filteredComments = allComments.stream()
            .filter(c -> pastAppointmentIds.contains(c.getAppointmentId()))
            .collect(Collectors.toList());
        
        // Get rated comments from filtered results only 
        List<Comment> filteredRatedComments = filteredComments.stream()
            .filter(c -> c.getRating() >= 1 && c.getRating() <= 5)
            .collect(Collectors.toList());
        
        StringBuilder summary = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#,###.00");
        
        long completedCount = completedAppointments.size();
        long normal = completedAppointments.stream().filter(a -> a.getServiceType() == ServiceType.NORMAL).count();
        long major = completedAppointments.stream().filter(a -> a.getServiceType() == ServiceType.MAJOR).count();
        long cancelledCount = pastAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
        long pendingAssignedCount = pastAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.PENDING || a.getStatus() == AppointmentStatus.ASSIGNED)
            .count();
        
        // Revenue from completed appointments only (from past appointments)
        double revenue = completedAppointments.stream()
            .filter(a -> paymentMap.containsKey(a.getId()))
            .mapToDouble(a -> paymentMap.get(a.getId()).getAmount())
            .sum();
        
        // Find top technician(s) based on completed jobs from past appointments only
        long maxTechnicianJobs = 0;
        List<String> topTechnicians = new ArrayList<>();
        Map<String, Long> technicianJobCount = new HashMap<>();
        for (Technician t : technicians) {
            long completedJobs = completedAppointments.stream()
                .filter(a -> t.getId().equals(a.getTechnicianId()))
                .count();
            if (completedJobs > 0) {
                technicianJobCount.put(t.getFullName(), completedJobs);
                if (completedJobs > maxTechnicianJobs) {
                    maxTechnicianJobs = completedJobs;
                }
            }
        }
        for (Map.Entry<String, Long> entry : technicianJobCount.entrySet()) {
            if (entry.getValue() == maxTechnicianJobs && maxTechnicianJobs > 0) {
                topTechnicians.add(entry.getKey());
            }
        }
        
        // Find top customer(s) based on completed appointments from past appointments only
        Map<String, Long> customerVisits = completedAppointments.stream()
            .collect(Collectors.groupingBy(Appointment::getCustomerId, Collectors.counting()));
        
        long maxCustomerVisits = 0;
        List<String> topCustomers = new ArrayList<>();
        Map<String, Long> customerVisitCount = new HashMap<>();
        for (Map.Entry<String, Long> entry : customerVisits.entrySet()) {
            Customer c = userDAO.findCustomerById(entry.getKey());
            if (c != null) {
                String name = c.getFullName();
                customerVisitCount.put(name, entry.getValue());
                if (entry.getValue() > maxCustomerVisits) {
                    maxCustomerVisits = entry.getValue();
                }
            }
        }
        for (Map.Entry<String, Long> entry : customerVisitCount.entrySet()) {
            if (entry.getValue() == maxCustomerVisits && maxCustomerVisits > 0) {
                topCustomers.add(entry.getKey());
            }
        }
        
        // Find top counter staff(s) based on past appointments only
        long maxStaffAppointments = 0;
        List<String> topCounterStaff = new ArrayList<>();
        Map<String, Long> staffAppointmentCount = new HashMap<>();
        for (CounterStaff cs : counterStaff) {
            long handledAppointments = pastAppointments.stream()
                .filter(a -> cs.getId().equals(a.getCounterStaffId()))
                .count();
            if (handledAppointments > 0) {
                staffAppointmentCount.put(cs.getFullName(), handledAppointments);
                if (handledAppointments > maxStaffAppointments) {
                    maxStaffAppointments = handledAppointments;
                }
            }
        }
        for (Map.Entry<String, Long> entry : staffAppointmentCount.entrySet()) {
            if (entry.getValue() == maxStaffAppointments && maxStaffAppointments > 0) {
                topCounterStaff.add(entry.getKey());
            }
        }
        
        // Customer analysis based only on completed appointments (from past)
        long activeCustomers = customerVisits.size();
        long returning = customerVisits.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .count();
        
        // Rating analysis based on comments from past appointments only
        double avgRating = filteredRatedComments.isEmpty() ? 0 : 
            filteredRatedComments.stream().mapToInt(Comment::getRating).average().orElse(0);
        int ratingCount = filteredRatedComments.size();
        
        // Calculate peak hours from completed appointments only (from past)
        String peakHours = calculatePeakHours(completedAppointments);
        
        if (category.equals("ALL") || category.equals("SERVICE")) {
            if (completedCount > 0) {
                summary.append("We have handled ").append(completedCount).append(" completed services, consisting of ")
                    .append(normal).append(" Normal and ").append(major).append(" Major repairs. ");
                if (pendingAssignedCount > 0) {
                    summary.append(pendingAssignedCount).append(" service(s) are still in progress. ");
                }
                if (cancelledCount > 0) {
                    summary.append(cancelledCount).append(" appointment(s) were cancelled. ");
                }
                if (!"No data available".equals(peakHours) && !"No time data available".equals(peakHours)) {
                    summary.append("Activity was highest around ").append(peakHours).append(". ");
                }
            } else {
                summary.append("There were no completed services in the selected period. ");
                if (pendingAssignedCount > 0) {
                    summary.append(pendingAssignedCount).append(" service(s) are in progress. ");
                }
                if (cancelledCount > 0) {
                    summary.append(cancelledCount).append(" appointment(s) were cancelled. ");
                }
            }
        }
        if (category.equals("ALL") || category.equals("REVENUE")) {
            if (completedCount > 0 && revenue > 0) {
                summary.append("Total revenue reached RM ").append(df.format(revenue)).append(" for the selected period. ");
            } else {
                summary.append("No revenue was recorded from completed services in the selected period. ");
            }
        }
        if (category.equals("ALL") || category.equals("CUSTOMER")) {
            if (activeCustomers > 0) {
                summary.append("We served ").append(activeCustomers).append(" customers, with ")
                    .append(activeCustomers - returning).append(" new and ").append(returning).append(" returning. ");
                if (topCustomers.size() == 1) {
                    summary.append(topCustomers.get(0)).append(" was the most frequent with ").append(maxCustomerVisits).append(" visit(s). ");
                } else if (topCustomers.size() > 1) {
                    summary.append(String.join(", ", topCustomers)).append(" were the most frequent with ").append(maxCustomerVisits).append(" visits each. ");
                }
            } else {
                summary.append("There were no customer interactions in the selected period. ");
            }
        }
        if (category.equals("ALL") || category.equals("STAFF")) {
            if (completedCount > 0) {
                if (topTechnicians.size() == 1) {
                    summary.append(topTechnicians.get(0)).append(" completed the most jobs (").append(maxTechnicianJobs).append("). ");
                } else if (topTechnicians.size() > 1) {
                    summary.append(String.join(", ", topTechnicians)).append(" each completed ").append(maxTechnicianJobs).append(" jobs. ");
                }
                
                if (topCounterStaff.size() == 1) {
                    summary.append(topCounterStaff.get(0)).append(" handled the most appointments (").append(maxStaffAppointments).append("). ");
                } else if (topCounterStaff.size() > 1) {
                    summary.append(String.join(", ", topCounterStaff)).append(" each handled ").append(maxStaffAppointments).append(" appointments. ");
                }
                
                if (topTechnicians.isEmpty() && topCounterStaff.isEmpty()) {
                    summary.append("Technicians completed ").append(completedCount).append(" jobs in total. ");
                }
            } else {
                summary.append("No jobs were completed in the selected period. ");
            }
        }
        if (category.equals("ALL") || category.equals("RATING")) {
            if (ratingCount > 0) {
                summary.append("Customer satisfaction averaged ").append(String.format("%.1f", avgRating))
                    .append("/5 across ").append(ratingCount).append(" total ratings. ");
            } else {
                summary.append("No customer ratings have been submitted for the selected period. ");
            }
        }
        
        reportSummaryArea.setText(summary.toString());
        reportSummaryArea.setCaretPosition(0);
    }
    
    @Override
    protected void addEventHandlers() {
    }
}