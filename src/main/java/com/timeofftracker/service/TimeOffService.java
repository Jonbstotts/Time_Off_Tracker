package com.timeofftracker.service;

import com.timeofftracker.model.*;
import com.timeofftracker.persistence.TimeOffRepository;
import com.timeofftracker.persistence.YearRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public class TimeOffService {
    private final TimeOffRepository entries;
    private final YearRepository years;

    public TimeOffService(TimeOffRepository entries, YearRepository years) {
        this.entries = entries;
        this.years = years;
    }

    public Optional<TimeOffYear> yearSettings(int year) {
        return years.findByYear(year);
    }

    public List<Integer> configuredYears() {
        return years.findAllYears();
    }

    public void saveYearSettings(TimeOffYear settings) {
        years.save(settings);
    }

    public List<TimeOffEntry> entriesForMonth(YearMonth month) {
        return entries.findBetween(month.atDay(1), month.atEndOfMonth());
    }

    public List<TimeOffEntry> entriesForYear(int year) {
        return entries.findByYear(year);
    }

    public Optional<TimeOffEntry> entryForDate(LocalDate date) {
        return entries.findByDate(date);
    }

    public TimeOffEntry saveEntry(TimeOffEntry entry) {
        validateEntry(entry);
        return entries.save(entry);
    }

    public void deleteEntry(long id) {
        entries.delete(id);
    }

    public BalanceSummary balanceForYear(int year) {
        TimeOffYear settings = yearSettings(year).orElse(new TimeOffYear(year, 0, 0, 8));
        List<TimeOffEntry> list = entriesForYear(year);

        double vacationUsed = list.stream()
                .filter(e -> e.type() == TimeOffType.VACATION && e.status() == EntryStatus.TAKEN)
                .mapToDouble(TimeOffEntry::hours).sum();
        double vacationScheduled = list.stream()
                .filter(e -> e.type() == TimeOffType.VACATION && e.status() == EntryStatus.SCHEDULED)
                .mapToDouble(TimeOffEntry::hours).sum();
        double etoUsed = list.stream()
                .filter(e -> e.type() == TimeOffType.ETO && e.status() == EntryStatus.TAKEN)
                .mapToDouble(TimeOffEntry::hours).sum();
        double etoScheduled = list.stream()
                .filter(e -> e.type() == TimeOffType.ETO && e.status() == EntryStatus.SCHEDULED)
                .mapToDouble(TimeOffEntry::hours).sum();

        long vacationCount = list.stream().filter(e -> e.type() == TimeOffType.VACATION).count();
        long etoCount = list.stream().filter(e -> e.type() == TimeOffType.ETO).count();

        return new BalanceSummary(
                settings.vacationAllowanceHours(), vacationUsed, vacationScheduled,
                settings.vacationAllowanceHours() - vacationUsed - vacationScheduled,
                settings.etoAllowanceHours(), etoUsed, etoScheduled,
                settings.etoAllowanceHours() - etoUsed - etoScheduled,
                vacationCount, etoCount
        );
    }

    private void validateEntry(TimeOffEntry entry) {
        TimeOffYear settings = yearSettings(entry.date().getYear())
                .orElseThrow(() -> new IllegalArgumentException("Set up " + entry.date().getYear() + " before adding time off."));
        if (entry.hours() <= 0 || entry.hours() > 24) {
            throw new IllegalArgumentException("Hours must be greater than 0 and no more than 24.");
        }
        if (entry.type().deductsBalance() && entry.hours() > Math.max(24, settings.standardWorkdayHours() * 2)) {
            throw new IllegalArgumentException("The entered hours are unusually high for one day.");
        }
    }
}
