package de.melanx.maledicta.mixin;

import de.melanx.maledicta.ModConfig;
import de.melanx.maledicta.blocks.MaledictusAufero;
import de.melanx.maledicta.lightning.LightningHelper;
import de.melanx.maledicta.registration.ModBlocks;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
public class LightningBoltMixin {

    @Inject(method = "defineSynchedData", at = @At(value = "HEAD"))
    private void defineSyncedData(CallbackInfo ci) {
        LightningBolt lightning = (LightningBolt) (Object) this;
        lightning.getEntityData().define(LightningHelper.COLOR_ACCESSOR, -1);
        lightning.getEntityData().define(LightningHelper.CURSED_ACCESSOR, false);
    }

    @Redirect(method = "powerLightningRod", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean powerCursedRod(BlockState state, Block block) {
        if (state.is(ModBlocks.maledictusAufero)) {
            LightningBolt lightning = (LightningBolt) (Object) this;
            lightning.setVisualOnly(ModConfig.safeLightnings);
            ((MaledictusAufero) state.getBlock()).onLightningStrike(state, lightning.level(), lightning.getStrikePosition());
            return false;
        }

        return state.is(Blocks.LIGHTNING_ROD);
    }
}
