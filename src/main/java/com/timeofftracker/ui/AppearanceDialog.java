package com.timeofftracker.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

public class AppearanceDialog extends JDialog {
    private final JComboBox<ThemeManager.Theme> themeCombo = new JComboBox<>(ThemeManager.Theme.values());
    private final JCheckBox automatic = new JCheckBox("Automatically use seasonal and holiday themes");
    private final Map<ThemeManager.SeasonalEvent, JCheckBox> eventChecks = new EnumMap<>(ThemeManager.SeasonalEvent.class);
    private final PreviewPanel preview = new PreviewPanel();
    private final JLabel description = new JLabel();
    private boolean saved;

    public AppearanceDialog(Window owner) {
        super(owner, "Appearance", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        loadValues();
        pack();
        setMinimumSize(new Dimension(680, 620));
        setLocationRelativeTo(owner);
    }

    private JComponent buildContent() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(20, 22, 18, 22));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(AppTheme.title("Appearance & Themes"));
        JLabel sub = AppTheme.muted(new JLabel("Choose an everyday look or let Time Off Tracker switch themes automatically for holidays."));
        heading.add(Box.createVerticalStrut(5));
        heading.add(sub);
        root.add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel themeCard = AppTheme.card(new BorderLayout(12, 12));
        JPanel picker = new JPanel(new BorderLayout(10, 6));
        picker.setOpaque(false);
        picker.add(AppTheme.sectionTitle("Base theme"), BorderLayout.NORTH);
        picker.add(themeCombo, BorderLayout.CENTER);
        themeCard.add(picker, BorderLayout.NORTH);
        preview.setPreferredSize(new Dimension(580, 150));
        themeCard.add(preview, BorderLayout.CENTER);
        description.setBorder(new EmptyBorder(3, 0, 0, 0));
        themeCard.add(description, BorderLayout.SOUTH);
        center.add(themeCard);
        center.add(Box.createVerticalStrut(14));

        JPanel seasonalCard = AppTheme.card(new BorderLayout(10, 10));
        JPanel seasonalTop = new JPanel();
        seasonalTop.setOpaque(false);
        seasonalTop.setLayout(new BoxLayout(seasonalTop, BoxLayout.Y_AXIS));
        seasonalTop.add(AppTheme.sectionTitle("Automatic seasonal themes"));
        seasonalTop.add(Box.createVerticalStrut(7));
        seasonalTop.add(automatic);
        seasonalCard.add(seasonalTop, BorderLayout.NORTH);

        JPanel events = new JPanel(new GridLayout(0, 2, 8, 5));
        events.setOpaque(false);
        for (ThemeManager.SeasonalEvent event : ThemeManager.SeasonalEvent.values()) {
            JCheckBox box = new JCheckBox(event.displayName());
            box.setOpaque(false);
            eventChecks.put(event, box);
            events.add(box);
        }
        seasonalCard.add(events, BorderLayout.CENTER);
        JLabel note = AppTheme.muted(new JLabel("Seasonal themes temporarily override the base theme, then return to it automatically."));
        seasonalCard.add(note, BorderLayout.SOUTH);
        center.add(seasonalCard);

        root.add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton save = AppTheme.primaryButton("Save Appearance");
        save.addActionListener(e -> save());
        getRootPane().setDefaultButton(save);
        buttons.add(cancel);
        buttons.add(save);
        root.add(buttons, BorderLayout.SOUTH);

        themeCombo.addActionListener(e -> refreshPreview());
        automatic.addActionListener(e -> updateSeasonalEnabled());
        ThemeManager.applyThemeRoles(root);
        return root;
    }

    private void loadValues() {
        themeCombo.setSelectedItem(ThemeManager.savedTheme());
        automatic.setSelected(ThemeManager.automaticSeasonalThemes());
        for (var entry : eventChecks.entrySet()) entry.getValue().setSelected(ThemeManager.isEventEnabled(entry.getKey()));
        updateSeasonalEnabled();
        refreshPreview();
    }

    private void updateSeasonalEnabled() {
        boolean enabled = automatic.isSelected();
        for (JCheckBox box : eventChecks.values()) box.setEnabled(enabled);
    }

    private void refreshPreview() {
        ThemeManager.Theme theme = (ThemeManager.Theme) themeCombo.getSelectedItem();
        if (theme == null) return;
        description.setText("<html>" + theme.description() + "</html>");
        preview.setTheme(theme);
    }

    private void save() {
        ThemeManager.Theme theme = (ThemeManager.Theme) themeCombo.getSelectedItem();
        Map<ThemeManager.SeasonalEvent, Boolean> enabled = new EnumMap<>(ThemeManager.SeasonalEvent.class);
        for (var entry : eventChecks.entrySet()) enabled.put(entry.getKey(), entry.getValue().isSelected());
        ThemeManager.saveAppearance(theme == null ? ThemeManager.Theme.LIGHT : theme, automatic.isSelected(), enabled);
        saved = true;
        dispose();
    }

    public boolean wasSaved() { return saved; }

    private static class PreviewPanel extends JPanel {
        private ThemeManager.Theme theme = ThemeManager.Theme.LIGHT;
        PreviewPanel() { setOpaque(false); }
        void setTheme(ThemeManager.Theme theme) { this.theme = theme; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ThemeManager.Palette p = ThemeManager.paletteForPreview(theme);

            int w = getWidth(), h = getHeight();
            g2.setColor(p.background());
            g2.fillRoundRect(0, 5, w, h - 10, 18, 18);

            g2.setColor(p.surface());
            g2.fillRoundRect(18, 22, Math.max(150, w - 215), h - 44, 14, 14);

            g2.setColor(p.calendarCell());
            int cellY = 80;
            g2.fillRoundRect(34, cellY, 68, 34, 8, 8);
            g2.fillRoundRect(108, cellY, 68, 34, 8, 8);
            g2.setColor(p.adjacentCell());
            g2.fillRoundRect(182, cellY, 68, 34, 8, 8);

            g2.setColor(p.accent());
            g2.fillRoundRect(w - 174, 24, 147, 40, 12, 12);
            g2.setColor(ThemeManager.contrastText(p.accent()));
            g2.drawString("Primary action", w - 146, 49);

            g2.setColor(p.text());
            g2.drawString(theme.displayName(), 34, 47);
            g2.setColor(p.mutedText());
            g2.drawString("Calendar • balances • schedule", 34, 68);

            g2.setColor(p.text());
            g2.drawString("12", 44, cellY + 21);
            g2.drawString("13", 118, cellY + 21);
            g2.setColor(p.mutedText());
            g2.drawString("14", 192, cellY + 21);

            g2.setColor(AppTheme.VACATION); g2.fillRoundRect(34, 124, 66, 8, 8, 8);
            g2.setColor(AppTheme.ETO); g2.fillRoundRect(108, 124, 50, 8, 8, 8);
            g2.setColor(AppTheme.HOLIDAY); g2.fillRoundRect(166, 124, 66, 8, 8, 8);
            g2.dispose();
        }
    }
}
