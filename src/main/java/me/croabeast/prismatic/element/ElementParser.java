package me.croabeast.prismatic.element;

import lombok.experimental.UtilityClass;
import me.croabeast.prismatic.PrismaticAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits raw text into segments.
 *
 * <p>This is the work that used to happen every time a component was constructed: locating the
 * interactive tags, splitting bare URLs, and carrying the trailing color of a segment into the
 * next one. An {@link Element} pays it once and every later render reuses the result.
 *
 * <p>Only the syntax-dependent part lives in {@link MarkupFormat}; everything here works for any
 * format.
 */
@UtilityClass
class ElementParser {

    private final Pattern HOVER_PATTERN = Pattern.compile("hover:\"(.*?)\"");

    final Pattern URL_PATTERN = Pattern.compile("(?i)\\b(?:https?://|www\\.)\\S+");

    final String LINE_SEPARATOR = "<n>";

    List<Segment> parse(String raw, MarkupFormat format) {
        List<Segment> segments = new ArrayList<>();
        appendTo(segments, raw, format);
        return segments;
    }

    void appendTo(List<Segment> segments, String raw, MarkupFormat format) {
        String line = format.prepare(raw == null ? "" : raw);
        if (line.isEmpty()) return;

        Matcher matcher = format.getPattern().matcher(line);
        int lastEnd = 0;

        while (matcher.find()) {
            appendByUrl(segments, line.substring(lastEnd, matcher.start()));

            for (Segment segment : format.accept(matcher).segments())
                add(segments, segment);

            lastEnd = matcher.end();
        }

        appendByUrl(segments, line.substring(lastEnd));
    }

    private void appendByUrl(List<Segment> segments, String text) {
        if (text.isEmpty()) return;

        Matcher matcher = URL_PATTERN.matcher(text);
        int end = 0;

        while (matcher.find()) {
            String before = text.substring(end, matcher.start());
            if (!before.isEmpty()) add(segments, new Segment(before));

            String url = matcher.group();
            add(segments, new Segment(url).withClick(Click.OPEN_URL, url));
            end = matcher.end();
        }

        String tail = text.substring(end);
        if (!tail.isEmpty()) add(segments, new Segment(tail));
    }

    /**
     * Appends a segment, carrying over the trailing color of the previous one so a segment that
     * does not open with an explicit color does not reset to white.
     */
    void add(List<Segment> segments, Segment segment) {
        if (segments.isEmpty()) {
            segments.add(segment);
            return;
        }

        String previous = PrismaticAPI.getEndColor(segments.get(segments.size() - 1).raw);
        segments.add(previous == null || PrismaticAPI.startsWithColor(segment.raw) ?
                segment :
                segment.withRaw(previous + segment.raw));
    }

    List<String> splitHoverLines(String raw) {
        String value = raw == null ? "" : raw;

        Matcher matcher = HOVER_PATTERN.matcher(value);
        while (matcher.find())
            value = value.replace(matcher.group(), matcher.group(1));

        return Arrays.asList(value.split(Pattern.quote(LINE_SEPARATOR), 0));
    }
}
