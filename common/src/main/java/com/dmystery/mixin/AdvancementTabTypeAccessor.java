package com.dmystery.mixin;

import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvancementTabType.class)
public interface AdvancementTabTypeAccessor {
    @Accessor("width")
    int modernAdvancements$getWidth();

    @Accessor("height")
    int modernAdvancements$getHeight();
}
