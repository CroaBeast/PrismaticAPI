package me.croabeast.prismatic.element;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders segments to Bungee components.
 *
 * <p>The colorized text of every segment comes from the segment cache, but the components
 * themselves are always built fresh: {@code TextComponent} is mutable, so handing the same
 * instances to two callers would let one of them change what the other sends.
 */
@UtilityClass
class BungeeRenderer {

    @SuppressWarnings("deprecation")
    BaseComponent[] render(Segment[] segments, RenderContext context) {
        List<BaseComponent> components = new ArrayList<>(segments.length);

        for (Segment segment : segments) {
            String text = segment.render(context);
            if (text.isEmpty() && !segment.hasEvents()) continue;

            TextComponent component = new TextComponent(TextComponent.fromLegacyText(text));

            if (segment.click != null)
                component.setClickEvent(new ClickEvent(action(segment.click), value(segment, context)));

            if (segment.hover != null)
                component.setHoverEvent(hover(segment.hover, context));

            components.add(component);
        }

        return components.toArray(new BaseComponent[0]);
    }

    private String value(Segment segment, RenderContext context) {
        return context.colorize(Part.resolve(segment.clickParts, context));
    }

    @SuppressWarnings("deprecation")
    private HoverEvent hover(Hover hover, RenderContext context) {
        if (hover.isItem())
            return new HoverEvent(
                    HoverEvent.Action.SHOW_ITEM,
                    new ComponentBuilder(hover.getItemJson()).create()
            );

        List<String> lines = hover.resolveLines(context);
        BaseComponent[] contents = new BaseComponent[lines.size()];

        for (int i = 0; i < lines.size(); i++) {
            String line = context.colorize(lines.get(i));
            if (i != lines.size() - 1) line += "\n";
            contents[i] = new TextComponent(TextComponent.fromLegacyText(line));
        }

        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, contents);
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
