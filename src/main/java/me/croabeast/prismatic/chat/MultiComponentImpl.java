package me.croabeast.prismatic.chat;

import lombok.Getter;
import me.croabeast.prismatic.PrismaticAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
final class MultiComponentImpl implements MultiComponent {

    private static final Pattern LEGACY_FORMAT_PATTERN =
            Pattern.compile("(?i)(hover|run|suggest|url)=\\[(.[^|\\[\\]]*)]");
    private static final Pattern HOVER_PATTERN = Pattern.compile("hover:\"(.*?)\"");

    private final ChatProcessor processor;
    private final String message;
    private final LinkedList<Segment> list = new LinkedList<>();

    private ChatFormat<ChatComponent<?>> format = new ChatFormat<ChatComponent<?>>() {
        @Getter
        private final String regex = DEFAULT_REGEX;

        private void setAction(ComponentImpl component, String action, String argument) {
            if ("hover_item".equalsIgnoreCase(action)) {
                component.setHoverItem(argument);
                return;
            }

            if ("hover".equalsIgnoreCase(action)) {
                component.setHover(argument);
                return;
            }

            component.setClick(action, argument);
        }

        @NotNull
        public ChatComponent<?> accept(Player player, String string) {
            ComponentImpl component = new ComponentImpl(processor, string);
            Matcher matcher = matcher(string);

            if (matcher.find()) {
                component = new ComponentImpl(processor, matcher.group(5));

                setAction(component, matcher.group(1), matcher.group(2));

                String secondAction = matcher.group(3);
                String secondArgument = matcher.group(4);
                if (secondAction != null && secondArgument != null)
                    setAction(component, secondAction, secondArgument);
            }

            return component;
        }

        @Override
        public String removeFormat(String string) {
            string = stripLegacyFormat(string);
            if (StringUtils.isBlank(string)) return string;

            Matcher matcher = format.matcher(string);
            while (matcher.find())
                string = string.replace(matcher.group(), matcher.group(5));
            return string;
        }

        @NotNull
        public String toFormattedString(ChatComponent<?> component) {
            if (component instanceof MultiComponentImpl)
                return formatMultiComponent((MultiComponentImpl) component);

            return component instanceof ComponentImpl ?
                    formatSingleComponent((ComponentImpl) component) :
                    component.getMessage();
        }
    };

    private final class Segment {
        final ChatComponent<?> component;
        String lastColor;

        Segment(ChatComponent<?> component) {
            this.component = component;

            String message = this.component.getMessage();
            lastColor = PrismaticAPI.getEndColor(message);

            Segment segment;
            try {
                segment = list.getLast();
            } catch (Exception ignored) {
                return;
            }

            if (PrismaticAPI.startsWithColor(message) || segment.lastColor == null)
                return;

            message = segment.lastColor + message;
            this.component.setMessage(message);
            lastColor = PrismaticAPI.getEndColor(message);
        }

        Segment(String message) {
            this(format.accept(message));
        }

        BaseComponent[] compile(Player player) {
            return component.compile(player);
        }
    }

    MultiComponentImpl(ChatProcessor processor, String message) {
        this.processor = Objects.requireNonNull(processor, "processor");
        this.message = message == null ? "" : message;
        splitToSegments(formatMessage(this.message));
    }

    private MultiComponentImpl(MultiComponentImpl component) {
        this.processor = component.processor;
        this.message = component.message;
        this.format = component.format;

        if (!component.list.isEmpty())
            this.list.addAll(component.list);
    }

    private static String stripLegacyFormat(String string) {
        if (StringUtils.isBlank(string)) return string;

        Matcher old = LEGACY_FORMAT_PATTERN.matcher(string);
        while (old.find()) {
            String temp = old.group(1) + ":\"" + old.group(2) + "\"";
            string = string.replace(old.group(), temp);
        }
        return string;
    }

    private String formatMultiComponent(MultiComponentImpl component) {
        List<Segment> segments = component.list;
        if (segments.isEmpty()) return "";

        StringBuilder result = new StringBuilder();
        for (Segment segment : segments) {
            ChatComponent<?> componentPart = segment.component;
            result.append(componentPart instanceof ComponentImpl ?
                    formatSingleComponent((ComponentImpl) componentPart) :
                    componentPart.getMessage());
        }

        return result.toString();
    }

    private String formatSingleComponent(ComponentImpl component) {
        if (!component.hasEvents()) return component.getMessage();

        StringBuilder builder = new StringBuilder("<");
        boolean hasAction = appendClick(builder, component);
        hasAction = appendHoverItem(builder, component, hasAction);
        appendHover(builder, component, hasAction);

        return builder.append('>')
                .append(component.getMessage())
                .append("</text>")
                .toString();
    }

    private boolean appendClick(StringBuilder builder, ComponentImpl component) {
        return component.hasClick() && appendAction(builder, false, component.getClickEvent().click.toString(), component.getClickEvent().input);
    }

    private boolean appendHoverItem(StringBuilder builder, ComponentImpl component, boolean hasAction) {
        return component.hasHoverItem() ?
                appendAction(builder, hasAction, "hover_item", component.getHoverItemEvent().serializedJson) :
                hasAction;
    }

    private void appendHover(StringBuilder builder, ComponentImpl component, boolean hasAction) {
        if (component.hasHover())
            appendAction(
                    builder,
                    hasAction,
                    "hover",
                    String.join(processor.getLineSeparator(), component.getHoverEvent().list)
            );
    }

    private boolean appendAction(StringBuilder builder, boolean hasAction, String action, String argument) {
        if (hasAction) builder.append('|');
        builder.append(action).append(":\"").append(argument).append('"');
        return true;
    }

    private String formatMessage(String string) {
        return processor.prepare(stripLegacyFormat(string));
    }

    private void splitByUrl(String text) {
        Matcher urlMatcher = ChatComponent.URL_PATTERN.matcher(text);
        int end = 0;

        while (urlMatcher.find()) {
            String before = text.substring(end, urlMatcher.start());
            if (!before.isEmpty()) list.add(new Segment(before));

            String url = urlMatcher.group();
            ChatComponent<?> component = ChatComponent.fromString(processor, url);
            list.add(new Segment(component.setClick(Click.OPEN_URL, url)));
            end = urlMatcher.end();
        }

        String tail = text.substring(end);
        if (!tail.isEmpty()) list.add(new Segment(tail));
    }

    private void splitToSegments(String line) {
        Matcher tagMatcher = format.matcher(line);
        int lastEnd = 0;

        while (tagMatcher.find()) {
            splitByUrl(line.substring(lastEnd, tagMatcher.start()));

            list.add(new Segment(tagMatcher.group()));
            lastEnd = tagMatcher.end();
        }

        splitByUrl(line.substring(lastEnd));
    }

    @NotNull
    public MultiComponent setFormat(@NotNull ChatFormat<ChatComponent<?>> format) {
        this.format = Objects.requireNonNull(format, "format");
        return instance();
    }

    @Override
    public boolean hasEvents() {
        for (Segment segment : list)
            if (segment.component.hasEvents()) return true;

        return false;
    }

    @NotNull
    public MultiComponent copy() {
        return new MultiComponentImpl(this);
    }

    @NotNull
    public MultiComponent append(String message) {
        splitToSegments(formatMessage(message));
        return instance();
    }

    @NotNull
    public MultiComponent append(@NotNull ChatComponent<?> component) {
        list.add(new Segment(component));
        return instance();
    }

    @NotNull
    public MultiComponent setClickToAll(Click click, String input) {
        list.forEach(segment -> segment.component.setClick(click, input));
        return instance();
    }

    @NotNull
    public MultiComponent setHoverToAll(List<String> list) {
        this.list.forEach(segment -> segment.component.setHover(list));
        return instance();
    }

    @NotNull
    public MultiComponent setHoverToAll(String string) {
        string = string == null ? "" : string;
        Matcher matcher = HOVER_PATTERN.matcher(string);
        while (matcher.find())
            string = string.replace(matcher.group(), matcher.group(1));
        return setHoverToAll(processor.splitLines(string));
    }

    @NotNull
    public MultiComponent setHoverItemToAll(String json) {
        this.list.forEach(segment -> segment.component.setHoverItem(json));
        return instance();
    }

    @NotNull
    public MultiComponent setClick(Click click, String input) {
        int lastIndex = list.size() - 1;
        if (lastIndex >= 0)
            list.get(lastIndex).component.setClick(click, input);
        return instance();
    }

    @NotNull
    public MultiComponent setHover(List<String> list) {
        int lastIndex = this.list.size() - 1;
        if (lastIndex >= 0)
            this.list.get(lastIndex).component.setHover(list);
        return instance();
    }

    @NotNull
    public MultiComponent setHover(String string) {
        string = string == null ? "" : string;
        Matcher matcher = HOVER_PATTERN.matcher(string);
        while (matcher.find())
            string = string.replace(matcher.group(), matcher.group(1));
        return setHover(processor.splitLines(string));
    }

    @NotNull
    public MultiComponent setHoverItem(String json) {
        int lastIndex = list.size() - 1;
        if (lastIndex >= 0)
            list.get(lastIndex).component.setHoverItem(json);
        return instance();
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    @NotNull
    public BaseComponent[] compile(Player player) {
        List<BaseComponent> components = new ArrayList<>();
        for (Segment segment : list)
            Collections.addAll(components, segment.compile(player));
        return components.toArray(new BaseComponent[0]);
    }

    @Override
    public String toString() {
        return toFormattedString();
    }

    @NotNull
    public MultiComponent instance() {
        return this;
    }
}
