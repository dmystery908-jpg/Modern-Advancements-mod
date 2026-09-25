package com.dmystery.client;

import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import org.jetbrains.annotations.Nullable;

public interface AdvancementTabExtension {
    @Nullable
    AdvancementWidget modernAdvancements$getHovered();

    void modernAdvancements$setHovered(@Nullable AdvancementWidget widget);
}
