package me.croabeast.prismatic.element;

import me.croabeast.prismatic.PrismaticAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Color capability profile of a render.
 *
 * <p>Only two profiles exist in practice, which is what makes a rendered {@link Element} cacheable
 * with two slots instead of one entry per player.
 *
 * @since 2.0.0
 */
public enum Target {

    /** The receiver understands exact RGB colors. */
    HEX,
    /** The receiver only understands the sixteen legacy colors. */
    LEGACY;

    /**
     * Resolves the profile of a player.
     *
     * @param player player to inspect, or {@code null} for the server default
     * @return the matching profile
     */
    @NotNull
    public static Target of(@Nullable Player player) {
        return PrismaticAPI.supportsHexColors(player) ? HEX : LEGACY;
    }
}
