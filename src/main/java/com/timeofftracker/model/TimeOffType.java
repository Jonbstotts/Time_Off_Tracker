package com.timeofftracker.model;

public enum TimeOffType {
    VACATION("Vacation", true, "VACATION"),
    ETO("Emergency Time Off (ETO)", true, "ETO"),
    HOLIDAY("Holiday / Company Off", false, "HOLIDAY"),
    LIMITED_SERVICE("Limited Service Day", false, "LIMITED"),
    WORKING_HOLIDAY("Working Holiday", false, "WORKING HOLIDAY");

    private final String displayName;
    private final boolean deductsBalance;
    private final String calendarLabel;

    TimeOffType(String displayName, boolean deductsBalance, String calendarLabel) {
        this.displayName = displayName;
        this.deductsBalance = deductsBalance;
        this.calendarLabel = calendarLabel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean deductsBalance() {
        return deductsBalance;
    }

    public String getCalendarLabel() {
        return calendarLabel;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
