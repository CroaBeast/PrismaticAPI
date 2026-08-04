package me.croabeast.prismatic.element;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Click action carried by an {@link Element}.
 *
 * <p>The constant holds no platform type on purpose: the mapping to a Bungee
 * {@code ClickEvent.Action} or to an Adventure {@code ClickEvent} happens inside the matching
 * renderer, so the element model stays free of both libraries.
 *
 * @since 2.0.0
 */
public enum Click {

    /** Runs a command as the player who clicks the text. */
    RUN_COMMAND("execute", "click", "run"),
    /** Inserts text into the player's chat input without sending it. */
    SUGGEST_COMMAND("suggest"),
    /** Opens a URL in the player's default browser. */
    OPEN_URL("open_url", "url"),
    /** Opens a file on the player's local filesystem. */
    OPEN_FILE("open_file", "file"),
    /** Changes the current page in a written book. */
    CHANGE_PAGE("change_page", "page"),
    /** Copies text to the player's clipboard. */
    COPY_TO_CLIPBOARD("clipboard", "copy");

    private final List<String> names = new ArrayList<>();

    /**
     * The first alias is the one {@link #toString()} returns, which is also what
     * {@link Element#toMarkup()} serializes with. Keeping it separate from {@link #name()} lets the
     * constants carry the canonical action names without changing the emitted markup.
     */
    Click(String... aliases) {
        Collections.addAll(names, aliases);
        names.add(name().toLowerCase(Locale.ENGLISH));
    }

    /**
     * Returns the primary lowercase alias of this action.
     *
     * @return primary alias, never {@code null}
     */
    @Override
    public String toString() {
        return names.get(0);
    }

    /**
     * Resolves an action from a case-insensitive alias.
     *
     * @param name alias to look up, such as {@code "run"}, {@code "url"} or {@code "copy"}
     * @return the matching action, or {@link #SUGGEST_COMMAND} when the alias is blank or unknown
     */
    public static Click fromName(String name) {
        if (StringUtils.isBlank(name)) return SUGGEST_COMMAND;

        String lower = name.toLowerCase(Locale.ENGLISH);
        for (Click click : values())
            if (click.names.contains(lower)) return click;

        return SUGGEST_COMMAND;
    }
}
