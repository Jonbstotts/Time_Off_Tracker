package com.timeofftracker.model;

import java.time.LocalDate;

public record ScheduleImportItem(
        LocalDate date,
        TimeOffType type,
        String description,
        String sourceText
) {}
