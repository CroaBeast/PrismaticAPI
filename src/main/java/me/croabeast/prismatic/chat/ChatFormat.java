package me.croabeast.prismatic.chat;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A typed text-format contract used by {@link MultiComponent} to parse and serialize
 * interactive component markup.
 *
 * <p>Implementations define a {@link #getRegex() regular expression} that identifies
 * the markup handled by this format, and an {@link #accept(Player, String) accept} method
 * that converts a matching raw string into a fully configured {@link ChatComponent}.
 *
 * <p>The interface also provides default implementations for convenience:
 * <ul>
 *   <li>{@link #matcher(String)} — returns a pre-compiled {@link Matcher} against the format regex</li>
 *   <li>{@link #isFormatted(String)} — quick check for whether the format is present in a string</li>
 *   <li>{@link #removeFormat(String)} — strips all formatted segments from a string</li>
 *   <li>{@link #accept(String)} — parses without a player context</li>
 *   <li>{@link #toFormattedString(Object)} — serializes a result back to markup (optional)</li>
 * </ul>
 *
 * @param <T> the type produced by {@link #accept(Player, String)}, typically {@link ChatComponent}
 * @since 1.5.0
 * @see MultiComponent#DEFAULT_FORMAT
 */
public interface ChatFormat<T> {

    /**
     * Returns the regular expression used to detect and extract segments handled by this format.
     *
     * <p>The returned string is compiled (and cached) by {@link #matcher(String)} into a
     * {@link Pattern}. Implementations should return a stable, non-null value because the
     * pattern is cached in a shared map keyed by this string.
     *
     * @return the regex string for this format; never {@code null}
     */
    @NotNull
    String getRegex();

    /**
     * Creates a {@link Matcher} for the given text using the pattern derived from {@link #getRegex()}.
     *
     * <p>Compiled {@link Pattern} instances are cached in {@link Holder#PATTERNS} to avoid
     * repeated compilation overhead. The first call for a given regex compiles and stores the
     * pattern; subsequent calls retrieve it from the cache.
     *
     * @param string the text to match against
     * @return a {@link Matcher} ready to be used for {@code find()} or {@code matches()} calls
     */
    @NotNull
    default Matcher matcher(String string) {
        return Holder.PATTERNS
                .computeIfAbsent(getRegex(), Pattern::compile)
                .matcher(string);
    }

    /**
     * Returns {@code true} when this format's regex finds at least one match inside the
     * given string.
     *
     * @param string the text to inspect
     * @return {@code true} if the format is present; {@code false} otherwise
     */
    default boolean isFormatted(String string) {
        return matcher(string).find();
    }

    /**
     * Removes every occurrence of this format from the given string, leaving only the
     * surrounding plain text.
     *
     * <p>The default implementation replaces each full match (including tags) with an empty
     * string. Implementations that need to preserve inner text (e.g. strip only the outer
     * markup but keep the content) should override this method.
     *
     * @param string the text from which to remove formatted segments; may be blank
     * @return the text without any segments matched by this format's regex
     */
    default String removeFormat(String string) {
        if (StringUtils.isBlank(string)) return string;

        Matcher matcher = matcher(string);
        while (matcher.find())
            string = string.replace(matcher.group(), "");
        return string;
    }

    /**
     * Parses a raw string into a typed result using the given player as context for
     * version-aware color resolution.
     *
     * <p>When the string matches this format, the returned result should carry the
     * interactive events (click, hover, etc.) extracted from the markup. When no match is
     * found, implementations typically return a plain component wrapping the original text.
     *
     * @param player the player who will receive the resulting component; used to resolve
     *               color capabilities. May be {@code null} for a conservative legacy fallback.
     * @param string the raw text to parse, which may or may not contain format markup
     * @return the parsed result; never {@code null}
     */
    @NotNull
    T accept(Player player, String string);

    /**
     * Parses a raw string into a typed result without player context.
     *
     * <p>Delegates to {@link #accept(Player, String)} with a {@code null} player, which
     * causes the formatting pipeline to fall back to legacy-safe color output because no
     * player capability information is available.
     *
     * @param string the raw text to parse
     * @return the parsed result; never {@code null}
     */
    @NotNull
    default T accept(String string) {
        return accept(null, string);
    }

    /**
     * Serializes a previously parsed result back to its formatted markup representation.
     *
     * <p>The default implementation throws {@link UnsupportedOperationException}. Implementations
     * that need round-trip serialization (e.g. for persistence or network transport) should
     * override this method.
     *
     * @param result the typed result to serialize; must have been produced by this format
     * @return the formatted string representation of the result
     * @throws UnsupportedOperationException if this format does not support serialization
     */
    @NotNull
    default String toFormattedString(T result) {
        throw new UnsupportedOperationException("toFormattedString() is not implemented");
    }

    /**
     * Internal cache that stores compiled {@link Pattern} instances keyed by their
     * source regex string.
     *
     * <p>Using a {@link ConcurrentHashMap} ensures that concurrent reads and writes during
     * server startup (where multiple plugins may initialize simultaneously) do not require
     * explicit synchronization. Patterns are only ever added, never removed, so no
     * invalidation logic is needed.
     */
    final class Holder {
        /** Thread-safe map from regex string to compiled {@link Pattern}. */
        private static final Map<String, Pattern> PATTERNS = new ConcurrentHashMap<>();

        /** Utility class — do not instantiate. */
        private Holder() {}
    }
}
