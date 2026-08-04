package me.croabeast.prismatic.element;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A literal run of text, or a placeholder token resolved at render time.
 *
 * <p>Keeping placeholders as parts instead of substituting them into the raw string is what lets a
 * gradient be computed over the resolved value: baking the colors at parse time spreads the
 * gradient over the length of {@code {player}} instead of over the actual name.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class Part {

    /** Tokens whose first character is {@code '#'} are Prismatic color syntax, not placeholders. */
    private static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\{([^{}#\\s][^{}]*)}|%([^%#\\s][^%\\s]*)%");

    final String value;
    final boolean placeholder;
    final String literal;

    static Part[] split(String raw) {
        if (raw == null || raw.isEmpty())
            return new Part[] {new Part("", false, "")};

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(raw);
        if (!matcher.find())
            return new Part[] {new Part(raw, false, raw)};

        List<Part> parts = new ArrayList<>();
        int last = 0;

        do {
            if (matcher.start() > last)
                parts.add(literal(raw.substring(last, matcher.start())));

            String name = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            parts.add(new Part(name, true, matcher.group()));
            last = matcher.end();
        } while (matcher.find());

        if (last < raw.length())
            parts.add(literal(raw.substring(last)));

        return parts.toArray(new Part[0]);
    }

    private static Part literal(String value) {
        return new Part(value, false, value);
    }
}
