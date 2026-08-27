package com.timeofftracker.model;

public record TimeOffYear(
        int year,
        double vacationAllowanceHours,
        double etoAllowanceHours,
        double standardWorkdayHours
) {}
