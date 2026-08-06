package me.croabeast.prismatic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the strip family, which is what callers use to get plain text out of a formatted line.
 */
class StrippingTest {

    private static final char SECTION = '§';

    @Test
    void stripBukkitRemovesColorCodesInBothPrefixes() {
        assertEquals("Hello", PrismaticAPI.stripBukkit("&aHello"));
        assertEquals("Hello", PrismaticAPI.stripBukkit(SECTION + "aHello"));
        assertEquals("HelloWorld", PrismaticAPI.stripBukkit("&aHello&1World"));
    }

    @Test
    void stripBukkitLeavesSpecialFormattingAlone() {
        assertEquals("&lBold", PrismaticAPI.stripBukkit("&a&lBold"));
    }

    @Test
    void stripSpecialRemovesStylesAndReset() {
        assertEquals("&aBold", PrismaticAPI.stripSpecial("&a&lBold"));
        assertEquals("&aPlain", PrismaticAPI.stripSpecial("&a&rPlain"));
        assertEquals("Text", PrismaticAPI.stripSpecial(SECTION + "kText"));
    }

    @Test
    void stripMiniMessageRemovesTags() {
        assertEquals("Hello", PrismaticAPI.stripMiniMessage("<red>Hello</red>"));
    }

    @Test
    void stripMiniMessageIsANoOpWithoutAngleBrackets() {
        String plain = "&aHello";
        assertEquals(plain, PrismaticAPI.stripMiniMessage(plain));
    }

    @Test
    void stripAllLeavesPlainText() {
        assertEquals("Hello", PrismaticAPI.stripAll("&a&l<red>Hello</red>"));
        assertEquals("Hello", PrismaticAPI.stripAll("<G:FF0000>Hello</G:00FF00>"));
    }

    @Test
    void stripKeepsBlankInputUntouched() {
        assertNull(PrismaticAPI.stripBukkit(null));
        assertNull(PrismaticAPI.stripAll(null));
        assertEquals("", PrismaticAPI.stripBukkit(""));
        assertEquals("   ", PrismaticAPI.stripSpecial("   "));
    }

    @Test
    void stripAllIsIdempotent() {
        String once = PrismaticAPI.stripAll("&a&l<red>Hello</red> <G:FF0000>World</G:00FF00>");
        assertEquals(once, PrismaticAPI.stripAll(once));
        assertTrue(once.indexOf('<') < 0 && once.indexOf('&') < 0 && once.indexOf(SECTION) < 0, once);
    }
}
