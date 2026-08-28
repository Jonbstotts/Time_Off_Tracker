package com.timeofftracker.ui;

import com.timeofftracker.model.TimeOffYear;

import javax.swing.*;
import java.awt.*;

public class SetupDialog extends JDialog {
    private final JSpinner yearSpinner;
    private final JSpinner vacationSpinner;
    private final JSpinner etoSpinner;
    private final JSpinner workdaySpinner;
    private boolean saved;

    public SetupDialog(Window owner, int year, TimeOffYear existing, boolean firstRun) {
        super(owner, firstRun ? "Welcome to Time Off Tracker" : "Year Settings", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        int initialYear = existing != null ? existing.year() : year;
        double vacation = existing != null ? existing.vacationAllowanceHours() : 0;
        double eto = existing != null ? existing.etoAllowanceHours() : 0;
        double workday = existing != null ? existing.standardWorkdayHours() : 8;

        yearSpinner = new JSpinner(new SpinnerNumberModel(initialYear, 2000, 2200, 1));
        vacationSpinner = hourSpinner(vacation);
        etoSpinner = hourSpinner(eto);
        workdaySpinner = new JSpinner(new SpinnerNumberModel(workday, 0.5, 24.0, 0.5));

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(AppTheme.title(firstRun ? "Set up your time off" : "Time off settings"));
        heading.add(Box.createVerticalStrut(5));
        JLabel sub = AppTheme.muted(new JLabel(firstRun
                ? "Enter your yearly allowances. You can change them later from Settings."
                : "Update the allowance and standard workday for this year."));
        heading.add(sub);
        root.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 6, 7, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        addRow(form, g, 0, "Year", yearSpinner);
        addRow(form, g, 1, "Vacation allowance (hours)", vacationSpinner);
        addRow(form, g, 2, "ETO allowance (hours)", etoSpinner);
        addRow(form, g, 3, "Standard workday (hours)", workdaySpinner);
        root.add(form, BorderLayout.CENTER);

        JButton cancel = new JButton("Cancel");
        JButton save = AppTheme.primaryButton(firstRun ? "Finish Setup" : "Save Settings");
        save.addActionListener(e -> {
            saved = true;
            dispose();
        });
        cancel.addActionListener(e -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        if (!firstRun) actions.add(cancel);
        actions.add(save);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
        ThemeManager.applyThemeRoles(root);
        pack();
        setSize(Math.max(getWidth(), 480), getHeight());
        setLocationRelativeTo(owner);
    }

    private JSpinner hourSpinner(double value) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, 0.0, 1000.0, 0.5));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0.##");
        spinner.setEditor(editor);
        return spinner;
    }

    private void addRow(JPanel panel, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridy = row;
        g.gridx = 0;
        g.weightx = 0.6;
        panel.add(new JLabel(label), g);
        g.gridx = 1;
        g.weightx = 0.4;
        panel.add(field, g);
    }

    public boolean wasSaved() { return saved; }

    public TimeOffYear getSettings() {
        return new TimeOffYear(
                ((Number) yearSpinner.getValue()).intValue(),
                ((Number) vacationSpinner.getValue()).doubleValue(),
                ((Number) etoSpinner.getValue()).doubleValue(),
                ((Number) workdaySpinner.getValue()).doubleValue()
        );
    }
}
