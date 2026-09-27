package com.dmystery.client;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancementDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static List<AdvancementHolder> cachedVanillaAdvancements = null;

    private static final List<ResourceLocation> CANONICAL_ROOTS = List.of(
            ResourceLocation.fromNamespaceAndPath("minecraft", "story/root"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "nether/root"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "end/root"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "adventure/root"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "husbandry/root")
    );

    private static int compareRoots(ResourceLocation a, ResourceLocation b) {
        int idxA = CANONICAL_ROOTS.indexOf(a);
        int idxB = CANONICAL_ROOTS.indexOf(b);
        if (idxA != -1 && idxB != -1) {
            return Integer.compare(idxA, idxB);
        }
        if (idxA != -1) return -1;
        if (idxB != -1) return 1;
        return a.compareTo(b);
    }

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

        List<AdvancementHolder> candidateRoots = new ArrayList<>();
        List<AdvancementHolder> candidateTasks = new ArrayList<>();
        for (AdvancementHolder candidate : candidates) {
            if (candidate.value().display().isPresent() && clientAdvancements.getTree().get(candidate.id()) == null) {
                if (candidate.value().isRoot()) {
                    candidateRoots.add(candidate);
                } else {
                    candidateTasks.add(candidate);
                }
            }
        }

        candidateRoots.sort((a, b) -> compareRoots(a.id(), b.id()));

        List<AdvancementHolder> toAdd = new ArrayList<>(candidateRoots.size() + candidateTasks.size());
        toAdd.addAll(candidateRoots);
        toAdd.addAll(candidateTasks);

        if (!toAdd.isEmpty()) {
            clientAdvancements.getTree().addAll(toAdd);
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

        try (MultiPackResourceManager resourceManager = new MultiPackResourceManager(
                PackType.SERVER_DATA,
                List.of(minecraft.getVanillaPackResources())
        )) {
            Map<ResourceLocation, Advancement> rawAdvancements = new HashMap<>();
            SimpleJsonResourceReloadListener.scanDirectory(
                    resourceManager,
                    FileToIdConverter.registry(Registries.ADVANCEMENT),
                    registryAccess.createSerializationContext(JsonOps.INSTANCE),
                    Advancement.CODEC,
                    rawAdvancements
            );

            List<AdvancementHolder> holders = new ArrayList<>();
            for (Map.Entry<ResourceLocation, Advancement> entry : rawAdvancements.entrySet()) {
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
