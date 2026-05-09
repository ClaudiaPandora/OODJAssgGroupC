package ui.dashboard;
import ui.customer.CustomerOverviewPanel;


import enums.UserRole;
import models.User;
import ui.auth.LoginFrame;
import ui.common.BaseFrame;
import ui.common.HeaderPanel;
import ui.common.NavigationPanel;
import ui.customer.CustomerFeedbackPanel;
import ui.customer.CustomerHistoryPanel;
import ui.manager.FeedbackPanel;
import ui.manager.PriceSettingsPanel;
import ui.manager.ReportsPanel;
import ui.manager.StaffManagementPanel;
import ui.technician.MyJobsPanel;
import ui.technician.TechnicianOverviewPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DashboardFrame extends BaseFrame {

    private User currentUser;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private NavigationPanel navPanel;

    public DashboardFrame(User user) {
        super();
        this.currentUser = user;

        setTitle("APU-ASC Dashboard - " + user.getFullName());
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        initializeComponents();
        setupLayout();
        addEventHandlers();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });
    }

    @Override
    protected void initializeComponents() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(PANEL_BG);
    }

    @Override
    protected void setupLayout() {
        HeaderPanel headerPanel = new HeaderPanel(currentUser);
        add(headerPanel, BorderLayout.NORTH);

        setJMenuBar(createMenuBar());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(1);
        splitPane.setDividerLocation(200);
        splitPane.setBorder(null);

        navPanel = new NavigationPanel(currentUser, cardLayout, contentPanel, this);
        splitPane.setLeftComponent(navPanel);

        setupContentPanels();

        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setBackground(PANEL_BG);
        rightWrapper.add(contentPanel, BorderLayout.CENTER);

        splitPane.setRightComponent(rightWrapper);

        add(splitPane, BorderLayout.CENTER);

        navPanel.setActiveButton("OVERVIEW");
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(240, 240, 240));
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_DARK));

        JMenu accountMenu = new JMenu("Account");
        accountMenu.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JMenuItem profileItem = new JMenuItem("Profile");
        profileItem.addActionListener(e -> {
            cardLayout.show(contentPanel, "PROFILE");
            navPanel.setActiveButton("PROFILE");
        });

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutItem.setForeground(new Color(200, 0, 0));
        logoutItem.addActionListener(e -> logout());

        accountMenu.add(profileItem);
        accountMenu.addSeparator();
        accountMenu.add(logoutItem);
        menuBar.add(accountMenu);

        return menuBar;
    }

    private void setupContentPanels() {
        contentPanel.removeAll();

        ProfilePanel profilePanel = new ProfilePanel(currentUser);
        contentPanel.add(profilePanel, "PROFILE");

        if (currentUser.getRole() == UserRole.TECHNICIAN) {
            contentPanel.add(new TechnicianOverviewPanel(currentUser), "OVERVIEW");
            contentPanel.add(new MyJobsPanel(currentUser), "JOBS");
        } else if (currentUser.getRole() == UserRole.MANAGER) {
            contentPanel.add(new OverviewPanel(currentUser), "OVERVIEW");
            contentPanel.add(new StaffManagementPanel(), "STAFF");
            contentPanel.add(new PriceSettingsPanel(), "PRICES");
            contentPanel.add(new FeedbackPanel(), "FEEDBACKS");
            contentPanel.add(new ReportsPanel(), "REPORTS");
        } else if (currentUser.getRole() == UserRole.CUSTOMER) {
            contentPanel.add(new CustomerOverviewPanel(currentUser), "OVERVIEW");
            contentPanel.add(new CustomerHistoryPanel(currentUser), "HISTORY");
            contentPanel.add(new CustomerFeedbackPanel(currentUser), "MY_FEEDBACK");
        } else {
            contentPanel.add(new OverviewPanel(currentUser), "OVERVIEW");
        }

        cardLayout.show(contentPanel, "OVERVIEW");
    }

    @Override
    protected void addEventHandlers() {
    }

    public void switchToPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
        navPanel.setActiveButton(panelName);
    }

    public void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            dispose();
        }
    }
}
