package ui.counter;

import dao.AppointmentDAO;
import dao.PaymentDAO;
import dao.UserDAO;
import dao.ServiceDAO;
import enums.AppointmentStatus;
import enums.PaymentMethod;
import models.Appointment;
import models.Payment;
import models.User;
import ui.common.BasePanel;
import utils.DateUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class PaymentPanel extends BasePanel {
    
    private AppointmentDAO appointmentDAO;
    private PaymentDAO paymentDAO;
    private UserDAO userDAO;
    private JTable paymentTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JButton refreshButton;
    private JButton exportButton;
    private ServiceDAO serviceDAO; 
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> sortCombo;
    private JLabel statsLabel;
    private JLabel emptyLabel;
    private JPanel tableSwitcher;
    
    private final Color CARD_BORDER = new Color(226, 232, 240);
    private final Color TABLE_HEADER_BG = new Color(244, 246, 250);
    private final Color ROW_SEPARATOR = new Color(231, 235, 240);
    private final Color GREEN = new Color(34, 197, 94);
    private final Color BLUE = new Color(59, 130, 246);
    private final Color ORANGE = new Color(234, 179, 8);
    private final Color NAVY = new Color(31, 66, 99);
    private final Color YELLOW = new Color(252, 202, 12);
    
    public PaymentPanel() {
        this.appointmentDAO = new AppointmentDAO();
        this.paymentDAO = new PaymentDAO();
        this.userDAO = new UserDAO();
        this.serviceDAO = new ServiceDAO();
        
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initializeComponents();
        setupLayout();
        addEventHandlers();
        loadPaymentData();
    }
    
    private void refreshData() {
        // Create new DAO instances to get fresh data from files
        this.appointmentDAO = new AppointmentDAO();
        this.paymentDAO = new PaymentDAO();
        this.userDAO = new UserDAO();
        this.serviceDAO = new ServiceDAO();
        
        // Reload the table data
        loadPaymentData();
        
        // Force repaint
        paymentTable.revalidate();
        paymentTable.repaint();
    }
    
    @Override
    protected void initializeComponents() {
        String[] columns = {"Receipt ID", "Appointment ID", "Customer", "Service", "Amount", "Date", "Payment Method", "Payment Status", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8;
            }
        };
        
        paymentTable = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        paymentTable.setRowSorter(rowSorter);
        setupTableStyle();
        
        // Create buttons with action listeners
        refreshButton = createStyledButton("Refresh", GREEN);
        refreshButton.addActionListener(e -> {
            refreshData();
            JOptionPane.showMessageDialog(this, "Data has been refreshed.", 
                "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
        });
        
        exportButton = createStyledButton("Export to Excel", BLUE);
        exportButton.addActionListener(e -> exportToExcel());
        
        // Search field
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(9, 12, 9, 12)
        ));
        searchField.setToolTipText("Search by Appointment ID or Customer Name");
        
        // Status filter combo
        statusFilterCombo = new JComboBox<>(new String[]{"All Status", "PAID", "UNPAID"});
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilterCombo.setFocusable(false);
        
        // Sort options
        sortCombo = new JComboBox<>(new String[]{"Most Recent", "Oldest"});
        sortCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sortCombo.setBackground(Color.WHITE);
        sortCombo.setPreferredSize(new Dimension(130, 34));
        
        // Stats label
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(new Color(107, 114, 128));
        
        // Empty label
        emptyLabel = new JLabel("No payments match the current filters.");
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emptyLabel.setForeground(new Color(107, 114, 128));
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    private void setupTableStyle() {
        paymentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        paymentTable.setRowHeight(50);
        paymentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        paymentTable.setFillsViewportHeight(true);
        paymentTable.setFocusable(false);
        paymentTable.setRowSelectionAllowed(true);
        paymentTable.setColumnSelectionAllowed(false);
        paymentTable.setShowVerticalLines(false);
        paymentTable.setShowHorizontalLines(false);
        paymentTable.setIntercellSpacing(new Dimension(0, 0));
        paymentTable.setSelectionBackground(new Color(232, 240, 254));
        paymentTable.setSelectionForeground(new Color(31, 41, 55));
        
        JTableHeader header = paymentTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(new Color(31, 41, 55));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setBackground(TABLE_HEADER_BG);
                return label;
            }
        };
        
        for (int i = 0; i < paymentTable.getColumnModel().getColumnCount(); i++) {
            paymentTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        
        paymentTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        paymentTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        paymentTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        paymentTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        paymentTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        paymentTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        paymentTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        paymentTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        paymentTable.getColumnModel().getColumn(8).setPreferredWidth(130);
        
        paymentTable.setDefaultRenderer(Object.class, new PaymentTableCellRenderer());
        paymentTable.getColumnModel().getColumn(8).setCellRenderer(new ButtonRenderer());
        paymentTable.getColumnModel().getColumn(8).setCellEditor(new ButtonEditor());
    }
    
    @Override
    protected void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(PANEL_BG);
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createTableCard(), BorderLayout.CENTER);
        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(PANEL_BG);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(PANEL_BG);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(PANEL_BG);

        JLabel titleLabel = new JLabel("Payment Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(titleLabel);

        titleRow.add(titleBlock, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(PANEL_BG);
        actions.add(exportButton);
        actions.add(refreshButton);
        titleRow.add(actions, BorderLayout.EAST);

        wrapper.add(titleRow, BorderLayout.NORTH);
        wrapper.add(createToolbar(), BorderLayout.CENTER);

        return wrapper;
    }
    
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        controlsPanel.setBackground(Color.WHITE);

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setPreferredSize(new Dimension(350, 34));

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(new Color(31, 41, 55));
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        // Status filter panel
        JPanel statusPanel = new JPanel(new BorderLayout(8, 0));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setPreferredSize(new Dimension(200, 34));

        JLabel statusLabel = new JLabel("Status");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(31, 41, 55));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(statusFilterCombo, BorderLayout.CENTER);

        // Sort panel
        JPanel sortPanel = new JPanel(new BorderLayout(8, 0));
        sortPanel.setBackground(Color.WHITE);
        sortPanel.setPreferredSize(new Dimension(180, 34));

        JLabel sortLabel = new JLabel("Sort by ID");
        sortLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sortLabel.setForeground(new Color(31, 41, 55));
        sortPanel.add(sortLabel, BorderLayout.WEST);
        sortPanel.add(sortCombo, BorderLayout.CENTER);

        // Reset button
        JButton resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        resetButton.setBackground(new Color(156, 163, 175));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.setBorderPainted(false);
        resetButton.setOpaque(true);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        resetButton.setPreferredSize(new Dimension(80, 34));

        resetButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                resetButton.setBackground(new Color(136, 143, 155));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                resetButton.setBackground(new Color(156, 163, 175));
            }
        });

        resetButton.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            sortCombo.setSelectedIndex(0);
            applyFilters();
            updateStatsLabel();
            SwingUtilities.invokeLater(this::updateTableVisibility);
        });

        controlsPanel.add(searchPanel);
        controlsPanel.add(statusPanel);
        controlsPanel.add(sortPanel);
        controlsPanel.add(resetButton);
        toolbar.add(controlsPanel, BorderLayout.WEST);

        return toolbar;
    }
    
    private JPanel createTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createLineBorder(CARD_BORDER));

        JScrollPane scrollPane = new JScrollPane(paymentTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.add(emptyLabel);

        tableSwitcher = new JPanel(new CardLayout());
        tableSwitcher.add(scrollPane, "TABLE");
        tableSwitcher.add(emptyPanel, "EMPTY");

        tableCard.add(tableSwitcher, BorderLayout.CENTER);
        return tableCard;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(PANEL_BG);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        footer.add(statsLabel, BorderLayout.WEST);
        
        return footer;
    }
    
    private void loadPaymentData() {
        // Clear the table model completely
        tableModel.setRowCount(0);
        
        List<Appointment> allAppointments = appointmentDAO.readAll();
        List<Payment> payments = paymentDAO.readAll();
        
        for (Appointment appt : allAppointments) {
            // Only show COMPLETED or ASSIGNED appointments
            if (appt.getStatus() != AppointmentStatus.COMPLETED && appt.getStatus() != AppointmentStatus.ASSIGNED) {
                continue;
            }
            
            Payment existingPayment = null;
            boolean isPaid = false;
            for (Payment p : payments) {
                if (p.getAppointmentId().equals(appt.getId())) {
                    existingPayment = p;
                    isPaid = true;
                    break;
                }
            }
            
            User customer = userDAO.findById(appt.getCustomerId());
            
            String receiptId = existingPayment != null ? existingPayment.getId() : "-";
            String paymentMethod = existingPayment != null ? existingPayment.getPaymentMethod().toString() : "-";
            String paymentDate = existingPayment != null ? existingPayment.getPaymentDate() : "-";
            String paymentStatus = isPaid ? "PAID" : "UNPAID";
            double displayAmount = isPaid && existingPayment != null ? existingPayment.getAmount() : appt.getAmount();
            String actionText = isPaid ? "VIEW RECEIPT" : "COLLECT PAYMENT";
            
            Object[] row = {
                receiptId,
                appt.getId(),
                customer != null ? customer.getFullName() : "Unknown",
                appt.getServiceType().toString(),
                String.format("RM %.2f", displayAmount),
                paymentDate,
                paymentMethod,
                paymentStatus,
                actionText
            };
            tableModel.addRow(row);
        }
        
        // Reset and reapply filters
        rowSorter.setRowFilter(null);
        applyFilters();
        updateStatsLabel();
        SwingUtilities.invokeLater(this::updateTableVisibility);
        
        paymentTable.revalidate();
        paymentTable.repaint();
    }
    
    private void applyFilters() {
        rowSorter.setSortKeys(null);

        String searchText = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();
        String selectedSort = (String) sortCombo.getSelectedItem();
        
        RowFilter<DefaultTableModel, Integer> rowFilter = new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                // Status filter
                boolean statusMatches = "All Status".equals(selectedStatus) || 
                    entry.getValue(7).toString().equals(selectedStatus);
                
                // Search filter (search by Appointment ID or Customer name)
                boolean searchMatches = searchText.isEmpty();
                if (!searchText.isEmpty()) {
                    String appointmentId = entry.getValue(1).toString().toLowerCase();
                    String customer = entry.getValue(2).toString().toLowerCase();
                    searchMatches = appointmentId.contains(searchText) || customer.contains(searchText);
                }
                
                return statusMatches && searchMatches;
            }
        };
        
        rowSorter.setRowFilter(rowFilter);
        
        SwingUtilities.invokeLater(() -> {
            if ("Most Recent".equals(selectedSort)) {
                rowSorter.setSortKeys(
                    Collections.singletonList(new RowSorter.SortKey(1, SortOrder.DESCENDING))
                );
            } else {
                rowSorter.setSortKeys(
                    Collections.singletonList(new RowSorter.SortKey(1, SortOrder.ASCENDING))
                );
            }
        });
        
        rowSorter.sort();
    }
    
    private void updateTableVisibility() {
        if (tableSwitcher == null) {
            return;
        }
        
        CardLayout layout = (CardLayout) tableSwitcher.getLayout();
        layout.show(tableSwitcher, rowSorter.getViewRowCount() == 0 ? "EMPTY" : "TABLE");
    }
    
    private void updateStatsLabel() {
        List<Appointment> allAppointments = appointmentDAO.readAll();
        long totalEligible = allAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED || a.getStatus() == AppointmentStatus.ASSIGNED)
            .count();
        int displayedPayments = rowSorter.getViewRowCount();
        
        statsLabel.setText(String.format(
            "Total Eligible Appointments: %d    Displaying: %d",
            totalEligible, displayedPayments
        ));
    }
    
    private boolean isAppointmentPaid(String appointmentId, List<Payment> payments) {
        for (Payment payment : payments) {
            if (payment.getAppointmentId().equals(appointmentId)) {
                return true;
            }
        }
        return false;
    }
    
    private Payment getPaymentForAppointment(String appointmentId, List<Payment> payments) {
        for (Payment payment : payments) {
            if (payment.getAppointmentId().equals(appointmentId)) {
                return payment;
            }
        }
        return null;
    }
    
    private void collectPayment(String appointmentId) {
        Appointment appt = appointmentDAO.findById(appointmentId);
        if (appt == null) {
            JOptionPane.showMessageDialog(this, "Appointment not found.");
            return;
        }
        
        if (appt.getStatus() == AppointmentStatus.CANCELLED) {
            JOptionPane.showMessageDialog(this, "Cannot collect payment for CANCELLED appointment.", 
                "Payment Rejected", JOptionPane.ERROR_MESSAGE);
            refreshData();
            return;
        }
        
        if (appt.getStatus() == AppointmentStatus.PENDING) {
            JOptionPane.showMessageDialog(this, "Cannot collect payment for PENDING appointment.\nPlease assign a technician first.", 
                "Payment Rejected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (isAppointmentPaid(appointmentId, paymentDAO.readAll())) {
            JOptionPane.showMessageDialog(this, "Payment has already been collected for this appointment.", 
                "Already Paid", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
            return;
        }
        
        double appointmentAmount = appt.getAmount();
        
        PaymentMethod[] methods = PaymentMethod.values();
        PaymentMethod method = (PaymentMethod) JOptionPane.showInputDialog(
            this,
            "Select payment method for appointment " + appointmentId + ":\nAmount: RM " + String.format("%.2f", appointmentAmount),
            "Payment Method",
            JOptionPane.QUESTION_MESSAGE,
            null,
            methods,
            methods[0]
        );
        
        if (method != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Confirm payment for appointment %s?\n\nAppointment: %s\nCustomer: %s\nService: %s\nAmount: RM %.2f\nMethod: %s",
                    appointmentId, appointmentId,
                    userDAO.findById(appt.getCustomerId()) != null ? userDAO.findById(appt.getCustomerId()).getFullName() : "Unknown",
                    appt.getServiceType(), appointmentAmount, method),
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                Payment payment = new Payment();
                payment.setAppointmentId(appointmentId);
                payment.setAmount(appointmentAmount);
                payment.setPaymentDate(DateUtils.getCurrentDate());
                payment.setPaymentMethod(method);
                
                boolean paymentSaved = paymentDAO.save(payment);
                
                if (paymentSaved) {
                    appt.setAmount(appointmentAmount);
                    appointmentDAO.update(appt);
                    refreshData();
                    showReceipt(payment, appt);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to process payment. Please try again.", 
                        "Payment Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void showReceipt(Payment payment, Appointment appt) {
        User customer = userDAO.findById(appt.getCustomerId());
        
        JDialog receiptDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Payment Receipt", true);
        receiptDialog.setSize(520, 650);
        receiptDialog.setLocationRelativeTo(this);
        receiptDialog.setResizable(true);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setOpaque(false);
        ReceiptCarLogo carLogo = new ReceiptCarLogo();
        carLogo.setPreferredSize(new Dimension(60, 60));
        logoPanel.add(carLogo);
        headerPanel.add(logoPanel);
        headerPanel.add(Box.createVerticalStrut(8));
        
        JLabel receiptTitle = new JLabel("PAYMENT RECEIPT");
        receiptTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        receiptTitle.setForeground(Color.WHITE);
        receiptTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(receiptTitle);
        
        JLabel receiptSubtitle = new JLabel("APU-ASC SUPPORT SYSTEM");
        receiptSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        receiptSubtitle.setForeground(YELLOW);
        receiptSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(receiptSubtitle);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 40, 20, 40));
        
        JPanel detailsCard = new JPanel();
        detailsCard.setBackground(new Color(248, 250, 252));
        detailsCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        detailsCard.setLayout(new GridBagLayout());
        detailsCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.weightx = 0.35;
        
        addReceiptRow(detailsCard, gbc, 0, "Receipt Number:", payment.getId());
        addReceiptRow(detailsCard, gbc, 1, "Date:", payment.getPaymentDate());
        addReceiptRow(detailsCard, gbc, 2, "Time:", java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        addSeparatorRow(detailsCard, gbc, 3);
        addReceiptRow(detailsCard, gbc, 4, "Appointment ID:", payment.getAppointmentId());
        addReceiptRow(detailsCard, gbc, 5, "Customer Name:", customer != null ? customer.getFullName() : "Unknown");
        addReceiptRow(detailsCard, gbc, 6, "Service Type:", appt.getServiceType().toString());
        addSeparatorRow(detailsCard, gbc, 7);
        addReceiptRow(detailsCard, gbc, 8, "Amount Paid:", String.format("RM %.2f", payment.getAmount()));
        addReceiptRow(detailsCard, gbc, 9, "Payment Method:", payment.getPaymentMethod().toString());
        
        detailsCard.setPreferredSize(new Dimension(400, detailsCard.getPreferredSize().height));

        JPanel detailsWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        detailsWrapper.setBackground(Color.WHITE);
        detailsWrapper.add(detailsCard);

        contentPanel.add(detailsWrapper);
        contentPanel.add(Box.createVerticalStrut(20));
        
        JPanel thankPanel = new JPanel();
        thankPanel.setBackground(new Color(240, 253, 244));
        thankPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(187, 247, 208), 1),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        thankPanel.setPreferredSize(new Dimension(440, 50));
        thankPanel.setMaximumSize(new Dimension(440, 50));
        
        JPanel thankWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        thankWrapper.setBackground(Color.WHITE);
        thankWrapper.add(thankPanel);
        
        JLabel thankLabel = new JLabel("Thank you for your payment! Your transaction has been processed successfully.");
        thankLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        thankLabel.setForeground(new Color(22, 101, 52));
        thankPanel.add(thankLabel);
        
        contentPanel.add(thankPanel);
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        footerPanel.setBackground(new Color(248, 250, 252));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
        
        JButton pdfBtn = new JButton("Save as PDF");
        pdfBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pdfBtn.setBackground(NAVY);
        pdfBtn.setForeground(Color.WHITE);
        pdfBtn.setFocusPainted(false);
        pdfBtn.setBorderPainted(false);
        pdfBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pdfBtn.setPreferredSize(new Dimension(120, 38));
        pdfBtn.addActionListener(e -> saveReceiptAsPDF(payment, appt, customer));
        
        JButton printBtn = new JButton("Print Receipt");
        printBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        printBtn.setBackground(new Color(34, 197, 94));
        printBtn.setForeground(Color.WHITE);
        printBtn.setFocusPainted(false);
        printBtn.setBorderPainted(false);
        printBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        printBtn.setPreferredSize(new Dimension(120, 38));
        printBtn.addActionListener(e -> printReceipt(payment, appt, customer));
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setBackground(new Color(156, 163, 175));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(100, 38));
        closeBtn.addActionListener(e -> receiptDialog.dispose());
        
        footerPanel.add(pdfBtn);
        footerPanel.add(printBtn);
        footerPanel.add(closeBtn);
        
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        receiptDialog.add(mainPanel);
        receiptDialog.setVisible(true);
    }
    
    private void saveReceiptAsPDF(Payment payment, Appointment appt, User customer) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String fileName = "receipt_" + payment.getId() + "_" + timestamp + ".html";
            String userHome = System.getProperty("user.home");
            String desktopPath = userHome + "/Desktop";
            
            File desktopDir = new File(desktopPath);
            if (!desktopDir.exists()) {
                desktopPath = userHome;
            }
            
            String filePath = desktopPath + File.separator + fileName;
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
                writer.println("<!DOCTYPE html>");
                writer.println("<html>");
                writer.println("<head>");
                writer.println("<meta charset='UTF-8'>");
                writer.println("<title>Payment Receipt</title>");
                writer.println("<style>");
                writer.println("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; }");
                writer.println(".receipt { max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; }");
                writer.println(".header { background-color: #1f4263; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }");
                writer.println(".content { padding: 20px; }");
                writer.println(".detail-row { display: flex; padding: 8px 0; border-bottom: 1px solid #e2e8f0; }");
                writer.println(".detail-label { font-weight: bold; width: 40%; }");
                writer.println(".detail-value { width: 60%; }");
                writer.println(".thankyou { background-color: #f0fdf4; border: 1px solid #bbf7d0; padding: 10px; text-align: center; border-radius: 5px; margin-top: 15px; }");
                writer.println(".footer { background-color: #f8fafc; padding: 10px; text-align: center; border-top: 1px solid #e2e8f0; font-size: 11px; }");
                writer.println("</style>");
                writer.println("</head>");
                writer.println("<body>");
                writer.println("<div class='receipt'>");
                writer.println("<div class='header'>");
                writer.println("<h2>PAYMENT RECEIPT</h2>");
                writer.println("<p>APU-ASC SUPPORT SYSTEM</p>");
                writer.println("</div>");
                writer.println("<div class='content'>");
                writer.println("<div class='detail-row'><div class='detail-label'>Receipt Number:</div><div class='detail-value'>" + payment.getId() + "</div></div>");
                writer.println("<div class='detail-row'><div class='detail-label'>Date:</div><div class='detail-value'>" + payment.getPaymentDate() + "</div></div>");
                writer.println("<div class='detail-row'><div class='detail-label'>Time:</div><div class='detail-value'>" + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "</div></div>");
                writer.println("<div class='detail-row'><div class='detail-label'>Appointment ID:</div><div class='detail-value'>" + payment.getAppointmentId() + "</div></div>");
                writer.println("<div class='detail-row'><div class='detail-label'>Customer Name:</div><div class='detail-value'>" + (customer != null ? customer.getFullName() : "Unknown") + "</div></div>");
                writer.println("<div class='detail-row'><div class='detail-label'>Service Type:</div><div class='detail-value'>" + appt.getServiceType() + "</div></div>");
                writer.println("<div class='detail-row'><div class='detail-label'>Amount Paid:</div><div class='detail-value'>RM " + String.format("%.2f", payment.getAmount()) + "</div></div>");
                writer.println("<div class='detail-row'><div class='detail-label'>Payment Method:</div><div class='detail-value'>" + payment.getPaymentMethod() + "</div></div>");
                writer.println("<div class='thankyou'>Thank you for your payment!</div>");
                writer.println("</div>");
                writer.println("<div class='footer'>Generated by APU-ASC System</div>");
                writer.println("</div>");
                writer.println("</body>");
                writer.println("</html>");
            }
            
            File htmlFile = new File(filePath);
            Desktop.getDesktop().browse(htmlFile.toURI());
            
            JOptionPane.showMessageDialog(this, 
                "Receipt saved as HTML file!\nFile saved to: " + filePath + "\nOpening in browser...", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving receipt: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void printReceipt(Payment payment, Appointment appt, User customer) {
        JPanel printPanel = new JPanel();
        printPanel.setLayout(new BoxLayout(printPanel, BoxLayout.Y_AXIS));
        printPanel.setBackground(Color.WHITE);
        printPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        JLabel titleLabel = new JLabel("PAYMENT RECEIPT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(NAVY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        printPanel.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("APU-ASC SUPPORT SYSTEM");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        printPanel.add(subtitleLabel);
        
        printPanel.add(Box.createVerticalStrut(20));
        
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(400, 2));
        printPanel.add(sep);
        printPanel.add(Box.createVerticalStrut(15));
        
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        addPrintableRow(detailsPanel, gbc, 0, "Receipt Number:", payment.getId());
        addPrintableRow(detailsPanel, gbc, 1, "Date:", payment.getPaymentDate());
        addPrintableRow(detailsPanel, gbc, 2, "Time:", java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        addPrintableRow(detailsPanel, gbc, 3, "Appointment ID:", payment.getAppointmentId());
        addPrintableRow(detailsPanel, gbc, 4, "Customer Name:", customer != null ? customer.getFullName() : "Unknown");
        addPrintableRow(detailsPanel, gbc, 5, "Service Type:", appt.getServiceType().toString());
        addPrintableRow(detailsPanel, gbc, 6, "Amount Paid:", String.format("RM %.2f", payment.getAmount()));
        addPrintableRow(detailsPanel, gbc, 7, "Payment Method:", payment.getPaymentMethod().toString());
        
        printPanel.add(detailsPanel);
        printPanel.add(Box.createVerticalStrut(15));
        
        JLabel thankLabel = new JLabel("Thank you for your payment!");
        thankLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        thankLabel.setForeground(GREEN);
        thankLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        printPanel.add(thankLabel);
        
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
                
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                printPanel.setSize((int)pageFormat.getImageableWidth(), printPanel.getPreferredSize().height);
                printPanel.print(g2d);
                
                return Printable.PAGE_EXISTS;
            });
            
            if (job.printDialog()) {
                job.print();
                JOptionPane.showMessageDialog(this, "Receipt sent to printer!", 
                    "Print", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Error printing: " + e.getMessage(), 
                "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addPrintableRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 11));
        panel.add(labelComp, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(valueComp, gbc);
    }
    
    private void addReceiptRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelComp.setForeground(new Color(60, 60, 60));
        panel.add(labelComp, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueComp.setForeground(new Color(31, 41, 55));
        panel.add(valueComp, gbc);
        gbc.weightx = 0.35;
    }
    
    private void addSeparatorRow(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(226, 232, 240));
        panel.add(sep, gbc);
        gbc.gridwidth = 1;
    }
    
    private void exportToExcel() {
        try {
            String timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "payment_report_" + timestamp + ".csv";
            String userHome = System.getProperty("user.home");
            String desktopPath = userHome + "/Desktop";
            
            File desktopDir = new File(desktopPath);
            if (!desktopDir.exists()) {
                desktopPath = userHome;
            }
            
            String filePath = desktopPath + File.separator + fileName;
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
                writer.println("Receipt ID,Appointment ID,Customer,Service,Amount,Date,Payment Method,Payment Status");
                
                boolean hasData = false;
                for (int i = 0; i < rowSorter.getViewRowCount(); i++) {
                    int modelRow = paymentTable.convertRowIndexToModel(i);
                    String receiptId = tableModel.getValueAt(modelRow, 0).toString();
                    String appointmentId = tableModel.getValueAt(modelRow, 1).toString();
                    String customer = tableModel.getValueAt(modelRow, 2).toString();
                    String service = tableModel.getValueAt(modelRow, 3).toString();
                    String amount = tableModel.getValueAt(modelRow, 4).toString();
                    String date = tableModel.getValueAt(modelRow, 5).toString();
                    String method = tableModel.getValueAt(modelRow, 6).toString();
                    String status = tableModel.getValueAt(modelRow, 7).toString();
                    
                    if (!receiptId.equals("-") && !appointmentId.equals("-")) {
                        writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                            receiptId, appointmentId, customer, service, amount, date, method, status);
                        hasData = true;
                    }
                }
                
                if (!hasData) {
                    writer.println("No payment records found");
                }
            }
            
            File excelFile = new File(filePath);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(excelFile);
            }
            
            JOptionPane.showMessageDialog(this, 
                "Payment report exported successfully!\nFile saved to: " + filePath, 
                "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage(), 
                "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
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
        button.setPreferredSize(new Dimension(130, 34));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    @Override
    protected void addEventHandlers() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
                updateStatsLabel();
                SwingUtilities.invokeLater(PaymentPanel.this::updateTableVisibility);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
                updateStatsLabel();
                SwingUtilities.invokeLater(PaymentPanel.this::updateTableVisibility);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
                updateStatsLabel();
                SwingUtilities.invokeLater(PaymentPanel.this::updateTableVisibility);
            }
        });
        
        statusFilterCombo.addActionListener(e -> {
            applyFilters();
            updateStatsLabel();
            SwingUtilities.invokeLater(this::updateTableVisibility);
        });
        
        sortCombo.addActionListener(e -> applyFilters());
    }
    
    private class ReceiptCarLogo extends JPanel {
        public ReceiptCarLogo() {
            setOpaque(false);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            g2d.setColor(YELLOW);
            g2d.fill(new RoundRectangle2D.Double(3, 3, width - 6, height - 6, 10, 10));
            
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2f));
            g2d.draw(new RoundRectangle2D.Double(3, 3, width - 6, height - 6, 10, 10));
            
            g2d.setColor(NAVY);
            g2d.fillRoundRect(14, height/2 - 3, 32, 14, 6, 6);
            g2d.fillRoundRect(20, height/2 - 10, 18, 10, 4, 4);
            
            g2d.setColor(new Color(200, 220, 255));
            g2d.fillRoundRect(22, height/2 - 8, 6, 6, 2, 2);
            g2d.fillRoundRect(30, height/2 - 8, 5, 6, 2, 2);
            
            g2d.setColor(Color.BLACK);
            g2d.fillOval(17, height/2 + 7, 8, 8);
            g2d.fillOval(35, height/2 + 7, 8, 8);
            
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval(19, height/2 + 9, 4, 4);
            g2d.fillOval(37, height/2 + 9, 4, 4);
            
            g2d.setColor(YELLOW);
            g2d.fillOval(43, height/2 + 1, 4, 4);
            
            g2d.setColor(Color.RED);
            g2d.fillOval(12, height/2 + 1, 4, 4);
        }
    }
    
    class PaymentTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            JLabel label = (JLabel) c;
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
                BorderFactory.createEmptyBorder(10, 18, 10, 10)
            ));
            
            if (!isSelected) {
                label.setBackground(Color.WHITE);
                label.setForeground(new Color(31, 41, 55));
            } else {
                label.setBackground(new Color(232, 240, 254));
                label.setForeground(new Color(31, 41, 55));
            }
            
            if (column == 7 && value != null) {
                String status = value.toString();
                if ("PAID".equals(status)) {
                    label.setForeground(GREEN);
                } else if ("UNPAID".equals(status)) {
                    label.setForeground(ORANGE);
                }
                if (isSelected) {
                    label.setForeground(new Color(31, 41, 55));
                }
            }
            
            return label;
        }
    }
    
    class ButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton button;
        
        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 8));
            setOpaque(true);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            removeAll();
            setBackground(isSelected ? new Color(232, 240, 254) : Color.WHITE);
            
            String actionText = value != null ? value.toString() : "";
            Color btnColor = actionText.equals("COLLECT PAYMENT") ? ORANGE : GREEN;
            
            button = new JButton(actionText);
            button.setFont(new Font("Segoe UI", Font.BOLD, 10));
            button.setBackground(btnColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setPreferredSize(new Dimension(115, 30));
            
            add(button);
            return this;
        }
    }
    
    class ButtonEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private JPanel panel;
        private JButton button;
        private String appointmentId;
        private String actionType;
        private int currentRow;
        
        public ButtonEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR));
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            panel.removeAll();
            panel.setBackground(new Color(232, 240, 254));
            
            int modelRow = table.convertRowIndexToModel(row);
            appointmentId = (String) tableModel.getValueAt(modelRow, 1);
            actionType = value != null ? value.toString() : "";
            currentRow = row;
            
            Color btnColor = actionType.equals("COLLECT PAYMENT") ? ORANGE : GREEN;
            button = new JButton(actionType);
            button.setFont(new Font("Segoe UI", Font.BOLD, 10));
            button.setBackground(btnColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setPreferredSize(new Dimension(115, 30));
            
            button.addActionListener(e -> {
                if (actionType.equals("COLLECT PAYMENT")) {
                    collectPayment(appointmentId);
                    SwingUtilities.invokeLater(() -> {
                        refreshData();
                    });
                } else if (actionType.equals("VIEW RECEIPT")) {
                    Appointment appt = appointmentDAO.findById(appointmentId);
                    Payment payment = null;
                    for (Payment p : paymentDAO.readAll()) {
                        if (p.getAppointmentId().equals(appointmentId)) {
                            payment = p;
                            break;
                        }
                    }
                    if (appt != null && payment != null) {
                        showReceipt(payment, appt);
                    }
                }
                fireEditingStopped();
            });
            
            panel.add(button);
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return actionType;
        }
    }
}