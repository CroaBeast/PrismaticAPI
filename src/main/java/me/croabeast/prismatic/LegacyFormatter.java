package me.croabeast.prismatic;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

final class LegacyFormatter implements Formatter<String> {

    private final PrismaticCore core;

    LegacyFormatter(PrismaticCore core) {
        this.core = core;
    }

    @Override
    public ChatColor fromString(String string, boolean legacy) {
        return core.fromString(string, legacy);
    }

    @Override
    public String applyColor(Color color, String string, boolean legacy) {
        return core.applyColor(color, string, legacy);
    }

    @Override
    public String applyGradient(String string, Color start, Color end, boolean legacy) {
        return core.applyGradient(string, start, end, legacy);
    }

    @Override
    public String applyRainbow(String string, float saturation, boolean legacy) {
        return core.applyRainbow(string, saturation, legacy);
    }

    @Override
    public String colorize(@Nullable Player player, String string) {
        return core.colorize(player, string);
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
}
