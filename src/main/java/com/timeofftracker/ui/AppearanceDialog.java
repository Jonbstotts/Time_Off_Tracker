package com.timeofftracker.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

/**
 * Theme selection UI.
 *
 * Selecting an item does not mutate the application's global look and feel.
 * The chosen FlatLaf theme is installed only when Save Appearance is pressed.
 */
public class AppearanceDialog extends JDialog {
    private final JComboBox<ThemeManager.Theme> themeCombo = new JComboBox<>(ThemeManager.Theme.values());
    private final JCheckBox automatic = new JCheckBox("Automatically use seasonal and holiday themes");
    private final Map<ThemeManager.SeasonalEvent, JCheckBox> eventChecks = new EnumMap<>(ThemeManager.SeasonalEvent.class);
    private final JLabel description = new JLabel();
    private boolean saved;

    public AppearanceDialog(Window owner) {
        super(owner, "Appearance", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        loadValues();
        pack();
        setMinimumSize(new Dimension(700, 560));
        setLocationRelativeTo(owner);
    }

    private JComponent buildContent() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(20, 22, 18, 22));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(AppTheme.title("Appearance & Themes"));
        heading.add(Box.createVerticalStrut(5));
        heading.add(AppTheme.muted(new JLabel(
                "Choose an official FlatLaf theme. The selection is applied when you save.")));
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
        description.setBorder(new EmptyBorder(4, 0, 0, 0));
        themeCard.add(description, BorderLayout.CENTER);
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
            JCheckBox box = new JCheckBox(event.displayName() + " → " + event.theme().displayName());
            box.setOpaque(false);
            eventChecks.put(event, box);
            events.add(box);
        }
        seasonalCard.add(events, BorderLayout.CENTER);
        seasonalCard.add(AppTheme.muted(new JLabel(
                "Seasonal themes temporarily replace the base theme and automatically return to it afterward.")),
                BorderLayout.SOUTH);
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

        themeCombo.addActionListener(e -> refreshDescription());
        automatic.addActionListener(e -> updateSeasonalEnabled());
        return root;
    }

    private void loadValues() {
        themeCombo.setSelectedItem(ThemeManager.savedTheme());
        automatic.setSelected(ThemeManager.automaticSeasonalThemes());
        for (var entry : eventChecks.entrySet()) {
            entry.getValue().setSelected(ThemeManager.isEventEnabled(entry.getKey()));
        }
        updateSeasonalEnabled();
        refreshDescription();
    }

    private void updateSeasonalEnabled() {
        boolean enabled = automatic.isSelected();
        for (JCheckBox box : eventChecks.values()) box.setEnabled(enabled);
    }

    private void refreshDescription() {
        ThemeManager.Theme theme = (ThemeManager.Theme) themeCombo.getSelectedItem();
        if (theme == null) return;
        description.setText("<html>" + theme.description() +
                "<br><br><b>Currently active:</b> " + ThemeManager.appliedTheme().displayName() +
                "</html>");
    }

    private void save() {
        ThemeManager.Theme theme = (ThemeManager.Theme) themeCombo.getSelectedItem();
        Map<ThemeManager.SeasonalEvent, Boolean> enabled = new EnumMap<>(ThemeManager.SeasonalEvent.class);
        for (var entry : eventChecks.entrySet()) {
            enabled.put(entry.getKey(), entry.getValue().isSelected());
        }
        saved = true;
        ThemeManager.saveAppearance(
                theme == null ? ThemeManager.Theme.FLAT_LIGHT : theme,
                automatic.isSelected(),
                enabled);
        dispose();
    }

    public boolean wasSaved() {
        return saved;
    }
}
