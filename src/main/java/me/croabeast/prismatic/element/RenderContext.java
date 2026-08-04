package me.croabeast.prismatic.element;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.croabeast.prismatic.PrismaticAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * Everything a render needs that is not part of the parsed {@link Element}: who receives it, which
 * color profile applies, and how placeholders are resolved.
 *
 * <p>The profile is resolved once per render instead of once per segment, which is what removes
 * the repeated protocol lookup from lore and scoreboard loops.
 *
 * @since 2.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RenderContext {

    /**
     * Returns the receiving player.
     *
     * @return the player, or {@code null}
     */
    @Getter
    @Nullable
    private final Player player;

    /**
     * Returns the color profile of this render.
     *
     * @return the target profile
     */
    @Getter
    @NotNull
    private final Target target;

    private final UnaryOperator<String> resolver;
    private final BiFunction<Player, String, String> formatter;

    /**
     * Creates a context for a player, leaving placeholders untouched.
     *
     * @param player receiving player, or {@code null} for the server default
     * @return a new context
     */
    @NotNull
    public static RenderContext of(@Nullable Player player) {
        return new RenderContext(player, Target.of(player), null, null);
    }

    /**
     * Creates a context for a player with a placeholder resolver.
     *
     * <p>The resolver receives the token with its delimiters, so it sees {@code {player}} and
     * {@code %vault_eco_balance%} verbatim and can tell the two syntaxes apart. Returning
     * {@code null} leaves the original token in place.
     *
     * @param player   receiving player, or {@code null} for the server default
     * @param resolver placeholder resolver
     * @return a new context
     */
    @NotNull
    public static RenderContext of(@Nullable Player player, @Nullable UnaryOperator<String> resolver) {
        return new RenderContext(player, Target.of(player), resolver, null);
    }

    /**
     * Returns a copy of this context that colorizes through the given formatter instead of through
     * {@link PrismaticAPI#colorize(Player, String)}.
     *
     * <p>This is the seam a consumer needs when it applies its own preparation step, such as small
     * caps or alignment, before Prismatic sees the text. A context with a custom formatter is not
     * cacheable, because the cached value of a segment belongs to the default pipeline.
     *
     * @param formatter colorizer receiving the player and the resolved text
     * @return a new context
     */
    @NotNull
    public RenderContext withFormatter(@Nullable BiFunction<Player, String, String> formatter) {
        return new RenderContext(player, target, resolver, formatter);
    }

    /**
     * Returns whether this context can resolve placeholders.
     *
     * <p>A context without a resolver produces output that depends only on the target, so an
     * element rendered through it stays cacheable even when it contains placeholder tokens.
     *
     * @return {@code true} when a resolver is present
     */
    public boolean hasResolver() {
        return resolver != null;
    }

    /**
     * Returns whether this context renders through the default Prismatic pipeline.
     *
     * @return {@code true} when no custom formatter was supplied
     */
    public boolean hasDefaultFormatter() {
        return formatter == null;
    }

    /**
     * Resolves a placeholder token.
     *
     * @param literal the token with its delimiters, returned when the resolver declines
     * @return the replacement value
     */
    @NotNull
    String resolve(String literal) {
        if (resolver == null) return literal;

        String value = resolver.apply(literal);
        return value != null ? value : literal;
    }

    /**
     * Colorizes text through this context.
     *
     * @param text text to colorize
     * @return the colorized text
     */
    @NotNull
    public String colorize(String text) {
        String value = formatter == null ?
                PrismaticAPI.colorize(player, text) :
                formatter.apply(player, text);

        return value == null ? "" : value;
    }
}
