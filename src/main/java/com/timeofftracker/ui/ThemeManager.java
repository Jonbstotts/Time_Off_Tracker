package com.timeofftracker.ui;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Owns theme selection and persistence only.
 *
 * FlatLaf owns component colors, borders, disabled states, hover states and
 * other look-and-feel details. This class intentionally does not install
 * application-wide UIManager color overrides or maintain a second palette.
 */
public final class ThemeManager {
    public enum Theme {
        SYSTEM("Follow System", null, false, "Uses the current macOS/desktop light or dark preference."),
        FLAT_LIGHT("FlatLaf Light", "com.formdev.flatlaf.FlatLightLaf", false, "Official FlatLaf light theme."),
        FLAT_DARK("FlatLaf Dark", "com.formdev.flatlaf.FlatDarkLaf", true, "Official FlatLaf dark theme."),
        INTELLIJ("IntelliJ", "com.formdev.flatlaf.FlatIntelliJLaf", false, "Official FlatLaf IntelliJ-style light theme."),
        DARCULA("Darcula", "com.formdev.flatlaf.FlatDarculaLaf", true, "Official FlatLaf Darcula-style dark theme."),
        MAC_LIGHT("macOS Light", "com.formdev.flatlaf.themes.FlatMacLightLaf", false, "Official FlatLaf macOS light theme."),
        MAC_DARK("macOS Dark", "com.formdev.flatlaf.themes.FlatMacDarkLaf", true, "Official FlatLaf macOS dark theme."),
        ARC("Arc", "com.formdev.flatlaf.intellijthemes.FlatArcIJTheme", false, "Arc from the official FlatLaf IntelliJ Themes Pack."),
        ARC_ORANGE("Arc Orange", "com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme", false, "Arc Orange from the official FlatLaf IntelliJ Themes Pack."),
        ARC_DARK("Arc Dark", "com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme", true, "Arc Dark from the official FlatLaf IntelliJ Themes Pack."),
        ARC_DARK_ORANGE("Arc Dark Orange", "com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme", true, "Arc Dark Orange from the official FlatLaf IntelliJ Themes Pack."),
        CARBON("Carbon", "com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme", true, "Carbon from the official FlatLaf IntelliJ Themes Pack."),
        COBALT_2("Cobalt 2", "com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme", true, "Cobalt 2 from the official FlatLaf IntelliJ Themes Pack."),
        CYAN_LIGHT("Cyan Light", "com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme", false, "Cyan Light from the official FlatLaf IntelliJ Themes Pack."),
        DARK_PURPLE("Dark Purple", "com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme", true, "Dark Purple from the official FlatLaf IntelliJ Themes Pack."),
        DRACULA("Dracula", "com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme", true, "Dracula from the official FlatLaf IntelliJ Themes Pack."),
        DEEP_OCEAN("Gradianto Deep Ocean", "com.formdev.flatlaf.intellijthemes.FlatGradiantoDeepOceanIJTheme", true, "Gradianto Deep Ocean from the official FlatLaf IntelliJ Themes Pack."),
        MIDNIGHT_BLUE("Gradianto Midnight Blue", "com.formdev.flatlaf.intellijthemes.FlatGradiantoMidnightBlueIJTheme", true, "Gradianto Midnight Blue from the official FlatLaf IntelliJ Themes Pack."),
        NATURE_GREEN("Gradianto Nature Green", "com.formdev.flatlaf.intellijthemes.FlatGradiantoNatureGreenIJTheme", true, "Gradianto Nature Green from the official FlatLaf IntelliJ Themes Pack."),
        GRUVBOX_DARK("Gruvbox Dark Hard", "com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkHardIJTheme", true, "Gruvbox Dark Hard from the official FlatLaf IntelliJ Themes Pack."),
        HIGH_CONTRAST("High Contrast", "com.formdev.flatlaf.intellijthemes.FlatHighContrastIJTheme", true, "High Contrast from the official FlatLaf IntelliJ Themes Pack."),
        MATERIAL_DARK("Material Design Dark", "com.formdev.flatlaf.intellijthemes.FlatMaterialDesignDarkIJTheme", true, "Material Design Dark from the official FlatLaf IntelliJ Themes Pack."),
        MONOKAI_PRO("Monokai Pro", "com.formdev.flatlaf.intellijthemes.FlatMonokaiProIJTheme", true, "Monokai Pro from the official FlatLaf IntelliJ Themes Pack."),
        NORD("Nord", "com.formdev.flatlaf.intellijthemes.FlatNordIJTheme", true, "Nord from the official FlatLaf IntelliJ Themes Pack."),
        ONE_DARK("One Dark", "com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme", true, "One Dark from the official FlatLaf IntelliJ Themes Pack."),
        SOLARIZED_DARK("Solarized Dark", "com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme", true, "Solarized Dark from the official FlatLaf IntelliJ Themes Pack."),
        SOLARIZED_LIGHT("Solarized Light", "com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme", false, "Solarized Light from the official FlatLaf IntelliJ Themes Pack."),
        SPACEGRAY("Spacegray", "com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme", true, "Spacegray from the official FlatLaf IntelliJ Themes Pack."),
        XCODE_DARK("Xcode Dark", "com.formdev.flatlaf.intellijthemes.FlatXcodeDarkIJTheme", true, "Xcode Dark from the official FlatLaf IntelliJ Themes Pack.");

        private final String displayName;
        private final String lafClassName;
        private final boolean dark;
        private final String description;

        Theme(String displayName, String lafClassName, boolean dark, String description) {
            this.displayName = displayName;
            this.lafClassName = lafClassName;
            this.dark = dark;
            this.description = description;
        }

        public String displayName() { return displayName; }
        public String lafClassName() { return lafClassName; }
        public boolean dark() { return dark; }
        public String description() { return description; }
        @Override public String toString() { return displayName; }
    }

    public enum SeasonalEvent {
        NEW_YEAR("New Year", Theme.COBALT_2),
        VALENTINES("Valentine's Day", Theme.DARK_PURPLE),
        ST_PATRICKS("St. Patrick's Day", Theme.NATURE_GREEN),
        EASTER("Easter / Spring", Theme.CYAN_LIGHT),
        MEMORIAL_DAY("Memorial Day", Theme.ARC),
        INDEPENDENCE_DAY("Independence Day", Theme.COBALT_2),
        LABOR_DAY("Labor Day", Theme.INTELLIJ),
        HALLOWEEN("Halloween", Theme.ARC_DARK_ORANGE),
        THANKSGIVING("Thanksgiving", Theme.ARC_ORANGE),
        CHRISTMAS("Christmas", Theme.NATURE_GREEN);

        private final String displayName;
        private final Theme theme;
        SeasonalEvent(String displayName, Theme theme) { this.displayName = displayName; this.theme = theme; }
        public String displayName() { return displayName; }
        public Theme theme() { return theme; }
    }

    private static final Preferences PREFS = Preferences.userNodeForPackage(ThemeManager.class);
    private static final String KEY_THEME = "theme.v4.nativeFlatLaf";
    private static final String KEY_AUTO = "automaticSeasonalThemes";
    private static final String EVENT_PREFIX = "seasonal.";
    private static Theme appliedTheme = Theme.FLAT_LIGHT;

    private ThemeManager() {}

    public static Theme savedTheme() {
        String stored = PREFS.get(KEY_THEME, null);
        if (stored != null) {
            try { return Theme.valueOf(stored); }
            catch (IllegalArgumentException ignored) {}
        }

        String previousFlatLaf = PREFS.get("theme.v3.flatlaf", null);
        if (previousFlatLaf != null) {
            try { return Theme.valueOf(previousFlatLaf); }
            catch (IllegalArgumentException ignored) {}
        }

        String legacy = PREFS.get("theme.v2", PREFS.get("theme", "LIGHT"));
        return migrateLegacyTheme(legacy);
    }

    private static Theme migrateLegacyTheme(String legacy) {
        return switch (legacy) {
            case "DARK" -> Theme.FLAT_DARK;
            case "MIDNIGHT" -> Theme.MIDNIGHT_BLUE;
            case "GRAPHITE" -> Theme.CARBON;
            case "OCEAN" -> Theme.DEEP_OCEAN;
            case "FOREST", "ST_PATRICKS", "CHRISTMAS" -> Theme.NATURE_GREEN;
            case "WARM_SAND", "THANKSGIVING" -> Theme.ARC_ORANGE;
            case "SLATE_BLUE", "MEMORIAL_DAY", "LABOR_DAY" -> Theme.ARC;
            case "HIGH_CONTRAST" -> Theme.HIGH_CONTRAST;
            case "HALLOWEEN" -> Theme.ARC_DARK_ORANGE;
            case "NEW_YEAR", "INDEPENDENCE_DAY" -> Theme.COBALT_2;
            case "VALENTINES" -> Theme.DARK_PURPLE;
            case "EASTER_SPRING" -> Theme.CYAN_LIGHT;
            case "SYSTEM" -> Theme.SYSTEM;
            default -> Theme.FLAT_LIGHT;
        };
    }

    public static boolean automaticSeasonalThemes() {
        return PREFS.getBoolean(KEY_AUTO, false);
    }

    public static boolean isEventEnabled(SeasonalEvent event) {
        return PREFS.getBoolean(EVENT_PREFIX + event.name(), true);
    }

    public static Map<SeasonalEvent, Boolean> eventSettings() {
        Map<SeasonalEvent, Boolean> result = new EnumMap<>(SeasonalEvent.class);
        for (SeasonalEvent event : SeasonalEvent.values()) result.put(event, isEventEnabled(event));
        return result;
    }

    public static void saveAppearance(Theme theme, boolean automatic, Map<SeasonalEvent, Boolean> enabledEvents) {
        Theme saved = theme == null ? Theme.FLAT_LIGHT : theme;
        PREFS.put(KEY_THEME, saved.name());
        PREFS.putBoolean(KEY_AUTO, automatic);
        for (SeasonalEvent event : SeasonalEvent.values()) {
            PREFS.putBoolean(EVENT_PREFIX + event.name(), enabledEvents.getOrDefault(event, true));
        }
        applySavedTheme();
    }

    public static void applySavedTheme() {
        applyTheme(resolveEffectiveTheme(LocalDate.now()), false);
    }

    /**
     * Installs the selected FlatLaf class and asks Swing to recreate the UI
     * delegates of all open windows. No colors are copied or overridden.
     */
    public static void applyTheme(Theme theme, boolean persist) {
        Theme requested = theme == null ? Theme.FLAT_LIGHT : theme;
        Theme actual = requested == Theme.SYSTEM ? systemTheme() : requested;

        try {
            UIManager.setLookAndFeel(actual.lafClassName());
        } catch (Exception ex) {
            System.err.println("Unable to load theme '" + actual.displayName() + "': " + ex.getMessage());
            FlatLightLaf.setup();
            actual = Theme.FLAT_LIGHT;
        }

        appliedTheme = actual;
        if (persist) PREFS.put(KEY_THEME, requested.name());
        updateOpenWindows();
    }

    public static Theme effectiveThemeToday() {
        return resolveEffectiveTheme(LocalDate.now());
    }

    public static Theme appliedTheme() {
        return appliedTheme;
    }

    public static boolean isDark() {
        return appliedTheme.dark();
    }

    private static Theme resolveEffectiveTheme(LocalDate date) {
        if (automaticSeasonalThemes()) {
            SeasonalEvent event = seasonalEventFor(date);
            if (event != null && isEventEnabled(event)) return event.theme();
        }
        Theme saved = savedTheme();
        return saved == Theme.SYSTEM ? systemTheme() : saved;
    }

    static SeasonalEvent seasonalEventFor(LocalDate date) {
        int year = date.getYear();
        if (date.equals(LocalDate.of(year, 12, 31)) || isBetween(date, LocalDate.of(year, 1, 1), LocalDate.of(year, 1, 2))) return SeasonalEvent.NEW_YEAR;
        if (isBetween(date, LocalDate.of(year, 2, 1), LocalDate.of(year, 2, 14))) return SeasonalEvent.VALENTINES;
        if (isBetween(date, LocalDate.of(year, 3, 1), LocalDate.of(year, 3, 17))) return SeasonalEvent.ST_PATRICKS;

        LocalDate easter = easterSunday(year);
        if (isBetween(date, easter.minusDays(14), easter.plusDays(1))) return SeasonalEvent.EASTER;

        LocalDate memorial = LocalDate.of(year, Month.MAY, 31).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (isBetween(date, memorial.minusDays(3), memorial)) return SeasonalEvent.MEMORIAL_DAY;
        if (isBetween(date, LocalDate.of(year, 7, 1), LocalDate.of(year, 7, 5))) return SeasonalEvent.INDEPENDENCE_DAY;

        LocalDate labor = LocalDate.of(year, Month.SEPTEMBER, 1).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        if (isBetween(date, labor.minusDays(3), labor)) return SeasonalEvent.LABOR_DAY;
        if (date.getMonth() == Month.OCTOBER) return SeasonalEvent.HALLOWEEN;
        if (date.getMonth() == Month.NOVEMBER) return SeasonalEvent.THANKSGIVING;
        if (isBetween(date, LocalDate.of(year, 12, 1), LocalDate.of(year, 12, 30))) return SeasonalEvent.CHRISTMAS;
        return null;
    }

    private static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private static LocalDate easterSunday(int year) {
        int a = year % 19, b = year / 100, c = year % 100, d = b / 4, e = b % 4;
        int f = (b + 8) / 25, g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4, k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }

    private static Theme systemTheme() {
        String appearance = System.getProperty("apple.awt.application.appearance", "");
        Object desktopAppearance = null;
        try {
            desktopAppearance = Toolkit.getDefaultToolkit().getDesktopProperty("apple.awt.application.appearance");
        } catch (HeadlessException ignored) {}

        String combined = appearance + " " + String.valueOf(desktopAppearance);
        return combined.toLowerCase().contains("dark") ? Theme.MAC_DARK : Theme.MAC_LIGHT;
    }

    private static void updateOpenWindows() {
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            window.invalidate();
            window.validate();
            window.repaint();
        }
    }

    /** Used only for semantic calendar colors, not for theming controls. */
    public static Color contrastText(Color color) {
        double white = contrastRatio(Color.WHITE, color);
        Color black = new Color(25, 25, 25);
        return white >= contrastRatio(black, color) ? Color.WHITE : black;
    }

    private static double contrastRatio(Color a, Color b) {
        double l1 = relativeLuminance(a);
        double l2 = relativeLuminance(b);
        double lighter = Math.max(l1, l2);
        double darker = Math.min(l1, l2);
        return (lighter + 0.05) / (darker + 0.05);
    }

    private static double relativeLuminance(Color c) {
        double r = channel(c.getRed() / 255.0);
        double g = channel(c.getGreen() / 255.0);
        double b = channel(c.getBlue() / 255.0);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double channel(double value) {
        return value <= 0.03928 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
    }
}
