package com.timeofftracker.model;

import java.time.LocalDate;

public record TimeOffEntry(
        long id,
        LocalDate date,
        TimeOffType type,
        EntryStatus status,
        double hours,
        String notes
) {
    public TimeOffEntry withId(long newId) {
        return new TimeOffEntry(newId, date, type, status, hours, notes);
    }
}
