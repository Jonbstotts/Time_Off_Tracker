package com.timeofftracker.model;

public record BalanceSummary(
        double vacationAllowance,
        double vacationUsed,
        double vacationScheduled,
        double vacationRemaining,
        double etoAllowance,
        double etoUsed,
        double etoScheduled,
        double etoRemaining,
        long vacationEntryCount,
        long etoEntryCount
) {}
