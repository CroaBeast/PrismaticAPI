package me.croabeast.prismatic.color;

import me.croabeast.prismatic.PrismaticAPI;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements a color formatting strategy that applies single, discrete color changes to text.
 * <p>
 * The {@code SingleColor} class is an implementation of the {@link ColorPattern} interface that aggregates
 * several simple color patterns. Each pattern corresponds to a different syntax for specifying a single color,
 * such as hexadecimal color codes enclosed in various delimiters.
 * </p>
 * <p>
 * During instantiation, {@code SingleColor} registers multiple color patterns (e.g., "{#RRGGBB}", "%#RRGGBB%",
 * "[#RRGGBB]", "<#RRGGBB>", "&xRRGGBB", and "&?#RRGGBB") that are used to identify and replace color codes
 * in strings.
 * </p>
 * <p>
 * When the {@link #apply(String, boolean)} method is invoked, the instance iterates through all registered
 * color patterns, applying each one to convert the color code to a corresponding {@link ChatColor}. Conversely,
 * the {@link #strip(String)} method removes all such color codes.
 * </p>
 *
 * @see ColorPattern
 * @see PrismaticAPI#fromString(String, boolean)
 */
class SingleColor implements ColorPattern {

    /**
     * A set of simple color patterns that this instance applies sequentially.
     */
    private final Set<ColorPattern> colors = new LinkedHashSet<>();

    /**
     * Constructs a new {@code SingleColor} instance and registers multiple simple color patterns.
     * <p>
     * Patterns supported include various syntaxes for representing hexadecimal color codes.
     * </p>
     */
    SingleColor() {
        new Color("[{]#([a-f\\d]{6})[}]");
        new Color("%#([a-f\\d]{6})%");
        new Color("\\[#([a-f\\d]{6})]");
        new Color("<#([a-f\\d]{6})>");
        new Color("&x([a-f\\d]{6})");
        new Color("&?#([a-f\\d]{6})");
    }

    /**
     * Applies all registered simple color patterns to the provided string.
     * <p>
     * Each registered pattern transforms the string by replacing matched color codes with their corresponding
     * {@link ChatColor} representations, depending on whether legacy mode is enabled.
     * </p>
     *
     * @param string   the string to be colorized
     * @param legacy {@code true} to apply legacy formatting; {@code false} for modern RGB colors
     * @return the colorized string
     */
    @Override
    public @NotNull String apply(String string, boolean legacy) {
        for (ColorPattern color : colors)
            string = color.apply(string, legacy);
        return string;
    }

    /**
     * Strips all registered color patterns from the provided string.
     * <p>
     * This method iterates through each registered pattern and removes any color formatting,
     * returning a plain, unformatted string.
     * </p>
     *
     * @param string the string from which to remove color formatting
     * @return the plain string with all color codes removed
     */
    @Override
    public @NotNull String strip(String string) {
        for (ColorPattern color : colors)
            string = color.strip(string);
        return string;
    }

    /**
     * A private inner class that represents a simple, single color pattern.
     * <p>
     * Each {@code Color} instance compiles a specific regular expression pattern used to identify
     * a particular format of hexadecimal color code in text. When applied, it converts the matched
     * color code into its corresponding {@link ChatColor} string using the {@link PrismaticAPI}.
     * </p>
     */
    private class Color implements ColorPattern {

        /**
         * The compiled regular expression pattern used to match a color code.
         */
        private final Pattern pattern;

        /**
         * Constructs a new {@code Color} instance with the given regex pattern.
         * <p>
         * The pattern is compiled in a case-insensitive manner. This instance is then added to the
         * parent {@code SingleColor} instance's set of color patterns.
         * </p>
         *
         * @param pattern the regex pattern string for matching a color code (must not be blank)
         */
        Color(String pattern) {
            this.pattern = Pattern.compile("(?i)" + pattern);
            colors.add(this);
        }

        /**
         * Applies this simple color pattern to the given string.
         * <p>
         * The method finds all occurrences of the color pattern in the string, converts each matched hexadecimal
         * color code to a {@link ChatColor} using {@link PrismaticAPI#fromString(String, boolean)}, and replaces
         * the matched substring with the resulting color code.
         * </p>
         *
         * @param string   the input string to transform
         * @param legacy {@code true} to use legacy color formatting; {@code false} for modern formatting
         * @return the string with this color pattern applied
         */
        @Override
        public @NotNull String apply(String string, boolean legacy) {
            Matcher m = pattern.matcher(string);
            while (m.find()) {
                ChatColor c = PrismaticAPI.fromString(m.group(1), legacy);
                string = string.replace(m.group(), c.toString());
            }
            return string;
        }

        /**
         * Strips this color pattern from the given string.
         * <p>
         * All substrings that match the color pattern are removed from the string.
         * </p>
         *
         * @param string the input string from which to remove the color codes
         * @return the string with this color pattern removed
         */
        @Override
        public @NotNull String strip(String string) {
            Matcher m = pattern.matcher(string);
            while (m.find())
                string = string.replace(m.group(), "");
            return string;
        }
    }
}
