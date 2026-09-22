package com.dmystery.fabric;

import com.dmystery.ModernAdvancements;
import net.fabricmc.api.ModInitializer;

public final class ModernAdvancementsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModernAdvancements.init();
    }
}
