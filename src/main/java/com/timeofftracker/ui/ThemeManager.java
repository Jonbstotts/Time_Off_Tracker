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

    public record Palette(
            Color background,
            Color surface,
            Color surfaceAlt,
            Color calendarCell,
            Color adjacentCell,
            Color text,
            Color mutedText,
            Color control,
            Color controlHover,
            Color border,
            Color accent
    ) {}

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

    public static final String ROLE = "timeoff.themeRole";
    public static final String ROLE_MUTED = "muted";
    public static final String ROLE_SURFACE = "surface";
    public static final String ROLE_SURFACE_ALT = "surfaceAlt";
    public static final String ROLE_PRIMARY = "primary";

    private static final Preferences PREFS = Preferences.userNodeForPackage(ThemeManager.class);
    private static final String KEY_THEME = "theme.v2";
    private static final String KEY_AUTO = "automaticSeasonalThemes";
    private static final String EVENT_PREFIX = "seasonal.";
    private static Theme appliedTheme = Theme.LIGHT;
    private static Palette appliedPalette = paletteFor(Theme.LIGHT);

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
        appliedTheme = actual;
        appliedPalette = paletteFor(actual);
        installDefaults(appliedPalette);
        if (persist) PREFS.put(KEY_THEME, theme.name());
        refreshWindows();
    }

    public static Theme effectiveThemeToday() { return resolveEffectiveTheme(LocalDate.now()); }
    public static Theme appliedTheme() { return appliedTheme; }
    public static Palette palette() { return appliedPalette; }
    public static Palette paletteForPreview(Theme theme) {
        Theme actual = theme == Theme.SYSTEM ? systemTheme() : theme;
        return paletteFor(actual);
    }
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
        try { desktopAppearance = Toolkit.getDefaultToolkit().getDesktopProperty("apple.awt.application.appearance"); }
        catch (HeadlessException ignored) {}
        String combined = appearance + " " + String.valueOf(desktopAppearance);
        return combined.toLowerCase().contains("dark") ? Theme.DARK : Theme.LIGHT;
    }

    private static void installDefaults(Palette p) {
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.width", 12);

        UIManager.put("Panel.background", p.background());
        UIManager.put("Viewport.background", p.background());
        UIManager.put("Label.foreground", p.text());
        UIManager.put("Label.disabledForeground", p.mutedText());
        UIManager.put("Component.foreground", p.text());
        UIManager.put("Component.focusColor", p.accent());
        UIManager.put("Component.accentColor", p.accent());
        UIManager.put("Component.borderColor", p.border());

        UIManager.put("Button.background", p.control());
        UIManager.put("Button.foreground", contrastText(p.control()));
        UIManager.put("Button.hoverBackground", p.controlHover());
        UIManager.put("Button.pressedBackground", blend(p.controlHover(), p.accent(), 0.20));
        UIManager.put("Button.default.background", p.accent());
        UIManager.put("Button.default.foreground", contrastText(p.accent()));

        UIManager.put("TextField.background", p.surfaceAlt());
        UIManager.put("TextField.foreground", p.text());
        UIManager.put("FormattedTextField.background", p.surfaceAlt());
        UIManager.put("FormattedTextField.foreground", p.text());
        UIManager.put("TextArea.background", p.surfaceAlt());
        UIManager.put("TextArea.foreground", p.text());
        UIManager.put("ComboBox.background", p.surfaceAlt());
        UIManager.put("ComboBox.foreground", p.text());
        UIManager.put("Spinner.background", p.surfaceAlt());

        UIManager.put("Table.background", p.surface());
        UIManager.put("Table.foreground", p.text());
        UIManager.put("Table.selectionBackground", blend(p.surface(), p.accent(), 0.35));
        UIManager.put("Table.selectionForeground", p.text());
        UIManager.put("TableHeader.background", p.surfaceAlt());
        UIManager.put("TableHeader.foreground", p.text());

        UIManager.put("ProgressBar.background", p.surfaceAlt());
        UIManager.put("ProgressBar.foreground", p.accent());
        UIManager.put("Separator.foreground", p.border());
        UIManager.put("TabbedPane.underlineColor", p.accent());
        UIManager.put("CheckBox.icon.checkmarkColor", contrastText(p.accent()));
    }

    private static Palette paletteFor(Theme theme) {
        return switch (theme) {
            case SYSTEM -> paletteFor(systemTheme());
            case LIGHT -> palette(false, "#F4F6F8", "#FFFFFF", "#EEF1F4", "#FFFFFF", "#E5E9EE", "#23272D", "#66707C", "#E7EBEF", "#DCE2E8", "#CCD3DA", theme.accent());
            case DARK -> palette(true, "#1F2329", "#2A3037", "#343B44", "#303740", "#242A31", "#F1F4F7", "#AAB3BD", "#39414A", "#454E59", "#505A65", theme.accent());
            case MIDNIGHT -> palette(true, "#101826", "#172235", "#1D2B42", "#1B2940", "#121C2B", "#F3F7FF", "#A8B7CE", "#22344E", "#2C4261", "#365170", theme.accent());
            case GRAPHITE -> palette(true, "#25272B", "#31343A", "#3A3E45", "#353941", "#292C31", "#F2F3F5", "#B7BBC1", "#41454C", "#4B5058", "#5A6069", theme.accent());
            case OCEAN -> palette(true, "#0E2632", "#153746", "#1D4658", "#1A4050", "#102C39", "#EFFBFF", "#A8C8D2", "#205062", "#286276", "#36768A", theme.accent());
            case FOREST -> palette(true, "#13281D", "#1D382A", "#274835", "#244330", "#172F22", "#F0F8F2", "#A9C3B0", "#2C4F3A", "#376249", "#48745A", theme.accent());
            case WARM_SAND -> palette(false, "#EEE4D2", "#FAF4E8", "#E6D8C1", "#F6EBD8", "#DED0B9", "#352B22", "#75685A", "#E2D2B9", "#D5C0A2", "#C8B08E", theme.accent());
            case SLATE_BLUE -> palette(false, "#E6EAF2", "#F5F7FB", "#DCE2EC", "#EEF2F8", "#D5DCE8", "#252D3A", "#687486", "#D9E0EB", "#CBD4E2", "#BAC6D7", theme.accent());
            case HIGH_CONTRAST -> palette(true, "#050505", "#111111", "#1B1B1B", "#151515", "#080808", "#FFFFFF", "#D9D9D9", "#222222", "#303030", "#FFFFFF", theme.accent());
            case HALLOWEEN -> palette(true, "#1C1820", "#29232D", "#342B38", "#302735", "#211B25", "#FFF5E9", "#C7B7C9", "#3B303F", "#4A3A4F", "#624A68", theme.accent());
            case THANKSGIVING -> palette(false, "#EFE0C8", "#FAF1E3", "#E7D2B4", "#F3E4CC", "#DEC8A8", "#3B281B", "#7D6858", "#E4C9A7", "#D6B98F", "#C9A77C", theme.accent());
            case CHRISTMAS -> palette(true, "#102A20", "#1B3A2E", "#234838", "#204234", "#142F25", "#F7F3EA", "#B9C8BF", "#2C4B3B", "#365C48", "#47705A", theme.accent());
            case NEW_YEAR -> palette(true, "#111521", "#1E2433", "#282F40", "#242B3B", "#151A27", "#FFFBEF", "#C8C0A9", "#343B4C", "#41495C", "#5A6273", theme.accent());
            case VALENTINES -> palette(false, "#F8E3EA", "#FFF4F7", "#EFD4DE", "#F9E8EE", "#E7CBD5", "#492A35", "#87636E", "#EACFD8", "#DCBAC6", "#D1A8B6", theme.accent());
            case ST_PATRICKS -> palette(true, "#0E2D1D", "#173D28", "#1F4C32", "#1D472F", "#123522", "#F2FFF5", "#A9CCB3", "#27563A", "#306A47", "#3F8057", theme.accent());
            case EASTER_SPRING -> palette(false, "#F1ECF7", "#FCF9FF", "#E6DDF0", "#F5EFFA", "#DED4EA", "#332A3D", "#766986", "#E2D7ED", "#D6C8E4", "#C7B5D9", theme.accent());
            case MEMORIAL_DAY -> palette(false, "#EAF0F8", "#F8FAFD", "#DDE6F1", "#F2F6FB", "#D4DFEC", "#222D3B", "#697789", "#D9E4F0", "#C8D7E7", "#B7CADF", theme.accent());
            case INDEPENDENCE_DAY -> palette(true, "#101F3B", "#182B4C", "#21375D", "#1E3458", "#132541", "#F7FAFF", "#AFBDD3", "#2A3F63", "#345078", "#49638A", theme.accent());
            case LABOR_DAY -> palette(false, "#E8EEF7", "#F7FAFD", "#DCE5F1", "#F0F5FA", "#D2DDEA", "#243044", "#68768A", "#D7E1ED", "#C7D5E4", "#B6C7DA", theme.accent());
        };
    }

    private static Palette palette(boolean dark, String background, String surface, String surfaceAlt,
                                   String calendarCell, String adjacentCell, String text, String muted,
                                   String control, String hover, String border, Color accent) {
        Palette p = new Palette(hex(background), hex(surface), hex(surfaceAlt), hex(calendarCell), hex(adjacentCell),
                hex(text), hex(muted), hex(control), hex(hover), hex(border), accent);
        return ensureReadable(p, dark);
    }

    private static Palette ensureReadable(Palette p, boolean dark) {
        Color text = ensureContrast(p.text(), p.background(), 4.5, dark ? Color.WHITE : Color.BLACK);
        Color muted = ensureContrast(p.mutedText(), p.background(), 3.0, dark ? Color.WHITE : Color.BLACK);
        return new Palette(p.background(), p.surface(), p.surfaceAlt(), p.calendarCell(), p.adjacentCell(),
                text, muted, p.control(), p.controlHover(), p.border(), p.accent());
    }

    public static Color backgroundFor(Theme theme) { return paletteFor(theme).background(); }
    public static Color calendarCellColor() { return appliedPalette.calendarCell(); }
    public static Color adjacentCalendarCellColor() { return appliedPalette.adjacentCell(); }
    public static Color textColor() { return appliedPalette.text(); }
    public static Color mutedTextColor() { return appliedPalette.mutedText(); }
    public static Color surfaceColor() { return appliedPalette.surface(); }
    public static Color surfaceAltColor() { return appliedPalette.surfaceAlt(); }

    public static Color contrastText(Color color) {
        return contrastRatio(Color.WHITE, color) >= contrastRatio(new Color(25, 25, 25), color)
                ? Color.WHITE : new Color(25, 25, 25);
    }

    private static Color ensureContrast(Color foreground, Color background, double minimum, Color fallback) {
        return contrastRatio(foreground, background) >= minimum ? foreground : fallback;
    }

    public static double contrastRatio(Color a, Color b) {
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

    private static double channel(double v) { return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4); }

    private static Color hex(String value) { return Color.decode(value); }

    public static Color blend(Color a, Color b, double amount) {
        amount = Math.max(0.0, Math.min(1.0, amount));
        int r = (int) Math.round(a.getRed() * (1 - amount) + b.getRed() * amount);
        int g = (int) Math.round(a.getGreen() * (1 - amount) + b.getGreen() * amount);
        int bl = (int) Math.round(a.getBlue() * (1 - amount) + b.getBlue() * amount);
        return new Color(r, g, bl);
    }

    private static void refreshWindows() {
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            applyThemeRoles(window);
            window.invalidate();
            window.validate();
            window.repaint();
        }
    }

    public static void applyThemeRoles(Component component) {
        if (component instanceof JComponent jc) {
            Object role = jc.getClientProperty(ROLE);
            if (ROLE_MUTED.equals(role)) {
                jc.setForeground(appliedPalette.mutedText());
            } else if (ROLE_SURFACE.equals(role)) {
                jc.setOpaque(true);
                jc.setBackground(appliedPalette.surface());
                jc.setForeground(appliedPalette.text());
            } else if (ROLE_SURFACE_ALT.equals(role)) {
                jc.setOpaque(true);
                jc.setBackground(appliedPalette.surfaceAlt());
                jc.setForeground(appliedPalette.text());
            } else if (ROLE_PRIMARY.equals(role)) {
                jc.setBackground(appliedPalette.accent());
                jc.setForeground(contrastText(appliedPalette.accent()));
            }
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) applyThemeRoles(child);
        }
    }
}
