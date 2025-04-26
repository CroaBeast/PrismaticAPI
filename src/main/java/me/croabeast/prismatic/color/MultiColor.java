package me.croabeast.prismatic.color;

import me.croabeast.prismatic.PrismaticAPI;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements a composite color pattern that supports multi-color formatting effects,
 * such as gradients and rainbows, applied to strings.
 * <p>
 * The {@code MultiColor} class aggregates several {@link ColorPattern} instances that transform
 * input text by inserting dynamic color codes. It leverages internal implementations (such as
 * {@link Gradient} and {@link Rainbow}) to process complex color formats.
 * </p>
 * <p>
 * When the {@code apply} method is called, the {@code MultiColor} instance iterates over its internal
 * collection of color patterns and applies each transformation sequentially. The {@code strip} method
 * similarly removes color formatting from the input string.
 * </p>
 * <p>
 * The class also provides static utility methods to split strings for gradient calculations and
 * to convert hexadecimal color strings into {@link Color} objects.
 * </p>
 *
 * @see ColorPattern
 * @see PrismaticAPI
 */
class MultiColor implements ColorPattern {

    /**
     * A set of internal color patterns that are applied sequentially.
     */
    private final Set<ColorPattern> colors = new LinkedHashSet<>();

    /**
     * Constructs a regular expression pattern for matching gradient color formats.
     * <p>
     * The pattern is based on a prefix and expects a hexadecimal color code.
     * </p>
     *
     * @param prefix the prefix to use for the color code (e.g. "#" or "g:")
     * @return a regex pattern string for gradient formatting
     */
    static String gradientPattern(String prefix) {
        String hex = prefix + "([\\da-f]{6})";
        return "<" + hex + ">(.+?)</" + hex + ">";
    }

    /**
     * Constructs a regular expression pattern for matching rainbow color formats.
     * <p>
     * The pattern uses a prefix and a numerical value for saturation.
     * </p>
     *
     * @param prefix the prefix to use for the pattern (e.g. "rainbow")
     * @return a regex pattern string for rainbow formatting
     */
    static String rainbowPattern(String prefix) {
        return "<" + prefix + ":(\\d{1,3})>(.+?)</" + prefix + ">";
    }

    /**
     * Splits a given text into a specified number of parts.
     * <p>
     * If {@code parts} is less than 2, the entire text is returned as a single-element array.
     * Otherwise, the text is divided into approximately equal parts.
     * </p>
     *
     * @param text  the text to split
     * @param parts the number of parts to split the text into
     * @return an array of substrings
     */
    static String[] splitString(String text, int parts) {
        if (parts < 2) return new String[] {text};
        String[] list = new String[parts];
        double length = text.length();
        int start = 0;
        for (int i = 0; i < parts; i++) {
            int chars = (int) Math.ceil((length - start) / (parts - i));
            int end = start + chars;
            list[i] = text.substring(start, end);
            start = end;
        }
        return list;
    }

    /**
     * Converts a hexadecimal color string into an AWT {@link Color} instance.
     *
     * @param line a string containing a hexadecimal color value
     * @return the {@link Color} object representing the hex color
     */
    static Color getColor(String line) {
        return new Color(Integer.parseInt(line, 16));
    }

    /**
     * Constructs a new {@code MultiColor} instance.
     * <p>
     * During initialization, several internal color patterns are added:
     * a custom pattern, two gradient patterns, and two rainbow patterns.
     * </p>
     */
    MultiColor() {
        colors.add(new ColorPattern() {
            final Pattern custom = Pattern.compile("<(#([a-f\\d]{6})(:#([a-f\\d]{6}))+)>(.+?)</g(radient)?>");

            @Override
            public @NotNull String apply(String string, boolean legacy) {
                Matcher m = custom.matcher(string);
                while (m.find()) {
                    String[] colors = m.group(1).split(":");
                    int count = colors.length - 1;
                    String[] text = splitString(m.group(5), count);
                    StringBuilder result = new StringBuilder();
                    int i = 0;
                    while (i < count) {
                        String textPart = text[i];
                        if (i > 0) {
                            final String prev = text[i - 1];
                            int l = prev.length() - 1;
                            char last = prev.toCharArray()[l];
                            textPart = last + textPart;
                        }
                        textPart = PrismaticAPI.applyGradient(
                                textPart,
                                getColor(colors[i]),
                                getColor(colors[i + 1]),
                                legacy
                        );
                        result.append(i > 0 ? textPart.substring(15) : textPart);
                        i++;
                    }
                    string = string.replace(m.group(), result);
                }
                return string;
            }

            @Override
            public @NotNull String strip(String string) {
                Matcher m = custom.matcher(string);
                while (m.find())
                    string = string.replace(m.group(), m.group(5));
                return string;
            }
        });
        new Gradient("g:");
        new Gradient("#");
        new Rainbow("rainbow");
        new Rainbow("r");
    }

    /**
     * Applies all registered color patterns to the provided string.
     * <p>
     * Each pattern in the internal set is applied sequentially.
     * </p>
     *
     * @param string   the input string to process
     * @param legacy {@code true} to use legacy color formatting; {@code false} for modern formatting
     * @return the colorized string after all transformations have been applied
     */
    @Override
    public @NotNull String apply(String string, boolean legacy) {
        for (ColorPattern color : colors)
            string = color.apply(string, legacy);
        return string;
    }

    /**
     * Strips all color formatting from the provided string by applying the {@code strip} method of each internal pattern.
     *
     * @param string the input string from which to remove color codes
     * @return the plain string without any color formatting
     */
    @Override
    public @NotNull String strip(String string) {
        for (ColorPattern color : colors)
            string = color.strip(string);
        return string;
    }

    /**
     * A private inner class that implements a gradient color pattern.
     * <p>
     * The {@code Gradient} class applies a gradient effect to portions of text that are wrapped in
     * specific markers defined by a prefix. It uses regular expressions to parse the input string,
     * splits the text accordingly, and then applies a color gradient via {@link PrismaticAPI#applyGradient}.
     * </p>
     */
    private class Gradient implements ColorPattern {

        private final Pattern pattern;
        private final BiFunction<String, Boolean, String> applier;
        private final UnaryOperator<String> stripOperator;

        /**
         * Constructs a new {@code Gradient} color pattern with the specified prefix.
         *
         * @param prefix the prefix used to define the gradient pattern (e.g., "g:" or "#")
         */
        Gradient(String prefix) {
            pattern = Pattern.compile("(?i)" + gradientPattern(prefix));
            applier = (string, legacy) -> {
                Matcher matcher = Gradient.this.pattern.matcher(string);
                while (matcher.find()) {
                    String x = matcher.group(1), text = matcher.group(2),
                            z = matcher.group(3),
                            r = "(?i)<" + prefix + "([\\da-f]{6})>";
                    Matcher inside = Pattern.compile(r).matcher(text);
                    String[] array = text.split(r);
                    List<String> ids = new ArrayList<>();
                    ids.add(x);
                    while (inside.find()) ids.add(inside.group(1));
                    ids.add(z);
                    StringBuilder result = new StringBuilder();
                    int i = 0;
                    while (i < ids.size() - 1) {
                        result.append(PrismaticAPI.applyGradient(
                                array[i],
                                getColor(ids.get(i)),
                                getColor(ids.get(i + 1)),
                                legacy
                        ));
                        i++;
                    }
                    string = string.replace(matcher.group(), result);
                }
                return string;
            };
            stripOperator = (string) -> {
                Matcher matcher = Gradient.this.pattern.matcher(string);
                while (matcher.find()) {
                    String[] array = matcher.group(2).split("(?i)<" + prefix + "([\\da-f]{6})>");
                    string = string.replace(matcher.group(), String.join("", array));
                }
                return string;
            };
            colors.add(this);
        }

        @Override
        public @NotNull String apply(String string, boolean legacy) {
            return applier.apply(string, legacy);
        }

        @Override
        public @NotNull String strip(String string) {
            return stripOperator.apply(string);
        }
    }

    /**
     * A private inner class that implements a rainbow color pattern.
     * <p>
     * The {@code Rainbow} class applies a rainbow effect to text based on a numerical parameter
     * that indicates the intensity or step of the rainbow. It uses regular expressions to identify
     * segments to transform and applies the rainbow effect using {@link PrismaticAPI#applyRainbow}.
     * </p>
     */
    private class Rainbow implements ColorPattern {

        private final Pattern pattern;
        private final BiFunction<String, Boolean, String> applier;
        private final UnaryOperator<String> stripOperator;

        /**
         * Constructs a new {@code Rainbow} color pattern with the specified prefix.
         *
         * @param prefix the prefix used to define the rainbow pattern (e.g., "rainbow" or "r")
         */
        Rainbow(String prefix) {
            pattern = Pattern.compile("(?i)" + rainbowPattern(prefix));
            applier = (string, legacy) -> {
                Matcher matcher = Rainbow.this.pattern.matcher(string);
                while (matcher.find()) {
                    String g = matcher.group(), c = matcher.group(2);
                    float f = Float.parseFloat(matcher.group(1));
                    String temp = PrismaticAPI.applyRainbow(c, f, legacy);
                    string = string.replace(g, temp);
                }
                return string;
            };
            stripOperator = (string) -> {
                Matcher matcher = Rainbow.this.pattern.matcher(string);
                while (matcher.find())
                    string = string.replace(matcher.group(), matcher.group(2));
                return string;
            };
            colors.add(this);
        }

        @Override
        public @NotNull String apply(String string, boolean legacy) {
            return applier.apply(string, legacy);
        }

        @Override
        public @NotNull String strip(String string) {
            return stripOperator.apply(string);
        }
    }
}
