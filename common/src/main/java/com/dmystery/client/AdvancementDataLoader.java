package com.dmystery.client;

import com.mojang.logging.LogUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public class AdvancementDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void ensureAdvancementsLoaded(ClientAdvancements clientAdvancements) {
        // In 1.20.1, packet updates and advancement tree synchronization are handled by vanilla ClientAdvancements.
    }

    public static void clearCache() {
    }
}
