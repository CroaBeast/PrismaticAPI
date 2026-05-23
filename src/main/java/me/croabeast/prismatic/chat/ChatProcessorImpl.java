package me.croabeast.prismatic.chat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.croabeast.prismatic.PrismaticAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ChatProcessorImpl implements ChatProcessor {

    static final ChatProcessorImpl INSTANCE = new ChatProcessorImpl();

    @NotNull
    @Override
    public String colorize(@Nullable Player player, String string) {
        return PrismaticAPI.colorize(player, string);
    }
}
