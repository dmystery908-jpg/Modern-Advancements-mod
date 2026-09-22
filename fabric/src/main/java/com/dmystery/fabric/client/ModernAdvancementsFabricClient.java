package com.dmystery.fabric.client;

import com.dmystery.ModernAdvancements;
import com.dmystery.ModernAdvancementsClient;
import com.dmystery.client.ModernAdvancementsConfigScreen;
import com.dmystery.client.PinnedAdvancementsHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModernAdvancementsFabricClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModernAdvancements.MOD_ID);
    private static final PinnedAdvancementsHud HUD = new PinnedAdvancementsHud();

    @Override
    public void onInitializeClient() {
        ModernAdvancementsClient.init();

        KeyBindingHelper.registerKeyBinding(ModernAdvancementsClient.OPEN_SETTINGS_KEY);

        HudRenderCallback.EVENT.register((graphics, deltaTracker) -> HUD.render(graphics, deltaTracker));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ModernAdvancementsClient.OPEN_SETTINGS_KEY.consumeClick()) {
                if (client != null) {
                    client.setScreen(new ModernAdvancementsConfigScreen(client.screen));
                }
            }
        });

        LOGGER.info("[Modern Advancements] Fabric client initialized with HUD Pinning and Settings support.");
    }
}
