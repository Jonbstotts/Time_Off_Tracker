package com.timeofftracker.app;

import com.timeofftracker.ui.ThemeManager;

import javax.swing.*;

public final class TimeOffTrackerApp {
    private TimeOffTrackerApp() {}

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Time Off Tracker");

        SwingUtilities.invokeLater(() -> {
            try {
                ThemeManager.applySavedTheme();
            } catch (Exception ignored) {
                ThemeManager.applyTheme(ThemeManager.Theme.FLAT_LIGHT, false);
            }
            new TimeOffTrackerFrame().setVisible(true);
        });
    }
}
