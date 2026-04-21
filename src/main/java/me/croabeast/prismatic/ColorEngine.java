package me.croabeast.prismatic;

import com.google.common.collect.ImmutableMap;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ColorEngine {

    private static final Map<Color, ChatColor> COLOR_MAP = ImmutableMap.<Color, ChatColor>builder()
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

    private static final Pattern BUKKIT_COLOR_PATTERN = Pattern.compile("(?i)[&§][a-f\\dx]");
    private static final Pattern SPECIAL_COLOR_PATTERN = Pattern.compile("(?i)[&§][k-orx]");

    private static final String COLOR_PATTERN = "(?i)" +
            "(?<!§x)(?<!§x§[0-9A-F])" +
            "(?<!§x(?:§[0-9A-F]){2})" +
            "(?<!§x(?:§[0-9A-F]){3})" +
            "(?<!§x(?:§[0-9A-F]){4})" +
            "(?<!§x(?:§[0-9A-F]){5})" +
            "(?>§x(?:§[0-9A-F]){6}|§[0-9A-FK-OR])";

    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile(COLOR_PATTERN);
    private static final Pattern START_COLOR_CODE_PATTERN = Pattern.compile("^" + COLOR_PATTERN);

    ChatColor fromString(String string, boolean legacy) {
        return getBukkit(new Color(Integer.parseInt(string, 16)), legacy);
    }

    ChatColor fromString(String string) {
        if (StringUtils.isBlank(string))
            return ChatColor.WHITE;

        ChatColor color = ChatColor.WHITE;

        if (string.matches("^[&§]x"))
            string = string.substring(2);
        string = string.replace("&", "").replace("§", "");

        if (string.length() == 1 &&
                ((color = ChatColor.getByChar(string.toCharArray()[0])) != null))
            return color;

        if (string.length() == 6) {
            try {
                color = ChatColor.of('#' + string);
            } catch (Exception ignored) {}
        }

        return color;
    }

    String applyColor(Color color, String string, boolean legacy) {
        return getBukkit(color, legacy) + string;
    }

    String applyGradient(String string, Color start, Color end, boolean legacy) {
        int length = stripSpecial(string).length();
        return length <= 1 ? string : apply(string, createGradient(start, end, length, legacy));
    }

    String applyRainbow(String string, float saturation, boolean legacy) {
        int length = stripSpecial(string).length();
        return length == 0 ? string : apply(string, createRainbow(length, saturation, legacy));
    }

    String stripBukkit(String string) {
        if (StringUtils.isBlank(string)) return string;
        return BUKKIT_COLOR_PATTERN.matcher(string).replaceAll("");
    }

    String stripSpecial(String string) {
        if (StringUtils.isBlank(string)) return string;
        return SPECIAL_COLOR_PATTERN.matcher(string).replaceAll("");
    }

    boolean startsWithColor(String string) {
        if (StringUtils.isBlank(string)) return false;
        return START_COLOR_CODE_PATTERN.matcher(string).find();
    }

    @Nullable
    String getStartColor(String string) {
        Matcher matcher = COLOR_CODE_PATTERN.matcher(string);
        String color = null;
        if (matcher.find())
            color = matcher.group();
        return color;
    }

    @Nullable
    String getEndColor(String string) {
        Matcher matcher = COLOR_CODE_PATTERN.matcher(string);
        String color = null;
        while (matcher.find())
            color = matcher.group();
        return color;
    }

    private ChatColor getClosestColor(Color color) {
        Color nearestColor = null;
        double nearestDistance = Integer.MAX_VALUE;
        for (Color entry : COLOR_MAP.keySet()) {
            double distance = Math.pow(color.getRed() - entry.getRed(), 2) +
                    Math.pow(color.getBlue() - entry.getBlue(), 2) +
                    Math.pow(color.getGreen() - entry.getGreen(), 2);

            if (nearestDistance <= distance)
                continue;

            nearestColor = entry;
            nearestDistance = distance;
        }
        return COLOR_MAP.get(nearestColor);
    }

    private ChatColor getBukkit(Color color, boolean legacy) {
        return legacy ? getClosestColor(color) : ChatColor.of(color);
    }

    private ChatColor[] createGradient(Color start, Color end, int step, boolean legacy) {
        ChatColor[] colors = new ChatColor[step];
        int stepR = Math.abs(start.getRed() - end.getRed()) / (step - 1);
        int stepG = Math.abs(start.getGreen() - end.getGreen()) / (step - 1);
        int stepB = Math.abs(start.getBlue() - end.getBlue()) / (step - 1);
        int[] direction = new int[] {
                start.getRed() < end.getRed() ? 1 : -1,
                start.getGreen() < end.getGreen() ? 1 : -1,
                start.getBlue() < end.getBlue() ? 1 : -1
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
        double colorStep = 1.00 / step;

        for (int i = 0; i < step; i++) {
            Color color = Color.getHSBColor((float) (colorStep * i), sat, sat);
            colors[i] = getBukkit(color, legacy);
        }

        return colors;
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
                if (Character.toLowerCase(code) == 'r')
                    specials.setLength(0);
                else
                    specials.append(current).append(code);
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
}
