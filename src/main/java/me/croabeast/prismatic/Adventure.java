package me.croabeast.prismatic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Adventure implements Formatter<Component>, AdventureBridge {

    private static final Pattern[] PRISMATIC_BLOCK_PATTERNS = new Pattern[] {
            Pattern.compile("(?is)<#[a-f\\d]{6}(?::#[a-f\\d]{6})+>.+?</g(?:radient)?>"),
            Pattern.compile("(?is)<g:[a-f\\d]{6}>.+?</g:[a-f\\d]{6}>"),
            Pattern.compile("(?is)<gradient:[a-f\\d]{6}>.+?</gradient:[a-f\\d]{6}>"),
            Pattern.compile("(?is)<#[a-f\\d]{6}>.+?</#[a-f\\d]{6}>"),
            Pattern.compile("(?is)<rainbow:\\d{1,3}>.+?</rainbow>"),
            Pattern.compile("(?is)<r:\\d{1,3}>.+?</r>")
    };

    private static final Pattern[] SINGLE_COLOR_PATTERNS = new Pattern[] {
            Pattern.compile("(?i)\\{#([a-f\\d]{6})}"),
            Pattern.compile("(?i)%#([a-f\\d]{6})%"),
            Pattern.compile("(?i)\\[#([a-f\\d]{6})]"),
            Pattern.compile("(?i)&x([a-f\\d]{6})"),
            Pattern.compile("(?i)(?<![</])&?#([a-f\\d]{6})")
    };

    private static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("(?i)[&\\u00A7]([0-9a-fk-or])");

    private final PrismaticCore core;

    Adventure(PrismaticCore core) {
        this.core = core;
    }

    @Override
    public ChatColor fromString(String string, boolean legacy) {
        return core.fromString(string, legacy);
    }

    @Override
    public Component applyColor(Color color, String string, boolean legacy) {
        return deserialize(core.applyColor(color, string, legacy));
    }

    @Override
    public Component applyGradient(String string, Color start, Color end, boolean legacy) {
        return deserialize(core.applyGradient(string, start, end, legacy));
    }

    @Override
    public Component applyRainbow(String string, float saturation, boolean legacy) {
        return deserialize(core.applyRainbow(string, saturation, legacy));
    }

    @Override
    public Component colorize(@Nullable Player player, String string) {
        if (StringUtils.isBlank(string)) {
            return Component.text(string == null ? "" : string);
        }

        return colorizeComponent(string, core.shouldUseLegacyColors(player));
    }

    @Override
    public String stripBukkit(String string) {
        return core.stripBukkit(string);
    }

    @Override
    public String stripSpecial(String string) {
        return core.stripSpecial(string);
    }

    @Override
    public String stripRGB(String string) {
        return core.stripRGB(string);
    }

    @Override
    public String stripAll(String string) {
        return core.stripAll(string);
    }

    @Override
    public boolean startsWithColor(String string) {
        return core.startsWithColor(string);
    }

    @Override
    public String getStartColor(String string) {
        return core.getStartColor(string);
    }

    @Override
    public String getEndColor(String string) {
        return core.getEndColor(string);
    }

    @Override
    public String colorizeLegacy(String string, boolean legacy) {
        if (StringUtils.isBlank(string)) {
            return string == null ? "" : string;
        }

        if (string.indexOf('<') == -1) {
            return core.applyLegacyPipeline(string, legacy);
        }

        return serialize(colorizeComponent(string, legacy));
    }

    private TokenizedComponents maskPrismaticBlocks(String string, boolean legacy) {
        List<Token<Component>> tokens = new ArrayList<>();
        for (Pattern pattern : PRISMATIC_BLOCK_PATTERNS) {
            string = maskComponentMatches(string, pattern, legacy, tokens);
        }
        return new TokenizedComponents(string, tokens);
    }

    private String normalizeMiniMessage(String string) {
        for (Pattern pattern : SINGLE_COLOR_PATTERNS) {
            string = replaceMatches(string, pattern, matcher -> "<#" + matcher.group(1) + ">");
        }

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
            List<Token<Component>> tokens
    ) {
        Matcher matcher = pattern.matcher(string);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String token = token(tokens.size());
            String legacyValue = core.applyLegacyPipeline(matcher.group(), legacy);
            tokens.add(new Token<>(token, deserialize(legacyValue)));
            matcher.appendReplacement(result, Matcher.quoteReplacement(token));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private Component replaceComponentTokens(Component component, List<Token<Component>> tokens) {
        for (Token<Component> token : tokens) {
            component = component.replaceText(builder -> builder.matchLiteral(token.key).replacement(token.value));
        }
        return component;
    }

    private String replaceMatches(String string, Pattern pattern, Replacer replacer) {
        Matcher matcher = pattern.matcher(string);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacer.replace(matcher)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private Component downsample(Component component) {
        TextColor color = component.color();
        if (color != null && !(color instanceof NamedTextColor)) {
            component = component.color(NamedTextColor.nearestTo(color));
        }

        List<Component> children = component.children();
        if (children.isEmpty()) {
            return component;
        }

        List<Component> mapped = new ArrayList<>(children.size());
        for (Component child : children) {
            mapped.add(downsample(child));
        }

        return component.children(mapped);
    }

    private String token(int index) {
        return "__PRISM_MM_TOKEN_" + index + "__";
    }

    private Component colorizeComponent(String string, boolean legacy) {
        if (string.indexOf('<') == -1) {
            return deserialize(core.applyLegacyPipeline(string, legacy));
        }

        TokenizedComponents masked = maskPrismaticBlocks(string, legacy);
        String normalized = normalizeMiniMessage(masked.value);

        Component component = MiniMessage.miniMessage().deserialize(normalized, TagResolver.empty());
        component = replaceComponentTokens(component, masked.tokens);

        return legacy ? downsample(component) : component;
    }

    private Component deserialize(String legacy) {
        String safe = legacy == null ? "" : legacy;
        return legacySerializer().deserialize(safe);
    }

    private String serialize(Component component) {
        return legacySerializer().serialize(component);
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
