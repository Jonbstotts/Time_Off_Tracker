# Time Off Tracker 1.3

A local-first Java 21 desktop calendar for tracking Vacation, Emergency Time Off (ETO), holidays, limited-service days, and working holidays.

## Highlights

- Calendar-first month view with clickable day cells.
- Vacation and ETO yearly allotments with automatic used, scheduled, and remaining balances.
- Vacation and ETO entries support scheduled/taken status, hours, and notes.
- Company calendar classifications: Holiday / Company Off, Limited Service Day, and Working Holiday. These do not reduce Vacation or ETO balances.
- Holiday/company-day descriptions appear directly on calendar cells.
- Annual schedule document import with a review screen before anything is saved.
- Startup layout sizing reserves enough height for calendar entries so populated days are not clipped.
- SQLite storage at `~/.timeofftracker/timeoff.db`, separate from the application itself.

## Appearance and themes

Version 1.3 replaces the simple light/dark toggle with a full Appearance window.

Everyday themes:

- Follow System
- Light
- Dark
- Midnight
- Graphite
- Ocean
- Forest
- Warm Sand
- Slate Blue
- High Contrast

Holiday and seasonal themes:

- Halloween
- Thanksgiving
- Christmas
- New Year
- Valentine's Day
- St. Patrick's Day
- Easter / Spring
- Memorial Day
- Independence Day
- Labor Day

Choose **Appearance** from the top-right of the main window to select a base theme. A built-in visual preview shows the palette before you save it.

### Automatic seasonal themes

Enable **Automatically use seasonal and holiday themes** to let the application temporarily override your normal base theme around holidays. Each seasonal theme can be enabled or disabled independently.

Current automatic windows include:

- New Year: Dec 31-Jan 2
- Valentine's Day: Feb 1-14
- St. Patrick's Day: Mar 1-17
- Easter / Spring: two weeks before Easter through the following Monday
- Memorial Day: Friday through Memorial Day
- Independence Day: Jul 1-5
- Labor Day: Friday through Labor Day
- Halloween: October
- Thanksgiving: November
- Christmas: Dec 1-30

When the seasonal window ends, Time Off Tracker automatically returns to your selected base theme.

Calendar entry colors for Vacation, ETO, Holiday, Limited Service, and Working Holiday remain distinct across themes.

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

On macOS, `Build Time Off Tracker.command` can build both the executable JAR and `dist/Time Off Tracker.app` after the script has executable permission.

Your saved data remains at `~/.timeofftracker/timeoff.db` when the application is rebuilt or replaced.
