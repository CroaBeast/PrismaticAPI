package me.croabeast.prismatic;

import com.google.common.collect.ImmutableMap;
import lombok.experimental.UtilityClass;
import me.croabeast.prismatic.color.ColorPattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Central utility for color and text formatting operations used by PrismaticAPI.
 * <p>
 * This class provides a single entry-point to:
 * </p>
 * <ul>
 *     <li>Parse single RGB colors and legacy color codes.</li>
 *     <li>Apply advanced effects such as gradients and rainbows.</li>
 *     <li>Process MiniMessage markup when it is available at runtime.</li>
 *     <li>Convert the final result into either legacy strings or Adventure components.</li>
 *     <li>Strip color/special formatting from already formatted text.</li>
 * </ul>
 * <p>
 * The main processing pipeline used by {@link #colorize(Player, String)} is:
 * MiniMessage (optional) -> multi-color formats (gradient/rainbow) -> single RGB colors
 * -> legacy ampersand translation.
 * </p>
 * <p>
 * Runtime compatibility is preserved by detecting MiniMessage through reflection.
 * If MiniMessage is not present, related parsing methods become safe no-ops and the
 * rest of the formatting pipeline continues normally.
 * </p>
 */
@UtilityClass
public class PrismaticAPI {

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

    private final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private final Pattern BUKKIT_COLOR_PATTERN = Pattern.compile("(?i)[&§][a-f\\dx]");
    private final Pattern SPECIAL_COLOR_PATTERN = Pattern.compile("(?i)[&§][k-orx]");

    private final Pattern MINI_MESSAGE_HASH_GRADIENT_SECTION =
            Pattern.compile("(?is)<#([a-f\\d]{6})>(.+?)</#([a-f\\d]{6})>");
    private final Pattern MINI_MESSAGE_HASH_COLOR_TAG = Pattern.compile("(?i)<#([a-f\\d]{6})>");
    private final Pattern[] MINI_MESSAGE_PROTECTED_PATTERNS = new Pattern[] {
            Pattern.compile("(?i)<#([a-f\\d]{6})(:#([a-f\\d]{6}))+>"),
            Pattern.compile("(?i)</g(radient)?>"),
            Pattern.compile("(?i)<g:([a-f\\d]{6})>"),
            Pattern.compile("(?i)</g:([a-f\\d]{6})>"),
            Pattern.compile("(?i)<gradient:([a-f\\d]{6})>"),
            Pattern.compile("(?i)</gradient:([a-f\\d]{6})>"),
            Pattern.compile("(?i)<rainbow:(\\d{1,3})>"),
            Pattern.compile("(?i)</rainbow>"),
            Pattern.compile("(?i)<r:(\\d{1,3})>"),
            Pattern.compile("(?i)</r>")
    };

    private final MiniMessageBridge MINI_MESSAGE = MiniMessageBridge.create();

    private final String COLOR_PATTERN = "(?i)" +
            "(?<!§x)(?<!§x§[0-9A-F])" +
            "(?<!§x(?:§[0-9A-F]){2})" +
            "(?<!§x(?:§[0-9A-F]){3})" +
            "(?<!§x(?:§[0-9A-F]){4})" +
            "(?<!§x(?:§[0-9A-F]){5})" +
            "(?>§x(?:§[0-9A-F]){6}|§[0-9A-FK-OR])";

    private final Pattern COLOR_CODE_PATTERN = Pattern.compile(COLOR_PATTERN);
    private final Pattern START_COLOR_CODE_PATTERN = Pattern.compile("^" + COLOR_PATTERN);

    /**
     * Checks whether MiniMessage can be used in the current runtime.
     * <p>
     * Detection is performed through reflection so the API can run on servers that
     * do not bundle MiniMessage without throwing class-loading errors.
     * </p>
     *
     * @return {@code true} if MiniMessage was discovered and initialized successfully;
     * {@code false} otherwise
     */
    public boolean isMiniMessageAvailable() {
        return MINI_MESSAGE.available;
    }

    /**
     * Parses MiniMessage markup into a legacy-formatted string if support is available.
     * <p>
     * This method only performs MiniMessage parsing. It does not apply Prismatic gradient
     * and single-color patterns by itself. Those are applied by the full {@link #colorize(Player, String)}
     * pipeline.
     * </p>
     * <p>
     * Custom Prismatic tags are masked before MiniMessage parsing to avoid collisions with
     * MiniMessage-native tags, then restored in the output.
     * </p>
     *
     * @param string raw text that may contain MiniMessage markup
     * @return parsed legacy string if MiniMessage is available; the original input otherwise
     */
    public String miniMessage(String string) {
        return applyMiniMessage(string);
    }

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

    private ChatColor getBukkit(Color color, boolean legacy) {
        return legacy ? getClosestColor(color) : ChatColor.of(color);
    }

    /**
     * Converts a 6-digit hexadecimal color string into a Bukkit {@link ChatColor}.
     * <p>
     * This overload explicitly receives the legacy mode so callers can decide whether
     * to use full RGB support or the nearest legacy fallback color.
     * </p>
     *
     * @param string hexadecimal color value without leading {@code #}, for example {@code ff00aa}
     * @param legacy {@code true} to force legacy fallback mapping, {@code false} for direct RGB
     * @return resolved {@link ChatColor} representation
     */
    public ChatColor fromString(String string, boolean legacy) {
        return getBukkit(new Color(Integer.parseInt(string, 16)), legacy);
    }

    /**
     * Parses a color token into a Bukkit {@link ChatColor} using safe defaults.
     * <p>
     * Supported inputs include:
     * </p>
     * <ul>
     *     <li>single legacy codes (for example {@code a}, {@code l}, {@code r})</li>
     *     <li>6-digit hex values (for example {@code ff00aa})</li>
     *     <li>legacy-prefixed variants like {@code &a}, {@code §a}, or {@code &xff00aa}</li>
     * </ul>
     * <p>
     * If parsing fails or input is not recognized, {@link ChatColor#WHITE} is returned.
     * </p>
     *
     * @param string raw color token
     * @return parsed color, or white when unknown
     */
    public ChatColor fromString(String string) {
        if (StringUtils.isBlank(string)) return ChatColor.WHITE;

        ChatColor color = ChatColor.WHITE;

        if (string.matches("^[&§]x")) string = string.substring(2);
        string = string.replace("&", "").replace("§", "");

        if (string.length() == 1 &&
                ((color = ChatColor.getByChar(string.toCharArray()[0])) != null))
            return color;

        if (string.length() == 6)
            try {
                color = ChatColor.of('#' + string);
            } catch (Exception ignored) {}

        return color;
    }

    private ChatColor[] createGradient(Color start, Color end, int step, boolean legacy) {
        ChatColor[] colors = new ChatColor[step];
        int stepR = Math.abs(start.getRed() - end.getRed()) / (step - 1);
        int stepG = Math.abs(start.getGreen() - end.getGreen()) / (step - 1);
        int stepB = Math.abs(start.getBlue() - end.getBlue()) / (step - 1);
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
            colors[i] = getBukkit(color, legacy);
        }
        return colors;
    }

    private ChatColor[] createRainbow(int step, float sat, boolean legacy) {
        ChatColor[] colors = new ChatColor[step];
        double colorStep = (1.00 / step);
        for (int i = 0; i < step; i++) {
            Color color = Color.getHSBColor((float) (colorStep * i), sat, sat);
            colors[i] = getBukkit(color, legacy);
        }
        return colors;
    }

    /**
     * Applies a single color at the beginning of the provided string.
     *
     * @param color source RGB color
     * @param string text to decorate
     * @param legacy {@code true} to use nearest legacy fallback, {@code false} for direct RGB
     * @return string prefixed with the resolved color code
     */
    public String applyColor(Color color, String string, boolean legacy) {
        return getBukkit(color, legacy) + string;
    }

    /**
     * Applies a single color and returns the result as an Adventure {@link TextComponent}.
     * <p>
     * Legacy mode is inferred from server version through {@link ClientVersion#SERVER_VERSION}.
     * </p>
     *
     * @param color source RGB color
     * @param string text to decorate
     * @return colorized component result
     */
    public TextComponent applyColor(Color color, String string) {
        return LEGACY_SERIALIZER.deserialize(getBukkit(color, ClientVersion.SERVER_VERSION < 16.0) + string);
    }

    private boolean isColorPrefix(char c) {
        return c == '&' || c == '§';
    }

    private boolean isLegacyCode(char c) {
        c = Character.toLowerCase(c);
        return (c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'f') ||
                (c >= 'k' && c <= 'o') ||
                c == 'r' ||
                c == 'x';
    }

    private String apply(String source, ChatColor[] colors) {
        if (StringUtils.isBlank(source))
            return source;

        StringBuilder specials = new StringBuilder();
        StringBuilder builder = new StringBuilder(source.length() * 2);
        char[] characters = source.toCharArray();
        int outIndex = 0;

        for (int i = 0; i < characters.length; i++) {
            char current = characters[i];

            if (isColorPrefix(current) && i + 1 < characters.length && isLegacyCode(characters[i + 1])) {
                char code = characters[i + 1];
                if (Character.toLowerCase(code) == 'r') specials.setLength(0);
                else specials.append(current).append(code);
                i++;
                continue;
            }

            if (outIndex >= colors.length) {
                builder.append(specials).append(current);
                continue;
            }

            builder.append(colors[outIndex++]).append(specials).append(current);
        }

        return builder.toString();
    }

    /**
     * Applies a gradient transition between two colors over the visible characters of a string.
     * <p>
     * Special formatting codes are preserved while colors are distributed by character count.
     * </p>
     *
     * @param string text to process
     * @param start gradient start color
     * @param end gradient end color
     * @param legacy {@code true} to map each gradient step to nearest legacy color, {@code false} for RGB
     * @return gradient-formatted string
     */
    public String applyGradient(String string, Color start, Color end, boolean legacy) {
        int i = stripSpecial(string).length();
        return i <= 1 ? string : apply(string, createGradient(start, end, i, legacy));
    }

    /**
     * Applies a gradient transition and returns the output as an Adventure {@link TextComponent}.
     *
     * @param start gradient start color
     * @param end gradient end color
     * @param string text to process
     * @return gradient-formatted component
     */
    public TextComponent applyGradient(Color start, Color end, String string) {
        int i = stripSpecial(string).length();
        return LEGACY_SERIALIZER.deserialize(i > 1 ?
                apply(string, createGradient(start, end, i, ClientVersion.SERVER_VERSION < 16.0)) :
                string);
    }

    /**
     * Applies a rainbow effect over a string using the provided saturation.
     *
     * @param string text to process
     * @param saturation rainbow saturation parameter used to build HSB colors
     * @param legacy {@code true} to use legacy fallback mapping, {@code false} for RGB output
     * @return rainbow-formatted string
     */
    public String applyRainbow(String string, float saturation, boolean legacy) {
        int i = stripSpecial(string).length();
        return i == 0 ? string : apply(string, createRainbow(i, saturation, legacy));
    }

    /**
     * Applies a rainbow effect and returns the result as an Adventure {@link TextComponent}.
     *
     * @param saturation rainbow saturation parameter used to build HSB colors
     * @param string text to process
     * @return rainbow-formatted component
     */
    public TextComponent applyRainbow(float saturation, String string) {
        int i = stripSpecial(string).length();
        return LEGACY_SERIALIZER.deserialize(i != 0 ?
                apply(string, createRainbow(i, saturation, ClientVersion.SERVER_VERSION < 16.0)) :
                string);
    }

    private boolean resolveLegacy(Player player) {
        if (ClientVersion.SERVER_VERSION < 16.0)
            return true;
        return player != null && ClientVersion.isLegacy(player);
    }

    private String applyRgbPipeline(String string, boolean legacy) {
        string = ColorPattern.MULTI.apply(string, legacy);
        return ColorPattern.SINGLE.apply(string, legacy);
    }

    /**
     * Color order pipeline:
     * <ol>
     *     <li>MiniMessage (if available)</li>
     *     <li>Gradients/rainbows (multi-color)</li>
     *     <li>Single colors</li>
     *     <li>Legacy ampersand translation</li>
     * </ol>
     *
     * This method is the recommended entry-point for user-facing strings.
     * Player context is used to decide whether legacy output is required for
     * old clients (for example when ViaVersion reports pre-1.16 clients).
     *
     * @param player player context, optional
     * @param string raw input
     * @return processed output string with all supported transformations applied
     */
    public String colorize(Player player, String string) {
        if (StringUtils.isBlank(string)) return string;

        boolean legacy = resolveLegacy(player);

        string = applyMiniMessage(string);
        string = applyRgbPipeline(string, legacy);

        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Colorizes text using server-only context (no player-specific compatibility checks).
     *
     * @param string raw input
     * @return processed output string
     */
    public String colorize(String string) {
        return colorize(null, string);
    }

    /**
     * Colorizes text and converts the final legacy string into an Adventure {@link TextComponent}.
     *
     * @param player optional player context for legacy compatibility decisions
     * @param string raw input
     * @return processed Adventure component
     */
    public TextComponent colorizeAsComponent(Player player, String string) {
        return LEGACY_SERIALIZER.deserialize(colorize(player, string));
    }

    /**
     * Colorizes text without player context and returns the result as an Adventure component.
     *
     * @param string raw input
     * @return processed Adventure component
     */
    public TextComponent colorizeAsComponent(String string) {
        return LEGACY_SERIALIZER.deserialize(colorize(string));
    }

    /**
     * Removes Bukkit color tokens from the input text.
     * <p>
     * This targets classic legacy markers such as {@code &a}, {@code §b}, and
     * related short codes recognized by Bukkit.
     * </p>
     *
     * @param string text to sanitize
     * @return string without Bukkit color tokens
     */
    public String stripBukkit(String string) {
        if (StringUtils.isBlank(string))
            return string;
        return BUKKIT_COLOR_PATTERN.matcher(string).replaceAll("");
    }

    /**
     * Removes special style tokens from the input text.
     * <p>
     * This includes style markers such as bold, italic, magic, reset, and the
     * extended marker used by legacy RGB notation.
     * </p>
     *
     * @param string text to sanitize
     * @return string without style/special tokens
     */
    public String stripSpecial(String string) {
        if (StringUtils.isBlank(string))
            return string;
        return SPECIAL_COLOR_PATTERN.matcher(string).replaceAll("");
    }

    /**
     * Removes custom RGB-related formats handled by Prismatic patterns.
     * <p>
     * Both multi-color and single-color custom syntaxes are stripped in order.
     * </p>
     *
     * @param string text to sanitize
     * @return string without custom Prismatic RGB syntax
     */
    public String stripRGB(String string) {
        if (StringUtils.isBlank(string))
            return string;
        string = ColorPattern.MULTI.strip(string);
        return ColorPattern.SINGLE.strip(string);
    }

    /**
     * Removes all known color/style formats from the input text.
     * <p>
     * This combines Bukkit token stripping, special token stripping, and Prismatic
     * custom RGB syntax stripping.
     * </p>
     *
     * @param string text to sanitize
     * @return plain text with formatting removed
     */
    public String stripAll(String string) {
        return stripRGB(stripSpecial(stripBukkit(string)));
    }

    /**
     * Checks whether a string begins with a valid color code after full processing.
     * <p>
     * The input is first colorized through the standard pipeline, then tested against
     * supported legacy and RGB legacy-serialized prefixes.
     * </p>
     *
     * @param string text to inspect
     * @return {@code true} when the processed string starts with a recognized color token
     */
    public boolean startsWithColor(String string) {
        if (StringUtils.isBlank(string))
            return false;
        string = colorize(string);
        return START_COLOR_CODE_PATTERN.matcher(string).find();
    }

    /**
     * Retrieves the first color code found in the processed string.
     *
     * @param string text to inspect
     * @return first color token, or {@code null} when none is present
     */
    @Nullable
    public String getStartColor(String string) {
        string = colorize(string);
        Matcher matcher = COLOR_CODE_PATTERN.matcher(string);
        String color = null;
        if (matcher.find()) color = matcher.group();
        return color;
    }

    /**
     * Retrieves the last color code found in the processed string.
     *
     * @param string text to inspect
     * @return last color token, or {@code null} when none is present
     */
    @Nullable
    public String getEndColor(String string) {
        string = colorize(string);
        Matcher matcher = COLOR_CODE_PATTERN.matcher(string);
        String color = null;
        while (matcher.find()) color = matcher.group();
        return color;
    }

    private String applyMiniMessage(String string) {
        if (StringUtils.isBlank(string) || !MINI_MESSAGE.available || string.indexOf('<') == -1)
            return string;

        List<String> tokens = new ArrayList<>();
        String masked = maskHashGradientSections(string, tokens);
        for (Pattern pattern : MINI_MESSAGE_PROTECTED_PATTERNS)
            masked = maskMatches(masked, pattern, tokens);

        String parsed = MINI_MESSAGE.deserializeToLegacy(masked);
        return restoreTokens(parsed, tokens);
    }

    private String maskHashGradientSections(String string, List<String> tokens) {
        Matcher matcher = MINI_MESSAGE_HASH_GRADIENT_SECTION.matcher(string);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String body = maskMatches(matcher.group(2), MINI_MESSAGE_HASH_COLOR_TAG, tokens);
            String replacement = storeToken("<#" + matcher.group(1) + ">", tokens)
                    + body
                    + storeToken("</#" + matcher.group(3) + ">", tokens);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String maskMatches(String string, Pattern pattern, List<String> tokens) {
        Matcher matcher = pattern.matcher(string);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(storeToken(matcher.group(), tokens)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String restoreTokens(String string, List<String> tokens) {
        for (int i = tokens.size() - 1; i >= 0; i--)
            string = string.replace(token(i), tokens.get(i));
        return string;
    }

    private String storeToken(String value, List<String> tokens) {
        int index = tokens.size();
        tokens.add(value);
        return token(index);
    }

    private String token(int index) {
        return "__PRISM_MM_TOKEN_" + index + "__";
    }

    @SuppressWarnings("all")
    private static final class MiniMessageBridge {

        private final Object instance;
        private final Method deserializeMethod;
        private final Object emptyTagResolver;
        private final boolean available;

        private MiniMessageBridge(Object instance, Method deserializeMethod, Object emptyTagResolver) {
            this.instance = instance;
            this.deserializeMethod = deserializeMethod;
            this.emptyTagResolver = emptyTagResolver;
            this.available = instance != null && deserializeMethod != null;
        }

        private static MiniMessageBridge create() {
            try {
                Class<?> miniMessageClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
                Object miniMessage = miniMessageClass.getMethod("miniMessage").invoke(null);
                try {
                    Class<?> tagResolverClass = Class.forName("net.kyori.adventure.text.minimessage.tag.resolver.TagResolver");
                    Method deserialize = miniMessageClass.getMethod("deserialize", String.class, tagResolverClass);
                    Object emptyResolver = tagResolverClass.getMethod("empty").invoke(null);
                    return new MiniMessageBridge(miniMessage, deserialize, emptyResolver);
                } catch (NoSuchMethodException ignored) {
                    Method deserialize = miniMessageClass.getMethod("deserialize", String.class);
                    return new MiniMessageBridge(miniMessage, deserialize, null);
                }
            } catch (Throwable ignored) {
                return new MiniMessageBridge(null, null, null);
            }
        }

        private String deserializeToLegacy(String string) {
            if (!available)
                return string;

            try {
                Object component = emptyTagResolver == null
                        ? deserializeMethod.invoke(instance, string)
                        : deserializeMethod.invoke(instance, string, emptyTagResolver);
                if (!(component instanceof Component))
                    return string;
                return LEGACY_SERIALIZER.serialize((Component) component);
            } catch (Throwable ignored) {
                return string;
            }
        }
    }
}
