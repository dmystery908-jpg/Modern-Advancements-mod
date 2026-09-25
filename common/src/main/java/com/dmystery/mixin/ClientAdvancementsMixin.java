package com.dmystery.mixin;

import com.dmystery.client.AdvancementCache;
import com.dmystery.client.AdvancementDataLoader;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientAdvancements.class)
public class ClientAdvancementsMixin {
    @Shadow @Nullable private ClientAdvancements.Listener listener;

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdatePacket(ClientboundUpdateAdvancementsPacket packet, CallbackInfo ci) {
        AdvancementDataLoader.ensureAdvancementsLoaded((ClientAdvancements) (Object) this);
        AdvancementCache.markDirty();
        if (this.listener != null) {
            this.listener.onAdvancementsUpdated();
        }
    }
}
