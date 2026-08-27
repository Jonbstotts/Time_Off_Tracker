package com.timeofftracker.app;

import com.timeofftracker.persistence.DatabaseManager;
import com.timeofftracker.persistence.TimeOffRepository;
import com.timeofftracker.persistence.YearRepository;
import com.timeofftracker.service.TimeOffService;
import com.timeofftracker.ui.MainPanel;
import com.timeofftracker.ui.SetupDialog;

import javax.swing.*;
import java.awt.*;
import java.time.Year;

public class TimeOffTrackerFrame extends JFrame {
    private final TimeOffService service;

    public TimeOffTrackerFrame() {
        super("Time Off Tracker");
        DatabaseManager db = new DatabaseManager();
        service = new TimeOffService(new TimeOffRepository(db), new YearRepository(db));

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 820));

        int currentYear = Year.now().getValue();
        if (service.configuredYears().isEmpty()) {
            SwingUtilities.invokeLater(() -> firstRunSetup(currentYear));
        } else {
            setContentPane(new MainPanel(this, service));
            sizeForCalendar();
        }
    }

    private void sizeForCalendar() {
        pack();
        Dimension preferred = getPreferredSize();
        int width = Math.max(1180, preferred.width);
        int height = Math.max(900, preferred.height);

        GraphicsConfiguration gc = getGraphicsConfiguration();
        if (gc != null) {
            Rectangle bounds = gc.getBounds();
            Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            int maxWidth = bounds.width - screenInsets.left - screenInsets.right - 40;
            int maxHeight = bounds.height - screenInsets.top - screenInsets.bottom - 40;
            width = Math.min(width, maxWidth);
            height = Math.min(height, maxHeight);
        }
        setSize(width, height);
        setLocationRelativeTo(null);
    }

    private void firstRunSetup(int year) {
        SetupDialog setup = new SetupDialog(this, year, null, true);
        setup.setVisible(true);
        if (setup.wasSaved()) {
            service.saveYearSettings(setup.getSettings());
            setContentPane(new MainPanel(this, service));
            sizeForCalendar();
            revalidate();
            repaint();
        } else {
            dispose();
        }
    }
}
