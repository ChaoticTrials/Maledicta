package de.melanx.maledicta.mixin;

import de.melanx.maledicta.client.DarkSky;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.WeatherAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Inject(method = "addEnvironmentAttributeLayers", at = @At("RETURN"))
    private void maledicta$addDarkSkyLayers(EnvironmentAttributeSystem.Builder environmentAttributes, CallbackInfoReturnable<EnvironmentAttributeSystem.Builder> cir) {
        WeatherAttributes.addBuiltinLayers(cir.getReturnValue(), DarkSky.WEATHER);
    }
}
