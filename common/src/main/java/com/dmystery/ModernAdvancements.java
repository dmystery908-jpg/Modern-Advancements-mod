package com.dmystery;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModernAdvancements {
    public static final String MOD_ID = "modern_advancements";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static void init() {
        LOGGER.info("[Modern Advancements] Common initialized.");
    }
}
