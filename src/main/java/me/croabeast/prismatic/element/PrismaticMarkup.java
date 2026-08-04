package me.croabeast.prismatic.element;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The built-in {@link MarkupFormat}.
 *
 * <p>Syntax: {@code <action:"argument">text</text>}, optionally with a second action separated by
 * {@code |}. Recognized actions are the {@link Click} aliases plus {@code hover} and
 * {@code hover_item}.
 */
final class PrismaticMarkup implements MarkupFormat {

    static final PrismaticMarkup INSTANCE = new PrismaticMarkup();

    private static final String CLICK_REGEX =
            "execute|click|(?:run|suggest)(?:_command)?|(?:open_)?(?:url|file)|(?:change_)?page|copy|(?:copy_to_)?clipboard";

    private static final Pattern PATTERN = Pattern.compile(
            "(?i)<(?:(hover_item|hover|" + CLICK_REGEX + "):\"(.[^|]*?)\""
                    + "(?:[|](?:(hover_item|hover|" + CLICK_REGEX + "):\"(.[^|]*?)\"))?)>(.+?)</text>"
    );

    /** Pre-1.5 syntax, normalized to the current one before matching. */
    private static final Pattern LEGACY_FORMAT_PATTERN =
            Pattern.compile("(?i)(hover|run|suggest|url)=\\[(.[^|\\[\\]]*)]");

    private PrismaticMarkup() {}

    @NotNull
    public Pattern getPattern() {
        return PATTERN;
    }

    @NotNull
    public Element accept(@NotNull Matcher matcher) {
        Segment segment = applyAction(new Segment(matcher.group(5)), matcher.group(1), matcher.group(2));

        String action = matcher.group(3);
        String argument = matcher.group(4);
        if (action != null && argument != null)
            segment = applyAction(segment, action, argument);

        return new Element(new Segment[] {segment});
    }

    @NotNull
    public String prepare(String raw) {
        if (StringUtils.isBlank(raw)) return raw == null ? "" : raw;

        String value = raw;
        Matcher matcher = LEGACY_FORMAT_PATTERN.matcher(value);
        while (matcher.find())
            value = value.replace(matcher.group(), matcher.group(1) + ":\"" + matcher.group(2) + "\"");

        return value;
    }

    private static Segment applyAction(Segment segment, String action, String argument) {
        if ("hover_item".equalsIgnoreCase(action))
            return segment.withHover(Hover.item(argument));

        if ("hover".equalsIgnoreCase(action))
            return segment.withHover(Hover.text(ElementParser.splitHoverLines(argument)));

        return segment.withClick(Click.fromName(action), argument);
    }
}
