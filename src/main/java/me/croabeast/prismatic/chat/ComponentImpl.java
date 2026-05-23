package me.croabeast.prismatic.chat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuppressWarnings("deprecation")
final class ComponentImpl implements ChatComponent<ComponentImpl> {

    private static final Pattern HOVER_PATTERN = Pattern.compile("hover:\"(.*?)\"");
    private static final String HOVER_ITEM_BASE64_PREFIX = "b64:";

    private final ChatProcessor processor;
    @NotNull
    private String message;

    private ClickHolder clickEvent;
    private HoverHolder hoverEvent;
    private HoverItemHolder hoverItemEvent;

    ComponentImpl(ChatProcessor processor, String message) {
        this.processor = Objects.requireNonNull(processor, "processor");
        this.message = message == null ? "" : message;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class ClickHolder {
        final Click click;
        final String input;

        ClickEvent create(Player player) {
            Click action = click == null ? Click.SUGGEST : click;
            return new ClickEvent(action.asBukkit(), processor.colorize(player, input));
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class HoverHolder {
        final List<String> list;

        HoverEvent create(Player player) {
            BaseComponent[] contents = new BaseComponent[list.size()];
            int size = list.size();

            for (int i = 0; i < size; i++) {
                String line = processor.colorize(player, list.get(i));
                if (i != size - 1) line += "\n";
                contents[i] = new TextComponent(TextComponent.fromLegacyText(line));
            }

            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, contents);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static final class HoverItemHolder {
        final String itemJson;
        final String serializedJson;

        HoverEvent create() {
            return new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(itemJson).create());
        }
    }

    @NotNull
    @Override
    public ComponentImpl setMessage(@NotNull String message) {
        this.message = Objects.requireNonNull(message, "message");
        return instance();
    }

    @NotNull
    @Override
    public ComponentImpl setClick(Click click, String input) {
        clickEvent = new ClickHolder(click, input);
        return instance();
    }

    @NotNull
    @Override
    public ComponentImpl setHover(List<String> list) {
        hoverItemEvent = null;
        hoverEvent = new HoverHolder(Objects.requireNonNull(list, "list"));
        return instance();
    }

    @NotNull
    @Override
    public ComponentImpl setHover(String string) {
        string = string == null ? "" : string;
        Matcher matcher = HOVER_PATTERN.matcher(string);
        while (matcher.find())
            string = string.replace(matcher.group(), matcher.group(1));
        return setHover(processor.splitLines(string));
    }

    @NotNull
    @Override
    public ComponentImpl setHoverItem(String json) {
        if (StringUtils.isBlank(json)) return instance();

        String itemJson = deserializeHoverItem(json);
        hoverEvent = null;
        hoverItemEvent = new HoverItemHolder(itemJson, serializeHoverItem(itemJson));
        return instance();
    }

    boolean hasClick() {
        return clickEvent != null && (clickEvent.click != null || StringUtils.isNotBlank(clickEvent.input));
    }

    boolean hasHover() {
        return hoverEvent != null && hoverEvent.list != null && !hoverEvent.list.isEmpty();
    }

    boolean hasHoverItem() {
        return hoverItemEvent != null && StringUtils.isNotBlank(hoverItemEvent.itemJson);
    }

    @Override
    public boolean hasEvents() {
        return hasClick() || hasHover() || hasHoverItem();
    }

    @NotNull
    @Override
    public BaseComponent[] compile(Player player) {
        Matcher urlMatch = URL_PATTERN.matcher(message);
        if (urlMatch.find())
            setClick(Click.OPEN_URL, urlMatch.group());

        BaseComponent[] array = TextComponent.fromLegacyText(processor.colorize(player, message));
        TextComponent component = new TextComponent(array);

        if (clickEvent != null)
            component.setClickEvent(clickEvent.create(player));

        if (hoverItemEvent != null)
            component.setHoverEvent(hoverItemEvent.create());
        else if (hoverEvent != null)
            component.setHoverEvent(hoverEvent.create(player));

        return new BaseComponent[] {component};
    }

    private String deserializeHoverItem(String payload) {
        if (StringUtils.isBlank(payload)) return payload;

        String decoded = tryDecodeHoverItem(payload);
        return decoded != null ? decoded : payload.replace("\\\\", "\\").replace("\\\"", "\"");
    }

    private String tryDecodeHoverItem(String payload) {
        if (!payload.startsWith(HOVER_ITEM_BASE64_PREFIX)) return null;

        try {
            return new String(
                    Base64.getUrlDecoder().decode(payload.substring(HOVER_ITEM_BASE64_PREFIX.length())),
                    StandardCharsets.UTF_8
            );
        } catch (IllegalArgumentException ignored) {}

        return null;
    }

    private String serializeHoverItem(String itemJson) {
        return StringUtils.isBlank(itemJson) ?
                itemJson :
                HOVER_ITEM_BASE64_PREFIX + Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(itemJson.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (BaseComponent component : compile(null))
            builder.append(component.toLegacyText());
        return builder.toString();
    }

    @NotNull
    @Override
    public ComponentImpl instance() {
        return this;
    }
}
