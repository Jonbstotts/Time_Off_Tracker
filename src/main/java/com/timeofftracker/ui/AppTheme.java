package com.timeofftracker.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class AppTheme {
    private AppTheme() {}

    public static final Color VACATION = new Color(78, 121, 167);
    public static final Color ETO = new Color(225, 120, 68);
    public static final Color HOLIDAY = new Color(89, 161, 79);
    public static final Color LIMITED_SERVICE = new Color(176, 122, 161);
    public static final Color WORKING_HOLIDAY = new Color(237, 201, 72);
    public static final Color TODAY = new Color(89, 161, 79);

    public static Color colorFor(com.timeofftracker.model.TimeOffType type) {
        return switch (type) {
            case VACATION -> VACATION;
            case ETO -> ETO;
            case HOLIDAY -> HOLIDAY;
            case LIMITED_SERVICE -> LIMITED_SERVICE;
            case WORKING_HOLIDAY -> WORKING_HOLIDAY;
        };
    }

    public static Color textColorFor(com.timeofftracker.model.TimeOffType type) {
        return ThemeManager.contrastText(colorFor(type));
    }

    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.putClientProperty("FlatLaf.style", "arc: 18; borderWidth: 0");
        p.putClientProperty(ThemeManager.ROLE, ThemeManager.ROLE_SURFACE);
        p.setBackground(ThemeManager.surfaceColor());
        p.setForeground(ThemeManager.textColor());
        p.setOpaque(true);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));
        return p;
    }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.putClientProperty("FlatLaf.style", "font: bold +12");
        return l;
    }

    public static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.putClientProperty("FlatLaf.style", "font: bold +4");
        return l;
    }

    public static <T extends JComponent> T muted(T component) {
        component.putClientProperty(ThemeManager.ROLE, ThemeManager.ROLE_MUTED);
        component.setForeground(ThemeManager.mutedTextColor());
        return component;
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.putClientProperty("FlatLaf.style", "arc: 12; font: bold; margin: 8,16,8,16");
        b.putClientProperty(ThemeManager.ROLE, ThemeManager.ROLE_PRIMARY);
        b.setBackground(ThemeManager.palette().accent());
        b.setForeground(ThemeManager.contrastText(ThemeManager.palette().accent()));
        return b;
    }
}
