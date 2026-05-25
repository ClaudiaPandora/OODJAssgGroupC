package ui.customer;

import dao.CommentDAO;
import dao.UserDAO;
import models.Comment;
import models.User;
import ui.common.BasePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class CustomerCommentHistoryPanel extends BasePanel {

    private final User currentUser;
    private final CommentDAO commentDAO;
    private final UserDAO userDAO;

    private JTextField searchField;
    private JComboBox<String> targetFilter;
    private JButton applyButton;
    private JButton resetButton;
    private JButton refreshButton;

    private JTable commentsTable;
    private DefaultTableModel commentsTableModel;

    private final Color CARD_BORDER = new Color(221, 225, 231);
    private final Color TABLE_HEADER_BG = new Color(244, 246, 250);
    private final Color TABLE_SELECTION = new Color(232, 240, 254);
    private final Color ROW_SEPARATOR = new Color(231, 235, 240);
    private final Color GREEN = new Color(34, 197, 94);
    private final Color BLUE = new Color(59, 130, 246);

    public CustomerCommentHistoryPanel(User currentUser) {
        this.currentUser = currentUser;
        this.commentDAO = new CommentDAO();
        this.userDAO = new UserDAO();

        setBackground(PANEL_BG);
        initializeComponents();
        setupLayout();
        addEventHandlers();
        refreshCommentsTable();
    }

    @Override
    protected void initializeComponents() {
        searchField = new PlaceholderTextField("Please enter appointment ID");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(9, 12, 9, 12)
        ));

        targetFilter = new JComboBox<>(new String[]{
                "All Comments", "Technician Comments", "Counter Staff Comments"
        });
        targetFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        targetFilter.setFocusable(false);

        applyButton = createActionButton("Apply", BLUE);
        resetButton = createActionButton("Reset", new Color(156, 163, 175));
        refreshButton = createActionButton("Refresh", GREEN);

        commentsTableModel = new DefaultTableModel(
                new String[]{"Appointment ID", "Target", "Staff Name", "Rating", "Comment"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        commentsTable = createStyledTable(commentsTableModel);
        setupTableStyle(commentsTable);
        setTableColumnWidths();
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

        JPanel filterPanel = createFilterPanel();
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel tablePanel = createTablePanel(commentsTable);
        tablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(filterPanel);
        content.add(Box.createVerticalStrut(15));
        content.add(tablePanel);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(PANEL_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pagePanel.add(scrollPane, BorderLayout.CENTER);
        add(pagePanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL_BG);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(PANEL_BG);

        JLabel titleLabel = new JLabel("My Comments History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(titleLabel);

        textPanel.add(Box.createVerticalStrut(5));

        JLabel subtitleLabel = new JLabel("View your submitted ratings and comments by appointment.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(subtitleLabel);

        header.add(textPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(PANEL_BG);
        rightPanel.add(refreshButton);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(new Color(31, 41, 55));

        searchField.setPreferredSize(new Dimension(250, 34));
        searchField.setMinimumSize(new Dimension(200, 34));

        JLabel targetLabel = new JLabel("Target");
        targetLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        targetLabel.setForeground(new Color(31, 41, 55));

        panel.add(searchLabel);
        panel.add(searchField);
        panel.add(targetLabel);
        panel.add(targetFilter);
        panel.add(applyButton);
        panel.add(resetButton);

        return panel;
    }

    private JPanel createTablePanel(JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                new EmptyBorder(0, 0, 0, 0)
        ));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(45);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(new Color(31, 41, 55));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        return table;
    }

    private void setupTableStyle(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(45);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(ROW_SEPARATOR);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(new Color(31, 41, 55));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
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
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );
                label.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setBackground(TABLE_HEADER_BG);
                return label;
            }
        };

        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        table.setDefaultRenderer(Object.class, new TableCellRenderer());
    }

    private void setTableColumnWidths() {
        commentsTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        commentsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        commentsTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        commentsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        commentsTable.getColumnModel().getColumn(4).setPreferredWidth(420);
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setPreferredSize(new Dimension(110, 34));

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

    private void refreshCommentsTable() {
        commentsTableModel.setRowCount(0);

        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String filter = (String) targetFilter.getSelectedItem();
        List<Comment> comments = commentDAO.findByCustomerId(currentUser.getId());

        for (Comment comment : comments) {
            if (!matchesSearch(comment, query)) {
                continue;
            }
            if (!matchesTargetFilter(comment, filter)) {
                continue;
            }

            String target;
            String staffName;
            if (comment.getTechnicianId() != null && !comment.getTechnicianId().trim().isEmpty()) {
                target = "Technician";
                staffName = resolveUserName(comment.getTechnicianId());
            } else if (comment.getCounterStaffId() != null && !comment.getCounterStaffId().trim().isEmpty()) {
                target = "Counter Staff";
                staffName = resolveUserName(comment.getCounterStaffId());
            } else {
                target = "-";
                staffName = "-";
            }

            commentsTableModel.addRow(new Object[]{
                    comment.getAppointmentId(),
                    target,
                    staffName,
                    comment.getRating() + "/5",
                    comment.getContent()
            });
        }
    }

    private boolean matchesSearch(Comment comment, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return comment.getAppointmentId() != null
                && comment.getAppointmentId().toLowerCase().contains(query);
    }

    private boolean matchesTargetFilter(Comment comment, String filter) {
        if (filter == null || "All Comments".equals(filter)) {
            return true;
        }
        if ("Technician Comments".equals(filter)) {
            return comment.getTechnicianId() != null && !comment.getTechnicianId().trim().isEmpty();
        }
        if ("Counter Staff Comments".equals(filter)) {
            return comment.getCounterStaffId() != null && !comment.getCounterStaffId().trim().isEmpty();
        }
        return true;
    }

    private String resolveUserName(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return "-";
        }
        User user = userDAO.findById(userId);
        return user != null ? user.getFullName() : userId;
    }

    @Override
    protected void addEventHandlers() {
        applyButton.addActionListener(e -> refreshCommentsTable());

        resetButton.addActionListener(e -> {
            searchField.setText("");
            targetFilter.setSelectedIndex(0);
            refreshCommentsTable();
        });

        refreshButton.addActionListener(e -> {
            refreshCommentsTable();
            JOptionPane.showMessageDialog(this,
                    "Data has been refreshed from files.",
                    "Refresh Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        targetFilter.addActionListener(e -> refreshCommentsTable());
        searchField.addActionListener(e -> refreshCommentsTable());
    }

    private static class PlaceholderTextField extends JTextField {
        private final String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (getText() != null && !getText().isEmpty()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(156, 163, 175));
            g2.setFont(getFont());
            Insets insets = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, insets.left, y);
            g2.dispose();
        }
    }

    private class TableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            JLabel label = (JLabel) c;
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
                    BorderFactory.createEmptyBorder(12, 18, 12, 10)
            ));

            if (!isSelected) {
                label.setBackground(Color.WHITE);
                label.setForeground(new Color(31, 41, 55));
            } else {
                label.setBackground(TABLE_SELECTION);
                label.setForeground(new Color(31, 41, 55));
            }

            return label;
        }
    }
}