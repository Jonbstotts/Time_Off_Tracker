package com.timeofftracker.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public final class ThemeManager {
    public enum Theme { LIGHT, DARK }

    private static final Preferences PREFS = Preferences.userNodeForPackage(ThemeManager.class);
    private static final String KEY_THEME = "theme";

    private ThemeManager() {}

    public static Theme savedTheme() {
        try {
            return Theme.valueOf(PREFS.get(KEY_THEME, Theme.LIGHT.name()));
        } catch (IllegalArgumentException ex) {
            return Theme.LIGHT;
        }
    }

    public static void applySavedTheme() {
        applyTheme(savedTheme(), false);
    }

    public static void applyTheme(Theme theme, boolean persist) {
        if (theme == Theme.DARK) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        installDefaults();
        if (persist) PREFS.put(KEY_THEME, theme.name());

        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            window.invalidate();
            window.validate();
            window.repaint();
        }
    }

    public static boolean isDark() {
        return savedTheme() == Theme.DARK;
    }

    private static void installDefaults() {
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.width", 12);
    }
}
