package me.croabeast.prismatic;

import me.croabeast.prismatic.color.ColorPattern;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.regex.Pattern;

final class PrismaticCore {

    private static final Pattern MINI_MESSAGE_TAG_PATTERN =
            Pattern.compile("(?i)</?[#a-z0-9_:-]+(?:\\s*:[^<>]*)?>");

    private final ColorEngine colorEngine = new ColorEngine();

    ChatColor fromString(String string, boolean legacy) {
        return colorEngine.fromString(string, legacy);
    }

    ChatColor fromString(String string) {
        return colorEngine.fromString(string);
    }

    String applyColor(Color color, String string, boolean legacy) {
        return colorEngine.applyColor(color, string, legacy);
    }

    String applyGradient(String string, Color start, Color end, boolean legacy) {
        return colorEngine.applyGradient(string, start, end, legacy);
    }

    String applyRainbow(String string, float saturation, boolean legacy) {
        return colorEngine.applyRainbow(string, saturation, legacy);
    }

    String colorize(@Nullable Player player, String string) {
        return StringUtils.isBlank(string) ? string == null ? "" : string : colorizeNonBlank(player, string);
    }

    private String colorizeNonBlank(@Nullable Player player, String string) {
        boolean legacy = shouldUseLegacyColors(player);
        return string.indexOf('<') != -1 && AdventureAccess.isAvailable() ?
                AdventureAccess.bridge(this).colorizeLegacy(string, legacy) :
                applyLegacyPipeline(string, legacy);
    }

    String stripBukkit(String string) {
        return colorEngine.stripBukkit(string);
    }

    String stripSpecial(String string) {
        return colorEngine.stripSpecial(string);
    }

    String stripRGB(String string) {
        return StringUtils.isBlank(string) ?
                string :
                ColorPattern.SINGLE.strip(ColorPattern.MULTI.strip(string));
    }

    String stripMiniMessage(String string) {
        return StringUtils.isBlank(string) || string.indexOf('<') == -1 ? string : stripMiniMessageTags(string);
    }

    private String stripMiniMessageTags(String string) {
        String adventureStripped = tryStripMiniMessageWithAdventure(string);
        return adventureStripped != null ? adventureStripped : MINI_MESSAGE_TAG_PATTERN.matcher(string).replaceAll("");
    }

    @Nullable
    private String tryStripMiniMessageWithAdventure(String string) {
        return AdventureAccess.isAvailable() ? stripMiniMessageWithAdventure(string) : null;
    }

    @Nullable
    private String stripMiniMessageWithAdventure(String string) {
        try {
            return AdventureAccess.bridge(this).stripMiniMessage(string);
        } catch (Throwable ignored) {}

        return null;
    }

    String stripAll(String string) {
        return stripRGB(stripMiniMessage(stripSpecial(stripBukkit(string))));
    }

    boolean startsWithColor(String string) {
        return colorEngine.startsWithColor(colorize(null, string));
    }

    @Nullable
    String getStartColor(String string) {
        return colorEngine.getStartColor(colorize(null, string));
    }

    @Nullable
    String getEndColor(String string) {
        return colorEngine.getEndColor(colorize(null, string));
    }

    boolean shouldUseLegacyColors(@Nullable Player player) {
        return !Capabilities.supportsHex(player);
    }

    String applyLegacyPipeline(String string, boolean legacy) {
        return ChatColor.translateAlternateColorCodes('&', applyRgbPipeline(string, legacy));
    }

    private String applyRgbPipeline(String string, boolean legacy) {
        return ColorPattern.SINGLE.apply(ColorPattern.MULTI.apply(string, legacy), legacy);
    }
}
