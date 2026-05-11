package ui.customer;

import dao.AppointmentDAO;
import dao.PaymentDAO;
import dao.UserDAO;
import enums.AppointmentStatus;
import enums.ServiceType;
import models.Appointment;
import models.Payment;
import models.User;
import ui.common.BasePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomerHistoryPanel extends BasePanel {

    private final User currentUser;
    private final AppointmentDAO appointmentDAO;
    private final PaymentDAO paymentDAO;
    private final UserDAO userDAO;

    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> serviceFilter;
    private JButton applyButton;
    private JButton resetButton;

    private JTable serviceTable;
    private JTable paymentTable;
    private DefaultTableModel serviceTableModel;
    private DefaultTableModel paymentTableModel;

    public CustomerHistoryPanel(User currentUser) {
        this.currentUser = currentUser;
        this.appointmentDAO = new AppointmentDAO();
        this.paymentDAO = new PaymentDAO();
        this.userDAO = new UserDAO();

        setBackground(PANEL_BG);
        initializeComponents();
        setupLayout();
        addEventHandlers();
        refreshTables();
    }

    @Override
    protected void initializeComponents() {
        searchField = new JTextField("Type appointment ID or date", 20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setForeground(Color.GRAY);
        searchField.setBackground(Color.WHITE);
        searchField.setCaretColor(Color.BLACK);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        statusFilter = new JComboBox<>(new String[]{
                "All Status", "PENDING", "ASSIGNED", "COMPLETED", "PAID"
        });
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.setBackground(Color.WHITE);
        statusFilter.setForeground(Color.BLACK);

        serviceFilter = new JComboBox<>(new String[]{
                "All Services", "NORMAL", "MAJOR"
        });
        serviceFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        serviceFilter.setBackground(Color.WHITE);
        serviceFilter.setForeground(Color.BLACK);

        applyButton = new JButton("Apply");
        applyButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        applyButton.setBackground(NAVY_BLUE);
        applyButton.setForeground(Color.WHITE);
        applyButton.setFocusPainted(false);
        applyButton.setBorderPainted(false);
        applyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        resetButton.setBackground(new Color(230, 230, 230));
        resetButton.setForeground(Color.BLACK);
        resetButton.setFocusPainted(false);
        resetButton.setBorderPainted(false);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        serviceTableModel = new DefaultTableModel(
                new String[]{"Appointment ID", "Date", "Time", "Service Type", "Status", "Technician", "Amount"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        paymentTableModel = new DefaultTableModel(
                new String[]{"Payment ID", "Appointment ID", "Payment Date", "Method", "Amount", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        serviceTable = createStyledTable(serviceTableModel);
        paymentTable = createStyledTable(paymentTableModel);

        serviceTable.getColumnModel().getColumn(4).setCellRenderer(new StatusColorRenderer());
        paymentTable.getColumnModel().getColumn(5).setCellRenderer(new StatusColorRenderer());
    }

    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(PANEL_BG);

        JLabel titleLabel = new JLabel("Service and Payment History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("View your service history, payment history, search and filter records.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel filterPanel = createSearchFilterPanel();
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(subtitleLabel);
        topPanel.add(Box.createVerticalStrut(15));
        topPanel.add(filterPanel);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.addTab("Service History", createTablePanel(serviceTable));
        tabbedPane.addTab("Payment History", createTablePanel(paymentTable));

        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createSearchFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 2),
                BorderFactory.createLineBorder(BORDER_DARK, 2)
        ));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel serviceLabel = new JLabel("Service:");
        serviceLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        panel.add(searchLabel);
        panel.add(searchField);
        panel.add(statusLabel);
        panel.add(statusFilter);
        panel.add(serviceLabel);
        panel.add(serviceFilter);
        panel.add(applyButton);
        panel.add(resetButton);

        return panel;
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

    private void refreshTables() {
        serviceTableModel.setRowCount(0);
        paymentTableModel.setRowCount(0);

        List<Appointment> appointments = getFilteredAppointments();
        Set<String> appointmentIds = new HashSet<>();

        for (Appointment appointment : appointments) {
            appointmentIds.add(appointment.getId());
            serviceTableModel.addRow(new Object[]{
                    appointment.getId(),
                    appointment.getDate(),
                    appointment.getStartTime(),
                    appointment.getServiceType(),
                    appointment.getStatus(),
                    resolveUserName(appointment.getTechnicianId()),
                    formatAmount(appointment.getAmount())
            });
        }

        for (Payment payment : paymentDAO.readAll()) {
            if (appointmentIds.contains(payment.getAppointmentId())) {
                paymentTableModel.addRow(new Object[]{
                        payment.getId(),
                        payment.getAppointmentId(),
                        payment.getPaymentDate(),
                        payment.getPaymentMethod(),
                        formatAmount(payment.getAmount()),
                        resolvePaymentStatus(payment)
                });
            }
        }
    }

    private List<Appointment> getFilteredAppointments() {
        List<Appointment> result = new ArrayList<>();
        String query = searchField.getText().trim().toLowerCase();

        if (query.equals("type appointment id or date")) {
            query = "";
        }

        String selectedStatus = (String) statusFilter.getSelectedItem();
        String selectedService = (String) serviceFilter.getSelectedItem();

        for (Appointment appointment : appointmentDAO.findByCustomerId(currentUser.getId())) {
            if (!matchesQuery(appointment, query)) {
                continue;
            }
            if (!matchesStatus(appointment, selectedStatus)) {
                continue;
            }
            if (!matchesService(appointment, selectedService)) {
                continue;
            }
            result.add(appointment);
        }

        return result;
    }

    private boolean matchesQuery(Appointment appointment, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return appointment.getId().toLowerCase().contains(query)
                || appointment.getDate().toLowerCase().contains(query);
    }

    private boolean matchesStatus(Appointment appointment, String selectedStatus) {
        if (selectedStatus == null || "All Status".equals(selectedStatus)) {
            return true;
        }
        return appointment.getStatus() == AppointmentStatus.valueOf(selectedStatus);
    }

    private boolean matchesService(Appointment appointment, String selectedService) {
        if (selectedService == null || "All Services".equals(selectedService)) {
            return true;
        }
        return appointment.getServiceType() == ServiceType.valueOf(selectedService);
    }

    private String resolvePaymentStatus(Payment payment) {
        if (payment == null) {
            return "-";
        }
        return payment.getAmount() > 0 ? "PAID" : "PENDING";
    }

    private String resolveUserName(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return "-";
        }
        User user = userDAO.findById(userId);
        return user != null ? user.getFullName() : userId;
    }

    private String formatAmount(double amount) {
        return amount > 0 ? String.format("RM%.2f", amount) : "-";
    }

    @Override
    protected void addEventHandlers() {
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals("Type appointment ID or date")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText("Type appointment ID or date");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        applyButton.addActionListener(e -> refreshTables());

        resetButton.addActionListener(e -> {
            searchField.setText("Type appointment ID or date");
            searchField.setForeground(Color.GRAY);
            statusFilter.setSelectedIndex(0);
            serviceFilter.setSelectedIndex(0);
            refreshTables();
        });

        searchField.addActionListener(e -> refreshTables());
    }

    private static class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );

            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            if (!isSelected) {
                label.setBackground(Color.WHITE);
            }

            String status = value == null ? "" : value.toString().toUpperCase();

            if ("COMPLETED".equals(status)) {
                label.setForeground(new Color(34, 139, 34));
            } else if ("PENDING".equals(status)) {
                label.setForeground(new Color(204, 153, 0));
            } else if ("ASSIGNED".equals(status)) {
                label.setForeground(new Color(0, 102, 204));
            } else if ("PAID".equals(status)) {
                label.setForeground(new Color(34, 139, 34));
            } else {
                label.setForeground(Color.BLACK);
            }

            return label;
        }
    }
}
