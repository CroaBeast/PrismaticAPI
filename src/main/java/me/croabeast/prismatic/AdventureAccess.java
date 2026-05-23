package me.croabeast.prismatic;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Constructor;

@UtilityClass
class AdventureAccess {

    private volatile Boolean available;
    private volatile AdventureBridge bridge;

    boolean isAvailable() {
        return available != null ? available : detectAvailable();
    }

    private boolean detectAvailable() {
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

    private AdventureBridge createBridge(PrismaticCore core) {
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

    AdventureBridge bridge(PrismaticCore core) {
        if (!isAvailable())
            throw new IllegalStateException("Adventure runtime is not available.");

        AdventureBridge result = bridge;
        return result != null ? result : initializeBridge(core);
    }

    private AdventureBridge initializeBridge(PrismaticCore core) {
        AdventureBridge result;
        synchronized (AdventureAccess.class) {
            result = bridge;
            bridge = result = result != null ? result : createBridge(core);
        }

        return result;
    }

    Formatter<?> formatter(PrismaticCore core) {
        return (Formatter<?>) bridge(core);
    }
}
