package com.timeofftracker.ui;

import com.timeofftracker.model.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TimeOffDialog extends JDialog {
    public enum Result { SAVE, DELETE, CANCEL }

    private final JComboBox<TimeOffType> typeCombo = new JComboBox<>(TimeOffType.values());
    private final JComboBox<EntryStatus> statusCombo = new JComboBox<>(EntryStatus.values());
    private final JSpinner hoursSpinner;
    private final JTextArea notesArea = new JTextArea(4, 28);
    private final JLabel statusLabel = new JLabel("Status");
    private final JLabel hoursLabel = new JLabel("Hours");
    private final JLabel categoryHint = new JLabel();
    private Result result = Result.CANCEL;
    private final long existingId;
    private final LocalDate date;
    private final double standardHours;

    public TimeOffDialog(Window owner, LocalDate date, double standardHours, TimeOffEntry existing) {
        super(owner, existing == null ? "Add Calendar Entry" : "Edit Calendar Entry", ModalityType.APPLICATION_MODAL);
        this.existingId = existing == null ? 0 : existing.id();
        this.date = date;
        this.standardHours = standardHours;
        this.hoursSpinner = new JSpinner(new SpinnerNumberModel(
                existing == null ? standardHours : existing.hours(), 0.5, 24.0, 0.5));
        this.hoursSpinner.setEditor(new JSpinner.NumberEditor(hoursSpinner, "0.##"));

        if (existing != null) {
            typeCombo.setSelectedItem(existing.type());
            statusCombo.setSelectedItem(existing.status());
            notesArea.setText(existing.notes());
        } else {
            statusCombo.setSelectedItem(date.isAfter(LocalDate.now()) ? EntryStatus.SCHEDULED : EntryStatus.TAKEN);
        }

        buildUi(existing != null);
        typeCombo.addActionListener(e -> updateCategoryControls());
        updateCategoryControls();
    }

    private void buildUi(boolean editing) {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = AppTheme.title(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        heading.add(title);
        JLabel sub = new JLabel(editing ? "Update or remove this calendar entry." : "Choose how this day should be recorded.");
        sub.setForeground(AppTheme.MUTED);
        heading.add(Box.createVerticalStrut(4));
        heading.add(sub);
        heading.add(Box.createVerticalStrut(6));
        categoryHint.setForeground(AppTheme.MUTED);
        categoryHint.putClientProperty("FlatLaf.style", "font: -1");
        heading.add(categoryHint);
        root.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 5, 6, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.weightx = 1;

        addRow(form, g, 0, new JLabel("Day type"), typeCombo);
        addRow(form, g, 1, statusLabel, statusCombo);
        addRow(form, g, 2, hoursLabel, hoursSpinner);

        g.gridy = 3;
        g.gridx = 0;
        g.weightx = 0;
        g.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Notes"), g);
        g.gridx = 1;
        g.weightx = 1;
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(notesArea);
        scroll.setPreferredSize(new Dimension(320, 90));
        form.add(scroll, g);

        root.add(form, BorderLayout.CENTER);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton save = AppTheme.primaryButton(editing ? "Save Changes" : "Add Entry");
        save.addActionListener(e -> { result = Result.SAVE; dispose(); });

        JPanel actions = new JPanel(new BorderLayout());
        if (editing) {
            JButton delete = new JButton("Delete Entry");
            delete.putClientProperty("FlatLaf.style", "foreground: #c74343");
            delete.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Remove this calendar entry? Any affected balance will update automatically.",
                        "Delete Entry", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    result = Result.DELETE;
                    dispose();
                }
            });
            actions.add(delete, BorderLayout.WEST);
        }
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(cancel);
        right.add(save);
        actions.add(right, BorderLayout.EAST);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void updateCategoryControls() {
        TimeOffType type = (TimeOffType) typeCombo.getSelectedItem();
        boolean pto = type == null || type.deductsBalance();
        statusCombo.setEnabled(pto);
        hoursSpinner.setEnabled(pto);
        statusLabel.setEnabled(pto);
        hoursLabel.setEnabled(pto);
        if (!pto) {
            hoursSpinner.setValue(standardHours);
            statusCombo.setSelectedItem(date.isAfter(LocalDate.now()) ? EntryStatus.SCHEDULED : EntryStatus.TAKEN);
            categoryHint.setText("This calendar classification will not reduce your Vacation or ETO balance.");
        } else {
            categoryHint.setText("Vacation and ETO entries automatically reduce the corresponding yearly balance.");
        }
    }

    private void addRow(JPanel panel, GridBagConstraints g, int row, JLabel label, JComponent field) {
        g.gridy = row;
        g.gridx = 0;
        g.weightx = 0.35;
        panel.add(label, g);
        g.gridx = 1;
        g.weightx = 0.65;
        panel.add(field, g);
    }

    public Result getResult() { return result; }

    public TimeOffEntry getEntry() {
        TimeOffType type = (TimeOffType) typeCombo.getSelectedItem();
        return new TimeOffEntry(
                existingId,
                date,
                type,
                (EntryStatus) statusCombo.getSelectedItem(),
                ((Number) hoursSpinner.getValue()).doubleValue(),
                notesArea.getText().trim()
        );
    }
}
