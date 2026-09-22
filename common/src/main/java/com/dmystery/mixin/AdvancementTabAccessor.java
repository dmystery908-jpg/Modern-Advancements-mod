package com.dmystery.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AdvancementTab.class)
public interface AdvancementTabAccessor {
    @Accessor("widgets")
    Map<Advancement, AdvancementWidget> modernAdvancements$getWidgets();

    @Accessor("scrollX")
    double modernAdvancements$getScrollX();

    @Accessor("scrollX")
    void modernAdvancements$setScrollX(double x);

    @Accessor("scrollY")
    double modernAdvancements$getScrollY();

    @Accessor("scrollY")
    void modernAdvancements$setScrollY(double y);
}
