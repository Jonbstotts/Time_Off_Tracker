package com.timeofftracker.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Application-specific presentation helpers.
 *
 * This class does not theme Swing controls. FlatLaf owns the look and feel.
 * The only fixed colors here are semantic calendar categories whose meaning
 * must remain stable across themes.
 */
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

    /**
     * Returns a card that derives its surface color from the active FlatLaf
     * theme whenever Swing updates the component UI.
     */
    public static JPanel card(LayoutManager layout) {
        return new SurfacePanel(layout);
    }

    public static Color surfaceColor() {
        Color surface = UIManager.getColor("TextField.background");
        if (surface == null) surface = UIManager.getColor("Panel.background");
        return surface != null ? surface : new Color(242, 242, 242);
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.putClientProperty("FlatLaf.style", "font: bold +12");
        return label;
    }

    public static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.putClientProperty("FlatLaf.style", "font: bold +4");
        return label;
    }

    /**
     * Uses Swing's disabled-label rendering so the active look and feel owns
     * the muted foreground color and updates it automatically on theme change.
     */
    public static <T extends JComponent> T muted(T component) {
        component.setEnabled(false);
        return component;
    }

    /** Standard FlatLaf button with only typography/spacing customized. */
    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty("FlatLaf.style", "font: bold; margin: 8,16,8,16");
        return button;
    }

    private static final class SurfacePanel extends JPanel {
        SurfacePanel(LayoutManager layout) {
            super(layout);
            setOpaque(true);
            setBorder(new EmptyBorder(16, 18, 16, 18));
        }

        @Override
        public void updateUI() {
            super.updateUI();
            setBackground(surfaceColor());
        }
    }
}
