package me.croabeast.prismatic.element;

import me.croabeast.prismatic.PrismaticAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An immutable, parsed message.
 *
 * <p>An {@code Element} is the compiled form of a raw string: the interactive markup, the bare
 * URLs and the placeholder tokens are located once, and every later render reuses that work.
 * Because it is immutable it is safe to keep one instance per config entry and hand it to every
 * player, which is what makes the per-target render cache pay off.
 *
 * <pre>{@code
 * Element line = Element.parse("<g:ff8800>Bienvenido</g:00ffaa> &7{player}");
 *
 * player.sendMessage(line.legacy(player));
 * player.spigot().sendMessage(line.bungee(player));
 * }</pre>
 *
 * <p>Mutation goes through {@link Builder}, so a shared instance can never be changed underneath
 * another consumer:
 *
 * <pre>{@code
 * Element button = Element.text("Spawn")
 *         .toBuilder()
 *         .color("#00ffaa")
 *         .bold()
 *         .click(Click.RUN_COMMAND, "/spawn")
 *         .hover("&7Ir al spawn")
 *         .build();
 * }</pre>
 *
 * <p>Adventure output lives in {@link AdventureElement} instead of on this type, so a server
 * without the Adventure runtime never loads a Kyori class by touching an element.
 *
 * @since 2.0.0
 */
public final class Element {

    /**
     * Detector used to turn a bare URL into its own segment with an {@link Click#OPEN_URL} event.
     *
     * <p>Matches {@code http://} and {@code https://} URLs as well as bare {@code www.} links.
     */
    public static final Pattern URL_PATTERN = ElementParser.URL_PATTERN;

    private static final Element EMPTY = new Element(new Segment[0]);

    private final Segment[] segments;

    Element(Segment[] segments) {
        this.segments = segments;
    }

    /**
     * Returns the shared empty element.
     *
     * @return an element with no segments
     */
    @NotNull
    public static Element empty() {
        return EMPTY;
    }

    /**
     * Parses raw markup into an element.
     *
     * <p>Interactive tags of the form {@code <action:"argument">text</text>} become segments with
     * their events attached, bare URLs get an {@link Click#OPEN_URL} event, and the trailing color
     * of a segment carries into the next one. Colors and placeholders are kept as data and applied
     * when the element is rendered.
     *
     * @param raw raw message
     * @return a new element
     */
    @NotNull
    public static Element parse(@Nullable String raw) {
        return parse(raw, MarkupFormat.prismatic());
    }

    /**
     * Parses raw markup with a custom format.
     *
     * @param raw    raw message
     * @param format parsing strategy
     * @return a new element
     */
    @NotNull
    public static Element parse(@Nullable String raw, @NotNull MarkupFormat format) {
        List<Segment> segments = ElementParser.parse(raw, format);
        return segments.isEmpty() ? EMPTY : new Element(segments.toArray(new Segment[0]));
    }

    /**
     * Removes interactive markup from raw text, keeping the visible content of every segment.
     *
     * @param raw text to clean
     * @return the text without markup tags
     */
    @NotNull
    public static String stripMarkup(@Nullable String raw) {
        return MarkupFormat.prismatic().strip(raw);
    }

    /**
     * Returns whether raw text contains interactive markup.
     *
     * @param raw text to inspect
     * @return {@code true} when at least one segment matches
     */
    public static boolean isMarkup(@Nullable String raw) {
        return MarkupFormat.prismatic().isFormatted(raw);
    }

    /**
     * Wraps literal text without parsing interactive markup.
     *
     * <p>Color syntax and placeholders inside the text are still honoured; only the
     * {@code <action:"...">...</text>} tags and URL splitting are skipped.
     *
     * @param raw literal text
     * @return a new element
     */
    @NotNull
    public static Element text(@Nullable String raw) {
        return raw == null || raw.isEmpty() ? EMPTY : new Element(new Segment[] {new Segment(raw)});
    }

    /**
     * Creates an empty builder.
     *
     * @return a new builder
     */
    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder seeded with the segments of this element.
     *
     * <p>This element is not affected by anything done to the returned builder.
     *
     * @return a new builder
     */
    @NotNull
    public Builder toBuilder() {
        return new Builder(segments);
    }

    /**
     * Returns whether this element has no segments.
     *
     * @return {@code true} when empty
     */
    public boolean isEmpty() {
        return segments.length == 0;
    }

    /**
     * Returns whether any segment carries a click or hover event.
     *
     * @return {@code true} when an event is present
     */
    public boolean hasEvents() {
        for (Segment segment : segments)
            if (segment.hasEvents()) return true;

        return false;
    }

    /**
     * Renders this element to a legacy string using the server default profile.
     *
     * @return the colorized string
     */
    @NotNull
    public String legacy() {
        return legacy(RenderContext.of(null));
    }

    /**
     * Renders this element to a legacy string for a player.
     *
     * @param player receiving player
     * @return the colorized string
     */
    @NotNull
    public String legacy(@NotNull Player player) {
        return legacy(RenderContext.of(player));
    }

    /**
     * Renders this element to a legacy string.
     *
     * @param context render context
     * @return the colorized string
     */
    @NotNull
    public String legacy(@NotNull RenderContext context) {
        if (segments.length == 1) return segments[0].render(context);

        StringBuilder builder = new StringBuilder(32);
        for (Segment segment : segments)
            builder.append(segment.render(context));

        return builder.toString();
    }

    /**
     * Renders this element to Bungee components using the server default profile.
     *
     * @return freshly built components, never shared between calls
     */
    @NotNull
    public BaseComponent[] bungee() {
        return bungee(RenderContext.of(null));
    }

    /**
     * Renders this element to Bungee components for a player.
     *
     * @param player receiving player
     * @return freshly built components, never shared between calls
     */
    @NotNull
    public BaseComponent[] bungee(@NotNull Player player) {
        return bungee(RenderContext.of(player));
    }

    /**
     * Renders this element to Bungee components.
     *
     * @param context render context
     * @return freshly built components, never shared between calls
     */
    @NotNull
    public BaseComponent[] bungee(@NotNull RenderContext context) {
        return BungeeRenderer.render(segments, context);
    }

    /**
     * Renders this element and strips every color and format code from the result.
     *
     * @return plain text
     */
    @NotNull
    public String plain() {
        return plain(RenderContext.of(null));
    }

    /**
     * Renders this element and strips every color and format code from the result.
     *
     * @param context render context
     * @return plain text
     */
    @NotNull
    public String plain(@NotNull RenderContext context) {
        return PrismaticAPI.stripAll(legacy(context));
    }

    /**
     * Returns the raw markup backing this element, without colors or placeholder resolution.
     *
     * @return the raw text
     */
    @NotNull
    public String raw() {
        StringBuilder builder = new StringBuilder(32);
        for (Segment segment : segments)
            builder.append(segment.raw);

        return builder.toString();
    }

    /**
     * Serializes this element back to interactive markup.
     *
     * <p>The result can be handed to {@link #parse(String)} to rebuild an equivalent element, which
     * makes it usable for persistence or transport.
     *
     * @return the markup representation
     */
    @NotNull
    public String toMarkup() {
        StringBuilder builder = new StringBuilder(32);
        for (Segment segment : segments)
            appendMarkup(builder, segment);

        return builder.toString();
    }

    private static void appendMarkup(StringBuilder builder, Segment segment) {
        if (!segment.hasEvents()) {
            builder.append(segment.raw);
            return;
        }

        builder.append('<');
        boolean hasAction = false;

        if (segment.click != null) {
            builder.append(segment.click).append(":\"").append(segment.clickValue).append('"');
            hasAction = true;
        }

        if (segment.hover != null) {
            if (hasAction) builder.append('|');

            builder.append(segment.hover.isItem() ? "hover_item:\"" : "hover:\"")
                    .append(segment.hover.isItem() ?
                            segment.hover.getSerializedItemJson() :
                            String.join(ElementParser.LINE_SEPARATOR, segment.hover.getLines()))
                    .append('"');
        }

        builder.append('>').append(segment.raw).append("</text>");
    }

    /**
     * Returns {@link #raw()}.
     *
     * <p>Rendering is deliberately not done here: {@code toString} is called by string
     * concatenation, logging and debuggers, and none of those should trigger a render or need a
     * player context.
     *
     * @return the raw text
     */
    @Override
    public String toString() {
        return raw();
    }

    Segment[] segments() {
        return segments;
    }

    /**
     * Mutable builder for {@link Element}.
     *
     * <p>Style and event calls apply to the last appended segment, so a chain reads in the order
     * the message is written.
     *
     * @since 2.0.0
     */
    public static final class Builder {

        private final List<Segment> segments = new ArrayList<>();

        Builder() {}

        Builder(Segment[] segments) {
            this.segments.addAll(Arrays.asList(segments));
        }

        /**
         * Appends literal text as a new segment.
         *
         * @param text text to append
         * @return this builder
         */
        @NotNull
        public Builder append(@Nullable String text) {
            if (text != null && !text.isEmpty())
                ElementParser.add(segments, new Segment(text));

            return this;
        }

        /**
         * Parses raw markup and appends the resulting segments.
         *
         * @param raw raw message
         * @return this builder
         */
        @NotNull
        public Builder appendMarkup(@Nullable String raw) {
            return appendMarkup(raw, MarkupFormat.prismatic());
        }

        /**
         * Parses raw markup with a custom format and appends the resulting segments.
         *
         * @param raw    raw message
         * @param format parsing strategy
         * @return this builder
         */
        @NotNull
        public Builder appendMarkup(@Nullable String raw, @NotNull MarkupFormat format) {
            ElementParser.appendTo(segments, raw, format);
            return this;
        }

        /**
         * Replaces the raw text of the last segment, keeping its events.
         *
         * @param raw new raw text
         * @return this builder
         */
        @NotNull
        public Builder raw(@NotNull String raw) {
            return replaceLast(last().withRaw(raw));
        }

        /**
         * Appends the segments of another element.
         *
         * @param element element to append
         * @return this builder
         */
        @NotNull
        public Builder append(@Nullable Element element) {
            if (element != null)
                for (Segment segment : element.segments)
                    ElementParser.add(segments, segment);

            return this;
        }

        /**
         * Prefixes the last segment with a color.
         *
         * @param color hex value such as {@code "#00ffaa"}, or a legacy code such as {@code "a"}
         * @return this builder
         */
        @NotNull
        public Builder color(@NotNull String color) {
            return prefix(color.startsWith("#") ? color : color.startsWith("&") ? color : '&' + color);
        }

        /**
         * Marks the last segment bold.
         *
         * @return this builder
         */
        @NotNull
        public Builder bold() {
            return prefix("&l");
        }

        /**
         * Marks the last segment italic.
         *
         * @return this builder
         */
        @NotNull
        public Builder italic() {
            return prefix("&o");
        }

        /**
         * Marks the last segment underlined.
         *
         * @return this builder
         */
        @NotNull
        public Builder underlined() {
            return prefix("&n");
        }

        /**
         * Marks the last segment struck through.
         *
         * @return this builder
         */
        @NotNull
        public Builder strikethrough() {
            return prefix("&m");
        }

        /**
         * Attaches a click event to the last segment.
         *
         * @param click  action to perform
         * @param value  payload delivered to the action
         * @return this builder
         */
        @NotNull
        public Builder click(@NotNull Click click, @Nullable String value) {
            return replaceLast(last().withClick(click, value == null ? "" : value));
        }

        /**
         * Attaches a click event to the last segment using a string alias.
         *
         * @param action alias such as {@code "run"}, {@code "suggest"} or {@code "url"}
         * @param value  payload delivered to the action
         * @return this builder
         */
        @NotNull
        public Builder click(@NotNull String action, @Nullable String value) {
            return click(Click.fromName(action), value);
        }

        /**
         * Attaches a click event from a compact {@code "action:payload"} string.
         *
         * @param compact combined action and payload, such as {@code "run:/spawn"}
         * @return this builder
         */
        @NotNull
        public Builder click(@NotNull String compact) {
            String[] parts = compact.replace("\"", "").split(":", 2);
            return click(parts[0], parts.length == 1 ? "" : parts[1]);
        }

        /**
         * Attaches a text hover to the last segment.
         *
         * @param lines hover lines
         * @return this builder
         */
        @NotNull
        public Builder hover(@NotNull String... lines) {
            return hover(Arrays.asList(lines));
        }

        /**
         * Attaches a text hover to the last segment.
         *
         * <p>A single string containing {@code <n>} separators is split into lines.
         *
         * @param lines hover lines
         * @return this builder
         */
        @NotNull
        public Builder hover(@NotNull List<String> lines) {
            return replaceLast(last().withHover(Hover.text(
                    lines.size() == 1 ? ElementParser.splitHoverLines(lines.get(0)) : lines
            )));
        }

        /**
         * Attaches an item hover to the last segment.
         *
         * @param json raw item JSON, or a {@code "b64:<base64>"} encoded payload
         * @return this builder
         */
        @NotNull
        public Builder hoverItem(@NotNull String json) {
            return replaceLast(last().withHover(Hover.item(json)));
        }

        /**
         * Attaches an already built hover to the last segment.
         *
         * @param hover hover content
         * @return this builder
         */
        @NotNull
        public Builder hover(@NotNull Hover hover) {
            return replaceLast(last().withHover(hover));
        }

        /**
         * Gives the last segment an {@link Click#OPEN_URL} event built from the first URL found in
         * its text, unless it already carries a click.
         *
         * <p>An explicit click always wins, so this never overwrites a configured event.
         *
         * @return this builder
         */
        @NotNull
        public Builder autoLinkUrl() {
            Segment segment = last();
            if (segment.click != null) return this;

            Matcher matcher = ElementParser.URL_PATTERN.matcher(segment.raw);
            return matcher.find() ? replaceLast(segment.withClick(Click.OPEN_URL, matcher.group())) : this;
        }

        /**
         * Removes the click event of the last segment.
         *
         * @return this builder
         */
        @NotNull
        public Builder clearClick() {
            return replaceLast(last().withClick(null, null));
        }

        /**
         * Removes the hover event of the last segment.
         *
         * @return this builder
         */
        @NotNull
        public Builder clearHover() {
            return replaceLast(last().withHover(null));
        }

        /**
         * Applies a click event to every segment.
         *
         * @param click action to perform
         * @param value payload delivered to the action
         * @return this builder
         */
        @NotNull
        public Builder clickAll(@NotNull Click click, @Nullable String value) {
            String payload = value == null ? "" : value;
            segments.replaceAll(segment -> segment.withClick(click, payload));
            return this;
        }

        /**
         * Applies a text hover to every segment.
         *
         * @param lines hover lines
         * @return this builder
         */
        @NotNull
        public Builder hoverAll(@NotNull List<String> lines) {
            Hover hover = Hover.text(lines.size() == 1 ? ElementParser.splitHoverLines(lines.get(0)) : lines);
            segments.replaceAll(segment -> segment.withHover(hover));
            return this;
        }

        /**
         * Builds the immutable element.
         *
         * @return a new element
         */
        @NotNull
        public Element build() {
            return segments.isEmpty() ? EMPTY : new Element(segments.toArray(new Segment[0]));
        }

        private Builder prefix(String code) {
            Segment segment = last();
            return replaceLast(segment.withRaw(code + segment.raw));
        }

        private Segment last() {
            if (segments.isEmpty()) segments.add(new Segment(""));
            return segments.get(segments.size() - 1);
        }

        private Builder replaceLast(Segment segment) {
            segments.set(segments.size() - 1, segment);
            return this;
        }
    }
}
