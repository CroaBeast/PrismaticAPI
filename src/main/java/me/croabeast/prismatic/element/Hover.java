package me.croabeast.prismatic.element;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Hover content carried by an {@link Element}, either a list of text lines or an item payload.
 *
 * @since 2.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Hover {

    private static final String BASE64_PREFIX = "b64:";

    private final List<String> lines;

    /**
     * Returns the decoded item payload, or {@code null} for a text hover.
     *
     * @return raw item JSON
     */
    @Getter
    @Nullable
    private final String itemJson;

    /**
     * Creates a text hover from ordered lines.
     *
     * @param lines hover lines, colorized when the element is rendered
     * @return a new text hover
     */
    @NotNull
    public static Hover text(String... lines) {
        return text(Arrays.asList(lines));
    }

    /**
     * Creates a text hover from ordered lines.
     *
     * @param lines hover lines, colorized when the element is rendered
     * @return a new text hover
     */
    @NotNull
    public static Hover text(List<String> lines) {
        return new Hover(Collections.unmodifiableList(new ArrayList<>(lines)), null);
    }

    /**
     * Creates an item hover from an NBT/SNBT payload.
     *
     * <p>The payload is accepted either as raw JSON or as a {@code "b64:<base64>"} encoded string,
     * which avoids escaping problems when the value travels through a config file.
     *
     * @param json raw or base64 encoded item payload
     * @return a new item hover
     */
    @NotNull
    public static Hover item(String json) {
        return new Hover(null, decode(json));
    }

    /**
     * Returns whether this hover carries an item instead of text.
     *
     * @return {@code true} when this is an item hover
     */
    public boolean isItem() {
        return itemJson != null;
    }

    /**
     * Returns the hover lines, or an empty list for an item hover.
     *
     * @return ordered hover lines
     */
    @NotNull
    public List<String> getLines() {
        return lines != null ? lines : Collections.<String>emptyList();
    }

    /**
     * Returns the hover lines with their placeholders resolved against a render.
     *
     * <p>Hover text carries placeholders like any other text, so the tokens are split once and
     * resolved here rather than scanned again per receiver.
     */
    @NotNull
    List<String> resolveLines(RenderContext context) {
        List<String> source = getLines();
        List<String> resolved = new ArrayList<>(source.size());

        for (String line : source)
            resolved.add(Part.resolve(Part.split(line), context));

        return resolved;
    }

    /**
     * Returns the item payload in its {@code "b64:<base64>"} transport form.
     *
     * @return encoded payload, or {@code null} for a text hover
     */
    @Nullable
    public String getSerializedItemJson() {
        return itemJson == null ? null : BASE64_PREFIX + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(itemJson.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String payload) {
        if (payload == null || payload.isEmpty()) return payload;

        if (payload.startsWith(BASE64_PREFIX)) {
            try {
                return new String(
                        Base64.getUrlDecoder().decode(payload.substring(BASE64_PREFIX.length())),
                        StandardCharsets.UTF_8
                );
            } catch (IllegalArgumentException ignored) {}
        }

        return payload.replace("\\\\", "\\").replace("\\\"", "\"");
    }
}
