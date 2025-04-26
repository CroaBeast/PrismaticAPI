package me.croabeast.prismatic;

import com.google.common.collect.Lists;
import com.viaversion.viaversion.api.Via;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a mapping between Minecraft client protocol versions and a simplified major version.
 * <p>
 * The {@code ClientVersion} class is used to determine the major client version from a player's protocol
 * version, which is helpful when implementing version-specific features or handling legacy clients.
 * It maintains an internal list of {@code ClientVersion} instances, each corresponding to a range of protocol
 * numbers. If no matching version is found, it returns an unknown version.
 * </p>
 * <p>
 * This class also offers utility methods to check if a client is considered legacy.
 * </p>
 * <p>
 * Example usage:
 * <pre><code>
 * int majorVersion = ClientVersion.getClientVersion(player);
 * boolean legacy = ClientVersion.legacy(player);
 * System.out.println("Player client major version: " + majorVersion);
 * </code></pre>
 * </p>
 *
 * @see Via
 */
public final class ClientVersion {

    /**
     * A list of all registered client version mappings.
     */
    private static final List<ClientVersion> PROTOCOL_LIST = new ArrayList<>();

    /**
     * Represents an unknown client version.
     */
    private static final ClientVersion UNKNOWN = new ClientVersion(0, 0, 0);

    static final double SERVER_VERSION;

    static {
        double main = 0.0;

        Pattern p = Pattern.compile("1\\.(\\d+(\\.\\d+)?)");
        Matcher m = p.matcher(Bukkit.getVersion());

        if (m.find())
            try {
                main = Double.parseDouble(m.group(1));
            } catch (Exception ignored) {
            }

        SERVER_VERSION = main;

        new ClientVersion(7, 0, 5);
        new ClientVersion(8, 6, 47);
        new ClientVersion(9, 48, 110);
        new ClientVersion(10, 201, 210, fromInts(206, 209));
        new ClientVersion(11, 301, 316);
        new ClientVersion(12, 317, 340);
        new ClientVersion(13, 341, 404);
        new ClientVersion(14, 441, 500, fromInts(499));
        new ClientVersion(15, 550, 578);
        new ClientVersion(16, 701, 754);
        new ClientVersion(17, 755, 756);
        new ClientVersion(18, 757, 758);
        new ClientVersion(19, 759, 762);
        new ClientVersion(20, 763, 766);
        new ClientVersion(21, 767, 770);
    }

    /**
     * The major version number for this client version.
     */
    private final int version;

    /**
     * A list of protocol numbers associated with this major client version.
     */
    private final List<Integer> protocols;

    /**
     * Creates a list of integers representing a range from the given start to end (inclusive).
     *
     * @param numbers an array of two integers where the first is the start and the second is the end of the range
     * @return a list of protocol numbers in the specified range
     */
    private static List<Integer> fromInts(Integer... numbers) {
        if (numbers.length != 2)
            return Lists.newArrayList(numbers);
        int z = numbers[1], y = numbers[0];
        Integer[] array = new Integer[(z - y) + 1];
        int index = 0;
        for (int i = y; i <= z; i++) {
            array[index] = i;
            index++;
        }
        return new ArrayList<>(Arrays.asList(array));
    }

    /**
     * Constructs a new {@code ClientVersion} with the specified major version and protocol range,
     * optionally excluding certain protocol numbers.
     *
     * @param version the major version number
     * @param start   the starting protocol number (inclusive)
     * @param end     the ending protocol number (inclusive)
     * @param ignore  a list of protocol numbers to ignore; may be {@code null} or empty
     */
    private ClientVersion(int version, int start, int end, List<Integer> ignore) {
        this.version = version;
        List<Integer> range = fromInts(start, end);
        if (ignore == null || ignore.isEmpty()) {
            protocols = range;
            PROTOCOL_LIST.add(this);
            return;
        }
        range.removeIf(ignore::contains);
        protocols = range;
        PROTOCOL_LIST.add(this);
    }

    /**
     * Constructs a new {@code ClientVersion} with the specified major version and protocol range.
     *
     * @param version the major version number
     * @param start   the starting protocol number (inclusive)
     * @param end     the ending protocol number (inclusive)
     */
    private ClientVersion(int version, int start, int end) {
        this(version, start, end, null);
    }

    /**
     * Returns a string representation of the client version.
     * <p>
     * For unknown versions, it returns "UNKNOWN_CLIENT:0". Otherwise, it returns "CLIENT:" followed by the major version number.
     * </p>
     *
     * @return a string representing the client version
     */
    @Override
    public String toString() {
        return version == 0 ? "UNKNOWN_CLIENT:0" : ("CLIENT:" + version);
    }

    /**
     * Returns an array containing all registered {@code ClientVersion} instances.
     *
     * @return an array of {@code ClientVersion} objects
     */
    public static ClientVersion[] values() {
        return PROTOCOL_LIST.toArray(new ClientVersion[0]);
    }

    /**
     * Determines the major client version for the given player based on their protocol version.
     * <p>
     * If ViaVersion is not enabled or the player is {@code null}, the server's version is returned.
     * Otherwise, the player's protocol version is retrieved, and the corresponding major version is determined.
     * </p>
     *
     * @param player the player whose client version is to be determined; may be {@code null}
     * @return the major client version number, or {@code 0} for unknown versions
     */
    public static int getClientVersion(Player player) {
        if (player == null)
            return (int) SERVER_VERSION;

        if (!Bukkit.getPluginManager().isPluginEnabled("ViaVersion"))
            return (int) SERVER_VERSION;

        int i = Via.getAPI().getPlayerVersion(player.getUniqueId());
        for (ClientVersion p : values()) {
            if (p == UNKNOWN) continue;
            if (p.protocols.contains(i)) return p.version;
        }

        return UNKNOWN.version;
    }

    /**
     * Checks whether the specified player's client is considered legacy.
     * <p>
     * A legacy client is defined as one with a major version of 15 or lower.
     * </p>
     *
     * @param player the player whose client version is to be checked
     * @return {@code true} if the player's client is legacy; {@code false} otherwise
     */
    public static boolean isLegacy(Player player) {
        return getClientVersion(player) <= 15;
    }
}
