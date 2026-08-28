package com.timeofftracker.ui;

import com.timeofftracker.model.TimeOffEntry;
import com.timeofftracker.model.TimeOffYear;
import com.timeofftracker.service.TimeOffService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;

public class MainPanel extends JPanel {
    private final Window owner;
    private final TimeOffService service;
    private final BalancePanel balancePanel = new BalancePanel();
    private final CalendarPanel calendarPanel;
    private final JLabel yearInfo = AppTheme.muted(new JLabel());
    private final JButton appearance = new JButton();

    public MainPanel(Window owner, TimeOffService service) {
        super(new BorderLayout(18, 18));
        this.owner = owner;
        this.service = service;
        setBorder(BorderFactory.createEmptyBorder(20, 24, 22, 24));

        YearMonth initial = YearMonth.now();
        calendarPanel = new CalendarPanel(service, initial, this::openDate, this::refreshSummary);

        add(buildTop(), BorderLayout.NORTH);
        JPanel calendarCard = AppTheme.card(new BorderLayout());
        calendarCard.add(calendarPanel, BorderLayout.CENTER);
        add(calendarCard, BorderLayout.CENTER);
        refreshAll();
    }

    private JComponent buildTop() {
        JPanel top = new JPanel(new BorderLayout(16, 16));
        top.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(AppTheme.title("Time Off Tracker"));
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(yearInfo);
        header.add(titleBox, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        configureHeaderButton(appearance);
        updateAppearanceButton();
        appearance.addActionListener(e -> openAppearance());
        controls.add(appearance);

        JButton importSchedule = new JButton("Import Annual Schedule");
        configureHeaderButton(importSchedule);
        importSchedule.addActionListener(e -> openScheduleImport());
        controls.add(importSchedule);

        JButton settings = new JButton("Settings");
        configureHeaderButton(settings);
        settings.addActionListener(e -> openSettings());
        controls.add(settings);
        header.add(controls, BorderLayout.EAST);

        top.add(header, BorderLayout.NORTH);
        top.add(balancePanel, BorderLayout.CENTER);
        return top;
    }

    private void configureHeaderButton(AbstractButton button) {
        button.putClientProperty("FlatLaf.style", "arc: 12; margin: 7,14,7,14");
        button.setFocusable(false);
        button.setFocusPainted(false);
    }

    private void openAppearance() {
        AppearanceDialog dialog = new AppearanceDialog(owner);
        dialog.setVisible(true);
        if (dialog.wasSaved()) {
            updateAppearanceButton();
            calendarPanel.refresh();
            refreshSummary();
            ThemeManager.applyThemeRoles(this);
        }
    }

    private void updateAppearanceButton() {
        ThemeManager.Theme effective = ThemeManager.effectiveThemeToday();
        appearance.setText("Appearance");
        String auto = ThemeManager.automaticSeasonalThemes() ? " • Auto seasonal on" : "";
        appearance.setToolTipText("Current theme: " + effective.displayName() + auto);
    }

    private void openDate(LocalDate date) {
        TimeOffYear settings = ensureYearConfigured(date.getYear());
        if (settings == null) return;

        TimeOffEntry existing = service.entryForDate(date).orElse(null);
        TimeOffDialog dialog = new TimeOffDialog(owner, date, settings.standardWorkdayHours(), existing);
        dialog.setVisible(true);

        try {
            if (dialog.getResult() == TimeOffDialog.Result.SAVE) {
                service.saveEntry(dialog.getEntry());
                refreshAll();
            } else if (dialog.getResult() == TimeOffDialog.Result.DELETE && existing != null) {
                service.deleteEntry(existing.id());
                refreshAll();
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Unable to Save", JOptionPane.ERROR_MESSAGE);
        }
    }

    private TimeOffYear ensureYearConfigured(int year) {
        return service.yearSettings(year).orElseGet(() -> {
            SetupDialog dialog = new SetupDialog(owner, year, null, false);
            dialog.setVisible(true);
            if (dialog.wasSaved()) {
                TimeOffYear settings = dialog.getSettings();
                service.saveYearSettings(settings);
                refreshAll();
                return settings;
            }
            return null;
        });
    }

    private void openScheduleImport() {
        int year = calendarPanel.getVisibleMonth().getYear();
        TimeOffYear settings = ensureYearConfigured(year);
        if (settings == null) return;

        ScheduleImportDialog dialog = new ScheduleImportDialog(owner, service, year);
        dialog.setVisible(true);
        if (dialog.wasImported()) refreshAll();
    }

    private void openSettings() {
        int year = calendarPanel.getVisibleMonth().getYear();
        TimeOffYear existing = service.yearSettings(year).orElse(null);
        SetupDialog dialog = new SetupDialog(owner, year, existing, false);
        dialog.setVisible(true);
        if (dialog.wasSaved()) {
            service.saveYearSettings(dialog.getSettings());
            refreshAll();
        }
    }

    public void refreshAll() {
        calendarPanel.refresh();
        refreshSummary();
        ThemeManager.applyThemeRoles(this);
    }

    private void refreshSummary() {
        int year = calendarPanel.getVisibleMonth().getYear();
        balancePanel.updateSummary(service.balanceForYear(year));
        String configured = service.yearSettings(year).isPresent() ? "" : " • Not configured yet";
        yearInfo.setText(year + " balances and calendar" + configured);
    }
}
