package me.croabeast.prismatic.element;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsing strategy for interactive markup.
 *
 * <p>A format owns the pattern that identifies an interactive segment and knows how to turn a match
 * into an {@link Element}. Everything that does not depend on the syntax, such as splitting bare
 * URLs and carrying the trailing color into the next segment, stays outside.
 *
 * <p>Unlike the contract it replaces, {@link #accept(Matcher)} takes no player: parsing happens once
 * and cannot depend on who will receive the message.
 *
 * @since 2.0.0
 */
public interface MarkupFormat {

    /**
     * Returns the built-in Prismatic format, which parses
     * {@code <action:"argument">text</text>} and {@code <action:"a"|action:"b">text</text>}.
     *
     * @return the default format
     */
    @NotNull
    static MarkupFormat prismatic() {
        return PrismaticMarkup.INSTANCE;
    }

    /**
     * Returns the pattern that identifies an interactive segment.
     *
     * @return the compiled pattern
     */
    @NotNull
    Pattern getPattern();

    /**
     * Builds an element from a match of {@link #getPattern()}.
     *
     * <p>The returned element carries the visible text and the events described by the match. Its
     * {@link Element#raw()} is what {@link #strip(String)} leaves behind.
     *
     * @param matcher a matcher positioned on a match
     * @return the parsed element
     */
    @NotNull
    Element accept(@NotNull Matcher matcher);

    /**
     * Normalizes raw text before it is matched.
     *
     * <p>This is the hook for a consumer that needs its own preparation step, such as a small caps
     * or alignment pass, before Prismatic sees the text.
     *
     * @param raw raw text
     * @return the prepared text
     */
    @NotNull
    default String prepare(String raw) {
        return raw == null ? "" : raw;
    }

    /**
     * Returns whether this format is present in the given text.
     *
     * @param raw text to inspect
     * @return {@code true} when at least one segment matches
     */
    default boolean isFormatted(String raw) {
        return getPattern().matcher(prepare(raw)).find();
    }

    /**
     * Removes the markup from the given text, keeping the visible content of every segment.
     *
     * @param raw text to clean
     * @return the text without markup tags
     */
    @NotNull
    default String strip(String raw) {
        String value = prepare(raw);
        if (value.trim().isEmpty()) return value;

        Matcher matcher = getPattern().matcher(value);
        while (matcher.find())
            value = value.replace(matcher.group(), accept(matcher).raw());

        return value;
    }
}
