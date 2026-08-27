# Time Off Tracker 1.2

A local-first Java 21 desktop calendar for tracking Vacation, Emergency Time Off (ETO), holidays, limited-service days, and working holidays.

## Features

- Calendar-first month view with clickable day cells.
- Vacation and ETO yearly allotments with automatic used, scheduled, and remaining balances.
- Vacation and ETO entries support scheduled/taken status, hours, and notes.
- Company calendar classifications: Holiday / Company Off, Limited Service Day, and Working Holiday. These do not reduce Vacation or ETO balances.
- Holiday/company-day descriptions appear directly on calendar cells (for example `Labor Day`, `Thanksgiving`, or `Christmas`).
- Persistent Light/Dark mode using FlatLaf.
- SQLite storage at `~/.timeofftracker/timeoff.db` so app upgrades do not erase your history.
- Annual schedule document import with a review screen before anything is saved.
- Startup layout sizing reserves enough height for calendar entries so populated days are not clipped until the window is resized.

## Annual schedule import

Choose **Import Annual Schedule** from the top-right of the application. The importer accepts PDF, DOCX, TXT, and CSV schedules. It scans for dated holiday-related entries, recognizes common holiday names plus `Limited Service` and `Working Holiday`, and presents everything in a preview table before import.

Examples:

```text
September 7, 2026 - Labor Day - Holiday
November 26-27, 2026 - Thanksgiving Holiday
December 24, 2026 - Limited Service - Christmas Eve
December 25, 2026 - Christmas - Holiday
```

Dates without a printed year are assigned to the year currently displayed in the calendar when the import starts.

## Build

Requirements: Java 21 and Maven.

```bash
mvn clean package
java -jar target/time-off-tracker.jar
```

The SQLite database remains at `~/.timeofftracker/timeoff.db`.
