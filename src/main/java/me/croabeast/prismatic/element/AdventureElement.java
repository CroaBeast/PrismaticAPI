package me.croabeast.prismatic.element;

import lombok.experimental.UtilityClass;
import me.croabeast.prismatic.PrismaticAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Adventure output for {@link Element}.
 *
 * <p>This lives apart from {@code Element} on purpose. Adventure is an optional runtime
 * dependency, so the type every consumer touches on every message must not name a Kyori class.
 * A server without Adventure never loads this class because nothing else references it.
 *
 * <p>Guard the call with {@link PrismaticAPI#isAdventureAvailable()} when Adventure is optional in
 * the consuming plugin.
 *
 * @since 2.0.0
 */
@UtilityClass
public class AdventureElement {

    /**
     * Renders an element to an Adventure component for a player.
     *
     * @param element element to render
     * @param player  receiving player, or {@code null} for the server default
     * @return the rendered component
     * @throws IllegalStateException if the Adventure runtime is not available
     */
    @NotNull
    public Component render(@NotNull Element element, @Nullable Player player) {
        return render(element, RenderContext.of(player));
    }

    /**
     * Renders an element to an Adventure component.
     *
     * @param element element to render
     * @param context render context
     * @return the rendered component
     * @throws IllegalStateException if the Adventure runtime is not available
     */
    @NotNull
    public Component render(@NotNull Element element, @NotNull RenderContext context) {
        Component result = Component.empty();

        for (Segment segment : element.segments()) {
            String text = segment.resolve(context);
            if (text.isEmpty()) continue;

            Component child = PrismaticAPI.adventure().colorize(context.getPlayer(), text);

            if (segment.click != null)
                child = child.clickEvent(ClickEvent.clickEvent(action(segment.click), clickValue(segment)));

            if (segment.hover != null)
                child = child.hoverEvent(hover(segment.hover, context));

            result = result.append(child);
        }

        return result;
    }

    private String clickValue(Segment segment) {
        return segment.clickValue == null ? "" : segment.clickValue;
    }

    private HoverEvent<?> hover(Hover hover, RenderContext context) {
        if (hover.isItem())
            return HoverEvent.showText(Component.text(String.valueOf(hover.getItemJson())));

        List<String> lines = hover.getLines();
        Component content = Component.empty();

        for (int i = 0; i < lines.size(); i++) {
            content = content.append(PrismaticAPI.adventure().colorize(context.getPlayer(), lines.get(i)));
            if (i != lines.size() - 1) content = content.append(Component.newline());
        }

        return HoverEvent.showText(content);
    }

    private ClickEvent.Action action(Click click) {
        switch (click) {
            case RUN_COMMAND:
                return ClickEvent.Action.RUN_COMMAND;
            case OPEN_URL:
                return ClickEvent.Action.OPEN_URL;
            case OPEN_FILE:
                return ClickEvent.Action.OPEN_FILE;
            case CHANGE_PAGE:
                return ClickEvent.Action.CHANGE_PAGE;
            case COPY_TO_CLIPBOARD:
                return ClickEvent.Action.COPY_TO_CLIPBOARD;
            default:
                return ClickEvent.Action.SUGGEST_COMMAND;
        }
    }
}
