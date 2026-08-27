package com.timeofftracker.ui;

import com.timeofftracker.model.BalanceSummary;

import javax.swing.*;
import java.awt.*;

public class BalancePanel extends JPanel {
    private final MetricCard vacation = new MetricCard("Vacation", AppTheme.VACATION);
    private final MetricCard eto = new MetricCard("Emergency Time Off (ETO)", AppTheme.ETO);
    private final JLabel totalLabel = new JLabel();

    public BalancePanel() {
        setLayout(new BorderLayout(12, 12));
        setOpaque(false);

        JPanel cards = new JPanel(new GridLayout(1, 2, 14, 0));
        cards.setOpaque(false);
        cards.add(vacation);
        cards.add(eto);
        add(cards, BorderLayout.CENTER);

        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalLabel.setForeground(AppTheme.MUTED);
        totalLabel.putClientProperty("FlatLaf.style", "font: +1");
        add(totalLabel, BorderLayout.SOUTH);
    }

    public void updateSummary(BalanceSummary s) {
        vacation.setValues(s.vacationAllowance(), s.vacationUsed(), s.vacationScheduled(), s.vacationRemaining(), s.vacationEntryCount());
        eto.setValues(s.etoAllowance(), s.etoUsed(), s.etoScheduled(), s.etoRemaining(), s.etoEntryCount());
        totalLabel.setText(String.format("Recorded days: %d Vacation  •  %d ETO", s.vacationEntryCount(), s.etoEntryCount()));
    }

    private static class MetricCard extends JPanel {
        private final JLabel name = new JLabel();
        private final JLabel remaining = new JLabel();
        private final JLabel detail = new JLabel();
        private final JProgressBar progress = new JProgressBar(0, 1000);

        MetricCard(String title, Color accent) {
            super(new BorderLayout(8, 8));
            name.setText(title.toUpperCase());
            name.setForeground(accent);
            name.putClientProperty("FlatLaf.style", "font: bold +1");
            remaining.putClientProperty("FlatLaf.style", "font: bold +9");
            detail.setForeground(AppTheme.MUTED);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(name);
            text.add(Box.createVerticalStrut(4));
            text.add(remaining);
            text.add(Box.createVerticalStrut(2));
            text.add(detail);

            progress.setStringPainted(false);
            progress.setPreferredSize(new Dimension(100, 7));
            progress.putClientProperty("FlatLaf.style", "arc: 999");

            add(text, BorderLayout.CENTER);
            add(progress, BorderLayout.SOUTH);
            putClientProperty("FlatLaf.style", "arc: 18; background: lighten(@background,3%)");
            setBorder(BorderFactory.createEmptyBorder(15, 17, 14, 17));
        }

        void setValues(double allowance, double used, double scheduled, double remainingHours, long count) {
            remaining.setText(String.format("%.1f hrs remaining", remainingHours));
            detail.setText(String.format("%.1f used  •  %.1f scheduled  •  %.1f allotted", used, scheduled, allowance));
            double consumed = used + scheduled;
            int value = allowance <= 0 ? 0 : (int) Math.round(Math.min(1.0, consumed / allowance) * 1000);
            progress.setValue(value);
            remaining.setToolTipText(count + " recorded day(s)");
        }
    }
}
