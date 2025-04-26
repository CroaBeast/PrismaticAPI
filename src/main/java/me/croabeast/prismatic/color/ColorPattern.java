package me.croabeast.prismatic.color;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Defines a contract for applying and stripping color formatting to strings.
 * <p>
 * Implementations of {@code ColorPattern} are responsible for transforming text by inserting color codes
 * (e.g. for gradients, rainbow effects, or single color changes) and for removing such formatting from text.
 * This is useful in contexts where dynamic text colorization is desired, such as chat messages, GUI displays,
 * or custom logging.
 * </p>
 * A default list of available color patterns is provided by the constant {@link #COLOR_PATTERNS},
 * which includes implementations like {@code MultiColor} and {@code SingleColor}.
 * </p>
 *
 * @see MultiColor
 * @see SingleColor
 */
public interface ColorPattern {

    /**
     * A default list of color patterns available for processing text.
     */
    List<ColorPattern> COLOR_PATTERNS = Arrays.asList(new MultiColor(), new SingleColor());

    /**
     * Strips the color formatting applied by this pattern from the given string.
     * <p>
     * This method removes any inserted color codes, resulting in a plain text string.
     * </p>
     *
     * @param string the text from which to remove color formatting
     * @return the plain text string with all color codes removed
     */
    @NotNull String strip(String string);

    /**
     * Applies the color pattern to the given string.
     * <p>
     * This method transforms the input text by inserting color codes according to the pattern's rules.
     * The {@code legacy} flag specifies whether legacy color formatting should be used.
     * </p>
     *
     * @param string   the input text to which the color pattern will be applied
     * @param legacy {@code true} if legacy formatting (e.g. 16-color mode) should be used; {@code false} for modern RGB support
     * @return the transformed, colorized string
     */
    @NotNull String apply(String string, boolean legacy);
}
