package com.dmystery.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvancementWidget.class)
public interface AdvancementWidgetAccessor {
    @Accessor("progress")
    AdvancementProgress modernAdvancements$getProgress();

    @Accessor("advancement")
    Advancement modernAdvancements$getAdvancement();

    @Accessor("display")
    DisplayInfo modernAdvancements$getDisplay();
}
