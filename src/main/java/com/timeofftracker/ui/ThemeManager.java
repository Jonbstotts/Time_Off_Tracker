package com.timeofftracker.ui;

import com.formdev.flatlaf.FlatDarkLaf;
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

public final class ThemeManager {
    public enum Theme {
        SYSTEM("Follow System", false, new Color(70, 130, 180), "Uses the current macOS/desktop light or dark preference."),
        LIGHT("Light", false, new Color(70, 120, 180), "Bright, clean everyday theme."),
        DARK("Dark", true, new Color(88, 150, 220), "Classic dark theme."),
        MIDNIGHT("Midnight", true, new Color(88, 166, 255), "Deep blue-black surfaces with cool blue accents."),
        GRAPHITE("Graphite", true, new Color(174, 181, 191), "Neutral charcoal surfaces with subtle silver accents."),
        OCEAN("Ocean", true, new Color(58, 177, 210), "Deep ocean blue with aqua accents."),
        FOREST("Forest", true, new Color(90, 170, 105), "Dark evergreen surfaces with natural green accents."),
        WARM_SAND("Warm Sand", false, new Color(178, 122, 66), "Warm neutral surfaces with earthy accents."),
        SLATE_BLUE("Slate Blue", false, new Color(88, 112, 171), "Soft cool gray-blue theme for everyday use."),
        HIGH_CONTRAST("High Contrast", true, new Color(255, 214, 64), "Maximum contrast for text and controls."),
        HALLOWEEN("Halloween", true, new Color(242, 126, 35), "Charcoal and pumpkin-orange seasonal theme."),
        THANKSGIVING("Thanksgiving", false, new Color(166, 88, 48), "Autumn cream, copper, and harvest accents."),
        CHRISTMAS("Christmas", true, new Color(214, 68, 75), "Evergreen surfaces with red and warm holiday accents."),
        NEW_YEAR("New Year", true, new Color(220, 190, 94), "Midnight surfaces with celebratory gold accents."),
        VALENTINES("Valentine's Day", false, new Color(196, 65, 105), "Soft blush surfaces with burgundy and pink accents."),
        ST_PATRICKS("St. Patrick's Day", true, new Color(66, 180, 93), "Deep emerald theme with bright green accents."),
        EASTER_SPRING("Easter / Spring", false, new Color(145, 112, 196), "Light spring surfaces with pastel accents."),
        MEMORIAL_DAY("Memorial Day", false, new Color(48, 86, 150), "Clean patriotic blue with restrained red accents."),
        INDEPENDENCE_DAY("Independence Day", true, new Color(215, 58, 69), "Navy surfaces with red, white, and blue accents."),
        LABOR_DAY("Labor Day", false, new Color(54, 95, 160), "Crisp late-summer patriotic theme.");

        private final String displayName;
        private final boolean dark;
        private final Color accent;
        private final String description;

        Theme(String displayName, boolean dark, Color accent, String description) {
            this.displayName = displayName;
            this.dark = dark;
            this.accent = accent;
            this.description = description;
        }

        public String displayName() { return displayName; }
        public boolean dark() { return dark; }
        public Color accent() { return accent; }
        public String description() { return description; }
        @Override public String toString() { return displayName; }
    }

    public enum SeasonalEvent {
        NEW_YEAR("New Year", Theme.NEW_YEAR),
        VALENTINES("Valentine's Day", Theme.VALENTINES),
        ST_PATRICKS("St. Patrick's Day", Theme.ST_PATRICKS),
        EASTER("Easter / Spring", Theme.EASTER_SPRING),
        MEMORIAL_DAY("Memorial Day", Theme.MEMORIAL_DAY),
        INDEPENDENCE_DAY("Independence Day", Theme.INDEPENDENCE_DAY),
        LABOR_DAY("Labor Day", Theme.LABOR_DAY),
        HALLOWEEN("Halloween", Theme.HALLOWEEN),
        THANKSGIVING("Thanksgiving", Theme.THANKSGIVING),
        CHRISTMAS("Christmas", Theme.CHRISTMAS);

        private final String displayName;
        private final Theme theme;
        SeasonalEvent(String displayName, Theme theme) { this.displayName = displayName; this.theme = theme; }
        public String displayName() { return displayName; }
        public Theme theme() { return theme; }
    }

    private static final Preferences PREFS = Preferences.userNodeForPackage(ThemeManager.class);
    private static final String KEY_THEME = "theme.v2";
    private static final String KEY_AUTO = "automaticSeasonalThemes";
    private static final String EVENT_PREFIX = "seasonal.";
    private static Theme appliedTheme = Theme.LIGHT;

    private ThemeManager() {}

    public static Theme savedTheme() {
        String fallback = PREFS.get("theme", Theme.LIGHT.name());
        try { return Theme.valueOf(PREFS.get(KEY_THEME, fallback)); }
        catch (IllegalArgumentException ex) { return Theme.LIGHT; }
    }

    public static boolean automaticSeasonalThemes() { return PREFS.getBoolean(KEY_AUTO, false); }
    public static void setAutomaticSeasonalThemes(boolean enabled) { PREFS.putBoolean(KEY_AUTO, enabled); }
    public static boolean isEventEnabled(SeasonalEvent event) { return PREFS.getBoolean(EVENT_PREFIX + event.name(), true); }
    public static void setEventEnabled(SeasonalEvent event, boolean enabled) { PREFS.putBoolean(EVENT_PREFIX + event.name(), enabled); }

    public static Map<SeasonalEvent, Boolean> eventSettings() {
        Map<SeasonalEvent, Boolean> result = new EnumMap<>(SeasonalEvent.class);
        for (SeasonalEvent event : SeasonalEvent.values()) result.put(event, isEventEnabled(event));
        return result;
    }

    public static void saveAppearance(Theme theme, boolean automatic, Map<SeasonalEvent, Boolean> enabledEvents) {
        PREFS.put(KEY_THEME, theme.name());
        PREFS.putBoolean(KEY_AUTO, automatic);
        for (SeasonalEvent event : SeasonalEvent.values()) {
            PREFS.putBoolean(EVENT_PREFIX + event.name(), enabledEvents.getOrDefault(event, true));
        }
        applySavedTheme();
    }

    public static void applySavedTheme() { applyTheme(resolveEffectiveTheme(LocalDate.now()), false); }

    public static void applyTheme(Theme theme, boolean persist) {
        Theme actual = theme == Theme.SYSTEM ? systemTheme() : theme;
        if (actual.dark()) FlatDarkLaf.setup(); else FlatLightLaf.setup();
        installDefaults(actual);
        appliedTheme = actual;
        if (persist) PREFS.put(KEY_THEME, theme.name());
        refreshWindows();
    }

    public static Theme effectiveThemeToday() { return resolveEffectiveTheme(LocalDate.now()); }
    public static Theme appliedTheme() { return appliedTheme; }
    public static boolean isDark() { return appliedTheme.dark(); }

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
        if (isBetween(date, LocalDate.of(year, 12, 31), LocalDate.of(year, 12, 31)) ||
                isBetween(date, LocalDate.of(year, 1, 1), LocalDate.of(year, 1, 2))) return SeasonalEvent.NEW_YEAR;
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
        try { desktopAppearance = Toolkit.getDefaultToolkit().getDesktopProperty("apple.awt.application.appearance"); }
        catch (HeadlessException ignored) {}
        String combined = appearance + " " + String.valueOf(desktopAppearance);
        return combined.toLowerCase().contains("dark") ? Theme.DARK : Theme.LIGHT;
    }

    private static void installDefaults(Theme theme) {
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.width", 12);
        Color accent = theme.accent();
        UIManager.put("Component.focusColor", accent);
        UIManager.put("Component.accentColor", accent);
        UIManager.put("ProgressBar.foreground", accent);
        UIManager.put("TabbedPane.underlineColor", accent);
        UIManager.put("Button.default.background", accent);
        UIManager.put("Button.default.foreground", contrastText(accent));

        Color bg = backgroundFor(theme);
        if (bg != null) {
            Color panel = bg;
            Color surface = adjust(bg, theme.dark() ? 12 : -6);
            Color field = adjust(bg, theme.dark() ? 18 : -10);
            Color text = theme == Theme.HIGH_CONTRAST ? Color.WHITE : (theme.dark() ? new Color(238, 240, 244) : new Color(36, 38, 42));
            UIManager.put("Panel.background", panel);
            UIManager.put("Viewport.background", panel);
            UIManager.put("Label.foreground", text);
            UIManager.put("Table.background", surface);
            UIManager.put("Table.foreground", text);
            UIManager.put("TextField.background", field);
            UIManager.put("FormattedTextField.background", field);
            UIManager.put("TextArea.background", field);
            UIManager.put("ComboBox.background", field);
        }
    }

    public static Color backgroundFor(Theme theme) {
        return switch (theme) {
            case MIDNIGHT -> new Color(18, 25, 39);
            case GRAPHITE -> new Color(42, 44, 48);
            case OCEAN -> new Color(18, 45, 58);
            case FOREST -> new Color(27, 48, 36);
            case WARM_SAND -> new Color(239, 229, 208);
            case SLATE_BLUE -> new Color(226, 231, 240);
            case HIGH_CONTRAST -> new Color(5, 5, 5);
            case HALLOWEEN -> new Color(35, 31, 38);
            case THANKSGIVING -> new Color(242, 226, 198);
            case CHRISTMAS -> new Color(23, 54, 43);
            case NEW_YEAR -> new Color(20, 24, 38);
            case VALENTINES -> new Color(249, 226, 232);
            case ST_PATRICKS -> new Color(20, 55, 38);
            case EASTER_SPRING -> new Color(241, 235, 248);
            case MEMORIAL_DAY -> new Color(234, 239, 247);
            case INDEPENDENCE_DAY -> new Color(20, 37, 68);
            case LABOR_DAY -> new Color(235, 240, 248);
            default -> null;
        };
    }

    public static Color contrastText(Color color) {
        double lum = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
        return lum > 160 ? new Color(35, 35, 35) : Color.WHITE;
    }

    private static Color adjust(Color color, int delta) {
        return new Color(clamp(color.getRed() + delta), clamp(color.getGreen() + delta), clamp(color.getBlue() + delta));
    }

    private static int clamp(int value) { return Math.max(0, Math.min(255, value)); }

    private static void refreshWindows() {
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            window.invalidate();
            window.validate();
            window.repaint();
        }
    }
}
