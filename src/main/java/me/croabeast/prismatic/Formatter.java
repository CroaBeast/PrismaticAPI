package me.croabeast.prismatic;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * Shared contract implemented by the {@link PrismaticAPI#legacy()} and {@link PrismaticAPI#adventure()}
 * facades.
 *
 * <p>Both facades use the same Prismatic parsing pipeline and only differ in the type they emit:
 * legacy output returns {@link String strings}, while Adventure output returns
 * {@link net.kyori.adventure.text.Component components}.
 *
 * @param <T> formatter result type
 * @since 1.4.0
 */
public interface Formatter<T> {

    /**
     * Converts a bare six-digit hexadecimal RGB value into a chat color.
     *
     * <p>When {@code legacy} is {@code false}, the exact RGB value is preserved. When {@code legacy} is
     * {@code true}, the result is downsampled to the nearest legacy Bukkit color.
     *
     * @param string six-digit hexadecimal RGB value without a leading {@code #}
     * @param legacy whether to downsample the resulting color to the legacy palette
     * @return the parsed chat color
     * @throws NumberFormatException if {@code string} is not a valid six-digit hexadecimal RGB value
     */
    ChatColor fromString(String string, boolean legacy);

    /**
     * Parses a color token into a chat color.
     *
     * <p>This overload accepts plain legacy codes such as {@code a}, prefixed legacy codes such as
     * {@code &a} or {@code §a}, exact RGB values such as {@code ff8800}, and compact hex tokens such as
     * {@code &xff8800}. Blank input falls back to {@link ChatColor#WHITE}.
     *
     * @param string color token to parse
     * @return the parsed chat color, or white when the input is blank or unrecognized
     */
    default ChatColor fromString(String string) {
        return fromString(string, false);
    }

    /**
     * Applies a single color to the beginning of a string.
     *
     * @param color color to prepend
     * @param string text to receive the color
     * @param legacy whether to use the nearest legacy Bukkit color instead of exact RGB
     * @return the colorized output
     */
    T applyColor(Color color, String string, boolean legacy);

    /**
     * Applies a per-character gradient between two colors.
     *
     * @param string text to colorize
     * @param start gradient start color
     * @param end gradient end color
     * @param legacy whether to downsample each generated step to the legacy Bukkit palette
     * @return the gradient-colored output
     */
    T applyGradient(String string, Color start, Color end, boolean legacy);

    /**
     * Applies a per-character rainbow effect.
     *
     * @param string text to colorize
     * @param saturation rainbow saturation/brightness factor used to generate the palette
     * @param legacy whether to downsample each generated step to the legacy Bukkit palette
     * @return the rainbow-colored output
     */
    T applyRainbow(String string, float saturation, boolean legacy);

    /**
     * Parses and colorizes a string using the full Prismatic pipeline for a specific player context.
     *
     * <p>If the player supports hex colors, RGB output is preserved. Otherwise the result is downsampled
     * to legacy-safe colors.
     *
     * @param player player used to resolve runtime color capabilities, or {@code null} to force the
     *               conservative legacy fallback
     * @param string text to parse and colorize
     * @return the formatted output
     */
    T colorize(@Nullable Player player, String string);

    /**
     * Parses and colorizes a string without player context.
     *
     * <p>This delegates to {@link #colorize(Player, String)} with a {@code null} player, which means the
     * formatting pipeline uses the conservative legacy fallback because no player capability information is
     * available.
     *
     * @param string text to parse and colorize
     * @return the formatted output
     */
    default T colorize(String string) {
        return colorize(null, string);
    }

    /**
     * Removes standard Bukkit color codes from a string.
     *
     * @param string text to clean
     * @return the string without standard Bukkit color codes
     */
    String stripBukkit(String string);

    /**
     * Removes Bukkit special formatting codes from a string.
     *
     * @param string text to clean
     * @return the string without Bukkit special formatting codes
     */
    String stripSpecial(String string);

    /**
     * Removes Prismatic RGB, gradient and rainbow syntax from a string while leaving legacy Bukkit codes
     * untouched.
     *
     * @param string text to clean
     * @return the string without Prismatic RGB syntax
     */
    String stripRGB(String string);

    /**
     * Removes Bukkit colors, Bukkit special formatting and Prismatic RGB syntax from a string.
     *
     * @param string text to clean
     * @return the fully stripped plain-text string
     */
    String stripAll(String string);

    /**
     * Determines whether the formatted representation of a string starts with a color code.
     *
     * @param string text to inspect
     * @return {@code true} if the formatted output begins with a color code
     */
    boolean startsWithColor(String string);

    /**
     * Returns the first color code found in the formatted representation of a string.
     *
     * @param string text to inspect
     * @return the first detected color code, or {@code null} if none is present
     */
    @Nullable
    String getStartColor(String string);

    /**
     * Returns the last color code found in the formatted representation of a string.
     *
     * @param string text to inspect
     * @return the last detected color code, or {@code null} if none is present
     */
    @Nullable
    String getEndColor(String string);
}
