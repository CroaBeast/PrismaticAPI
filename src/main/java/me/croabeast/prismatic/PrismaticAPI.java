package me.croabeast.prismatic;

import com.google.common.collect.ImmutableMap;
import lombok.experimental.UtilityClass;
import me.croabeast.prismatic.color.ColorPattern;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class that provides methods for color manipulation and text formatting.
 * <p>
 * {@code PrismaticAPI} offers functionality to convert hexadecimal color codes to Bukkit {@link ChatColor}
 * objects, create gradients and rainbow effects, and process strings for color codes.
 * It supports both legacy color formatting and modern RGB color support, adapting based on the server version
 * or player client.
 * </p>
 * <p>
 * Key features include:
 * <ul>
 *     <li>Mapping of AWT {@link Color} objects to Bukkit {@link ChatColor} using a predefined color map.</li>
 *     <li>Conversion of hex strings into {@link ChatColor} via {@link #fromString(String, boolean)}.</li>
 *     <li>Creation of gradient and rainbow color arrays for dynamic text effects.</li>
 *     <li>Methods to apply these color effects to strings and to strip color formatting.</li>
 *     <li>Handling of legacy formatting by selecting the closest available color if needed.</li>
 * </ul>
 * </p>
 * <p>
 * Example usage:
 * <pre><code>
 * // Colorize a message for a specific player:
 * String coloredMessage = PrismaticAPI.colorize(player, "&aHello, world!");
 *
 * // Apply a gradient effect to a string:
 * String gradientText = PrismaticAPI.applyGradient("Gradient Text", new Color(255, 0, 0), new Color(0, 0, 255), false);
 *
 * // Strip all color codes from a string:
 * String plainText = PrismaticAPI.stripAll("&aColored &bText");
 * </code></pre>
 * </p>
 *
 * @see ChatColor
 * @see ColorPattern
 */
@UtilityClass
public class PrismaticAPI {

    /**
     * A mapping between AWT {@link Color} objects and Bukkit {@link ChatColor} objects.
     * This map is built using a {@link ImmutableMap#builder} and contains the standard Minecraft color values.
     */
    private final Map<Color, ChatColor> COLOR_MAP = ImmutableMap.<Color, ChatColor>builder()
            .put(new Color(0), ChatColor.getByChar('0'))
            .put(new Color(170), ChatColor.getByChar('1'))
            .put(new Color(43520), ChatColor.getByChar('2'))
            .put(new Color(43690), ChatColor.getByChar('3'))
            .put(new Color(11141120), ChatColor.getByChar('4'))
            .put(new Color(11141290), ChatColor.getByChar('5'))
            .put(new Color(16755200), ChatColor.getByChar('6'))
            .put(new Color(11184810), ChatColor.getByChar('7'))
            .put(new Color(5592405), ChatColor.getByChar('8'))
            .put(new Color(5592575), ChatColor.getByChar('9'))
            .put(new Color(5635925), ChatColor.getByChar('a'))
            .put(new Color(5636095), ChatColor.getByChar('b'))
            .put(new Color(16733525), ChatColor.getByChar('c'))
            .put(new Color(16733695), ChatColor.getByChar('d'))
            .put(new Color(16777045), ChatColor.getByChar('e'))
            .put(new Color(16777215), ChatColor.getByChar('f'))
            .build();

    /**
     * Retrieves the {@link ChatColor} closest to the specified {@link Color} from the color map.
     *
     * @param color the AWT color to match
     * @return the closest corresponding {@link ChatColor}
     */
    private ChatColor getClosestColor(Color color) {
        Color nearestColor = null;
        double nearestDistance = Integer.MAX_VALUE;
        for (Color c : COLOR_MAP.keySet()) {
            double d = Math.pow(color.getRed() - c.getRed(), 2) +
                    Math.pow(color.getBlue() - c.getBlue(), 2) +
                    Math.pow(color.getGreen() - c.getGreen(), 2);
            if (nearestDistance <= d) continue;
            nearestColor = c;
            nearestDistance = d;
        }
        return COLOR_MAP.get(nearestColor);
    }

    /**
     * Retrieves a {@link ChatColor} for the given {@link Color}, taking into account legacy mode.
     *
     * @param color    the AWT color to convert
     * @param isLegacy if {@code true}, legacy colors are used (closest match); otherwise, modern RGB support is used
     * @return the corresponding {@link ChatColor}
     */
    private ChatColor getBukkit(Color color, boolean isLegacy) {
        return isLegacy ? getClosestColor(color) : ChatColor.of(color);
    }

    /**
     * Converts a hexadecimal color string into a {@link ChatColor}.
     *
     * @param string   the hexadecimal color code (without the leading '#' character)
     * @param isLegacy if {@code true}, legacy color mode is used
     * @return the resulting {@link ChatColor}
     */
    public ChatColor fromString(String string, boolean isLegacy) {
        return getBukkit(new Color(Integer.parseInt(string, 16)), isLegacy);
    }

    /**
     * Creates an array of {@link ChatColor} objects representing a gradient between two colors.
     *
     * @param start    the starting color of the gradient
     * @param end      the ending color of the gradient
     * @param step     the number of steps (colors) in the gradient
     * @param isLegacy if {@code true}, legacy color mode is used
     * @return an array of {@link ChatColor} forming the gradient
     */
    private ChatColor[] createGradient(Color start, Color end, int step, boolean isLegacy) {
        ChatColor[] colors = new ChatColor[step];
        int stepR = Math.abs(start.getRed() - end.getRed()) / (step - 1),
                stepG = Math.abs(start.getGreen() - end.getGreen()) / (step - 1),
                stepB = Math.abs(start.getBlue() - end.getBlue()) / (step - 1);
        int[] direction = new int[] {
                start.getRed() < end.getRed() ? +1 : -1,
                start.getGreen() < end.getGreen() ? +1 : -1,
                start.getBlue() < end.getBlue() ? +1 : -1
        };
        for (int i = 0; i < step; i++) {
            Color color = new Color(
                    start.getRed() + ((stepR * i) * direction[0]),
                    start.getGreen() + ((stepG * i) * direction[1]),
                    start.getBlue() + ((stepB * i) * direction[2])
            );
            colors[i] = getBukkit(color, isLegacy);
        }
        return colors;
    }

    /**
     * Creates an array of {@link ChatColor} objects representing a rainbow effect.
     *
     * @param step     the number of colors in the rainbow
     * @param sat      the saturation level (0.0 to 1.0)
     * @param isLegacy if {@code true}, legacy color mode is used
     * @return an array of {@link ChatColor} forming a rainbow
     */
    private ChatColor[] createRainbow(int step, float sat, boolean isLegacy) {
        ChatColor[] colors = new ChatColor[step];
        double colorStep = (1.00 / step);
        for (int i = 0; i < step; i++) {
            Color color = Color.getHSBColor((float) (colorStep * i), sat, sat);
            colors[i] = getBukkit(color, isLegacy);
        }
        return colors;
    }

    /**
     * Prepends a given string with the {@link ChatColor} corresponding to the provided color.
     *
     * @param color    the color to apply
     * @param string   the string to colorize
     * @param isLegacy if {@code true}, legacy color mode is used
     * @return the colorized string
     */
    public String applyColor(Color color, String string, boolean isLegacy) {
        return getBukkit(color, isLegacy) + string;
    }

    /**
     * Applies an array of {@link ChatColor} objects sequentially to each character of the source string.
     * <p>
     * The method preserves special color codes and applies the next color from the array for each character.
     * </p>
     *
     * @param source the source string to apply colors to
     * @param colors the array of {@link ChatColor} to apply
     * @return the resulting string with applied colors
     */
    private String apply(String source, ChatColor[] colors) {
        StringBuilder specials = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(source)) return source;
        String[] characters = source.split("");
        int outIndex = 0;
        for (int i = 0; i < characters.length; i++) {
            if (!characters[i].matches("[&§]") || i + 1 >= characters.length) {
                builder.append(colors[outIndex++])
                        .append(specials)
                        .append(characters[i]);
                continue;
            }
            if (characters[i + 1].equals("r")) specials.setLength(0);
            else specials.append(characters[i]).append(characters[i + 1]);
            i++;
        }
        return builder.toString();
    }

    /**
     * Applies a gradient color effect to the given string.
     *
     * @param string   the string to apply the gradient to
     * @param start    the starting color of the gradient
     * @param end      the ending color of the gradient
     * @param isLegacy if {@code true}, legacy color mode is used
     * @return the string with a gradient effect applied
     */
    public String applyGradient(String string, Color start, Color end, boolean isLegacy) {
        int i = stripSpecial(string).length();
        return i <= 1 ? string : apply(string, createGradient(start, end, i, isLegacy));
    }

    /**
     * Applies a rainbow color effect to the given string.
     *
     * @param string     the string to apply the rainbow effect to
     * @param saturation the saturation level for the rainbow (0.0 to 1.0)
     * @param isLegacy   if {@code true}, legacy color mode is used
     * @return the string with a rainbow effect applied
     */
    public String applyRainbow(String string, float saturation, boolean isLegacy) {
        int i = stripSpecial(string).length();
        return i == 0 ? string : apply(string, createRainbow(i, saturation, isLegacy));
    }

    /**
     * Colorizes the given string by applying all registered {@link ColorPattern} effects.
     * <p>
     * The method determines if legacy color formatting is needed based on the server version and player client.
     * Finally, it translates alternate color codes using Bukkit's translation method.
     * </p>
     *
     * @param player the player for whom the colorization context is applied (may be {@code null})
     * @param string the string to colorize
     * @return the colorized string
     */
    public String colorize(Player player, String string) {
        boolean isLegacy = ClientVersion.SERVER_VERSION < 16.0;
        if (player != null)
            isLegacy = isLegacy || ClientVersion.isLegacy(player);
        for (ColorPattern p : ColorPattern.COLOR_PATTERNS)
            string = p.apply(string, isLegacy);
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Colorizes the given string without any player context.
     *
     * @param string the string to colorize
     * @return the colorized string
     */
    public String colorize(String string) {
        return colorize(null, string);
    }

    /**
     * Strips Bukkit color codes (e.g. {@code &a}, {@code §b}) from the provided string.
     *
     * @param string the string from which to remove color codes
     * @return the string without Bukkit color codes
     */
    public String stripBukkit(String string) {
        if (StringUtils.isBlank(string)) return string;
        Matcher m = Pattern.compile("(?i)[&§][a-f\\dx]").matcher(string);
        while (m.find())
            string = string.replace(m.group(), "");
        return string;
    }

    /**
     * Strips special formatting codes (e.g. magic, bold, etc.) from the provided string.
     *
     * @param string the string from which to remove special formatting
     * @return the string without special formatting codes
     */
    public String stripSpecial(String string) {
        if (StringUtils.isBlank(string)) return string;
        Matcher m = Pattern.compile("(?i)[&§][k-orx]").matcher(string);
        while (m.find())
            string = string.replace(m.group(), "");
        return string;
    }

    /**
     * Strips RGB color codes applied by {@link ColorPattern} implementations from the provided string.
     *
     * @param string the string from which to remove RGB color codes
     * @return the string without RGB color codes
     */
    public String stripRGB(String string) {
        for (ColorPattern p : ColorPattern.COLOR_PATTERNS)
            string = p.strip(string);
        return string;
    }

    /**
     * Strips all types of color and formatting codes from the provided string.
     *
     * @param string the string from which to remove all formatting
     * @return the plain string without any color or formatting codes
     */
    public String stripAll(String string) {
        return stripRGB(stripSpecial(stripBukkit(string)));
    }

    private final String COLOR_PATTERN = "(?i)" +
            "(?<!§x)(?<!§x§[0-9A-F])" +
            "(?<!§x(?:§[0-9A-F]){2})" +
            "(?<!§x(?:§[0-9A-F]){3})" +
            "(?<!§x(?:§[0-9A-F]){4})" +
            "(?<!§x(?:§[0-9A-F]){5})" +
            "(?>§x(?:§[0-9A-F]){6}|§[0-9A-FK-OR])";

    /**
     * Checks if the provided string starts with a valid color code.
     *
     * @param string the string to check
     * @return {@code true} if the string starts with a color code; {@code false} otherwise
     */
    public boolean startsWithColor(String string) {
        if (StringUtils.isBlank(string)) return false;
        string = colorize(string);
        Matcher matcher = Pattern.compile("^" + COLOR_PATTERN).matcher(string);
        return matcher.find();
    }

    /**
     * Retrieves the start color code found in the provided string.
     *
     * @param string the string to search for color codes
     * @return the start color code as a {@link String}, or {@code null} if none is found
     */
    @Nullable
    public String getStartColor(String string) {
        string = colorize(string);
        Matcher matcher = Pattern.compile(COLOR_PATTERN).matcher(string);
        String color = null;
        if (matcher.find()) color = matcher.group();
        return color;
    }

    /**
     * Retrieves the end color code found in the provided string.
     *
     * @param string the string to search for color codes
     * @return the end color code as a {@link String}, or {@code null} if none is found
     */
    @Nullable
    public String getEndColor(String string) {
        string = colorize(string);
        Matcher matcher = Pattern.compile(COLOR_PATTERN).matcher(string);
        String color = null;
        while (matcher.find()) color = matcher.group();
        return color;
    }
}
