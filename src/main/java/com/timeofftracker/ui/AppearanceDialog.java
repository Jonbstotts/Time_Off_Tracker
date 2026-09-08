package com.timeofftracker.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

/**
 * Appearance settings with explicit manual vs. seasonal behavior.
 *
 * Choosing a base theme automatically selects MANUAL mode. Seasonal mode is a
 * separate opt-in behavior, so a manual selection is never silently replaced.
 */
public class AppearanceDialog extends JDialog {
    private final JComboBox<ThemeManager.Theme> themeCombo = new JComboBox<>(ThemeManager.Theme.values());
    private final JRadioButton manualMode = new JRadioButton("Use selected theme");
    private final JRadioButton seasonalMode = new JRadioButton("Automatically use seasonal and holiday themes");
    private final Map<ThemeManager.SeasonalEvent, JCheckBox> eventChecks = new EnumMap<>(ThemeManager.SeasonalEvent.class);
    private final JLabel description = new JLabel();
    private boolean loading;
    private boolean saved;

    public AppearanceDialog(Window owner) {
        super(owner, "Appearance", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        loadValues();
        pack();
        setMinimumSize(new Dimension(720, 590));
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
                "Choose how themes should behave. Selecting a theme switches to manual mode.")));
        root.add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel themeCard = AppTheme.card(new BorderLayout(12, 12));
        JPanel picker = new JPanel(new BorderLayout(10, 8));
        picker.setOpaque(false);
        picker.add(AppTheme.sectionTitle("Base theme"), BorderLayout.NORTH);
        picker.add(themeCombo, BorderLayout.CENTER);
        themeCard.add(picker, BorderLayout.NORTH);

        JPanel manualRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        manualRow.setOpaque(false);
        manualRow.add(manualMode);
        themeCard.add(manualRow, BorderLayout.CENTER);

        description.setBorder(new EmptyBorder(4, 0, 0, 0));
        themeCard.add(description, BorderLayout.SOUTH);
        center.add(themeCard);
        center.add(Box.createVerticalStrut(14));

        JPanel seasonalCard = AppTheme.card(new BorderLayout(10, 10));
        JPanel seasonalTop = new JPanel();
        seasonalTop.setOpaque(false);
        seasonalTop.setLayout(new BoxLayout(seasonalTop, BoxLayout.Y_AXIS));
        seasonalTop.add(AppTheme.sectionTitle("Automatic seasonal themes"));
        seasonalTop.add(Box.createVerticalStrut(7));
        seasonalTop.add(seasonalMode);
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
                "Seasonal mode uses the base theme outside enabled holiday windows.")), BorderLayout.SOUTH);
        center.add(seasonalCard);

        root.add(center, BorderLayout.CENTER);

        ButtonGroup behaviorGroup = new ButtonGroup();
        behaviorGroup.add(manualMode);
        behaviorGroup.add(seasonalMode);

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

        themeCombo.addActionListener(e -> {
            if (!loading) {
                manualMode.setSelected(true);
                updateModeControls();
                refreshDescription();
            }
        });
        manualMode.addActionListener(e -> {
            updateModeControls();
            refreshDescription();
        });
        seasonalMode.addActionListener(e -> {
            updateModeControls();
            refreshDescription();
        });
        for (JCheckBox box : eventChecks.values()) {
            box.addActionListener(e -> refreshDescription());
        }
        return root;
    }

    private void loadValues() {
        loading = true;
        themeCombo.setSelectedItem(ThemeManager.savedTheme());
        if (ThemeManager.savedMode() == ThemeManager.ThemeMode.SEASONAL) {
            seasonalMode.setSelected(true);
        } else {
            manualMode.setSelected(true);
        }
        for (var entry : eventChecks.entrySet()) {
            entry.getValue().setSelected(ThemeManager.isEventEnabled(entry.getKey()));
        }
        loading = false;
        updateModeControls();
        refreshDescription();
    }

    private void updateModeControls() {
        boolean seasonal = seasonalMode.isSelected();
        for (JCheckBox box : eventChecks.values()) box.setEnabled(seasonal);
    }

    private ThemeManager.ThemeMode selectedMode() {
        return seasonalMode.isSelected()
                ? ThemeManager.ThemeMode.SEASONAL
                : ThemeManager.ThemeMode.MANUAL;
    }

    private Map<ThemeManager.SeasonalEvent, Boolean> selectedEvents() {
        Map<ThemeManager.SeasonalEvent, Boolean> enabled = new EnumMap<>(ThemeManager.SeasonalEvent.class);
        for (var entry : eventChecks.entrySet()) {
            enabled.put(entry.getKey(), entry.getValue().isSelected());
        }
        return enabled;
    }

    private void refreshDescription() {
        ThemeManager.Theme base = (ThemeManager.Theme) themeCombo.getSelectedItem();
        if (base == null) return;

        ThemeManager.ThemeMode mode = selectedMode();
        Map<ThemeManager.SeasonalEvent, Boolean> events = selectedEvents();
        ThemeManager.Theme effective = ThemeManager.resolveEffectiveTheme(base, mode, LocalDate.now(), events);
        ThemeManager.SeasonalEvent event = mode == ThemeManager.ThemeMode.SEASONAL
                ? ThemeManager.seasonalEventFor(LocalDate.now())
                : null;
        boolean eventEnabled = event != null && events.getOrDefault(event, true);

        StringBuilder html = new StringBuilder("<html>");
        html.append(base.description());
        html.append("<br><br><b>After saving:</b> ").append(effective.displayName());
        if (mode == ThemeManager.ThemeMode.MANUAL) {
            html.append(" <span style='font-weight:normal'>(manual mode)</span>");
        } else if (eventEnabled) {
            html.append(" <span style='font-weight:normal'>(")
                    .append(event.displayName())
                    .append(" seasonal theme)</span>");
        } else {
            html.append(" <span style='font-weight:normal'>(base theme; no enabled seasonal override today)</span>");
        }
        html.append("<br><b>Currently active:</b> ").append(ThemeManager.appliedTheme().displayName());
        html.append("</html>");
        description.setText(html.toString());
    }

    private void save() {
        ThemeManager.Theme theme = (ThemeManager.Theme) themeCombo.getSelectedItem();
        ThemeManager.ThemeMode mode = selectedMode();
        Map<ThemeManager.SeasonalEvent, Boolean> enabled = selectedEvents();

        saved = true;
        dispose();
        ThemeManager.saveAppearance(
                theme == null ? ThemeManager.Theme.FLAT_LIGHT : theme,
                mode,
                enabled);
    }

    public boolean wasSaved() {
        return saved;
    }
}
