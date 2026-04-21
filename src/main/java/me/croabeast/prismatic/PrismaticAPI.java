package me.croabeast.prismatic;

import lombok.experimental.UtilityClass;
import me.croabeast.prismatic.color.ColorPattern;
import me.croabeast.vnc.VNC;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * Central utility for color and text formatting operations used by PrismaticAPI.
 */
@UtilityClass
public class PrismaticAPI {

    private final ColorEngine COLOR_ENGINE = new ColorEngine();

    /**
     * Parses a textual color identifier into a Bungee {@link ChatColor}.
     *
     * <p>The input may be a named legacy color, a legacy color code, or a 6-digit hexadecimal
     * value. When {@code legacy} is {@code true}, RGB colors are downsampled to the nearest
     * legacy color supported by older clients.</p>
     *
     * @param string color text to parse
     * @param legacy whether the result should be restricted to legacy colors
     * @return resolved chat color
     */
    public ChatColor fromString(String string, boolean legacy) {
        return COLOR_ENGINE.fromString(string, legacy);
    }

    /**
     * Parses a textual color identifier using full RGB support.
     *
     * @param string color text to parse
     * @return resolved chat color
     */
    public ChatColor fromString(String string) {
        return COLOR_ENGINE.fromString(string);
    }

    /**
     * Applies a single color to a string.
     *
     * @param color target color
     * @param string text to colorize
     * @param legacy whether the output should be restricted to legacy colors
     * @return colorized legacy string
     */
    public String applyColor(Color color, String string, boolean legacy) {
        return COLOR_ENGINE.applyColor(color, string, legacy);
    }

    /**
     * Applies a single color and returns both Adventure and legacy representations.
     *
     * <p>This is useful when callers want to keep a component form available while still having
     * a ready-to-send legacy fallback string.</p>
     *
     * @param color target color
     * @param string text to colorize
     * @return rich text result containing both component and legacy representations
     */
    public RichText applyColorText(Color color, String string) {
        return legacyText(COLOR_ENGINE.applyColor(color, string, !canUseHexColors()));
    }

    /**
     * Applies a two-stop gradient to a string.
     *
     * @param string text to colorize
     * @param start gradient start color
     * @param end gradient end color
     * @param legacy whether the output should be restricted to legacy colors
     * @return colorized legacy string
     */
    public String applyGradient(String string, Color start, Color end, boolean legacy) {
        return COLOR_ENGINE.applyGradient(string, start, end, legacy);
    }

    /**
     * Applies a two-stop gradient and returns both Adventure and legacy representations.
     *
     * @param start gradient start color
     * @param end gradient end color
     * @param string text to colorize
     * @return rich text result containing both component and legacy representations
     */
    public RichText applyGradientText(Color start, Color end, String string) {
        return legacyText(COLOR_ENGINE.applyGradient(string, start, end, !canUseHexColors()));
    }

    /**
     * Applies a rainbow effect to a string.
     *
     * @param string text to colorize
     * @param saturation rainbow saturation value
     * @param legacy whether the output should be restricted to legacy colors
     * @return colorized legacy string
     */
    public String applyRainbow(String string, float saturation, boolean legacy) {
        return COLOR_ENGINE.applyRainbow(string, saturation, legacy);
    }

    /**
     * Applies a rainbow effect and returns both Adventure and legacy representations.
     *
     * @param saturation rainbow saturation value
     * @param string text to colorize
     * @return rich text result containing both component and legacy representations
     */
    public RichText applyRainbowText(float saturation, String string) {
        return legacyText(COLOR_ENGINE.applyRainbow(string, saturation, !canUseHexColors()));
    }

    private boolean resolveLegacy(@Nullable Player player) {
        return !canUseHexColors(player);
    }

    private String applyRgbPipeline(String string, boolean legacy) {
        string = ColorPattern.MULTI.apply(string, legacy);
        return ColorPattern.SINGLE.apply(string, legacy);
    }

    private String applyLegacyPipeline(String string, boolean legacy) {
        return ChatColor.translateAlternateColorCodes('&', applyRgbPipeline(string, legacy));
    }

    private RichText legacyText(String string) {
        String safe = string == null ? "" : string;
        return new RichText(legacySerializer().deserialize(safe), safe);
    }

    private String colorizedLegacy(String string) {
        return colorize(string);
    }

    private LegacyComponentSerializer legacySerializer() {
        return LegacyComponentSerializer.legacySection();
    }

    /**
     * Runs the full formatting pipeline and returns a rich result tailored to a specific player.
     *
     * <p>The player's effective version is consulted through VNC so old clients can be
     * downsampled automatically when ViaVersion is present. The pipeline processes MiniMessage
     * first when available, then Prismatic gradients/rainbows, then single RGB formats, and
     * finally legacy ampersand translation.</p>
     *
     * @param player target player, or {@code null} to use server-level capabilities
     * @param string raw text to process
     * @return rich text result containing both component and legacy representations
     */
    public RichText colorizeText(@Nullable Player player, String string) {
        if (StringUtils.isBlank(string))
            return legacyText(string);

        boolean legacy = resolveLegacy(player);
        if (string.indexOf('<') != -1 && Adventure.isAvailable())
            return Adventure.colorize(string, legacy, PrismaticAPI::applyLegacyPipeline);

        return legacyText(applyLegacyPipeline(string, legacy));
    }

    /**
     * Runs the full formatting pipeline using server-level capabilities only.
     *
     * @param string raw text to process
     * @return rich text result containing both component and legacy representations
     */
    public RichText colorizeText(String string) {
        return colorizeText(null, string);
    }

    /**
     * Runs the full formatting pipeline and returns only the legacy string representation.
     *
     * @param player target player, or {@code null} to use server-level capabilities
     * @param string raw text to process
     * @return formatted legacy string
     */
    public String colorize(@Nullable Player player, String string) {
        return colorizeText(player, string).asLegacy();
    }

    /**
     * Runs the full formatting pipeline and returns only the legacy string representation.
     *
     * @param string raw text to process
     * @return formatted legacy string
     */
    public String colorize(String string) {
        return colorize(null, string);
    }

    /**
     * Removes legacy Bukkit color markers such as {@code &a} and {@code §a}.
     *
     * @param string input text
     * @return text without legacy color markers
     */
    public String stripBukkit(String string) {
        return COLOR_ENGINE.stripBukkit(string);
    }

    /**
     * Removes special legacy formatting markers such as bold, italic, or reset codes.
     *
     * @param string input text
     * @return text without special legacy formatting markers
     */
    public String stripSpecial(String string) {
        return COLOR_ENGINE.stripSpecial(string);
    }

    /**
     * Removes Prismatic RGB syntaxes from a string.
     *
     * <p>This strips gradients, rainbows, and single RGB markers while leaving the original text
     * content intact.</p>
     *
     * @param string input text
     * @return text without Prismatic RGB markers
     */
    public String stripRGB(String string) {
        if (StringUtils.isBlank(string)) return string;

        string = ColorPattern.MULTI.strip(string);
        return ColorPattern.SINGLE.strip(string);
    }

    /**
     * Removes every formatting syntax understood by this library.
     *
     * @param string input text
     * @return text without any known legacy or RGB formatting
     */
    public String stripAll(String string) {
        return stripRGB(stripSpecial(stripBukkit(string)));
    }

    /**
     * Checks whether the formatted form of a string starts with a color code.
     *
     * @param string input text
     * @return {@code true} when the formatted output starts with a color code
     */
    public boolean startsWithColor(String string) {
        return COLOR_ENGINE.startsWithColor(colorizedLegacy(string));
    }

    /**
     * Resolves the first color code present in the formatted output.
     *
     * @param string input text
     * @return first color code, or {@code null} when none is present
     */
    @Nullable
    public String getStartColor(String string) {
        return COLOR_ENGINE.getStartColor(colorizedLegacy(string));
    }

    /**
     * Resolves the last color code present in the formatted output.
     *
     * @param string input text
     * @return last color code, or {@code null} when none is present
     */
    @Nullable
    public String getEndColor(String string) {
        return COLOR_ENGINE.getEndColor(colorizedLegacy(string));
    }

    private boolean canUseHexColors() {
        return VNC.SERVER_MINECRAFT_VERSION.supportsHex();
    }

    private boolean canUseHexColors(@Nullable Player player) {
        return canUseHexColors() &&
                player != null &&
                VNC.player(player).supportsHex();
    }
}
