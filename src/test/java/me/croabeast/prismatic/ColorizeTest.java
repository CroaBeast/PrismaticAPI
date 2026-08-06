package me.croabeast.prismatic;

import net.md_5.bungee.api.ChatColor;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers colorization in the headless case, where no server runtime is present and the pipeline
 * has to fall back to the legacy palette.
 */
class ColorizeTest {

    private static final char SECTION = '§';

    @Test
    void colorizeTranslatesAmpersandCodes() {
        assertEquals(SECTION + "aHello", PrismaticAPI.colorize("&aHello"));
        assertEquals(SECTION + "a" + SECTION + "lHello", PrismaticAPI.colorize("&a&lHello"));
    }

    @Test
    void colorizeKeepsPlainTextUntouched() {
        assertEquals("Hello", PrismaticAPI.colorize("Hello"));
    }

    @Test
    void colorizeNormalizesBlankInputToAnEmptyString() {
        assertEquals("", PrismaticAPI.colorize(null));
        assertEquals("", PrismaticAPI.colorize(""));
    }

    @Test
    void colorizeResolvesGradientsToLegacyColorsWhenHexIsUnavailable() {
        String result = PrismaticAPI.colorize("<G:FF0000>Hello</G:00FF00>");

        // No server runtime means no hex support, so the tags must be gone and the output must be
        // legacy section codes rather than the §x§f§f... hex form.
        assertFalse(result.contains("<G:"), result);
        assertTrue(result.indexOf(SECTION) >= 0, result);
        assertFalse(result.contains(SECTION + "x"), result);
        assertEquals("Hello", PrismaticAPI.stripAll(result));
    }

    @Test
    void fromStringParsesExactHexIntoTheNearestLegacyColor() {
        // #FF0000 sits closer to DARK_RED (#AA0000) than to RED (#FF5555) in the legacy palette.
        assertEquals(ChatColor.DARK_RED, PrismaticAPI.fromString("FF0000", true));
        assertEquals(ChatColor.WHITE, PrismaticAPI.fromString("FFFFFF", true));
        assertEquals(ChatColor.BLACK, PrismaticAPI.fromString("000000", true));
    }

    @Test
    void fromStringRejectsNonHexInputOnTheStrictOverload() {
        assertThrows(NumberFormatException.class, () -> PrismaticAPI.fromString("potato", true));
    }

    @Test
    void fromStringTokenOverloadAcceptsLegacyCodesAndFallsBackToWhite() {
        assertEquals(ChatColor.GREEN, PrismaticAPI.fromString("a"));
        assertEquals(ChatColor.GREEN, PrismaticAPI.fromString("&a"));
        assertEquals(ChatColor.GREEN, PrismaticAPI.fromString(SECTION + "a"));
        assertEquals(ChatColor.WHITE, PrismaticAPI.fromString(""));
        assertEquals(ChatColor.WHITE, PrismaticAPI.fromString(null));
        assertEquals(ChatColor.WHITE, PrismaticAPI.fromString("potato"));
    }

    @Test
    void fromStringTokenOverloadAcceptsExactAndCompactHex() {
        String expected = ChatColor.of("#ff8800").toString();

        assertEquals(expected, PrismaticAPI.fromString("ff8800").toString());
        assertEquals(expected, PrismaticAPI.fromString("&xff8800").toString());
    }

    @Test
    void applyColorPrependsTheColor() {
        String result = PrismaticAPI.applyColor(Color.RED, "Hello", true);

        assertTrue(result.endsWith("Hello"), result);
        assertEquals(SECTION + "4", result.substring(0, result.length() - "Hello".length()));
    }

    @Test
    void applyGradientColorsEveryVisibleCharacter() {
        String result = PrismaticAPI.applyGradient("Hello", Color.RED, Color.GREEN, true);

        assertEquals("Hello", PrismaticAPI.stripAll(result));
        assertTrue(result.length() > "Hello".length(), result);
    }

    @Test
    void applyGradientPreservesSpecialFormatting() {
        String result = PrismaticAPI.applyGradient("&lHello", Color.RED, Color.GREEN, true);
        assertTrue(result.contains(SECTION + "l") || result.contains("&l"), result);
    }

    @Test
    void applyRainbowColorsEveryVisibleCharacter() {
        String result = PrismaticAPI.applyRainbow("Hello", 1.0F, true);

        assertEquals("Hello", PrismaticAPI.stripAll(result));
        assertTrue(result.length() > "Hello".length(), result);
    }

    @Test
    void startAndEndColorsAreReadFromTheFormattedOutput() {
        assertTrue(PrismaticAPI.startsWithColor("&aHello"));
        assertFalse(PrismaticAPI.startsWithColor("Hello"));

        assertEquals(SECTION + "a", PrismaticAPI.getStartColor("&aHello&cWorld"));
        assertEquals(SECTION + "c", PrismaticAPI.getEndColor("&aHello&cWorld"));
        assertNull(PrismaticAPI.getStartColor("Hello"));
    }

    @Test
    void adventureIsAbsentInAHeadlessTestRuntime() {
        assertFalse(PrismaticAPI.isAdventureAvailable());
        assertNotNull(PrismaticAPI.legacy());
    }

    @Test
    void playerCacheHelpersTolerateNullInput() {
        PrismaticAPI.forgetPlayer((java.util.UUID) null);
        PrismaticAPI.clearPlayerCache();
    }
}
