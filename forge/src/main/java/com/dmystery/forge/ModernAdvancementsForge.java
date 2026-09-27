package com.dmystery.forge;

import com.dmystery.ModernAdvancements;
import com.dmystery.ModernAdvancementsClient;
import com.dmystery.client.ModernAdvancementsConfigScreen;
import com.dmystery.client.PinnedAdvancementsHud;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ModernAdvancements.MOD_ID)
public final class ModernAdvancementsForge {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModernAdvancements.MOD_ID);
    private static final PinnedAdvancementsHud HUD = new PinnedAdvancementsHud();

    public ModernAdvancementsForge() {
        ModernAdvancements.init();

        // Register client-only display test so connecting to pure vanilla servers works without issue
        ModLoadingContext.get().registerExtensionPoint(
            IExtensionPoint.DisplayTest.class,
            () -> new IExtensionPoint.DisplayTest(
                () -> IExtensionPoint.DisplayTest.IGNORESERVERONLY,
                (remote, isServer) -> true
            )
        );

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModernAdvancementsClient.init();

            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            modEventBus.addListener(this::registerKeyMappings);
            modEventBus.addListener(this::registerGuiOverlays);

            MinecraftForge.EVENT_BUS.addListener(this::onClientTick);

            ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                    (mc, parent) -> new ModernAdvancementsConfigScreen(parent)
                )
            );

            LOGGER.info("[Modern Advancements] Forge client initialized with HUD Pinning and Settings support.");
        }
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModernAdvancementsClient.OPEN_SETTINGS_KEY);
    }

    private void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(
            VanillaGuiOverlay.HOTBAR.id(),
            "pinned_advancements",
            (gui, graphics, partialTick, width, height) -> HUD.render(graphics, partialTick)
        );
    }

    private void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft client = Minecraft.getInstance();
            while (ModernAdvancementsClient.OPEN_SETTINGS_KEY.consumeClick()) {
                if (client != null) {
                    client.setScreen(new ModernAdvancementsConfigScreen(client.screen));
                }
            }
        }
    }
}
