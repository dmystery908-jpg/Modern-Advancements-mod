package com.dmystery.neoforge;

import com.dmystery.ModernAdvancements;
import com.dmystery.ModernAdvancementsClient;
import com.dmystery.client.ModernAdvancementsConfigScreen;
import com.dmystery.client.PinnedAdvancementsHud;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = ModernAdvancements.MOD_ID, dist = Dist.CLIENT)
public final class ModernAdvancementsNeoForge {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModernAdvancements.MOD_ID);
    private static final PinnedAdvancementsHud HUD = new PinnedAdvancementsHud();

    public ModernAdvancementsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        ModernAdvancements.init();
        ModernAdvancementsClient.init();

        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::registerGuiLayers);

        NeoForge.EVENT_BUS.addListener(this::onClientTick);

        modContainer.registerExtensionPoint(
            IConfigScreenFactory.class,
            (container, parent) -> new ModernAdvancementsConfigScreen(parent)
        );

        LOGGER.info("[Modern Advancements] NeoForge client initialized with HUD Pinning and Settings support.");
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModernAdvancementsClient.OPEN_SETTINGS_KEY);
    }

    private void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
            VanillaGuiLayers.HOTBAR,
            ModernAdvancements.id("pinned_advancements"),
            (graphics, deltaTracker) -> HUD.render(graphics, deltaTracker)
        );
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        while (ModernAdvancementsClient.OPEN_SETTINGS_KEY.consumeClick()) {
            if (client != null) {
                client.setScreen(new ModernAdvancementsConfigScreen(client.screen));
            }
        }
    }
}
