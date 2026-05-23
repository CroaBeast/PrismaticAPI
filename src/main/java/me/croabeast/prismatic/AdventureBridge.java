package me.croabeast.prismatic;

/**
 * Internal bridge interface that decouples the Adventure-specific implementation from the
 * core Prismatic pipeline.
 *
 * <p>Because Adventure classes are optional at runtime, this interface is defined using only
 * types that are always available on the classpath. The concrete implementation
 * ({@code me.croabeast.prismatic.Adventure}) is loaded reflectively by {@link AdventureAccess}
 * only when the required Adventure classes are detected.
 *
 * <p>Plugin code should never depend on this interface directly. Use
 * {@link PrismaticAPI#adventure()} and {@link PrismaticAPI#isAdventureAvailable()} instead.
 */
interface AdventureBridge {

    /**
     * Colorizes a string through the full Prismatic pipeline and serializes the result back
     * to a legacy section-sign ({@code §}) color-code string.
     *
     * <p>This method is called by {@link PrismaticCore} when Adventure is available and the
     * input string contains {@code '<'} characters that could be MiniMessage tags. The
     * {@code legacy} flag controls whether hex colors are downsampled to the nearest Bukkit
     * palette entry.
     *
     * @param string the raw text to colorize; must not be {@code null}
     * @param legacy {@code true} to downsample RGB colors to the legacy 16-color Bukkit palette;
     *               {@code false} to preserve exact hex output
     * @return the colorized string serialized to legacy section-sign color codes
     */
    String colorizeLegacy(String string, boolean legacy);

    /**
     * Removes MiniMessage formatting tags from a string using the Adventure MiniMessage API.
     *
     * <p>This method is preferred over a plain regex strip when Adventure is available because
     * MiniMessage's own tag-stripping logic handles edge cases (e.g. nested tags, custom tag
     * resolvers) more reliably than a generic pattern.
     *
     * @param string the text from which MiniMessage tags should be removed; must not be {@code null}
     * @return the text with all MiniMessage tags stripped
     */
    String stripMiniMessage(String string);
}
