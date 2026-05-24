package ui.technician;

import dao.AppointmentDAO;
import dao.CommentDAO;
import dao.PaymentDAO;
import dao.FeedbackDAO;
import dao.UserDAO;
import enums.AppointmentStatus;
import models.Appointment;
import models.Comment;
import models.Payment;
import models.Feedback;
import models.User;
import ui.common.BasePanel;
import utils.DateUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyJobsPanel extends BasePanel {

    private final User currentTechnician;
    private AppointmentDAO appointmentDAO;
    private UserDAO userDAO;
    private CommentDAO commentDAO;
    private FeedbackDAO techNoteDAO;
    private PaymentDAO paymentDAO;

    private JTable jobsTable;
    private JobTableModel tableModel;
    private TableRowSorter<JobTableModel> rowSorter;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JLabel statsLabel;
    private JLabel emptyLabel;
    private JPanel tableSwitcher;
    private JButton viewButton;
    private JButton updateButton;
    private JButton resetButton;
    private JButton refreshButton; 
    
    private Set<String> paidAppointmentIds;
    
    private final Color CARD_BORDER = new Color(221, 225, 231);
    private final Color TABLE_HEADER_BG = new Color(244, 246, 250);
    private final Color TABLE_SELECTION = new Color(232, 240, 254);
    private final Color ROW_SEPARATOR = new Color(231, 235, 240);
    private final Color GREEN = new Color(34, 197, 94);
    private final Color BLUE = new Color(37, 99, 235);
    private final Color ORANGE = new Color(234, 179, 8);

    public MyJobsPanel(User technician) {
        this.currentTechnician = technician;
        this.appointmentDAO = new AppointmentDAO();
        this.userDAO = new UserDAO();
        this.commentDAO = new CommentDAO();
        this.techNoteDAO = new FeedbackDAO();
        this.paymentDAO = new PaymentDAO();
        this.paidAppointmentIds = new HashSet<>();

        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout());

        initializeComponents();
        setupLayout();
        addEventHandlers();
        loadJobs();
    }

    private void refreshData() {
        this.appointmentDAO = new AppointmentDAO();
        this.userDAO = new UserDAO();
        this.commentDAO = new CommentDAO();
        this.techNoteDAO = new FeedbackDAO();
        this.paymentDAO = new PaymentDAO();
        refreshPaidAppointmentIds();
        loadJobs();
    }

    @Override
    protected void initializeComponents() {
        String[] columns = {"ID", "Date", "Time", "Service", "Status", "Rating", "Feedback"};
        
        tableModel = new JobTableModel();
        rowSorter = new TableRowSorter<>(tableModel);
        jobsTable = new JTable(tableModel);
        jobsTable.setRowSorter(rowSorter);
        setupTableStyle();

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(9, 12, 9, 12)
        ));

        statusFilter = new JComboBox<>(new String[]{"All Status", "ASSIGNED", "COMPLETED", "CANCELLED"});        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.setFocusable(false);

        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(new Color(107, 114, 128));

        emptyLabel = new JLabel("No jobs match the current filters.");
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emptyLabel.setForeground(new Color(107, 114, 128));
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);

        viewButton = createActionButton("View Details", BLUE);
        updateButton = createActionButton("Update Job", GREEN);
        resetButton = createSecondaryButton("Reset");
        refreshButton = createActionButton("Refresh", new Color(34, 197, 94));
        
        viewButton.setEnabled(false);
        updateButton.setEnabled(false);
    }

    private void setupTableStyle() {
        jobsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jobsTable.setRowHeight(50);
        jobsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        jobsTable.setFillsViewportHeight(true);
        jobsTable.setFocusable(false);
        jobsTable.setRowSelectionAllowed(true);
        jobsTable.setColumnSelectionAllowed(false);
        jobsTable.setShowVerticalLines(false);
        jobsTable.setShowHorizontalLines(false);
        jobsTable.setGridColor(ROW_SEPARATOR);
        jobsTable.setIntercellSpacing(new Dimension(0, 0));
        jobsTable.setSelectionBackground(TABLE_SELECTION);
        jobsTable.setSelectionForeground(new Color(31, 41, 55));
        jobsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = jobsTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
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

        for (int i = 0; i < jobsTable.getColumnModel().getColumnCount(); i++) {
            jobsTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        jobsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        jobsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        jobsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        jobsTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        jobsTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        jobsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        jobsTable.getColumnModel().getColumn(6).setPreferredWidth(250);

        for (int i = 0; i < 7; i++) {
            jobsTable.getColumnModel().getColumn(i).setCellRenderer(new PaddedCellRenderer(i == 4));
        }
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

        JLabel titleLabel = new JLabel("My Jobs");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBlock.add(titleLabel);
        titleRow.add(titleBlock, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(PANEL_BG);
        rightPanel.add(refreshButton);
        titleRow.add(rightPanel, BorderLayout.EAST);

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

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setPreferredSize(new Dimension(350, 34));

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(new Color(31, 41, 55));
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JPanel filterPanel = new JPanel(new BorderLayout(8, 0));
        filterPanel.setBackground(Color.WHITE);

        JLabel filterLabel = new JLabel("Status");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterLabel.setForeground(new Color(31, 41, 55));
        filterPanel.add(filterLabel, BorderLayout.WEST);
        filterPanel.add(statusFilter, BorderLayout.CENTER);
        filterPanel.setPreferredSize(new Dimension(200, 34));

        controlsPanel.add(searchPanel);
        controlsPanel.add(filterPanel);
        controlsPanel.add(resetButton);
        toolbar.add(controlsPanel, BorderLayout.WEST);

        return toolbar;
    }
    
    private JPanel createTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createLineBorder(CARD_BORDER));

        JScrollPane scrollPane = new JScrollPane(jobsTable);
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
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(PANEL_BG);
        buttonPanel.add(viewButton);
        buttonPanel.add(updateButton);
        footer.add(buttonPanel, BorderLayout.EAST);
        
        return footer;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setPreferredSize(new Dimension(110, 34));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(new Color(156, 163, 175));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setPreferredSize(new Dimension(80, 34));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(136, 143, 155));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(156, 163, 175));
            }
        });
        
        return button;
    }

    @Override
    protected void addEventHandlers() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        statusFilter.addActionListener(e -> applyFilters());
        
        resetButton.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            applyFilters();
        });
        
        refreshButton.addActionListener(e -> {
            refreshData();
            JOptionPane.showMessageDialog(this, "Jobs have been refreshed.", 
                "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
        });

        jobsTable.getSelectionModel().addListSelectionListener(e -> updateButtonStates());
        jobsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && getSelectedAppointment() != null) {
                    viewDetails(getSelectedAppointment());
                }
            }
        });

        viewButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleViewButtonClick();
            }
        });
        
        updateButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleUpdateButtonClick();
            }
        });
    }

    private void handleViewButtonClick() {
        Appointment selected = getSelectedAppointment();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select an appointment to view.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        viewDetails(selected);
    }

    private void handleUpdateButtonClick() {
        Appointment selected = getSelectedAppointment();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select an appointment to update.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String status = getDisplayStatus(selected);
        
        // Check if appointment is CANCELLED
        if ("CANCELLED".equals(status)) {
            JOptionPane.showMessageDialog(this, 
                "Cannot update a cancelled appointment.\n\n"
                + "Appointment " + selected.getId() + " has been cancelled.\n"
                + "You can only view the details.", 
                "Cannot Update", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        boolean hasNote = hasTechNote(selected.getId());
        
        if ("COMPLETED".equals(status) && hasNote) {
            JOptionPane.showMessageDialog(this, 
                "This job has already been completed with service notes.\n\n"
                + "Status: COMPLETED\n"
                + "Feedback: Already submitted\n\n"
                + "No further updates are allowed for this appointment.", 
                "Cannot Update", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        showUpdateJobDialog(selected);
    }

    private void updateButtonStates() {
        Appointment selected = getSelectedAppointment();
        boolean hasSelection = selected != null;
        
        if (hasSelection) {
            String status = getDisplayStatus(selected);
            boolean hasNote = hasTechNote(selected.getId());
            
            // Check if appointment is CANCELLED
            if ("CANCELLED".equals(status)) {
                viewButton.setEnabled(true);
                updateButton.setEnabled(false);
                updateButton.setToolTipText("Cannot update cancelled appointments");
            } else if ("COMPLETED".equals(status) && hasNote) {
                viewButton.setEnabled(true);
                updateButton.setEnabled(false);
                updateButton.setToolTipText("Already completed with service notes");
            } else {
                viewButton.setEnabled(true);
                updateButton.setEnabled(true);
                updateButton.setToolTipText("Update job status and add service notes");
            }
        } else {
            viewButton.setEnabled(false);
            updateButton.setEnabled(false);
            viewButton.setToolTipText("Select a job to view");
            updateButton.setToolTipText("Select a job to update");
        }
    }
    
    private void refreshPaidAppointmentIds() {
        paidAppointmentIds.clear();
        for (Payment payment : paymentDAO.readAll()) {
            paidAppointmentIds.add(payment.getAppointmentId());
        }
    }
    
    private String getDisplayStatus(Appointment appointment) {
        return appointment.getStatus().toString();
    }
    
    private boolean isPaid(String appointmentId) {
        return paidAppointmentIds.contains(appointmentId);
    }
    
    private boolean hasTechNote(String appointmentId) {
        for (Feedback note : techNoteDAO.readAll()) {
            if (note.getAppointmentId().equals(appointmentId) 
                    && currentTechnician.getId().equals(note.getTechnicianId())) {
                return true;
            }
        }
        return false;
    }
    
    private String getTechNotePreview(String appointmentId) {
        for (Feedback note : techNoteDAO.readAll()) {
            if (note.getAppointmentId().equals(appointmentId) 
                    && currentTechnician.getId().equals(note.getTechnicianId())) {
                String content = note.getContent();
                if (content.length() > 60) {
                    return content.substring(0, 57) + "...";
                }
                return content;
            }
        }
        return "No feedback yet";
    }

    private void loadJobs() {
        refreshPaidAppointmentIds();
        // Show ALL appointments assigned to this technician (ASSIGNED, COMPLETED, and CANCELLED)
        List<Appointment> allAppointments = appointmentDAO.findByTechnicianId(currentTechnician.getId());
        tableModel.setAppointments(allAppointments);
        applyFilters();
        updateButtonStates();
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedStatus = (String) statusFilter.getSelectedItem();

        rowSorter.setRowFilter(new RowFilter<JobTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends JobTableModel, ? extends Integer> entry) {
                JobTableModel model = entry.getModel();
                Appointment appointment = model.getAppointmentAt(entry.getIdentifier());
                
                if (appointment == null) return false;
                
                boolean statusMatches = "All Status".equals(selectedStatus);
                if (!statusMatches) {
                    statusMatches = getDisplayStatus(appointment).equals(selectedStatus);
                }
                
                if (!statusMatches) return false;
                
                if (!search.isEmpty()) {
                    return appointment.getId().toLowerCase().contains(search);
                }
                
                return true;
            }
        });

        updateTableVisibility();
        updateStatsLabel();
    }

    private void updateTableVisibility() {
        if (tableSwitcher == null) return;
        CardLayout layout = (CardLayout) tableSwitcher.getLayout();
        layout.show(tableSwitcher, jobsTable.getRowCount() == 0 ? "EMPTY" : "TABLE");
    }

    private void updateStatsLabel() {
        int total = jobsTable.getRowCount();
        statsLabel.setText("Showing " + total + " job" + (total != 1 ? "s" : ""));
    }

    private Appointment getSelectedAppointment() {
        int selectedRow = jobsTable.getSelectedRow();
        if (selectedRow < 0) return null;
        int modelRow = jobsTable.convertRowIndexToModel(selectedRow);
        return tableModel.getAppointmentAt(modelRow);
    }
    
    private void viewDetails(Appointment a) {
        if (a == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select an appointment to view.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Appointment Details", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(PANEL_BG);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(PANEL_BG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        root.add(createDetailsHeader(a), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(PANEL_BG);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 0, 12, 12);
        g.fill = GridBagConstraints.BOTH;
        g.weightx = 1;

        g.gridx = 0;
        g.gridy = 0;
        g.weighty = 0;
        content.add(createInfoCard("Service Details",
                new String[][]{
                        {"Date", safe(a.getDate())},
                        {"Time", safe(a.getStartTime())},
                        {"Service", String.valueOf(a.getServiceType())},
                        {"Duration", a.getDuration() + " hour(s)"}
                }), g);

        g.gridx = 1;
        content.add(createInfoCard("People and Payment",
                new String[][]{
                        {"Customer", resolveUserName(a.getCustomerId())},
                        {"Technician", resolveUserName(a.getTechnicianId())},
                        {"Counter Staff", resolveUserName(a.getCounterStaffId())},
                        {"Amount", getPaymentAmount(a)}
                }), g);

        g.gridx = 0;
        g.gridy = 1;
        g.gridwidth = 2;
        g.weighty = 1;
        content.add(createTextSections(a), g);

        root.add(content, BorderLayout.CENTER);

        JButton closeButton = createPrimaryButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(closeButton);
        root.add(footer, BorderLayout.SOUTH);

        dialog.add(root, BorderLayout.CENTER);
        dialog.setSize(760, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createTextSections(Appointment a) {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.addTab("Customer Comments", createRatingBannerPanel(getCustomerComments(a), "No customer comments yet."));
        tabs.addTab("Technician Notes", createTextPanel(buildTechNotes(a)));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        wrapper.add(tabs, BorderLayout.CENTER);
        return wrapper;
    }

    private JScrollPane createRatingBannerPanel(List<Comment> comments, String emptyText) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        List<Comment> technicianComments = new ArrayList<>();
        for (Comment c : comments) {
            if (currentTechnician.getId().equals(c.getTechnicianId())) {
                technicianComments.add(c);
            }
        }

        if (technicianComments.isEmpty()) {
            JLabel emptyLabel = new JLabel(emptyText);
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            emptyLabel.setForeground(new Color(120, 120, 120));
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            panel.add(emptyLabel);
        } else {
            for (Comment comment : technicianComments) {
                panel.add(createRatingBanner(
                        comment.getRating(),
                        comment.getContent(),
                        "Customer Rating",
                        "Appointment " + comment.getAppointmentId()
                ));
                panel.add(Box.createVerticalStrut(10));
            }
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        return scrollPane;
    }
    
    private JScrollPane createTextPanel(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setBackground(Color.WHITE);
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private JPanel createRatingBanner(int rating, String content, String title, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(ratingBgColor(rating));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ratingBorderColor(rating)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        scorePanel.setOpaque(false);
        scorePanel.setPreferredSize(new Dimension(96, 0));

        JLabel score = new JLabel(rating + "/5");
        score.setFont(new Font("Segoe UI", Font.BOLD, 28));
        score.setForeground(ratingTextColor(rating));
        score.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(ratingLabel(rating));
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(ratingTextColor(rating));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        scorePanel.add(score);
        scorePanel.add(Box.createVerticalStrut(2));
        scorePanel.add(label);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title + "  |  " + subtitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea body = new JTextArea(content == null || content.trim().isEmpty() ? "No comment text available." : content);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        body.setForeground(new Color(50, 50, 50));
        body.setBackground(ratingBgColor(rating));
        body.setBorder(null);
        body.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(body);

        card.add(scorePanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }
    
    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(NAVY_BLUE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 36));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(NAVY_BLUE.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(NAVY_BLUE);
            }
        });
        
        return button;
    }

    private String getPaymentAmount(Appointment a) {
        for (Payment payment : paymentDAO.readAll()) {
            if (payment.getAppointmentId().equals(a.getId())) {
                return String.format("RM %.2f (PAID)", payment.getAmount());
            }
        }
        return String.format("RM %.2f", a.getAmount());
    }

    private JPanel createDetailsHeader(Appointment a) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JLabel title = new JLabel("Appointment " + a.getId());
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(NAVY_BLUE);
        panel.add(title, BorderLayout.WEST);
        
        JLabel badge = createStatusBadge(getDisplayStatus(a));
        badge.setAlignmentY(Component.CENTER_ALIGNMENT);
        panel.add(badge, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createInfoCard(String title, String[][] rows) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(NAVY_BLUE);
        card.add(titleLabel, BorderLayout.NORTH);

        JPanel rowPanel = new JPanel(new GridLayout(rows.length, 2, 12, 10));
        rowPanel.setBackground(Color.WHITE);
        for (String[] row : rows) {
            JLabel key = new JLabel(row[0]);
            key.setFont(new Font("Segoe UI", Font.BOLD, 11));
            key.setForeground(new Color(100, 116, 139));
            JLabel value = new JLabel(row[1]);
            value.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            value.setForeground(new Color(31, 41, 55));
            rowPanel.add(key);
            rowPanel.add(value);
        }
        card.add(rowPanel, BorderLayout.CENTER);
        return card;
    }

    private JLabel createStatusBadge(String status) {
        JLabel label = new JLabel(status);
        label.setOpaque(true);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));
        
        if ("COMPLETED".equals(status)) {
            label.setBackground(new Color(34, 197, 94));
            label.setForeground(Color.WHITE);
        } else if ("ASSIGNED".equals(status)) {
            label.setBackground(new Color(59, 130, 246));
            label.setForeground(Color.WHITE);
        } else if ("CANCELLED".equals(status)) {
            label.setBackground(new Color(220, 38, 38)); // Red color for cancelled
            label.setForeground(Color.WHITE);
        } else {
            label.setBackground(new Color(234, 179, 8));
            label.setForeground(Color.WHITE);
        }
        
        return label;
    }
    
    private String ratingLabel(int rating) {
        if (rating >= 5) return "Excellent";
        if (rating >= 4) return "Good";
        if (rating >= 3) return "Average";
        return "Needs Review";
    }

    private Color ratingTextColor(int rating) {
        if (rating >= 4) return new Color(22, 101, 52);
        if (rating == 3) return new Color(146, 64, 14);
        return new Color(153, 27, 27);
    }

    private Color ratingBgColor(int rating) {
        if (rating >= 4) return new Color(240, 253, 244);
        if (rating == 3) return new Color(255, 251, 235);
        return new Color(254, 242, 242);
    }

    private Color ratingBorderColor(int rating) {
        if (rating >= 4) return new Color(187, 247, 208);
        if (rating == 3) return new Color(253, 230, 138);
        return new Color(254, 202, 202);
    }
        
    private JPanel createUpdateSummary(Appointment a) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel title = new JLabel("Update " + a.getId());
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(NAVY_BLUE);

        JLabel details = new JLabel(a.getDate() + " | " + a.getStartTime() + " | " + a.getServiceType() + " | " + resolveUserName(a.getCustomerId()));
        details.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        details.setForeground(new Color(100, 116, 139));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(details);

        panel.add(text, BorderLayout.WEST);
        panel.add(createStatusBadge(getDisplayStatus(a)), BorderLayout.EAST);
        return panel;
    }
    
    private void showUpdateJobDialog(Appointment a) {
        if (a == null) return;
        
        String status = getDisplayStatus(a);
        boolean hasNote = hasTechNote(a.getId());
        
        if ("COMPLETED".equals(status) && hasNote) {
            JOptionPane.showMessageDialog(this, 
                "This job has already been completed with service notes.\n\n"
                + "Status: COMPLETED\n"
                + "Feedback: Already submitted\n\n"
                + "No further updates are allowed for this appointment.", 
                "Cannot Update", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        if ("COMPLETED".equals(status) && !hasNote) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "This appointment is marked as COMPLETED but has no service notes yet.\n\n"
                + "Would you like to add service notes now?",
                "Add Service Notes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        List<Feedback> allNotes = techNoteDAO.readAll();
        Feedback existingNote = findExistingNote(allNotes, a);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(createUpdateSummary(a));
        panel.add(Box.createVerticalStrut(15));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(31, 41, 55));
        
        JComboBox<String> statusDropdown = new JComboBox<>(new String[]{"ASSIGNED", "COMPLETED"});
        statusDropdown.setSelectedItem(a.getStatus().toString());
        statusDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusDropdown.setPreferredSize(new Dimension(150, 32));
        
        if ("COMPLETED".equals(status)) {
            statusDropdown.setSelectedItem("COMPLETED");
            statusDropdown.setEnabled(false);
        }
        
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(statusDropdown);
        statusPanel.add(Box.createHorizontalGlue());
        
        panel.add(statusPanel);
        panel.add(Box.createVerticalStrut(15));

        JPanel notesContainer = new JPanel(new BorderLayout(0, 10));
        notesContainer.setBackground(Color.WHITE);
        notesContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel notesTitle = new JLabel("Service Notes (Optional)");
        notesTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        notesTitle.setForeground(NAVY_BLUE);
        notesContainer.add(notesTitle, BorderLayout.NORTH);

        JTextArea notesArea = new JTextArea(8, 50);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        if (existingNote != null && existingNote.getContent() != null) {
            notesArea.setText(existingNote.getContent());
            notesArea.setForeground(Color.BLACK);
        } else {
            notesArea.setText("");
        }

        JPanel templatePanel = createTemplatePanel(notesArea);
        templatePanel.setPreferredSize(new Dimension(0, 70)); 
        notesContainer.add(templatePanel, BorderLayout.CENTER);
        
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Service Notes",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.PLAIN, 11)
        ));
        notesScroll.setPreferredSize(new Dimension(450, 150));
        notesContainer.add(notesScroll, BorderLayout.SOUTH);
        
        panel.add(notesContainer);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(PANEL_BG);
        
        JButton saveBtn = new JButton("Save Updates");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.setBackground(new Color(34, 197, 94));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setPreferredSize(new Dimension(120, 38));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelBtn.setBackground(new Color(156, 163, 175));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setPreferredSize(new Dimension(100, 38));
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(buttonPanel);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Job - " + a.getId(), true);
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.setSize(550, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        saveBtn.addActionListener(e -> {
            String notes = notesArea.getText().trim();
            // Notes are now optional - no validation required
            saveJobUpdate(a, existingNote, notes, statusDropdown);
            dialog.dispose();
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        dialog.setVisible(true);
    }
    
    private JPanel createTemplatePanel(JTextArea notesArea) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Quick Templates (Optional - Click to insert)",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11)
        ));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttonPanel.setBackground(new Color(248, 250, 252));
        
        String[] templates = {
            "Engine oil changed", "Brake pads checked", "Tire pressure adjusted",
            "Battery tested", "Air filter cleaned", "Diagnostic scan performed",
            "Coolant topped up", "No issues found"
        };
        
        for (String template : templates) {
            JButton btn = new JButton(template);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(31, 41, 55));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setFocusPainted(false);
            
            btn.addActionListener(e -> {
                String currentText = notesArea.getText();
                if (currentText.isEmpty()) {
                    notesArea.setText(template);
                    notesArea.setForeground(Color.BLACK);
                } else {
                    notesArea.append(", " + template);
                }
            });
            
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(new Color(230, 230, 230));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    btn.setBackground(Color.WHITE);
                }
            });
            
            buttonPanel.add(btn);
        }
        
        JScrollPane scrollPane = new JScrollPane(buttonPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(new Color(248, 250, 252));
        scrollPane.setPreferredSize(new Dimension(0, 55));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private void saveJobUpdate(Appointment appointment, Feedback existingNote, String content, JComboBox<String> statusDropdown) {
        // Notes are optional - can be empty
        boolean noteSaved = true;
        
        // Only save note if content is not empty
        if (content != null && !content.isEmpty()) {
            if (existingNote != null) {
                existingNote.setContent(content);
                noteSaved = techNoteDAO.update(existingNote);
            } else {
                Feedback note = new Feedback();
                note.setAppointmentId(appointment.getId());
                note.setTechnicianId(currentTechnician.getId());
                note.setContent(content);
                note.setDate(DateUtils.getCurrentDate());
                noteSaved = techNoteDAO.save(note);
            }
        }

        boolean appointmentSaved = true;
        if (statusDropdown.isEnabled()) {
            AppointmentStatus selectedStatus = AppointmentStatus.valueOf(statusDropdown.getSelectedItem().toString());
            appointment.setStatus(selectedStatus);
            appointmentSaved = appointmentDAO.update(appointment);
        }

        if (noteSaved && appointmentSaved) {
            JOptionPane.showMessageDialog(this, "Job updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadJobs();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update job.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Feedback findExistingNote(List<Feedback> allNotes, Appointment appointment) {
        for (Feedback note : allNotes) {
            if (appointment.getId().equals(note.getAppointmentId())
                    && currentTechnician.getId().equals(note.getTechnicianId())) {
                return note;
            }
        }
        return null;
    }

    private List<Comment> getCustomerComments(Appointment appointment) {
        List<Comment> comments = new ArrayList<>();
        for (Comment c : commentDAO.readAll()) {
            if (appointment.getId().equals(c.getAppointmentId()) 
                    && currentTechnician.getId().equals(c.getTechnicianId())) {
                comments.add(c);
            }
        }
        return comments;
    }

    private String getAppointmentRatingText(Appointment appointment) {
        int rating = getAppointmentRating(appointment);
        return rating > 0 ? rating + "/5" : "-";
    }

    private int getAppointmentRating(Appointment appointment) {
        int total = 0;
        int count = 0;
        for (Comment comment : commentDAO.readAll()) {
            if (appointment.getId().equals(comment.getAppointmentId()) 
                    && currentTechnician.getId().equals(comment.getTechnicianId())) {
                total += comment.getRating();
                count++;
            }
        }
        return count == 0 ? 0 : Math.round((float) total / count);
    }
        
    private String buildTechNotes(Appointment appointment) {
        StringBuilder builder = new StringBuilder();
        for (Feedback n : techNoteDAO.readAll()) {
            if (appointment.getId().equals(n.getAppointmentId())
                    && currentTechnician.getId().equals(n.getTechnicianId())) {
                builder.append(n.getContent()).append("\n\n");
            }
        }
        return builder.length() == 0 ? "No service notes yet." : builder.toString().trim();
    }
    
    private String resolveUserName(String userId) {
        if (userId == null || userId.trim().isEmpty()) return "-";
        User user = userDAO.findById(userId);
        return user != null ? user.getFullName() : userId;
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    class JobTableModel extends AbstractTableModel {
        private List<Appointment> appointments = new ArrayList<>();
        private final String[] columns = {"ID", "Date", "Time", "Service", "Status", "Rating", "Feedback"};

        public void setAppointments(List<Appointment> apps) {
            this.appointments = apps;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return appointments.size(); }
        @Override
        public int getColumnCount() { return columns.length; }
        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Appointment a = appointments.get(row);
            switch (col) {
                case 0: return a.getId();
                case 1: return a.getDate();
                case 2: return a.getStartTime();
                case 3: return a.getServiceType();
                case 4: return getDisplayStatus(a);
                case 5: return getAppointmentRatingText(a);
                case 6: return getTechNotePreview(a.getId());
                default: return "";
            }
        }

        public Appointment getAppointmentAt(int row) {
            if (row >= 0 && row < appointments.size()) return appointments.get(row);
            return null;
        }
    }

    private class PaddedCellRenderer extends DefaultTableCellRenderer {
        private final boolean statusColumn;

        private PaddedCellRenderer(boolean statusColumn) {
            this.statusColumn = statusColumn;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(JLabel.LEFT);
            label.setFont(new Font("Segoe UI", statusColumn ? Font.BOLD : Font.PLAIN, 12));
            
            if (statusColumn && value instanceof String) {
                if ("COMPLETED".equals(value)) {
                    label.setForeground(new Color(34, 197, 94));
                } else if ("ASSIGNED".equals(value)) {
                    label.setForeground(new Color(59, 130, 246));
                } else if ("CANCELLED".equals(value)) {
                    label.setForeground(new Color(220, 38, 38)); // Red for cancelled
                } else {
                    label.setForeground(new Color(234, 179, 8));
                }
            } else if (!statusColumn && !isSelected) {
                label.setForeground(new Color(31, 41, 55));
            }
            
            label.setBackground(isSelected ? TABLE_SELECTION : Color.WHITE);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
                    BorderFactory.createEmptyBorder(12, 18, 12, 10)
            ));
            return label;
        }
    }
}