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
    public static final Color MUTED = new Color(120, 120, 120);

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
        return type == com.timeofftracker.model.TimeOffType.WORKING_HOLIDAY ? new Color(45, 45, 45) : Color.WHITE;
    }

    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.putClientProperty("FlatLaf.style", "arc: 18; borderWidth: 0; background: lighten(@background,3%)");
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

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.putClientProperty("FlatLaf.style", "arc: 12; font: bold; margin: 8,16,8,16");
        return b;
    }
}
