package ui.technician;

import dao.AppointmentDAO;
import dao.CommentDAO;
import dao.FeedbackDAO;
import dao.UserDAO;
import dao.TechNoteDAO;
import models.TechNote;
import enums.AppointmentStatus;
import models.Appointment;
import models.Comment;
import models.Feedback;
import models.User;
import ui.common.BasePanel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.DefaultCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import javax.swing.table.DefaultTableCellRenderer;

public class MyJobsPanel extends BasePanel {
    
    private User currentTechnician;
    private AppointmentDAO appointmentDAO;
    private UserDAO userDAO;
    private FeedbackDAO feedbackDAO;
    private CommentDAO commentDAO;
    private TechNoteDAO techNoteDAO;
    
    private JTable jobsTable;
    private JobTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JButton resetButton;
    
    public MyJobsPanel(User technician) {
        this.currentTechnician = technician;
        this.appointmentDAO = new AppointmentDAO();
        this.userDAO = new UserDAO();
        this.feedbackDAO = new FeedbackDAO();
        this.commentDAO = new CommentDAO();
        this.techNoteDAO = new TechNoteDAO();
        
        setBackground(PANEL_BG);
        initializeComponents();
        setupLayout();
        addEventHandlers();
        loadJobs();
        jobsTable.getTableHeader().setReorderingAllowed(false);
    }
    
    @Override
    protected void initializeComponents() {

        tableModel = new JobTableModel();
        jobsTable = new JTable(tableModel);

        jobsTable.setRowHeight(40);
        jobsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jobsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        jobsTable.getTableHeader().setBackground(new Color(240, 240, 240));
        jobsTable.setSelectionBackground(new Color(0, 51, 153)); 
        jobsTable.setSelectionForeground(Color.WHITE);
        jobsTable.setShowGrid(true);
        jobsTable.setGridColor(new Color(220, 220, 220));
        
        resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        resetButton.setBackground(new Color(230, 230, 230));
        resetButton.setFocusPainted(false);
        resetButton.setBorderPainted(false);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        jobsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                Appointment a = tableModel.getAppointmentAt(row);

                if (!isSelected) { 
                	if (a.getStatus() == AppointmentStatus.ASSIGNED || a.getStatus() == AppointmentStatus.PENDING) {
                	    c.setForeground(new Color(255, 140, 0));
                	} else if (a.getStatus() == AppointmentStatus.COMPLETED) {
                	    c.setForeground(new Color(34, 197, 94));
                	} else {
                	    c.setForeground(Color.GRAY);
                	}
                }

                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER); 

                return c;
            }
    
        });

        jobsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        jobsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        jobsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        jobsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        jobsTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        jobsTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        jobsTable.getColumnModel().getColumn(6).setPreferredWidth(160);

        jobsTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        jobsTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor());

        searchField = new JTextField("Type appointment ID", 20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setForeground(Color.GRAY);
        searchField.setBackground(Color.WHITE);
        searchField.setCaretColor(Color.BLACK);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        statusFilter = new JComboBox<>(new String[]{"All Status", "ASSIGNED", "PENDING", "COMPLETED", "PAID"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.setBackground(Color.WHITE);
        statusFilter.setForeground(Color.BLACK);
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_BG);
        
        JLabel titleLabel = new JLabel("My Jobs");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(PANEL_BG);
        titlePanel.add(titleLabel);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PANEL_BG);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 2),
                BorderFactory.createLineBorder(BORDER_DARK, 2)
        ));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(statusLabel);
        searchPanel.add(statusFilter);
        searchPanel.add(resetButton);

        add(searchPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(jobsTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            BorderFactory.createLineBorder(BORDER_DARK, 1)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadJobs() {
        loadJobsWithFilter(null, null);
    }
    
    private void loadJobsWithFilter(String searchId, String status) {

        List<Appointment> allAppointments = appointmentDAO.findByTechnicianId(currentTechnician.getId());
        List<Appointment> filtered = new ArrayList<>();

        // Placeholder
        if (searchId != null && searchId.equals("Type appointment ID")) {
            searchId = "";
        }

        for (Appointment a : allAppointments) {

            // Search
            if (searchId != null && !searchId.trim().isEmpty()) {
                if (!a.getId().toLowerCase().contains(searchId.toLowerCase())) {
                    continue;
                }
            }

            // Status filter
            if (status != null && !status.equals("All Status")) {
                if (!a.getStatus().toString().equals(status)) {
                    continue;
                }
            }

            filtered.add(a);
        }

        tableModel.setAppointments(filtered);
    }
    
    private void viewDetails(Appointment a) {
        if (a == null) return;

        User customer = userDAO.findById(a.getCustomerId());
        String customerName = customer != null ? customer.getFullName() : "Unknown";

        List<Comment> allComments = commentDAO.readAll();
        StringBuilder customerComments = new StringBuilder();

        for (Comment c : allComments) {
            if (a.getId().equals(c.getAppointmentId())) {
                if (c.getCounterStaffId() == null) {
                    customerComments.append("Comment: ")
                                    .append(c.getContent())
                                    .append("\n");
                }
            }
        }
        if (customerComments.length() == 0) {
            customerComments.append("No comments.");
        }


        // FEEDBACK → feedback.txt
        List<Feedback> allFeedbacks = feedbackDAO.readAll();
        StringBuilder customerFeedbacks = new StringBuilder();

        for (Feedback f : allFeedbacks) {
            if (a.getId().equals(f.getAppointmentId())) {
                customerFeedbacks.append("Rating: ")
                                 .append(f.getRating())
                                 .append("/5\n");
                customerFeedbacks.append("Feedback: ")
                                 .append(f.getContent())
                                 .append("\n");
            }
        }
        if (customerFeedbacks.length() == 0) {
            customerFeedbacks.append("No feedback.");
        }
        
        // Technician Notes
        List<TechNote> allNotes = techNoteDAO.readAll();
        StringBuilder techNotes = new StringBuilder();
        for (TechNote n : allNotes) {
            if (a.getId().equals(n.getAppointmentId()) &&
                currentTechnician.getId().equals(n.getTechnicianId())) {
                techNotes.append(n.getContent()).append("\n");
            }
        }
        if (techNotes.length() == 0) techNotes.append("No technician notes yet.");

        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setContentType("text/html");

        textPane.setText(
            "<html>" +
            "=======================================<br>" +
            "<b>APPOINTMENT DETAILS</b><br>" +
            "=======================================<br><br>" +

            "ID: " + a.getId() + "<br>" +
            "Date: " + a.getDate() + "<br>" +
            "Time: " + a.getStartTime() + "<br>" +
            "Service: " + a.getServiceType() + "<br>" +
            "Duration: " + a.getDuration() + " hour(s)<br>" +
            "Status: " + a.getStatus() + "<br>" +
            "Amount: RM " + String.format("%.2f", a.getAmount()) + "<br>" +
            "Customer: " + customerName + "<br><br>" +

            "---------------------------------------<br>" +
            "<b>CUSTOMER COMMENTS:</b><br>" +
            customerComments.toString().replace("\n","<br>") + "<br><br>" +

            "---------------------------------------<br>" +
            "<b>CUSTOMER FEEDBACK:</b><br>" +
            customerFeedbacks.toString().replace("\n","<br>") + "<br><br>" +

            "---------------------------------------<br>" +
            "<b>TECHNICIAN NOTES:</b><br>" +
            techNotes.toString().replace("\n","<br>") +

            "</html>"
        );

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(550, 500));

        JOptionPane.showMessageDialog(this, scrollPane,
                "Appointment Details", JOptionPane.INFORMATION_MESSAGE);
    }


    private void addServiceNotes(Appointment a) {
        if (a == null) return;

        List<TechNote> allNotes = techNoteDAO.readAll();
        String existingContent = null;
        for (TechNote n : allNotes) {
            if (a.getId().equals(n.getAppointmentId()) &&
                currentTechnician.getId().equals(n.getTechnicianId())) {
                existingContent = n.getContent();
                break;
            }
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel infoLabel = new JLabel("Edit service notes for: " + a.getId());
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JComponent statusComponent;

        if (a.getStatus() == AppointmentStatus.PAID) {
            JLabel fixedStatus = new JLabel("PAID");
            fixedStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
            fixedStatus.setForeground(Color.BLUE);

            JLabel reminder = new JLabel("This appointment is already PAID. Status cannot be changed.");
            reminder.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            reminder.setForeground(Color.GRAY);

            JPanel statusPanel = new JPanel(new BorderLayout());
            statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
            statusPanel.add(fixedStatus, BorderLayout.NORTH);
            statusPanel.add(reminder, BorderLayout.SOUTH);

            statusComponent = statusPanel;
        } else {

            String[] statuses = {"ASSIGNED", "PENDING", "COMPLETED"};
            JComboBox<String> statusDropdown = new JComboBox<>(statuses);
            statusDropdown.setSelectedItem(a.getStatus().toString());
            statusDropdown.setBorder(BorderFactory.createTitledBorder("Status"));
            statusComponent = statusDropdown;
        }

        panel.add(statusComponent, BorderLayout.NORTH);

        String[] quickOptions = {
            "Engine oil changed", "Brake pads checked", "Tire pressure adjusted",
            "Battery tested", "Air filter cleaned", "Diagnostic scan performed",
            "Coolant topped up", "No issues found"
        };
        JPanel quickPanel = new JPanel(new GridLayout(2, 4, 8, 8));
        quickPanel.setBorder(BorderFactory.createTitledBorder("Quick Templates"));
        quickPanel.setBackground(Color.WHITE);

        JTextArea textArea = new JTextArea(8, 40);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (existingContent != null) {
            textArea.setText(existingContent);
        }
        textArea.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));

        for (String option : quickOptions) {
            JButton btn = new JButton(option);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            btn.addActionListener(e -> {
                if (textArea.getText().isEmpty()) {
                    textArea.setText("• " + option);
                } else {
                    textArea.append("\n• " + option);
                }
            });
            quickPanel.add(btn);
        }

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Service Notes"));

        panel.add(quickPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        Object[] options = {"Save", "Cancel"};
        int result = JOptionPane.showOptionDialog(
            this, panel, "Edit Service Notes",
            JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
            null, options, options[0]
        );

        if (result == 0) {
            String content = textArea.getText().trim();
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Notes cannot be empty.");
                return;
            }

            boolean success;
            if (existingContent != null) {
                TechNote existing = allNotes.stream()
                    .filter(n -> a.getId().equals(n.getAppointmentId()) &&
                                 currentTechnician.getId().equals(n.getTechnicianId()))
                    .findFirst().orElse(null);
                if (existing != null) {
                    existing.setContent(content);
                    success = techNoteDAO.update(existing);
                } else {
                    success = false;
                }
            } else {
                TechNote note = new TechNote();
                note.setAppointmentId(a.getId());
                note.setTechnicianId(currentTechnician.getId());
                note.setContent(content);
                success = techNoteDAO.save(note);
            }

            if (success) {
                JOptionPane.showMessageDialog(this, "Service notes updated.");
                loadJobs();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save notes.");
            }
        }
    }

    
    @Override
    protected void addEventHandlers() {

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                loadJobsWithFilter(searchField.getText(), (String) statusFilter.getSelectedItem());
            }
        });

        statusFilter.addActionListener(e -> {
            loadJobsWithFilter(searchField.getText(), (String) statusFilter.getSelectedItem());
        });

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals("Type appointment ID")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText("Type appointment ID");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        resetButton.addActionListener(e -> {
            searchField.setText("Type appointment ID");
            searchField.setForeground(Color.GRAY);
            statusFilter.setSelectedIndex(0);
            loadJobs();
        });
    }
    
    class JobTableModel extends AbstractTableModel {
        private List<Appointment> appointments = new ArrayList<>();
        private final String[] columns = {"ID", "Date", "Time", "Service", "Status", "Amount (RM)", "Action"};
        
        public void setAppointments(List<Appointment> apps) {
            this.appointments = apps;
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return appointments.size();
        }
        
        @Override
        public int getColumnCount() {
            return columns.length;
        }
        
        @Override
        public String getColumnName(int col) {
            return columns[col];
        }
        
        @Override
        public Object getValueAt(int row, int col) {
            Appointment a = appointments.get(row);
            switch (col) {
                case 0: return a.getId();
                case 1: return a.getDate();
                case 2: return a.getStartTime();
                case 3: return a.getServiceType();
                case 4: return a.getStatus();
                case 5: return String.format("%.2f", a.getAmount());
                case 6: return "";
                default: return "";
            }
        }
        
        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 6;
        }
        
        @Override
        public Class<?> getColumnClass(int col) {
            return col == 6 ? JPanel.class : String.class;
        }
        
        public Appointment getAppointmentAt(int row) {
            if (row >= 0 && row < appointments.size()) {
                return appointments.get(row);
            }
            return null;
        }
    }
    
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JPanel panel;

        public ButtonRenderer() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
            panel.setBackground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            panel.removeAll();

            JButton viewBtn = createButton("View", new Color(59, 130, 246));
            JButton editBtn = createButton("Edit", new Color(168, 85, 247));

            panel.add(viewBtn);
            panel.add(editBtn);

            return panel;
        }

        private JButton createButton(String text, Color color) {
            JButton btn = new JButton(text);
            btn.setForeground(Color.WHITE);
            btn.setBackground(color);
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(100, 30));
            return btn;
        }
    }
    
    class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private int currentRow;

        public ButtonEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {

            panel.removeAll();
            currentRow = row;
            Appointment a = tableModel.getAppointmentAt(row);

            JButton viewBtn = createButton("View", new Color(59,130,246), e -> {
                fireEditingStopped();
                viewDetails(a);
            });

            JButton editBtn = createButton("Edit", new Color(168,85,247), e -> {
                fireEditingStopped();
                addServiceNotes(a);
            });

            panel.add(viewBtn);
            panel.add(editBtn);

            return panel;
        }

        private JButton createButton(String text, Color color, ActionListener action) {
            JButton btn = new JButton(text);
            btn.setForeground(Color.WHITE);
            btn.setBackground(color);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(100, 30));
            btn.addActionListener(action);
            return btn;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}