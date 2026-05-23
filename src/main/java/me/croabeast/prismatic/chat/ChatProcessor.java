package me.croabeast.prismatic.chat;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Small formatting bridge used by Prismatic chat components.
 *
 * <p>The default processor delegates to {@code PrismaticAPI}. Consumers that need an extra text
 * preparation step, such as custom small-caps or alignment passes, can provide their own implementation
 * without coupling this package to a larger runtime.
 */
public interface ChatProcessor {

    /**
     * Returns the default processor backed by {@code PrismaticAPI}.
     *
     * @return default Prismatic processor
     */
    @NotNull
    static ChatProcessor prismatic() {
        return ChatProcessorImpl.INSTANCE;
    }

    /**
     * Applies colors to the given text.
     *
     * @param player optional player context
     * @param string text to colorize
     * @return colorized text
     */
    @NotNull
    String colorize(@Nullable Player player, String string);

    /**
     * Applies optional pre-processing before a multi-component is split into segments.
     *
     * @param string raw text
     * @return prepared text
     */
    @NotNull
    default String prepare(String string) {
        return string == null ? "" : string;
    }

    /**
     * Literal separator used when a hover text is serialized back to component markup.
     *
     * @return line separator literal
     */
    @NotNull
    default String getLineSeparator() {
        return "<n>";
    }

    /**
     * Regex used to split hover text into lines.
     *
     * @return line separator regex
     */
    @NotNull
    default String getLineSeparatorRegex() {
        return Pattern.quote(getLineSeparator());
    }

    /**
     * Splits text into lines using {@link #getLineSeparatorRegex()}.
     *
     * @param string text to split
     * @param limit split limit
     * @return split lines
     */
    @NotNull
    default String[] splitLines(String string, int limit) {
        return (string == null ? "" : string).split(getLineSeparatorRegex(), limit);
    }

    /**
     * Splits text into lines without a limit.
     *
     * @param string text to split
     * @return split lines
     */
    @NotNull
    default String[] splitLines(String string) {
        return splitLines(string, 0);
    }
}
