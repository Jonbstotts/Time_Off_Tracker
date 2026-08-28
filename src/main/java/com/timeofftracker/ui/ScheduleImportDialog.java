package com.timeofftracker.ui;

import com.timeofftracker.model.EntryStatus;
import com.timeofftracker.model.ScheduleImportItem;
import com.timeofftracker.model.TimeOffEntry;
import com.timeofftracker.model.TimeOffType;
import com.timeofftracker.model.TimeOffYear;
import com.timeofftracker.service.ScheduleImportService;
import com.timeofftracker.service.TimeOffService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ScheduleImportDialog extends JDialog {
    private final TimeOffService service;
    private final int targetYear;
    private final ScheduleImportService importer = new ScheduleImportService();
    private final ImportTableModel model = new ImportTableModel();
    private final JTable table = new JTable(model);
    private final JLabel fileLabel = AppTheme.muted(new JLabel("No schedule selected"));
    private final JLabel summaryLabel = AppTheme.muted(new JLabel("Choose an annual schedule document to preview detected calendar days."));
    private boolean imported;

    public ScheduleImportDialog(Window owner, TimeOffService service, int targetYear) {
        super(owner, "Import Annual Schedule", ModalityType.APPLICATION_MODAL);
        this.service = service;
        this.targetYear = targetYear;
        buildUi();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(AppTheme.title("Import " + targetYear + " Annual Schedule"));
        heading.add(Box.createVerticalStrut(4));
        JLabel hint = AppTheme.muted(new JLabel("PDF, DOCX, TXT, and CSV schedules are supported. Review the detected days before importing."));
        heading.add(hint);
        root.add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        JPanel chooserRow = new JPanel(new BorderLayout(10, 0));
        chooserRow.setOpaque(false);
        JButton choose = new JButton("Choose Schedule Document…");
        choose.addActionListener(e -> chooseFile());
        chooserRow.add(choose, BorderLayout.WEST);
        chooserRow.add(fileLabel, BorderLayout.CENTER);
        center.add(chooserRow, BorderLayout.NORTH);

        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(115);
        TableColumn typeColumn = table.getColumnModel().getColumn(2);
        typeColumn.setPreferredWidth(150);
        typeColumn.setCellEditor(new DefaultCellEditor(new JComboBox<>(new TimeOffType[]{
                TimeOffType.HOLIDAY, TimeOffType.LIMITED_SERVICE, TimeOffType.WORKING_HOLIDAY
        })));
        table.getColumnModel().getColumn(3).setPreferredWidth(230);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        center.add(summaryLabel, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton importButton = AppTheme.primaryButton("Import Selected Days");
        importButton.addActionListener(e -> importSelected());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(cancel);
        actions.add(importButton);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
        ThemeManager.applyThemeRoles(root);
        setMinimumSize(new Dimension(850, 520));
        setSize(940, 620);
        setLocationRelativeTo(getOwner());
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose Annual Schedule");
        chooser.setFileFilter(new FileNameExtensionFilter("Schedule documents (PDF, DOCX, TXT, CSV)", "pdf", "docx", "txt", "csv"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        fileLabel.setText(file.getName());
        try {
            List<ScheduleImportItem> detected = importer.readSchedule(file, targetYear).stream()
                    .filter(i -> i.date().getYear() == targetYear)
                    .toList();
            model.setItems(detected, service);
            long existing = model.rows.stream().filter(r -> r.existing).count();
            summaryLabel.setText(detected.size() + " schedule day(s) detected" +
                    (existing > 0 ? " • " + existing + " already have calendar entries and are unchecked" : "") + ".");
            if (detected.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No holiday, limited-service, or working-holiday dates could be detected.\n" +
                                "The importer works best when each schedule line contains a date plus a descriptor such as Holiday or Limited Service.",
                        "No Schedule Days Detected", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            model.setItems(List.of(), service);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Unable to Read Schedule", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importSelected() {
        List<ImportRow> selected = model.rows.stream().filter(r -> r.selected).toList();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select at least one detected schedule day to import.", "Nothing Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int importedCount = 0;
        try {
            for (ImportRow row : selected) {
                TimeOffYear yearSettings = service.yearSettings(row.item.date().getYear()).orElse(null);
                if (yearSettings == null) continue;
                TimeOffEntry entry = new TimeOffEntry(
                        0,
                        row.item.date(),
                        row.type,
                        row.item.date().isAfter(LocalDate.now()) ? EntryStatus.SCHEDULED : EntryStatus.TAKEN,
                        yearSettings.standardWorkdayHours(),
                        row.description.trim()
                );
                service.saveEntry(entry);
                importedCount++;
            }
            imported = importedCount > 0;
            JOptionPane.showMessageDialog(this, importedCount + " schedule day(s) were added to the calendar.", "Schedule Imported", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Import Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean wasImported() { return imported; }

    private static final class ImportRow {
        boolean selected;
        final ScheduleImportItem item;
        TimeOffType type;
        String description;
        final boolean existing;

        ImportRow(ScheduleImportItem item, boolean existing) {
            this.item = item;
            this.existing = existing;
            this.selected = !existing;
            this.type = item.type();
            this.description = item.description();
        }
    }

    private static final class ImportTableModel extends AbstractTableModel {
        private final String[] columns = {"Import", "Date", "Type", "Description", "Status"};
        private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMM d, yyyy");
        private final List<ImportRow> rows = new ArrayList<>();

        void setItems(List<ScheduleImportItem> items, TimeOffService service) {
            rows.clear();
            for (ScheduleImportItem item : items) {
                rows.add(new ImportRow(item, service.entryForDate(item.date()).isPresent()));
            }
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public Class<?> getColumnClass(int column) { return column == 0 ? Boolean.class : column == 2 ? TimeOffType.class : String.class; }
        @Override public boolean isCellEditable(int row, int column) { return !rows.get(row).existing && (column == 0 || column == 2 || column == 3); }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            ImportRow row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.selected;
                case 1 -> row.item.date().format(dateFormat);
                case 2 -> row.type;
                case 3 -> row.description;
                case 4 -> row.existing ? "Already on calendar" : "Ready to import";
                default -> "";
            };
        }

        @Override public void setValueAt(Object value, int rowIndex, int columnIndex) {
            ImportRow row = rows.get(rowIndex);
            if (columnIndex == 0) row.selected = Boolean.TRUE.equals(value);
            if (columnIndex == 2 && value instanceof TimeOffType t) row.type = t;
            if (columnIndex == 3) row.description = value == null ? "" : value.toString();
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
