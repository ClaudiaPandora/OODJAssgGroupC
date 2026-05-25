package ui.counter;

import dao.AppointmentDAO;
import dao.UserDAO;
import enums.AppointmentStatus;
import models.Appointment;
import models.Technician;
import models.User;
import ui.common.BasePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TechnicianSchedulePanel extends BasePanel {

    private AppointmentDAO appointmentDAO;
    private UserDAO userDAO;

    private JComboBox<String> dateCombo;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JLabel statusLabel;

    private List<Technician> technicians;

    private final Color CARD_BORDER    = new Color(226, 232, 240);
    private final Color LIGHT_BG       = new Color(248, 250, 252);
    private final Color TABLE_HEADER_BG = new Color(244, 246, 250);
    private final Color ROW_SEPARATOR  = new Color(231, 235, 240);
    private final Color AVAILABLE_COLOR = new Color(34, 197, 94);
    private final Color BUSY_COLOR      = new Color(220, 38, 38);
    private final Color NAVY            = new Color(31, 66, 99);

    public TechnicianSchedulePanel() {
        this.appointmentDAO = new AppointmentDAO();
        this.userDAO = new UserDAO();

        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeComponents();
        setupLayout();
        addEventHandlers();
        loadSchedule();
    }

    @Override
    protected void initializeComponents() {
        technicians = userDAO.readTechnicians();

        // Date combo — today + next 30 days
        dateCombo = new JComboBox<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i <= 30; i++) {
            LocalDate d = today.plusDays(i);
            String label = d.format(fmt);
            if (i == 0) label += "  (Today)";
            else if (i == 1) label += "  (Tomorrow)";
            dateCombo.addItem(label);
        }
        dateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateCombo.setBackground(Color.WHITE);
        dateCombo.setPreferredSize(new Dimension(220, 36));

        // Table: Technician name + time slots 09:00–17:00 every hour
        String[] columns = buildColumns();
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        scheduleTable = new JTable(tableModel);
        setupTableStyle();

        refreshButton = createStyledButton("Refresh", new Color(34, 197, 94));
        statusLabel = new JLabel("Showing schedule for: " + today.format(fmt));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 116, 139));
    }

    private String[] buildColumns() {
        // Technician | 09:00 | 10:00 | ... | 17:00
        List<String> cols = new ArrayList<>();
        cols.add("Technician");
        for (int h = 9; h <= 17; h++) {
            cols.add(String.format("%02d:00", h));
        }
        return cols.toArray(new String[0]);
    }

    private void setupTableStyle() {
        scheduleTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        scheduleTable.setRowHeight(46);
        scheduleTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        scheduleTable.setFillsViewportHeight(true);
        scheduleTable.setFocusable(false);
        scheduleTable.setRowSelectionAllowed(false);
        scheduleTable.setShowVerticalLines(true);
        scheduleTable.setShowHorizontalLines(false);
        scheduleTable.setGridColor(ROW_SEPARATOR);
        scheduleTable.setIntercellSpacing(new Dimension(1, 0));

        JTableHeader header = scheduleTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(new Color(31, 41, 55));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));

        // Custom renderer for colour-coded cells
        scheduleTable.setDefaultRenderer(Object.class, new SlotCellRenderer());

        // Column widths: technician column wider, time slots equal
        scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        for (int i = 1; i < scheduleTable.getColumnCount(); i++) {
            scheduleTable.getColumnModel().getColumn(i).setPreferredWidth(72);
        }
    }

    @Override
    protected void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(PANEL_BG);

        // ── Header ──
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_BG);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        headerPanel.setPreferredSize(new Dimension(0, 50));

        JLabel titleLabel = new JLabel("Technician Schedule");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(PANEL_BG);
        rightPanel.add(refreshButton);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(14));

        // ── Filter bar ──
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        filterCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel dateLabel = new JLabel("Select Date:");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dateLabel.setForeground(new Color(31, 41, 55));
        filterCard.add(dateLabel);
        filterCard.add(dateCombo);

        JButton viewBtn = createStyledButton("View Schedule", NAVY);
        viewBtn.addActionListener(e -> loadSchedule());
        filterCard.add(viewBtn);
        filterCard.add(statusLabel);

        mainPanel.add(filterCard);
        mainPanel.add(Box.createVerticalStrut(14));

        // ── Legend ──
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        legendPanel.setBackground(PANEL_BG);
        legendPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        legendPanel.add(makeLegendDot(AVAILABLE_COLOR));
        legendPanel.add(makeLegendText("Free"));
        legendPanel.add(Box.createHorizontalStrut(8));
        legendPanel.add(makeLegendDot(BUSY_COLOR));
        legendPanel.add(makeLegendText("Busy"));
        legendPanel.add(Box.createHorizontalStrut(8));
        legendPanel.add(makeLegendDot(new Color(156, 163, 175)));
        legendPanel.add(makeLegendText("Outside working hours (09:00 – 17:00)"));
        mainPanel.add(legendPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // ── Schedule table ──
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));

        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(tableCard);

        JScrollPane outerScroll = new JScrollPane(mainPanel);
        outerScroll.setBorder(null);
        outerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        outerScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(outerScroll, BorderLayout.CENTER);
    }

    private JLabel makeLegendDot(Color c) {
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dot.setForeground(c);
        return dot;
    }

    private JLabel makeLegendText(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(55, 65, 81));
        return lbl;
    }

    private void loadSchedule() {
        tableModel.setRowCount(0);

        // Parse selected date (strip the "(Today)" etc)
        String raw = (String) dateCombo.getSelectedItem();
        if (raw == null) return;
        String selectedDate = raw.trim().split("\\s")[0]; // "yyyy-MM-dd"

        List<Appointment> allAppts = appointmentDAO.readAll();

        // Hours 09–17 inclusive (9 slots)
        int[] hours = {9, 10, 11, 12, 13, 14, 15, 16, 17};

        for (Technician tech : technicians) {
            Object[] row = new Object[1 + hours.length];
            row[0] = tech.getFullName();

            for (int i = 0; i < hours.length; i++) {
                int slotHour = hours[i];
                boolean busy = isBusy(tech.getId(), selectedDate, slotHour, allAppts);
                row[1 + i] = busy ? "BUSY" : "FREE";
            }
            tableModel.addRow(row);
        }

        if (technicians.isEmpty()) {
            tableModel.addRow(new Object[]{"No technicians found", "", "", "", "", "", "", "", "", ""});
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter display = DateTimeFormatter.ofPattern("dd MMM yyyy");
        try {
            LocalDate d = LocalDate.parse(selectedDate, fmt);
            statusLabel.setText("Showing schedule for: " + d.format(display));
        } catch (Exception ex) {
            statusLabel.setText("Showing schedule for: " + selectedDate);
        }
    }

    /**
     * Returns true if the technician has a non-cancelled appointment that
     * overlaps with the 1-hour window starting at slotHour on selectedDate.
     */
    private boolean isBusy(String techId, String date, int slotHour, List<Appointment> allAppts) {
        for (Appointment a : allAppts) {
            if (a.getStatus() == AppointmentStatus.CANCELLED) continue;
            if (!techId.equals(a.getTechnicianId())) continue;
            if (!date.equals(a.getDate())) continue;

            try {
                String[] timeParts = a.getStartTime().split(":");
                int apptStartHour = Integer.parseInt(timeParts[0]);
                int apptDuration  = a.getDuration(); // hours
                int apptEndHour   = apptStartHour + apptDuration; // exclusive end

                // Slot occupies [slotHour, slotHour+1)
                // Overlap if apptStart < slotHour+1 AND apptEnd > slotHour
                if (apptStartHour < slotHour + 1 && apptEndHour > slotHour) {
                    return true;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    @Override
    protected void addEventHandlers() {
        refreshButton.addActionListener(e -> {
            this.appointmentDAO = new AppointmentDAO();
            this.userDAO = new UserDAO();
            this.technicians = userDAO.readTechnicians();
            loadSchedule();
            JOptionPane.showMessageDialog(this,
                "Schedule refreshed successfully.",
                "Refreshed", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(color.darker()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(color); }
        });
        return btn;
    }

    // ── Custom cell renderer: GREEN = FREE, RED = BUSY, grey = technician name ──
    private class SlotCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {

            Component c = super.getTableCellRendererComponent(
                    table, value, false, false, row, col);
            JLabel lbl = (JLabel) c;
            lbl.setHorizontalAlignment(col == 0 ? JLabel.LEFT : JLabel.CENTER);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
                BorderFactory.createEmptyBorder(6, col == 0 ? 14 : 4, 6, 4)
            ));
            lbl.setFont(new Font("Segoe UI",
                col == 0 ? Font.BOLD : Font.PLAIN, 12));

            if (col == 0) {
                // Technician name column
                lbl.setBackground(LIGHT_BG);
                lbl.setForeground(new Color(31, 41, 55));
            } else if ("BUSY".equals(value)) {
                lbl.setBackground(new Color(254, 226, 226)); // light red
                lbl.setForeground(BUSY_COLOR);
                lbl.setText("Busy");
            } else if ("FREE".equals(value)) {
                lbl.setBackground(new Color(220, 252, 231)); // light green
                lbl.setForeground(AVAILABLE_COLOR);
                lbl.setText("Free");
            } else {
                lbl.setBackground(Color.WHITE);
                lbl.setForeground(new Color(31, 41, 55));
            }

            return lbl;
        }
    }
}
