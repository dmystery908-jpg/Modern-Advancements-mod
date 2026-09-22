package com.dmystery;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class ModernAdvancementsClient {
    public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
        ModernAdvancements.id("key_category")
    );

    public static final KeyMapping OPEN_SETTINGS_KEY = new KeyMapping(
        "key.modern_advancements.open_settings",
        InputConstants.Type.KEYSYM,
        InputConstants.UNKNOWN.getValue(),
        KEY_CATEGORY
    );

    public static void init() {
    }
}
