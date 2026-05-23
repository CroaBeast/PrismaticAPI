package me.croabeast.prismatic.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A mutable, fluent Bungee/Spigot chat component that supports optional click and hover events.
 *
 * <p>A {@code ChatComponent} wraps a raw message string and lets you attach interactive events
 * ({@link #setClick}, {@link #setHover}, {@link #setHoverItem}) through a builder-style API. When
 * you are done configuring the component, call {@link #compile(Player)} to produce the
 * {@link BaseComponent} array expected by {@code player.spigot().sendMessage(...)}.
 *
 * <p>The simplest way to create an instance is through the factory methods:
 * <pre>{@code
 * BaseComponent[] msg = PrismaticAPI.chatComponent("<#ff8800>Click me!")
 *         .setClick("run", "/help")
 *         .setHover("Open help<n>Second line")
 *         .compile(player);
 *
 * player.spigot().sendMessage(msg);
 * }</pre>
 *
 * <p>URLs found inside the message text are automatically converted into
 * {@link Click#OPEN_URL OPEN_URL} click events during {@link #compile(Player)}.
 *
 * @param <C> concrete component type returned by the fluent methods, enabling type-safe chaining
 *            in subinterfaces
 * @since 1.5.0
 * @see me.croabeast.prismatic.PrismaticAPI#chatComponent(String)
 * @see MultiComponent
 */
public interface ChatComponent<C extends ChatComponent<C>> {

    /**
     * URL detector used to automatically attach {@link Click#OPEN_URL} click events to any
     * URL-only segment found inside the raw message during {@link #compile(Player)}.
     *
     * <p>The pattern matches both {@code http://} and {@code https://} URLs as well as bare
     * {@code www.} prefixed links.
     */
    Pattern URL_PATTERN = Pattern.compile("(?i)\\b(?:https?://|www\\.)\\S+");

    /**
     * Returns the raw, un-colorized message text stored in this component.
     *
     * @return the raw message string; never {@code null}
     */
    @NotNull
    String getMessage();

    /**
     * Replaces the raw message text of this component.
     *
     * @param message the new raw message; must not be {@code null}
     * @return this component for fluent chaining
     */
    @NotNull
    C setMessage(@NotNull String message);

    /**
     * Attaches a click event to this component.
     *
     * @param click the click action to perform when the player clicks the text
     * @param input the payload delivered to the click action (command, URL, etc.)
     * @return this component for fluent chaining
     */
    @NotNull
    C setClick(Click click, String input);

    /**
     * Attaches a click event to this component using a string alias for the action.
     *
     * <p>The alias is resolved through {@link Click#fromName(String)}; unknown aliases fall
     * back to {@link Click#SUGGEST}.
     *
     * @param click a case-insensitive alias for the desired click action
     *              (e.g. {@code "run"}, {@code "suggest"}, {@code "url"})
     * @param input the payload delivered to the click action
     * @return this component for fluent chaining
     */
    @NotNull
    default C setClick(String click, String input) {
        return setClick(Click.fromName(click), input);
    }

    /**
     * Attaches a click event from a compact {@code "action:payload"} string.
     *
     * <p>The string is split on the first {@code ':'} character; surrounding quotation marks
     * are stripped before parsing. Example: {@code "run:/spawn"} or {@code "url:https://example.com"}.
     *
     * @param input combined action and payload string
     * @return this component for fluent chaining
     */
    @NotNull
    default C setClick(String input) {
        String[] parts = input.replace("\"", "").split(":", 2);
        return setClick(parts[0], parts.length == 1 ? "" : parts[1]);
    }

    /**
     * Sets a text hover event whose content is drawn from the provided lines.
     *
     * <p>Each element in the list is rendered as a separate line in the tooltip shown to the
     * player. The lines are colorized via the component's processor when the component is
     * compiled.
     *
     * @param list ordered list of hover text lines; must not be {@code null}
     * @return this component for fluent chaining
     */
    @NotNull
    C setHover(List<String> list);

    /**
     * Sets a text hover event whose content is drawn from the provided lines.
     *
     * <p>Convenience overload for {@link #setHover(List)}.
     *
     * @param array ordered hover text lines
     * @return this component for fluent chaining
     */
    @NotNull
    default C setHover(String... array) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, array);
        return setHover(list);
    }

    /**
     * Sets a text hover event from a single, optionally multi-line string.
     *
     * <p>The string is split into lines using the processor's
     * {@link ChatProcessor#getLineSeparatorRegex() line separator regex} (default: {@code <n>}).
     * Surrounding {@code hover:"..."} markup is stripped before splitting.
     *
     * @param string raw hover content, may contain {@code <n>} separators
     * @return this component for fluent chaining
     */
    @NotNull
    C setHover(String string);

    /**
     * Sets an item hover event whose content is described by an NBT/SNBT JSON string.
     *
     * <p>The payload can be provided either as a raw JSON string or as a Base64-encoded string
     * prefixed with {@code "b64:"}. This is useful for including special characters or complex
     * item NBT without escaping issues.
     *
     * @param json raw item JSON or a {@code "b64:<base64>"} encoded payload
     * @return this component for fluent chaining
     */
    @NotNull
    C setHoverItem(String json);

    /**
     * Returns {@code true} when this component has at least one click or hover event attached.
     *
     * <p>This can be used to decide whether to wrap the compiled output in additional markup
     * or to skip unnecessary processing when no interactive events were configured.
     *
     * @return {@code true} if a click, text hover or item hover event is present
     */
    boolean hasEvents();

    /**
     * Compiles this component into a {@link BaseComponent} array ready for Spigot/Bungee chat APIs.
     *
     * <p>The message text is colorized through the component's {@link ChatProcessor} using the
     * provided player context so that player-aware hex fallback is applied correctly. URLs found
     * inside the message are automatically converted to {@link Click#OPEN_URL} events.
     *
     * @param player the player who will receive the message, used for version-aware color output;
     *               may be {@code null} to force the legacy fallback
     * @return a non-empty array of compiled {@link BaseComponent} objects
     */
    @NotNull
    BaseComponent[] compile(Player player);

    /**
     * Returns the concrete implementation instance of this component.
     *
     * <p>Implementations return {@code this}; this method exists so that fluent setters defined
     * in default methods on sub-interfaces can return the correct concrete type.
     *
     * @return this component instance
     */
    @NotNull
    C instance();

    /**
     * Creates a new {@code ChatComponent} backed by the given {@link ChatProcessor}.
     *
     * @param processor the text processor used to colorize the message and hover content
     * @param message   the initial raw message text
     * @return a new {@code ChatComponent} instance
     */
    static ChatComponent<?> fromString(ChatProcessor processor, String message) {
        return new ComponentImpl(processor, message);
    }

    /**
     * Creates a new {@code ChatComponent} backed by the default Prismatic processor.
     *
     * <p>This is the recommended factory method for most use cases. It uses
     * {@link ChatProcessor#prismatic()} which delegates colorization to
     * {@link me.croabeast.prismatic.PrismaticAPI}.
     *
     * @param message the initial raw message text
     * @return a new {@code ChatComponent} instance
     */
    static ChatComponent<?> fromString(String message) {
        return fromString(ChatProcessor.prismatic(), message);
    }

    /**
     * Enumeration of the click actions that a {@code ChatComponent} can carry.
     *
     * <p>Each constant wraps a Bungee {@link ClickEvent.Action} and also holds a collection of
     * lowercase string aliases that are recognized by {@link #fromName(String)}. This allows
     * callers to use short, human-friendly names like {@code "run"}, {@code "suggest"} or
     * {@code "url"} instead of the verbose Bungee action constants.
     *
     * <p>Example:
     * <pre>{@code
     * component.setClick(Click.EXECUTE, "/spawn");
     * // or equivalently:
     * component.setClick("run", "/spawn");
     * }</pre>
     */
    enum Click {
        /** Runs a command as the player who clicks the text. */
        EXECUTE(ClickEvent.Action.RUN_COMMAND, "click", "run"),
        /** Opens a URL in the player's default browser. */
        OPEN_URL(ClickEvent.Action.OPEN_URL, "url"),
        /** Opens a file on the player's local filesystem (rarely used in practice). */
        OPEN_FILE(ClickEvent.Action.OPEN_FILE, "file"),
        /** Inserts text into the player's chat input without sending it. */
        SUGGEST(ClickEvent.Action.SUGGEST_COMMAND, "suggest"),
        /** Changes the current page in a written book. */
        CHANGE_PAGE(ClickEvent.Action.CHANGE_PAGE, "page"),
        /** Copies text to the player's clipboard. */
        CLIPBOARD(ClickEvent.Action.COPY_TO_CLIPBOARD, "copy");

        private final ClickEvent.Action bukkit;
        private final List<String> names = new ArrayList<>();

        Click(ClickEvent.Action bukkit, String... extras) {
            this.bukkit = bukkit;
            names.add(name().toLowerCase(Locale.ENGLISH));
            names.add(bukkit.name().toLowerCase(Locale.ENGLISH));
            Collections.addAll(names, extras);
        }

        /**
         * Returns the underlying Bungee {@link ClickEvent.Action} for this click type.
         *
         * @return the corresponding Bungee click action; never {@code null}
         */
        public ClickEvent.Action asBukkit() {
            return bukkit;
        }

        /**
         * Returns the primary lowercase alias for this click type (e.g. {@code "execute"},
         * {@code "open_url"}).
         *
         * @return the primary alias string
         */
        @Override
        public String toString() {
            return names.get(0);
        }

        /**
         * Resolves a {@code Click} constant from a string alias.
         *
         * <p>The comparison is case-insensitive and matches against all registered aliases,
         * including the constant name, the Bungee action name and any extra aliases declared
         * in the constructor. When the input is blank or no alias matches,
         * {@link #SUGGEST} is returned as a safe default.
         *
         * @param name a case-insensitive alias to look up (e.g. {@code "run"}, {@code "url"})
         * @return the matching {@code Click} constant, or {@link #SUGGEST} if none is found
         */
        public static Click fromName(String name) {
            if (StringUtils.isBlank(name)) return SUGGEST;

            String lower = name.toLowerCase(Locale.ENGLISH);
            for (Click type : values())
                if (type.names.contains(lower)) return type;

            return SUGGEST;
        }
    }
}
