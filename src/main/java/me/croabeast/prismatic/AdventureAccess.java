package me.croabeast.prismatic;

import java.lang.reflect.Constructor;

final class AdventureAccess {

    private static volatile Boolean available;
    private static volatile AdventureBridge bridge;

    private AdventureAccess() {}

    static boolean isAvailable() {
        if (available != null) {
            return available;
        }

        try {
            ClassLoader loader = AdventureAccess.class.getClassLoader();
            Class.forName("net.kyori.adventure.text.Component", false, loader);
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage", false, loader);
            Class.forName("net.kyori.adventure.text.minimessage.tag.resolver.TagResolver", false, loader);
            Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer", false, loader);
            return available = true;
        } catch (Throwable ignored) {
            return available = false;
        }
    }

    static AdventureBridge bridge(PrismaticCore core) {
        if (!isAvailable())
            throw new IllegalStateException("Adventure runtime is not available.");

        AdventureBridge result = bridge;
        if (result != null) return result;

        synchronized (AdventureAccess.class) {
            result = bridge;
            if (result == null) {
                result = createBridge(core);
                bridge = result;
            }
        }

        return result;
    }

    static Formatter<?> formatter(PrismaticCore core) {
        return (Formatter<?>) bridge(core);
    }

    private static AdventureBridge createBridge(PrismaticCore core) {
        try {
            Class<?> type = Class.forName(
                    "me.croabeast.prismatic.Adventure",
                    true,
                    AdventureAccess.class.getClassLoader()
            );

            Constructor<?> constructor = type.getDeclaredConstructor(PrismaticCore.class);
            constructor.setAccessible(true);
            return (AdventureBridge) constructor.newInstance(core);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Adventure runtime bridge could not be initialized.", e);
        }
    }
}
