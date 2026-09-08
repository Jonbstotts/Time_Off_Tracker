package com.timeofftracker.ui;

import com.formdev.flatlaf.FlatLightLaf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ThemeManagerTest {

    @AfterEach
    void restoreDefaultTheme() {
        FlatLightLaf.setup();
    }

    @TestFactory
    Stream<DynamicTest> everyConfiguredThemeLoadsThroughThemeManager() {
        return Arrays.stream(ThemeManager.Theme.values())
                .filter(theme -> theme != ThemeManager.Theme.SYSTEM)
                .map(theme -> DynamicTest.dynamicTest(theme.displayName(), () -> {
                    ThemeManager.applyTheme(theme, false);

                    assertEquals(theme.lafClassName(), UIManager.getLookAndFeel().getClass().getName());
                    assertEquals(theme, ThemeManager.appliedTheme());
                    assertNotNull(UIManager.getColor("Panel.background"));
                    assertNotNull(UIManager.getColor("Label.foreground"));
                    assertNotNull(UIManager.getColor("Button.background"));

                    SwingUtilities.invokeAndWait(() -> {
                        JPanel root = new JPanel(new BorderLayout());
                        JPanel card = AppTheme.card(new BorderLayout());
                        JButton button = new JButton("Button");
                        JLabel muted = AppTheme.muted(new JLabel("Muted"));
                        card.add(button, BorderLayout.CENTER);
                        root.add(card, BorderLayout.CENTER);
                        root.add(muted, BorderLayout.SOUTH);

                        SwingUtilities.updateComponentTreeUI(root);

                        assertNotNull(root.getUI());
                        assertNotNull(card.getUI());
                        assertNotNull(button.getUI());
                        assertNotNull(button.getBackground());
                        assertNotNull(button.getForeground());
                        assertNotNull(card.getBackground());
                    });
                }));
    }

    @Test
    void manualThemeIsNeverOverriddenBySeasonalDate() {
        LocalDate laborDay2026 = LocalDate.of(2026, 9, 7);
        Map<ThemeManager.SeasonalEvent, Boolean> enabled = allSeasonalEvents(true);

        ThemeManager.Theme effective = ThemeManager.resolveEffectiveTheme(
                ThemeManager.Theme.SOLARIZED_DARK,
                ThemeManager.ThemeMode.MANUAL,
                laborDay2026,
                enabled);

        assertEquals(ThemeManager.Theme.SOLARIZED_DARK, effective);
    }

    @Test
    void seasonalModeUsesEnabledHolidayTheme() {
        LocalDate laborDay2026 = LocalDate.of(2026, 9, 7);
        Map<ThemeManager.SeasonalEvent, Boolean> enabled = allSeasonalEvents(true);

        ThemeManager.Theme effective = ThemeManager.resolveEffectiveTheme(
                ThemeManager.Theme.SOLARIZED_DARK,
                ThemeManager.ThemeMode.SEASONAL,
                laborDay2026,
                enabled);

        assertEquals(ThemeManager.Theme.INTELLIJ, effective);
    }

    @Test
    void disabledHolidayFallsBackToSelectedBaseTheme() {
        LocalDate laborDay2026 = LocalDate.of(2026, 9, 7);
        Map<ThemeManager.SeasonalEvent, Boolean> enabled = allSeasonalEvents(true);
        enabled.put(ThemeManager.SeasonalEvent.LABOR_DAY, false);

        ThemeManager.Theme effective = ThemeManager.resolveEffectiveTheme(
                ThemeManager.Theme.SOLARIZED_DARK,
                ThemeManager.ThemeMode.SEASONAL,
                laborDay2026,
                enabled);

        assertEquals(ThemeManager.Theme.SOLARIZED_DARK, effective);
    }

    private Map<ThemeManager.SeasonalEvent, Boolean> allSeasonalEvents(boolean value) {
        Map<ThemeManager.SeasonalEvent, Boolean> result = new EnumMap<>(ThemeManager.SeasonalEvent.class);
        for (ThemeManager.SeasonalEvent event : ThemeManager.SeasonalEvent.values()) {
            result.put(event, value);
        }
        return result;
    }
}
