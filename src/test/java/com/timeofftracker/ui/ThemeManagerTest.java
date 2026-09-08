package com.timeofftracker.ui;

import com.formdev.flatlaf.FlatLightLaf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ThemeManagerTest {

    @AfterEach
    void restoreDefaultTheme() {
        FlatLightLaf.setup();
    }

    @TestFactory
    Stream<DynamicTest> everyConfiguredThemeLoadsAndUpdatesSwingComponents() {
        return Arrays.stream(ThemeManager.Theme.values())
                .filter(theme -> theme != ThemeManager.Theme.SYSTEM)
                .map(theme -> DynamicTest.dynamicTest(theme.displayName(), () -> {
                    UIManager.setLookAndFeel(theme.lafClassName());

                    assertEquals(theme.lafClassName(), UIManager.getLookAndFeel().getClass().getName());
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
}
