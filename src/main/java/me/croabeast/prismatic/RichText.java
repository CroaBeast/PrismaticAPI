package me.croabeast.prismatic;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable pair containing both the Adventure and legacy representations of the same text.
 *
 * <p>{@code RichText} is returned by the high-level formatting methods in {@link PrismaticAPI}
 * when callers want to keep both output forms available without re-running the formatting
 * pipeline.</p>
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class RichText {

    @NotNull
    private final Component component;
    @NotNull
    private final String legacy;

    /**
     * Returns the Adventure component representation.
     *
     * @return Adventure component result
     */
    @NotNull
    public Component component() {
        return component;
    }

    /**
     * Returns the legacy string representation.
     *
     * @return legacy string result
     */
    @NotNull
    public String asLegacy() {
        return legacy;
    }
}
