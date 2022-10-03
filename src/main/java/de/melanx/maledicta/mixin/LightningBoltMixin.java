package de.melanx.maledicta.mixin;

import de.melanx.maledicta.lightning.LightningHelper;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
public class LightningBoltMixin {

    @Inject(method = "defineSynchedData", at = @At(value = "HEAD"))
    private void defineSyncedData(CallbackInfo ci) {
        ((LightningBolt) (Object) this).getEntityData().define(LightningHelper.COLOR_ACCESSOR, "");
    }
}
