package com.timeofftracker.ui;

import com.timeofftracker.model.TimeOffEntry;
import com.timeofftracker.model.TimeOffType;
import com.timeofftracker.service.TimeOffService;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class CalendarPanel extends JPanel {
    private final TimeOffService service;
    private final JPanel grid = new JPanel(new GridLayout(6, 7, 7, 7));
    private final JLabel monthTitle = new JLabel();
    private YearMonth visibleMonth;
    private final Consumer<LocalDate> dateClickHandler;
    private final Runnable monthChangedHandler;

    public CalendarPanel(TimeOffService service, YearMonth initialMonth,
                         Consumer<LocalDate> dateClickHandler,
                         Runnable monthChangedHandler) {
        super(new BorderLayout(10, 10));
        this.service = service;
        this.visibleMonth = initialMonth;
        this.dateClickHandler = dateClickHandler;
        this.monthChangedHandler = monthChangedHandler;
        setOpaque(false);
        buildUi();
        refresh();
    }

    private void buildUi() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setOpaque(false);

        JButton prev = new JButton("‹");
        JButton next = new JButton("›");
        prev.putClientProperty("FlatLaf.style", "font: bold +8; arc: 12");
        next.putClientProperty("FlatLaf.style", "font: bold +8; arc: 12");
        prev.setFocusable(false);
        next.setFocusable(false);
        prev.setFocusPainted(false);
        next.setFocusPainted(false);
        prev.addActionListener(e -> setVisibleMonth(visibleMonth.minusMonths(1)));
        next.addActionListener(e -> setVisibleMonth(visibleMonth.plusMonths(1)));

        monthTitle.putClientProperty("FlatLaf.style", "font: bold +8");
        monthTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(prev);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(next);

        nav.add(left, BorderLayout.WEST);
        nav.add(monthTitle, BorderLayout.CENTER);
        nav.add(right, BorderLayout.EAST);
        add(nav, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(7, 7));
        body.setOpaque(false);
        JPanel weekdayRow = new JPanel(new GridLayout(1, 7, 7, 7));
        weekdayRow.setOpaque(false);
        for (DayOfWeek dow : List.of(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)) {
            JLabel l = new JLabel(dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()).toUpperCase(), SwingConstants.CENTER);
            l.setForeground(AppTheme.MUTED);
            l.putClientProperty("FlatLaf.style", "font: bold -1");
            weekdayRow.add(l);
        }
        body.add(weekdayRow, BorderLayout.NORTH);
        grid.setOpaque(false);
        grid.setPreferredSize(new Dimension(840, 540));
        grid.setMinimumSize(new Dimension(700, 480));
        body.add(grid, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        add(buildLegend(), BorderLayout.SOUTH);
    }

    private JComponent buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 2));
        legend.setOpaque(false);
        addLegendItem(legend, "Vacation", AppTheme.VACATION);
        addLegendItem(legend, "ETO", AppTheme.ETO);
        addLegendItem(legend, "Holiday", AppTheme.HOLIDAY);
        addLegendItem(legend, "Limited Service", AppTheme.LIMITED_SERVICE);
        addLegendItem(legend, "Working Holiday", AppTheme.WORKING_HOLIDAY);
        return legend;
    }

    private void addLegendItem(JPanel parent, String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(color);
        JLabel label = new JLabel(text);
        label.setForeground(AppTheme.MUTED);
        label.putClientProperty("FlatLaf.style", "font: -1");
        item.add(dot);
        item.add(label);
        parent.add(item);
    }

    public void refresh() {
        monthTitle.setText(visibleMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        grid.removeAll();

        Map<LocalDate, TimeOffEntry> entryMap = new HashMap<>();
        for (TimeOffEntry e : service.entriesForMonth(visibleMonth)) entryMap.put(e.date(), e);

        LocalDate first = visibleMonth.atDay(1);
        int sundayBasedIndex = first.getDayOfWeek().getValue() % 7;
        LocalDate cellDate = first.minusDays(sundayBasedIndex);

        for (int i = 0; i < 42; i++) {
            TimeOffEntry entry = entryMap.get(cellDate);
            grid.add(new DayCell(cellDate, visibleMonth, entry));
            cellDate = cellDate.plusDays(1);
        }
        revalidate();
        repaint();
    }

    public void setVisibleMonth(YearMonth month) {
        this.visibleMonth = month;
        refresh();
        if (monthChangedHandler != null) monthChangedHandler.run();
    }

    public YearMonth getVisibleMonth() {
        return visibleMonth;
    }

    private Color calendarBaseColor() {
        Color themed = ThemeManager.backgroundFor(ThemeManager.appliedTheme());
        if (themed != null) return themed;
        Color panel = UIManager.getColor("Panel.background");
        return panel != null ? panel : new Color(245, 245, 245);
    }

    private Color currentMonthCellColor() {
        Color base = calendarBaseColor();
        boolean dark = ThemeManager.appliedTheme().dark();
        return blend(base, dark ? Color.WHITE : Color.BLACK, dark ? 0.09 : 0.035);
    }

    private Color adjacentMonthCellColor() {
        Color base = calendarBaseColor();
        boolean dark = ThemeManager.appliedTheme().dark();
        return blend(base, dark ? Color.BLACK : Color.WHITE, dark ? 0.04 : 0.35);
    }

    private static Color blend(Color a, Color b, double amount) {
        amount = Math.max(0.0, Math.min(1.0, amount));
        int r = (int) Math.round(a.getRed() * (1 - amount) + b.getRed() * amount);
        int g = (int) Math.round(a.getGreen() * (1 - amount) + b.getGreen() * amount);
        int bl = (int) Math.round(a.getBlue() * (1 - amount) + b.getBlue() * amount);
        return new Color(r, g, bl);
    }

    private class DayCell extends JButton {
        private final LocalDate date;

        DayCell(LocalDate date, YearMonth month, TimeOffEntry entry) {
            this.date = date;
            setHorizontalAlignment(SwingConstants.LEFT);
            setVerticalAlignment(SwingConstants.TOP);
            setMargin(new Insets(9, 10, 9, 10));
            setFocusPainted(false);
            setFocusable(false);
            setOpaque(true);
            setContentAreaFilled(true);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(110, 86));
            setMinimumSize(new Dimension(95, 80));

            boolean inMonth = YearMonth.from(date).equals(month);
            String dayText = Integer.toString(date.getDayOfMonth());
            String secondary = "";
            if (entry != null) {
                if (entry.type().deductsBalance()) {
                    secondary = "<br><b>" + entry.type().getCalendarLabel() + "</b><br>" + trimHours(entry.hours()) + " hrs";
                } else {
                    String descriptor = entry.notes() == null ? "" : entry.notes().trim();
                    if (descriptor.isBlank()) descriptor = entry.type().getCalendarLabel();
                    secondary = "<br><b>" + escapeHtml(descriptor) + "</b><br><span style='font-size:9px'>" + entry.type().getCalendarLabel() + "</span>";
                }
            }
            setText("<html><div style='width:100px'>" + dayText + secondary + "</div></html>");

            if (!inMonth) {
                setEnabled(false);
                setBackground(adjacentMonthCellColor());
                setForeground(new Color(145, 145, 145));
                putClientProperty("FlatLaf.style", "arc: 14; borderWidth: 0");
            } else if (entry != null) {
                setForeground(AppTheme.textColorFor(entry.type()));
                setBackground(AppTheme.colorFor(entry.type()));
                putClientProperty("FlatLaf.style", "arc: 14; borderWidth: 0");
                setToolTipText(buildTooltip(entry));
            } else if (date.equals(LocalDate.now())) {
                setBackground(currentMonthCellColor());
                setForeground(AppTheme.TODAY);
                setBorderPainted(true);
                putClientProperty("FlatLaf.style", "arc: 14; borderWidth: 2; borderColor: #59a14f");
                setToolTipText("Today");
            } else {
                setBackground(currentMonthCellColor());
                putClientProperty("FlatLaf.style", "arc: 14; borderWidth: 0");
            }

            addActionListener(e -> dateClickHandler.accept(this.date));
        }

        private String buildTooltip(TimeOffEntry entry) {
            StringBuilder tip = new StringBuilder(entry.type().getDisplayName());
            if (entry.type().deductsBalance()) {
                tip.append(" — ").append(entry.status().getDisplayName())
                   .append(" — ").append(trimHours(entry.hours())).append(" hours");
            } else {
                tip.append(" — does not reduce Vacation or ETO");
            }
            if (!entry.notes().isBlank()) tip.append(" — ").append(entry.notes());
            return tip.toString();
        }

        private String trimHours(double hours) {
            return hours == Math.rint(hours) ? Integer.toString((int) hours) : String.format("%.1f", hours);
        }

        private String escapeHtml(String value) {
            return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}
