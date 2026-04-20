package ui.common;

import javax.swing.*;
import java.awt.*;

public abstract class BaseFrame extends JFrame {
    
    protected Color NAVY_BLUE = new Color(0, 0, 128);
    protected Color YELLOW = new Color(252, 202, 12);
    protected Color PANEL_BG = new Color(249, 249, 247);
    protected Color BORDER_DARK = new Color(176, 176, 176);
    protected Color BORDER_LIGHT = new Color(255, 255, 255);
    
    public BaseFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }
    
    protected abstract void initializeComponents();
    protected abstract void setupLayout();
    protected abstract void addEventHandlers();
}