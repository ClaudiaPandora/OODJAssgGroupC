package ui.manager;

import dao.PriceHistoryDAO;
import dao.ServiceDAO;
import enums.ServiceType;
import models.PriceHistory;
import models.Service;
import models.User;
import ui.common.BasePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PriceSettingsPanel extends BasePanel {

    private final Color TEXT_DARK = new Color(31, 41, 55);
    private final Color TEXT_MUTED = new Color(107, 114, 128);
    private final Color CARD_BORDER = new Color(221, 225, 231);
    private final Color SOFT_BLUE = new Color(239, 246, 255);
    private final Color SUCCESS_GREEN = new Color(22, 163, 74);
    private final Color BORDER_LIGHT = new Color(229, 231, 235);
    private final Color BORDER_DARK = new Color(209, 213, 219);

    private ServiceDAO serviceDAO;
    private PriceHistoryDAO historyDAO;
    private JTextField normalPriceField;
    private JTextField majorPriceField;
    private JPanel historyContent;
    private JLabel normalCurrentLabel;
    private JLabel majorCurrentLabel;
    private User currentUser;

    public PriceSettingsPanel() {
        this.serviceDAO = new ServiceDAO();
        this.historyDAO = new PriceHistoryDAO();

        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeComponents();
        setupLayout();
        addEventHandlers();
        refreshData();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @Override
    protected void initializeComponents() {
        normalPriceField = createPriceField();
        majorPriceField = createPriceField();

        normalCurrentLabel = createMutedLabel("Current: RM 0");
        majorCurrentLabel = createMutedLabel("Current: RM 0");

        historyContent = new JPanel();
        historyContent.setLayout(new BoxLayout(historyContent, BoxLayout.Y_AXIS));
        historyContent.setBackground(Color.WHITE);
    }

    @Override
    protected void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(PANEL_BG);

        mainPanel.add(createHeaderPanel());
        mainPanel.add(Box.createVerticalStrut(15));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(PANEL_BG);
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        JPanel pricingColumn = new JPanel();
        pricingColumn.setLayout(new BoxLayout(pricingColumn, BoxLayout.Y_AXIS));
        pricingColumn.setBackground(PANEL_BG);

        JPanel cardsRow = new JPanel(new GridLayout(1, 2, 12, 0));
        cardsRow.setBackground(PANEL_BG);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        cardsRow.add(createPriceCard("Normal Service", "1 hour", ServiceType.NORMAL, normalPriceField, normalCurrentLabel));
        cardsRow.add(createPriceCard("Major Service", "3 hours", ServiceType.MAJOR, majorPriceField, majorCurrentLabel));

        pricingColumn.add(cardsRow);
        pricingColumn.add(Box.createVerticalStrut(12));
        pricingColumn.add(createRevenueImpactPanel());
        pricingColumn.add(Box.createVerticalStrut(12));
        pricingColumn.add(createPricingPolicyPanel());

        gbc.gridx = 0;
        gbc.weightx = 0.32;  
        gbc.insets = new Insets(0, 0, 0, 12);
        contentPanel.add(pricingColumn, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.85; 
        gbc.insets = new Insets(0, 12, 0, 0);
        contentPanel.add(createHistoryPanel(), gbc);

        mainPanel.add(contentPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("Service Pricing");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(NAVY_BLUE);
        panel.add(titleLabel, BorderLayout.WEST);

        JButton refreshButton = createStyledButton("Refresh", new Color(34, 197, 94));
        refreshButton.addActionListener(e -> {
            refreshData();
            JOptionPane.showMessageDialog(this,
                    "Price settings have been refreshed.",
                    "Refresh Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(refreshButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createPriceCard(String title, String duration, ServiceType type, JTextField field, JLabel currentLabel) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY_BLUE);
        header.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.WEST);

        JLabel durationLabel = new JLabel(duration);
        durationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        durationLabel.setForeground(new Color(230, 230, 230));
        header.add(durationLabel, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));

        currentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(currentLabel);
        content.add(Box.createVerticalStrut(12));

        JPanel fieldRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        fieldRow.setBackground(Color.WHITE);

        JLabel rmLabel = new JLabel("RM");
        rmLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        rmLabel.setForeground(TEXT_DARK);
        fieldRow.add(rmLabel);

        field.setPreferredSize(new Dimension(110, 34));
        fieldRow.add(field);
        content.add(fieldRow);
        content.add(Box.createVerticalStrut(14));

        JButton updateButton = createStyledButton("Update Price", NAVY_BLUE);
        updateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateButton.addActionListener(e -> updatePrice(type, field));
        content.add(updateButton);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createRevenueImpactPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70)); 
        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(248, 248, 253));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
            BorderFactory.createEmptyBorder(6, 12, 6, 12) 
        ));
        
        JLabel headerLabel = new JLabel("Revenue Impact Analysis");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        headerLabel.setForeground(NAVY_BLUE);
        header.add(headerLabel, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);
        
        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10)); 
        content.setBackground(Color.WHITE);
        
        JLabel infoLabel = new JLabel("Pricing affects monthly reports and overall profit margins. Review quarterly to stay competitive.");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setForeground(TEXT_MUTED);
        content.add(infoLabel);
        
        panel.add(content, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPricingPolicyPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(252, 202, 12, 50));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(252, 202, 12)),
            BorderFactory.createEmptyBorder(6, 12, 6, 12) 
        ));
        
        JLabel headerLabel = new JLabel("Pricing Policy & Terms");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        headerLabel.setForeground(NAVY_BLUE);
        header.add(headerLabel, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);
        
        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10)); 
        content.setBackground(new Color(255, 251, 235));
        
        JLabel infoLabel = new JLabel("New rates apply to future appointments only. Unpaid bookings keep their original price unless manually updated.");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setForeground(TEXT_MUTED);
        content.add(infoLabel);
        
        panel.add(content, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createHistoryPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SOFT_BLUE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel titleLabel = new JLabel("Price Change History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(NAVY_BLUE);
        header.add(titleLabel, BorderLayout.WEST);

        panel.add(header, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(historyContent);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setPreferredSize(new Dimension(0, 250));
        scrollPane.setMinimumSize(new Dimension(0, 250));

        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshData() {
        loadPrices();
        loadHistory();
    }

    private void loadPrices() {
        Service normal = serviceDAO.findByType(ServiceType.NORMAL);
        Service major = serviceDAO.findByType(ServiceType.MAJOR);

        double normalPrice = normal != null ? normal.getPrice() : 0;
        double majorPrice = major != null ? major.getPrice() : 0;

        normalPriceField.setText(formatPriceValue(normalPrice));
        majorPriceField.setText(formatPriceValue(majorPrice));
        normalCurrentLabel.setText("Current: RM " + formatPriceValue(normalPrice));
        majorCurrentLabel.setText("Current: RM " + formatPriceValue(majorPrice));
    }

    private void loadHistory() {
        historyContent.removeAll();
        List<PriceHistory> historyList = historyDAO.readAllSortedByDate();

        if (historyList.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setPreferredSize(new Dimension(0, 180));

            JLabel emptyLabel = createMutedLabel("No price changes recorded yet.");
            emptyPanel.add(emptyLabel);
            historyContent.add(emptyPanel);
        } else {
            for (PriceHistory history : historyList) {
            	JPanel entry = createHistoryEntry(history);
            	entry.setAlignmentX(Component.LEFT_ALIGNMENT);
            	historyContent.add(entry);
            	historyContent.add(Box.createVerticalStrut(10));            }
        }
        
        historyContent.add(Box.createVerticalGlue());
        historyContent.revalidate();
        historyContent.repaint();
    }

    private JPanel createHistoryEntry(PriceHistory history) {
        JPanel entry = new JPanel(new GridBagLayout());
        entry.setBackground(Color.WHITE);
        entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));  
        entry.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(238, 242, 247)),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)  
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.5;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 0); 
        
        JLabel serviceLabel = new JLabel(history.getServiceType() + " SERVICE");
        serviceLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        serviceLabel.setForeground(NAVY_BLUE);
        entry.add(serviceLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        
        JPanel priceRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        priceRow.setBackground(Color.WHITE);
        
        JLabel oldPrice = createMutedLabel("RM " + formatPriceValue(history.getOldPrice()));
        JLabel arrow = createMutedLabel("→");
        JLabel newPrice = new JLabel("RM " + formatPriceValue(history.getNewPrice()));
        newPrice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        newPrice.setForeground(SUCCESS_GREEN);
        
        priceRow.add(oldPrice);
        priceRow.add(arrow);
        priceRow.add(newPrice);
        entry.add(priceRow, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 0);
        
        JLabel dateLabel = createMutedLabel(history.getTimestamp());
        entry.add(dateLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        
        JLabel byLabel = createMutedLabel("by " + history.getChangedBy());
        entry.add(byLabel, gbc);
        
        return entry;
    }    

    private void updatePrice(ServiceType type, JTextField field) {
        Service existing = serviceDAO.findByType(type);
        double oldPrice = existing != null ? existing.getPrice() : 0;

        Double newPrice = parsePrice(field.getText());
        if (newPrice == null) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid numeric price.",
                    "Invalid Price",
                    JOptionPane.WARNING_MESSAGE);
            field.setText(formatPriceValue(oldPrice));
            return;
        }

        if (newPrice <= 0 || newPrice > 10000) {
            JOptionPane.showMessageDialog(this,
                    "Price must be greater than 0 and not more than RM 10,000.",
                    "Invalid Price",
                    JOptionPane.WARNING_MESSAGE);
            field.setText(formatPriceValue(oldPrice));
            return;
        }

        if (Double.compare(newPrice, oldPrice) == 0) {
            JOptionPane.showMessageDialog(this,
                    "Price is the same as the current value.",
                    "No Change",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Update %s service price?\n\nCurrent: RM %s\nNew: RM %s",
                        type.name(), formatPriceValue(oldPrice), formatPriceValue(newPrice)),
                "Confirm Price Update",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            field.setText(formatPriceValue(oldPrice));
            return;
        }

        Service service = new Service(type, newPrice);
        boolean saved = existing == null ? serviceDAO.save(service) : serviceDAO.update(service);

        if (!saved) {
            JOptionPane.showMessageDialog(this,
                    "Failed to update service price.",
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            field.setText(formatPriceValue(oldPrice));
            return;
        }

        String changedBy = currentUser != null ? currentUser.getFullName() : "System";
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        historyDAO.save(new PriceHistory(null, type, oldPrice, newPrice, changedBy, timestamp));

        refreshData();
        JOptionPane.showMessageDialog(this,
                type.name() + " service price updated successfully.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JTextField createPriceField() {
        JTextField field = new JTextField(8);
        field.setFont(new Font("Segoe UI", Font.BOLD, 16));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setForeground(TEXT_DARK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JLabel createMutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(TEXT_MUTED);
        return label;
    }

    private JPanel createCard() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
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
        button.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));  // Changed from 8,14 to 8,10
        button.setPreferredSize(new Dimension(90, 34));  // Added to match ReportsPanel
        
        // Store original color for hover effect
        Color originalColor = color;
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(originalColor.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }
    
    private Double parsePrice(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return null;
            }
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatPriceValue(double price) {
        if (price == Math.rint(price)) {
            return String.format("%.0f", price);
        }
        return String.format("%.2f", price);
    }

    @Override
    protected void addEventHandlers() {
    }
}