package com.dmystery;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class ModernAdvancementsClient {
    public static final KeyMapping OPEN_SETTINGS_KEY = new KeyMapping(
        "key.modern_advancements.open_settings",
        InputConstants.Type.KEYSYM,
        InputConstants.UNKNOWN.getValue(),
        "key.categories.modern_advancements"
    );

    public static void init() {
    }
}
