package me.croabeast.prismatic.color;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Low-level contract for Prismatic color-pattern processors.
 *
 * <p>Implementations transform raw strings by either applying or stripping Prismatic-specific color syntax
 * such as gradients, rainbows and single RGB markers. Most plugin code should prefer
 * {@link me.croabeast.prismatic.PrismaticAPI} rather than interacting with this SPI directly.
 *
 * <p>The built-in processors are exposed through {@link #MULTI}, {@link #SINGLE} and
 * {@link #COLOR_PATTERNS}.
 */
public interface ColorPattern {

    /**
     * Built-in processor for gradients, rainbows and other multi-color Prismatic tags.
     */
    ColorPattern MULTI = new MultiColor();

    /**
     * Built-in processor for single RGB tokens such as {@code {#ff8800}} or {@code &#ff8800}.
     */
    ColorPattern SINGLE = new SingleColor();

    /**
     * Immutable ordered list of the built-in processors used by the default Prismatic pipeline.
     * <p>
     * The order is always:
     * <ol>
     *     <li>multi-color (gradients/rainbows)</li>
     *     <li>single-color</li>
     * </ol>
     * </p>
     */
    List<ColorPattern> COLOR_PATTERNS = Collections.unmodifiableList(Arrays.asList(MULTI, SINGLE));

    /**
     * Removes the syntax handled by this pattern from a string.
     *
     * @param string the text from which to remove color formatting
     * @return the plain text string with all color codes removed
     */
    @NotNull
    String strip(String string);

    /**
     * Applies this pattern to a string.
     *
     * <p>The {@code legacy} flag allows implementations to choose between exact RGB output and downsampled
     * legacy Bukkit colors.
     *
     * @param string   the input text to which the color pattern will be applied
     * @param legacy {@code true} if legacy formatting (e.g. 16-color mode) should be used; {@code false} for modern RGB support
     * @return the transformed, colorized string
     */
    @NotNull
    String apply(String string, boolean legacy);
}
