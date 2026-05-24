package ui.technician;

import dao.AppointmentDAO;
import enums.AppointmentStatus;
import models.Appointment;
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

public class MySchedulePanel extends BasePanel {

    private final User currentTechnician;
    private AppointmentDAO appointmentDAO;

    private JComboBox<String> dateCombo;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JLabel statusLabel;

    private final Color CARD_BORDER = new Color(226, 232, 240);
    private final Color LIGHT_BG = new Color(248, 250, 252);
    private final Color TABLE_HEADER_BG = new Color(244, 246, 250);
    private final Color ROW_SEPARATOR = new Color(231, 235, 240);
    private final Color AVAILABLE_COLOR = new Color(34, 197, 94);
    private final Color BUSY_COLOR = new Color(220, 38, 38);
    private final Color NAVY = new Color(31, 66, 99);

    public MySchedulePanel(User currentTechnician) {
        this.currentTechnician = currentTechnician;
        this.appointmentDAO = new AppointmentDAO();

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
        dateCombo = new JComboBox<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i <= 30; i++) {
            LocalDate date = today.plusDays(i);
            String label = date.format(formatter);

            if (i == 0) {
                label += "  (Today)";
            } else if (i == 1) {
                label += "  (Tomorrow)";
            }

            dateCombo.addItem(label);
        }

        dateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateCombo.setBackground(Color.WHITE);
        dateCombo.setPreferredSize(new Dimension(220, 36));

        tableModel = new DefaultTableModel(buildColumns(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        scheduleTable = new JTable(tableModel);
        setupTableStyle();

        refreshButton = createStyledButton("Refresh", new Color(34, 197, 94));
        statusLabel = new JLabel("Showing schedule for: " + today.format(formatter));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 116, 139));
    }

    private String[] buildColumns() {
        List<String> columns = new ArrayList<>();
        columns.add("Technician");

        for (int hour = 9; hour <= 17; hour++) {
            columns.add(String.format("%02d:00", hour));
        }

        return columns.toArray(new String[0]);
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

        scheduleTable.setDefaultRenderer(Object.class, new SlotCellRenderer());
        scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(180);

        for (int i = 1; i < scheduleTable.getColumnCount(); i++) {
            scheduleTable.getColumnModel().getColumn(i).setPreferredWidth(72);
        }
    }

    @Override
    protected void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(PANEL_BG);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_BG);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        headerPanel.setPreferredSize(new Dimension(0, 50));

        JLabel titleLabel = new JLabel("My Schedule");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(PANEL_BG);
        rightPanel.add(refreshButton);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(14));

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

        JButton viewButton = createStyledButton("View Schedule", NAVY);
        viewButton.addActionListener(e -> loadSchedule());

        filterCard.add(dateLabel);
        filterCard.add(dateCombo);
        filterCard.add(viewButton);
        filterCard.add(statusLabel);

        mainPanel.add(filterCard);
        mainPanel.add(Box.createVerticalStrut(14));

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        legendPanel.setBackground(PANEL_BG);
        legendPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        legendPanel.add(makeLegendDot(AVAILABLE_COLOR));
        legendPanel.add(makeLegendText("Free"));
        legendPanel.add(Box.createHorizontalStrut(8));
        legendPanel.add(makeLegendDot(BUSY_COLOR));
        legendPanel.add(makeLegendText("Busy"));

        mainPanel.add(legendPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));

        JScrollPane tableScrollPane = new JScrollPane(scheduleTable);
        tableScrollPane.setBorder(null);
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(tableScrollPane, BorderLayout.CENTER);

        mainPanel.add(tableCard);

        JScrollPane outerScrollPane = new JScrollPane(mainPanel);
        outerScrollPane.setBorder(null);
        outerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        outerScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(outerScrollPane, BorderLayout.CENTER);
    }

    private JLabel makeLegendDot(Color color) {
        JLabel dot = new JLabel("*");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dot.setForeground(color);
        return dot;
    }

    private JLabel makeLegendText(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(55, 65, 81));
        return label;
    }

    private void loadSchedule() {
        tableModel.setRowCount(0);

        String rawDate = (String) dateCombo.getSelectedItem();
        if (rawDate == null) {
            return;
        }

        String selectedDate = rawDate.trim().split("\\s")[0];
        List<Appointment> allAppointments = appointmentDAO.readAll();
        int[] hours = {9, 10, 11, 12, 13, 14, 15, 16, 17};

        Object[] row = new Object[1 + hours.length];
        row[0] = currentTechnician.getFullName();

        for (int i = 0; i < hours.length; i++) {
            boolean busy = isBusy(selectedDate, hours[i], allAppointments);
            row[i + 1] = busy ? "BUSY" : "FREE";
        }

        tableModel.addRow(row);
        updateStatusLabel(selectedDate);
    }

    private boolean isBusy(String date, int slotHour, List<Appointment> allAppointments) {
        for (Appointment appointment : allAppointments) {
            if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                continue;
            }

            if (!currentTechnician.getId().equals(appointment.getTechnicianId())) {
                continue;
            }

            if (!date.equals(appointment.getDate())) {
                continue;
            }

            try {
                String[] timeParts = appointment.getStartTime().split(":");
                int appointmentStartHour = Integer.parseInt(timeParts[0]);
                int appointmentEndHour = appointmentStartHour + appointment.getDuration();

                if (appointmentStartHour < slotHour + 1 && appointmentEndHour > slotHour) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    private void updateStatusLabel(String selectedDate) {
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd MMM yyyy");

        try {
            LocalDate date = LocalDate.parse(selectedDate, inputFormat);
            statusLabel.setText("Showing schedule for: " + date.format(displayFormat));
        } catch (Exception e) {
            statusLabel.setText("Showing schedule for: " + selectedDate);
        }
    }

    @Override
    protected void addEventHandlers() {
        refreshButton.addActionListener(e -> {
            appointmentDAO = new AppointmentDAO();
            loadSchedule();

            JOptionPane.showMessageDialog(
                    this,
                    "Schedule refreshed successfully.",
                    "Refreshed",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

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

    private class SlotCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            Component component = super.getTableCellRendererComponent(
                    table,
                    value,
                    false,
                    false,
                    row,
                    column
            );

            JLabel label = (JLabel) component;
            label.setHorizontalAlignment(column == 0 ? JLabel.LEFT : JLabel.CENTER);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
                    BorderFactory.createEmptyBorder(6, column == 0 ? 14 : 4, 6, 4)
            ));
            label.setFont(new Font("Segoe UI", column == 0 ? Font.BOLD : Font.PLAIN, 12));

            if (column == 0) {
                label.setBackground(LIGHT_BG);
                label.setForeground(new Color(31, 41, 55));
            } else if ("BUSY".equals(value)) {
                label.setBackground(new Color(254, 226, 226));
                label.setForeground(BUSY_COLOR);
                label.setText("Busy");
            } else if ("FREE".equals(value)) {
                label.setBackground(new Color(220, 252, 231));
                label.setForeground(AVAILABLE_COLOR);
                label.setText("Free");
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(new Color(31, 41, 55));
            }

            return label;
        }
    }
}