package me.croabeast.prismatic;

import me.croabeast.prismatic.color.ColorPattern;
import me.croabeast.vnc.VNC;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

final class PrismaticCore {

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
        if (StringUtils.isBlank(string)) {
            return string == null ? "" : string;
        }

        boolean legacy = shouldUseLegacyColors(player);
        if (string.indexOf('<') != -1 && AdventureAccess.isAvailable()) {
            return AdventureAccess.bridge(this).colorizeLegacy(string, legacy);
        }

        return applyLegacyPipeline(string, legacy);
    }

    String stripBukkit(String string) {
        return colorEngine.stripBukkit(string);
    }

    String stripSpecial(String string) {
        return colorEngine.stripSpecial(string);
    }

    String stripRGB(String string) {
        if (StringUtils.isBlank(string)) return string;

        string = ColorPattern.MULTI.strip(string);
        return ColorPattern.SINGLE.strip(string);
    }

    String stripAll(String string) {
        return stripRGB(stripSpecial(stripBukkit(string)));
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
        return !canUseHexColors(player);
    }

    String applyLegacyPipeline(String string, boolean legacy) {
        return ChatColor.translateAlternateColorCodes('&', applyRgbPipeline(string, legacy));
    }

    private String applyRgbPipeline(String string, boolean legacy) {
        string = ColorPattern.MULTI.apply(string, legacy);
        return ColorPattern.SINGLE.apply(string, legacy);
    }

    private boolean canUseHexColors() {
        return VNC.SERVER_MINECRAFT_VERSION.supportsHex();
    }

    private boolean canUseHexColors(@Nullable Player player) {
        return canUseHexColors() &&
                player != null &&
                VNC.player(player).supportsHex();
    }
}
