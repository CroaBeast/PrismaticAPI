package me.croabeast.prismatic.chat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A composite {@link ChatComponent} that splits a raw message into interactive segments
 * and manages them as a unified, fluent unit.
 *
 * <p>{@code MultiComponent} understands a compact markup language for attaching click and
 * hover events to specific portions of text. The default markup format is:
 * <pre>{@code
 * <action:"argument">visible text</text>
 * <action:"arg1"|action2:"arg2">visible text</text>
 * }</pre>
 *
 * <p>For example:
 * <pre>{@code
 * BaseComponent[] msg = PrismaticAPI
 *         .multiComponent(
 *             "<run:\"/spawn\">&aGo to Spawn</text>" +
 *             " &7| " +
 *             "<suggest:\"/msg \">&bSend a Message</text>"
 *         )
 *         .compile(player);
 *
 * player.spigot().sendMessage(msg);
 * }</pre>
 *
 * <p>Segments that contain no markup tags are treated as plain text. URLs found in plain
 * segments are automatically wrapped in an {@link ChatComponent.Click#OPEN_URL OPEN_URL}
 * click event.
 *
 * <p>The {@code setClick*} / {@code setHover*} / {@code setHoverItem*} methods come in two
 * flavors: ones that target <em>only the last segment</em> (matching the behavior of
 * {@link ChatComponent}) and ones suffixed with {@code ToAll} that apply the event to
 * <em>every</em> segment in the component.
 *
 * <p>Color continuity is maintained automatically: if a segment does not start with an
 * explicit color code, the last color from the preceding segment is prepended, preventing
 * unexpected color resets between segments.
 *
 * @since 1.5.0
 * @see me.croabeast.prismatic.PrismaticAPI#multiComponent(String)
 * @see ChatComponent
 * @see ChatFormat
 */
public interface MultiComponent extends ChatComponent<MultiComponent> {

    /**
     * Regex fragment that matches all recognized click-action aliases accepted in markup.
     *
     * <p>This fragment is incorporated into {@link #DEFAULT_REGEX} and can be reused by
     * custom {@link ChatFormat} implementations that want to extend the default syntax
     * while staying consistent with the recognized action names.
     *
     * <p>Recognized aliases include: {@code execute}, {@code click}, {@code run},
     * {@code run_command}, {@code suggest}, {@code suggest_command}, {@code url},
     * {@code open_url}, {@code file}, {@code open_file}, {@code page}, {@code change_page},
     * {@code copy} and {@code copy_to_clipboard}.
     */
    String CLICK_REGEX = "execute|click|(?:run|suggest)(?:_command)?|(?:open_)?(?:url|file)|(?:change_)?page|copy|(?:copy_to_)?clipboard";

    /**
     * The default markup regex used by {@link #DEFAULT_FORMAT} to identify interactive segments.
     *
     * <p>A segment matches when it is wrapped in opening and closing tags of the form:
     * <pre>{@code
     * <action:"argument">text</text>
     * <action:"arg1"|action2:"arg2">text</text>
     * }</pre>
     *
     * <p>Recognized opening-tag actions are: {@code hover_item}, {@code hover} and any alias
     * listed in {@link #CLICK_REGEX}. The closing tag is always {@code </text>}.
     */
    String DEFAULT_REGEX = "(?i)<(?:(hover_item|hover|" + CLICK_REGEX + "):\"(.[^|]*?)\"(?:[|](?:(hover_item|hover|" + CLICK_REGEX + "):\"(.[^|]*?)\"))?)>(.+?)</text>";

    /**
     * The default {@link ChatFormat} instance used when a {@code MultiComponent} is created
     * without an explicit format.
     *
     * <p>It parses {@link #DEFAULT_REGEX} markup and wires up the corresponding
     * {@link ChatComponent} events. It also implements {@link ChatFormat#toFormattedString}
     * so that components can be round-tripped back to markup.
     */
    ChatFormat<ChatComponent<?>> DEFAULT_FORMAT =
            new MultiComponentImpl(ChatProcessor.prismatic(), "").getFormat();

    /**
     * Returns the {@link ChatFormat} used by this component to parse markup and serialize
     * segments back to their string representation.
     *
     * @return the current format; never {@code null}
     */
    @NotNull
    ChatFormat<ChatComponent<?>> getFormat();

    /**
     * Replaces the {@link ChatFormat} used by this component.
     *
     * <p>Changing the format does not reparse already-created segments. It only affects
     * how subsequent calls to {@link #append(String)} parse new text, and how
     * {@link #toFormattedString()} serializes the component.
     *
     * @param format the new format; must not be {@code null}
     * @return this component for fluent chaining
     */
    @NotNull
    MultiComponent setFormat(@NotNull ChatFormat<ChatComponent<?>> format);

    /**
     * Returns a deep copy of this multi-component, including all current segments and the
     * current format.
     *
     * <p>Modifying the copy does not affect the original.
     *
     * @return a new {@code MultiComponent} with the same segments and format
     */
    @NotNull
    MultiComponent copy();

    /**
     * Parses the given raw text and appends the resulting segments to this component.
     *
     * <p>The text is processed by the current {@link ChatFormat}: markup segments are
     * extracted and interactive events are wired up, while plain text spans are kept
     * as-is. URLs in plain spans are automatically turned into {@link ChatComponent.Click#OPEN_URL}
     * events.
     *
     * @param message the raw text to parse and append
     * @return this component for fluent chaining
     */
    @NotNull
    MultiComponent append(String message);

    /**
     * Appends an existing {@link ChatComponent} as a new segment at the end of this component.
     *
     * <p>The component is not re-parsed; it is added directly. Color continuity is still
     * maintained between the new segment and its predecessor.
     *
     * @param component the component to append; must not be {@code null}
     * @return this component for fluent chaining
     */
    @NotNull
    MultiComponent append(@NotNull ChatComponent<?> component);

    /**
     * Appends the string representation of an object as a new raw-text segment.
     *
     * <p>Equivalent to {@link #append(String) append(String.valueOf(object))}.
     *
     * @param object the value whose {@link Object#toString() toString()} is appended
     * @return this component for fluent chaining
     */
    @NotNull
    default MultiComponent append(Object object) {
        return append(String.valueOf(object));
    }

    /**
     * Applies a click event to <em>every</em> segment in this component.
     *
     * @param click the click action
     * @param input the click payload
     * @return this component for fluent chaining
     */
    @NotNull
    MultiComponent setClickToAll(Click click, String input);

    /**
     * Applies a click event to <em>every</em> segment in this component using a string alias.
     *
     * <p>The alias is resolved via {@link Click#fromName(String)}.
     *
     * @param click a case-insensitive click-action alias
     * @param input the click payload
     * @return this component for fluent chaining
     */
    @NotNull
    default MultiComponent setClickToAll(String click, String input) {
        return setClickToAll(Click.fromName(click), input);
    }

    /**
     * Applies a click event to <em>every</em> segment from a compact {@code "action:payload"} string.
     *
     * @param input combined action and payload string (e.g. {@code "run:/spawn"})
     * @return this component for fluent chaining
     */
    @NotNull
    default MultiComponent setClickToAll(String input) {
        String[] parts = input.replace("\"", "").split(":", 2);
        return setClickToAll(parts[0], parts.length == 1 ? "" : parts[1]);
    }

    /**
     * Applies a text hover event to <em>every</em> segment in this component.
     *
     * @param list ordered list of hover text lines
     * @return this component for fluent chaining
     */
    @NotNull
    MultiComponent setHoverToAll(List<String> list);

    /**
     * Applies a text hover event to <em>every</em> segment in this component.
     *
     * <p>Convenience overload for {@link #setHoverToAll(List)}.
     *
     * @param strings ordered hover text lines
     * @return this component for fluent chaining
     */
    @NotNull
    default MultiComponent setHoverToAll(String... strings) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, strings);
        return setHoverToAll(list);
    }

    /**
     * Applies a text hover event to <em>every</em> segment from a single, optionally
     * multi-line string.
     *
     * <p>The string is split into lines using the processor's line-separator regex
     * (default: {@code <n>}).
     *
     * @param string raw hover content; may contain {@code <n>} separators
     * @return this component for fluent chaining
     */
    @NotNull
    MultiComponent setHoverToAll(String string);

    /**
     * Applies an item hover event to <em>every</em> segment in this component.
     *
     * @param json raw item JSON or {@code "b64:<base64>"} encoded payload
     * @return this component for fluent chaining
     */
    @NotNull
    MultiComponent setHoverItemToAll(String json);

    /**
     * Serializes this component back to its formatted markup string using the current
     * {@link ChatFormat}.
     *
     * <p>The result can be passed to {@link #fromString(String)} to reconstruct an
     * equivalent component, making it useful for persistence or network transport.
     *
     * @return the formatted markup string representation of this component
     */
    @NotNull
    default String toFormattedString() {
        return getFormat().toFormattedString(this);
    }

    /**
     * Unsupported — the message of a {@code MultiComponent} is derived from its individual
     * segments and cannot be replaced as a single string.
     *
     * @param message ignored
     * @return never returns normally
     * @throws IllegalStateException always
     */
    @NotNull
    @Override
    default MultiComponent setMessage(@NotNull String message) {
        throw new IllegalStateException("Message can not be set");
    }

    /**
     * Creates a new {@code MultiComponent} backed by a custom {@link ChatProcessor}.
     *
     * <p>The raw message is immediately parsed using {@link #DEFAULT_FORMAT}.
     *
     * @param processor the text processor used to colorize segments and hover content
     * @param message   the initial raw message text, parsed into segments on construction
     * @return a new {@code MultiComponent} instance
     */
    static MultiComponent fromString(ChatProcessor processor, String message) {
        return new MultiComponentImpl(processor, message);
    }

    /**
     * Creates a new {@code MultiComponent} backed by the default Prismatic processor.
     *
     * <p>This is the recommended factory method for most use cases. The raw message is
     * immediately parsed using {@link #DEFAULT_FORMAT}.
     *
     * @param message the initial raw message text, parsed into segments on construction
     * @return a new {@code MultiComponent} instance
     */
    static MultiComponent fromString(String message) {
        return fromString(ChatProcessor.prismatic(), message);
    }
}
