package ui.manager;

import javax.swing.*;
import java.awt.*;

public class FeedbackPanel extends JPanel {

    public FeedbackPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Feedbacks - Coming Soon", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}