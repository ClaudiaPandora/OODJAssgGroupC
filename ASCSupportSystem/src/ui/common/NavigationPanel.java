package ui.common;

import enums.UserRole;
import models.User;
import ui.dashboard.DashboardFrame;

import javax.swing.*;
import java.awt.*;

public class NavigationPanel extends JPanel {
    
    private Color PANEL_BG = new Color(249, 249, 247);
    private Color NAVY_BLUE = new Color(0, 0, 128);
    private Color ACTIVE_BG = new Color(0, 0, 100);
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private DashboardFrame parentFrame;
    private JButton activeButton = null;
    
    public NavigationPanel(User user, CardLayout cardLayout, JPanel contentPanel, DashboardFrame parentFrame) {
        this.cardLayout = cardLayout;
        this.contentPanel = contentPanel;
        this.parentFrame = parentFrame;
        
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(180, 180, 180)));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        initializeComponents(user);
    }
    
    private void initializeComponents(User user) {
        add(Box.createVerticalStrut(20));
        
        add(createSectionLabel("MAIN MENU"));
        
        add(createNavButton("Overview", "OVERVIEW"));
        
        if (user.getRole() == UserRole.MANAGER) {
            add(createNavButton("Manage Staff", "STAFF"));
            add(createNavButton("Set Prices", "PRICES"));
            add(createNavButton("Feedbacks & Comments", "FEEDBACKS"));
            add(createNavButton("Reports", "REPORTS"));
        } else if (user.getRole() == UserRole.COUNTER_STAFF) {
            add(createNavButton("Manage Customers", "CUSTOMERS"));
            add(createNavButton("Appointments", "APPOINTMENTS"));
            add(createNavButton("Payments", "PAYMENTS"));
            add(createNavButton("Tech Schedule", "TECH_SCHEDULE"));
        } else if (user.getRole() == UserRole.TECHNICIAN) {
            add(createNavButton("My Jobs", "JOBS"));
            add(createNavButton("My Schedule", "MY_SCHEDULE"));
        } else if (user.getRole() == UserRole.CUSTOMER) {
            add(createNavButton("Service History", "HISTORY"));
            add(createNavButton("My Feedback", "MY_FEEDBACK"));
            add(createNavButton("My Comment History", "MY_COMMENT_HISTORY"));
        }
        
        add(Box.createVerticalStrut(30));
        
        add(createSectionLabel("ACCOUNT"));
        add(createNavButton("Profile", "PROFILE"));
        
        add(Box.createVerticalGlue());
        
        add(Box.createVerticalStrut(10));
        add(createLogoutButton());
        add(Box.createVerticalStrut(20));
    }
    
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(100, 100, 100));
        label.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JButton createNavButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setFocusPainted(false);

        button.setBackground(PANEL_BG);
        button.setForeground(new Color(50, 50, 50));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);

        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        button.addActionListener(e -> {
            setActiveButton(panelName);
            cardLayout.show(contentPanel, panelName);
        });
        
        return button;
    }
    
    private JButton createLogoutButton() {
        JButton button = new JButton("Logout");
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setFocusPainted(false);

        button.setBackground(PANEL_BG);
        button.setForeground(new Color(180, 0, 0));
        button.setOpaque(true);
        button.setContentAreaFilled(true);

        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        button.addActionListener(e -> parentFrame.logout());
        
        return button;
    }
    
    public void setActiveButton(String panelName) {
        if (activeButton != null) {
            activeButton.setBackground(PANEL_BG);
            activeButton.setForeground(new Color(50, 50, 50));
            activeButton.setOpaque(true);
            activeButton.setContentAreaFilled(true);
            activeButton.setBorderPainted(false);
            activeButton.repaint();
        }
        
        for (Component c : getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton) c;
                if (btn.getText().equals(getButtonTextForPanel(panelName))) {
                    btn.setBackground(NAVY_BLUE);
                    btn.setForeground(Color.WHITE);
                    btn.setOpaque(true);
                    btn.setContentAreaFilled(true);
                    btn.setBorderPainted(false);
                    btn.repaint();
                    activeButton = btn;
                    break;
                }
            }
        }
    }
    
    private String getButtonTextForPanel(String panelName) {
        switch (panelName) {
            case "OVERVIEW":            return "Overview";
            case "STAFF":               return "Manage Staff";
            case "PRICES":              return "Set Prices";
            case "FEEDBACKS":           return "Feedbacks & Comments";
            case "REPORTS":             return "Reports";
            case "CUSTOMERS":           return "Manage Customers";
            case "APPOINTMENTS":        return "Appointments";
            case "PAYMENTS":            return "Payments";
            case "TECH_SCHEDULE":       return "Tech Schedule";
            case "JOBS":                return "My Jobs";
            case "MY_SCHEDULE":         return "My Schedule";
            case "HISTORY":             return "Service History";
            case "MY_FEEDBACK":         return "My Feedback";
            case "MY_COMMENT_HISTORY":  return "My Comment History";
            case "PROFILE":             return "Profile";
            default:                    return "";
        }
    }
}