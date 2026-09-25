package com.dmystery.client;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.StrictJsonParser;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdvancementDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static List<AdvancementHolder> cachedVanillaAdvancements = null;

    public static void ensureAdvancementsLoaded(ClientAdvancements clientAdvancements) {
        if (clientAdvancements == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        MinecraftServer singleplayerServer = minecraft.getSingleplayerServer();

        Collection<AdvancementHolder> candidates = null;

        if (singleplayerServer != null && singleplayerServer.getAdvancements() != null) {
            candidates = singleplayerServer.getAdvancements().getAllAdvancements();
        } else {
            candidates = getOrCreateVanillaAdvancements();
        }

        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        List<AdvancementHolder> missing = new ArrayList<>();
        for (AdvancementHolder candidate : candidates) {
            if (clientAdvancements.tree().get(candidate.id()) == null) {
                missing.add(candidate);
            }
        }

        if (!missing.isEmpty()) {
            clientAdvancements.tree().addAll(missing);
        }
    }

    private static synchronized List<AdvancementHolder> getOrCreateVanillaAdvancements() {
        if (cachedVanillaAdvancements != null) {
            return cachedVanillaAdvancements;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        if (connection == null) {
            return List.of();
        }

        RegistryAccess registryAccess = connection.registryAccess();
        Optional<Registry<Advancement>> advRegistry = registryAccess.lookup(Registries.ADVANCEMENT);
        if (advRegistry.isPresent()) {
            List<AdvancementHolder> holders = new ArrayList<>();
            for (Map.Entry<ResourceKey<Advancement>, Advancement> entry : advRegistry.get().entrySet()) {
                if (entry.getValue().display().isPresent()) {
                    holders.add(new AdvancementHolder(entry.getKey().identifier(), entry.getValue()));
                }
            }
            if (!holders.isEmpty()) {
                cachedVanillaAdvancements = List.copyOf(holders);
                LOGGER.info("Modern Advancements: Loaded {} advancements from registry", cachedVanillaAdvancements.size());
                return cachedVanillaAdvancements;
            }
        }

        try (MultiPackResourceManager resourceManager = new MultiPackResourceManager(
                PackType.SERVER_DATA,
                List.of(minecraft.getVanillaPackResources().fullResources())
        )) {
            Map<Identifier, Advancement> rawAdvancements = new HashMap<>();
            FileToIdConverter lister = FileToIdConverter.registry(Registries.ADVANCEMENT);
            DynamicOps<JsonElement> ops = registryAccess.createSerializationContext(JsonOps.INSTANCE);
            for (Map.Entry<Identifier, Resource> entry : lister.listMatchingResources(resourceManager).entrySet()) {
                Identifier fileId = entry.getKey();
                Identifier advId = lister.fileToId(fileId);
                try (BufferedReader reader = entry.getValue().openAsReader()) {
                    JsonElement json = StrictJsonParser.parse(reader);
                    Advancement.CODEC.parse(ops, json).result().ifPresent(adv -> rawAdvancements.put(advId, adv));
                } catch (Exception e) {
                    LOGGER.error("Couldn't parse advancement data file '{}' from '{}'", advId, fileId, e);
                }
            }

            List<AdvancementHolder> holders = new ArrayList<>();
            for (Map.Entry<Identifier, Advancement> entry : rawAdvancements.entrySet()) {
                if (entry.getValue().display().isPresent()) {
                    holders.add(new AdvancementHolder(entry.getKey(), entry.getValue()));
                }
            }

            cachedVanillaAdvancements = List.copyOf(holders);
            LOGGER.info("Modern Advancements: Loaded {} vanilla display advancements", cachedVanillaAdvancements.size());
            return cachedVanillaAdvancements;
        } catch (Exception e) {
            LOGGER.error("Modern Advancements: Failed to load vanilla advancements", e);
            return List.of();
        }
    }

    public static void clearCache() {
        cachedVanillaAdvancements = null;
    }
}
