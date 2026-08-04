package me.croabeast.prismatic;

import com.viaversion.viaversion.api.Via;
import lombok.experimental.UtilityClass;
import me.croabeast.vnc.MinecraftVersion;
import me.croabeast.vnc.VNC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves and caches the color capabilities of a player.
 *
 * <p>The protocol of a connected player cannot change during a session, so the lookup is resolved
 * once per {@link UUID} instead of once per formatted line. The cache is bounded, so a consumer
 * that never calls {@link #forget(UUID)} still cannot leak.
 */
@UtilityClass
class Capabilities {

    private final Map<UUID, Boolean> HEX_SUPPORT = new ConcurrentHashMap<>();
    private final int MAX_ENTRIES = 512;

    boolean supportsHex(@Nullable Player player) {
        if (!serverSupportsHex()) return false;
        if (player == null) return true;

        UUID id = player.getUniqueId();
        Boolean cached = HEX_SUPPORT.get(id);
        if (cached != null) return cached;

        boolean supported = resolveHexSupport(player);

        if (HEX_SUPPORT.size() >= MAX_ENTRIES) HEX_SUPPORT.clear();
        HEX_SUPPORT.put(id, supported);

        return supported;
    }

    void forget(UUID id) {
        if (id != null) HEX_SUPPORT.remove(id);
    }

    void clear() {
        HEX_SUPPORT.clear();
    }

    private boolean serverSupportsHex() {
        return VNC.SERVER_MINECRAFT_VERSION != null && VNC.SERVER_MINECRAFT_VERSION.supportsHex();
    }

    private boolean resolveHexSupport(Player player) {
        MinecraftVersion version = MinecraftVersion.fromProtocol(playerProtocol(player));
        return version != null ? version.supportsHex() : serverSupportsHex();
    }

    private int playerProtocol(Player player) {
        return isViaVersionEnabled() ? viaPlayerProtocol(player) : serverProtocol();
    }

    private boolean isViaVersionEnabled() {
        try {
            return Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
        } catch (Throwable ignored) {}

        return false;
    }

    private int viaPlayerProtocol(Player player) {
        try {
            return Via.getAPI().getPlayerVersion(player.getUniqueId());
        } catch (Throwable ignored) {}

        return serverProtocol();
    }

    private int serverProtocol() {
        return VNC.SERVER_MINECRAFT_VERSION != null &&
                VNC.SERVER_MINECRAFT_VERSION.getProtocol() != null ?
                VNC.SERVER_MINECRAFT_VERSION.getProtocol() : -1;
    }
}
