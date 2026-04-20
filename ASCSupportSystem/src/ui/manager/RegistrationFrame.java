package ui.manager;

import enums.Mode;
import ui.common.BaseFrame;

import java.awt.*;

public class RegistrationFrame extends BaseFrame {
    
    public RegistrationFrame() {
        setTitle("Register New Staff");
        setSize(500, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    @Override
    protected void initializeComponents() {
        // Form is handled by StaffFormPanel
    }
    
    @Override
    protected void setupLayout() {
        StaffFormPanel formPanel = new StaffFormPanel(this, Mode.CREATE, null, () -> dispose());
        formPanel.setVisible(true);
    }
    
    @Override
    protected void addEventHandlers() {
        // Form is handled by StaffFormPanel
    }
}