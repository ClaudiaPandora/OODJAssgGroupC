package ui.manager;

import dao.UserDAO;
import enums.Mode;
import enums.UserRole;
import models.User;
import ui.common.BasePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class StaffManagementPanel extends BasePanel {

    private static final Color TEXT_DARK = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color CARD_BORDER = new Color(221, 225, 231);
    private static final Color TABLE_HEADER_BG = new Color(244, 246, 250);
    private static final Color TABLE_SELECTION = new Color(232, 240, 254);
    private static final Color ROW_SEPARATOR = new Color(231, 235, 240);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color RED = new Color(220, 38, 38);

    private UserDAO userDAO;
    private JTable staffTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private JComboBox<String> roleFilter;
    private JLabel statsLabel;
    private JLabel emptyLabel;
    private JPanel tableSwitcher;
    private JButton editButton;
    private JButton deleteButton;
    private JButton addButton;
    private JButton refreshButton;

    public StaffManagementPanel() {
        this.userDAO = new UserDAO();
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeComponents();
        setupLayout();
        addEventHandlers();
        loadStaffData();
    }

    @Override
    protected void initializeComponents() {
        String[] columns = {"ID", "Name", "Role", "Email", "Phone", "Username"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        staffTable = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        rowSorter.setSortsOnUpdates(false);
        for (int i = 0; i < columns.length; i++) {
            rowSorter.setSortable(i, false);
        }
        staffTable.setRowSorter(rowSorter);

        staffTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        staffTable.setRowHeight(54);
        staffTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        staffTable.setFillsViewportHeight(true);
        staffTable.setFocusable(false);
        staffTable.setRowSelectionAllowed(true);
        staffTable.setColumnSelectionAllowed(false);
        staffTable.setShowVerticalLines(false);
        staffTable.setShowHorizontalLines(false);
        staffTable.setGridColor(ROW_SEPARATOR);
        staffTable.setIntercellSpacing(new Dimension(0, 0));
        staffTable.setSelectionBackground(TABLE_SELECTION);
        staffTable.setSelectionForeground(TEXT_DARK);
        staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = staffTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_DARK);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                                                           boolean isSelected, boolean hasFocus, 
                                                           int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                
                label.setHorizontalAlignment(JLabel.LEFT);
                label.setVerticalAlignment(JLabel.CENTER);
                label.setBackground(TABLE_HEADER_BG);
                label.setForeground(TEXT_DARK);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
                
                return label;
            }
        };

        for (int i = 0; i < staffTable.getColumnModel().getColumnCount(); i++) {
            staffTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        
        staffTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        staffTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        staffTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        staffTable.getColumnModel().getColumn(3).setPreferredWidth(250);
        staffTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        staffTable.getColumnModel().getColumn(5).setPreferredWidth(140);
        staffTable.setIntercellSpacing(new Dimension(0, 0));

        for (int i = 0; i < 6; i++) {
            staffTable.getColumnModel().getColumn(i).setCellRenderer(new PaddedCellRenderer(i == 2));
        }

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(9, 12, 9, 12)
        ));

        roleFilter = new JComboBox<>(new String[]{"All Roles", "Manager", "Counter Staff", "Technician"});
        roleFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        roleFilter.setFocusable(false);

        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(TEXT_MUTED);

        emptyLabel = new JLabel("No staff members match the current filters.");
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emptyLabel.setForeground(TEXT_MUTED);
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Create buttons (always clickable)
        editButton = createActionButton("Edit", BLUE);
        deleteButton = createActionButton("Delete", RED);
        addButton = createStyledButton("+ Add Staff", NAVY_BLUE);
        refreshButton = createStyledButton("Refresh", GREEN);
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

        JLabel titleLabel = new JLabel("Staff Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBlock.add(titleLabel);
        titleRow.add(titleBlock, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(PANEL_BG);

        actions.add(addButton);
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

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setPreferredSize(new Dimension(430, 34));

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(TEXT_DARK);
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JPanel filterPanel = new JPanel(new BorderLayout(8, 0));
        filterPanel.setBackground(Color.WHITE);

        JLabel filterLabel = new JLabel("Role");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterLabel.setForeground(TEXT_DARK);
        filterPanel.add(filterLabel, BorderLayout.WEST);
        filterPanel.add(roleFilter, BorderLayout.CENTER);
        filterPanel.setPreferredSize(new Dimension(250, 34));

        controlsPanel.add(searchPanel);
        controlsPanel.add(filterPanel);
        toolbar.add(controlsPanel, BorderLayout.WEST);

        return toolbar;
    }

    private JPanel createTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createLineBorder(CARD_BORDER));

        JScrollPane scrollPane = new JScrollPane(staffTable);
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
        
        // Left side - stats label
        footer.add(statsLabel, BorderLayout.WEST);
        
        // Right side - action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(PANEL_BG);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        footer.add(buttonPanel, BorderLayout.EAST);
        
        return footer;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 10, 8, 10));  
        button.setPreferredSize(new Dimension(90, 34));  
        
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
        button.setPreferredSize(new Dimension(100, 34));
        
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
    
    private void loadStaffData() {
        tableModel.setRowCount(0);
        userDAO = new UserDAO();
        List<User> allUsers = userDAO.readAll();

        addStaffRowsForRole(allUsers, UserRole.MANAGER);
        addStaffRowsForRole(allUsers, UserRole.COUNTER_STAFF);
        addStaffRowsForRole(allUsers, UserRole.TECHNICIAN);

        applyFilters();
        updateStatsLabel();
        staffTable.revalidate();
        staffTable.repaint();
    }

    private void addStaffRowsForRole(List<User> users, UserRole role) {
        for (User user : users) {
            if (user.getRole() == role) {
                tableModel.addRow(new Object[]{
                        user.getId(),
                        user.getFullName(),
                        user.getRoleDisplayName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getUsername()
                });
            }
        }
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedRole = String.valueOf(roleFilter.getSelectedItem());

        rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String rowRole = String.valueOf(entry.getValue(2));
                boolean roleMatches = "All Roles".equals(selectedRole) || selectedRole.equals(rowRole);

                boolean searchMatches = search.isEmpty();
                for (int i = 0; i < 6 && !searchMatches; i++) {
                    Object value = entry.getValue(i);
                    searchMatches = value != null && value.toString().toLowerCase().contains(search);
                }

                return roleMatches && searchMatches;
            }
        });

        updateTableVisibility();
    }

    private void updateTableVisibility() {
        if (tableSwitcher == null) {
            return;
        }

        CardLayout layout = (CardLayout) tableSwitcher.getLayout();
        layout.show(tableSwitcher, staffTable.getRowCount() == 0 ? "EMPTY" : "TABLE");
    }

    private void updateStatsLabel() {
        List<User> staff = userDAO.readAll();
        long managerCount = staff.stream().filter(u -> u.getRole() == UserRole.MANAGER).count();
        long counterCount = staff.stream().filter(u -> u.getRole() == UserRole.COUNTER_STAFF).count();
        long techCount = staff.stream().filter(u -> u.getRole() == UserRole.TECHNICIAN).count();
        long totalStaff = managerCount + counterCount + techCount;

        statsLabel.setText(String.format(
                "Total Staff: %d    Managers: %d    Counter Staff: %d    Technicians: %d",
                totalStaff, managerCount, counterCount, techCount, staffTable.getRowCount()
        ));
    }
    
    private User getSelectedUser() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        
        int modelRow = staffTable.convertRowIndexToModel(selectedRow);
        String userId = (String) tableModel.getValueAt(modelRow, 0);
        
        return userDAO.findById(userId);
    }

    private void openRegistrationFrame() {
        StaffFormPanel formPanel = new StaffFormPanel(
                (Frame) SwingUtilities.getWindowAncestor(this),
                Mode.CREATE,
                null,
                this::loadStaffData
        );
        formPanel.setVisible(true);
    }

    private void showEditStaffDialog() {
        User user = getSelectedUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a staff member to edit.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        StaffFormPanel formPanel = new StaffFormPanel(
                (Frame) SwingUtilities.getWindowAncestor(this),
                Mode.UPDATE,
                user,
                this::loadStaffData
        );
        formPanel.setVisible(true);
    }

    private void deleteStaff() {
        User user = getSelectedUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a staff member to delete.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (user.getRole() == UserRole.MANAGER && countManagers() <= 1) {
            JOptionPane.showMessageDialog(this,
                    "At least one manager account must remain in the system.",
                    "Delete Not Allowed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete " + user.getFullName() + "?\nThis action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = userDAO.delete(user.getId());

            if (success) {
                loadStaffData();
                JOptionPane.showMessageDialog(this,
                        user.getFullName() + " has been deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete staff member.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private long countManagers() {
        return userDAO.readAll().stream()
                .filter(user -> user.getRole() == UserRole.MANAGER)
                .count();
    }

    @Override
    protected void addEventHandlers() {
        staffTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showEditStaffDialog();
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtersChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtersChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtersChanged();
            }

            private void filtersChanged() {
                applyFilters();
                updateStatsLabel();
            }
        });

        roleFilter.addActionListener(e -> {
            applyFilters();
            updateStatsLabel();
        });
        
        refreshButton.addActionListener(e -> {
            loadStaffData();
            JOptionPane.showMessageDialog(this, "Staff list has been refreshed.",
                    "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
        });
        
        addButton.addActionListener(e -> openRegistrationFrame());
        editButton.addActionListener(e -> showEditStaffDialog());
        deleteButton.addActionListener(e -> deleteStaff());
    }

    private class PaddedCellRenderer extends DefaultTableCellRenderer {
        private final boolean roleColumn;

        private PaddedCellRenderer(boolean roleColumn) {
            this.roleColumn = roleColumn;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );

            label.setHorizontalAlignment(JLabel.LEFT);
            label.setFont(new Font("Segoe UI", roleColumn ? Font.BOLD : Font.PLAIN, 13));
            label.setForeground(roleColumn ? NAVY_BLUE : TEXT_DARK);
            label.setBackground(isSelected ? TABLE_SELECTION : Color.WHITE);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
                    BorderFactory.createEmptyBorder(0, 18, 0, 0)
            ));
            return label;
        }
    }
}