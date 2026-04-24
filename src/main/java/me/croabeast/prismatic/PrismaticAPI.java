package me.croabeast.prismatic;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * Main entry point for the PrismaticAPI formatting pipeline.
 *
 * <p>This class exposes two facades backed by the same parser and color engine:
 * {@link #legacy()} returns plain strings containing Bukkit-compatible color codes and is always safe
 * to use, while {@link #adventure()} returns Adventure components when the Adventure runtime is available.
 *
 * <p>The top-level methods on this class are retained for source compatibility with older PrismaticAPI
 * versions and delegate to {@link #legacy()}.
 *
 * <p>The shared pipeline understands legacy color codes, multiple RGB syntaxes, gradients, rainbows and,
 * when Adventure MiniMessage is present at runtime, MiniMessage tags that coexist with Prismatic tags.
 */
@UtilityClass
public class PrismaticAPI {

    private final PrismaticCore CORE = new PrismaticCore();
    private final Formatter<String> LEGACY = new LegacyFormatter(CORE);

    /**
     * Returns the always-safe legacy formatter facade.
     *
     * <p>The returned formatter produces {@link String strings} containing Bukkit/Bungee color codes and
     * does not require Adventure to exist at runtime.
     *
     * @return formatter that produces legacy strings
     * @since 1.4.0
     */
    public Formatter<String> legacy() {
        return LEGACY;
    }

    /**
     * Returns the Adventure formatter facade when the required Adventure classes are present at runtime.
     *
     * <p>This facade produces {@link net.kyori.adventure.text.Component components} from the same Prismatic
     * parsing pipeline used by {@link #legacy()}. Call {@link #isAdventureAvailable()} before invoking this
     * method when Adventure is an optional dependency in the consuming plugin.
     *
     * @return formatter that produces Adventure components
     * @throws IllegalStateException if Adventure or MiniMessage is not available at runtime
     * @since 1.4.0
     */
    @SuppressWarnings("unchecked")
    public Formatter<net.kyori.adventure.text.Component> adventure() {
        if (!isAdventureAvailable()) {
            throw new IllegalStateException(
                    "Adventure runtime is not available. Check PrismaticAPI.isAdventureAvailable() before calling adventure()."
            );
        }

        return (Formatter<net.kyori.adventure.text.Component>) AdventureAccess.formatter(CORE);
    }

    /**
     * Checks whether the Adventure facade can be used safely at runtime.
     *
     * <p>The check validates the presence of the Adventure component API, MiniMessage and the legacy
     * serializer classes required by PrismaticAPI's Adventure bridge.
     *
     * @return {@code true} when the Adventure formatter can be created safely
     * @since 1.4.0
     */
    public boolean isAdventureAvailable() {
        return AdventureAccess.isAvailable();
    }

    /**
     * Converts a bare six-digit hexadecimal RGB value into a {@link ChatColor}.
     *
     * <p>When {@code legacy} is {@code false}, the returned color preserves the exact RGB value.
     * When {@code legacy} is {@code true}, the value is downsampled to the nearest legacy Bukkit color.
     *
     * @param string six-digit hexadecimal RGB value without a leading {@code #}
     * @param legacy whether to downsample the result to the legacy Bukkit palette
     * @return the resulting chat color
     * @throws NumberFormatException if {@code string} is not a valid six-digit hexadecimal RGB value
     */
    public ChatColor fromString(String string, boolean legacy) {
        return legacy().fromString(string, legacy);
    }

    /**
     * Parses a color token into a {@link ChatColor}.
     *
     * <p>This overload accepts plain legacy codes such as {@code a}, prefixed legacy codes such as
     * {@code &a} or {@code §a}, exact RGB values such as {@code ff8800}, and compact hex tokens such as
     * {@code &xff8800}. Blank input falls back to {@link ChatColor#WHITE}.
     *
     * @param string color token to parse
     * @return the parsed chat color, or white when the input is blank or unrecognized
     */
    public ChatColor fromString(String string) {
        return legacy().fromString(string);
    }

    /**
     * Applies a single color to the beginning of a string.
     *
     * @param color color to prepend
     * @param string text to receive the color
     * @param legacy whether to use the nearest legacy Bukkit color instead of exact RGB
     * @return the colorized string
     */
    public String applyColor(Color color, String string, boolean legacy) {
        return legacy().applyColor(color, string, legacy);
    }

    /**
     * Applies a per-character gradient between two colors.
     *
     * <p>Existing special format codes such as bold or italic are preserved while visible characters are
     * recolored across the gradient.
     *
     * @param string text to colorize
     * @param start gradient start color
     * @param end gradient end color
     * @param legacy whether to downsample each generated step to the legacy Bukkit palette
     * @return the gradient-colored string
     */
    public String applyGradient(String string, Color start, Color end, boolean legacy) {
        return legacy().applyGradient(string, start, end, legacy);
    }

    /**
     * Applies a per-character rainbow effect.
     *
     * @param string text to colorize
     * @param saturation rainbow saturation/brightness factor used to generate the palette
     * @param legacy whether to downsample each generated step to the legacy Bukkit palette
     * @return the rainbow-colored string
     */
    public String applyRainbow(String string, float saturation, boolean legacy) {
        return legacy().applyRainbow(string, saturation, legacy);
    }

    /**
     * Parses and colorizes a string using the full Prismatic pipeline for a specific player context.
     *
     * <p>If the player supports hex colors, RGB output is preserved. Otherwise the result is converted to
     * legacy-safe colors. When Adventure MiniMessage is available at runtime, MiniMessage tags are handled
     * before Prismatic tags are restored into the final output.
     *
     * @param player player used to resolve runtime color capabilities, or {@code null} to force the
     *               conservative legacy fallback
     * @param string text to parse and colorize
     * @return the final formatted string
     */
    public String colorize(@Nullable Player player, String string) {
        return legacy().colorize(player, string);
    }

    /**
     * Parses and colorizes a string using the conservative no-player fallback.
     *
     * <p>This overload delegates to {@link #colorize(Player, String)} with a {@code null} player, which
     * means the result is treated as legacy-safe output because no player capability information is available.
     *
     * @param string text to parse and colorize
     * @return the formatted legacy-safe string
     */
    public String colorize(String string) {
        return legacy().colorize(string);
    }

    /**
     * Removes standard Bukkit color codes from a string.
     *
     * <p>This targets color codes such as {@code &a}, {@code §c} and the compact {@code x} hex prefix.
     * It does not remove Prismatic gradient/rainbow tags.
     *
     * @param string text to clean
     * @return the string without standard Bukkit color codes
     */
    public String stripBukkit(String string) {
        return legacy().stripBukkit(string);
    }

    /**
     * Removes Bukkit special formatting codes from a string.
     *
     * <p>This includes styles such as bold, italic, underline, obfuscated and reset markers.
     *
     * @param string text to clean
     * @return the string without Bukkit special formatting codes
     */
    public String stripSpecial(String string) {
        return legacy().stripSpecial(string);
    }

    /**
     * Removes Prismatic RGB, gradient and rainbow syntax from a string while leaving legacy Bukkit codes
     * untouched.
     *
     * @param string text to clean
     * @return the string without Prismatic RGB syntax
     */
    public String stripRGB(String string) {
        return legacy().stripRGB(string);
    }

    /**
     * Removes Bukkit colors, Bukkit special formatting and Prismatic RGB syntax from a string.
     *
     * @param string text to clean
     * @return the fully stripped plain-text string
     */
    public String stripAll(String string) {
        return legacy().stripAll(string);
    }

    /**
     * Determines whether the formatted representation of a string starts with a color code.
     *
     * <p>The inspection is performed against the conservative legacy rendering used by
     * {@link #colorize(String)}.
     *
     * @param string text to inspect
     * @return {@code true} if the formatted output begins with a color code
     */
    public boolean startsWithColor(String string) {
        return legacy().startsWithColor(string);
    }

    /**
     * Returns the first color code found in the formatted representation of a string.
     *
     * <p>The returned value is based on the conservative legacy rendering used by
     * {@link #colorize(String)} and is typically a section-sign color code such as {@code §a}.
     *
     * @param string text to inspect
     * @return the first detected color code, or {@code null} if none is present
     */
    @Nullable
    public String getStartColor(String string) {
        return legacy().getStartColor(string);
    }

    /**
     * Returns the last color code found in the formatted representation of a string.
     *
     * <p>The returned value is based on the conservative legacy rendering used by
     * {@link #colorize(String)} and is typically a section-sign color code such as {@code §a}.
     *
     * @param string text to inspect
     * @return the last detected color code, or {@code null} if none is present
     */
    @Nullable
    public String getEndColor(String string) {
        return legacy().getEndColor(string);
    }
}
