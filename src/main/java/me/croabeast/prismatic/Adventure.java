package me.croabeast.prismatic;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
final class Adventure {

    private Boolean available;

    private final Pattern[] PRISMATIC_BLOCK_PATTERNS = new Pattern[] {
            Pattern.compile("(?is)<#[a-f\\d]{6}(?::#[a-f\\d]{6})+>.+?</g(?:radient)?>"),
            Pattern.compile("(?is)<g:[a-f\\d]{6}>.+?</g:[a-f\\d]{6}>"),
            Pattern.compile("(?is)<gradient:[a-f\\d]{6}>.+?</gradient:[a-f\\d]{6}>"),
            Pattern.compile("(?is)<#[a-f\\d]{6}>.+?</#[a-f\\d]{6}>"),
            Pattern.compile("(?is)<rainbow:\\d{1,3}>.+?</rainbow>"),
            Pattern.compile("(?is)<r:\\d{1,3}>.+?</r>")
    };

    private final Pattern[] SINGLE_COLOR_PATTERNS = new Pattern[] {
            Pattern.compile("(?i)\\{#([a-f\\d]{6})}"),
            Pattern.compile("(?i)%#([a-f\\d]{6})%"),
            Pattern.compile("(?i)\\[#([a-f\\d]{6})]"),
            Pattern.compile("(?i)&x([a-f\\d]{6})"),
            Pattern.compile("(?i)(?<![</])&?#([a-f\\d]{6})")
    };

    private final Pattern LEGACY_CODE_PATTERN = Pattern.compile("(?i)[&\\u00A7]([0-9a-fk-or])");

    boolean isAvailable() {
        if (available != null)
            return available;

        try {
            MiniMessage.miniMessage();
            TagResolver.empty();
            legacySerializer();
            return available = true;
        } catch (Throwable ignored) {
            return available = false;
        }
    }

    RichText colorize(String string, boolean legacy, BiFunction<String, Boolean, String> rgbColorizer) {
        if (StringUtils.isBlank(string)) {
            String safe = string == null ? "" : string;
            return new RichText(Component.text(safe), safe);
        }

        TokenizedComponents masked = maskPrismaticBlocks(string, legacy, rgbColorizer);
        String normalized = normalizeMiniMessage(masked.value);

        Component component = MiniMessage.miniMessage().deserialize(normalized, TagResolver.empty());
        component = replaceComponentTokens(component, masked.tokens);

        if (legacy)
            component = downsample(component);

        return new RichText(component, legacySerializer().serialize(component));
    }

    private TokenizedComponents maskPrismaticBlocks(
            String string,
            boolean legacy,
            BiFunction<String, Boolean, String> rgbColorizer
    ) {
        List<Token<Component>> tokens = new ArrayList<>();
        for (Pattern pattern : PRISMATIC_BLOCK_PATTERNS)
            string = maskComponentMatches(string, pattern, legacy, rgbColorizer, tokens);
        return new TokenizedComponents(string, tokens);
    }

    private String normalizeMiniMessage(String string) {
        for (Pattern pattern : SINGLE_COLOR_PATTERNS)
            string = replaceMatches(string, pattern, matcher -> "<#" + matcher.group(1) + ">");

        return replaceMatches(string, LEGACY_CODE_PATTERN, matcher -> legacyTag(matcher.group(1).charAt(0)));
    }

    private String legacyTag(char code) {
        switch (Character.toLowerCase(code)) {
            case '0':
                return "<black>";
            case '1':
                return "<dark_blue>";
            case '2':
                return "<dark_green>";
            case '3':
                return "<dark_aqua>";
            case '4':
                return "<dark_red>";
            case '5':
                return "<dark_purple>";
            case '6':
                return "<gold>";
            case '7':
                return "<gray>";
            case '8':
                return "<dark_gray>";
            case '9':
                return "<blue>";
            case 'a':
                return "<green>";
            case 'b':
                return "<aqua>";
            case 'c':
                return "<red>";
            case 'd':
                return "<light_purple>";
            case 'e':
                return "<yellow>";
            case 'f':
                return "<white>";
            case 'k':
                return "<obfuscated>";
            case 'l':
                return "<bold>";
            case 'm':
                return "<strikethrough>";
            case 'n':
                return "<underlined>";
            case 'o':
                return "<italic>";
            case 'r':
                return "<reset>";
            default:
                return String.valueOf(code);
        }
    }

    private String maskComponentMatches(
            String string,
            Pattern pattern,
            boolean legacy,
            BiFunction<String, Boolean, String> rgbColorizer,
            List<Token<Component>> tokens
    ) {
        Matcher matcher = pattern.matcher(string);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String token = token(tokens.size());
            String legacyValue = rgbColorizer.apply(matcher.group(), legacy);
            tokens.add(new Token<>(token, legacySerializer().deserialize(legacyValue)));
            matcher.appendReplacement(result, Matcher.quoteReplacement(token));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private Component replaceComponentTokens(Component component, List<Token<Component>> tokens) {
        for (Token<Component> token : tokens)
            component = component.replaceText(builder -> builder.matchLiteral(token.key).replacement(token.value));
        return component;
    }

    private String replaceMatches(String string, Pattern pattern, Replacer replacer) {
        Matcher matcher = pattern.matcher(string);
        StringBuffer result = new StringBuffer();
        while (matcher.find())
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacer.replace(matcher)));
        matcher.appendTail(result);
        return result.toString();
    }

    private Component downsample(Component component) {
        TextColor color = component.color();
        if (color != null && !(color instanceof NamedTextColor))
            component = component.color(NamedTextColor.nearestTo(color));

        List<Component> children = component.children();
        if (children.isEmpty())
            return component;

        List<Component> mapped = new ArrayList<>(children.size());
        for (Component child : children)
            mapped.add(downsample(child));

        return component.children(mapped);
    }

    private String token(int index) {
        return "__PRISM_MM_TOKEN_" + index + "__";
    }

    private LegacyComponentSerializer legacySerializer() {
        return LegacyComponentSerializer.legacySection();
    }

    @FunctionalInterface
    private interface Replacer {
        String replace(Matcher matcher);
    }

    private static final class Token<T> {

        private final String key;
        private final T value;

        private Token(String key, T value) {
            this.key = key;
            this.value = value;
        }
    }

    private static final class TokenizedComponents {

        private final String value;
        private final List<Token<Component>> tokens;

        private TokenizedComponents(String value, List<Token<Component>> tokens) {
            this.value = value;
            this.tokens = Collections.unmodifiableList(tokens);
        }
    }
}
