package com.timeofftracker.model;

public enum EntryStatus {
    SCHEDULED("Scheduled"),
    TAKEN("Taken / Called In");

    private final String displayName;

    EntryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
