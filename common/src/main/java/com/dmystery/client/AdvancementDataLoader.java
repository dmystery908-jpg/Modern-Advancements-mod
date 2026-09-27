package com.dmystery.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.*;

public class AdvancementDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static Map<ResourceLocation, Advancement.Builder> cachedVanillaBuilders = null;

    private static final List<ResourceLocation> CANONICAL_ROOTS = List.of(
        new ResourceLocation("minecraft", "story/root"),
        new ResourceLocation("minecraft", "nether/root"),
        new ResourceLocation("minecraft", "end/root"),
        new ResourceLocation("minecraft", "adventure/root"),
        new ResourceLocation("minecraft", "husbandry/root")
    );

    public static int compareRoots(ResourceLocation a, ResourceLocation b) {
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
        AdvancementList clientList = clientAdvancements.getAdvancements();

        List<Advancement> candidateRoots = new ArrayList<>();
        List<Advancement> candidateTasks = new ArrayList<>();

        if (singleplayerServer != null && singleplayerServer.getAdvancements() != null) {
            for (Advancement adv : singleplayerServer.getAdvancements().getAllAdvancements()) {
                if (adv.getDisplay() != null && clientList.get(adv.getId()) == null) {
                    if (adv.getParent() == null) {
                        candidateRoots.add(adv);
                    } else {
                        candidateTasks.add(adv);
                    }
                }
            }
        }

        candidateRoots.sort((a, b) -> compareRoots(a.getId(), b.getId()));

        Map<ResourceLocation, Advancement.Builder> toAdd = new LinkedHashMap<>();
        for (Advancement root : candidateRoots) {
            toAdd.put(root.getId(), root.deconstruct());
        }
        for (Advancement task : candidateTasks) {
            toAdd.put(task.getId(), task.deconstruct());
        }

        if (toAdd.isEmpty() && singleplayerServer == null) {
            Map<ResourceLocation, Advancement.Builder> vanilla = getOrCreateVanillaBuilders();
            List<Map.Entry<ResourceLocation, Advancement.Builder>> rootEntries = new ArrayList<>();
            List<Map.Entry<ResourceLocation, Advancement.Builder>> taskEntries = new ArrayList<>();

            for (Map.Entry<ResourceLocation, Advancement.Builder> entry : vanilla.entrySet()) {
                if (clientList.get(entry.getKey()) == null) {
                    if (CANONICAL_ROOTS.contains(entry.getKey())) {
                        rootEntries.add(entry);
                    } else {
                        taskEntries.add(entry);
                    }
                }
            }

            rootEntries.sort((a, b) -> compareRoots(a.getKey(), b.getKey()));

            for (Map.Entry<ResourceLocation, Advancement.Builder> entry : rootEntries) {
                toAdd.put(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<ResourceLocation, Advancement.Builder> entry : taskEntries) {
                toAdd.put(entry.getKey(), entry.getValue());
            }
        }

        if (!toAdd.isEmpty()) {
            clientList.add(toAdd);
        }

        // Ensure the internal roots set in clientList is always sorted in canonical order
        if (clientList.getRoots() instanceof Set<?> set) {
            @SuppressWarnings("unchecked")
            Set<Advancement> rootsSet = (Set<Advancement>) set;
            if (rootsSet.size() > 1) {
                List<Advancement> sortedRoots = new ArrayList<>(rootsSet);
                sortedRoots.sort((a, b) -> compareRoots(a.getId(), b.getId()));
                rootsSet.clear();
                rootsSet.addAll(sortedRoots);
            }
        }
    }

    private static synchronized Map<ResourceLocation, Advancement.Builder> getOrCreateVanillaBuilders() {
        if (cachedVanillaBuilders != null) {
            return cachedVanillaBuilders;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Map<ResourceLocation, Advancement.Builder> map = new HashMap<>();
        LootDataManager lootDataManager = new LootDataManager();

        try (MultiPackResourceManager resourceManager = new MultiPackResourceManager(
                PackType.SERVER_DATA,
                List.of(minecraft.getVanillaPackResources())
        )) {
            FileToIdConverter converter = FileToIdConverter.json("advancements");
            Map<ResourceLocation, Resource> resources = converter.listMatchingResources(resourceManager);

            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                ResourceLocation id = converter.fileToId(entry.getKey());
                try (Reader reader = entry.getValue().openAsReader()) {
                    JsonObject json = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                    if (json != null && json.has("display")) {
                        DeserializationContext ctx = new DeserializationContext(id, lootDataManager);
                        Advancement.Builder builder = Advancement.Builder.fromJson(json, ctx);
                        if (builder != null) {
                            map.put(id, builder);
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.error("[Modern Advancements] Failed to parse advancement JSON {}: {}", id, ex.getMessage());
                }
            }

            cachedVanillaBuilders = Map.copyOf(map);
            LOGGER.info("[Modern Advancements] Loaded {} vanilla advancement builders for offline discovery", cachedVanillaBuilders.size());
            return cachedVanillaBuilders;
        } catch (Exception e) {
            LOGGER.error("[Modern Advancements] Failed to load vanilla advancements from resources", e);
            return Map.of();
        }
    }

    public static void clearCache() {
        cachedVanillaBuilders = null;
    }
}
