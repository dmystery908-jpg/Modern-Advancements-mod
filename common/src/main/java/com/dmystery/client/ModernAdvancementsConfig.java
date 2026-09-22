package com.dmystery.client;

import com.dmystery.ModernAdvancements;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModernAdvancementsConfig {
    public static Path getConfigDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.gameDirectory != null) {
                return mc.gameDirectory.toPath().resolve("config");
            }
        } catch (Throwable ignored) {}
        return Paths.get("config");
    }

    public enum HudPosition {
        TOP_RIGHT("modern_advancements.config.hud_position.top_right"),
        TOP_LEFT("modern_advancements.config.hud_position.top_left"),
        BOTTOM_RIGHT("modern_advancements.config.hud_position.bottom_right"),
        BOTTOM_LEFT("modern_advancements.config.hud_position.bottom_left");

        private final String key;

        HudPosition(String key) {
            this.key = key;
        }

        public Component getDisplayName() {
            return Component.translatable(this.key);
        }
    }

    private static final Path CONFIG_PATH = getConfigDirectory().resolve("modern_advancements.json");
    private static final Path LEGACY_CONFIG_PATH = getConfigDirectory().resolve("modern-advancements.json");
    private static final Path SECOND_LEGACY_CONFIG_PATH = getConfigDirectory().resolve("advancements-refined.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModernAdvancementsConfig INSTANCE = null;

    public boolean showGlobalBar = true;
    public boolean showTabBadges = true;
    public boolean showTooltipHints = true;
    public float treeZoom = 0.65f;
    public boolean hudEnabled = true;
    public HudPosition hudPosition = HudPosition.TOP_RIGHT;
    public int maxPins = 3;
    public boolean autoUnpinOnComplete = false;

    public static synchronized ModernAdvancementsConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ModernAdvancementsConfig();
            INSTANCE.load();
        }
        return INSTANCE;
    }

    public void load() {
        Path path = CONFIG_PATH;
        if (!Files.exists(path)) {
            if (Files.exists(LEGACY_CONFIG_PATH)) {
                path = LEGACY_CONFIG_PATH;
            } else if (Files.exists(SECOND_LEGACY_CONFIG_PATH)) {
                path = SECOND_LEGACY_CONFIG_PATH;
            } else {
                save();
                return;
            }
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            ModernAdvancementsConfig loaded = GSON.fromJson(reader, ModernAdvancementsConfig.class);
            if (loaded != null) {
                this.showGlobalBar = loaded.showGlobalBar;
                this.showTabBadges = loaded.showTabBadges;
                this.showTooltipHints = loaded.showTooltipHints;
                this.treeZoom = Mth.clamp(loaded.treeZoom, 0.5f, 1.0f);
                this.hudEnabled = loaded.hudEnabled;
                this.hudPosition = loaded.hudPosition != null ? loaded.hudPosition : HudPosition.TOP_RIGHT;
                this.maxPins = Mth.clamp(loaded.maxPins, 1, 5);
                this.autoUnpinOnComplete = loaded.autoUnpinOnComplete;
            }
        } catch (Exception e) {
            ModernAdvancements.LOGGER.error("Failed to load configuration from {}", CONFIG_PATH, e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            ModernAdvancements.LOGGER.error("Failed to save configuration to {}", CONFIG_PATH, e);
        }
    }

    public void resetDefaults() {
        this.showGlobalBar = true;
        this.showTabBadges = true;
        this.showTooltipHints = true;
        this.treeZoom = 0.65f;
        this.hudEnabled = true;
        this.hudPosition = HudPosition.TOP_RIGHT;
        this.maxPins = 3;
        this.autoUnpinOnComplete = false;
        save();
    }
}
