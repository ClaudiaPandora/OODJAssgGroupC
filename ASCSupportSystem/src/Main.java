import dao.ServiceDAO;
import enums.ServiceType;
import models.Service;
import ui.auth.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Initialize default service prices if needed
        initializeServices();
        
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
    
    private static void initializeServices() {
        ServiceDAO serviceDAO = new ServiceDAO();
        if (serviceDAO.readAll().isEmpty()) {
            serviceDAO.save(new Service(ServiceType.NORMAL, 100.0));
            serviceDAO.save(new Service(ServiceType.MAJOR, 300.0));
            System.out.println("Default service prices initialized.");
        }
    }
}